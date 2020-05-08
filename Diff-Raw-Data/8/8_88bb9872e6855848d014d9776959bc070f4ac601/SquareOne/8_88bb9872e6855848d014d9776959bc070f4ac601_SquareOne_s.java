 package edu.berkeley.gcweb.gui.gamescubeman.SquareOne;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import edu.berkeley.gcweb.gui.gamescubeman.PuzzleUtils.PuzzleSticker;
 import edu.berkeley.gcweb.gui.gamescubeman.PuzzleUtils.PuzzleTurn;
 import edu.berkeley.gcweb.gui.gamescubeman.PuzzleUtils.TwistyPuzzle;
 import edu.berkeley.gcweb.gui.gamescubeman.PuzzleUtils.Utils;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.PolygonCollection;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.RotationMatrix;
 
 public class SquareOne extends TwistyPuzzle {
 	public SquareOne() {
 		super(0, 0, 4);
 	}
 	
 	public String getPuzzleName() {
		return "Square One";
 	}
 	
 	private static final String NORMAL = "Square 1",
 								TWO_LAYER = "Two Layer Square 1",
 								UNBANDAGED = "Square 2",
 								THREE_FLOWER = "3 Layer Flower",
 								TWO_FLOWER = "2 Layer Flower",
 								BARREL = "Barrel"; //edges only, w/ matching middle layer
 	protected String[] getPuzzleVariations() {
 		//http://twistypuzzles.com/forum/viewtopic.php?t=7493
 		return new String[] { NORMAL, TWO_LAYER, UNBANDAGED };//, THREE_FLOWER, TWO_FLOWER, BARREL };
 	}
 	public void setPuzzleVariation(String variation) {
 		super.setPuzzleVariation(variation);
 		createPolys(false);
 	}
 	protected double getDefaultStickerGap() {
 		return 0.05;
 	}
 	public void resetRotation() {
 		setRotation(new RotationMatrix(0, -45).multiply(new RotationMatrix(1, -15)));
 	}
 	
 	//these are the pieces on top arranged counterclockwise, and null if the second part of a corner
 	//the bottom is the same as the top, but starting in the back, such that a slash turn will switch the 0th indices
 	private PolygonCollection<PuzzleSticker>[] ogTopLayerPolys, ogBottomLayerPolys;
 	private PolygonCollection<PuzzleSticker>[] topLayerPolys, bottomLayerPolys;
 	private Iterable<PolygonCollection<PuzzleSticker>> getHalfPolys(boolean rightHalf) {
 		ArrayList<PolygonCollection<PuzzleSticker>> polys = new ArrayList<PolygonCollection<PuzzleSticker>>();
 		int start = rightHalf ? 0 : 6;
 		for(int i = start; i < start+6; i++) {
 			if(topLayerPolys[i] != null)
 				polys.add(topLayerPolys[i]);
 			if(bottomLayerPolys[i] != null)
 				polys.add(bottomLayerPolys[i]);
 		}
 		if(rightHalf)
 			polys.add(rightHalfPolys);
 		else
 			polys.add(leftHalfPolys);
 		return polys;
 	}
 	private PolygonCollection<PuzzleSticker> leftHalfPolys, rightHalfPolys;
 	private Boolean[] topLayer, bottomLayer;
 	//this keeps track of how many times the left and right halves have been twisted
 	private boolean leftHalfEven, rightHalfEven, leftRightSwitched;
 	protected void createPolys2(boolean copyOld) {
 		double gap = super.getStickerGap();
 		double sin15 = Math.sin(Math.toRadians(15));
 		double sin30 = Math.sin(Math.toRadians(30));
 		double tan37_5 = Math.tan(Math.toRadians(37.5));
 		double layerHeight = 1/3. * 2;
 		
 		PolygonCollection<PuzzleSticker> topEdge = new PolygonCollection<PuzzleSticker>();
 		PuzzleSticker sticker = new PuzzleSticker();
 		sticker.addPoint(0, 1, gap/sin15);
 		sticker.addPoint(-sin15+gap/tan37_5, 1, 1-gap);
 		sticker.addPoint(sin15-gap/tan37_5, 1, 1-gap);
 		topEdge.add(sticker);
 		
 		sticker = new PuzzleSticker();
 		sticker.addPoint(-sin15+gap/tan37_5, 1-gap, 1);
 		sticker.addPoint(sin15-gap/tan37_5, 1-gap, 1);
 		sticker.addPoint(sin15-gap/tan37_5, 1-layerHeight+gap, 1);
 		sticker.addPoint(-sin15+gap/tan37_5, 1-layerHeight+gap, 1);
 		topEdge.add(sticker);
 
 		//the sticker immediately clockwise of the U/D sticker
 		PuzzleSticker cwsticker = new PuzzleSticker();
 		cwsticker.addPoint(1, 1-gap, 1-gap);
 		cwsticker.addPoint(1, 1-gap, sin15+gap*tan37_5);
 		cwsticker.addPoint(1, 1-layerHeight+gap, sin15+gap*tan37_5);
 		cwsticker.addPoint(1, 1-layerHeight+gap, 1-gap);
 		
 		PuzzleSticker ccwsticker = new PuzzleSticker();
 		ccwsticker.addPoint(sin15+gap*tan37_5, 1-gap, 1);
 		ccwsticker.addPoint(1-gap, 1-gap, 1);
 		ccwsticker.addPoint(1-gap, 1-layerHeight+gap, 1);
 		ccwsticker.addPoint(sin15+gap*tan37_5, 1-layerHeight+gap, 1);
 		
 		ArrayList<PolygonCollection<PuzzleSticker>> topCorners = new ArrayList<PolygonCollection<PuzzleSticker>>();
 		PolygonCollection<PuzzleSticker> corner = new PolygonCollection<PuzzleSticker>();
 		boolean bandagedCorners = !getPuzzleVariation().equals(UNBANDAGED);
 		if(!bandagedCorners) {
 			sticker = new PuzzleSticker();
 			sticker.addPoint(gap/sin30/Math.sqrt(2), 1, gap/sin30/Math.sqrt(2)+gap);
 			sticker.addPoint(sin15+gap*tan37_5, 1, 1-gap);
 			sticker.addPoint(1-gap*2, 1, 1-gap);
 			corner.add(sticker);
 			corner.add(ccwsticker);
 			topCorners.add(corner);
 			
 			corner = new PolygonCollection<PuzzleSticker>();
 			sticker = new PuzzleSticker();
 			sticker.addPoint(gap/sin30/Math.sqrt(2)+gap, 1, gap/sin30/Math.sqrt(2));
 			sticker.addPoint(1-gap, 1, sin15+gap*tan37_5);
 			sticker.addPoint(1-gap, 1, 1-gap*2);
 			corner.add(sticker);
 			corner.add(cwsticker);
 			topCorners.add(corner);
 		} else {
 			sticker = new PuzzleSticker();
 			sticker.addPoint(gap/sin30/Math.sqrt(2), 1, gap/sin30/Math.sqrt(2));
 			sticker.addPoint(sin15+gap*tan37_5, 1, 1-gap);
 			sticker.addPoint(1-gap, 1, 1-gap);
 			sticker.addPoint(1-gap, 1, sin15+gap*tan37_5);
 			corner.add(sticker);
 			corner.add(cwsticker);
 			corner.add(ccwsticker);
 			topCorners.add(corner);
 		}	
 		
 		boolean twoLayer = getPuzzleVariation().equals(TWO_LAYER) || getPuzzleVariation().equals(TWO_FLOWER);
 		if(twoLayer) {
 			for(PolygonCollection<PuzzleSticker> topCorner : topCorners) {
 				topCorner.translate(0, -.5*layerHeight, 0);
 			}
 			topEdge.translate(0, -.5*layerHeight, 0);
 		} else {
 			RotationMatrix leftRotate = null, rightRotate = null;
 			if(copyOld) {
 				leftRotate = leftHalfPolys.getNetRotations();
 				rightRotate = rightHalfPolys.getNetRotations();
 			}
 			leftHalfPolys = new PolygonCollection<PuzzleSticker>();
 			sticker = new PuzzleSticker();
 			sticker.addPoint(1-gap, -layerHeight/2.+gap, -1);
 			sticker.addPoint(1-gap, layerHeight/2.-gap, -1);
 			sticker.addPoint(sin15+gap*tan37_5, layerHeight/2.-gap, -1);
 			sticker.addPoint(sin15+gap*tan37_5, -layerHeight/2.+gap, -1);
 			leftHalfPolys.add(sticker);
 			sticker = new PuzzleSticker();
 			sticker.addPoint(1-gap, layerHeight/2.-gap, 1);
 			sticker.addPoint(1-gap, -layerHeight/2.+gap, 1);
 			sticker.addPoint(-sin15+gap/tan37_5, -layerHeight/2.+gap, 1);
 			sticker.addPoint(-sin15+gap/tan37_5, layerHeight/2.-gap, 1);
 			leftHalfPolys.add(sticker);
 			sticker = new PuzzleSticker();
 			sticker.addPoint(1, layerHeight/2.-gap, 1-gap);
 			sticker.addPoint(1, -layerHeight/2.+gap, 1-gap);
 			sticker.addPoint(1, -layerHeight/2.+gap, -1+gap);
 			sticker.addPoint(1, layerHeight/2.-gap, -1+gap);
 			leftHalfPolys.add(sticker);
 			addPolys(leftHalfPolys);
 
 			rightHalfPolys = leftHalfPolys.clone();
 			rightHalfPolys.rotate(new RotationMatrix(0, 180), false);
 			rightHalfPolys.mirror(0);
 			addPolys(rightHalfPolys);
 			
 			leftHalfPolys.get(0).setFace("F");
 			leftHalfPolys.get(1).setFace("B");
 			leftHalfPolys.get(2).setFace("L");
 			rightHalfPolys.get(0).setFace("B");
 			rightHalfPolys.get(1).setFace("F");
 			rightHalfPolys.get(2).setFace("R");
 			if(copyOld) {
 				//this is some nasty stuff to get the puzzle to not reset when the gap size is changed
 				leftHalfPolys.rotate(leftRotate);
 				rightHalfPolys.rotate(rightRotate);
 			}
 		}
 		
 		PolygonCollection<PuzzleSticker> downEdge = topEdge.clone().mirror(1);
 		ArrayList<PolygonCollection<PuzzleSticker>> downCorners = new ArrayList<PolygonCollection<PuzzleSticker>>();
 		for(PolygonCollection<PuzzleSticker> coll : topCorners) {
 			PolygonCollection<PuzzleSticker> cc = coll.clone().mirror(1);
 			if(bandagedCorners) //this means we have bandaged corners w/ 3 polys
 				cc.swap(1, 2); //we want to maintain our clockwise ordering (messed up by the mirroring)
 			downCorners.add(cc);
 		}
 		if(!bandagedCorners) {
 			Utils.swap(downCorners, 0, 1);
 		}
 		
 		RotationMatrix cw90 = new RotationMatrix(1, -90);
 		RotationMatrix ccw90 = new RotationMatrix(1, 90);
 		for(PolygonCollection<PuzzleSticker> topCorner : topCorners)
 			topCorner.rotate(cw90, false).rotate(cw90, false);
 		topEdge.rotate(cw90, false).rotate(cw90, false);
 		for(PolygonCollection<PuzzleSticker> downCorner : downCorners)
 			downCorner.rotate(cw90, false);
 		downEdge.rotate(cw90, false);
 
 		for(PolygonCollection<PuzzleSticker> topCorner : topCorners)
 			topCorner.get(0).setFace("U");
 		topEdge.get(0).setFace("U");
 		for(PolygonCollection<PuzzleSticker> downCorner : downCorners)
 			downCorner.get(0).setFace("D");
 		downEdge.get(0).setFace("D");
 		
 		ArrayList<PolygonCollection<PuzzleSticker>> top = new ArrayList<PolygonCollection<PuzzleSticker>>();
 		ArrayList<PolygonCollection<PuzzleSticker>> down = new ArrayList<PolygonCollection<PuzzleSticker>>();
 		String[] faces = { "F", "R", "B", "L" };
 		for(int i=0; i<faces.length; i++) {
 			topEdge.get(1).setFace(faces[i]);
 			PuzzleSticker cw, ccw;
 			cw = bandagedCorners ? topCorners.get(0).get(1) : topCorners.get(1).get(1);
 			ccw = bandagedCorners ? topCorners.get(0).get(2) : topCorners.get(0).get(1);
 			ccw.setFace(faces[i]);
 			cw.setFace(faces[(i+1)%faces.length]);
 			
 			cw = bandagedCorners ? downCorners.get(0).get(1) : downCorners.get(1).get(1);
 			ccw = bandagedCorners ? downCorners.get(0).get(2) : downCorners.get(0).get(1);
 			downEdge.get(1).setFace(faces[(faces.length-i+1)%faces.length]);
 			ccw.setFace(faces[(faces.length-i+2)%faces.length]);
 			cw.setFace(faces[(faces.length-i+1)%faces.length]);
 			
 			top.add(topEdge);
 			for(PolygonCollection<PuzzleSticker> topCorner : topCorners)
 				top.add(topCorner);
 			if(bandagedCorners)
 				top.add(null);
 			
 			for(PolygonCollection<PuzzleSticker> downCorner : downCorners)
 				down.add(downCorner);
 			if(bandagedCorners)
 				down.add(null);
 			down.add(downEdge);
 			
 			addPolys(topEdge);
 			for(PolygonCollection<PuzzleSticker> topCorner : topCorners)
 				addPolys(topCorner);
 			for(PolygonCollection<PuzzleSticker> downCorner : downCorners)
 				addPolys(downCorner);
 			addPolys(downEdge);
 			topEdge = topEdge.clone().rotate(ccw90, false);
 			for(int c=0; c<topCorners.size(); c++) {
 				topCorners.set(c, topCorners.get(c).clone().rotate(ccw90, false));
 				downCorners.set(c, downCorners.get(c).clone().rotate(cw90, false));
 			}
 			downEdge = downEdge.clone().rotate(cw90, false);
 		}
 		PolygonCollection<PuzzleSticker>[] oldTopLayer = topLayerPolys, oldBottomLayer = bottomLayerPolys;
 		topLayerPolys = (PolygonCollection<PuzzleSticker>[]) top.toArray(new PolygonCollection[0]);
 		bottomLayerPolys = (PolygonCollection<PuzzleSticker>[]) down.toArray(new PolygonCollection[0]);
 		
 		RotationMatrix m = new RotationMatrix(1, 15);
 		for(PolygonCollection<PuzzleSticker> coll : topLayerPolys)
 			if(coll != null)
 				coll.rotate(m, false);
 		for(PolygonCollection<PuzzleSticker> coll : bottomLayerPolys)
 			if(coll != null)
 				coll.rotate(m, false);
 		leftHalfPolys.rotate(m, false);
 		rightHalfPolys.rotate(m, false);
 
 		PolygonCollection<PuzzleSticker>[] oldOgTopLayerPolys = ogTopLayerPolys, oldOgBottomLayerPolys = ogBottomLayerPolys;
 		ogTopLayerPolys = Utils.copyOf(topLayerPolys, topLayerPolys.length);
 		ogBottomLayerPolys = Utils.copyOf(bottomLayerPolys, bottomLayerPolys.length);
 		if(copyOld) {
 			//this is some nasty stuff to get the puzzle to not reset when the gap size is changed
 			for(int i=0; i<ogTopLayerPolys.length; i++) {
 				if(ogTopLayerPolys[i] != null) {
 					ogTopLayerPolys[i].rotate(oldOgTopLayerPolys[i].getNetRotations());
 					
 					PolygonCollection<PuzzleSticker>[] searchLayer = oldTopLayer, destLayer = topLayerPolys;
 					int currPosition;
 					while((currPosition = Utils.indexOf(oldOgTopLayerPolys[i], searchLayer)) == -1) {
 						searchLayer = oldBottomLayer; destLayer = bottomLayerPolys;
 					}
 					destLayer[currPosition] = ogTopLayerPolys[i];
 					if(ogTopLayerPolys[(i+1)%ogTopLayerPolys.length] == null)
 						destLayer[(currPosition+1)%destLayer.length] = null;
 				}
 				if(ogBottomLayerPolys[i] != null) {
 					ogBottomLayerPolys[i].rotate(oldOgBottomLayerPolys[i].getNetRotations());
 					
 					int currPosition = -1;
 					PolygonCollection<PuzzleSticker>[] searchLayer = oldTopLayer, destLayer = topLayerPolys;
 					while((currPosition = Utils.indexOf(oldOgBottomLayerPolys[i], searchLayer)) == -1) {
 						searchLayer = oldBottomLayer; destLayer = bottomLayerPolys;
 					}
 					destLayer[currPosition] = ogBottomLayerPolys[i];
 					if(ogBottomLayerPolys[(i+1)%ogBottomLayerPolys.length] == null)
 						destLayer[(currPosition+1)%destLayer.length] = null;
 				}
 			}
 		}
 		
 		topLayer = new Boolean[topLayerPolys.length];
 		for(int i=0; i<topLayer.length; i++)
 			topLayer[i] = (topLayerPolys[i] != null);
 		bottomLayer = new Boolean[bottomLayerPolys.length];
 		for(int i=0; i<bottomLayer.length; i++)
 			bottomLayer[i] = (bottomLayerPolys[i] != null);
 		leftHalfEven = rightHalfEven = true;
 		leftRightSwitched = copyOld ? leftRightSwitched : false;
 	}
 	
 	private class SquareOneTurn extends PuzzleTurn {
 		private int top, down;
 		public SquareOneTurn(int topPieces, int bottomPieces, boolean legalIncrements) {
 			if(legalIncrements) {
 				int topDir = (int) Math.signum(topPieces), bottomDir = (int) Math.signum(bottomPieces);
 				while(Math.abs(topPieces) > 0 || Math.abs(bottomPieces) > 0) {
 					topPieces -= topDir;
 					bottomPieces -= bottomDir;
 					do {
 						top += topDir;
 						down += bottomDir;
 					} while(!isSlashLegal(top, down));
 					if(topPieces == 0) topDir = 0;
 					if(bottomPieces == 0) bottomDir = 0;
 				}
 			} else {
 				this.top = topPieces;
 				this.down = bottomPieces;
 			}
 		}
 		//this is a slash
 		private boolean slash, leftSlash, cw;
 		private int ccw_axis;
 		public SquareOneTurn(boolean left, boolean cw) {
 			this.cw = cw;
 			slash = true;
 			leftSlash = left;
 			ccw_axis = cw ? 1 : -1;
 			if(!left)
 				ccw_axis = -ccw_axis;
 		}
 		private static final String axisnames = "xyz";
 		private int axis=-1;
 		private char axisname;
 		public SquareOneTurn(int axis, boolean cw) {
 			this.axis = axis;
 			axisname = axisnames.charAt(axis);
 			this.cw = cw;
 			ccw_axis = cw ? 1 : -1;
 			//z and x need to be inverted
 			if(axis == 2 || axis == 0)
 				ccw_axis = -ccw_axis;
 		}
 		private int frames = -1;
 		private RotationMatrix topRotationMatrix, downRotationMatrix, rotationMatrix;
 		public boolean animateMove() {
 			if(frames == -1) {
 				frames = getFramesPerAnimation();
 				if(slash) {
 					rotationMatrix = new RotationMatrix(0, ccw_axis*180./frames);
 				} else if(axis != -1) {
 					rotationMatrix = new RotationMatrix(axis, -ccw_axis*180./frames);
 				} else {
 					topRotationMatrix = new RotationMatrix(1, -top*30./frames);
 					downRotationMatrix = new RotationMatrix(1, down*30./frames);
 				}
 			}
 			if(slash) {
 				for(PolygonCollection<PuzzleSticker> polys : getHalfPolys(!leftSlash))
 					polys.rotate(rotationMatrix);
 			} else if(axis != -1) {
 				for(PolygonCollection<PuzzleSticker> polys : getHalfPolys(true))
 					polys.rotate(rotationMatrix);
 				for(PolygonCollection<PuzzleSticker> polys : getHalfPolys(false))
 					polys.rotate(rotationMatrix);
 			} else {
 				for(PolygonCollection<PuzzleSticker> piece : topLayerPolys)
 					if(piece != null)
 						piece.rotate(topRotationMatrix);
 				for(PolygonCollection<PuzzleSticker> piece : bottomLayerPolys)
 					if(piece != null)
 						piece.rotate(downRotationMatrix);
 			}
 			return --frames == 0;
 		}
 		public void updateInternalRepresentation(boolean polygons) {
 			if(slash) {
 				int start = leftSlash ? 6 : 0;
 				for(int i = start; i < start+6; i++) {
 					if(polygons) {
 						PolygonCollection<PuzzleSticker> temp = topLayerPolys[i];
 						topLayerPolys[i] = bottomLayerPolys[i];
 						bottomLayerPolys[i] = temp;
 					} else {
 						boolean temp = topLayer[i];
 						topLayer[i] = bottomLayer[i];
 						bottomLayer[i] = temp;
 					}
 				}
 				if(!polygons) {
 					if(leftSlash)
 						leftHalfEven = !leftHalfEven;
 					else
 						rightHalfEven = !rightHalfEven;
 				}
 			} else if(axis != -1) {
 				//cube rotation
 				if(!polygons && axis != 0) //we aren't affecting the polygons
 					leftRightSwitched = !leftRightSwitched;
 				if(axis == 2 || axis == 0) {
 					//this indicates a z2 or x2, so we swap the top and bottom
 					if(polygons) {
 						PolygonCollection<PuzzleSticker>[] temp = topLayerPolys;
 						topLayerPolys = bottomLayerPolys;
 						bottomLayerPolys = temp;
 					} else {
 						Boolean[] temp = topLayer;
 						topLayer = bottomLayer;
 						bottomLayer = temp;
 						leftHalfEven = !leftHalfEven;
 						rightHalfEven = !rightHalfEven;
 					}
 				}
 			} else {
 				if(polygons) {
 					PolygonCollection<PuzzleSticker>[] topLayerCopy = Utils.copyOf(topLayerPolys, topLayerPolys.length);
 					PolygonCollection<PuzzleSticker>[] bottomLayerCopy = Utils.copyOf(bottomLayerPolys, bottomLayerPolys.length);
 					for(int i=0; i<topLayerCopy.length; i++) {
 						topLayerPolys[i] = topLayerCopy[Utils.modulo(i + top, topLayerCopy.length)];
 						bottomLayerPolys[i] = bottomLayerCopy[Utils.modulo(i + down, bottomLayerCopy.length)];
 					}
 				} else {
 					Boolean[] topLayerCopy = Utils.copyOf(topLayer, topLayer.length);
 					Boolean[] bottomLayerCopy = Utils.copyOf(bottomLayer, bottomLayer.length);
 					for(int i=0; i<topLayerCopy.length; i++) {
 						topLayer[i] = topLayerCopy[Utils.modulo(i + top, topLayerCopy.length)];
 						bottomLayer[i] = bottomLayerCopy[Utils.modulo(i + down, bottomLayerCopy.length)];
 					}
 				}
 			}
 		}
 		public boolean isAnimationMergeble(PuzzleTurn o) {
 			SquareOneTurn other = (SquareOneTurn) o;
 			if(this.axis != -1 || other.axis != -1) //can't merge rotations
 				return false;
 			return this.slash == other.slash && this.leftSlash != other.leftSlash || //we can animate a left & right slash simultaneously
 				!this.slash && !other.slash && (this.top == 0 || other.top == 0); //we can animate turning the top/bottom simultaneously (regardless of layer)
 		}
 		public boolean isNullTurn() {
 			return slash == false && top == 0 && down == 0 && axis == -1;
 		}
 		public boolean isInspectionLegal() {
 			return axis != -1; //this indicates a cube rotation
 		}
 		public PuzzleTurn mergeTurn(PuzzleTurn o) {
 			if(o.isNullTurn()) return this;
 			SquareOneTurn other = (SquareOneTurn) o;
 			if(this.axis != -1 || other.axis != -1) { //can't merge rotations
 				if(this.axis == other.axis)
 					return new SquareOneTurn(0, 0, false);
 				return null;
 			}
 			if(this.isNullTurn()) return o;
 			if(this.slash && other.slash) {
 				if(this.leftSlash == other.leftSlash)
 					return new SquareOneTurn(0, 0, false); //this is no turn
 				return null; //or could merge this into an x2?
 			} else if(this.slash ^ other.slash)
 				return null; //can't merge a slash with a top/bottom turn
 			//if we made it to here, we're merging top/bottom turns in the same layer
 			return new SquareOneTurn(this.top + other.top, this.down + other.down, false);
 		}
 		public String toString() {
 			if(slash)
 				return (leftSlash ? "\\" : "/") + (cw ? "" : "'");
 			else if(axis != -1)
 				return axisname + "2" + (cw ? "" : "'");
 			String turns = "(" + top + ", " + down + ")";
 			return turns;
 		}
 	}
 
 	private Pattern turnPattern = Pattern.compile("(-?\\d*), *(-?\\d*)");
 	public boolean doTurn2(String turn) {
 		Matcher m;
 		SquareOneTurn s1turn = null;
 		if(turn.startsWith("/"))
 			s1turn = new SquareOneTurn(leftRightSwitched, leftRightSwitched ^ turn.substring(1).equals("'"));
 		else if(turn.startsWith("\\"))
 			s1turn = new SquareOneTurn(!leftRightSwitched, leftRightSwitched ^ turn.substring(1).equals("'"));
 		else if((m=turnPattern.matcher(turn)).find()) {
 			int top = Integer.parseInt(m.group(1));
 			int bottom = Integer.parseInt(m.group(2));
 			s1turn = new SquareOneTurn(top, bottom, turn.charAt(0) == '[');
 		} else {
 			//cube rotations
 			String face = turn.substring(0, 1);
 			String dir = turn.substring(1);
 			boolean ccw = dir.endsWith("'");
 			if(ccw)
 				dir = dir.substring(0, dir.length()-1);
 			//only allowing rotations by 2 for ease of implementation
 			//and because they're all that people would use 
 			if(!dir.equals("2"))
 				return false;
 			if(face.equals("x")) {
 				appendTurn(new SquareOneTurn(0, !ccw));
 			} else if(face.equals("y")) {
 				appendTurn(new SquareOneTurn(1, !ccw));
 			} else if(face.equals("z")) {
 				appendTurn(new SquareOneTurn(2, !ccw));
 			}
 			return true;
 		}
 		if(s1turn.slash && !isSlashLegal(0, 0))
 			return false;
 		appendTurn(s1turn);
 		return true;
 	}
 	
 	private boolean isSlashLegal(int top, int bottom) {
 		int len = topLayer.length;
 		return topLayer[Utils.modulo(top, len)] && 
 				topLayer[Utils.modulo(6+top, len)] &&
 				bottomLayer[Utils.modulo(bottom, len)] &&
 				bottomLayer[Utils.modulo(6+bottom, len)];
 	}
 
 	public String getState() {
 		return null;
 	}
 
 	//TODO - is solved doesn't work for 2 layer sq1 and square 2
 	public boolean isSolved() {
 		boolean twoLayer = getPuzzleVariation().equals(TWO_LAYER) || getPuzzleVariation().equals(TWO_FLOWER);
 		boolean unbandaged = getPuzzleVariation().equals(UNBANDAGED);
 		Color side = null;
 		for(PolygonCollection<PuzzleSticker> piece : topLayerPolys)
 			if(piece != null) {
 				if(side == null)
 					side = piece.get(0).getFillColor();
 				else if(!piece.get(0).getFillColor().equals(side))
 					return false;
 			}
 		side = null;
 		for(PolygonCollection<PuzzleSticker> piece : bottomLayerPolys)
 			if(piece != null) {
 				if(side == null)
 					side = piece.get(0).getFillColor();
 				else if(!piece.get(0).getFillColor().equals(side))
 					return false;
 			}
 		if(!twoLayer && leftHalfEven != rightHalfEven)
 			return false;
 		if(twoLayer) {
 			int edgeIndex = -1;
 			for(int i=0; i<topLayerPolys.length; i++) {
 				if(topLayerPolys[i] != null && topLayerPolys[i].size() == 2) { //searching for an edge
 					edgeIndex = i;
 					break;
 				}
 			}
 			if(edgeIndex == -1) return false; //no edge found on top layer
 			for(int i=edgeIndex; i<edgeIndex+12; i+=3) {
 				Color c = null;
 				for(int ch : new int[]{ 0, -1, 1 }) {
 					PolygonCollection<PuzzleSticker> topPiece, bottomPiece;
 					int index = i+ch;
 					while((topPiece = Utils.modoloAcces(topLayerPolys, index)) == null) index--;
 					index = i+ch;
 					while((bottomPiece = Utils.modoloAcces(bottomLayerPolys, -index+5)) == null) index++;
 
 					//check that the pieces are the same type (corner corner, or edge edge)
 					if(topPiece.size() != bottomPiece.size())
 						return false;
 					boolean corner = topPiece.size() == 3;
 					//check that the piece's colors match
 					int topIndex = 1, bottomIndex = 1;
 					if(corner) {
 						if(ch == -1) {
 							topIndex = 1; bottomIndex = 2;
 						} else if(ch == 1) {
 							topIndex = 2; bottomIndex = 1;
 						} else //if ch == 0, we should be at an edge
 							return false;
 					}
 					Color topColor = topPiece.get(topIndex).getFillColor();
 					Color bottomColor = bottomPiece.get(bottomIndex).getFillColor();
 					if(c == null) c = topColor;
 					if(!c.equals(topColor) || !c.equals(bottomColor))
 						return false;
 				}
 			}
 		} else { //3 layers
 			Color[] faces = new Color[4]; //F R B L
 			for(int i=0; i<topLayerPolys.length; i+=3) {
 				int index = rightHalfEven ? i : i-1;
 				PolygonCollection<PuzzleSticker> topEdge = Utils.modoloAcces(topLayerPolys, index);
 				PolygonCollection<PuzzleSticker> topCorner1 = Utils.modoloAcces(topLayerPolys, index+1);
 				PolygonCollection<PuzzleSticker> topCorner2 = Utils.modoloAcces(topLayerPolys, index+2);
 				index = rightHalfEven ? i : i+1;
 				PolygonCollection<PuzzleSticker> bottomCorner1 = Utils.modoloAcces(bottomLayerPolys, index);
 				PolygonCollection<PuzzleSticker> bottomCorner2 = Utils.modoloAcces(bottomLayerPolys, index+1);
 				PolygonCollection<PuzzleSticker> bottomEdge = Utils.modoloAcces(bottomLayerPolys, index+2);
 				
 				if(topEdge == null || topCorner1 == null || bottomEdge == null || bottomCorner1 == null || 
 						(!unbandaged && (topCorner2 != null || bottomCorner2 != null)))
 					return false;
 				Color topCW, topCCW, bottomCW, bottomCCW;
 				if(unbandaged) {
 					topCCW = topCorner1.get(1).getFillColor();
 					topCW = topCorner2.get(1).getFillColor();
 					bottomCCW = bottomCorner1.get(1).getFillColor();
 					bottomCW = bottomCorner2.get(1).getFillColor();
 				} else {
 					topCCW = topCorner1.get(2).getFillColor();
 					topCW = topCorner1.get(1).getFillColor();
 					bottomCCW = bottomCorner1.get(2).getFillColor();
 					bottomCW = bottomCorner1.get(1).getFillColor();
 				}
 				int group = i/3;
 				if(!sameColor(topEdge.get(1).getFillColor(), faces, group) ||
 						!sameColor(topCCW, faces, group) ||
 						!sameColor(topCW, faces, group+1) ||
 						!sameColor(bottomEdge.get(1).getFillColor(), faces, faces.length-group+1) ||
 						!sameColor(bottomCCW, faces, faces.length-group+2) ||
 						!sameColor(bottomCW, faces, faces.length-group+1))
 					return false;
 			}
 			int front, right, back, left;
 			if(leftHalfEven) {
 				front = 0; right = 1; back = 2; left = 3;
 			} else {
 				front = 2; right = 1; back = 0; left = 3;
 			}
 			if(!equals(faces[front], leftHalfPolys.get(0).getFillColor()) || !equals(faces[front], rightHalfPolys.get(1).getFillColor()) ||
 					!equals(faces[back], leftHalfPolys.get(1).getFillColor()) || !equals(faces[back], rightHalfPolys.get(0).getFillColor()) ||
 					!equals(faces[left], leftHalfPolys.get(2).getFillColor()) || 
 					!equals(faces[right], rightHalfPolys.get(2).getFillColor()))
 				return false;
 		}
 		return true;
 	}
 	private boolean equals(Object a, Object b) {
 		if(a == null)
 			return b == null;
 		return a.equals(b);
 	}
 	private boolean sameColor(Color c, Color[] arr, int i) {
 		i = Utils.modulo(i, arr.length);
 		if(arr[i] == null) {
 			arr[i] = c;
 			return true;
 		}
 		return arr[i].equals(c);
 	}
 
 	private static final int SCRAMBLE_LENGTH = 40;
 	public void scramble2() {
 		boolean top = true, bottom = true, slash = true;
 		for(int i=0; i<SCRAMBLE_LENGTH; i++) {
 			ArrayList<SquareOneTurn> legalTurns = new ArrayList<SquareOneTurn>();
 			for(int c=0; c<12; c++) {
 				if(top && isSlashLegal(c, 0))
 					legalTurns.add(new SquareOneTurn(c, 0, false));
 				if(bottom && isSlashLegal(0, c))
 					legalTurns.add(new SquareOneTurn(0, c, false));
 				if(slash && isSlashLegal(0, 0))
 					legalTurns.add(new SquareOneTurn(false, true));
 			}
 			SquareOneTurn turn = Utils.choose(legalTurns);
 			if(turn.top != 0) {
 				top = false;
 				slash = true;
 			}
 			if(turn.down != 0) {
 				bottom = false;
 				slash = true;
 			}
 			if(turn.slash) {
 				top = bottom = true;
 				slash = false;
 			}
 			appendTurn(turn);
 		}
 	}
 	public HashMap<String, Color> getDefaultColorScheme() {
 		HashMap<String, Color> colors = new HashMap<String, Color>();
 		colors.put("F", Color.RED);
 		colors.put("U", Color.YELLOW);
 		colors.put("R", Color.GREEN);
 		colors.put("B", Color.ORANGE);
 		colors.put("L", Color.BLUE);
 		colors.put("D", Color.WHITE);
 		return colors;
 	}
 }
