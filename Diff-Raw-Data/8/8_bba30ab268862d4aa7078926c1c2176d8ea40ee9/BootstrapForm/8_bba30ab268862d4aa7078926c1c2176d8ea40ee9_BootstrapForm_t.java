 package org.mcupdater;
 
 import joptsimple.*;
 import org.apache.commons.lang3.text.StrSubstitutor;
 import org.mcupdater.downloadlib.DownloadQueue;
 import org.mcupdater.downloadlib.Downloadable;
 import org.mcupdater.downloadlib.TrackerListener;
 
 import javax.swing.*;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.*;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import static java.io.File.pathSeparator;
 
 public class BootstrapForm extends JWindow
 	implements TrackerListener
 {
 	private static final ResourceBundle config = ResourceBundle.getBundle("config"); //$NON-NLS-1$
 	
 	private static final long serialVersionUID = 1L;
	private static String bootstrapUrl;
	private static String distribution;
 	private final JProgressBar progressBar;
 	private final JLabel lblStatus;
 	private Distribution distro;
 	private static File basePath;// = new File("/home/sbarbour/Bootstrap-test");
 	private static PlatformType thisPlatform;
 	private String[] passthroughParams;
 	private static String defaultPack;
 	protected static BootstrapForm frame;
 	private static boolean debug = false;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(final String[] args) {
 		OptionParser optParser = new OptionParser();
 		optParser.allowsUnrecognizedOptions();
 		optParser.accepts("help", "Show help").forHelp();
 		optParser.formatHelpWith(new BuiltinHelpFormatter(160,3));
 		ArgumentAcceptingOptionSpec<String> bootstrapSpec = optParser.accepts("bootstrap", "Bootstrap URL").withRequiredArg().ofType(String.class).defaultsTo(config.getString("bootstrapURL"));
 		ArgumentAcceptingOptionSpec<String> distSpec = optParser.accepts("distribution", "MCUpdater distribution").withRequiredArg().ofType(String.class).defaultsTo(config.getString("distribution"));
 		ArgumentAcceptingOptionSpec<String> defaultpackSpec = optParser.accepts("defaultpack", "Default pack URL").withRequiredArg().ofType(String.class).defaultsTo(config.getString("defaultPack"));
 		ArgumentAcceptingOptionSpec<String> rootSpec = optParser.accepts("MCURoot", "Custom folder for MCUpdater").withRequiredArg().ofType(String.class).defaultsTo(config.getString("customPath"));
 		final NonOptionArgumentSpec<String> nonOpts = optParser.nonOptions();
 		optParser.accepts("debug","Show console output from MCUpdater");
 		final OptionSet options = optParser.parse(args);
 		defaultPack = options.valueOf(defaultpackSpec);
 		if (options.has("debug")) {
 			debug = true;
 		}
 		if (options.has("help")) {
 			try {
 				optParser.printHelpOn(System.out);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return;
 		}
		bootstrapUrl = bootstrapSpec.value(options);
		distribution = distSpec.value(options);
 
 		String customPath = options.valueOf(rootSpec);
 		if(System.getProperty("os.name").startsWith("Windows"))
 		{
 			basePath = new File(new File(System.getenv("APPDATA")),".MCUpdater");
 			thisPlatform = PlatformType.valueOf("WINDOWS" + System.getProperty("sun.arch.data.model"));
 		} else if(System.getProperty("os.name").startsWith("Mac"))
 		{
 			basePath = new File(new File(new File(new File(System.getProperty("user.home")),"Library"),"Application Support"),"MCUpdater");
 			thisPlatform = PlatformType.valueOf("OSX64");
 		}
 		else
 		{
 			basePath = new File(new File(System.getProperty("user.home")),".MCUpdater");
 			thisPlatform = PlatformType.valueOf("LINUX" + System.getProperty("sun.arch.data.model"));
 		}
 		if (!customPath.isEmpty()) {
 			basePath = new File(customPath);
 		}
 		final Map<String, Object> opts = new HashMap<String, Object>();
 		opts.put("bootstrapURL", options.valueOf(bootstrapSpec));
 		opts.put("distribution", options.valueOf(distSpec));
 
 		EventQueue.invokeLater(new Runnable() {
 
 			public void run() {
 				try {
 					for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 						//System.out.println(info.getName() + " : " + info.getClassName());
 						if ("Nimbus".equals(info.getName())) {
 							UIManager.setLookAndFeel(info.getClassName());
 							break;
 						}
 					}
 					if (UIManager.getLookAndFeel().getName().equals("Metal")) {
 						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 					}
 					frame = new BootstrapForm();
 					List<String> passthrough = new ArrayList<String>();
 					passthrough.addAll(options.valuesOf(nonOpts));
 					passthrough.addAll(Arrays.asList(config.getString("passthroughArgs")));
 					frame.setPassthroughParams(passthrough.toArray(new String[passthrough.size()]));
 					frame.setLocationRelativeTo( null );
 					frame.setVisible(true);
 					frame.doWork(opts);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public void setPassthroughParams(String[] args) {
 		this.passthroughParams = args;
 	}
 	
 	protected void doWork(Map<String, Object> opts) {
 // *** Debug section
 		System.out.println("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
 		System.out.println("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
 		System.out.println("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
 		System.out.println("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
 		System.out.println("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
 		System.out.println("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");
 // ***
		distro = DistributionParser.loadFromURL(bootstrapUrl, distribution, System.getProperty("java.version").substring(0,3), thisPlatform);
 		if (distro == null) {
 			JOptionPane.showMessageDialog(this, "No configuration found that matches distribution \"" + opts.get("distribution") + "\" and Java " + System.getProperty("java.version").substring(0,3),"MCU-Bootstrap",JOptionPane.ERROR_MESSAGE);
 			System.exit(-1);
 		}
 		lblStatus.setText("Downloading " + distro.getFriendlyName());
 		Collection<Downloadable> dl = new ArrayList<Downloadable>();
 		for (Library l : distro.getLibraries()) {
 			Downloadable dlEntry = new Downloadable(l.getName(),l.getFilename(),l.getMd5(),l.getSize(),l.getDownloadURLs());
 			dl.add(dlEntry);
 		}
 		DownloadQueue queue = new DownloadQueue("Bootstrap", "Bootstrap", this, dl, new File(basePath,"lib"), null);
 		queue.processQueue(new ThreadPoolExecutor(0, 1, 500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()));		
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public BootstrapForm() {
 		// setBounds(100, 100, 450, 300);
 		JPanel contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		
 		JPanel progressPanel = new JPanel();
 		progressPanel.setBorder(new EmptyBorder(3, 0, 0, 0));
 		contentPane.add(progressPanel, BorderLayout.SOUTH);
 		progressPanel.setLayout(new BorderLayout(0, 0));
 		
 		JPanel primaryProgress = new JPanel();
 		progressPanel.add(primaryProgress, BorderLayout.CENTER);
 		primaryProgress.setLayout(new BorderLayout(0, 0));
 		
 		progressBar = new JProgressBar();
 		progressBar.setStringPainted(true);
 		progressBar.setMaximum(10000);
 		primaryProgress.add(progressBar, BorderLayout.CENTER);
 		
 		lblStatus = new JLabel();
 		primaryProgress.add(lblStatus, BorderLayout.SOUTH);
 		
 		JPanel logoPanel = new JPanel() {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 8686753828984892019L;
 			final ImageIcon image = new ImageIcon(BootstrapForm.class.getResource("/bg_main.png"));
 			
 			@Override
 			protected void paintComponent(Graphics g) {
 				Image source = this.image.getImage();
 				int w = source.getWidth(null);
 				int h = source.getHeight(null);
 				BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
 				Graphics2D g2d = (Graphics2D)image.getGraphics();
 				g2d.drawImage(source, 0, 0, null);
 				g2d.dispose();
 		        int width = getWidth();  
 		        int height = getHeight();  
 		        int imageW = image.getWidth(this);  
 		        int imageH = image.getHeight(this);  
 		   
 		        // Tile the image to fill our area.  
 		        for (int x = 0; x < width; x += imageW) {  
 		            for (int y = 0; y < height; y += imageH) {  
 		                g.drawImage(image, x, y, this);  
 		            }  
 		        }  
 			}
 			
 			
 		};
 		contentPane.add(logoPanel, BorderLayout.CENTER);
 		logoPanel.setLayout(new BorderLayout(0, 0));
 		
 		JLabel lblLogo = new JLabel("");
 		lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
 		lblLogo.setIcon(new ImageIcon(BootstrapForm.class.getResource("/mcu-logo-new.png")));
 		logoPanel.add(lblLogo, BorderLayout.CENTER);
 		
 		setSize(480, 250);
 	}
 
 	@Override
 	public void onQueueFinished(DownloadQueue queue) {
 		if (queue.getFailedFileCount() > 0) {
 			lblStatus.setText("Failed!");
 			StringBuilder msg = new StringBuilder("Failed to download:\n");
 			for (Downloadable entry : queue.getFailures()) {
 				msg.append("   ").append(entry.getFilename()).append("\n");
 			}
 			JOptionPane.showMessageDialog(this, msg.toString(),"MCU-Bootstrap",JOptionPane.ERROR_MESSAGE);
 			System.exit(-2);
 		} else {
 			lblStatus.setText("Finished!");
 			StringBuilder sbClassPath = new StringBuilder();
 			if (System.getProperty("os.name").startsWith("Mac")) {
 				sbClassPath.append(pathSeparator).append(".");
 			}
 			for (Library lib : distro.getLibraries()){
 				if (lib.getFilename().endsWith("jar")) {
 					sbClassPath.append(pathSeparator).append((new File(new File(basePath, "lib"), lib.getFilename())).getAbsolutePath());
 				}
 			}
 			sbClassPath.append(pathSeparator).append(System.getProperty("java.home")).append(File.separator).append("lib/jfxrt.jar");
 			try {
 				String javaBin = "java";
 				File binDir;
 				if (System.getProperty("os.name").startsWith("Mac")) {
 					binDir = new File(new File(System.getProperty("java.home")), "Commands");
 				} else {
 					binDir = new File(new File(System.getProperty("java.home")), "bin");
 				}
 				if( binDir.exists() ) {
 					javaBin = (new File(binDir, "java")).getAbsolutePath();
 				}
 				List<String> args = new ArrayList<String>();
 				args.add(javaBin);
 				args.add("-Djavafx.verbose=true");
 				if (System.getProperty("os.name").toUpperCase().equals("MAC OS X")) {
 					//args.add("-XstartOnFirstThread");
 					args.add("-Xdock:name=" + distro.getFriendlyName());
 					args.add("-Xdock:icon=" + (new File(new File(basePath, "lib"), "mcu-icon.icns")).getAbsolutePath());
 				}
 				args.add("-cp");
 				args.add(sbClassPath.toString().substring(1));
 				args.add(distro.getMainClass());
 				Map<String,String> fields = new HashMap<String,String>();
 				StrSubstitutor fieldReplacer = new StrSubstitutor(fields);
 				fields.put("defaultPack", defaultPack);
 				fields.put("MCURoot", basePath.getAbsolutePath());
 				//if (distro.getParams() != null) { args.addAll(Arrays.asList(fieldReplacer.replace(distro.getParams()).split(" ")));}
 				if (distro.getParams() != null) {
 					String[] fieldArr = distro.getParams().split(" ");
 					for (int i = 0; i < fieldArr.length; i++) {
 						fieldArr[i] = fieldReplacer.replace(fieldArr[i]);
 					}
 					args.addAll(Arrays.asList(fieldArr));					
 				}
 
 				args.addAll(Arrays.asList(this.passthroughParams));
 				String[] params = args.toArray(new String[args.size()]);
 				if (!debug) {
 					Process p = Runtime.getRuntime().exec(params);
 					if (p != null) {
 						Thread.sleep(5000);
 						System.exit(0);
 					}
 				} else {
 					for (String s : args) {
 						System.out.print((s.contains(" ") ? ("\"" + s + "\"") : s) + " ");
 					}
 					System.out.print("\n");
 					ProcessBuilder pb = new ProcessBuilder(params);
 					pb.redirectErrorStream(true);
 					Process p = pb.start();
 					Thread.sleep(2000);
 					frame.setVisible(false);
 					if (p != null) {
 						String line;
 						BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						while ((line = input.readLine()) != null) {
 							System.out.println(line);
 						}
 						System.exit(0);
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void onQueueProgress(DownloadQueue queue) {
 		lblStatus.setText("Downloading: " + queue.getName());
 		progressBar.setValue((int) (queue.getProgress()*10000.0F));
 	}
 
 	@Override
 	public void printMessage(String msg) {
 		System.out.println(msg);
 	}
 
 }
