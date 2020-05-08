 /**
  * 
  */
 package aufgabe6;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JList;
 import javax.swing.JTree;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import aufgabe6.net.Client;
 import aufgabe6.net.Server;
 
 /**
  * @author sascha
  *
  */
 public class Gui implements GuiInterface {
 	
 	private static final Dimension FENSTER_MIN_DIM = new Dimension(800,600);
 
 	private static final int NAMENSFELD_MAX_BREITE = 20;
 	private static final int IPFELD_MAX_BREITE = 20;
 
 	private JFrame fenster = null;
 	
 	private DefaultListModel serverList = null;
 	
 	private JPanel serverSicht = null;
 	
 	private JTree spielerAnsicht = null;
 	
 	private DefaultMutableTreeNode wurzelKnotenSpielerAnsicht = null;
 	
 	private JScrollPane serverAnsichtsContainer = null;
 	
 	private DefaultMutableTreeNode wurzelKnotenServerAnsicht = null;
 	
 	private JTextField namensFeld = null;
 	public String getNamensFeldInhalt()
     {
         return namensFeld.getText();
     }
 
     private JTextField ipFeld = null;
 	
 	private JPanel knopfContainerServerAnsicht = null;
 	
 	private JButton erstellKnopf = null;
 	
 	private JButton hinzufuegenKnopf = null;
 	
 	private JButton verbindeKnopf = null;
 	
 	private JButton trenneKnopf = null;
 	
 	private JTextPane spielNachrichten = null;
 	
 	private GuiSpielfeld spielfeld = null;
 
 	private JPanel spielfeldContainer = null;
 	
 	private static Gui singleTon = null;
 	
 	public static Gui getGui(){
 		if(Gui.singleTon == null){
 			Gui.singleTon = new Gui();
 		}
 		return Gui.singleTon;
 	}
 	
 	private Gui(){
 		
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		this.fenster = new JFrame("Mensch aergere dich nicht");
 		this.fenster.setMinimumSize(FENSTER_MIN_DIM);
 		this.fenster.setUndecorated(false);
 		this.fenster.setLocationByPlatform(true);
 		this.fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JSplitPane unterteiler = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); 
 		unterteiler.setResizeWeight(1d);
 		unterteiler.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 		unterteiler.setDividerSize(5);
 		
 		this.spielfeldContainer  = new JPanel();
 		
 		this.spielfeld = new GuiSpielfeld();
 		
 		spielfeldContainer.add(this.spielfeld);
 		
 		unterteiler.setLeftComponent(spielfeldContainer);
 		
 		JPanel rechtesUnterFenster = new JPanel(new BorderLayout());
 		rechtesUnterFenster.setPreferredSize(new Dimension(200,1));
 		
 		this.serverSicht = new JPanel(new BorderLayout());
 		
 		serverList = new DefaultListModel();
 		
 		final JList serverAnsicht = new JList(serverList);
 		serverAnsicht.setLayoutOrientation(JList.VERTICAL);
 		serverAnsicht.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		serverAnsicht.addListSelectionListener(new ListSelectionListener() {
 
 			public void valueChanged(ListSelectionEvent event) {
 				if (event.getFirstIndex() >= 0) {
 					verbindeKnopf.setEnabled(true);
 				} else {
 					verbindeKnopf.setEnabled(false);
 				}
 			}
 			
 		});
 		
 		this.serverAnsichtsContainer = new JScrollPane(serverAnsicht);
 		
 		this.serverSicht.add(this.serverAnsichtsContainer,BorderLayout.NORTH);
 		
 		this.knopfContainerServerAnsicht = new JPanel(new GridLayout(2,3));
 		
 		this.namensFeld  = new JTextField("Spielername", NAMENSFELD_MAX_BREITE);
 		this.namensFeld.setFont(this.namensFeld.getFont().deriveFont(9.f));
 		this.namensFeld.setEditable(true);
 						
 		this.erstellKnopf = new JButton("erstellen");
 		this.erstellKnopf.setToolTipText("Startet einen Serverprozeß im Hintergrund, der auf Verbindungen wartet.");
 		this.erstellKnopf.addActionListener(
 		        new ActionListener()
 		        {
                     @Override
                     public void actionPerformed(ActionEvent e)
                     {
                         Server s = new Server(9999, namensFeld.getText());
                         s.lausche();
                         erstellKnopf.setToolTipText("Im Moment läuft bereits ein Serverprozess.");
                         erstellKnopf.setEnabled(false);
                     }
 		        }
		)		this.namensFeld.setFont(this.namensFeld.getFont().deriveFont(9.f));
;
 		
 	    this.ipFeld = new JTextField("IP", IPFELD_MAX_BREITE);
 		this.ipFeld.setFont(this.ipFeld.getFont().deriveFont(9.f));
         this.ipFeld.setEditable(true);
 
         this.hinzufuegenKnopf = new JButton("hinzufügen");
 		this.hinzufuegenKnopf.addActionListener(new ActionListener()
 		{
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 Client c = Client.getInstance();
                 Client.ServerInfo si = c.new ServerInfo(namensFeld.getText(),ipFeld.getText());
                 Client.getInstance().getServerInfos().add(si);
                 serverList.addElement(namensFeld.getText());
             }
 		}
 		        );
 		
 		this.verbindeKnopf = new JButton("verbinden");
 		this.verbindeKnopf.setEnabled(false);
 		this.verbindeKnopf.addActionListener(
 		        new ActionListener()
 		        {
 
                     @Override
                     public void actionPerformed(ActionEvent e)
                     {
                         Client c = Client.getInstance();
                         int index = serverAnsicht.getSelectedIndex();
                         System.out.println(c.getServerInfos().get(index).getIp());
                         c.verbinde(c.getServerInfos().get(index).getIp(), namensFeld.getText());
                     }
 		            
 		        }
 		
 		);
 		
 		this.knopfContainerServerAnsicht.add(this.namensFeld);
 	    this.knopfContainerServerAnsicht.add(this.ipFeld);
 		this.knopfContainerServerAnsicht.add(this.hinzufuegenKnopf);
 		this.knopfContainerServerAnsicht.add(new JLabel());//free space
         this.knopfContainerServerAnsicht.add(this.erstellKnopf);
 		this.knopfContainerServerAnsicht.add(this.verbindeKnopf);
 		
 		this.serverSicht.add(this.knopfContainerServerAnsicht, BorderLayout.CENTER);
 		
 		rechtesUnterFenster.add(this.serverSicht, BorderLayout.NORTH);
 		
 		this.wurzelKnotenSpielerAnsicht = new DefaultMutableTreeNode();
 		
 		this.spielerAnsicht = new JTree(this.wurzelKnotenSpielerAnsicht);
 		
 		this.trenneKnopf = new JButton("trennen");
 		
 		this.spielNachrichten = new JTextPane();
 		this.spielNachrichten.setEditable(true);
 		
 		JScrollPane nachrichtenContainer = new JScrollPane(this.spielNachrichten);
 		
 		rechtesUnterFenster.add(nachrichtenContainer, BorderLayout.CENTER);
 		
 		unterteiler.setRightComponent(rechtesUnterFenster);
 		
 		this.fenster.add(unterteiler);
 	
 	}
 	
 	/* (non-Javadoc)
 	 * @see aufgabe6.GuiInterface#aenderFigurPosition(aufgabe6.Figur, int, int)
 	 */
 
 	@Override
 	public void aenderFigurPosition(Figur f, int vorher, int nachher) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void starteGui() {
 		this.fenster.pack();
 		this.passeSpielfeldAn();
 		this.fenster.setVisible(true);	}
 
 	private void passeSpielfeldAn() {
 		int groesse = Math.min(this.spielfeldContainer.getWidth(), this.spielfeldContainer.getHeight());
 		Dimension d = new Dimension(groesse-(groesse%11),groesse-(groesse%11));
 		this.spielfeld.setPreferredSize(d);
 		this.spielfeld.setSize(d); // damit das Spielfeld auch unter Windows sofort in der richtigen Gr��e angezeigt wird... 
 		this.spielfeldContainer.validate();	
 	}
 	
 	@SuppressWarnings({ "serial" })
 	private class GuiSpielfeld extends JPanel{
 		private byte [][] feld = null;
 		private Point[] figurenPositionen = null;
 		private int[] figurenImHaus = null;
 		
 		public GuiSpielfeld(){
 			this.setBackground(Color.WHITE);
 			
 			this.feld = new byte[][] { 
 					{ 2, 2, 0, 0, 1, 1, 3, 0, 0, 3, 3 },
 					{ 2, 2, 0, 0, 1, 3, 1, 0, 0, 3, 3 },
 					{ 0, 0, 0, 0, 1, 3, 1, 0, 0, 0, 0 },
 					{ 0, 0, 0, 0, 1, 3, 1, 0, 0, 0, 0 },
 					{ 2, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1 },
 					{ 1, 2, 2, 2, 2, 0, 4, 4, 4, 4, 1 },
 					{ 1, 1, 1, 1, 1, 5, 1, 1, 1, 1, 4 },
 					{ 0, 0, 0, 0, 1, 5, 1, 0, 0, 0, 0 },
 					{ 0, 0, 0, 0, 1, 5, 1, 0, 0, 0, 0 },
 					{ 5, 5, 0, 0, 1, 5, 1, 0, 0, 4, 4 },
 					{ 5, 5, 0, 0, 5, 1, 1, 0, 0, 4, 4 } };
 			
 			this.figurenPositionen = new Point[]{
 					new Point(4,0),new Point(4,1),new Point(4,2),new Point(4,3),new Point(4,4),new Point(3,4),
 					new Point(2,4),new Point(1,4),new Point(0,4),new Point(0,5),new Point(0,6),new Point(1,6),
 					new Point(2,6),new Point(3,6),new Point(4,6),new Point(4,7),new Point(4,8),new Point(4,9),
 					new Point(4,10),new Point(5,10),new Point(6,10),new Point(6,9),new Point(6,8),new Point(6,7),
 					new Point(6,6),new Point(7,6),new Point(8,6),new Point(9,6),new Point(10,6),new Point(10,5),
 					new Point(10,4),new Point(9,4),new Point(8,4),new Point(7,4),new Point(6,4),new Point(6,3),
 					new Point(6,2),new Point(6,1),new Point(6,0),new Point(5,0)
 			};
 			
 			this.figurenImHaus = new int[]{ 0,0,0,0 };
 		}
 		
 		protected void paintComponent(Graphics g){
 			super.paintComponent(g);
 			int laenge = this.getWidth()/11;
 			Graphics2D g2 = (Graphics2D)g;
 			Color tmpColor = g2.getColor();
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			for(int i = 0; i< this.feld.length;i++){
 				for(int j = 0; j < feld[i].length; j++){
 					Color currentColor= berechneFarbe(feld[i][j]);
 					if(currentColor == null) continue;
 					g2.setColor(currentColor);
 					g2.fillOval(j*laenge+2, i*laenge+laenge/10, laenge-laenge/5, laenge-laenge/5);
 					g2.setColor(Color.BLACK);
 					g2.drawOval(j*laenge+2, i*laenge+laenge/10, laenge-laenge/5, laenge-laenge/5);			
 				}
 			}
 			for(Figur f: Spielfeld.getInstance().getWahrscheinlichFiguren()){
 				if(f!=null){
 					Color currentColor = berechneFarbe(f.getBesitzer().getSpielernummer()+2);
 					g2.setColor(currentColor.darker().darker());
 					Point position = berechnePosition(f,laenge);
 					g2.fillOval(position.x, position.y, laenge-laenge/16, laenge-laenge/16);
 					g2.setColor(currentColor.darker());
 					g2.fillOval(position.x, position.y, laenge-laenge/32, laenge-laenge/32);
 				}
 			}
 			g2.setColor(tmpColor);	
 		}
 
 		private Point berechnePosition(Figur f, int laenge) {
 			Point position = new Point();
 			if(f.istInZiel() || f.getPosition() == -1){
 				position.x = this.figurenPositionen[f.getPosition()].y;
 				position.y = this.figurenPositionen[f.getPosition()].x;
 			}else{
 				switch(f.getBesitzer().getSpielernummer()){
 				case 0:
 					position.x = 0;
 					position.y = 0;
 					break;
 				case 1:
 					position.x = 9;
 					position.y = 0;
 					break;
 				case 2:
 					position.x = 0;
 					position.y = 9;
 					break;
 				case 3:
 					position.x = 9;
 					position.y = 9;
 					break;
 				default:
 					return null;
 				}
 			}
 			return null;
 		}
 
 		private Color berechneFarbe(int i) {
 			Color currentColor = null;
 			switch(i){
 			case 0:
 				break;
 			case 2:
 				currentColor = Color.GREEN;
 				break;
 			case 3:
 				currentColor = Color.RED;
 				break;
 			case 4:
 				currentColor = Color.BLUE;
 				break;
 			case 5:
 				currentColor = Color.YELLOW;
 				break;
 			case 1:
 			default:
 				currentColor = Color.WHITE;
 				break;
 			}
 			return currentColor;
 		}
 	}
 }
