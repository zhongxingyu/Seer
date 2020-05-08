 package net.todd.games.boardgame;
 
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.Bounds;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.vecmath.Point3d;
 
 import com.sun.j3d.utils.universe.SimpleUniverse;
 
 public class GameEngine {
 	private final ISceneGenerator sceneGenerator;
 	private final IPieceGenerator pieceGenerator;
 	private final ICameraGenerator cameraGenerator;
 	private final Bounds bounds;
 	private final BranchGroup bg;
 
 	public GameEngine(BranchGroup bg, ISceneGenerator sceneGenerator,
 			IPieceGenerator pieceGenerator, ICameraGenerator cameraGenerator) {
 		this.bg = bg;
 		this.sceneGenerator = sceneGenerator;
 		this.pieceGenerator = pieceGenerator;
 		this.cameraGenerator = cameraGenerator;
 
 		bounds = new BoundingSphere(new Point3d(0, 0, 0), 100);
 	}
 
 	public void createScene(IPicker picker) {
 		sceneGenerator.lightScene(bg, bounds);
 		sceneGenerator.createGameGrid(bg, picker);
 		sceneGenerator.createBackground(bg, bounds);
 		pieceGenerator.createPieces(bg, picker, bounds);
 	}
 
 	public void createCamera(SimpleUniverse su, Canvas3D canvas3D) {
 		cameraGenerator.createCamera(su, canvas3D, bounds);
 	}
 }
