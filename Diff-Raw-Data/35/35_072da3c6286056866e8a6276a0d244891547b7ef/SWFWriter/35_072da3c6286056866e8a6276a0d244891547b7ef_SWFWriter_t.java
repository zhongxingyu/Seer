 
 /*****************************************************************************
  * SWFWriter.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.utils.ListFormat;
 import org.openlaszlo.iv.flash.api.*;
 import org.openlaszlo.iv.flash.api.action.*;
 import org.openlaszlo.iv.flash.api.button.*;
 import org.openlaszlo.iv.flash.api.image.*;
 import org.openlaszlo.iv.flash.api.sound.*;
 import org.openlaszlo.iv.flash.api.shape.*;
 import org.openlaszlo.iv.flash.api.text.*;
 import org.openlaszlo.iv.flash.util.*;
 import org.openlaszlo.iv.flash.cache.*;
 import org.openlaszlo.compiler.CompilationEnvironment;
 
 import org.openlaszlo.media.*;
 
 import java.io.*;
 import java.util.*;
 import java.lang.Math;
 import java.lang.Character;
 
 import org.jdom.Element;
 
 // jgen 1.4
 import java.awt.geom.Rectangle2D;
 
 import org.apache.log4j.*;
 
 /** Accumulates code, XML, and assets to a SWF object file.
  *
  * Make heavy use of JGenerator API.
  *
  * Properties documented in Compiler.getProperties.
  */
 class SWFWriter extends ObjectWriter {
 
     /** Movie being constructed. */
     private SWFFile mFlashFile;
 
     /** Fonts being collected. */
     private FontsCollector mPreloaderFontsCollector = new FontsCollector();
     private FontManager mFontManager = new FontManager();
     /** Have any fonts been imported into this swf movie?  */
     private boolean mLibFontsDefined = false;
 
     /** True iff close() has been called. */
     private boolean mCloseCalled = false;
 
     /** If true, wrap 'with (_level0) { ...} ' around code blocks */
     private boolean level0 = false;
     
     /** Total number of frames in the movie **/
     private int mLastFrame = 0;
     
     /** Input text fontinfo map */
     private final TreeMap mInputTextSet = new TreeMap();
 
     /** Index of font table in the first frame */
     private int mFontTableIndex = -1;
 
     /** Index of resource table in the first frame */
     private int mResourceTableIndex = -1;
 
     /** Default font */
     private Font mDefaultFont = null;
     private String mDefaultFontName = null;
     private String mDefaultFontFileName = null;
     private String mDefaultBoldFontFileName = null;
     private String mDefaultItalicFontFileName = null;
     private String mDefaultBoldItalicFontFileName = null;
     // TODO: [2003-12-08 bloch] need logic to set this to true
     private boolean mDefaultFontUsedForMeasurement = false;
 
     /** Flash format version */
     private int mFlashVersion = 6;
 
     /** frame rate of movie */
     private int mFrameRate = 30; 
 
     /** Leading for text and input text */
     private int mTextLeading = 2; 
 
     private Map mDeviceFontTable = new HashMap();
 
     /** Logger */
     private static Logger mLogger = org.apache.log4j.Logger.getLogger(SWFWriter.class);
 
     /** Height for generated advance (width) table */
     public static final int DEFAULT_SIZE = 8;
 
 
     /** Logger for jgenerator */
     /**
      * Initializes a SWFWriter with an OutputStream to which a new SWF
      * will be written when <code>SWFWriter.close()</code> is called.
      * 
      * @param stream A <code>java.io.OutputStream</code> that the
      * movie will be written to.
      * @param props list of properties
      * @param cache media cache
      * @param importLibrary If true, the compiler will add in the LaszloLibrary.
      */
     SWFWriter(Properties props, OutputStream stream,
               CompilerMediaCache cache,
               boolean importLibrary,
               CompilationEnvironment env) {
         super(props, stream, cache, importLibrary, env);
 
         String s;
 
         s = mProperties.getProperty("swf.frame.rate", "30");
         mFrameRate = Integer.parseInt(s);
 
         s = mProperties.getProperty("swf.text.leading", "2");
         mTextLeading = Integer.parseInt(s);
 
         mFlashVersion = env.getSWFVersionInt();
 
         try {
             if (!importLibrary) {
                 mFlashFile = new SWFFile(props);
                 mFlashFile.setVersion(mFlashVersion);
                 mFlashFile.setMainScript(new Script(1));
             }
 
         } catch (CompilationError e) {
             throw new ChainedException(e);
         }
     }
 
     /** Wrap a 'with (_level0) { ...}' around compiled code blocks */
     void setLevel0(boolean val) {
         this.level0 = val;
     }
 
     public void importBaseLibrary(String library, CompilationEnvironment env) {
 
         try {
             File f = env.resolveLibrary(library, "");
             mFlashFile = new SWFFile(f.getAbsolutePath(), mProperties);
             mFlashFile.setVersion(mFlashVersion);
             // Set the frame rate (shifted)
             mFlashFile.setFrameRate(mFrameRate << 8); 
             
             //            mFlashFile.printContent(System.out);
             
             if (LPS.isInternalBuild()) {
                 long lfcModTime = f.lastModified();
                 List newerFiles = new Vector(); // List<File>
                 List sourceDirs = new Vector(); // List<File>
                 sourceDirs.add(f.getParentFile());
                 while (!sourceDirs.isEmpty()) {
                     File ff = (File) sourceDirs.get(0);
                     sourceDirs.remove(0);
                     if (ff.isDirectory()) {
                         sourceDirs.addAll(Arrays.asList(ff.listFiles()));
                     } else if (ff.isFile() && ff.getName().endsWith(".as")) {
                         long modTime = ff.lastModified();
                         if (modTime > lfcModTime)
                             newerFiles.add(ff.getName());
                     }
                 }
                 if (!newerFiles.isEmpty()) {
                     env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes=p[0] + " is older than " + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-235", new Object[] {f.getName(), new ListFormat().format(newerFiles)})
                                                         );
                 }
             }
         } catch (FileNotFoundException e) {
             throw new ChainedException(e);
         }
     }
     
     FontManager getFontManager() {
         return mFontManager;
     }
 
     public boolean isDeviceFont(String face) {
         return (mDeviceFontTable.get(face) != null);
     }
 
     public void setDeviceFont(String face) {
         mDeviceFontTable.put(face,"true");
     }
 
     public void setFontManager(FontManager fm) {
         this.mFontManager = fm;
     }
 
     void addPreloaderScript(String script) {
         if (mPreloaderAdded != true)
             throw new RuntimeException(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Preloader not added yet."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-268")
 );
 
         addScript(script, 0);
     }
         
     void addPreloader(CompilationEnvironment env) {
         if (mPreloaderAdded == true) 
             throw new RuntimeException(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Preloader already added."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-282")
 );
 
         // TODO: [2004-03-04 bloch] maybe someday we have different versions of
         // preloader.lzl (e.g. debug, profile).
         File f;
         try {
             f = env.resolve("lzpreloader.lzl", null);
         } catch (FileNotFoundException e) {
             throw new ChainedException(e);
         }
         mFlashFile.addPreloaderFrame(f.getAbsolutePath());
         mLastFrame = 1;
 
         mPreloaderAdded = true;
     }
 
     /**
      * Sets the canvas for the movie
      *
      * @param canvas
      * 
      */
     void setCanvas(Canvas canvas, String canvasConstructor) {
         Rectangle2D r = new Rectangle2D.Double(
             0, 0,
             (canvas.getWidth()*TWIP),
             (canvas.getHeight()*TWIP));
 
         mFlashFile.setFrameSize(r);
 
         int bgc = canvas.getBGColor();
         int red   = (bgc >> 16) & 0xff;
         int green = (bgc >> 8)  & 0xff;
         int blue  = (bgc)       & 0xff; 
         Color c = new Color(red, green, blue);
         SetBackgroundColor setbgc = new SetBackgroundColor(c);
         mFlashFile.getMainScript().setBackgroundColor(setbgc);
         
         // Write scriptlimits tag if requested
         if ((this.mRecursionLimit != 0) || (this.mExecutionTimeout != 0)) {
           // ScriptLimits tag, to set max recursion depth and timeout
           Frame frame = mFlashFile.getMainScript().getFrameAt(mLastFrame);
           ScriptLimits slimit = new ScriptLimits(this.mRecursionLimit, this.mExecutionTimeout);
           frame.addFlashObject(slimit);
         }
 
         // NOTE: disable constant pool when compiling canvas constructor
         // so that build id etc... are easy for qa to pick out.
         Properties props = (Properties)mProperties.clone();
         props.setProperty("disableConstantPool", "1");
         //scriptWriter.println(canvasConstructor);
         byte[] action = ScriptCompiler.compileToByteArray(canvasConstructor, props);
         Program program = new Program(action, 0, action.length);
         mLogger.debug("    Adding a program of " + action.length + " bytes.");
         addProgram(program);
 
         // Set width and height properties for preloader...
         mWidth = canvas.getWidth();
         mHeight = canvas.getHeight();
 
         // Get default font info 
         FontInfo fontInfo = canvas.getFontInfo();
 
         mDefaultFontName = canvas.defaultFont;
         mDefaultFontFileName = canvas.defaultFontFilename;
         mDefaultBoldFontFileName = canvas.defaultBoldFontFilename;
         mDefaultItalicFontFileName = canvas.defaultItalicFontFilename;
         mDefaultBoldItalicFontFileName = canvas.defaultBoldItalicFontFilename;
 
         Frame frame = mFlashFile.getMainScript().getFrameAt(mLastFrame);
 
         // Make a bogus object for us to replace with the font and resource table
         // actionscript at the end of compilation.
         mFontTableIndex = frame.size();
         frame.addFlashObject(new SetBackgroundColor(new Color(0, 0, 0)));
         mResourceTableIndex = frame.size();
         frame.addFlashObject(new SetBackgroundColor(new Color(0, 0, 0)));
 
         mEnv.getCanvas().addInfo(mInfo); 
     }
 
 
     /** Get default fonts and stuff from canvas; used for snippet compilation */
     void setCanvasDefaults(Canvas canvas, CompilerMediaCache mc) {
         Rectangle2D r = new Rectangle2D.Double(
             0, 0,
             (canvas.getWidth()*TWIP),
             (canvas.getHeight()*TWIP));
 
         mFlashFile.setFrameSize(r);
         this.mCache = mc;
         mWidth = canvas.getWidth();
         mHeight = canvas.getHeight();
         // Get default font info 
         FontInfo fontInfo = canvas.getFontInfo();
         mDefaultFontName = canvas.defaultFont;
         mDefaultFontFileName = canvas.defaultFontFilename;
         mDefaultBoldFontFileName = canvas.defaultBoldFontFilename;
         mDefaultItalicFontFileName = canvas.defaultItalicFontFilename;
         mDefaultBoldItalicFontFileName = canvas.defaultBoldItalicFontFilename;
     }
 
 
     /** Create a program from the given script */
     Program program(String script) {
          byte[] action = ScriptCompiler.compileToByteArray(script, mProperties);
          return new Program(action, 0, action.length);
     }
 
     /** Compiles the specified script to bytecodes
      * and add its bytecodes to the current frame in this movie.
      *
      * @param script the script to be compiled
      * @return the number of bytes
      */
     public int addScript(String script) {
         if (level0) {
             script = "with (_level0) { "+script + "}";
         }
 
          byte[] action = ScriptCompiler.compileToByteArray(script, mProperties);
          //scriptWriter.println(script);
          Program program = new Program(action, 0, action.length);
          mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Adding a program of " + p[0] + " bytes."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-410", new Object[] {new Integer(action.length)})
 );
          addProgram(program);
          return action.length;
     }
 
     /** Compiles the specified script to bytecodes
      * and add its bytecodes to the current frame in this movie.
      *
      * @param script the script to be compiled
      * @param offset of frame to add to
      */
     private void addScript(String script, int offset) {
         if (level0) {
             script = "with (_level0) { "+script + "}";
         }
          byte[] action = ScriptCompiler.compileToByteArray(script, mProperties);
          //scriptWriter.println(script);
          Program program = new Program(action, 0, action.length);
          mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Adding a program of " + p[0] + " bytes."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-410", new Object[] {new Integer(action.length)})
 );
          addProgram(program, offset);
     }
 
     /**
      * Adds the program to the next frame
      *
      * @param program to be added
      */
     void addProgram(Program program) {
          Frame frame = mFlashFile.getMainScript().getFrameAt(mLastFrame);
          frame.addFlashObject(new DoAction(program));
     }
 
     /**
      * Adds the program to the specified frame
      *
      * @param program to be added
      * @param offset of frame to add to 
      */
     private void addProgram(Program program, int offset) {
          Frame frame = mFlashFile.getMainScript().getFrameAt(offset);
          frame.addFlashObject(new DoAction(program));
     }
 
 
     public static String stripBaseName (String fileName, CompilationEnvironment env) {
         try {
             fileName = (new File(fileName)).getCanonicalFile().toString();
         } catch (java.io.IOException e) {
         }
         String base = env.getErrorHandler().fileBase;
         if (base == null || "".equals(base)) {
             return fileName;
         }
         base = (new File(base)).getAbsolutePath() + File.separator;
         if (base != null) {
             int i = 1;
             // Find longest common substring
             while (i < base.length() && fileName.startsWith(base.substring(0, i))) { i++; }
             // remove base string prefix
             return fileName.substring(i-1);
         } else {
             return fileName;
         }
     }
 
 
     /** Import a resource file into the preloader movie.
      * Using a name that already exists clobbers the
      * old resource (for now).
      *
      * @param fileName file name of the resource
      * @param name name of the MovieClip/Sprite
      * @throws CompilationError
      */
     public void importPreloadResource(String fileName, String name) 
         throws ImportResourceError
     {
         if (name.equals(""))
             name = createName();
         importResource(fileName, name, 0, mPreloaderFontsCollector);
     }
 
     public void importPreloadResource(File fFileName, String name) 
         throws ImportResourceError
     {
         if (name.equals(""))
             name = createName();
         importResource(fFileName.toString(), name, 0, mPreloaderFontsCollector);
     }
 
     /** Import a multiframe resource into the current movie.  Using a
      * name that already exists clobbers the old resource (for now).
      */
     public void importPreloadResource(List sources, String name, File parent)
         throws ImportResourceError
     {
         if (name.equals("")) 
             name = createName();
         importResource(sources, name, parent, 0, mPreloaderFontsCollector);
     }
 
 
 
     /** Import a multiframe resource into the current movie.  Using a
      * name that already exists clobbers the old resource (for now).
      *
      * @param sources file names of the resources
      * @param name name of the MovieClip/Sprite
      * @param parent parent's File object 
      */
     public void importResource(List sources, String name, File parent)
     {
         importResource(sources, name, parent, -1);
     }
 
     /** Import a multiframe resource into the current movie.  Using a
      * name that already exists clobbers the old resource (for now).
      *
      * @param sources file names of the resources
      * @param name name of the MovieClip/Sprite
      * @param parent parent's File object 
      * @param frameNum frame offset to add to
      */
     public void importResource(List sources, String name, File parent, int frameNum)
     {
         importResource(sources, name, parent, frameNum, null);
     }
 
     /** Import a resource file into the current movie.
      * Using a name that already exists clobbers the
      * old resource (for now).
      *
      * @param fileName file name of the resource
      * @param name name of the MovieClip/Sprite
      * @throws CompilationError
      */
     public void importResource(String fileName, String name)
         throws ImportResourceError
     {
         importResource(fileName, name, -1);
     }
 
     public void importResource(File fFile, String name)
         throws ImportResourceError
     {
         importResource(fFile.toString(), name);
     }
 
     /** Import a resource file into the current movie.
      * Using a name that already exists clobbers the
      * old resource (for now).
      *
      * @param fileName file name of the resource
      * @param name name of the MovieClip/Sprite
      * @param frameNum frame offset to add to
      * @throws CompilationError
      */
     public void importResource(String fileName, String name, int frameNum)
         throws CompilationError
     {
         importResource(fileName, name, frameNum, null);
     }
 
 
     /** Import a resource file into the current movie.
      * Using a name that already exists clobbers the
      * old resource (for now).
      *
      * @param fileName file name of the resource
      * @param name name of the MovieClip/Sprite
      * @param frameNum frame offset to add to
      * @param fontsCollector fonts collector for resource (used by preloader)
      * @throws CompilationError
      */
     public void importResource(String fileName, String name, int frameNum, 
                                FontsCollector fontsCollector)
         throws CompilationError
     {
 
             File inputFile = new File(fileName);
             if (inputFile.isDirectory()) {
                 String[] sources = inputFile.list();
                 ArrayList outsources = new ArrayList();
                 
                 for (int i = 0; i < sources.length; i++) {
                     String fname = fileName + File.separator + sources[i];
                     File f = new File(fname);
                     //mLogger.debug("SWFWriter file: " + f.isFile());    
 
                     if (f.isFile()) {
                         //mLogger.debug("SWFWriter adding: " + fname);    
                         outsources.add(fname);
                     }
                 }
                 importResource(outsources, name, null, -1, null, false);
                 return;
             }
 
         mLogger.debug("    Importing resource " + name);
         try {
             fileName = new File(fileName).getCanonicalPath();
         } catch (java.io.IOException e) {
             throw new ImportResourceError(fileName, e, mEnv);
         }
         FlashDef def = null;
 
         Resource res =  (Resource)mResourceMap.get(fileName);
         boolean oldRes = (res != null);
         if (!oldRes) {
             // Get the resource and put in the map
             res = getResource(fileName, name);
             mResourceMap.put(fileName, res);
 
             def = res.getFlashDef();
             if (fontsCollector != null) def.collectFonts(fontsCollector);
             mFlashFile.addDefToLibrary(name, def);
             def.setName(name);
         
         } else {
             def = res.getFlashDef();
             if (fontsCollector != null) def.collectFonts(fontsCollector);
 
             // Add an element with 0 size, since it's already there.
             Element elt = new Element("resource");
                 elt.setAttribute("name", name);
                 // elt.setAttribute("mime-type", MimeType.MP3); 
                 elt.setAttribute("source", fileName);
                 elt.setAttribute("filesize", "0");
             mInfo.addContent(elt);
         } 
         
         ExportAssets ea = new ExportAssets();
         ea.addAsset(name, def);
         Timeline timeline = mFlashFile.getMainScript().getTimeline();
         if (frameNum == -1) {
             frameNum = timeline.getFrameCount() - 1;
         }
         Frame frame = timeline.getFrameAt(frameNum);
         frame.addFlashObject(ea);
     }
     
     /** Import a multiframe resource into the current movie.  Using a
      * name that already exists clobbers the old resource (for now).
      *
      * @param sources file names of the resources
      * @param name name of the MovieClip/Sprite
      * @param parent parent's File object 
      * @param frameNum frame offset to add to
      * @param fontsCollector fonts collector for resource (used by preloader)
      */
     public void importResource(List sources, String name, File parent, int frameNum, 
                                FontsCollector fontsCollector)
 
     {
         importResource(sources, name, parent, frameNum, fontsCollector, true);
     }
 
     /** Import a multiframe resource into the current movie.  Using a
      * name that already exists clobbers the old resource (for now).
      *
      * @param sources file names of the resources
      * @param name name of the MovieClip/Sprite
      * @param parent parent's File object 
      * @param frameNum frame offset to add to
      * @param fontsCollector fonts collector for resource (used by preloader)
      * @param addStop if true, add a stop frame after each imported resource 
      */
     public void importResource(List sources, String name, File parent, int frameNum, 
                                FontsCollector fontsCollector, boolean addStop)
     {
         Script out = new Script(1);
         String fileName = null;
         mLogger.debug("Including multiple resources as " + name);
         int width = 0;
         int height = 0;
         int fNum = 0;
         for (Iterator e = sources.iterator() ; e.hasNext() ;) {
             fileName = (String)e.next();
             mLogger.debug("    Importing " + fileName);
 
             // Definition to add to the library (without stop)
             Resource res = getMultiFrameResource(fileName, name, fNum);
             Script scr = (Script)res.getFlashDef(); 
             if (fontsCollector != null) scr.collectFonts(fontsCollector);
             int bc = out.getFrameCount();
             out.appendScript(scr);
             int fc = out.getFrameCount();
             Frame f = out.getFrameAt(fc - 1);
             if (addStop) f.addStopAction();
             mLogger.debug("    Added " + (fc - bc) + " of " + fc + "frame(s)");
 
             int rw = res.getWidth();
             int rh = res.getHeight();
             if (rw > width) {
                 width = rw;
             }
             if (rh > height) {
                 height = rh;
             }
             // NOTE: add the ratio attribute to each frame here; this 
             // appears to be required to make a multi-frame resource that has individual 
             // frames that are swfs with nested movieclips work correctly.
             // This was "guessed" by dumping the contents
             // of a multi-frame SWF created by the Flash tool itself.
             // This is weird since the docs on 'ratio' say it is only
             // needed to cope with Morphing.  Hmph.  See bug 4961 for details.
             if (fNum > 0) {
                 for (int i = 0; i < f.size(); i++) {
                     if (f.getFlashObjectAt(i) instanceof Instance) {
                         Instance inst = (Instance) f.getFlashObjectAt(i);
                         inst.ratio = fNum;
                         break;
                     }
                 }
             }
             fNum++;
         }
 
         // TODO [2003-1-2 bloch]: Could optimize and only add
         // multi-frame resources when the total size is greater than
         // the size of the first frame
         mMultiFrameResourceSet.add(new Resource(name, out, width, height));
 
         FlashDef def = (FlashDef)out;
         mFlashFile.addDefToLibrary(name, def);
         def.setName(name);
         ExportAssets ea = new ExportAssets();
         ea.addAsset(name, def);
         Timeline timeline = mFlashFile.getMainScript().getTimeline();
         if (frameNum == -1) {
             frameNum = timeline.getFrameCount() - 1;
         }
         Frame frame = timeline.getFrameAt(frameNum);
         frame.addFlashObject(ea);
     }
 
 
     /** Imports this resource, if it has not previously been imported, as
      * resource that can be used as a click region, and returns in any
      * case the name of the clip that refers to it.  
      */
     public String importClickResource(File file) throws ImportResourceError
     {
         String fileName;
         try {
             fileName = file.getCanonicalPath();
         } catch (java.io.IOException e) {
             throw new CompilationError(e);
         }
 
         String name = (String) mClickResourceMap.get(fileName);
 
         if (name == null) {
 
             Button2 but = new Button2();
 
             FlashDef def;
             Rectangle2D bounds;
 
             // FIXME: [2004-06-29 bloch] 
             // For each instance in the first frame, add a button record.
             // Get bounds for entire clip; should only get bounds for first frame!
             // Should only allow swf resources as click resources.
             try {
                 Script script = FlashFile.parse(fileName).getMainScript();
                 Frame frame = script.getFrameAt(0);
                 bounds = script.getBounds();
                 for(int i = 0; i < frame.size(); i++) {
                     FlashObject o = (FlashObject)frame.getFlashObjectAt(i);
                     if (o instanceof Instance) {
                         Instance inst = (Instance)o;
                         CXForm cxform = inst.cxform;
                         if (cxform == null) {
                             cxform = CXForm.newIdentity(false);
                         }
                         java.awt.geom.AffineTransform matrix = inst.matrix;
                         if (matrix == null) {
                             matrix = new java.awt.geom.AffineTransform();
                         }
                          
                         but.addButtonRecord(new ButtonRecord(ButtonRecord.HitTest,
                                                  inst.def, 1, matrix, cxform));
                     }
                 }
             } catch (Exception e) {
                 throw new ImportResourceError(fileName, e, mEnv);
             }
 
             // TODO: [2004-06029 bloch] When we merge into lps-intl2, there should
             // be some code sharing between this and SWFFile
             but.addActionCondition(ActionCondition.onPress(program(
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmousedown')")));
             but.addActionCondition(ActionCondition.onRelease(program(
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmouseup');" + 
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onclick')")));
             but.addActionCondition(ActionCondition.onReleaseOutside(program(
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmouseup');" + 
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmouseupoutside')")));
             but.addActionCondition(ActionCondition.onRollOver(program(
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmouseover')")));
             but.addActionCondition(ActionCondition.onRollOut(program(
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmouseout')")));
             but.addActionCondition(ActionCondition.onDragOut(program(
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmouseout');" + 
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmousedragout')")));
             but.addActionCondition(ActionCondition.onDragOver(program(
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmouseover');" + 
                "_root.LzMouseKernel.handleMouseEvent( myView, 'onmousedragin')")));
             
             name = createName();
 
             // Scale the movieclip to 100x100 for use by LFC.
             Script movieClip = new Script(1);
             Frame f = movieClip.getFrameAt(0);
             double sx = 100.0 * TWIP / (double)bounds.getWidth();
             double sy = 100.0 * TWIP / (double)bounds.getHeight();
             java.awt.geom.AffineTransform matrix = java.awt.geom.AffineTransform.getScaleInstance(sx, sy);
             f.addInstance(but, 1, matrix, null);
 
             mFlashFile.addDefToLibrary(name, movieClip);
             movieClip.setName(name);
             ExportAssets ea = new ExportAssets();
             ea.addAsset(name, movieClip);
             Timeline timeline = mFlashFile.getMainScript().getTimeline();
             Frame frame = timeline.getFrameAt(timeline.getFrameCount() - 1);
             frame.addFlashObject(ea);
 
             mClickResourceMap.put(fileName, name);
         }
 
         return name;
     }
     
     /**
      * Recursively strips out the ActionScript from a 
      * given movie Script (MovieClip)
      *
      * Actually, it leaves the actionscript blocks in, 
      * but turns them into programs that do nothing.
      */
     private void stripActions(Script s) {
         Timeline t = s.getTimeline();
         int n = t.getFrameCount();
 
         boolean didStop = false;
 
         for(int i = 0; i < n; i++ ) {
             Frame f = s.getFrameAt(i);
 
             for (int j = 0; j < f.size(); j++) {
                 FlashObject o = f.getFlashObjectAt(j);
                 if (o instanceof Script) {
                     stripActions((Script)o);
                 } else if (o instanceof DoAction) {
                     DoAction doa = (DoAction) o;
                     Program p = new Program();
                     p.none();
                     doa.setProgram(p);
                 }
             }
         }
     }
 
 
     /** strip compiler pragmas from script
      */
     String stripPragmas(String str) {
         String s2 = str;
         //s2 = s2.replaceAll("[\r\n]#[^\r\n]*[\r\n]", "");
         s2 = s2.replaceAll("#line\\s\\d*", "");
         s2 = s2.replaceAll("#beginAttributeStatements", "");
         s2 = s2.replaceAll("#endAttributeStatements", "");
         s2 = s2.replaceAll("#pragma\\s\\S*", "");
         s2 = s2.replaceAll("#beginAttribute", "");
         s2 = s2.replaceAll("#endAttribute", "");
         s2 = s2.replaceAll("#beginContent", "");
         s2 = s2.replaceAll("#endContent", "");
         s2 = s2.replaceAll("#file\\s[^ \t\"]*", "");
         return s2;
     }
 
     /** Writes the SWF to the <code>OutputStream</code> that was
      * supplied to the SWFWriter's constructor.
      * @throws IOException if an error occurs
      */
     public void close() throws IOException {
 
         if (mCloseCalled) {
             throw new IllegalStateException("SWFWriter.close() called twice");
         }
 
         // Add font information
         addFontTable();
 
         // Add resource information to canvas.
         addResourceTable();
 
         if (mPreloaderAdded == true) {
             // Add script to hide the preloader after finishing instantiation
             String finalobj = "_root.lzpreloader.done();";
             addScript(finalobj);
         }
 
         // Flag says to post a copy of all debug.write() calls back to the server
         if (mProperties.getProperty("logdebug", "false").equals("true")) {
             addScript("_dbg_log_all_writes = true;");
         }
 
         boolean debug = mProperties.getProperty("debug", "false").equals("true");
 
         // This indicates whether the user's source code already manually invoked
         // <debug> to create a debug window. If they didn't explicitly call for
         // a debugger window, instantiate one now by passing 'true' to __LzDebug.startDebugWindow()
         boolean makedebugwindow =  !mEnv.getBooleanProperty(mEnv.USER_DEBUG_WINDOW);
 
         // Bring up a debug window if needed.
         if (debug && makedebugwindow) {
             addScript("__LzDebug.makeDebugWindow()");
         }
 
         // Tell the canvas we're done loading.
         addScript("canvas.initDone()");
 
         // Make sure we stop
         Program program = new Program();
         program.stop();
         program.none();
         addProgram(program);
 
         // Always compress
         mFlashFile.setCompressed(true);
      
         try { 
 
             InputStream input;
             input = mFlashFile.generate(mEnv.getEmbedFonts() ? mFontsCollector : new FontsCollector(), 
                                             mEnv.getEmbedFonts() ? mPreloaderFontsCollector : new FontsCollector(),
                                             mPreloaderAdded).getInputStream();
 
             FileUtils.send(input, mStream);
 
 
         } catch (IVException e) {
             throw new ChainedException(e);
         }
 
         mCloseCalled = true;
     }
 
     public void openSnippet(String liburl) throws IOException {
         // How do we make sure an initial frame exists?  Does this do it? 
         Frame frame = mFlashFile.getMainScript().getFrameAt(mLastFrame);
         // if we don't have any frame, then code which adds resources gets
         // an error. This happens if you have a resource declared before any code.
         this.liburl = liburl;
     }
 
     public void closeSnippet() throws IOException {
 
         if (mCloseCalled) {
             throw new IllegalStateException("SWFWriter.closeSnippet() called twice");
         }
 
         mFlashFile.setCompressed(true);
 
         // If any fonts have been declared in this library, add an
         // ImportAssets(2?) to import assets.  While this doesn't seem
         // to be of any use (importing our own assets) it does cause
         // the font assets to become visible to the main app for some
         // reason.
         //
         // Note: This trick doesn't seem to work for image resource,
         // unfortunately; we haven't figured out a way to make the
         // imported image assets be visible/attachable to the loading
         // movieclip.
         
 
         if (mLibFontsDefined) {
             ImportAssets2 ia = new ImportAssets2();
             //System.err.println("setting ImportAssets url="+this.liburl);
             //get property of base url for fonts to import
             String importFontUrlPath = LPS.getProperty("import.font.base.url");
             if (importFontUrlPath != null) {
                 if (importFontUrlPath.endsWith("/") == false) {
                     importFontUrlPath += "/";
                 }
                 if (liburl.startsWith("/")) {
                     liburl = liburl.substring(1);
                 }
                 liburl = importFontUrlPath + liburl;
             }
             ia.setUrl(liburl);
 
             Enumeration enu = mFlashFile.definitions();
             while (enu.hasMoreElements()) {
                 FlashDef def = (FlashDef) enu.nextElement();
                 ia.addAsset(def.getName(), def);
             }
         
             Timeline timeline = mFlashFile.getMainScript().getTimeline();
             Frame frame = timeline.getFrameAt(timeline.getFrameCount() - 1);
             frame.addFlashObject(ia);
         }
 
         ////////////////////////////////////////////////////////////////
 
         // Make sure we stop
         addScript("this._parent.loader.snippetLoaded(this._parent, null)");
         Program program = new Program();
         program.stop();
         program.none();
         addProgram(program);
 
 
         try { 
             InputStream input;
             input = mFlashFile.generate(mEnv.getEmbedFonts() ? mFontsCollector : new FontsCollector(), 
                                         mEnv.getEmbedFonts() ? mPreloaderFontsCollector : new FontsCollector(),
                                         mPreloaderAdded).getInputStream();
             FileUtils.send(input, mStream);
         } catch (IVException e) {
             throw new ChainedException(e);
         }
 
         mCloseCalled = true;
     }
 
 
 
     /**
      * Generate a warning message
      */
     void warn(CompilationEnvironment env, String msg) {
         CompilationError cerr;
         env.warn(msg);
     }
 
     /**
      * Import a font of a given style into the SWF we are writing.
      *
      * @param fileName filename for font in LZX
      * @param face face name of font
      * @param style style of font
      */
     void importFontStyle(String fileName, String face, String style,
             CompilationEnvironment env)
         throws FileNotFoundException, CompilationError {
 
         int styleBits = FontInfo.styleBitsFromString(style);
 
         mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="importing " + p[0] + " of style " + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1225", new Object[] {face, style})
 );
 
         FontInfo fontInfo = mEnv.getCanvas().getFontInfo(); 
         boolean isDefault = false;
 
         Font font = importFont(fileName, face, styleBits, false);
         mLibFontsDefined = true;
 
         if (fontInfo.getName().equals(face)) {
             if (styleBits == FontInfo.PLAIN) {
                 isDefault = true;
                 mDefaultFont = font;
             } 
         }
 
         FontFamily family = mFontManager.getFontFamily(face, true);
 
         switch (styleBits) {
             case FontInfo.PLAIN: 
                 if (family.plain != null) {
                     if (!isDefault || mDefaultFontUsedForMeasurement) {
                         warn(env, 
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Redefined plain style of font: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1252", new Object[] {face})
 );
                     }
                 }
                 family.plain = font; break;
             case FontInfo.BOLD:
                 if (family.bold != null) {
                     warn(env, 
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Redefined bold style of font: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1265", new Object[] {face})
 );
                 }
                 family.bold = font; break;
             case FontInfo.ITALIC:
                 if (family.italic != null) {
                     warn(env, 
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Redefined italic style of font: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1277", new Object[] {face})
 );
                 }
                 family.italic = font; break;
             case FontInfo.BOLDITALIC:
                 if (family.bitalic != null) {
                     warn(env, 
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Redefined bold italic style of font: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1289", new Object[] {face})
 );
                 }
                 family.bitalic = font; break;
             default:
                 throw new ChainedException(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Unexpected style"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1300")
 );
         }
 
     }
 
     /**
      * Import a font into the SWF we are writing
      *
      * @param fileName name of font file 
      * @param face font name of font in LZX
      */
     private Font importFont(String fileName, String face, int styleBits,
         boolean replace)
         throws FileNotFoundException, CompilationError {
 
         if (isDeviceFont(face)) {
             return Font.createDummyFont(face);
         }
 
         mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Importing font " + p[0] + " from " + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1327", new Object[] {face, fileName})
 );
 
         String fromType = FontType.fromName(fileName);
         String location = null;
         try {
             File fontFile = mCache.transcode(new File(fileName), fromType, 
                     FontType.FFT);
             location = fontFile.getAbsolutePath();
         } catch (TranscoderException e) {
             throw new CompilationError(e);
         } catch (FileNotFoundException e) {
             throw e;
         } catch (IOException e) {
             throw new CompilationError(e);
         }
 
         Font font = Font.createDummyFont(face);
 
         long fileSize =  FileUtils.getSize(new File(location));
 
         // FIXME: [2004-05-31 bloch] embed fonts shouldn't be global
         if (mFlashVersion == 5 || mEnv.getEmbedFonts()) {
             Element elt = new Element("font");
                 elt.setAttribute("face", face);
                     elt.setAttribute("style", FontInfo.styleBitsToString(styleBits, true));
                 elt.setAttribute("location", location);
                 elt.setAttribute("source", fileName);
                 elt.setAttribute("filesize", "" + fileSize);
             mInfo.addContent(elt);
         }
 
         try {
 
             // Parse the font 
             mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Font file name " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1368", new Object[] {location})
 );
             FlashFile fontFile = FlashFile.parse( location );
             Enumeration defs = fontFile.definitions();
             FontDef fontDef = (FontDef)defs.nextElement();
 
             // Copy the font
             // For now, add the entire font (including "layout" info)
             fontDef.getFont().copyTo(font);
 
             if ((font.flags & Font.SHIFT_JIS) != 0) {
                 throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't handle SHIFT_JIS font: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1385", new Object[] {fileName})
                                 );
             }
             // Make sure font has LAYOUT info 
             if ((font.flags & Font.HAS_LAYOUT) == 0) {
                 throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes=p[0] + " has no layout information."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1396", new Object[] {fileName})
                                 );
             }
 
             // Put in our face name
             font.fontName = face;
 
             // Clean out existing styles.
             font.flags &= ~(Font.BOLD | Font.ITALIC);
 
             // Write in ours.
             if ((styleBits & FontInfo.BOLD) != 0) {
                 font.flags |= Font.BOLD;
             }
             if ((styleBits & FontInfo.ITALIC) != 0) {
                 font.flags |= Font.ITALIC;
             }
 
             // FIXME: [OLD bloch] debugging shouldn't go to system.err; should log!
             // need adapter class for logger for that
             if (mProperties.getProperty("trace.fonts", "false").equals("true")) {
                  font.printContent(System.err, "  ", 0);
             }
 
             // Add to the list of fonts
             FontDef fdef = mFontsCollector.addFont(font, null);
             // For now, write all characters.
             fdef.setWriteAllChars(true);
             fdef.setWriteLayout(true);
 
         } catch (IVException e) {
             throw new ChainedException(e);
         }
 
         return font;
     }
 
     /**
      * Import all action script blocks
      */
      void importActions(String fileName) 
          throws FileNotFoundException, IVException {
 
          Timeline t = FlashFile.parse(fileName).getMainScript().getTimeline();
 
          for(int i = 0; i < t.getFrameCount(); i++) {
              Frame frame = t.getFrameAt(i);
              for(int j = 0; j < frame.size(); j++) {
                  FlashObject o = frame.getFlashObjectAt(j);
                  if (o instanceof DoAction) {
                      DoAction action = (DoAction)o;
                      addProgram(action.getProgram());
                  }
              }
          }
      }
 
     /**
      * @return first action block
      */
      DoAction getFirstDoAction(String fileName) 
          throws FileNotFoundException, IVException {
 
          Timeline t = FlashFile.parse(fileName).getMainScript().getTimeline();
 
          for(int i = 0; i < t.getFrameCount(); i++) {
              Frame frame = t.getFrameAt(i);
              for(int j = 0; j < frame.size(); j++) {
                  FlashObject o = frame.getFlashObjectAt(j);
                  if (o instanceof DoAction) {
                      return (DoAction)o;
                  }
              }
          }
          return null;
      }
 
      /**
       * Adds font information to the canvas
       */
      private void addFontTable() {
 
         if (mFontTableIndex == -1) {
             mLogger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="No canvas.  Skipping font table"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1485")
 );
             return;
         }
         Enumeration fonts = mFontManager.getNames();
         StringBuffer actions = new StringBuffer("");
         while(fonts.hasMoreElements()) {
             // TODO: [old bloch] Add assert that the canvas has been created
             // before this action script is added!
             String name = (String)fonts.nextElement();
             FontFamily family = mFontManager.getFontFamily(name);
             mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Adding font family: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-1502", new Object[] {name})
 );
 
             actions.append("_root.LzFontManager.addFont('" + name + "', " );
                     
             appendFont(actions, family.plain, family.getBounds(FontInfo.PLAIN));
             actions.append(",");
             appendFont(actions, family.bold, family.getBounds(FontInfo.BOLD));
             actions.append(",");
             appendFont(actions, family.italic, family.getBounds(FontInfo.ITALIC));
             actions.append(",");
             appendFont(actions, family.bitalic, family.getBounds(FontInfo.BOLDITALIC));
             actions.append("\n)\n");
         }
 
         if (mProperties.getProperty("trace.fonts", "false").equals("true")) {
             mLogger.debug(actions.toString());
         }
 
         byte[] action = ScriptCompiler.compileToByteArray(actions.toString(), mProperties);
         Program program = new Program(action, 0, action.length);
 
         // Add the program right in the spot where it belongs
         Frame frame = mFlashFile.getMainScript().getFrameAt(mLastFrame);
         frame.setFlashObjectAt(new DoAction(program), mFontTableIndex);
     }
 
      /**
       * Adds resource information to the canvas
       */
      private void addResourceTable() {
 
          StringBuffer buf = new StringBuffer("");
          // Sort the keys, so that regression tests aren't sensitive to
          // the undefined order of iterating a (non-TreeSet) Set.
          SortedSet sset = new TreeSet(mMultiFrameResourceSet);
          Iterator resources = sset.iterator();
          while(resources.hasNext()) {
              Resource res = (Resource)resources.next();
              String str = "canvas.resourcetable[\"" + res.getName() + 
                     "\"]={ width : " + res.getWidth() + ", height :" + 
                            res.getHeight() + "};\n";
              buf.append(str);
          }
 
          byte[] action = ScriptCompiler.compileToByteArray(buf.toString(), mProperties);
          Program program = new Program(action, 0, action.length);
 
          // Add the program right in the spot where it belongs
          Frame frame = mFlashFile.getMainScript().getFrameAt(mLastFrame);
          frame.setFlashObjectAt(new DoAction(program), mResourceTableIndex);
      }
 
     /**
      * @return height of fontinfo in pixels
      * @param fontInfo
      */
     double getFontHeight (FontInfo fontInfo) {
         return fontHeight(getFontFromInfo(fontInfo));
     }
 
     /**
      * @return lineheight which lfc LzInputText expects for a given fontsize
      */
     double getLFCLineHeight (FontInfo fontInfo, int fontsize) {
         return lfcLineHeight(getFontFromInfo(fontInfo), fontsize);
     }
 
     /**
      * Convert em units to pixels, truncated to 2 decimal places.
      * Slow float math... but simple code to read.
      *
      * @param units number of 1024 em units
      * @return pixels
      */
     private static double emUnitsToPixels(int units) {
         int x = (100 * units * DEFAULT_SIZE) / 1024;
         return (double)(x / 100.0);
     }
 
     /**
      * Compute font bounding box
      *
      * @param font
      */
     static double fontHeight(Font font) {
         if (font == null) { return 0; }
         double ascent  = emUnitsToPixels(font.ascent);
         double descent = emUnitsToPixels(font.descent);
         double leading = emUnitsToPixels(font.leading);
         double lineheight = ascent+descent+leading;
         return lineheight;
     }
 
     /**
      * Compute font bounding box
      *
      * @param font
      */
     double lfcLineHeight(Font font, int fontsize) {
         double ascent  = emUnitsToPixels(font.ascent);
         double descent = emUnitsToPixels(font.descent);
         //double leading = emUnitsToPixels(font.leading);
         double lineheight = mTextLeading + ((ascent+descent) * ((double)fontsize) / DEFAULT_SIZE);
         return lineheight;
     }
 
     /**
      * Appends font to actionscript string buffer
      * @param actions string
      * @param font font
      */
     private static void appendFont(StringBuffer actions, Font font,
         Rectangle2D[] bounds) {
 
         final String newline = "\n  ";
         actions.append(newline);
 
         if (font == null) {
             actions.append("null");
             return;
         }
 
         double ascent  = emUnitsToPixels(font.ascent);
         double descent = emUnitsToPixels(font.descent);
         double leading = emUnitsToPixels(font.leading);
 
         final String comma = ", ";
 
         actions.append("{");
         actions.append("ascent:");
         actions.append(ascent);
         actions.append(comma);
         actions.append("descent:");
         actions.append(descent);
         actions.append(comma);
         actions.append("leading:");
         actions.append(leading);
         actions.append(comma);
         actions.append("advancetable:");
 
         int idx, adv;
             
         actions.append(newline);
         actions.append("[");
         // FIXME: [2003-03-19 bloch] We only support ANSI 8bit (up to
         // 255) char encodings.  We lose the higher characters is
         // UNICODE and we don't support anything else.
 
         for(int i = 0; i < 256; i++) {
             idx = font.getIndex(i);
             adv = font.getAdvanceValue(idx);
 
             // Convert to pixels rounded to nearest 100th
             double advance = emUnitsToPixels(adv);
             actions.append(advance);
             if (i != 255) {
                 actions.append(comma);
             }
 
             if (i%10 == 9) {
                 actions.append(newline);
             }
         }
         actions.append("],");
         actions.append(newline);
 
         actions.append("lsbtable:");
         actions.append(newline);
         actions.append("[");
 
         int m;
         int max;
         int adj;
         for(int i = 0; i < 256; i++) {
             idx = font.getIndex(i);
             try {
                 m = (int)bounds[idx].getMinX();
                 //max = (int)bounds[idx].getMaxX();
             } catch (Exception e) {
                 m = 0;
                 //max = 0;
             }
             adv = font.getAdvanceValue(idx);
             adj = m;
             if (adj < 0) adj = 0;
 
             /* The following makes the lsb bigger
                but is strictly wrong */
             /*max = max - adv;
             if (max < 0) max = 0;
             
             if (max > adj) {
                 adj = max;
             }*/
             
             // Convert to pixels rounded to nearest 100th
             double lsb = emUnitsToPixels(adj);
             actions.append(lsb);
             if (i != 255) {
                 actions.append(comma);
             }
 
             if (i%10 == 9) {
                 actions.append(newline);
             }
         }
 
         actions.append("],");
 
         actions.append(newline);
         actions.append("rsbtable:");
         actions.append(newline);
         actions.append("[");
 
         for(int i = 0; i < 256; i++) {
             idx = font.getIndex(i);
             try {
                 m = (int)bounds[idx].getMaxX();
             } catch (Exception e) {
                 m = 0;
             }
             adv = font.getAdvanceValue(idx);
             adj = m - adv;
             if (adj < 0) adj = 0;
             
             // Convert to pixels rounded to nearest 100th
             double rsb = emUnitsToPixels(adj);
             actions.append(rsb);
             if (i != 255) {
                 actions.append(comma);
             }
 
             if (i%10 == 9) {
                 actions.append(newline);
             }
         }
 
         actions.append("]}");
     }
 
 
     /**
      * @return font given a font info
      */
     private Font getFontFromInfo(FontInfo fontInfo) {
         // This will bring in the default bold ofnt if it's not here yet
         checkFontExists(fontInfo);
         String fontName = fontInfo.getName();
         FontFamily family   = mFontManager.getFontFamily(fontName);
         String style = fontInfo.getStyle();
 
         if (family == null) {
             return null;
             /*
             throw new CompilationError("Font '" + fontName + 
                 "' used but not defined");
             */
         }
         Font font = family.getStyle(fontInfo.styleBits);
         if (font == null) {
             throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Font '" + p[0] + "' style ('" + p[1] + "') used but not defined"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-2089", new Object[] {fontName, style})
                                         );
         }
         return font;
     }
 
     /**
      * @return true if the font exists
      *
      * If this is the default bold font and it hasn't been 
      * declared, import it.
      */
     boolean checkFontExists(FontInfo fontInfo) {
 
         // Bulletproofing...
         if (fontInfo.getName() == null) {
             return false;
         }
  
         boolean a = mFontManager.checkFontExists(fontInfo);
         if (a) {
             return a;
         }
 
         if (fontInfo.getName().equals(mDefaultFontName) && 
             fontInfo.styleBits == FontInfo.PLAIN) {
             try {
                     File f = mEnv.resolve(mDefaultFontFileName, null);
                     importFontStyle(f.getAbsolutePath(), mDefaultFontName, "plain", mEnv);
                 } catch (FileNotFoundException fnfe) {
                     throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="default font " + p[0] + " missing " + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-2125", new Object[] {mDefaultFontFileName, fnfe})
                                         );
             }
             return true;
         }
 
         if (fontInfo.getName().equals(mDefaultFontName) &&
             fontInfo.styleBits == FontInfo.BOLD) {
             try {
                 File f = mEnv.resolve(mDefaultBoldFontFileName, null);
                 importFontStyle(f.getAbsolutePath(), mDefaultFontName, "bold", mEnv);
             } catch (FileNotFoundException fnfe) {
                 throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="default bold font " + p[0] + " missing " + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-2143", new Object[] {mDefaultBoldFontFileName, fnfe})
                                                 );
             }
             return true;
         } 
 
         if (fontInfo.getName().equals(mDefaultFontName) &&
             fontInfo.styleBits == FontInfo.ITALIC) {
             try {
                 File f = mEnv.resolve(mDefaultItalicFontFileName, null);
                 importFontStyle(f.getAbsolutePath(), mDefaultFontName, "italic", mEnv);
             } catch (FileNotFoundException fnfe) {
                 throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="default italic font " + p[0] + " missing " + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-2161", new Object[] {mDefaultItalicFontFileName, fnfe})
                                                 );
             }
             return true;
         } 
 
         if (fontInfo.getName().equals(mDefaultFontName) &&
             fontInfo.styleBits == FontInfo.BOLDITALIC) {
             try {
                 File f = mEnv.resolve(mDefaultBoldItalicFontFileName, null);
                 importFontStyle(f.getAbsolutePath(), mDefaultFontName, "bold italic", mEnv);
             } catch (FileNotFoundException fnfe) {
                 throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="default bold italic font " + p[0] + " missing " + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SWFWriter.class.getName(),"051018-2179", new Object[] {mDefaultBoldItalicFontFileName, fnfe})
                                                 );
             }
             return true;
         } 
 
         return false;
     }
 }
