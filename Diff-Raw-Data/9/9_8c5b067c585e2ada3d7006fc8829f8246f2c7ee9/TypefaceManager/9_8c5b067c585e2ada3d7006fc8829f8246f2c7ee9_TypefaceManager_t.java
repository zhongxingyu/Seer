 package com.gloria.offlineexperiments.ui.fonts;
 
 import java.util.Hashtable;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 public enum TypefaceManager {
 	INSTANCE;
 	
 	public static final String VERDANA = "fonts/verdana.ttf";
 	public static final String VERDANA_BOLD = "fonts/verdanab.ttf";
 	
 	private final Hashtable<String, Typeface> typefaces = new Hashtable<String, Typeface>();
 	
 	public synchronized Typeface getTypeface(Context context, String assetsPath) {
 		Typeface typeface = typefaces.get(assetsPath);
 		if (typeface == null) {
 			typeface = Typeface.createFromAsset(context.getAssets(), assetsPath);
 			typefaces.put(assetsPath, typeface);
 		}
 		return typeface;
 	}
 	
 	public void applyTypefaceToAllViews(Activity activity, Typeface typeface) {
 		View rootView = activity.findViewById(android.R.id.content);
 		if (rootView != null && rootView instanceof ViewGroup) {
 			applyTypefaceToAllViews(((ViewGroup)rootView).getChildAt(0), typeface);
 		}
 	}
 	
 	public void applyTypefaceToAllViews(View rootView, Typeface typeface) {
 		if (rootView instanceof ViewGroup) {
 			ViewGroup viewGroup = (ViewGroup) rootView;
 			for (int i = 0; i < viewGroup.getChildCount(); i ++) {
 				applyTypefaceToAllViews(viewGroup.getChildAt(i), typeface);
 			}
 		} else if (rootView instanceof TextView) {
 			TextView textView = (TextView) rootView;
			if (textView.getTypeface() != typeface) {
				textView.setTypeface(typeface);
			}
 		} else if (rootView instanceof Button) {
 			Button button = (Button) rootView;
			if (button.getTypeface() != typeface) {
				button.setTypeface(typeface);
			}
 		}
 	}
 }
