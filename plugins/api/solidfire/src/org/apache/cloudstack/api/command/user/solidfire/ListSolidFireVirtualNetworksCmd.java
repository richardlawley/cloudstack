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
package org.apache.cloudstack.api.command.user.solidfire;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.helper.ApiHelper;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.ApiSolidFireVirtualNetworkResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.ZoneResponse;
import org.apache.cloudstack.solidfire.ApiSolidFireService2;
import org.apache.cloudstack.solidfire.dataaccess.SfVirtualNetwork;

@APICommand(name = "listSolidFireVirtualNetworks", responseObject = ApiSolidFireVirtualNetworkResponse.class, description = "List SolidFire Virtual Networks",
    requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListSolidFireVirtualNetworksCmd extends BaseListCmd {
    private static final Logger s_logger = Logger.getLogger(ListSolidFireVirtualNetworksCmd.class.getName());
    private static final String s_name = "listsolidfirevirtualnetworksresponse";

    @Inject private ApiSolidFireService2 _apiSolidFireService2;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ApiSolidFireVirtualNetworkResponse.class, description = ApiHelper.VIRTUAL_NETWORK_ID_DESC)
    private Long _id;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = ApiHelper.ZONE_ID_DESC)
    private Long _zoneId;

    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, description = ApiHelper.ACCOUNT_ID_DESC)
    private Long _accountId;

    @Inject private ApiHelper _apiHelper;

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public void execute() {
        try {
            s_logger.info(ListSolidFireVirtualNetworksCmd.class.getName() + ".execute invoked");

            final List<SfVirtualNetwork> sfVirtualNetworks;

            if (_id != null) {
                sfVirtualNetworks = new ArrayList<>();

                SfVirtualNetwork sfVirtualNetwork = _apiSolidFireService2.listSolidFireVirtualNetwork(_id);

                if (sfVirtualNetwork != null) {
                    sfVirtualNetworks.add(sfVirtualNetwork);
                }
            }
            else {
                sfVirtualNetworks = _apiSolidFireService2.listSolidFireVirtualNetworks(_zoneId, _accountId);
            }

            ResponseView responseView = ApiHelper.instance().isRootAdmin() ? ResponseView.Full : ResponseView.Restricted;

            List<ApiSolidFireVirtualNetworkResponse> responses = ApiHelper.instance().getApiSolidFireVirtualNetworkResponse(sfVirtualNetworks, responseView);

            ListResponse<ApiSolidFireVirtualNetworkResponse> listReponse = new ListResponse<>();

            listReponse.setResponses(responses);
            listReponse.setResponseName(getCommandName());
            listReponse.setObjectName("apilistsolidfirevirtualnetworks");

            setResponseObject(listReponse);
        }
        catch (Throwable t) {
            s_logger.error(t.getMessage());

            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, t.getMessage());
        }
    }
}
