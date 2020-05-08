 package crescendo.sheetmusic;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import crescendo.base.ErrorHandler;
 import crescendo.base.ErrorHandler.Response;
 import crescendo.base.profile.ProfileManager;
 import crescendo.base.profile.SongPreference;
 import crescendo.base.song.SongFactory;
 import crescendo.base.song.SongModel;
 
 public class SongSelectionScreen extends JPanel {
 
 	private JLabel Song1 = new JLabel("Song1");
 	private JButton LoadFile = new JButton("Load Song File");
 	private EventListener l = new EventListener();
 	private SheetMusic module;
 	private List<SongPreference> s;
 	private List<SongLabel> songsLabelsList = new ArrayList<SongLabel>();
 	private int width, height;
 
 	public SongSelectionScreen(SheetMusic module,int width, int height){
 		this.module = module;
 		this.setSize(width, height);
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		Song1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		Song1.setSize(width, height/10);
 
 		parseSongList();
 		LoadFile.addActionListener(l);
 
 		this.add(LoadFile);
 
 
 	}
 
 	private void makeLabels(int i){
 		for(int j = 0; j<i; j++){
 			songsLabelsList.add(new SongLabel());
 		}
 	}
 
 	private JPanel getPane(){
 		return this;
 	}
 
 	private void parseSongList() {
 		s = ProfileManager.getInstance().getActiveProfile().getSongPreferences();
 		makeLabels(s.size());
 		for(int i = 0; i<s.size();i++){
 			songsLabelsList.get(i).setSongPath(s.get(i).getFilePath());
 			songsLabelsList.get(i).setText(s.get(i).getSongName()+"\n"+s.get(i).getCreator());
 			songsLabelsList.get(i).addActionListener(l);
 			add(songsLabelsList.get(i));
 		}
 
 	}
 
 	private void loadSong(String filename){
 		File file = new File(filename);
 		SongModel loadedSong = null;
 		boolean loading = true;
 		try {
 			while(loading){
 				loadedSong = SongFactory.generateSongFromFile(file.getAbsolutePath());
 				loading = false;
 			}
 		} catch (IOException e1) {
 			Response response = ErrorHandler.showRetryFail("Failed to load song", "Application failed to load song: "+file.getAbsolutePath()+" would you like to try again?");
 			if(response == Response.RETRY){
 				loading = true;
 			}else{
 				loading = false;
 			}
 		}
 		if(loadedSong!=null){
 			SongPreference newSong = new SongPreference(filename, loadedSong.getTracks().size(), 0);
 			newSong.setSongName(loadedSong.getTitle());
			if(loadedSong.getCreators().size()>0)
				newSong.setCreator(loadedSong.getCreators().get(0).getName());
			else
				newSong.setCreator("");
 			boolean doAdd = true;
 			for(SongPreference p : ProfileManager.getInstance().getActiveProfile().getSongPreferences()){
 				if(p.getFilePath().equals(filename)){
 					doAdd=false;
 				}
 			}
 			if(doAdd){
 				ProfileManager.getInstance().getActiveProfile().getSongPreferences().add(newSong);
 			}
 
 			module.loadSong(loadedSong,0);
 
 		}
 	}
 
 
 	private class EventListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			if(e.getSource() instanceof SongLabel){
 				String songPath = ((SongLabel)(e.getSource())).getSongPath();
 				loadSong(songPath);
 			}
 
 			if(e.getSource() == LoadFile){
 				JFileChooser jfc = new JFileChooser();
 
 				int returnVal = jfc.showOpenDialog(SongSelectionScreen.this);
 
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					File file = jfc.getSelectedFile();
 					loadSong(file.getAbsolutePath());
 				}
 
 			}
 
 		}
 
 	}
 
 	@SuppressWarnings("serial")
 	private class SongLabel extends JButton{
 
 		private String songPath;
 
 		public String getSongPath(){
 			return songPath;
 		}
 
 		public void setSongPath(String p){
 			songPath = p;
 		}
 	}
 
 
 }
