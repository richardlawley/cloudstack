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
import com.thoughtworks.xstream.XStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

        // Create a new command, set name/description/usage
        Command apiCommand = new Command(command, Class.forName(_allApiCommands.get(command)));

        if (apiCommand.getIncludeInDocumentation()) {
            out.writeObject(apiCommand);
        } else {
            s_logger.debug("Command " + command + " is not exposed in api doc");
        }
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
