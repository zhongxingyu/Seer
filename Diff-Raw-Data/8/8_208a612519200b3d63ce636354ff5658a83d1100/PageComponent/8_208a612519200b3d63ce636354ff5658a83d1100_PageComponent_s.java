 package mobi.monaca.framework.nativeui.component;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Map;
 
 import mobi.monaca.framework.MonacaApplication;
 import mobi.monaca.framework.nativeui.ComponentEventer;
 import mobi.monaca.framework.nativeui.DefaultStyleJSON;
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.UIEventer;
 import mobi.monaca.framework.nativeui.UIGravity;
 import mobi.monaca.framework.nativeui.UIUtil;
 import mobi.monaca.framework.nativeui.UIValidator;
 import mobi.monaca.framework.nativeui.container.TabbarContainer;
 import mobi.monaca.framework.nativeui.container.ToolbarContainer;
 import mobi.monaca.framework.nativeui.exception.ConversionException;
 import mobi.monaca.framework.nativeui.exception.DuplicateIDException;
 import mobi.monaca.framework.nativeui.exception.InvalidValueException;
 import mobi.monaca.framework.nativeui.exception.MenuNameNotDefinedInAppMenuFileException;
 import mobi.monaca.framework.nativeui.exception.NativeUIException;
 import mobi.monaca.framework.nativeui.exception.NativeUIIOException;
 import mobi.monaca.framework.nativeui.exception.RequiredKeyNotFoundException;
 import mobi.monaca.framework.nativeui.menu.MenuRepresentation;
 import mobi.monaca.framework.util.MyLog;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Shader.TileMode;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.text.TextUtils;
 import android.util.FloatMath;
 import android.view.Gravity;
 import android.view.View;
 
 /**
  * Used for manipulating MonacaPageActivity background style
  * 
  */
 public class PageComponent extends Component {
 
 	private static final String TAG = PageComponent.class.getSimpleName();
 	private LayerDrawable mLayeredBackgroundDrawable;
 	private PageOrientation mScreenOrientation = PageOrientation.INHERIT;
 	protected Component topComponent;
 	protected Component bottomComponent;
 	protected ComponentEventer mBackButtonEventer;
 	public UIEventer eventer;
 	public String menuName;
 
 	protected static String[] validKeys = { "top", "bottom", "event", "style", "iosStyle", "androidStyle", "menu", "id" };
 	protected static String[] styleValidKeys = { "backgroundColor", "backgroundImage", "backgroundSize", "backgroundRepeat", "backgroundPosition",
 			"screenOrientation" };
 
 	@Override
 	public String[] getValidKeys() {
 		return validKeys;
 	}
 
 	public PageComponent(UIContext uiContext, JSONObject pageJSON) throws NativeUIException, JSONException {
 		super(uiContext, pageJSON);
 		uiContext.setPageComponent(this);
 		UIValidator.validateKey(uiContext, "Page's style", style, styleValidKeys);
 
 		JSONObject event = getComponentJSON().optJSONObject("event");
 		if (event != null) {
 			eventer = new UIEventer(uiContext, getComponentJSON().optJSONObject("event"));
 		}
 		menuName = getComponentJSON().optString("menu");
 		if (!TextUtils.isEmpty(menuName)) {
 			MonacaApplication app = (MonacaApplication) uiContext.getPageActivity().getApplication();
 			MenuRepresentation menuRepresentation = app.findMenuRepresentation(menuName);
 			if (menuRepresentation == null) {
 				throw new MenuNameNotDefinedInAppMenuFileException(getComponentName(), menuName);
 			}
 		}
 		style();
 		buildChildren();
 	}
 	
 	public ComponentEventer getBackButtonEventer() {
 		return mBackButtonEventer;
 	};
 	
 	public void setBackButtonEventer(ComponentEventer mBackButtonEventer) {
 		this.mBackButtonEventer = mBackButtonEventer;
 	}
 
 	public Map<String, Component> getComponentIDsMap(){
 		return uiContext.getComponentIDsMap();
 	}
 	
 	protected void addIDtoComponentIDsMap() throws DuplicateIDException {
 		// this is the top component -> a new of object of this page should clear old ids of previous
 		uiContext.getComponentIDsMap().clear();
 		super.addIDtoComponentIDsMap();
 	}
 
 	private static final String[] TOP_CONTAINER_VALID_VALUES = { "toolbar" };
 	private static final String[] BOTTOM_CONTAINER_VALID_VALUES = { "toolbar, tabbar" };
 
 	private void buildChildren() throws NativeUIException, JSONException   {
 		JSONObject topJSON = getComponentJSON().optJSONObject("top");
 		if (topJSON != null) {
 			String containerType = topJSON.optString("container");
 			if (!TextUtils.isEmpty(containerType)) {
 				if (containerType.equalsIgnoreCase("toolbar")) {
 					topComponent = new ToolbarContainer(uiContext, topJSON, true);
 				} else {
 					InvalidValueException exception = new InvalidValueException("Page top", "container", containerType, TOP_CONTAINER_VALID_VALUES);
 					throw exception;
 				}
 			} else {
 				RequiredKeyNotFoundException exception = new RequiredKeyNotFoundException("top", "container");
 				throw exception;
 			}
 		}
 
 		JSONObject bottomJSON = getComponentJSON().optJSONObject("bottom");
 		if (bottomJSON != null) {
 			String containerType = bottomJSON.optString("container");
 			if (!TextUtils.isEmpty(containerType)) {
 				if (containerType.equalsIgnoreCase("toolbar")) {
 					bottomComponent = new ToolbarContainer(uiContext, bottomJSON, false);
 				} else if (containerType.equalsIgnoreCase("tabbar")) {
 					bottomComponent = new TabbarContainer(uiContext, bottomJSON);
 				} else {
 					InvalidValueException exception = new InvalidValueException("Page bottom", "container", containerType, BOTTOM_CONTAINER_VALID_VALUES);
 					UIValidator.reportException(uiContext, exception);
 				}
 			} else {
 				RequiredKeyNotFoundException exception = new RequiredKeyNotFoundException("top", "container");
 				UIValidator.reportException(uiContext, exception);
 			}
 		}
 	}
 
 	@Override
 	public View getView() {
 		return null;
 	}
 
 	public Component getTopComponent() {
 		return topComponent;
 	}
 
 	public Component getBottomComponent() {
 		return bottomComponent;
 	}
 
 	public PageOrientation getScreenOrientation() {
 		return mScreenOrientation;
 	}
 
 	public Drawable getBackgroundDrawable() {
 		return mLayeredBackgroundDrawable;
 	}
 
 	@Override
 	public void updateStyle(JSONObject update) throws InvalidValueException {
 		UIUtil.updateJSONObject(style, update);
 		style();
 		uiContext.getPageActivity().setupBackground(mLayeredBackgroundDrawable);
 	}
 
 	private void style() throws InvalidValueException {
 		ArrayList<Drawable> layerList = new ArrayList<Drawable>();
 		processScreenOrientation();
 		processPageStyleBackgroundColor(style, layerList);
 		processPageStyleBackgroundImage(style, layerList);
 		processPageStyleBackgroundRepeat(layerList);
 
 		Drawable[] layers = new Drawable[layerList.size()];
 		mLayeredBackgroundDrawable = new LayerDrawable(layerList.toArray(layers));
 	}
 
 	/*
 	 * "screenOrientation": "landscape,portrait"
 	 * "screenOrientation": "landscape"
 	 * "screenOrientation": "portrait"
 	 */
 	
 	private static final String[] SCREEN_ORIENTATION_VALID_VALUES = {"portrait", "landscape", "inherit", "portrait, landscape"};
 	private void processScreenOrientation() throws InvalidValueException {
 		String screenOrientationString = style.optString("screenOrientation").trim();
 		if(TextUtils.isEmpty(screenOrientationString)){
 			return;
 		}
 		
 		if(screenOrientationString.contains(",")){
 			// multiple values
 			if(screenOrientationString.contains("portrait") && screenOrientationString.contains("landscape")){
 				mScreenOrientation = PageOrientation.SENSOR;
 			}else{
 				raiseScreenOrietationInvalidValueException(screenOrientationString);
 			}
 		}else{
 			// single value
 			if (screenOrientationString.equalsIgnoreCase("portrait")) {
 				mScreenOrientation = PageOrientation.PORTRAIT;
 			} else if (screenOrientationString.equalsIgnoreCase("landscape")) {
 				mScreenOrientation = PageOrientation.LANDSCAPE;
 			} else if (screenOrientationString.equalsIgnoreCase("inherit")) {
 				mScreenOrientation = PageOrientation.INHERIT;
 			} else {
 				raiseScreenOrietationInvalidValueException(screenOrientationString);
 			}
 		}
 	}
 
 	private void raiseScreenOrietationInvalidValueException(String screenOrientationString) throws InvalidValueException {
 		InvalidValueException invalidValueException = new InvalidValueException(getComponentName() + " style", "screenOrientation", screenOrientationString, SCREEN_ORIENTATION_VALID_VALUES);
 		throw invalidValueException;
 	}
 
 	private static final String[] validBackgroundRepeatValues = { "repeat-x", "repeat-y", "repeat" };
 
 	private void processPageStyleBackgroundRepeat(ArrayList<Drawable> layerList) {
 		String backgroundRepeatString = style.optString("backgroundRepeat");
 		String backgroundImageString = style.optString("backgroundImage");
 		if (!backgroundRepeatString.equalsIgnoreCase("") && !backgroundImageString.equalsIgnoreCase("")) {
 
 			Drawable drawable = getTopDrawable(layerList);
 			if ((drawable instanceof BitmapDrawable) == false) {
 				return;
 			}
 
 			BitmapDrawable bitmapDrawable = (BitmapDrawable) getTopDrawable(layerList);
 			MyLog.e(TAG, "background repeat:" + backgroundRepeatString);
 			if (backgroundRepeatString.equalsIgnoreCase("repeat-x")) {
 				bitmapDrawable.setTileModeX(TileMode.REPEAT);
 			} else if (backgroundRepeatString.equalsIgnoreCase("repeat-y")) {
 				bitmapDrawable.setTileModeY(TileMode.REPEAT);
 			} else if (backgroundRepeatString.equalsIgnoreCase("repeat")) {
 				bitmapDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
 			} else if (backgroundRepeatString.equalsIgnoreCase("no-repeat")) {
 				bitmapDrawable.setTileModeXY(null, null);
 			} else {
 				InvalidValueException exception = new InvalidValueException("Page's style", "backgroundRepeat", backgroundRepeatString,
 						validBackgroundRepeatValues);
 				UIValidator.reportException(uiContext, exception);
 			}
 		}
 	}
 
 	private Drawable getTopDrawable(ArrayList<Drawable> layerList) {
 		return layerList.get(layerList.size() - 1);
 	}
 
 	private void processPageStyleBackgroundColor(JSONObject pageStyle, ArrayList<Drawable> layerList) {
 		// // background color
 		String backgroundColorString = pageStyle.optString("backgroundColor").trim();
 
 		// default
 		int color = Color.WHITE;
 		if (!backgroundColorString.equalsIgnoreCase("")) {
 			// #xxxxxx format
 			if (backgroundColorString.startsWith("#")) {
 				try {
 					color = Color.parseColor(backgroundColorString);
 				} catch (NumberFormatException e) {
 					e.printStackTrace();
 					ConversionException exception = new ConversionException("Page's style", "backgroundColor", backgroundColorString, "Color");
 					UIValidator.reportException(uiContext, exception);
 				}
 			} else {
 				ConversionException exception = new ConversionException("Page's style", "backgroundColor", backgroundColorString, "Color");
 				UIValidator.reportException(uiContext, exception);
 			}
 		}
 
 		ColorDrawable colorDrawable = new ColorDrawable(color);
 		layerList.add(colorDrawable);
 	}
 
 	private void processPageStyleBackgroundImage(JSONObject pageStyle, ArrayList<Drawable> layerList) {
 		// // background image
 		String backgroundImageFile = pageStyle.optString("backgroundImage").trim();
 		boolean shouldSkipBackgroundPosition = false;
 		if (!backgroundImageFile.equalsIgnoreCase("")) {
 			Bitmap bitmap;
 			try {
 				bitmap = uiContext.readScaledBitmap(backgroundImageFile);
 				BitmapDrawable backgroundImage = new BitmapDrawable(uiContext.getResources(), bitmap);
 				layerList.add(backgroundImage);
 				float bitmapWidth = bitmap.getWidth();
 				float bitmapHeight = bitmap.getHeight();
 				float deviceWidth = uiContext.getDisplayMetrics().widthPixels;
 				float deviceHeight = uiContext.getDisplayMetrics().heightPixels;
 
 				// // Background Size
 				String backgroundSize = pageStyle.optString("backgroundSize").trim();
 				if (backgroundSize.equalsIgnoreCase("")) {
 					backgroundSize = "cover";
 				}
 
 				// cover
 				if (backgroundSize.equalsIgnoreCase("cover")) {
 					backgroundImage.setGravity(Gravity.FILL);
 					shouldSkipBackgroundPosition = true;
 					// contain
 				} else if (backgroundSize.equalsIgnoreCase("contain")) {
 					float widthRatio = deviceWidth / bitmapWidth;
 					float heightRatio = deviceHeight / bitmapHeight;
 
 					float safeRatio = widthRatio < heightRatio ? widthRatio : heightRatio;
 					int scaledHeight = (int) FloatMath.ceil(bitmapHeight * safeRatio);
 
 					bitmap = UIUtil.resizeBitmap(bitmap, scaledHeight);
 					BitmapDrawable drawable = new BitmapDrawable(uiContext.getResources(), bitmap);
 					layerList.remove(layerList.size() - 1);
 					layerList.add(drawable);
 				}
 
 				// pixel or percentage or dip
 				else {
 					int width = 0;
 					int height = 0;
 					// both width and height specified
 					if (backgroundSize.trim().contains(",")) {
 						JSONArray sizes = pageStyle.optJSONArray("backgroundSize");
 						if (sizes != null) {
 							// width
 							String widthString = sizes.optString(0);
 							if (widthString.endsWith("%")) {
 								String percentageString = widthString.replace("%", "");
 								int percentage = Integer.parseInt(percentageString);
 								width = (int) (deviceWidth * percentage / 100);
 							} else if (widthString.endsWith("px")) {
 								widthString = widthString.replace("px", "");
 								width = Integer.parseInt(widthString);
 							} else {
 								widthString = widthString.replace("dip", "");
 								width = Integer.parseInt(widthString);
 								width = UIUtil.dip2px(uiContext, width);
 							}
 
 							// height
 							String heightString = sizes.optString(1);
 							if (heightString.endsWith("%")) {
 								String percentageString = heightString.replace("%", "");
 								int percentage = Integer.parseInt(percentageString);
 								height = (int) (deviceHeight * percentage / 100);
 							} else if (widthString.endsWith("px")) {
 								heightString = heightString.replace("px", "");
 								height = Integer.parseInt(heightString);
 							} else {
 								heightString = widthString.replace("dip", "");
 								height = Integer.parseInt(widthString);
 								height = UIUtil.dip2px(uiContext, width);
 							}
 						} else {
 							ConversionException exception = new ConversionException("Page style", "backgroundSize", backgroundSize, "Size");
 							UIValidator.reportException(uiContext, exception);
 						}
 
 					} else {
 						// only width specified
 						String widthString = backgroundSize;
 						if (widthString.endsWith("%")) {
 							String percentageString = widthString.replace("%", "");
 							int percentage = Integer.parseInt(percentageString);
 							width = (int) (deviceWidth * percentage / 100);
 						} else if (widthString.endsWith("px")) {
 							widthString = widthString.replace("px", "");
 							width = Integer.parseInt(widthString);
 						} else {
 							widthString = widthString.replace("dip", "");
 							width = Integer.parseInt(widthString);
 							width = UIUtil.dip2px(uiContext, width);
 						}
 
 						float ratio = bitmapHeight / bitmapWidth;
 						height = (int) (width * ratio);
 						MyLog.v(TAG, "scaled height:" + height);
 					}
 
 					// finished calculating width and height
 					bitmap = UIUtil.resizeBitmap(bitmap, width, height);
 					BitmapDrawable drawable = new BitmapDrawable(uiContext.getResources(), bitmap);
 					layerList.remove(layerList.size() - 1);
 					layerList.add(drawable);
 				}
 
 				BitmapDrawable finalDrawable = (BitmapDrawable) getTopDrawable(layerList);
 				processBackgroundPosition(pageStyle, finalDrawable, shouldSkipBackgroundPosition);
 			} catch (IOException e) {
 				e.printStackTrace();
 				NativeUIIOException exception = new NativeUIIOException("Page style", "backgroundImage", backgroundImageFile, e.getMessage());
 				UIValidator.reportException(uiContext, exception);
 			}
 		}
 	}
 
 	private void processBackgroundPosition(JSONObject pageStyle, BitmapDrawable backgroundImage, boolean shouldSkipBackgroundPosition) {
 		if (shouldSkipBackgroundPosition) {
 			return;
 		}
 
 		// // position
 		String horizontalPositionString = "center";
 		String verticalPositionString = "center";
 		String backgroundPosition = pageStyle.optString("backgroundPosition").trim();
 
 		// get horizontal and vertical
 		Integer horizontalGravity = Gravity.CENTER;
 		Integer verticalGravity = Gravity.CENTER;
 		if (!backgroundPosition.equalsIgnoreCase("")) {
 			// default
 			horizontalPositionString = "center";
 			verticalPositionString = "center";
 
 			if (backgroundPosition.contains(" ")) {
 				// user specified both horizontal and vertial
 				MyLog.w(TAG, backgroundPosition + " contains a space!");
 				String[] positions = backgroundPosition.split(" ");
 				horizontalPositionString = positions[0];
 				verticalPositionString = positions[1];
 			} else {
 				// only horizontal position
 				horizontalPositionString = backgroundPosition;
 			}
 
 			// check horizontal
 			// left, center, or right
 			if (UIGravity.hasHorizontalGravity(horizontalPositionString)) {
 				horizontalGravity = UIGravity.getHorizontalGravity(horizontalPositionString);
 				if (horizontalGravity == null) {
 					InvalidValueException exception = new InvalidValueException("Page style", "backgroundPosition", horizontalPositionString,
 							UIGravity.HORIZONTAL_POSITIONS);
 					UIValidator.reportException(uiContext, exception);
 				}
 			}
 
 			// check vertical
 			// top, center, or bottom
 			if (UIGravity.hasVerticalGravity(verticalPositionString)) {
 				verticalGravity = UIGravity.getVerticalGravity(verticalPositionString);
 				if (verticalGravity == null) {
 					InvalidValueException exception = new InvalidValueException("Page style", "backgroundPosition", verticalPositionString,
 							UIGravity.VERTICAL_POSITIONS);
 					UIValidator.reportException(uiContext, exception);
 				}
 			}
 		}
 		backgroundImage.setGravity(horizontalGravity | verticalGravity);
 	}
 
 	@Override
 	public String getComponentName() {
 		return "Page";
 	}
 
 	@Override
 	public JSONObject getDefaultStyle() {
 		return DefaultStyleJSON.page();
 	}
 }
