 package net.pixelcop.monstrous.server;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.util.Vector;
 
 import net.pixelcop.monstrous.Job;
 import net.pixelcop.monstrous.Node;
 import net.pixelcop.monstrous.Stats;
 import net.pixelcop.monstrous.http.JsonUtils;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class Server {
     
     private static final Server instance = new Server();
     
     private Vector<Node> clients;
     
     private Job job;
     private int numClientsProcessing;
     private Stats stats;
     private long startTime;
     
     private Server() {
         clients = new Vector<Node>();
         this.reset();
     }
     
     public static Server getInstance() {
         return instance;
     }
     
     public void reset() {
         job = null;
         numClientsProcessing = 0;
         stats = null;
         startTime = 0;
     }
     
     /**
      * Register a node with the server
      * 
      * @param node
      */
     public void addClient(Node node) {
         clients.add(node);
     }
     
     /**
      * Send the given job to all known clients
      * 
      * @param job
      */
     public void createJob(Job job) {
         
         if (this.job != null) {
             // TODO throw error
             return;
         }
         
         this.job = job;
         this.stats = new Stats();
         this.startTime = System.currentTimeMillis();
        
         for (Node node : clients) {
             try {
                 sendJob(node, job);
                 numClientsProcessing++;
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         
     }
     
     public boolean isJobComplete() {
         return (numClientsProcessing == 0);
     }
     
     public void addStats(Stats stats) {
         this.stats.add(stats);
         numClientsProcessing--;
     }
 
     /**
      * Tell client to start given job
      * 
      * @param node
      * @param job
      * @throws IOException 
      * @throws ClientProtocolException 
      */
     private void sendJob(Node node, Job job) throws IOException {
         InetAddress addr = node.getAddress();
         String uri = "http://" + addr.getHostAddress() + ":9998/job/new";
         System.out.println(uri);
         HttpPost post = new HttpPost(uri);
         try {
             post.setEntity(new StringEntity(JsonUtils.toJsonString(job)));
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         DefaultHttpClient http = new DefaultHttpClient();
         http.execute(post);
     }
     
     public Stats getStats() {
         return stats;
     }
     
     public void setStats(Stats stats) {
         this.stats = stats;
     }
 
     public Vector<Node> getClients() {
         return clients;
     }
 
     public void setClients(Vector<Node> clients) {
         this.clients = clients;
     }
 
     public Job getJob() {
         return job;
     }
 
     public void setJob(Job job) {
         this.job = job;
     }
 
     public long getStartTime() {
         return startTime;
     }
 
     public void removeClient(String hostAddress) {
        
         for (Node node : clients) {
             if (node.getAddress().getHostAddress().equalsIgnoreCase(hostAddress)) {
                 clients.remove(node);
                 return;
             }
         }
         
     }
 
 }
