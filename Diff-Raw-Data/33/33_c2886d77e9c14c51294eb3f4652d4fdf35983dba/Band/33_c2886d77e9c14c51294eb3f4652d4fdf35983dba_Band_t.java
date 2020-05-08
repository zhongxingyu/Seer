 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.TreeSet;
 
 /**
  *
  * @author guetar
  */
 public class Band {
 
     private Mitgliedsverwaltung mitgliedsVerwaltung;
     private ArrayList<Song> repertoire;
     private Kalender kalender;
     private Bilanz bilanz;
 
     /**
      * Konstruktor
      */
     public Band() {
         this.mitgliedsVerwaltung = new Mitgliedsverwaltung();
         this.repertoire = new ArrayList<Song>();
         this.kalender = new Kalender();
         this.bilanz = new Bilanz();
     }
 
     // Mitglieder
     /**
      * Fuegt der Band ein Mitglied hinzu.
      *
      * @param m hinzuzufuegendes Mitglied
      * @return Erfolg
      */
     public boolean mitgliedHinzufuegen(Mitglied m, GregorianCalendar eintrittsDatum) {
         return mitgliedsVerwaltung.mitgliedHinzufuegen(m, eintrittsDatum);
     }
     
     public boolean ersatzMitgliedHinzufuegen(Mitglied m) {
         return mitgliedsVerwaltung.ersatzMitgliedHinzufuegen(m);
     }
     
     public void swapMitglied(Mitglied mAusErsatz, Mitglied mAusFix, GregorianCalendar aenderungsDatum) {
         mitgliedsVerwaltung.swapMitglied(mAusErsatz, mAusFix, aenderungsDatum);
     }
 
     /**
      * Entfernt ein Mitglied aus der Band.
      *
      * @param m zu entferndenes Mitglied
      * @return Erfolg
      */
     public boolean mitgliedEntfernen(Mitglied m, GregorianCalendar austrittsdatum) {
         return mitgliedsVerwaltung.mitgliedEntfernen(m, austrittsdatum);
     }
 
     /**
      * Listet alle Mitglieder der Band.
      *
      * @return Mitglieder
      */
     public HashSet<Mitglied> mitgliederAuflisten() {
         return new HashSet<Mitglied>(mitgliedsVerwaltung.mitgliederAuflisten());
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
         return new HashSet<Mitglied>(mitgliedsVerwaltung.mitgliederAuflisten(date));
     }
 
     // Repertoire
     /**
      * Fuegt dem Repertoire der Band einen Song hinzu.
      *
      * @param s hinzuzufuegender Song
      * @return Erfolg
      */
     public boolean songHinzufuegen(Song s) {
         return repertoire.add(s);
     }
 
     /**
      * Entfernt einen Song aus dem Repertoire der Band.
      *
      * @param s zu entfernender Song
      * @return Erfolg
      */
     public boolean songEntfernen(Song s) {
         if (repertoire.contains(s)) {
             return repertoire.remove(s);
         }
         return false;
     }
 
     /**
      * Listet das Repertoire der Band
      *
      * @return Repertoire
      */
     public ArrayList<Song> songsAuflisten() {
         return repertoire;
     }
 
     /**
      * Listet das Repertoire der Band zu einem gewissen Zeitpunkt.
      *
      * @param datum gesuchter Zeitpunkt
      * @return Repertoire zu dem gesuchten Zeitpunkt
      */
     public ArrayList<Song> songsAuflisten(GregorianCalendar datum, boolean versionen) {
         ArrayList<Song> repertoireListe = new ArrayList<Song>();
         HashSet<Mitglied> mitglieder = mitgliedsVerwaltung.mitgliederAuflisten(datum);
 
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
     
     // Kalender und Bilanz
     
     /**
      * Fuegt dem Kalender der Band einen Termin hinzu
      * 
      * @param t hinzuzufuegender Termin
      * @return Erfolg
      */
     public boolean terminHinzufuegen(Termin t) {
         return  kalender.terminHinzufuegen(t) && bilanz.postenHinzufuegen(new Posten(t));
     }
     
     /**
      * Erzeugt eine neue Abstimmung zu einem Termin
      * 
      * @param t abzustimmender Termin
      * @return Abstimmung
      */
     public Abstimmung abstimmenTermin(Termin t) {
         return new Abstimmung(mitgliedsVerwaltung.mitgliederAuflisten(), t);
     }
     
     /**
      * Aendert einen Termin
      * 
      * @param alt zu aendernder Termin
      * @param neu neuer Termin
      * @return Erfolg
      */
     public boolean terminAendern(Termin alt, Termin neu) {
         HashSet<Mitglied> mitglieder = mitgliedsVerwaltung.mitgliederAuflisten();
         
         Termin t = kalender.terminAendern(alt.getDatum(), neu);
         
         if(t != null) {
             Posten p = new Posten(alt);
             if(bilanz.postenExistiert(p)) bilanz.postenAendern(p, new Posten(neu));
             for(Mitglied m : mitglieder) {
                 m.message("Folgender Termin wurde geaendert: " + t.toString());
                 m.terminAendern(alt, neu);
             }
             return true;
         }
         return false;
     }
     
     /**
      * loescht einen Termin
      * 
      * @param t zu loeschender Termin
      * @return Erfolg
      */
     public boolean terminLoeschen(Termin t) {
         HashSet<Mitglied> mitglieder = mitgliedsVerwaltung.mitgliederAuflisten();
         
         if(kalender.terminLoeschen(t)) {
             Posten p = new Posten(t);
             if (bilanz.postenExistiert(p)) bilanz.postenLoeschen(p);
             for(Mitglied m : mitglieder) {
                 m.message("Folgender Termin wurde abgesagt: " + t.toString());
                 m.terminLoeschen(t);
             }
             return true;
         }
         return false;
     }
     
     /**
      * Stellt einen zuvor geaenderten Termin wieder her
      * 
      * @param t wiederherzustellender Termin
      * @return Erfolg
      */
     public Termin terminWiederherstellen(Termin t) {
         Posten p = new Posten(t);
         if(bilanz.postenExistiert(p)) bilanz.postenWiederherstellen(p);
         return kalender.terminWiederherstellen(t.getDatum());
     }
     
     /**
      * Listet alle Termine innerhalb des gesuchten Zeitraums
      * 
      * @param von Beginn des gesuchten Zeitraums
      * @param bis Ende des gesuchten Zeitraum
      * @return Liste der gesuchten Termine
      */
     public ArrayList<? extends Termin> termineAuflisten(GregorianCalendar von, GregorianCalendar bis) {
         return kalender.termineAuflisten(von, bis);
     }
     
     /**
      * Listet alle Proben innerhalb des gesuchten Zeitraums
      * 
      * @param von Beginn des gesuchten Zeitraums
      * @param bis Ende des gesuchten Zeitraum
      * @return Liste der gesuchten Proben
      */
     public ArrayList<Probe> probenAuflisten(GregorianCalendar von, GregorianCalendar bis) {
         return kalender.probenAuflisten(von, bis);
     }
     
     /**
      * Listet alle Auftritte innerhalb des gesuchten Zeitraums
      * 
      * @param von Beginn des gesuchten Zeitraums
      * @param bis Ende des gesuchten Zeitraum
      * @return Liste der gesuchten Auftritte
      */
     public ArrayList<Auftritt> auftritteAuflisten(GregorianCalendar von, GregorianCalendar bis) {
         return kalender.auftritteAuflisten(von, bis);
     }
     
     /**
      * Fuegt der Bilanz einen Posten hinzu
      * 
      * @param p hinzuzufuegender Posten
      * @return Erfolg
      */
     public boolean postenHinzufuegen(Posten p) {
         return bilanz.postenHinzufuegen(p);
     }
 
     /**
      * Aendert einen Posten
      * 
      * @param alt zu aendernder Posten
      * @param neu neuer Posten
      * @return neuer Posten
      */
     public Posten postenAendern(Posten alt, Posten neu) {
         return bilanz.postenAendern(alt, neu);
     }
     
     /**
      * Loescht einen Posten
      * 
      * @param p zu loeschender Posten
      * @return Erfolg
      */
     public boolean postenLoeschen(Posten p) {
         return bilanz.postenLoeschen(p);
     }
     
     /**
      * Stellt einen zuvor geaenderten Posten wieder her
      * 
      * @param p wiederherzustellender POsten
      * @return wiederhergestellter Posten
      */
     public Posten postenWiederherstellen(Posten p) {
         return bilanz.postenWiederherstellen(p);
     }
     
    public TreeSet<Posten> bilanzAuflisten(boolean showAuftr, boolean showProben, boolean showSonstige, GregorianCalendar von, GregorianCalendar bis) {
         return bilanz.postenAuflisten(showAuftr, showProben, showSonstige, von, bis);
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
     public int postenSummieren(boolean showAuftr, boolean showProben, boolean showSonstige, GregorianCalendar von, GregorianCalendar bis) {
         return bilanz.postenSummieren(showAuftr, showProben, showSonstige, von, bis);
     }
 
     /**
      * Fuegt einen Ort hinzu
      *
      * @param o hinzuzufuegender Ort
      * @return Erfolg
      */
     public boolean ortHinzufuegen(Ort o) {
         return kalender.ortHinzufuegen(o);
     }
 
     /**
      * liefert Liste mit Orten, die eine bestimmte Infrastruktur haben.
      *
      * @param plaetze Gesuchte Anzahl an Zuschauerplaetzen(oder 0, wenn egal)
      *
      * @return die Orte, die die bestimmte Infrastruktur haben. Leere Liste,
      * wenn kein Ort die Voraussetzungen erfuellt.
      */
     public ArrayList<Ort> findeOrt(int plaetze) {
         return kalender.findeOrt(plaetze);
     }
     
     public String printMitglieder() {
         String s = "";
         HashSet<Mitglied> mitglieder = mitgliedsVerwaltung.mitgliederAuflisten();
         for (Mitglied m : mitglieder) {
             s += m + "\n";
         }
         return s;
     }
 }
