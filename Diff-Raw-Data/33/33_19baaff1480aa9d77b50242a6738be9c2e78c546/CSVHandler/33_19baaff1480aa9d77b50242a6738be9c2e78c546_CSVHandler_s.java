 package utils;
 
 import beans.DayBean;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author Marco Tinacci
  */
 public class CSVHandler {
 
 
    static public void writeArray(double[][] array, String fileName)
            throws FileNotFoundException, IOException{
       BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
       for(int i = 0; i < array.length; i++){
         bw.write(array[i].toString().replaceAll("[", "")
                 .replaceAll("]", "\n"));
       }
    }
 
    /**
     * metodo che legge i dati contenuti nel file CSV specificato e ritorna una
     * lista di record incapsulati
     * @param fileName nome del file CSV
     * @return lista di record
     * @throws FileNotFoundException sollevata su file non trovato
     * @throws IOException sollevata su problemi di lettura del file
     * @throws ParseException sollevata su errata interpretazione delle date
     */
    static public List<DayBean> readAll(String fileName)
            throws FileNotFoundException, IOException, ParseException{
 
       // formato data
       DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
       // lista di record
       LinkedList<DayBean> list = new LinkedList<DayBean>();
       // apri il file csv
       BufferedReader br = new BufferedReader(new FileReader(fileName));
       String row = "";
 
       // salta la prima riga di titolo
       br.readLine();
       // leggi il file riga per riga
       while((row = br.readLine()) != null)
       {
          //System.out.println("Riga: "+row);
          
          DayBean day = new DayBean();
          // dividi la linea in campi
          String[] raw = row.split(",");
 
          day.setDate(df.parse(raw[0]));               // date
          day.setOpen(Float.parseFloat(raw[1]));       // open
          day.setHigh(Float.parseFloat(raw[2]));       // high
          day.setLow(Float.parseFloat(raw[3]));        // low
          day.setClose(Float.parseFloat(raw[4]));      // close
          day.setVolume(Integer.parseInt(raw[5]));     // volume
          day.setAdjClose(Float.parseFloat(raw[6]));   // adj close
 
          // aggiungi record alla lista, invertendo l'ordine
          list.addFirst(day);
       }
       return list;
    }
 }
