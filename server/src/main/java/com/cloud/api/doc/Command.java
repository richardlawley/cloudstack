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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cloud.serializer.Param;
import com.cloud.utils.IteratorUtil;
import com.cloud.utils.ReflectUtil;
import com.google.gson.annotations.SerializedName;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.AsyncJobResponse;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.SecurityGroupResponse;
import org.apache.cloudstack.api.response.SnapshotResponse;
import org.apache.cloudstack.api.response.StoragePoolResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VolumeResponse;

public class Command implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -4318310162503004975L;
    private String name;
    private String description;
    private String usage;
    private boolean isAsync;
    private String sinceVersion = null;
    private ArrayList<Argument> request;
    private ArrayList<Argument> response;
    private String responseType;
    private boolean isList;
    private boolean includeInDocumentation = true;
    private String listContainerName;
    protected static final List<String> AsyncResponses = setAsyncResponses();

    public Command(String name, Class<?> implementation) {
        this.name = name;

        APICommand annotation = implementation.getAnnotation(APICommand.class);
        if (annotation == null) {
            annotation = implementation.getSuperclass().getAnnotation(APICommand.class);
        }
        if (annotation == null) {
            throw new IllegalStateException(
                    String.format("An %1$s annotation is required for class %2$s.", APICommand.class.getCanonicalName(), implementation.getCanonicalName()));
        }
        Class<? extends BaseResponse> responseObject = annotation.responseObject();
        responseType = responseObject.getName();

        try {
            Object commandInstance = implementation.newInstance();
            if (commandInstance instanceof BaseListCmd) {
                // BaseListCmd listCmd = (BaseListCmd) commandInstance;
                this.isList = true;

                // The serialised name of the list will be the "entityType" of the class.
                if (annotation.entityType() != null && annotation.entityType().length > 0) {
                    this.listContainerName = annotation.entityType()[0].getSimpleName();
                } else {
                    // Look for an entity annotation on the referenced class
                    EntityReference entityAnnotation = responseObject.getAnnotation(EntityReference.class);
                    if (entityAnnotation != null && entityAnnotation.value().length > 0) {
                        this.listContainerName = entityAnnotation.value()[0].getSimpleName();
                    }
                }
            } else if (name.equals("registerTemplate")) {
                // One of the few non list- APIs which returns multiple records
                this.isList = true;
                this.listContainerName = "templates";
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        includeInDocumentation = annotation.includeInApiDoc();
        this.description = annotation.description();
        if (this.description == null || this.description.isEmpty()) {
            System.out.println(String.format("Command %s misses description", this.name));
        }

        this.usage = annotation.usage();

        // Set version when the API is added
        if (!annotation.since().isEmpty()) {
            this.sinceVersion = annotation.since();
        }

        this.isAsync = ReflectUtil.isCmdClassAsync(implementation, new Class<?>[] { BaseAsyncCmd.class, BaseAsyncCreateCmd.class });

        Set<Field> fields = ReflectUtil.getAllFieldsForClass(implementation, new Class<?>[] { BaseCmd.class, BaseAsyncCmd.class, BaseAsyncCreateCmd.class });
        ArrayList<Argument> request = setRequestFields(fields);

        // Get response parameters
        Class<?> responseClas = annotation.responseObject();
        Field[] responseFields = responseClas.getDeclaredFields();
        ArrayList<Argument> response = setResponseFields(responseFields, responseClas);

        this.request = request;
        this.response = response;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean getIncludeInDocumentation() {
        return includeInDocumentation;
    }

    public ArrayList<Argument> getRequest() {
        return request;
    }

    public ArrayList<Argument> getResponse() {
        return response;
    }

    public String getResponseType() {
        return responseType;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public boolean isList() {
        return isList;
    }

    public String getListContainerName() {
        return listContainerName;
    }

    public String getSinceVersion() {
        return sinceVersion;
    }

    public Argument getReqArgByName(String name) {
        for (Argument a : getRequest()) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public Argument getResArgByName(String name) {
        for (Argument a : getResponse()) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    private ArrayList<Argument> setRequestFields(Set<Field> fields) {
        ArrayList<Argument> arguments = new ArrayList<Argument>();
        Set<Argument> requiredArguments = new HashSet<Argument>();
        Set<Argument> optionalArguments = new HashSet<Argument>();
        Argument id = null;
        for (Field f : fields) {
            Parameter parameterAnnotation = f.getAnnotation(Parameter.class);
            if (parameterAnnotation != null && parameterAnnotation.expose() && parameterAnnotation.includeInApiDoc()) {
                Argument reqArg = new Argument(parameterAnnotation.name());
                reqArg.setRequired(parameterAnnotation.required());
                if (!parameterAnnotation.description().isEmpty()) {
                    reqArg.setDescription(parameterAnnotation.description());
                }

                if (parameterAnnotation.type() == BaseCmd.CommandType.LIST || parameterAnnotation.type() == BaseCmd.CommandType.MAP) {
                    reqArg.setType(parameterAnnotation.type().toString().toLowerCase());
                }

                reqArg.setDataType(parameterAnnotation.type().toString().toLowerCase());

                if (!parameterAnnotation.since().isEmpty()) {
                    reqArg.setSinceVersion(parameterAnnotation.since());
                }

                if (reqArg.isRequired()) {
                    if (parameterAnnotation.name().equals("id")) {
                        id = reqArg;
                    } else {
                        requiredArguments.add(reqArg);
                    }
                } else {
                    optionalArguments.add(reqArg);
                }
            }
        }

        // sort required and optional arguments here
        if (id != null) {
            arguments.add(id);
        }
        arguments.addAll(IteratorUtil.asSortedList(requiredArguments));
        arguments.addAll(IteratorUtil.asSortedList(optionalArguments));

        return arguments;
    }

    private ArrayList<Argument> setResponseFields(Field[] responseFields, Class<?> responseClas) {
        ArrayList<Argument> arguments = new ArrayList<Argument>();
        ArrayList<Argument> sortedChildlessArguments = new ArrayList<Argument>();
        ArrayList<Argument> sortedArguments = new ArrayList<Argument>();

        Argument id = null;

        for (Field responseField : responseFields) {
            SerializedName nameAnnotation = responseField.getAnnotation(SerializedName.class);
            if (nameAnnotation != null) {
                Param paramAnnotation = responseField.getAnnotation(Param.class);
                Argument respArg = new Argument(nameAnnotation.value());

                boolean hasChildren = false;
                if (paramAnnotation != null && paramAnnotation.includeInApiDoc()) {
                    String description = paramAnnotation.description();
                    Class<?> fieldClass = paramAnnotation.responseObject();
                    if (description != null && !description.isEmpty()) {
                        respArg.setDescription(description);
                    }

                    respArg.setDataType(responseField.getType().getSimpleName().toLowerCase());

                    if (!paramAnnotation.since().isEmpty()) {
                        respArg.setSinceVersion(paramAnnotation.since());
                    }

                    if (fieldClass != null) {
                        Class<?> superClass = fieldClass.getSuperclass();
                        if (superClass != null) {
                            String superName = superClass.getName();
                            if (superName.equals(BaseResponse.class.getName())) {
                                ArrayList<Argument> fieldArguments = new ArrayList<Argument>();
                                Field[] fields = fieldClass.getDeclaredFields();
                                fieldArguments = setResponseFields(fields, fieldClass);
                                respArg.setArguments(fieldArguments);
                                hasChildren = true;
                            }
                        }
                    }
                }

                if (paramAnnotation != null && paramAnnotation.includeInApiDoc()) {
                    if (nameAnnotation.value().equals("id")) {
                        id = respArg;
                    } else {
                        if (hasChildren) {
                            respArg.setName(nameAnnotation.value() + "(*)");
                            sortedArguments.add(respArg);
                        } else {
                            sortedChildlessArguments.add(respArg);
                        }
                    }
                }
            }
        }

        Collections.sort(sortedArguments);
        Collections.sort(sortedChildlessArguments);

        if (id != null) {
            arguments.add(id);
        }
        arguments.addAll(sortedChildlessArguments);
        arguments.addAll(sortedArguments);

        if (responseClas.getName().equalsIgnoreCase(AsyncJobResponse.class.getName())) {
            Argument jobIdArg = new Argument("jobid", "the ID of the async job");
            arguments.add(jobIdArg);
        } else if (AsyncResponses.contains(responseClas.getName())) {
            Argument jobIdArg = new Argument("jobid", "the ID of the latest async job acting on this object");
            Argument jobStatusArg = new Argument("jobstatus", "the current status of the latest async job acting on this object");
            arguments.add(jobIdArg);
            arguments.add(jobStatusArg);
        }

        return arguments;
    }

    private static List<String> setAsyncResponses() {
        List<String> asyncResponses = new ArrayList<String>();
        asyncResponses.add(TemplateResponse.class.getName());
        asyncResponses.add(VolumeResponse.class.getName());
        //asyncResponses.add(LoadBalancerResponse.class.getName());
        asyncResponses.add(HostResponse.class.getName());
        asyncResponses.add(IPAddressResponse.class.getName());
        asyncResponses.add(StoragePoolResponse.class.getName());
        asyncResponses.add(UserVmResponse.class.getName());
        asyncResponses.add(SecurityGroupResponse.class.getName());
        //asyncResponses.add(ExternalLoadBalancerResponse.class.getName());
        asyncResponses.add(SnapshotResponse.class.getName());

        return asyncResponses;
    }
}
