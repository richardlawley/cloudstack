package org.apache.cloudstack.api.helper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.ApiSolidFireClusterResponse;
import org.apache.cloudstack.api.response.ApiSolidFireVirtualNetworkResponse;
import org.apache.cloudstack.api.response.ApiSolidFireVolumeResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.solidfire.dataaccess.SfCluster;
import org.apache.cloudstack.solidfire.dataaccess.SfVirtualNetwork;
import org.apache.cloudstack.solidfire.dataaccess.SfVolume;
import org.apache.cloudstack.solidfire.dataaccess.dao.SfClusterDao;
import org.apache.cloudstack.solidfire.dataaccess.dao.SfVirtualNetworkDao;
import org.apache.cloudstack.storage.datastore.util.SolidFireUtil;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.user.Account;
import com.cloud.user.AccountDetailVO;
import com.cloud.user.AccountDetailsDao;
import com.cloud.user.AccountManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.exception.CloudRuntimeException;

public class ApiHelper {
    private static ApiHelper s_apiHelperInstance = new ApiHelper();

    public static final String MVIP = "mvip";
    public static final String SVIP = "svip";
    public static final String CLUSTER_NAME = "clustername";
    public static final String NAME = "name";
    public static final String TAG = "tag";
    public static final String START_IP = "startip";
    public static final String SIZE = "size";
    public static final String NETMASK = "netmask";
    public static final String TOTAL_CAPACITY = "totalcapacity";
    public static final String TOTAL_MIN_IOPS = "totalminiops";
    public static final String TOTAL_MAX_IOPS = "totalmaxiops";
    public static final String TOTAL_BURST_IOPS = "totalburstiops";
    public static final String BURST_IOPS = "burstiops";
    public static final String SF_VIRTUAL_NETWORK_ID = "sfvirtualnetworkid";

    // descriptions
    public static final String SOLIDFIRE_CLUSTER_NAME_DESC = "SolidFire cluster name";
    public static final String SOLIDFIRE_MVIP_DESC = "SolidFire management virtual IP address";
    public static final String SOLIDFIRE_SVIP_DESC = "SolidFire storage virtual IP address for VLAN";
    public static final String SOLIDFIRE_USERNAME_DESC = "SolidFire cluster admin username";
    public static final String SOLIDFIRE_PASSWORD_DESC = "SolidFire cluster admin password";
    public static final String TOTAL_CAPACITY_DESC = "Total capacity (in GBs)";
    public static final String TOTAL_MIN_IOPS_DESC = "Total minimum IOPS";
    public static final String TOTAL_MAX_IOPS_DESC = "Total maximum IOPS";
    public static final String TOTAL_BURST_IOPS_DESC = "Total burst IOPS";
    public static final String SIZE_DESC = "Size (in GBs)";
    public static final String MIN_IOPS_DESC = "Min IOPS";
    public static final String MAX_IOPS_DESC = "Max IOPS";
    public static final String BURST_IOPS_DESC = "Burst IOPS";
    public static final String VIRTUAL_NETWORK_NAME_DESC = "VLAN name";
    public static final String VIRTUAL_NETWORK_TAG_DESC = "VLAN tag";
    public static final String START_IP_ADDRESS_DESC = "Start IP address";
    public static final String NUMBER_OF_IP_ADDRESSES_DESC = "Number of contiguous IP addresses starting at '" + ApiHelper.START_IP + "'";
    public static final String NETMASK_DESC = "Netmask of VLAN";
    public static final String ACCOUNT_ID_DESC = "Account ID";
    public static final String VIRTUAL_NETWORK_ID_DESC = "Virtual network ID";
    public static final String VOLUME_ID_DESC = "Volume ID";
    public static final String VOLUME_NAME_DESC = "Name for volume";
    public static final String ZONE_ID_DESC = "Zone ID";

    @Inject private AccountDao _accountDao;
    @Inject private AccountDetailsDao _accountDetailsDao;
    @Inject private AccountManager _accountMgr;
    @Inject private SfClusterDao _sfClusterDao;
    @Inject private DataCenterDao _zoneDao;
    @Inject private SfVirtualNetworkDao _sfVirtualNetworkDao;

    private ApiHelper() {
    }

    public static ApiHelper instance() {
        return s_apiHelperInstance;
    }

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
        sfResponse.setZoneId(sfCluster.getZoneId());

        sfResponse.setObjectName("sfcluster");

