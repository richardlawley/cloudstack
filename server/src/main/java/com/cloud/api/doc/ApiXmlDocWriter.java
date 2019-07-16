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

import com.cloud.alert.AlertManager;
import com.cloud.serializer.Param;
import com.cloud.utils.IteratorUtil;
import com.cloud.utils.ReflectUtil;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.xstream.XStream;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.AsyncJobResponse;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ApiXmlDocWriter extends ApiProcessor {
    public static final Logger s_logger = Logger.getLogger(ApiXmlDocWriter.class.getName());

    private String _dirName = "";

    public ApiXmlDocWriter(String dirName) {
        _dirName = dirName;
    }

    @Override
    public void ProcessApiCommands() throws IOException, ClassNotFoundException {
        // Create object writer
        XStream xs = new XStream();
        xs.alias("command", Command.class);
        xs.alias("arg", Argument.class);
        String xmlDocDir = _dirName + "/xmldoc";
        String rootAdminDirName = xmlDocDir + "/apis";
        (new File(rootAdminDirName)).mkdirs();

        ObjectOutputStream out = xs.createObjectOutputStream(new FileWriter(_dirName + "/commands.xml"), "commands");
        ObjectOutputStream rootAdmin = xs.createObjectOutputStream(new FileWriter(rootAdminDirName + "/" + "apiSummary.xml"), "commands");
        ObjectOutputStream rootAdminSorted = xs.createObjectOutputStream(new FileWriter(rootAdminDirName + "/" + "apiSummarySorted.xml"), "commands");

        Iterator<?> it = _allApiCommands.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            // Write admin commands
            writeCommand(out, key);
            writeCommand(rootAdmin, key);
            // Write single commands to separate xml files
            ObjectOutputStream singleRootAdminCommandOs = xs.createObjectOutputStream(new FileWriter(rootAdminDirName + "/" + key + ".xml"), "command");
            writeCommand(singleRootAdminCommandOs, key);
            singleRootAdminCommandOs.close();
        }

        // Write sorted commands
        it = _allApiCommandsSorted.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            writeCommand(rootAdminSorted, key);
        }

        out.close();
        rootAdmin.close();
        rootAdminSorted.close();

        // write alerttypes to xml
        writeAlertTypes(xmlDocDir);
    }

    private void writeCommand(ObjectOutputStream out, String command) throws ClassNotFoundException, IOException {
        Class<?> clas = Class.forName(_allApiCommands.get(command));
        ArrayList<Argument> request = new ArrayList<Argument>();
        ArrayList<Argument> response = new ArrayList<Argument>();

        // Create a new command, set name/description/usage
        Command apiCommand = new Command();
        apiCommand.setName(command);

        APICommand impl = clas.getAnnotation(APICommand.class);
        if (impl == null) {
            impl = clas.getSuperclass().getAnnotation(APICommand.class);
        }

        if (impl == null) {
            throw new IllegalStateException(String.format("An %1$s annotation is required for class %2$s.", APICommand.class.getCanonicalName(), clas.getCanonicalName()));
        }

        if (impl.includeInApiDoc()) {
            String commandDescription = impl.description();
            if (commandDescription != null && !commandDescription.isEmpty()) {
                apiCommand.setDescription(commandDescription);
            } else {
                System.out.println("Command " + apiCommand.getName() + " misses description");
            }

            String commandUsage = impl.usage();
            if (commandUsage != null && !commandUsage.isEmpty()) {
                apiCommand.setUsage(commandUsage);
            }

            //Set version when the API is added
            if (!impl.since().isEmpty()) {
                apiCommand.setSinceVersion(impl.since());
            }

            boolean isAsync = ReflectUtil.isCmdClassAsync(clas, new Class<?>[] {BaseAsyncCmd.class, BaseAsyncCreateCmd.class});

            apiCommand.setAsync(isAsync);

            Set<Field> fields = ReflectUtil.getAllFieldsForClass(clas, new Class<?>[] {BaseCmd.class, BaseAsyncCmd.class, BaseAsyncCreateCmd.class});

            request = setRequestFields(fields);

            // Get response parameters
            Class<?> responseClas = impl.responseObject();
            Field[] responseFields = responseClas.getDeclaredFields();
            response = setResponseFields(responseFields, responseClas);

            apiCommand.setRequest(request);
            apiCommand.setResponse(response);

            out.writeObject(apiCommand);
        } else {
            s_logger.debug("Command " + command + " is not exposed in api doc");
        }
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

    private static void writeAlertTypes(String dirName) {
        XStream xs = new XStream();
        xs.alias("alert", Alert.class);
        try(ObjectOutputStream out = xs.createObjectOutputStream(new FileWriter(dirName + "/alert_types.xml"), "alerts");) {
            for (Field f : AlertManager.class.getFields()) {
                if (f.getClass().isAssignableFrom(Number.class)) {
                    String name = f.getName().substring(11);
                    Alert alert = new Alert(name, f.getInt(null));
                    out.writeObject(alert);
                }
            }
        } catch (IOException e) {
            s_logger.error("Failed to create output stream to write an alert types ", e);
        } catch (IllegalAccessException e) {
            s_logger.error("Failed to read alert fields ", e);
        }
    }

    public static void main(String[] args) {
        String dirName = "";
        List<String> argsList = Arrays.asList(args);
        Iterator<String> iter = argsList.iterator();
        while (iter.hasNext()) {
            String arg = iter.next();
            if (arg.equals("-d")) {
                dirName = iter.next();
            }
        }

        try {
            ApiXmlDocWriter processor = new ApiXmlDocWriter(dirName);
            processor.ProcessApiCommands();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(2);
        }
    }
}
