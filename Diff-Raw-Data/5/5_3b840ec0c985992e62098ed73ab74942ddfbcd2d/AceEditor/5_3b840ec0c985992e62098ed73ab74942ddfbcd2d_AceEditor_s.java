 package play.modules.editor;
 
 import groovy.text.SimpleTemplateEngine;
 import groovy.text.Template;
 import org.apache.commons.lang.StringEscapeUtils;
 import play.Play;
 import play.classloading.ApplicationClasses;
 import play.exceptions.CompilationException;
 import play.libs.Files;
 import play.libs.IO;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.URLEncoder;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 import play.mvc.Http;
 import play.utils.HTML;
 import play.vfs.VirtualFile;
 
 public class AceEditor {
    // TODO : completion with tokens of the same page
    // TODO : Command-Clic
     // TODO : auto imports ???
     public static void index(Http.Request request, Http.Response response) throws Exception {
         String projectName = Play.configuration.getProperty("application.name", "Unknown");
         VirtualFile vf = Play.roots.get(0);
         List<SourceFile> files = getFiles(vf, new ArrayList<SourceFile>());
         Node node = new Node(vf.getName(), vf.getRealFile().getAbsolutePath(), true);
         getFileTree(vf, node);
         vf = vf.child("/app/controllers/Application.java");
         String src = "";
         String currentFile = "NONE";
         if (vf.exists()) {
             src = vf.contentAsString();
             currentFile = vf.getRealFile().getAbsolutePath();
         }
         String type = "ace/mode/java";
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("projectName", projectName);
         params.put("src", src);
         params.put("files", files);
         params.put("currentFile", currentFile);
         params.put("type", type);
         params.put("line", 0);
         params.put("node", node);
         params.put("prefix", Play.configuration.getProperty("http.path", ""));
         response.contentType = "text/html";
         for (VirtualFile f : Play.roots) {
             if (f.getName().equals("editor")) {
                 VirtualFile root = f;
                 root = root.child("/app/views/AceEditor/index.html");
                 renderGroovytemplate(root.getRealFile(), params, response.out);
                 return;
             }
         }
         byte[] bytes = "<h1>Error while loading editor template</h1><h4>Maybe the editor module isn't in a folder named editor.</h4>".getBytes("utf-8");
         response.out.write(bytes, 0, bytes.length);
     }
 
     public static void file(Http.Request request, Http.Response response, String path, int line) throws Exception {
         String projectName = Play.configuration.getProperty("application.name", "Unknown");
         VirtualFile vf = Play.roots.get(0);
         List<SourceFile> files = getFiles(vf, new ArrayList<SourceFile>());
         Node node = new Node(vf.getName(), vf.getRealFile().getAbsolutePath(), true);
         getFileTree(vf, node);
         File file = new File(path);
         String src = IO.readContentAsString(file).trim();
         String currentFile = file.getAbsolutePath();
         String type = type(file);
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("projectName", projectName);
         params.put("src", src);
         params.put("files", files);
         params.put("currentFile", currentFile);
         params.put("type", type);
         params.put("line", line);
         params.put("node", node);
         params.put("prefix", Play.configuration.getProperty("http.path", ""));
         response.contentType = "text/html";
         for (VirtualFile f : Play.roots) {
             if (f.getName().equals("editor")) {
                 VirtualFile root = f;
                 root = root.child("/app/views/AceEditor/index.html");
                 renderGroovytemplate(root.getRealFile(), params, response.out);
                 return;
             }
         }
         byte[] bytes = "<h1>Error while loading editor template</h1><h4>Maybe the editor module isn't in a folder named editor.</h4>".getBytes("utf-8");
         response.out.write(bytes, 0, bytes.length);
     }
 
     public static void save(Http.Request request, Http.Response response, String currentFile, String src) throws Exception {
         response.contentType = "text/plain";
         if (currentFile.equals("NONE")) {
             response.out.write("".getBytes("utf-8"), 0, 0);
         } else {
             File vf = new File(currentFile);
             IO.writeContent(src, vf);
             byte[] content = IO.readContentAsString(vf).trim().getBytes("utf-8");
             response.out.write(content, 0, content.length);
         }
     }
 
     public static void createFile(Http.Request request, Http.Response response, String name) throws Exception {
         new File(Play.roots.get(0).getRealFile().getAbsolutePath() + name).createNewFile();
         response.contentType = "text/plain";
         response.out.write("".getBytes("utf-8"), 0, 0);
     }
 
     public static void createDir(Http.Request request, Http.Response response, String name) throws Exception {
         new File(Play.roots.get(0).getRealFile().getAbsolutePath() + name).mkdirs();
         response.contentType = "text/plain";
         response.out.write("".getBytes("utf-8"), 0, 0);
     }
 
     public static void deleteFile(Http.Request request, Http.Response response, String name) throws Exception {
         Files.delete(new File(name));
         response.contentType = "text/plain";
         response.out.write("".getBytes("utf-8"), 0, 0);
     }
 
     public static void compile(Http.Request request, Http.Response response, String path, String source) throws Exception {
         response.contentType = "application/json";
         if (path.endsWith(".java")) {
             javaCompile(response, source, path);
         } else {
             byte[] res = "{\"compilation\":true}".getBytes("utf-8");
             response.out.write(res, 0, res.length);
         }
     }
 
     private static void javaCompile(Http.Response response, String source, String path) throws Exception {
         path = path.replace(Play.roots.get(0).getRealFile().getAbsolutePath() + "/", "");
         String dotPath = path.replaceAll("/", ".");
         int offset = dotPath.indexOf("app.");
         boolean done = false;
         if (offset == 0) {
             String classPath = dotPath.substring(4);
             classPath = classPath.substring(0, classPath.length()-5);
             ApplicationClasses.ApplicationClass applicationClass = Play.classes.getApplicationClass(classPath);
             if (applicationClass != null) {
                 try {
                     applicationClass.refresh();
                     applicationClass.javaSource = source;
                     applicationClass.compile();
                     byte[] result = "{\"compilation\":true}".getBytes("utf-8");
                     response.out.write(result, 0, result.length);
                     done = true;
                 } catch (CompilationException e) {
                     int errorLine = e.getLineNumber();
                     int srcStart = e.getSourceStart();
                     int srcEnd = e.getSourceEnd();
                     String msg = e.getMessage();
                     String result = "{\"compilation\":false," +
                     "\"errorLine\":" + errorLine + "," +
                     "\"msg\":\"" + StringEscapeUtils.escapeJavaScript(msg) + "\"," +
                     "\"srcStart\":\"" + srcStart + "\"," +
                     "\"srcEnd\":\"" + srcEnd + "\"}";
                     byte[] resultBytes = result.getBytes("utf-8");
                     response.out.write(resultBytes, 0, resultBytes.length);
                     done = true;
                 }
             }
         }
         if (!done) {
             byte[] res = "{\"compilation\":true}".getBytes("utf-8");
             response.out.write(res, 0, res.length);
         }
     }
 
     private static List<SourceFile> getFiles(VirtualFile file, List<SourceFile> files) {
         VirtualFile vf = Play.roots.get(0);
         for (VirtualFile f : file.list()) {
             if (f.isDirectory()) {
                 getFiles(f, files);
             } else {
                 String display = f.getRealFile().getAbsolutePath().replace(vf.getRealFile().getAbsolutePath(), "");
                 if (isValid(display)) {
                     files.add(new SourceFile(f.getRealFile().getAbsolutePath(), f.getName(), display));
                 }
             }
         }
         return files;
     }
 
     private static void getFileTree(VirtualFile file, Node tree) {
         for (VirtualFile f : file.list()) {
             Node node = new Node(f.getName(), f.getRealFile().getAbsolutePath());
             if (isValid2(node.name)) tree.addChild(node);
             if (f.isDirectory()) {
                 getFileTree(f, node);
             }
         }
     }
 
     private static boolean isValid(String path) {
         if (path.startsWith("/tmp")) {
             return false;
         } else if (path.startsWith("/public")) {
             if (path.startsWith("/public/javascripts")) {
                 return true;
             }
             if (path.startsWith("/public/stylesheets")) {
                 return true;
             }
             if (path.equals("/public")) {
                 return true;
             }
             return false;
         } else if (path.startsWith("/lib")) {
             return false;
         } else if (path.startsWith("/modules")) {
             return false;
         } else if (path.contains("DS_Store")) {
             return false;
         } else {
             return true;
         }
     }
 
     private static boolean isValid2(String path) {
         if (path.contains("DS_Store")) {
             return false;
         } else {
             return true;
         }
     }
 
     private static String type(File f) {
         if (f.getAbsolutePath().endsWith("application.conf")) {
             return "ace/mode/java";
         }
         if (f.getAbsolutePath().contains("/conf/messages")) {
             return "ace/mode/java";
         }
         if (f.getAbsolutePath().contains("/conf/routes")) {
             return "ace/mode/java";
         }
         if (f.getAbsolutePath().endsWith(".java")) {
             return "ace/mode/java";
         }
         if (f.getAbsolutePath().endsWith(".js")) {
             return "ace/mode/javascript";
         }
         if (f.getAbsolutePath().endsWith(".html")) {
             return "ace/mode/html";
         }
         if (f.getAbsolutePath().endsWith(".yml")) {
             return "ace/mode/yaml";
         }
         if (f.getAbsolutePath().endsWith(".sh")) {
             return "ace/mode/sh";
         }
         if (f.getAbsolutePath().endsWith(".xml")) {
             return "ace/mode/xml";
         }
         if (f.getAbsolutePath().endsWith(".sql")) {
             return "ace/mode/sql";
         }
         if (f.getAbsolutePath().endsWith(".scala")) {
             return "ace/mode/scala";
         }
         if (f.getAbsolutePath().endsWith(".less")) {
             return "ace/mode/less";
         }
         if (f.getAbsolutePath().endsWith(".cs")) {
             return "ace/mode/coffee";
         }
         if (f.getAbsolutePath().endsWith(".css")) {
             return "ace/mode/css";
         }
         return "ace/mode/text";
     }
 
     public static class SourceFile {
         public String path;
         public String name;
         public String displayPath;
         public SourceFile(String path, String name, String displayPath) {
             this.path = path;
             this.name = name;
             this.displayPath = displayPath;
         }
     }
 
     public static class Node {
         public boolean isFirst;
         public String name;
         public String fullPath;
         public List<Node> children = new ArrayList<Node>();
         public Node(String name, String fullPath) {
             this.name = name;
             this.fullPath = fullPath;
             this.isFirst = false;
         }
         public Node(String name, String fullPath, boolean isFirst) {
             this.name = name;
             this.fullPath = fullPath;
             this.isFirst = isFirst;
         }
         public boolean isLeaf() {
             return children.isEmpty();
         }
         public void addChild(Node child) {
             this.children.add(child);
         }
         public String toHTML() {
             if (isLeaf()) {
                 return "<li class=\"file\"><a class=\"sourcefilelink\" data-type=\"" + type(new File(fullPath))
                         + "\" data-path=\"" + fullPath
                         + "\" href=\"/@editor/file?path="
                         + URLEncoder.encode(fullPath) + "&line=0\">" + HTML.htmlEscape(name) + "</a></li>";
             } else {
                 Collections.sort(children, new Comparator<Node>() {
                     @Override
                     public int compare(Node node, Node node2) {
                         if (node.isLeaf() && node2.isLeaf()) { return 0; }
                         if (!node.isLeaf() && !node2.isLeaf()) { return 0; }
                         if (node.isLeaf()) { return 1; }
                         if (node2.isLeaf()) { return -1; }
                         return 0;
                     }
                 });
                 StringBuilder builder = new StringBuilder();
                 long id = System.nanoTime();
                 builder.append("<li><label for=\"folder")
                         .append(id).append("\">")
                         .append(HTML.htmlEscape(name))
                         .append("</label> <input type=\"checkbox\" id=\"folder")
                         .append(id)
                         .append("\" ");
                 if (isFirst) {
                     builder.append("checked disabled");
                 }
                 builder.append(" /><ol>");
                 for (Node node : children) {
                     builder.append(node.toHTML());
                 }
                 builder.append("</ol></li>");
                 return builder.toString();
             }
         }
     }
 
     private static final SimpleTemplateEngine engine = new SimpleTemplateEngine();
 
     private static final ConcurrentHashMap<File, Template> templates =
             new ConcurrentHashMap<File, groovy.text.Template>();
 
     private static void renderGroovytemplate(File file, Map<String, Object> context, OutputStream os) throws Exception {
         OutputStreamWriter osw = new OutputStreamWriter(os);
         //if (!templates.containsKey(file)) {
             String code = IO.readContentAsString(file);
             code = code.replace("$.", "\\$.").replace("$(", "\\$(");
             Template template = engine.createTemplate(code);
         //    templates.putIfAbsent(file, template);
             templates.put(file, template);
         //}
         templates.get(file).make(context).writeTo(osw);
     }
 }
