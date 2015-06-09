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
package org.apache.cloudstack.api.command.admin.solidfire;

import com.cloud.user.Account;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.helper.ApiHelper;
import org.apache.cloudstack.api.response.ApiSolidFireVirtualNetworkResponse;
import org.apache.cloudstack.solidfire.ApiSolidFireService2;
import org.apache.cloudstack.solidfire.dataaccess.SfVirtualNetwork;

@APICommand(name = "updateSolidFireVirtualNetwork", responseObject = ApiSolidFireVirtualNetworkResponse.class, description = "Update SolidFire Virtual Network",
    requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateSolidFireVirtualNetworkCmd extends BaseCmd {
    private static final Logger s_logger = Logger.getLogger(UpdateSolidFireVirtualNetworkCmd.class.getName());
    private static final String s_name = "updatesolidfirevirtualnetworkresponse";

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, description = "SolidFire virtual network ID", required = true)
    private long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "VLAN name", required = true)
    private String name;

    @Parameter(name = "tag", type = CommandType.STRING, description = "VLAN tag", required = true)
    private String tag;

    @Parameter(name = "startip", type = CommandType.STRING, description = "Start IP address", required = true)
    private String startIp;

    @Parameter(name = "size", type = CommandType.INTEGER, description = "Number of contiguous IP addresses starting at 'startip'", required = true)
    private int size;

    @Parameter(name = "netmask", type = CommandType.STRING, description = "Netmask", required = true)
    private String netmask;

    @Parameter(name = "svip", type = CommandType.STRING, description = "SolidFire SVIP", required = true)
    private String svip;

    @Inject private ApiSolidFireService2 _apiSolidFireService2;

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        SfVirtualNetwork sfVirtualNetwork = _entityMgr.findById(SfVirtualNetwork.class, id);

        if (sfVirtualNetwork != null) {
            sfVirtualNetwork.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    @Override
    public void execute() {
        s_logger.info("UpdateSolidFireVirtualNetworkCmd.execute invoked");

        try {
            SfVirtualNetwork sfVirtualNetwork = _apiSolidFireService2.updateSolidFireVirtualNetwork(id, name, tag, startIp, size, netmask, svip);

            ApiSolidFireVirtualNetworkResponse response = ApiHelper.getApiSolidFireVirtualNetworkResponse(sfVirtualNetwork);

            response.setResponseName(getCommandName());
            response.setObjectName("apiupdatesolidfirevirtualnetwork");

            setResponseObject(response);
        }
        catch (Exception ex) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }
}
