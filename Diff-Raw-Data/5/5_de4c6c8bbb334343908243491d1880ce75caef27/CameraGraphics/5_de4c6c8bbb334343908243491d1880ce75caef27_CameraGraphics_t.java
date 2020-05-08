 package DeviceGraphics;
 
 import java.util.ArrayList;
 
 import Networking.Request;
 import Networking.Server;
 import Utils.Constants;
 import Utils.Location;
 import agent.Agent;
 import agent.CameraAgent;
 
 /**
  * Server-side Camera object
  * @author Peter Zhang
  */
 public class CameraGraphics implements DeviceGraphics,
 		GraphicsInterfaces.CameraGraphics {
 
 	private final Location location;
 
 	private final Server server;
 	private final CameraAgent agent;
 
 	public CameraGraphics(Server myServer, Agent a) {
 		server = myServer;
 		agent = (CameraAgent) a;
 
 		location = new Location(100, 100);
 	}
 
 	@Override
 	public void takeNestPhoto(GraphicsInterfaces.NestGraphics nest1,
 			GraphicsInterfaces.NestGraphics nest2) {
 		ArrayList<Location> nests = new ArrayList<Location>();
 		nests.add(nest1.getLocation());
 		nests.add(nest2.getLocation());
 
 		server.sendData(new Request(Constants.CAMERA_TAKE_NEST_PHOTO_COMMAND,
 				Constants.CAMERA_TARGET, nests));
 		agent.msgTakePictureNestDone(nest1, true, nest2, true);
 	}
 
 	@Override
 	public void takeKitPhoto(KitGraphics kit) {
 		server.sendData(new Request(Constants.CAMERA_TAKE_KIT_PHOTO_COMMAND,
				Constants.CAMERA_TARGET, kit.getLocation()));
 		agent.msgTakePictureKitDone(kit, true);
 	}
 
 	@Override
 	public void receiveData(Request req) {
 		if (req.getCommand().equals(Constants.CAMERA_TAKE_NEST_PHOTO_COMMAND)) {
 			KitGraphics kit = new KitGraphics(server);
 			kit.setLocation(new Location(20, 200));
 
 			// agent.startV0Sequence(kit);
 
 		}
 	}
 
 }
