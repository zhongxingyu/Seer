 package cz.muni.fi.xtovarn.heimdall.localserver;
 
 import com.google.inject.Inject;
 import cz.muni.fi.xtovarn.heimdall.guice.Startable;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
public class LocalSocketServer implements Startable, Runnable {
 
	private final ExecutorService service = Executors.newFixedThreadPool(2);
 	private final Resender resender;
 	private ServerSocket serverSocket;
 
 	@Inject
 	public LocalSocketServer(Resender resender) {
 		this.resender = resender;
 	}
 
 	@Override
 	public void start() {
 		boolean bRunning = false;
 
 		try {
 			serverSocket = new ServerSocket(5000);
 			bRunning = true;
 
 			while (true) {
 				Socket socket = serverSocket.accept();
 				service.submit(new PushToResender(socket, resender));
 			}
 		} catch (IOException e) {
 			if (serverSocket != null && serverSocket.isClosed())
 				; //Ignore if closed by stopServer() call
 			else
 				e.printStackTrace();
 		} finally {
 			serverSocket = null;
 			bRunning = false;
 		}
 	}
 
 	@Override
 	public void stop() {
 		System.out.println("Closing " + this.getClass() + "...");
 
 		try {
 			serverSocket.close();
 		} catch (IOException e) {
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 
 		System.out.println(this.getClass() + " closed!");
 	}

	@Override
	public void run() {
		start();
	}
 }
