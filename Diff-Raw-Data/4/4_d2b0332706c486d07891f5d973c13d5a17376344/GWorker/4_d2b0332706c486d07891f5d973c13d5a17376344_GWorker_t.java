 package eu.ahref;
 
 import com.jimplush.goose.Article;
 import com.jimplush.goose.Configuration;
 import com.jimplush.goose.ContentExtractor;
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 import redis.clients.jedis.Jedis;
 
 import java.io.IOException;
 
 
 /**
  * Goose's thread
  *
  * @author Martino Pizzol
  */
 
 public class GWorker implements Runnable {
     String jswork;
     Jedis jedis;
     Logger logger;
     Configuration gooseConfig;
     String gpath;
     String redgooSiteList;
 
 
     public GWorker(String tsk, String connUrl,Logger log, Configuration gConfig,String rsl){
         this.jedis = new Jedis(connUrl);
         this.jswork = tsk;
         this.logger = log;
         this.gooseConfig = gConfig;
         try {
             gpath = DirectoryManager.createTempDirectory();
         } catch (IOException e) {
             logger.debug("Create goose temp dir failed \n "+e.getStackTrace().toString());
             return;
         }
         this.redgooSiteList = rsl;
         gooseConfig.setLocalStoragePath(gpath);
     }
 
     public void run() {
         JSONObject jwork = null;
         String url=null;
 
         try {
             jwork = new JSONObject(jswork);
             url = jwork.getString("url");
             logger.debug("URL: "+url);
 
             //String original = jwork.getString("original");
 
             ContentExtractor contentExtractor = new ContentExtractor(gooseConfig);
             Article article = null;
             try{
                 if(url != null){
                     article = contentExtractor.extractContent(url);
                 }/*else if(original!=null){
                     //TODO capire come gestire il testo senza url
                 }  */
                 JSONObject jout = new JSONObject();
                 jout.put("html", article.getCleanedArticleText());
                 jout.put("title", article.getTitle());
                 jout.put("image", article.getTopImage().getImageSrc());
                 jout.put("domain", article.getDomain());
                 jout.put("original",article.getOriginalDoc());
                 jedis.publish(jwork.getString("id"),jout.toString());
 
             }catch(Exception e){
                JSONObject jerr = new JSONObject();
                jerr.put("status", "error");
               jedis.publish(jwork.getString("id"),jerr.toString());
                jedis.rpush(this.redgooSiteList,url);
                logger.debug("Goose exception\n"+ e.getStackTrace().toString());

             }
         } catch (JSONException e) {
             logger.error("JSON exception\n"+e.getStackTrace().toString());
         }finally {
             //TODO cancellare la directory quando finisice il thread
             DirectoryManager.deleteDir(gpath);
             try {
                 jedis.disconnect();
             } catch (IOException e) {
                 logger.error("Redis disconnection\n"+e.getStackTrace().toString());
             }
 
         }
 
     }
 }
