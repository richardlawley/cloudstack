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

    }

   
}
