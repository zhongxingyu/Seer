 package net.zwerks.dumpfs;
 
 import java.io.InputStream;
 
 import javax.swing.JOptionPane;
 
 import net.zwerks.dumpfs.SSHExec.MyUserInfo;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.UserInfo;
 
 public class DumpFSLocal {
 
 	private String UserOnHost2Dump;
 	private String Host2Dump;
 	private Session openSession;
 	//private DumpFSStatistics myStatistics;
 	
 	public DumpFSLocal() {
 		// TODO Auto-generated constructor stub
 		
 		//Print out OS that CBB is running from
 		System.out.println("------------------------------------");
 		System.out.println("CBB FileSystem Dumper running on: " + System.getProperty("os.name").toUpperCase() + "...");
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		//For Benchmarking the time
 		long progStartTime = System.currentTimeMillis();
 		DumpFSStatistics myStatistics = new DumpFSStatistics();
 		myStatistics.setAppStartTime(progStartTime);
 		
 		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		System.out.println("~~~~~~~~~~~~~~~ Application Timer Started ... ("+ progStartTime +")~~~~~~~~~~~~~~");
 		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		
 		//Initialize the FSDumper
 		DumpFSLocal myFSDumper= new DumpFSLocal();
 		myFSDumper.InitiateConnection("root", "", "192.168.1.150");
 		//myFSDumper.InitiateConnection("root", "abc123", "192.168.1.182");
 		//myFSDumper.InitiateConnection("root", "abc123", "192.168.0.104");
 		//myFSDumper.InitiateConnection("root", "", "192.168.0.104");
 		//myFSDumper.InitiateConnection("root", "admin", "192.168.0.104");
 		
 		myFSDumper.doPortForwardingL(54137, "127.0.0.1", 57314);
 		//myFSDumper.doPortForwardingL(54137, "localhost", 57314);
 		//myFSDumper.doPortForwardingL(54137, "192.168.1.182", 57314);
 		
 		//myFSDumper.StartLocalNetCat(54137);
 		
 		
 		//String CommandToExecute = "set|grep SSH";
 		//---->//For Dumping the Chumby
		String CommandToExecute = "dd if=/dev/mtdblock5 | gzip | nc -l -p 57314 -w 10";
		//String CommandToExecute = "dd if=/dev/mtdblock5 | nc -l -p 57314 -w 10"; 			//Without ZIPing
 		//---->//
 		//String CommandToExecute = "dd if=/dev/block/mmcblk0p22 | gzip | nc -l -p 57314 -w 10";	//MT4GS <<<---- /system
 		//String CommandToExecute = "dd if=/dev/block/mmcblk0p24 | gzip | nc -l -p 57314 -w 10";		//MT4GS <<<---- /cache
 		//String CommandToExecute = "dd if=/dev/block/mmcblk0p19 | gzip | nc -l -p 57314 -w 10";
 		//String CommandToExecute = "dd if=/dev/block/mmcblk0p19 | gzip | nc -l -p 57314 -w 10";
 		
 		//String CommandToExecute = "echo '1234567zxckhgjgh' | gzip | nc -l -p 57314 -w 10";
 		//String CommandToExecute = "echo '1234567zxckhgjgh' | nc -l -p 57314 -w 10";
 		//String CommandToExecute = "echo '1234567zxckhgjgh'";
 		//String CommandToExecute = "nc -l -p 57314 -w 350";
 				
 		//myFSDumper.StartRemoteDD2Netcat(CommandToExecute);
 		RemoteDD2Netcat myDD2Netcat = new RemoteDD2Netcat(CommandToExecute, myFSDumper.getOpenSession());
 		Thread dd2netcatThread = new Thread(myDD2Netcat);
 		dd2netcatThread.start();
 		//myDD2Netcat.send();
 		
 		myFSDumper.StartLocalNetCat(54137, myStatistics);
 		
 		//String stopCommand = "killall nc";
 		//myFSDumper.StopRemoteNetcat(stopCommand);
 		
 		//System.out.println("Still Sending ...");
 	}
 	
 	public void InitiateConnection(String username, String secret, String hostname){
 		try{
 			//Create new Java Secure Channel
 			JSch jsch=new JSch();
 		    
 			//Load known_hosts file 
 			String KnownHostsFilePath = System.getProperty("user.dir") + this.getCurrOSPathFormat() + "known_hosts.txt";
 			System.out.println(KnownHostsFilePath);
 			System.out.println("------------------------------------");
 			jsch.setKnownHosts(KnownHostsFilePath);
 			
 			//Get the username and hostname of the machine/host to dump
 			this.Host2Dump = hostname;
 			this.UserOnHost2Dump = username;
 			
 			//Open an SSH Session using the above parameters
 			Session session = jsch.getSession(this.UserOnHost2Dump, this.Host2Dump, 22);
 			//Session session = jsch.getSession(this.UserOnHost2Dump, this.Host2Dump, 35335);
 			
 			session.setPassword(secret);
 			
 			/*
 			// username and password will be given via UserInfo interface.
 			UserInfo ui=new MyUserInfo();
 			session.setUserInfo(ui);
 			*/
 			
 			session.connect();
 			this.openSession = session;
 			
 		      
 		    }
 		    catch(Exception e){
 		      System.out.println(e);
 		    }
 	}
 	
 	public void doPortForwardingL(int lport, String rhost, int rport){
 		Session currSession = this.getOpenSession();
 		try{
 			int assigned_port = currSession.setPortForwardingL(lport, rhost, rport);
 			System.out.println("localhost:"+assigned_port+" -> "+rhost+":"+rport);
 		}
 		catch(JSchException ex){
 			System.out.println("Caught Exception while doing Local Port Forwarding: \n" + ex);
 		}
 	}
 	
 	public void StartLocalNetCat(int listenPort, DumpFSStatistics statsCollector){
 				
 		DumpReceiver dumpRcvr = new DumpReceiver(System.getProperty("user.dir") + this.getCurrOSPathFormat(),
 				"myDumpFile.ida.gz", listenPort, statsCollector);
 		Thread t = new Thread(dumpRcvr);
 		try{
 			t.sleep(1000);
 		} catch(InterruptedException iex){
 			System.out.println("Thread exception with Dumper: "+iex);
 		}
 		
 		t.start();
 		//dumpRcvr.listen(listenPort);
 		
 	}
 	
 	public void StopRemoteNetcat(String command){
 		try{
 			Session currSession = this.getOpenSession();
 			Channel channel = currSession.openChannel("exec");
 			((ChannelExec)channel).setCommand(command);
 
 			channel.setInputStream(null);
 
 			((ChannelExec)channel).setErrStream(System.err);
 
 			InputStream in=channel.getInputStream();
 
 			channel.connect();
 
 			byte[] tmp=new byte[1024];
 
 			while(true){
 				while(in.available()>0){
 					int i=in.read(tmp, 0, 1024);
 					if(i<0)break;
 					
 					System.out.print(new String(tmp, 0, i));
 				}
 				if(channel.isClosed()){
 					System.out.println("exit-status: "+channel.getExitStatus());
 					break;
 				}
 				
 				try{Thread.sleep(1000);}catch(Exception ee){}
 			}
 			channel.disconnect();
 			currSession.disconnect();
 		}catch(Exception ex){
 			
 		}
 	}
 	
 	public Session getOpenSession(){
 		return this.openSession;
 	}
 	
 	public String getCurrOSPathFormat(){
 		String myCurrOS =  System.getProperty("os.name").toLowerCase();
 		String pathFormat = "";
 		
 		if (myCurrOS.equals("linux")){
 			pathFormat = "/";
 		}else if(myCurrOS.startsWith("win")){
 			pathFormat = "\\";
 		}
 		
 		System.out.println(pathFormat);
 		
 		return pathFormat;
 	}
 }
 
