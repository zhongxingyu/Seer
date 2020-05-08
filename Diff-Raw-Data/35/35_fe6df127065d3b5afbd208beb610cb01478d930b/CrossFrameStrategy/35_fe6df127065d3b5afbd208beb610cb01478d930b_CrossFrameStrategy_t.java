 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gr.dsigned.springcrudutils.strategies;
 
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.SimpleDateFormat;
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author nk
  */
 public class CrossFrameStrategy<T> implements RenderStrategy<T> {
 
    protected ObjectMapper mapper;

    public CrossFrameStrategy() {
        this.mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
 
     @Override
     public String render(T data) {
         StringBuilder sb = new StringBuilder();
        try {
            sb.append("<!doctype html><html><head><title></title></head><body><script type='text/javascript'>");
            sb.append("window.parent.postMessage('").append(mapper.writeValueAsString(data)).append("','*');");
            sb.append("</script></body></html>");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
         return sb.toString();
     }
 
     @Override
     public void setup(HttpServletResponse response) {
         response.setContentType(ContentType.TEXT_HTML.toString());
     }
 }
