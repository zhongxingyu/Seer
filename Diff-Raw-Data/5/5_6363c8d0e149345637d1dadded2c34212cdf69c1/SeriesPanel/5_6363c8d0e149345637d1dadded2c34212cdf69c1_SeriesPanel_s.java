 // %155607735:de.hattrickorganizer.gui.league%
 package ho.module.series;
 
 import ho.core.db.DBManager;
 import ho.core.gui.RefreshManager;
 import ho.core.gui.Refreshable;
 import ho.core.gui.comp.panel.ImagePanel;
 import ho.core.gui.theme.HOColorName;
 import ho.core.gui.theme.HOIconName;
 import ho.core.gui.theme.ThemeManager;
 import ho.core.model.HOVerwaltung;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.text.DateFormat;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JViewport;
 
 
 
 /**
  * Panel, das die Ligatabelle sowie das letzte und das nächste Spiel enthält
  */
 public class SeriesPanel extends ImagePanel implements Refreshable, ItemListener, ActionListener, MouseListener, KeyListener{
 	
 	private static final long serialVersionUID = -5179683183917344230L;
 	
     //~ Static fields/initializers -----------------------------------------------------------------
 
     private static Spielplan AKTUELLER_SPIELPLAN;
 
     private static String MARKIERTER_VEREIN;
 
     //~ Instance fields ----------------------------------------------------------------------------
 
     private JButton m_jbDrucken = new JButton(ThemeManager.getIcon(HOIconName.PRINTER));
     private JButton m_jbLoeschen = new JButton(ThemeManager.getIcon(HOIconName.REMOVE));
     
     private JComboBox m_jcbSaison;
     private SeriesTablePanel m_jpLigaTabelle;
     private MatchDayPanel[] matchDayPanels = new MatchDayPanel[14];
     private SeriesHistoryPanel m_jpTabellenverlaufStatistik;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new LigaTabellePanel object.
      */
     public SeriesPanel() {
         RefreshManager.instance().registerRefreshable(this);
         initComponents();
 
         fillSaisonCB();
     }
 
     static Spielplan getAktuellerSpielPlan() {
         return AKTUELLER_SPIELPLAN;
     }
 
     static String getMarkierterVerein() {
         return MARKIERTER_VEREIN;
     }
 
     @Override
 	public final void actionPerformed(ActionEvent e) {
         if (e.getSource().equals(m_jbLoeschen)) {
             if (m_jcbSaison.getSelectedItem() != null) {
                 final Spielplan spielplan = (Spielplan) m_jcbSaison.getSelectedItem();
                 final int value = JOptionPane.showConfirmDialog(this,
                                                                 HOVerwaltung.instance().getLanguageString("Ligatabelle")
                                                                 + " "
                                                                 + HOVerwaltung.instance().getLanguageString("loeschen")
                                                                 + ":\n" + spielplan.toString(), "",
                                                                 JOptionPane.YES_NO_OPTION);
 
                 if (value == JOptionPane.YES_OPTION) {
                     final String[] dbkey = {"Saison", "LigaID"};
                     final String[] dbvalue = {spielplan.getSaison() + "", spielplan.getLigaId()
                                              + ""};
 
                     DBManager.instance().deleteSpielplanTabelle(dbkey, dbvalue);
                     DBManager.instance().deletePaarungTabelle(dbkey, dbvalue);
                     AKTUELLER_SPIELPLAN = null;
 
                     RefreshManager.instance().doReInit();
                 }
             }
         } else if (e.getSource().equals(m_jbDrucken)) {
             final java.util.Calendar calendar = java.util.Calendar.getInstance();
             calendar.setTimeInMillis(System.currentTimeMillis());
 
             final String titel = HOVerwaltung.instance().getLanguageString("Ligatabelle")
                                  + " - "
                                  + HOVerwaltung.instance().getModel().getBasics().getTeamName()
                                  + " - "
                                  + DateFormat.getDateTimeInstance().format(calendar.getTime());
 
             final SeriesPrintPanelDialog printDialog = new SeriesPrintPanelDialog();
             printDialog.doPrint(titel);
             printDialog.setVisible(false);
             printDialog.dispose();
         }
     }
 
     //Listener--------------
     @Override
 	public final void itemStateChanged(ItemEvent e) {
         if (e.getStateChange() == ItemEvent.SELECTED) {
             //Aktuellen Spielplan bestimmen
             if (m_jcbSaison.getSelectedItem() instanceof Spielplan) {
                 AKTUELLER_SPIELPLAN = (Spielplan) m_jcbSaison.getSelectedItem();
             } else {
                 AKTUELLER_SPIELPLAN = null;
             }
 
             //Alle Panels informieren
             informSaisonChange();
         }
     }
 
     @Override
 	public final void keyPressed(KeyEvent e) {
         doEvent();
     }
 
     @Override
 	public final void keyReleased(KeyEvent e) {
         doEvent();
     }
 
     @Override
 	public void keyTyped(KeyEvent e) {
     }
 
     @Override
 	public final void mouseClicked(MouseEvent e) {
         doEvent();
     }
 
     @Override
 	public void mouseEntered(MouseEvent e) {
     }
 
     @Override
 	public void mouseExited(MouseEvent e) {
     }
 
     @Override
 	public void mousePressed(MouseEvent e) {
     }
 
     @Override
 	public final void mouseReleased(MouseEvent e) {
         doEvent();
     }
 
     private void doEvent(){
    	if (((MARKIERTER_VEREIN == null) && (m_jpLigaTabelle.getSelectedTeam() != null))
                 || !MARKIERTER_VEREIN.equals(m_jpLigaTabelle.getSelectedTeam())) {
                 MARKIERTER_VEREIN = m_jpLigaTabelle.getSelectedTeam();
                 markierungInfo();
             }
     }
     @Override
 	public final void reInit() {
         fillSaisonCB();
     }
 
     @Override
 	public void refresh() {
     }
 
     private void fillSaisonCB() {
         //Die Spielpläne als Objekte mit den Paarungen holen
         final Spielplan[] spielplaene = DBManager.instance().getAllSpielplaene(true);
 
         m_jcbSaison.removeItemListener(this);
 
         final Spielplan markierterPlan = (Spielplan) m_jcbSaison.getSelectedItem();
 
         //Alle alten Saisons entfernen
         m_jcbSaison.removeAllItems();
 
         //Neue füllen
         for (int i = 0; (spielplaene != null) && (i < spielplaene.length); i++) {
             m_jcbSaison.addItem(spielplaene[i]);
         }
 
         //Alte markierung wieder herstellen
         m_jcbSaison.setSelectedItem(markierterPlan);
 
         if ((m_jcbSaison.getSelectedIndex() < 0) && (m_jcbSaison.getItemCount() > 0)) {
             m_jcbSaison.setSelectedIndex(0);
         }
 
         m_jcbSaison.addItemListener(this);
 
         //Aktuellen Spielplan bestimmen
         if (m_jcbSaison.getSelectedItem() instanceof Spielplan) {
             AKTUELLER_SPIELPLAN = (Spielplan) m_jcbSaison.getSelectedItem();
         } else {
             AKTUELLER_SPIELPLAN = null;
         }
 
         //Alle Panels informieren
         informSaisonChange();
     }
 
     private void informSaisonChange() {
         m_jpLigaTabelle.changeSaison();
         m_jpTabellenverlaufStatistik.changeSaison();
         markierungInfo();
     }
 
     private void initComponents() {
         setLayout(new BorderLayout());
         
         //ComboBox für Saisonauswahl
         final JPanel panel = new ImagePanel(new BorderLayout());
 
         final JPanel toolbarPanel = new ImagePanel(null);
         m_jcbSaison = new JComboBox();
         m_jcbSaison.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Ligatabelle_Saisonauswahl"));
         m_jcbSaison.addItemListener(this);
         m_jcbSaison.setSize(200, 25);
         m_jcbSaison.setLocation(10, 5);
         toolbarPanel.add(m_jcbSaison);
 
         m_jbLoeschen.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Ligatabelle_SaisonLoeschen"));
         m_jbLoeschen.addActionListener(this);
         m_jbLoeschen.setSize(25, 25);
         m_jbLoeschen.setLocation(220, 5);
         m_jbLoeschen.setBackground(ThemeManager.getColor(HOColorName.BUTTON_BG));
         toolbarPanel.add(m_jbLoeschen);
 
         m_jbDrucken.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Ligatabelle_SaisonDrucken"));
         m_jbDrucken.addActionListener(this);
         m_jbDrucken.setSize(25, 25);
         m_jbDrucken.setLocation(255, 5);
         toolbarPanel.add(m_jbDrucken);
 
         toolbarPanel.setPreferredSize(new Dimension(240, 35));
         panel.add(toolbarPanel, BorderLayout.NORTH);
 
         final JPanel tablePanel = new ImagePanel(new BorderLayout());
         tablePanel.add(initLigaTabelle(), BorderLayout.NORTH);
 
         final JPanel historyPanel = new ImagePanel(new BorderLayout());
         historyPanel.add(initTabellenverlaufStatistik(), BorderLayout.NORTH);
         historyPanel.add(initSpielPlan(), BorderLayout.CENTER);
 
         tablePanel.add(historyPanel, BorderLayout.CENTER);
 
         panel.add(tablePanel, BorderLayout.CENTER);
 
         add(panel, BorderLayout.CENTER);
     }
 
     private Component initLigaTabelle() {
         m_jpLigaTabelle = new SeriesTablePanel();
         m_jpLigaTabelle.addMouseListener(this);
         m_jpLigaTabelle.addKeyListener(this);
 
         final JScrollPane scrollpane = new JScrollPane(m_jpLigaTabelle);
         scrollpane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
         scrollpane.setPreferredSize(new Dimension((int) m_jpLigaTabelle.getPreferredSize().getWidth(),
                                                   (int) m_jpLigaTabelle.getPreferredSize()
                                                                        .getHeight() + 22));
 
         return scrollpane;
     }
 
     private Component initSpielPlan() {
         JLabel label = null;
         for (int i = 0; i < matchDayPanels.length; i++) {
         	matchDayPanels[i] = new MatchDayPanel(i+1);
 		}
 
         final GridBagLayout layout = new GridBagLayout();
         final GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.NONE;
         constraints.weightx = 1.0;
         constraints.weighty = 1.0;
         constraints.gridy = 0;
         constraints.insets = new Insets(4, 4, 4, 4);
 
         final JPanel panel = new ImagePanel(layout);
 
         label = new JLabel();
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.gridheight = 7;
         constraints.fill = GridBagConstraints.HORIZONTAL;
         layout.setConstraints(label, constraints);
         panel.add(label);
 
         for (int i = 0; i < 7; i++) {
         	constraints.gridx = 1;
             constraints.gridy = i;
             constraints.gridheight = 1;
             layout.setConstraints(matchDayPanels[i], constraints);
             panel.add(matchDayPanels[i]);
 		}
 
 
         label = new JLabel();
         constraints.gridx = 2;
         constraints.gridy = 0;
         constraints.gridheight = 7;
         constraints.fill = GridBagConstraints.HORIZONTAL;
         layout.setConstraints(label, constraints);
         panel.add(label);
 
         for (int i = 7; i < matchDayPanels.length; i++) {
         	constraints.gridx = 3;
             constraints.gridy = i-7;
             constraints.gridheight = 1;
             layout.setConstraints(matchDayPanels[i], constraints);
             panel.add(matchDayPanels[i]);
 		}
 
         label = new JLabel();
         constraints.gridx = 4;
         constraints.gridy = 0;
         constraints.gridheight = 7;
         constraints.fill = GridBagConstraints.HORIZONTAL;
         layout.setConstraints(label, constraints);
         panel.add(label);
 
         final JScrollPane scrollpane = new JScrollPane(panel);
         scrollpane.getVerticalScrollBar().setBlockIncrement(100);
         scrollpane.getVerticalScrollBar().setUnitIncrement(20);
         scrollpane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
         return scrollpane;
     }
 
     private Component initTabellenverlaufStatistik() {
         m_jpTabellenverlaufStatistik = new SeriesHistoryPanel();
 
         final JPanel panel = new ImagePanel();
         panel.add(m_jpTabellenverlaufStatistik);
 
         final JScrollPane scrollpane = new JScrollPane(panel);
         scrollpane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
         scrollpane.setPreferredSize(new Dimension((int) m_jpLigaTabelle.getPreferredSize().getWidth(),
                                                   (int) m_jpLigaTabelle.getPreferredSize()
                                                                        .getHeight()));
 
         return scrollpane;
     }
 
     private void markierungInfo() {
     	for (int i = 0; i < matchDayPanels.length; i++) {
     		matchDayPanels[i].changeSaison();
 		}
     }
 }
