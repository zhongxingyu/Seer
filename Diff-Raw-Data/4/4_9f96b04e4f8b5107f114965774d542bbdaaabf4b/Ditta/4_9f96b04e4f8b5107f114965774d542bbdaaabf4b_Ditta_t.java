 /*
  * Copyright (c) 2013 Pezzutti Marco
  * 
  * This file is part of Sistema Trasporti Artici (progetto per l'insegnamento 
  * di Programmazione Concorrente e Distribuita).
 
  * Sistema Trasporti Artici is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Sistema Trasporti Artici is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Sistema Trasporti Artici.  If not, see <http://www.gnu.org/licenses/>.
  */
 package ditta;
 
 import common.IAutotreno;
 import common.IBase;
 import common.IDitta;
 import common.IOrdine;
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Random;
 
 /**
  * Classe che rappresenta la Ditta di trasporti, si occupa di gestire gli ordini generati 
  * attraverso la GUI inviandoli alle basi prefissate; registra basi ed autotreni inserendoli 
  * in delle mappe dove viene salvato il loro nome e il loro stato; controlla periodicamente che basi ed 
  * autotreni siano attivi, altrimenti li imposta come non attivi nelle relative mappe; 
  * alla richiesta di chiusura si preoccupa di chiudere correttamente tutte le basi e gli 
  * autotreni attivi
  * 
  * @author Pezzutti Marco 1008804
  */
 public class Ditta extends UnicastRemoteObject implements IDitta {
     private final Map<IBase, Boolean> basiAttive;
     private final Map<String, IBase> nomiBasi;
     private final Map<IBase, String> basiNomi;
     
     private final Map<IAutotreno, Boolean> autotreniAttivi;
     private final Map<String, IAutotreno> nomiAutotreni;
     private final Map<IAutotreno, String> autotreniNomi;
     
     private final Queue<IOrdine> elencoOrdini;
     private final Queue<IOrdine> storicoOrdini;
     
     private final DittaGUI gui;
     
     private boolean terminato;
     
     private final int tempo = 2000;
     
     private static final String HOST = "localhost:";
     
     /**
      * Costruttore che inizializza le strutture dati e imposta la GUI passata come parametro
      * 
      * @param gui                   riferimento all'interfaccia grafica
      * @throws RemoteException 
      */
     public Ditta(DittaGUI gui) throws RemoteException {
         basiAttive = new HashMap<IBase, Boolean>();
         nomiBasi = new HashMap<String, IBase>();
         basiNomi = new HashMap<IBase, String>();
         
         autotreniAttivi = new HashMap<IAutotreno, Boolean>();
         nomiAutotreni = new HashMap<String, IAutotreno>();
         autotreniNomi = new HashMap<IAutotreno, String>();
         
         elencoOrdini = new LinkedList<IOrdine>();
         storicoOrdini = new LinkedList<IOrdine>();
         
         this.gui = gui;
         terminato = false;
     }
     
     /**
      * Metodo chiamato dalla GUI che ritorna un nuovo thread che genera ordini automaticamente
      * 
      * @return                      istanza di un thread che genera ordinidi automaticamente
      */
     final Thread avviaCreaOrdini() {
         gui.aggiornaStatoTextArea("Generazione automatica degli ordini avviata");
         return new Thread(new CreaOrdini());
     }
     
     /**
      * Metodo utilizzato per inserire nell'elenco degli ordini nuovi ordini creati dalla GUI
      * 
      * @param partenza              nome della base di partenza dell'ordine
      * @param destinazione          nome della base di destinazione dell'ordine
      * @param quantita              quantità di ordini da generare
      */
     final void inserisciOrdine(final String partenza, final String destinazione, final int quantita) {
         final IBase basePartenza;
         final IBase baseDestinazione;
         basePartenza = nomiBasi.get(partenza);
         baseDestinazione = nomiBasi.get(destinazione);
         
         //prendo il lock sull'elenco degli ordini
         //aggiungo un ordine fino al raggiungimento della quantità desiderata
         synchronized(elencoOrdini) {
             try {
                 for(int i = 0; i < quantita; i++) {
                     IOrdine ordine = new Ordine(basePartenza, baseDestinazione);
                     elencoOrdini.add(ordine);
                    synchronized(storicoOrdini) {
                        storicoOrdini.add(ordine);
                    }
                     aggiornaOrdiniGUI();
                 }
             } catch(RemoteException e) {
                 System.out.println("Dio: Errore durante la creazione di un nuovo ordine");
             }
             elencoOrdini.notify();
         }
     }
     
     /**
      * Metodo che crea una stringa con la lista di tutti gli ordini per aggiornare 
      * la lista degli ordini nell'interfaccia grafica
      */
     private void aggiornaOrdiniGUI() {
         String text = "";
         synchronized(storicoOrdini) {
             for(IOrdine ordine : storicoOrdini) {
                 try {
                     text += ordine.stampaStato() + "\n";
                 } catch(RemoteException e) {
                     System.out.println("Dao: Errore di comunicazione con un ordine");
                 }
             }
         }
         gui.aggiornaOrdiniTextArea(text);
     }
     
     /**
      * Thread che controlla ogni tempo millisecondi che le basi siano attive, 
      * in caso contrario imposta la base come non attiva e avvisa gli autotreni 
      * attivi e non in viaggio di controllare che la base presso cui sono parcheggiati 
      * sia ancora attiva
      */
     private class ControllaBasi implements Runnable {
         @Override
         public void run() {
             while(!terminato) {
                 //prendo il lock sulla lista di basi attive
                 //controllo che le basi esistano, altrimenti le cancello dall'elenco
                 synchronized(basiAttive) {
                     if(!terminato) {
                         for(IBase base : basiAttive.keySet()) {
                             //controllo solo le basi ancora attive
                             if(basiAttive.get(base)) {
                                 try {
                                     base.stato();
                                 } catch(RemoteException e) {
                                     System.out.println("DCB: La base " + basiNomi.get(base)
                                             + " non è più attiva");
                                     aggiornaBasiAttive(base);
                                     //aggiorno le basi di partenza degli autotreni
                                     //in quanto potrebbero essere parcheggiati presso
                                     //la base che non è più attiva
                                     for(IAutotreno autotreno : autotreniAttivi.keySet()) {
                                         //controllo solo gli autotreni ancora attivi
                                         //e che non siano in viaggio
                                         try {
                                             if(autotreniAttivi.get(autotreno) 
                                                     && !autotreno.getViaggioEseguito()) {
                                                 autotreno.aggiornaBasePartenza();
                                             }
                                         } catch(RemoteException e1) {
                                             System.out.println("DCB: Errore di comunicazione"
                                                     + "con l'autotreno "
                                                     + autotreniNomi.get(autotreno));
                                             aggiornaAutotreniAttivi(autotreno);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 try {
                     Thread.currentThread().sleep(tempo);
                 } catch(InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
     
     /**
      * Thread che controlla ogni tempo millisecondi che gli autotreni siano attivi, 
      * in caso contrario imposta l'autotreno come non attivo e avvisa le basi attive 
      * di eliminare l'autotreno non più attivo dal parcheggio, se presente
      */
     private class ControllaAutotreni implements Runnable{
         @Override
         public void run() {
             while(!terminato) {
                 //prendo il lock sulla  lista degli autotreni attivi
                 //controllo che gli autotreni esistano, altrimenti li cancello dall'elenco
                 synchronized(autotreniAttivi) {
                     if(!terminato) {
                         for(IAutotreno autotreno : autotreniAttivi.keySet()) {
                             //controllo solo gli autotreni ancora attivi
                             if(autotreniAttivi.get(autotreno)) {
                                 try {
                                     autotreno.stato();
                                 } catch(RemoteException e) {
                                     System.out.println("DCA: L'autotreno " 
                                             + autotreniNomi.get(autotreno)
                                             + " non è più attivo");
                                     aggiornaAutotreniAttivi(autotreno);
                                     for(IBase base : basiAttive.keySet()) {
                                         //controllo solo le basi ancora attive
                                         if(basiAttive.get(base)) {
                                             try {
                                                 base.aggiornaListaAutotreni(autotreno);
                                             } catch(RemoteException e1) {
                                                 System.out.println("DCA: Errore di comunicazione "
                                                         + "con la base "
                                                         + basiNomi.get(base));
                                                 aggiornaBasiAttive(base);
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 try {
                     Thread.currentThread().sleep(tempo);
                 } catch(InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
     
     /**
      * Thread che, se attivo, crea ordini casuali ogni tempo millisecondi
      */
     final class CreaOrdini implements Runnable {
         private IBase basePartenza;
         private IBase baseDestinazione;
         
         private final List<IBase> listaBasi;
         
         private final int tempo = 1000;
         private final int quantitaOrdini = 5;
 
         /**
          * Costruttore che inizializza la lista delle basi
          */
         CreaOrdini() {
             listaBasi = new LinkedList<IBase>();
         }
         
         /**
          * Metodo che aggiunge una base alla lista solo se è attiva
          */
         private void creaListaBasi() {
             listaBasi.clear();
             synchronized(basiAttive) {
                 for(IBase base : basiAttive.keySet()) {
                     if(basiAttive.get(base)) {
                         listaBasi.add(base);
                     }
                 }
             }
         }
         
         /*
          * Metodo che crea ordini scegliendo base di partenza e di destinazione 
          * diverse in modo causale e li invia alla Ditta di trasporti
          */
         @Override
         public void run() {
             try {
                 while(!terminato && !Thread.currentThread().isInterrupted()) {
                     Random random = new Random();
                     creaListaBasi();
                     //proseguo solo se ci sono basi attive
                     //controllo che il thread sia attivo
                     if(!terminato && !listaBasi.isEmpty()) {
                         basePartenza = listaBasi. get(random.nextInt(listaBasi.size()));
                         baseDestinazione = listaBasi.get(random.nextInt(listaBasi.size()));
                         //controllo che il thread sia attivo
                         //controllo che le basi siano diverse
                         while(!terminato && basiNomi.get(basePartenza).equals(basiNomi.get(baseDestinazione))) {
                             baseDestinazione = listaBasi.get(random.nextInt(listaBasi.size()));
                         }
                         inserisciOrdine(basiNomi.get(basePartenza), basiNomi.get(baseDestinazione), random.nextInt(quantitaOrdini));
 
                         Thread.currentThread().sleep(tempo);
                     }
                 }
             } catch(InterruptedException e) {
                 gui.aggiornaStatoTextArea("Generazione automatica degli ordini interrotta");
             }
         }
     }
     
     /**
      * Metodo che termina l'attività di tutte le basi, di tutti gli autotreni attivi 
      * e termina l'attività della ditta di trasporti; tenta inoltre di rimuovere 
      * la Ditta dal registro RMI
      */
     final void terminaAttivita() {
         terminato = true;
         
         //risveglio gli eventuali thread dormienti
         synchronized(elencoOrdini) {
             elencoOrdini.notify();
         }
         
         //termina tutti gli autotreni
         for(IAutotreno autotreno : autotreniAttivi.keySet()) {
             if(autotreniAttivi.get(autotreno)) {
                 try {
                     autotreno.terminaAttivita();
                 } catch(RemoteException ignore) {}
             }
         }
         
         //termina tutte le basi
         for(IBase base : basiAttive.keySet()) {
             if(basiAttive.get(base)) {
                 try {
                     base.terminaAttivita();
                 } catch(RemoteException ignore) {}
             }
         }
         
         //rimuove dal registro RMI la ditta di trasporti
         try {
             String rmiNomeDitta = "rmi://" + HOST + "/dittaTrasporti";
             Naming.unbind(rmiNomeDitta);
         } catch(RemoteException e) {
             System.out.println("Dta: Errore nella cancellazione della registrazione "
                     + "della ditta dal registro RMI");
         } catch(MalformedURLException e1) {
             e1.printStackTrace();
         } catch(NotBoundException e2) {
             e2.printStackTrace();
         }
         
         //chiudo l'interfaccia utente
         gui.dispose();
         
         //chiudo la ditta
         System.exit(0);
     }
     
     @Override
     public final void registraBase(final IBase base) {
         //prendo il lock sulla lista delle basi attive
         //aggiungo la nuova base
         synchronized(basiAttive) {
             basiAttive.put(base, true);
         }
         
         try {
             //prendo il lock sulla mappa dei nomi delle basi
             //aggiorno la mappa dei nomi delle basi
             synchronized(nomiBasi) {
                     nomiBasi.put(base.getNomeBase(), base);
                     nomiBasi.notifyAll();
             }
             
             //aggiorno la mappa delle basi
             synchronized(basiNomi) {
                     basiNomi.put(base, base.getNomeBase());
             }
         } catch(RemoteException e) {
             System.out.println("Drb: Errore di connessione con una base in fase "
                     + "di registrazione");
         }
         
         gui.aggiornaStatoTextArea("La base "+ basiNomi.get(base) 
             + " si è registrata");
         //aggiorno i combo box della gui che contengono le basi
         gui.aggiungiBaseComboBox(basiNomi.get(base));
         //avvio il thread che controlla l'esistenza delle basi
         new Thread(this.new ControllaBasi()).start();
     }
     
     @Override
     public final IBase registraAutotreno(final IAutotreno autotreno, final String nomeBasePartenza) {
         IBase partenza = null;
         try {
             //prendo il lock sulla mappa dei nomi delle basi
             //controllo che la base richiesta dall'autoreno sia già registrata
             synchronized(nomiBasi) {
                 while(!nomiBasi.containsKey(nomeBasePartenza)) {
                     nomiBasi.wait();
                 }
                 //prendo il lock sulla lista di autotreni attivi
                 synchronized(autotreniAttivi) {
                     autotreniAttivi.put(autotreno, true);
                 }
                 partenza = nomiBasi.get(nomeBasePartenza);
             }
         } catch(InterruptedException e) {
             e.printStackTrace();
         }
         
         try {
             //prendo il lock sulla mappa dei nomi degli autotreni
             //aggiorno la mappa dei nomi delgi autotreni
             synchronized(nomiAutotreni) {
                 nomiAutotreni.put(autotreno.getNomeAutotreno(), autotreno);
             }
 
             //prendo il lock sulla mappa degli autotreni
             //aggiorno la mappa degli autotreni
             synchronized(autotreniNomi) {
                 autotreniNomi.put(autotreno, autotreno.getNomeAutotreno());
             }
         } catch(RemoteException e) {
             System.out.println("Dra: Errore di comunicazione con un autotreno in "
                     + "fase di registrazione");
         }
 
         gui.aggiornaStatoTextArea("L'autotreno " + autotreniNomi.get(autotreno)
                 + " si è registrato presso la base " + nomeBasePartenza);
         
         //avvio il thread che controlla l'esistenza degli autotreni
         new Thread(this.new ControllaAutotreni()).start();
         
         return partenza;
     }
 
     @Override
     public final void notificaEsito(final IOrdine ordine) {
         try {
             gui.aggiornaStatoTextArea(ordine.stampaEsito());
         } catch(RemoteException e) {
             System.out.println("Dne: Errore di comunicazione con un ordine consegnato");
         }
         aggiornaOrdiniGUI();
     }
 
     /**
      * Metodo che aggiorna la lista delle basi attive rimuovendo la base passata 
      * come parametro
      * 
      * @param base                  riferimento alla base non più attiva
      */
     private void aggiornaBasiAttive(final IBase base) {
         rimuoviBase(base);
         gui.aggiornaStatoTextArea("La base " + basiNomi.get(base)
                 + " non è più attiva");
     }
     
     /**
      * Metodo che imposta come non attiva la base passata come parametro nella lista 
      * delle basi attive e avvisa la GUI di aggiornarsi
      * 
      * @param base                  riferimento alla base non più attiva
      */
     private void rimuoviBase(final IBase base) {
         synchronized(basiAttive) {
             basiAttive.put(base, false);
         }
         gui.rimuoviBaseComboBox(basiNomi.get(base));
     }
 
     /**
      * Metodo che aggiorna la lista degli autotreni attivi rimuovendo l'autotreno 
      * passato come parametro; inoltre cerca il possibile ordine in transito con l'autotreno
      * e ne imposta lo stato ad abortito
      * 
      * @param autotreno             riferimento all'autotreno non più attivo
      */
     private void aggiornaAutotreniAttivi(final IAutotreno autotreno) {
         for(IOrdine ordine : storicoOrdini) {
             try {
                 if("in transito".equals(ordine.getStato())) {
                     if(autotreniNomi.get(ordine.getAutotreno()).equals(autotreniNomi.get(autotreno))) {
                         ordine.setStato("abortito");
                         ordine.getBasePartenza().notificaOrdine(ordine);
                     }
                 }
             } catch(RemoteException e) {
                 System.out.println("Daaa: Errore di connessione con un ordine");
             }
         }
         rimuoviAutotreno(autotreno);
         gui.aggiornaStatoTextArea("L'autotreno " + autotreniNomi.get(autotreno)
                 + " non è più attivo");
     }
     
     /**
      * Metodo che imosta a non attivo l'autotreno passato come parametro nella lista 
      * degli autotreni attivi
      * 
      * @param autotreno             riferimento all'autotreno non più attivo
      */
     private void rimuoviAutotreno(final IAutotreno autotreno) {
         synchronized(autotreniAttivi) {
             autotreniAttivi.put(autotreno, false);
         }
     }
     
     @Override
     public final IBase impostaNuovaBase(final IAutotreno autotreno) throws RemoteException {
         IBase nuovaBase = null;
         for(IBase base : basiAttive.keySet()) {
             if(basiAttive.get(base)) {
                 nuovaBase = base;
             }
         }
         return nuovaBase;
     }
     
     /**
      * Thread che gestisce l'invio degli ordini alle rispettive basi
      */
     final class InviaOrdini implements Runnable {
         private IBase partenza;
         private IOrdine ordine;
         
         /**
          * Metodo che esegue l'invio degli ordini alle rispettive basi; recupera 
          * il primo ordine sulla lista e la relativa base di partenza e avvisa tale 
          * base di inviare l'ordine
          */
         @Override
         public void run() {
             while(!terminato) {
                 try {
                     //prendo il lock sull'elenco degli ordini e controllo che non sia vuoto
                     synchronized(elencoOrdini) {
                         while(!terminato && elencoOrdini.isEmpty()) {
                             elencoOrdini.wait();
                         }
                         //recupero il primo ordine dall alista
                         if(!terminato) {
                             ordine = elencoOrdini.poll();
                         }
                     }
                     if(!terminato) {
                         //recupero la base di partenza dell'ordine
                         try {
                             partenza = ordine.getBasePartenza();
                         } catch(RemoteException e) {
                             System.out.println("DIO: Errore di comunicazione con un ordine");
                         }
                         //avviso la base di partenza di consegnare il nuovo ordine
                         try {
                             partenza.registraOrdine(ordine);
                         } catch(RemoteException e) {
                             System.out.println("DIO: Errore di comunicazione con la base "
                                     + basiNomi.get(partenza));
                             aggiornaBasiAttive(partenza);
                         }
                     }
                 } catch(InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 }
