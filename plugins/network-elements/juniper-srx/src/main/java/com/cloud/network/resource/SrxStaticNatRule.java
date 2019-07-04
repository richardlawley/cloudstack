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

public class SrxStaticNatRule extends SrxNatRule {
    public SrxStaticNatRule(Node node) throws RuleParseException {
        // Expected format:
        // <rule>
        // <name>192-168-2-197-10-33-224-112</name>
        // <static-nat-rule-match>
        // <destination-address>
        // <dst-addr>192.168.2.197/32</dst-addr>
        // </destination-address>
        // </static-nat-rule-match>

        // <then>
        // <static-nat>
        // <prefix>
        // <addr-prefix>10.33.224.112/32</addr-prefix>
        // </prefix>
        // </static-nat>
        // </then>
        // </rule>

        Node nameNode = getChildNode(node, "name");
        if (nameNode != null) {
            this.name = nameNode.getFirstChild().getNodeValue();
        }

        Node staticNatRuleMatchNode = getChildNode(node, "static-nat-rule-match");
        if (staticNatRuleMatchNode != null) {
            Node dstAddr = getChildNode(getChildNode(staticNatRuleMatchNode, "destination-address"), "dst-addr");
            if (dstAddr != null) {
                String value = dstAddr.getFirstChild().getNodeValue();
                if (value.endsWith("/32")) {
                    this.publicIp = value.substring(0, value.length() - "/32".length());
                }
            }
        }

        Node thenNode = getChildNode(node, "then");
        if (thenNode != null) {
            Node addrPrefix = getChildNode(getChildNode(getChildNode(thenNode, "static-nat"), "prefix"), "addr-prefix");
            if (addrPrefix != null) {
                String value = addrPrefix.getFirstChild().getNodeValue();
                if (value.endsWith("/32")) {
                    this.privateIp = value.substring(0, value.length() - "/32".length());
                }
            }
        }

        if (this.name == null || this.publicIp == null || this.privateIp == null) {
            throw new RuleParseException("Could not parse Static NAT rule " + node);
        }
    }

    public String[] toPairArray() {
        return new String[] { this.publicIp, this.privateIp };
    }

    @Override
    public String toString() {
        return String.format("StaticNat %s <-> %s", this.getPublicIp(), this.getPrivateIp());
    }
}