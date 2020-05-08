 package crescendo.sheetmusic;
 
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 
 import crescendo.base.AudioPlayer;
 import crescendo.base.ErrorHandler;
 import crescendo.base.song.SongModel;
 import crescendo.base.song.Track;
 
 public class TrackSelectionScreen extends JScrollPane implements ActionListener {
 	private static final long serialVersionUID=1L;
 
 	private SheetMusic module;
 	private SongModel model;
 	private JCheckBox[] active;
 	private JCheckBox[] enabled;
 	private JComboBox[] instruments;
 	
 	private JButton play1;
 	private JButton play2;
 	
 	public TrackSelectionScreen(SheetMusic module,SongModel model) {
 		this.module=module;
 		this.model=model;
 		
 		JPanel superPanel=new JPanel();
 		
 		JPanel form=new JPanel();
 		form.setAlignmentY(0);
 		form.setLayout(new GridBagLayout());
 		GridBagConstraints c=new GridBagConstraints();
 		c.gridwidth=GridBagConstraints.REMAINDER;
 		c.weightx=0;
 		c.weighty=0;
 		c.ipadx=5;
 		c.fill=GridBagConstraints.NONE;
 		
 		JLabel infoLabel=new JLabel("Song: "+model.getTitle());
 		infoLabel.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));
 		form.add(infoLabel,c);
 		
 		c.gridwidth=GridBagConstraints.RELATIVE;
 		form.add(new JLabel("Select options below, then click Play."),c);
 		
 		c.gridwidth=GridBagConstraints.REMAINDER;
 		play1=new JButton("Play");
 		play1.addActionListener(this);
 		c.fill=GridBagConstraints.HORIZONTAL;
 		form.add(play1,c);
 		
 		c.fill=GridBagConstraints.NONE;
 		int size=model.getTracks().size();
 		active=new JCheckBox[size];
 		enabled=new JCheckBox[size];
 		instruments=new JComboBox[size];
		String[] isn=new String[AudioPlayer.instrumentList.length];
 		for (int i=0; i<isn.length && i<128; i++)
 		{
			isn[i]=AudioPlayer.instrumentList[i].getName();
 		}
 		
 		for (int i=0; i<size; i++)
 		{
 			c.gridwidth=GridBagConstraints.REMAINDER;
 			c.fill=GridBagConstraints.HORIZONTAL;
 			form.add(new JSeparator(JSeparator.HORIZONTAL),c);
 		
 			Track track=model.getTracks().get(i);
 			c.gridwidth=1;
 			c.fill=GridBagConstraints.NONE;
 			
 			JLabel title=new JLabel("Track " +(i+1)+": "+track.getName());
 			title.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
 			form.add(title,c);
 			
 			active[i]=new JCheckBox("I will play this track");
 			active[i].setSelected(i==0);
 			active[i].addActionListener(this);
 			form.add(active[i],c);
 			
 			enabled[i]=new JCheckBox("Play this track's audio");
 			enabled[i].setSelected(i!=0);
 			form.add(enabled[i],c);
 			
 			c.gridwidth=GridBagConstraints.REMAINDER;
 			instruments[i]=new JComboBox(isn);
 			instruments[i].setSelectedIndex(track.getVoice());
 			form.add(instruments[i],c);
 		}
 		
 		play2=new JButton("Play");
 		play2.addActionListener(this);
 		c.fill=GridBagConstraints.HORIZONTAL;
 		c.gridwidth=GridBagConstraints.REMAINDER;
 		form.add(play2,c);
 		
 		superPanel.add(form);
 		this.setViewportView(superPanel);
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource()==play1 || e.getSource()==play2)
 		{
 			// check if active track checkboxes haven't been somehow thwarted
 			int activetrack=-1;
 			List<Track> activeTracks=new LinkedList<Track>();
 			List<Track> audioTracks=new LinkedList<Track>();
 			for (int i=0; i<active.length; i++)
 			{
 				if (active[i].isSelected())
 				{
 					activetrack=i; // TODO remove this for multiple tracks
 					activeTracks.add(model.getTracks().get(i));
 				}
 			}
 			
 			if (activetrack<0)
 			{
 				ErrorHandler.showNotification("No Active Track","You must select a track to play yourself.");
 			}
 			else
 			{
 				// go track by track
 				List<Track> newTracks=new LinkedList<Track>();
 				for (int i=0; i<model.getTracks().size(); i++)
 				{
 					Track track=model.getTracks().get(i);
 					if (enabled[i].isSelected() || activeTracks.contains(track))
 					{
 						if (enabled[i].isSelected())
 						{
 							audioTracks.add(track);
 						}
 						track.setVoice(instruments[i].getSelectedIndex());
 						newTracks.add(track);
 					}
 				}
 				
 				SongModel newModel=new SongModel(newTracks,model.getTitle(),model.getCreators(),model.getLicense(),model.getBPM(),model.getTimeSignature(),model.getKeySignature());
 				
 				module.loadSong(newModel,activeTracks,audioTracks);
 			}
 		}
 		else // has to be one of the active checkboxes
 		{
 			for (int i=0; i<active.length; i++)
 			{
 				if (e.getSource()==active[i])
 				{
 					enabled[i].setSelected(!active[i].isSelected());
 				}
 			}
 		}
 	}
 }
