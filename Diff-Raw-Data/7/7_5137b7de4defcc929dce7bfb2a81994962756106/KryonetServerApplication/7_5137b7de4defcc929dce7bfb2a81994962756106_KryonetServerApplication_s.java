 import java.io.IOException;
 
 import net.dlogic.kryonet.server.KryonetServer;
 import net.dlogic.kryonet.server.KryonetServerException;
 import net.dlogic.kryonet.server.KryonetServerInstance;
import net.dlogic.kryonet.server.KryonetServerListener;
 
 
 public class KryonetServerApplication {
 	public static void main(String[] args) {
 		try {
 			int writeBufferSize = Integer.parseInt(args[0]);
 			int objectBufferSize = Integer.parseInt(args[1]);
 			int tcpPort = Integer.parseInt(args[2]);
 			int udpPort = Integer.parseInt(args[3]);
 			KryonetServerInstance.initialize(writeBufferSize, objectBufferSize);
 			KryonetServer server = KryonetServerInstance.getInstance();
			KryonetServerListener listener = server.getKryonetServerListener();
			listener.setConnectionEventHandler(MyConnectionEventHandler.class);
			listener.setLoginOrLogoutEventHandler(MyLoginOrLogoutEventHandler.class);
 			server.start(tcpPort, udpPort);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (KryonetServerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
