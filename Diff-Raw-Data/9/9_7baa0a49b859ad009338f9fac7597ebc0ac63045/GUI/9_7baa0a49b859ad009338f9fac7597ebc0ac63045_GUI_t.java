 package GUI;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.ScrollPane;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.*;
 
 import servicesCombined.CombinedServicesInteressent;
 import servicesCombined.CombinedServicesVeranstalter;
 
 import xmppService.*;
  
 public class GUI 
 {
 	static CombinedServicesVeranstalter csv = new CombinedServicesVeranstalter();
 	static CombinedServicesInteressent csi = new CombinedServicesInteressent();
 	
 	static int sportgruppenIndex = 0;
 	static int sportartenIndex = 0;
 	static int veranstaltungenIndex = 0;
 	
 	
 	static ActionListener DropDownSAListen = new ActionListener()
 	{
 		public void actionPerformed(ActionEvent dropdownSAe) 
 		{	
 			btnUnsubscribeSA.setVisible(true);
 			btnSubscribeSA.setVisible(true);
     		String info;        			
 			sportartenIndex = dropdownSA.getSelectedIndex();
 			
 			info = csi.getSportart(String.valueOf(sportgruppenIndex),
 					String.valueOf(sportartenIndex));
 			AreaSG.setText(info);
 			
 			showDropdownV();
 
 
 			if(csi.isSubscribed(String.valueOf(sportgruppenIndex)+String.valueOf(sportartenIndex)
 					+ "Sportart")){
 				btnSubscribeSA.setEnabled(false);
 				btnUnsubscribeSA.setEnabled(true);
 				 	//btnSubscribeSA.setToolTipText("Node wurde bereits subscribed.");
 			}
 			else{
 				btnSubscribeSA.setEnabled(true);
 				btnUnsubscribeSA.setEnabled(false);
 				//btnUnsubscribeSA.setToolTipText("Klicken Sie hier zum unsubscriben.");
 			}    				
			dropdownV.removeActionListener(DropDownVListen);
 			dropdownV.removeAllItems();
 			showDropdownV();
 			
 			}
 		};
 	
 	static ActionListener DropDownVListen = new ActionListener(){
 		
 		public void actionPerformed(ActionEvent dropdownVe)
 		{
 			String info;        			
 			veranstaltungenIndex = dropdownV.getSelectedIndex();
			info = csi.getVeranstaltung((String.valueOf(sportgruppenIndex)), 
					(String.valueOf(sportartenIndex)), String.valueOf(veranstaltungenIndex));
 			AreaSG.setText(info);
 
 			
 			
 			if(csi.isSubscribed(String.valueOf(sportgruppenIndex)+String.valueOf(sportartenIndex)
 					+String.valueOf(veranstaltungenIndex)+("Veranstaltung"))){
 				btnSubscribeSA.setEnabled(false);
 				btnUnsubscribeSA.setEnabled(true);
 				//btnSubscribeSA.setToolTipText("Node wurde bereits subscribed.");
 			}
 			else{
 			btnSubscribeSA.setEnabled(true);
 			btnUnsubscribeSA.setEnabled(false);
 			//btnUnsubscribeSA.setToolTipText("Klicken Sie hier zum unsubscriben.");
 			}    				
 			
 			}
 	};
 	
 	/********************************************/
 	/**************Interessent*******************/
 	/********************************************/
 	private static JLabel labelSG, labelSA, labelV, labelO, labelAreaSG, 
 		labelAreaV, labelAreaO, labelVS, labelSubList;
 	
 	private static JComboBox dropdownSG, dropdownSA, dropdownV, dropdownVS, 
 		dropdownO;		
 	
 	private static JTextArea AreaSG, AreaV, AreaO;
 	
 	private static JButton btnSubscribeSA, btnUnsubscribeSA,
 		btnUnsubscribeV, btnSubscribeV, btnzurueckSG, btnzurueckVS, btnzurueckO, btnEditOK;  
 	
 	
 	/********************************************/
 	/**************Veranstalter******************/
 	/********************************************/	
 	private static JButton btnDeleteV, btnEditV, btnSubscribeE, btnSubscribeG, 
 		btnUnsubscribeE, btnUnsubscribeG, btnZurueckVV, btnNewV, btnNewOK, 
 		btnAbortEdit, btnAbortNew;
 	
 	private static JLabel labelVSG, labelAreaVV, labelAreaVSG, labelAreaVSA, 
 		labelVSA, labelVV, labelVEBeschr, labelVEInfo, labelVEDatum, 
 		labelVEUhrzeit, labelVENiveau, labelVEVorraussetungen, labelVEGebude,
 		labelAreaAllgSGV, labelAreaAllgVV, 
 		labelAreaAllgV, labelAreaAllgSA, labelAreaAllgVS, labelAreaAllgO;		
 	private static JComboBox dropdownVSG, dropdownVSA, dropdownVV, dropdownDayE, 
 		dropdownMonthE, dropdownYearE, dropdownHourE, dropdownMinuteE, 
 		dropdownGebudeE, 
 		dropdownDayAE, dropdownMonthAE, dropdownYearAE, dropdownHourAE, 
 		dropdownMinuteAE, dropdownGebudeAE;
 	private static JTextArea AreaVV, AreaVSG, AreaVSA, AreaInfoE, AreInfoAE, 
 		AreaAllgemeinPanelV, AreaAllgemeinSG, AreaAllgemeinVS,AreaAllgemeinO,
 		AreaAllgemeinSA, AreaAllgemeinV;
 	
 	private static JTextField fieldBeschrE, fieldNiveauE, fieldBeschrAE,
 		fieldVorraussetzungenE, fieldNiveauAE, fieldVorraussetzungenAE;	
 
 	private static JScrollPane scrollpaneAreaInfoE, scrollpaneAreaAllg, 
 		scrollpaneAreaInfoAE, scrollpaneAreaAllgSG, scrollpaneAreaAllgVS, 
 		scrollpaneAreaAllgO, scrollpaneAreaAllgSA, scrollpaneAreaAllgV;
 	
 	/********************************************/
 	/**************KlickCounter******************/
 	/********************************************/
 	static int countVV = 0;
 	static int countNew = 0;
 	static int countSA = 0;
 		
 	final static JFrame fenster1 = new JFrame("Herzlich Willkommen");
 	final static JFrame fenster2 = new JFrame("Herzlich Willkommen");
 	final static JTabbedPane tabLeiste = new JTabbedPane();
 	final static JTabbedPane tabLeiste2 = new JTabbedPane();
 		
     static JPanel PanelMain = new JPanel ();
 	static JPanel panelSG = new JPanel();
 	static JPanel panelVS = new JPanel();
 	static JPanel panelO = new JPanel();
 	static JPanel panelE = new JPanel();
 	static JPanel panelGB = new JPanel();
 	static JPanel panelVV = new JPanel();
 		
     public static void main(String[] args) {
                 
     	/********************************************/
     	/*****************Windows********************/
     	/********************************************/
     	fenster1.setSize(1000, 600);
     	fenster1.setResizable(false);
         fenster1.setLocationRelativeTo(null);
         fenster1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         fenster1.add(tabLeiste);
                 
         fenster2.setSize(1000, 600);
         fenster2.setResizable(false);
         fenster2.setLocationRelativeTo(null);
         fenster2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         fenster2.add(tabLeiste2);
         
         
         /********************************************/
     	/**************OptionPane********************/
     	/*********dient der Rollenabfrage************/ 
         /********************************************/
 
         Icon icon = new ImageIcon( "/Ausarbeitungen/icon.png" );
         Object[] options = {"Interessent", "Veranstalter"};
                
         int selected = JOptionPane.showOptionDialog(
         null, "Welche Rolle haben Sie?", "Alternativen", JOptionPane.DEFAULT_OPTION, 
         JOptionPane.INFORMATION_MESSAGE, icon, options, options[0]);
 
         	//Also wurde Interessent gewhlt!
         	if(selected == 0){
         		try {
         			//Verbindung als Interessent aufbauen
 					csi.initialize();
 				} catch (InterruptedException e) {
 					System.out.println("Fehler beim Verbinden!");
 					e.printStackTrace();
 				}
 	            showPanelsI();
 	            showDropdownSG();
 	            showAreaO();
 	            showAreaVS();
 	            showButtonZurueckO();
 	            showLogoutSG();
 	            showButtonZurueckVS();
 	            showAreaAllgemeinSG();	
 	            showSubscriptionsList();
 	            showAreaAllgemeinVS();
 	            showAreaAllgemeinO(); 
 	            createDropdownSA();
 	            createDropdownV();
 	            fenster1.setVisible(true);
 	            fenster2.setVisible(false);
              } 
 
         	
         	//Veranstalter wurde gewhlt
              if(selected == 1){
                	showPanelsV();
                	showDropdownVSG();
                	showBtnLogoutVV();
 	            showAreaAllgPanelV();
 	            createDropdownVS();
 	            createDropdownVSA();
 	            createDropdownVV();
                 fenster2.setVisible(true);
                 fenster1.setVisible(false);
              }
             
             //Zwecks Debbuging ist das jetzt hier! 
             labelAreaVSG = new JLabel("Informationen bezglich: ");
      		labelAreaVSG.setBounds(600, 10, 300, 100);
      		labelAreaVSG.setVisible(false);
             panelVV.add(labelAreaVSG);
          	panelVV.validate();
          	
 
 
 
             AreaVSG = new JTextArea();
             AreaVSG.setLineWrap(true);
             AreaVSG.setBounds(600, 90, 350, 300);
       		AreaVSG.setVisible(false);
 
      		panelVV.add(AreaVSG);
          	panelVV.validate();
       
 			showAreaSG();
 			showAreaVSG();
 			
 			btnSubscribeSA = new JButton("Subscribe");
         	btnSubscribeSA.setBounds(600, 400, 150, 25);
         	panelSG.add(btnSubscribeSA);
         	btnSubscribeSA.setVisible(false);
         	
         	panelSG.validate();
         	panelSG.repaint();
         	
         	btnUnsubscribeSA = new JButton("Unsubscribe");
         	btnUnsubscribeSA.setBounds(800, 400, 150, 25);
         	panelSG.add(btnUnsubscribeSA);
         	btnUnsubscribeSA.setVisible(false);
         	panelSG.validate();
         	panelSG.repaint();
         	
         	
     }	
 			
 	
     
     /*Ntzliche Schnipsel
      * 
      * Eine ComboBox (Dropdown) mit einem Array fllen
      * DefaultComboBoxModel model = new DefaultComboBoxModel( yourStringArray );
 		comboBox.setModel( model );
      * 
      * removeAllItems() - Lscht alle Items der Combobox
      * addItem() - Fgt eins hinzu
      * 
      * 
      * */
     
     /**************************PANELS********************
      *Die einzelnen Tabs werden dem Panel hinzugefgt
      *Layouter wird gesetzt (null-Layout)
      *
      ****************************************************************/
     public static void showPanelsI(){
     		
 	    	
 	        
     		PanelMain.setLayout(null);
     		
 	        panelSG.setLayout(null);	
 	        tabLeiste.addTab("Sportgruppen", panelSG);
 	            
 	        
 	        panelVS.setLayout(null);
 	        tabLeiste.addTab("Veranstalter", panelVS);
 	
 	        panelO.setLayout(null);
 	        tabLeiste.addTab("Orte", panelO);
     	}
     public static void showPanelsV(){
     		
 	    	/***************************************************************/
 	    	/**************************PANEL********************************/
 	    	/***************************************************************/	
 	        
     		panelVV.setLayout(null);
 	        tabLeiste2.addTab("Sportveranstaltungen", panelVV);
     		
 	        panelE.setLayout(null);	
 	        tabLeiste2.addTab("Equiment", panelE);
 	            
 	        
 	        panelGB.setLayout(null);
 	        tabLeiste2.addTab("Gebude", panelGB);
 	
 	        
     	}
     
     /**************************DropdownSG********************
      *Das Dropdown mit den Sportgruppen wird angezeigt.
      *Hierzu wird gleichzeitig das Label erstellt.
      *Sobald eine SG gewhlt wurde, erscheint das DropDownSA sowie eine Area.
      *TODONE: In den String DropdownSG mssen die richtigen Sportgruppen rein
      *TODONE: AreaSG muss befllt werden
      * @throws InterruptedException 
      ****************************************************************/
     public static void showDropdownSG()
     	{
 	    		List<String> sportgruppenListe = csi.getSportgruppen();
 	    		
 	            labelSG = new JLabel("Bitte whlen Sie eine Sportgruppe!");
 	            labelSG.setBounds(10, 50, 300, 25);
 	            panelSG.add(labelSG);
 	            
 	            final String[] DropDownSG = sportgruppenListe.toArray(new String[sportgruppenListe.size()]);
 	        	dropdownSG = new JComboBox(DropDownSG);
 	        	dropdownSG.setBounds(10, 70, 200, 25);
 	        	panelSG.add(dropdownSG);
 	        	
 
 	        	dropdownSG.addActionListener(new ActionListener()
 	        	{
 	        		public void actionPerformed(ActionEvent dropdownSGe) 
 	        		{ 	
 	        			sportgruppenIndex = dropdownSG.getSelectedIndex();
 	        			String info = csi.getSportgruppe(String.valueOf(sportgruppenIndex));
 	        			AreaSG.setText(info);
 	        			dropdownSA.removeActionListener(DropDownSAListen);
 	        			dropdownSA.setVisible(false);
  	        			dropdownSA.removeAllItems();
  	        			if(btnUnsubscribeSA.isVisible()){
  	 	        			btnUnsubscribeSA.setVisible(false);
  	 	        			btnSubscribeSA.setVisible(false);
  	        			}
  	        			
 	        			showDropdownSA();
 	        		}
 	        	});
             
     	}
 
     /**************************DropdownSA********************
      *Das Dropdown mit den Sportarten wird angezeigt.
      *Hierzu wird gleichzeitig das Label erstellt.
      *Sobald eine SA gewhlt wurde, erscheint das DropDownV sowie eine Area.
      *TODO: In den String DropdownSA mssen die richtigen Sportarten rein
      *TODO: AreaSA muss befllt werden
      *****************************************************************/	
    public static void createDropdownSA(){
 	   
 	   labelSA = new JLabel("Bitte whlen Sie eine Sportart!");
 	   labelSA.setBounds(10, 100, 300, 25);
 	   labelSA.setVisible(false);
 	   panelSG.add(labelSA);
 	   panelSG.validate();
 	   panelSG.repaint();
 		
 		final String[] DropDownSA = {""};
 		dropdownSA = new JComboBox(DropDownSA);
 		dropdownSA.setBounds(10, 120, 200, 25);
 		dropdownSA.setVisible(false);
 		panelSG.add(dropdownSA);	
 		panelSG.validate();
 		
    }
    
     /** Buggs
      * 
      */
     public static void showDropdownSA(){
     		
     	List<String> sportartenListe = csi.getSportarten(String.valueOf(sportgruppenIndex));
     	labelSA.setVisible(true);
     	
     	for(String sportarten:sportartenListe){
         	dropdownSA.addItem(sportarten);
     	}
     	dropdownSA.setVisible(true);
     	dropdownSA.addActionListener(DropDownSAListen);
     }
     
 
     /**************************DropdownV********************
      * Das Dropdown mit den Veranstaltungen wird angezeigt.
      * Hierzu wird gleichzeitig das Label erstellt.
      * Sobald eine V gewhlt wurde, erscheinen die Subscribe und Unsubrice Button sowie eine Area.
      * TODO: In den String DropdownV mssen die richtigen Veranstaltungen rein
      * TODO: AreaV muss befllt werden
      * ****************************************************************/   	
     public static void createDropdownV(){
     	
     	labelV = new JLabel("Bitte whlen Sie eine Veranstaltung");
 		labelV.setBounds(10, 150, 300, 25);
 		labelV.setVisible(false);
 		panelSG.add(labelV);
 		panelSG.validate();
 		
        final String[] DropDownV = {""};
     	
         dropdownV = new JComboBox(DropDownV);
     	dropdownV.setBounds(10, 170, 200, 25);
     	dropdownV.setVisible(false);
     	panelSG.add(dropdownV);
     	panelSG.validate();
     }
     
     public static void showDropdownV(){
     	
     	List<String> veranstaltungenListe = csi.getVeranstaltungen(String.valueOf(sportgruppenIndex), String.valueOf(sportartenIndex));
     	labelV.setVisible(true);
     	
     	for(String veranstaltungen:veranstaltungenListe){
         	dropdownV.addItem(veranstaltungen);
     	}
     	dropdownV.setVisible(true);
     	dropdownV.addActionListener(DropDownVListen);
     	
     	}
 
     	
     /**************************DropdownVS********************
      * Das Dropdown mit den Veranstaltern wird angezeigt.
      * Hierzu wird gleichzeitig das Label erstellt.
      * Sobald ein VS gewhlt wurde, erscheinen die Subscribe und Unsubrice Button sowie eine Area.
      * TODO: In den String DropdownVS mssen die richtigen Veranstalter rein
      * TODO: AreaVS muss befllt werden
      * ****************************************************************/ 
    public static void createDropdownVS(){
 		labelVS = new JLabel("Bitte whlen Sie eine(n) Veranstalter/in!");
 		labelVS.setBounds(10, 10, 300, 100);
 		labelVS.setVisible(false);
 		panelVS.add(labelVS);      
 		panelVS.validate();
        
 	   	final String[] DropDownVS = new String[] {"Veranstalter", "David", "Laura", "Super", "Mentor"};
 	   	dropdownVS = new JComboBox(DropDownVS);
 	   	dropdownVS.setBounds(10, 70, 200, 25);
 	   	dropdownVS.setVisible(false);
 	   	panelVS.add(dropdownVS);
 	   	panelVS.validate();
    }
     
     public static void showDropdownVS(){
         	
     		labelVS.setVisible(true);
     		dropdownVS.setVisible(true);
     		
         	
         	
     	}
 
     /**************************DropdownO********************
      * Das Dropdown mit den Orten wird angezeigt.
      * Hierzu wird gleichzeitig das Label erstellt.
      * Sobald ein O gewhlt wurde, erscheinen die Subscribe und Unsubrice Button sowie eine Area.
      * TODO: In den String DropdownO mssen die richtigen Ort rein
      * TODO: AreaO muss befllt werden
      * ****************************************************************/ 
     public static void createDropdownO(){
     	
     	labelO = new JLabel("Bitte whlen Sie einen Ort");
         labelO.setBounds(10, 10, 300, 100);
         labelO.setVisible(false);
         panelO.add(labelO);
         labelO.validate();
 		
 		final String[] DropDownO = new String[] {"Orte", "Bernberg", "Hesselbach", "Niedersemar", "Wiehl"};
     	dropdownO = new JComboBox(DropDownO);
     	dropdownO.setVisible(false);
     	dropdownO.setBounds(10, 70, 200, 25);
     	panelO.add(dropdownO);
     	panelO.validate();
     }
     
     public static void showDropdownO(){
     		
         labelO.setVisible(true);
     	dropdownO.setVisible(true);
 
     		
         	dropdownO.addActionListener(new ActionListener()
         	{
         		public void actionPerformed(ActionEvent dropdownOe) 
         		{ 			
         			//TODO: komischer Typ
         			showButtonZurueckO();
         		}
         	});
     	}
 
     /**************************Logout**********************************
      * Die Logout Button werden erstellt. 
      * Mittels ActionListenern wird erneut die OptionPane eingeblendet
      * ****************************************************************/ 
     public static void showLogoutSG(){
     		
     		btnzurueckSG = new JButton ("Logout");
         	btnzurueckSG.setBounds(800, 500, 150, 25);
         	btnzurueckSG.setToolTipText("Zurck zur Rollenauswahl");
         	panelSG.add(btnzurueckSG);
         	panelSG.validate();
 
         	
         	
         	btnzurueckSG.addActionListener(new ActionListener() {
 				
 				
             	public void actionPerformed(ActionEvent btnzuruecke) {
 				            	
             		fenster1.dispose();
                 	
                 	Object[] options = {"Interessent", "Veranstalter"};
                     
                     int selected = JOptionPane.showOptionDialog(null,
                       "Welche Rolle haben Sie?",
                       "Alternativen",
                       JOptionPane.DEFAULT_OPTION, 
                       JOptionPane.INFORMATION_MESSAGE, 
                       null, options, options[0]);
     					
                     if(selected == 0)
                     {
         	            showPanelsI();
         	            showDropdownSG();
         	            showDropdownO();
         	            showAreaO();
         	            showAreaVS();
         	            showButtonZurueckO();
         	            showLogoutSG();
         	            showButtonZurueckVS();
         	            showAreaAllgemeinSG();	
         	            showAreaAllgemeinVS();
         	            showAreaAllgemeinO();
 
         	             
         	            fenster1.setVisible(true);
         	            fenster2.setVisible(false);
                      } 
 
                      if(selected == 1)
                      {
                        	showPanelsV();
                        	showDropdownVSG();
                        	showBtnLogoutVV();
         	            showAreaAllgPanelV();
 
                         	
                         fenster2.setVisible(true);
                         fenster1.setVisible(false);
                      }
             	}
     			});
         	}
  	
     public static void showButtonZurueckVS(){
     		
     		btnzurueckVS = new JButton ("Logout");
         	btnzurueckVS.setBounds(800, 500, 150, 25);
         	btnzurueckVS.setToolTipText("Zurck zur Rollenauswahl");
         	panelVS.add(btnzurueckVS);
         	panelVS.validate();
 
         	
         	
         	btnzurueckVS.addActionListener(new ActionListener() {
 				
 				
             	public void actionPerformed(ActionEvent btnzurueckVe) {
 				
             	fenster1.dispose();
             	
             	Object[] options = {"Interessent", "Veranstalter"};
                 
                 int selected = JOptionPane.showOptionDialog(null,
                   "Welche Rolle haben Sie?",
                   "Alternativen",
                   JOptionPane.DEFAULT_OPTION, 
                   JOptionPane.INFORMATION_MESSAGE, 
                   null, options, options[0]);
 					
                 if(selected == 0)
                 {
     	            showPanelsI();
     	            showDropdownSG();
     	            showDropdownVS();
     	            showDropdownO();
     	            showAreaO();
     	            showAreaVS();
     	            showButtonZurueckO();
     	            showLogoutSG();
     	            showButtonZurueckVS();
     	            showAreaAllgemeinSG();	
     	            showAreaAllgemeinVS();
     	            showAreaAllgemeinO();
     	            
 
     	             
     	            fenster1.setVisible(true);
     	            fenster2.setVisible(false);
                  } 
 
                  if(selected == 1)
                  {
                    	showPanelsV();
                    	showDropdownVSG();
                    	showBtnLogoutVV();
     	            showAreaAllgPanelV();
 
                     	
                     fenster2.setVisible(true);
                     fenster1.setVisible(false);
                  }
                 
 				}
 			});
     	}
 
     public static void showButtonZurueckO(){
     		
     		btnzurueckO = new JButton ("Logout");
         	btnzurueckO.setBounds(800, 500, 150, 25);
         	btnzurueckO.setToolTipText("Zurck zur Rollenauswahl");
         	panelO.add(btnzurueckO);
         	panelO.validate();
 
         	
         	
         	btnzurueckO.addActionListener(new ActionListener() {
 				
 				
             	public void actionPerformed(ActionEvent btnzurueckOe) {
 				
             		fenster1.dispose();
                 	
                 	Object[] options = {"Interessent", "Veranstalter"};
                     
                     int selected = JOptionPane.showOptionDialog(null,
                       "Welche Rolle haben Sie?",
                       "Alternativen",
                       JOptionPane.DEFAULT_OPTION, 
                       JOptionPane.INFORMATION_MESSAGE, 
                       null, options, options[0]);
     					
                     if(selected == 0)
                     {
         	            showPanelsI();
         	            showDropdownSG();
         	            showDropdownVS();
         	            showDropdownO();
         	            showAreaO();
         	            showAreaVS();
         	            showButtonZurueckO();
         	            showLogoutSG();
         	            showButtonZurueckVS();
         	            showAreaAllgemeinSG();	
         	            showAreaAllgemeinVS();
         	            showAreaAllgemeinO();
 
         	             
         	            fenster1.setVisible(true);
         	            fenster2.setVisible(false);
                      } 
 
                      if(selected == 1)
                      {
                        	showPanelsV();
                        	showDropdownVSG();
                        	showBtnLogoutVV();
         	            showAreaAllgPanelV();
 
                         	
                         fenster2.setVisible(true);
                         fenster1.setVisible(false);
                      }
                     
     				}
     			});
         	}
 
     /***************Areas zur Darstellung der Werte in der XML***********
      * 
      * Die Areas (rechts in der GUI) werden erstellt.
      * AreaSG, AreaVS und AreaO
      * 
      * Eventuell noch setEditable(false) setzen? 
      * 
      * TODO: Areas mit Inhalt befllen. 
      * Das macht man mit AreaXYZ.setText(t);
      *
      * ****************************************************************/
     public static void showAreaSG(){
     		
 
     		labelAreaSG = new JLabel("Informationen bezglich: ");
             labelAreaSG.setBounds(600, 10, 300, 100);
             panelSG.add(labelAreaSG);
         	panelSG.validate();
         	panelSG.repaint();
 
             AreaSG = new JTextArea();
             AreaSG.setLineWrap(true);
     		AreaSG.setBounds(600, 90, 350, 300);
     		panelSG.add(AreaSG);
         	panelSG.validate();
             
     	}
     	
     public static void showAreaVS(){
     	
             
             labelAreaV = new JLabel("Informationen bezglich: ");
             labelAreaV.setBounds(600, 10, 300, 100);
             panelVS.add(labelAreaV);
             panelVS.validate();
             
 
             AreaV = new JTextArea();
             AreaV.setLineWrap(true);
     		AreaV.setBounds(600, 90, 350, 300);
     		panelVS.add(AreaV);
     		panelVS.validate();
     		
     		
     	}
     	
     public static void showAreaO(){
     	
             
             labelAreaO = new JLabel("Informationen bezglich: ");
             labelAreaO.setBounds(600, 10, 300, 100);
             panelO.add(labelAreaO);
         	panelO.validate();
 
             
 
             AreaO = new JTextArea();
             AreaO.setLineWrap(true);
     		AreaO.setBounds(600, 90, 350, 300);
     		panelO.add(AreaO);
         	panelO.validate();
 
             
     		
     	}
     	
     
     public static void showSubscriptionsList(){
     	
     	labelSubList = new JLabel("Your Subscriptions:");
     	labelSubList.setBounds(350, 300, 300, 100);
         panelSG.add(labelSubList);
     	panelSG.validate();
     	panelSG.repaint();
     	
         String SubsribedItems[] = {"Politik", "Autos", "Mode", 
             "Film- und Fernsehen", "Computer", "Tiere", "Sport"};
         JList Subscriptions = new JList(SubsribedItems);
         Subscriptions.setBounds(350, 370, 200, 150);
         panelSG.add(Subscriptions);
     	
     }
 
     /***************Areas zur Darstellung der Mitteilung***********
      * 
      * Die Areas (unten in der GUI) werden erstellt.
      * Fr jeden Tab eine also SG, VS und O
      * 
      * Eventuell noch setEditable(false) setzen? 
      * 
      * TODO: Mitteilungen
      *
      * ****************************************************************/
     public static void showAreaAllgemeinSG(){
     		
     		labelAreaAllgSGV = new JLabel("Meldungen:");
     		labelAreaAllgSGV.setBounds(10, 300, 300, 100);
             panelSG.add(labelAreaAllgSGV);
         	panelSG.validate();
         	panelSG.repaint();
     		
     		AreaAllgemeinSG = new JTextArea(7, 20);
     		AreaAllgemeinSG.setText("");
     		AreaAllgemeinSG.setLineWrap(true);
     		AreaAllgemeinSG.setWrapStyleWord(true);
             scrollpaneAreaAllgSG = new JScrollPane(AreaAllgemeinSG); 
          	scrollpaneAreaAllgSG.setBounds(10, 370, 300, 150);
             panelSG.add(scrollpaneAreaAllgSG);
 
     		
     	}
     	
     public static void showAreaAllgemeinVS(){
     		
     		labelAreaAllgVS = new JLabel("Meldungen:");
     		labelAreaAllgVS.setBounds(10, 300, 300, 100);
             panelVS.add(labelAreaAllgVS);
         	panelVS.validate();
         	panelVS.repaint();
     		
     		AreaAllgemeinVS = new JTextArea(7, 20);
     		AreaAllgemeinVS.setText("");
     		AreaAllgemeinVS.setLineWrap(true);
     		AreaAllgemeinVS.setWrapStyleWord(true);
             scrollpaneAreaAllgVS = new JScrollPane(AreaAllgemeinVS); 
          	scrollpaneAreaAllgVS.setBounds(10, 370, 400, 150);
             panelVS.add(scrollpaneAreaAllgVS);
 
     		
     	}
     	
     public static void showAreaAllgemeinO(){
     		
     		labelAreaAllgO = new JLabel("Meldungen:");
     		labelAreaAllgO.setBounds(10, 300, 300, 100);
             panelO.add(labelAreaAllgO);
         	panelO.validate();
         	panelO.repaint();
     		
     		AreaAllgemeinO = new JTextArea(7, 20);
     		AreaAllgemeinO.setText("");
     		AreaAllgemeinO.setLineWrap(true);
     		AreaAllgemeinO.setWrapStyleWord(true);
             scrollpaneAreaAllgO = new JScrollPane(AreaAllgemeinO); 
          	scrollpaneAreaAllgO.setBounds(10, 370, 400, 150);
             panelO.add(scrollpaneAreaAllgO);
 
     		
     	}
     
     
     
     /********************************************/
 	/**********Methoden Veranstalter*************/
     /********************************************/
     
     
     
     public static void showBtnLogoutVV(){
     		
     	btnZurueckVV = new JButton ("Logout");
        	btnZurueckVV.setBounds(800, 500, 150, 25);
        	panelVV.add(btnZurueckVV);
        	panelVV.validate();
        	
        	btnZurueckVV.addActionListener(new ActionListener() {
 				
 		public void actionPerformed(ActionEvent btnZurueckVVe) {
 				
 			Object[] options = {"Interessent", "Veranstalter"};
                     
 			int selected = JOptionPane.showOptionDialog(null,
                       "Welche Rolle haben Sie?",
                       "Alternativen",
                       JOptionPane.DEFAULT_OPTION, 
                       JOptionPane.INFORMATION_MESSAGE, 
                       null, options, options[0]);
     					
                     if(selected == 0)
                     {
         	            showPanelsI();
         	            showDropdownSG();
         	            showDropdownVS();
         	            showDropdownO();
         	            showAreaO();
         	            showAreaVS();
         	            showButtonZurueckO();
         	            showLogoutSG();
         	            showButtonZurueckVS();
         	            showAreaAllgemeinSG();	
         	            showAreaAllgemeinVS();
         	            showAreaAllgemeinO();
 
         	             
         	            fenster1.setVisible(true);
         	            fenster2.setVisible(false);
                      } 
 
                      if(selected == 1){
                        	showPanelsV();
                        	showDropdownVSG();
                        	showBtnLogoutVV();
         	            showAreaAllgPanelV();
 
                         	
                         fenster2.setVisible(true);
                         fenster1.setVisible(false);
                      }    	
             	}
 			});
     	}
     	
    
     
     /***************Button Besttigen neue Veranstaltung***********
      * 
      * Der Button wird erstellt und positioniert.
      *
      * ****************************************************************/	
     public static void showButtonNewOK(){
     		
     		btnNewOK = new JButton("OK");
     		btnNewOK.setBounds(800, 450, 150, 25);
     		panelVV.add(btnNewOK);
     		panelVV.validate();
         	panelVV.repaint();
         	
         	
         	btnNewOK.addActionListener(new ActionListener() {
 				
 				public void actionPerformed(ActionEvent BtnOKe) {
 					
 					hideFieldsNewV();
 					hideLabelsFields();
 					showAreaVSG();
 					btnAbortNew.setVisible(false);
 					/*TODO: Publishen - Ok Button bernimmt eingegebenen Werte und macht nen Post*/
 					
 				}
 			});
     	}
 
     
     /***************Button neue V abbrechen***********
      * Das Erstellen der vVeranstaltung wird abgebrochen
      *
      * ****************************************************************/
     public static void showButtonNewAbort(){
     		
     		btnAbortNew = new JButton("Abbrechen");
     		btnAbortNew.setBounds(650, 450, 150, 25);
     		panelVV.add(btnAbortNew);
     		panelVV.validate();
         	panelVV.repaint();
         	
         	
         	btnAbortNew.addActionListener(new ActionListener() {
 				
 				public void actionPerformed(ActionEvent BtnNewAborte) {
 					
 					hideFieldsNewV();
 					hideLabelsFields();
 					showAreaVSG();
 					btnAbortNew.setVisible(false);
 					
 					
 				}
 			});
     	}
     	
     
     /***************Button neue Veranstaltung***********
      * 
      * Der Button wird erstellt und positioniert.
      *Sobald er gedrpckt wird werden die Fields geladen
      * ****************************************************************/
     public static void showButtonNewV(){
     		
         	btnNewV = new JButton("Neue Veranstaltung");
         	btnNewV.setBounds(220, 120, 150, 25);
         	panelVV.add(btnNewV);
         	panelVV.validate();
         	panelVV.repaint();
         	
         	btnNewV.addActionListener(new ActionListener() {
 				
 				
 				public void actionPerformed(ActionEvent NewVe) {
 					
 					countNew++;
 					showTextfieldsVErstellen();
 					hideDropDownVS();
 					showButtonNewOK();
 					showButtonNewAbort();
 					hideAreaVSG();
 					
 				}
 			});
 
         	
     	}
 
     
     /***************Area zur Darstellung der Werte in der XML***********
      * 
      * Die Areas (rechts in der GUI) werden erstellt.
      * AreaVSG
      * 
      * Eventuell noch setEditable(false) setzen? 
      * 
      * TODO: Areas mit Inhalt befllen. 
      * Das macht man mit AreaXYZ.setText(t);
      *
      * ****************************************************************/
     public static void showAreaVSG(){
     	
     		labelAreaVSG.setVisible(true);
     		AreaVSG.setVisible(true);
 
             
     	}
     	
     /**************************DropdownVSG********************
      *Das Dropdown mit den Sportgruppen wird angezeigt.
      *Hierzu wird gleichzeitig das Label erstellt.
      *Sobald eine VSG gewhlt wurde, erscheint das DropDownVSA sowie eine Area.
      *TODO: In den String DropdownVSG mssen die richtigen Sportgruppen rein
      *TODO: AreaVSG muss befllt werden / eventuell aktualisiert? 
      */
     	
     public static void showDropdownVSG(){
     		
     	
     		labelVSG = new JLabel("Bitte whlen Sie eine Sportgruppe");
             labelVSG.setBounds(10, 50, 300, 25);
             panelVV.add(labelVSG);
             labelVSG.setVisible(true);
             labelVSG.validate();
     		
     		final String[] DropDownVSG = new String[] {"Judo", "Kampf", "Pi", "Pa", "Po"};
         	dropdownVSG = new JComboBox(DropDownVSG);
         	dropdownVSG.setBounds(10, 70, 200, 25);
         	panelVV.add(dropdownVSG);
         	panelVV.validate();
         	
         	
         	dropdownVSG.addActionListener(new ActionListener()
         	{
         		public void actionPerformed(ActionEvent dropdowVVe) 
         		{ 			
 //        				JComboBox item = (JComboBox) dropdownSGe.getSource();
 //        				
 //        				if (item.getSelectedIndex() != 0 && item.getSelectedIndex() < DropDownSG.length){
         				
         			
         				showDropdownVSA();
         				
         				if (countNew > 0){
         					hideFieldsNewV();
         					hideLabelsFields();
         				}
         				
         				if(countSA > 0){
         					hideAreaVSG();
         				}
         				
         				if(countVV >0){
         					hideAreaVSG();
         				}
         		}
 
         	});
     	}
     
     
     /**************************DropdownVSA********************
      *Das Dropdown mit den Sportarten wird angezeigt.
      *Hierzu wird gleichzeitig das Label erstellt.
      *Sobald eine VSA gewhlt wurde, erscheint das DropDownVV sowie eine Area.
      *TODO: In den String DropdownVSA mssen die richtigen Sportarten rein
      *TODO: AreaVSG muss befllt werden / eventuell aktualisiert? 
      */
    public static void createDropdownVSA(){
 	   
 	   	labelVSA = new JLabel("Bitte whlen Sie eine Sportart!");
    		labelVSA.setBounds(10, 100, 300, 25);
    		labelVSA.setVisible(false);
    		panelVV.add(labelVSA);
    		panelVV.validate();
    		panelVV.repaint();
        
        final String[] DropDownVSA = new String[] {"Sportarten", "Kampfsport", "Rckschlag", "Schnee", "Soooonenschein"};
      	dropdownVSA = new JComboBox(DropDownVSA);
       	dropdownVSA.setBounds(10, 120, 200, 25);
       	dropdownVSA.setVisible(false);
       	panelVV.add(dropdownVSA);
       	panelVV.validate();
    }
     
     public static void showDropdownVSA(){
         	
     		labelVSA.setVisible(true);
     		dropdownVSA.setVisible(true);
            	
            	dropdownVSA.addActionListener(new ActionListener(){
            		
            		public void actionPerformed(ActionEvent dropdownSAe) { 			
            			
            			
            			showButtonNewV();
            			showDropdownVV();
            			if (countVV >0){
            				hideAreaVSG();
            				btnDeleteV.setVisible(false);
            				btnEditV.setVisible(false);
            			}
            			
            			if (countNew > 0){
     					hideFieldsNewV();
     					hideLabelsFields();
     				}
            			
            			for (int i = 0; i < countSA; i++) {
            				hideAreaVSG();
 
 					}
            			}
            			
            		
        		});
         }
     
     
     /**************************DropdownVV********************
      *Das Dropdown mit den Veranstaltungen wird angezeigt.
      *Hierzu wird gleichzeitig das Label erstellt.
      *Sobald eine VSG gewhlt wurde, erscheint das DropDownVSA sowie eine Area.
      *TODO: In den String DropdownVV mssen die richtigen Veranstaltungen rein
      *TODO: AreaVSG muss befllt werden / eventuell aktualisiert? 
      */
   
     public static void createDropdownVV(){
     	labelVV = new JLabel("Bitte whlen Sie eine Veranstaltung!");
 		labelVV.setBounds(10, 150, 300, 25);
 		labelVV.setVisible(false);
         panelVV.add(labelVV);
         panelVV.validate();
 
 		
         String[] DropDownVV = new String[] {"Veranstaltungen", "Bla", "Beispiel1", "Example", "Ejemplo"};
     	
         dropdownVV = new JComboBox(DropDownVV);
     	dropdownVV.setBounds(10, 170, 200, 25);
     	dropdownVV.setVisible(false);
     	panelVV.add(dropdownVV);
     	panelVV.validate();
     }
     
     public static void showDropdownVV(){
     		
     		labelVV.setVisible(true);
     		dropdownVV.setVisible(true);
         	
         	dropdownVV.addActionListener(new ActionListener(){
            		
            		public void actionPerformed(ActionEvent dropdownVVe) { 			
            			
            			showBtnDelete();
            			showBtnEdit();
            			
            			countVV++;
            			
            			if (countNew > 0){
            				hideFieldsNewV();
            				hideLabelsFields();
            			}
            		}
        		});
         	
     	}
     	
     
     /*******************Button ndern einer Veranstaltung*************
      *Der Button erscheint.
      *Bei Klick wird die aktuelle Veranstaltung geladen
      */
     public static void showBtnEdit(){
     		
     		
         	btnEditV = new JButton("ndern");
     		btnEditV.setBounds( 600, 450, 150, 25);
         	panelVV.add(btnEditV);
         	panelVV.validate();
         	panelVV.repaint();
         	
         	btnEditV.addActionListener(new ActionListener() {
 				
 				public void actionPerformed(ActionEvent BtnEdite) {
 					hideAreaVSG();
 					showTextfieldsVAendern();
 					/*TODO: Hier muss dann die gewhlte Veranstaltung rein*/
 					btnEditV.setVisible(false);
 					btnDeleteV.setVisible(false);
 //					hideBtnAbortEdit();
 					showBtnEditOK();
 					showBtnAbortEdit();
 				}
 			});
     	}
 
     /************Button ndern einer Veranstaltung Abbrechen*************
      *Der Button erscheint.
      */
     public static void showBtnAbortEdit(){
 		
 		btnAbortEdit = new JButton("Abbrechen");
 		btnAbortEdit.setBounds( 650, 450, 150, 25);
     	panelVV.add(btnAbortEdit);
     	panelVV.validate();
     	panelVV.repaint();
     	
     	btnAbortEdit.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent BtnAbortEdite) {
 				hideFieldsEditV();
 				hideLabelsFields();
 				hideBtnEditOK();
 				hideBtnAbortEdit();
 				showAreaVSG();
 			}
 		});
 	}
     	
     /*******************Button Lschen einer Veranstaltung*************
      *Der Button erscheint.
      *Bei Klick wird die aktuelle Veranstaltung geladen
      */
     public static void showBtnDelete(){
     		
     		btnDeleteV = new JButton("Lschen");
     		btnDeleteV.setBounds(800, 450, 150, 25);
         	panelVV.add(btnDeleteV);
         	panelVV.validate();
         	panelVV.repaint();
     	} 
     	
     
     /***********Button ndern einer Veranstaltung Besttigen*************
      *Der Button erscheint.
      *
      */
     public static void showBtnEditOK(){
     		
     		btnEditOK = new JButton("bernehmen");
     		btnEditOK.setBounds(800, 450, 150, 25);
         	panelVV.add(btnEditOK);
         	panelVV.validate();
         	panelVV.repaint();
         	
         	
         	btnEditOK.addActionListener(new ActionListener() {
 				
 				public void actionPerformed(ActionEvent BtnEditOKe) {
 					hideFieldsEditV();
 					hideLabelsFields();
 					hideBtnEditOK();
 					hideBtnAbortEdit();
 					showAreaVSG();
 					/*TODO: Veranstaltung aktualisieren*/
 					
 				}
 			});
     	}  
     	
     
     /*******************Labels der Fields einblenden**************/
     public static void showLabelsFields(){
     		
     		labelVEBeschr = new JLabel("Name/Beschreibung:");
     		labelVEBeschr.setBounds(650, 60, 200, 25);
     		labelVEBeschr.setVisible(true);
     		panelVV.add(labelVEBeschr);
     		panelVV.validate();
     		
     		labelVEInfo = new JLabel("Informationen:");
         	labelVEInfo.setBounds(650, 110, 200, 25);
         	labelVEInfo.setVisible(true);
     		panelVV.add(labelVEInfo);
     		panelVV.validate();
     		
         	labelVEDatum = new JLabel("Datum:");
         	labelVEDatum.setBounds(650, 185, 200, 25);
         	labelVEDatum.setVisible(true);
     		panelVV.add(labelVEDatum);
     		panelVV.validate();
     		
     		labelVEUhrzeit = new JLabel("Uhrzeit:");
         	labelVEUhrzeit.setBounds(650, 225, 200, 25);
         	labelVEUhrzeit.setVisible(true);
     		panelVV.add(labelVEUhrzeit);
     		panelVV.validate();
 
         	labelVENiveau = new JLabel("Niveau:");
         	labelVENiveau.setBounds(650, 270, 200, 25);
         	labelVENiveau.setVisible(true);
     		panelVV.add(labelVENiveau);
     		panelVV.validate();
 
         	labelVEVorraussetungen = new JLabel("Vorraussetzungen:");
         	labelVEVorraussetungen.setBounds(650, 320, 200, 25);
         	labelVEVorraussetungen.setVisible(true);
     		panelVV.add(labelVEVorraussetungen);
     		panelVV.validate();
 
         	labelVEGebude = new JLabel("Gebude:");
         	labelVEGebude.setBounds(650, 370, 200, 25);
         	labelVEGebude.setVisible(true);
     		panelVV.add(labelVEGebude);
     		panelVV.validate();
     		
     	}
    
     
     /*******************TextFields zur Erstellung einblenden**************/
     public static void showTextfieldsVErstellen(){
     		
 //    		Color colorGrey = new Color(152,153,155);
         	showLabelsFields();
     		fieldBeschrE = new JTextField("");
     		fieldBeschrE.setToolTipText("Bitte tragen Sie hier einen Namen und eine Beschreibung der Veranstaltung ein");
     		fieldBeschrE.setBounds(650, 80, 300, 25);
         	panelVV.add(fieldBeschrE);
         	panelVV.validate();
     		
         	AreaInfoE = new JTextArea(5, 20);
         	AreaInfoE.setText("Lorem ipsum dolor sit amet, " +
             		"consetetur sadipscing elitr, sed diam nonumy " +
             		"eirmod tempor invidunt ut labore et " +
             		"dolore magna aliquyam erat, sed diam voluptua. " +
             		"At vero eos et accusam et justo duo dolores et " +
                             "ea rebum.");
         	 AreaInfoE.setLineWrap(true);
         	 AreaInfoE.setWrapStyleWord(true);
              scrollpaneAreaInfoE = new JScrollPane(AreaInfoE); 
           	 scrollpaneAreaInfoE.setBounds(650, 130, 300, 50);
              panelVV.add(scrollpaneAreaInfoE);
              scrollpaneAreaInfoE.setVisible(true);
              
         	final String[] DropDownDay = new String[] {"1", "2","3","4","5","6","7","8", "9","10","11","12","13", "14","15","16","17","18","19","20","21","22","23", "24","25","26","27","28","29","30","31"};
         	dropdownDayE = new JComboBox(DropDownDay);
         	dropdownDayE.setBounds(650, 200, 60, 30);
         	panelVV.add(dropdownDayE);
         	panelVV.validate();
         	
         	final String[] DropDownMonth = new String[] {"Jan", "Feb", "Mrz", "April", "Mai", "Juni","Juli","Aug","Sep","Okt","Nov","Dez"};
         	dropdownMonthE = new JComboBox(DropDownMonth);
         	dropdownMonthE.setBounds(720, 200, 80, 30);
         	panelVV.add(dropdownMonthE);
         	panelVV.validate();
         	
         	
         	final String[] DropDownYear = new String[] {"2013", "2014", "2015", "2016"};
         	dropdownYearE = new JComboBox(DropDownYear);
         	dropdownYearE.setBounds(820, 200, 100, 30);
         	panelVV.add(dropdownYearE);
         	panelVV.validate();
 
         	final String[] DropDownHour = new String[] {"08", "09", "10", "11", "12", "13","14","15","16","17","18","19","20","21", "22"};
         	dropdownHourE = new JComboBox(DropDownHour);
         	dropdownHourE.setBounds(650, 240, 80, 30);
         	panelVV.add(dropdownHourE);
         	panelVV.validate();
         	
         	
         	final String[] DropDownMinute = new String[] {"00","01", "02","03","04","05","06","07","08", "09","10","11","12","13", "14","15","16","17","18","19","20","21","22","23", "24","25","26","27","28","29","30","31", "32","33","34","35","36","37","38", "39","40","41","42","43", "44","45","46","47","48","49","50","51","52","53", "54","55","56","57","58","59"};
         	dropdownMinuteE = new JComboBox(DropDownMinute);
         	dropdownMinuteE.setBounds(740, 240, 80, 30);
         	panelVV.add(dropdownMinuteE);
         	panelVV.validate();
         	
         	
         	fieldNiveauE = new JTextField("");
         	fieldNiveauE.setBounds(650, 290, 300, 25);
         	fieldNiveauE.setToolTipText("Bitte geben Sie hier das Niveau der Veranstaltung ein!");
         	panelVV.add(fieldNiveauE);
         	panelVV.validate();
         	
         	
         	fieldVorraussetzungenE = new JTextField("");
         	fieldVorraussetzungenE.setBounds(650, 340, 300, 25);
         	fieldVorraussetzungenE.setToolTipText("Bitte geben Sie hier das Niveau der Veranstaltung ein!");
         	panelVV.add(fieldVorraussetzungenE);
         	panelVV.validate();
         	
         	final String[] DropDownGebude = new String[] {"G1", "G2", "10", "11", "12", "13"};
         	dropdownGebudeE = new JComboBox(DropDownGebude);
         	dropdownGebudeE.setBounds(650, 390, 300, 30);
         	panelVV.add(dropdownGebudeE);
         	panelVV.validate();
         
     	}
     	
     	
     /**************Meldungen bezglich Publishen/Subscriben**********
      *Unten links Area
      *TODO: Inhalt fllen
      */
     public static void showAreaAllgPanelV(){
     		
     		labelAreaAllgVV = new JLabel("Meldungen:");
     		labelAreaAllgVV.setBounds(10, 300, 300, 100);
             panelVV.add(labelAreaAllgVV);
         	panelVV.validate();
         	panelVV.repaint();
     		
     		AreaAllgemeinPanelV = new JTextArea(7, 20);
     		AreaAllgemeinPanelV.setText("");
     		AreaAllgemeinPanelV.setLineWrap(true);
     		AreaAllgemeinPanelV.setWrapStyleWord(true);
             scrollpaneAreaAllg = new JScrollPane(AreaAllgemeinPanelV); 
          	scrollpaneAreaAllg.setBounds(10, 370, 400, 150);
             panelVV.add(scrollpaneAreaAllg);
 
     		
     	}
     	
     
     /*******************TextFields zur nderung einblenden*
      * 
      * TODO: Die entsprechenden Daten mssen eingefpgt werden
      * 
      */
     public static void showTextfieldsVAendern(){
     		
     		showLabelsFields();
     		
     		fieldBeschrAE = new JTextField("");
     		fieldBeschrAE.setBounds(650, 80, 300, 25);
         	panelVV.add(fieldBeschrAE);
         	panelVV.validate();
         	
     		
         	AreInfoAE = new JTextArea(5, 20);
          	AreInfoAE.setText("Lorem ipsum dolor sit amet, " +
            		"consetetur sadipscing elitr, sed diam nonumy " +
            		"eirmod tempor invidunt ut labore et " +
            		"dolore magna aliquyam erat, sed diam voluptua. " +
            		"At vero eos et accusam et justo duo dolores et " +
                            "ea rebum.");
          	AreInfoAE.setLineWrap(true);
          	AreInfoAE.setWrapStyleWord(true);
          	scrollpaneAreaInfoAE = new JScrollPane(AreInfoAE); 
          	scrollpaneAreaInfoAE.setBounds(650, 130, 300, 50);
             panelVV.add(scrollpaneAreaInfoAE);
         	
     		
         	final String[] DropDownDayAE = new String[] {"0","1", "2","3","4","5","6","7","8", "9","10","11","12","13", "14","15","16","17","18","19","20","21","22","23", "24","25","26","27","28","29","30","31"};
         	dropdownDayAE = new JComboBox(DropDownDayAE);
         	dropdownDayAE.setBounds(650, 200, 60, 30);
         	panelVV.add(dropdownDayAE);
         	panelVV.validate();
         	
         	final String[] DropDownMonthAE = new String[] {"Jan", "Feb", "Mrz", "April", "Mai", "Juni","Juli","Aug","Sep","Okt","Nov","Dez"};
         	dropdownMonthAE = new JComboBox(DropDownMonthAE);
         	dropdownMonthAE.setBounds(720, 200, 80, 30);
         	panelVV.add(dropdownMonthAE);
         	panelVV.validate();
         	
         	
         	final String[] DropDownYearAE = new String[] {"2013", "2014", "2015", "2016"};
         	dropdownYearAE = new JComboBox(DropDownYearAE);
         	dropdownYearAE.setBounds(820, 200, 100, 30);
         	panelVV.add(dropdownYearAE);
         	panelVV.validate();
 
         	
         	final String[] DropDownHourAE = new String[] {"08", "09", "10", "11", "12", "13","14","15","16","17","18","19","20","21", "22"};
         	dropdownHourAE = new JComboBox(DropDownHourAE);
         	dropdownHourAE.setBounds(650, 240, 80, 30);
         	panelVV.add(dropdownHourAE);
         	panelVV.validate();
         	
         	
         	final String[] DropDownMinuteAE = new String[] {"00","01", "02","03","04","05","06","07","08", "09","10","11","12","13", "14","15","16","17","18","19","20","21","22","23", "24","25","26","27","28","29","30","31", "32","33","34","35","36","37","38", "39","40","41","42","43", "44","45","46","47","48","49","50","51","52","53", "54","55","56","57","58","59"};
         	dropdownMinuteAE = new JComboBox(DropDownMinuteAE);
         	dropdownMinuteAE.setBounds(740, 240, 80, 30);
         	panelVV.add(dropdownMinuteAE);
         	panelVV.validate();
         	
         	
         	fieldNiveauAE = new JTextField("");
         	fieldNiveauAE.setBounds(650, 290, 300, 25);
         	panelVV.add(fieldNiveauAE);
         	panelVV.validate();
         	
         	
         	fieldVorraussetzungenAE = new JTextField("");
         	fieldVorraussetzungenAE.setBounds(650, 340, 300, 25);
         	panelVV.add(fieldVorraussetzungenAE);
         	panelVV.validate();
         	
         	
         	
         	final String[] DropDownGebudeAE = new String[] {"G1", "G2", "10", "11", "12", "13"};
         	dropdownGebudeAE = new JComboBox(DropDownGebudeAE);
         	dropdownGebudeAE.setBounds(650, 390, 300, 30);
         	panelVV.add(dropdownGebudeAE);
         	panelVV.validate();
     	}
     	
     public static void hideBtnNewV(){
     		btnNewV.setVisible(false);
     		
     	}
     public static void hideDropDownVS(){
     		
     		dropdownVV.setVisible(false);
     		labelVV.setVisible(false);
     	}
     public static void hideAreaVSG(){
     		
     		AreaVSG.setVisible(false);
     		labelAreaVSG.setVisible(false);
     	}
     public static void hideBtnDelete(){
     		
     		btnDeleteV.setVisible(false);
     		
     	}
     public static void hideBtnEdit(){
     		
     		btnEditV.setVisible(false);
     	}
     public static void hideBtnAbortEdit(){
     		
     		btnAbortEdit.setVisible(false);
     		
     	}
     public static void hideFieldsEditV(){
     		fieldBeschrAE.setVisible(false);
     		AreInfoAE.setVisible(false);
     		dropdownDayAE.setVisible(false);
     		scrollpaneAreaInfoAE.setVisible(false);
     		dropdownMonthAE.setVisible(false);
     		dropdownYearAE.setVisible(false);
     		dropdownHourAE.setVisible(false);
     		dropdownMinuteAE.setVisible(false);
     		dropdownGebudeAE.setVisible(false);
     		fieldNiveauAE.setVisible(false);
     		fieldVorraussetzungenAE.setVisible(false);
     		
     	}
     public static void hideFieldsNewV(){
     		labelVEBeschr.setVisible(false);
     		fieldBeschrE.setVisible(false);
     		labelVEInfo.setVisible(false);
     		scrollpaneAreaInfoE.setVisible(false);
     		labelVEDatum.setVisible(false);
     		dropdownDayE.setVisible(false);
     		dropdownMonthE.setVisible(false);
     		dropdownYearE.setVisible(false);
     		labelVEUhrzeit.setVisible(false);
     		dropdownHourE.setVisible(false);
     		dropdownMinuteE.setVisible(false);
     		labelVEGebude.setVisible(false);
     		dropdownGebudeE.setVisible(false);
     		labelVENiveau.setVisible(false);
     		fieldNiveauE.setVisible(false);
     		labelVEVorraussetungen.setVisible(false);
     		fieldVorraussetzungenE.setVisible(false);
     		btnNewOK.setVisible(false);
     		
     	}
     public static void hideBtnEditOK(){
     		btnEditOK.setVisible(false);
     	}
     public static void hideLabelsFields(){
     		
     		labelVEBeschr.setVisible(false);
     		labelVEInfo.setVisible(false);
         	labelVEDatum.setVisible(false);
     		labelVEUhrzeit.setVisible(false);
         	labelVENiveau.setVisible(false);
         	labelVEVorraussetungen.setVisible(false);
         	labelVEGebude.setVisible(false);
     		
     	}
 
     /***************(Un-)Subscribe Equipment und Gebude***********
     * 
     * Sind nicht augerufen.
     * Eventuell spter einfach noch machen? 
     *
     ****************************************************************/
         public static void showButtonSubsribeE(){
         		
             	btnSubscribeE = new JButton("Subscribe");
             	btnSubscribeE.setBounds(600, 400, 150, 25);
             	panelE.add(btnSubscribeSA);
             	panelE.validate();
             	panelE.repaint();
             	
             	btnSubscribeE.addActionListener(new ActionListener() {
     				
     				
     				public void actionPerformed(ActionEvent btnSubEe) {
 
     					
     				}
     			});
 
             	
         	}
 
         public static void showButtonSubsribeG(){
         		
             	btnSubscribeG = new JButton("Subscribe");
             	btnSubscribeG.setBounds(600, 400, 150, 25);
             	panelVV.add(btnSubscribeG);
             	panelVV.validate();
             	panelVV.repaint();
             	
             	btnSubscribeG.addActionListener(new ActionListener() {
     				
     				
     				public void actionPerformed(ActionEvent btnSubGe) {
 
     					
     				}
     			});
 
             	
         	}
 
         public static void showButtonUnsubsribeE(){
         		
             	btnUnsubscribeE = new JButton("Subscribe");
             	btnUnsubscribeE.setBounds(600, 400, 150, 25);
             	panelE.add(btnUnsubscribeE);
             	panelE.validate();
             	panelE.repaint();
             	
             	btnUnsubscribeE.addActionListener(new ActionListener() {
     				
     				
     				public void actionPerformed(ActionEvent btnUnsubEe) {
 
     					
     				}
     			});
 
             	
         	}
 
         public static void showButtonUnsubsribeG(){
         		
             	btnUnsubscribeG = new JButton("Unsubscribe");
             	btnUnsubscribeG.setBounds(600, 400, 150, 25);
             	panelVV.add(btnUnsubscribeG);
             	panelVV.validate();
             	panelVV.repaint();
             	
             	btnUnsubscribeG.addActionListener(new ActionListener() {
     				
     				
     				public void actionPerformed(ActionEvent btnUnsubGe) {
 
     					
     				}
     			});
 
             	
         	}
 
 
 
 }
