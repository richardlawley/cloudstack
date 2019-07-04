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
package com.cloud.network.resource;

import org.w3c.dom.Node;

public class SrxDestNatRule extends SrxNatRule {
    private String sourcePort = null;
    private String destPort = null;
    private String ruleSet = null;
    private String poolName = null;

    public SrxDestNatRule(Node node, String ruleSet) throws RuleParseException {
        // Expected format:
        // <rule>
        // <name>destnatrule-12345678p</name>
        // <dest-nat-rule-match>
        // <destination-address>
        // <dst-addr>192.168.2.197</dst-addr>
        // </destination-address>
        // <destination-port>
        // <name>99</name>
        // </destination-port>
        // </dest-nat-rule-match>
        // <then>
        // <destination-nat>
        // <pool>
        // <pool-name>10-5-17-75-99</pool-name>
        // </pool>
        // </destination-nat>
        // </then>
        // </rule>
        this.ruleSet = ruleSet;

        Node nameNode = getChildNode(node, "name");
        if (nameNode != null) {
            this.name = nameNode.getFirstChild().getNodeValue();
        }

        Node destNatRuleMatchNode = getChildNode(node, "dest-nat-rule-match");
        if (destNatRuleMatchNode != null) {
            Node dstAddr = getChildNode(getChildNode(destNatRuleMatchNode, "destination-address"), "dst-addr");
            if (dstAddr != null) {
                this.publicIp = dstAddr.getFirstChild().getNodeValue();
            }
            Node destPortNode = getChildNode(destNatRuleMatchNode, "destination-port");
            if (destPortNode != null) {
                Node destPortStart = getChildNode(destPortNode, "name");
                if (destPortStart == null) {
                    destPortStart = getChildNode(destPortNode, "dst-port");     // Old JunOS syntax, may not be needed
                }
                if (destPortStart != null) {
                    this.sourcePort = destPortStart.getFirstChild().getNodeValue();
                }
                // TODO: Dest Port Range?
            }
        }

        Node thenNode = getChildNode(node, "then");
        if (thenNode != null) {
            Node poolNameNode = getChildNode(getChildNode(getChildNode(thenNode, "destination-nat"), "pool"), "pool-name");
            if (poolNameNode != null) {
                this.poolName = poolNameNode.getFirstChild().getNodeValue();
                if (this.poolName.matches("^\\d+(-\\d+){4}$")) {
                    this.privateIp = this.poolName.substring(0, this.poolName.lastIndexOf("-")).replace('-', '.');
                    this.destPort = this.poolName.substring(this.poolName.lastIndexOf("-") + 1);
                }
            }
        }

        // NodeList ruleEntries = node.getChildNodes();

        // for (int ruleEntryIndex = 0; ruleEntryIndex < ruleEntries.getLength(); ruleEntryIndex++) {
        //     Node ruleEntry = ruleEntries.item(ruleEntryIndex);

        //     if (ruleEntry.getNodeName().equals("name")) {
        //         this.name = ruleEntry.getFirstChild().getNodeValue();
        //     } else if (ruleEntry.getNodeName().equals("dest-nat-rule-match")) {
        //         NodeList ruleMatchEntries = ruleEntry.getChildNodes();
        //         for (int ruleMatchIndex = 0; ruleMatchIndex < ruleMatchEntries.getLength(); ruleMatchIndex++) {
        //             Node ruleMatchEntry = ruleMatchEntries.item(ruleMatchIndex);
        //             if (ruleMatchEntry.getNodeName().equals("destination-address")) {
        //                 NodeList destAddressEntries = ruleMatchEntry.getChildNodes();
        //                 for (int destAddressIndex = 0; destAddressIndex < destAddressEntries
        //                         .getLength(); destAddressIndex++) {
        //                     Node destAddressEntry = destAddressEntries.item(destAddressIndex);
        //                     if (destAddressEntry.getNodeName().equals("dst-addr")) {
        //                         this.publicIp = destAddressEntry.getFirstChild().getNodeValue().split("/")[0];
        //                     }
        //                 }
        //             } else if (ruleMatchEntry.getNodeName().equals("destination-port")) {
        //                 NodeList destPortEntries = ruleMatchEntry.getChildNodes();
        //                 for (int destPortIndex = 0; destPortIndex < destPortEntries.getLength(); destPortIndex++) {
        //                     Node destPortEntry = destPortEntries.item(destPortIndex);
        //                     if (destPortEntry.getNodeName().equals("dst-port")
        //                             || destPortEntry.getNodeName().equals("name")) {
        //                         this.sourcePort = destPortEntry.getFirstChild().getNodeValue();
        //                     }
        //                 }
        //             }
        //         }
        //     } else if (ruleEntry.getNodeName().equals("then")) {
        //         NodeList ruleThenEntries = ruleEntry.getChildNodes();
        //         for (int ruleThenIndex = 0; ruleThenIndex < ruleThenEntries.getLength(); ruleThenIndex++) {
        //             Node ruleThenEntry = ruleThenEntries.item(ruleThenIndex);
        //             if (ruleThenEntry.getNodeName().equals("destination-nat")) {
        //                 NodeList destNatEntries = ruleThenEntry.getChildNodes();
        //                 for (int destNatIndex = 0; destNatIndex < destNatEntries.getLength(); destNatIndex++) {
        //                     Node destNatEntry = destNatEntries.item(destNatIndex);
        //                     if (destNatEntry.getNodeName().equals("pool")) {
        //                         NodeList poolEntries = destNatEntry.getChildNodes();
        //                         for (int poolIndex = 0; poolIndex < poolEntries.getLength(); poolIndex++) {
        //                             Node poolEntry = poolEntries.item(poolIndex);
        //                             if (poolEntry.getNodeName().equals("pool-name")) {
        //                                 String[] poolName = poolEntry.getFirstChild().getNodeValue().split("-");
        //                                 if (poolName.length == 5) {
        //                                     this.privateIp = poolName[0] + "." + poolName[1] + "." + poolName[2] + "."
        //                                             + poolName[3];
        //                                     this.destPort = poolName[4];
        //                                 }
        //                             }
        //                         }
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }

        if (this.name == null || this.publicIp == null || this.privateIp == null || this.sourcePort == null
                || this.destPort == null || this.poolName == null) {
            throw new RuleParseException(String.format("Cannot parse Dest Nat Rule (name=%s, publicIp=%s, privateIp=%s, sourcePort=%s, destPort=%s, poolName=%s)",
                this.name, this.publicIp, this.privateIp, this.sourcePort, this.destPort, this.poolName));
        }
    }

    public String getRuleSet() {
        return this.ruleSet;
    }

    public int getSourcePort() {
        return Integer.parseInt(this.sourcePort);
    }

    public int getDestPort() {
        return Integer.parseInt(this.destPort);
    }

    public String getPoolName() {
        return this.poolName;
    }

    @Override
    public String toString() {
        return String.format("DestNat %s:%s -> %s:%s", this.getPublicIp(), this.getSourcePort(), this.getPrivateIp(),
                this.getDestPort());
    }
}