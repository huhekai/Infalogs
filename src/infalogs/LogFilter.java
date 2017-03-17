package infalogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFilter {

    public static boolean isInteresting(LogEntry e, List<String> filter) {
        for (String s : filter) {
            if (e.message.startsWith(s) || e.severity.startsWith("ERR")) {
                return true;
            }
        }
        return false;
    }
    
    public List<LogEntry> filter(List<LogEntry> list, List<String> filter) {
        List<LogEntry> filteredList = new ArrayList<LogEntry>();
        for (LogEntry e : list) {
            for (String s : filter) {
                if (e.message.startsWith(s)) {
                    filteredList.add(e);
                }
            }
        }
        return filteredList;
    }

    public List<LogEntry> getErrors(List<LogEntry> list) {
        List<LogEntry> filteredList = new ArrayList<LogEntry>();
        for (LogEntry e : list) {
            if (e.severity.startsWith("ERR")) {
                filteredList.add(e);
            }
        }
        return filteredList;
    }

    public Map<String, Integer> groupErrors(List<LogEntry> list) {
        Map<String, Integer> result = new HashMap<String, Integer>();

        for (LogEntry e : list) {
            if (e.severity.startsWith("ERR")) {
                // pattern \[\d*\]
                String msg = (e.message.length() < InfaLogs.INFALOG_MAX_LEN) ? e.message.substring(0, e.message.length()) : e.message.substring(0, InfaLogs.INFALOG_MAX_LEN);
                String patternString = "\\[\\d*\\]";
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(msg);
                msg = matcher.replaceAll("X");
                
                String patternString2 = "\\d.*";
                pattern = Pattern.compile(patternString2);
                matcher = pattern.matcher(msg);
                msg = matcher.replaceAll("X");

                Integer count = result.get(msg);
                if (count == null) {
                    result.put(msg, new Integer(1));
                } else {
                    result.put(msg, new Integer(count + 1));
                }

            }
        }
        return result;
    }
}
