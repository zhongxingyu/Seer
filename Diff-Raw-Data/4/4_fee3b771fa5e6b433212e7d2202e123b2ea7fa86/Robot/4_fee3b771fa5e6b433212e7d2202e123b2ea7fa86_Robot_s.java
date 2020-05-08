 package robot;
 
 import java.util.List;
 
 import robot.brain.Explorer;
 import robot.brain.Pathfinder;
 import simulator.VirtualRobotConnector;
 
 import exception.CommunicationException;
 import field.Barcode;
 import field.Border;
 import field.Direction;
 import field.Field;
 import field.PanelBorder;
 import field.Tile;
 import field.UnsureBorder;
 import field.WhiteBorder;
 
 /**
  * @author  Samuel
  */
 public class Robot {
 	
 	private AbstractRobotConnector robotConn;
 	private Position position = new Position();
 	private Field field = new Field();
 	private boolean isBusy = false;
 	private Tile currTile;
 	private Tile startTile;
 	private Tile endTile;
 	
 	public Robot(int connectionType) {
 		robotConn = ConnectionFactory.getConnection(connectionType);
 	}
 	
 	public void drivePolygon(double length, int corners) throws IllegalArgumentException {
 		if(corners==0 || length == 0){
 			throw new IllegalArgumentException();
 		}
 		else{
 			setBusy(true);
 			double angle = 360/corners;
 			for (int i = 0; i < corners; i++) {
 				moveForward(length);
 				turnRight(angle);
 			}
 			setBusy(false);
 		}
 	}
 
 	public boolean isBusy() {
 		return isBusy;
 	}
 
 	protected void setBusy(boolean isBusy) {
 		this.isBusy = isBusy;
 	}
 	
 	public Tile getStartTile() {
 		return startTile;
 	}
 
 	private void setStartTile(Tile startTile) {
 		this.startTile = startTile;
 	}
 	
 	public Tile getFinishTile() {
 		return endTile;
 	}
 
 	private void setFinishTile(Tile endTile) {
 		this.endTile = endTile;
 	}
 
 	public void initialize() throws CommunicationException {
 		robotConn.initialize();
 		currTile = new Tile(0, 0);
 		/*try {
 			setField(FieldFactory.fieldFromFile("C:\\demo2.txt"));
 			currTile = getField().getTileMap().getObjectAtId(new field.Position(0, 0));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		getField().addTile(currTile);
 		/*List<Border> list = robotConn.getBorderSurroundings();
 		if (list != null) {
 			for (Border border : list) {
 				if (getField().canHaveAsBorder(border.getBorderPos()))
 				{
 					getField().addBorder(border);
 				}
 			}
 		}*/
 	}
 	
 	public void terminate() {
 		robotConn.terminate();
 	}
 	
 	public void startMovingForward() {
 		robotConn.startMovingForward();
 	}
 	
 	public void startMovingBackward() {
 		robotConn.startMovingBackward();
 	}
 	
 	public void stopMoving() {
 		robotConn.stopMoving();
 		updatePosition();
 	}
 	
 	public void moveForward(double distance) {
 		robotConn.moveForward(distance);
 	}
 	
 	public void moveBackward(double distance) {
 		robotConn.moveBackward(distance);
 	}
 
 	public double getDistanceMoved() {
 		return robotConn.getDistanceMoved();
 	}
 	
 	public void startTurningLeft() {
 		robotConn.startTurningLeft();
 	}
 	
 	public void startTurningRight() {
 		robotConn.startTurningRight();
 	}
 	
 	public void turnLeft(double angle) {
 		robotConn.turnLeft(angle);
 	}
 	
 	public void turnRight(double angle) {
 		robotConn.turnRight(angle);
 	}
 	
 	public double getRotationTurned() {
 		return robotConn.getRotationTurned();
 	}
 	
 	public void setMoveSpeed(double moveSpeed) {
 		robotConn.setMoveSpeed(moveSpeed);
 	}
 	
 	public void setTurnSpeed(double turnSpeed) {
 		robotConn.setTurnSpeed(turnSpeed);
 	}
 
 	
 	public void orientOnWhiteLine(boolean b) {
 		robotConn.orientOnWhiteLine(b);
 	}
 	
 	public void scanSonar(){
 		robotConn.scanSonar();
 	}
 	
 	public void setOnCenterTile(){
		//robotConn.setOnCenterTile(this);
		test();
 	}
 	
 	public void newTileScan() {
 		robotConn.newTileScan();
 	}
 	
 	public void checkScan() {
 		robotConn.checkScan();
 	}
 	
 	public void explore() {
 		Thread r = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				Explorer.explore(Robot.this);
 				
 			}
 		});
 		r.start();
 	}
 	
 	List<Tile> tilelistref = null;
 	
 	public List<Tile> getAStarTileList() {
 		return tilelistref;
 	}
 	
 	public void setAStartTileList(List<Tile> input) {
 		tilelistref = input;
 	}
 	
 	public void resetAStartTileList() {
 		tilelistref = null;
 	}
 	
 	public void pauseExplore() {
 		Explorer.pause();
 	}
 	
 	public void resumeExplore() {
 		Explorer.resume();
 	}
 	
 	public boolean isMoving() {
 		return robotConn.isMoving();
 	}
 	
 	/**
 	 * @return if the robot is scanning a barcode
 	 */
 	public boolean isScanning() {
 		return robotConn.isScanning();
 	}
 	
 	public void turnToAngle(double angle) {
 		if (angle < 0)
 			angle += 360;
 		double currAngle = getPosition().getRotation();
 		if (currAngle < 0)
 			currAngle += 360;
 		double delta = angle - currAngle;
 		if (Math.abs(delta) < 1)
 			return;
 		if (delta > 180)
 			delta -= 360;
 		if (delta < -180) 
 			delta += 360;
 		if (delta < 0) {
 			turnLeft(-delta);
 			DebugBuffer.addInfo("l " + delta);
 			//System.out.println("l " + delta);
 		} else {
 			turnRight(delta);
 			DebugBuffer.addInfo("r " + delta);
 			//System.out.println("r " + delta);
 		}
 	}
 	
 	public void travelToNextTile(Tile tile) {
 		if (tile.getPosition().manhattanDistance(getCurrTile().getPosition()) > 1)
 			throw new IllegalArgumentException("tile is not next to current tile " + tile.getPosition());
 		if (tile.getPosition().manhattanDistance(getCurrTile().getPosition()) == 0)
 			return;
 		int diffx = tile.getPosition().getX() - getCurrTile().getPosition().getX();
 		int diffy = tile.getPosition().getY() - getCurrTile().getPosition().getY();
 		if (diffx == 0 && diffy == 1) {
 			turnToAngle(0);
 			//turnLeft(getPosition().getRotation());
 		} else if (diffx == 0 && diffy == -1) {
 			turnToAngle(180);
 			//turnRight(180-getPosition().getRotation());
 		} else if (diffx == 1 && diffy == 0) {
 			turnToAngle(90);
 			//turnRight(90-getPosition().getRotation());
 		} else if (diffx == -1 && diffy == 0) {
 			turnToAngle(-90);
 			//turnRight(270-getPosition().getRotation());
 		}
 		moveNext();
 		//System.out.println("moveto " + tile.getPosition());
 	}
 	
 	public void travelFromTileToTile(Tile start, Tile finish, Tile prev) {
 		int diffx = finish.getPosition().getX() - start.getPosition().getX();
 		int diffy = finish.getPosition().getY() - start.getPosition().getY();
 		int diffxprev = start.getPosition().getX() - prev.getPosition().getX();
 		int diffyprev = start.getPosition().getY() - prev.getPosition().getY();
 		/*if (diffx + diffxprev == 2 || diffy + diffyprev == 2) {
 			orientOnWhiteLine(false);
 			moveForward(200);
 			return;
 		}*/
 		Direction from = Direction.fromDiffPos(diffxprev, diffyprev);
 		Direction to = Direction.fromDiffPos(diffx, diffy);
 		
 		int turn = from.turnTo(to);
 		//DebugBuffer.addInfo("turn "+ turn);
 		if (turn > 0)
 			turnRight(turn);
 		if (turn < 0)
 			turnLeft(-turn);
 		
 		/*if (diffx == 0 && diffy == 1) {
 			//turnToAngle(0);
 			//turnLeft(getPosition().getRotation());
 		} else if (diffx == 0 && diffy == -1) {
 			//turnToAngle(180);
 			//turnRight(180-getPosition().getRotation());
 		} else if (diffx == 1 && diffy == 0) {
 			//turnToAngle(90);
 			//turnRight(90-getPosition().getRotation());
 		} else if (diffx == -1 && diffy == 0) {
 			//turnToAngle(-90);
 			//turnRight(270-getPosition().getRotation());
 		}*/
 
 		if (Math.abs(turn) > 0 && getField().getTileMap().getObjectAtId(start.getPosition()).getBarcode() != null) {
 			moveForward(60);
 			DebugBuffer.addInfo("testcode");
 		}
 		moveNext();
 		//DebugBuffer.addInfo("traveling from "+ start.getPosition() + " to " + finish.getPosition());
 	}
 	
 	private int counter = 0;
 	
 	public void moveNext() {
 		//if (counter == 0){
 			orientOnWhiteLine(false);
 			moveForward(230);
 			counter++;
 		/*} else {
 			moveForward(430);
 			counter = (counter + 1) % 2;
 		}*/
 	}
 	
 	public void travelListOfTiles(final List<Tile> list) {
 		Thread r = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				for (Tile tile : list) {
 					travelToNextTile(tile);
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					while (Robot.this.isMoving()) {
 						try {
 							Thread.sleep(1000);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 				
 			}
 		});
 		r.start();
 	}
 	
 	public void moveToTile(Tile tile) {
 		if (tile == null) return;
 		travelListOfTiles(Pathfinder.findShortestPath(this, tile));
 	}
 	
 	private boolean isWall(int x) {
 		return (x<40);
 	}
 	
 	public void updatePosition() {		
 		getPosition().updatePosition(robotConn.getDistanceMoved());
 		getPosition().updateRotation(robotConn.getRotationTurned());
 		
 		// Set whether the current tile is a start tile, a finish tile or an ordinary tile
 		if(robotConn.isStartTile())
 			addAction(0);
 		else if(robotConn.isFinishTile())
 			addAction(1);
 		
 		//System.out.println("x " + SensorBuffer.canClear());
 		if (!SensorBuffer.canClear() && SensorBuffer.getDistances().size() >= 4) {
 			System.out.println("scan values given");
 			findBorderObjects();
 		}
 		
 		/*field.Position pos = Field.convertToTilePosition(getPosition().getPosX(), getPosition().getPosY());
 		if (!getField().getTileMap().hasId(pos)) {
 			getField().addTile(new Tile(pos));
 			List<Border> list = robotConn.getBorderSurroundings();
 			if (list != null) {
 				for (Border border : list) {
 					if (getField().canHaveAsBorder(border.getBorderPos()))
 					{
 						getField().addBorder(border);
 					}
 				}
 			}
 		}*/
 		//System.out.println("add " + pos);
 		//if (robotConn instanceof VirtualRobotConnector && ((VirtualRobotConnector)robotConn).passedWhiteBorder()) {
 		passedWhiteBorder();
 		if (passedWhite/* || Math.abs(getPosition().getPosX()) > 30 || Math.abs(getPosition().getPosY()) > 30*/) {
 			passedWhite = false;
 			//System.out.println("LINE");
 			Direction dir = Direction.fromPos(getPosition());
 			field.Position newPos = dir.getPositionInDirection(currTile.getPosition());
 			//System.out.println("dit " + dir);
 			if (!getField().getTileMap().hasId(newPos)) {
 				getField().addTile(new Tile(newPos));
 				currTile = getField().getTileMap().getObjectAtId(newPos);
 				System.out.println("add " + newPos);
 			} else {
 				currTile = getField().getTileMap().getObjectAtId(newPos);
 			}
 			if ((robotConn instanceof VirtualRobotConnector && ((VirtualRobotConnector)robotConn).getField().getTileMap().getObjectAtId(newPos).getBarcode() != null)){
 				currTile.setBarcode(((VirtualRobotConnector)robotConn).getField().getTileMap().getObjectAtId(newPos).getBarcode());
 				if (currTile.getBarcode().equals(new Barcode(1,1,0,1,1,1))) {
 					setFinishTile(currTile);
 					DebugBuffer.addInfo("einde");
 				}
 				if (currTile.getBarcode().equals(new Barcode(0,0,1,1,0,1))) {
 					setStartTile(currTile);
 					DebugBuffer.addInfo("start");
 				}
 			}
 			if(field.canHaveAsBorder(dir.opposite().getBorderPositionInDirection(newPos)))
 				field.addBorder(new WhiteBorder(dir.opposite().getBorderPositionInDirection(newPos)));
 			getPosition().resetPosition(dir);
 		}
 	}
 	
 	public void scanOnlyLines(boolean flag) {
 		robotConn.scanOnlyLines(flag);
 	}
 	
 	private boolean passedWhite = false;
 	
 	private boolean correctBarcode = false;
 	
 	public boolean hasCorrectBarcode() {
 		if (correctBarcode) {
 			correctBarcode = false;
 			DebugBuffer.addInfo("has correct barcode!");
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean wrongBarcode = false;
 	
 	public boolean hasWrongBarcode() {
 		if (wrongBarcode) {
 			wrongBarcode = false;
 			DebugBuffer.addInfo("has wrong barcode!");
 			return true;
 		}
 		return false;
 	}
 	
 	public void passedWhiteBorder() {
 		passedWhite = SensorBuffer.getLightUpdates().contains(0);
 		correctBarcode = SensorBuffer.getLightUpdates().contains(1) || correctBarcode;
 		wrongBarcode = SensorBuffer.getLightUpdates().contains(3) || wrongBarcode;
 		if (SensorBuffer.getLightUpdates().size() > 0) {
 			DebugBuffer.addInfo("lijst "+SensorBuffer.getLightUpdates());
 			//DebugBuffer.addInfo("cb " + correctBarcode + " c1 " + SensorBuffer.getLightUpdates().contains(1));
 			//DebugBuffer.addInfo("wb " + wrongBarcode + " c3 " + SensorBuffer.getLightUpdates().contains(3));
 		}
 		//System.out.println("cb " + correctBarcode);
 		SensorBuffer.getLightUpdates().clear();
 		/*boolean passedWhiteBorder = SensorBuffer.getLightUpdates().contains(0);
 		SensorBuffer.getLightUpdates().remove((Integer)0);
 		return passedWhiteBorder;*/
 	}
 	
 	public void zeroPos() {
 		//setField(new Field());
 		getField().clear();
 		currTile = new Tile(0, 0);
 		getField().addTile(currTile);
 		if (robotConn instanceof VirtualRobotConnector) {
 			((VirtualRobotConnector)robotConn).zeroPos();
 		}
 		getPosition().zeroPos();
 	}
 	
 	public void fieldFromFile(String filename) {
 		((VirtualRobotConnector)robotConn).SetupFieldFile(filename);
 		zeroPos();
 	}
 	
 	/**
 	 * @return
 	 * @uml.property  name="position"
 	 */
 	public Position getPosition() {
 		return position;
 	}
 	
 	public Field getField() {
 		return field;
 	}
 	public void setField(Field f) {
 		field = f;
 	}
 	
 	public Tile getCurrTile() {
 		return currTile;
 	}
 	
 	private void findTileObjects(){
 		// maakt nieuwe vakjes aan wanneer ontdekt.
 		// plaatst ook witte lijn tussen vorig en huidig vakje.
 		// moet aangeroepen wanneer witte lijn overschreden.
 		// nieuwe tegel adhv positie in vakje. (voor reset van positie vorig vakje!).
 		if (Math.abs(position.getPosX()) > 20 || Math.abs(position.getPosY()) > 20){
 			int currXCoord = getCurrTile().getPosition().getX();
 			int currYCoord = getCurrTile().getPosition().getY();
 			int xCoord = 0;
 			int yCoord = 0;
 			if (Math.abs(position.getPosX()) > 20){
 				if (position.getPosX() > 20){
 					xCoord = currXCoord + 1;
 				} else {
 					xCoord = currYCoord - 1;
 				}
 			}
 			if (Math.abs(position.getPosY()) > 20){
 				if (position.getPosX() > 20){
 					yCoord = currXCoord + 1;
 				} else {
 					yCoord = currYCoord - 1;
 				}
 			}
 			Tile tile = new Tile(xCoord,yCoord);
 			field.addTile(tile);
 			WhiteBorder whiteBorder = new WhiteBorder(currXCoord,currYCoord,xCoord,yCoord);
 			field.addBorder(whiteBorder);
 		}
 	}
 	private void findBorderObjects(){
 		// maakt nieuwe borders aan wanneer ontdekt.
 		// aanroepen na scan met ultrasone sensor.
 		// klopt niet altijd wanneer scheef => telkens witte lijn => rechtzetten.
 		// probeer ook midden van tegels te rijden.
 		// rotatie is hier 0 als naar boven gericht.
 		Tile tile = getCurrTile();
 		int xCoord1 = tile.getPosition().getX();
 		int yCoord1 = tile.getPosition().getY();
 		double orientation = getPosition().getRotation();
 		List<Integer> distances = SensorBuffer.getDistances();
 		if (distances.size() >= 4) {
 			double sensorRotation = 0.0;
 			for (int i = 0; i < 4; i++){
 				int distance = SensorBuffer.getDistances().get(distances.size() - 4 + i);
 				int xCoord2 = 0;
 				int yCoord2 = 0;
 				double rotation = ((orientation + sensorRotation) % 360);
 				if (rotation < 0)
 					rotation += 360;
 				if (rotation > 225 && rotation < 315){
 					// WEST (links)
 					xCoord2 = xCoord1 - 1;
 					yCoord2 = yCoord1;
 				}
 				if (rotation > 135 && rotation < 225){
 					// ZUID (onder)
 					xCoord2 = xCoord1;
 					yCoord2 = yCoord1 - 1;
 				}
 				if (rotation > 45 && rotation < 135){
 					// OOST (rechts)
 					xCoord2 = xCoord1 + 1;
 					yCoord2 = yCoord1;
 				}
 				if (rotation > 315 || rotation < 45){
 					// NOORD (boven)
 					xCoord2 = xCoord1;
 					yCoord2 = yCoord1 + 1;
 				}
 				Border border;
 				//System.out.println("x1 " + xCoord1 + " y1 " + yCoord1 + " x2 " + xCoord2 + " y2 " + yCoord2 + " r " + rotation + " o " + orientation + " sr " + sensorRotation);
 				if (distance != -1) {
 					if (distance < 25){
 						border = new PanelBorder(xCoord1,yCoord1,xCoord2,yCoord2);
 					} else if (distance >= 25 && distance < 45){
 						border = new UnsureBorder(xCoord1,yCoord1,xCoord2,yCoord2);
 					} else {
 						border = new WhiteBorder(xCoord1,yCoord1,xCoord2,yCoord2);
 					}
 					if (field.canHaveAsBorder(border.getBorderPos())) {
 						field.addBorder(border);
 						if (border instanceof WhiteBorder) {
 							if (!field.getTileMap().hasId(new field.Position(xCoord2, yCoord2)))
 								field.addTile(new Tile(xCoord2, yCoord2));
 						}
 					}
 				}
 				sensorRotation -= 90.0;
 			}
 			SensorBuffer.getDistances().clear();
 			SensorBuffer.setClear(false);
 		}
 	}
 	
 	public void waitTillRobotStops() {
 		try {
 			Thread.sleep(400);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		while (isMoving()) {
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public boolean isSim() {
 		return robotConn instanceof VirtualRobotConnector;
 	}
 	
 	public double getSimX() {
 		return ((VirtualRobotConnector)robotConn).getTDistanceX();
 	}
 	
 	public double getSimY() {
 		return ((VirtualRobotConnector)robotConn).getTDistanceY();
 	}
 	
 	public double getSimAngle() {
 		return ((VirtualRobotConnector)robotConn).getTRotation() * Math.PI / 180;
 	}
 	
 	private double width = 2;
 	private double length = 2;
 	
 	public double[][] getCorners() {
 		double[][] arr = new double[4][2];
 		
 		double tx = getPosition().getPosX();
 		double ty = getPosition().getPosY();
 		double ta = -getPosition().getRotation() * Math.PI / 180;
 		
 		
 		//left upper
 		arr[0][0] = tx + ( width * Math.cos(ta + Math.PI) + length * Math.sin(ta + Math.PI)); 
 		arr[0][1] = ty + ( length * Math.sin(ta + Math.PI / 2)  + width * Math.cos(ta + Math.PI / 2)); 
 		//right upper
 		arr[1][0] = tx + ( width * Math.cos(ta)  + length * Math.sin(ta)); 
 		arr[1][1] = ty + ( length * Math.sin(ta + Math.PI / 2)  + width * Math.cos(ta + Math.PI / 2)); 
 		//left lower
 		arr[2][0] = tx + ( width * Math.cos(ta + Math.PI)  + length * Math.sin(ta + Math.PI)); 
 		arr[2][1] = ty + ( length * Math.sin(ta + 3 * Math.PI / 2)  + width * Math.cos(ta + 3 * Math.PI / 2)); 
 		//right lower
 		arr[3][0] = tx + ( width * Math.cos(ta)  + length * Math.sin(ta)); 
 		arr[3][1] = ty + ( length * Math.sin(ta + 3 * Math.PI / 2)  + width * Math.cos(ta + 3 * Math.PI / 2)); 
 		
 		
 		
 		return arr;
 	}
 	
 	public double[] getCornersX() {
 		double[] xarr = new double[4];
 		
 		double[][] arr = getCorners();
 		
 		xarr[0] = arr[0][0];
 		xarr[1] = arr[1][0];
 		xarr[2] = arr[2][0];
 		xarr[3] = arr[3][0];
 		
 		return xarr;
 	}
 	
 	public double[] getCornersY() {
 		double[] yarr = new double[4];
 		
 		double[][] arr = getCorners();
 		
 		yarr[0] = arr[0][1];
 		yarr[1] = arr[1][1];
 		yarr[2] = arr[2][1];
 		yarr[3] = arr[3][1];
 		
 		return yarr;
 	}
 	
 	public void test() {
 		double[][] corners = ((VirtualRobotConnector)robotConn).getCorners();
 		System.out.println("corners: x1:" + corners[0][0] + " y1: " + corners[0][1]
 				 + " x2: " + corners[1][0] + " y2: " + corners[1][1]
 						 + " x3: " + corners[2][0] + " y3: " + corners[2][1]
 								 + " x4: " + corners[3][0] + " y4: " + corners[3][1]);
 	}
 
 	public void addAction(int action) {
 		System.out.println("action: " + action);
 		switch(action) {
 			case 0: 
 				setStartTile(currTile);
 				DebugBuffer.addInfo("Start tile set: " + currTile.getPosition());
 				break;
 			case 1:
 				setFinishTile(currTile);
 				DebugBuffer.addInfo("Finish tile set: " + currTile.getPosition());
 				break;
 		}
 		
 	}
 
 }
