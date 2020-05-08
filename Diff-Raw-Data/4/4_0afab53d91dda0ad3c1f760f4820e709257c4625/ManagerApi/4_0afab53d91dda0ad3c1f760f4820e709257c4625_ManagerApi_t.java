 package org.metawatch.manager.emailhack;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.TextPaint;
 import android.util.Log;
 
 public class ManagerApi {
 	private static final String[] IDS = new String[] {
 		"emailHackMWM_16_16",
 		"emailHackMWM_24_32"
 	};
 	private static final String[] DESCS = new String[] {
 		"EmailHack (16x16)",
 		"EmailHack (24x32)"
 	};
 	private static final int[][] SIZES = new int[][] {
 		new int[] { 16, 16 },
 		new int[] { 24, 32 }
 	};
 
 	private static Typeface typefaceCaps = null;
 	private static Typeface typefaceNumerals = null;
 	
 	static void updateWidget(Context context, boolean generateAll) {
 		if (DatabaseService.isCacheOld(context)) {
 			if (!DatabaseService.isRunning()) context.startService(new Intent(context, DatabaseService.class));
 			DatabaseService.refreshUnreadCount(context, true);
 			// Will trigger an update() by itself.
 			
 		} else {
 			int count = DatabaseService.getUnreadCount(context);
 			
 			for (int i = 0; i < IDS.length; i++) {
 				boolean active = (generateAll || IntentReceiver.shown_widgets == null || IntentReceiver.shown_widgets.contains(IDS[i]));
 				if (active) {
 					genWidget(context, i, count);
 				}
 			}
 		}
 	}
 
 	private synchronized static void genWidget(Context context, int index, int count) {
 		if (Main.log) Log.d(Main.TAG, "genWidget() start - "+IDS[index]);
 		
 		if (typefaceCaps==null) {
 			typefaceCaps = Typeface.createFromAsset(context.getAssets(), "metawatch_8pt_5pxl_CAPS.ttf");
 		}
 		if (typefaceNumerals==null) {
 			typefaceNumerals = Typeface.createFromAsset(context.getAssets(), "metawatch_8pt_5pxl_Numerals.ttf");
 		}
 		
 		TextPaint textPaint = new TextPaint();
 		textPaint.setColor(Color.BLACK);
 		textPaint.setTextSize(8);
 		textPaint.setTextAlign(Align.CENTER);
 		
 		int width = SIZES[index][0];
 		int height = SIZES[index][1];
 		
 		String text;
 		Typeface smallTypeface = typefaceNumerals;
 		// Stop the text being too wide for the widget
 		if (height==16 && count>1999) {
 			text="999+";
 		} else {
 			if (count<0) {
 				text = "err";
 				smallTypeface = typefaceCaps;
 			} else {
 				text = Integer.toString(count);
 			}
 		}
 		
 		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
 		Canvas canvas = new Canvas(bitmap);
 		canvas.drawColor(Color.WHITE);
 		
 		if(height==16) {
 			textPaint.setTypeface(smallTypeface);
 			canvas.drawBitmap(loadBitmapFromAssets(context, "idle_mail_10.bmp"), 2, 0, null);
 			canvas.drawText(text, 8, 15, textPaint);
 		}  else {
 			textPaint.setTypeface(typefaceCaps);
 			canvas.drawBitmap(loadBitmapFromAssets(context, "idle_mail.bmp"), 0, 3, null);
 			canvas.drawText(text, 12, 30, textPaint);
 		}
 		 	
 		Intent i = createUpdateIntent(bitmap, IDS[index], DESCS[index], 1);
 		context.sendBroadcast(i);
 	
 		if (Main.log) Log.d(Main.TAG, "genWidget() end");
 	}
 
 	public static void sendNotification(Context context, int count) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		int buzzes = Integer.parseInt(prefs.getString("notifyBuzzes", "3"));
 		
 		Bitmap icon = loadBitmapFromAssets(context, "email.bmp");
 		int pixelArray[] = new int[icon.getWidth() * icon.getHeight()];
 		icon.getPixels(pixelArray, 0, icon.getWidth(), 0, 0, icon.getWidth(), icon.getHeight());
 		
 		Intent broadcast = new Intent("org.metawatch.manager.NOTIFICATION");
 		Bundle b = new Bundle();
 		b.putString("title", "Email");
		b.putString("text", count + " new " + (count > 1 ? "messages" : "message"));
 		b.putIntArray("icon", pixelArray);
 		b.putInt("iconWidth", icon.getWidth());
 		b.putInt("iconHeight", icon.getHeight());
 		b.putBoolean("sticky", false);
 	
 		if (buzzes > 0) {
 	    	b.putInt("vibrate_on", 500);
 	    	b.putInt("vibrate_off", 500);
 	    	b.putInt("vibrate_cycles", buzzes);
 		}
 	
 		broadcast.putExtras(b);
 	
 		if (Main.log) Log.d(Main.TAG, "Sending notification");
 		context.sendBroadcast(broadcast);
 	}
 
 	/**
 	 * @param bitmap Widget image to send
 	 * @param id ID of this widget - should be unique, and sensibly identify
 	 *        the widget
 	 * @param description User friendly widget name (will be displayed in the
 	 * 		  widget picker)
 	 * @param priority A value that indicates how important this widget is, for
 	 * 		  use when deciding which widgets to discard.  Lower values are
 	 *        more likely to be discarded.
 	 * @return Filled-in intent, ready for broadcast.
 	 */
 	private static Intent createUpdateIntent(Bitmap bitmap, String id, String description, int priority) {
 		int pixelArray[] = new int[bitmap.getWidth() * bitmap.getHeight()];
 		bitmap.getPixels(pixelArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
 	
 		Intent intent = new Intent("org.metawatch.manager.WIDGET_UPDATE");
 		Bundle b = new Bundle();
 		b.putString("id", id);
 		b.putString("desc", description);
 		b.putInt("width", bitmap.getWidth());
 		b.putInt("height", bitmap.getHeight());
 		b.putInt("priority", priority);
 		b.putIntArray("array", pixelArray);
 		intent.putExtras(b);
 	
 		return intent;
 	}
 
 	private static Bitmap loadBitmapFromAssets(Context context, String path) {
 		try {
 			InputStream inputStream = context.getAssets().open(path);
 	        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
 	        inputStream.close();
 	        return bitmap;
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 }
