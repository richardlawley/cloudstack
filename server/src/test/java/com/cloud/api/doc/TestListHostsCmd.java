package com.cloud.api.doc;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListTaggedResourcesCmd;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.HostResponse;

@APICommand(name = "listTestHosts", description = "Test List Hosts", responseObject = HostResponse.class)
public class TestListHostsCmd extends BaseListTaggedResourcesCmd {

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException,
            ConcurrentOperationException, ResourceAllocationException, NetworkRuleConflictException {
    }

    @Override
    public String getCommandName() {
        return null;
    }
}