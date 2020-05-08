 import Buchung.BuchungContainer;
 import Buchung.Filter.AbstractFilter;
 import Buchung.Filter.TypFilter;
 import Buchung.Filter.ZeitraumFilter;
 import Buchung.Gage;
 import Buchung.RaumMiete;
 import Buchung.Steuer;
 
 import java.lang.Integer;
 import java.lang.System;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class Test {
 
     public static void main(String[] args) throws Exception, ClassNotFoundException {
         Musikgruppe m = new Musikgruppe("MyMusikGroup", "Blub");  // denkt euch nen Namen und eine Richtung aus ;)
         System.out.println("Musikguppe: " + m);
 
         // Termin Test
         System.out.println("\n---Termin testen---\n");
         SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm");
 
         Ort o1 = new Ort("Garage", "Hauptstraße 7", "1020", true, true, 12);
         Ort o2 = new Ort("Saal", "Bahnhofsplatz 17", "1020", true, false, 60);
         Ort o3 = new Ort("Halle", "Wiedener Hauptstraße 11", "1050", false, true, 200);

         Probe p1 = new Probe(15.0f, o1, sdf.parse("17.11.2012 19:00"), 120);
         Probe p2 = new Probe(20.0f, o1, sdf.parse("24.11.2012 18:30"), 90);
         Auftritt a1 = new Auftritt(150.0f, o2, sdf.parse("24.12.2012 21:00"), 60);
         Auftritt a2 = new Auftritt(135.0f, o3, sdf.parse("3.9.2012 14:30"), 120);
 
         m.getTerminContainer().addTermin(p1);
         System.out.println("Add: " + p1);
         m.getTerminContainer().addTermin(p2);
         System.out.println("Add: " + p2);
         m.getTerminContainer().addTermin(a1);
         System.out.println("Add: " + a1);
         m.getTerminContainer().addTermin(a2);
         System.out.println("Add: " + a2);
 
 
         float gage = m.getTerminContainer().getGage(sdf.parse("1.12.2012 00:00"), sdf.parse("1.1.2013 00:00"));
         System.out.println("Gage von 1.12.2012 00:00 bis 1.1.2013 00:00 : " + gage);
 
         float miete = m.getTerminContainer().getMiete(sdf.parse("1.11.2012 00:00"), sdf.parse("1.12.2012 00:00"));
         System.out.println("Gage von 1.11.2012 00:00 bis 1.12.2012 00:00 : " + miete);
 
         float saldo = m.getTerminContainer().getSaldo(sdf.parse("1.1.2012 00:00"), sdf.parse("1.1.2013 00:00"));
         System.out.println("Saldo von 1.1.2012 00:00 bis 1.1.2013 00:00 : " + saldo);
 
         List<Probe> lp = m.getTerminContainer().getProben(sdf.parse("1.1.2012 00:00"), sdf.parse("1.1.2013 00:00"));
         System.out.println("Proben von 1.1.2012 00:00 bis 1.1.2013 00:00");
         for(Probe p : lp) {
             System.out.println(p);
         }
 
         List<Auftritt> la = m.getTerminContainer().getAuftritte(sdf.parse("1.6.2012 00:00"), sdf.parse("1.10.2012 00:00"));
         System.out.println("Proben von 1.6.2012 00:00 bis 1.10.2012 00:00");
         for(Auftritt a : la) {
             System.out.println(a);
         }
 
         List<Termin> lt = m.getTerminContainer().getTermine(sdf.parse("1.1.2012 00:00"), sdf.parse("1.1.2013 00:00"));
         System.out.println("Proben von 1.1.2012 00:00 bis 1.1.2013 00:00");
         for(Termin t : lt) {
             System.out.println(t);
         }
 
         System.out.println("\n---Termin ändern/löschen---\n");
 
         // Termin ändern
         System.out.println("Proben von 17.11.2012 auf Dauer '30' und Ort 'Wohnzimmer' ändern:");
         lp = m.getTerminContainer().getProben(sdf.parse("17.11.2012 00:00"), sdf.parse("18.11.2012 00:00"));
         Probe p = lp.get(0);
         p.setDauer(30);
         p.setOrt(new Ort("Wohnzimmer", "Hauptstraße 16/7", "1045", false, false, 5));
         System.out.println(p);
 
         // letzte Änderung rückgängig machen
         System.out.println("Zweite Änderung Rückgäng machen:");
         m.getTerminContainer().removeTermin(p);
         m.getTerminContainer().addTermin(p.getPreviousVersion());
         // Ausgabe des jetzt enthaltenen Termins
         lp = m.getTerminContainer().getProben(sdf.parse("17.11.2012 00:00"), sdf.parse("18.11.2012 00:00"));
         System.out.println(lp.get(0));
 
         // Status ändern
         System.out.println("Probe absagen:");
         lp = m.getTerminContainer().getProben(sdf.parse("17.11.2012 00:00"), sdf.parse("18.11.2012 00:00"));
         p = lp.get(0);
         p.setStatus(Termin.Status.ABGESAGT);
         m.getMitgliedContainer().updateTermin(p);  // Mitglieder informieren
         System.out.println(p);
 
         // Termin löschen
         System.out.println("Proben von 17.11.2012 löschen:");
         lp = m.getTerminContainer().getProben(sdf.parse("17.11.2012 00:00"), sdf.parse("18.11.2012 00:00"));
         m.getTerminContainer().removeTermin(lp.get(0));
         lp = m.getTerminContainer().getProben(sdf.parse("17.11.2012 00:00"), sdf.parse("18.11.2012 00:00"));
         for(Termin t : lp) {  // leere List weil gelöscht
             System.out.println(t);
         }
         // gelöschte Probe ausgeben
         System.out.println("Gelöschte Probe ausgeben:");
         System.out.println(m.getTerminContainer().getRemoved().pop());
 
 
 
         /*
          * -----------------------------------------------------------
          * ANFANG: Tests für Repertoires und Varianten (AB ÜBUNG 2)
          * -----------------------------------------------------------
          */
         System.out.println("\n---- Testing for Excercise 2: Repertoires and alternative Songs ");
 
         Musikstueck a = new Musikstueck("SONG A",123);
         Musikstueck aa = new Stueckvariante("SONG A", "a", 123);
         Musikstueck ab = new Stueckvariante("SONG A", "b", 123);
         Musikstueck ba = new Stueckvariante("SONG B", "a", 123);
         Musikstueck cf = new Stueckvariante("SONG C", "f", 123);
 
         Mitglied ian = new Mitglied("Ian","0","Bass",null,null);
         ian.getRepertoire().addElement(a, sdf.parse("01.10.1970 00:00"));
         ian.getRepertoire().addElement(aa, sdf.parse("01.10.1970 00:00"));
         ian.getRepertoire().addElement(ab, sdf.parse("01.10.1970 00:00"));
         ian.getRepertoire().addElement(ba, sdf.parse("01.10.1970 00:00"));
         ian.getRepertoire().addElement(cf, sdf.parse("01.10.1970 00:00"));
         Mitglied rob = new Mitglied("Rob","1","Vocals", null,null);
         rob.getRepertoire().addElement(a, sdf.parse("01.10.1970 00:00"));
         rob.getRepertoire().addElement(aa, sdf.parse("01.10.1970 00:00"));
         rob.getRepertoire().addElement(ab, sdf.parse("01.10.1970 00:00"));
         rob.getRepertoire().addElement(ba, sdf.parse("01.10.1970 00:00"));
         Mitglied glenn = new Mitglied("Glenn","2","Guitar,Keyboard",null,null);
         glenn.getRepertoire().addElement(a, sdf.parse("01.10.1970 00:00"));
         glenn.getRepertoire().addElement(aa, sdf.parse("01.10.1970 00:00"));
         glenn.getRepertoire().addElement(ab, sdf.parse("01.10.1970 00:00"));
         glenn.getRepertoire().addElement(ba, sdf.parse("01.10.1970 00:00"));
         Mitglied scott = new Mitglied("Scott", "3", "Drums",null,null);
         scott.getRepertoire().addElement(a, sdf.parse("01.10.1970 00:00"));
         scott.getRepertoire().addElement(aa, sdf.parse("01.10.1970 00:00"));
         scott.getRepertoire().addElement(ab, sdf.parse("01.10.1970 00:00"));
         Mitglied richie = new Mitglied("Richie", "4", "Guitar",null,null);
         richie.getRepertoire().addElement(aa, sdf.parse("01.10.1970 00:00"));
         Mitglied kenneth = new Mitglied("Kenneth","5","Guitar",sdf.parse("01.10.1970 00:00"), sdf.parse("20.04.2011 00:00"));
 
         m.getMitgliedContainer().addMitglied(ian);
         m.getMitgliedContainer().addMitglied(rob);
         m.getMitgliedContainer().addMitglied(glenn);
         m.getMitgliedContainer().addMitglied(scott);
         m.getMitgliedContainer().addMitglied(richie);
         m.getMitgliedContainer().addMitglied(kenneth);
 
         System.out.println("\n ONLY SONG A, VARIANT A IS SUPPOSED TO BE PRINTED:");
        System.out.println( m.getActiveRepertoire(new Date()).toString());
 
 
 
         /*
          * -----------------------------------------------------------
          * ENDE: Tests für Repertoires und Varianten (AB ÜBUNG 2)
          * -----------------------------------------------------------
          */
 
         /**
          * Tests für Musikstueck
          */
         System.out.println();
         System.out.println("---- Testing Mustikstueck part");
 
         MusikstueckContainer musikstueckContainer = new MusikstueckContainer(); //m.getMusikstueckContainer();
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.DATE, -1);
         Date today = new Date();
         Date yesterday = new Date(cal.getTimeInMillis());
         cal.add(Calendar.DATE, 2);
         Date tomorrow = new Date(cal.getTimeInMillis());
         cal.add(Calendar.DATE, 1);
         Date dayAfterTomorrow = new Date(cal.getTimeInMillis());
         Integer size;
 
         size = musikstueckContainer.getList(today).size();
         if (size == 0) {
             System.out.println("OK: Musikgruppe hat ein leeres Repertoire");
         } else {
             System.err.println("ERROR: Musikgruppe sollte ein leeres Repertoire haben");
         }
 
         // Add a musikstueck
         Musikstueck stueck1 = new Musikstueck("Für Amelie", 666);
         musikstueckContainer.addElement(stueck1, today);
         size = musikstueckContainer.getList(today).size();
         if (size == 1) {
             System.out.println("OK: Musikgruppe kennt ein Stück");
         } else {
             System.err.println("ERROR: Musikgruppe sollte ein Stück kennen");
         }
         if (musikstueckContainer.getList(today).get(0).equals(stueck1)) {
             System.out.println("OK: Die Stücke stimmen überein");
         } else {
             System.err.println("ERROR: Die Stücke sollten überein stimmen");
         }
 
         // Musikstück sollte gestern nicht sichtbar sein, aber morgen schon
         size = musikstueckContainer.getList(yesterday).size();
         if (size == 0) {
             System.out.println("OK: Musikgruppe kannte gestern kein Stück");
         } else {
             System.err.println("ERROR: Musikgruppe sollte gestern kein Stück kennen");
         }
         size = musikstueckContainer.getList(tomorrow).size();
         if (size == 1) {
             System.out.println("OK: Musikgruppe kennt morgen ein Stück");
         } else {
             System.err.println("ERROR: Musikgruppe sollte morgen ein Stück kennen");
         }
 
         // Entfernen des Elements
         musikstueckContainer.removeElement(stueck1, tomorrow);
         size = musikstueckContainer.getList(yesterday).size();
         if (size == 0) {
             System.out.println("OK: Musikgruppe kannte gestern immer noch kein Stück");
         } else {
             System.err.println("ERROR: Musikgruppe sollte gestern kein Stück kennen");
         }
         size = musikstueckContainer.getList(today).size();
         if (size == 1) {
             System.out.println("OK: Musikgruppe kennt heute ein Stück");
         } else {
             System.err.println("ERROR: Musikgruppe sollte heute ein Stück kennen");
         }
         size = musikstueckContainer.getList(tomorrow).size();
         if (size == 1) {
             System.out.println("OK: Musikgruppe kennt morgen ein Stück");
         } else {
             System.err.println("ERROR: Musikgruppe sollte morgen ein Stück kennen");
         }
         size = musikstueckContainer.getList(dayAfterTomorrow).size();
         if (size == 0) {
             System.out.println("OK: Musikgruppe kennt übermorgen kein Stück");
         } else {
             System.err.println("ERROR: Musikgruppe sollte übermorgen kein Stück kennen");
         }
 
 		/*****************
 		 * BuchungsContainer *
 		 ****************/
 	    System.out.println();
 	    System.out.println("BUCHUNGSCONTAINER TESTS");
 	    BuchungContainer buchungContainer = new BuchungContainer();
 	    if (buchungContainer.summe(new ArrayList<AbstractFilter>()) == 0.0f) {
 		    System.out.println("OK: summe() = 0 ohne Buchungen");
 	    } else {
 		    System.out.println("ERROR: summe() != 0 ohne Buchungen");
 	    }
 	    Gage buchung1 = new Gage(500.0f, new Date());
 	    buchungContainer.addBuchung(buchung1);
 	    if (buchungContainer.summe(new ArrayList<AbstractFilter>()) == 500.0f) {
 		    System.out.println("OK: summe() = 500 mit buchung1");
 	    } else {
 		    System.out.println("ERROR: summe() != 500 mit buchung1");
 	    }
 	    RaumMiete buchung2 = new RaumMiete(300.0f, new Date());
 	    buchungContainer.addBuchung(buchung2);
 	    if (buchungContainer.summe(new ArrayList<AbstractFilter>()) == 200.0f) {
 		    System.out.println("OK: summe() = 200 mit buchung1 & buchung2");
 	    } else {
 		    System.out.println("ERROR: summe() != 200 mit buchung1 & buchung2");
 	    }
 	    buchungContainer.removeBuchung(buchung1);
 	    if (buchungContainer.summe(new ArrayList<AbstractFilter>()) == -300.0f) {
 		    System.out.println("OK: summe() = -300 mit buchung2");
 	    } else {
 		    System.out.println("ERROR: summe() != -300 mit buchung2");
 	    }
 	    /*******************************
 	     * BuchungsContainer mit Filter
 	     ******************************/
 	    System.out.println();
 	    System.out.println("BUCHUNGSCONTAINER MIT FILTER TESTS");
 	    buchungContainer = new BuchungContainer();
 	    buchung1 = new Gage(100.0f, today);
 	    buchung2 = new RaumMiete(200.0f, tomorrow);
 		buchungContainer.addBuchung(buchung1);
 		buchungContainer.addBuchung(buchung2);
 	    ZeitraumFilter filter1 = new ZeitraumFilter(yesterday, tomorrow);
 	    ArrayList<AbstractFilter> filter1list = new ArrayList<AbstractFilter>();
 	    filter1list.add(filter1);
 	    ZeitraumFilter filter2 = new ZeitraumFilter(today, tomorrow);
 	    ArrayList<AbstractFilter> filter2list = new ArrayList<AbstractFilter>();
 	    filter2list.add(filter2);
 	    ZeitraumFilter filter3 = new ZeitraumFilter(tomorrow, tomorrow);
 	    ArrayList<AbstractFilter> filter3list = new ArrayList<AbstractFilter>();
 	    filter3list.add(filter3);
 		if (buchungContainer.summe(filter1list) == -100.0f) {
 			System.out.println("OK: Filter1 passt");
 		} else {
 			System.out.println("ERROR: Filter1 passt nicht");
 		}
 		if (buchungContainer.summe(filter2list) == -100.0f) {
 			System.out.println("OK: Filter2 passt");
 		} else {
 			System.out.println("ERROR: Filter2 passt nicht");
 		}
 		if (buchungContainer.summe(filter3list) == -200.0f) {
 			System.out.println("OK: Filter3 passt");
 		} else {
 			System.out.println("ERROR: Filter3 passt nicht");
 		}
 
 	    /*************
 	     * TerminContainer mit Filtern
 	     ************/
 	    System.out.println();
 	    System.out.println("Termincontainer MIT FILTER TESTS");
 	    TerminContainer tc = new TerminContainer();
	    Auftritt auf1 = new Auftritt(555f, o1, today, 123);
	    Auftritt auf2 = new Auftritt(100f, o1, today, 123);
 	    tc.addTermin(auf1);
 	    tc.addTermin(auf2);
 	    if (tc.getSaldo(yesterday, tomorrow) == 655f) {
 		    System.out.println("OK: getSaldo passt");
 	    } else {
 		    System.out.println("ERROR: getSaldo passt nicht");
 	    }
 
 	    /*************
 	     * MusikGruppe mit Filter
 	     ************/
 	    System.out.println();
 	    System.out.println("Musikgruppe MIT FILTER TESTS");
 	    Musikgruppe mg = new Musikgruppe("Foo", "Bar");
 	    mg.getTerminContainer().addTermin(auf1);
 	    mg.getTerminContainer().addTermin(auf2);
 	    if (mg.getSaldo(new ArrayList<AbstractFilter>()) == 655f) {
 		    System.out.println("OK: getSumme 1 passt");
 	    } else {
 		    System.out.println("ERROR: getSumme 1 passt nicht");
 	    }
 	    mg.getBuchungContainer().addBuchung(new Steuer(55f, today));
 	    if (mg.getSaldo(new ArrayList<AbstractFilter>()) == 600f) {
 		    System.out.println("OK: getSumme 2 passt");
 	    } else {
 		    System.out.println("ERROR: getSumme 2 passt nicht");
 	    }
     }
 }
