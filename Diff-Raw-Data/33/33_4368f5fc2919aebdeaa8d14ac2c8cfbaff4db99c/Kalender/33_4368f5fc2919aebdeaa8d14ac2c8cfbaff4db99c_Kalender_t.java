 package com;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.TreeSet;
 
 /**
  *
  * @author guetar
  */
 public class Kalender {
 
     // Invariante: Termine immer aufsteigend sortiert.
     private TreeSet<Termin> termine;
     // Invariante: Orte immer aufsteigend sortiert.
     private TreeSet<Ort> orte;
     private LinkedList<Termin> trash;
 
     public Kalender() {
         this.termine = new TreeSet<Termin>();
         this.trash = new LinkedList<Termin>();
         this.orte = new TreeSet<Ort>();
     }
 
     /**
      * Nachbedingung
      * 
      * Retournierter Wert immer
      * true, falls der Ort nicht vorhanden war und erfolgreich hinzugefuegt wurde,
      * falsch, falls der Ort schon vorhanden war oder nicht erfolgreich hinzugefuegt werden konnte.
      */
     public boolean ortHinzufuegen(Ort o) {
         if(!orte.contains(o)) {
             return orte.add(o);
         }
         return false;
     }
     
     /**
      * Nachbedingung
      * 
      * Retournierter Wert imer
      * true, falls der Ort vorhanden war und erfolgreich geloescht werden konnte,
      * false, falls der Ort nicht vorhanden war oder nicht erfolgreich geloescht werden konnte.
      */
     public boolean ortLoeschen(Ort o) {
         if(orte.contains(o)) {
             return orte.remove(o);
         }
         return false;
     }
 
     /**
      * Nachgedingung
      * 
      * Retournierte Orte besitzen immer
      * Plaetze >= gesuchte Plaetze.
      */
     public ArrayList<Ort> findeOrt(int plaetze) {
         ArrayList<Ort> gefOrte = new ArrayList<Ort>();
 
         for (Ort o : orte) {
 
             if (o.getPlaetze() >= plaetze) {
                 gefOrte.add(o);
             }
         }
 
         return gefOrte;
     }
 
     /**
      * Nachbedingung
      * 
      * Retorunierter Wert immer
      * true, falls der Termin noch nicht vorhanden war und erfolgreich hinzugefuegt werden konnte,
      * false, falls der Termin bereits vorhanden war oder nicht erfolgreich hinzugefuegt werden konnte.
      * 
      * GOOD: Durch dynamisches Binden ist nur eine Liste zur Verwaltung von Proben und Auftritten notwendig.
      */
     public boolean terminHinzufuegen(Termin t) {
         if(!termine.contains(t)) {
             return termine.add(t);
         }
         return false;
     }
 
     /**
      * Vorbedingung
      * 
      * Neuer Termin darf nicht null sein, das sonst eine NullpointerException entsteht.
      * 
      * Nachbedingung
      * 
      * Retournierter Termin immer
      * Instanz von Probe, falls eine Probe geaendert wurde,
      * Instanz von Auftritt, falls ein Auftritt geaendert wurde,
      * null, falls der Termin nicht gefunden wurde.
      * 
      * BAD: Durch dynamisches Binden ist diese Funktion sehr unuebersichtlich,
      * da immer festgestellt werden muss, Instanz welcher Klasse das aktuelle Objekt ist.
      */
     public Termin terminAendern(GregorianCalendar alt, Termin neu) {
         
         for(Termin t: termine) {
             if(t.getDatum().equals(alt)) {
                 if(t instanceof Probe) {
                 
                     termine.remove(t);
                     Probe p = (Probe) t;
                     Probe p_neu = p.setProbe((Probe) neu);
                     termine.add(p_neu);
                     return p_neu;
 
                 } else if (t instanceof Auftritt) {
 
                     termine.remove(t);
                     Auftritt a = (Auftritt) t;
                     Auftritt a_neu = a.setAuftritt((Auftritt) neu);
                     termine.add(a_neu);
                     return a;
                 }
             }
         }
         return null;
     }
 
     /**
      * Nachbedingung
      * 
      * Retournierter Wert immer
      * true, falls der Termin vorhanden war und erfolgreich geloescht werden konnte,
      * false, falls der Termin nicht vorhanden oder bereits geloescht war.
      * 
      * GOOD: Durch dynamisches Binden muss der Termin, egal ob Probe oder Auftritt,
      * nur in einer Liste gesucht werden.
      */
     public boolean terminLoeschen(Termin t) {
         if (termine.contains(t) && !trash.contains(t)) {
             trash.add(t);
             termine.remove(t);
             
             for(Mitglied m : t.getTeilnehmer()) {
                 m.terminLoeschen(t);
             }
             return true;
         }
         return false;
     }
 
     /**
      * Invariante
      * 
      * Sicherstellen, dass Termine immer noch aufsteigend sortiert sind durch
      * .remove() und anschließendes .add().
      * 
      * Nachbedingung
      * 
      * Retournierter Termin immer
      * Instanz von Probe, falls eine Probe wiederhergestellt wurde,
      * Instanz von Auftritt, falls ein Auftritt wiederhergestellt wurde,
      * null, falls der Termin nicht gefunden wurde.
      * 
      * BAD: Durch dynamisches Binden ist diese Funktion sehr unuebersichtlich,
      * da immer festgestellt werden muss, Instanz welcher Klasse das aktuelle Objekt ist.
      */
     public Termin terminWiederherstellen(GregorianCalendar datum) {
         for(Termin t : termine) {
             if(t.getDatum().equals(datum)) {
                 Termin alt = t.popFromStack();
         
                 if(alt != null) {
                     // Eine alte Version des Termins lag am Stack => wird wiederhergestellt
                     if(alt instanceof Probe) {
 
                         termine.remove(t);
                         Probe p_alt = (Probe) alt;
                         Probe t_alt = (Probe) t;
                         t_alt.setProbe(p_alt);
                         termine.add(t_alt);
                         return t_alt;
 
                     } else if(alt instanceof Auftritt) {
 
                         termine.remove(t);
                         Auftritt a_alt = (Auftritt) alt;
                         Auftritt t_alt = (Auftritt) t;
                         t_alt.setAuftritt(a_alt);
                         termine.add(t_alt);
                         return t_alt;
                     }
                 }
             }
         }
         
         for(Termin t : trash) {
             if(t.getDatum().equals(datum)) {
                 // keine alte Version vorhanden => Termin muss geloescht worden sein
                 if(trash.contains(t)) {
                     trash.remove(t);
                     termine.add(t);
                     return t;
                 }
             }
         }
         
         return null;
     }
 
     /**
      * GOOD: Durch dynamisches Binden kann leicht eine allgemeine Terminliste zurueckgegeben werden.
      */
     public TreeSet<? extends Termin> termineAuflisten() {
         return termine;
     }
 
     /**
     * Vorbedingung
     * 
     * von chronologisch vor bis
     * 
      * Nachbedingung
      * 
      * Retournierte Termine immer
      * innerhalb des gegebenen Zeitraums.
      * von < t.getVon() && t.getBis() < bis
      * 
      * GOOD: Durch dynamisches Binden muss nur eine Liste durchiteriert werden.
      */
     public ArrayList<? extends Termin> termineAuflisten(GregorianCalendar von, GregorianCalendar bis) {
         ArrayList<Termin> termine_liste = new ArrayList<Termin>();
 
         for (Termin t : termine) {
             if (von.before(t.getDatum()) && bis.after(t.getDatum())) {
                 termine_liste.add(t);
             } else if (bis.before(t.getDatum())) {
                 break;
             }
         }
 
         return termine_liste;
     }
 
     /**
     * Vorbedingung
     * 
     * von chronologisch vor bis
     * 
      * Nachbedingung
      * 
      * Retournierte Proben immer
      * innerhalb des gegebenen Zeitraums.
      * von < t.getVon() && t.getBis() < bis
      * 
      * BAD: Durch dynamisches Binden muss bei jeder Iteration zusaetzlich ueberprueft werden,
      * ob der Termin Instanz von Probe ist.
      */
     public ArrayList<Probe> probenAuflisten(GregorianCalendar von, GregorianCalendar bis) {
         ArrayList<Probe> probenListe = new ArrayList<Probe>();
 
         for (Termin t : termine) {
             if (t instanceof Probe && von.before(t.getDatum()) && bis.after(t.getDatum())) {
                 probenListe.add((Probe) t);
             } else if (bis.before(t.getDatum())) {
                 break;
             }
         }
 
         return probenListe;
     }
 
     /**
     * Vorbedingung
     * 
     * von chronologisch vor bis
     * 
      * Nachbedingung
      * 
      * Retournierte Auftritte immer
      * innerhalb des gegebenen Zeitraums
      * von < t.getVon() && t.getBis() < bis
      * 
      * BAD: Durch dynamisches Binden muss bei jeder Iteration zusaetzlich ueberprueft werden,
      * ob der Termin Instanz von Auftritt ist.
      */
     public ArrayList<Auftritt> auftritteAuflisten(GregorianCalendar von, GregorianCalendar bis) {
         ArrayList<Auftritt> auftritteListe = new ArrayList<Auftritt>();
 
         for (Termin t : termine) {
             if (t instanceof Auftritt && von.before(t.getDatum()) && bis.after(t.getDatum())) {
                 auftritteListe.add((Auftritt) t);
             } else if (bis.before(t.getDatum())) {
                 break;
             }
         }
 
         return auftritteListe;
     }
     
     /**
      * GOOD: Durch dynamisches Binden ist nur eine Liste zum Loeschen von Proben und Auftritten notwendig.
      */
     public LinkedList<Termin> trashAuflisten() {
         return trash;
     }
 }
