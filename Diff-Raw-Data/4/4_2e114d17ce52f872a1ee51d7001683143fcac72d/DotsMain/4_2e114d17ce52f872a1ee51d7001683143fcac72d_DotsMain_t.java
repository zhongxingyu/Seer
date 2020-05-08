 /**********************************************
  * Copyright (C) 2010 Lukas Laag
  * This file is part of lib-gwt-svg-edu.
  * 
  * libgwtsvg-edu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * libgwtsvg-edu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with libgwtsvg-edu.  If not, see http://www.gnu.org/licenses/
  **********************************************/
 package org.vectomatic.svg.edu.client.dots;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.vectomatic.dom.svg.OMNode;
 import org.vectomatic.dom.svg.OMNodeList;
 import org.vectomatic.dom.svg.OMSVGCircleElement;
 import org.vectomatic.dom.svg.OMSVGDefsElement;
 import org.vectomatic.dom.svg.OMSVGDocument;
 import org.vectomatic.dom.svg.OMSVGFEColorMatrixElement;
 import org.vectomatic.dom.svg.OMSVGFEGaussianBlurElement;
 import org.vectomatic.dom.svg.OMSVGFilterElement;
 import org.vectomatic.dom.svg.OMSVGGElement;
 import org.vectomatic.dom.svg.OMSVGLength;
 import org.vectomatic.dom.svg.OMSVGMatrix;
 import org.vectomatic.dom.svg.OMSVGNumber;
 import org.vectomatic.dom.svg.OMSVGPoint;
 import org.vectomatic.dom.svg.OMSVGPointList;
 import org.vectomatic.dom.svg.OMSVGPolylineElement;
 import org.vectomatic.dom.svg.OMSVGRect;
 import org.vectomatic.dom.svg.OMSVGSVGElement;
 import org.vectomatic.dom.svg.OMSVGTextElement;
 import org.vectomatic.dom.svg.OMSVGTransform;
 import org.vectomatic.dom.svg.OMText;
 import org.vectomatic.dom.svg.ui.SVGPushButton;
 import org.vectomatic.dom.svg.utils.AsyncXmlLoader;
 import org.vectomatic.dom.svg.utils.AsyncXmlLoaderCallback;
 import org.vectomatic.dom.svg.utils.DOMHelper;
 import org.vectomatic.dom.svg.utils.OMSVGParser;
 import org.vectomatic.dom.svg.utils.SVGConstants;
 import org.vectomatic.svg.edu.client.commons.CommonBundle;
 import org.vectomatic.svg.edu.client.commons.CommonConstants;
 import org.vectomatic.svg.edu.client.commons.LicenseBox;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.core.client.Duration;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.dom.client.Style.Visibility;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseEvent;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.widgetideas.client.HSliderBar;
 import com.google.gwt.widgetideas.client.SliderBar;
 import com.google.gwt.widgetideas.client.SliderListenerAdapter;
 
 /**
  * Main game class
  * @author laaglu
  */
 public class DotsMain implements MouseDownHandler, MouseMoveHandler, MouseUpHandler, EntryPoint {
 	interface DotsMainBinder extends UiBinder<FlowPanel, DotsMain> {
 	}
 	private static DotsMainBinder mainBinder = GWT.create(DotsMainBinder.class);
 	
 	private static String[] pictures;
 	
 	enum Mode {
 		GAME,
 		DESIGN
 	};
 	private static final String DIR = "dots";
 	private static final String ID_ALPHA1_FILTER = "pictureAlpha";
 	private static final String ID_TRANSITION_FILTER = "pictureTransition";
 	private static final String ID_ALPHA2_FILTER = "dotAlpha";
 
 	private DotsCss css = DotsResources.INSTANCE.css();
 	
 	@UiField(provided=true)
 	CommonBundle common = CommonBundle.INSTANCE;
 	@UiField
 	HTML svgContainer;
 	@UiField
 	SVGPushButton prevButton;
 	@UiField
 	SVGPushButton nextButton;
 	@UiField
 	FlowPanel navigationPanel;
 	Widget menuWidget;
 
 	@UiField
 	DecoratorPanel designPanel;
 	@UiField
 	Button addButton;
 	@UiField
 	Button removeButton;
 	@UiField
 	Button saveButton;
 	@UiField
 	Button testButton;
 	@UiField
 	CheckBox showLineCheck;
 	@UiField
 	HSliderBar pictureAlphaSlider;
 	@UiField
 	Label fileLabel;
 	@UiField
 	ListBox dotList;
 	@UiField
 	TextArea textArea;
 	private FlowPanel panel;
 	
 	/**
 	 * Index of the currently displayed image in the pictures array
 	 */
 	private int level;
 	/**
 	 * The SVG document. The document has the following structure
 	 * <tt>
 	 * <svg>          // rootSvg
 	 *  <defs>
 	 *  <g/>          // pictureGroup
 	 *  <g>           // lineGroup
 	 *   <polyline>   // polyline
 	 *  </g>
 	 *  <g>           // dotGroup
 	 *   <g/>         // first dot
 	 *   <g/>         // N-th dot
 	 *  </g>
 	 * </svg>
 	 * </tt>
 	 */
 	private OMSVGDocument doc;
 	private OMSVGSVGElement rootSvg;
 	private OMSVGDefsElement defs;
 	private OMSVGGElement pictureGroup;
 	private OMSVGGElement lineGroup;
 	private OMSVGPolylineElement polyline;
 	private OMSVGGElement dotGroup;
 	/**
 	 * The vertices of the polyline
 	 */
 	OMSVGPointList points;
 	/**
 	 * The dots in dotGroup
 	 */
 	private List<OMSVGGElement> dots;
 	/**
 	 * The dot currently being moved
 	 */
 	private OMSVGGElement currentDot;
 	/**
 	 * The index in dotGroup of dot currently being moved
 	 */
 	private int currentDotIndex;
 	/**
 	 * The alpha channel in the filter applied to pictureSvg in edit mode
 	 */
 	private OMSVGNumber pictureAlpha1;
 	/**
 	 * The alpha channel in the filter applied to pictureSvg in game mode
 	 */
 	private OMSVGNumber pictureAlpha2;
 	/**
 	 * The Gaussian blur filter applied to pictureSvg
 	 */
 	private OMSVGFEGaussianBlurElement gaussianBlur;
 	/**
 	 * The alpha channel in the filter applied to dotSvg
 	 */
 	private OMSVGNumber dotAlpha;
 	/**
 	 * The mousedown point
 	 */
 	private OMSVGPoint mouseDownPoint;
 	/**
 	 * The position of the current dot at the time of mousedown
 	 */
 	private OMSVGPoint p0;
 	/**
 	 * The index of the last dot found by the player
 	 */
 	private int maxIndex;
 	/**
 	 * To load game levels
 	 */
 	AsyncXmlLoader loader;
 
 	/**
 	 * Constructor for standalone game
 	 */
 	public DotsMain() {
 	}
 	/**
 	 * Constructor for integration in a menu
 	 */
 	public DotsMain(Widget menuWidget) {
 		this.menuWidget = menuWidget;
 	}
 
 	/**
 	 * Entry point
 	 */
 	@Override
 	public void onModuleLoad() {
 		css.ensureInjected();
 		common.css().ensureInjected();
 		
 		// Initialize the UI with UiBinder
 		panel = mainBinder.createAndBindUi(this);
 		if (menuWidget == null) {
 			menuWidget = LicenseBox.createAboutButton();
 		}
 		navigationPanel.insert(menuWidget, 0);
 		designPanel.setVisible(false);
 		RootPanel.get(CommonConstants.ID_UIROOT).add(panel);
 		Element div = svgContainer.getElement();
 		
 		// Handle resizing issues.
 		ResizeHandler resizeHandler = new ResizeHandler() {
 			@Override
 			public void onResize(ResizeEvent event) {
 				updatePictureSize();
 			}
 		};
 		Window.addResizeHandler(resizeHandler);
 		
 		pictureAlphaSlider.addSliderListener(new SliderListenerAdapter() {
 			@Override
 			public void onValueChanged(SliderBar slider, double curValue) {
 				setPictureAlpha((float)curValue);
 			}			
 		});
 
 		dots = new ArrayList<OMSVGGElement>();
 		
 		// Create the root SVG structure elements
 		doc = OMSVGParser.currentDocument();
 		rootSvg = doc.createSVGSVGElement();
		rootSvg.getWidth().getBaseVal().newValueSpecifiedUnits(Unit.PCT, 100);
		rootSvg.getHeight().getBaseVal().newValueSpecifiedUnits(Unit.PCT, 100);
 
 		// Create the SVG filters
 		defs = doc.createSVGDefsElement();
 		OMSVGFilterElement alpha1Filter = doc.createSVGFilterElement();
 		alpha1Filter.setId(ID_ALPHA1_FILTER);
 		OMSVGFEColorMatrixElement feColorMatrix1 = doc.createSVGFEColorMatrixElement();
 		feColorMatrix1.getIn1().setBaseVal(OMSVGFilterElement.IN_SOURCE_GRAPHIC);
 		feColorMatrix1.getType().setBaseVal(OMSVGFEColorMatrixElement.SVG_FECOLORMATRIX_TYPE_MATRIX);
 		pictureAlpha1 = feColorMatrix1.getValues().getBaseVal().appendItems(rootSvg, new float[]{
 				1f, 0f, 0f, 0f, 0f,
 				0f, 1f, 0f, 0f, 0f,
 				0f, 0f, 1f, 0f, 0f,
 				0f, 0f, 0f, 1f, 0f,
 		})[18];
 		
 		OMSVGFilterElement transitionFilter = doc.createSVGFilterElement();
 		transitionFilter.setId(ID_TRANSITION_FILTER);
 		gaussianBlur = doc.createSVGFEGaussianBlurElement();
 		gaussianBlur.setStdDeviation(0, 0);
 		gaussianBlur.getIn1().setBaseVal(OMSVGFilterElement.IN_SOURCE_GRAPHIC);
 		gaussianBlur.getResult().setBaseVal("blur");
 		
 		gaussianBlur = doc.createSVGFEGaussianBlurElement();
 		gaussianBlur.setStdDeviation(0, 0);
 		gaussianBlur.getIn1().setBaseVal(OMSVGFilterElement.IN_SOURCE_GRAPHIC);
 		gaussianBlur.getResult().setBaseVal("blur");
 		
 		OMSVGFEColorMatrixElement feColorMatrix2 = doc.createSVGFEColorMatrixElement();
 		feColorMatrix2.getIn1().setBaseVal("blur");
 		feColorMatrix2.getType().setBaseVal(OMSVGFEColorMatrixElement.SVG_FECOLORMATRIX_TYPE_MATRIX);
 		pictureAlpha2 = feColorMatrix2.getValues().getBaseVal().appendItems(rootSvg, new float[]{
 				1f, 0f, 0f, 0f, 0f,
 				0f, 1f, 0f, 0f, 0f,
 				0f, 0f, 1f, 0f, 0f,
 				0f, 0f, 0f, 1f, 0f,
 		})[18];
 
 		OMSVGFilterElement alpha3Filter = doc.createSVGFilterElement();
 		alpha3Filter.setId(ID_ALPHA2_FILTER);
 		OMSVGFEColorMatrixElement feColorMatrix3 = doc.createSVGFEColorMatrixElement();
 		feColorMatrix3.getIn1().setBaseVal(OMSVGFilterElement.IN_SOURCE_GRAPHIC);
 		feColorMatrix3.getType().setBaseVal(OMSVGFEColorMatrixElement.SVG_FECOLORMATRIX_TYPE_MATRIX);
 		dotAlpha = feColorMatrix3.getValues().getBaseVal().appendItems(rootSvg, new float[]{
 				1f, 0f, 0f, 0f, 0f,
 				0f, 1f, 0f, 0f, 0f,
 				0f, 0f, 1f, 0f, 0f,
 				0f, 0f, 0f, 1f, 0f,
 		})[18];
 
 
 		// Compose the root SVG structure
 		rootSvg.appendChild(defs);
 		pictureGroup = doc.createSVGGElement();
 		dotGroup = doc.createSVGGElement();
 		dotGroup.setAttribute("id", "dots");
 		lineGroup = doc.createSVGGElement();
 		polyline = doc.createSVGPolylineElement();
 		polyline.setClassNameBaseVal(css.lineInvisible());			
 		polyline.getStyle().setSVGProperty(SVGConstants.SVG_FILTER_ATTRIBUTE, DOMHelper.toUrl(ID_ALPHA2_FILTER));
 		points = polyline.getPoints();
 		lineGroup.appendChild(polyline);
 		rootSvg.appendChild(pictureGroup);
 		defs.appendChild(alpha1Filter);
 		alpha1Filter.appendChild(feColorMatrix1);
 		defs.appendChild(transitionFilter);
 		transitionFilter.appendChild(gaussianBlur);
 		transitionFilter.appendChild(feColorMatrix2);
 		defs.appendChild(alpha3Filter);
 		alpha3Filter.appendChild(feColorMatrix3);
 		rootSvg.appendChild(lineGroup);
 		rootSvg.appendChild(dotGroup);
 
 		// Add the SVG to the HTML page
 		div.appendChild(rootSvg.getElement());					
 
 		// Read the picture list
 		pictures = DotsResources.INSTANCE.pictureList().getText().split("\\s");
 		String levelParam = Window.Location.getParameter("level");
 		if (levelParam != null) {
 			try {
 				int value = Integer.parseInt(levelParam);
 				if (value >= 0 && value < pictures.length) {
 					level = value;
 				}
 			} catch(NumberFormatException e) {
 			}
 		}
 		
 		loader = GWT.create(AsyncXmlLoader.class);
 
 		updateLevel();
 	}
 	
 	/**
 	 * UiBinder factory method to instantiate HSliderBar 
 	 * @return
 	 */
 	@UiFactory
 	HSliderBar makeHSliderBar() {
 		HSliderBar sliderBar = new HSliderBar(0, 1);
 		sliderBar.setStepSize(0.1);
 		sliderBar.setCurrentValue(0);
 		return sliderBar;
 	}
 
 	private void updatePictureSize() {
 		if (rootSvg != null) {
 			OMSVGMatrix m = dotGroup.getCTM().inverse();
 			updateScales(m.getA(), m.getD());
 		}
 	}
 	
 	private void updateLevel() {
 		fileLabel.setText(pictures[level]);
 
 		// The data come in two files: a picture file and a dot file
 		// In design mode, both must be read
 		// In game mode, the dot file is read first and the picture file
 		// is read later if the player succeeds
 		if (getMode() == Mode.DESIGN) {
 			readPicture(true);
 		} else {
 			readDots();
 		}
 	}
 	
 	private String getPictureUrl() {
 		return GWT.getModuleBaseURL() + DIR + "/" + pictures[level];
 	}
 	
 	private String getDotsUrl() {
 		String url = GWT.getModuleBaseURL() + DIR + "/" + pictures[level] + ".dots";
 		// Add a bogus query to bypass the browser cache as advised by:
 		// https://developer.mozilla.org/En/Using_XMLHttpRequest#Bypassing_the_cache
 		url += (url.indexOf("?") == -1) ? ("?ts=" + System.currentTimeMillis()) : ("&ts=" + + System.currentTimeMillis());
 		return url;
 	}
 	
 	private Mode getMode() {
 		return "design".equals(Window.Location.getParameter("mode")) ? Mode.DESIGN : Mode.GAME;
 	}
 	
 	public void readPicture(final boolean readDots) {
 		loader.loadResource(getPictureUrl(), new AsyncXmlLoaderCallback() {
 			@Override
 			public void onError(String resourceName, Throwable error) {
 				svgContainer.setHTML("Cannot find resource");
 			}
 
 			@Override
 			public void onSuccess(String resourceName, com.google.gwt.dom.client.Element root) {
 				OMSVGSVGElement svg = OMNode.convert(root);
 
 				// Position the filter on the picture
 				OMSVGGElement g = reparent(svg);
 				pictureAlpha2.setValue(0f);
 				OMSVGRect viewBox = svg.getViewBox().getBaseVal();
 				rootSvg.setViewBox(viewBox.inset(svg.createSVGRect(), viewBox.getWidth() * -0.025f, viewBox.getHeight() * -0.025f));
 				
 				// Insert the picture into the SVG structure
 				rootSvg.replaceChild(g, pictureGroup);
 				pictureGroup = g;
 				pictureGroup.getStyle().setSVGProperty(SVGConstants.SVG_FILTER_ATTRIBUTE, DOMHelper.toUrl(ID_TRANSITION_FILTER));
 
 				// Send the dots request
 				if (readDots) {
 					readDots();
 				} else {
 					transition(null);
 				}				
 			}
 		});
 	}
 	
 	public void readDots() {
 		maxIndex = -1;
 		dots.clear();
 		dotList.clear();
 		points.clear();
 		loader.loadResource(getDotsUrl(), new AsyncXmlLoaderCallback() {
 			private void finish() {
 				if (getMode() == Mode.DESIGN) {
 					pictureGroup.getStyle().setSVGProperty(SVGConstants.SVG_FILTER_ATTRIBUTE, DOMHelper.toUrl(ID_ALPHA1_FILTER));
 					setPictureAlpha(1f);
 					showLineCheck.setValue(false);
 					pictureAlphaSlider.setCurrentValue(1);			
 				} else {
 					pictureGroup.getStyle().setVisibility(Visibility.HIDDEN);
 					polyline.setClassNameBaseVal(css.lineVisible());
 				}
 				dotAlpha.setValue(1f);
 				dotGroup.getStyle().setSVGProperty(SVGConstants.SVG_FILTER_ATTRIBUTE, DOMHelper.toUrl(ID_ALPHA2_FILTER));
 				designPanel.setVisible(getMode() == Mode.DESIGN);
 
 				// Resize to the size of the window
 				updatePictureSize();
 				updateUI();
 			}
 			
 			@Override
 			public void onError(String resourceName, Throwable error) {
 				if (getMode() == Mode.GAME) {
 					svgContainer.setHTML("Cannot find resource");
 					return;
 				}
 				OMSVGGElement g = doc.createSVGGElement();
 				rootSvg.replaceChild(g, dotGroup);
 				dotGroup = g;
 				finish();
 			}
 
 			@Override
 			public void onSuccess(String resourceName, com.google.gwt.dom.client.Element root) {
 				OMSVGSVGElement svg = OMNode.convert(root);
 				OMSVGGElement g = (OMSVGGElement) svg.getFirstChild();
 				rootSvg.replaceChild(g, dotGroup);
 				dotGroup = g;
 				OMSVGRect viewBox = svg.getViewBox().getBaseVal();
 				rootSvg.setViewBox(viewBox.inset(svg.createSVGRect(), viewBox.getWidth() * -0.025f, viewBox.getHeight() * -0.025f));
 				
 				// Parse the dots to recreate the polyline
 				OMNodeList<OMSVGGElement> childNodes = dotGroup.getChildNodes();
 				for (int i = 0, size = childNodes.getLength(); i < size; i++) {
 					OMSVGGElement g1 = childNodes.getItem(i);
 					g1.addMouseDownHandler(DotsMain.this);
 					dots.add(g1);
 					if (getMode() == Mode.DESIGN) {
 						g1.addMouseMoveHandler(DotsMain.this);
 						g1.addMouseUpHandler(DotsMain.this);
 						dotList.addItem(toDotName(i));
 						OMSVGMatrix m = g1.getTransform().getBaseVal().getItem(0).getMatrix();
 						points.appendItem(rootSvg.createSVGPoint(m.getE(), m.getF()));
 					}
 				}
 				finish();
 			}
 		});
 	}
 	
 	@UiHandler("prevButton")
 	public void previousPicture(ClickEvent event) {
 		level--;
 		if (level < 0) {
 			level = pictures.length - 1;
 		}
 		updateLevel();
 	}
 	
 	@UiHandler("nextButton")
 	public void nextPicture(ClickEvent event) {
 		level++;
 		if (level >= pictures.length) {
 			level = 0;
 		}
 		updateLevel();
 	}
 
 	private OMSVGPoint getLocalCoordinates(MouseEvent<? extends EventHandler> e) {
 		OMSVGPoint p = rootSvg.createSVGPoint(e.getClientX(), e.getClientY());
 		OMSVGMatrix m = rootSvg.getScreenCTM().inverse();
 		return p.matrixTransform(m);
 	}
 	
 	@UiHandler("addButton")
 	public void addDot(ClickEvent event) {
 		int pIndex = dots.size();
 		OMSVGRect viewBox = rootSvg.getViewBox().getBaseVal();
 		// Position the new points in a circle centered at the view box
 		// with a radius of 20%
 		float r = Math.min(viewBox.getWidth(), viewBox.getHeight()) * 0.2f;
 		float x = ((float)Math.cos(pIndex / 16d * 2d * Math.PI)) * r + viewBox.getCenterX();
 		float y = ((float)Math.sin(pIndex / 16d * 2d * Math.PI)) * r + viewBox.getCenterY();
 		OMSVGGElement g1 = createDot(pIndex + 1, x, y);
 		dots.add(g1);
 		points.appendItem(rootSvg.createSVGPoint(x, y));
 		dotGroup.appendChild(g1);
 		dotList.addItem(toDotName(pIndex));
 		// Autoselect the new point
 		dotList.setSelectedIndex(dotList.getItemCount() - 1);
 		updateUI();	
 	}
 
 	@UiHandler("removeButton")
 	public void removeDot(ClickEvent event) {
 		int index = dotList.getSelectedIndex();
 		OMSVGGElement g1 = dots.remove(index);
 		dotGroup.removeChild(g1);
 		dotList.removeItem(index);
 		points.removeItem(index);
 		updateUI();
 		renumber();
 	}
 
 	@UiHandler("saveButton")
 	public void save(ClickEvent event) {
 		rootSvg.removeChild(defs);
 		rootSvg.removeChild(pictureGroup);
 		rootSvg.removeChild(lineGroup);
 		dotGroup.removeAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
 		textArea.setText(rootSvg.getMarkup());
 		rootSvg.insertBefore(lineGroup, dotGroup);
 		rootSvg.insertBefore(pictureGroup, lineGroup);
 		rootSvg.insertBefore(defs, pictureGroup);
 		dotGroup.getStyle().setSVGProperty(SVGConstants.SVG_FILTER_ATTRIBUTE, DOMHelper.toUrl(ID_ALPHA2_FILTER));
 	}
 
 	@UiHandler("showLineCheck")
 	public void toggleShowLine(ClickEvent event) {
 		polyline.setClassNameBaseVal(showLineCheck.getValue() ? css.lineVisible() : css.lineInvisible());
 	}
 		
 	@UiHandler("dotList")
 	void onChange(ChangeEvent event) {
 		updateUI();
 	}
 	
 	@UiHandler("testButton")
 	public void transition(ClickEvent event) {
 		pictureAlpha2.setValue(0f);
 		gaussianBlur.setStdDeviation(10f, 10f);
 		polyline.setClassNameBaseVal(css.lineVisible());
 		pictureGroup.getStyle().setSVGProperty(SVGConstants.SVG_FILTER_ATTRIBUTE, DOMHelper.toUrl(ID_TRANSITION_FILTER));
 		if (points.getNumberOfItems() > 0) {
 			points.appendItem(points.getItem(0).assignTo(rootSvg.createSVGPoint()));
 		}
 		Animation transition = new Animation() {
 			@Override
 			protected void onUpdate(double progress) {
 				pictureAlpha2.setValue((float)progress);
 				float stdDev = 10f * (1f - (float)progress);
 				gaussianBlur.setStdDeviation(stdDev, stdDev);
 				dotAlpha.setValue(1f - (float)progress);
 			}
 			@Override
 			protected void onComplete() {
 				if (getMode() == Mode.DESIGN) {
 					polyline.setClassNameBaseVal(showLineCheck.getValue() ? css.lineVisible() : css.lineInvisible());
 					pictureGroup.getStyle().setSVGProperty(SVGConstants.SVG_FILTER_ATTRIBUTE, DOMHelper.toUrl(ID_ALPHA1_FILTER));
 					dotAlpha.setValue(1f);
 					if (points.getNumberOfItems() > 0) {
 						points.removeItem(points.getNumberOfItems() - 1);
 					}
 				} else {
 					pictureAlpha2.setValue(1f);
 					gaussianBlur.setStdDeviation(0.00001f, 0.00001f);
 					dotAlpha.setValue(0f);		
 				}
 			}
 		};
 		pictureGroup.getStyle().setVisibility(Visibility.VISIBLE);
 		transition.run(2000, Duration.currentTimeMillis() + 1000);
 	}
 	
 	private void updateUI() {
 		textArea.setText("");
 		removeButton.setEnabled(dotList.getSelectedIndex() != -1);
 	}
 
 	private OMSVGGElement createDot(int pIndex, float x, float y) {
 		OMSVGGElement g1 = doc.createSVGGElement();
 		OMSVGTransform translation = rootSvg.createSVGTransform();
 		translation.setTranslate(x, y);
 		g1.getTransform().getBaseVal().appendItem(translation);
 
 		OMSVGGElement g2 = doc.createSVGGElement();
 		OMSVGTransform scaling = rootSvg.createSVGTransform();
 		OMSVGMatrix m = rootSvg.getScreenCTM().inverse();
 		scaling.setScale(m.getA(), m.getD());
 		g2.getTransform().getBaseVal().appendItem(scaling);
 
 		OMSVGCircleElement circle1 = doc.createSVGCircleElement(0f, 0f, 5f);
 		OMSVGCircleElement circle2 = doc.createSVGCircleElement(0f, 0f, 3f);
 		OMSVGTextElement text = doc.createSVGTextElement(0f, 16f, OMSVGLength.SVG_LENGTHTYPE_PX, Integer.toString(pIndex));
 		
 		g1.appendChild(g2);
 		g2.appendChild(circle1);
 		g2.appendChild(circle2);
 		g2.appendChild(text);
 		
 		g1.addMouseDownHandler(this);
 		g1.addMouseMoveHandler(this);
 		g1.addMouseUpHandler(this);
 		
 		return g1;
 	}
 	
 	private void updateScales(float sx, float sy) {
 		for (OMSVGGElement g1 : dots) {
 			OMSVGGElement g2 = (OMSVGGElement)g1.getFirstChild();
 			OMSVGTransform scaling = g2.getTransform().getBaseVal().getItem(0);
 			scaling.setScale(sx, sy);
 		}
 		polyline.getStyle().setSVGProperty(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, Float.toString(sx));
 	}
 	
 	private void setPictureAlpha(float value) {
 		pictureAlpha1.setValue(value);
 	}
 
 	private void renumber() {
 		for (int i = 0, size = dots.size(); i < size; i++) {
 			OMText data = (OMText)dots.get(i).getFirstChild().getLastChild().getFirstChild();
 			data.setData(Integer.toString(i + 1));
 			dotList.setItemText(i, toDotName(i));
 		}
 	}
 
 	@Override
 	public void onMouseDown(MouseDownEvent event) {
 		mouseDownPoint = getLocalCoordinates(event);
 		currentDot = (OMSVGGElement) event.getSource();
 		currentDotIndex = dots.indexOf(currentDot);
 		if (getMode() == Mode.DESIGN) {
 			OMSVGMatrix m = currentDot.getTransform().getBaseVal().getItem(0).getMatrix();
 			p0 = rootSvg.createSVGPoint(m.getE(), m.getF());
 			DOMHelper.setCaptureElement(currentDot, null);
 			event.stopPropagation();
 			event.preventDefault();
 		} else {
 			if (currentDotIndex == maxIndex + 1) {
 				maxIndex++;
 				currentDot.setClassNameBaseVal(css.validated());
 				OMSVGMatrix m = currentDot.getTransform().getBaseVal().getItem(0).getMatrix();
 				points.appendItem(rootSvg.createSVGPoint(m.getE(), m.getF()));
 				if (maxIndex + 1 == dots.size()) {
 					// Level is succcessfully completed
 					readPicture(false);
 				}
 			}
 			currentDot = null;
 		}
 	}
 	
 	@Override
 	public void onMouseMove(MouseMoveEvent event) {
 		if (currentDot != null) {
 			OMSVGPoint p1 = getLocalCoordinates(event).substract(mouseDownPoint).add(p0);
 			// Update the dot position
 			OMSVGMatrix m = currentDot.getTransform().getBaseVal().getItem(0).getMatrix();
 			m.setE(p1.getX());
 			m.setF(p1.getY());
 			// Update the polyline
 			p1.assignTo(points.getItem(currentDotIndex));
 		}
 		event.stopPropagation();
 		event.preventDefault();
 	}
 
 	@Override
 	public void onMouseUp(MouseUpEvent event) {
 		if (currentDot != null) {
 			DOMHelper.releaseCaptureElement();
 			currentDot = null;
 			currentDotIndex = -1;
 		}
 		event.stopPropagation();
 		event.preventDefault();
 	}
 	
 	private static String toDotName(int pIndex) {
 		return DotsConstants.INSTANCE.dot() + " #" + (pIndex + 1);
 	}
 	
 	@UiFactory
 	PushButton createPushButton(ImageResource image) {
 		return new PushButton(new Image(image));
 	}
 	
 	/**
 	 * Removes all the child nodes of the svg document and
 	 * puts them in a group
 	 * @param svg the svg document root
 	 * @return the new group
 	 */
 	protected OMSVGGElement reparent(OMSVGSVGElement svg) {
 		OMSVGGElement g = OMSVGParser.currentDocument().createSVGGElement();
 		Element gElement = g.getElement();
 		Element svgElement = svg.getElement();
 		Node node;
 		while((node = svgElement.getFirstChild()) != null) {
 			gElement.appendChild(svgElement.removeChild(node));
 		}
 		svgElement.appendChild(gElement);
 		return g;
 	}
 
 
 }
