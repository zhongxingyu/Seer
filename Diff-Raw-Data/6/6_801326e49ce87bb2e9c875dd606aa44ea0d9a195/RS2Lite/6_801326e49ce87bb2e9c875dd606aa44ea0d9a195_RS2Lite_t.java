 /**
  * RS2Lite, the open source Runescape Launcher
  * Copyright (C) 2012 Nikki <nikki@nikkii.us>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.rs2lite;
 
 import java.applet.Applet;
 import java.awt.AWTException;
 import java.awt.BorderLayout;
 import java.awt.Image;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.Robot;
 import java.awt.TrayIcon.MessageType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.xml.bind.DatatypeConverter;
 
 import org.rs2lite.loader.AppletPanel;
 import org.rs2lite.loader.WebAppletLoader;
 import org.rs2lite.util.StreamUtils;
 import org.rs2lite.util.Utils;
 
 import tray.SystemTrayAdapter;
 import tray.SystemTrayProvider;
 import tray.TrayIconAdapter;
 
 /**
  * The main RS2Lite class
  * 
  * @author Nikki
  *
  */
 public class RS2Lite {
 	
 	/**
 	 * The application title
 	 */
 	private static final String APPLICATION_TITLE = "RS2Lite v2.0";
 
 	/**
 	 * The tray icon, used from a github repository which adds native support to
 	 * allow linux transparency
 	 * 
 	 * @see tray.TrayIconAdapter
 	 */
 	private TrayIconAdapter icon;
 	
 	/**
 	 * The JFrame
 	 */
 	private JFrame frame;
 	
 	/**
 	 * The panel which contains the RS Applet/Loading text
 	 */
 	private AppletPanel panel;
 	
 	/**
 	 * The robot instance
 	 */
 	private Robot robot;
 	
 	/**
 	 * The loader that RS is loaded on, and later for screenshots
 	 */
 	private ExecutorService clientService = Executors.newSingleThreadExecutor();
 
 	/**
 	 * The loader future
 	 */
 	private Future<Applet> loaderFuture;
 	
 	/**
 	 * The icon
 	 */
 	private Image image;
 
 	/**
 	 * Initialize the tray icon
 	 */
 	public void initTrayIcon() {
 		try {
 			image = ImageIO.read(RS2Lite.class.getResourceAsStream("/icon.png"));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		// Add uploaders from the list we loaded earlier
 		PopupMenu tray = new PopupMenu();
 		MenuItem screenshot = new MenuItem("Screenshot");
 		screenshot.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				screenshot();
 			}
 		});
 		tray.add(screenshot);
 		//Exit menu
 		MenuItem exit = new MenuItem("Exit");
 		exit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 
 		});
 		tray.add(exit);
 		SystemTrayAdapter adapter = SystemTrayProvider.getSystemTray();
 		icon = adapter.createAndAddTrayIcon(RS2Lite.class.getResource("/icon.png"), APPLICATION_TITLE,
 				tray);
 		
 	}
 	
 	/**
 	 * Take a screenshot, a VERY messy method, but it uses my website.
 	 */
 	protected void screenshot() {
 		final BufferedImage image = robot.createScreenCapture(frame.getBounds());
 		
 		clientService.execute(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					final ByteArrayOutputStream output = new ByteArrayOutputStream();
 					ImageIO.write(image, "png", output);
 					
 					//Connect to the image site
 					HttpURLConnection connection = (HttpURLConnection) new URL("http://ksnp.co/upload").openConnection();
 					connection.setDoOutput(true);
 					OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
 					writer.write("image="+URLEncoder.encode(DatatypeConverter.printBase64Binary(output.toByteArray()), "UTF-8"));
 					writer.close();
 					
 					//Read the contents
 					String url = StreamUtils.readContents(connection.getInputStream());
 					icon.displayMessage("Upload complete", "Image uploaded to "+url, MessageType.INFO);
 					Utils.setClipboard(url);
 					
 					connection.disconnect();
 				} catch(IOException e) {
 					JOptionPane.showMessageDialog(frame, "Screenshot failed due to error:\n"+e);
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Initialize the frame
 	 * 			
 	 */
 	public void init() {
 		frame = new JFrame(APPLICATION_TITLE);
 		if(image != null)
 			frame.setIconImage(image);
 		panel = new AppletPanel();
 		panel.setLayout(new BorderLayout());
 		frame.add(panel);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setVisible(true);
 		//Used to check for an applet
 		Executors.newSingleThreadExecutor().execute(new Runnable() {
 			@Override
 			public void run() {
 				while(!panel.hasApplet()) {
 					panel.paintOverlay();
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		//Initialize the robot
 		try {
 			robot = new Robot();
 		} catch (AWTException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Start the loading process on the RS applet
 	 */
 	public void load() {
 		loaderFuture = clientService.submit(new Callable<Applet>() {
 			@Override
 			public Applet call() throws Exception {
				//I think there are 169 worlds, oh wel
 				int world = new Random().nextInt(169);
				//Oops!
				if(world == 0)
					world = 1;
 				WebAppletLoader loader = new WebAppletLoader(
 						"http://world"+world+".runescape.com/,j0");
 				loader.load();
 				//Return the loaded applet
 				return loader.newApplet();
 			}
 		});
 	}
 	
 	/**
 	 * Wait for the loader to finish starting, then add the applet/repack the frame
 	 */
 	public void start() {
 		if(loaderFuture == null) {
 			throw new IllegalArgumentException("Loader is not set!");
 		}
 		try {
 			Applet applet = loaderFuture.get();
 			applet.setVisible(true);
 			//Set the applet/add it to the panel
 			panel.setApplet(applet);
 			panel.add(applet);
 			//Needed to refresh :(
 			frame.pack();
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(frame, "Unable to load RS2Lite : "+e);
 		}
 	}
 
 	/**
 	 * Main entry point
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		RS2Lite rs2lite = new RS2Lite();
 		//Start loading the applet
 		rs2lite.load();
 		//Initialize all other stuff
 		rs2lite.initTrayIcon();
 		rs2lite.init();
 		//Finally, add the Applet
 		rs2lite.start();
 	}
 
 }
