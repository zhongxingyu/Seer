 package pudgewars.network;
 
 import pudgewars.Game;
 import pudgewars.entities.Team;
 import pudgewars.util.Vector2;
 
 public class ClientNetwork extends Network {
 	private MyConnection clientConn;
 
 	public ClientNetwork(MyConnection conn) {
 		super();
 		this.clientConn = conn;
 	}
 
 	public void sendEntityData(String msg){
 		System.out.println(msg);
 		clientConn.sendMessage(msg);
 	}
 	
 	public void getEntityData(){
 		String msg;
 		while(!(msg = clientConn.getMessage()).equals("EOM")){
 			String t[] = msg.split(":");
 			int index = Integer.parseInt(t[0]);
 			if(Game.entities.entities.size()-1 < index) Game.entities.addClientEntity(msg);
			else Game.entities.entities.get(index).setNetworkString(t[1] + ":" + t[2] + ":" + t[3]);
 		}
 	}
 	
 	public void generateEntities() {
 		String msg = clientConn.getMessage();
 		while (!msg.equals("EOM")) {
 			System.out.println(msg);
 			String parts[] = msg.split(" ");
 			Vector2 position = new Vector2(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
 			Team team = (parts[3].equals("leftTeam")) ? Team.leftTeam : Team.rightTeam;
 			boolean controllable = (parts[4].equals("true")) ? true : false;
 			//Game.entities.addClientEntity(position, team, controllable);
 			msg = clientConn.getMessage();
 		}
 	}
 
 	public void getMoveTargets() {
 		String msg = clientConn.getMessage();
 		int x = 0;
 		do {
 			moveTargets.add(null);
 			if (msg.equals("null")) moveTargets.set(x, null);
 			else {
 				String parts[] = msg.split(" ");
 				moveTargets.set(x, new Vector2(Float.parseFloat(parts[0]), Float.parseFloat(parts[1])));
 			}
 			x++;
 			msg = clientConn.getMessage();
 		} while (!msg.equals("EOM"));
 	}
 
 	public void getHookTargets() {
 		String msg = clientConn.getMessage();
 		int x = 0;
 		do {
 			hookTargets.add(null);
 			isSpecialHook.add(false);
 			if (msg.equals("null")) {
 				hookTargets.set(x, null);
 				isSpecialHook.set(x, false);
 			} else {
 				String parts[] = msg.split(" ");
 				hookTargets.set(x, new Vector2(Float.parseFloat(parts[0]), Float.parseFloat(parts[1])));
 				System.out.println(msg);
 				isSpecialHook.set(x, (parts[2].equals("true")) ? true : false);
 			}
 			x++;
 			msg = clientConn.getMessage();
 		} while (!msg.equals("EOM"));
 	}
 
 	public void sendMoveTarget(Vector2 target) {
 		if (target == null) clientConn.sendMessage("null");
 		else clientConn.sendMessage(target.x + " " + target.y);
 	}
 
 	public void sendHookTarget(Vector2 target, boolean isSpecialHook) {
 		if (target == null) clientConn.sendMessage("null");
 		else {
 			target = Game.s.screenToWorldPoint(target);
 			clientConn.sendMessage(target.x + " " + target.y + " " + isSpecialHook);
 		}
 	}
 }
