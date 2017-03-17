package infalogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InfaLogs implements Comparable<InfaLogs> {

    public static int INFALOG_MAX_LEN = 1024;

    private final File file;
    private List<LogEntry> logs;
    private List<LogEntry> stats;
    private List<LogEntry> errors;
    private final List<String> filters;
    private boolean success = true;
    private Date startTime, endTime;
    private final LogFilter logFilter = new LogFilter();

    public InfaLogs(File file, List<String> filters) {
        this.file = file;
        this.filters = filters;
        processLogfile();
    }

    private void processLogfile() {
        try {
            readFile();
            if (stats.size() > 0) {
                startTime = parseTimestamp(stats.get(0));
                endTime = parseTimestamp(stats.get(stats.size()-1));
            }
        } catch (IOException ex) {
            System.err.println(ex.getLocalizedMessage());
            success = false;
        } catch (ParseException ex) {
            System.err.println(ex.getLocalizedMessage());
            success = false;
        }
    }

    public Date parseTimestamp(LogEntry entry) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ", Locale.ENGLISH);
        return df.parse(entry.date);
    }
    
    public boolean isSuccessful() {
        return success;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
    
    public boolean isErrors() {
        if (errors.size() > 0) {
            return true;
        }
        return false;
    }

    public List<LogEntry> getStats() {
        return stats;
    }

    public Map<String, Integer> getErrors() {
        return logFilter.groupErrors(errors);
    }

    private void readFile() throws FileNotFoundException, IOException {
        FileInputStream fis;
        XMLLogParser parser = new XMLLogParser();
        fis = new FileInputStream(file);
       
        logs = parser.parse(fis, filters);
        
        stats = logFilter.filter(logs, filters);
        errors = logFilter.getErrors(logs);
        fis.close();
        logs.clear();
        logs = null;
    }

    @Override
    public int compareTo(InfaLogs o) {
        return startTime.compareTo(o.getStartTime());
    }
}
