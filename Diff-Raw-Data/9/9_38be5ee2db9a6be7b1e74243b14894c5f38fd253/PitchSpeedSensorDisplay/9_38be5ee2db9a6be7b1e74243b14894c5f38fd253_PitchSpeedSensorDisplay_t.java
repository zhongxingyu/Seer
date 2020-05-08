 package fi.leif.java.kindlet.sailersensor.sensordisplay;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.util.Map;
 
 import com.amazon.kindle.kindlet.ui.KBox;
 import com.amazon.kindle.kindlet.ui.KBox.Filler;
 import com.amazon.kindle.kindlet.ui.KLabel;
 import com.amazon.kindle.kindlet.ui.KPanel;
 
 import fi.leif.java.kindlet.sailersensor.Config;
 
 
 public class PitchSpeedSensorDisplay extends SensorDisplay
 {
 	private static final long serialVersionUID = 1L;
 	KLabel pitchLabel;
 	KLabel speedLabel;
 	KLabel knLabel;
 
 	private final String MSG_PITCH_ID = "AP";
 	private final String MSG_GPS_SPEED_ID = "GS";
 
 	public PitchSpeedSensorDisplay(Config config)
 	{
 		super(config);
 		
 		setLayout(new BorderLayout());
 		
 		// Pitch
 		KPanel pitchBox = new KPanel();
 		pitchBox.setLayout(new BorderLayout());
 		addHeader("  PITCH", pitchBox);
 		pitchLabel = new KLabel("  ", KLabel.CENTER);
 		pitchLabel.setFont(new Font(config.FONTFAMILY, Font.BOLD, config.DATA_FONT_SIZE_2));
 		pitchBox.add( pitchLabel, BorderLayout.CENTER );
 		add(pitchBox, BorderLayout.WEST);
 		
 		// Center (crap solution, but will do for now)
 		KPanel center = new KPanel();
 		center.setLayout(new BorderLayout());
 		addHeader("    ",center);
 		add(center, BorderLayout.CENTER);
 		
 		// Speed
 		KPanel speedBox = new KPanel();
 		speedBox.setLayout(new BorderLayout());
 		addHeader("SPEED  ", speedBox);
 		speedLabel = new KLabel("  ", KLabel.CENTER );
 		speedLabel.setFont(new Font(config.FONTFAMILY, Font.BOLD, config.DATA_FONT_SIZE_2));
 		speedBox.add( speedLabel, BorderLayout.WEST );
 		knLabel = new KLabel(" kn ", KLabel.CENTER );
 		knLabel.setFont(new Font(config.FONTFAMILY, Font.BOLD, config.DATA_FONT_SIZE_3));
 		speedBox.add( knLabel, BorderLayout.EAST );
 		add(speedBox, BorderLayout.EAST);
 	}
 
 	public void messageRetrieved(Map m) throws Exception
 	{
 		if(m.get(MSG_PITCH_ID) != null)
 		{
 			Double d = (Double)m.get(MSG_PITCH_ID);
 			pitchLabel.setText(" " + d.intValue() + "\u00B0");
 			pitchLabel.repaint();
 		}
 		if(m.get(MSG_GPS_SPEED_ID) != null)
 		{
 			Double d = (Double)m.get(MSG_GPS_SPEED_ID);
			speedLabel.setText(d.toString());
 			speedLabel.repaint();
 		}
 	}
 }
