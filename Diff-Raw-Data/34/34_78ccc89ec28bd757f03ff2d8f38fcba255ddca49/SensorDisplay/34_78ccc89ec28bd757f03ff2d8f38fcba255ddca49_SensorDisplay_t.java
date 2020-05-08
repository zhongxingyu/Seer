 package fi.leif.java.kindlet.sailersensor.sensordisplay;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Font;
 import java.util.Map;
 
 import com.amazon.kindle.kindlet.ui.KLabel;
 import com.amazon.kindle.kindlet.ui.KPanel;
 
 import fi.leif.java.kindlet.sailersensor.Config;
 
 public abstract class SensorDisplay extends KPanel
 {
 	protected Config config;
 	
 	public SensorDisplay(Config config)
 	{
 		this.config = config;
 	}
 
 	protected void addHeader(String title)
 	{
 		addHeader(title, this);
 	}
 	
 	protected void addHeader(String title, KPanel parent)
 	{
 		// Add header
 		KLabel label = new KLabel(title);
 		label.setFont(new Font(config.FONTFAMILY, Font.BOLD, config.TITLE_FONT_SIZE));
 		label.setBackground(Color.BLACK);
 		label.setForeground(Color.WHITE);
 		parent.add( label, BorderLayout.NORTH );
 	}
 
        protected String getVal(String pre, double d, String post)
        {
	  return(d == -1) ? "---" : pre + Double.toString(d) + post;
	}

        protected String getVal(String pre, int i, String post)
        {
	  return(i == -1) ? "---" : pre + Integer.toString(i) + post;
	}

 	public abstract void messageRetrieved(Map msg) throws Exception;
 }
