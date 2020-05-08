 package BluetoothNotify.Bluetooth;
 
 import BluetoothNotify.Main;
 import java.io.IOException;
 import java.io.InputStream;
 import javax.microedition.io.Connector;
 import javax.microedition.io.StreamConnection;
 
 /**
  *
  * @author FH
  */
 public class DataConnection implements Runnable {
 
 	private Main main;
 	private Thread me;
 	private boolean stop;
 	private String connectionURL;
 
 	public DataConnection(Main main) {
 		this.main = main;
 		connectionURL = "";
 		reInit();
 	}
 
 	private void reInit() {
 		stop = false;
 	}
 
 	public void start() {
 		me = new Thread(this);
 		me.start();
 	}
 
 	public void run() {
 
 		try {
 			StreamConnection connection = (StreamConnection) Connector.open(connectionURL);
 			InputStream in = connection.openInputStream();
 
 			byte[] serialData;
 			while (!stop) {
 				int lengthavai = 0;
 				lengthavai = in.available();
 
 				if (lengthavai > 0) {
 					serialData = new byte[lengthavai];
 					int length = in.read(serialData);
 					System.out.println("data read: " + new String(serialData));
 					//dataViewForm.append(new String(serialData));
 				}
 			}
 
 			in.close();
 			connection.close();
 
 		} catch (IOException ioe) {
			main.getVisualMain().displayError("Error while communicating: "+ioe.getMessage());
 		}
 	}
 
 	public String getConnectionURL() {
 		return connectionURL;
 	}
 
 	public void setConnectionURL(String ConnectionURL) {
 		this.connectionURL = ConnectionURL;
 	}
 }
