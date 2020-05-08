 package com.redcareditor.mate.colouring.swt;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.jface.text.JFaceTextUtil;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CaretEvent;
 import org.eclipse.swt.custom.CaretListener;
 import org.eclipse.swt.custom.LineStyleEvent;
 import org.eclipse.swt.custom.LineStyleListener;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Caret;
 import org.eclipse.swt.widgets.Display;
 
 import com.redcareditor.mate.DoublePattern;
 import com.redcareditor.mate.MateText;
 import com.redcareditor.mate.Scope;
 import com.redcareditor.mate.SinglePattern;
 import com.redcareditor.mate.colouring.Colourer;
 import com.redcareditor.theme.Theme;
 import com.redcareditor.theme.ThemeSetting;
 import com.redcareditor.util.swt.ColourUtil;
 
 public class SwtColourer implements Colourer {
 	private Theme theme;
 	private MateText mateText;
 
 	private int highlightedLine = 0;
 	private StyledText control;
 
 	/* cached swt colors */
 	private Color globalLineBackground;
 	private Color globalBackground;
 	private Color globalForeground;
 
 	public SwtColourer(MateText mt) {
 		mateText = mt;
 
 		control = mateText.getControl();
 
 		this.control.addLineStyleListener(new LineStyleListener() {
 			public void lineGetStyle(LineStyleEvent event) {
 				colourLine(event);
 			}
 		});
 
 		control.addCaretListener(new CaretListener() {
 			public void caretMoved(CaretEvent e) {
 				updateHighlightedLine(control.getLineAtOffset(e.caretOffset));
 			}
 		});
 	}
 
 	private void updateHighlightedLine(int line) {
 		if (caretLineHasChanged(line)) {
			int maxLineIx = control.getLineCount() - 1;
			if (line <= maxLineIx)
				control.setLineBackground(line, 1, globalLineBackground);
			if (highlightedLine <= maxLineIx)
 			control.setLineBackground(highlightedLine, 1, globalBackground);
 			highlightedLine = line;
 		}
 	}
 
 	private boolean caretLineHasChanged(int line) {
 		return line != highlightedLine;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.redcareditor.mate.Colourer#setTheme(com.redcareditor.theme.Theme)
 	 */
 	public void setTheme(Theme theme) {
 		this.theme = theme;
 		theme.initForUse();
 		initCachedColours();
 		setMateTextColours();
 		setCaretColour();
 	}
 
 	private void setCaretColour() {
 		Caret caret = control.getCaret();
 		Rectangle bounds = caret.getBounds();
 		int width = bounds.width;
 		int height = bounds.height;
 		caret = new Caret(control, SWT.NONE);
 		Display display = Display.getCurrent();
 		// System.out.printf("caret colour: %s %d %d\n", globalColour("caret"), width, height);
 		String caretColourString = globalColour("caret");
 		Color caretColour = ColourUtil.getColour(caretColourString);
 		Color white = display.getSystemColor(SWT.COLOR_WHITE);
 		Color black = display.getSystemColor(SWT.COLOR_BLACK);
 		String backgroundColourString = globalColour("background");
 		int red = Integer.parseInt(backgroundColourString.substring(1, 3), 16) ^ 
 					Integer.parseInt(caretColourString.substring(1, 3), 16);
 		int green = Integer.parseInt(backgroundColourString.substring(3, 5), 16) ^ 
 						Integer.parseInt(caretColourString.substring(3, 5), 16);
 		int blue = Integer.parseInt(backgroundColourString.substring(5, 7), 16) ^
 						Integer.parseInt(caretColourString.substring(5, 7), 16);
 		// System.out.printf("r, g, b: %d, %d, %d\n", red, green, blue);
 		PaletteData palette = new PaletteData (
 			new RGB [] {
 				new RGB (0, 0, 0),
 				new RGB (red, green, blue),
 				new RGB (0xFF, 0xFF, 0xFF),
 			});
 		// System.out.printf("depth: %d\n", Display.getCurrent().getDepth());
 		ImageData maskData = new ImageData (1, height, 2, palette);
 		for (int y=0; y < height; y++)
 			maskData.setPixel(0, y, 1);
 		Image image = new Image (display, maskData);
 		// Image image = new Image(display, 1, height);
 		// GC gc = new GC(image);
 		// gc.setAntialias(SWT.OFF);
 		// gc.setBackground(white);
 		// gc.fillRectangle(0, 0, 1, height);
 		// gc.setForeground(caretColour);
 		// gc.drawLine(0, 0, 0, height);
 		// System.out.printf("alpha: %d\n", gc.getAlpha());
 		// gc.dispose();
 		// caret.setLocation(10, 10);
 		caret.setImage(image);
 		control.setCaret(caret);
 	}
 
 	private void setMateTextColours() {
 		control.setBackground(globalBackground);
 		control.setForeground(globalForeground);
 		int currentLine = control.getLineAtOffset(control.getCaretOffset());
 		int startLine = JFaceTextUtil.getPartialTopIndex(control);
 		int endLine = JFaceTextUtil.getPartialBottomIndex(control);
 		for (int i = startLine; i <= endLine; i ++)
 			control.setLineBackground(i, 1, globalBackground);
 		control.setLineBackground(currentLine, 1, globalLineBackground);
 		mateText.setGutterBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
 		mateText.setGutterForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
 	}
 
 	private void initCachedColours() {
 		globalBackground = ColourUtil.getColour(globalColour("background"));
 		globalForeground = ColourUtil.getColour(globalColour("foreground"));
 		globalLineBackground = ColourUtil.getColour(ColourUtil.mergeColour(globalColour("background"), globalColour("lineHighlight")));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.redcareditor.mate.Colourer#getTheme()
 	 */
 	public Theme getTheme() {
 		return theme;
 	}
 
 	private String globalColour(String name) {
 		String colour = theme.globalSettings.get(name);
 		if (isColorDefined(colour)) {
 			return colour;
 		} else {
 			return "#FFFFFF";
 		}
 	}
 
 	private boolean isColorDefined(String colour) {
 		return colour != null && !(colour.length() == 0);
 	}
 
 	public class StyleRangeComparator implements Comparator {
 		
 		public int compare(Object o1, Object o2) {
 			StyleRange s1 = (StyleRange) o1;
 			StyleRange s2 = (StyleRange) o2;
 			if (s1.start < s2.start) {
 				return -1;
 			} else {
 				if (s1.start > s2.start) {
 					return 1;
 				}
 				else {
 			 		if (s1.length < s2.length) {
 						return -1;
 					}
 					else {
 						if (s1.length > s2.length) {
 							return 1;
 						}
 						else {
 							return 0;
 						}
 					}
 				}
 			}
 		}
 		
 		public boolean equals(Object obj) {
 			return false;
 		}
 	}
 	
 	private void colourLine(LineStyleEvent event) {
 		if (theme == null)
 			return;
 		if (!mateText.shouldColour())
 			return;
 		int eventLine = mateText.getControl().getLineAtOffset(event.lineOffset);
 		// System.out.printf("c%d, ", eventLine);
 		// System.out.printf("[Color] colouring %d\n", eventLine);
 		ArrayList<Scope> scopes = mateText.parser.root.scopesOnLine(eventLine);
 		// System.out.printf("[Color] got to colour %d scopes\n", scopes.size());
 		ArrayList<StyleRange> styleRanges = new ArrayList<StyleRange>();
 		for (Scope scope : scopes) {
 			// System.out.printf("[Color] scope: %s\n", scope.name);
 			if (scope.parent == null) {
 				continue;
 			}
 			if (scope.name == null && scope.pattern != null
 					&& (scope.pattern instanceof SinglePattern || ((DoublePattern) scope.pattern).contentName == null)) {
 				continue;
 			}
 			addStyleRangeForScope(styleRanges, scope, false);
 			if (scope.pattern instanceof DoublePattern && ((DoublePattern) scope.pattern).contentName != null && scope.isCapture == false)
 				addStyleRangeForScope(styleRanges, scope, true);
 		}
 		//
 		// Collections.sort(styleRanges, new StyleRangeComparator());
 		// System.out.printf("length: %d\n", styleRanges.size());
 		event.styles = (StyleRange[]) styleRanges.toArray(new StyleRange[0]);
 	}
 
 	private void addStyleRangeForScope(ArrayList<StyleRange> styleRanges, Scope scope, boolean inner) {
 		StyleRange styleRange = new StyleRange();
 		// TODO: allow for multiple settings that set different
 		// parts of the style.
 		ThemeSetting setting = null;
 		ThemeSetting excludeSetting = null;
 		if (scope.parent != null)
 			excludeSetting = scope.parent.themeSetting;
 		setting = theme.settingsForScope(scope, inner, null);// exclude_setting);
 
 		if (inner) {
 			styleRange.start = scope.getInnerStart().getOffset();
 			styleRange.length = scope.getInnerEnd().getOffset() - styleRange.start;
 		} else {
 			styleRange.start = scope.getStart().getOffset();
 			styleRange.length = scope.getEnd().getOffset() - styleRange.start;
 		}
 		if (setting != null) {
 			setStyleRangeProperties(scope, setting, styleRange);
 		styleRanges.add(styleRange);
 			// System.out.printf("[Color] style range (%d, %d) %s\n", styleRange.start, styleRange.length, styleRange.toString());
 		}
 	}
 
 	private void setStyleRangeProperties(Scope scope, ThemeSetting setting, StyleRange styleRange) {
 		String fontStyle = setting.settings.get("fontStyle");
 		if (fontStyle == "italic") {
 			styleRange.fontStyle = SWT.ITALIC;
 		} else
 			styleRange.fontStyle = SWT.NORMAL;
 
 		if (fontStyle == "underline")
 			styleRange.fontStyle = SWT.UNDERLINE_SINGLE;
 		else
 			styleRange.fontStyle = SWT.NORMAL;
 
 		String background = setting.settings.get("background");
 		// System.out.printf("[Color] scope background: %s\n", background);
 		String mergedBgColour;
 		String parentBg = theme.globalSettings.get("background");
 		// System.out.printf("		   global background: %s\n", parentBg);
 		// TODO: wasn't this a better way of creating the background colours?
 		// var parent_bg = scope.nearest_background_colour();
 		// if (parent_bg == null) {
 		// }
 		// else {
 		// stdout.printf("		  parent background: %s\n", parent_bg);
 		// }
 		if (background != null && background != "") {
 			// if (parent_bg != null) {
 			// merged_bg_colour = parent_bg;
 			// }
 			// else {
 			mergedBgColour = ColourUtil.mergeColour(parentBg, background);
 			// }
 			if (mergedBgColour != null) {
 				scope.bgColour = mergedBgColour;
 				styleRange.background = ColourUtil.getColour(mergedBgColour);
 				// System.out.printf("[Color] background = %s\n", mergedBgColour);
 			}
 		} else {
 			mergedBgColour = parentBg;
 		}
 		// stdout.printf("		  merged_bg_colour:	 %s\n", merged_bg_colour);
 		String foreground = setting.settings.get("foreground");
 		// System.out.printf("[Color] scope foreground: %s\n", foreground);
 		String parentFg = scope.nearestForegroundColour();
 		if (parentFg == null) {
 			parentFg = theme.globalSettings.get("foreground");
 			// stdout.printf("		  global foreground:		%s\n",
 			// parent_fg);
 		}
 		if (foreground != null && foreground != "") {
 			String mergedFgColour;
 			if (parentFg != null && !scope.isCapture)
 				mergedFgColour = ColourUtil.mergeColour(parentFg, foreground);
 			else
 				mergedFgColour = foreground;
 			if (mergedFgColour != null) {
 				scope.fgColour = mergedFgColour;
 				styleRange.foreground = ColourUtil.getColour(mergedFgColour);
 			}
 			// stdout.printf("		 merged_fg_colour: %s\n", merged_fg_colour);
 		}
 		// stdout.printf("\n");
 	}
 
 }
