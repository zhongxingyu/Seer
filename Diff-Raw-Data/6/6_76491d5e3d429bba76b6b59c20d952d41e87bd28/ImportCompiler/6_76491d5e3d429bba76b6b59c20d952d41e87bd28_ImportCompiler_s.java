 /* *****************************************************************************
  * ImportCompiler.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import java.io.*;
 import java.util.*;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.openlaszlo.xml.internal.XMLUtils;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.utils.ChainedException;
 import org.apache.log4j.*;
 /** Compiler for <code>import</code> elements.
  *
  * @author  Henry Minsky
  */
 class ImportCompiler extends ToplevelCompiler {
     final static String HREF_ANAME = "href";
     final static String NAME_ANAME = "name";
     
     private static Logger mLogger  = Logger.getLogger(ImportCompiler.class);
 
     ImportCompiler(CompilationEnvironment env) {
         super(env);
     }
 
     static boolean isElement(Element element) {
         return element.getName().equals("import");
     }
 
     /** Return the difference of two strings.
      *  stringDiff("foo/bar/baz.lzx", "foo/bar") ==> /baz.lzx
      */
     static String stringDiff(String s1, String s2) {
         int sl1 = s1.length();
         int sl2 = s2.length();
         return s1.substring(sl2+1, sl1);
     }
 
     public void compile(Element element) throws CompilationError
     {
         String href = XMLUtils.requireAttributeValue(element, HREF_ANAME);
         String libname = XMLUtils.requireAttributeValue(element, NAME_ANAME);
         String stage = XMLUtils.requireAttributeValue(element, "stage");
 
         mLogger.debug("ImportCompiler.compile libname="+libname+", href="+href+", stage="+stage);
 
         Element module = LibraryCompiler.resolveLibraryElement(
             element, mEnv, mEnv.getImportedLibraryFiles());
         if (module != null) {
             // check for conflict in the value of the "proxied"
             // attribute declared on the <import> tag vs the
             // <library> tag.
 
             String libproxied = module.getAttributeValue("proxied", "inherit");
             String importproxied = element.getAttributeValue("proxied", "inherit");
 
             if ((importproxied.equals("true") && libproxied.equals("false")) ||
                 (importproxied.equals("false") && libproxied.equals("true"))){
                 mEnv.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The value of the 'proxied' attribute on this library, '" + p[0] + "', conflicts with the import element value of '" + p[1] + "'"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ImportCompiler.class.getName(),"051018-71", new Object[] {libproxied, importproxied})
                                 , element);
             }
                 
             // We're not compiling this into the current app, we're
             // building a separate binary library object file for it.
             File appdir = mEnv.getApplicationFile().getParentFile();
             File libsrcfile = mEnv.resolveReference(element, HREF_ANAME, true);
             String adjustedhref = libsrcfile.getPath();
 
             if (appdir != null) {
                 adjustedhref = FileUtils.relativePath(libsrcfile.getPath(), appdir.getPath());
             }
 
 
             // If we added a "library.lzx" to the path, set it properly in the XML
             element.setAttribute(HREF_ANAME, adjustedhref);
 
             // I'm scared of the CompilationManager, just generate the output file
             // directly for now.
             String libfile = libsrcfile.getName();
             String libprefix = mEnv.getLibPrefix();
             String runtime = mEnv.getProperty(mEnv.RUNTIME_PROPERTY);
            String extension = ".swf";
            if (mEnv.isAS3()) {
                extension = ".swf";
             } else if (Compiler.SCRIPT_RUNTIMES.contains(runtime)) {
                 extension = ".js";
             } 
             String objfilename = libprefix + "/" + libfile + extension;
             String objpath = mEnv.getLibPrefixRelative() + "/" + libfile + extension;
 
             mLogger.info(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="calling compilerLibrary libsrcfile=" + p[0] + " objfile=" + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ImportCompiler.class.getName(),"051018-103", new Object[] {libsrcfile, objfilename})
                                         );
 
             try {
                 FileUtils.makeFileAndParentDirs(new File(objfilename));
             } catch (java.io.IOException e) {
                 throw new CompilationError(element, e);
             }
 
             if (mEnv.isAS3()) {
                 // In Flash 9/10 we compile the main app first, then compile the libraries
                 // against that generated source tree.
                 mLogger.debug("... queueing import lib compilation" +libsrcfile+", " +objfilename  +", "+ objpath+", "+module);
                 queueLibraryCompilation(libsrcfile, objfilename, objpath, module);
             } else {
                 compileLibrary(libsrcfile, objfilename, objpath, module);
             }
 
 
             // Emit code into main app to instantiate a LzLibrary
             // object, which implements the load() method.
             ViewCompiler vc =  new ViewCompiler(mEnv);
 
             // Override the href with a pointer to the library object-file build directory
             element.setAttribute("href", objpath);
             vc.compile(element);
         }
     }
 
     void updateSchema(Element element, ViewSchema schema, Set visited) {
         element = LibraryCompiler.resolveLibraryElement(element, mEnv, visited);
         if (element != null) {
             super.updateSchema(element, schema, visited);
         }
     }
 
 
 
     /**
      * Queue this library to be compiled. The actual compilation must
      * be done after the main application is compiled, so that the AS3
      * compiler can use it to link against (actually, just to check
      * the links).
      */
     void queueLibraryCompilation(File infile, String outfile, String liburl, Element element) {
         LibraryCompilation lc = new LibraryCompilation(this, infile, outfile, liburl, element);
         mEnv.getMainCompilationEnv().queueLibraryCompilation(lc);
     }
 
     /**
      * Compile a standalone binary library file with no canvas.
      */
     void compileLibrary(File infile, String outfile, String liburl, Element element) throws CompilationError{
         // copy fields from mEnv to new Environment
         CompilationEnvironment env =  new CompilationEnvironment(mEnv);
         CompilationErrorHandler errors = env.getErrorHandler();
         env.setApplicationFile(infile);
         Properties props = (Properties) env.getProperties().clone();
         byte[] action;
 
         try {
 
             OutputStream ostream = new FileOutputStream(outfile);
             try {
                 ObjectWriter writer;
 
                 String runtime = env.getProperty(env.RUNTIME_PROPERTY);
                 if (env.isAS3()) {
                     props.setProperty(org.openlaszlo.sc.Compiler.SWF9_LOADABLE_LIB, "true");
                     // Compile the main app and all libraries using the same temp as3 dir, so all
                     // definitions are easy to share when the flex compiler does link-checking.
                     props.setProperty(org.openlaszlo.sc.Compiler.REUSE_WORK_DIRECTORY, "true");
                     writer = new SWF9Writer(props, ostream, mEnv.getMediaCache(), false, env);
                 } else if (Compiler.SCRIPT_RUNTIMES.contains(runtime)) {
                     writer = new DHTMLWriter(props, ostream,
                                              env.getMediaCache(), false, env);
                 } else if (Compiler.SWF_RUNTIMES.contains(runtime)) {
                     // Set the "SWF8_LOADABLE_LIB" flag to true for this compiler
                     props.setProperty(org.openlaszlo.sc.Compiler.SWF8_LOADABLE_LIB, "true");
                     // Ensures that _level0 is prefixed where needed in snippets code
                     env.setGlobalPrefix("_level0.");
                     writer = new SWFWriter(props, ostream,
                                            env.getMediaCache(), false, env);
                 } else {
                     throw new CompilationError("runtime "+runtime+" not supported for generating an import library", element);
                 }
                     
                 env.setObjectWriter(writer);
                 // Set the main SWFWriter so we can output resources
                 // to the main app
                 env.setMainObjectWriter(mEnv.getGenerator());
 
                 // Mark it as an runtime-importable library.
                 env.setImportLib(true);
 
                 // We can embed swf fonts in libraries now
                 env.setEmbedFonts(true);
 
                 // copy the fontmanager from old env to new one.
                 writer.setFontManager(mEnv.getGenerator().getFontManager());
                 writer.setCanvasDefaults(mEnv.getCanvas(), mEnv.getMediaCache());
                 
                 writer.openSnippet(liburl);
 
                 env.compileScript("// BEGIN compiling <IMPORT> Library "+liburl+"\n");
 
                 // Note: canvas.initDone() resets the _lzinitialsubviews list, so
                 // that has to be called when the library finishes loading. This is currently
                 // done by LzLibraryLoader.snippetLoaded(), which is the callback
                 // that we emit at the end of our snippet file.
 
                 // Setting "level0" is a hack for loading SWF movie
                 // clips, which puts with (_level0) { ... } around
                 // every script block emitted by
                 // env.compileScript(). This is an attempt to get code
                 // in the snippet to act like it is being run in the
                 // main app.
                 //
                 // It's not a complete solution, anything that
                 // declares a global var needs to put _level0.xxx as a
                 // prefix. But this covers global var lookups inside
                 // of a library.
                 if (Compiler.SWF_RUNTIMES.contains(runtime)) {
                     ((SWFWriter) env.getGenerator()).setLevel0(true);                    
                 }
 
 
                 for (Iterator iter = element.getChildren().iterator();
                      iter.hasNext(); ) {
                     Element child = (Element) iter.next();
                     if (!NodeModel.isPropertyElement(child)) {
                         Compiler.compileElement(child, env);
 
                     }
                 }
                 // Now output the additions to the tag map.
                 outputTagMap(env);
 
                 if (Compiler.SWF_RUNTIMES.contains(runtime)) {
                     ((SWFWriter) env.getGenerator()).setLevel0(false);
                 }
 
                 ViewCompiler.checkUnresolvedResourceReferences (env);
                 
                 writer.closeSnippet();
                 env.compileScript("// FINISH compiling <IMPORT> Library "+liburl+"\n");
             } finally {
                 ostream.close();
             }
 
             /** Copy any compilation errors/warnings over to canvas's CompilationErrorHandler
              */
             mEnv.getErrorHandler().appendErrors(env.getErrorHandler());
 
         } catch (CompilationError e) {
             // TBD: e.initPathname(file.getPath());
             e.attachErrors(errors.getErrors());
             throw e;
             //errors.addError(new CompilationError(e.getMessage() + "; compilation aborted"));
             //throw errors.toCompilationError();
         } catch (org.openlaszlo.xml.internal.MissingAttributeException e) {
             /* The validator will have caught this, but if we simply
              * pass the error through, the vaildation warnings will
              * never be printed.  Create a new message that includes
              * them so that we get the source information. */
             errors.addError(new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes=p[0] + "; compilation aborted"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ImportCompiler.class.getName(),"051018-210", new Object[] {e.getMessage()})
 ));
             throw errors.toCompilationError();
         } catch (IOException e) {
             throw new CompilationError(element, e);
         }
 
     }
 }
 
