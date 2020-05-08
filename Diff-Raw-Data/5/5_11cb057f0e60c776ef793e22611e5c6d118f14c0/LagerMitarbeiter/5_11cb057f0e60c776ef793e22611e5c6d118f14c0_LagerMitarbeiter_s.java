 package tgm.sew.hit.roboterfabrik;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 
 /**
  * Stellt einen Lager Mitarbeiter dar,
  * welcher sich um die Lagerung der Teile kümmert
  * 
  * @author Dominik
 * @version 0.9
  */
 public class LagerMitarbeiter {
     
     private File lagerFolder;
     private Map<TeilType,File> teilFiles = new HashMap<>();
     private File threadeeFile;
     
     private static final Logger logger = Logger.getLogger("Fabriklog");
 
     
     /**
      * Konstrutor des LagerMitarbeiters
      */
     public LagerMitarbeiter(File lagerFolder) {
         this.lagerFolder = lagerFolder;
         File f;
         for (TeilType tt : TeilType.values()) {
             f = new File(lagerFolder.getAbsolutePath() + File.separator + tt.filename() + ".csv");
             teilFiles.put(tt, f);
             try {
                 f.createNewFile();
             } catch (IOException ex) {
             }
         }
         threadeeFile = new File(lagerFolder.getAbsolutePath() + File.separator + "threadee.csv");
         try {
             threadeeFile.createNewFile();
         } catch (IOException ex) {
         }
         logger.log(Level.INFO, "Datein geladen");
     }
     
     /**
      * Lagert ein Teil ein
      * @param teil das Teil, welches gelagert werden soll
      */
     public void lagerTeil(Teil teil) {
         BufferedWriter w;
         try {
             w = new BufferedWriter(new FileWriter(teilFiles.get(teil.getType()),true));
             StringBuilder sb = new StringBuilder(teil.getType().casename());
             for (int i : teil.getZahlenList()) sb.append(",").append(i);
             w.write(sb.toString() + System.lineSeparator());
             w.flush();
             w.close();
             logger.log(Level.INFO, teil.getType().casename() + " eingelagert");
         } catch (IOException ex) {
             logger.log(Level.ERROR, "Daten konnten nicht gespeichert werden");
         }
     }
     
     /**
      * Liest ein Teil ein
      * @param teilType welche Art von Teil geladen werden soll
      * @return ein Teil des entsprechenden TeilTypes aus dem Lager
      */
     public Teil leseTeil(TeilType teilType) {
         File f = teilFiles.get(teilType);
         List<Integer> li = new ArrayList<>();
         BufferedReader r;
         try {
             r = new BufferedReader(new FileReader(f));
             String line = r.readLine();
             if (line == null || line.equals("")) return null;
             StringTokenizer st = new StringTokenizer(line,",");
             st.nextToken();
             while (st.hasMoreTokens()) {
                 try {
                     String s = st.nextToken();
                     li.add(Integer.parseInt(s));
                 } catch (NumberFormatException nfe) {
                     logger.log(Level.ERROR, "Corrupted File, keine Nummer: " + teilType.filename() + ".csv\n");
                 }
             }
             r.close();
             deleteLine(f);
         } catch (FileNotFoundException ex) {
             logger.log(Level.ERROR, "File wurde nicht gefunden: " + teilType.filename() + ".csv");
         } catch (IOException ex) {
             logger.log(Level.ERROR, "Fehler beim bearbeiten der Datei: " + teilType.filename() + ".csv");
         }
         return new Teil(teilType,li);
     }
     
     /**
      * Löscht eine Zeile aus dem File 
      * @param f das File aus welchem die Zeile gelöscht werden soll
      */
     public void deleteLine(File f) {
         File nf = new File(f.getAbsolutePath() + ".temp");
         try {
             BufferedReader r = new BufferedReader(new FileReader(f));
             BufferedWriter w = new BufferedWriter(new FileWriter(nf));
             r.readLine();
             for (String s = "";s != null; s = r.readLine()) {
                 if (!s.equals("")) {
                     w.write(s + System.lineSeparator());
                     w.flush();
                 }
             }
             w.close();
             r.close();
             f.delete();
             nf.renameTo(f);
         } catch (FileNotFoundException ex) {
             logger.log(Level.ERROR, "File wurde nicht gefunden: " + f.getName());
         } catch (IOException ex) {
             logger.log(Level.ERROR, "Fehler beim bearbeiten der Datei: " + nf.getName());
         }
     }
 
     /**
      * Lagert einen Threadee
      * @param threadee der Threadee welcher gelagert werden soll
      */
     public void lagerThreadee(Threadee threadee) {
         BufferedWriter w;
         try {
            w = new BufferedWriter(new FileWriter(threadeeFile));
             StringBuilder sb = new StringBuilder("Threadee-ID" + threadee.getID() + ",Mitarbeiter-ID" + threadee.getMitarbeiterID() + ",");
             for(Teil teil : threadee.getTeilListe()) {
                 sb.append(teil.getType().casename());
                 for (int i : teil.getZahlenList()) sb.append(",").append(i);
             }
             w.write(sb.toString() + System.lineSeparator());
             w.flush();
             w.close();
             logger.log(Level.INFO, "Threadee " + threadee.getID() + " eingelagert");
         } catch (IOException ex) {
             logger.log(Level.ERROR, "Daten konnten nicht gespeichert werden");
         }
     }
     
 }
