 /*
  * Signal Visualization Tools for the Owl Platform
  * Copyright (C) 2012 Robert Moore
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package com.owlplatform.sigvis;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Collection;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.owlplatform.worldmodel.Attribute;
 import com.owlplatform.worldmodel.client.ClientWorldConnection;
 import com.owlplatform.worldmodel.client.Response;
 import com.owlplatform.worldmodel.client.StepResponse;
 import com.owlplatform.worldmodel.client.WorldState;
 import com.owlplatform.worldmodel.types.ByteArrayConverter;
 import com.owlplatform.worldmodel.types.DataConverter;
 import com.owlplatform.worldmodel.types.DoubleConverter;
 
 public class ConnectionHandler {
 
   private static final Logger log = LoggerFactory
       .getLogger(ConnectionHandler.class);
 
   private static final long JOIN_TIMEOUT = 1000;
   private ClientWorldConnection wmc;
 
   private RssiHandler rssiHandler;
   private VarianceHandler varianceHandler;
   private DataCache2 cache;
 
   private String clientHost;
   private int clientPort;
   private String region;
 
   private boolean shouldConnect = false;
 
   /**
    * Creates a new connection handler without a cache or connection properties.
    * If you need to connect to a world model server, be sure to set a cache
    * first.
    */
   public ConnectionHandler() {
     super();
 
   }
 
   /**
    * Sets the cache for this connection handler. This method MUST be called
    * before any connections are started.
    * 
    * @param cache
    *          the new cache for this connection handler.
    */
   public void setCache(final DataCache2 cache) {
     this.cache = cache;
   }
 
   public void setRegion(final String region) {
     this.region = region;
 
   }
 
   /**
    * Sets the world model connection details for the client connection. The
    * region should also be set before a connection attempt is made to the world
    * model.
    * 
    * @param host
    *          the hostname or IP address of the world model server.
    * @param port
    *          the TCP port on which the world model server is listening for
    *          incoming client connections.
    */
   public void setClientConnection(String host, int port) {
     if (this.wmc != null) {
       this.disconnectAsClient();
 
     }
     this.wmc = new ClientWorldConnection();
 
     this.clientPort = port;
     this.clientHost = host;
     this.wmc.setHost(host);
     this.wmc.setPort(port);
 
   }
 
   /**
    * Attempts to connect to the configured World Model as a client in order to
    * stream data into the cache. If the cache is null, or the connection fails
    * for any reason, then {@code false} is returned.
    * 
    * @return {@code true} if the connection succeeds, or {@code false} if there
    *         are any errors.
    */
   public boolean connectAsClient() {
     if (this.cache == null) {
       log.error("No cache set.  Unable to connect.");
       return false;
     }
 
     if (this.wmc == null) {
       log.error("World Model connection is null.  Can't connect.");
       return false;
     }
 
     if (this.region == null) {
       log.error("No region specified. Can't connect.");
       return false;
     }
     log.info("Connecting to {}", this.wmc);
     if (!this.wmc.connect(0)) {
       log.error("Unable to connect. See log for details.");
       return false;
     }
     this.cache.setClone(false);
     this.shouldConnect = true;
     log.info("Connected to {}", this.wmc);
     return true;
   }
 
   /**
    * Should be called after connections are ready in order to start streaming
    * data into the cache.
    */
   public void startup() {
     this.startConnChecker();
     Thread startStreamThread = new Thread() {
       public void run() {
         log.info("Starting request threads.");
         ConnectionHandler.this.getStarted();
         log.info("Request threads started.");
       }
     };
     startStreamThread.start();
 
   }
 
   private ConnectionChecker connCheckThread;
 
   private static class ConnectionChecker extends Thread {
     private boolean keepRunning = true;
     private long interval;
     private long lastCheck = System.currentTimeMillis();
     private boolean alreadyShown = false;
     private final ConnectionHandler handler;
     private final JProgressBar progress = new JProgressBar();
     private final JFrame progressFrame = new JFrame("No Data Received");
     private static final String MSG_FMT_STRING = "No data received from World Model for %d seconds.";
     private final JButton dismissButton = new JButton("Dismiss");
     private final JLabel messageLabel = new JLabel(String.format(MSG_FMT_STRING,9999));
 
     public ConnectionChecker(final ConnectionHandler handler, final long interval) {
       super();
       
       this.progress.setIndeterminate(true);
       
       this.dismissButton.addActionListener(new ActionListener() {
         
         @Override
         public void actionPerformed(ActionEvent arg0) {
           ConnectionChecker.this.progressFrame.setVisible(false);
         }
       });
       this.dismissButton.addKeyListener(new KeyListener() {
         
         @Override
         public void keyTyped(KeyEvent arg0) {
           if(arg0.getKeyChar() == KeyEvent.VK_ENTER){
             ConnectionChecker.this.progressFrame.setVisible(false);
           }
           
         }
         
         @Override
         public void keyReleased(KeyEvent arg0) {
           // TODO Auto-generated method stub
           
         }
         
         @Override
         public void keyPressed(KeyEvent arg0) {
           // TODO Auto-generated method stub
           
         }
       });
       
       this.progress.setPreferredSize(new Dimension(320, 10));
       this.progressFrame.getContentPane().setLayout(new BorderLayout());
       this.progressFrame.getContentPane().add(
           this.messageLabel, BorderLayout.NORTH);
       
       this.progressFrame.getContentPane().add(progress, BorderLayout.CENTER);
       JPanel panel = new JPanel();
       panel.add(dismissButton);
       this.progressFrame.getContentPane().add(panel, BorderLayout.SOUTH);
       this.progressFrame.validate();
       this.progressFrame.pack();
       this.interval = interval;
       this.handler = handler;
       
 
     }
 
     @Override
     public void run() {
       while (this.keepRunning) {
         long now = System.currentTimeMillis();
         if (this.handler.shouldConnect
             && (now - this.lastCheck > this.interval)) {
           if (this.handler.cache.lastRssiUpdate < this.lastCheck) {
             int seconds = (int)(now - this.handler.cache.getLastRssiUpdate())/1000;
             this.messageLabel.setText(String.format(MSG_FMT_STRING,seconds));
             if (!this.alreadyShown && !this.progressFrame.isVisible()) {
               Dimension screenSize = Toolkit.getDefaultToolkit()
                   .getScreenSize();
 
               this.progressFrame.setLocation((int)(screenSize.getWidth() / 2
                   - this.progressFrame.getWidth() / 2), (int)(screenSize.getHeight()
                   / 2 - this.progressFrame.getHeight() / 2));
               this.progressFrame.setVisible(true);
               this.alreadyShown = true;
             }
           } else {
             this.progressFrame.setVisible(false);
             this.alreadyShown = false;
             this.lastCheck = now;
           }
         }
         try {
           Thread.sleep(250);
         } catch (InterruptedException ie) {
 
         }
 
       }
     }
 
     public void shutdown() {
       this.keepRunning = false;
     }
   }
 
   private void startConnChecker() {
     if (this.connCheckThread != null) {
       this.connCheckThread.shutdown();
       this.connCheckThread = null;
     }
     this.connCheckThread = new ConnectionChecker(this, 5000);
     this.connCheckThread.start();
   }
 
   public void disconnectAsClient() {
     this.shouldConnect = false;
     if (this.rssiHandler != null) {
       this.rssiHandler.shutdown();
       try {
         this.rssiHandler.join(JOIN_TIMEOUT);
       } catch (InterruptedException ie) {
         log.warn("Couldn't join with RSSI handler thread.");
       }
       this.rssiHandler = null;
     }
 
     if (this.varianceHandler != null) {
       this.varianceHandler.shutdown();
       try {
         this.varianceHandler.join(JOIN_TIMEOUT);
       } catch (InterruptedException ie) {
         log.warn("Couldn't join with Variance handler thread.");
       }
       this.varianceHandler = null;
     }
 
     if (this.wmc != null) {
       try {
         this.wmc.disconnect();
       } catch (Exception e) {
         log.error("Exception while disconnecting from world model.", e);
       } finally {
         this.wmc = null;
       }
     }
   }
 
   protected void getStarted() {
 
     if (this.cache == null) {
       log.error("No cache set.  Cannot start.");
       return;
     }
 
     // Set-up the initial data from the world model
     this.cache.setRegionUri(this.region);
     String[] matchingUris = this.wmc.searchId("region\\." + this.region);
     this.retrieveRegionInfo(matchingUris);
     this.retrieveAnchors(this.region);
 
     // Retrieve transient data from world model
     this.startStreams();
   }
 
   /*
    * winlab.anchor.pipsqueak.receiver.667 winlab.anchor.pipsqueak.repeater.220
    * winlab.anchor.pipsqueak.transmitter.112
    */
 
   protected void retrieveRegionInfo(String[] matchingUris) {
     for (String uri : matchingUris) {
       Response res = this.wmc.getCurrentSnapshot(uri, "location\\..*",
           "image\\.url");
       double width = 0;
       double height = 0;
       String imageUrlString = null;
       try {
         WorldState state = res.get();
         for (String stateUri : state.getIdentifiers()) {
           Collection<Attribute> attributes = state.getState(stateUri);
           for (Attribute attrib : attributes) {
             if ("location.maxx".equals(attrib.getAttributeName())) {
               width = ((Double) DataConverter.decode(
                   attrib.getAttributeName(), attrib.getData())).doubleValue();
             } else if ("location.maxy".equals(attrib.getAttributeName())) {
               height = ((Double) DataConverter.decode(
                   attrib.getAttributeName(), attrib.getData())).doubleValue();
             } else if ("image.url".equals(attrib.getAttributeName())) {
               imageUrlString = (String) DataConverter.decode(
                   attrib.getAttributeName(), attrib.getData());
             }
           }
         }
       } catch (Exception e) {
         log.error("Couldn't retrieve dimension data for {}", uri);
 
       }
 
       if (width != 0 && height != 0) {
         if (this.cache != null) {
           this.cache
               .setRegionBounds(new Rectangle2D.Double(0, 0, width, height));
           log.info("Set region bounds: {},{}", width, height);
         }
       }
       if (imageUrlString != null) {
         BufferedImage regionImage;
         try {
 
           if (!imageUrlString.startsWith("http://")) {
             imageUrlString = "http://" + imageUrlString;
           }
           if (this.cache != null) {
             this.cache.setRegionImageUrl(imageUrlString);
             URL imageUrl = new URL(imageUrlString);
             URLConnection conn = imageUrl.openConnection();
             conn.setConnectTimeout(5000);
             conn.connect();
             regionImage = ImageIO.read(conn.getInputStream());
             this.cache.setRegionImage(regionImage);
             log.info("Set image for {}: \"{}\".", uri, imageUrl);
           }
 
         } catch (MalformedURLException e) {
           log.warn("Malformed URL: {}", imageUrlString);
           e.printStackTrace();
         } catch (IOException e) {
           log.warn("Unable to load region image URL {} due to an exception.",
               imageUrlString, e);
           e.printStackTrace();
         }
       }
     }
   }
 
   protected void retrieveAnchors(final String regionName) {
     log.info("Retrieving anchor locations.");
     boolean success = false;
     do {
       try {
         Response res = this.wmc.getCurrentSnapshot(regionName + "\\.anchor.*",
             "location\\..*", "sensor.*");
         WorldState state = res.get();
 
         success = true;
        Collection<String> identifiers = state.getIdentifiers();
        for (String uri : identifiers) {
           log.debug("Checking {}", uri);
           String sensorString = null;
           BigInteger deviceId = null;
           double x = -1;
           double y = -1;
           Collection<Attribute> attribs = state.getState(uri);
           for (Attribute att : attribs) {
             if ("location.xoffset".equals(att.getAttributeName())) {
               x = ((Double) DataConverter.decode(att.getAttributeName(),
                   att.getData())).doubleValue();
             } else if ("location.yoffset".equals(att.getAttributeName())) {
               y = ((Double) DataConverter.decode(att.getAttributeName(),
                   att.getData())).doubleValue();
             } else if (att.getAttributeName().startsWith("sensor")) {
               if (att.getAttributeName().equals("sensor.mim")) {
                 continue;
               }
               byte[] id = new byte[16];
               System.arraycopy(att.getData(), 1, id, 0, 16);
               sensorString = ByteArrayConverter.get().asString(id);
               deviceId = new BigInteger(sensorString.substring(2), 16);
             }
           }
 
           if (x > 0 && y > 0 && deviceId != null) {
             if (uri.indexOf("wifi") != -1) {
               // sensorString = "2."+deviceId.toString(10);
               sensorString = deviceId.toString(10);
             } else if (uri.indexOf("pipsqueak") != -1) {
               // sensorString = "1." + deviceId.toString(10);
               sensorString = deviceId.toString(10);
             }
 
             // HashableByteArray deviceHash = new HashableByteArray(deviceId);
             Point2D location = new Point2D.Double(x, y);
             if (uri.contains("transmitter")) {
               this.cache.addFiduciaryTransmitter(uri);
             } else if (uri.contains("receiver")) {
               this.cache.addReceiver(uri);
             } else {
              continue;
             }
 
             this.cache.mapSensorToUri(sensorString, uri);
             this.cache.setDeviceLocation(uri, location);
           }
         }
 
       } catch (Exception e) {
         log.error("Couldn't retrieve location data for anchors in "
             + this.region + ".", e);
         try {
           Thread.sleep(250);
         } catch (InterruptedException ie) {
           // Ignored
         }
       }
     } while (!success);
 
     log.info("Loaded anchors.");
   }
 
   protected void startStreams() {
 
     this.rssiHandler = new RssiHandler(this);
     this.rssiHandler.start();
     this.varianceHandler = new VarianceHandler(this);
     this.varianceHandler.start();
   }
 
   protected void noCacheListeners() {
     if (this.rssiHandler != null) {
       this.rssiHandler.shutdown();
       try {
         this.rssiHandler.join(JOIN_TIMEOUT);
       } catch (InterruptedException ie) {
         // Ignored
       }
     }
     if (this.varianceHandler != null) {
       this.varianceHandler.shutdown();
       try {
 
         this.varianceHandler.join(JOIN_TIMEOUT);
       } catch (InterruptedException ie) {
 
       }
     }
 
     if (this.wmc != null) {
       this.wmc.disconnect();
       log.info("Disconnected from world model.");
     }
   }
 
   private static final class RssiHandler extends Thread {
 
     private final ConnectionHandler handler;
     private boolean keepRunning = true;
 
     public RssiHandler(final ConnectionHandler handler) {
       this.handler = handler;
     }
 
     @Override
     public void run() {
       main: while (this.keepRunning) {
         log.info("Requesting average RSSI values.");
 
         if (this.handler.wmc == null) {
           log.info("RSSI Handler exiting.");
 
           break;
         }
         final StepResponse rssiResponse = this.handler.wmc.getStreamRequest(
             ".*", System.currentTimeMillis(), 0, "link average");
 
         WorldState state = null;
         while (!rssiResponse.isComplete() && !rssiResponse.isError()
             && this.keepRunning) {
           try {
             state = rssiResponse.next();
 
             if (state == null) {
               break;
             }
             for (String uri : state.getIdentifiers()) {
 
               int txSensStart = uri.indexOf('.');
               int rxSensStart = uri.lastIndexOf('.');
               String txerSensor = uri.substring(txSensStart + 1, rxSensStart);
               String rxerSensor = uri.substring(rxSensStart + 1);
               Collection<Attribute> attribs = state.getState(uri);
               if (attribs == null || !attribs.iterator().hasNext()) {
                 continue;
               }
               Attribute linkAvg = attribs.iterator().next();
               double value = DoubleConverter.get()
                   .decode(linkAvg.getData());
               if (this.handler.cache != null) {
                 this.handler.cache.addRssi(rxerSensor, txerSensor,
                     (float) value, linkAvg.getCreationDate());
               }
             }
           } catch (Exception e) {
             log.error("Exception when retrieving RSSI value.", e);
             continue main;
           }
         }
         rssiResponse.cancel();
       }
 
     }
 
     public void shutdown() {
       this.keepRunning = false;
     }
   }
 
   private static final class VarianceHandler extends Thread {
 
     private final ConnectionHandler handler;
     private boolean keepRunning = true;
 
     public VarianceHandler(final ConnectionHandler handler) {
       this.handler = handler;
     }
 
     @Override
     public void run() {
       main: while (this.keepRunning) {
         log.info("Requesting RSSI variance values.");
         if (this.handler.wmc == null) {
           log.info("Variance Handler exiting.");
 
           break;
         }
         final StepResponse rssiResponse = this.handler.wmc.getStreamRequest(
             ".*", System.currentTimeMillis(), 0, "link variance");
 
         WorldState state = null;
         while (!rssiResponse.isComplete() && !rssiResponse.isError()
             && this.keepRunning) {
           try {
             state = rssiResponse.next();
 
             if (state == null) {
               break;
             }
             for (String uri : state.getIdentifiers()) {
 
               int txSensStart = uri.indexOf('.');
               int rxSensStart = uri.lastIndexOf('.');
               String txerSensor = uri.substring(txSensStart + 1, rxSensStart);
               String rxerSensor = uri.substring(rxSensStart + 1);
               Collection<Attribute> attribs = state.getState(uri);
               if (attribs == null || !attribs.iterator().hasNext()) {
                 continue;
               }
               Attribute linkAvg = attribs.iterator().next();
               double value = DoubleConverter.get()
                   .decode(linkAvg.getData());
               if (this.handler.cache != null) {
                 this.handler.cache.addVariance(rxerSensor, txerSensor,
                     (float) value, linkAvg.getCreationDate());
               }
             }
           } catch (Exception e) {
             e.printStackTrace();
             continue main;
           }
         }
         rssiResponse.cancel();
       }
     }
 
     public void shutdown() {
       this.keepRunning = false;
     }
   }
 
   public String getClientHost() {
     return clientHost;
   }
 
   public int getClientPort() {
     return clientPort;
   }
 
   public String getRegion() {
     return region;
   }
 }
