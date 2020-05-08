 package yang.util;
 
 import yang.events.eventtypes.YangEvent;
 import yang.events.eventtypes.YangPointerEvent;
 import yang.graphics.defaults.DefaultGraphics;
 import yang.graphics.listeners.DrawListener;
 import yang.graphics.textures.TextureProperties;
 import yang.graphics.textures.TextureRenderTarget;
 import yang.graphics.textures.enums.TextureFilter;
 import yang.graphics.textures.enums.TextureWrap;
 import yang.graphics.translator.GraphicsTranslator;
 import yang.surface.YangSurface;
 import yang.util.window.YangTextureDrawer;
 
 public class SurfaceWrapper<SurfaceType extends YangSurface> extends YangTextureDrawer {
 
 	public GraphicsTranslator mTranslator;
 	public SurfaceType mInnerSurface;
 	public TextureRenderTarget mTexTarget;
 	public int mIdShift = 0;
 
 	public SurfaceWrapper(DefaultGraphics<?> graphics,SurfaceType surface) {
 		super(graphics);
 		mInnerSurface = surface;
 	}
 
 	public void init(int mSurfWidth,int mSurfHeight) {
 		mTranslator = mGraphics.mTranslator;
 		mInnerSurface.setGraphics(mTranslator);
 		mInnerSurface.onSurfaceCreated(false);
 		mTexTarget = mTranslator.createRenderTarget(mSurfWidth,mSurfHeight,new TextureProperties(TextureWrap.CLAMP,TextureFilter.LINEAR));
 		mFlipY = true;
 		super.setDimensions(2*(float)mSurfWidth/mSurfHeight,2);
 		mTexture = mTexTarget.mTargetTexture;
 	}
 
 	public void updateTexture() {
 		mTranslator.setTextureRenderTarget(mTexTarget);
 		mGraphics.setGlobalTransformEnabled(false);
 		final DrawListener prevListener = mTranslator.mCurDrawListener;
 		mInnerSurface.drawContent(true);
 		prevListener.activate();
 		mTranslator.leaveTextureRenderTarget();
 	}
 
 	public void handleEvent(YangEvent event) {
 		event.handle(mInnerSurface);
 	}
 
 	@Override
 	public boolean rawEvent(YangEvent event) {
 		if(event instanceof YangPointerEvent) {
 			((YangPointerEvent)event).mId += mIdShift;
 			event.handle(mInnerSurface);
 			((YangPointerEvent)event).mId -= mIdShift;
 		}else
 			event.handle(mInnerSurface);
 
 		return true;
 	}
 
 }
