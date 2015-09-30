package com.cloud.api.doc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.SecurityGroupResponse;
import org.apache.cloudstack.api.response.SnapshotResponse;
import org.apache.cloudstack.api.response.StoragePoolResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.log4j.Logger;

public abstract class ApiParser {
    public static final Logger s_logger = Logger.getLogger(ApiParser.class.getName());

    private static final short DOMAIN_ADMIN_COMMAND = 4;
    private static final short USER_COMMAND = 8;

    private Map<String, Class<?>> _apiNameCmdClassMap = new HashMap<String, Class<?>>();
    private List<String> _allApiCommands = new ArrayList<String>();
    private List<String> _domainAdminApiCommands = new ArrayList<String>();
    private List<String> _regularUserApiCommands = new ArrayList<String>();
    private String _dirName = "";

    private ApiCommandParser _commandParser;

    public ApiParser(Set<Class<?>> cmdClasses) {
        _commandParser = new ApiCommandParser();

        // API methods returning async get some additional properties
        _commandParser.RegisterAsyncResponseClass(TemplateResponse.class.getName());
        _commandParser.RegisterAsyncResponseClass(VolumeResponse.class.getName());
        _commandParser.RegisterAsyncResponseClass(HostResponse.class.getName());
        _commandParser.RegisterAsyncResponseClass(IPAddressResponse.class.getName());
        _commandParser.RegisterAsyncResponseClass(StoragePoolResponse.class.getName());
        _commandParser.RegisterAsyncResponseClass(UserVmResponse.class.getName());
        _commandParser.RegisterAsyncResponseClass(SecurityGroupResponse.class.getName());
        _commandParser.RegisterAsyncResponseClass(SnapshotResponse.class.getName());

        for (Class<?> cmdClass : cmdClasses) {
            String apiName = cmdClass.getAnnotation(APICommand.class).name();
            if (_apiNameCmdClassMap.containsKey(apiName)) {
                // handle API cmd separation into admin cmd and user cmd with the common api name
                Class<?> curCmd = _apiNameCmdClassMap.get(apiName);
                if (curCmd.isAssignableFrom(cmdClass)) {
                    // api_cmd map always keep the admin cmd class to get full response and parameters
                    _apiNameCmdClassMap.put(apiName, cmdClass);
                } else if (cmdClass.isAssignableFrom(curCmd)) {
                    // just skip this one without warning
                    continue;
                } else {
                    System.out.println("Warning, API Cmd class " + cmdClass.getName() + " has non-unique apiname " + apiName);
                    continue;
                }
            } else {
                _apiNameCmdClassMap.put(apiName, cmdClass);
            }
        }
    }

    public void LoadCommandsFromFile(String fileName) {
        LinkedProperties properties = new LinkedProperties();
        try(FileInputStream in = new FileInputStream(fileName);) {
            properties.load(in);
        } catch (FileNotFoundException ex) {
            System.out.println("Can't find file " + fileName);
            System.exit(2);
        } catch (IOException ex1) {
            System.out.println("Error reading from file " + ex1);
            System.exit(2);
        }

        for (String key : (List<String>) (List) properties.keys) {
            String preProcessedCommand = properties.getProperty(key);
            int splitIndex = preProcessedCommand.lastIndexOf(";");
            String commandRoleMask = preProcessedCommand.substring(splitIndex + 1);

            short cmdPermissions = 1;
            if (commandRoleMask != null) {
                cmdPermissions = Short.parseShort(commandRoleMask);
            }

            AddCommandToParse(key, cmdPermissions);
        }
    }

    public void AddCommandToParse(String commandName, short permissionMask) {
        _allApiCommands.add(commandName);

        if ((permissionMask & DOMAIN_ADMIN_COMMAND) != 0) {
            _domainAdminApiCommands.add(commandName);
        }
        if ((permissionMask & USER_COMMAND) != 0) {
            _regularUserApiCommands.add(commandName);
        }
    }

    public Map<String, Command> ParseCommands() {
        Map<String, Command> results = new HashMap<String, Command>();

        for (String commandName: _allApiCommands) {
            if (!_apiNameCmdClassMap.containsKey(commandName)) {
                s_logger.warn("Implementation class not found for api command " + commandName);
            } else {
                System.out.println("Parsed api command" + commandName);
                results.put(commandName, _commandParser.ParseCommand(commandName,  _apiNameCmdClassMap.get(commandName)));
            }
        }

        return results;
    }

    public void WriteResults(String baseDirectory) {
        Map<String, Command> commands = ParseCommands();

        WriteResultsCore(baseDirectory, commands, _allApiCommands, _domainAdminApiCommands, _regularUserApiCommands);
    }

    protected abstract void WriteResultsCore(String baseDirectory, Map<String, Command> commands, List<String> allCommandNames, List<String> domainAdminCommandNames, List<String> userCommandNames);

    private static class LinkedProperties extends Properties {
        private final LinkedList<Object> keys = new LinkedList<Object>();

        @Override
        public Enumeration<Object> keys() {
            return Collections.<Object> enumeration(keys);
        }

        @Override
        public Object put(Object key, Object value) {
            // System.out.println("Adding key" + key);
            keys.add(key);
            return super.put(key, value);
        }
    }

}