        return sfResponse;
    }

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

    public ApiSolidFireVirtualNetworkResponse getApiSolidFireVirtualNetworkResponse(SfVirtualNetwork sfVirtualNetwork, ResponseView responseView) {
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

        Account account = _accountDao.findById(sfVirtualNetwork.getAccountId());

        sfResponse.setAccountUuid(account.getUuid());

        SfCluster sfCluster = _sfClusterDao.findById(sfVirtualNetwork.getSfClusterId());

        sfResponse.setZoneId(sfCluster.getZoneId());

        DataCenterVO dataCenterVO = _zoneDao.findById(sfCluster.getZoneId());

        sfResponse.setZoneUuid(dataCenterVO.getUuid());

        if (ResponseView.Full.equals(responseView)) {
            sfResponse.setClusterName(sfCluster.getName());
        }

        sfResponse.setObjectName("sfvirtualnetwork");

        return sfResponse;
    }

    public List<ApiSolidFireVirtualNetworkResponse> getApiSolidFireVirtualNetworkResponse(List<SfVirtualNetwork> sfVirtualNetworks, ResponseView responseView) {
        List<ApiSolidFireVirtualNetworkResponse> sfResponse = new ArrayList<>();

        if (sfVirtualNetworks != null) {
            for (SfVirtualNetwork sfVirtualNetwork : sfVirtualNetworks) {
                ApiSolidFireVirtualNetworkResponse response = getApiSolidFireVirtualNetworkResponse(sfVirtualNetwork, responseView);

                sfResponse.add(response);
            }
        }

        return sfResponse;
    }

    public ApiSolidFireVolumeResponse getApiSolidFireVolumeResponse(SfVolume sfVolume, ResponseView responseView) {
        ApiSolidFireVolumeResponse sfResponse = new ApiSolidFireVolumeResponse();

        sfResponse.setId(sfVolume.getId());
        sfResponse.setUuid(sfVolume.getUuid());
        sfResponse.setName(sfVolume.getName());
        sfResponse.setSize(sfVolume.getSize());
        sfResponse.setMinIops(sfVolume.getMinIops());
        sfResponse.setMaxIops(sfVolume.getMaxIops());
        sfResponse.setBurstIops(sfVolume.getBurstIops());

        SfVirtualNetwork sfVirtualNetwork = _sfVirtualNetworkDao.findById(sfVolume.getSfVirtualNetworkId());

        sfResponse.setAccountId(sfVirtualNetwork.getAccountId());

        Account account = _accountDao.findById(sfVirtualNetwork.getAccountId());

        sfResponse.setAccountUuid(account.getUuid());

        SfCluster sfCluster = _sfClusterDao.findById(sfVirtualNetwork.getSfClusterId());

        sfResponse.setZoneId(sfCluster.getZoneId());

        DataCenterVO dataCenterVO = _zoneDao.findById(sfCluster.getZoneId());

        sfResponse.setZoneUuid(dataCenterVO.getUuid());

        if (ResponseView.Full.equals(responseView)) {
            sfResponse.setClusterName(sfCluster.getName());
        }

        sfResponse.setTargetPortal(sfVirtualNetwork.getSvip());

        AccountDetailVO accountDetail = _accountDetailsDao.findDetail(sfVirtualNetwork.getAccountId(), SolidFireUtil.CHAP_INITIATOR_USERNAME);

        sfResponse.setChapInitiatorUsername(accountDetail.getValue());

        accountDetail = _accountDetailsDao.findDetail(sfVirtualNetwork.getAccountId(), SolidFireUtil.CHAP_INITIATOR_SECRET);

        sfResponse.setChapInitiatorSecret(accountDetail.getValue());

        accountDetail = _accountDetailsDao.findDetail(sfVirtualNetwork.getAccountId(), SolidFireUtil.CHAP_TARGET_USERNAME);

        sfResponse.setChapTargetUsername(accountDetail.getValue());

        accountDetail = _accountDetailsDao.findDetail(sfVirtualNetwork.getAccountId(), SolidFireUtil.CHAP_TARGET_SECRET);

        sfResponse.setChapTargetSecret(accountDetail.getValue());

        sfResponse.setObjectName("sfvolume");

        return sfResponse;
    }

    public List<ApiSolidFireVolumeResponse> getApiSolidFireVolumeResponse(List<SfVolume> sfVolumes, ResponseView responseView) {
        List<ApiSolidFireVolumeResponse> sfResponse = new ArrayList<>();

        if (sfVolumes != null) {
            for (SfVolume sfVolume : sfVolumes) {
                ApiSolidFireVolumeResponse response = getApiSolidFireVolumeResponse(sfVolume, responseView);

                sfResponse.add(response);
            }
        }

        return sfResponse;
    }

    public boolean isRootAdmin() {
        Account account = getCallingAccount();

        return isRootAdmin(account.getId());
    }

    public boolean isRootAdmin(long accountId) {
        return _accountMgr.isRootAdmin(accountId);
    }

    public Account getCallingAccount() {
        Account account = CallContext.current().getCallingAccount();

        if (account == null) {
            throw new CloudRuntimeException("The user's account cannot be determined.");
        }

        return account;
    }
}
