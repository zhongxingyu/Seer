 import java.net.*;
 import java.io.*;
 import java.util.*;
 
 public class ProxyServer{
     private static int interval = 5000;// init in 5sec interval
     private static int threshold = 10;
     private static int numTasks = 0;
 
     private static class CheckTime extends TimerTask{
 	public void run(){
 	    numTasks= 0;
 	}
     }
 
     public static void main(String[] args) throws IOException{
 	int HTTP_PORT = 8080;
 	//File dir = new File("c:\\workspace\\Intern");
 	File dir = new File("/home/hat-tun/intern/reverse_proxy");
 	int numThreads = 5;
 	int cnt = 0;
 	ServerSocket servsock = new ServerSocket(HTTP_PORT);	
       	for(int i = 1; i < numThreads; i++){
 	    RequestPassThrough req = new RequestPassThrough();
 	    Thread t = new Thread(req);
 	    t.start();
 	}
 	for(int i = 1; i < numThreads; i++){
 	    RequestRedirect req2 = new RequestRedirect(dir);
 	    Thread t2 = new Thread(req2);
 	    t2.start();
 	}
 	
 	System.out.println("ip" + InetAddress.getLocalHost().getHostAddress() + " port" + servsock.getLocalPort());
 	
 	TimerTask task = new CheckTime();
 	Timer timer = new Timer();
 	timer.schedule(task, 0, interval);
 	
 	while(true){
 	    try{
 		Socket sock = servsock.accept();
 		cnt++;
 		numTasks++;
 		System.out.println("task = " + numTasks);
 		if(numTasks > threshold){
 		    System.out.println("Overflow");
 		    RequestRedirect.reqProcRedirect(sock, cnt);
 		}else{
 		    RequestPassThrough.reqProc(sock, cnt);
 		}
 	    }
 	    catch (IOException e){
 		e.printStackTrace();
 		System.exit(1);
 	    }
 	}
     }    
 }															
 
 class RequestPassThrough implements Runnable{
     //private static String webserver ="10.228.155.194";
     private static String webserver ="192.168.74.141";
    //private static String webserver ="10.228.109.120";
     private static int HTTP_PORT = 8000;
     private static int max_size = 3;
     private static LinkedList queue = new LinkedList();
     
     public static void reqProc(Socket s, int cnt){
 	synchronized (queue){
 	    queue.addLast(s);
 	    System.out.println("connect:" + cnt + " " + queue);
 	    queue.notifyAll();
 	}
     }
     
     public void run(){
 	while(true){
 	    Socket sock;
 	    synchronized (queue){
 		while(queue.isEmpty()){
 		    try{
 			queue.wait();
 		    }
 		    catch (InterruptedException e){						
 		    }
 		}
 		sock = (Socket) queue.removeFirst();
 	    }
 	    try{		
 		String filename;
 		String indxname;
 		BufferedOutputStream outs = new BufferedOutputStream(sock.getOutputStream());
 		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outs));
 		BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
 				
 		Socket sock2 = new Socket(webserver, HTTP_PORT); // connect to web server		
 		BufferedOutputStream outs2 = new BufferedOutputStream(sock2.getOutputStream());
 		
 		byte[] buf3 = new byte[1024];
 		byte[] buf4 = new byte[1024];
   		in.read(buf3);//std input stream has no terminal???  So once read.	    
 		buf4 = buf3;		
 		String req = new String(buf3);
 		String[] request = req.split("\r\n");
 		String get = request[0]; //first line fetch		
 		StringTokenizer st = new StringTokenizer(get);
 		String method = st.nextToken();
 		if(method.equals("GET")){
 		    filename = st.nextToken();
 		    indxname = filename.substring(1, filename.length());
 		    
 		    InetAddress ip = sock.getLocalAddress();
 		    IpTable iptab = new IpTable(ip,indxname);
 		    String ipstr = ip.toString().substring(1);
 		    outs2.write(buf4);
 		    outs2.flush();
 
 		    int k;			
 		    byte[] buf2 = new byte[1024];
 		    BufferedInputStream in2 = new BufferedInputStream(sock2.getInputStream());
 		    // Response Transfer
 		    while(true){				
 			k = in2.read(buf2);
 			if(k == -1){
 			    break;
 			}
 			outs.write(buf2, 0, k);
 			outs.flush();
 		    }
 		    //iptab register
 		    iptab.addLast(iptab);
 		    if(iptab.size() > max_size){
 			iptab.removeFirst();
 		    }
 		    in2.close();
 		    outs2.close();
 		    sock2.close();		    
 		    in.close();
 		    out.close();
 		    outs.close();
 		    sock.close();
 		}
 	    }
 	    catch(IOException e) {
 	    }
 	    finally{
 		try{
 		    sock.close();
 		}
 		catch (IOException e){
 		}
 	    }
 	}
     }
 }
 
 class RequestRedirect implements Runnable{
    //private static String webserver ="10.228.155.83";
    private static String webserver ="192.168.74.141";
     private static int HTTP_PORT = 8080;
     private static LinkedList queue = new LinkedList();
     private File homeDir;
 
     public RequestRedirect(File homeDir){
 	this.homeDir = homeDir;
     }
     
     public static void reqProcRedirect(Socket s, int cnt){
 	synchronized (queue){
 	    queue.addLast(s);
 	    System.out.println("connect:" + cnt + " " + queue);
 	    queue.notifyAll();
 	}
     }
 
     public void run(){
 	while(true){
 	    Socket sock;
 	    synchronized (queue){
 		while(queue.isEmpty()){
 		    try{
 			queue.wait();
 		    }
 		    catch (InterruptedException e){						
 		    }
 		}
 		sock = (Socket) queue.removeFirst();
 	    }
 	    try{
 		
 		String filename;
 		String indxname;
 		String contentType;
 		String version;		
 		BufferedOutputStream outs = new BufferedOutputStream(sock.getOutputStream());
 		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outs));
 		BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
 		
 		byte[] buf3 = new byte[1024];
   		in.read(buf3);//std input stream has no terminal???  So once read.		
 		String req = new String(buf3);
 		String[] request = req.split("\r\n");
 		String get = request[0];
 		StringTokenizer st = new StringTokenizer(get);
 		String method = st.nextToken();
 		if(method.equals("GET")){
 		    filename = st.nextToken();
 		    indxname = filename.substring(1, filename.length());
 		    contentType = "text/html";
 		    version = st.nextToken();
 		    //String adr = homeDir + "\\" + indxname;
 		    String adr = homeDir + "/" + indxname;
 		    File file = new File(adr);
 		    boolean flg = file.canRead();
 		    Date now = new Date();
 		    InetAddress ip = sock.getLocalAddress();
 		    String ipstr = ip.toString().substring(1);
 		    if(version.startsWith("HTTP/")){
 			if(indxname.equals("dummy.html")){ // deny endless loop
 			    if(flg){
 				FileInputStream fin = new FileInputStream(file);
 				byte[] buf = new byte[(int) file.length()];		
 				fin.read(buf);
 				fin.close();
 				
 				out.write("HTTP/1.1 200 OK\r\n");
 				out.write("Date: " + now + "\r\n");
 				out.write("Server: HTTP 1.1\r\n");
 				out.write("Content-length: " + buf.length + "\r\n");
 				out.write("Content-type: " + contentType + "\r\n\r\n");
 				out.flush();				
 				outs.write(buf);
 				outs.flush();
 			    }
 			}else{ //Redirect				
 			    IpTable iptab = IpTable.getFirst();
 			    iptab.printTab(iptab);
 			    
 			    out.write("HTTP/1.1 302 Found\r\n");
 			    out.write("Date: " + now + "\r\n");
 			    out.write("Server: HTTP 1.1\r\n");
 			    out.write("Location: http://"+webserver+":"+HTTP_PORT+"/dummy.html\r\n");
 			    out.flush();
 			}
 		    }		
 		    in.close();
 		    out.close();
 		    outs.close();
 		    sock.close();
 		}
 	    }
 	    catch(IOException e) {
 	    }
 	    finally{
 		try{
 		    sock.close();
 		}
 		catch (IOException e){
 		}
 	    }
 	}
     }
 }
 
 class IpTable{
     private static LinkedList ip_queue = new LinkedList();
     private InetAddress ip;
     private String path;
     
     public IpTable(InetAddress ip, String path){
 	this.ip = ip;
 	this.path = path;
     }
 	
     public static void addLast(IpTable ips){
 	ip_queue.addLast(ips);
     }
 
     public static int size(){
 	return(ip_queue.size());
     }
 
     public static void removeFirst(){
 	ip_queue.removeFirst();
     }
 
     public static IpTable getFirst(){
 	return((IpTable)ip_queue.getFirst());
     }
 
     public static void printTab(IpTable iptab){
 	System.out.println(iptab.ip + " " + iptab.path);
     }
 }	
