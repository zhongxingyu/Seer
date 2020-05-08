 package org.watermint.sourcecolon.prettify;
 
 import net.arnx.jsonic.JSON;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Script;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.tools.shell.Global;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Google Code Prettify Wrapper API.
  */
 public class Prettify {
     private Global global;
     private Context context;
     private Scriptable scope;
     private ByteArrayOutputStream log;
 
     /**
      * Constructor.
      *
      * @throws PrettifyException
      */
     public Prettify() throws PrettifyException {
         global = new Global();
         context = ContextFactory.getGlobal().enterContext();
         log = new ByteArrayOutputStream();
 
         global.init(context);
         clearLog();
 
         context.setOptimizationLevel(-1);
         context.setLanguageVersion(Context.VERSION_1_7);
 
         scope = context.initStandardObjects(global);
 
         try {
             loadScript(context, scope, "env.rhino.1.2.js");
             for (String name : getGoogleCodePrettifyScripts()) {
                 loadScript(context, scope, name);
             }
             loadScript(context, scope, "sourcecolon.js");
         } catch (IOException e) {
             throw new PrettifyException(e);
         }
     }
 
     /**
      * Prettify.
      *
      * @param code source code
      * @return prettified code.
      * @throws PrettifyException
      */
     public String prettify(String code) throws PrettifyException {
         return prettify(code, null);
     }
 
     /**
      * Prettify.
      *
      * @param code source code.
      * @param lang language hint.
      * @return prettified code.
      * @throws PrettifyException
      */
     public String prettify(String code, String lang) throws PrettifyException {
         String escapedCode = code.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("(\r\n|\n|\r)", "<br/>");
 
         Map<String, String> params = new HashMap<>();
         params.put("code", escapedCode);
         if (lang != null) {
             params.put("lang", lang);
         }
         String paramString = JSON.encode(params);
         String evaluate = "sourcecolon(" + paramString + ")";
 
         Object result = context.evaluateString(scope, evaluate, "<cmd>", 1, null);
 
         return Context.toString(result);
     }
 
     /**
      * Prettify.
      *
      * @param file source file.
      * @return prettified code.
      * @throws PrettifyException
      * @throws IOException
      */
     public String prettify(File file) throws PrettifyException, IOException {
         return prettify(file, null);
     }
 
     /**
      * Prettify.
      *
      * @param file source file.
      * @param lang language hint.
      * @return prettified code.
      * @throws PrettifyException
      * @throws IOException
      */
     public String prettify(File file, String lang) throws PrettifyException, IOException {
         StringBuilder code = new StringBuilder();
 
         try (Reader source = new FileReader(file)) {
             BufferedReader s = new BufferedReader(source);
             String line = null;
             while ((line = s.readLine()) != null) {
                 code.append(line);
                 code.append("\n");
             }
         }
 
         return prettify(code.toString(), lang);
     }
 
     /**
      * Console log of Rhino.
      *
      * @return console log.
      */
     public String getLog() {
         return log.toString();
     }
 
     /**
      * Clear console log.
      */
     public void clearLog() {
         log = new ByteArrayOutputStream();
         global.setOut(new PrintStream(log));
     }
 
     /**
      * Load script into the context.
      *
      * @param context context
      * @param scope   scope
      * @param name    script name.
      * @throws IOException
      */
     private void loadScript(Context context, Scriptable scope, String name) throws IOException {
        String path = "src/main/resources";

        try (Reader reader = new FileReader(new File(path + "/" + name))) {
             Script script = context.compileReader(reader, name, 1, null);
             script.exec(context, scope);
         }
     }
 
     /**
      * File names of google-code-prettify.
      *
      * @return file names.
      */
     private static String[] getGoogleCodePrettifyScripts() {
         return new String[]{"prettify.js", "lang-apollo.js", "lang-basic.js", "lang-clj.js", "lang-css.js", "lang-dart.js", "lang-erlang.js", "lang-go.js", "lang-hs.js", "lang-lisp.js", "lang-llvm.js", "lang-lua.js", "lang-matlab.js", "lang-ml.js", "lang-mumps.js", "lang-n.js", "lang-pascal.js", "lang-proto.js", "lang-r.js", "lang-rd.js", "lang-scala.js", "lang-sql.js", "lang-tcl.js", "lang-tex.js", "lang-vb.js", "lang-vhdl.js", "lang-wiki.js", "lang-xq.js", "lang-yaml.js"};
     }
 
     /**
      * Main for test.
      *
      * @param args arguments.
      * @throws Exception
      */
     public static void main(String... args) throws Exception {
         if (args.length < 1) {
             System.out.println("java org.watermint.sourcecolon.prettify.Prettify _file_ ...");
         } else {
             Prettify p = new Prettify();
 
             for (String file : args) {
                 System.out.println(p.prettify(new File(file)));
             }
         }
     }
 }
