// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.solidfire;

import java.util.List;
import java.util.ArrayList;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.cloudstack.acl.APIChecker;
import org.apache.cloudstack.api.command.admin.solidfire.CreateReferenceToSolidFireClusterCmd;
import org.apache.cloudstack.api.command.admin.solidfire.CreateSolidFireVirtualNetworkCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireClusterCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireClustersCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireVirtualNetworkCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireVirtualNetworksCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ModifyReferenceToSolidFireClusterCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ModifySolidFireVirtualNetworkCmd;
import org.apache.cloudstack.api.response.ApiSolidFireClusterResponse;
import org.apache.cloudstack.api.response.ApiSolidFireVirtualNetworkResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.solidfire.dataaccess.SfCluster;
import org.apache.cloudstack.solidfire.dataaccess.SfVirtualNetwork;
import org.apache.cloudstack.solidfire.dataaccess.dao.SfClusterDao;
import org.apache.cloudstack.solidfire.dataaccess.dao.SfVirtualNetworkDao;
import org.apache.cloudstack.solidfire.dataaccess.vo.SfClusterVO;
import org.apache.cloudstack.solidfire.dataaccess.vo.SfVirtualNetworkVO;
import org.apache.cloudstack.solidfire.util.SolidFireConnection;
import org.springframework.stereotype.Component;

import com.cloud.exception.PermissionDeniedException;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.User;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.exception.CloudRuntimeException;

@Component
@Local(value = APIChecker.class)
public class ApiSolidFireServiceImpl2 extends AdapterBase implements APIChecker, ApiSolidFireService2 {
    private static final Logger s_logger = Logger.getLogger(ApiSolidFireServiceImpl2.class);

    @Inject private AccountManager _accountMgr;
    @Inject private SfClusterDao _sfClusterDao;
    @Inject private SfVirtualNetworkDao _sfVirtualNetworkDao;

    @Override
    public SfCluster listSolidFireCluster(String clusterName) {
        s_logger.info("listSolidFireCluster invoked");

        verifyRootAdmin();

        return getSfCluster(clusterName);
    }

    @Override
    public List<SfCluster> listSolidFireClusters() {
        s_logger.info("listSolidFireClusters invoked");

        verifyRootAdmin();

        List<SfCluster> sfClusters = new ArrayList<>();

        List<SfClusterVO> sfClusterVOs = _sfClusterDao.listAll();

        if (sfClusterVOs != null) {
            sfClusters.addAll(sfClusterVOs);
        }

        return sfClusters;
    }

    @Override
    public SfCluster createReferenceToSolidFireCluster(String mvip, String username, String password, long totalCapacity,
            long totalMinIops, long totalMaxIops, long totalBurstIops, long zoneId) {
        s_logger.info("createReferenceToSolidFireCluster invoked");

        verifyRootAdmin();

        SolidFireConnection sfConnection = new SolidFireConnection(mvip, username, password);

        String clusterName = sfConnection.getClusterName();

        SfClusterVO sfClusterVO = new SfClusterVO(clusterName, mvip, username, password, totalCapacity, totalMinIops, totalMaxIops, totalBurstIops, zoneId);

        sfClusterVO = _sfClusterDao.persist(sfClusterVO);

        return sfClusterVO;
    }

    @Override
    public SfCluster modifyReferenceToSolidFireCluster(String clusterName, long totalCapacity,
            long totalMinIops, long totalMaxIops, long totalBurstIops) {
        s_logger.info("modifyReferenceToSolidFireCluster invoked");

        verifyRootAdmin();

        SfClusterVO sfClusterVO = getSfCluster(clusterName);

        sfClusterVO.setTotalCapacity(totalCapacity);
        sfClusterVO.setTotalMinIops(totalMinIops);
        sfClusterVO.setTotalMaxIops(totalMaxIops);
        sfClusterVO.setTotalBurstIops(totalBurstIops);

        if (_sfClusterDao.update(sfClusterVO.getId(), sfClusterVO)) {
            return sfClusterVO;
        }

        throw new CloudRuntimeException("Unable to update the cluster table.");
    }

    @Override
    public SfVirtualNetwork listSolidFireVirtualNetwork(long id) {
        s_logger.info("listSolidFireVirtualNetwork invoked");

        verifyRootAdmin();

        return getSfVirtualNetwork(id);
    }

