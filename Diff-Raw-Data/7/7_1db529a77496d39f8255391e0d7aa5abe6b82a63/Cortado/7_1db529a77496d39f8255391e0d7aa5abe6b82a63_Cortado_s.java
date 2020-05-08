 /* Cortado - a video player java applet
  * Copyright (C) 2004 Fluendo S.L.
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
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Street #330, Boston, MA 02111-1307, USA.
  */
 
 package com.fluendo.player;
 
 import java.applet.*;
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.net.*;
 import java.util.*;
 import com.fluendo.utils.*;
 
 public class Cortado extends Applet implements ImageTarget,
                 PreBufferNotify,
                 Runnable, 
 		MouseMotionListener,
 		MouseListener
 {
   private static Cortado cortado;
 
   private String urlString;
   private boolean local;
   private double framerate;
   private boolean audio;
   private boolean video;
   private boolean keepAspect;
   private int bufferSize;
   private String userId;
   private String password;
   private boolean usePrebuffer;
   private PreBuffer preBuffer;
   private int bufferLow;
   private int bufferHigh;
 
   private double aspect;
 
   private Image image;
   private Thread videoThread;
   private Thread audioThread;
   private Thread mainThread;
   private Thread statusThread;
   private DataConsumer videoConsumer;
   private DataConsumer audioConsumer;
   private Demuxer demuxer;
 
   private InputStream is;
   private Clock clock;
   private boolean havePreroll;
   private Status status;
   private PopupMenu menu;
   private boolean stopping;
   private Hashtable params = new Hashtable();
   private Configure configure;
 
   private boolean useDb = true;
   private Dimension dbSize;
   private Image dbImage;
   private Graphics dbGraphics;
   private Dimension appletDimension;
 
   public String getAppletInfo() {
     return "Title: Fluendo media player \nAuthor: Wim Taymans \nA Java based network multimedia player.";
   }
 
   public String[][] getParameterInfo() {
     String[][] info = {
       {"url",         "URL",     "The media file to play"},
       {"local",       "boolean", "Is this a local file (default false)"},
       {"framerate",   "float",   "The default framerate of the video (default 5.0)"},
       {"audio",       "boolean", "Enable audio playback (default true)"},
       {"video",       "boolean", "Enable video playback (default true)"},
       {"keepAspect",  "boolean", "Use aspect ratio of video (default true)"},
       {"preBuffer",   "boolean", "Use Prebuffering (default = true)"},
       {"doubleBuffer","boolean", "Use double buffering for screen updates (default = true)"},
       {"bufferSize",  "int",     "The size of the prebuffer in Kbytes (default 100)"},
       {"bufferLow",   "int",     "Percent of empty buffer (default 10)"},
       {"bufferHigh",  "int",     "Percent of full buffer (default 70)"},
       {"userId",      "string",  "userId for basic authentication (default null)"},
       {"password",    "string",  "password for basic authentication (default null)"},
     };
     return info;
   }
 
   public void setParam(String name, String value) {
     params.put(name, value);
   }
 
   public void restart() {
     stop();
     init();
     start();
   }
 
   public String getParam(String name, String def)
   {
     String result;
 
     result = (String) params.get(name);
 
     if (result == null) {
       try {
         result = getParameter(name);
       }
       catch (Exception e) { 
       }
     }
     if (result == null) {
       result = def;
     }
     return result;
   }
 
   public static void shutdown(Throwable error) {
     System.out.println("shutting down: reason: "+error.getMessage());
     error.printStackTrace();
     cortado.stop();
   }
   
   public void init() {
     cortado = this;
 
     image = null;
     aspect = 0.0;
     is = null;
     clock = null;
     preBuffer = null;
 
     urlString = getParam("url", null);
     local = String.valueOf(getParam("local", "false")).equals("true");
     framerate = Double.valueOf(getParam("framerate", "5.0")).doubleValue();
     audio = String.valueOf(getParam("audio","true")).equals("true");
     video = String.valueOf(getParam("video","true")).equals("true");
     keepAspect = String.valueOf(getParam("keepAspect","true")).equals("true");
     usePrebuffer = String.valueOf(getParam("preBuffer","true")).equals("true");
     useDb = String.valueOf(getParam("doubleBuffer","true")).equals("true");
     bufferSize = Integer.valueOf(getParam("bufferSize","200")).intValue();
     bufferLow = Integer.valueOf(getParam("bufferLow","10")).intValue();
     bufferHigh = Integer.valueOf(getParam("bufferHigh","70")).intValue();
     userId = getParam("userId",  null);
     password = getParam("password",  null);
     configure = new Configure();
     System.out.println("build info: " + configure.buildInfo);
 
     /* FIXME: this needs to be redone in resize callbacks */
     appletDimension = getSize();
 
     setBackground(Color.black);
     setForeground(Color.white);
 
     status = new Status(this);
     status.setVisible(true);
     status.setHaveAudio (audio);
 
     menu = new PopupMenu();
     menu.add("About...");
     this.add (menu);
   }
 
   public Component getComponent() {
     return this;
   }
 
   public void update(Graphics g) {
     if (useDb) {
       if (appletDimension == null) {
         appletDimension = getSize();
       }
       boolean makeImage =
         dbImage == null ||
         dbSize.height != appletDimension.height ||
         dbSize.width != appletDimension.width;
 
       if (makeImage)
       {
         dbSize = appletDimension;
         dbImage = createImage (dbSize.width, dbSize.height);
         dbGraphics = dbImage.getGraphics();
       }
       dbGraphics.setColor (getBackground() );
       dbGraphics.fillRect (0, 0, dbSize.width, dbSize.height);
       dbGraphics.setColor (Color.black);
       dbGraphics.setFont (getFont() );
       paint (dbGraphics);
       g.drawImage (dbImage, 0, 0, this);
     }
     else {
       paint(g);
     }
   }
 
   public void run() {
     try {
       realRun();
     }
     catch (Throwable t) {
       Cortado.shutdown(t);
     }
   }
   private void realRun() {
     System.out.println("entering status thread");
     while (!stopping) {
       try {
         if (preBuffer != null) {
           int percent = (preBuffer.getFilled() * 100) /
 	           (1024 * bufferSize);
 
           if (status.isVisible()) {
             status.setBufferPercent(percent);
 	  }
 	}
 
         Thread.sleep(500);
       }
       catch (Exception e) {
         if (!stopping)
           e.printStackTrace();
       }
     }
     System.out.println("exit status thread");
   }
 
   public void paint(Graphics g) {
     if (appletDimension == null) {
       appletDimension = getSize();
     }
     int dwidth = appletDimension.width;
     int dheight = appletDimension.height;
     int x = 0, y = 0;
     int width = dwidth;
     
     if (image != null) {
       int height = dheight;
 
       /* need to get the image dimension or else the image
          will not draw for some reason */
       int imgW = image.getWidth(this);
       int imgH = image.getHeight(this);
 
       if (keepAspect) {
 	double aspectSrc = (((double)imgW) / imgH) * aspect;
 
 	height = (int) (width / aspectSrc);
 	if (height > dheight) {
 	  height = dheight;
 	  width = (int) (height * aspectSrc);
 	}
       }
       x = (dwidth - width) / 2;
       y = (dheight - height) / 2;
 
       if (status.isVisible()) {
         g.setClip(x, y, width, dheight-12-y);
         g.drawImage(image, x, y, width, height, null); 
         g.setClip(0, 0, dwidth, dheight);
       }
       else {
         g.drawImage(image, x, y, width, height, null); 
         g.setColor(Color.black);
 	int pos = Math.max (y+height, dheight-12);
         g.fillRect(x, pos, x+width, dheight);
       }
     }
     if (status != null && status.isVisible()) {
       status.setBounds(x, dheight-12, width, 12);
       status.paint(g);
     }
   }
 
   public void setImage(Image newImage, double framerate, double aspect) {
     //System.out.println("set image "+newImage);
     if (image != newImage) {
       image = newImage;
       this.framerate = framerate;
       this.aspect = aspect;
       if (!havePreroll) {
     	int dwidth = appletDimension.width;
     	int dheight = appletDimension.height;
         getGraphics().clearRect(0, 0, dwidth, dheight);
         status.setMessage("Buffering...");
       }
       repaint();
     }
   }
 
   public void preBufferNotify (int state) {
     String str = null;
 
     synchronized (preBuffer) {
       if (!havePreroll && state != STATE_BUFFER) {
         return;
       }
       switch (state) {
         case PreBufferNotify.STATE_BUFFER:
           str = "Buffering...";
 	  status.setVisible(true);
   	  clock.pause();
 	  break;
         case PreBufferNotify.STATE_PLAYBACK:
           str = "Playing...";
 	  clock.play();
 	  status.setVisible(false);
 	  break;
         case PreBufferNotify.STATE_OVERFLOW:
 	  clock.play();
 	  break;
         default:
 	  break;
       }
     }
     if (str == null)
       return;
 
     status.setMessage(str);
   }
 
   public void mouseClicked(MouseEvent e){}
   public void mouseEntered(MouseEvent e) {}
   public void mouseExited(MouseEvent e) 
   {
     status.setVisible(false);
   }
   public void mousePressed(MouseEvent e) 
   {
     if (e.getButton() == MouseEvent.BUTTON3) {
       menu.show(this, e.getX(), e.getY());
     }
   }
   public void mouseReleased(MouseEvent e) 
   {
   }
 
   public void mouseDragged(MouseEvent e){}
   public void mouseMoved(MouseEvent e)
   {
     if (status != null) {
       if (e.getY() > appletDimension.height-12) {
         status.setVisible(true);
       }
       else {
         status.setVisible(false);
       }
     }
   }
 
   public void start() 
   {
     stopping = false;
     Plugin plugin = null;
 
     status.setMessage("Opening "+urlString+"...");
     try {
       try {
         if (local) {
           System.out.println("reading from file "+urlString);
           is = new FileInputStream (urlString);
         }
         else {
           System.out.println("reading from url "+urlString);
           URL url = new URL(urlString);
           System.out.println("trying to open "+url);
   	  URLConnection uc = url.openConnection();
 	  if (userId != null && password != null) {
 	    String userPassword = userId + ":" + password;
 	    String encoding = Base64Converter.encode (userPassword.getBytes());
 	    uc.setRequestProperty ("Authorization", "Basic " + encoding);
 	  }
 	  String mime = uc.getContentType();
 	  int extraPos = mime.indexOf(';');
           if (extraPos != -1) {
 	    mime = mime.substring(0, extraPos);
   	  }
 	  System.out.println ("got stream mime: "+mime);
 	  plugin = Plugin.makeByMime(mime);
 	  if (plugin == null) {
             status.setMessage("Unknown stream "+urlString+"...");
             return;
  	  }
           is = uc.getInputStream();
           System.out.println("opened "+url);
         }
       }
       catch (SecurityException e) {
         e.printStackTrace();
         status.setMessage("Not allowed "+urlString+"...");
         return;
       }
       catch (Exception e) {
         e.printStackTrace();
         status.setMessage("Failed opening "+urlString+"...");
         return;
       }
       status.setMessage("Loading media...");
 
       clock = new Clock();
       QueueManager.reset();
       addMouseMotionListener(this);
       addMouseListener(this);
 
       if (video) {
         videoConsumer = new VideoConsumer(clock, this, framerate);
         videoThread = new Thread(videoConsumer);
       }
       if (audio) {
         try {
 	  Class.forName("javax.sound.sampled.AudioSystem");
 	  System.out.println("using high quality javax.sound.* as audio backend");
           audioConsumer = new AudioConsumer(clock);
 	}
 	catch (ClassNotFoundException e) {
 	  System.out.println("using low quality sun.audio.* as audio backend");
           audioConsumer = new AudioConsumerSun(clock);
 	}
         audioThread = new Thread(audioConsumer);
 	//clock.setProvider(audioConsumer);
       }
 
       if (plugin == null) {
         plugin = Plugin.makeByMime("application/ogg");
       }
 
       InputStream dis;
       if (usePrebuffer) {
         preBuffer = new PreBuffer (is, 1024 * bufferSize, bufferLow, bufferHigh, this);
         dis = preBuffer;
       }
       else {
         dis = is;
       }
       demuxer = new Demuxer(dis, plugin, this, audioConsumer, videoConsumer);
       mainThread = new Thread(demuxer);
 
       statusThread = new Thread(this);
       statusThread.start();
 
       if (audio) {
         audioThread.start();
       }
       if (video) {
         videoThread.start();
       }
 
       try {
         synchronized (Thread.currentThread()) {
           mainThread.start();
         }
 
         synchronized (clock) {
           boolean ready;
 
           havePreroll = false;
           System.out.println("waiting for preroll...");
           do {
 	    ready = true;
 	    if (video) {
 	      ready &= videoConsumer.isReady();
 	    }
 	    if (audio) {
 	      ready &= audioConsumer.isReady();
 	    }
 	    if (!ready) {
 	      synchronized (this) {
                 clock.wait(100);	
 	      }
 	    }
           } while (!ready);
         }
         havePreroll = true;
 
         long timeBase = 0;
 	if (video) {
 	  timeBase = videoConsumer.getQueuedTime();
 	  System.out.println("video timeBase: "+timeBase);
 	}
 	if (audio) {
 	  timeBase = audioConsumer.getQueuedTime();
 	  System.out.println("audio timeBase: "+timeBase);
 	}
 	clock.updateAdjust(timeBase);
 	
         if (preBuffer != null) {
           synchronized (preBuffer) {
             System.out.println("consumers ready");
   	    System.out.println("preroll done, starting...");
             status.setHavePercent (usePrebuffer);
 	    preBuffer.startBuffer();
 	    if (preBuffer.isFilled()) {
 	      clock.play();
 	    }
 	    else {
 	      System.out.println("not buffered, not starting yet "+preBuffer.getFilled());
 	    }
 	  }
         }
         else {
           System.out.println("consumers ready");
   	  System.out.println("preroll done, starting...");
 	  status.setVisible(false);
 	  status.setMessage("Playing...");
   	  clock.play();
         }
       }
       catch (Exception e) {
         e.printStackTrace();
       }
     }
     catch (Throwable e) {
       e.printStackTrace();
       status.setMessage("Failed opening "+urlString+"...");
       stop();
     }
   }
 
   public void stop() {
     demuxer.stop();
     try {
       stopping = true;
       if (preBuffer != null)
         preBuffer.stop();
       if (video)
         videoConsumer.stop();
       if (audio)
         audioConsumer.stop();
     }
     catch (Exception e) {
       e.printStackTrace();
     }
     try {
       if (video)
         videoThread.interrupt();
     } catch (Exception e) { }
     try {
       if (audio)
         audioThread.interrupt();
     } catch (Exception e) { }
     try {
       mainThread.interrupt();
     } catch (Exception e) { }
     try {
       statusThread.interrupt();
     } catch (Exception e) { }
     try {
       is.close();
       if (video)
         videoThread.join();
       if (audio)
         audioThread.join();
       mainThread.join();
       statusThread.join();
     }
     catch (Exception e) {
       e.printStackTrace();
     }
   }
 
 }
