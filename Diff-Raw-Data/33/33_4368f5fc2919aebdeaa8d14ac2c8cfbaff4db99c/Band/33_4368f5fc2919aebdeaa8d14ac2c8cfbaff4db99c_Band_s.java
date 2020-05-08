 package com;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.TreeSet;
 
 public class Band {
 
     private Mitglieder mitglieder;
     private ArrayList<Song> repertoire;
     private Kalender kalender;
     private Bilanz bilanz;
 
     public Band() {
         this.mitglieder = new Mitglieder();
         this.repertoire = new ArrayList<Song>();
         this.kalender = new Kalender();
         this.bilanz = new Bilanz();
     }
 
     /**
      * Vorbedingung
      * 
      * Mitglied sollte nicht null sein.
      * 
      * Nachbedingung
      * 
      * return
      * true, falls das Mitglied hinzugefuegt werden und der Zustand der Band gespeichert werden konnte,
      * false andernfalls.
      */
     public boolean mitgliedHinzufuegen(Mitglied m, GregorianCalendar eintrittsDatum) {
         return mitglieder.mitgliedHinzufuegen(m, eintrittsDatum);
     }
 
     /**
      * Vorbedingung
      * 
      * Mitglied sollte nicht null sein.
      * 
      * Nachbedingung
      * 
      * return
      * true, falls das Mitglied hinzugefuegt werden konnte,
      * false andernfalls.
      */
     public boolean ersatzMitgliedHinzufuegen(Mitglied m) {
         return mitglieder.ersatzMitgliedHinzufuegen(m);
     }
     
     /**
      * Mitgliedsstatus vom fixen Mitglied in Ersatzmitglied aendern
      * 
      * @param mAusErsatz
      * @param mAusFix 
      * @param aenderungsDatum 
      */
     public void swapMitglied(Mitglied mAusErsatz, Mitglied mAusFix, GregorianCalendar aenderungsDatum) {
         mitglieder.swapMitglied(mAusErsatz, mAusFix, aenderungsDatum);
     }
 
     /**
      * Entfernt ein Mitglied aus der Band.
      *
      * @param m zu entferndenes Mitglied
      * @return Erfolg
      */
     public boolean mitgliedEntfernen(Mitglied m, GregorianCalendar austrittsdatum) {
         return mitglieder.mitgliedEntfernen(m, austrittsdatum);
     }
 
     /**
      * Listet alle Mitglieder der Band.
      *
      * @return Mitglieder
      */
     public HashSet<Mitglied> mitgliederAuflisten() {
         return new HashSet<Mitglied>(mitglieder.mitgliederAuflisten());
     }
 
     /**
      * Listet alle Personen, die innerhalb eines gesuchten Zeitraums Mitglieder
      * der Band waren.
      *
      * @param von Beginn des gesuchten Zeitraumes
      * @param bis Ende des gesuchten Zeitraumes
      * @return Mitglieder innerhalb des gesuchten Zeitraumes
      */
     public HashSet<Mitglied> mitgliederAuflisten(GregorianCalendar date) {
         return new HashSet<Mitglied>(mitglieder.mitgliederAuflisten(date));
     }
 
     /**
      * Nachbedingung
      * 
      * return
      * true, falls der Song hinzugefuegt werden konnte,
      * false andernfalls.
      */
     public boolean songHinzufuegen(Song s) {
         return repertoire.add(s);
     }
 
     /**
      * Nachbedingung
      * 
      * return
      * true, falls der Song vorhanden war und geloescht werden konnte,
      * false andernfalls.
      */
     public boolean songEntfernen(Song s) {
         if (repertoire.contains(s)) {
             return repertoire.remove(s);
         }
         return false;
     }
 
     /**
      * 
      */
     public ArrayList<Song> songsAuflisten() {
         return repertoire;
     }
 
     /**
      * Nachbedingung
      * 
      * return
      * Liste beinhaltet Versionen wie spezifiziert.
      * Songs sind alle nach dem spezifizierten Datum entstanden.
      * s.getVon() > datum
      */
     public ArrayList<Song> songsAuflisten(GregorianCalendar datum, boolean versionen) {
         ArrayList<Song> repertoireListe = new ArrayList<Song>();
         HashSet<Mitglied> mitglieder = this.mitglieder.mitgliederAuflisten(datum);
 
         for(Mitglied m : mitglieder) {
             if(!versionen) {
                 repertoireListe.addAll(m.getRepertoire(datum));
             } else {
                 for(Song s : m.getRepertoire(datum)) {
                     repertoireListe.addAll(s.getVersionen());
                 }
             }
         }
 
         return repertoireListe;
     }
     
     /**
      * Vorbedingung
      * 
      * Uebergebener Termin sollte nicht null sein.
      * 
      * Nachbedingung
      * 
      * return
      * true, falls der Termin und dessen Ort dem Kalender
      * und der Bilanz der zugehoerige Posten hinzugefuegt werden konnt,
      * false, andernfalls.
      */
     public boolean terminHinzufuegen(Termin t) {
         return  kalender.terminHinzufuegen(t) && kalender.ortHinzufuegen(t.getOrt()) && bilanz.postenHinzufuegen(new Posten(t));
     }
     
     /**
      * Erzeugt eine neue Abstimmung zu einem Termin
      * 
      * @param t abzustimmender Termin
      * @return Abstimmung
      */
     public Abstimmung abstimmenTermin(Termin t) {
         return new Abstimmung(mitglieder.mitgliederAuflisten(), t);
     }
     
     /**
      * Vorbedingung
      * 
      * Uebergebene Termine sollten nicht null sein.
      * 
      * Nachbedingung
      * 
      * return
      * true, falls Termin, zugehoeriger Ort und Posten geaendert werden konnten
      * false andernfalls.
      * ERROR: return nicht richtig gesetzt.
      * 
      * GOOD: Durch dynamisches Binden muessen nur Termine geaendert werden.
      * BAD: Bei jeder Aenderungsaktion muessen saemtliche verknuepften Objekte einzeln geaendert werden.
      */
     public boolean terminAendern(Termin alt, Termin neu) {
         HashSet<Mitglied> mitgliederListe = this.mitglieder.mitgliederAuflisten();
         
         Termin t = kalender.terminAendern(alt.getDatum(), neu);
         
         if(t != null) {
             
             Posten p = new Posten(alt);
             kalender.ortLoeschen(alt.getOrt());
             kalender.ortHinzufuegen(neu.getOrt());
             if(bilanz.postenExistiert(p)) bilanz.postenAendern(p, new Posten(neu));
             
             for(Mitglied m : mitgliederListe) {
                 m.message("Folgender Termin wurde geaendert: " + t.toString());
                 m.terminAendern(alt, neu);
             }
             return true;
         }
         return false;
     }
     
     /**
      * Vorbedingung
      * 
      * Uebergebener Termin sollte nicht null sein.
      * 
      * Nachbedingung
      * 
      * return
      * true, falls Termin, zugehoeriger Ort und Posten geloescht werden konnten
      * false andernfalls.
      * ERROR: return nicht richtig gesetzt.
      * 
      * GOOD: Durch dynamisches Binden muessen nur Termine geloescht werden.
      * BAD: Bei jeder Loeschaktion muessen saemtliche verknuepften Objekte einzeln geloescht werden.
      */
     public boolean terminLoeschen(Termin t) {
         HashSet<Mitglied> mitgliederListe = this.mitglieder.mitgliederAuflisten();
         
         if(kalender.terminLoeschen(t)) {
             
             Posten p = new Posten(t);
             kalender.ortLoeschen(t.getOrt());
             if (bilanz.postenExistiert(p)) bilanz.postenLoeschen(p);
             
             for(Mitglied m : mitgliederListe) {
                 m.message("Folgender Termin wurde abgesagt: " + t.toString());
                 m.terminLoeschen(t);
             }
             return true;
         }
         return false;
     }
     
     /**
      * Vorbedingung
      * 
      * Termin sollte nicht null sein.
      * 
      * Nachbedingung
      * 
      * return
      * true, falls Termin, zugehoeriger Ort und Posten wiederhergestellt werden konnten
      * false andernfalls.
      * ERROR: Ort wird nicht wiederhergestellt.
      * ERROR: return nicht richtig gesetzt.
      * 
      * GOOD: Durch dynamisches Binden muessen nur Termine wiederhergestellt werden.
      * BAD: Bei jeder Wiederherstellungsaktion muessen saemtliche verknuepften Objekte einzeln wiederhergestellt werden.
      */
     public Termin terminWiederherstellen(Termin t) {
         Posten p = new Posten(t);
         if(bilanz.postenExistiert(p)) bilanz.postenWiederherstellen(p);
         return kalender.terminWiederherstellen(t.getDatum());
     }
     
     /**
      * Nachbedingung
      * 
      * return
      * Termine liegen innerhalb des gegebenen Intervalls.
      */
     public ArrayList<? extends Termin> termineAuflisten(GregorianCalendar von, GregorianCalendar bis) {
         return kalender.termineAuflisten(von, bis);
     }
     
     /**
      * Nachbedingung
      * 
      * return
      * Proben liegen innerhalb des gegebenen Intervalls.
      */
     public ArrayList<Probe> probenAuflisten(GregorianCalendar von, GregorianCalendar bis) {
         return kalender.probenAuflisten(von, bis);
     }
     
     /**
      * Nachbedingung
      * 
      * return
      * Auftritte liegen innerhalb des gegebenen Intervalls.
      */
     public ArrayList<Auftritt> auftritteAuflisten(GregorianCalendar von, GregorianCalendar bis) {
         return kalender.auftritteAuflisten(von, bis);
     }
     
     /**
      * Nachbedingung
      * 
      * return
      * true, falls der Posten erfolgreich hinzugefuegt werden konnte,
      * false andernfalls.
      */
     public boolean postenHinzufuegen(Posten p) {
         return bilanz.postenHinzufuegen(p);
     }
     
     /**
      * Nachbedingung
      * 
      * return
      * true, falls der Posten erfolgreich geaendert werden konnte,
      * false andernfalls.
      */
     public Posten postenAendern(Posten alt, Posten neu) {
         return bilanz.postenAendern(alt, neu);
     }
     
     /**
      * Nachbedingung
      * 
      * return
      * true, falls der Posten erfolgreich geloescht werden konnte,
      * false andernfalls.
      */
     public boolean postenLoeschen(Posten p) {
         return bilanz.postenLoeschen(p);
     }
     
     /**
      * Nachbedingung
      * 
      * return
      * true, falls der Posten erfolgreich wiederhergestellt werden konnte,
      * false andernfalls.
      */
     public Posten postenWiederherstellen(Posten p) {
         return bilanz.postenWiederherstellen(p);
     }
     
     public TreeSet<Posten> postenAuflisten(boolean showAuftr, boolean showProben, boolean showEinnahmen, boolean showAusgaben, GregorianCalendar von, GregorianCalendar bis) {
         return bilanz.postenAuflisten(showAuftr, showProben, showEinnahmen, showAusgaben, von, bis);
     }
     
     /**
      * Summiert die Kosten, die innerhalb eines gesuchten Zeitraumes durch das Mieten der Proberaume entstehen
      * 
      * @param von Beginn des gesuchten Zeitraumes
      * @param bis Ende des gesuchten Zeitraumes
      * @return Kosten, die innerhalb des gesuchten Zeitraumes entstanden sind
      */
 //    public int kostenSummieren(boolean showProben, boolean showSonstige, GregorianCalendar von, GregorianCalendar bis) {
 //        return bilanz.kosten(showProben, showSonstige, von, bis);
 //    }
 
     /**
      * Summiert den Umsatz, der innerhalb eines gesuchten Zeitraumes durch Gagen
      * bei den Auftritten verdient wurde
      *
      * @param von Beginn des gesuchten Zeitraumes
      * @param bis Ende des gesuchten Zeitraumes
      * @return Umsatz, der innerhalb des gesuchten Zeitraumes erwirtschaftet
      * werden konnte
      */
 //    public int umsatzSummieren(boolean showAuftr, boolean showSonstige, GregorianCalendar von, GregorianCalendar bis) {
 //        return bilanz.umsatz(showAuftr, showSonstige, von, bis);
 //    }
 
     /**
      * Summiert den Gewinn, der innerhalb eines gesuchten Zeitraumes
      * erwirtschaftet werden konnte
      *
      * @param von Beginn des gesuchten Zeitraumes
      * @param bis Ende des gesuchten Zeitraumes
      * @return Gewinn, der innerhalb des gesuchten Zeitraumes erwirtschaftet
      * werden konnte
      */
     public int postenSummieren(boolean showAuftr, boolean showProben, boolean showSonstigeEinnahmen, boolean showSonstigeAusgaben, GregorianCalendar von, GregorianCalendar bis) {
         return bilanz.postenSummieren(showAuftr, showProben, showSonstigeEinnahmen, showSonstigeAusgaben, von, bis);
     }
 
     /**
      * Nachbedingung
      * 
      * return
      * Orte mit gleich vielen oder mehr Plaetzen als gesucht.
      * o.getPlaetze() >= plaetze
      */
     public ArrayList<Ort> findeOrt(int plaetze) {
         return kalender.findeOrt(plaetze);
     }
     
     public String printMitglieder() {
         String s = "";
         HashSet<Mitglied> mitglieder = this.mitglieder.mitgliederAuflisten();
         for (Mitglied m : mitglieder) {
             s += m + "\n";
         }
         return s;
     }
 }