    @Override
    public List<SfVirtualNetwork> listSolidFireVirtualNetworks() {
        s_logger.info("listSolidFireVirtualNetworks invoked");

        verifyRootAdmin();

        List<SfVirtualNetwork> sfVirtualNetworks = new ArrayList<>();

        List<SfVirtualNetworkVO> sfVirtualNetworkVOs = _sfVirtualNetworkDao.listAll();

        if (sfVirtualNetworkVOs != null) {
            sfVirtualNetworks.addAll(sfVirtualNetworkVOs);
        }

        return sfVirtualNetworks;
    }

    @Override
    public SfVirtualNetwork createSolidFireVirtualNetwork(String clusterName, String name, String tag, String startIp, int size,
            String netmask, String svip, long accountId) {
        s_logger.info("createSolidFireVirtualNetwork invoked");

        verifyRootAdmin();

        SfClusterVO sfClusterVO = getSfCluster(clusterName);

        SolidFireConnection sfConnection = new SolidFireConnection(sfClusterVO.getMvip(), sfClusterVO.getUsername(), sfClusterVO.getPassword());

        long sfVirtualNetworkId = sfConnection.createVirtualNetwork(name, tag, startIp, size, netmask, svip);

        SfVirtualNetworkVO sfVirtualNetworkVO = new SfVirtualNetworkVO(sfVirtualNetworkId, name, tag, startIp, size, netmask, svip, accountId, sfClusterVO.getId());

        sfVirtualNetworkVO = _sfVirtualNetworkDao.persist(sfVirtualNetworkVO);

        return sfVirtualNetworkVO;
    }

    @Override
    public SfVirtualNetwork modifySolidFireVirtualNetwork(long id, String name, String tag, String startIp, int size,
            String netmask, String svip) {
        s_logger.info("modifySolidFireVirtualNetwork invoked");

        verifyRootAdmin();

        SfVirtualNetworkVO sfVirtualNetworkVO = getSfVirtualNetwork(id);

        long sfClusterId = sfVirtualNetworkVO.getSfClusterId();

        SfClusterVO sfClusterVO = getSfCluster(sfClusterId);

        SolidFireConnection sfConnection = new SolidFireConnection(sfClusterVO.getMvip(), sfClusterVO.getUsername(), sfClusterVO.getPassword());

        sfConnection.modifyVirtualNetwork(sfVirtualNetworkVO.getSfId(), name, tag, startIp, size, netmask, svip);

        sfVirtualNetworkVO = new SfVirtualNetworkVO(sfVirtualNetworkVO.getId(), name, tag, startIp, size, netmask, svip,
                sfVirtualNetworkVO.getAccountId(), sfClusterId);

        if (_sfVirtualNetworkDao.update(sfVirtualNetworkVO.getId(), sfVirtualNetworkVO)) {
            return sfVirtualNetworkVO;
        }

        throw new CloudRuntimeException("Unable to update the virtual network table.");
    }

    @Override
    public ApiSolidFireClusterResponse getApiSolidFireClusterResponse(SfCluster sfCluster) {
        ApiSolidFireClusterResponse sfResponse = new ApiSolidFireClusterResponse();

        sfResponse.setId(sfCluster.getId());
        sfResponse.setUuid(sfCluster.getUuid());
        sfResponse.setName(sfCluster.getName());
        sfResponse.setMvip(sfCluster.getMvip());
        sfResponse.setUsername(sfCluster.getUsername());
        sfResponse.setTotalCapacity(sfCluster.getTotalCapacity());
        sfResponse.setTotalMinIops(sfCluster.getTotalMinIops());
        sfResponse.setTotalMaxIops(sfCluster.getTotalMaxIops());
        sfResponse.setTotalBurstIops(sfCluster.getTotalBurstIops());
        sfResponse.setZoneId(sfCluster.getDataCenterId());

        return sfResponse;
    }

    @Override
    public List<ApiSolidFireClusterResponse> getApiSolidFireClusterResponse(List<SfCluster> sfClusters) {
        List<ApiSolidFireClusterResponse> sfResponse = new ArrayList<>();

        if (sfClusters != null) {
            for (SfCluster sfCluster : sfClusters) {
                ApiSolidFireClusterResponse response = getApiSolidFireClusterResponse(sfCluster);

                sfResponse.add(response);
            }
        }

        return sfResponse;
    }

