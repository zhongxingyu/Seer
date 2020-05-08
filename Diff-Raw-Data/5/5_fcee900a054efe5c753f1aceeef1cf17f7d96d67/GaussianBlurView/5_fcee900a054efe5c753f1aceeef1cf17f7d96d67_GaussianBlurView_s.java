 package com.hekai.camera;
 
 import android.content.Context;
 import android.graphics.Canvas;
 
 public class GaussianBlurView extends OverlayView{
 
 	private static final String TAG="GaussianBlurView";
 	
 	private float sigma = 1.5f;
 	private int radius = 1;
 	
 	private float[][] weights;
 	
 	private int[] cacheColors,drawColors;
 	private byte[] cacheR,cacheG,cacheB;
 	
 	public GaussianBlurView(Context context) {
 		super(context);
 	}
 
 	@Override
 	public void init() {
 		cacheColors = new int[mWidth * mHeight];
 		drawColors = new int[mWidth * mHeight];
 		cacheR=new byte[mWidth * mHeight];
 		cacheG=new byte[mWidth * mHeight];
 		cacheB=new byte[mWidth * mHeight];
 		
 		weights=new float[][]{
 				{ 0.0947416f, 0.118318f, 0.0947416f },
 				{ 0.118318f, 0.147761f, 0.118318f },
 				{ 0.0947416f, 0.118318f, 0.0947416f }
 				};
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		int angle = 90 * mRotation;
 		canvas.rotate(angle,mCenterX,mCenterY);
 		
 		canvas.drawBitmap(drawColors, 0, mWidth, mCenterX - mWidth / 2,
 				mCenterY - mHeight / 2, mWidth, mHeight, true, null);
 		
 	}
 	
 	@Override
 	public void updateData(byte[] data, int width, int height, int format) {
 		int skipX=width/mWidth;
 		int skipY=height/mHeight;
 		int index=0;
 		
 		final int frameSize = width * height;
 		int r,g,b;
 		
 		for (int j = 0; j < height; j+=skipY) {
 			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
 			for (int i = 0; i < width; i+=skipX) {
 				int y=(0xff & (int)(data[j*width+i]));
 				if ((i & 1) == 0) {
 		            v = 0xff & (int)data[uvp++];
 		            u = 0xff & (int)data[uvp++];
 		        }
 				
 				r = y + (int) 1.4075f * (v-128);
 				g = y - (int) (0.3455f * (u-128) + 0.7169f * (v-128));
 				b = y + (int) 1.779f * (u-128);
 				
 			    r = r>255? 255 : r<0 ? 0 : r;
 			    g = g>255? 255 : g<0 ? 0 : g;
 			    b = b>255? 255 : b<0 ? 0 : b;
 	            
 //	            int color=0xff000000 | r<<16 | g<<8 | b;
 //	            cacheColors[index++]=color;
 			    cacheR[index]=(byte)r;
 			    cacheG[index]=(byte)g;
 			    cacheB[index]=(byte)b;
 			    index++;
 			}
 		}
 		
 		for(int j=0;j<mHeight;j++){
 			for(int i=0;i<mWidth;i++){
 //				int k=j*mHeight+i;
 				r=gaussion(i,j,0);
 				g=gaussion(i,j,1);
 				b=gaussion(i,j,2);
 				
				cacheColors[j*mHeight+i]=0xff000000 | r<<16 | g<<8 | b;
 			}
 		}
 		
 		System.arraycopy(cacheColors, 0, drawColors, 0, cacheColors.length);
 	}
 	
 	private int gaussion(int x,int y,int type){
 		byte[] cacheRGB=null;
 		switch(type){
 		case 0:
 			cacheRGB=cacheR;
 			break;
 		case 1:
 			cacheRGB=cacheG;
 			break;
 		default:
 			cacheRGB=cacheB;	
 		}
 		
 		float result=0;
 		
 		for(int j=-radius;j<=radius;j++){
 			for(int i=-radius;i<=radius;i++){
 				int px=x+i;
 				int py=y+j;
 				if(px<0 || px>=mWidth || py<0 || py>=mHeight)
 					continue;
 				
 				float w=weights[j+radius][i+radius];
				int rgb=0xff & cacheRGB[py*mHeight+px];
 				result+=w*rgb;
 			}
 		}
 		
 		int rgbResult=(int)result;
 		
 		rgbResult = rgbResult>255? 255 : rgbResult<0 ? 0 : rgbResult;
 		
 		return rgbResult;
 	}
 
 
 }
