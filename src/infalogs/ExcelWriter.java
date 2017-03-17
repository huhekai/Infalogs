package infalogs;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableCellFormat;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelWriter {

    public void write(List<InfaLogs> logs, String path) throws IOException {
        try {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            String pvm = df.format(new Date());
            //WritableWorkbook workbook = Workbook.createWorkbook(new File(path + "\\report_" + pvm + ".xls"));
            WritableWorkbook workbook = Workbook.createWorkbook(new File(path + "\\raportti.xls"));
            WritableSheet sheet = workbook.createSheet("Raportti " + pvm, 0);
            WritableSheet errorSheet = workbook.createSheet("Virheet",1);
            sheet.setColumnView(0, 20);
            sheet.setColumnView(1, 160);
            errorSheet.setColumnView(0, 20);
            errorSheet.setColumnView(1, 160);
            //sheet.setColumnView(2, 120);
            WritableCellFormat wrappedText = new WritableCellFormat(WritableWorkbook.ARIAL_10_PT);
            wrappedText.setBackground(Colour.VERY_LIGHT_YELLOW);
            wrappedText.setWrap(true);

            WritableCellFormat errorText = new WritableCellFormat(WritableWorkbook.ARIAL_10_PT);
            errorText.setBackground(Colour.ROSE);
            errorText.setWrap(true);

            // print logs
            int i = 0;
            for (InfaLogs log : logs) {
                boolean once = true;
                for (LogEntry entry : log.getStats()) {
                    if (once) {
                        Label label = new Label(0, i, entry.date);
                        sheet.addCell(label);
                        once = false;
                    }
                    if (entry.toString().startsWith("Table:")) {
                        Label label = new Label(1, i++, entry.toString(), wrappedText);
                        sheet.addCell(label);
                    } else {
                        Label label = new Label(1, i++, entry.toString());
                        sheet.addCell(label);
                    }
                }

                long diff = log.getEndTime().getTime() - log.getStartTime().getTime();
//                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
//                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff); 

                Label labelRunningTime = new Label(0, i, String.format("Duration: %d m, %d s",
                        TimeUnit.MILLISECONDS.toMinutes(diff),
                        TimeUnit.MILLISECONDS.toSeconds(diff)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))));
                sheet.addCell(labelRunningTime);

                if (!log.isErrors()) {
                    Label label = new Label(1, i++, "No errors.");
                    sheet.addCell(label);
                } else {

                    Map<String, Integer> errorCounts = log.getErrors();
                    Label label = new Label(1, i++, "Workflow errors:");
                    sheet.addCell(label);

                    Iterator<Map.Entry<String, Integer>> iter = errorCounts.entrySet().iterator();

                    while (iter.hasNext()) {
                        Map.Entry<String, Integer> entry = iter.next();
                        StringBuilder sb = new StringBuilder();
                        sb.append("'");
                        sb.append(entry.getKey());
                        if (((String) entry.getKey()).length() == InfaLogs.INFALOG_MAX_LEN) {
                            sb.append(" ...'");
                        } else {
                            sb.append(" '");
                        }
                        sb.append(" ");
                        sb.append(entry.getValue());
                        sb.append(" pcs");

                        Label labelErr = new Label(1, i++, sb.toString(), errorText);
                        sheet.addCell(labelErr);

                    }
                }
                Label labelEmptyrow = new Label(0, i++, "");
                sheet.addCell(labelEmptyrow);
            }
                        
            // write errors to another tab
            int j = 0;
            for (InfaLogs log : logs) {
                if (log.isErrors()) {                    
                    boolean once = true;
                    for (LogEntry entry : log.getStats()) {
                        if (once) {
                            Label label = new Label(0, j, entry.date);
                            errorSheet.addCell(label);
                            once = false;
                        }
                        if (entry.toString().startsWith("Table:")) {
                            Label label = new Label(1, j++, entry.toString(), wrappedText);
                            errorSheet.addCell(label);
                        } else {
                            Label label = new Label(1, j++, entry.toString());
                            errorSheet.addCell(label);
                        }
                    }

                    Map<String, Integer> errorCounts = log.getErrors();
                    Label label = new Label(1, j++, "Workflow errors:");
                    errorSheet.addCell(label);

                    Iterator<Map.Entry<String, Integer>> iter = errorCounts.entrySet().iterator();

                    while (iter.hasNext()) {
                        Map.Entry<String, Integer> entry = iter.next();
                        StringBuilder sb = new StringBuilder();
                        sb.append("'");
                        sb.append(entry.getKey());
                        if (((String) entry.getKey()).length() == InfaLogs.INFALOG_MAX_LEN) {
                            sb.append(" ...'");
                        } else {
                            sb.append(" '");
                        }
                        sb.append(" ");
                        sb.append(entry.getValue());
                        sb.append(" pcs");

                        Label labelErr = new Label(1, j++, sb.toString(), errorText);
                        errorSheet.addCell(labelErr);

                        Label labelEmptyrow = new Label(0, j++, "");
                        errorSheet.addCell(labelEmptyrow);
                    }
                }
            }          
            
            workbook.write();
            workbook.close();
        } catch (WriteException ex) {
            Logger.getLogger(ExcelWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
