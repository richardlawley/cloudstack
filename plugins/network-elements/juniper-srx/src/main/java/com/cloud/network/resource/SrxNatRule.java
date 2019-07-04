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
import org.w3c.dom.NodeList;

public class SrxNatRule {
    protected String publicIp = null;
    protected String privateIp = null;
    protected String name = null;

    public String getPublicIp() {
        return this.publicIp;
    }
    public String getPrivateIp() {
        return this.privateIp;
    }

    public String getName() {
        return this.name;
    }

    public class RuleParseException extends Exception {
        private static final long serialVersionUID = 2684851104623962757L;
        public RuleParseException(String message) {
            super(message);
        }
    }

    protected Node getChildNode(Node parent, String name) {
        if (parent == null) {
            return null;
        }
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeName().equals(name)) {
                return node;
            }
        }
        return null;
    }
}