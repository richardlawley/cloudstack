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
package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.api.helper.ApiHelper;
import org.apache.cloudstack.solidfire.dataaccess.SfVolume;

@EntityReference(value = SfVolume.class)
public class ApiSolidFireVolumeResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "CloudStack ID")
    private long _id;

    @SerializedName("uuid")
    @Param(description = "CloudStack UUID")
    private String _uuid;

    @SerializedName("name")
    @Param(description = ApiHelper.VOLUME_NAME_DESC)
    private String _name;

    @SerializedName("size")
    @Param(description = ApiHelper.SIZE_DESC)
    private long _size;

    @SerializedName("miniops")
    @Param(description = ApiHelper.MIN_IOPS_DESC)
    private long _minIops;

    @SerializedName("maxiops")
    @Param(description = ApiHelper.MAX_IOPS_DESC)
    private long _maxIops;

    @SerializedName("burstiops")
    @Param(description = ApiHelper.BURST_IOPS_DESC)
    private long _burstIops;

    @SerializedName("accountid")
    @Param(description = "ID of the account the volume is associated with")
    private long _accountId;

    @SerializedName("accountuuid")
    @Param(description = "UUID of the account the volume is associated with")
    private String _accountUuid;

    @SerializedName("zoneid")
    @Param(description = "ID of the zone the volume is associated with")
    private long _zoneId;

    @SerializedName("zoneuuid")
    @Param(description = "UUID of the zone the volume is associated with")
    private String _zoneUuid;

    @SerializedName("clustername")
    @Param(description = "Name of cluster the volume belongs to")
    private String _clusterName;

    @SerializedName("targetportal")
    @Param(description = "Target portal")
    private String _targetPortal;

    @SerializedName("chapinitiatorusername")
    @Param(description = "CHAP initiator username")
    private String _chapInitiatorUsername;

    @SerializedName("chapinitiatorsecret")
    @Param(description = "CHAP initiator secret")
    private String _chapInitiatorSecret;

    @SerializedName("chaptargetusername")
    @Param(description = "CHAP target username")
    private String _chapTargetUsername;

    @SerializedName("chaptargetsecret")
    @Param(description = "CHAP target secret")
    private String _chapTargetSecret;

    public void setId(long id) {
        _id = id;
    }

    public long getId() {
        return _id;
    }

    public void setUuid(String uuid) {
        _uuid = uuid;
    }

    public String getUuid() {
        return _uuid;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public void setSize(long size) {
        _size = size;
    }

    public long getSize() {
        return _size;
    }

    public void setMinIops(long minIops) {
        _minIops = minIops;
    }

    public long getMinIops() {
        return _minIops;
    }

    public void setMaxIops(long maxIops) {
        _maxIops = maxIops;
    }

    public long getMaxIops() {
        return _maxIops;
    }

    public void setBurstIops(long burstIops) {
        _burstIops = burstIops;
    }

    public long getBurstIops() {
        return _burstIops;
    }

    public void setAccountId(long accountId) {
        _accountId = accountId;
    }

    public long getAccountId() {
        return _accountId;
    }

    public void setAccountUuid(String accountUuid) {
        _accountUuid = accountUuid;
    }

    public String getAccountUuid() {
        return _accountUuid;
    }

    public void setZoneId(long zoneId) {
        _zoneId = zoneId;
    }

    public long getZoneId() {
        return _zoneId;
    }

    public void setZoneUuid(String zoneUuid) {
        _zoneUuid = zoneUuid;
    }

    public String getZoneUuid() {
        return _zoneUuid;
    }

    public void setClusterName(String clusterName) {
        _clusterName = clusterName;
    }

    public String getClusterName() {
        return _clusterName;
    }

    public void setTargetPortal(String targetPortal) {
        _targetPortal = targetPortal;
    }

    public String getTargetPortal() {
        return _targetPortal;
    }

    public void setChapInitiatorUsername(String chapInitiatorUsername) {
        _chapInitiatorUsername = chapInitiatorUsername;
    }

    public String getChapInitiatorUsername() {
        return _chapInitiatorUsername;
    }

    public void setChapInitiatorSecret(String chapInitiatorSecret) {
        _chapInitiatorSecret = chapInitiatorSecret;
    }

    public String getChapInitiatorSecret() {
        return _chapInitiatorSecret;
    }

    public void setChapTargetUsername(String chapTargetUsername) {
        _chapTargetUsername = chapTargetUsername;
    }

    public String getTargetInitiatorUsername() {
        return _chapTargetUsername;
    }

    public void setChapTargetSecret(String chapTargetSecret) {
        _chapTargetSecret = chapTargetSecret;
    }

    public String getTargetInitiatorSecret() {
        return _chapTargetSecret;
    }
}
