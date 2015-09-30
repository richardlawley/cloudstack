package com.cloud.api.doc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.AsyncJobResponse;
import org.apache.log4j.Logger;

import com.cloud.serializer.Param;
import com.cloud.utils.IteratorUtil;
import com.cloud.utils.ReflectUtil;
import com.google.gson.annotations.SerializedName;

public class ApiCommandParser {
    public static final Logger s_logger = Logger.getLogger(ApiCommandParser.class.getName());
    private List<String> _asyncResponseClasses = new ArrayList<String>();

    /**
     * Register a response class as asynchronous
     * @param className
     */
    public void RegisterAsyncResponseClass(String className) {
        _asyncResponseClasses.add(className);
    }

    public Command ParseCommand(String name, Class<?> implementation) {
        ArrayList<Argument> request = new ArrayList<Argument>();
        ArrayList<Argument> response = new ArrayList<Argument>();

        // Create a new command, set name/description/usage
        Command apiCommand = new Command();
        apiCommand.setName(name);

        APICommand annotation = implementation.getAnnotation(APICommand.class);
        if (annotation == null) {
            annotation = implementation.getSuperclass().getAnnotation(APICommand.class);
        }

        if (annotation == null) {
            throw new IllegalStateException(String.format("An %1$s annotation is required for class %2$s.", APICommand.class.getCanonicalName(), implementation.getCanonicalName()));
        }

        if (annotation.includeInApiDoc()) {
            String commandDescription = annotation.description();
            if (commandDescription != null && !commandDescription.isEmpty()) {
                apiCommand.setDescription(commandDescription);
            } else {
                System.out.println("Command " + apiCommand.getName() + " misses description");
            }

            String commandUsage = annotation.usage();
            if (commandUsage != null && !commandUsage.isEmpty()) {
                apiCommand.setUsage(commandUsage);
            }

            //Set version when the API is added
            if (!annotation.since().isEmpty()) {
                apiCommand.setSinceVersion(annotation.since());
            }

            boolean isAsync = ReflectUtil.isCmdClassAsync(implementation, new Class<?>[] {BaseAsyncCmd.class, BaseAsyncCreateCmd.class});

            apiCommand.setAsync(isAsync);

            Set<Field> fields = ReflectUtil.getAllFieldsForClass(implementation, new Class<?>[] {BaseCmd.class, BaseAsyncCmd.class, BaseAsyncCreateCmd.class});

            request = setRequestFields(fields);

            // Get response parameters
            Class<?> responseClas = annotation.responseObject();
            Field[] responseFields = responseClas.getDeclaredFields();
            response = setResponseFields(responseFields, responseClas);

            apiCommand.setRequest(request);
            apiCommand.setResponse(response);

            return apiCommand;
        } else {
            s_logger.debug("Command " + name + " is not exposed in api doc");
            return null;
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
        } else if (_asyncResponseClasses.contains(responseClas.getName())) {
            Argument jobIdArg = new Argument("jobid", "the ID of the latest async job acting on this object");
            Argument jobStatusArg = new Argument("jobstatus", "the current status of the latest async job acting on this object");
            arguments.add(jobIdArg);
            arguments.add(jobStatusArg);
        }

        return arguments;
    }
}
