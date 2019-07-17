package com.cloud.api.doc;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;

@APICommand(name = CommandTest.NAME, description = CommandTest.DESC, responseObject = TestApiCmd.TestApiResponse.class, since = CommandTest.VERSION)
public class TestApiCmd extends BaseCmd {
    public TestApiCmd() {
    }

    @Parameter(name = CommandTest.PARAMNAME1, type = CommandType.STRING, required = true, description = "Param Description 1")
    private String p1;

    @Parameter(name = CommandTest.PARAMNAME2, type = CommandType.LONG, required = false, description = "Param Description 2")
    private Long p2;

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException,
            ConcurrentOperationException, ResourceAllocationException, NetworkRuleConflictException {
    }

    @Override
    public String getCommandName() {
        return null;
    }

    @Override
    public long getEntityOwnerId() {
        return 0;
    }

    public class TestApiResponse extends BaseResponse {
        @SerializedName(CommandTest.RESPONSEARG1)
        @Param(description = "Response Arg 1")
        private String r1;

        @SerializedName(CommandTest.RESPONSEARG2)
        @Param(description = "Response Arg 2")
        private Long r2;
    }
}