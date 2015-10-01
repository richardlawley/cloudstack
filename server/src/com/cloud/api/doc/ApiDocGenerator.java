package com.cloud.api.doc;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cloudstack.api.APICommand;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.cloud.utils.ReflectUtil;

public class ApiDocGenerator {

    public static void main(String[] args) {
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.DEBUG);
        console.activateOptions();
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);

        // Parse runtime arguments
        List<String> argsList = Arrays.asList(args);
        Iterator<String> iter = argsList.iterator();
        String[] fileNames = null;
        String dirName = null;
        while (iter.hasNext()) {
            String arg = iter.next();
            // populate the file names
            if (arg.equals("-f")) {
                fileNames = iter.next().split(",");
            }
            if (arg.equals("-d")) {
                dirName = iter.next();
            }
        }

        if ((fileNames == null) || (fileNames.length == 0)) {
            System.out.println("Please specify input file(s) separated by coma using -f option");
            System.exit(2);
        }

        // Create Command objects for each API method
        Set<Class<?>> cmdClasses = ReflectUtil.getClassesWithAnnotation(APICommand.class, new String[] {
                "org.apache.cloudstack.api",
                "com.cloud.api",
                "com.cloud.api.commands",
                "com.globo.globodns.cloudstack.api",
                "org.apache.cloudstack.network.opendaylight.api",
                "com.cloud.api.commands.netapp",
                "org.apache.cloudstack.api.command.admin.zone",
                "org.apache.cloudstack.network.contrail.api.command"});

        //ApiParser apiWriter = new ApiXmlDocWriter(cmdClasses);
        ApiParser apiWriter = new ApiNetSdkWriter(cmdClasses);

        for (String fileName : fileNames) {
            apiWriter.LoadCommandsFromFile(fileName);
        }

        apiWriter.WriteResults(dirName);
    }
}