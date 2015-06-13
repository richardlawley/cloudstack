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
import org.apache.cloudstack.solidfire.dataaccess.SfVirtualNetwork;

@EntityReference(value = SfVirtualNetwork.class)
public class ApiSolidFireVirtualNetworkResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "CloudStack ID")
    private long _id;

    @SerializedName("uuid")
    @Param(description = "CloudStack UUID")
    private String _uuid;

    @SerializedName("name")
    @Param(description = "SolidFire VLAN name")
    private String _name;

    @SerializedName("tag")
    @Param(description = "SolidFire VLAN tag")
    private String _tag;

    @SerializedName("startip")
    @Param(description = "Start of range of IP addresses")
    private String _startIp;

    @SerializedName("size")
    @Param(description = "Size of range of IP addresses")
    private int _size;

    @SerializedName("netmask")
    @Param(description = "Netmask of VLAN")
    private String _netmask;

    @SerializedName("svip")
    @Param(description = "SVIP of VLAN")
    private String _svip;

    @SerializedName("accountid")
    @Param(description = "Account ID that the virtual network is associated with")
    private long _accountId;

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

    public void setTag(String tag) {
        _tag = tag;
    }

    public String getTag() {
        return _tag;
    }

    public void setStartIp(String startIp) {
        _startIp = startIp;
    }

    public String getStartIp() {
        return _startIp;
    }

    public void setSize(int size) {
        _size = size;
    }

    public int getSize() {
        return _size;
    }

    public void setNetmask(String netmask) {
        _netmask = netmask;
    }

    public String getNetmask() {
        return _netmask;
    }

    public void setSvip(String svip) {
        _svip = svip;
    }

    public String getSvip() {
        return _svip;
    }

    public void setAccountId(long accountId) {
        _accountId = accountId;
    }

    public long getAccountId() {
        return _accountId;
    }
}
