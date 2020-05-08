 package model;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import model.elements.Bunker;
 import model.elements.Invader;
 import model.elements.InvaderType;
 import model.elements.Player;
 import org.xml.sax.SAXException;
 import utils.SaxHandler;
 
 /**
  * This Factory is responsible for creating {@link GameState}s that are levels
  */
 public class GameStateFactory {
         
     private static final String XML_FILE = "./GameStateLevels.xml";
     private ArrayList<ArrayList> levels;
     
     public GameStateFactory() {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         
         try {
             InputStream xmlInput = this.getClass().getResourceAsStream(XML_FILE);
             
             SAXParser saxParser = factory.newSAXParser();
             
             SaxHandler handler = new SaxHandler();
             saxParser.parse(xmlInput, handler);
             
             levels = handler.parseFile();
             
             // TODO:
             for (ArrayList level : levels) {
                 for (Object obj : level) {
                     Player o = (Player) obj;
                     //System.out.println(obj.getClass());
                     System.out.println(o);
                 }
             }
     
         } catch (FileNotFoundException e) {
             System.out.println("ERROR: Cannot find file: "+XML_FILE);
             System.exit(1);
         } catch (ParserConfigurationException e) {
             System.out.println("ERROR: ParserConfigurationException thrown");
             System.exit(1);
         } catch (SAXException e) {
             System.out.println("ERROR: SAXException thrown");
             System.exit(1);
         } catch (IOException e) {
             System.out.println("ERROR: IOException thrown");
             System.exit(1);
         }
     }
 
     static public GameState createLevelOne() {
             GameState gs = new GameState();
 
             // Bunkers
             Bunker b1 = new Bunker();
             b1.getPosition().x = 50;
             b1.getPosition().y = 450;
             gs.getBunkers().add(b1);
 
             // Player
             gs.setPlayer(new Player());
             gs.getPlayer().getPosition().x = 250 - 24;
             gs.getPlayer().getPosition().y = 600;
 
             // Invaders
             int invaderCounter = 0, columnsOfInvaders = 7, rowsOfInvaders = 3, widthBetweenInvaders = 55, heightBetweenInvaders = 50;
             int xInvaderStart = 50, yInvaderStart = 50;
             for (int i = 0; i < columnsOfInvaders; i++) {
             	for (int j = 0; j < rowsOfInvaders; j++) {
             		gs.getInvaders().add(new Invader(InvaderType.A, 1));
                 	gs.getInvaders().get(invaderCounter).getPosition().x = i*widthBetweenInvaders+xInvaderStart;
                 	gs.getInvaders().get(invaderCounter).getPosition().y = j*heightBetweenInvaders+yInvaderStart;
                 	invaderCounter++;
 				}
             	
 			}
             
 
             return gs;
     }
 
 }
