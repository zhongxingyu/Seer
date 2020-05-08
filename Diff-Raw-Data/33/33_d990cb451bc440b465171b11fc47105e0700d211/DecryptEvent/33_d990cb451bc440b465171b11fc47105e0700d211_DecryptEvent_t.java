 /** ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  *  +      Zuercher Hochschule angewandter Wissenschaften      +
  *  +                    Software Projekt 2                    +
  *  +                                                          +
  *  +        Gruppe 10: Miro Ljubicic & Mathias Weigert        +
  *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  */
 package krypto.gui.action.panel;
 
 import java.util.Map;
 
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.scene.control.TextArea;
 import javafx.scene.text.Text;
 import krypto.gui.javafx.panel.bottom.CaesarBottomFX;
 import krypto.tools.decrypt.CaesarBruteForce;
 
 /**
  * @author Mathias Weigert & Miro Ljubicic
  * @version 1.0
  */
 public class DecryptEvent implements EventHandler<Event>{
 
 	private TextArea cipher;
 	private Object bottomPanel;
 	private Character activePanel;
 	
 	public DecryptEvent(TextArea cipher, Object bottomPanel,Character activePanel) {
 		super();
 		this.cipher = cipher;
 		this.bottomPanel = bottomPanel;
 		this.activePanel = activePanel;
 	}
 
 	@Override
 	public void handle(Event arg0) {
 		// TODO Auto-generated method stub
 		System.out.println("Decrypt: " + activePanel);
 		if (activePanel.equals('C')) {
 			CaesarBottomFX caesarBottom = (CaesarBottomFX) bottomPanel;
 			CaesarBruteForce cbf = new CaesarBruteForce(cipher.getText().toUpperCase());
 			String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 			Map<Character, String> report = cbf.getReport();
 			
 			String outputStr = new String();
 			for (int i = 0; i < 26; i++) {
 				outputStr = outputStr + "Key: "+ abc.charAt(i) + " Plain: " + report.get(abc.charAt(i)) + "\n";
 			}
 			System.out.println(outputStr);
//			TextArea outputArea = new TextArea(outputStr);
//			caesarBottom.setOutputArea(outputArea );
			caesarBottom.getOutputArea().setText(outputStr);
 			Text elapsedTime = new Text("Elapsed time: " + cbf.getTime() + " ms.");
 			caesarBottom.setTimeLabel(elapsedTime);
 		}
 	}
 
 	/**
 	 * @return the activePanel
 	 */
 	public Character getActivePanel() {
 		return activePanel;
 	}
 
 	/**
 	 * @param activePanel the activePanel to set
 	 */
 	public void setActivePanel(Character activePanel) {
 		this.activePanel = activePanel;
 	}
 
 	
 }
