 package mobi.monaca.framework.nativeui.component.view;
 
 import mobi.monaca.framework.nativeui.component.ButtonBackgroundDrawable;
 import mobi.monaca.framework.nativeui.component.Component;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.res.ColorStateList;
 import android.util.AttributeSet;
 import android.util.TypedValue;
 import android.view.View;
 import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
 import static mobi.monaca.framework.nativeui.UIUtil.*;
 
 public class MonacaTextButton extends Button {
     protected JSONObject style;
     protected Context context;
 
     public MonacaTextButton(Context context) {
         super(context);
         this.context = context;
         this.style = new JSONObject();
     }
     
     public MonacaTextButton(Context context, AttributeSet attr) {
         super(context, attr);
         this.context = context;
         this.style = buildStyleFromAttributeSet(attr);
     }
     
     protected JSONObject buildStyleFromAttributeSet(AttributeSet attr) {
         JSONObject style = new JSONObject();
         String value = attr.getAttributeValue(
                 "http://schemas.android.com/apk/res/android", "text");
         if (value != null) {
             if (value.startsWith("@")) {
                 value = context.getResources().getString(
                         Integer.parseInt(value.substring(1), 10));
             }
             try {
                 style.put("text", value);
             } catch (JSONException e) {
                 // ignore
             }
         }
 
         return style;
     }
     
     public void updateStyle(JSONObject update) {
         updateJSONObject(style, update);
         style();
     }
     
     protected void style() {
         ColorStateList textColor = new ColorStateList(new int[][] {
                 new int[] { android.R.attr.state_pressed }, new int[0] },
                 new int[] {
                         buildColor(style.optString("activeTextColor",
                                 style.optString("textColor", "#999999"))),
                         buildColor(style.optString("textColor", "#ffffff")) });
 
         ButtonBackgroundDrawable background = new ButtonBackgroundDrawable(
                 context, buildColor(style.optString("backgroundColor",
                         "#000000")));
         background.setAlpha(buildOpacity(style.optDouble("opacity", 1.0)));
         setBackgroundDrawable(new ButtonDrawable(background));
        
        setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            background.getIntrinsicHeight()
        ));
 
         setText(style.optString("text", ""));
         setVisibility(style.optBoolean("visibility", true) ? View.VISIBLE
                 : View.GONE);
         setTextColor(textColor);
 
         setTextSize(TypedValue.COMPLEX_UNIT_PX,
                 getFontSizeFromDip(context, Component.BUTTON_TEXT_DIP));
 
         setTextColor(0xffffffff);
         setTextColor(getTextColors().withAlpha(
                 buildOpacity(style.optDouble("opacity", 1.0))));
 
         setEnabled(!style.optBoolean("disable", false));
         if (style.optBoolean("disable", false)) {
             setTextColor(0xff999999);
             setTextColor(getTextColors().withAlpha(
                     buildOpacity(style.optDouble("opacity", 1.0))));
         }
 
         setShadowLayer(1f, 0f, -1f, 0xff000000);
     }
 
 }
