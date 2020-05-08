 package com.aragaer.reminder;
 
 import java.io.ByteArrayInputStream;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.GradientDrawable;
 
 public class Bitmaps {
 	public static final int COLOR_WHITE = 0;
 	public static final int COLOR_BLUE = 1;
 	public static final int COLOR_PURPLE = 2;
 	public static final int COLOR_GREEN = 3;
 	public static final int COLOR_YELLOW = 4;
 	public static final int COLOR_RED = 5;
 
 	public static final int N_COLORS = 6;
 
 	static final int colors[] = { Color.WHITE,
 			Color.argb(0xff, 0x33, 0xb5, 0xe5),
 			Color.argb(0xff, 0xaa, 0x66, 0xcc),
 			Color.argb(0xff, 0x99, 0xcc, 0x00),
 			Color.argb(0xff, 0xff, 0xbb, 0x33),
 			Color.argb(0xff, 0xff, 0x44, 0x44), };
 	static final float filters[][] = {
 			{ 0, 0, 0, 0, 0xFF, 0, 0, 0, 0, 0xFF, 0, 0, 0, 0, 0xFF, .8f, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0x33, 0, 0, 0, 0, 0xB5, 0, 0, 0, 0, 0xE5, 1, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0xAA, 0, 0, 0, 0, 0x66, 0, 0, 0, 0, 0xCC, 1, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0x99, 0, 0, 0, 0, 0xCC, 0, 0, 0, 0, 0x00, 1, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0xFF, 0, 0, 0, 0, 0xBB, 0, 0, 0, 0, 0x33, 1, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0xFF, 0, 0, 0, 0, 0x44, 0, 0, 0, 0, 0x44, 1, 0, 0, 0, 0x00 }, };
 	static final float filters_inv[][] = {
 			{ 0, 0, 0, 0, 0x00, 0, 0, 0, 0, 0x00, 0, 0, 0, 0, 0x00, .6f, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0x00, 0, 0, 0, 0, 0x99, 0, 0, 0, 0, 0xCC, 1, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0x99, 0, 0, 0, 0, 0x33, 0, 0, 0, 0, 0xCC, 1, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0x66, 0, 0, 0, 0, 0x99, 0, 0, 0, 0, 0x00, 1, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0xFF, 0, 0, 0, 0, 0x88, 0, 0, 0, 0, 0x00, 1, 0, 0, 0, 0x00 },
 			{ 0, 0, 0, 0, 0xCC, 0, 0, 0, 0, 0x00, 0, 0, 0, 0, 0x00, 1, 0, 0, 0, 0x00 }, };
 	static final Paint paints[] = new Paint[N_COLORS];
 	static final Paint darker[] = new Paint[N_COLORS];
 	static {
 		for (int i = 0; i < N_COLORS; i++) {
 			paints[i] = new Paint(0x07);
 			paints[i].setColorFilter(filter(i, false));
 			darker[i] = new Paint(0x07);
 			darker[i].setColorFilter(filter(i, true));
 		}
 	}
 
 	static ColorMatrixColorFilter filter(int color_num, boolean invert) {
 		return new ColorMatrixColorFilter((invert ? filters_inv : filters)[color_num]);
 	}
 
 	static public Bitmap list_bmp(Context ctx, int extra, boolean invert) {
 		Resources r = ctx.getResources();
 		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
 				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
 		Bitmap t = BitmapFactory.decodeResource(r, R.drawable.list);
 		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
 		Canvas c = new Canvas(b);
 		c.drawBitmap(t, 0, 0, (invert ? darker : paints)[0]);
 
 		GradientDrawable d = border(r.getDimensionPixelSize(R.dimen.border_width),
				invert ? Color.argb(255, 0x99, 0xcc, 0x00)
 						: colors[COLOR_GREEN]);
 		d.setBounds(0, 0, size, size);
 		d.draw(c);
 		if (extra > 0) {
 			Paint p = new Paint(0x07);
 			p.setTextSize(size / 5);
 			p.setColor(Color.WHITE);
 			p.setTypeface(Typeface.DEFAULT_BOLD);
 			String draw = String.format("+%d", extra);
 			Rect bounds = new Rect();
 			p.getTextBounds(draw, 0, draw.length(), bounds);
 			RectF border = new RectF(bounds);
 			border.inset(-2, -2);
 			border.offsetTo(size * 0.9f - bounds.width() - 2, size * 0.9f
 					- bounds.height() - 2);
 			p.setColor(Color.argb(200, 255, 136, 0));
 			p.setStyle(Style.FILL);
 			c.drawRoundRect(border, size / 20, size / 20, p);
 			p.setColor(Color.argb(255, 255, 136, 0));
 			p.setStyle(Style.STROKE);
 			c.drawRoundRect(border, size / 20, size / 20, p);
 			p.setColor(Color.WHITE);
 			c.drawText(draw, size * 0.9f - bounds.width(), size * 0.9f, p);
 		}
 		return b;
 	}
 
 	static public Bitmap draw_char(String str, int size) {
 		Bitmap b = Bitmap.createBitmap(size, size, Config.RGB_565);
 		Canvas c = new Canvas(b);
 		Paint p = new Paint(0x07);
 		p.setTextSize(size);
 		p.setColor(Color.WHITE);
 		Rect bounds = new Rect();
 		p.getTextBounds(str, 0, str.length(), bounds);
 		c.drawText(str, size / 2 - bounds.centerX(),
 				size / 2 - bounds.centerY(), p);
 		return b;
 	}
 
 	static public Bitmap add_new_bmp(Context ctx, boolean invert) {
 		Resources r = ctx.getResources();
 		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
 				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
 		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
 		Canvas c = new Canvas(b);
 		GradientDrawable d = border(r.getDimensionPixelSize(R.dimen.border_width),
				invert ? Color.argb(255, 0x99, 0xcc, 0x00)
 						: colors[COLOR_GREEN]);
 		d.setBounds(0, 0, size, size);
 		d.draw(c);
 		c.drawBitmap(draw_char("+", size), 0, 0, (invert ? darker : paints)[0]);
 		return b;
 	}
 
 	static public Bitmap memo_bmp(Context ctx, ReminderItem item, boolean invert) {
 		Resources r = ctx.getResources();
 		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
 				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
 		Bitmap glyph = BitmapFactory.decodeByteArray(item.glyph_data, 0,
 				item.glyph_data.length);
 		Bitmap result = Bitmap.createScaledBitmap(glyph, size, size, true);
 
 		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
 		Canvas c = new Canvas(b);
 		c.drawBitmap(result, 0, 0, (invert ? darker : paints)[item.color]);
 		return b;
 	}
 
 	static public BitmapDrawable memo_drawable(Context ctx, ReminderItem item, boolean invert) {
 		BitmapDrawable result = new BitmapDrawable(ctx.getResources(), new ByteArrayInputStream(item.glyph_data));
 		result.setColorFilter(filter(item.color, invert));
 		return result;
 	}
 
 	static GradientDrawable border(int stroke, int color) {
 		GradientDrawable result = new GradientDrawable();
 		result.setCornerRadius(7);
 		result.setSize(60, 60);
 		result.setColor(Color.TRANSPARENT);
 		result.setStroke(stroke, color);
 		return result;
 	}
 }
