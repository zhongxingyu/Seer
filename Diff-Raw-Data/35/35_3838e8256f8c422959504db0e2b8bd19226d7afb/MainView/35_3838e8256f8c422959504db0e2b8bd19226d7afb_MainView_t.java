 package severeLobster.frontend.view;
 
 import infrastructure.ResourceManager;
 import infrastructure.components.Koordinaten;
 import infrastructure.constants.enums.SpielmodusEnumeration;
 import severeLobster.backend.spiel.Spiel;
 import severeLobster.backend.spiel.SternenSpielApplicationBackend;
 import severeLobster.frontend.application.MainFrame;
 import severeLobster.frontend.controller.SpielfeldDarstellungsSteuerung;
 import severeLobster.frontend.controller.SpielmodusViewController;
 import severeLobster.frontend.controller.TrackingControllViewController;
 import severeLobster.frontend.dialogs.SpielfeldGroessenDialog;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 public class MainView extends JPanel {
 
     private final ResourceManager resourceManager = ResourceManager.get();
     private final Image backgroundimage = getToolkit().getImage(
             resourceManager.getGraphicURL("sternenhimmel.jpg"));
     private JPanel jpMenu = null;
     private JPanel jpKampagne = null;
     private JPanel jpSpielAuswahl = null;
    /**
     * Wird am Anfang initialisiert und je nach Methode immer wieder vom Panel
     * genommen oder neu drauf gesetzt, aber nie wieder neu erzeugt.
     */
    private final SpielfeldDarstellung spielfeldDarstellung;
 
     private final SternenSpielApplicationBackend backend;
 
     public SternenSpielApplicationBackend getBackend() {
         return this.backend;
     }
 
     public MainView(final SternenSpielApplicationBackend backend)
             throws IOException {
         this.backend = backend;
        this.spielfeldDarstellung = new SpielfeldDarstellung();
        new SpielfeldDarstellungsSteuerung(spielfeldDarstellung, backend);
         addMenuPanel();
         setVisible(true);
     }
 
     /**
      * Erstellt ein neues SpiefeldPanel anhand des übergebenen PuzzleNamens aus
      * dem Resource Ordner
      * 
      * @param strPuzzleName
      *            - Der Name des zu ladenen Puzzles (ohne ".puz")
      * @return - JPanel auf dem das Spielfeld und das SpielInfo panel zu sehen
      *         sind
      * @author fwenisch
      * @date 04.11.2012
      */
     public void addNewSpielfeld(String strPuzzleName) {
         try {
             backend.startNewSpielFrom(strPuzzleName);
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
        JPanel spielfeldUndInfoViewPanel = new JPanel();
         final SpielmodusViewPanel spielmodusView = new SpielmodusViewPanel();
         new SpielmodusViewController(spielmodusView, backend);
        this.spielfeldDarstellung.setPreferredSize(new Dimension(500, 500));
         final TrackingControllView trackingView = new TrackingControllView();
         final TrackingControllViewController trackingViewCtrl = new TrackingControllViewController(
                 backend);
         trackingView.setTrackingControllViewController(trackingViewCtrl);
         JPanel spielinfo = new SpielinfoView(trackingView, backend);
        spielfeldUndInfoViewPanel.add(this.spielfeldDarstellung, BorderLayout.CENTER);
        spielfeldUndInfoViewPanel.add(spielinfo, BorderLayout.EAST);
        spielfeldUndInfoViewPanel.setOpaque(false);
         removeAll();
        add(spielfeldUndInfoViewPanel);
         validate();
         repaint();
     }
 
     /**
      * Erstellt ein neues SpiefeldPanel anhand der übergebenen Spielfeldgroesse
      * aus
      * 
      * @param x
      *            x-Achsengroesse des Spielfeldes
      * @param y
      *            y-Achsengroesse des Spielfeldes
      */
     public void addNewSpielfeld(int x, int y) {
         Spiel spiel = new Spiel(SpielmodusEnumeration.EDITIEREN);
         spiel.initializeNewSpielfeld(x, y);
 
         backend.setSpiel(spiel);
 
         JPanel Spielfeld = new JPanel();
         final SpielmodusViewPanel spielmodusView = new SpielmodusViewPanel();
         new SpielmodusViewController(spielmodusView, backend);
        this.spielfeldDarstellung.setPreferredSize(new Dimension(500, 500));
         // JPanel spielinfo = new SpielinfoView(backend);
 
        Spielfeld.add(this.spielfeldDarstellung, BorderLayout.CENTER);
         // Spielfeld.add(spielinfo, BorderLayout.EAST);
 
         Spielfeld.setOpaque(false);
         removeAll();
         add(Spielfeld);
         validate();
         repaint();
     }
 
     /**
      * Fügt das KampagnenPanel hinzu
      * 
      * @author fwenisch
      * @date 10.11.2012
      */
     public void addKampagnenPanel() {
         if (jpKampagne == null) {
             jpKampagne = new JPanel();
             jpKampagne.setPreferredSize(new Dimension(700, 600));
             jpKampagne.setMinimumSize(new Dimension(700, 600));
             JPanel jpSpielfeldAuswahl = new JPanel();
             GridLayout layout = new GridLayout(2, 5);
             layout.setHgap(10);
             jpSpielfeldAuswahl.setLayout(layout);
             jpSpielfeldAuswahl.setOpaque(false);
 
             for (int i = 0; i < 10; i++) {
                 jpSpielfeldAuswahl.add(new PuzzlePreviewView("Standardspiel0"
                         + (i + 1)));
             }
             JPanel jpBottom = new JPanel();
             jpBottom.setOpaque(false);
             jpBottom.setPreferredSize(new Dimension(600, 30));
             JButton jbBackToMenu = new JButton(
                     resourceManager.getText("back.to.main.menu"));
             jbBackToMenu.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 
                     try {
                         addMenuPanel();
                     } catch (Exception e) {
                         removeAll();
                         addMenuPanel();
                     }
                 }
             });
             jpBottom.add(jbBackToMenu);
             jpKampagne.add(jpSpielfeldAuswahl, BorderLayout.CENTER);
             jpKampagne.add(jpBottom, BorderLayout.SOUTH);
             jpKampagne.setOpaque(false);
         }
         removeAll();
         add(jpKampagne);
         validate();
         repaint();
     }
 
     /**
      * Fügt das Hauptmenü hinzu
      * 
      * @author fwenisch
      * @date 10.11.2012
      */
     public void addMenuPanel() {
         if (jpMenu == null) {
             jpMenu = new JPanel();
             jpMenu.setPreferredSize(new Dimension(450, 450));
             jpMenu.setLayout(new GridLayout(5, 0));
             jpMenu.setOpaque(false);
             JLabel jlLogo = new JLabel(resourceManager.getImageIcon("Logo.png"));
             jlLogo.setMinimumSize(new Dimension(450, 200));
             JButton jbKampagneSpielen = new JButton(
                     resourceManager.getText("start.campaign"));
             jbKampagneSpielen.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 
                     try {
                         addKampagnenPanel();
                     } catch (Exception e) {
                         removeAll();
                         addMenuPanel();
                     }
                 }
             });
             JButton jbSpielSpielen = new JButton(
                     resourceManager.getText("start.new.game"));
             jbSpielSpielen.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     try {
                         addSpielAuswahlPanel();
                     } catch (Exception e) {
                         removeAll();
                         addMenuPanel();
                     }
                 }
             });
             JButton jbSpielErstellen = new JButton(
                     resourceManager.getText("create.new.puzzle"));
             jbSpielErstellen.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     try {
                         addSpielErstellenPanel();
                     } catch (Exception e) {
                         removeAll();
                         addMenuPanel();
                     }
                 }
             });
             jpMenu.add(jlLogo);
             jpMenu.add(jbKampagneSpielen);
             jpMenu.add(jbSpielSpielen);
             jpMenu.add(jbSpielErstellen);
         }
         MainFrame.controlSpielMenue(false);
         MainFrame.controlEditierMenue(false);
         removeAll();
         add(jpMenu);
         validate();
         repaint();
     }
 
     public void addSpielAuswahlPanel() {
         if (jpSpielAuswahl == null) {
             jpSpielAuswahl = new JPanel();
             jpSpielAuswahl.setPreferredSize(new Dimension(450, 450));
             jpSpielAuswahl.setLayout(new GridLayout(3, 0));
             jpSpielAuswahl.setOpaque(false);
             JLabel jlLogo = new JLabel(resourceManager.getImageIcon("Logo.png"));
             jlLogo.setMinimumSize(new Dimension(450, 200));
             JButton jbLokalSpielSpielen = new JButton(
                     resourceManager.getText("start.own.game"));
             jbLokalSpielSpielen.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 
                     try {
                         MainFrame.neuesSpielOeffnen();
                     } catch (Exception e) {
                         removeAll();
                         addMenuPanel();
                     }
                 }
             });
             JButton jbOnlineSpielSpielen = new JButton(
                     resourceManager.getText("search.online.archive"));
             jbOnlineSpielSpielen.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     try {
                         addOnlineSpielAuswahlPanel();
                     } catch (Exception e) {
                         removeAll();
                         addMenuPanel();
                     }
                 }
             });
 
             jpSpielAuswahl.add(jlLogo);
             jpSpielAuswahl.add(jbLokalSpielSpielen);
             jpSpielAuswahl.add(jbOnlineSpielSpielen);
 
         }
         removeAll();
         add(jpSpielAuswahl);
         validate();
         repaint();
     }
 
     public void addSpielErstellenPanel() {
         Koordinaten koordinaten = SpielfeldGroessenDialog.show(MainFrame.frame);
         addNewSpielfeld(koordinaten.getX(), koordinaten.getY());
     }
 
     public void addOnlineSpielAuswahlPanel() {
         MainFrame.oFTP.connect();
         MainFrame.oFTP.updateFiles();
         JPanel jpAuswahl = new JPanel();
         jpAuswahl.setOpaque(false);
         jpAuswahl.setPreferredSize(new Dimension(700, 600));
         jpAuswahl.setMinimumSize(new Dimension(700, 600));
         jpAuswahl.setMaximumSize(new Dimension(700, 600));
         JPanel jpSpielfeldAuswahl = new JPanel();
 
         GridLayout layout = new GridLayout(MainFrame.oFTP.files.length, 1);
         jpSpielfeldAuswahl.setLayout(layout);
         jpSpielfeldAuswahl.setOpaque(false);
         /*
          * for (int i = 0; i < 30; i++) { jpSpielfeldAuswahl.add(new
          * PuzzlePreviewView("Standardspiel0" + (i + 1))); }
          */
         for (int i = 0; i < MainFrame.oFTP.files.length; i++) {
             jpSpielfeldAuswahl.add(new OnlinePuzzlePreviewView(
                     MainFrame.oFTP.files[i].getName()));
         }
         JPanel jpBottom = new JPanel();
         jpBottom.setOpaque(false);
         jpBottom.setPreferredSize(new Dimension(600, 30));
         JButton jbBackToMenu = new JButton(
                 resourceManager.getText("back.to.main.menu"));
         jbBackToMenu.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
 
                 try {
                     addMenuPanel();
                 } catch (Exception e) {
                     removeAll();
                     addMenuPanel();
                 }
             }
         });
         jpBottom.add(jbBackToMenu);
         JScrollPane jpScroll = new JScrollPane();
         jpScroll.setOpaque(false);
         jpScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         jpScroll.getViewport().add(jpSpielfeldAuswahl);
         jpScroll.setPreferredSize(new Dimension(700, 400));
         jpScroll.getViewport().setOpaque(false);
         JLabel jlUeberschrift = new JLabel();
         jlUeberschrift.setPreferredSize(new Dimension(700, 50));
         jlUeberschrift.setForeground(Color.YELLOW);
         jlUeberschrift.setFont(new Font("Verdana", 0, 36));
         jlUeberschrift.setHorizontalAlignment(JLabel.CENTER);
         jlUeberschrift.setText(resourceManager.getText("online.archive"));
         jpAuswahl.add(jlUeberschrift, BorderLayout.NORTH);
         jpAuswahl.add(jpScroll, BorderLayout.CENTER);
         jpAuswahl.add(jpBottom, BorderLayout.SOUTH);
 
         removeAll();
         add(jpAuswahl);
         validate();
         repaint();
     }
 
     @Override
     /**
      * Hintergrundbild wird skaliert gezeichnet
      * @author fwenisch
      * @date	04.11.2012
      */
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         Insets insets = getInsets();
         int x = insets.left;
         int y = insets.top;
         int width = getWidth() - x - insets.right;
         int height = getHeight() - y - insets.bottom;
         g.drawImage(backgroundimage, x, y, width, height, this);
 
     }
 
     public Spiel getCurrentSpiel() {
         return backend.getSpiel();
     }
 }
