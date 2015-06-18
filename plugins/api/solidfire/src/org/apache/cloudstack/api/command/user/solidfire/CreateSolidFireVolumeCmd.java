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

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.helper.ApiHelper;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.ApiSolidFireVirtualNetworkResponse;
import org.apache.cloudstack.api.response.ApiSolidFireVolumeResponse;
import org.apache.cloudstack.solidfire.ApiSolidFireService2;
import org.apache.cloudstack.solidfire.dataaccess.SfVolume;

@APICommand(name = "createSolidFireVolume", responseObject = ApiSolidFireVolumeResponse.class, description = "Create SolidFire Volume",
    requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateSolidFireVolumeCmd extends BaseCmd {
    private static final Logger s_logger = Logger.getLogger(CreateSolidFireVolumeCmd.class.getName());
    private static final String s_name = "createsolidfirevolumeresponse";

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "Name", required = true)
    private String name;

    @Parameter(name = ApiConstants.SIZE, type = CommandType.LONG, description = "Size (in GBs)", required = true)
    private long size;

    @Parameter(name = ApiConstants.MIN_IOPS, type = CommandType.LONG, description = "Min IOPS", required = true)
    private long minIops;

    @Parameter(name = ApiConstants.MAX_IOPS, type = CommandType.LONG, description = "Max IOPS", required = true)
    private long maxIops;

    @Parameter(name = "burstiops", type = CommandType.LONG, description = "Burst IOPS", required = true)
    private long burstIops;

    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, description = "Account ID", required = true)
    private long accountId;

    @Parameter(name = "sfvirtualnetworkid", type = CommandType.UUID, entityType = ApiSolidFireVirtualNetworkResponse.class, description = "Virtual Network ID", required = true)
    private long sfVirtualNetworkId;

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
        return accountId;
    }

    @Override
    public void execute() {
        s_logger.info("CreateSolidFireVolumeCmd.execute invoked");

        try {
            SfVolume sfVolume = _apiSolidFireService2.createSolidFireVolume(name, size, minIops, maxIops, burstIops, accountId, sfVirtualNetworkId);

            ResponseView responseView = ApiHelper.instance().isRootAdmin() ? ResponseView.Full : ResponseView.Restricted;

            ApiSolidFireVolumeResponse response = ApiHelper.instance().getApiSolidFireVolumeResponse(sfVolume, responseView);

            response.setResponseName(getCommandName());
            response.setObjectName("apicreatesolidfirevolume");

            setResponseObject(response);
        }
        catch (Exception ex) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }
}
