 package com.luzi82.chitanda.game.ui;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes;
 import com.luzi82.chitanda.ChitandaGame;
 import com.luzi82.chitanda.game.logic.Board;
 import com.luzi82.gdx.GrScreen;
 
 public class GameScreen extends GrScreen<ChitandaGame> {
 
 	public static final float PHI = (float) (1 + Math.sqrt(5)) / 2;
 
 	private static final float LINE_ALPHA = 1f / 16;
 	private static final float LINE_WIDTH = 1f / 16;
 
 	public Board mBoard;
 
 	private OrthographicCamera mCamera;
 
 	// private Pixmap mBasePixmap0;
 	// private Pixmap mBasePixmap1;
 	private Texture mBaseTexture0;
 	private Texture mBaseTexture1;
 	private Mesh mBaseMesh0;
 	private Mesh mBaseMesh1;
 
 	private Mesh mLineMeshH;
 	private float[] mLineMeshHF;
 	private final int[] LINE_MESH_HA = {//
 	1 * 7 - 1,//
 			2 * 7 - 1,//
 			7 * 7 - 1,//
 			8 * 7 - 1,//
 	};
 
 	public CameraCalc mCameraCalc;
 	public CameraControl mCameraControl;
 
 	// screen density
 	public float mBlockPerPixelBorder;
 
 	public BlockDraw mBlockDraw;
 
 	public GameScreen(ChitandaGame aParent) {
 		super(aParent);
 	}
 
 	@Override
 	protected void onScreenShow() {
 	}
 
 	@Override
 	protected void onScreenLoad() {
 		mBoard = new Board();
 		mBoard.setAll(true);
 
 		// create a hole in center
 		int cx = Board.WIDTH / 2;
 		int cy = Board.HEIGHT / 2;
 		int r = 800;
 		int rr = r * r;
 		for (int i = cx - r; i < cx + r; ++i) {
 			int dx = i - cx;
 			dx *= dx;
 			for (int j = cy - r; j < cy + r; ++j) {
 				int dy = j - cy;
 				dy *= dy;
 				int dd = dx + dy;
 				mBoard.set(i, j, dd > rr);
 			}
 		}
 
 		mCamera = new OrthographicCamera();
 
 		Pixmap tmpPixmap;
 		tmpPixmap = new Pixmap(Gdx.files.internal("data/chitanda0.png"));
 		mBaseTexture0 = new Texture(tmpPixmap, true);
 		tmpPixmap.dispose();
 		tmpPixmap = null;
 		tmpPixmap = new Pixmap(Gdx.files.internal("data/chitanda1.png"));
 		mBaseTexture1 = new Texture(tmpPixmap, true);
 		tmpPixmap.dispose();
 		tmpPixmap = null;
 
 		VertexAttributes va;
 		va = new VertexAttributes( //
 				new VertexAttribute(VertexAttributes.Usage.Position, 3, "position"),//
 				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texturecoordinates")//
 		);
 
 		mBaseMesh0 = new Mesh(true, 4, 4, va);
 		mBaseMesh0.setVertices(new float[] { //
 				0f, 1024f * 4, 0f, 0f, 0f,//
 						1024f * 4, 1024f * 4, 0f, 1f, 0f,//
 						0f, 0f, 0f, 0f, 1f,//
 						1024f * 4, 0f, 0f, 1f, 1f,//
 				});
 		mBaseMesh0.setIndices(new short[] { 0, 1, 2, 3 });
 		mBaseMesh1 = new Mesh(true, 4, 4, va);
 		mBaseMesh1.setVertices(new float[] { //
 				1024f * 4, 1024f * 4, 0f, 0f, 0f,//
 						1024f * 8, 1024f * 4, 0f, 1f, 0f,//
 						1024f * 4, 0f, 0f, 0f, 1f,//
 						1024f * 8, 0f, 0f, 1f, 1f,//
 				});
 		mBaseMesh1.setIndices(new short[] { 0, 1, 2, 3 });
 
 		va = new VertexAttributes( //
 				new VertexAttribute(VertexAttributes.Usage.Position, 3, "position"),//
 				new VertexAttribute(VertexAttributes.Usage.Color, 4, "color")//
 		);
 
 		mLineMeshH = new Mesh(false, 8, 12, va);
 		mLineMeshHF = new float[] { //
 		1f, 0f, 0f, 0f, 0f, 0f, LINE_ALPHA,//
 				0f, 0f, 0f, 0f, 0f, 0f, LINE_ALPHA,//
 				1f, LINE_WIDTH, 0f, 0f, 0f, 0f, 0f,//
 				0f, LINE_WIDTH, 0f, 0f, 0f, 0f, 0f,//
 
 				1f, 1 - LINE_WIDTH, 0f, 0f, 0f, 0f, 0f,//
 				0f, 1 - LINE_WIDTH, 0f, 0f, 0f, 0f, 0f,//
 				1f, 1f, 0f, 0f, 0f, 0f, LINE_ALPHA,//
 				0f, 1f, 0f, 0f, 0f, 0f, LINE_ALPHA,//
 		};
 		mLineMeshH.setIndices(new short[] { //
 				0, 1, 2, //
 						1, 2, 3, //
 						4, 5, 6, //
 						5, 6, 7 //
 				});
 
 		mCameraCalc = new CameraCalc();
 		mCameraControl = new CameraControl(mCameraCalc);
 		mBlockDraw = new BlockDraw(this);
 	}
 
 	@Override
 	public void onScreenRender(float aDelta) {
 		GL10 gl = Gdx.graphics.getGL10();
 		gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
 		gl.glDisable(GL10.GL_DEPTH_TEST);
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		updateCamera(aDelta, gl);
 		drawImage(gl);
 		drawLine(gl);
 		mBlockDraw.drawBlock(gl);
 	}
 
 	private void updateCamera(float aDelta, GL10 aGl) {
 		mCameraControl.update(aDelta);
 
 		mCamera.zoom = mCameraCalc.iCameraRealZoom;
 		mCamera.position.x = mCameraCalc.iCameraRealBX;
 		mCamera.position.y = mCameraCalc.iCameraRealBY;
 		mCamera.update();
 		mCamera.apply(aGl);
 	}
 
 	private void drawImage(GL10 aGl) {
 		aGl.glDisable(GL10.GL_BLEND);
 		aGl.glBlendFunc(GL10.GL_ONE, GL10.GL_ZERO);
 		aGl.glEnable(GL10.GL_TEXTURE_2D);
 		aGl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 		mBaseTexture0.bind();
 		mBaseMesh0.render(GL10.GL_TRIANGLE_STRIP);
 		mBaseTexture1.bind();
 		mBaseMesh1.render(GL10.GL_TRIANGLE_STRIP);
 	}
 
 	private void drawLine(GL10 aGl) {
 		int i;
 
 		aGl.glEnable(GL10.GL_BLEND);
 		aGl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		aGl.glDisable(GL10.GL_TEXTURE_2D);
 
 		if (mCameraCalc.iCameraRealZoom < mCameraCalc.mZoomMin * PHI * PHI) {
 			float a = mCameraCalc.iCameraRealZoom / (mCameraCalc.mZoomMin * PHI);
 			a = (float) Math.log(a);
 			a /= (float) Math.log(PHI);
 			a = 1 - a;
 			if (a > 0) {
 				if (a > 1)
 					a = 1f;
 				a *= LINE_ALPHA;
 				for (int ai : LINE_MESH_HA) {
 					mLineMeshHF[ai] = a;
 				}
 				mLineMeshH.setVertices(mLineMeshHF);
 
 				int min, max;
 
 				min = (int) Math.floor(mCameraCalc.viewBY0Min());
 				max = (int) Math.ceil(mCameraCalc.viewBY0Max());
 				if (min < 0)
 					min = 0;
 				if (max > Board.HEIGHT)
 					max = Board.HEIGHT;
 				for (i = min; i < max; ++i) {
 					aGl.glPushMatrix();
 					aGl.glTranslatef(0, i, 0);
 					aGl.glScalef(Board.WIDTH, 1, 1);
 					mLineMeshH.render(GL10.GL_TRIANGLES);
 					aGl.glPopMatrix();
 				}
 
 				min = (int) Math.floor(mCameraCalc.viewBX0Min());
 				max = (int) Math.ceil(mCameraCalc.viewBX0Max());
 				if (min < 0)
 					min = 0;
 				if (max > Board.WIDTH)
 					max = Board.WIDTH;
 				aGl.glPushMatrix();
 				aGl.glRotatef(90, 0, 0, 1);
 				aGl.glScalef(1, -1, 1);
 				for (i = min; i < max; ++i) {
 					aGl.glPushMatrix();
 					aGl.glTranslatef(0, i, 0);
 					aGl.glScalef(Board.HEIGHT, 1, 1);
 					mLineMeshH.render(GL10.GL_TRIANGLES);
 					aGl.glPopMatrix();
 				}
 				aGl.glPopMatrix();
 			}
 		}
 	}
 
 	@Override
 	public void onScreenResize() {
 		mCameraCalc.onScreenResize(mScreenWidth, mScreenHeight);
 		mCamera.viewportWidth = mCameraCalc.mViewPortW;
 		mCamera.viewportHeight = mCameraCalc.mViewPortH;
 		mBlockPerPixelBorder = 10f / 6 / Gdx.graphics.getPpcX();
 		mBlockDraw.updateCellTextureV();
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button, long aTime) {
 		// iLogger.debug("touchDown");
 		int minSide = Math.min(mScreenWidth, mScreenHeight);
 		float blockPerPixel = mCameraCalc.iCameraRealZoom / minSide;
 		if (blockPerPixel <= mBlockPerPixelBorder) {
 			if (!mCameraControl.mMoving) {
 				mCameraCalc.mLockTime = System.currentTimeMillis() + 1000;
 			}
 		} else {
 			mCameraControl.mMoving = true;
 		}
 
 		mCameraControl.touchDown(x, y, pointer, button, aTime);
 
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button, long aTime) {
 		// iLogger.debug("touchUp");
 		mCameraControl.touchUp(x, y, pointer, button, aTime);
 
 		if ((mCameraCalc.mLockTime >= 0) && (!mCameraControl.mMoving)) {
 			int minSide = Math.min(mScreenWidth, mScreenHeight);
 			float blockPerPixel = mCameraCalc.iCameraRealZoom / minSide;
 			if (blockPerPixel <= mBlockPerPixelBorder) {
 				int bx = (int) mCameraCalc.screenToBoardRealX(x);
 				int by = (int) mCameraCalc.screenToBoardRealY(y);
 				boolean good = true;
 				good = good && (bx >= 0);
 				good = good && (bx < Board.WIDTH);
 				good = good && (by >= 0);
 				good = good && (by < Board.HEIGHT);
 				if (good) {
 					if (mBoard.get0(bx, by)) {
 						mBoard.set(bx, by, false);
 						mBlockDraw.dirty(bx, by);
 					}
 				}
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean touchDragged(int x, int y, int pointer, long aTime) {
 		// iLogger.debug("touchDragged");
 		mCameraControl.touchDragged(x, y, pointer, aTime);
 		return true;
 	}
 
 	@Override
 	public boolean touchMoved(int x, int y, long aTime) {
 		// iLogger.debug("touchMoved");
 		mCameraControl.touchMoved(x, y, aTime);
 		return true;
 	}
 
 	@Override
 	public boolean scrolled(int amount, long aTime) {
 		mCameraControl.scrolled(amount, aTime);
 		return true;
 	}
 
 }
