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
package org.apache.cloudstack.solidfire;

import java.util.List;

import org.apache.cloudstack.solidfire.dataaccess.SfCluster;
import org.apache.cloudstack.solidfire.dataaccess.SfVirtualNetwork;

import com.cloud.utils.component.PluggableService;

public interface ApiSolidFireService2 extends PluggableService {
    // ********** Cluster-related commands **********

    SfCluster listSolidFireCluster(String clusterName);

    List<SfCluster> listSolidFireClusters();

    SfCluster createReferenceToSolidFireCluster(String mvip, String username, String password, long totalCapacity,
            long totalMinIops, long totalMaxIops, long totalBurstIops, long zoneId);

    SfCluster updateReferenceToSolidFireCluster(String clusterName, long totalCapacity,
            long totalMinIops, long totalMaxIops, long totalBurstIops);

    SfCluster deleteReferenceToSolidFireCluster(String clusterName);

    // ********** VLAN-related commands **********

    SfVirtualNetwork listSolidFireVirtualNetwork(long id);

    List<SfVirtualNetwork> listSolidFireVirtualNetworks();

    SfVirtualNetwork createSolidFireVirtualNetwork(String clusterName, String name, String tag, String startIp, int size,
            String netmask, String svip, long accountId);

    SfVirtualNetwork updateSolidFireVirtualNetwork(long id, String name, String tag, String startIp, int size,
            String netmask, String svip);

    SfVirtualNetwork deleteSolidFireVirtualNetwork(long id);
}
