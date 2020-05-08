 package article;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.Validate;
 import org.w3c.dom.Node;
 
 import xquery4j.DOMUtils;
 import xquery4j.XQueryEvaluator;
 
 public class Article {
     
     private XQueryEvaluator evaluator;
     
     public static class Mod {
         public static Node highlight(final String code, String lang) throws Exception {
             Validate.notNull(lang);
             final Process p = new ProcessBuilder("highlight", "-X", "--syntax", lang).start();
             Thread t = new Thread(new Runnable() {
 
                 public void run() {
                     try {
                         OutputStream out = p.getOutputStream();
                        IOUtils.write(code, p.getOutputStream());
                         out.flush();
                         out.close();
                     } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                     }
                 }
             });
             t.start();
             String result = IOUtils.toString(p.getInputStream());
             t.join();
             return DOMUtils.parse(result).getDocumentElement();
         }
     }
     
     public Article() {
         evaluator = new XQueryEvaluator();
         evaluator.declareJavaClass("urn:article", Mod.class);
     }
     
     public void gen() throws Exception {
         Node r = (Node) evaluator.evaluateExpression(IOUtils.toString(getClass().getResourceAsStream("/article.xq")), DOMUtils.parse(System.in)).get(0);
         System.out.println(DOMUtils.domToString(r));
     }
     
     public static void main(String[] args) throws Exception {
         new Article().gen();
     }
 }
