 package de.atp.parser.csv;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import android.annotation.SuppressLint;
 import de.atp.controller.DataController;
 import de.atp.data.Row;
 import de.atp.data.RowStatus;
 import de.atp.parser.InvalidFormatException;
 import de.atp.parser.Parser;
 import de.atp.parser.RowConverter;
 
 @SuppressLint("DefaultLocale")
 public class CSVParser implements Parser, RowConverter {
 
     private File csvFile;
 
     private String[] head;
 
     /**
      * Create a parser to read and write a CSV file
      * 
      * @param probandCode
      *            The code of the proband - name conversion by task giver
      * @param head
      *            The head of the file - the first line
      */
     public CSVParser(String probandCode, String[] head) {
         this.head = head;
 
         this.csvFile = new File(DataController.getAppDir(), probandCode + ".csv");
         if (!csvFile.exists())
             create();
     }
 
     /**
      * Create an empty CSV file with the head
      */
     private void create() {
         write(Collections.<Row> emptyList());
     }
 
     /**
      * Read and parse from the CSV file to get a list of rows (a table)
      * 
      * @return A list of row - the table containing all data
      * @throws InvalidFormatException
      *             Thrown when invalid CSV format found
      */
     @Override
     public List<Row> parse() throws InvalidFormatException {
         List<Row> table = new ArrayList<Row>();
         try {
             BufferedReader bReader = new BufferedReader(new FileReader(csvFile));
             String line = bReader.readLine(); // Skip first line - the head
             if (line == null) {// file is empty!
                 bReader.close();
                 throw new InvalidFormatException("Invalid csv format!");
 
             }
             while ((line = bReader.readLine()) != null) {
                 Row row = readRow(line);
                 if (row == null) {
                     bReader.close();
                     throw new InvalidFormatException("Invalid csv format!");
 
                 }
                 table.add(readRow(line));
             }
             bReader.close();
         } catch (NumberFormatException e) {
             throw new InvalidFormatException("Invalid csv format!");
 
         } catch (IOException e) {
             e.printStackTrace();
             // TODO: handle exception
         }
         return table;
     }
 
     /**
      * Write the table to the CSV file
      * 
      * @param table
      *            The table
      */
     @Override
     public void write(List<Row> table) {
         try {
             BufferedWriter bWriter = new BufferedWriter(new FileWriter(csvFile));
             for (String header : head) {
                 bWriter.append(header);
                 bWriter.append(';');
             }
             bWriter.newLine();
             for (Row row : table) {
                 bWriter.append(writeRow(row));
                 bWriter.newLine();
             }
             bWriter.close();
         } catch (Exception e) {
             e.printStackTrace();
             // TODO: handle exception
         }
     }
 
     /**
      * Convert a row to a writable string in CSV format
      * 
      * @param row
      *            The row of the table
      * @return A string representing the data of the string in a CSV valid
      *         format
      */
     public String writeRow(Row row) {
         StringBuilder sBuilder = new StringBuilder();
         sBuilder.append(row.getCode()).append(';');
         sBuilder.append(DateFormat.getDateInstance().format(row.getDate().toDate())).append(';');
 
         sBuilder.append(TIME_FORMAT.print(row.getAlarmTime())).append(';');
         switch (row.getStatus()) {
             case ABORTED :
                 sBuilder.append("-1");
                 break;
             case DIRTY :
                 sBuilder.append("00:00");
                 break;
             case OK :
                sBuilder.append(TIME_FORMAT.print(row.getAnswerTime())).append(';');
                 break;
             default :
                 break;
         }
         sBuilder.append(';');
         sBuilder.append(row.getStatus().getStatus()).append(';');
         sBuilder.append(row.getContacts()).append(';');
         sBuilder.append(row.getHours()).append(';');
        sBuilder.append(row.getMinutes()).append(';');
 
         return sBuilder.toString();
     }
     /**
      * Pattern to split a single string - compile once to save performance
      */
     private final static Pattern SPLIT_PATTERN = Pattern.compile(";");
 
     private final static DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HH:mm");
 
     /**
      * Parse a Row from a single line
      * 
      * @param line
      *            The line to be parsed. Must be in CSV format.
      * @return <code>null</code> when format of the CSV is wrong or integer
      *         parsing failed
      */
     public Row readRow(String line) {
         try {
             String[] split = SPLIT_PATTERN.split(line);
             if (split == null || split.length != Row.DATA_LENGTH) {
                 return null;
             }
 
             int p = 0;
             String probandCode = split[p++];
             LocalDate day = new LocalDate(DateFormat.getDateInstance().parse(split[p++]));
             LocalTime alarmTime = new LocalTime(TIME_FORMAT.parseLocalTime(split[p++]));
             LocalTime answerTime = new LocalTime(TIME_FORMAT.parseLocalTime(split[p++]));
             RowStatus status = RowStatus.getStatus(Integer.parseInt(split[p++]));
             int contacts = Integer.parseInt(split[p++]);
             int hours = Integer.parseInt(split[p++]);
             int minutes = Integer.parseInt(split[p++]);
 
             return new Row(probandCode, day, alarmTime, answerTime, status, contacts, hours, minutes);
         } catch (Exception e) {
             return null;
         }
     }
 
     public File getFile() {
         return csvFile;
     }
 }
