 package mensonge.userinterface;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JSlider;
import javax.swing.SwingUtilities;
 
 import mensonge.core.tools.Utils;
 
 import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
 import uk.co.caprica.vlcj.player.MediaPlayer;
 import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
 
 public class PlayerEventListener extends MediaPlayerEventAdapter
 {
 	public static final ImageIcon IMG_ICON_PAUSE = new ImageIcon("images/Pause.png");
 	public static final ImageIcon IMG_ICON_LECTURE = new ImageIcon("images/Lecture.png");
 	public static final ImageIcon IMG_ICON_STOP = new ImageIcon("images/Stop.png");
 
 	private JSlider slider;
 	private JButton boutonLecture;
 	private JLabel labelDureeActuelle;
 	private JLabel labelDureeMax;
 
 	public PlayerEventListener(JSlider slider, JButton boutonLecture, JLabel labelDureeMax, JLabel labelDureeActuelle)
 	{
 		this.slider = slider;
 		this.boutonLecture = boutonLecture;
 		this.labelDureeActuelle = labelDureeActuelle;
 		this.labelDureeMax = labelDureeMax;
 	}
 
 	@Override
 	public void paused(MediaPlayer arg0)
 	{
 		boutonLecture.setIcon(IMG_ICON_LECTURE);
 	}
 
 	@Override
 	public void playing(MediaPlayer arg0)
 	{
 		boutonLecture.setIcon(IMG_ICON_PAUSE);
 	}
 
 	@Override
 	public void mediaChanged(MediaPlayer mediaPlayer, libvlc_media_t media, String mrl)
 	{
 		slider.setValue(0);
 		boutonLecture.setIcon(IMG_ICON_LECTURE);
 		labelDureeActuelle.setText("00:00:00");
 	}
 
 	@Override
 	public void timeChanged(MediaPlayer mediaPlayer, long newTime)
 	{
 		labelDureeActuelle.setText(Utils.getFormattedTime(newTime / 1000));
 		slider.setValue((int) newTime);
 	}
 
 	@Override
 	public void stopped(final MediaPlayer mediaPlayer)
 	{
 		boutonLecture.setIcon(IMG_ICON_LECTURE);
 		slider.setValue(0);
 		labelDureeActuelle.setText("00:00:00");
		SwingUtilities.invokeLater(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				mediaPlayer.start();
 				mediaPlayer.pause();				
 			}
		});
 	}
 
 	@Override
 	public void lengthChanged(MediaPlayer mediaPlayer, long newLength)
 	{
 		labelDureeMax.setText(Utils.getFormattedTime(mediaPlayer.getLength() / 1000));
 		slider.setMaximum((int) newLength);
 	}
 
 	@Override
 	public void finished(MediaPlayer mediaPlayer)
 	{
 		mediaPlayer.stop();
 	}
 }
