 /**
  *
  */
 package ar.com.sourcerain.labs.ide;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.inject.Inject;
 
 import org.cometd.bayeux.server.BayeuxServer;
 import org.cometd.bayeux.server.ConfigurableServerChannel;
 import org.cometd.bayeux.server.ServerMessage;
 import org.cometd.bayeux.server.ServerSession;
 import org.cometd.java.annotation.Configure;
 import org.cometd.java.annotation.Listener;
 import org.cometd.java.annotation.Service;
 import org.cometd.java.annotation.Session;
 import org.cometd.server.authorizer.GrantAuthorizer;
 
 @Service("files")
 public class FilesService {
 
       private static final String root_directory = "/C:/engine";
 //    private static final String root_directory = "/home/sebas/projects";
     @Inject
     private BayeuxServer _bayeux;
     @Session
     private ServerSession _session;
 
     @SuppressWarnings("unused")
     @Configure("/files/**")
     private void configureChatStarStar(ConfigurableServerChannel channel) {
         channel.addAuthorizer(GrantAuthorizer.GRANT_PUBLISH);
     }
 
     @Listener("/service/files")
     public void files(ServerSession client, ServerMessage message)  {
         Map<String, Object> result = new HashMap<String, Object>();
         Map<String, Object> params = message.getDataAsMap();
         File file = new File(root_directory + "/" + params.get("filename"));
         result.put("filename", file.getName() );
         
         String[] children = file.list(new FilenameFilter() {
             public boolean accept(File file, String name) {
                 return !name.startsWith(".");
             }
         });
 
         try {
             if (file.exists()) {
                 if (children == null) {
                     BufferedReader reader = new BufferedReader(new FileReader(file));
                     String line = null;
                     StringBuilder buf = new StringBuilder();
                     while ((line = reader.readLine()) != null) {
                         buf.append(line).append('\n');
                     }
                     result.put("file", buf.toString());
                     
                     _session.getLocalSession().getChannel("/view").publish(result);
                 } else {
                     List<String> files = new ArrayList<String>();
                     for (String childName : children) {
                         String meta = new File(file.getAbsolutePath()+"/"+childName).isDirectory()?"directory":"text_file";
                         files.add( meta + ":" + childName);
                     }
                     
                     result.put("files", files);
                     
                     _session.getLocalSession().getChannel("/files").publish(result);
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 }
