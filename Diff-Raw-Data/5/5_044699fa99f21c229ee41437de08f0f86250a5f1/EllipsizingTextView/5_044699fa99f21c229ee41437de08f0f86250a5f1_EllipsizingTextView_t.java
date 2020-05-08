 package com.egeniq.widget;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.text.Layout;
 import android.text.Layout.Alignment;
 import android.text.StaticLayout;
 import android.util.AttributeSet;
 import android.widget.TextView;
 
 /**
  * Text view that ellipsizes its text based on the maximum line height.
  */
 public class EllipsizingTextView extends TextView {
     private static final String ELLIPSIS = "...";
 
     private float _lineSpacingMultiplier = 1.0f;
     private float _lineAdditionalVerticalPadding = 0.0f;
     private int _maxLines = -1;
     private int _minLines = -1;
     
     private String _drawCacheKey = null;
     private String _drawCacheText = null;
     
     private CharSequence _originalText = "";
 
     /**
      * Constructor.
      * 
      * @param context
      */
     public EllipsizingTextView(Context context) {
         super(context);
     }
 
     /**
      * Constructor.
      * 
      * @param context
      */
     public EllipsizingTextView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     /**
      * Constructor.
      * 
      * @param context
      */
     public EllipsizingTextView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     /**
      * Set line spacing.
      * 
      * @param add
      */
     @Override
     public void setLineSpacing(float additionalVerticalPadding, float spacingMultiplier) {
         _lineAdditionalVerticalPadding = additionalVerticalPadding;
         _lineSpacingMultiplier = spacingMultiplier;
         super.setLineSpacing(additionalVerticalPadding, spacingMultiplier);
     }
     
     /**
      * Set minimum lines.
      * 
      * @param minLines
      */
     @Override
     public void setMinLines(int minLines) {
         _minLines = minLines;
         super.setMinLines(minLines);
     }     
     
     /**
      * Set maximum lines.
      * 
      * @param maxLines
      */
     @Override
     public void setMaxLines(int maxLines) {
         _maxLines = maxLines;
         super.setMaxLines(maxLines);
     }    
     
     /**
      * Create static layout for the given text.
      * 
      * @param text
      * 
      * @return static text layout
      */
     private Layout _createLayout(String text) {
         return new StaticLayout(text, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(), Alignment.ALIGN_NORMAL, _lineSpacingMultiplier, _lineAdditionalVerticalPadding, false);
     }
     
     /**
      * Store the original text.
      * 
      * @param text
      * @param type
      */
     public void setText(CharSequence text, BufferType type) {
         _originalText = text;
         super.setText(text, type);
     }
 
     /**
      * Draw.
      */
     @Override
     protected void onDraw(Canvas canvas) {
        if (getHeight() <= 0.0f || getLineHeight() <= 0.0f) {
            super.onDraw(canvas);
            return;
        }
        
         int lines = (int)Math.floor((float)getHeight() / (float)getLineHeight());
         if (_maxLines > 0 && _maxLines < lines) {
             lines = _maxLines;
         }
         
         if (_minLines > 0 && _minLines > lines) {
             lines = _minLines;
         }
         
         String text = _originalText.toString();
         
         String cacheKey = lines + "#" + getWidth() + "#" + getPaddingLeft() + "#" + getPaddingRight() + "#" + _lineSpacingMultiplier + "#" + _lineAdditionalVerticalPadding + "#" + text;
         if (cacheKey.equals(_drawCacheKey)) {
             text = _drawCacheText;
         } else {
             Layout layout = _createLayout(text);
             if (layout.getLineCount() > lines) {
                 text = text.substring(0, layout.getLineEnd(lines - 1)).trim();
                 while (_createLayout(text + ELLIPSIS).getLineCount() > lines) {
                     int lastSpace = text.lastIndexOf(' ');
                     if (lastSpace < 0) {
                         break;
                     }
                     
                     text = text.substring(0, text.length() - 1);
                 }
                 
                 if (!text.equals(_originalText.toString())) {
                     text = text + ELLIPSIS;                    
                 }
             }
          
             _drawCacheKey = cacheKey;
             _drawCacheText = text;
         }
         
         super.setText(text, BufferType.NORMAL);
         super.onDraw(canvas);
     }
 }
