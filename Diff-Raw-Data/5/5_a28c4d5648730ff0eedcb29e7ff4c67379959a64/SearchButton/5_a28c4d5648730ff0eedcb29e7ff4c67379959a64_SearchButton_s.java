 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import deck.MTGCard;
 import deck.MTGDeckMain;
 import deck.XMLParser;
 
 public class SearchButton extends JButton implements ActionListener {
 	
 	private XMLParser parser;
 	private ArrayList<MTGCard> results;
 	private CardDisplayPanel cardDisplay;
 
 	public SearchButton(String name, CardDisplayPanel cardDisplay, XMLParser parser) throws ParserConfigurationException, SAXException, IOException{
 		super(name);
 		this.parser = parser;
 		this.addActionListener(this);
 		this.cardDisplay = cardDisplay;
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 			// TODO Auto-generated method stub
 			String type = JOptionPane.showInputDialog(MTGDeckMain.messages.getString("SearchButton2"));
 			String term = JOptionPane.showInputDialog(MTGDeckMain.messages.getString("SearchButton3"));
 			String query = this.parser.buildXPathQuery(type.toLowerCase(), term);
 			this.results = this.parser.searchXML(query);
 			this.cardDisplay.setListOfCards(this.results);
 			JOptionPane.showMessageDialog(this,  MTGDeckMain.messages.getString("SearchComplete"));
 			this.getParent().getParent().repaint();
 	}
 	
 }
