 package vnd.blueararat.Effoto;
 
 import java.io.FilenameFilter;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BlurMaskFilter;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorMatrix;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.EmbossMaskFilter;
 import android.graphics.MaskFilter;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.RectF;
 import android.graphics.Shader;
 import android.graphics.SweepGradient;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.util.FloatMath;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 
 public class BorderEf extends Effect implements
 		WaveSettingsLayout.OnSettingsChangedListener {
 
 	public static void scaleStrokeWidth(float scale) {
 		sStrokeWidth *= scale;
 	}
 
 	public void setBackgroundColor(int backgroundColor) {
 		mBackgroundColor = backgroundColor;
 	}
 
 	public int getBackgroundColor() {
 		return mBackgroundColor;
 	}
 
 	// private long i1;
 	// public int index;
 
 	private Paint mPaint, mPaint5, mBitmapPaint;// , mTextPaint;
 	private MaskFilter mEmboss;
 	private MaskFilter mBlur;
 	private int mColor;
 	private static int sColor = 0xFF7777FF;
 	private static float sStrokeWidth = 4;
 	private int mBckgrWidth = 100, mBckgrHeight = 100;
 	private float mStrokeWidth;
 	private boolean isRainbow = false;
 	// private static FrameLayout mRainbowFrame;
 	private boolean adjustRainbow = false;
 	private boolean isBlur = false;
 	private boolean isNone = false;
 	private float mCenterRainbowX;
 	private float mCenterRainbowY;
 	public static final int HATCH_COLOR_BG = 0xFF222222;
 	public static final int HATCH_COLOR_LINES = 0xFF888888;
 	static final int SELECT_FOLDER = 1;
 	private int mMode1;
 	private int mMode2;
 	private final static int PADX = 0;
 	private final static int PADX2 = 2 * PADX;
 	// private static boolean isJPG;
 	private int mBackgroundColor = -1;
 	// private static SharedPreferences preferences;
 	int[] mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF,
 			0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
 	// private static TextView sInfo;
 	private FilenameFilter mFilenameFilter;
 	// private static volatile boolean mustStop = false;
 	private boolean mustFit = true;
 	private boolean mustClip = false;
 	private float mDx = 20, mDy = 20;
 	private float mStartX = 10, mStartY = 10, mSmooth = 5;
 	private boolean isCircle = false;
 	// private int mNumWaves = 10;
 	// private static boolean shouldDrawText = false;
 	private float[] dt = new float[] { 0, 0 };
 	// private boolean adjustTextPosition = false;
 	private float pathLength = 0;
 	private float currentLength = 0;
 	// private static String sText = "";
 	// private static boolean fillWithText = false;
 	// private static Typeface font;
 	private WaveSettingsLayout sd;
 	private float[] mFitted = new float[6];
 	private Drawable d1, d2;
 
 	public BorderEf(Context ctx) {
 		this(ctx, Color.WHITE, 0, 0, sStrokeWidth);
 	}
 
 	public BorderEf(Context ctx, int color, int mode1, int mode2,
 			float strokewidth) {
 		super(ctx);
 		mStrokeWidth = strokewidth;
 
 		// if (sBackgroundColor == -1) {
 		mBackgroundColor = ma.prefs.getInt(Prefs.KEY_COLOR, 0xFFFFFFFF);
 		// mTextPaint.setColor(mBackgroundColor);
 		// if (font != null)
 		// mTextPaint.setTypeface(font);
 		// mv.updateTextBounds();
 		// }
 
 		mDx = 10 + MTRNGJNILib.randInt(20);
 		mDy = 10 + MTRNGJNILib.randInt(20);
 		float m = mDx * 2;
 		if (m > sBorderWidth)
 			sBorderWidth = m;
 		mStartX = 10;
 		mStartY = 10;
 		mSmooth = 5 - MTRNGJNILib.randInt(10);
 		// mNumWaves = 5 + MTRNGJNILib.randInt(10);
 
 		float[] f = new float[3];
 		Color.colorToHSV(sColor, f);
 
 		f[0] = 360.f * MTRNGJNILib.randExc();// (float) Math.random();
 		// Toast.makeText(mContext, "" + f[0], 0).show();
 		mColor = Color.HSVToColor(Color.alpha(sColor), f);
 		sColor = mColor;
 
 		mPaint = new Paint() {
 			{
 				setAntiAlias(true);
 				setDither(true);
 				setColor(mColor);
 				setStyle(Paint.Style.STROKE);
 				setStrokeJoin(Paint.Join.ROUND);
 				setStrokeCap(Paint.Cap.ROUND);
 				setStrokeWidth(mStrokeWidth);
 			}
 		};
 
 		mPaint5 = new Paint() {
 			{
 				setStrokeWidth(1);
 				setColor(HATCH_COLOR_LINES);
 				setStyle(Paint.Style.STROKE);
 				setAntiAlias(true);
 			}
 		};
 
 		// mTextPaint = new Paint() {
 		// {
 		// setAntiAlias(true);
 		// setTextSize(mStrokeWidth);
 		// }
 		// };
 
 		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
 
 		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
 
 		mBlur = new BlurMaskFilter(mStrokeWidth != 0 ? mStrokeWidth : 1,
 				BlurMaskFilter.Blur.NORMAL);
 
 		// if (sText.length() == 0)
 		// sText = ma.getString(R.string.edit_text);
 		// if (mRainbowFrame == null) {
 		// mRainbowFrame = (FrameLayout)ma.findViewById(R.id.frame_rainbow);
 		// mRainbowFrame.setBackgroundDrawable(getDrawable());
 		// }
 		// ib.setPadding(2, 2, 2, 2);
 		// ib.setImageResource(R.drawable.bt_border);
 		d1 = mContext.getResources().getDrawable(R.drawable.bt_border_normal)
 				.mutate();
 		d2 = mContext.getResources().getDrawable(R.drawable.bt_border_pressed)
 				.mutate();
 		setHue(d1);
 		setHue(d2);
 		// ib.setImageDrawable(d1);
 
 		this.mMode1 = mode1;
 		this.mMode2 = mode2;
 		this.mStrokeWidth = strokewidth;
 		LayoutInflater inflater = LayoutInflater.from(ctx);
 		View v = inflater.inflate(R.layout.sliding_drawer_2,
 				ma.getParentLayout(), true);
 		wsd = (WrappingSlidingDrawer) v.findViewById(R.id.slidingDrawer);
 		sd = (WaveSettingsLayout) wsd.findViewById(R.id.content2);
 		sd.setListener(this);
 
 		Button bt = (Button) wsd.getHandle();
 		bt.setText("" + index);
 
 		MainActivity.setViewGroupFont(wsd, Typeface.MONOSPACE);
 		sd.update(mStrokeWidth);
 	}
 
 	private class Draw extends AsyncTask<Void, Void, Void> {
 		private int total = 0;
 		// private Bitmap.Config mG;
 		private Bitmap.CompressFormat mCf;
 		private int mQ;
 		private float strwidth;
 		private Paint paint = new Paint(mPaint);
 		private volatile float scaleX, scaleY;
 		private final float lStrokeWidth = mStrokeWidth;
 		private final float lDx = mDx;
 		private final float lDy = mDy;
 		private final float lCenterRainbowX = mCenterRainbowX;
 		private final float lCenterRainbowY = mCenterRainbowY;
 		// private final String lOutputPath = sOutputPath;
 		private final float lStartX = mStartX;
 		private final float lStartY = mStartY;
 		private final float lBitmapWidth = mBitmapWidth;
 		private final float lBitmapHeight = mBitmapHeight;
 		private final float lSmooth = mSmooth;
 		private final boolean lBlur = isBlur;
 		private final boolean lEmboss = false;// (sMode1 ==
 												// SettingsDialog.EMBOSS);
 		private final boolean lNone = isNone;
 		private final boolean lRainbow = isRainbow;
 		private final boolean lFit = mustFit;
 		// private final boolean isPNG = MainActivity.isPNG;
 		private final int lBackgroundColor = mBackgroundColor;
 		private final boolean lCircle = isCircle;
 		private final int lNumWaves = 2 * (int) ((lBitmapWidth + lBitmapHeight) / lDy);;
 		double dangle = 2 * Math.PI / lNumWaves;
 
 		// private final Paint lTextPaint = new Paint(mTextPaint);
 		// private final String lText = sText;
 		// private final boolean lShouldDrawText = shouldDrawText;
 		private final float lPathLength = pathLength;
 		private float pLength, cLength;
 		private final float lCurrentLength = currentLength;
 		// private final boolean lFillWithText = fillWithText;
 		private float ladj1;
 		private int ltw;
 
 		// private void lUpdateTextBounds() {
 		// Rect bounds = new Rect();
 		// lTextPaint.getTextBounds(sText + "...", 0, sText.length() + 3,
 		// bounds);
 		// ltw = bounds.right;
 		// ladj1 = -(bounds.bottom + bounds.top) / 2;
 		// }
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			isLocked = true;
 			iAmLocked = true;
 			bmp3 = drawIntoBitmap();
 			return null;
 			// return export();
 		}
 
 		private synchronized Path drawFrameExport(Bitmap bitmap) {
 			int width = bitmap.getWidth();
 			int height = bitmap.getHeight();
 
 			scaleX = 1;// (float) width / lBitmapWidth;
 			scaleY = 1;// (float) height / lBitmapHeight;
 
 			float startX, startY, dx, dy, smooth;
 
 			// if (lCircle) {
 			float scale = 1;// Math.min(width, height)
 			// / Math.min(lBitmapWidth, lBitmapHeight);
 			startX = lStartX * scaleX;
 			startY = lStartY * scaleY;
 			dx = lDx * scale;
 			dy = lDy * scale;
 			smooth = lSmooth * scale;
 			strwidth = lStrokeWidth * scale;
 			// } else {
 			// startX = lStartX * scaleY;
 			// startY = lStartY * scaleY;
 			// dx = lDx * scaleY;
 			// dy = lDy * scaleY;
 			// smooth = lSmooth * scaleY;
 			// strwidth = lStrokeWidth * scaleY;
 			// }
 
 			int numberOfWavesX, numberOfWavesY;
 			float dx2, dy2;
 
 			Path path = new Path();
 
 			float j = dx;
 			if (lCircle) {
 				float adj = j + adjust(lNone, lBlur, strwidth);
 				float cX = width / 2;
 				float cY = height / 2;
 				float radius = Math.min(cX, cY) - adj;
 				float x = Math.max(j, j + cX - cY) + adj;
 
 				if (!lFit) {
 					cX += startX;
 					cY += startY;
 					x += startX;
 				}
 
 				float y = cY;
 
 				double ang = 0;
 				path.moveTo(x, y);
 				float cos = (float) Math.cos(ang);
 				float sin = (float) Math.sin(ang);
 				float x1, x2, y1, y2;
 				for (int n = 0; n < lNumWaves; n++) {
 					x1 = x + smooth * sin;
 					y1 = y + smooth * cos;
 					ang += dangle;
 					cos = (float) Math.cos(ang);
 					sin = (float) Math.sin(ang);
 					x = cX - (radius + j) * cos;
 					y = cY + (radius + j) * sin;
 					x2 = x - smooth * sin;
 					y2 = y - smooth * cos;
 					path.cubicTo(x1, y1, x2, y2, x, y);
 					j = -j;
 				}
 				path.close();
 			} else {
 				if (lFit) {
 					mFitted = fitPath(width, height, dx, dy, smooth, startX,
 							startY, strwidth, lNone, lBlur);
 					startX = mFitted[0];
 					startY = mFitted[1];
 					dx2 = mFitted[2];
 					dy2 = mFitted[3];
 					numberOfWavesX = (int) mFitted[4];
 					numberOfWavesY = (int) mFitted[5];
 				} else {
 					numberOfWavesX = (int) ((width - startX * 2) / dy);
 					numberOfWavesY = (int) ((height - startY * 2) / dy);
 					if (numberOfWavesX % 2 == 0) {
 						numberOfWavesX--;
 					}
 					if (numberOfWavesY % 2 == 0) {
 						numberOfWavesY--;
 					}
 					dx2 = ((float) width - startX * 2) / numberOfWavesX;
 					dy2 = ((float) height - startY * 2) / numberOfWavesY;
 				}
 
 				float x = startX;
 				float y = startY;
 				path.moveTo(startX, startY);
 				for (int n = 1; n <= numberOfWavesY; n++) {
 					y += dy2;
 					path.cubicTo(x - j, y - dy2 / 2 - smooth, x - j, y - dy2
 							/ 2 + smooth, x, y);
 					j = -j;
 				}
 				for (int n = 1; n <= numberOfWavesX; n++) {
 					x += dx2;
 					path.cubicTo(x - dx2 / 2 - smooth, y - j, x - dx2 / 2
 							+ smooth, y - j, x, y);
 					j = -j;
 				}
 				j = -j;
 				for (int n = 1; n <= numberOfWavesY; n++) {
 					y -= dy2;
 					path.cubicTo(x - j, y + dy2 / 2 + smooth, x - j, y + dy2
 							/ 2 - smooth, x, y);
 					j = -j;
 				}
 				for (int n = 1; n <= numberOfWavesX; n++) {
 					x -= dx2;
 					path.cubicTo(x + dx2 / 2 + smooth, y - j, x + dx2 / 2
 							- smooth, y - j, x, y);
 					j = -j;
 				}
 				path.close();
 			}
 			return path;
 		}
 
 		private Bitmap drawIntoBitmap() {
 			Bitmap.Config g;
 			if (MainActivity.isPNG) { // && bitmap.hasAlpha()
 				g = Bitmap.Config.ARGB_8888;
 			} else {
 				g = Bitmap.Config.RGB_565;
 			}
 			// boolean b = true;
 			Bitmap bitmap = bmp1;
 			Bitmap bitmap2 = null;
 			// while (b) {
 			try {
 				bitmap2 = Bitmap.createBitmap(bitmap.getWidth(),
 						bitmap.getHeight(), g);
 				// b = false;
 			} catch (OutOfMemoryError e) {
 				e.printStackTrace();
 				// b = false;
 				if (opts.inSampleSize < 2)
 					opts.inSampleSize = 2;
 				else
 					opts.inSampleSize++;
 				// bitmap = null;
 				// bitmap2 = null;
 				// System.gc();
				iAmLocked = false;
 				return null;
 			}
 			// catch (NullPointerException e) {
 			// // Log.e("qwe123", "" + index);
 			// return ma.getResultBitmap();
 			// }
 			Canvas canvas = new Canvas(bitmap2);
 			if (!MainActivity.isPNG)
 				canvas.drawColor(lBackgroundColor);
 
 			Path path = drawFrameExport(bitmap);
 			if (mustClip)
 				canvas.clipPath(path);
 			canvas.drawBitmap(bitmap, 0, 0, mBitmapPaint);
 			// bitmap.recycle();
 			// System.gc();
 			// System.gc();
 
 			paint.setStrokeWidth(strwidth);
 			if (lRainbow) {
 				float x = lCenterRainbowX;
 				float y = lCenterRainbowY;
 				Shader s = new SweepGradient(x, y, mColors, null);
 				paint.setShader(s);
 			}
 			if (lBlur) {
 				MaskFilter blur = new BlurMaskFilter(strwidth,
 						BlurMaskFilter.Blur.NORMAL);
 				paint.setMaskFilter(blur);
 			} else if (lEmboss) {
 				MaskFilter emboss = new EmbossMaskFilter(
 						new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f * scaleY);
 				paint.setMaskFilter(emboss);
 			}
 
 			if (lStartX == 0 && lStartY == 0 && !lCircle) {
 				float p = strwidth / 2;
 				canvas.drawRect(p, p, bitmap.getWidth() - p, bitmap.getHeight()
 						- p, paint);
 			}
 
 			canvas = new Canvas(bitmap2);
 			canvas.drawPath(path, paint);
 
 			// if (lShouldDrawText) {
 			// PathMeasure pm = new PathMeasure(path, true);
 			// pLength = pm.getLength();
 			// cLength = pLength * lCurrentLength / lPathLength;
 			// lTextPaint.setTextSize(strwidth);
 			// lUpdateTextBounds();
 			//
 			// if (lFillWithText) {
 			// int count = (int) pLength / ltw;
 			// for (int i = 0; i < count; i++) {
 			// canvas.drawTextOnPath(sText, path, cLength + i * ltw,
 			// ladj1, lTextPaint);
 			// }
 			// } else {
 			// canvas.drawTextOnPath(sText, path, cLength, ladj1,
 			// lTextPaint);
 			// }
 			// }
 
 			if (isCircle && lFit) {
 				int l = 0, u = 0, dr;
 				int w = bitmap2.getWidth();
 				int h = bitmap2.getHeight();
 				if (w > h) {
 					l = (w - h) / 2;
 					dr = h;
 				} else if (w < h) {
 					u = (h - w) / 2;
 					dr = w;
 				} else {
 					return bitmap2;
 				}
 				bitmap2 = Bitmap.createBitmap(bitmap2, l, u, dr, dr);
 			}
 
 			return bitmap2;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (bmp3 == null) {
 				ma.loadBitmap(true);
 				return;
 			}
 
 			if (isMoving) {
 				ma.updateMoving(bmp3);
 				// bmp3 = null;
 				// System.gc();
 				isLocked = false;
 				iAmLocked = false;
 				// ma.update(bmp3, ma.index + 1);
 				// mImageView.setImageBitmap(bmp3);
 			} else {
 				isLocked = false;
 				iAmLocked = false;
 				ma.update(bmp3, index);
 				if (!iAmLocked)
 					freeMemory();
 			}
 			// Toast.makeText(ma, result, Toast.LENGTH_SHORT).show();
 		}
 		// @Override
 		// protected void onPreExecute() {
 		// }
 
 		// @Override
 		// protected void onProgressUpdate(Void... v) {
 		//
 		// }
 	}
 
 	@Override
 	public void setBitmap(Bitmap bmp1) {
 		super.setBitmap(bmp1);
 		if (!iAmLocked) {
 			mCenterRainbowX = mBitmapWidth / 2;
 			mCenterRainbowY = mBitmapHeight / 2;
 		}
 	}
 
 	@Override
 	public void rescale(float scale) {
 		bmp1 = null;
 		bmp3 = null;
 		// Toast.makeText(mContext, "" + opts.inSampleSize, 0).show();
 		mStrokeWidth *= scale;
 		sd.update(mStrokeWidth);
 		mDx *= scale;
 		mDy *= scale;
 		mCenterRainbowX *= scale;
 		mCenterRainbowY *= scale;
 		mStartX *= scale;
 		mStartY *= scale;
 		mSmooth *= scale;
 		System.gc();
 	}
 
 	@Override
 	public void invalidate() {
 		if (!isLocked && bmp1 != null)
 			new Draw().execute();
 	}
 
 	@Override
 	public void activate() {
 		super.activate();
 		ib.setImageDrawable(d2);
 		// ib.setImageResource(R.drawable.bt_border_pressed);
 	}
 
 	@Override
 	public void deactivate() {
 		super.deactivate();
 		ib.setImageDrawable(d1);
 		// ib.setImageResource(R.drawable.bt_border);
 	}
 
 	// private Bitmap mBitmap;
 	// private float mDy2, mDx2;
 	private int tw;
 	private float mX, mY, adj1;
 	float sD, sMx, sMy, mSmoothInitial;// = 5;
 
 	// private int mRainbowD = PADX;
 	// private float mAdjusted;
 	// private Paint mDotPaint = new Paint();
 
 	public boolean onTouchEvent(MotionEvent event) {
 		int action = event.getActionMasked();
 		int P = event.getPointerCount();
 		// int N = event.getHistorySize();
 		if (action != MotionEvent.ACTION_MOVE) {
 			if (action == MotionEvent.ACTION_DOWN) {
 				isMoving = true;
 				touch_start(event.getX(0), event.getY(0));
 				return true;
 			} else if (action == MotionEvent.ACTION_UP) {
 				isMoving = false;
 				if (CirclesEf.sOnlyBorderIndex > -1) {
 					sBorderWidth = ma.getMaxWaveHeight();
 					//isLocked = false;
 					ma.update(null, 0);
 				} else {
 					invalidate();
 				}
 				// if (adjustTextPosition) {
 				// adjustTextPosition = false;
 				// invalidate();
 				// return true;
 				// }
 				// invalidate();
 				// if (!isLocked)
 				// ma.update(bmp3, ma.index + 1);
 			}
 			if (action == MotionEvent.ACTION_POINTER_DOWN) {
 				if (P == 2) {
 					float sX2 = event.getX(0) - event.getX(1);
 					float sY2 = event.getY(0) - event.getY(1);
 					sD = FloatMath.sqrt(sX2 * sX2 + sY2 * sY2);
 					mSmoothInitial = mSmooth;
 					return true;
 				} else if (P == 3 && !mustFit) {
 					sMx = mStartX - event.getX(2);
 					sMy = mStartY - event.getY(2);
 					return true;
 				}
 				return false;
 			}
 		} else {
 			if (P == 1) {
 				touch_move(event.getX(), event.getY());
 			} else if (P == 2) {
 				float sX2 = event.getX(0) - event.getX(1);
 				float sY2 = event.getY(0) - event.getY(1);
 				float sD2 = FloatMath.sqrt(sX2 * sX2 + sY2 * sY2);
 				mSmooth = mSmoothInitial + sD2 - sD;
 			} else if (P == 3 && !mustFit) {
 				mStartX = (int) (sMx + event.getX(2));
 				mStartY = (int) (sMy + event.getY(2));
 			} else {
 				return false;
 			}
 			invalidate();
 		}
 		return true;
 	}
 
 	public void setAdjustRainbow(boolean b) {
 		adjustRainbow = b;
 	}
 
 	private void touch_move(float x, float y) {
 		// if (adjustRainbow) {
 		// // mRainbowFrame.setX(x);
 		// // mRainbowFrame.setY(y);
 		// mCenterRainbowX = x - PADX;
 		// mCenterRainbowY = y - PADX;
 		// Shader s = new SweepGradient(2 * (mCenterRainbowX),// - mBitmapWidth
 		// // / 4.f),
 		// 2 * (mCenterRainbowY), mColors, null);// - mBitmapHeight /
 		// // 4.f), mColors,
 		// // null);
 		// mPaint.setShader(s);
 		// } else
 		// if (adjustTextPosition) {
 		// float dx = (x - mX);
 		// float dy = (y - mY);
 		// if (x > mBckgrWidth / 2 + PADX)
 		// dy = -dy;
 		// if (y < mBckgrHeight / 2 + PADX)
 		// dx = -dx;
 		// currentLength += dx + dy;
 		// if (currentLength > pathLength)
 		// currentLength = 0;
 		// else if (currentLength < 0)
 		// currentLength = pathLength;
 		// mX = x;
 		// mY = y;
 		// } else {
 		float dx = x - mX;
 		float dy = y - mY;
 		mDx = Math.abs(mDx + dx);
 		mDy = Math.abs(mDy + dy);
 
 		if (mDy < 2) {
 			mDy = 2;
 		} else {
 			mSmooth = mSmooth + dy / 2;
 		}
 		if (mDx < 2)
 			mDx = 2;
 		mX = x;
 		mY = y;
 		// }
 	}
 
 	private void touch_start(float x, float y) {
 		mX = x;
 		mY = y;
 		if (isRainbow) {
 			if (adjustRainbow) {
 
 			}
 			// if (Math.abs(x - mCenterRainbowX - PADX) < mRainbowD
 			// && Math.abs(y - mCenterRainbowY - PADX) < mRainbowD) {
 			// adjustRainbow = true;
 			// } else {
 			// adjustRainbow = false;
 			// }
 		}
 		// if (shouldDrawText) {
 		// if (Math.abs(x - dt[0] - PADX) < 20
 		// && Math.abs(y - dt[1] - PADX) < 20) {
 		// adjustTextPosition = true;
 		// } else {
 		// adjustTextPosition = false;
 		// }
 		// }
 	}
 
 	private static float[] fitPath(float width, float height, float mDx,
 			float mDy, float mSmooth, float startX, float startY,
 			float strokewidth, boolean none, boolean blur) {
 		float sX, sY;
 		float dx2, dy2;
 		float adjustStrokeWidth = adjust(none, blur, strokewidth);
 		int numberOfWavesX, numberOfWavesY;
 		Path p = new Path();
 		RectF bounds = new RectF();
 		// int k = 0;
 		do {
 			sX = startX;
 			sY = startY;
 			numberOfWavesX = (int) ((width - sX * 2) / mDy);
 			numberOfWavesY = (int) ((height - sY * 2) / mDy);
 			if (numberOfWavesX % 2 == 0) {
 				numberOfWavesX--;
 			}
 			if (numberOfWavesY % 2 == 0) {
 				numberOfWavesY--;
 			}
 			dy2 = ((float) height - sY * 2) / numberOfWavesY;
 			dx2 = ((float) width - sX * 2) / numberOfWavesX;
 
 			p.moveTo(0, 0);
 			float j = mDx;
 			p.cubicTo(j, dy2 / 2 - mSmooth, j, dy2 / 2 + mSmooth, 0, dy2);
 			p.computeBounds(bounds, false);
 			p.reset();
 			startX = 3 * (bounds.right - bounds.left) / 4 + adjustStrokeWidth;
 
 			p.moveTo(0, 0);
 			p.cubicTo(dx2 / 2 - mSmooth, -j, dx2 / 2 + mSmooth, -j, dx2, 0);
 			bounds = new RectF();
 			p.computeBounds(bounds, true);
 			p.reset();
 			startY = 3 * (bounds.bottom - bounds.top) / 4 + adjustStrokeWidth;
 			// k++;
 
 		} while (Math.abs(sX - startX) > 0.01 || Math.abs(sY - startY) > 0.01);
 
 		return new float[] { sX, sY, dx2, dy2, numberOfWavesX, numberOfWavesY };
 	}
 
 	private static float adjust(boolean none, boolean blur, float strokewidth) {
 		float adjustStrokeWidth = strokewidth / 2;
 		if (none && !blur) {
 			adjustStrokeWidth = -adjustStrokeWidth;
 		} else if (none && blur) {
 			adjustStrokeWidth = 0;
 		} else if (blur) {
 			adjustStrokeWidth *= 2;
 		}
 		return adjustStrokeWidth;
 	}
 
 	@Override
 	public void settingsChanged(int color, int mode1, int mode2,
 			float strokewidth, float rainbow[]) {
 		// if (adjustRainbow)
 		// adjustRainbow = false;
 		if (color != -1) {
 			sColor = color;
 			mColor = color;
 			mPaint.setColor(color);
 			setHue(d1);
 			setHue(d2);
 
 			float[] f1 = new float[3];
 			float[] f = new float[3];
 			Color.colorToHSV(mColor, f1);
 			for (int i = 0; i < 7; i++) {
 				Color.colorToHSV(mColors[i], f);
 				f[1] = f1[1];
 				f[2] = f1[2];
 				mColors[i] = Color.HSVToColor(f);
 			}
 			if (isRainbow) {
 				Shader s = new SweepGradient(mCenterRainbowX, mCenterRainbowY,
 						mColors, null);
 				mPaint.setShader(s);
 			}
 		}
 		if (strokewidth != -1) {
 			sStrokeWidth = strokewidth;
 			mPaint.setStrokeWidth(strokewidth);
 			// mTextPaint.setTextSize(strokewidth);
 			// updateTextBounds();
 			if (strokewidth != 0)
 				mBlur = new BlurMaskFilter(strokewidth,
 						BlurMaskFilter.Blur.NORMAL);
 			mStrokeWidth = strokewidth;
 			if (isBlur) {
 				mPaint.setMaskFilter(mBlur);
 			}
 		}
 		if (mode1 != -1)
 			mMode1 = mode1;
 		if (mode2 != -1)
 			mMode2 = mode2;
 		switch (mode1) {
 		case WaveSettingsLayout.NORMAL:
 			// mPaint.setXfermode(null);
 			mPaint.setMaskFilter(null);
 			isBlur = false;
 			break;
 		case WaveSettingsLayout.BLUR:
 			// mPaint.setXfermode(null);
 			mPaint.setMaskFilter(mBlur);
 			isBlur = true;
 			break;
 		case WaveSettingsLayout.EMBOSS:
 			// mPaint.setXfermode(null);
 			mPaint.setMaskFilter(mEmboss);
 			isBlur = false;
 			break;
 		}
 		switch (mode2) {
 		case WaveSettingsLayout.COLOR:
 			sd.setRainbow(false);
 			mPaint.setXfermode(null);
 			mPaint.setShader(null);
 			isRainbow = false;
 			// mRainbowFrame.setVisibility(View.GONE);
 			isNone = false;
 			break;
 		case WaveSettingsLayout.RAINBOW:
 			sd.setRainbow(true);
 			// mRainbowFrame.setVisibility(View.VISIBLE);
 			mPaint.setXfermode(null);
 			if (!isRainbow) {
 				if (mColors[0] == mColors[2]) {
 					mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
 							0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
 				}
 				Shader s = new SweepGradient(mCenterRainbowX, mCenterRainbowY,
 						mColors, null);
 				mPaint.setShader(s);
 				isRainbow = true;
 			}
 			isNone = false;
 			break;
 		case WaveSettingsLayout.NONE:
 			sd.setRainbow(false);
 			// mPaint.setMaskFilter(null);
 			isRainbow = false;
 			// mRainbowFrame.setVisibility(View.GONE);
 			mPaint.setShader(null);
 			isNone = true;
 			if (!MainActivity.isPNG) {
 				if (isBlur) {
 					mPaint.setColor(mColor);
 					mPaint.setXfermode(new PorterDuffXfermode(
 							PorterDuff.Mode.CLEAR));
 				} else {
 					mPaint.setColor(mBackgroundColor);
 					mPaint.setXfermode(null);
 				}
 			} else {
 				mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
 			}
 			break;
 		}
 		if (rainbow != null) {
 			mCenterRainbowX = (bmp1.getWidth() / 2.f)
 					* (1.f - rainbow[0] / sd.getRainbowScale());
 			mCenterRainbowY = (bmp1.getHeight() / 2.f)
 					* (1.f - rainbow[1] / sd.getRainbowScale());
 			Shader s = new SweepGradient(mCenterRainbowX, mCenterRainbowY,
 					mColors, null);
 			mPaint.setShader(s);
 		}
 		invalidate();
 	}
 
 	public int getColor() {
 		return mColor;
 	}
 
 	// private void updateTextBounds() {
 	// Rect bounds = new Rect();
 	// mTextPaint.getTextBounds(sText + "...", 0, sText.length() + 3, bounds);
 	// tw = bounds.right;
 	// adj1 = -(bounds.bottom + bounds.top) / 2;
 	// }
 
 	// @Override
 	// public String getString() {
 	// return "Waves";
 	// }
 
 	public void setFit(boolean mustFit) {
 		this.mustFit = mustFit;
 		invalidate();
 	}
 
 	public void setCircle(boolean isCircle) {
 		this.isCircle = isCircle;
 		invalidate();
 	}
 
 	public void setClip(boolean mustClip) {
 		this.mustClip = mustClip;
 		invalidate();
 	}
 
 	public void setPostmark() {
 		if (!isCircle) {
 			mustFit = false;
 			mustClip = true;
 			mStartX = 0;
 			mStartY = 0;
 			invalidate();
 		}
 	}
 
 	public boolean getFit() {
 		return mustFit;
 	}
 
 	public boolean getCircle() {
 		return isCircle;
 	}
 
 	public boolean getClip() {
 		return mustClip;
 	}
 
 	public float getWaveHeight() {
 		if (mustFit) {
 			return mFitted[0];
 		}
 		return mDx;
 	}
 
 	private void setHue(Drawable drawable) {
 		if (drawable == null)
 			return;
 		// Drawable d =
 		// mContext.getResources().getDrawable(R.drawable.bt_border);
 		// Bitmap b = Bitmap.createBitmap(d., height, config)
 		// Bitmap b = BitmapFactory.decodeResource(mContext.getResources(),
 		// R.drawable.bt_border);
 		// Paint p = new Paint(mPaint);
 		// p.set
 		// Canvas c = new Canvas(b.getWidth(),b.getHeight(),null);
 
 		final ColorMatrix matrixA = new ColorMatrix();
 		// making image B&W
 		matrixA.setSaturation(0);
 
 		final ColorMatrix matrixB = new ColorMatrix();
 		// applying scales for RGB color values
 
 		matrixB.setScale((float) Color.red(mColor) / 255,
 				(float) Color.green(mColor) / 255,
 				(float) Color.blue(mColor) / 255,
 				(float) Color.alpha(mColor) / 255);
 		matrixA.setConcat(matrixB, matrixA);
 
 		final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
 				matrixA);
 		drawable.setColorFilter(filter);
 	}
 }
