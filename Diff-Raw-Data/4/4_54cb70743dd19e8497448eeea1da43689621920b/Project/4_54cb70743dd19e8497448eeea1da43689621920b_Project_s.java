 package MoF;
 
 import amidst.Log;
 import amidst.Options;
 import amidst.map.MapObject;
 import amidst.map.MapObjectPlayer;
 import amidst.minecraft.Minecraft;
 import amidst.minecraft.MinecraftUtil;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.sql.Timestamp;
 import java.util.Date;
 
 import javax.swing.JPanel;
 
 @Deprecated //TODO: we should remove this and integrate it into Options
 public class Project extends JPanel {
 	private static final long serialVersionUID = 1132526465987018165L;
 	
 	public MapViewer map;
 	public static int FRAGMENT_SIZE = 256;
 	private Timer timer;
 	public MapObject curTarget;
 	
 	public boolean saveLoaded;
 	public SaveLoader save;
 	
 	public Project(String seed) {
 		this(stringToLong(seed));
 		Options.instance.seedText = seed;
 		
 		Google.track("seed/" + seed + "/" + Options.instance.seed);
 	}
 	
 	public Project(long seed) {
 		this(seed, SaveLoader.Type.DEFAULT);
 	}
 	
 	public Project(SaveLoader file) {
 		this(file.seed, SaveLoader.genType, file);
 		
 		Google.track("seed/file/" + Options.instance.seed);
 	}
 	
 	public Project(String seed, SaveLoader.Type type) {
 		this(stringToLong(seed), type);
 		
 		Google.track("seed/" + seed + "/" + Options.instance.seed);
 	}
 	
 	public Project(long seed, SaveLoader.Type type) {
 		this(seed, type, null);
 	}
 	public Project(long seed, SaveLoader.Type type, SaveLoader saveLoader) {
 		File historyFile = new File("./history.txt");
 		if (historyFile.exists() && historyFile.isFile()) {
 			FileWriter writer = null;
 			try {
				writer = new FileWriter(historyFile);
				writer.write(new Timestamp(new Date().getTime()).toString() + " " + seed);
 			} catch (IOException e) {
 				Log.w("Unable to write to history.txt.");
 				e.printStackTrace();
 			} finally {
 				try {
 					if (writer != null)
 						writer.close();
 				} catch (IOException e) {
 					Log.w("Unable to close writer for history.txt.");
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		SaveLoader.genType = type;
 		saveLoaded = !(saveLoader == null);
 		save = saveLoader;
 		//Enter seed data:
 		Options.instance.seed = seed;
 		
 		BorderLayout layout = new BorderLayout();
 		this.setLayout(layout);
 		MinecraftUtil.createBiomeGenerator(seed, type);
 		//Create MapViewer
 		map = new MapViewer(this);
 		add(map, BorderLayout.CENTER);
 		//Debug
 		this.setBackground(Color.BLUE);
 		
 		//Timer:
 		timer = new Timer();
 		
 		timer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
 				tick();
 			}
 		}, 20, 20);
 		
 	}
 	
 	public void tick() {
 		map.repaint();
 	}
 	
 	public void dispose() {
 		map.dispose();
 		map = null;
 		timer.cancel();
 		timer = null;
 		curTarget = null;
 		save = null;
 		System.gc();
 	}
 	
 	private static long stringToLong(String seed) {
 		long ret;
 		try {
 			ret = Long.parseLong(seed);
 		} catch (NumberFormatException err) { 
 			ret = seed.hashCode();
 		}
 		return ret;
 	}
 	
 	
 	public KeyListener getKeyListener() {
 		return map;
 	}
 	public void moveMapTo(long x, long y) {
 		map.centerAt(x, y);
 	}
 }
