 /*****************************************************************************
  * DHTMLWriter.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
  * Copyright 2001-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
  * Use is subject to license terms.                                            *
  * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.utils.ListFormat;
 import org.openlaszlo.compiler.CompilationEnvironment;
 import org.openlaszlo.compiler.ObjectWriter.ImportResourceError;
 import org.openlaszlo.compiler.ObjectWriter.Resource;
 
 import org.openlaszlo.iv.flash.api.FlashDef;
 
 import org.openlaszlo.media.*;
 
 import java.io.*;
 import java.util.*;
 import java.lang.Math;
 import java.lang.Character;
 
 import org.jdom.Element;
 
 // jgen 1.4
 import java.awt.geom.Rectangle2D;
 
 import org.apache.log4j.*;
 
 /** Accumulates code, XML, and assets to a DHTML object file.
  *
  * Properties documented in Compiler.getProperties.
  */
 class DHTMLWriter extends ObjectWriter {
 
 
     static final String localResourceDir = "lps/resources";
 
     // Accumulate script here, to pass to script compiler
     protected PrintWriter scriptWriter = null;
     protected StringWriter scriptBuffer = null;
 
     // List of declarations of resources
     protected StringBuffer mResourceDefs = new StringBuffer();
 
     /** Logger */
     protected static Logger mLogger = org.apache.log4j.Logger.getLogger(DHTMLWriter.class);
 
     DHTMLWriter(Properties props, OutputStream stream,
                 CompilerMediaCache cache,
                 boolean importLibrary,
                 CompilationEnvironment env) {
 
         super(props, stream, cache, importLibrary, env);
 
         scriptBuffer = new StringWriter();
         scriptWriter= new PrintWriter(scriptBuffer);
 
     }
 
 
     /**
      * Sets the canvas for the app
      *
      * @param canvas
      * 
      */
     void setCanvas(Canvas canvas, String canvasConstructor) {
         scriptWriter.println(canvasConstructor);
     }
 
     void setCanvasDefaults(Canvas canvas, CompilerMediaCache mc) { };
 
 
 
     public int addScript(String script) {
         scriptWriter.println(script);
         return script.length();
     }
 
 
     public void importPreloadResource(File fileName, String name) 
         throws ImportResourceError
     {
     }
 
     public void importPreloadResource(String fileName, String name) 
         throws ImportResourceError
     {
     }
 
     /** Import a multiframe resource into the current movie.  Using a
      * name that already exists clobbers the old resource (for now).
      */
     public void importPreloadResource(List sources, String name, File parent)
         throws ImportResourceError
     {
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
         importResource(new File(fileName), name);
     }
 
     public void importResource(File inputFile, String name)
         throws ImportResourceError
     {
         // Moved this conversion from below.
         try {
             inputFile = inputFile.getCanonicalFile(); //better be ok if this does not yet exist. changed from getcanonicalPath to getCanonicalFile
         } catch (java.io.IOException e) {
             throw new ImportResourceError(inputFile.toString(), e, mEnv);
         }
 
         File[] sources;
         List outsources = new ArrayList();
         mLogger.debug("DHTMLWriter: Importing resource: " + name);
         if (inputFile.isDirectory()) {
             //mLogger.debug("DHTMLWriter Is directory: " + inputFile.toString());    
             sources = inputFile.listFiles();
             //mLogger.debug("DHTMLWriter: "+inputFile.toString()+" is a directory containing "+ sources.length +" files.");    
             for (int i = 0; i < sources.length; i++) {
                 // Construct path from directory and file names.
                 /* TODO: In theory, file resolution might get files that come from somewhere on the file system that doesn't match where
                         things will be on the server. Part of the root path may differ, and this File doesn't actually have to correspond 
                         to a file on the server disk. Thus the path is reconstructed here.
                         That said, the current code isn't actually abstracting the path here.
                         This will actually come into play when the '2 phase' file resolution currently occuring is
                         compacted to a single phase. To do this, more resource and file descriptor information will have
                         to be maintained, either in a global table or using a more abstract path structure, or both.
                         For now I'll leave this as it is. [pga]
                 */
                 String sFname = inputFile.toString() + File.separator + sources[i].getName();
                 if (isFileValidForImport(sFname)) {
                     outsources.add(sFname);
                 }
             }
             importResource(outsources, name, null);
             return;
         } else if (!inputFile.exists()) {
             // This case is supposed to handle multiframe resources, as is the one above.
             sources = FileUtils.matchPlusSuffix(inputFile);
             for (int i = 0; i < sources.length; i++) {
                 // Construct path from directory and file names. (see comment above [pga])
                 File fDir = inputFile.getParentFile();
                 String sFname = fDir.toString() + File.separator + sources[i].getName();
                 if (isFileValidForImport(sFname)) {
                     outsources.add(sFname);
                 }
             }
             importResource(outsources, name, null);
             return;
         }
 
         // Conversion to canonical path was here.
         org.openlaszlo.iv.flash.api.FlashDef def = null;
 
         File dirfile = mEnv.getApplicationFile().getParentFile();
         String[] fileInfo = getRelPath(inputFile);
         String pType = fileInfo[0];
         String relPath = fileInfo[1];
 
         // make it relative and watch out, it comes back canonicalized with forward slashes.
         // Comparing to file.separator is wrong on the pc.
          if (relPath.charAt(0) == '/') {
                  relPath = relPath.substring(1);
         }
 
          // If this is a "external" resource and copy-resources flag
          // is set, make a copy of the resource file to bundle with the app.
          // (Note: the SOLO DHTML deploy script used to be responsible or this).
          if (pType.equals("sr") && mEnv.getBooleanProperty(mEnv.COPY_RESOURCES_LOCAL)) {
              // If this is a "sr" (server-root-relative) path, make a local copy in the
              // localResourceDir
              copyResourceFile(inputFile, dirfile, relPath);
          }
 
         StringBuffer sbuf = new StringBuffer("LzResourceLibrary." + 
                                              name + "={ptype: \"" + pType + "\", frames:[");
         sbuf.append("'"+relPath+"'");
 
         Resource res =  (Resource)mResourceMap.get(inputFile.toString());
         boolean oldRes = (res != null);
         if (!oldRes) {
             // Get the resource and put in the map
             res = getResource(inputFile.toString(), name);
             mLogger.debug("Building resource map; res: "+ res + ", relPath: "+ relPath +", fileName: "+inputFile.toString());
             mResourceMap.put(relPath, res); //[pga] was fileName
             def = res.getFlashDef();
             def.setName(name);
         } else {
             def = res.getFlashDef();
             // Add an element with 0 size, since it's already there.
             Element elt = new Element("resource");
             elt.setAttribute("name", name);
             // elt.setAttribute("mime-type", MimeType.MP3); 
             elt.setAttribute("source", relPath); //[pga] was fileName
             elt.setAttribute("filesize", "0");
             mInfo.addContent(elt);
         } 
         
         Rectangle2D b = def.getBounds();
             
         if (b != null) {
             sbuf.append("],width:" + (b.getWidth() / 20));
             sbuf.append(",height:" + (b.getHeight() / 20));
         } else { 
             // could be an mp3 resource
             sbuf.append("]");
         }
         sbuf.append("};");
         mResourceDefs.append(sbuf.toString());
     }
 
     public void addResourceDefs () {
         addScript(mResourceDefs.toString());
     }
     public void importResource(List sources, String sResourceName, File parent)
     {
         writeResourceLibraryDescriptor(sources, sResourceName, parent);
     }
     
     /* Write resource descriptor library */
     public void writeResourceLibraryDescriptor(List sources, String sResourceName, File parent)
     {
         mLogger.debug("Constructing resource library: " + sResourceName);
         int width = 0;
         int height = 0;
         int fNum = 0;
         String sep = "";
         boolean first = true;
         // Initialize the temporary buffer.
         StringBuffer sbuf= new StringBuffer("");
         if (sources.isEmpty()) {
             return;
         }
         String pType;
         String relPath;
         File dirfile = mEnv.getApplicationFile().getParentFile();
 
         for (Iterator e = sources.iterator() ; e.hasNext() ;) {
             File fFile = new File((String)e.next());
             String[] fileInfo = getRelPath(fFile);
             pType = fileInfo[0];
             relPath = fileInfo[1];
 
             if (first == true) {
                 sbuf.append("LzResourceLibrary." + sResourceName + "={ptype: \"" + pType + "\", frames:[");
                 first = false;
             }
 
             mLogger.debug("relFile is: "+relPath);
 
             // If this is a "external" resource and copy-resources flag
             // is set, make a copy of the resource file to bundle with the app.
             // (Note: the SOLO DHTML deploy script used to be responsible or this).
             if (pType.equals("sr") && mEnv.getBooleanProperty(mEnv.COPY_RESOURCES_LOCAL)) {
                 // If this is a "sr" (server-root-relative) path, make a local copy in the
                 // localResourceDir
                 copyResourceFile(fFile, dirfile, relPath);
             }
 
             sbuf.append(sep+"'"+relPath+"'");
             sep = ",";
             // Definition to add to the library (without stop)
             Resource res = getMultiFrameResource(fFile.toString(), sResourceName, fNum);
             int rw = res.getWidth();
             int rh = res.getHeight();
             if (rw > width) {
                 width = rw;
             }
             if (rh > height) {
                 height = rh;
             }
             fNum++;
         }
         mMultiFrameResourceSet.add(new Resource(sResourceName, width, height));
         sbuf.append("],width:" + width);
         sbuf.append(",height:" + height);
 
         // create a montage for multiframe resources
         if (sources.size() > 1) {
             String montageFile = (String)sources.get(0);
            // offset of one character to the left of the extension, e.g. '.png'
            int extoffset = montageFile.lastIndexOf(FileUtils.getExtension(montageFile)) - 1;
            montageFile = montageFile.substring(0, extoffset) + mEnv.IMAGEMONTAGE_STRING + montageFile.substring(extoffset + 1);
             try {
                 ImageMontageMaker.assemble(sources, montageFile);
                 String[] fileInfo = getRelPath(new File(montageFile));
                 relPath = fileInfo[1];
                 pType = fileInfo[0];
 
                 if (pType.equals("sr") && mEnv.getBooleanProperty(mEnv.COPY_RESOURCES_LOCAL)) {
                     // If this is a "sr" (server-root-relative) path, make a local copy in the
                     // localResourceDir
                     copyResourceFile(new File(montageFile), dirfile, relPath);
                 }
                 sbuf.append(",sprite:'" + relPath + "'");
             } catch (Exception e) {
                 mLogger.error("Assembling css sprite: " + sources + ", " + e);
             }
         }
         sbuf.append("};");
         mResourceDefs.append(sbuf.toString());
     }
 
     private String[] getRelPath(File fFile) { 
         try {
             File dirfile = mEnv.getApplicationFile().getParentFile();
             String appdir = new File(dirfile != null ? dirfile.getPath() : ".").getCanonicalPath();
             //File appHomeParent = new File(LPS.getHomeParent());
             String sHome = LPS.HOME();
             String arPath;
             String pType;
             String relPath;
 
             String appRelativePrefix = FileUtils.findMaxCommonPrefix(fFile.toString(), appdir);
             String LPSRelativePrefix = FileUtils.findMaxCommonPrefix(fFile.toString(), sHome);
 
             String prefix;
 
             //appRelativePrefix.length() > LPSRelativePrefix.length() &&
             if (fFile.toString().startsWith(appdir)) {
                 prefix = appRelativePrefix;
                 pType = "ar";}
             else {
                 prefix = LPSRelativePrefix;
                 pType = "sr";
             }
 
             relPath = FileUtils.relativePath(fFile, prefix);
 
             // make it relative and watch out, it comes back canonicalized with forward slashes.
             // Comparing to file.separator is wrong on the pc.
             if (relPath.charAt(0) == '/') {
                 relPath = relPath.substring(1);
             }
             mLogger.debug("relPath is: "+relPath);
 
             String[] out = {pType, relPath};
             return out;
         } catch (java.io.IOException e) {
             throw new ImportResourceError(fFile.toString(), e, mEnv);
         }
     }
 
     public void close() throws IOException { 
         //Should we emit javascript or SWF?
         //boolean emitScript = mEnv.isDHTML();
 
         if (mCloseCalled) {
             throw new IllegalStateException("DHTMLWriter.close() called twice");
         }
         
         // special case for IE7, need to copy lps/includes/blank.gif to lps/resources/lps/includes/blank.gif
         if (mEnv.getBooleanProperty(mEnv.COPY_RESOURCES_LOCAL)) {
             File inputFile = new File(LPS.HOME() + "/lps/includes/blank.gif");
             File dirfile = mEnv.getApplicationFile().getParentFile();
             copyResourceFile(inputFile, dirfile, "lps/includes/blank.gif");
         }
 
         addResourceDefs();
 
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
 
         try { 
             Properties props = (Properties)mProperties.clone();
             scriptWriter.close();
             byte[] objcode = ScriptCompiler.compileToByteArray(scriptBuffer.toString(), props);
             InputStream input = new ByteArrayInputStream(objcode);
             mLogger.debug("compiled DHTML code is "+new String(objcode));
             FileUtils.send(input, mStream);
         } catch (org.openlaszlo.sc.CompilerException e) {
             throw new CompilationError(e);
         } catch (Exception e) {
             throw new ChainedException(e);
         }
 
         mCloseCalled = true;
     }
 
     public void openSnippet(String url) throws IOException {
         this.liburl = url;
     }
 
     public void closeSnippet() throws IOException {
         addResourceDefs();
 
         // Callback to let library know we're done loading
         addScript("LzLibrary.__LZsnippetLoaded('"+this.liburl+"')");
 
         if (mCloseCalled) {
             throw new IllegalStateException("DHTMLWriter.close() called twice");
         }
 
         try { 
             Properties props = (Properties)mProperties.clone();
             scriptWriter.close();
             byte[] objcode = ScriptCompiler.compileToByteArray(scriptBuffer.toString(), props);
             InputStream input = new ByteArrayInputStream(objcode);
             mLogger.debug("compiled DHTML code is "+new String(objcode));
             FileUtils.send(input, mStream);
         } catch (org.openlaszlo.sc.CompilerException e) {
             throw new CompilationError(e);
         } catch (Exception e) {
             throw new ChainedException(e);
         }
 
         mCloseCalled = true;
     }
 
     /* [todo 2006-02-09 hqm] These methods are to be compatible with
        SWF font machinery -- this should get factored away someday so that the FontCompiler
        doesn't try to do anything with <font> tags in DHTML, (except maybe make aliases for them?)
     */
     FontManager getFontManager() {
         //        mEnv.warn("DHTML runtime doesn't support FontManager API");
         return null;
     }
 
     public boolean isDeviceFont(String face) {
         return true;
     }
 
     public void setDeviceFont(String face) {}
     public void setFontManager(FontManager fm) {}
 
     public void importFontStyle(String fileName, String face, String style,
                                 CompilationEnvironment env)
         throws FileNotFoundException, CompilationError {
         env.warn("DHTMLWriter does not support importing fonts");
     }
 
 
     void addPreloaderScript(String script) { } ;
     void addPreloader(CompilationEnvironment env) { } ;
 
     public void importBaseLibrary(String library, CompilationEnvironment env) {
         env.warn("DHTMLWriter does not implement importBaseLibrary");
     }
 
     public String importClickResource(File file) throws ImportResourceError {
         mEnv.warn( "clickregion not implemented by DHTMLWriter");
         return("DHTMLWriter clickregions not implemented");
     }
 
 
 
 /** Get a resource descriptor without resource content.
 *
 * @param name name of the resource
 * @param fileName file name of the resource
 * @param stop include stop action if true
 * 
 */
 //  TODO: Required for performance improvement. So far not differentiated from ObjectWriter version.
     protected Resource getResource(String fileName, String name, boolean stop)
         throws ImportResourceError
     {
         try {
                 String inputMimeType = MimeType.fromExtension(fileName);
                 if (!Transcoder.canTranscode(inputMimeType, MimeType.SWF) 
                                 && !inputMimeType.equals(MimeType.SWF)) {
                         inputMimeType = Transcoder.guessSupportedMimeTypeFromContent(fileName);
                         if (inputMimeType == null || inputMimeType.equals("")) {
                                 throw new ImportResourceError(fileName, new Exception(
                                                 /* (non-Javadoc)
                                                  * @i18n.test
                                                  * @org-mes="bad mime type"
                                                  */
                                                 org.openlaszlo.i18n.LaszloMessages.getMessage(
                                                                 ObjectWriter.class.getName(),"051018-549")
                                 ), mEnv);
                         }
                 }
                 // No need to get these from the cache since they don't need to be
                 // transcoded and we usually keep the cmcache on disk.
                 if (inputMimeType.equals(MimeType.SWF)) {
 
                         long fileSize =  FileUtils.getSize(new File(fileName));
 
                         Element elt = new Element("resource");
                         elt.setAttribute("name", name);
                         elt.setAttribute("mime-type", inputMimeType);
                         elt.setAttribute("source", fileName);
                         elt.setAttribute("filesize", "" + fileSize);
                         mInfo.addContent(elt);
 
                         return importSWF(fileName, name, false);
                 }
 
                 // TODO: [2002-12-3 bloch] use cache for mp3s; for now we're skipping it 
                 // arguably, this is a fixme
                 if (inputMimeType.equals(MimeType.MP3) || 
                                 inputMimeType.equals(MimeType.XMP3)) {
                         return importMP3(fileName, name);
                 }
 
                 File inputFile = new File(fileName);
                 File outputFile = mCache.transcode(inputFile, inputMimeType, MimeType.SWF);
                 mLogger.debug(
                                 /* (non-Javadoc)
                                  * @i18n.test
                                  * @org-mes="importing: " + p[0] + " as " + p[1] + " from cache; size: " + p[2]
                                  */
                                 org.openlaszlo.i18n.LaszloMessages.getMessage(
                                                 ObjectWriter.class.getName(),"051018-584", new Object[] {fileName, name, new Long(outputFile.length())})
                 );
 
                 long fileSize =  FileUtils.getSize(outputFile);
 
                 Element elt = new Element("resource");
                 elt.setAttribute("name", name);
                 elt.setAttribute("mime-type", inputMimeType);
                 elt.setAttribute("source", fileName);
                 elt.setAttribute("filesize", "" + fileSize);
                 mInfo.addContent(elt);
 
                 return importSWF(outputFile.getPath(), name, stop);
         } catch (Exception e) {
                 mLogger.error(
                                 /* (non-Javadoc)
                                  * @i18n.test
                                  * @org-mes="Can't get resource " + p[0]
                                  */
                                 org.openlaszlo.i18n.LaszloMessages.getMessage(
                                                 ObjectWriter.class.getName(),"051018-604", new Object[] {fileName})
                 );
                 throw new ImportResourceError(fileName, e, mEnv);
         }
 
     }
 
     /**
        Copies a file, INPUTFILE, to the file RELPATH relative to the
        system-generated application local resources directory in
        DESTDIR/lps/resources.
      */
     void copyResourceFile(File inputFile, File destdir, String relPath) {
         try {
             File resourceDirRoot = new File(destdir, localResourceDir);
             File resCopy = new File(resourceDirRoot, relPath);
             // Ensure that parent directories exist down to the file
             File dir = resCopy.getParentFile();
             if (dir != null)
                 dir.mkdirs();
             // Make copy of file
             FileUtils.send(new FileInputStream(inputFile), new FileOutputStream(resCopy));
 
         }
         catch (IOException e) {
             mLogger.error("Error copying resource file in DHTMLWriter "+e.getMessage());
             throw new ImportResourceError(inputFile.toString(), e, mEnv);
         }
     }
 
 }
 
