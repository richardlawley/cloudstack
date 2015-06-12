package org.apache.cloudstack.api.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.cloudstack.api.response.ApiSolidFireClusterResponse;
import org.apache.cloudstack.api.response.ApiSolidFireVirtualNetworkResponse;
import org.apache.cloudstack.api.response.ApiSolidFireVolumeResponse;
import org.apache.cloudstack.solidfire.dataaccess.SfCluster;
import org.apache.cloudstack.solidfire.dataaccess.SfVirtualNetwork;
import org.apache.cloudstack.solidfire.dataaccess.SfVolume;

public class ApiHelper {
    public static ApiSolidFireClusterResponse getApiSolidFireClusterResponse(SfCluster sfCluster) {
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

        return sfResponse;
    }

    public static List<ApiSolidFireClusterResponse> getApiSolidFireClusterResponse(List<SfCluster> sfClusters) {
        List<ApiSolidFireClusterResponse> sfResponse = new ArrayList<>();

        if (sfClusters != null) {
            for (SfCluster sfCluster : sfClusters) {
                ApiSolidFireClusterResponse response = getApiSolidFireClusterResponse(sfCluster);

                sfResponse.add(response);
            }
        }

        return sfResponse;
    }

    public static ApiSolidFireVirtualNetworkResponse getApiSolidFireVirtualNetworkResponse(SfVirtualNetwork sfVirtualNetwork) {
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

    public static List<ApiSolidFireVirtualNetworkResponse> getApiSolidFireVirtualNetworkResponse(List<SfVirtualNetwork> sfVirtualNetworks) {
        List<ApiSolidFireVirtualNetworkResponse> sfResponse = new ArrayList<>();

        if (sfVirtualNetworks != null) {
            for (SfVirtualNetwork sfVirtualNetwork : sfVirtualNetworks) {
                ApiSolidFireVirtualNetworkResponse response = getApiSolidFireVirtualNetworkResponse(sfVirtualNetwork);

                sfResponse.add(response);
            }
        }

        return sfResponse;
    }

    public static ApiSolidFireVolumeResponse getApiSolidFireVolumeResponse(SfVolume sfVolume) {
        ApiSolidFireVolumeResponse sfResponse = new ApiSolidFireVolumeResponse();

        sfResponse.setId(sfVolume.getId());
        sfResponse.setUuid(sfVolume.getUuid());
        sfResponse.setName(sfVolume.getName());
        sfResponse.setSize(sfVolume.getSize());
        sfResponse.setMinIops(sfVolume.getMinIops());
        sfResponse.setMaxIops(sfVolume.getMaxIops());
        sfResponse.setBurstIops(sfVolume.getBurstIops());

        return sfResponse;
    }

    public static List<ApiSolidFireVolumeResponse> getApiSolidFireVolumeResponse(List<SfVolume> sfVolumes) {
        List<ApiSolidFireVolumeResponse> sfResponse = new ArrayList<>();

        if (sfVolumes != null) {
            for (SfVolume sfVolume : sfVolumes) {
                ApiSolidFireVolumeResponse response = getApiSolidFireVolumeResponse(sfVolume);

                sfResponse.add(response);
            }
        }

        return sfResponse;
    }
}
