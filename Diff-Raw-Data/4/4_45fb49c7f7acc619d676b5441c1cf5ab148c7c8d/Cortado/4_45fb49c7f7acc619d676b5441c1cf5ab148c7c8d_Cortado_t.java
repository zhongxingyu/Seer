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
 import java.awt.*;
 import java.awt.event.*;
 import java.net.*;
 import java.util.*;
 import com.fluendo.utils.*;
 import com.fluendo.jst.*;
 
 public class Cortado extends Applet implements Runnable, MouseMotionListener,
         MouseListener, ComponentListener, BusHandler, StatusListener, ActionListener {
     private static final long serialVersionUID = 1L;
 
     private Cortado cortado;
     private CortadoPipeline pipeline;
 
     private String urlString;
     private boolean audio;
     private boolean video;
     private boolean showSpeaker;
     private boolean keepAspect;
     private boolean autoPlay;
     private int bufferSize;
     private String userId;
     private String password;
     private int bufferLow;
     private int bufferHigh;
     private int debug;
     private double duration;
 
     private boolean statusRunning;
     private Thread statusThread;
     public Status status;
     private int statusHeight = 20;
     private boolean inStatus;
     private boolean isBuffering;
     private int desiredState;
 
     private boolean isEOS;
     private boolean isError;
 
     private static final String[] autoBoolVals = { "auto", "true", "false" };
     private static final int BOOL_AUTO = 0;
     private static final int BOOL_TRUE = 1;
     private static final int BOOL_FALSE = 2;
     private int seekable;
     private int live;
 
     private int showStatus;
     private static final String[] showStatusVals = { "auto", "show", "hide" };
     private static final int STATUS_AUTO = 0;
     private static final int STATUS_SHOW = 1;
     private static final int STATUS_HIDE = 2;
     private int hideTimeout;
     private int hideCounter;
     private boolean mayHide;
 
     private PopupMenu menu;
 
     private Hashtable params = new Hashtable();
 
     private Configure configure;
 
     private Dimension appletDimension;
 
     public String getAppletInfo() {
         return "Title: Fluendo media player \nAuthor: Wim Taymans \nA Java based network multimedia player.";
     }
     public String getRevision() {
         return "$Revision: 4170 $";
     }
 
     public String[][] getParameterInfo() {
         String[][] info = {
                 { "url", "URL", "The media file to play" },
                 { "seekable", "enum",
                         "Can you seek in this file (auto|true|false) (default auto)" },
                 { "live", "enum",
                         "Is this a live stream (disabled PAUSE) (auto|true|false) (default auto)" },
                 { "duration", "float",
                         "Total duration of the file in seconds (default unknown)" },
                 { "audio", "boolean", "Enable audio playback (default true)" },
                 { "video", "boolean", "Enable video playback (default true)" },
                 { "statusHeight", "int", "The height of the status area (default 12)" },
                 { "autoPlay", "boolean", "Automatically start playback (default true)" },
                 { "showStatus", "enum", "Show status area (auto|show|hide) (default auto)" },
                 { "hideTimeout", "int", "Timeout in seconds to hide the status area when " +
 			"showStatus is auto (default 0)" },
                 { "showSpeaker", "boolean", "Show a speaker icon when audio is available (default true)" },
                 { "keepAspect", "boolean",
                         "Use aspect ratio of video (default true)" },
                 { "bufferSize", "int",
                         "The size of the prebuffer in Kbytes (default 100)" },
                 { "bufferLow", "int", "Percent of empty buffer (default 10)" },
                 { "bufferHigh", "int", "Percent of full buffer (default 70)" },
                 { "userId", "string",
                         "userId for basic authentication (default null)" },
                 { "password", "string",
                         "password for basic authentication (default null)" },
                 { "debug", "int", "Debug level 0 - 4 (default = 3)" }, };
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
 
     public String getParam(String name, String def) {
         String result;
 
         result = (String) params.get(name);
 
         if (result == null) {
             try {
                 result = getParameter(name);
             } catch (Exception e) {
             }
         }
         if (result == null) {
             result = def;
         }
         return result;
     }
 
     public int getEnumParam(String name, String[] vals, String def) {
       int res = -1;
 
       String val = getParam (name, def);
       for (int i=0; i<vals.length;i++) {
 	 if (vals[i].equals (val)) {
            res = i;
 	   break;
 	 }
       }
       if (res != -1) 
         Debug.info("param \""+name+"\" has enum value \""+res+"\" ("+vals[res]+")");
       else
         Debug.info("param \""+name+"\" has invalid enum value");
       return res;
     }
     public String getStringParam(String name, String def) {
       String res = getParam(name, def);
       Debug.info("param \""+name+"\" has string value \""+res+"\"");
       return res;
     }
     public boolean getBoolParam(String name, boolean def) {
       boolean res;
       String defStr = def ? "true" : "false";
       String paramVal;
 
       paramVal = String.valueOf(getParam(name, defStr));
 
       res = paramVal.equals("true");
       res |= paramVal.equals("1");
 
       Debug.info("param \""+name+"\" has boolean value \""+res+"\"");
       return res;
     }
     public double getDoubleParam(String name, double def) {
       double res;
       res = Double.valueOf(getParam(name, ""+def)).doubleValue();
       Debug.info("param \""+name+"\" has double value \""+res+"\"");
       return res;
     }
     public int getIntParam(String name, int def) {
       int res;
       res = Integer.valueOf(getParam(name, ""+def)).intValue();
       Debug.info("param \""+name+"\" has int value \""+res+"\"");
       return res;
     }
 
     public void shutDown(Throwable error) {
         Debug.log(Debug.INFO, "shutting down: reason: " + error.getMessage());
         error.printStackTrace();
         stop();
     }
 
     public synchronized void init() {
         cortado = this;
 
 	Debug.info("init()");
 
 	if (pipeline != null)
           stop();
 
         pipeline = new CortadoPipeline();
         configure = new Configure();
 
         urlString = getStringParam("url", null);
         seekable = getEnumParam("seekable", autoBoolVals, "auto");
         live = getEnumParam("live", autoBoolVals, "auto");
         duration = getDoubleParam("duration", -1.0);
         audio = getBoolParam("audio", true);
         video = getBoolParam("video", true);
         statusHeight = getIntParam("statusHeight", 12);
         autoPlay = getBoolParam("autoPlay", true);
         showStatus = getEnumParam("showStatus", showStatusVals, "auto");
         hideTimeout = getIntParam("hideTimeout", 0);
         showSpeaker = getBoolParam("showSpeaker", true);
         keepAspect = getBoolParam("keepAspect", true);
         bufferSize = getIntParam("bufferSize", 200);
         bufferLow = getIntParam("bufferLow", 10);
         bufferHigh = getIntParam("bufferHigh", 70);
         debug = getIntParam("debug", 3);
         userId = getStringParam("userId", null);
         password = getStringParam("password", null);
 
         Debug.level = debug;
         Debug.log(Debug.INFO, "build info: " + configure.buildInfo);
         Debug.log(Debug.INFO, "revision: " + getRevision());
 
 	/* FIXME, HTTP Range returns 206, which HTTPConnection on MS JVM thinks
 	 * is a fatal error. Disable seeking for now. */
 	if (System.getProperty("java.vendor").toUpperCase().startsWith ("MICROSOFT", 0)){
 	  Debug.log (Debug.WARNING, "Found MS JVM, disable seeking.");
           seekable = BOOL_FALSE;
 	}
 
         pipeline.setUrl(urlString);
         pipeline.setUserId(userId);
         pipeline.setPassword(password);
         pipeline.enableAudio(audio);
         pipeline.enableVideo(video);
         pipeline.setBufferSize(bufferSize);
         pipeline.setBufferLow(bufferLow);
         pipeline.setBufferHigh(bufferHigh);
 
 	URL documentBase;
 	try {
 	  documentBase = getDocumentBase();
           Debug.log(Debug.INFO, "Document base: " + documentBase);
 	}
 	catch (Throwable t) {
           documentBase = null;
 	}
         pipeline.setDocumentBase(documentBase);
         pipeline.setComponent(this);
         pipeline.getBus().addHandler(this);
 
         setBackground(Color.black);
         setForeground(Color.white);
 
         status = new Status(this);
         status.setShowSpeaker(showSpeaker);
         status.setHaveAudio(audio);
         status.setHavePercent(true);
 	/* assume live stream unless specified */
 	if (live == BOOL_FALSE)
           status.setLive(false);
 	else
           status.setLive(true);
 
 	/* assume non seekable stream unless specified */
 	if (seekable == BOOL_TRUE)
           status.setSeekable(true);
 	else
           status.setSeekable(false);
 
         status.setDuration(duration);
         inStatus = false;
         mayHide = (hideTimeout == 0);
 	hideCounter = 0;
 	if (showStatus != STATUS_HIDE) {
           status.setVisible(true);
 	}
 	else {
           status.setVisible(false);
 	}
 
         menu = new PopupMenu();
         menu.add("About...");
         menu.addActionListener(this);
         this.add(menu);
     }
 
     public void actionPerformed(ActionEvent e) {
 	String command = e.getActionCommand();
 
         if (command.equals("About...")) {
             AboutFrame about = new AboutFrame(pipeline);
             about.d.setVisible(true);
         }
     }
 
     public Graphics getGraphics() {
 	Dimension dim = getSize();
         Graphics g = super.getGraphics();
 
         if (status != null && status.isVisible()) {
             g.setClip(0, 0, dim.width, dim.height - statusHeight);
         } else {
             g.setClip(0, 0, dim.width, dim.height);
         }
         return g;
     }
 
     public void componentHidden(ComponentEvent e) {
     }
     public void componentMoved(ComponentEvent e) {
     }
     public void componentResized(ComponentEvent e) {
       /* reset cached dimension */
       appletDimension = null;
     }
     public void componentShown(ComponentEvent e) {
     }
 
     public Dimension getSize() {
         if (appletDimension == null)
             appletDimension = super.getSize();
 
         return appletDimension;
     }
 
     public void update(Graphics g) {
         paint(g);
     }
 
     public void run() {
         try {
             realRun();
         } catch (Throwable t) {
             shutDown(t);
         }
     }
 
     private void realRun() {
         Debug.log(Debug.INFO, "entering status thread");
         while (statusRunning) {
             try {
 		long now;
 		
 		now = pipeline.getPosition() / Clock.SECOND;
                 status.setTime(now);
 
                 Thread.sleep(1000);
 
 		if (hideCounter > 0) {
 	          hideCounter--;
 		  if (hideCounter == 0) {
 	            mayHide = true;
                     setStatusVisible(false, false);
 		  }
 		}
             } catch (Exception e) {
                 if (statusRunning) {
                     Debug.log(Debug.ERROR, "Exception in status thread:");
                     e.printStackTrace();
 		}
             }
         }
         Debug.log(Debug.INFO, "exit status thread");
     }
 
     public void paint(Graphics g) {
 	Dimension dim = getSize();
 
         int dwidth = dim.width;
         int dheight = dim.height;
 
         /* sometimes dimension is wrong */
         if (dwidth <= 0 || dheight < statusHeight) {
 	  appletDimension = null;
 	  Debug.log (Debug.WARNING,
                "paint aborted: appletDimension wrong; dwidth " + dwidth
                + ", dheight " + dheight + ", statusHeight " + statusHeight);
 	  return;
 	}
 	
         if (status != null && status.isVisible()) {
             status.setBounds(0, dheight - statusHeight, dwidth, statusHeight);
             status.paint(g);
         }
     }
 
     private void setStatusVisible(boolean b, boolean force) {
         /* no change, do nothing */
         if (status.isVisible() == b) {
             return;
 	}
 
 	/* refuse to hide when hideTimeout did not expire */
 	if (!b && !mayHide) {
             return;
 	}
 
 	if (!force) {
 	  if (showStatus == STATUS_SHOW && !b) {
               return;
 	  }
 	  if (showStatus == STATUS_HIDE && b) {
               return;
 	  }
 	}
 	/* never hide when we are in error */
 	if (isError && !b) {
             return;
 	}
           
         /* don't make invisible when the mouse pointer is inside status area */
         if (inStatus && !b) {
             return;
 	}
 
 	Debug.log (Debug.INFO, "Status: "+ (b ? "Show" : "Hide"));
         status.setVisible(b);
         repaint();
     }
 
     private boolean intersectStatus(MouseEvent e) {
 	int y = e.getY();
 	int max = getSize().height;
 	int top = max - statusHeight;
 
         inStatus = y > top && y < max;
         return inStatus;
     }
 
     public void mouseClicked(MouseEvent e) {
     }
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseExited(MouseEvent e) {
         setStatusVisible(false, false);
     }
 
     public void mousePressed(MouseEvent e) {
         if (intersectStatus(e)) {
             int y = getSize().height - statusHeight;
             e.translatePoint(0, -y);
             ((MouseListener) status).mousePressed(e);
         } else {
             if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                 menu.show(this, e.getX(), e.getY());
             }
         }
     }
 
     public void mouseReleased(MouseEvent e) {
         if (intersectStatus(e)) {
             int y = getSize().height - statusHeight;
             e.translatePoint(0, -y);
             ((MouseListener) status).mouseReleased(e);
         }
 	else {
             status.cancelMouseOperation();
 	}
     }
 
     public void mouseDragged(MouseEvent e) {
         if (intersectStatus(e)) {
             int y = getSize().height - statusHeight;
             setStatusVisible(true, false);
             e.translatePoint(0, -y);
             ((MouseMotionListener) status).mouseDragged(e);
         } else {
             setStatusVisible(false, false);
         }
     }
 
     public void mouseMoved(MouseEvent e) {
         if (intersectStatus(e)) {
             int y = getSize().height - statusHeight;
             setStatusVisible(true, false);
             e.translatePoint(0, -y);
             ((MouseMotionListener) status).mouseMoved(e);
         } else {
             setStatusVisible(false, false);
         }
     }
 
     public void handleMessage(Message msg) {
         switch (msg.getType()) {
         case Message.WARNING:
             Debug.info(msg.toString());
             break;
         case Message.ERROR:
             Debug.info(msg.toString());
 	    if (!isError) {
               status.setMessage(msg.parseErrorString());
               status.setState(Status.STATE_STOPPED);
               pipeline.setState(Element.STOP);
               setStatusVisible(true, true);
 	      isError = true;
 	    }
             break;
         case Message.EOS:
             Debug.log(Debug.INFO, "EOS: playback ended");
 	    if (!isError) {
               status.setState(Status.STATE_STOPPED);
               status.setMessage("Playback ended");
 	      isEOS = true;
               pipeline.setState(Element.STOP);
               setStatusVisible(true, false);
 	    }
             break;
         case Message.STREAM_STATUS:
             Debug.info(msg.toString());
             break;
         case Message.RESOURCE:
 	    if (!isError) {
               status.setMessage(msg.parseResourceString());
               setStatusVisible(true, false);
 	    }
             break;
         case Message.DURATION:
 	    long duration;
 
 	    duration = msg.parseDurationValue();
 
             Debug.log(Debug.DEBUG, "got duration: "+duration);
 	    if (duration != -1) {
 	      /* we got duration, we can enable automatic setting */
 	      if (seekable == BOOL_AUTO)
                 status.setSeekable(true);
 	      if (live == BOOL_AUTO)
                 status.setLive(false);
 	    }
             break;
         case Message.BUFFERING:
 	    boolean busy;
 	    int percent;
 
 	    if (isError)
 	      break;
 
 	    busy = msg.parseBufferingBusy();
 	    percent = msg.parseBufferingPercent();
 
 	    if (busy) {
 	      if (!isBuffering) {
                 Debug.log(Debug.INFO, "PAUSE: we are buffering");
 		if (desiredState == Element.PLAY)
 	          pipeline.setState(Element.PAUSE);
 		isBuffering = true;
                 setStatusVisible(true, false);
 	      }
               status.setBufferPercent(busy, percent);
 	    }
 	    else {
 	      if (isBuffering) {
                 Debug.log(Debug.INFO, "PLAY: we finished buffering");
 		if (desiredState == Element.PLAY)
 	          pipeline.setState(Element.PLAY);
 		isBuffering = false;
                 setStatusVisible(false, false);
 	      }
               status.setBufferPercent(busy, percent);
 	    }
             break;
         case Message.STATE_CHANGED:
             if (msg.getSrc() == pipeline) {
                 int old, next;
 
                 old = msg.parseStateChangedOld();
                 next = msg.parseStateChangedNext();
 
                 switch (next) {
                 case Element.PAUSE:
 		    if (!isError && !isEOS) {
                       status.setMessage("Paused");
 		    }
                     status.setState(Status.STATE_PAUSED);
                     break;
                 case Element.PLAY:
 		    if (!isError && !isEOS) {
                       status.setMessage("Playing");
                       setStatusVisible(false, false);
 		      if (!mayHide)
 		        hideCounter = hideTimeout;
 		    }
                     status.setState(Status.STATE_PLAYING);
                     break;
                 case Element.STOP:
 		    if (!isError && !isEOS) {
                       status.setMessage("Stopped");
                       setStatusVisible(true, false);
 		    }
                     status.setState(Status.STATE_STOPPED);
                     break;
                 }
             }
             break;
         default:
             break;
         }
     }
 
     public void doPause() {
       isError = false;
       isEOS = false;
       status.setMessage("Pause");
       desiredState = Element.PAUSE;
       pipeline.setState(desiredState);
     }
     public void doPlay() {
       isError = false;
       isEOS = false;
       status.setMessage("Play");
       desiredState = Element.PLAY;
       pipeline.setState(desiredState);
     }
     public void doStop() {
       status.setMessage("Stop");
       desiredState = Element.STOP;
       pipeline.setState(desiredState);
     }
 
     public void doSeek(double aPos) {
       boolean res;
       com.fluendo.jst.Event event;
 
       /* get value, convert to PERCENT and construct seek event */
       event = com.fluendo.jst.Event.newSeek(Format.PERCENT,
               (int) (aPos * 100.0 * Format.PERCENT_SCALE));
 
       /* send event to pipeline */
       res = pipeline.sendEvent(event);
       if (!res) {
           Debug.log(Debug.WARNING, "seek failed");
       }
     }
 
    public double getPlayPosition() {
      return (double) pipeline.getPosition() / Clock.SECOND;
    }

     public void newState(int aState) {
         int ret;
         switch (aState) {
         case Status.STATE_PAUSED:
             doPause();
             break;
         case Status.STATE_PLAYING:
             doPlay();
             break;
         case Status.STATE_STOPPED:
 	    doStop();
             break;
         default:
             break;
         }
     }
     public void newSeek(double aPos) {
       doSeek (aPos);
     }
 
     public synchronized void start() {
         int res;
 
 	Debug.info("Application starting");
 
         addComponentListener(this);
         addMouseListener(this);
         addMouseMotionListener(this);
         status.addStatusListener(this);
 
 	if (autoPlay) 
           desiredState = Element.PLAY;
 	else
           desiredState = Element.PAUSE;
 
         res = pipeline.setState(desiredState);
 
 	if (statusThread != null)
           throw new RuntimeException ("invalid state");
 
         statusThread = new Thread(this, "cortado-StatusThread-"+Debug.genId());
 	statusRunning = true;
         statusThread.start();
     }
 
     public synchronized void stop() {
       Debug.info("Application stopping...");
 
         status.removeStatusListener(this);
         removeMouseMotionListener(this);
         removeMouseListener(this);
         removeComponentListener(this);
 
 	statusRunning = false;
         desiredState = Element.STOP;
 	if (pipeline != null) {
           try {
             pipeline.setState(desiredState);
 	  }
           catch (Throwable e) {
 	  }
           try {
             pipeline.shutDown();
 	  }
           catch (Throwable e) {
 	  }
 	  pipeline = null;
 	}
         if (statusThread != null) {
           try {
             statusThread.interrupt();
           } catch (Throwable e) {
           }
           try {
             statusThread.join();
           } catch (Throwable e) {
           }
           statusThread = null;
 	}
 	Debug.info("Application stopped");
     }
 }
 
 /* dialog box */
 
 class AppFrame extends Frame 
     implements WindowListener { 
   public AppFrame(String title) { 
     super(title); 
     addWindowListener(this); 
   } 
   public void windowClosing(WindowEvent e) { 
     setVisible(false); 
     dispose(); 
     System.exit(0); 
   } 
   public void windowClosed(WindowEvent e) {} 
   public void windowDeactivated(WindowEvent e) {} 
   public void windowActivated(WindowEvent e) {} 
   public void windowDeiconified(WindowEvent e) {} 
   public void windowIconified(WindowEvent e) {} 
   public void windowOpened(WindowEvent e) {} 
 } 
 
 class AboutFrame extends AppFrame { 
   Dialog d; 
 
     public String getRevision() {
         return "$Revision: 4170 $";
     }
  
   public AboutFrame(CortadoPipeline pipeline) { 
     super("AboutFrame"); 
 
     Configure configure = new Configure();
     SourceInfo info = new SourceInfo();
 
     setSize(200, 100); 
     Button dbtn; 
     d = new Dialog(this, "About Cortado", false); 
     d.setVisible(true);
 
     TextArea ta = new TextArea("", 8, 40, TextArea.SCROLLBARS_NONE);
     d.add(ta);
     ta.appendText("This is Cortado " + configure.buildVersion + ".\n");
     ta.appendText("Brought to you by Wim Taymans.\n");
     ta.appendText("(C) Copyright 2004,2005,2006 Fluendo\n\n");
     ta.appendText("Built on " + configure.buildDate + "\n");
     ta.appendText("Built in " + configure.buildType + " mode.\n");
     ta.appendText("Built from SVN branch " + info.branch + ", revision " +
         info.revision + "\n");
     ta.appendText("Running on Java VM " + System.getProperty("java.version")
                   + " from " + System.getProperty("java.vendor") + "\n");
 
     if (pipeline.isAudioEnabled()) {
       if (pipeline.usingJavaX) {
         ta.appendText("Using the javax.sound backend.");
       } else {
         ta.appendText("Using the sun.audio backend.\n\n");
         ta.appendText("NOTE: you should install the Java(TM) from Sun for better audio quality.");
       }
     }
 
     d.add(dbtn = new Button("OK"), 
       BorderLayout.SOUTH); 
 
     Dimension dim = d.getPreferredSize();
     d.setSize(dim);
     dbtn.addActionListener(new ActionListener() { 
       public void actionPerformed(ActionEvent e) { 
         d.setVisible(false); 
       } 
     }); 
     d.addWindowListener(new WindowAdapter() { 
       public void windowClosing(WindowEvent e) { 
         d.setVisible(false); 
       } 
     }); 
   } 
 }
