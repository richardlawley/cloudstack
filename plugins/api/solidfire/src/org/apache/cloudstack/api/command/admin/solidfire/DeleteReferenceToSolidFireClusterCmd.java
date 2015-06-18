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
import org.apache.cloudstack.api.response.ApiSolidFireClusterResponse;
import org.apache.cloudstack.solidfire.ApiSolidFireService2;
import org.apache.cloudstack.solidfire.dataaccess.SfCluster;

@APICommand(name = "deleteReferenceToSolidFireCluster", responseObject = ApiSolidFireClusterResponse.class, description = "Delete Reference to SolidFire Cluster",
    requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteReferenceToSolidFireClusterCmd extends BaseCmd {
    private static final Logger s_logger = Logger.getLogger(DeleteReferenceToSolidFireClusterCmd.class.getName());
    private static final String s_name = "deletereferencetosolidfireclusterresponse";

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "SolidFire cluster name", required = true)
    private String name;

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
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute() {
        s_logger.info("DeleteReferenceToSolidFireClusterCmd.execute invoked");

        try {
            SfCluster sfCluster = _apiSolidFireService2.deleteReferenceToSolidFireCluster(name);

            ApiSolidFireClusterResponse response = ApiHelper.instance().getApiSolidFireClusterResponse(sfCluster);

            response.setResponseName(getCommandName());
            response.setObjectName("apideletereferencetosolidfirecluster");

            setResponseObject(response);
        }
        catch (Exception ex) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }
}