    @Override
    public ApiSolidFireVirtualNetworkResponse getApiSolidFireVirtualNetworkResponse(SfVirtualNetwork sfVirtualNetwork) {
        ApiSolidFireVirtualNetworkResponse sfResponse = new ApiSolidFireVirtualNetworkResponse();

        sfResponse.setId(sfVirtualNetwork.getId());
        sfResponse.setUuid(sfVirtualNetwork.getUuid());
        sfResponse.setName(sfVirtualNetwork.getName());
        sfResponse.setTag(sfVirtualNetwork.getTag());
        sfResponse.setStartIp(sfVirtualNetwork.getStartIp());
        sfResponse.setSize(sfVirtualNetwork.getSize());
        sfResponse.setNetmask(sfVirtualNetwork.getNetmask());
        sfResponse.setSvip(sfVirtualNetwork.getSvip());
        sfResponse.setAccountId(sfVirtualNetwork.getAccountId());

        return sfResponse;
    }

    @Override
    public List<ApiSolidFireVirtualNetworkResponse> getApiSolidFireVirtualNetworkResponse(List<SfVirtualNetwork> sfVirtualNetworks) {
        List<ApiSolidFireVirtualNetworkResponse> sfResponse = new ArrayList<>();

        if (sfVirtualNetworks != null) {
            for (SfVirtualNetwork sfVirtualNetwork : sfVirtualNetworks) {
                ApiSolidFireVirtualNetworkResponse response = getApiSolidFireVirtualNetworkResponse(sfVirtualNetwork);

                sfResponse.add(response);
            }
        }

        return sfResponse;
    }

    @Override
    public boolean checkAccess(User user, String apiCommandName) throws PermissionDeniedException {
        if (_accountMgr.isRootAdmin(user.getAccountId())) {
            return true;
        }

        // substitute these commands with ones that a non-root admin can do and return true instead
        if ("listSolidFireCluster".equals(apiCommandName) ||
            "listSolidFireClusters".equals(apiCommandName)) {
            return false;
        }

        return false;
    }

    @Override
    public List<Class<?>> getCommands() {
        List<Class<?>> cmdList = new ArrayList<Class<?>>();

        cmdList.add(ListSolidFireClusterCmd.class);
        cmdList.add(ListSolidFireClustersCmd.class);
        cmdList.add(CreateReferenceToSolidFireClusterCmd.class);
        cmdList.add(ModifyReferenceToSolidFireClusterCmd.class);
        cmdList.add(ListSolidFireVirtualNetworkCmd.class);
        cmdList.add(ListSolidFireVirtualNetworksCmd.class);
        cmdList.add(CreateSolidFireVirtualNetworkCmd.class);
        cmdList.add(ModifySolidFireVirtualNetworkCmd.class);

        return cmdList;
    }

    private SfClusterVO getSfCluster(String clusterName) {
        SfClusterVO sfClusterVO = _sfClusterDao.findByName(clusterName);

        if (sfClusterVO == null) {
            throw new CloudRuntimeException("The following SolidFire cluster name cannot be located in the database: '" + clusterName + "'.");
        }

        return sfClusterVO;
    }

    private SfClusterVO getSfCluster(long sfClusterId) {
        SfClusterVO sfClusterVO = _sfClusterDao.findById(sfClusterId);

        if (sfClusterVO == null) {
            throw new CloudRuntimeException("The SolidFire cluster with the following ID cannot be located in the database: '" + sfClusterId + "'.");
        }

        return sfClusterVO;
    }

    private SfVirtualNetworkVO getSfVirtualNetwork(long id) {
        SfVirtualNetworkVO sfVirtualNetworkVO = _sfVirtualNetworkDao.findById(id);

        if (sfVirtualNetworkVO == null) {
            throw new CloudRuntimeException("The SolidFire VLAN with the following ID cannot be located in the database: '" + id + "'.");
        }

        return sfVirtualNetworkVO;
    }

    private void verifyRootAdmin() {
        Account account = CallContext.current().getCallingAccount();

        if (account == null) {
            throw new CloudRuntimeException("The user's account cannot be determined.");
        }

        if (!_accountMgr.isRootAdmin(account.getId())) {
            throw new PermissionDeniedException("Only a root admin can perform this operation.");
        }
    }
}
