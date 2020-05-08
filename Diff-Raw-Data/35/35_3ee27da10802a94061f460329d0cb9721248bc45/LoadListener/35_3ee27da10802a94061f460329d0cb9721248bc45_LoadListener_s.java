 /**
  * 
  */
 package gui.action.menu;
 
 import gui.main.TLP;
 
 import java.io.File;
 
 import tools.io.LogLoader;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
import javafx.scene.control.Label;
 import javafx.scene.layout.BorderPane;
 import javafx.stage.FileChooser;
 
 /**
  * @author Tingle Driftwood
  * 
  */
 public class LoadListener implements EventHandler<ActionEvent> {
 
 	private BorderPane border = new BorderPane();
 	private TLP tlp;
 
 	public LoadListener(BorderPane border, TLP tlp) {
 		super();
 		this.border = border;
 		this.tlp = tlp;
 	}
 
 	@Override
 	public void handle(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 		FileChooser fc = new FileChooser();
 
 		File file = fc.showOpenDialog(null);
 
 		LogLoader loader = new LogLoader(file);
 		
 		tlp.setLog(loader.getLog());
 		
		Label infoBar = new Label();
		infoBar.setText("Actual file: " + file.getPath());
 
		border.setBottom(infoBar);
 	}
 
 }
