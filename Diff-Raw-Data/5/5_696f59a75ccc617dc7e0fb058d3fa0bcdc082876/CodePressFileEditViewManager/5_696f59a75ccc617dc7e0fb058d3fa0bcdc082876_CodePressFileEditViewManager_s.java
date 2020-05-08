 package org.makumba.parade.view.managers;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringWriter;
 
 import org.apache.log4j.Logger;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.view.interfaces.FileEditorView;
 
 import freemarker.template.SimpleHash;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 /**
  * This class is handling the display for the editor working with CodePress.
  * 
  * TODO it seems that CodePress makes it not possible to save the file the way it was working before,
  * so this needs to be investigated
  * 
  * @author Manuel Gay
  * @version $id
  */
 
 public class CodePressFileEditViewManager implements FileEditorView {
 
     static Logger logger = Logger.getLogger(CodePressFileEditViewManager.class.getName());
         
     public String getFileEditorView(Row r, String path, File file, String[] source) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         java.io.File f= new java.io.File(file.getPath());
         String content="";
         
         if (source != null) {
             content = source[0];
         }
         else {
             // we read the file
             if (f.exists()) {
                 Reader rd;
                 try {
                     rd = new BufferedReader(new FileReader(f));
                     int c;
                     StringBuffer sb = new StringBuffer();
                     while ((c = rd.read()) != -1) {
                         sb.append((char) c);
                     }
                     content = sb.toString();
                 } catch (FileNotFoundException e) {
                     logger.error(e);
                 } catch (IOException e) {
                     logger.error(e);
                 }
             }
         }
         
         // we convert special characters so they are correctly displayed in HTML
         // commented out for now, because it may cause troubles with this editor
        /*
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < content.length(); i++)
             if (content.charAt(i) == '<')
                 sb.append("&lt;");
             else if (content.charAt(i) == '&')
                 sb.append("&amp;");
             else
                 sb.append(content.charAt(i));
         content = sb.toString();
        */
         
         /* Constructing the data model */
         SimpleHash root = new SimpleHash();
         root.put("fileName", file.getName());
         root.put("rowName", r.getRowname());
         root.put("path", path);
         root.put("content", content);
         
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate("codePressEditor.ftl");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         /* Merge data model with template */
         try {
             temp.process(root, out);
         } catch (TemplateException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         
         return result.toString();
         
     }
     
 }
