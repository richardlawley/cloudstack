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
import org.apache.cloudstack.storage.datastore.util.SolidFireUtil;
import org.springframework.stereotype.Component;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.user.Account;
import com.cloud.user.AccountDetailVO;
import com.cloud.user.AccountDetailsDao;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.User;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.exception.CloudRuntimeException;

@Component
@Local(value = APIChecker.class)
public class ApiSolidFireServiceImpl2 extends AdapterBase implements APIChecker, ApiSolidFireService2 {
    private static final Logger s_logger = Logger.getLogger(ApiSolidFireServiceImpl2.class);
    private static final int s_lockTimeInSeconds = 180;

    @Inject private AccountDao _accountDao;
    @Inject private AccountDetailsDao _accountDetailsDao;
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

        verifyClusterQuotas(totalCapacity, totalMinIops, totalMaxIops, totalBurstIops);

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

        List<SfVirtualNetworkVO> sfVirtualNetworks = _sfVirtualNetworkDao.findByClusterId(sfCluster.getId());

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

        verifyPermissionsForAccount(sfVolume.getAccountId());

        return sfVolume;
    }

    public List<SfVolume> listSolidFireVolumes() {
        s_logger.info("listSolidFireVolumes invoked");

        List<SfVolume> sfVolumes = new ArrayList<>();

        final List<SfVolumeVO> sfVolumeVOs;

        if (isRootAdmin()) {
            sfVolumeVOs = _sfVolumeDao.listAll();
        }
        else {
            sfVolumeVOs = _sfVolumeDao.findByAccountId(getCallingAccount().getId());
        }

        if (sfVolumeVOs != null) {
            sfVolumes.addAll(sfVolumeVOs);
        }

        return sfVolumes;
    }

    /** @todo Mike T. make use of account-level limits, too **/
    /** @todo Mike T. allow the user to specify the VLAN **/
    @Override
    public SfVolume createSolidFireVolume(String name, long size, long minIops, long maxIops, long burstIops, long accountId, long zoneId) {
        s_logger.info("createSolidFireVolume invoked");

        verifyPermissionsForAccount(accountId);

        List<SfClusterVO> sfClusterVOs = _sfClusterDao.findByZoneId(zoneId);

        if (sfClusterVOs == null || sfClusterVOs.size() <= 0) {
            throw new CloudRuntimeException("Unable to find any storage clusters in the following zone: " + zoneId);
        }

        SfVolume sfVolume = null;

        for (SfCluster sfCluster : sfClusterVOs) {
            if ((sfVolume = createVolume(sfCluster, name, size, minIops, maxIops, burstIops, accountId)) != null) {
                return sfVolume;
            }
        }

        throw new CloudRuntimeException("Unable to find applicable storage to house this volume");
    }

    @Override
    public SfVolume updateSolidFireVolume(long id, long size, long minIops, long maxIops, long burstIops) {
        s_logger.info("updateSolidFireVolume invoked");

        SfVolumeVO sfVolumeVO = getSfVolume(id);

        verifyPermissionsForAccount(sfVolumeVO.getAccountId());

        if ((sfVolumeVO = updateVolume(sfVolumeVO, size, minIops, maxIops, burstIops)) != null) {
            return sfVolumeVO;
        }

        throw new CloudRuntimeException("Unable to update the volume with the following id: " + id);
    }

    @Override
    public SfVolume deleteSolidFireVolume(long id) {
        s_logger.info("deleteSolidFireVolume invoked");

        SfVolume sfVolume = getSfVolume(id);

        verifyPermissionsForAccount(sfVolume.getAccountId());

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

    private void verifyPermissionsForAccount(long accountId) {
        Account account = getCallingAccount();

        if (isRootAdmin(account.getId())) {
            return; // permissions OK
        }

        if (accountId == account.getId()) {
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

    private SfVolume createVolume(SfCluster sfCluster, String name, long size, long minIops, long maxIops, long burstIops, long accountId) {
        GlobalLock lock = GlobalLock.getInternLock(sfCluster.getUuid());

        if (!lock.lock(s_lockTimeInSeconds)) {
            String errMsg = "Couldn't lock the DB on the following string: " + sfCluster.getUuid();

            s_logger.debug(errMsg);

            throw new CloudRuntimeException(errMsg);
        }

        try {
            TotalRemaining totalRemaining = getTotalRemaining(sfCluster);

            long totalRemainingCapacity = totalRemaining.getTotalRemainingCapacity() - size;
            long totalRemainingMinIops = totalRemaining.getTotalRemainingMinIops() - minIops;
            long totalRemainingMaxIops = totalRemaining.getTotalRemainingMaxIops() - maxIops;
            long totalRemainingBurstIops = totalRemaining.getTotalRemainingBurstIops() - burstIops;

            if (totalRemainingCapacity >= 0 && totalRemainingMinIops >= 0 && totalRemainingMaxIops >= 0 && totalRemainingBurstIops >= 0) {
                SolidFireConnection sfConnection = new SolidFireConnection(sfCluster.getMvip(), sfCluster.getUsername(), sfCluster.getPassword());

                long sfClusterId = sfCluster.getId();

                AccountDetailVO accountDetail = getAccountDetail(accountId, sfClusterId);

                if (accountDetail == null || accountDetail.getValue() == null) {
                    AccountVO account = _accountDao.findById(accountId);
                    String sfAccountName = getSolidFireAccountName(accountId, account.getUuid());
                    SolidFireConnection.SolidFireAccount sfAccount = sfConnection.getSolidFireAccount(sfAccountName);

                    if (sfAccount == null) {
                        sfAccount = createSolidFireAccount(sfConnection, sfAccountName);
                    }

                    updateCsDbWithSolidFireAccountInfo(account.getId(), sfAccount, sfClusterId);

                    accountDetail = getAccountDetail(accountId, sfClusterId);
                }

                long sfAccountId = Long.parseLong(accountDetail.getValue());

                long sfVolumeId = sfConnection.createVolume(name, sfAccountId, size, minIops, maxIops, burstIops);

                SfVolumeVO sfVolumeVO = new SfVolumeVO(sfVolumeId, name, size, minIops, maxIops, burstIops, accountId, sfClusterId);

                return _sfVolumeDao.persist(sfVolumeVO);
            }
        }
        finally {
            lock.unlock();
            lock.releaseRef();
        }

        return null;
    }

    private TotalRemaining getTotalRemaining(SfCluster sfCluster) {
        return getTotalRemaining(sfCluster, null);
    }

    private TotalRemaining getTotalRemaining(SfCluster sfCluster, SfVolume volumeToExclude) {
        long totalRemainingCapacity = sfCluster.getTotalCapacity();
        long totalRemainingMinIops = sfCluster.getTotalMinIops();
        long totalRemainingMaxIops = sfCluster.getTotalMaxIops();
        long totalRemainingBurstIops = sfCluster.getTotalBurstIops();

        List<SfVolumeVO> sfVolumeVOs = _sfVolumeDao.findByClusterId(sfCluster.getId());

        if (sfVolumeVOs != null) {
            for (SfVolume sfVolume : sfVolumeVOs) {
                if (volumeToExclude == null || sfVolume.getId() != volumeToExclude.getId()) {
                    totalRemainingCapacity -= sfVolume.getSize();
                    totalRemainingMinIops -= sfVolume.getMinIops();
                    totalRemainingMaxIops -= sfVolume.getMaxIops();
                    totalRemainingBurstIops -= sfVolume.getBurstIops();
                }
            }
        }

        return new TotalRemaining(totalRemainingCapacity, totalRemainingMinIops, totalRemainingMaxIops, totalRemainingBurstIops);
    }

    private static class TotalRemaining {
        private final long _totalRemainingCapacity;
        private final long _totalRemainingMinIops;
        private final long _totalRemainingMaxIops;
        private final long _totalRemainingBurstIops;

        public TotalRemaining(long totalRemainingCapacity, long totalRemainingMinIops, long totalRemainingMaxIops, long totalRemainingBurstIops) {
            _totalRemainingCapacity = totalRemainingCapacity;
            _totalRemainingMinIops = totalRemainingMinIops;
            _totalRemainingMaxIops = totalRemainingMaxIops;
            _totalRemainingBurstIops = totalRemainingBurstIops;
        }

        public long getTotalRemainingCapacity() {
            return _totalRemainingCapacity;
        }

        public long getTotalRemainingMinIops() {
            return _totalRemainingMinIops;
        }

        public long getTotalRemainingMaxIops() {
            return _totalRemainingMaxIops;
        }

        public long getTotalRemainingBurstIops() {
            return _totalRemainingBurstIops;
        }
    }

    private SfVolumeVO updateVolume(SfVolumeVO sfVolumeVO, long size, long minIops, long maxIops, long burstIops) {
        SfCluster sfCluster = getSfCluster(sfVolumeVO.getSfClusterId());

        GlobalLock lock = GlobalLock.getInternLock(sfCluster.getUuid());

        if (!lock.lock(s_lockTimeInSeconds)) {
            String errMsg = "Couldn't lock the DB on the following string: " + sfCluster.getUuid();

            s_logger.debug(errMsg);

            throw new CloudRuntimeException(errMsg);
        }

        try {
            TotalRemaining totalRemaining = getTotalRemaining(sfCluster, sfVolumeVO);

            long totalRemainingCapacity = totalRemaining.getTotalRemainingCapacity() - size;
            long totalRemainingMinIops = totalRemaining.getTotalRemainingMinIops() - minIops;
            long totalRemainingMaxIops = totalRemaining.getTotalRemainingMaxIops() - maxIops;
            long totalRemainingBurstIops = totalRemaining.getTotalRemainingBurstIops() - burstIops;

            if (totalRemainingCapacity >= 0 && totalRemainingMinIops >= 0 && totalRemainingMaxIops >= 0 && totalRemainingBurstIops >= 0) {
                SolidFireConnection sfConnection = new SolidFireConnection(sfCluster.getMvip(), sfCluster.getUsername(), sfCluster.getPassword());

                sfConnection.modifyVolume(sfVolumeVO.getSfId(), size, minIops, maxIops, burstIops);

                sfVolumeVO.setSize(size);
                sfVolumeVO.setMinIops(minIops);
                sfVolumeVO.setMaxIops(maxIops);
                sfVolumeVO.setBurstIops(burstIops);

                if (!_sfVolumeDao.update(sfVolumeVO.getId(), sfVolumeVO)) {
                    throw new CloudRuntimeException("Unable to update the following volume:" + sfVolumeVO.getId());
                }

                return sfVolumeVO;
            }
        }
        finally {
            lock.unlock();
            lock.releaseRef();
        }

        return null;
    }

    private static String getSolidFireAccountName(long accountId, String accountUuid) {
        return "CloudStack_" + accountId + "_" + accountUuid;
    }

    private static String getAccountKey(long sfClusterId) {
        return "sfAccountIdForClusterId_" + sfClusterId;
    }

    private AccountDetailVO getAccountDetail(long accountId, long sfClusterId) {
        return _accountDetailsDao.findDetail(accountId, getAccountKey(sfClusterId));
    }

    private SolidFireConnection.SolidFireAccount createSolidFireAccount(SolidFireConnection sfConnection, String sfAccountName) {
        long accountNumber = sfConnection.createSolidFireAccount(sfAccountName);

        return sfConnection.getSolidFireAccountById(accountNumber);
    }

    private void updateCsDbWithSolidFireAccountInfo(long accountId, SolidFireConnection.SolidFireAccount sfAccount, long sfClusterId) {
        AccountDetailVO accountDetail = new AccountDetailVO(accountId,
                getAccountKey(sfClusterId),
                String.valueOf(sfAccount.getId()));

        _accountDetailsDao.persist(accountDetail);

        accountDetail = new AccountDetailVO(accountId,
                SolidFireUtil.CHAP_INITIATOR_USERNAME,
                sfAccount.getName());

        _accountDetailsDao.persist(accountDetail);

        accountDetail = new AccountDetailVO(accountId,
                SolidFireUtil.CHAP_INITIATOR_SECRET,
                sfAccount.getInitiatorSecret());

        _accountDetailsDao.persist(accountDetail);

        accountDetail = new AccountDetailVO(accountId,
                SolidFireUtil.CHAP_TARGET_USERNAME,
                sfAccount.getName());

        _accountDetailsDao.persist(accountDetail);

        accountDetail = new AccountDetailVO(accountId,
                SolidFireUtil.CHAP_TARGET_SECRET,
                sfAccount.getTargetSecret());

        _accountDetailsDao.persist(accountDetail);
    }
}
