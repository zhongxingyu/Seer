 package edu.umd.umiacs.newsstand;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class PopupPanel extends Overlay {
     private final MapView _mapView;
     private final Context _ctx;
     private final TopStories _ts;
 
     private final View popup;
     private boolean isVisible=false;
 
     private final int POPUP_OFFSET = 15;
     private final int MARKER_HEIGHT = 40;
     private final int RECT_MARGIN = 10;
     private final int TEXT_MARGIN = 20;
     private final int ARROW_HEIGHT = 5;
     private final int ARROW_WIDTH = 32;
 
     private int mArrowOffset;
     private boolean mAlignTop;
 
     private Paint innerPaint, borderPaint, textPaint, arrowPaint;
 
     private String mGazId;
 
     PopupPanel(Context ctx, int layout, MapView map_view, int mode) {
     	if(mode  == 0){
     		_ctx = (NewsStand)ctx;
     		_ts = null;
     	}
     	else{
     		_ctx = null;
     		_ts = (TopStories)ctx;
     	}
         _mapView = map_view;
         ViewGroup parent=(ViewGroup)_mapView.getParent();
 
         LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(
                 Context.LAYOUT_INFLATER_SERVICE);
         popup=inflater.inflate(layout, parent, false);
 
         popup.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
             	Context cur = _ctx;
             	if (cur == null)
             		cur = _ts;
             	
                     Intent i = new Intent(cur, ClusterViewer.class);
                     i.putExtra("gaz_id", mGazId);
                     cur.startActivity(i);
             }
         });
     }
 
 	View getView() {
         return(popup);
     }
 
     public void show(boolean alignTop, Point marker_pos ) {
         mAlignTop = alignTop;
         mArrowOffset = marker_pos.x;
 
         // Show text view
         RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(
                 RelativeLayout.LayoutParams.WRAP_CONTENT,
                 RelativeLayout.LayoutParams.WRAP_CONTENT);
 
        //if (alignTop) {
        //    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //    lp.setMargins(TEXT_MARGIN, 0, TEXT_MARGIN, (_mapView.getHeight() - marker_pos.y) + MARKER_HEIGHT + POPUP_OFFSET);
        //}
        //else {
        	mAlignTop = false;
             lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
             lp.setMargins(TEXT_MARGIN, marker_pos.y + POPUP_OFFSET, TEXT_MARGIN, 0);
        //}
 
         hide();
 
         ViewGroup parent=(ViewGroup)_mapView.getParent();
         parent.addView(popup, lp);
         isVisible=true;
 
         if (_mapView.getOverlays().size() >= 2) {
             _mapView.getOverlays().set(1, this);
         }
         else {
             _mapView.getOverlays().add(this);
         }
     }
 
     @Override
     public void draw(Canvas canvas, MapView mapView, boolean shadow) {
         if (isVisible) {
             View v = getView();
 
             //  Setup the info window with the right size & location
             int width = v.getWidth() + 2 * RECT_MARGIN;
             int height = v.getHeight() + 2 * RECT_MARGIN;
             int left = v.getLeft() - RECT_MARGIN;
             int top = v.getTop() - RECT_MARGIN;
             int arrow_left = mArrowOffset - ARROW_WIDTH / 2;
             int arrow_right = mArrowOffset + ARROW_WIDTH / 2;
             int arrow_top;
             int arrow_bottom;
 
             if (mAlignTop) {
                 arrow_top = v.getBottom() + RECT_MARGIN - ARROW_HEIGHT;
                 arrow_bottom = v.getBottom() + RECT_MARGIN;
             }
             else {
                 arrow_top = v.getTop() - RECT_MARGIN;
                 arrow_bottom = v.getTop() - RECT_MARGIN + ARROW_HEIGHT;
             }
 
             arrow_left = (arrow_left < left) ? left : arrow_left;
             arrow_right = (arrow_right > (left + width)) ? (left + width) : arrow_right;
 
             RectF infoWindowRect = new RectF(0, 0, width, height);
             infoWindowRect.offset(left, top);
 
             //  Draw inner info window
             canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());
 
             //  Draw border for info window
             canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());
 
             //  Draw arrow pointing to relavent location
             //  (the arrow is actually a filled rectangle adjacent to the
             //  cluster marker)
             canvas.drawRect(arrow_left, arrow_top, arrow_right, arrow_bottom, getArrowPaint());
         }
     }
 
 
     public void display(GeoPoint marker_loc, String headline, String snippet, String gaz_id) {
 
         Point pt=_mapView.getProjection().toPixels(marker_loc, null);
 
         View view = getView();
 
         snippet = str_replace(snippet, "<span class='georef'>", "<b><font color=\"red\">");
         snippet = str_replace(snippet, "</span>", "</font></b>");
         snippet = str_replace(snippet, "&mdash;", "-");
         snippet = str_replace(snippet, "&ldquo;", "\"");
         snippet = str_replace(snippet, "&rdquo;", "\"");
         snippet = str_replace(snippet, "&quot;", "\"");
         snippet = str_replace(snippet, "&#39;", "'");
         snippet = str_replace(snippet, "&#x2029;", "");
 
         //headline = str_replace(headline, "&amp;", "&");
 
         ((TextView)view.findViewById(R.id.headline)).setText(headline);
         //((TextView)view.findViewById(R.id.snippet)).setText(snippet);
         
         if (snippet!=null)
         	((TextView)view.findViewById(R.id.snippet)).setText(Html.fromHtml(snippet));
 
         mGazId = gaz_id;
 
        show(pt.y*2>_mapView.getHeight(), pt);
     }
 
     // Naive unescaping of HTML...
     // Modeled after:
     // http://java.sun.com/developer/technicalArticles/releases/1.4regex/
     public String str_replace(String the_string, String from_what, String to_what) {
     	
     	if (the_string==null)
     		return the_string;
     	
         Pattern p = Pattern.compile(from_what);
         Matcher m = p.matcher(the_string);
 
         StringBuffer sb = new StringBuffer();
         boolean result = m.find();
         while (result) {
             m.appendReplacement(sb, to_what);
             result = m.find();
         }
 
         m.appendTail(sb);
 
         return sb.toString();
     }
 
     public void hide() {
         if (isVisible) {
             isVisible=false;
             ((ViewGroup)popup.getParent()).removeView(popup);
         }
     }
 
     public Paint getInnerPaint() {
         if ( innerPaint == null) {
             innerPaint = new Paint();
             innerPaint.setARGB(220, 75, 75, 75); //gray
             innerPaint.setAntiAlias(true);
         }
         return innerPaint;
     }
 
     public Paint getBorderPaint() {
         if ( borderPaint == null) {
             borderPaint = new Paint();
             borderPaint.setARGB(255, 255, 255, 255);
             borderPaint.setAntiAlias(true);
             borderPaint.setStyle(Style.STROKE);
             borderPaint.setStrokeWidth(2);
         }
         return borderPaint;
     }
 
     public Paint getArrowPaint() {
         if ( arrowPaint == null) {
             arrowPaint = new Paint();
             arrowPaint.setARGB(255, 255, 255, 255);
             arrowPaint.setAntiAlias(true);
         }
         return arrowPaint;
     }
 
     public Paint getTextPaint() {
         if ( textPaint == null) {
             textPaint = new Paint();
             textPaint.setARGB(255, 255, 255, 255);
             textPaint.setAntiAlias(true);
         }
         return textPaint;
     }
 
 }
