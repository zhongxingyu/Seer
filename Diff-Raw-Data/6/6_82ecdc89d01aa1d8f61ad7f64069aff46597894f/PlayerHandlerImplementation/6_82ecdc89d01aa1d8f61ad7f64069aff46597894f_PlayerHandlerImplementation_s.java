 package peno.htttp.impl;
 
 import java.util.Collection;
 import java.util.List;
 
 import field.Barcode;
 import field.Border;
 import field.Direction;
 import field.SeesawBorder;
 import field.TilePosition;
 import field.fromfile.MazePart;
 import field.representation.FieldRepresentation;
 import field.representation.PenoHtttpFieldRepresentation;
 import field.simulation.FieldSimulation;
 
 import peno.htttp.DisconnectReason;
 import peno.htttp.PlayerHandler;
 import peno.htttp.Tile;
 import robot.DebugBuffer;
 import robot.Position;
 import robot.Robot;
 
 public class PlayerHandlerImplementation implements PlayerHandler {
 	
 	private Robot robot;
 
 	public PlayerHandlerImplementation(Robot robot) {
 		super();
 		this.robot = robot;
 	}
 	
 	@Override
 	public void gameStarted() {
 		robot.sendPosition();
 		robot.explore();
 		DebugBuffer.addInfo("game started");
 	}
 
 	@Override
 	public void gameStopped() {
 		DebugBuffer.addInfo("game stopped");
 	}
 
 	@Override
 	public void gamePaused() {
 		DebugBuffer.addInfo("game paused");
 		
 	}
 
 	@Override
 	public void gameRolled(int playerNumber, int objectNr) {
 		robot.setObjectNr(objectNr);
 		//playerNumber += 1;
 		DebugBuffer.addInfo("player number: " + playerNumber + " object nr " + objectNr);
 		robot.setPlayerNr(playerNumber);
 	}
 
 	@Override
 	public void playerJoining(String playerId) {
 		DebugBuffer.addInfo("player " + playerId + " joining");
 	}
 
 	@Override
 	public void playerDisconnected(String playerId, DisconnectReason disconnectReason) {
 		DebugBuffer.addInfo("player " + playerId + " disconnected because of " + disconnectReason.toString());
 	}
 
 	@Override
 	public void playerReady(String playerId, boolean isReady) {
 		DebugBuffer.addInfo("player " + playerId + " ready: " + isReady);
 	}
 
 	@Override
 	public void playerFoundObject(String playerID, int playerNumber) {
 		
 	}
 
 	@Override
 	public void playerJoined(String playerId) {
 		DebugBuffer.addInfo("player " + playerId + " joined");
 	}
 
 	@Override
 	public void teamConnected(String partnerId) {
 		robot.setTeamMateID(partnerId);
 		robot.sendPosition();
 	}
 
 	@Override
 	public void teamTilesReceived(List<Tile> tiles) {
 		if (!robot.receivedTeamTiles()) {
 			FieldRepresentation teamMateField = new PenoHtttpFieldRepresentation(tiles);
 			robot.getTeamMate().setField(teamMateField);			
 			robot.setReceivedTeamTiles(true);
 		} else {
 			((PenoHtttpFieldRepresentation)robot.getTeamMate().getField())
 				.addTilesFromTeammate(tiles, robot.getField().getRotation());
 		}
 	}
 	
 	
 
 	@Override
 	public void gameWon(int teamNumber) {
 		robot.stopGame();
 		DebugBuffer.addInfo("victory for " + teamNumber);
 	}
 	
 
 	@Override
 	public void teamPosition(long x, long y, double angle) {
		angle -= 90;
 		angle *= -1;
 		
 		//DebugBuffer.addInfo("teammate orig: " + x + " " + y);
 		/*x = x + robot.getField().getTranslX();
 		y = y + robot.getField().getTranslY();
 		angle = angle - robot.getField().getRotation();*/
 		
 		int teamx = robot.getField().getTeammateStartPos()[0];
 		int teamy = robot.getField().getTeammateStartPos()[1];
 		DebugBuffer.addInfo("field tr: " + teamx + " " + teamy);
 		switch(Direction.fromAngle(robot.getField().getRotation())) {
 		case BOTTOM:
 			teamx -= x;
 			teamy -= y;
 			break;
 		case LEFT:
 			teamx += y;
 			teamy -= x;
 			break;
 		case RIGHT:
 			teamx -= y;
 			teamy += x;
 			break;
 		case TOP:
 			teamx += x;
 			teamy += y;
 			break;
 		default:
 			break;
 
 		}
 		
 		DebugBuffer.addInfo("teammate new: " + teamx + " " + teamy + " :: " + x + " " + y);
 		
 		robot.getTeamMate().setPosition(new Position(
 				0, 0, angle
 				), new field.Tile(new TilePosition(teamx, teamy)));
 		robot.getTeamMate()
 			.setCurrTile(new field.Tile(new TilePosition(teamx, teamy)));
 		robot.setHasValidTeamMatePosition(true);
 
 		
 		/*robot.getTeamMate().setPosition(new Position(
 				0, 0, angle
 				), new field.Tile(new TilePosition((int)x, (int)y)));
 		robot.getTeamMate()
 			.setCurrTile(new field.Tile(new TilePosition((int)x, (int)y)));*/
 		
 	}
 
 
 }
