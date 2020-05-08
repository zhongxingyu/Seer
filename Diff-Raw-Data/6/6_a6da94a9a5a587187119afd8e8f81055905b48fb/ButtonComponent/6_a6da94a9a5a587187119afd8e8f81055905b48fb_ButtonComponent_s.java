 package mobi.monaca.framework.nativeui.component;
 
 import static mobi.monaca.framework.nativeui.UIUtil.createBitmapWithColorFilter;
 import static mobi.monaca.framework.nativeui.UIUtil.updateJSONObject;
 
 import java.io.IOException;
 
 import mobi.monaca.framework.nativeui.ComponentEventer;
 import mobi.monaca.framework.nativeui.DefaultStyleJSON;
 import mobi.monaca.framework.nativeui.NonScaleBitmapDrawable;
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.UIUtil;
 import mobi.monaca.framework.nativeui.UIValidator;
 import mobi.monaca.framework.nativeui.component.view.MonacaButton;
 import mobi.monaca.framework.nativeui.exception.NativeUIException;
 import mobi.monaca.framework.nativeui.exception.NativeUIIOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffColorFilter;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.graphics.drawable.StateListDrawable;
 import android.text.TextUtils;
 import android.view.View;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 
 public class ButtonComponent extends ToolbarComponent {
 
 	protected FrameLayout layout;
 	protected MonacaButton button;
 	protected MonacaImageButton imageButton;
 	protected ComponentEventer eventer;
 	protected static final String[] BUTTON_VALID_KEYS = { "component", "style", "iosStyle", "androidStyle", "id", "event" };
 	protected static final String[] STYLE_VALID_KEYS = { "visibility", "disable", "opacity", "backgroundColor", "activeTextColor", "textColor", "image", "innerImage", "text" };
 
 	public ButtonComponent(UIContext context, JSONObject buttonJSON) throws NativeUIException, JSONException {
 		super(context, buttonJSON);
 		UIValidator.validateKey(context, getComponentName() + " style", style, getStyleValidKeys());
 		
 		buildEventer();
 		initView();
 	}
 	
 	// to be overriden by BackButton Component
 	protected String[] getStyleValidKeys(){
 		return STYLE_VALID_KEYS;
 	}
 
 	private void buildEventer() throws NativeUIException, JSONException {
		this.eventer = new ComponentEventer(uiContext, getComponentJSON().optJSONObject("event"));
 	}
 
 	public ComponentEventer getUIEventer() {
 		return eventer;
 	}
 
 	protected void initView() throws NativeUIException {
 		layout = new FrameLayout(uiContext);
 		layout.setClickable(true);
 
 		button = new MonacaButton(uiContext);
 		button.getButton().setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				eventer.onTap();
 			}
 		});
 		button.getInnerImageButton().setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				eventer.onTap();
 			}
 		});
 
 		imageButton = new MonacaImageButton(uiContext);
 		imageButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				eventer.onTap();
 			}
 		});
 
 		layout.addView(button);
 		layout.addView(imageButton);
 
 		style();
 	}
 
 	public View getView() {
 		return layout;
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		imageButton.setBackgroundDrawable(null);
 	}
 
 	/**
 	 * visibility: true / false [bool] (default: true) disable: true / false
 	 * [bool] (default: false) opacity: 0.0～1.0 [float] (default: 1.0)
 	 * backgroundColor: #000000 [string] (default: #000000) activeColor: #000000
 	 * [string] (default: #0000FF) textColor: #000000 [string] (default:
 	 * #FFFFFF) image: hoge.png (このファイルからみたときの相対パス) [string] text: テキスト [string]
 	 * @throws NativeUIException 
 	 */
 	protected void style() throws NativeUIException {
 		if (style.optString("image").length() > 0) {
 			button.setVisibility(View.GONE);
 			imageButton.setVisibility(View.VISIBLE);
 			styleImageButton();
 		} else {
 			button.setVisibility(View.VISIBLE);
 			imageButton.setVisibility(View.GONE);
 			styleButton();
 		}
 	}
 
 	protected void styleButton() throws NativeUIException {
 		button.updateStyle(style);
 		button.style();
 
 		Bitmap bitmap = readImage("innerImage");
 		if( bitmap != null ) {
 			ImageButton imageButton = button.getInnerImageButton();
 			imageButton.setImageBitmap(bitmap);
 		}
 	}
 
 	protected void styleImageButton() throws NativeUIIOException {
 		imageButton.setVisibility(style.optBoolean("visibility", true) ? View.VISIBLE : View.GONE);
 		imageButton.setBackgroundColor(0);
 		imageButton.setEnabled(!style.optBoolean("disable", false));
 
 		Bitmap bitmap = readImage("image");
 		if(bitmap != null){
 			if (imageButton.getHeight() > 0) {
 				int scaledHeight = imageButton.getHeight();
 				bitmap = UIUtil.resizeBitmap(bitmap, scaledHeight);
 			}
 			Drawable drawable = new ImageButtonDrawable(new NonScaleBitmapDrawable(bitmap));
 
 			imageButton.setBackgroundDrawable(drawable);
 			imageButton.setPadding(0, 0, 0, 0);
 		}else{
 			imageButton.setBackgroundDrawable(null);
 		}		
 	}
 
 	@Override
 	public void updateStyle(JSONObject update) throws NativeUIException {
 		updateJSONObject(style, update);
 		style();
 	}
 
 	public static class ButtonDrawable extends LayerDrawable {
 		protected int backgroundColor, pressedBackgroundColor;
 
 		private ButtonDrawable(Drawable drawable) {
 			super(new Drawable[] { drawable });
 		}
 
 		@Override
 		protected boolean onStateChange(int[] states) {
 			for (int state : states) {
 				if (state == android.R.attr.state_pressed) {
 					super.setColorFilter(0x66000000, Mode.MULTIPLY);
 				} else {
 					super.clearColorFilter();
 				}
 			}
 			return super.onStateChange(states);
 		}
 
 		@Override
 		public boolean isStateful() {
 			return true;
 		}
 
 	}
 
 	public class ImageButtonDrawable extends StateListDrawable {
 		protected int backgroundColor, pressedBackgroundColor;
 
 		private ImageButtonDrawable(Drawable drawable) {
 			super();
 			Drawable pressed = new BitmapDrawable(uiContext.getResources(), createBitmapWithColorFilter(drawable, new PorterDuffColorFilter(0x66000000,
 					PorterDuff.Mode.MULTIPLY)));
 			Drawable disabled = new BitmapDrawable(uiContext.getResources(), createBitmapWithColorFilter(drawable, new PorterDuffColorFilter(0x66000000,
 					PorterDuff.Mode.MULTIPLY)));
 
 			addState(new int[] { android.R.attr.state_pressed }, pressed);
 			addState(new int[] { -android.R.attr.state_enabled }, disabled);
 			addState(new int[0], drawable.mutate());
 		}
 	}
 
 	public class MonacaImageButton extends Button {
 
 		public MonacaImageButton(Context context) {
 			super(context);
 		}
 
 		@Override
 		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 			Bitmap bitmap;
 			try {
 				bitmap = readImage("image");
 				if (bitmap != null) {
 					int width = bitmap.getWidth();
 					int height = bitmap.getHeight();
 
 					int resolvedWidth = resolveSize(width, widthMeasureSpec);
 					int resolvedHeight = resolveSize(height, heightMeasureSpec);
 
 					setMeasuredDimension(resolvedWidth, resolvedHeight);
 				} else {
 					super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 				}
 			} catch (NativeUIIOException e) {
 				e.printStackTrace();
 				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 			}
 		}
 
 		@Override
 		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 			try {
 				resizeImage();
 			} catch (NativeUIIOException e) {
 				e.printStackTrace();
 			}
 			super.onSizeChanged(w, h, oldw, oldh);
 		}
 
 		private void resizeImage() throws NativeUIIOException {
 			Bitmap bitmap = readImage("image");
 			if (bitmap != null) {
 				if (getMeasuredHeight() > 0) {
 					int scaledHeight = getMeasuredHeight();
 					bitmap = UIUtil.resizeBitmap(bitmap, scaledHeight);
 				}
 				Drawable drawable = new ImageButtonDrawable(new NonScaleBitmapDrawable(bitmap));
 
 				imageButton.setBackgroundDrawable(drawable);
 				imageButton.setPadding(0, 0, 0, 0);
 			} else {
 				imageButton.setBackgroundDrawable(null);
 			}
 		}
 	}
 	
 	private Bitmap readImage(String imageKeyName) throws NativeUIIOException {
 		String imagePath = style.optString(imageKeyName);
 		if (!TextUtils.isEmpty(imagePath)) {
 			try {
 				Bitmap bitmap = uiContext.readScaledBitmap(imagePath);
 				return bitmap;
 			} catch (IOException e) {
 				NativeUIIOException exception = new NativeUIIOException(getComponentName() + " style", imageKeyName, imagePath, e.getMessage());
 				throw exception;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String[] getValidKeys() {
 		return BUTTON_VALID_KEYS;
 	}
 
 	@Override
 	public String getComponentName() {
 		return "Button";
 	}
 
 	@Override
 	public JSONObject getDefaultStyle() {
 		return DefaultStyleJSON.button();
 	}
 
 }
