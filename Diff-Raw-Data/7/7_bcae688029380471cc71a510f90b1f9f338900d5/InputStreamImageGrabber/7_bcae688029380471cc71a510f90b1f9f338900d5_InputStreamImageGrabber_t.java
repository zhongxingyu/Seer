 package ch.zhaw.headtracker.image;
 
import ch.zhaw.headtracker.gui.ControlPanel;
 import java.io.*;
 import java.net.*;
 
 public final class InputStreamImageGrabber implements ImageGrabber {
 	private final InputStream input;
 	private final int width;
 	private final int height;
 
 	public InputStreamImageGrabber(InputStream input, int width, int height) {
 		this.input = input;
 		this.width = width;
 		this.height = height;
 	}
 	
 	@Override
 	public Image getImage() throws IOException {
 		return Image.readFromStream(input, width, height);
 	}
 
	@Override
	public ControlPanel.Setting[] getSettings() {
		return new ControlPanel.Setting[] { };
	}

 	@SuppressWarnings({ "IOResourceOpenedButNotSafelyClosed" })
 	public static ImageGrabber fromFile(String path, int width, int height) throws FileNotFoundException {
 		return new InputStreamImageGrabber(new FileInputStream(path), width, height);
 	}
 
 	@SuppressWarnings({ "SocketOpenedButNotSafelyClosed" })
 	public static ImageGrabber fromSocketAddress(String address, short port, int exposureTimeMicroseconds, int width, int height) throws IOException {
 		Socket socket = new Socket(InetAddress.getByName(address), port);
 
 		PrintStream output = new PrintStream(socket.getOutputStream());
 		
 		// Sent to the leanXcam to set exposure time
 		output.println(exposureTimeMicroseconds);
 		output.flush();
 		socket.getOutputStream().flush();
 		
 		return new InputStreamImageGrabber(socket.getInputStream(), width, height);
 	}
 }
