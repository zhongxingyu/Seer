 package com.blarg.gdx.graphics;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g3d.ModelBatch;
 import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
 import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.utils.Disposable;
 
 public class RenderContext implements Disposable {
 	public final SpriteBatch spriteBatch;
 	public final DelayedSpriteBatch delayedSpriteBatch;
 	public final DecalBatch decalBatch;
 	public final BillboardSpriteBatch billboardSpriteBatch;
 	public final ShapeRenderer debugGeometryRenderer;
 	public final ModelBatch modelBatch;
 	public final ScreenPixelScaler pixelScaler;
 	public final SolidColorTextureCache solidColorTextures;
 
 	AlphaTestCameraGroupStrategy cameraGroupStrategy;
 	Camera perspectiveCamera;
 	OrthographicCamera orthographicCamera;
 
 	public RenderContext(boolean use2dPixelScaling) {
 		Gdx.app.debug("RenderContext", "ctor");
 		spriteBatch = new SpriteBatch();
 		delayedSpriteBatch = new DelayedSpriteBatch();
 		debugGeometryRenderer = new ShapeRenderer();
 		modelBatch = new ModelBatch();
 		solidColorTextures = new SolidColorTextureCache();
 
 		if (use2dPixelScaling)
 			pixelScaler = new DefaultScreenPixelScaler();
 		else
 			pixelScaler = new NoScaleScreenPixelScaler();
 		pixelScaler.calculateScale(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		orthographicCamera = new OrthographicCamera(pixelScaler.getScaledWidth(), pixelScaler.getScaledHeight());
 
 		setDefaultPerspectiveCamera();
 
 		cameraGroupStrategy = new AlphaTestCameraGroupStrategy(perspectiveCamera);
 		decalBatch = new DecalBatch(cameraGroupStrategy);
 		billboardSpriteBatch = new BillboardSpriteBatch();
 	}
 
 	public Camera getPerspectiveCamera() {
 		return perspectiveCamera;
 	}
 
 	public OrthographicCamera getOrthographicCamera() {
 		return orthographicCamera;
 	}
 
 	public void setPerspectiveCamera(Camera camera) {
 		perspectiveCamera = camera;
 		if (cameraGroupStrategy != null)
 			cameraGroupStrategy.setCamera(camera);
 	}
 
 	public void setDefaultPerspectiveCamera() {
 		PerspectiveCamera camera = new PerspectiveCamera(60.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		camera.position.set(0.0f, 0.0f, 0.0f);
 		camera.lookAt(0.0f, 0.0f, 1.0f);
 		camera.near = 0.1f;
 		camera.far = 100.0f;
 		camera.update();
 		setPerspectiveCamera(camera);
 	}
 
 	public void clear() {
 		clear(0.0f, 0.0f, 0.0f, 1.0f);
 	}
 
 	public void clear(float red, float green, float blue, float alpha) {
 		Gdx.graphics.getGL20().glClearColor(red, green, blue, alpha);
 		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
 	}
 
 	public void onPreRender() {
 		spriteBatch.setProjectionMatrix(orthographicCamera.combined);
		debugGeometryRenderer.setProjectionMatrix(perspectiveCamera.combined);
 		debugGeometryRenderer.begin(ShapeRenderer.ShapeType.Line);
 		delayedSpriteBatch.begin(spriteBatch, perspectiveCamera, pixelScaler.getScale());
 		billboardSpriteBatch.begin(decalBatch, perspectiveCamera);
 		modelBatch.begin(perspectiveCamera);
 	}
 
 	public void onPostRender() {
 		modelBatch.end();
 		billboardSpriteBatch.end();
 		delayedSpriteBatch.end();
 		debugGeometryRenderer.end();
 	}
 
 	public void onUpdate(float delta) {
 		perspectiveCamera.update();
 		orthographicCamera.update();
 	}
 
 	public void onResize(int width, int height) {
 		Gdx.app.debug("RenderContext", String.format("onResize(%d, %d)", width, height));
 		pixelScaler.calculateScale(width, height);
 		orthographicCamera.setToOrtho(false, pixelScaler.getScaledWidth(), pixelScaler.getScaledHeight());
		perspectiveCamera.viewportWidth = width;
		perspectiveCamera.viewportHeight = height;
 	}
 
 	public void onPause() {
 		Gdx.app.debug("RenderContext", String.format("onPause"));
 		solidColorTextures.onPause();
 	}
 
 	public void onResume() {
 		Gdx.app.debug("RenderContext", String.format("onResume"));
 		solidColorTextures.onResume();
 	}
 
 	@Override
 	public void dispose() {
 		Gdx.app.debug("RenderContext", String.format("dispose"));
 		solidColorTextures.dispose();
 		spriteBatch.dispose();
 		decalBatch.dispose();
 		cameraGroupStrategy.dispose();
 	}
 }
