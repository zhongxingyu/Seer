 package com.treasure_data.file;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.CharsetDecoder;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.json.simple.JSONValue;
 import org.json.simple.parser.JSONParser;
 
 import com.treasure_data.commands.CommandException;
 import com.treasure_data.commands.Config;
 import com.treasure_data.commands.bulk_import.PreparePartsRequest;
 import com.treasure_data.commands.bulk_import.PreparePartsResult;
 
 public class JSONFileParser extends
         FileParser<PreparePartsRequest, PreparePartsResult> {
 
     private static final Logger LOG = Logger.getLogger(JSONFileParser.class.getName());
 
     private BufferedReader reader;
     private String aliasTimeColumn;
     private Long timeValue = new Long(-1);
 
     public JSONFileParser(PreparePartsRequest request) {
         super(request);
     }
 
     @Override
     public void initParser(CharsetDecoder decoder, InputStream in)
             throws CommandException {
         aliasTimeColumn = request.getAliasTimeColumn();
         timeValue = request.getTimeValue();
 
         BufferedReader sampleReader = new BufferedReader(new InputStreamReader(in, decoder));
         try {
             String jsonText = sampleReader.readLine();
             Map<String, Object> firstRow = (Map<String, Object>) JSONValue.parse(jsonText);
 
             // print sample row
             if (firstRow != null) {
                 JSONFileWriter w = new JSONFileWriter(request);
                 parseMap(w, firstRow);
                 String ret = JSONValue.toJSONString(w.getRecord());
                 LOG.info("sample row: " + ret);
             } else {
                 LOG.info("sample row is null");
             }
         } catch (IOException e) {
             throw new CommandException(e);
         } finally {
             if (sampleReader != null) {
                 try {
                     sampleReader.close();
                 } catch (IOException e) {
                     // ignore
                 }
             }
         }
     }
 
     @Override
     public void startParsing(CharsetDecoder decoder, InputStream in)
             throws CommandException {
         reader = new BufferedReader(new InputStreamReader(in, decoder));
     }
 
     @Override
     public boolean parseRow(
             @SuppressWarnings("rawtypes") com.treasure_data.file.FileWriter w)
             throws CommandException {
         Map<String, Object> row = null;
         try {
             String jsonText = reader.readLine();
            if (jsonText == null) {
                return false;
            }
             row = (Map<String, Object>) JSONValue.parse(jsonText);
         } catch (IOException e) {
             e.printStackTrace();
             String msg = String.format("reason: %s, line: %d",
                     e.getMessage(), getRowNum());
             writeErrorRecord(msg);
 
             LOG.warning("Skip row number: " + getRowNum());
             return true;
         }
 
         if (row == null || row.isEmpty()) {
             return false;
         }
 
         // increment row number
        incrRowNum();
 
         return parseMap(w, row);
     }
 
     private boolean parseMap(
             @SuppressWarnings("rawtypes") com.treasure_data.file.FileWriter w,
             Map<String, Object> row) throws CommandException {
         /**
         if (LOG.isLoggable(Level.FINE)) {
             LOG.fine(String.format("lineNo=%s, rowNo=%s, customerList=%s",
                     reader.getLineNumber(), reader.getRowNumber(),
                     row));
         }
          */
         /** DEBUG
         System.out.println(String.format("lineNo=%s, rowNo=%s, customerList=%s",
                 reader.getLineNumber(), reader.getRowNumber(), row));
          */
 
         try {
             int allSize = row.size();
             boolean hasTimeColumn = row.containsKey("time");
 
             if (!hasTimeColumn) {
                 w.writeBeginRow(allSize + 1);
             } else {
                 w.writeBeginRow(allSize);
             }
 
             long time = 0;
             for (Map.Entry<String, Object> e : row.entrySet()) {
                 String key = e.getKey();
                 Object val = e.getValue();
 
                 if (key.equals("time")) {
                     val = ((Number) val).longValue();
                 } else if (key.equals(aliasTimeColumn)) {
                     time = ((Number) val).longValue();
                 } else {
                     
                 }
 
                 w.write(key);
                 w.write(val);
             }
 
             if (!hasTimeColumn) {
                 w.write(Config.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE);
                 if (aliasTimeColumn != null) {
                     w.write(time);
                 } else if (timeValue > 0) {
                     w.write(timeValue);
                 } else {
                     throw new CommandException(
                             "'time' represented column not specified. --time-column or --time-value option is required");
                 }
             }
 
             w.writeEndRow();
 
             w.incrRowNum();
             return true;
         } catch (Exception e) {
             throw new CommandException(e);
         }
     }
 
     @Override
     public void close() throws CommandException {
         if (reader != null) {
             try {
                 reader.close();
             } catch (IOException e) {
                 throw new CommandException(e);
             }
         }
     }
 
 }
