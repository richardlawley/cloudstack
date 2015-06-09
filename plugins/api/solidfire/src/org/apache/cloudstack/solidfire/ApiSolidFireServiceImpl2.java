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
import org.apache.cloudstack.api.command.admin.solidfire.CreateSolidFireVolumeCmd;
import org.apache.cloudstack.api.command.admin.solidfire.DeleteReferenceToSolidFireClusterCmd;
import org.apache.cloudstack.api.command.admin.solidfire.DeleteSolidFireVirtualNetworkCmd;
import org.apache.cloudstack.api.command.admin.solidfire.DeleteSolidFireVolumeCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireClusterCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireClustersCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireVirtualNetworkCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireVirtualNetworksCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireVolumeCmd;
import org.apache.cloudstack.api.command.admin.solidfire.ListSolidFireVolumesCmd;
import org.apache.cloudstack.api.command.admin.solidfire.UpdateReferenceToSolidFireClusterCmd;
import org.apache.cloudstack.api.command.admin.solidfire.UpdateSolidFireVirtualNetworkCmd;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.solidfire.dataaccess.SfCluster;
import org.apache.cloudstack.solidfire.dataaccess.SfVirtualNetwork;
import org.apache.cloudstack.solidfire.dataaccess.SfVolume;
import org.apache.cloudstack.solidfire.dataaccess.dao.SfClusterDao;
import org.apache.cloudstack.solidfire.dataaccess.dao.SfVirtualNetworkDao;
import org.apache.cloudstack.solidfire.dataaccess.dao.SfVolumeDao;
import org.apache.cloudstack.solidfire.dataaccess.vo.SfClusterVO;
import org.apache.cloudstack.solidfire.dataaccess.vo.SfVirtualNetworkVO;
import org.apache.cloudstack.solidfire.dataaccess.vo.SfVolumeVO;
import org.apache.cloudstack.solidfire.util.SolidFireConnection;
import org.springframework.stereotype.Component;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.User;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.exception.CloudRuntimeException;

@Component
@Local(value = APIChecker.class)
public class ApiSolidFireServiceImpl2 extends AdapterBase implements APIChecker, ApiSolidFireService2 {
    private static final Logger s_logger = Logger.getLogger(ApiSolidFireServiceImpl2.class);

    @Inject private AccountDao _accountDao;
    @Inject private AccountManager _accountMgr;
    @Inject private DataCenterDao _zoneDao;
    @Inject private SfClusterDao _sfClusterDao;
    @Inject private SfVirtualNetworkDao _sfVirtualNetworkDao;
    @Inject private SfVolumeDao _sfVolumeDao;

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

        verifyClusterQuotas(totalCapacity, totalMinIops, totalMaxIops, totalBurstIops);

        verifyZone(zoneId);

        SolidFireConnection sfConnection = new SolidFireConnection(mvip, username, password);

        String clusterName = sfConnection.getClusterName();

        SfClusterVO sfClusterVO = new SfClusterVO(clusterName, mvip, username, password, totalCapacity, totalMinIops, totalMaxIops, totalBurstIops, zoneId);

