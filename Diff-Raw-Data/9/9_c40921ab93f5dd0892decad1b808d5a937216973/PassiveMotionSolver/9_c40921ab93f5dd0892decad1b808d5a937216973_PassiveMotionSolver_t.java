 /*
  * GRAIL Real Time Localization System
  * Copyright (C) 2011 Rutgers University and Robert Moore
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
 
 package com.owlplatform.solver.passivemotion;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.imageio.ImageIO;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.owlplatform.solver.passivemotion.gui.panels.GraphicalUserInterface;
 import com.owlplatform.solver.passivemotion.gui.panels.UserInterfaceAdapter;
 import com.owlplatform.worldmodel.Attribute;
 import com.owlplatform.worldmodel.client.ClientWorldConnection;
 import com.owlplatform.worldmodel.client.ClientWorldModelInterface;
 import com.owlplatform.worldmodel.client.Response;
 import com.owlplatform.worldmodel.client.StepResponse;
 import com.owlplatform.worldmodel.client.WorldState;
 import com.owlplatform.worldmodel.client.protocol.messages.DataResponseMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.IdSearchResponseMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.SnapshotRequestMessage;
 import com.owlplatform.worldmodel.solver.SolverWorldConnection;
 import com.owlplatform.worldmodel.solver.protocol.messages.AttributeAnnounceMessage.AttributeSpecification;
 import com.owlplatform.worldmodel.types.ByteArrayConverter;
 import com.owlplatform.worldmodel.types.DataConverter;
 import com.owlplatform.worldmodel.types.DoubleConverter;
 import com.owlplatform.worldmodel.types.StringConverter;
 import com.thoughtworks.xstream.XStream;
 
 public class PassiveMotionSolver extends Thread {
   private static final class VarianceHandler extends Thread {
 
     private final PassiveMotionSolver handler;
     private boolean keepRunning = true;
 
     public VarianceHandler(final PassiveMotionSolver handler) {
       this.handler = handler;
     }
 
     @Override
     public void run() {
       main: while (this.keepRunning) {
         log.info("Requesting RSSI variance values.");
         if (this.handler.clientWM == null) {
           log.info("Variance Handler exiting.");
 
           break;
         }
         final StepResponse rssiResponse = this.handler.clientWM
             .getStreamRequest(".*", System.currentTimeMillis(), 0,
                 "link variance");
 
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
               double value = DoubleConverter.get().decode(linkAvg.getData());
               if (this.handler.algorithm != null) {
                 this.handler.algorithm.addVariance(rxerSensor, txerSensor,
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
 
   private static final class DeviceHandler extends Thread {
 
     private final PassiveMotionSolver handler;
     private boolean keepRunning = true;
 
     public DeviceHandler(final PassiveMotionSolver handler) {
       this.handler = handler;
     }
 
     @Override
     public void run() {
       main: while (this.keepRunning) {
         log.info("Requesting locations.");
         if (this.handler.clientWM == null) {
           log.info("Device Handler exiting.");
 
           break;
         }
 
         final StepResponse rssiResponse = this.handler.clientWM
             .getStreamRequest(
                 "(region\\." + this.handler.algorithm.getRegionId() + "|"
                     + this.handler.algorithm.getRegionId() + ".*)",
                 System.currentTimeMillis(), 0, ".*");
 
         WorldState state = null;
         while (!rssiResponse.isComplete() && !rssiResponse.isError()
             && this.keepRunning) {
           try {
             state = rssiResponse.next();
 
             if (state == null) {
               break;
             }
             for (String uri : state.getIdentifiers()) {
 
               // Handle interesting regions
               if (this.handler.algorithm.getRegionId().equals(uri)) {
                 Collection<Attribute> fields = state.getState(uri);
                 if (fields == null) {
                   log.warn("No information about {} available.",
                       this.handler.algorithm.getRegionId());
                   return;
                 }
 
                 Dimension regionDimensions = new Dimension();
                 double width = 0.0;
                 double height = 0.0;
                 String mapURL = null;
                 for (Attribute field : fields) {
                   if ("location.maxx".equals(field.getAttributeName())) {
                     width = ((Double) DataConverter.decode(
                         field.getAttributeName(), field.getData()));
                     continue;
                   } else if ("location.maxy".equals(field.getAttributeName())) {
                     height = ((Double) DataConverter.decode(
                         field.getAttributeName(), field.getData()));
                     continue;
                   } else if ("image.url".equals(field.getAttributeName())) {
                     mapURL = (String) DataConverter.decode(
                         field.getAttributeName(), field.getData());
                     continue;
                   }
                 }
                 regionDimensions.setSize(width, height);
                 this.handler.setRegionImageUri(mapURL);
               }
 
               // Check if this is a transmitter
               if (uri.indexOf("transmitter") != -1) {
                 Transmitter txer = new Transmitter();
                 Collection<Attribute> fields = state.getState(uri);
                 if (fields == null) {
                   log.warn("No fields available for {}.", uri);
                   return;
                 }
                 for (Attribute field : fields) {
                   // X-coordinate
                   if ("location.x".equalsIgnoreCase(field.getAttributeName())) {
                     Double xPos = (Double) DataConverter.decode(
                         field.getAttributeName(), field.getData());
                     txer.setxLocation(xPos.floatValue());
                   } else if ("location.y".equalsIgnoreCase(field
                       .getAttributeName())) {
                     Double yPos = (Double) DataConverter.decode(
                         field.getAttributeName(), field.getData());
                     txer.setyLocation(yPos.floatValue());
                   } else if ("id".equalsIgnoreCase(field.getAttributeName())) {
                     Long id = (Long) DataConverter.decode(
                         field.getAttributeName(), field.getData());
                     // FIXME: Shouldn't hard-code, where do we consolidate the
                     // device ID size?
                     String deviceId = StringConverter.get()
                         .decode(field.getData()).substring(1);
 
                     txer.setDeviceId(deviceId);
                   } else if ("region_uri".equalsIgnoreCase(field
                       .getAttributeName())) {
                     String region = (String) DataConverter.decode(
                         field.getAttributeName(), field.getData());
                     txer.setRegionUri(region);
                   }
                 }
                 log.info("Added {} ", txer);
                 this.handler.algorithm.addTransmitter(txer);
               } else if (uri.indexOf("receiver") != -1) {
                 Receiver rxer = new Receiver();
 
                 Collection<Attribute> fields = state.getState(uri);
                 if (fields == null) {
                   log.warn("No fields available for {}.", uri);
                   return;
                 }
                 for (Attribute field : fields) {
                   // X-coordinate
                   if ("location.x".equalsIgnoreCase(field.getAttributeName())) {
                     Double xPos = DoubleConverter.get().decode(field.getData());
                     rxer.setxLocation(xPos.floatValue());
                   } else if ("location.y".equalsIgnoreCase(field
                       .getAttributeName())) {
                     Double yPos = DoubleConverter.get().decode(field.getData());
                     rxer.setyLocation(yPos.floatValue());
                   } else if ("id".equalsIgnoreCase(field.getAttributeName())) {
                     String deviceId = StringConverter.get()
                         .decode(field.getData()).substring(1);
                     rxer.setDeviceId(deviceId);
                   } else if ("region.uri".equalsIgnoreCase(field
                       .getAttributeName())) {
                     String region = StringConverter.get().decode(
                         field.getData());
                   }
                 }
 
                 log.info("Added {}", rxer);
                 this.handler.algorithm.addReceiver(rxer);
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
 
   private static final Logger log = LoggerFactory
       .getLogger(PassiveMotionSolver.class);
 
   /**
    * The name of the attribute this solver will produce.
    */
   public static final String GENERATED_ATTRIBUTE_NAME = "passive motion.tile";
 
   /**
    * The name of this solver.
    */
   public static final String SOLVER_ORIGIN_STRING = "java.solver.passive_motion.v2";
 
   /**
    * How often to send solutions (if they are available).
    */
   public static final long UPDATE_FREQUENCY = 1000l;
 
   /**
    * For producing the passive motion results.
    */
   PassiveMotionAlgorithm algorithm;;
 
   /**
    * For displaying results to the user in realtime (optional).
    */
   protected UserInterfaceAdapter userInterface = null;
 
   /**
    * URL of the region image.
    */
   protected String regionImageUrl = null;
 
   /**
    * Image of the region (for the GUI).
    */
   protected BufferedImage regionImage = null;
 
   /**
    * Connection to the world model as a solver.
    */
   protected final SolverWorldConnection solverWM = new SolverWorldConnection();
 
   /**
    * Connection to the world model as a client.
    */
   protected final ClientWorldConnection clientWM = new ClientWorldConnection();
 
   /**
    * Handler for processing variance values.
    */
   protected VarianceHandler varianceHandler;
 
   /**
    * Handler for region, transmitters, and receivers.
    */
   protected DeviceHandler deviceHandler;
 
   /**
    * Accepts 4 required parameters and launches a new solver thread.
    * 
    * @param args
    *          world model host, solver port, client port, region name, algorithm
    *          config.
    */
   public static void main(String[] args) {
 
     if (args.length < 5) {
       printUsageInfo();
       return;
     }
 
     XStream xstream = new XStream();
 
     AlgorithmConfig config = (AlgorithmConfig) xstream
         .fromXML(new File(args[4]));
 
     PassiveMotionSolver solver = new PassiveMotionSolver(args[0],
         Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], config);
 
     if (args.length > 4) {
       for (int i = 4; i < args.length; ++i) {
         if (args[i].equals("--gui")) {
           solver.setUserInterface(new GraphicalUserInterface());
         }
       }
     }
 
     solver.start();
   }
 
   public PassiveMotionSolver(String wmHost, int solverPort, int clientPort,
       String region, AlgorithmConfig config) {
 
     this.solverWM.setHost(wmHost);
     this.solverWM.setPort(solverPort);
     this.solverWM.setOriginString(SOLVER_ORIGIN_STRING);
     AttributeSpecification spec = new AttributeSpecification();
     spec.setAttributeName(GENERATED_ATTRIBUTE_NAME);
     spec.setIsOnDemand(false);
     this.solverWM.addAttribute(spec);
 
     this.clientWM.setHost(wmHost);
     this.clientWM.setPort(clientPort);
 
     // Configure the fingerprinter
     StdDevFingerprintGenerator fingerprinter = new StdDevFingerprintGenerator();
     fingerprinter.setMaxNumSamples(3);
     fingerprinter.setMaxSampleAge(5000l);
 
     this.algorithm = new PassiveMotionAlgorithm(config);
     this.algorithm.setStdDevFingerprinter(fingerprinter);
     this.algorithm.setRegionUri(region);
   }
 
   public void run() {
 
     // Connect to aggregator, distributor, and world server
     if (!this.startConnections()) {
       log.error("Unable to connect to the world model.");
       return;
     }
 
     this.retrieveRegionInfo(new String[] { "region."
         + this.algorithm.getRegionId() });
 
     this.retrieveAnchors(this.algorithm.getRegionId());
 
     if (!this.launchWorkers()) {
       this.shutdown();
       return;
     }
 
     long lastUpdateTime = 0l;
     while (true) {
       long now = System.currentTimeMillis();
       if (now - lastUpdateTime > 1000l) {
         FilteredTileResultSet resultSet = this.algorithm.generateResults();
         log.info("Generated {}", resultSet);
         if (this.userInterface != null) {
           this.userInterface.solutionGenerated(resultSet);
         }
         if (resultSet != null && resultSet.getTilesToPublish() != null
             && resultSet.getTilesToPublish().size() > 0) {
           Collection<ScoredTile> tiles = resultSet.getTilesToPublish();
 
           ByteBuffer solutionBytes = ByteBuffer.allocate(tiles.size() * 20);
           for (ScoredTile tile : tiles) {
             // X1, Y1, X2, Y2, Score
             solutionBytes.putFloat(tile.getTile().x);
             solutionBytes.putFloat(tile.getTile().y);
             solutionBytes.putFloat(tile.getTile().x + tile.getTile().width);
             solutionBytes.putFloat(tile.getTile().y + tile.getTile().height);
             solutionBytes.putFloat(tile.getScore());
           }
 
           Attribute solution = new Attribute();
           solution.setData(solutionBytes.array());
           solution.setId(this.algorithm.getRegionId());
           solution.setAttributeName(GENERATED_ATTRIBUTE_NAME);
 
           this.solverWM.updateAttribute(solution);
           log.info("Sent {}", solution);
         }
         lastUpdateTime = now;
       }
       if (500l - (now - lastUpdateTime) > 2) {
         try {
           Thread.sleep(500l - (now - lastUpdateTime));
         } catch (InterruptedException ie) {
           // Ignored
         }
       }
     }
   }
 
   protected void retrieveRegionInfo(String[] matchingUris) {
     for (String uri : matchingUris) {
       Response res = this.clientWM.getCurrentSnapshot(uri, "location\\..*",
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
               width = ((Double) DataConverter.decode(attrib.getAttributeName(),
                   attrib.getData())).doubleValue();
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
         if (this.algorithm != null) {
           this.algorithm.setRegionXMax((float) width);
           this.algorithm.setRegionYMax((float) height);
           log.info("Set region bounds: {},{}", width, height);
         }
       }
       if (imageUrlString != null) {
         BufferedImage regionImage;
         try {
 
           if (!imageUrlString.startsWith("http://")) {
             imageUrlString = "http://" + imageUrlString;
           }
 
           this.setRegionImageUri(imageUrlString);
           URL imageUrl = new URL(imageUrlString);
           URLConnection conn = imageUrl.openConnection();
           conn.setConnectTimeout(5000);
           conn.connect();
           this.regionImage = regionImage = ImageIO.read(conn.getInputStream());
           
           log.info("Set image for {}: \"{}\".", uri, imageUrl);
 
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
         Response res = this.clientWM.getCurrentSnapshot(regionName + "\\.anchor.*",
             "location\\..*", "sensor.*");
         WorldState state = res.get();
 
         success = true;
 
         for (String uri : state.getIdentifiers()) {
 
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
               Transmitter tx = new Transmitter();
               tx.setRegionUri(this.algorithm.getRegionId());
               tx.setDeviceId(sensorString);
               tx.setxLocation((float)x);
               tx.setyLocation((float)y);
               this.algorithm.addTransmitter(tx);
             } else if (uri.contains("receiver")) {
               Receiver rx = new Receiver();
               rx.setRegionUri(this.algorithm.getRegionId());
               rx.setxLocation((float)x);
               rx.setyLocation((float)y);
               rx.setDeviceId(sensorString);
               this.algorithm.addReceiver(rx);
             } else {
               return;
             }
           }
         }
 
       } catch (Exception e) {
         log.error("Couldn't retrieve location data for anchors in "
             + this.algorithm.getRegionId() + ".", e);
         try {
           Thread.sleep(250);
         } catch (InterruptedException ie) {
           // Ignored
         }
       }
     } while (!success);
 
     log.info("Loaded anchors.");
   }
 
   private boolean launchWorkers() {
 //    this.deviceHandler = new DeviceHandler(this);
 //    this.deviceHandler.start();
 
     this.varianceHandler = new VarianceHandler(this);
     this.varianceHandler.start();
 
     return true;
   }
 
   void updateReceiver() {
 
   }
 
   void updateTransmitter() {
 
   }
 
   void updateRssi() {
 
   }
 
   private void shutdown() {
     this.solverWM.disconnect();
     this.clientWM.disconnect();
   }
 
   public void setRegionImageUri(String regionImageUri) {
     if (this.userInterface == null)
       return;
     this.regionImageUrl = regionImageUri;
 
     if (this.regionImageUrl.indexOf("http://") == -1) {
       this.regionImageUrl = "http://" + this.regionImageUrl;
     }
 
     try {
       BufferedImage origImage = ImageIO.read(new URL(this.regionImageUrl));
       BufferedImage invert = PassiveMotionSolver.negative(origImage);
       this.userInterface.setBackground(invert);
 
     } catch (MalformedURLException e) {
       log.warn("Invalid region URI: {}", this.regionImageUrl);
       e.printStackTrace();
     } catch (IOException e) {
       log.warn("Could not load region URI at {}.", this.regionImageUrl);
       e.printStackTrace();
     }
 
   }
 
   public static void printUsageInfo() {
     System.out
        .println("Usage: <world model host> <solver port> <client port> <region name> <algorithm config> [--gui]");
   }
 
   /**
    * Connects to the world model as a solver and client.
    * 
    * @return {@code true} on connection success, else {@code false}.
    */
   public boolean startConnections() {
 
     if (!this.solverWM.connect(10000)) {
       log.error("Unable to connect to the world model as a solver.");
       return false;
     }
     if (!this.clientWM.connect(10000)) {
       log.error("Unable to connect to the world model as a client.");
       return false;
     }
     return true;
   }
 
   /**
    * Originally by <a
    * href="http://www.dreamincode.net/code/snippet4860.htm">erik.price of
    * dreamincode.net</a> (2011/08/04).
    * 
    * @param img
    *          image to negate.
    * @return photo negative version of the image.
    */
   public static BufferedImage negative(BufferedImage img) {
     Color col;
     for (int x = 0; x < img.getWidth(); x++) { // width
       for (int y = 0; y < img.getHeight(); y++) { // height
 
         int RGBA = img.getRGB(x, y); // gets RGBA data for the specific
         // pixel
 
         col = new Color(RGBA, true); // get the color data of the
         // specific pixel
 
         col = new Color(Math.abs(col.getRed() - 255),
             Math.abs(col.getGreen() - 255), Math.abs(col.getBlue() - 255)); // Swaps
         // values
         // i.e. 255, 255, 255 (white)
         // becomes 0, 0, 0 (black)
 
         img.setRGB(x, y, col.getRGB()); // set the pixel to the altered
         // colors
       }
     }
     return img;
   }
 
   public UserInterfaceAdapter getUserInterface() {
     return userInterface;
   }
 
   public void setUserInterface(UserInterfaceAdapter userInterface) {
     this.userInterface = userInterface;
   }
 
   // TODO: Got a search response from the world model.
   public void idSearchResponseReceived(ClientWorldModelInterface worldModel,
       IdSearchResponseMessage message) {
     // Is this the receiver search?
     String[] uriValues = message.getMatchingIds();
     if (uriValues == null || uriValues.length == 0) {
       log.error("No results returned for a URI search request.");
       System.exit(1);
     }
 
     for (String uri : uriValues) {
       SnapshotRequestMessage request = new SnapshotRequestMessage();
       request.setBeginTimestamp(0l);
       request.setEndTimestamp(System.currentTimeMillis());
       request.setIdRegex(uri);
       request.setAttributeRegexes(new String[] { ".*" });
       // TODO: Get a streaming request for transmitter/receiver locations
     }
   }
 
 }
