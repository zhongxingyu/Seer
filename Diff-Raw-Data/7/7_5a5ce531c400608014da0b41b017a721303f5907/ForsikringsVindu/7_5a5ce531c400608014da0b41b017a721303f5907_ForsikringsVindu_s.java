 /* 
  * Innlevering 4 - 16/11-2011
  * Kristoffer Berdal - s180212
  * Jan E. Vandevjen - s180494
  * Tommy Nyrud - s180487
  * Informasjonsteknologi 1IA og
  * Dataingeniør 1AA 
  */
 
 /* Konstruerer GUI og sørger for all knappefunksjon.
    Koden er redusert til å kun omhhandle bilforsikring slik som vi ble bedt om,
    resten av koden er kommentert ut. */
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class ForsikringsVindu extends JFrame
 {
 
   private JTextField kundefelt, fakturafelt, nrfelt;
   //private JTextField adressefelt,boligtypefelt, arealfelt,
   //	                   verdifelt, sumfelt;
   private JTextField biltypefelt,regårfelt, regnrfelt,
 	                   lengdefelt, bonusfelt;
   //private JTextField reisesumfelt;
 
   private JTextArea utskrift;
 
   private JButton regKunde, visKunde, slettKunde, regBil/*,
           regHusInnbo, regReise*/, visAlle;
 
   ForsikringsRegister kunderegisteret = new ForsikringsRegister();
 
   public ForsikringsVindu()
   {
     super("TRYGG FORSIKRING");
     Lytter lytter = new Lytter();
 
     Container c = getContentPane();
 
 
     JPanel vestpanel = new JPanel();
     vestpanel.setLayout(new GridLayout(23,1));
 
     vestpanel.add(new JLabel("REGISTRER KUNDE "));
 
     JPanel p1 = new JPanel();
     p1.setLayout(new GridLayout(1,2));
 	kundefelt = new JTextField(20);
 	kundefelt.addActionListener(lytter);
 	p1.add(new JLabel("Kundens navn: "));
 	p1.add(kundefelt);
 	vestpanel.add(p1);
 
     JPanel p2 = new JPanel();
     p2.setLayout(new GridLayout(1,2));
 	fakturafelt = new JTextField(20);
 	fakturafelt.addActionListener(lytter);
 	p2.add(new JLabel("Fakturaadresse: "));
 	p2.add(fakturafelt);
 	vestpanel.add(p2);
 
 	JPanel p = new JPanel();
 	p.setLayout(new GridLayout(1,2));
 	nrfelt = new JTextField(20);
     p.add(new JLabel("Kundenr: "));
 	p.add(nrfelt);
 	vestpanel.add(p);
 
     JPanel knappepanel = new JPanel();
 	knappepanel.setLayout(new GridLayout(1,4));
 	regKunde = new JButton("Ny kunde");
 	regKunde.addActionListener(lytter);
 	visKunde = new JButton("Vis kunde");
 	visKunde.addActionListener(lytter);
 	slettKunde = new JButton("Slett kunde");
 	slettKunde.addActionListener(lytter);
 	visAlle = new JButton("Kunderegister");
 	visAlle.addActionListener(lytter);
 
     knappepanel.add(regKunde);
 	knappepanel.add(visKunde);
 	knappepanel.add(slettKunde);
 	knappepanel.add(visAlle);
 
 	vestpanel.add(knappepanel);
 
 
     /*vestpanel.add(new JLabel("HUS OG INNBO-FORSIKRING "));
 
     JPanel p3 = new JPanel();
     p3.setLayout(new GridLayout(1,2));
 	adressefelt = new JTextField(20);
 	adressefelt.addActionListener(lytter);
 	p3.add(new JLabel("Forsikringsadresse: "));
 	p3.add(adressefelt);
     vestpanel.add(p3);
 
     JPanel p4 = new JPanel();
     p4.setLayout(new GridLayout(1,2));
     boligtypefelt = new JTextField(20);
 	boligtypefelt.addActionListener(lytter);
 	p4.add(new JLabel("Boligtype "));
 	p4.add(boligtypefelt);
 	vestpanel.add(p4);
 
     JPanel p5 = new JPanel();
     p5.setLayout(new GridLayout(1,2));
     arealfelt = new JTextField(20);
 	arealfelt.addActionListener(lytter);
 	p5.add(new JLabel("Areal "));
 	p5.add(arealfelt);
 	vestpanel.add(p5);
 
     JPanel p6 = new JPanel();
     p6.setLayout(new GridLayout(1,2));
 	verdifelt = new JTextField(20);
 	verdifelt.addActionListener(lytter);
 	p6.add(new JLabel("Boligens fullverdigrunnlag "));
 	p6.add(verdifelt);
     vestpanel.add(p6);
 
     JPanel p7 = new JPanel();
     p7.setLayout(new GridLayout(1,2));
 	sumfelt = new JTextField(20);
 	sumfelt.addActionListener(lytter);
 	p7.add(new JLabel("Sum innbo og l�s�re "));
 	p7.add(sumfelt);
     vestpanel.add(p7);
 
 	regHusInnbo = new JButton("Tegn hus og innboforsikring");
 	regHusInnbo.addActionListener(lytter);
 	vestpanel.add(regHusInnbo);*/
 
 
     vestpanel.add(new JLabel("BIL-FORSIKRING "));
 
     JPanel p8 = new JPanel();
     p8.setLayout(new GridLayout(1,2));
     biltypefelt = new JTextField(20);
 	biltypefelt.addActionListener(lytter);
 	p8.add(new JLabel("Biltype "));
 	p8.add(biltypefelt);
     vestpanel.add(p8);
 
     JPanel p9 = new JPanel();
     p9.setLayout(new GridLayout(1,2));
     regårfelt = new JTextField(20);
 	regårfelt.addActionListener(lytter);
 	p9.add(new JLabel("Registreringsår "));
 	p9.add(regårfelt);
     vestpanel.add(p9);
 
     JPanel p10 = new JPanel();
     p10.setLayout(new GridLayout(1,2));
     regnrfelt = new JTextField(20);
 	regnrfelt.addActionListener(lytter);
 	p10.add(new JLabel("Registringsnummer "));
 	p10.add(regnrfelt);
 	vestpanel.add(p10);
 
     JPanel p11 = new JPanel();
     p11.setLayout(new GridLayout(1,2));
     lengdefelt = new JTextField(20);
 	lengdefelt.addActionListener(lytter);
 	p11.add(new JLabel("Årlig kjørelengde "));
 	p11.add(lengdefelt);
 	vestpanel.add(p11);
 
     JPanel p12 = new JPanel();
     p12.setLayout(new GridLayout(1,2));
     bonusfelt = new JTextField(20);
 	bonusfelt.addActionListener(lytter);
 	p12.add(new JLabel("Bonus "));
 	p12.add(bonusfelt);
 	vestpanel.add(p12);
 
 	regBil = new JButton("Tegn bilforsikring");
 	regBil.addActionListener(lytter);
 	vestpanel.add(regBil);
 
     /*vestpanel.add(new JLabel("REISE-FORSIKRING "));
 
     JPanel p13 = new JPanel();
     p13.setLayout(new GridLayout(1,2));
     reisesumfelt = new JTextField(20);
 	reisesumfelt.addActionListener(lytter);
 	p13.add(new JLabel("Reiseforsikringssum "));
 	p13.add(reisesumfelt);
     vestpanel.add(p13);
 
 
 	regReise = new JButton("Tegn reiseforsikring");
 	regReise.addActionListener(lytter);
 	vestpanel.add(regReise);*/
 
     JPanel utskriftspanel = new JPanel();
         
 	utskrift = new JTextArea(21,30);
 	utskriftspanel.add(new JScrollPane(utskrift));
 
 	JPanel registreringspanel = new JPanel();
 
     registreringspanel.add(vestpanel);
 	c.add(registreringspanel,BorderLayout.WEST);
 
 
 	c.add(utskriftspanel,BorderLayout.EAST);
 	setSize(850, 400);
 	setVisible(true);
 	setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
  }
 
 
 		public void slettKundeFelt()
 		{
 			kundefelt.setText("");
 			fakturafelt.setText("");
 			nrfelt.setText("");
 		}
 
 		public void slettBilFelt()
 		{
 			biltypefelt.setText("");
 			regårfelt.setText("");
 			regnrfelt.setText("");
 			lengdefelt.setText("");
 			bonusfelt.setText("");
 		}
                 /*
 		public void slettHusFelt()
 		{
 			adressefelt.setText("");
 			boligtypefelt.setText("");
 			arealfelt.setText("");
 			verdifelt.setText("");
 			sumfelt.setText("");
 		}
 
 		public void slettReiseFelt()
 		{
 			reisesumfelt.setText("");
 	  }
 */
     
     //Metode for å registrere en ny kunde
     public void registrerNyKunde() {
       boolean failed = false;
       String kundenavn = "";
       String adresse = "";
       ForsikringsKunde kunden = null;
       try {
         kundenavn = kundefelt.getText();
         adresse = fakturafelt.getText();
       }
       catch (Exception e) {
         failed = true;
       }
       
       if (!failed && !kundenavn.equals("") && !adresse.equals("")) {
         // Vi har verdier som ikke er tomme.
         kunden = new ForsikringsKunde(kundenavn, adresse);
       }
       else {
         utskrift.append("Kunden ble ikke registert fordi du ikke har oppgitt nok informasjon.\n\n");
       }
       if (kunden != null) {
         if(kunderegisteret.nyKunde(kunden)) {
         utskrift.append("Ny forsikringskunde ble registrert\n\n");
         }
       
         else {
           utskrift.append("Kunden ble ikke registert\n\n");
         }
       }
     }
 
     //Metode for å finne en gitt kunde
     public void visKunde() {
       boolean failed = false;
       ForsikringsKunde kunden = null;
       
       try { 
         int nr = Integer.parseInt(nrfelt.getText());
       
         kunden = kunderegisteret.finnKunde(nr);
       }
       
       catch(Exception e) {
         failed = true;
       }
       
       if(!failed) {
         if(kunden != null) {
         utskrift.append(kunden.toString());
         }
       }
         else {
           utskrift.append("Kunden finnes ikke i registeret\n");
         }
     }
 
     //Metode for å slette en gitt kunde
     public void slettKunde() {
       boolean failed = false;
       ForsikringsKunde kunden = null;
       int nr = -1;
       
       try { 
         nr = Integer.parseInt(nrfelt.getText());
         kunden = kunderegisteret.finnKunde(nr);
       }
       
       catch(Exception e) {
         failed = true;
       }
       
       if(!failed) {
         if(kunden != null) {
         kunderegisteret.fjernKunde(nr);
         utskrift.append("Kundenr " + nr + " ble slettet\n");
         }
       }
       
       else {
         utskrift.append("Ingen kunde med dette kundenummeret finnes i systemet\n");
       }
       
     }
     
     //Viser alle kundene i registeret
     public void visKunderegister() {
       kunderegisteret.visRegister(utskrift);
     }
     
    //GÅR ANN Å REGISTRERE EN FORSIKRING UTEN REGNR, MEN IKKE UTEN NOE ANNET?!??! FIKS
     //Registrerer en ny BilForikring på gitt bruker
     public void tegnBilForsikring(ForsikringsKunde k) {
       String type = "";
       int regår = -1;
       String regnr = "";
       int lengde = -1;
       double bonus = -1;
       boolean failed = false;
       try {
        
         type = biltypefelt.getText(); 
         regår = Integer.parseInt(regårfelt.getText());
         regnr = regnrfelt.getText();
         lengde = Integer.parseInt(lengdefelt.getText());
         bonus = Double.parseDouble(bonusfelt.getText());
       }
       
       catch (Exception e) {
         failed = true;
       }
       if (!failed && type != null && !type.equals("") && regår != -1 && regnr != null && !regnr.equals("") && lengde != -1 ) {
         // Nå burde vi ha alle.
         BilForsikring bilForsikring = new BilForsikring(type, regår, regnr, lengde, bonus);
        k.tegnForsikring(bilForsikring);
        utskrift.append("Ny forsikring ble opprettet\n");  
       }
       else {
         utskrift.append("Forsikringen ble ikke opprettet,"
                 + "\nNoen felt har feil informasjon eller mangler\n");
       }
 
     }
 
     /*public void tegnHusInnboForsikring( ForsikringsKunde k ) {
       < Metoden leser inn n�dvendig informasjon for og oppretter
         hus- og innboforsikring for den kunden som parameteren k angir,
         under forutsetning av at vedkommende ikke har hus- og innboforsikring
         fra f�r. I tekstomr�det utskrift gis det melding om utfallet
         av opprettelsen.
         Merk! Denne metoden skal du IKKE programmere! >
     } */
 
     /*public void tegnReiseForsikring( ForsikringsKunde k ) {
       < Metoden leser inn n�dvendig informasjon for � opprette
         reiseforsikring for den kunden som parameteren k angir, under
         forutsetning av at vedkommende ikke har reiseforsikring fra
         f�r. I tekstomr�det utskrift gis det melding om utfallet
         av opprettelsen.
         Merk! Denne metoden skal du IKKE programmere! >
     } */
     
     //Lager en ny forsikring utifra hvilken knapp som ble trykket på
     public void tegnForsikring( int type ) {
       boolean failed = false;
       ForsikringsKunde kunden = null;
       int nr = -1;
       
       try { 
         nr = Integer.parseInt(nrfelt.getText());
         kunden = kunderegisteret.finnKunde(nr);
       }
       
       catch(Exception e) {
         failed = true;
       }
       
       if(!failed) {
         if(kunden != null) {
           switch (type) {
         
             case Forsikring.BIL:
               tegnBilForsikring(kunden);
               break;
              
             /*case Forsikring.HUS_INNBO:
               tegnHusInnboForsikring(kunden);
               break;
               
             case Forsikring.REISE:
               tegnReiseForsikring(kunden);
               break; */
           }
           
         }
       }
       
       else {
         utskrift.append("Ingen kunde med dette kundenummeret finnes i systemet");
       }
 
     }
 
     // Lytterklasse. Knapper som er kommentert bort er ikke lagt til
     private class Lytter implements ActionListener {
   
     @Override
     public void actionPerformed(ActionEvent e) {
       
       if (e.getSource() == regKunde) {
         registrerNyKunde();
       }
       
       else if(e.getSource() == visKunde) {
         visKunde();
       }
       
       else if(e.getSource() == slettKunde) {
         slettKunde();
       }
       
       else if(e.getSource() == visAlle) {
         visKunderegister();
       }
       
       else if(e.getSource() == regBil) {
         tegnForsikring(Forsikring.BIL);
       }
            
     }
       
     }   
   }
