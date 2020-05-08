 package views;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import logic.GekozenAntwoord;
 import logic.Vraag;
 import net.miginfocom.swing.MigLayout;
 import views.components.NiceButton;
 import views.factories.JLabelFactory;
 import views.factories.JPanelFactory;
 import controllers.Spel;
 
 public class ResultatenScherm extends JPanel {
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Spel spel = new Spel();
 					List<logic.Onderwerp> onderwerpen = spel.getOnderwerpen();
 					spel.setOnderwerp(onderwerpen.get(2));
 
 					List<logic.Onderdeel> ondrln = spel.getOnderdelen();
 					List<logic.Onderdeel> ondrln2 = new ArrayList<logic.Onderdeel>(); 
 					
 					ondrln2.add(ondrln.get(0)); // 0
 					ondrln2.add(ondrln.get(1)); // 1
 					ondrln2.add(ondrln.get(2)); // 2
 					ondrln2.add(ondrln.get(3)); // 3
 					ondrln2.add(ondrln.get(4)); // 4
 					ondrln2.add(ondrln.get(5)); // 5
 					ondrln2.add(ondrln.get(6)); // 6
 					ondrln2.add(ondrln.get(7)); // 7
 					ondrln2.add(ondrln.get(8)); // 8
 					
 					for (logic.Onderdeel o : ondrln2) {
 						spel.volgendeOnderdeel();
 						spel.kiesOnderdeel(o);
 					}

 					
 					SpeelScherm speelscherm = new SpeelScherm(spel);
 					spel.zetJokersIn(0);
 					spel.getTimer().stop();
 					spel.openPanel(speelscherm);
 					speelscherm.openResultaten();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	private JPanel	onderdelen;
 	private JPanel	panel_1;
 
 	/**
 	 * Create the panel.
 	 * 
 	 * @param spel
 	 */
 	public ResultatenScherm(final Spel spel) {
 		ArrayList<GekozenAntwoord> gekozenAntwoorden = spel.getGekozenAntwoorden();
 
 		setBackground(new Color(43, 43, 43));
 		setBorder(null);
 		setLayout(new MigLayout("", "[grow][77px][grow]", "[grow][20px,growprio 50,grow][grow]"));
 
 		JPanel panel = JPanelFactory.createBackgroundJPanel();
 		add(panel, "cell 1 1,grow");
 		panel.setLayout(new MigLayout("", "[grow,fill][95px]", "[20px,growprio 50,grow]"));
 
 		panel_1 = JPanelFactory.createTransparentJPanel();
 		panel.add(panel_1, "cell 1 0,grow");
 		panel_1.setLayout(new MigLayout("", "[95px][90px]", "[20px,growprio 50,grow][20px]"));
 
 		JLabel dtrpnScore = new JLabel();
 		panel_1.add(dtrpnScore, "cell 0 0 2 1,grow");
 		dtrpnScore.setBackground(new Color(0, 0, 0, 0));
 		dtrpnScore.setForeground(Color.BLACK);
 
 		int aantalGoed = 0;
 		int aantalFout = 0;
 
 		for (GekozenAntwoord gk : gekozenAntwoorden)
 			if (gk.isGoed())
 				aantalGoed++;
 			else aantalFout++;
 		
 		Vraag vraag = spel.getHuidigeVraag();
 		int jokers = vraag.getHoeveelJokersGebruikt();
 		StringBuilder builder = new StringBuilder();
 		builder.append("<html>Aantal <span style=\"color: green; font-weight:bold;\">goed</span>: ");
 		builder.append(aantalGoed);
 		if(jokers > 0) {
 			builder.append("<b> + "+(Math.min(aantalFout, jokers))+"</b>");
 		}
 		builder.append("<br/>");
 		builder.append("Aantal <span style=\"color: red; font-weight:bold;\">fout</span>: ");
 		builder.append(aantalFout);
 		builder.append("<br/>");
 		builder.append("Hoeveel goed hebben: ");
 		builder.append(spel.getHoeveelGoedVerplicht());
 		builder.append("<br/>");
 		builder.append("Jokers gebruikt: ");
 		builder.append(vraag.getHoeveelJokersGebruikt());
 		builder.append("<br/>");
 		builder.append("Jokers over: ");
 		builder.append(spel.getJokerAantal());
 		builder.append("<br/>");
 		builder.append("Score: ");
 		builder.append(spel.getEindScore());
 		builder.append(", tijd: ");
 		builder.append(spel.getTimer().getTime());
 		builder.append(" sec");
 		if(!spel.magDoorspelen()) {
 			builder.append("<br /><br /><b>Je hebt verloren.</b>");
 		} else if(spel.moetDoorspelen()) {
 			builder.append("<br /><br /><b>Je moet doorspelen.</b>");	
 		}
 		
 		builder.append("</html>");
 		dtrpnScore.setText(builder.toString());
 
 		NiceButton btnStoppen = new NiceButton("Stoppen");
 		NiceButton btnNewButton = new NiceButton("Verder");
 		
 		btnStoppen.setEnabled(!spel.moetDoorspelen());
 		
 		panel_1.add(btnStoppen, "cell 0 1,grow");
 		panel_1.add(btnNewButton, "cell 1 1,grow");
 		btnNewButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				spel.volgendeVraag();
 			}
 		});
 		btnStoppen.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
				spel.backToMainMenu();
 			}
 		});
 
 		onderdelen = JPanelFactory.createTransparentJPanel();
 		panel.add(onderdelen, "cell 0 0,alignx left,growy");
 		onderdelen.setLayout(new MigLayout("", "[200px:n,grow,fill][10px][200px:n,grow,fill]", "[20px][20px][20px][20px][20px][20px][20px][20px][20px]"));
 		
 		int i = 0;
 		
 		for (GekozenAntwoord gk : gekozenAntwoorden) {
 			JLabel txt = JLabelFactory.createAntwoordJLabel(gk.getHuidigeOnderdeel().getTekst());
 			onderdelen.add(txt, "cell 0 " + i + ",grow");
 			
 			onderdelen.add(JLabelFactory.createAntwoordJLabel("="), "cell 1 " + i + ",grow");
 			
 			JLabel txtBoolean =  JLabelFactory.createAntwoordJLabel(gk.getGekozenOnderdeel().getAntwoord());
 
 			txtBoolean.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 			if (gk.isGoed())
 				txtBoolean.setForeground(new Color(43, 111, 43));
 			else if(jokers > 0) {
 				txtBoolean.setForeground(new Color(244, 77, 50));
 				txtBoolean.setText("<html><s>"+txtBoolean.getText() + "</s></html>");
 				jokers--;
 			} else txtBoolean.setForeground(Color.RED);
 
 			onderdelen.add(txtBoolean, "cell 2 " + i + ",grow");
 
 			i++;
 		}
 
 	}
 
 }