        return _sfClusterDao.persist(sfClusterVO);
    }

    @Override
    public SfCluster updateReferenceToSolidFireCluster(String clusterName, long totalCapacity,
            long totalMinIops, long totalMaxIops, long totalBurstIops) {
        s_logger.info("updateReferenceToSolidFireCluster invoked");

        verifyRootAdmin();

        SfClusterVO sfClusterVO = getSfCluster(clusterName);

        sfClusterVO.setTotalCapacity(totalCapacity);
        sfClusterVO.setTotalMinIops(totalMinIops);
        sfClusterVO.setTotalMaxIops(totalMaxIops);
        sfClusterVO.setTotalBurstIops(totalBurstIops);

        if (_sfClusterDao.update(sfClusterVO.getId(), sfClusterVO)) {
            return sfClusterVO;
        }

        throw new CloudRuntimeException("Unable to update the cluster table");
    }

    @Override
    public SfCluster deleteReferenceToSolidFireCluster(String clusterName) {
        s_logger.info("deleteReferenceToSolidFireCluster invoked");

        verifyRootAdmin();

        SfCluster sfCluster = getSfCluster(clusterName);

        List<SfVirtualNetworkVO> sfVirtualNetworks = _sfVirtualNetworkDao.findByCluster(sfCluster.getId());

        if (sfVirtualNetworks == null || sfVirtualNetworks.size() <= 0) {
            throw new CloudRuntimeException("Unable to delete a cluster that has one or more virtual networks");
        }

        List<SfVolumeVO> sfVolumes = _sfVolumeDao.findByClusterId(sfCluster.getId());

        if (sfVolumes == null || sfVolumes.size() <= 0) {
            throw new CloudRuntimeException("Unable to delete a cluster that has one or more volumes");
        }

        if (!_sfClusterDao.remove(sfCluster.getId())) {
            throw new CloudRuntimeException("Unable to remove the following cluster: " + clusterName);
        }

        return sfCluster;
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

        verifyAccount(accountId);

        SfCluster sfCluster = getSfCluster(clusterName);

        SolidFireConnection sfConnection = new SolidFireConnection(sfCluster.getMvip(), sfCluster.getUsername(), sfCluster.getPassword());

        long sfVirtualNetworkId = sfConnection.createVirtualNetwork(name, tag, startIp, size, netmask, svip);

        SfVirtualNetworkVO sfVirtualNetworkVO = new SfVirtualNetworkVO(sfVirtualNetworkId, name, tag, startIp, size, netmask, svip, accountId, sfCluster.getId());

        return _sfVirtualNetworkDao.persist(sfVirtualNetworkVO);
    }

    @Override
    public SfVirtualNetwork updateSolidFireVirtualNetwork(long id, String name, String tag, String startIp, int size,
            String netmask, String svip) {
        s_logger.info("updateSolidFireVirtualNetwork invoked");

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

        throw new CloudRuntimeException("Unable to update the virtual network table");
    }

    @Override
    public SfVirtualNetwork deleteSolidFireVirtualNetwork(long id) {
        s_logger.info("deleteSolidFireVirtualNetwork invoked");

        verifyRootAdmin();

        SfVirtualNetwork sfVirtualNetwork = getSfVirtualNetwork(id);

        if (!_sfVirtualNetworkDao.remove(id)) {
            throw new CloudRuntimeException("Unable to remove the following virtual network:" + id);
        }

        SfCluster sfCluster = getSfCluster(sfVirtualNetwork.getSfClusterId());

        SolidFireConnection sfConnection = new SolidFireConnection(sfCluster.getMvip(), sfCluster.getUsername(), sfCluster.getPassword());

        sfConnection.deleteVirtualNetwork(sfVirtualNetwork.getSfId());

        return sfVirtualNetwork;
    }

    public SfVolume listSolidFireVolume(long id) {
        s_logger.info("listSolidFireVolume invoked");

        SfVolume sfVolume = getSfVolume(id);

        verifyPermissionsForVolume(sfVolume);

        return sfVolume;
    }

    public List<SfVolume> listSolidFireVolumes() {
        s_logger.info("listSolidFireVolumes invoked");

        List<SfVolume> sfVolumes = new ArrayList<>();

        if (isRootAdmin()) {
            List<SfVolumeVO> sfVolumeVOs = _sfVolumeDao.listAll();

            if (sfVolumeVOs != null) {
                sfVolumes.addAll(sfVolumeVOs);
            }
        }
        else {
            List<SfVolumeVO> sfVolumeVOs = _sfVolumeDao.findByAccountId(getCallingAccount().getId());

            if (sfVolumeVOs != null) {
                sfVolumes.addAll(sfVolumeVOs);
            }
        }

        return sfVolumes;
    }

    /** @todo Mike T. make use of account-level limits, too **/
    /** @todo Mike T. synchronization **/
    /** @todo Mike T. allow the user to specify the VLAN **/
    @Override
    public SfVolume createSolidFireVolume(String name, long size, long minIops, long maxIops, long burstIops, long zoneId, long accountId) {
        s_logger.info("createSolidFireVolume invoked");

        List<SfClusterVO> sfClusterVOs = _sfClusterDao.listAll();

        if (sfClusterVOs == null || sfClusterVOs.size() <= 0) {
            throw new CloudRuntimeException("Unable to find any storage clusters");
        }

        for (SfCluster sfCluster : sfClusterVOs) {
            if (sfCluster.getZoneId() != zoneId) {
                continue;
            }

            List<SfVolumeVO> sfVolumeVOs = _sfVolumeDao.findByClusterId(sfCluster.getId());

            long totalRemainingCapacity = sfCluster.getTotalCapacity();
            long totalRemainingMinIops = sfCluster.getTotalMinIops();
            long totalRemainingMaxIops = sfCluster.getTotalMaxIops();
            long totalRemainingBurstIops = sfCluster.getTotalBurstIops();

            if (sfVolumeVOs != null) {
                for (SfVolume sfVolume : sfVolumeVOs) {
                    totalRemainingCapacity -= sfVolume.getSize();
                    totalRemainingMinIops -= sfVolume.getMinIops();
                    totalRemainingMaxIops -= sfVolume.getMaxIops();
                    totalRemainingBurstIops -= sfVolume.getBurstIops();
                }
            }

            totalRemainingCapacity -= size;
            totalRemainingMinIops -= minIops;
            totalRemainingMaxIops -= maxIops;
            totalRemainingBurstIops -= burstIops;

            if (totalRemainingCapacity >= 0 && totalRemainingMinIops >= 0 && totalRemainingMaxIops >= 0 && totalRemainingBurstIops >= 0) {
                SolidFireConnection sfConnection = new SolidFireConnection(sfCluster.getMvip(), sfCluster.getUsername(), sfCluster.getPassword());

                /** @todo Mike T. create an account, if necessary */
                long sfVolumeId = sfConnection.createVolume(name, 0, size, minIops, maxIops, burstIops);

                SfVolumeVO sfVolumeVO = new SfVolumeVO(sfVolumeId, name, size, minIops, maxIops, burstIops, accountId, sfCluster.getId());

                return _sfVolumeDao.persist(sfVolumeVO);
            }
        }

        throw new CloudRuntimeException("Unable to find applicable storage to house this volume");
    }

    @Override
    public SfVolume updateSolidFireVolume(long id, long size, long minIops, long maxIops, long burstIops) {
        s_logger.info("modifySolidFireVolume invoked");

        SfVolumeVO sfVolumeVO = getSfVolume(id);

        verifyPermissionsForVolume(sfVolumeVO);

        SfCluster sfCluster = getSfCluster(sfVolumeVO.getSfClusterId());

        SolidFireConnection sfConnection = new SolidFireConnection(sfCluster.getMvip(), sfCluster.getUsername(), sfCluster.getPassword());

        /** @todo Mike T. must figure out if this exceeds any quotas */
        sfConnection.modifyVolume(sfVolumeVO.getSfId(), size, minIops, maxIops, burstIops);

        sfVolumeVO.setSize(size);
        sfVolumeVO.setMinIops(minIops);
        sfVolumeVO.setMaxIops(maxIops);
        sfVolumeVO.setBurstIops(burstIops);

        if (!_sfVolumeDao.update(id, sfVolumeVO)) {
            throw new CloudRuntimeException("Unable to update the following volume:" + id);
        }

        return sfVolumeVO;
    }

    @Override
    public SfVolume deleteSolidFireVolume(long id) {
        s_logger.info("deleteSolidFireVolume invoked");

        SfVolume sfVolume = getSfVolume(id);

        verifyPermissionsForVolume(sfVolume);

        if (!_sfVolumeDao.remove(id)) {
            throw new CloudRuntimeException("Unable to remove the following volume:" + id);
        }

        SfCluster sfCluster = getSfCluster(sfVolume.getSfClusterId());

        SolidFireConnection sfConnection = new SolidFireConnection(sfCluster.getMvip(), sfCluster.getUsername(), sfCluster.getPassword());

        sfConnection.deleteVolume(sfVolume.getSfId());

        return sfVolume;
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
        cmdList.add(UpdateReferenceToSolidFireClusterCmd.class);
        cmdList.add(DeleteReferenceToSolidFireClusterCmd.class);

        cmdList.add(ListSolidFireVirtualNetworkCmd.class);
        cmdList.add(ListSolidFireVirtualNetworksCmd.class);
        cmdList.add(CreateSolidFireVirtualNetworkCmd.class);
        cmdList.add(UpdateSolidFireVirtualNetworkCmd.class);
        cmdList.add(DeleteSolidFireVirtualNetworkCmd.class);

        cmdList.add(ListSolidFireVolumeCmd.class);
        cmdList.add(ListSolidFireVolumesCmd.class);
        cmdList.add(CreateSolidFireVolumeCmd.class);
        cmdList.add(DeleteSolidFireVolumeCmd.class);

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

    private SfVolumeVO getSfVolume(long id) {
        SfVolumeVO sfVolumeVO = _sfVolumeDao.findById(id);

        if (sfVolumeVO == null) {
            throw new CloudRuntimeException("The SolidFire volume with the following ID cannot be located in the database: '" + id + "'.");
        }

        return sfVolumeVO;
    }

    private Account getCallingAccount() {
        Account account = CallContext.current().getCallingAccount();

        if (account == null) {
            throw new CloudRuntimeException("The user's account cannot be determined.");
        }

        return account;
    }

    private boolean isRootAdmin() {
        Account account = getCallingAccount();

        return isRootAdmin(account.getId());
    }

    private boolean isRootAdmin(long accountId) {
        return _accountMgr.isRootAdmin(accountId);
    }

    private void verifyRootAdmin() {
        if (!isRootAdmin()) {
            throw new PermissionDeniedException("Only a root admin can perform this operation.");
        }
    }

    private void verifyPermissionsForVolume(SfVolume sfVolume) {
        Account account = getCallingAccount();

        if (isRootAdmin(account.getId())) {
            return; // permissions OK
        }

        if (sfVolume.getAccountId() == account.getId()) {
            return; // permissions OK
        }

        throw new PermissionDeniedException("Only a root admin or a user of the owning account can perform this operation.");
    }

    private void verifyClusterQuotas(long totalCapacity, long totalMinIops, long totalMaxIops, long totalBurstIops) {
        if (totalCapacity < 0) {
            throw new CloudRuntimeException("The total capacity of the cluster must be a positive whole number.");
        }

        if (totalMinIops < 0) {
            throw new CloudRuntimeException("The total minimum IOPS of the cluster must be a positive whole number.");
        }

        if (totalMaxIops < 0) {
            throw new CloudRuntimeException("The total maximum IOPS of the cluster must be a positive whole number.");
        }

        if (totalBurstIops < 0) {
            throw new CloudRuntimeException("The total burst IOPS of the cluster must be a positive whole number.");
        }

        if (totalMinIops > totalMaxIops) {
            throw new CloudRuntimeException("The total minimum IOPS of the cluster must be less than or equal to the total maximum IOPS of the cluster.");
        }

        if (totalMaxIops > totalBurstIops) {
            throw new CloudRuntimeException("The total maximum IOPS of the cluster must be less than or equal to the total burst IOPS of the cluster.");
        }
    }

    private void verifyAccount(long accountId) {
        Account account = _accountDao.findById(accountId);

        if (account == null) {
            throw new CloudRuntimeException("Unable to locate the following account: " + accountId);
        }
     }

    private void verifyZone(long zoneId) {
       DataCenterVO dataCenterVO = _zoneDao.findById(zoneId);

       if (dataCenterVO == null) {
           throw new CloudRuntimeException("Unable to locate the following zone: " + zoneId);
       }
    }
}
