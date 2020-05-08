 package matrix;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import filterData.DataContainer;
 import filterData.FilterData;
 import filterData.ParserXml;
 import objectPos.Object;
 import objectPos.Position;
 
 public class MatrixCreator {
 
 	private static final int MAX_BLUE = 240;
 	private static final int MIN_BLUE = 10;
 
 	private int height = 2800;
 	private int width = 2800;
 	private int defaultColor = 100;
 	private int red;
 	private int green;
 	private int blue;
 	private ArrayList<ArrayList<Color>> matrix;
 	private ArrayList<int[]> data;
 	private DataContainer infos;
 	private double alpha;
 	private int wheelwidth = 18;
 
 	public MatrixCreator() {
 		matrix = new ArrayList<ArrayList<Color>>();
 	}
 
 	public void createMatrix() {
 		for (int i = 0; i < width; i++) {
 			matrix.add(new ArrayList<Color>());
 			for (int j = 0; j < height; j++) {
 				matrix.get(i).add(new Color(defaultColor, defaultColor, defaultColor));
 			}
 		}
 	}
 
 	public void resetMatrix() {
 		matrix.clear();
 		for (int i = 0; i < width; i++) {
 			matrix.add(new ArrayList<Color>());
 			for (int j = 0; j < height; j++) {
 				matrix.get(i).add(new Color(defaultColor, defaultColor, defaultColor));
 			}
 		}
 	}
 
 	public boolean checkMatrixSize(int x, int y) {
 		if (x >= 0 && x < width && y >= 0 && y < height) {
 			return true;
 		}
 		return false;
 	}
 
 	
 	public void setPixelinMatrix(int x, int y, int r, int g, int b) {
 		//System.out.println("x,y" + x +"," + y);
 		if (checkMatrixSize(x, y)) {
 			matrix.get(x).set(y, new Color(r, g, b));
 		} else { 
 			;//System.out.println("Error setPixelinMatrix(int x, int y, int r, int g, int b): out of map");
 		}
 	}
 
 	public void setPixelinMatrix(int x, int y, Color color) {
 		if (checkMatrixSize(x, y)) {
 			matrix.get(x).set(y, color);
 		} else { 
 			System.out.println("Error setPixelinMatrix(int x, int y, Color color): out of map");
 		}
 	}
 
 	public void setNewPosition(Object obj) {
 		if (obj.getOldPosition() == null) {
 			if (checkMatrixSize(obj.getPosition().getX(), obj.getPosition().getY())) {
 				obj.setCurrentColorObject(matrix.get(obj.getPosition().getX()).get(obj.getPosition().getY()));
 				setPixelinMatrix(obj.getPosition().getX(), obj.getPosition().getY(), 0, 0, obj.getColor());
 				obj.setOldPosition(obj.getPosition());
 			}
 		} else if (obj.getOldPosition().getX() == obj.getPosition().getX() && obj.getOldPosition().getY() == obj.getPosition().getY()) {
 			System.out.println("object do not move");
 
 		} else {
 			// with delete old pos
 			if (checkMatrixSize(obj.getPosition().getX(), obj.getPosition().getY())) {
 				setPixelinMatrix(obj.getOldPosition().getX(), obj.getOldPosition().getY(), obj.getCurrentColorObject());
 				obj.setCurrentColorObject(matrix.get(obj.getPosition().getX()).get(obj.getPosition().getY()));
 				setPixelinMatrix(obj.getPosition().getX(), obj.getPosition().getY(), 0, 0, obj.getColor());
 				obj.setOldPosition(obj.getPosition());
 			}
 		}
 	}
 
 	public void setColorFromScannedPixel(int x, int y) {
 		if (checkMatrixSize(x, y)) {
 			blue = matrix.get(x).get(y).getBlue();
 			green = matrix.get(x).get(y).getGreen();
 			red = matrix.get(x).get(y).getRed();
 		} else { 
 			System.out.println("Error setColorFromScannedPixel(int x, int y): out of map");
 		}
 	}
 
 	public void setScanInfos(DataContainer infos) {
 		this.infos = infos;
 		this.data = infos.getData();
 		infos.setPos(new int[] { 20,20 });
 	}
 
 	public ArrayList<ArrayList<Color>> getCreatedMatrix() {
 		return matrix;
 	}
 
 	private void drawLine(Position myPos, Position scanPos) {
 		int x, y, error, differenz, schritt, dx, dy, inc_x, inc_y;
 		x = myPos.getX();
 		y = myPos.getY();
 		dx = scanPos.getX() - x;
 		dy = scanPos.getY() - y;
 		if (dx > 0) // Linie nach rechts?
 			inc_x = 1; // x inkrementieren
 		else
 			// Linie nach links
 			inc_x = -1; // x dekrementieren
 
 		if (dy > 0) // Linie nach oben ?
 			inc_y = 1; // y inkrementieren
 		else
 			// Linie nach unten
 			inc_y = -1; // y dekrementieren
 
 		if (Math.abs(dy) < Math.abs(dx)) { // flach nach oben oder unten
 			error = -Math.abs(dx); // Fehler bestimmen
 			differenz = 2 * Math.abs(dy); // Delta bestimmen
 			schritt = 2 * error; // Schwelle bestimmen
 			while (x != scanPos.getX()) {
 				if (x != myPos.getX()) {
 					decPix(x, y);
 				}
 				x += inc_x; // naechste x-Koordinate
 				error = error + differenz; // Fehler aktualisieren
 				if (error > 0) { // neue Spalte erreicht?
 					y += inc_y; // y-Koord. aktualisieren
 					error += schritt; // Fehler aktualisieren
 				}
 			}
 		} else { // steil nach oben oder unten
 			error = -Math.abs(dy); // Fehler bestimmen
 			differenz = 2 * Math.abs(dx); // Delta bestimmen
 			schritt = 2 * error; // Schwelle bestimmen
 			while (y != scanPos.getY()) {
 				if (y != myPos.getY()) {// fuer jede y-Koordinate
 					decPix(x, y);
 				}// setze
 				// Pixel
 				y += inc_y; // naechste y-Koordinate
 				error = error + differenz; // Fehler aktualisieren
 				if (error > 0) { // neue Zeile erreicht?
 					x += inc_x; // x-Koord. aktualisieren
 					error += schritt; // Fehler
 					// aktualisieren
 				}
 			}
 		}
 		if ((x != scanPos.getX() && y != scanPos.getY())) {
 			incPix(x, y);
 		}
 	}
 
 	public void decPix(int x, int y) {
 		if (checkMatrixSize(x, y)) {
 			if (matrix.get(x).get(y).getBlue() > MIN_BLUE && matrix.get(x).get(y).getGreen() > MIN_BLUE) {
 				setPixelinMatrix(x, y, (matrix.get(x).get(y).getBlue() - 10), (matrix.get(x).get(y).getGreen() - 10), (matrix.get(x).get(y).getBlue() - 10));
 			}
 		}
 	}
 
 	public void incPix(int x, int y) {
 		if (checkMatrixSize(x, y)) {
 			if (matrix.get(x).get(y).getBlue() < MAX_BLUE && matrix.get(x).get(y).getGreen() > MIN_BLUE) {
 				setPixelinMatrix(x, y, (matrix.get(x).get(y).getBlue() + 10), (matrix.get(x).get(y).getGreen() + 10), (matrix.get(x).get(y).getBlue() + 10));
 			}
 		}
 	}
 
 	public void updateMatrix() {
 		Position myPos = new Position();
 		myPos.setPosArray(infos.getPos());
 		for (int i = 0; i < data.size(); i++) {
 			int x = data.get(i)[0];
 			int y = data.get(i)[1];
 			setColorFromScannedPixel(x, y);
 			if (blue < MAX_BLUE) {
 				blue += 10;
 				green += 10;
 				red += 10;
 			}
 			setPixelinMatrix(x, y, red, green, blue);
 			Position zielPos = new Position();
 			zielPos.setPosArray(data.get(i));
 			drawLine(myPos, zielPos);
 		}
 		data.clear();
 
 		// System.out.println("zielPos"+zielPos.getX()+","+zielPos.getY());
 		// System.out.println("myPos"+myPos.getX()+","+myPos.getY());
 	}
 
 	public void updateAngleToX() {
 		double lchain = infos.getDirection()[0];
 		double rchain = infos.getDirection()[1];
 
 		alpha += ((lchain - rchain) / wheelwidth) * 180 / Math.PI;
 		if (alpha > 360) {
 			alpha -= 360;
 		}
 		System.out.println("alpha"+alpha);
 	}
 	
 	public void updateDataTupel() {
 		for(int i = 0; i < data.size(); i++) {
 			int[] tupel =  rotateVektor(alpha, data.get(i)[0], data.get(i)[1]);
 			tupel[0]+=infos.getPos()[0];
 			tupel[1]+=infos.getPos()[1];
 			data.set(i,tupel);
 		}
 	}
 	
 	
 	private int[] rotateVektor(double alpha, int x, int y) {
		double newX = (x*Math.cos(alpha)-y*Math.sin(alpha));
		double newY = x*Math.sin(alpha)+y*Math.cos(alpha);
 		int[] newTupel = {(int) newX, (int) newY};
 		System.out.println("new tuple: " + newX + "," + newY + "old tuple" + x+"," + y);
 		return newTupel;
 	}
 	
 
 	// public Position absolutePosofobstacle(Position myPos, Position scanPos) {
 	// double dbx = scanPos.getX() - myPos.getX();
 	// double dby = scanPos.getY() - myPos.getY();
 	// double length = Math.sqrt(Math.pow(dbx, 2) + Math.pow(dby, 2));
 	// System.out.println("length"+length);
 	// Position absPos = new Position();
 	// //double gamma = getAngleToX() + getAngleToObstacle(myPos, scanPos);
 	// double gamma = getAngleToX();
 	//
 	// absPos.setX((int) Math.round((length *
 	// Math.cos(Math.toRadians(gamma)))));
 	// absPos.setY((int) Math.round((length *
 	// Math.sin(Math.toRadians(gamma)))));
 	//		
 	// absPos.setX(absPos.getX() + myPos.getX());
 	// absPos.setY(absPos.getY() + myPos.getY());//
 	// //
 	// // System.out.println("Alpha: " + getAngleToX() + " Beta: " +
 	// // getAngleToObstacle(myPos, scanPos) + " Gamma: " + gamma);
 	// // System.out.println("mypos:"+myPos.getX()+","+myPos.getY());
 	// // System.out.println("relativePos:"+scanPos.getX()+","+scanPos.getY());
 	// // System.out.println("absolutePos:" + absPos.getX() + "," +
 	// // absPos.getY());
 	// return absPos;
 	// }
 
 	// public double getAngleToObstacle(Position myPos, Position scanPos) {
 	// double dax = 0, day = 10;
 	// double dbx, dby;
 	// double lengtha, lengthb;
 	// dbx = scanPos.getX() - myPos.getX();
 	// dby = scanPos.getY() - myPos.getY();
 	// lengtha = Math.sqrt(Math.pow(dax, 2) + Math.pow(day, 2));
 	// lengthb = Math.sqrt(Math.pow(dbx, 2) + Math.pow(dby, 2));
 	// double beta = Math.toDegrees(Math.acos((dax * dbx + day * dby) / (lengtha
 	// * lengthb)));
 	//
 	// if (scanPos.getY() > myPos.getY())// oberhalb
 	// {
 	//
 	// if (scanPos.getX() > myPos.getX())// rechts
 	// {
 	// } else {
 	// beta = beta + 90;
 	// }
 	// } else {
 	// if (scanPos.getX() > myPos.getX()) {
 	// beta = beta + 180;
 	// } else {
 	// beta = beta + 90;
 	// }
 	// }
 	// // System.out.println("beta" + beta);
 	// return beta;
 	// }
 	/*
 	 * public static void main(String[] args) {
 	 * 
 	 * MatrixCreator mc = new MatrixCreator(); mc.createMatrix();
 	 * ArrayList<ArrayList<Color>> matrix = mc.getCreatedMatrix();
 	 * 
 	 * // System.out.println(matrix.get(1).get(1).getRGB());
 	 * 
 	 * }
 	 */
 }
