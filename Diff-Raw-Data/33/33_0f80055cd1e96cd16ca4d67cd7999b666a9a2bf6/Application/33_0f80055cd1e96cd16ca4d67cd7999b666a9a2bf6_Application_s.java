 //////////////////////////////////////////////////////////////////////
 //
 // File: Application.java
 //
 // Copyright (c) 2003-2005 TiVo Inc.
 //
 //////////////////////////////////////////////////////////////////////
 
 package com.tivo.hme.sdk;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.image.BufferedImage;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.WeakReference;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import com.tivo.core.ds.TeDict;
 import com.tivo.core.ds.TeIterator;
 import com.tivo.hme.interfaces.IApplication;
 import com.tivo.hme.interfaces.IArgumentList;
 import com.tivo.hme.interfaces.IContext;
 import com.tivo.hme.interfaces.IFactory;
 import com.tivo.hme.interfaces.IHmeConstants;
 import com.tivo.hme.interfaces.IListener;
 import com.tivo.hme.interfaces.ILogger;
 import com.tivo.hme.interfaces.ILookAheadBuffer;
 import com.tivo.hme.sdk.io.ChunkedInputStream;
 import com.tivo.hme.sdk.io.ChunkedOutputStream;
 import com.tivo.hme.sdk.io.FastOutputStream;
 import com.tivo.hme.sdk.util.Ticker;
 
 /**
  * The application base class.
  *
  * @author      Adam Doppelt
  * @author      Arthur van Hoff
  * @author      Brigham Stevens
  * @author      Jonathan Payne
  * @author      Steven Samorodin
  */
 @SuppressWarnings("unchecked")
 abstract public class Application extends StreamResource implements IApplication
 {
     final static int AUTO_FLUSH_DELAY = 500;
     private boolean DEBUG = false;
     
     /**
      * The context within which the application is running.
      */
     private IContext context;
     private ChunkedOutputStream out;
     private Factory myFactory;
 
     /**
      * The root of the onscreen view hierarchy.
      */
     private View root;
 
     /**
      * The dimensions of the screen. The screen size is always the same
      * regardless of how the application is scaled.
      */
     private int width, height;
 
     // the focus view receives key events
     private View focus;
     
     // resources
     private Map resources;
     private int rsrcID;
 
     private Thread eventThread;
 
     private FlushSentinel flusher;
     private Exception flushTrace;
     
     // buffer, for writing files
     byte buf[] = new byte[4096];
     
     static Map systemIDs;
 
     // the version of the protocol this application is speaking with
     // the HME receiver
     private int protocolVersion;
     
     private boolean applicationClosing = false;
 
     /**
      * @return the root view pane of the application.
      */
     public View getRoot()
     {
         return root;
     }
 
     /**
      * @return the width of the screen.
      */
     public int getWidth()
     {
         return width;
     }
 
     /**
      * @return the height of the screen.
      */
     public int getHeight() 
     {
         return height;
     }
 
     /**
      * @return the resource map containing all resources in use by the application.
      */
     public Map getResources()
     {
         return resources;
     }
 
     /**
      * @return the Context for this application.
      */
     public IContext getContext()
     {
         return context;
     }
     
     /**
      * @return Returns the appClosing.
      */
     public boolean isApplicationClosing() {
         return applicationClosing;
     }
     /**
      * Creates a new <code>Application</code> instance.
      */
     protected Application()
     {
         setApp(this);
         setID(ID_ROOT_STREAM);
         this.resources = new HashMap();
         this.rsrcID = ID_CLIENT;
         addResource(this);
         
         root = new View(this);
     }
 
     //
     // resource maps
     //
 
     synchronized int getNextID()
     {
         return rsrcID++;
     }
 
     void addResource(Resource rsrc)
     {
         addResource(new Integer(rsrc.getID()), rsrc);
     }
 
     void addResource(Object key, Resource rsrc)
     {
         resources.put(key, new WeakReference(rsrc));
     }
 
     Resource getResource(int id)
     {
         return getResource(new Integer(id));
     }
 
     /**
      * Get a resource by key name. The resource will be created if it does not
      * already exist.
      *
      * @link HmeObject#getResource(Object)
      * @param key Can be a String (uri, hex color, animation, or file), an awt
      * Color, an awt Image or a URL.
      * @return The resource or null if it could not be created.
      */
     public synchronized Resource getResource(Object key)
     {
         if (key == null) {
             return null;
         }
 
         if (key instanceof Resource) {
             return (Resource)key;
         }
         
         WeakReference weak = (WeakReference)resources.get(key);
         Resource rsrc = (weak == null) ? null : (Resource)weak.get();
 
         // check for removed resources that haven't been collected yet
         if (rsrc != null && rsrc.getID() == -1) {
             resources.remove(key);
             rsrc = null;
         }
         
         if (rsrc == null) {
             if (key instanceof String) {
                 rsrc = createResource((String)key);
             } else if (key instanceof URL) {
                 rsrc = createStream(((URL)key).toString());
             } else if (key instanceof Color) {
                 rsrc = createColor((Color)key);
             } else if (key instanceof Image) {
                 rsrc = createImage((Image)key);
             }
             if (rsrc != null) {
                 addResource(key, rsrc);
             }
         }
 
         if (rsrc == null) {
             if (key instanceof Integer) {
                 System.out.println(this + " warning: resource " + key + " not found.");
             } else {
                 System.out.println(this + " warning: could not make resource from: '" + key + "'");
             }
             if (DEBUG) {
                 Thread.dumpStack();
             }
         }
         
         return rsrc;
     }
 
     /**
      * Create a resource by name, but don't cache it.
      */
     Resource createResource(String name)
     {
         if (name.indexOf("://") > 0) {
             return createStream(name);
         }
 
         if (name.startsWith("0x")) {
             String rgbstr = name.substring(2);
             try {
                 int rgb = (int)Long.parseLong(rgbstr, 16);
                 return createColor(new Color(rgb, rgbstr.length() == 8));
             } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("'" + name + "' is not a valid color.");
             }
         }
 
         if (name.startsWith("*")) {
             try {
                 int comma = name.indexOf(',');
                 int duration;
                 float ease = 0;
                 if (comma != -1) {
                     duration = Integer.parseInt(name.substring(1, comma));
                     ease = Float.parseFloat(name.substring(comma + 1));
                 } else {
                     duration = Integer.parseInt(name.substring(1));
                 }
                 return createAnimation(duration, ease);
             } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("'" + name + "' is not a valid anim");
             }
         }
 
         int i = name.lastIndexOf('.');
         String ext = (i < 0) ? "unknown" : name.substring(i).toLowerCase();
         if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") ||
             ext.equals(".gif") || ext.equals(".mpg")) {
             return createImage(name);
         }
         if (ext.equals(".snd")) {
             Integer id = (Integer)systemIDs.get(name.toLowerCase());
             if (id != null) {
                 return new SoundResource(this, name, id.intValue());
             } else {
                 return createSound(name);
             }
         }
         if (ext.equals(".ttf")) {
             Integer id = (Integer)systemIDs.get(name.toLowerCase());            
             if (id != null) {
                 return new Resource.TrueTypeResource(this, name, id.intValue());
             } else {
                 return createTrueType(name);
             }
         }
 
         if (ext.equals(".font")) {
             name = name.substring(0, name.length() - ".font".length());
             int dash = name.indexOf('-');
             if (dash == -1) {
                 throw new IllegalArgumentException("'" + name + "' is not a valid font.");
             }
             int dash2 = name.indexOf('-', dash + 1);
             
             String family = name.substring(0, dash) + ".ttf";
             int size;
             int style = FONT_PLAIN;
 
             try {
                 if (dash2 != -1) {
                     size = Integer.parseInt(name.substring(dash + 1, dash2));
                     String styles = name.substring(dash2 + 1).toLowerCase();
                     if (styles.equals("bold")) {
                         style = FONT_BOLD;
                     } else if (styles.equals("italic")) {
                         style = FONT_ITALIC;
                     } else if (styles.equals("bolditalic")) {
                         style = FONT_BOLD | FONT_ITALIC;
                     } else {
                         throw new IllegalArgumentException("'" + name + "' is not a valid font.");
                     }
                 } else {
                     size = Integer.parseInt(name.substring(dash + 1));
                 }
             } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("'" + name + "' is not a valid font.");
             }
 
             return createFont(family, style, size);
         }
 
         throw new IllegalArgumentException("'" + name + "' is not a valid resource string.");
     }
 
     //
     // populate systemIDs
     //
     
     static {
         systemIDs = new HashMap();
         systemIDs.put("default.ttf",    new Integer(ID_DEFAULT_TTF));
         systemIDs.put("system.ttf",     new Integer(ID_SYSTEM_TTF));
         systemIDs.put("bonk.snd",       new Integer(ID_BONK_SOUND));
         systemIDs.put("updown.snd",     new Integer(ID_UPDOWN_SOUND));
         systemIDs.put("thumbsup.snd",   new Integer(ID_THUMBSUP_SOUND));
         systemIDs.put("thumbsdown.snd", new Integer(ID_THUMBSDOWN_SOUND));
         systemIDs.put("select.snd",     new Integer(ID_SELECT_SOUND));
         systemIDs.put("tivo.snd",       new Integer(ID_TIVO_SOUND));
         systemIDs.put("left.snd",       new Integer(ID_LEFT_SOUND));
         systemIDs.put("right.snd",      new Integer(ID_RIGHT_SOUND));
         systemIDs.put("pageup.snd",     new Integer(ID_PAGEUP_SOUND));
         systemIDs.put("pagedown.snd",   new Integer(ID_PAGEDOWN_SOUND));
         systemIDs.put("alert.snd",      new Integer(ID_ALERT_SOUND));
         systemIDs.put("deselect.snd",   new Integer(ID_DESELECT_SOUND));
         systemIDs.put("error.snd",      new Integer(ID_ERROR_SOUND));
         systemIDs.put("slowdown1.snd",  new Integer(ID_SLOWDOWN1_SOUND));
         systemIDs.put("speedup1.snd",   new Integer(ID_SPEEDUP1_SOUND));
         systemIDs.put("speedup2.snd",   new Integer(ID_SPEEDUP2_SOUND));
         systemIDs.put("speedup3.snd",   new Integer(ID_SPEEDUP3_SOUND));
     }
     
     /**
      * startup / shutdown
      * Sets the size of the root pane based on paramters or defaults.
      */
     protected void setContext(IContext context, int version)
     {
         this.context = context;
         this.protocolVersion = version;
 
         if (context.getOutputStream() instanceof ChunkedOutputStream) {
             this.out = (ChunkedOutputStream)context.getOutputStream();
         } else {
             this.out = new ChunkedOutputStream(context.getOutputStream(), IHmeConstants.TCP_BUFFER_SIZE);
         }
             
         if ( protocolVersion < VERSION_0_40 ) 
         {
             if (DEBUG) {
                 context.getLogger().log(ILogger.LOG_DEBUG, "NOT Using vStrings!");
             }
             this.out.setUseVString( false );
         }
         else {
             if (DEBUG) {
                 context.getLogger().log(ILogger.LOG_DEBUG, "Using vStrings!");
             }
             this.out.setUseVString( true );
         }            
 
         int lwidth = 640;
         int lheight = 480;
 
         String widthStr = context.getConnectionAttribute("width");
         if (widthStr != null)
         {
             lwidth = Integer.parseInt(widthStr);
         }
         String heightStr = context.getConnectionAttribute("height");
         if (heightStr != null)
         {
             lheight = Integer.parseInt(heightStr);
         }
 
         root.setRootSize(lwidth, lheight);
         root.setVisible( false );
 
         this.width = root.getWidth();
         this.height = root.getHeight();
     }
 
     /**
      * Init the application. This is the main entry point for a new application
      * and subclasses should override this method.
      * @param context provided for convenience
      */
     public void init(IContext context) throws Exception
     {
     }
 
     /**
      * Open the application. This is the main entry point from the hosting
      * environment.  Subclasses should override the init() method to do 
      * application specific initialization.
      * 
      * @param context provided for convenience
      */
     final public void open(IContext context) throws Exception {
         init(context);
         flush();
     }
     
     /**
      * Close the application. This is called when the application is exiting
      * and subclasses should override this method to do any application required
      * clean up.  Note - the context will be set to null AFTER this method is called.
      */
     public void destroy()
     {
     }
 
     /**
      * Close the application.
      */
     public final synchronized void close()
     {
         applicationClosing = true;
 
         // let subclasses do their own shutdown
         destroy();
         
         // now close up shop
         if (context != null) {
             try {
                 context.close();
             } catch (IOException e) {
             }
 
             getFactory().removeApplication(this);
             this.context = null;
 
             if (IContext.DEBUG_FLUSHES) {
                 clearFlusher();
             }
         }
     }
     
     /**
      * Acknowledge an idle notification
      * @param isHandled true iff the application is handling screen saving
      */
     public void acknowledgeIdle( boolean isHandled )
     {
         cmdReceiverAcknowledgeIdle( getID(), isHandled );
     }
 
     /**
      * Transition "forward" to a different HME application or some
      * other native user interface.  When that application exits or
      * transitions back, this application will be started again.  The
      * memento will be returned to this application on restart so that
      * it can be used to help recover its previous state.
      * @param loc a URL naming the place to which to transition.
      * @param params a dictionary containing parameters to be sent forward.
      * @param memento a bit of data to be remembered and returned when
      * this application starts back up.  The memento must be 10Kbytes
      * or smaller.
      */
     public void transitionForward(String loc, TeDict params, byte memento[])
     {
     	cmdReceiverTransition(loc, TRANSITION_FORWARD, params, memento);
     }
 
     /**
      * Transition "back" to the application that started us.
      * @param params the return parameters to send back to the
      * "caller".
      */
     public void transitionBack(TeDict params)
     {
         cmdReceiverTransition("", TRANSITION_BACK, params, null);
     }
 
     /**
      * Log something with one of the ILogger.LOG_XXX priorities.
      */
     public void log(int priority, String s)
     {
         if (context != null) {
           context.getLogger().log(priority, s);
         } else {
             System.out.println("log after close : " + s);
         }
     }
 
     /**
      * Clears the flush reminder.
      */
     private void clearFlusher()
     {
         if (flusher != null) {
             Ticker.master.remove(flusher, null);
             flusher = null;
             flushTrace = null;
         }
     }
 
     /**
      * Flushes the connection if currently connected. It may be necessary to
      * manually flush the connection if commands are written from external
      * threads.
      * @link HmeObject#flush()
      */
     public synchronized void flush()
     {
         if (context == null || out == null) {
             return;
         }
         
         try {
             if (IContext.DEBUG_FLUSHES) {
                 clearFlusher();
             }
             out.flush();
         } catch (IOException e) {
             fatalError(new HmeException("flush failed: " + e.getMessage()));
         }
     }
 
     //
     // events
     //
 
     /**
      * Set the keyboard focus to the given view.  This will cause key events to
      * be delivered directly to the view with the focus.
      *
      * @param focus The view that will receive key events first
      */
     public void setFocus(View focus)
     {
         if (this.focus != focus) {
             if (this.focus != null) {
                 this.focus.handleFocus(false);
             }
             this.focus = focus;
             if (this.focus != null) {
                 this.focus.handleFocus(true);
             }
         }
     }
 
     /**
      * Get the view that has the current keyboard focus.
      */
     public View getFocus()
     {
         return focus;
     }
 
     public Factory getFactory() {
         return myFactory;
     }
     public void setFactory(Factory fact) {
         this.myFactory = fact;
 
         Boolean debugOn = (Boolean)fact.getFactoryData().get(IFactory.HME_DEBUG_KEY);
         if (debugOn != null)
         {
         	this.DEBUG = debugOn.booleanValue();
         }
     }
     
     /**
      * <p>The low level event handler. Override handleEvent only if you are
      * interested in non-key events.
      * 
      * <p>For handling key events you should override one of the handleKeyXXXX
      * methods: {@link com.tivo.hme.sdk.HmeObject#handleKeyPress(int,
      * long)}</br> {@link com.tivo.hme.sdk.HmeObject#handleKeyRepeat(int,
      * long)}</br> {@link com.tivo.hme.sdk.HmeObject#handleKeyRelease(int,
      * long)}</br> <p>Be sure to return super.handleEvent for any events that
      * you do not consume.
      *
      * @see HmeEvent.ApplicationInfo
      * @see HmeEvent.DeviceInfo
      * @see HmeEvent.InitInfo
      * @see HmeEvent.Key
      * @see HmeEvent.ResourceInfo
      * 
      * @param event The <code>HmeEvent</code> value
      * @return true if the event was handled.
      */
     public boolean handleEvent(HmeEvent event)
     {
         switch (event.getOpCode()) {
           case EVT_APP_INFO: {
               HmeEvent.ApplicationInfo info = (HmeEvent.ApplicationInfo)event;
               if (info.getMap().get("error.code") != null) {
                   int errorCode = Integer.parseInt((String)info.getMap().get("error.code"));
                   String errorText = (String)info.getMap().get("error.text");
                   return handleApplicationError(errorCode, errorText);
               }
               if (info.getMap().get("active") != null) {
                   return handleActive("true".equals(info.getMap().get("active")));
               }
               if (info.getMap().get("ping") != null) {
                   long echo = Long.parseLong((String)info.getMap().get("ping"));
                   return handlePing(echo);
               }
               break;
           }
           case EVT_IDLE: {
               HmeEvent.Idle idle = (HmeEvent.Idle) event;
               return handleIdle( idle.isIdle() );
           }
         }
         return super.handleEvent(event);
     }
 
     /**
      * A non-fatal error occurred on the receiver while running the
      * application. Return true when the event is consumed.
      * @param errorCode one of the IHmeProtocol.APP_ERROR_XXX values
      * @param errorText the warning string sent by the receiver
      */
     public boolean handleApplicationError(int errorCode, String errorText)
     {
         System.out.println(this + " handleApplicationError(" + errorCode + "," + errorText + ")");
         return false;
     }
 
     /**
      * This application was activated or deactivated. Return true when the event
      * is consumed.
      */
     public boolean handleActive(boolean active)
     {
         getRoot().setVisible(true);
         return false;
     }
 
     /**
      * A special ping APP_INFO was received - respond with an empty key
      * event. This behavior can be used to measure the response time for an
      * application.
      */
     public boolean handlePing(long echo)
     {
         sendEvent(new HmeEvent.Key(0, 0, 0, echo));
         return true;
     }
     
     /**
      * The receiver notified us that the idle state changed.  If the application
      * wants to handle idleness, it should acknowledge receipt of this event using
      * the acknowledgeIdle() method.
      * 
      * @param isIdle true if the receiver became idle
      * @return true if event was handled
      */
     public boolean handleIdle( boolean isIdle )
     {
     	return false;
     }
 
     /* (non-Javadoc)
      * @see com.tivo.hme.hosting.IApplication#isAChunk(com.tivo.hme.hosting.ILookAheadBuffer)
      */
     public boolean isAChunk(ILookAheadBuffer buf) {
         boolean foundWholeChunk = false;
         
         byte[] bytes = new byte[2];
         
         while (!foundWholeChunk && buf.bytesAvailable() >= 2) {
             buf.peekBytes(2, bytes);
 
             int ch1 = bytes[0];
             int ch2 = bytes[1];
             
             int clen = ((ch1&0xff) << 8) + (ch2&0xff);
             
             if (clen == 0) {
                 foundWholeChunk = true;
                 buf.skipBytes(2);
             } else {
                 if (buf.bytesAvailable() >= clen + 2) {
                     buf.skipBytes(clen + 2);
                 } else {
                     // not enough bytes to skip, so we don't have
                     // a whole chunk
                     break;
                 }
             }
         }
         
         return foundWholeChunk;
     }
 
     /* (non-Javadoc)
      * @see com.tivo.hme.hosting.IApplication#handleChunk(java.io.InputStream)
      */
     public boolean handleChunk(InputStream in) 
     {
         boolean doMore = true;
         
         // flush any data that was generated from previous handling of
         // event
         flush();
 
         ChunkedInputStream chunkInStr = null;
         if (in instanceof ChunkedInputStream)
         {
             chunkInStr = (ChunkedInputStream)in;
         }
         else
         {
             chunkInStr = new ChunkedInputStream(in); //, IHmeConstants.TCP_BUFFER_SIZE);
         }
 
         if ( protocolVersion < VERSION_0_40 ) 
         {
             chunkInStr.setUseVString( false );
         }
         else {
             chunkInStr.setUseVString( true );
         }            
 
         int opcode = -1;
         try {
             opcode = (int)chunkInStr.readVInt();
         } catch (IOException e) {
             // receiver closed - ignore
         }
         if (opcode == -1) {
             doMore = false;
             return doMore;
         }
         
         HmeEvent evt = null;
         try
         {
             switch (opcode) {
               case EVT_DEVICE_INFO: evt = new HmeEvent.DeviceInfo(chunkInStr); break;
               case EVT_APP_INFO:    evt = new HmeEvent.ApplicationInfo(chunkInStr); break;
               case EVT_RSRC_INFO:   evt = new HmeEvent.ResourceInfo(chunkInStr, this); break;
               case EVT_KEY:         evt = new HmeEvent.Key(chunkInStr); break;
               case EVT_IDLE:        evt = new HmeEvent.Idle(chunkInStr); break;
               case EVT_FONT_INFO:   evt = new HmeEvent.FontInfo(chunkInStr); break;
               case EVT_INIT_INFO:   evt = new HmeEvent.InitInfo(chunkInStr); break;
             }
             chunkInStr.readTerminator();
         }
         catch (IOException e) {
             evt = null;
             e.printStackTrace();
         }
         
         if (evt == null) {
             log(ILogger.LOG_DEBUG, "unknown event opcode : " + opcode);
         }
         else
         {
             if (DEBUG) {
                 log(ILogger.LOG_DEBUG, "event " + evt);
             }
             dispatchEvent(evt);
         }
         // flush any data that was generated from handling of event
         flush();
         return doMore;
     }
     
 
     /**
      * Dispatch an event by posting it to a resource or view. Subclasses can
      * override this method to see all events before they are posted.
      */
     protected void dispatchEvent(HmeEvent event)
     {
         switch (event.getOpCode()) {
           case EVT_APP_INFO:
           case EVT_INIT_INFO:
           case EVT_RSRC_INFO:
           case EVT_FONT_INFO:
             HmeObject ref = getResource(event.getID());
             if (ref != null) {
                 ref.postEvent(event);
             } else {
                 log(ILogger.LOG_DEBUG, "Received event for unknown resource id. Id = " + event.getID() +", event = " + event);
             }
             break;
 
           case EVT_KEY:
             View focus = this.focus;
             if (focus != null) {
                 focus.postEvent(event);
             } else {
                 postEvent(event);
             }
             break;
 
           case EVT_IDLE:
             postEvent( event );
             break;
             
           default:
             postEvent(event);
             break;
         }
     }
     
     /**
      * Get a stream to a resource. The default behavior is to call {@link
      * Factory#getStream(String)}.
      */
     public InputStream getStream(String uri) throws IOException
     {
         return getFactory().getStream(uri);
     }
 
     /**
      * Convert an image to a png or jpg stream.
      */
     FastOutputStream getStream(Image image) throws IOException
     {
         //
         // 1. convert the image to a BufferedImage if necessary
         //
 
         BufferedImage bi = null;
 
         if (image instanceof BufferedImage) {
             bi = (BufferedImage)image;
         } else {
             if (image.getWidth(null) == -1 || image.getHeight(null) == -1) {
                 MediaTracker mt = new MediaTracker(new Canvas());
                 mt.addImage(image, 0);
                 try {
                     mt.waitForAll();
                 } catch(InterruptedException e) {
                 }
             }
             bi = new BufferedImage(image.getWidth(null),
                                    image.getHeight(null),
                                    BufferedImage.TYPE_INT_ARGB);
             bi.getGraphics().drawImage(image, 0, 0, null);
         }
 
         //
         // 2. use jpg if we have three color planes
         //
 
         if (bi.getColorModel().getNumComponents() <= 3) {
             FastOutputStream outStr = new FastOutputStream(4096);
             ImageIO.write(bi, "jpg", outStr);
             return outStr;
         }
         
         //
         // 3. use png if we can do it in less than 512kb
         //
         
         FastOutputStream outStr = new FastOutputStream(4096);
         ImageIO.write(bi, "png", outStr);
         if (outStr.getCount() <= 512 * 1024) {
             return outStr;
         }
 
         //
         // 4. strip the alpha channel and use jpg
         //
 
         if (DEBUG){
             log(ILogger.LOG_DEBUG, "warning : discarding alpha for " +
                               bi.getWidth(null) + "x" + bi.getHeight(null));
         }
         BufferedImage bi2 = new BufferedImage(bi.getWidth(null),
                                               bi.getHeight(null),
                                               BufferedImage.TYPE_INT_RGB);
         bi2.getGraphics().drawImage(bi, 0, 0, null);
     
         outStr = new FastOutputStream(4096);
         ImageIO.write(bi2, "jpg", outStr);
         return outStr;
     }
 
 
     class FlushSentinel implements Ticker.Client
     {
         public long tick(long tm, Object arg)
         {
             // there should be two bytes in the stream (the current chunk length)
             int unflushed = out.getUnflushedCount();
             if (unflushed > 2) {
                 log(ILogger.LOG_DEBUG, Application.this + " : warning, " + unflushed + " bytes not flushed after " + AUTO_FLUSH_DELAY + "ms.");
                 flushTrace.printStackTrace();
             }
             return 0;
         }
     }
 
     //
     // low-level protocol I/O 
     //
 
     private void fatalError(Throwable t)
     {
         if (context != null) {
             if ((t instanceof FileNotFoundException) ||
                 !(t instanceof IOException)) {
                 t.printStackTrace();
             }
             close();
             if (t instanceof HmeException) {
                 throw (HmeException) t;
             }
             throw new HmeException(t);
         }
     }
 
     synchronized void cmdViewAdd(int id, View parent, int x, int y, int width, int height, boolean visible)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".VIEW_ADD(" + parent.getID() + "," + x + "," + y + "," + width + "," + height + "," + visible + ")");
         }
         
         try {
             writeCommand(CMD_VIEW_ADD, id);
             out.writeVInt(parent.getID());
             out.writeVInt(x);
             out.writeVInt(y);
             out.writeVInt(width);
             out.writeVInt(height);
             out.writeBoolean(visible);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdViewSetBounds(int id, int x, int y, int width, int height, Resource animation)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".VIEW_SET_BOUNDS(" + x + "," + y + "," + width + "," + height + "," + animation + ")");
         }
         
         try {
             writeCommand(CMD_VIEW_SET_BOUNDS, id);
             out.writeVInt(x);
             out.writeVInt(y);
             out.writeVInt(width);
             out.writeVInt(height);
             out.writeVInt((animation != null) ? animation.getID() : ID_NULL);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdViewSetScale(int id, float sx, float sy, Resource animation)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".VIEW_SET_SCALE(" + sx + "," + sy + "," + animation + ")");
         }
         
         try {
             writeCommand(CMD_VIEW_SET_SCALE, id);
             out.writeFloat(sx);
             out.writeFloat(sy);
             out.writeVInt((animation != null) ? animation.getID() : ID_NULL);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
     
     synchronized void cmdViewSetTranslation(int id, int tx, int ty, Resource animation)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".VIEW_SET_TRANSLATION(" + tx + "," + ty + "," + animation + ")");
         }
         
         try {
             writeCommand(CMD_VIEW_SET_TRANSLATION, id);
             out.writeVInt(tx);
             out.writeVInt(ty);
             out.writeVInt((animation != null) ? animation.getID() : ID_NULL);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
     
     synchronized void cmdViewSetTransparency(int id, float transparency, Resource animation)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".VIEW_SET_TRANSPARENCY(" + transparency + "," + animation + ")");
         }
         
         try {
             writeCommand(CMD_VIEW_SET_TRANSPARENCY, id);
             out.writeFloat(transparency);
             out.writeVInt((animation != null) ? animation.getID() : ID_NULL);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdViewSetVisible(int id, boolean visible, Resource animation)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".VIEW_SET_VISIBLE(" + visible + "," + animation + ")");
         }
         
         try {
             writeCommand(CMD_VIEW_SET_VISIBLE, id);
             out.writeBoolean(visible);
             out.writeVInt((animation != null) ? animation.getID() : ID_NULL);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdViewSetPainting(int id, boolean painting)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".VIEW_SET_PAINTING(" + painting + ")");
         }
         
         try {
             writeCommand(CMD_VIEW_SET_PAINTING, id);
             out.writeBoolean(painting);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdViewSetResource(int id, Resource resource, int flags)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".VIEW_SET_RESOURCE(" + resource + " " + rsrcFlagsToString(flags) + ")");
         }
         
         try {
             writeCommand(CMD_VIEW_SET_RESOURCE, id);
             out.writeVInt((resource != null) ? resource.getID() : ID_NULL);
             out.writeVInt(flags);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdViewRemove(int id, Resource animation)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".VIEW_REMOVE(" + animation + ")");
         }
         
         try {
             writeCommand(CMD_VIEW_REMOVE, id);
             out.writeVInt((animation != null) ? animation.getID() : ID_NULL);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcAddColor(int id, Color color)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_COLOR(" + color + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_COLOR, id);
             out.writeInt(color.getRGB());
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcAddTtf(int id, String filename)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_TTF(" + filename + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_TTF, id);
             writeStream(filename);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcAddFont(int id, Resource ttf, int style, float size, int flags)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_FONT(" + ttf.getID() + "," + style + "," + size + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_FONT, id);
             out.writeVInt(ttf.getID());
             out.writeVInt(style);
             out.writeFloat(size);
             out.writeVInt(flags);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcAddText(int id, Resource font, Resource color, String text)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_TEXT(" + font.getID() + "," + color + "," + text + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_TEXT, id);
             out.writeVInt(font.getID());
             out.writeVInt(color.getID());
             out.writeUTF(text);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcAddImage(int id, Image image)
     {
         try {
             writeCommand(CMD_RSRC_ADD_IMAGE, id);
             
             FastOutputStream imageOut = getStream(image);
             if (DEBUG) {
                 log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_IMAGE(... image => " +
                 		imageOut.getCount() + " bytes ...)");
             }
             out.write(imageOut.getBuffer(), 0, (int)imageOut.getCount());
             writeTerminator();
         } catch (Throwable t) {
            fatalError(t);
         }
     }
 
     synchronized void cmdRsrcAddImage(int id, byte buf[], int off, int len)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_IMAGE(" + "len=" + len + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_IMAGE, id);
             out.write(buf, off, len);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
     
     synchronized void cmdRsrcAddImage(int id, String filename)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_IMAGE(" + filename + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_IMAGE, id);
             writeStream(filename);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcAddSound(int id, byte buf[], int off, int len)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_SOUND(" + "len=" + len + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_SOUND, id);
             out.write(buf, off, len);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
     
     synchronized void cmdRsrcAddSound(int id, String filename)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_SOUND(" + filename + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_SOUND, id);
             writeStream(filename);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcAddStream(int id, String uri, String contentType, boolean play)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_STREAM(" + uri + "," + contentType + "," + play + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_STREAM, id);
             out.writeUTF(uri);
             out.writeUTF((contentType == null) ? "" : contentType);
             out.write(play ? 1 : 0);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcAddAnim(int id, int duration, float ease)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_ADD_ANIM(" + duration + "," + ease + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_ADD_ANIM, id);
             out.writeVInt(duration);
             out.writeFloat(ease);           
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcSetActive(int id, boolean active)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_SET_ACTIVE(" + active + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_SET_ACTIVE, id);
             out.writeBoolean(active);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcSetPosition(int id, long position)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_SET_POSITION(" + position + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_SET_POSITION, id);
             out.writeVInt(position);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcSetSpeed(int id, float speed)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_SET_SPEED(" + speed + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_SET_SPEED, id);
             out.writeFloat(speed);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
     synchronized void cmdRsrcSendEvent(int id, HmeEvent evt, Resource animation)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_SEND_EVENT(" + evt + "," + animation + ")");
         }
         
         try {
             writeCommand(CMD_RSRC_SEND_EVENT, id);
             out.writeVInt((animation != null) ? animation.getID() : ID_NULL);
             evt.write(out);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcClose(int id)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_CLOSE()");
         }
         
         try {
             writeCommand(CMD_RSRC_CLOSE, id);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdRsrcRemove(int id)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RSRC_REMOVE()");
         }
         
         try {
             writeCommand(CMD_RSRC_REMOVE, id);
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
 
     synchronized void cmdReceiverAcknowledgeIdle(int id, boolean isHandled)
     {
         if (DEBUG) {
             log(ILogger.LOG_DEBUG, id + ".RECEIVER_ACKNOWLEDGE_IDLE(" + isHandled + ")");
         }
         
         try {
             writeCommand(CMD_RECEIVER_ACKNOWLEDGE_IDLE, id);
             out.writeBoolean( isHandled );
             writeTerminator();
         } catch (Throwable t) {
             fatalError(t);
         }
     }
     
     synchronized void cmdReceiverTransition(String loc, int type, TeDict params, byte data[])
     {
     	if (DEBUG) {
     		int cnt = 0;
     		if (params != null)
     		{
     			for (TeIterator iter = params.getNames();
     			iter.hasNext(); iter.next())
     			{
     				cnt++;
     			}
     		}
     		log(ILogger.LOG_DEBUG, ID_ROOT_STREAM + ".RECEIVER_TRANSITION(" +
     				loc + ", " + type + ", n_args=" + cnt +
     				", data_size=" + ((null==data) ? 0 : data.length) + ")");
     	}
     	
     	// Make sure that the memento isn't too large
     	int lengthToSend = 0;
     	if ( data != null ) {
     		lengthToSend = data.length;
     	}
     	final int maxMemento = 10*1024; // 10Kbytes max memento
     	if ( lengthToSend > maxMemento ) {
     		throw new IllegalArgumentException("Memento must be smaller than 10k");
     	}
     	
     	try {
     		writeCommand(CMD_RECEIVER_TRANSITION, ID_ROOT_STREAM);
     		// Write the location
     		out.writeUTF(loc);
     		// Write the type of transition
     		out.writeVInt(type);
     		// Write the param dictionary
     		out.writeDict( params );
     		// Write the memento, if any
     		out.writeVData(data, lengthToSend);
     		writeTerminator();
     	} catch (Throwable t) {
     		fatalError(t);
     	}
     }
     
     //
     // Following three helper methods are called from synchronized methods
     //
     
     private void writeCommand(int opcode, int id) throws IOException
     {
         if (context == null) {
             throw new HmeException("application closed");
         }
 
         out.writeVInt(opcode);
         out.writeVInt(id);
     }
 
     private void writeStream(String filename) throws IOException
     {
         InputStream in = getApp().getStream(filename);
         try {
             while (true) {
                 int count = in.read(buf, 0, buf.length);
                 if (count < 0) {
                     break;
                 }
                 out.write(buf, 0, count);
             }
         } finally {
             in.close();
         }
     }
 
     private void writeTerminator() throws IOException
     {
         if (context == null) {
             return;
         }
         
         out.writeTerminator();
 
         if (IContext.DEBUG_FLUSHES) {
             // watch for unflushed bytes if this isn't the event thread
             Thread current = Thread.currentThread();
             if ((eventThread == null) && (IListener.ACCEPTOR_NAME.equals(current.getName())))
             {
                 eventThread = current;
             }
             if (current != eventThread && !IListener.ACCEPTOR_NAME.equals(current.getName())) {
                 if (flusher == null) {
                     flusher = new FlushSentinel();
                     flushTrace = new Exception("missed flush");
                 }
                 flushTrace.fillInStackTrace();
                 Ticker.master.add(flusher, System.currentTimeMillis() + AUTO_FLUSH_DELAY, null);
             }
         }
     }
 
     public static IFactory getAppFactory(String appClassName, ClassLoader loader, IArgumentList args) 
     {
         String classNoPackage = appClassName.substring(appClassName.lastIndexOf('.') + 1);
     
         // check for a custom factory or use the generic factory
         IFactory factory = null;
         Class factoryClass = null;
         
         try {
             String innerClass = appClassName + "$" + classNoPackage + "Factory";
             factoryClass = Class.forName(innerClass, true, loader);
             if (!IFactory.class.isAssignableFrom(factoryClass))
             {
                 // inner class doesn't implement IFactory, log message
                 // and try to use the base factory
                 System.out.println(ILogger.LOG_WARNING + ", " + innerClass + " does not implement IFactory interface, using default Factory.");
                 factoryClass = Factory.class;
             }
         } catch (ClassNotFoundException e) {
             // use the generic factory
             factoryClass = Factory.class;
         }
 
         try
         {
             factory = (IFactory)factoryClass.newInstance();
             factory.initFactory(appClassName, loader, args);
         }
         catch (Exception e) 
         {
             System.out.println("error constructing Factory, exiting.");
             e.printStackTrace();
         }
         return factory;
     }
 }
