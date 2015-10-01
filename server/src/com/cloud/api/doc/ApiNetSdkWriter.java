package com.cloud.api.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class ApiNetSdkWriter extends ApiParser {
    public static final Logger s_logger = Logger.getLogger(ApiNetSdkWriter.class.getName());
    private HashSet<String> _createdTypes = new HashSet<String>();

    public ApiNetSdkWriter(Set<Class<?>> cmdClasses) {
        super(cmdClasses);
    }

    @Override
    protected void WriteResultsCore(String baseDirectory, Map<String, Command> commands, List<String> allCommandNames,
            List<String> domainAdminCommandNames, List<String> userCommandNames) {

        File interfaceFile = new File(baseDirectory, "ICloudStackApi.cs");
        if (interfaceFile.exists()) {
            interfaceFile.delete();
        }

        PrintWriter writer;
        try {
            writer = new PrintWriter(interfaceFile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        IndentPrinter printer = new IndentPrinter(writer, "    ");
        printer.println("import System;");
        printer.println();
        printer.println("namespace CloudStack {");
        printer.incrementIndent();
        printer.println("public interface ICloudStackApi");
        printer.println("{");
        printer.incrementIndent();

        for (String commandName : allCommandNames) {
            Command command = commands.get(commandName);
            if (command == null) {
                s_logger.warn(String.format("Class for %s not found", commandName));
            } else {
                if (command.getDescription() != null) {
                    printer.println("/// <summary>");
                    printer.println("/// %s", command.getDescription().replace("\n", "\n        /// "));
                    printer.println("/// </summary>");
                }
                printer.println("void %s()", command.getName());

            }
        }

        printer.decrementIndent();
        printer.println("}");
        printer.decrementIndent();
        printer.println("}");
    }

}
