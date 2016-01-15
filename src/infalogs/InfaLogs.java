package infalogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            parseTimestamp();

        } catch (IOException ex) {
            System.err.println(ex.getLocalizedMessage());
            success = false;
        } catch (ParseException ex) {
            System.err.println(ex.getLocalizedMessage());
            success = false;
        }
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
        InputStream fis;
        BufferedReader br;
        fis = new FileInputStream(file);
        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

        logs = parseLoadSummary(br);
        br.close();

        stats = logFilter.filter(logs, filters);
        errors = logFilter.getErrors(logs);
        logs.clear();
        logs = null;
    }

    private List<LogEntry> parseLoadSummary(BufferedReader br) throws IOException {
        boolean first = true;

        List<LogEntry> list = new ArrayList<LogEntry>();
        LogEntry le = new LogEntry();
        String line;
        
        while ((line = br.readLine()) != null) {
            if (line.length() > 14) {
                String sevr = line.substring(10, 14);
                sevr = sevr.trim();

                if (!sevr.isEmpty()) {
                    if (first) {
                        first = false;
                    } else {     
                        list.add(le);
                        le = new LogEntry();
                    }
                }
                if (line.length() >= 10) {
                    le.date += ((line.substring(0, 10) == null) ? "" : line.substring(0, 10).trim());
                    le.date += " ";
                }
                if (line.length() >= 15) {
                    le.severity += ((line.substring(10, 15) == null) ? "" : line.substring(10, 15).trim());
                }
                if (line.length() >= 20) {
                    le.code += ((line.substring(15, 20) == null) ? "" : line.substring(15, 20).trim());
                }
                if (line.length() >= 30) {
                    le.service += ((line.substring(20, 30) == null) ? "" : line.substring(20, 30).trim());
                }
                if (line.length() >= 30) {
                    le.message += ((line.substring(30) == null) ? "" : line.substring(30).trim());
                    le.message += " ";
                }                             
            }
        }
        return list;
    }
    private void parseTimestamp() throws ParseException {
        if (stats.size() > 0) {
            LogEntry le = stats.get(0);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ", Locale.ENGLISH);
            startTime = df.parse(le.date);
            le = stats.get(stats.size()-1);
            endTime = df.parse(le.date);
        }
    }

    @Override
    public int compareTo(InfaLogs o) {
        return startTime.compareTo(o.getStartTime());
    }
}
