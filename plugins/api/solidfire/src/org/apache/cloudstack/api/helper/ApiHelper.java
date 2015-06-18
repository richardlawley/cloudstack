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

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.exception.CloudRuntimeException;

public class ApiHelper {
    private static ApiHelper _instance = new ApiHelper();

    @Inject private AccountDao _accountDao;
    @Inject private AccountManager _accountMgr;
    @Inject private SfClusterDao _sfClusterDao;
    @Inject private DataCenterDao _zoneDao;
    @Inject private SfVirtualNetworkDao _sfVirtualNetworkDao;

    private ApiHelper() {
    }

    public static ApiHelper instance() {
        return _instance;
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
