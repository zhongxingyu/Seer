 package fi.leif.java.kindlet.sailersensor.sensordisplay;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.util.Map;
 
 import com.amazon.kindle.kindlet.ui.KLabel;
 
 import fi.leif.java.kindlet.sailersensor.Config;
 
 
 public class GpsHeadSensorDisplay extends SensorDisplay
 {
 	KLabel head;
 
 	private final String MSG_ID = "GH";
 
 	public GpsHeadSensorDisplay(Config config)
 	{
 		super(config);
 		
 		setLayout(new BorderLayout());
 
 		// Add header
 		addHeader("  GPS HEADING");
 
 		// Speed label
 		head = new KLabel("", KLabel.CENTER );
 		head.setFont(new Font(config.FONTFAMILY, Font.BOLD, config.DATA_FONT_SIZE));
 		add( head, BorderLayout.CENTER );
 	}
 
 	public void messageRetrieved(Map m) throws Exception
 	{
 		if(m.get(MSG_ID) != null)
 		{
 			Double d = (Double)m.get(MSG_ID);
 			if(d == null) return;
 			int i = new Float( Math.round( d.doubleValue() ) ).intValue();
			head.setText(getVal("",i,"\u00B0"));
 			head.repaint();
 		}
 	}
 }
