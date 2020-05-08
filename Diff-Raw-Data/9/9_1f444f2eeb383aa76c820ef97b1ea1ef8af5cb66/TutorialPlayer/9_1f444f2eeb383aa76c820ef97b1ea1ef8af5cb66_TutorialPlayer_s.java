 package gui.layout;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import java.awt.Component;
 import java.awt.BorderLayout;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.media.MediaLocator;
 import javax.media.Player;
 import javax.media.Manager;
 import javax.media.NoPlayerException;
 import javax.media.CannotRealizeException;
 
 import java.io.IOException;
 
 import log.LogLevel;
 import log.Logger;
 
 @SuppressWarnings("serial")
 public class TutorialPlayer extends JFrame{
 
 	private Player _player;
 	
 	public TutorialPlayer(String title){
 		super();
 		
 		JFileChooser chooser = new JFileChooser();
 		int option = chooser.showOpenDialog(null);
 		
 		if ( option == JFileChooser.APPROVE_OPTION ){
 			
 			URL url;
 			try {
 				// url = chooser.getSelectedFile().toURL();
 				url = new URL("file", null, "etc/tutorials/video1.avi");
 				
 				Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
 				
 				try {
 					createPlayerAndShowComponents(url);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 			} catch (MalformedURLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			
 		}
 	}
 	
 	private void createPlayerAndShowComponents(URL url) throws IOException, NoPlayerException, CannotRealizeException{
 		
		MediaLocator ml;
		ml = new MediaLocator( "file://home/alex/repos/simgui/etc/tutorials/video1.avi" );
 		
//		_player = Manager.createRealizedPlayer( url );
		_player = Manager.createRealizedPlayer( ml );
 		
 		Component comp;
 		if ( (comp = _player.getVisualComponent()) != null ){
 			Logger.Log(LogLevel.DEBUG, "VISUAL OK");
 			add(comp, BorderLayout.CENTER);
 		}
 		if ( (comp = _player.getControlPanelComponent()) != null ){
 			add(comp, BorderLayout.SOUTH);
 			Logger.Log(LogLevel.DEBUG, "CONTROL OK");
 		}
 	}
 }
