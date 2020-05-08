 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gr.dsigned.springcrudutils.strategies;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author nk
  */
 public class CrossFrameStrategy<T> implements RenderStrategy<T> {
 
    protected Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
 
     @Override
     public String render(T data) {
         StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head><title></title></head><body><script type='text/javascript'>");
        sb.append("window.parent.postMessage('").append(g.toJson(data)).append("','*');");
        sb.append("</script></body></html>");
         return sb.toString();
     }
 
     @Override
     public void setup(HttpServletResponse response) {
         response.setContentType(ContentType.TEXT_HTML.toString());
     }
 }
