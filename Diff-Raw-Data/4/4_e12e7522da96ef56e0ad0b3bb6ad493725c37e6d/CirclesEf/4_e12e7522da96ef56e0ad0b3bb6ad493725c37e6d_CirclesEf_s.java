 package vnd.blueararat.Effoto;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapShader;
 import android.graphics.BlurMaskFilter;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorMatrix;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.Shader;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class CirclesEf extends Effect {
 
 	private float scale, radius, rnd_pos_max, rnd_size_max, blur_radius, satur,
 			contrast, brightness;
 	private int opacity;
 
 	private SeekBar mSeekBarScale;
 	private SeekBar mSeekBarRadius;
 	private SeekBar mSeekBarOpacity;
 	private SeekBar mSeekBarRndPos;
 	private SeekBar mSeekBarRndSize;
 	private SeekBar mSeekBarBlur;
 	private SeekBar mSeekBarSatur;
 	private SeekBar mSeekBarContrast;
 	private SeekBar mSeekBarBrightness;
 
 	private TextView mTextViewScaleVal;
 	private TextView mTextViewRadiusVal;
 	private TextView mTextViewOpacityVal;
 	private TextView mTextViewRndPosVal;
 	private TextView mTextViewRndSizeVal;
 	private TextView mTextViewBlurRadVal;
 	private TextView mTextViewSaturVal;
 	private TextView mTextViewContrastVal;
 	private TextView mTextViewBrightnessVal;
 	private Bitmap bmp2;
 	// private long i1;
 	// public int index;
 	public boolean isOnlyBorder = true;
 	public static int sOnlyBorderIndex = -1;
 
 	// private int mBitmapHeight, mBitmapWidth;
 
 	public CirclesEf(Context ctx) {
 		this(ctx, 0.01f + MTRNGJNILib.randDblExc(0.1f));
 		// this(ctx,
 		// MTRNGJNILib.randDblExc(0.3f),
 		// 2+MTRNGJNILib.randInt(10),
 		// 255,
 		// 0,
 		// 0,
 		// 0,
 		// MTRNGJNILib.randDblExc(2),
 		// MTRNGJNILib.randDblExc(1),
 		// 0);
 		// this(ctx, 0.05f, 10.0f, 255, 0, 0, 0, 1, 1, 0);
 	}
 
 	public CirclesEf(Context ctx, float lscale) {
 		this(ctx, lscale, 0.25f / lscale + MTRNGJNILib.rand(0.25f / lscale),
 				255, 0, 0, 0, 0.6f + MTRNGJNILib.rand(0.8f), 0.6f + MTRNGJNILib
 						.rand(0.8f), 0, null);
 	}
 
 	public CirclesEf(Context ctx, File serialized) {
 		this(ctx, 0, 0, 0, 0, 0, 0, 0, 0, 0, serialized);
 		ib.setImageResource(R.drawable.bt_circles_normal);
 	}
 
 	public CirclesEf(Context ctx, float lscale, float lradius, int lopacity,
 			float lrnd_pos_max, float lrnd_size_max, float lblur_radius,
 			float lsatur, float lcontrast, float lbrightness, File serialized) {
 		super(ctx);
 
 		// ib.setImageResource(R.drawable.bt_circles);
 		if (serialized == null || !load(serialized)) {
 			this.scale = lscale;
 			this.radius = lradius;
 			this.opacity = lopacity;
 			this.rnd_pos_max = lrnd_pos_max;
 			this.rnd_size_max = lrnd_size_max;
 			this.blur_radius = lblur_radius;
 			this.satur = lsatur;
 			this.contrast = lcontrast;
 			this.brightness = lbrightness;
 		}
 		LayoutInflater inflater = LayoutInflater.from(ctx);
 		View v = inflater.inflate(R.layout.sliding_drawer_1,
 				ma.getParentLayout(), true);
 		wsd = (WrappingSlidingDrawer) v.findViewById(R.id.slidingDrawer);
 
 		Button bt = (Button) wsd.getHandle();
 		bt.setText("" + index);
 
 		MainActivity.setViewGroupFont(wsd, Typeface.MONOSPACE);
 
 		if (sOnlyBorderIndex < 0) {
 			sBorderWidth = ma.getMaxWaveHeight();
 			if (sBorderWidth == 0) {
 				isOnlyBorder = false;
 			} else {
 				sOnlyBorderIndex = index - 1;
 			}
 		}
 
 		mTextViewScaleVal = (TextView) wsd.findViewById(R.id.scale_val);
 		mTextViewScaleVal.setText(String.format("%.2f", scale));
 		mTextViewRadiusVal = (TextView) wsd.findViewById(R.id.radius_val);
 		mTextViewRadiusVal.setText(String.format("%.2f", radius));
 		mTextViewOpacityVal = (TextView) wsd.findViewById(R.id.opacity_val);
 		mTextViewOpacityVal.setText(String
 				.format("%.2f", (float) opacity / 255));
 		mTextViewRndPosVal = (TextView) wsd.findViewById(R.id.rnd_pos_val);
 		mTextViewRndPosVal.setText(String.format("%.2f", rnd_pos_max));
 		mTextViewRndSizeVal = (TextView) wsd.findViewById(R.id.rnd_size_val);
 		mTextViewRndSizeVal.setText(String.format("%.2f", rnd_size_max));
 		mTextViewBlurRadVal = (TextView) wsd.findViewById(R.id.blur_rad_val);
 		mTextViewBlurRadVal.setText(String.format("%.2f", blur_radius));
 		mTextViewSaturVal = (TextView) wsd.findViewById(R.id.satur_val);
 		mTextViewSaturVal.setText(String.format("%.2f", satur));
 		mTextViewContrastVal = (TextView) wsd.findViewById(R.id.contrast_val);
 		mTextViewContrastVal.setText(String.format("%.2f", contrast));
 		mTextViewBrightnessVal = (TextView) wsd
 				.findViewById(R.id.brightness_val);
 		mTextViewBrightnessVal.setText(String.format("%.2f",
 				(brightness + 255) / 255));
 
 		mSeekBarScale = (SeekBar) wsd.findViewById(R.id.scale);
 		mSeekBarScale.setProgress((int) (scale * 1000 - 1));
 		mSeekBarScale.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				isMoving = false;
 				// scale = (float) (seekBar.getProgress() + 1) / 1000;
 				invalidate();
 				// int w = Math.round(bmp1.getWidth() * scale);
 				// int h = Math.round(bmp1.getHeight() * scale);
 				// bmp2 = Bitmap.createScaledBitmap(bmp1, w, h, true);
 				// new Draw().execute(bmp2);
 				// draw(bmp2);
 				// mImageView.setImageBitmap(bmp3);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				isMoving = true;
 			}
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				if (fromUser)
 					scale = (float) (progress + 1) / 1000;
 				mTextViewScaleVal.setText(String.format("%.2f", scale));
 			}
 		});
 
 		mSeekBarRadius = (SeekBar) wsd.findViewById(R.id.radius);
 		mSeekBarRadius.setProgress((int) (radius * 10 - 1));
 		mSeekBarRadius
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						isMoving = false;
 						// radius = (float) (seekBar.getProgress() + 1) / 10;
 						invalidate();
 						// new Draw().execute(bmp2);
 						// draw(bmp2);
 						// mImageView.setImageBitmap(bmp3);
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						isMoving = true;
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 						if (fromUser)
 							radius = (float) (progress + 1) / 10;
 						mTextViewRadiusVal.setText(String
 								.format("%.2f", radius));
 					}
 				});
 
 		mSeekBarOpacity = (SeekBar) wsd.findViewById(R.id.opacity);
 		mSeekBarOpacity.setProgress(opacity - 1);
 		mSeekBarOpacity
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						isMoving = false;
 						invalidate();
 						// draw(bmp2);
 						// mImageView.setImageBitmap(bmp3);
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						isMoving = true;
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 						opacity = progress + 1;
 						mTextViewOpacityVal.setText(String.format("%.2f",
 								(float) opacity / 255));
 					}
 				});
 
 		mSeekBarRndPos = (SeekBar) wsd.findViewById(R.id.rnd_pos);
 		mSeekBarRndPos.setProgress((int) (rnd_pos_max * 1000));
 		mSeekBarRndPos
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						isMoving = false;
 						invalidate();
 						// draw(bmp2);
 						// mImageView.setImageBitmap(bmp3);
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						isMoving = true;
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 						if (fromUser)
 							rnd_pos_max = (float) progress / 1000;
 						mTextViewRndPosVal.setText(String.format("%.2f",
 								rnd_pos_max));
 					}
 				});
 
 		mSeekBarRndSize = (SeekBar) wsd.findViewById(R.id.rnd_size);
 		mSeekBarRndSize.setProgress((int) (rnd_size_max * 1000));
 		mSeekBarRndSize
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						isMoving = false;
 						invalidate();
 						// draw(bmp2);
 						// mImageView.setImageBitmap(bmp3);
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						isMoving = true;
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 						if (fromUser)
 							rnd_size_max = (float) progress / 1000;
 						mTextViewRndSizeVal.setText(String.format("%.2f",
 								rnd_size_max));
 					}
 				});
 
 		mSeekBarBlur = (SeekBar) wsd.findViewById(R.id.blur_rad);
 		mSeekBarBlur.setProgress((int) blur_radius);
 		mSeekBarBlur.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				isMoving = false;
 				// radius = (float) (seekBar.getProgress() + 1) / 10;
 				invalidate();
 				// draw(bmp2);
 				// mImageView.setImageBitmap(bmp3);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				isMoving = true;
 			}
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				if (fromUser)
 					blur_radius = (float) progress;
 				mTextViewBlurRadVal.setText(String.format("%.0f", blur_radius));
 			}
 		});
 
 		mSeekBarSatur = (SeekBar) wsd.findViewById(R.id.satur);
 		mSeekBarSatur.setProgress((int) (satur * 100));
 		mSeekBarSatur.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				isMoving = false;
 				// radius = (float) (seekBar.getProgress() + 1) / 10;
 				invalidate();
 				// draw(bmp2);
 				// mImageView.setImageBitmap(bmp3);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				isMoving = true;
 			}
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				if (fromUser)
 					satur = (float) progress / 100;
 				mTextViewSaturVal.setText(String.format("%.2f", satur));
 			}
 		});
 
 		mSeekBarContrast = (SeekBar) wsd.findViewById(R.id.contrast);
 		mSeekBarContrast.setProgress((int) (contrast * 100));
 		mSeekBarContrast
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						isMoving = false;
 						// radius = (float) (seekBar.getProgress() + 1) / 10;
 						invalidate();
 						// draw(bmp2);
 						// mImageView.setImageBitmap(bmp3);
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						isMoving = true;
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 						if (fromUser)
 							contrast = (float) progress / 100;
 						mTextViewContrastVal.setText(String.format("%.2f",
 								contrast));
 					}
 				});
 
 		mSeekBarBrightness = (SeekBar) wsd.findViewById(R.id.brightness);
 		mSeekBarBrightness.setProgress((int) (brightness + 255));
 		mSeekBarBrightness
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						isMoving = false;
 						// radius = (float) (seekBar.getProgress() + 1) / 10;
 						invalidate();
 						// draw(bmp2);
 						// mImageView.setImageBitmap(bmp3);
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						isMoving = true;
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 						if (fromUser)
 							brightness = progress - 255;
 						mTextViewBrightnessVal.setText(String.format("%.2f",
 								(float) progress / 255));
 					}
 				});
 	}
 
 	class Draw extends AsyncTask<Bitmap, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Bitmap... params) {
 			isLocked = true;
 			iAmLocked = true;
 			draw(params[0]);
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 
 			if (bmp3 == null) {
 				ma.loadBitmap(true);
 				return;
 			}
 
 			isLocked = false;
 			iAmLocked = false;
 			if (isMoving) {
 				ma.updateMoving(bmp3);
 				if (!iAmLocked)
 					bmp3 = null;
 				System.gc();
 			} else {
 				ma.update(bmp3, index);
 				if (!iAmLocked)
 					freeMemory();
 			}
 		}
 	}
 
 	private synchronized void draw(Bitmap bitmap) {
 		// i1 = System.currentTimeMillis();
 		try {
 			bmp3 = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight,
 					Bitmap.Config.ARGB_8888);
 		} catch (OutOfMemoryError e) {
 			bmp3 = null;
 			if (opts.inSampleSize < 2)
 				opts.inSampleSize = 2;
 			else
 				opts.inSampleSize++;
 			iAmLocked = false;
 			return;
 		}
 		Canvas c = new Canvas(bmp3);
 		if (!ma.isPNG) {
 			int color = ma.prefs.getInt(Prefs.KEY_COLOR, 0xFFFFFFFF);
 			c.drawColor(color);
 		}
 
 		int bh = bitmap.getHeight();
 		int bw = bitmap.getWidth();
 
 		// float f2 = 1 / scale;
 		// float f = 0.5f / scale;
 		float f2 = (float) mBitmapWidth / bw;
 		float f20 = f2 / 2;// scale;
 		// float f3 = (float)mBitmapHeight/bh;
 		// float f30 = f3/2;
 		// 2*f20+f3*bh=mBitmapHeight;
 		float f3 = ((float) mBitmapHeight - f20 * 2.f) / (bh - 1);
 		float bottom = (float) mBitmapHeight - sBorderWidth;
 		float right = (float) mBitmapWidth - sBorderWidth;
 
 		Paint p = new Paint();
 		p.setDither(true);
 		p.setAntiAlias(true);
 		// p.setXfermode(new AvoidXfermode(Color.RED, 245,
 		// AvoidXfermode.Mode.TARGET));
 		// c.drawBitmap(bmp1, 0, 0, p);
 		// float mx[] = { -0.3f, -0.3f, -0.3f, 0.0f, 255.0f, -0.3f, -0.3f,
 		// -0.3f,
 		// 0.0f, 255.0f, -0.3f, -0.3f, -0.3f, 0.0f, 255.0f, 0.0f, 0.0f,
 		// 0.0f, 1.0f, 0.0f };
 		// float mxs[] = { 1.f - 2 * satur, satur, satur, 0, 0,
 		// satur, 1.f - 2 * satur, satur, 0, 0,
 		// satur, satur, 1.f - 2 * satur, 0, 0,
 		// 0, 0, 0, 1.f, 0 };
 		ColorMatrix cm = new ColorMatrix();
 		cm.setSaturation(satur);
 
 		// float sc = contrast;
 		float translate = (-0.5f * contrast + 0.5f) * 255.f;
 		cm.postConcat(new ColorMatrix(new float[] { contrast, 0, 0, 0,
 				translate, 0, contrast, 0, 0, translate, 0, 0, contrast, 0,
 				translate, 0, 0, 0, 1, 0 }));
 
 		// float mxb[] = { 1, 0, 0, 0, brightness,
 		// 0, 1, 0, 0, brightness,
 		// 0, 0, 1, 0, brightness,
 		// 0, 0, 0, 1, 0 };
 		cm.postConcat(new ColorMatrix(new float[] { 1, 0, 0, 0, brightness, 0,
 				1, 0, 0, brightness, 0, 0, 1, 0, brightness, 0, 0, 0, 1, 0 }));
 		// ColorMatrix cmb = new ColorMatrix(mxb);
 		// BlurMaskFilter Blur = new BlurMaskFilter(50,
 		// BlurMaskFilter.Blur.NORMAL);
 		// p.setMaskFilter(Blur);
 
 		p.setColorFilter(new ColorMatrixColorFilter(cm));
 		// p.setAlpha(50);
 
 		// if (isOnlyBorder) {
 		// Path path = new Path();
 		// path.addRect(sBorderWidth, sBorderWidth, right, bottom,
 		// Direction.CCW);
 		// c.clipPath(path);
 		// }
 
 		c.drawBitmap(bmp1, 0, 0, p);
 		p.setColorFilter(null);
 
 		float x;
 		float y = 0;
 		int k = 0;
 
 		int length = bitmap.getWidth() * bitmap.getHeight();
 		int[] array = new int[length];
 		bitmap.getPixels(array, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
 				bitmap.getHeight());
 		k = 0;
 		y = f20;
 
 		if (blur_radius > 0) {
 			BlurMaskFilter Blur = new BlurMaskFilter(blur_radius,
 					BlurMaskFilter.Blur.NORMAL);
 			p.setMaskFilter(Blur);
 		}
 		float y1 = sBorderWidth + radius, y2 = bottom - radius, x2 = right
 				- radius;
 		for (int i = 0; i < bh; i++) {
 			x = f20;
 			for (int j = 0; j < bw; j++) {
 				if (isOnlyBorder && (y > y1 && y < y2) && (x > y1 && x < x2)) {
 					x += f2;
 					k++;
 					continue;
 				}
 				p.setColor(array[k]);
 				p.setAlpha(opacity);
 				// p.setAlpha(125);
 				c.drawCircle(x + rnd_pos_max * radius * MTRNGJNILib.rand(), y
 						+ rnd_pos_max * radius * MTRNGJNILib.rand(), radius
 						+ rnd_size_max * radius * (0.5f - MTRNGJNILib.rand()),
 						p);
 				// c.drawText(text[k % 4], x, y, p);
 				x += f2;
 				k++;
 			}
 			y += f3;
 		}
 		
 		p.setAlpha(255);
		
 		if (isOnlyBorder) {
 			// if (true) {
 			BitmapShader shader = new BitmapShader(bmp1, Shader.TileMode.CLAMP,
 					Shader.TileMode.CLAMP);
 			p.setShader(shader);
 			RectF r1 = new RectF(sBorderWidth, sBorderWidth, right, bottom);
 			c.drawRoundRect(r1, radius, radius, p);
 			p.setShader(null);
 			// } else {
 			// Rect r1 = new Rect((int) sBorderWidth, (int) sBorderWidth,
 			// (int) right, (int) bottom);
 			// c.drawBitmap(bmp1, r1, r1, p);
 			// }
 
 		}
 	}
 
 	@Override
 	public void invalidate() {
 		if (!isLocked && bmp1 != null) {
 			int w = Math.round(bmp1.getWidth() * scale);
 			int h = Math.round(bmp1.getHeight() * scale);
 			if (w == 0)
 				w = 1;
 			if (h == 0)
 				h = 1;
 			bmp2 = Bitmap.createScaledBitmap(bmp1, w, h, true);
 			new Draw().execute(bmp2);
 		}
 	}
 
 	@Override
 	public void activate() {
 		super.activate();
 		ib.setImageResource(R.drawable.bt_circles_pressed);
 	}
 
 	@Override
 	public void deactivate() {
 		super.deactivate();
 		ib.setImageResource(R.drawable.bt_circles_normal);
 	}
 
 	// @Override
 	// public String getString() {
 	// return "Circles";
 	// }
 
 	@Override
 	public void rescale(float scale) {
 		bmp1 = null;
 		bmp2 = null;
 		bmp3 = null;
 		System.gc();
 		this.scale /= scale;
 		mSeekBarScale.setProgress((int) (1000 * scale) - 1);
 		radius *= scale;
 		mSeekBarRadius.setProgress((int) (10 * radius) - 1);
 		rnd_pos_max *= scale;
 		mSeekBarRndPos.setProgress((int) (1000 * rnd_pos_max));
 		rnd_size_max *= scale;
 		mSeekBarRndSize.setProgress((int) (1000 * rnd_size_max));
 		blur_radius *= scale;
 		mSeekBarBlur.setProgress((int) blur_radius);
 	}
 
 	@Override
 	public void freeMemory() {
 		super.freeMemory();
 		bmp2 = null;
 		System.gc();
 	}
 
 	private static class SavedEffect implements Serializable {
 
 		private static final long serialVersionUID = -2576726311705577506L;
 		// private int lindex;
 		private float lscale;
 		private float lradius;
 		private float lrnd_pos_max;
 		private float lrnd_size_max;
 		private float lblur_radius;
 		private float lsatur;
 		private float lcontrast;
 		private float lbrightness;
 		private int lopacity;
 		private boolean lisOnlyBorder;
 		private float lsBorderWidth;
 		private int lsOnlyBorderIndex;
 
 		// private int sOnlyBorderIndex = -1;
 		private SavedEffect(CirclesEf ef) {
 			// lindex = ef.index;
 			lscale = ef.scale;
 			lradius = ef.radius;
 			lrnd_pos_max = ef.rnd_pos_max;
 			lrnd_size_max = ef.rnd_size_max;
 			lblur_radius = ef.blur_radius;
 			lsatur = ef.satur;
 			lcontrast = ef.contrast;
 			lbrightness = ef.brightness;
 			lopacity = ef.opacity;
 			lisOnlyBorder = ef.isOnlyBorder;
 			lsBorderWidth = sBorderWidth;
 			lsOnlyBorderIndex = sOnlyBorderIndex;
 		}
 	}
 
 	@Override
 	protected void save(File folder) {
 		File f = new File(folder, index + ":" + getClass().getName());
 		// Log.e("class", f.getAbsolutePath());
 		SavedEffect se = new SavedEffect(this);
 		try {
 			// FileOutputStream fileOut = new FileOutputStream(f);
 			ObjectOutputStream out = new ObjectOutputStream(
 					new FileOutputStream(f));
 			out.writeObject(se);
 			out.close();
 			// fileOut.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected boolean load(File serialized) {
 		// File f = new File(folder, index + getClass().getName());
 		// Log.e("class", f.getAbsolutePath());
 		// SavedEffect se = new SavedEffect();
 		SavedEffect se = null;
 		try {
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
 					serialized));
 			se = (SavedEffect) in.readObject();
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		scale = se.lscale;
 		radius = se.lradius;
 		rnd_pos_max = se.lrnd_pos_max;
 		rnd_size_max = se.lrnd_size_max;
 		blur_radius = se.lblur_radius;
 		satur = se.lsatur;
 		contrast = se.lcontrast;
 		brightness = se.lbrightness;
 		opacity = se.lopacity;
 		isOnlyBorder = se.lisOnlyBorder;
 		sBorderWidth = se.lsBorderWidth;
 		sOnlyBorderIndex = se.lsOnlyBorderIndex;
 		return true;
 	}
 
 	@Override
 	protected int getColor() {
 		return Color.WHITE;
 	}
 }
