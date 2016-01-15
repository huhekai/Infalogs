package infalogs;

public class LogEntry {
    public String date;
    public String severity;
    public String code;
    public String service;
    public String message;
    
    public LogEntry() {
        date = "";
        severity = "";
        code = "";
        service = "";
        message = "";       
    }
    
    @Override
    public String toString() {
       return message;        
    }
}
