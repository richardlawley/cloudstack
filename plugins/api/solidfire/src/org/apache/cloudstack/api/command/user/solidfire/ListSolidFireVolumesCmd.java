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

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.helper.ApiHelper;
import org.apache.cloudstack.api.response.ApiSolidFireVolumeResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.solidfire.ApiSolidFireService2;
import org.apache.cloudstack.solidfire.dataaccess.SfVolume;

@APICommand(name = "listSolidFireVolumes", responseObject = ApiSolidFireVolumeResponse.class, description = "List SolidFire Volumes",
    requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListSolidFireVolumesCmd extends BaseListCmd {
    private static final Logger s_logger = Logger.getLogger(ListSolidFireVolumesCmd.class.getName());
    private static final String s_name = "listsolidfirevolumesresponse";

    @Inject private ApiSolidFireService2 _apiSolidFireService2;

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public void execute() {
        s_logger.info("ListSolidFireVolumesCmd.execute invoked");

        try {
            List<SfVolume> sfVolumes = _apiSolidFireService2.listSolidFireVolumes();

            List<ApiSolidFireVolumeResponse> responses = ApiHelper.getApiSolidFireVolumeResponse(sfVolumes);

            ListResponse<ApiSolidFireVolumeResponse> listReponse = new ListResponse<>();

            listReponse.setResponses(responses);
            listReponse.setResponseName(getCommandName());
            listReponse.setObjectName("apilistsolidfirevolumes");

            setResponseObject(listReponse);
        }
        catch (Exception ex) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }
}
