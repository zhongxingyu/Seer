 package View;
 
 import Controller.*;
 import Model.*;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 /**
  * To change this template use File | Settings | File Templates.
  */
 public class TicketVerkoop extends JFrame {
 
     private static TicketVerkoop ticketVerkoop;
 
     private JComboBox voorstelling;
     private JComboBox complex;
     private JButton registreerVerkoop;
     private JButton annuleer;
     private JSpinner aantal;
 
     private JLabel lblVoorstelling;
     private JLabel lblAantal;
     private JLabel lblVerkoopsKanaal;
 
     private VoorstellingController vc;
     private VerkoopKanaalController vkc;
     private ComplexController cc;
 
     private ZoneController zc;
     private ZetelController zetc;
     private JComboBox verkoopKanaal;
     private JComboBox zone;
     private final int MAX_AANTAL = 500;
     private JComboBox zetels;
 
     private JPanel panel;
     private JLabel lblComplex;
 
     private JLabel lblZone;
     private JLabel lblZetel;
     private JLabel lblKlant;
     private JCheckBox chkKlant;
     private JComboBox klant;
     private KlantController kc;
     private JLabel lblPrijs;
 
     private TicketVerkoop() throws HeadlessException {
         maakComponenten();
         maakLayout();
         voegListenersToe();
         toonFrame();
     }
 
     public static synchronized TicketVerkoop getTicketVerkoop(){
         if (ticketVerkoop == null){
             ticketVerkoop = new TicketVerkoop();
         }
         return ticketVerkoop;
     }
 
     private void maakComponenten() {
         cc = new ComplexController();
         complex = new JComboBox(cc.getComplexen().toArray());
         verkoopKanaal = new JComboBox();
         vulVerkoopKanaal();
         registreerVerkoop = new JButton("Registreer Verkoop");
         annuleer = new JButton("Annuleer");
         lblComplex = new JLabel("Complex");
         aantal = new JSpinner(new SpinnerNumberModel(1, 1 ,MAX_AANTAL, 1));
         voorstelling= new JComboBox();
         zetels = new JComboBox();
         vulVoorstellingen();
         lblVoorstelling = new JLabel("Voorstelling");
         lblAantal = new JLabel("Aantal");
         lblVerkoopsKanaal = new JLabel("Verkoopskanaal");
         zone = new JComboBox();
         lblZone = new JLabel("Zone");
         lblZetel = new JLabel("Zetel");
         checkZetelReservatie();
         lblKlant = new JLabel("Klant");
         kc = new KlantController();
         chkKlant = new JCheckBox("Niet Van Toepassing");
         klant = new JComboBox(kc.getKlanten().toArray());
         lblPrijs = new JLabel("Prijs");
     }
 
     private void maakLayout() {
         panel = new JPanel(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.weightx= 0.5;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel.add(lblComplex, gbc);
         gbc.gridx = 1;
         panel.add(complex, gbc);
         gbc.gridx = 0;
         gbc.gridy = 2;
         panel.add(lblVoorstelling, gbc);
         gbc.gridx = 1;
         gbc.gridy = 2;
         panel.add(voorstelling, gbc);
         gbc.gridx = 0;
         gbc.gridy = 3;
         panel.add(lblAantal, gbc);
         gbc.gridx = 1;
         gbc.gridy = 3;
         panel.add(aantal, gbc);
         gbc.gridx = 0;
         gbc.gridy = 4;
         panel.add(lblVerkoopsKanaal, gbc);
         gbc.gridx = 1;
         gbc.gridy = 4;
         panel.add(verkoopKanaal, gbc);
 
         gbc.gridx = 3;
         gbc.gridy = 2;
         panel.add(lblZone, gbc);
         gbc.gridx = 4;
         gbc.gridy = 2;
         panel.add(zone, gbc);
         gbc.gridx = 3;
         gbc.gridy = 3;
         panel.add(lblZetel, gbc);
         gbc.gridx = 4;
         gbc.gridy = 3;
         panel.add(zetels, gbc);
         gbc.gridx = 0;
         gbc.gridy = 5;
         panel.add(lblKlant, gbc);
         gbc.gridx = 1;
         gbc.gridy = 5;
         panel.add(klant, gbc);
         gbc.gridx = 0;
         gbc.gridy = 6;
         panel.add(chkKlant, gbc);
         gbc.gridx = 0;
         gbc.gridy = 7;
         gbc.ipady = 30;
         panel.add(registreerVerkoop, gbc);
         gbc.gridx = 1;
         gbc.gridy = 7;
         panel.add(annuleer, gbc);
         gbc.gridx = 2;
         gbc.gridy = 7;
         gbc.ipadx = 50;
         panel.add(lblPrijs, gbc);
         add(panel);
     }
 
     private void voegListenersToe() {
         registreerVerkoop.addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 VerkoopController vkc = new VerkoopController();
                 Voorstelling v = (Voorstelling)voorstelling.getSelectedItem();
                 Klant k = null;
                 if (klant.isEnabled()){
                     k = (Klant) klant.getSelectedItem();
                 }
                 double prijs = vkc.voegVerkoopToe(v, k, (Integer) aantal.getValue(), (VerkoopKanaal) verkoopKanaal.getSelectedItem(), (Zetel) zetels.getSelectedItem(), (Zone) zone.getSelectedItem());
                 lblPrijs.setText("Prijs €" + prijs);
             }
         });
         annuleer.addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 TicketVerkoop.this.setVisible(false);
             }
         });
         voorstelling.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 try{
                checkZetelReservatie();
                 TicketVerkoop.this.pack();
                 }catch (NullPointerException ex){
                     //voorstellinglijst nog aan het invullen...
                 }
             }
         });
         complex.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 vulVerkoopKanaal();
                 vulVoorstellingen();
             }
         });
         zone.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (zone.getItemCount() > 0){
                     vulZetels((Zone) zone.getSelectedItem());
                 }
             }
         });
         chkKlant.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (e.getStateChange() == ItemEvent.SELECTED){
                     klant.setEnabled(false);
                 }
                 else {
                     klant.setEnabled(true);
                 }
             }
         });
     }
 
     private void vulZetels(Zone z) {
         zetc = new ZetelController();
         zetels.removeAllItems();
         for (Zetel zet : (ArrayList<Zetel>) zetc.getBeschikbarePlaatsen((Voorstelling)voorstelling.getSelectedItem(), z)){
             zetels.addItem(zet);
         }
     }
 
     private void vulVoorstellingen() {
         vc = new VoorstellingController();
         Complex c = (Complex)complex.getSelectedItem();
         voorstelling.removeAllItems();
         for (Voorstelling voors : (ArrayList<Voorstelling>) vc.getVoorstellingen(c)){
             voorstelling.addItem(voors);
         }
     }
 
     private void toonFrame() {
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setBounds(200, 200, 400, 280);
         pack();
     }
 
 
     private void checkZetelReservatie(){
         zc = new ZoneController();
         Voorstelling v = (Voorstelling)voorstelling.getSelectedItem();
         if (v.isZetelReservatie()){
             zone.removeAllItems();
             for (Zone zo : (ArrayList<Zone>) zc.getZones(v.getZaal())){
                 zone.addItem(zo);
             }
             vulZetels((Zone)zone.getSelectedItem());
             zone.setVisible(true);
             lblZetel.setVisible(true);
             lblZone.setVisible(true);
             lblAantal.setVisible(false);
             aantal.setVisible(false);
             zetels.setVisible(true);
         }
         else{
             lblZetel.setVisible(false);
             lblZone.setVisible(false);
             zone.setVisible(false);
             zetels.setVisible(false);
             lblAantal.setVisible(true);
             aantal.setVisible(true);
         }
     }
 
 
     private void vulVerkoopKanaal(){
         vkc = new VerkoopKanaalController();
         Complex c = (Complex)complex.getSelectedItem();
         verkoopKanaal.removeAllItems();
         for (VerkoopKanaal verk : (ArrayList<VerkoopKanaal>) vkc.getVerkoopKanalen(c)){
             verkoopKanaal.addItem(verk);
         }
     }
 
 }
