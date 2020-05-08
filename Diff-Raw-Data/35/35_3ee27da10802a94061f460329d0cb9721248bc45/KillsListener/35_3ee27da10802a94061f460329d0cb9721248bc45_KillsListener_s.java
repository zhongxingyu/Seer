 /**
  * 
  */
 package gui.action.side;
 
 import gui.main.TLP;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import tools.analyse.KillList;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.control.TextArea;
 import javafx.scene.layout.BorderPane;
 
 /**
  * @author Tingle Driftwood
  * 
  */
 public class KillsListener implements EventHandler<ActionEvent> {
 
 	private TLP tlp;
 	private Map<Integer, String> log;
 	private BorderPane border;
 
 	public KillsListener(BorderPane border, TLP tlp) {
 		super();
 		this.tlp = tlp;
 		this.border = border;
 	}
 
 	@Override
 	public void handle(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 		String info = new String();
 		log = new HashMap<Integer, String>();
 		log = tlp.getLog();
		
		KillList klist = new KillList(log);
		Map<Integer, String> kills = klist.GetKillList();
		
 		for (int i = 0; i < kills.size(); i++) {
 			if (i > 0) {
 				info += "\n";
 			}
 			info += kills.get(i);
 		}
 
 		TextArea text = new TextArea(info);
 		border.setCenter(text);
 	}
 
 }
