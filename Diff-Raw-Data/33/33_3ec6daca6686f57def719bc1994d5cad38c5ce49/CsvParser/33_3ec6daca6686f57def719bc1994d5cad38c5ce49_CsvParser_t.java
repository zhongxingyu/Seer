 package at.ac.tuwien.ldsc.group1.application;
 
 import at.ac.tuwien.ldsc.group1.domain.components.Application;
 import at.ac.tuwien.ldsc.group1.domain.components.ApplicationImpl;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Read a CSV file with Application definition and create an instances of
  * an Applications
  *
  * @author Sebastian Geiger, Peter Patonai
  */
 public class CsvParser {
     private BufferedReader bufferedReader;
     private DataInputStream inputStream;
     private String filename;
 
     public CsvParser(String fileName) {
         setFileName(fileName);
     }
 
     public void setFileName(String fileName) {
         this.filename = fileName;
    }

    private void openFileStream() {
         try {
            FileInputStream fis = new FileInputStream(this.filename);
             inputStream = new DataInputStream(fis);
             bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
         } catch (FileNotFoundException e) {
            System.out.println("Unable to open file: " + this.filename);
         }
     }
 
     public List<Application> parse() {
        openFileStream(); // reopen the file stream each time we start a new simulation
        List<Application> appList = new ArrayList<>();
        int count = 0;
         try {
             String strLine;
             while ((strLine = bufferedReader.readLine()) != null) {
                 String[] parts = strLine.split(";");
                 try {
                     Long timeStamp = Long.parseLong(parts[0]);
                     Integer hddSize = Integer.parseInt(parts[1]);
                     Integer ram = Integer.parseInt(parts[2]);
                     Integer cpuInMhz = Integer.parseInt(parts[3]);
                     Long duration = Long.parseLong(parts[4]);
 
                     Application app = new ApplicationImpl(ram, hddSize, cpuInMhz, duration, timeStamp);
                     appList.add(app);
                    count++;
                 } catch (NumberFormatException e) {
                     continue; //skip header in the first row
                 }
             }
            bufferedReader.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
        assert (count == appList.size());
         return appList;
     }
 
     public String getFilename() {
         return filename;
     }
 }
