package infalogs;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XMLLogParser implements javax.xml.stream.StreamFilter {

    public List<LogEntry> parse(FileInputStream fis, List<String> filters) throws IOException {
        XMLInputFactory xmlif = null;
        try {
            xmlif = XMLInputFactory.newInstance();
            xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
            xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            xmlif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getLocalizedMessage());
            throw new IOException(ex);
        }

        List<LogEntry> list = new ArrayList<LogEntry>();

        try {
            LogEntry entry;
            XMLStreamReader xmlr = xmlif.createFilteredReader(xmlif.createXMLStreamReader(fis), new XMLLogParser());

            int eventType = xmlr.getEventType();
            while (xmlr.hasNext()) {
                eventType = xmlr.next();
                if (xmlr.isStartElement()) {
                    entry = readEntry(xmlr);
                    if (entry != null) {
                        if (!entry.severity.isEmpty()) {
                            if (LogFilter.isInteresting(entry, filters)) {                                
                                list.add(entry);
                            }
                        }
                    }
                }
            }

        } catch (XMLStreamException ex) {
            System.err.println(ex.getLocalizedMessage());
            throw new IOException(ex);
        }
        return list;
    }

    private LogEntry readEntry(XMLStreamReader xmlr) {
        if (xmlr.getAttributeCount() > 0) {
            LogEntry entry = new LogEntry();

            int count = xmlr.getAttributeCount();
            for (int i = 0; i < count; i++) {

                QName name = xmlr.getAttributeName(i);
                String value = xmlr.getAttributeValue(i);

                if (name.getLocalPart().equals("severity")) {
                    if (value.equalsIgnoreCase("1")) {
                        entry.severity = "ERR";
                    } else
                        entry.severity = value;
                } else if (name.getLocalPart().equals("message")) {
                    entry.message = value;
                } else if (name.getLocalPart().equals("timestamp")) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ", Locale.ENGLISH);
                    entry.date = df.format(new Date(Long.parseLong(value))); 
                } else if (name.getLocalPart().equals("messagecode")) {
                    entry.code = value;
                }
            }
            return entry;
        }
        return null;
    }

    @Override
    public boolean accept(XMLStreamReader reader) {
        if (!reader.isStartElement() && !reader.isEndElement()) {
            return false;
        } else {
            return true;
        }

    }
}
