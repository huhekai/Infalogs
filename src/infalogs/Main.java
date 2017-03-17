package infalogs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {

        if (args.length != 1) {
            return;
        }
        List<InfaLogs> logs = new ArrayList<InfaLogs>();

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        };

        List<String> filters = new ArrayList<String>();
        filters.add("Workflow:");
        filters.add("Mapping name:");
        filters.add("Session run completed successfully.");
        filters.add("Source Load Summary.");
        filters.add("Target Load Summary.");
        filters.add("Table:");

        File folder = new File(args[0]);
        File[] listOfFiles = folder.listFiles(filter);
        for (File file : listOfFiles) {
            InfaLogs log = new InfaLogs(file, filters);

            if (log.isSuccessful()) {
                logs.add(log);
            }
        }

        // sort logs
        Collections.sort(logs);

        // write logs to xls-file
        ExcelWriter writer = new ExcelWriter();
        try {
            writer.write(logs, args[0]);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
}
