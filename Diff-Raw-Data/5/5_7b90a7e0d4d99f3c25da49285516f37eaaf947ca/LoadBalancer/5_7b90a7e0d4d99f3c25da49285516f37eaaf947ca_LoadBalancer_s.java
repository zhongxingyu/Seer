 import com.sun.net.httpserver.HttpExchange;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.HttpHostConnectException;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class LoadBalancer extends AbstractHttpServer {
 
     ArrayList<NodeAddr> addresses = null;
     private final int DEFAULT_DENOMINATOR = 97;
     private int lastID = 0;
 
     public LoadBalancer(int port1, ArrayList<NodeAddr> _servers) throws IOException{
         this.create(port1);
         addresses = _servers;
     }
 
     public static String parseHtml(String html) {
         String string = html.replaceFirst("<html>", "").replaceFirst("</html>", "");
         string = string.replaceAll("<html>(.*)</html>", "");
         return string.trim();
     }
 
     public static String doQuery(String query, String addr) throws IOException {
         BufferedReader rd = null;
         String answer = "";
         HttpClient client = new DefaultHttpClient();
         HttpGet request = new HttpGet("http://" + addr + "/?command=" + query.replace(" ", "+") + "&submit=submit");
         HttpResponse response = null;
         try {
             response = client.execute(request);
             rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 
             while ((query = rd.readLine()) != null) {
                 answer = answer.concat(query);
             }
         } catch (HttpHostConnectException e) {
             return null;
         } catch (IOException e) {
             System.out.println(e.getMessage());
             rd.close();
         }
 
         return LoadBalancer.parseHtml(answer);
 
     }
 
     public static String processingQuerry(String string) {
         string = string.substring("/?command=".length());
         final int i = string.lastIndexOf('&');
         string = string.substring(0, i);
         //String[] ans = string.split("\\+");
         string = string.replace("+", " ");
         return string;
     }
 
     public int getHash(String value) {
         int sum = 0;
         for (char a : value.toCharArray()) {
             sum += (int) a;
         }
 
         sum %= DEFAULT_DENOMINATOR;
         sum %= addresses.size();
         return sum;
     }
 
     public void handle(HttpExchange exc) throws IOException {
         exc.sendResponseHeaders(200, 0);
         PrintWriter out = new PrintWriter(exc.getResponseBody());
         final URI u = exc.getRequestURI();
         final String query = u.toString();
 
         String q = processingQuerry(query);
         String answer = "";
         if (q.contains("get_by_name") == true) {
             String name = q.substring(q.indexOf(" ") + 1);
 
             int i=0;
             String tmp = null;
             ArrayList<String> sl = addresses.get(getHash(name)).slavesAddr;
 
             while ((tmp = doQuery(q, sl.get(i))) == null) {
                 if (++i > sl.size()-1) {
                     answer += "Node is unavailable.";
                 }
             }
 
             if (tmp != null)
                 answer += tmp + "\n";
             //System.out.println("Result: " + answer);
 
         } else if (q.contains("get_by_id") || q.contains("get_by_number")) {
             for (NodeAddr a : addresses) {
 
                 int i=0;
                 String tmp = null;
                 ArrayList<String> sl = a.slavesAddr;
 
                 while ((tmp = doQuery(q, sl.get(i))) == null) {
                     if (++i > sl.size()-1) {
                         answer += "Node is unavailable.";
                     }
                 }
 
                 if (tmp != null)
                     answer += tmp + "\n";
             }
             answer = answer.replaceAll("Nothing found\n", "");
 
             if (answer.trim().equals(""))
                 answer = "Nothing found";
 
         } else if (q.contains("new") || q.contains("delete")) {
             for (NodeAddr a : addresses) {
                 answer = doQuery(q, a.masterAddr) + "\n";
             }
         } else if (q.contains("delete")) {
             for (NodeAddr a : addresses) {
                 answer += doQuery(q, a.masterAddr) + "\n";
 
                 String tmp = null;
                 int i=0;
                 while((tmp = doQuery(q, a.masterAddr)) == null) {
                     if (++i > a.slavesAddr.size()-1) {
                         answer += "Writing node is unavailable.";
                     }
                     doQuery("set_master", a.slavesAddr.get(i));
                     String temp = a.slavesAddr.get(i);
                     a.slavesAddr.set(i,a.masterAddr);
                     a.masterAddr = temp;
                 }
                 if (tmp != null)
                     answer += tmp;
             }
         } else if (q.contains("update")) {
             //id+old_name+new_name+tel
             Pattern pattern=Pattern.compile("([update]{1}) (\\d+) ([A-Za-z]+) ([A-Za-z]+) (.+)");
             Matcher matcher=pattern.matcher(q);
             matcher.find();
             String name = matcher.group(3);
 
             String tmp = null;
             int i=0;
             NodeAddr adr = addresses.get(getHash(name));
             while((tmp = doQuery("delete "+matcher.group(2),adr.masterAddr)) == null) {
                 if (++i > adr.slavesAddr.size()-1) {
                     answer += "Writing node is unavailable.";
                 }
                 doQuery("set_master", adr.slavesAddr.get(i));
                 String temp = adr.slavesAddr.get(i);
                 adr.slavesAddr.set(i,adr.masterAddr);
                 adr.masterAddr = temp;
             }
 
             if (tmp != null)
                 answer += tmp;
 
             i = 0;
             adr = addresses.get(getHash(matcher.group(4)));
             while ((tmp = doQuery("add "+matcher.group(4)+" "+matcher.group(5)+" "+matcher.group(2), adr.masterAddr)) == null) {
                 if (++i > adr.slavesAddr.size()-1) {
                     answer += "Writing node is unavailable.";
                 }
                 doQuery("set_master", adr.slavesAddr.get(i));
                 String temp = adr.slavesAddr.get(i);
                 adr.slavesAddr.set(i,adr.masterAddr);
                 adr.masterAddr = temp;
             }
 
             if (tmp != null)
                 answer += tmp;
         } else if (q.contains("add")) {
             String name = q.trim().substring(q.indexOf(" ") + 1, q.lastIndexOf(" "));
             q += " " + lastID++;
 
             NodeAddr adr = addresses.get(getHash(name));
             String tmp = null;
             int i=0;
             while((tmp = doQuery(q, adr.masterAddr)) == null) {
                 if (++i > adr.slavesAddr.size()-1) {
                     answer += "Writing node is unavailable.";
                 }
                 doQuery("set_master", adr.slavesAddr.get(i));
                 String temp = adr.slavesAddr.get(i);
                 adr.slavesAddr.set(i,adr.masterAddr);
                 adr.masterAddr = temp;
             }
             if (tmp != null)
                 answer += tmp;
             if(answer.trim()==null){
                 answer="could not be added";
             }
            //answer += doQuery(q, adr.masterAddr);
         }else if(q.contains("show_bd")){
             for(NodeAddr a:addresses){
                 answer+=doQuery(q,a.slavesAddr.get(0))+" " ;
             }
         }  else if(q.contains("save_bd")){
             for (NodeAddr a:addresses){
                 for(int i=0;i<a.slavesAddr.size();i++){
                     answer+=doQuery(q,a.slavesAddr.get(i))+" ";
                 }
                 answer+=doQuery(q,a.masterAddr)+" ";
                 if(answer.contains("Base has been saved")){
                     answer="Base has been saved";
                 }
             }
         }   else if(q.contains("load_bd")){
             for(NodeAddr a:addresses){
                 for (int i=0;i<a.slavesAddr.size();i++){
                     answer+=doQuery(q,a.slavesAddr.get(i))+ " ";
                 }
                 answer+=doQuery(q,a.masterAddr)+" ";
                 if(answer.contains("OK")){
                     answer="OK";
                 }
             }
         } else if(q.contains("exit_bd")){
             for(NodeAddr a:addresses){
                 for (int i=0;i<a.slavesAddr.size();i++){
                     answer+=doQuery(q,a.slavesAddr.get(i))+ " ";
                     lastID=0;
                 }
                 answer+=doQuery(q,a.masterAddr)+" ";
                 if(answer.contains("OK")){
                     answer="OK";
                 }
             }
         }  else if(q.contains("quit")){
              System.exit(1);
         }
 
 
         out.println("<html>" + answer + "</html>");
         out.close();
         exc.close();
     }
 
 //    public static void main(String[] args) throws IOException {
 //        LoadBalancer l = new LoadBalancer(8080, 2122, 2123, 2124);
 //    }
 
 }
 
 
 
