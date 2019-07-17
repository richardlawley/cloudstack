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
package com.cloud.api.doc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.cloud.utils.ReflectUtil;

import org.apache.cloudstack.api.APICommand;

public abstract class ApiProcessor {
    protected Map<String, Class<?>> _apiNameCmdClassMap = new HashMap<String, Class<?>>();
    protected LinkedHashMap<Object, String> _allApiCommands = new LinkedHashMap<Object, String>();
    protected TreeMap<Object, String> _allApiCommandsSorted = new TreeMap<Object, String>();

    public ApiProcessor() {

        // Namespaces to search for API commands
        List<String> namespaces = new ArrayList<String>();
        namespaces.add("org.apache.cloudstack.api");
        namespaces.add("com.cloud.api");
        namespaces.add("com.cloud.api.commands");
        namespaces.add("com.globo.globodns.cloudstack.api");
        namespaces.add("org.apache.cloudstack.network.opendaylight.api");
        namespaces.add("org.apache.cloudstack.api.command.admin.zone");
        namespaces.add("org.apache.cloudstack.network.contrail.api.command");

        // Use reflection to find classes annotated with APICommand
        Set<Class<?>> cmdClasses = ReflectUtil.getClassesWithAnnotation(APICommand.class,
                namespaces.toArray(new String[namespaces.size()]));
        AddClasses(cmdClasses);

        System.out.println(String.format("Scanned and found %d APIs\n", _apiNameCmdClassMap.size()));

        for (Map.Entry<String, Class<?>> entry : _apiNameCmdClassMap.entrySet()) {
            Class<?> cls = entry.getValue();
            _allApiCommands.put(entry.getKey(), cls.getName());
        }

        _allApiCommandsSorted.putAll(_allApiCommands);
    }

    private void AddClasses(Set<Class<?>> cmdClasses) {
        for (Class<?> cmdClass : cmdClasses) {
            if (cmdClass.getAnnotation(APICommand.class) == null) {
                // Possibly an inherited command without its own @ApiCommand attribute
                System.out.println(
                        String.format("Warning, API Cmd class %s has no APICommand annotation ", cmdClass.getName()));
                continue;
            }
            String apiName = cmdClass.getAnnotation(APICommand.class).name();
            if (_apiNameCmdClassMap.containsKey(apiName)) {
                // handle API cmd separation into admin cmd and user cmd with the common api
                // name
                Class<?> curCmd = _apiNameCmdClassMap.get(apiName);
                if (curCmd.isAssignableFrom(cmdClass)) {
                    // api_cmd map always keep the admin cmd class to get full response and
                    // parameters
                    _apiNameCmdClassMap.put(apiName, cmdClass);
                } else if (cmdClass.isAssignableFrom(curCmd)) {
                    // just skip this one without warning
                    continue;
                } else {
                    System.out.println(String.format("Warning, API Cmd class %s has non-unique apiname %s",
                            cmdClass.getName(), apiName));
                    continue;
                }
            } else {
                _apiNameCmdClassMap.put(apiName, cmdClass);
            }
        }
    }

    public abstract void ProcessApiCommands() throws IOException, ClassNotFoundException;

}