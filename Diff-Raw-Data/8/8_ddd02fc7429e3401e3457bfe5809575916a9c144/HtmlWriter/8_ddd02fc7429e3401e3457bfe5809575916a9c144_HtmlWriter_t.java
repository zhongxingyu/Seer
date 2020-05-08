 package com.psddev.dari.util;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.reflect.Array;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 /** Writer implementation that adds basic HTML formatting. */
 public class HtmlWriter extends Writer {
 
     private static final String GRID_PADDING; static {
         StringBuilder gp = new StringBuilder();
         for (int i = 0; i < 500; ++ i) {
             gp.append(" .");
         }
         GRID_PADDING = gp.toString();
     }
 
     private Writer delegate;
     private final Map<Class<?>, HtmlFormatter<Object>> defaultFormatters = new HashMap<Class<?>, HtmlFormatter<Object>>();
     private final Map<Class<?>, HtmlFormatter<Object>> overrideFormatters = new HashMap<Class<?>, HtmlFormatter<Object>>();
     private final Deque<String> tags = new ArrayDeque<String>();
 
     /** Creates an instance. */
     public HtmlWriter() {
     }
 
     /**
      * Creates an instance that writes to the given {@code delegate}.
      *
      * @param delegate May be {@code null}.
      */
     public HtmlWriter(Writer delegate) {
         this.delegate = delegate;
     }
 
     /**
      * Returns the delegate.
      *
      * @return May be {@code null}.
      */
     public Writer getDelegate() {
         return delegate;
     }
 
     /**
      * Sets the delegate.
      *
      * @param delegate May be {@code null}.
      */
     public void setDelegate(Writer delegate) {
         this.delegate = delegate;
     }
 
     @SuppressWarnings("unchecked")
     public <T> void putDefault(Class<T> objectClass, HtmlFormatter<? super T> formatter) {
         defaultFormatters.put(objectClass, (HtmlFormatter<Object>) formatter);
     }
 
     @SuppressWarnings("unchecked")
     public <T> void putOverride(Class<T> objectClass, HtmlFormatter<? super T> formatter) {
         overrideFormatters.put(objectClass, (HtmlFormatter<Object>) formatter);
     }
 
     public void putAllStandardDefaults() {
         putDefault(null, HtmlFormatter.NULL);
         putDefault(Class.class, HtmlFormatter.CLASS);
         putDefault(Collection.class, HtmlFormatter.COLLECTION);
         putDefault(Date.class, HtmlFormatter.DATE);
         putDefault(Double.class, HtmlFormatter.FLOATING_POINT);
         putDefault(Enum.class, HtmlFormatter.ENUM);
         putDefault(Float.class, HtmlFormatter.FLOATING_POINT);
         putDefault(Map.class, HtmlFormatter.MAP);
         putDefault(Number.class, HtmlFormatter.NUMBER);
         putDefault(PaginatedResult.class, HtmlFormatter.PAGINATED_RESULT);
         putDefault(StackTraceElement.class, HtmlFormatter.STACK_TRACE_ELEMENT);
         putDefault(Throwable.class, HtmlFormatter.THROWABLE);
 
         // Optional.
         if (HtmlFormatter.JASPER_EXCEPTION_CLASS != null) {
             putDefault(HtmlFormatter.JASPER_EXCEPTION_CLASS, HtmlFormatter.JASPER_EXCEPTION);
         }
     }
 
     public void removeDefault(Class<?> objectClass) {
         defaultFormatters.remove(objectClass);
     }
 
     public void removeOverride(Class<?> objectClass) {
         overrideFormatters.remove(objectClass);
     }
 
     /**
      * Escapes the given {@code string} so that it's safe to use in
      * an HTML page.
      */
     protected String escapeHtml(String string) {
         return StringUtils.escapeHtml(string);
     }
 
     /**
      * Writes the given {@code object} as a string without any escaping.
      *
      * @param object If {@code null}, writes nothing.
      */
     public HtmlWriter writeRaw(Object object) throws IOException {
         if (object != null) {
             if (object instanceof CharSequence) {
                 append((CharSequence) object);
 
             } else {
                 write(object.toString());
             }
         }
 
         return this;
     }
 
     /**
      * Writes the given {@code tag} with the given {@code attributes}.
      *
      * <p>This method doesn't keep state, so it should be used with doctype
      * declaration and self-closing tags like {@code img}.</p>
      */
     public HtmlWriter writeTag(String tag, Object... attributes) throws IOException {
         if (tag == null) {
             throw new IllegalArgumentException("Tag can't be null!");
         }
 
         Writer delegate = getDelegate();
 
         delegate.write('<');
         delegate.write(tag);
 
         if (attributes != null) {
             Map<String, Object> map = new LinkedHashMap<String, Object>();
 
             addAttributes(map, attributes);
 
             for (Map.Entry<String, Object> entry : map.entrySet()) {
                 String name = entry.getKey();
                 Object value = entry.getValue();
 
                 if (!ObjectUtils.isBlank(name) && value != null) {
                     delegate.write(' ');
                     delegate.write(escapeHtml(name));
                     delegate.write("=\"");
                     delegate.write(escapeHtml(value.toString()));
                     delegate.write('"');
                 }
             }
         }
 
         delegate.write('>');
         return this;
     }
 
     private void addAttributes(Map<String, Object> map, Object... attributes) {
         for (int i = 0, length = attributes.length; i < length; ++ i) {
             Object name = attributes[i];
 
             if (name == null) {
                 ++ i;
 
             } else if (name.getClass().isArray()) {
                 addAttributes(map, ObjectUtils.to(Object[].class, name));
 
             } else if (name instanceof Map) {
                 for (Map.Entry<?, ?> entry : ((Map<?, ?>) name).entrySet()) {
                     Object key = entry.getKey();
                     Object value = entry.getValue();
 
                     if (key != null && value != null) {
                         map.put(key.toString(), value);
                     }
                 }
 
             } else {
                 ++ i;
 
                 if (i < length) {
                     map.put(name.toString(), attributes[i]);
                 }
             }
         }
     }
 
     /**
      * Writes the given start {@code tag} with the given {@code attributes}.
      *
      * <p>This method keeps state, so there should be a matching {@link #end}
      * call afterwards.</p>
      */
     public HtmlWriter writeStart(String tag, Object... attributes) throws IOException {
         writeTag(tag, attributes);
         tags.addFirst(tag);
         return this;
     }
 
     /** Writes the end tag previously started with {@link #start}. */
     public HtmlWriter writeEnd() throws IOException {
         String tag = tags.removeFirst();
 
         if (tag == null) {
             throw new IllegalStateException("No more tags!");
         }
 
         Writer delegate = getDelegate();
 
         delegate.write("</");
         delegate.write(tag);
         delegate.write('>');
 
         return this;
     }
 
     /**
      * Escapes and writes the given {@code unescapedHtml}, or if it's
      * {@code null}, the given {@code defaultUnescapedHtml}.
      */
     public HtmlWriter writeHtmlOrDefault(Object unescapedHtml, String defaultUnescapedHtml) throws IOException {
         getDelegate().write(escapeHtml(unescapedHtml == null ? defaultUnescapedHtml : unescapedHtml.toString()));
         return this;
     }
 
     /**
      * Escapes and writes the given {@code unescapedHtml}, or if it's
      * {@code null}, nothing.
      */
     public HtmlWriter writeHtml(Object unescapedHtml) throws IOException {
         writeHtmlOrDefault(unescapedHtml, "");
         return this;
     }
 
     /** Formats and writes the given {@code object}. */
     public HtmlWriter writeObject(Object object) throws IOException {
         HtmlFormatter<Object> formatter;
 
         if (object == null) {
             formatter = overrideFormatters.get(null);
             if (formatter == null) {
                 formatter = defaultFormatters.get(null);
             }
             if (formatter != null) {
                 formatter.format(this, null);
                 return this;
             }
 
         } else {
             if (formatWithMap(overrideFormatters, object)) {
                 return this;
             }
 
             if (object instanceof HtmlObject) {
                 ((HtmlObject) object).format(this);
                 return this;
             }
 
             if (formatWithMap(defaultFormatters, object)) {
                 return this;
             }
         }
 
         if (object != null && object.getClass().isArray()) {
             writeStart("ul");
                 for (int i = 0, length = Array.getLength(object); i < length; ++ i) {
                     writeStart("li").writeObject(Array.get(object, i)).writeEnd();
                 }
             writeEnd();
             return this;
         }
 
         return writeHtml(object);
     }
 
     private boolean formatWithMap(
             Map<Class<?>, HtmlFormatter<Object>> formatters,
             Object object)
             throws IOException {
 
         HtmlFormatter<Object> formatter;
 
         for (Class<?> objectClass = object.getClass();
                 objectClass != null;
                 objectClass = objectClass.getSuperclass()) {
 
             formatter = formatters.get(objectClass);
             if (formatter != null) {
                 formatter.format(this, object);
                 return true;
             }
 
             for (Class<?> interfaceClass : objectClass.getInterfaces()) {
                 formatter = formatters.get(interfaceClass);
                 if (formatter != null) {
                     formatter.format(this, object);
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /** Returns a CSS string based on the given {@code properties}. */
     public String cssString(Object... properties) {
         return Static.cssString(properties);
     }
 
     /** Writes a CSS rule based on the given parameters. */
     public HtmlWriter writeCss(String selector, Object... properties) throws IOException {
         write(selector);
         write('{');
         write(cssString(properties));
         write('}');
         return this;
     }
 
     /**
      * Writes CSS common to all grids.
      */
     public HtmlWriter writeCommonGridCss() throws IOException {
         writeCss(".dari-grid-area",
                 "-moz-box-sizing", "content-box",
                 "-webkit-box-sizing", "content-box",
                 "box-sizing", "content-box",
                 "float", "left",
                 "margin", "0 -100% 0 -30000px");
 
         writeCss(".dari-grid-adj",
                 "float", "left");
 
         writeCss(".dari-grid-adj-px:before",
                 "content", "'" + GRID_PADDING + "'",
                 "display", "block",
                 "height", 0,
                 "overflow", "hidden",
                 "visibility", "hidden");
 
         writeCss(".dari-grid-mw",
                 "height", 0);
 
         writeCss(".dari-grid-mh",
                 "width", 0);
 
         if (isGridDebug()) {
             writeCss(".dari-grid-debug",
                     "outline", "solid 1px rgba(0, 0, 0, 0.3)");
 
             writeCss(".dari-grid-debug:before",
                     "background-color", "rgba(0, 0, 0, 0.3)",
                     "color", "black",
                     "content", "attr(data-grid-selector) ' / ' attr(data-grid-area)",
                     "display", "block",
                     "font-family", "'Andale Mono', 'Lucida Console', monospace",
                     "font-size", "14px",
                     "line-height", "25px",
                     "padding", "0 5px");
 
             writeCss(".dari-grid-debug:hover",
                     "outline-color", "rgba(0, 0, 0, 0.7)",
                     "outline-width", "5px");
 
             writeCss(".dari-grid-debug:hover:before",
                     "background-color", "rgba(0, 0, 0, 0.7)",
                     "color", "white");
         }
 
         return this;
     }
 
     /**
      * Writes grid CSS for the given {@code selector} and {@code grid}.
      */
     public HtmlWriter writeGridCss(String selector, HtmlGrid grid) throws IOException {
         StringBuilder cssSuffix = new StringBuilder();
 
         for (int lastBraceAt = 0, braceAt;
                 (braceAt = selector.indexOf('{', lastBraceAt)) > -1;
                 lastBraceAt = braceAt + 1) {
             cssSuffix.append('}');
         }
 
         CssUnit minWidth = grid.getMinimumWidth().getSingle();
 
         if (minWidth != null) {
             writeCss(selector,
                     "min-width", minWidth);
             writeRaw(cssSuffix);
         }
 
         writeCss(selector + " > .dari-grid-area[data-grid-area]",
                 "display", "none");
         writeRaw(cssSuffix);
 
         for (Area area : createAreas(grid).values()) {
             String selectorSuffix = "[data-grid-area=\"" + area.name + "\"]";
 
             writeCss(selector + " > .dari-grid-area" + selectorSuffix,
                     "clear", area.clear ? "left" : null,
                     "display", "block",
                     "padding-left", area.frPaddingLeft + "%",
                     "width", area.frWidth + "%");
             writeRaw(cssSuffix);
 
             for (Map.Entry<String, Adjustment> entry : area.adjustments.entrySet()) {
                 String unit = entry.getKey();
                 Adjustment adjustment = entry.getValue();
 
                 writeCss(selector + " .dari-grid-adj-" + unit + selectorSuffix,
                         "height", adjustment.height != null ? adjustment.height : "auto",
                         "margin", adjustment.getMargin(unit),
                         "width", adjustment.width != null ? adjustment.width : "auto");
                 writeRaw(cssSuffix);
             }
 
             for (CssUnit width : area.width.getAll()) {
                 String unit = width.getUnit();
 
                writeCss(selector + " .dari-grid-mw-" + unit + selectorSuffix,
                        "padding-left", "fr".equals(unit) ? 0 : width);
                writeRaw(cssSuffix);
             }
 
             for (CssUnit height : area.height.getAll()) {
                 writeCss(selector + " .dari-grid-mh-" + height.getUnit() + selectorSuffix,
                         "padding-top", height);
                 writeRaw(cssSuffix);
             }
         }
 
         return this;
     }
 
     /**
      * Writes all grid CSS found within the given {@code context}.
      *
      * @deprecated Use {@link #writeAllGridCss} instead.
      */
     @Deprecated
     public HtmlWriter writeGridCss(ServletContext context, HttpServletRequest request) throws IOException {
         return writeAllGridCss(context, request);
     }
 
     /**
      * Writes all grid CSS found within the given {@code context}.
      */
     public HtmlWriter writeAllGridCss(ServletContext context, HttpServletRequest request) throws IOException {
         writeCommonGridCss();
 
         for (Map.Entry<String, HtmlGrid> gridEntry : HtmlGrid.Static.findAll(context, request).entrySet()) {
             write("\n\n");
             writeGridCss(gridEntry.getKey(), gridEntry.getValue());
         }
 
         return this;
     }
 
     /**
      * Writes all grid JavaScript found within the given {@code context}.
      *
      * @deprecated Use {@link #writeAllGridJavaScript} instead.
      */
     @Deprecated
     public HtmlWriter writeGridJavaScript(ServletContext context, HttpServletRequest request) throws IOException {
         return writeAllGridJavaScript(context, request);
     }
 
     // Returns true if in grid debugging mode.
     private boolean isGridDebug() {
         try {
             return !Settings.isProduction() && ObjectUtils.to(boolean.class, PageContextFilter.Static.getRequest().getParameter("_grid"));
         } catch (IllegalStateException error) {
             return false;
         }
     }
 
     // Writes JavaScript console logging message.
     private void logJavaScript(String message) throws IOException {
         if (isGridDebug()) {
             write("console.log('[GRID] ' + ");
             write(message);
             write(");");
         }
     }
 
     /**
      * Writes all grid JavaScript found within the given {@code context}.
      */
     public HtmlWriter writeAllGridJavaScript(ServletContext context, HttpServletRequest request) throws IOException {
         Map<String, Map<String, HtmlGrid>> gridsByMedia = new LinkedHashMap<String, Map<String, HtmlGrid>>();
 
         for (Map.Entry<String, HtmlGrid> gridEntry : HtmlGrid.Static.findAll(context, request).entrySet()) {
             String gridSelector = gridEntry.getKey();
             HtmlGrid grid = gridEntry.getValue();
             String media = null;
 
             if (gridSelector.startsWith("@media")) {
                 gridSelector = gridSelector.substring(6).trim();
                 int braceAt = gridSelector.indexOf('{');
 
                 if (braceAt > -1) {
                     media = gridSelector.substring(0, braceAt).trim();
                     gridSelector = gridSelector.substring(braceAt + 1).trim();
                 }
             }
 
             Map<String, HtmlGrid> grids = gridsByMedia.get(media);
 
             if (grids == null) {
                 grids = new HashMap<String, HtmlGrid>();
                 gridsByMedia.put(media, grids);
             }
 
             grids.put(gridSelector, grid);
         }
 
         Map<String, HtmlGrid> defaultGrids = gridsByMedia.remove(null);
 
         if (defaultGrids != null) {
             gridsByMedia.put(null, defaultGrids);
         }
 
         write("if (typeof jQuery !== 'undefined') (function($, win, undef) {");
             write("var reorder, reorderTimer;");
             write("if (!window.matchMedia) return;");
 
             write("reorder = function() {");
                 Map<String, Map<String, Collection<String>>> gridsByMediaJson = new CompactMap<String, Map<String, Collection<String>>>();
 
                 for (Map.Entry<String, Map<String, HtmlGrid>> entry : gridsByMedia.entrySet()) {
                     String media = entry.getKey();
                     Map<String, Collection<String>> gridsJson = new CompactMap<String, Collection<String>>();
 
                     gridsByMediaJson.put(media == null ? "DEFAULT" : media, gridsJson);
 
                     for (Map.Entry<String, HtmlGrid> gridEntry : entry.getValue().entrySet()) {
                         gridsJson.put(gridEntry.getKey(), gridEntry.getValue().getAreas());
                     }
                 }
 
                 write("$.each(");
                 write(ObjectUtils.toJson(gridsByMediaJson));
                 write(", function(media, grids) {");
                     write("if (media === 'DEFAULT' || window.matchMedia(media).matches) {");
                         logJavaScript("'Matched [' + media + ']'");
 
                         write("$.each(grids, function(selector, areas) {");
                             write("$(selector).each(function() {");
                                 write("var $layout = $(this),");
                                         write("$children = $layout.find('> .dari-grid-area'),");
                                         write("expected = areas.join(', '),");
                                         write("current = $.map($children, function(area) { return $(area).attr('data-grid-area'); }).join(', '),");
                                         write("$clear;");
 
                                 write("if (expected === current) { return; }");
 
                                 logJavaScript("'Rearranging [' + selector + '] from [' + current + '] to [' + expected + ']'");
 
                                 write("$.each(areas, function(index, area) {");
                                     write("var $child = $children.filter('[data-grid-area=\"' + area + '\"]');");
                                     write("if ($child.length > 0) { $layout[0].appendChild($child[0]); }");
                                 write("});");
 
                                 write("$clear = $layout.find('> .dari-grid-clear');");
                                 write("if ($clear.length > 0) { $layout[0].appendChild($clear[0]); }");
                             write("});");
                         write("});");
 
                         write("return false;");
                     write("}");
                 write("});");
             write("};");
 
             write("$(reorder);");
 
             write("$(win).resize(function() {");
                 write("if (!reorderTimer) {");
                     write("reorderTimer = setTimeout(function() {");
                         write("reorder();");
                         write("reorderTimer = null;");
                     write("}, 100);");
                 write("}");
             write("});");
         write("})(jQuery, window);");
 
         return this;
     }
 
     /**
      * Writes the given {@code object} and positions it according to the
      * given {@code grid}.
      *
      * @see <a href="http://dev.w3.org/csswg/css3-grid-layout/">CSS Grid Layout</a>
      */
     public HtmlWriter writeGrid(Object object, HtmlGrid grid) throws IOException {
         Map<String, Area> areas = createAreas(grid);
         boolean debug = isGridDebug();
 
         if (object == null) {
             object = areas;
         }
 
         for (Map.Entry<String, Area> entry : areas.entrySet()) {
             String areaName = entry.getKey();
             Area area = entry.getValue();
 
             // The main wrapping DIV around the area. Initially shifted
             // left 30000px so that it's off-screen as not to overlap
             // other elements that come before.
             writeStart("div",
                     "class", "dari-grid-area",
                     "data-grid-area", areaName);
 
                 int adjustments = 0;
 
                 for (String unit : area.adjustments.keySet()) {
                     ++ adjustments;
 
                     writeStart("div",
                             "class", "dari-grid-adj dari-grid-adj-" + unit,
                             "data-grid-area", areaName);
                 }
 
                 if (debug) {
                     writeStart("div",
                             "class", "dari-grid-debug",
                             "data-grid-selector", grid.getSelector(),
                             "data-grid-area", areaName);
                 }
 
                 // Minimum width with multiple units.
                 if (area.singleWidth == null) {
                     int i = 0;
 
                     for (CssUnit column : area.width.getAll()) {
                         String unit = column.getUnit();
 
                         if (!"fr".equals(unit)) {
                             ++ i;
                             writeStart("div",
                                     "class", "dari-grid-mw dari-grid-mw-" + unit,
                                     "data-grid-area", areaName);
                         }
                     }
 
                     for (; i > 0; -- i) {
                         writeEnd();
                     }
                 }
 
                 // Minimum height with multiple units.
                 if (area.singleHeight == null) {
                     writeStart("div", "style", cssString(
                             "float", "left",
                             "width", 0));
 
                     int i = 0;
 
                     for (CssUnit row : area.height.getAll()) {
                         ++ i;
                         writeStart("div",
                                 "class", "dari-grid-mh dari-grid-mh-" + row.getUnit(),
                                 "data-grid-area", areaName);
                     }
 
                     for (; i > 0; -- i) {
                         writeEnd();
                     }
 
                     writeEnd();
                 }
 
                 writeObject(CollectionUtils.getByPath(object, entry.getKey()));
 
                 if (area.singleHeight == null) {
                     writeStart("div", "style", cssString("clear", "left"));
                     writeEnd();
                 }
 
                 if (debug) {
                     writeEnd();
                 }
 
                 for (; adjustments > 0; -- adjustments) {
                     writeEnd();
                 }
 
             writeEnd();
         }
 
         writeStart("div",
                 "class", "dari-grid-clear",
                 "style", cssString("clear", "left"));
         writeEnd();
 
         return this;
     }
 
     private Map<String, Area> createAreas(HtmlGrid grid) {
         List<CssUnit> columns = grid.getColumns();
         List<CssUnit> rows = grid.getRows();
         List<List<String>> template = new ArrayList<List<String>>(grid.getTemplate());
 
         // Clone the template so that the original isn't changed.
         for (ListIterator<List<String>> i = template.listIterator(); i.hasNext(); ) {
             i.set(new ArrayList<String>(i.next()));
         }
 
         Map<String, Area> areaInstances = new LinkedHashMap<String, Area>();
         int clearAt = -1;
 
         for (int rowStart = 0, rowSize = rows.size(); rowStart < rowSize; ++ rowStart) {
             List<String> areas = template.get(rowStart);
 
             for (int columnStart = 0, columnSize = columns.size(); columnStart < columnSize; ++ columnStart) {
                 String area = areas.get(columnStart);
 
                 // Already processed or padding.
                 if (area == null || ".".equals(area)) {
                     continue;
                 }
 
                 int rowStop = rowStart + 1;
                 int columnStop = columnStart + 1;
 
                 // Figure out the "width" of the area.
                 for (; columnStop < columnSize; ++ columnStop) {
                     if (!ObjectUtils.equals(areas.get(columnStop), area)) {
                         break;
                     } else {
                         areas.set(columnStop, null);
                     }
                 }
 
                 // Figure out the "height" of the area.
                 for (; rowStop < rowSize; ++ rowStop) {
                     if (columnStart < template.get(rowStop).size() && !ObjectUtils.equals(template.get(rowStop).get(columnStart), area)) {
                         break;
                     } else {
                         for (int i = columnStart; i < columnStop; ++ i) {
                             if (i < template.get(rowStop).size()) {
                                 template.get(rowStop).set(i, null);
                             }
                         }
                     }
                 }
 
                 // Figure out the rough initial position and size using
                 // percentages.
                 Area areaInstance = new Area(area);
                 areaInstances.put(area, areaInstance);
 
                 double frMax = 0;
                 double frBefore = 0;
                 double frAfter = 0;
 
                 for (int i = 0; i < columnSize; ++ i) {
                     CssUnit column = columns.get(i);
 
                     if ("fr".equals(column.getUnit())) {
                         double fr = column.getNumber();
 
                         frMax += fr;
 
                         if (i < columnStart) {
                             frBefore += fr;
 
                         } else if (i >= columnStop) {
                             frAfter += fr;
                         }
                     }
                 }
 
                 if (frMax == 0) {
                     frMax = 1;
                     frAfter = 1;
                 }
 
                 double frBeforeRatio = frBefore / frMax;
                 double frAfterRatio = frAfter / frMax;
 
                 areaInstance.frPaddingLeft = frBeforeRatio * 100.0;
                 areaInstance.frWidth = (frMax - frBefore - frAfter) * 100.0 / frMax;
 
                 // Adjust left and width.
                 for (int i = 0; i < columnSize; ++ i) {
                     CssUnit column = columns.get(i);
                     String columnUnit = column.getUnit();
 
                     if (!"fr".equals(columnUnit)) {
                         double columnNumber = column.getNumber();
                         double left = columnNumber * ((i < columnStart ? 1 : 0) - frBeforeRatio);
                         double right = columnNumber * ((i >= columnStop ? 1 : 0) - frAfterRatio);
 
                         if (left != 0.0 || right != 0.0) {
                             Adjustment adjustment = areaInstance.getOrCreateAdjustment(columnUnit);
                             adjustment.left += left;
                             adjustment.right += right;
                         }
                     }
                 }
 
                 // Adjust top.
                 for (int i = rowSize - 1; i >= 0; -- i) {
                     CssUnit row = rows.get(i);
                     String rowUnit = row.getUnit();
 
                     if (i < rowStart && "auto".equals(rowUnit)) {
                         break;
 
                     } else if (!"fr".equals(rowUnit)) {
                         double top = row.getNumber() * (i < rowStart ? 1 : 0);
 
                         if (top != 0.0) {
                             Adjustment adjustment = areaInstance.getOrCreateAdjustment(rowUnit);
                             adjustment.top += top;
                         }
                     }
                 }
 
                 // Make sure there's always "px" adjustment layer so that
                 // we can shift right 30000px to cancel out the positioning
                 // from the main wrapping DIV.
                 Adjustment pxAdjustment = areaInstance.adjustments.remove("px");
 
                 if (pxAdjustment == null) {
                     pxAdjustment = new Adjustment();
                 }
 
                 pxAdjustment.left += 30000;
                 pxAdjustment.right -= 30000;
 
                 areaInstance.adjustments.put("px", pxAdjustment);
 
                 // Set width explicitly if there's only one unit.
                 CssCombinedUnit width = areaInstance.width = new CssCombinedUnit(columns.subList(columnStart, columnStop));
                 CssUnit singleWidth = areaInstance.singleWidth = width.getSingle();
 
                 if (singleWidth != null) {
                     pxAdjustment.width = singleWidth;
                 }
 
                 // Set height explicitly if there's only one unit.
                 CssCombinedUnit height = areaInstance.height = new CssCombinedUnit(rows.subList(rowStart, rowStop));
                 CssUnit singleHeight = areaInstance.singleHeight = height.getSingle();
 
                 if (singleHeight != null) {
                     pxAdjustment.height = singleHeight;
                 }
 
                 // Clear because of "auto" height?
                 if (clearAt >= 0 && clearAt <= rowStart) {
                     clearAt = -1;
                     areaInstance.clear = true;
                 }
 
                 if (height.hasAuto() && rowStop > clearAt) {
                     clearAt = rowStop;
                 }
             }
         }
 
         return areaInstances;
     }
 
     /**
      * Writes the given {@code object} and positions it according to the
      * grid rules as specified by the given parameters.
      *
      * @see #grid(Object, HtmlGrid)
      */
     public HtmlWriter writeGrid(Object object, String columns, String rows, String... template) throws IOException {
         return writeGrid(object, new HtmlGrid(columns, rows, template));
     }
 
     private static class Area {
 
         public final String name;
         public boolean clear;
         public double frPaddingLeft;
         public double frWidth;
         public CssCombinedUnit width;
         public CssUnit singleWidth;
         public CssCombinedUnit height;
         public CssUnit singleHeight;
         public final Map<String, Adjustment> adjustments = new LinkedHashMap<String, Adjustment>();
 
         public Area(String name) {
             this.name = name;
         }
 
         public Adjustment getOrCreateAdjustment(String unit) {
             Adjustment adjustment = adjustments.get(unit);
             if (adjustment == null) {
                 adjustment = new Adjustment();
                 adjustments.put(unit, adjustment);
             }
             return adjustment;
         }
     }
 
     private static class Adjustment {
 
         public double left;
         public double right;
         public double top;
         public CssUnit width;
         public CssUnit height;
 
         public String getMargin(String unit) {
             return new CssUnit(top, unit) + " " +
                     new CssUnit(right, unit) + " 0 " +
                     new CssUnit(left, unit);
         }
     }
 
     // --- Writer support ---
 
     @Override
     public Writer append(char letter) throws IOException {
         getDelegate().write(letter);
         return this;
     }
 
     @Override
     public Writer append(CharSequence text) throws IOException {
         getDelegate().append(text);
         return this;
     }
 
     @Override
     public Writer append(CharSequence text, int start, int end) throws IOException {
         getDelegate().append(text, start, end);
         return this;
     }
 
     @Override
     public void close() throws IOException {
         getDelegate().close();
     }
 
     @Override
     public void flush() throws IOException {
         getDelegate().flush();
     }
 
     @Override
     public void write(char[] buffer) throws IOException {
         getDelegate().write(buffer);
     }
 
     @Override
     public void write(char[] buffer, int offset, int length) throws IOException {
         getDelegate().write(buffer, offset, length);
     }
 
     @Override
     public void write(int letter) throws IOException {
         getDelegate().write(letter);
     }
 
     @Override
     public void write(String text) throws IOException {
         getDelegate().write(text);
     }
 
     @Override
     public void write(String text, int offset, int length) throws IOException {
         getDelegate().write(text, offset, length);
     }
 
     /** {@link HtmlWriter} utility methods. */
     public static final class Static {
 
         /** Returns a CSS string based on the given {@code properties}. */
         public static String cssString(Object... properties) {
             StringBuilder css = new StringBuilder();
 
             if (properties != null) {
                 for (int i = 1, length = properties.length; i < length; i += 2) {
                     Object property = properties[i - 1];
 
                     if (property != null) {
                         Object value = properties[i];
 
                         if (value != null) {
                             css.append(property);
                             css.append(':');
                             css.append(value);
                             css.append(';');
                         }
                     }
                 }
             }
 
             return css.toString();
         }
     }
 
     // --- Deprecated ---
 
     /** @deprecated Use {@link #writeHtmlOrDefault} instead. */
     @Deprecated
     public HtmlWriter stringOrDefault(Object string, String defaultString) throws IOException {
         return htmlOrDefault(string, defaultString);
     }
 
     /** @deprecated Use {@link #writeHtml} instead. */
     @Deprecated
     public HtmlWriter string(Object string) throws IOException {
         return html(string);
     }
 
     /** @deprecated Use {@link #writeTag} instead. */
     @Deprecated
     public HtmlWriter tag(String tag, Object... attributes) throws IOException {
         return writeTag(tag, attributes);
     }
 
     /** @deprecated Use {@link #writeStart} instead. */
     @Deprecated
     public HtmlWriter start(String tag, Object... attributes) throws IOException {
         return writeStart(tag, attributes);
     }
 
     /** @deprecated Use {@link #writeEnd} instead. */
     @Deprecated
     public HtmlWriter end() throws IOException {
         return writeEnd();
     }
 
     /** @deprecated Use {@link #writeHtmlOrDefault} instead. */
     @Deprecated
     public HtmlWriter htmlOrDefault(Object unescapedHtml, String defaultUnescapedHtml) throws IOException {
         return writeHtmlOrDefault(unescapedHtml, defaultUnescapedHtml);
     }
 
     /** @deprecated Use {@link #writeHtml} instead. */
     @Deprecated
     public HtmlWriter html(Object unescapedHtml) throws IOException {
         return writeHtml(unescapedHtml);
     }
 
     /** @deprecated Use {@link #writeObject} instead. */
     @Deprecated
     public HtmlWriter object(Object object) throws IOException {
         return writeObject(object);
     }
 
     /** @deprecated Use {@link #writeCss} instead. */
     @Deprecated
     public HtmlWriter css(String selector, Object... properties) throws IOException {
         return writeCss(selector, properties);
     }
 
     /** @deprecated Use {@link #writeGrid} instead. */
     @Deprecated
     public HtmlWriter grid(Object object, HtmlGrid grid, boolean inlineCss) throws IOException {
         return writeGrid(object, grid, inlineCss);
     }
 
     /** @deprecated Use {@link #writeGrid} instead. */
     @Deprecated
     public HtmlWriter grid(Object object, HtmlGrid grid) throws IOException {
         return writeGrid(object, grid);
     }
 
     /** @deprecated Use {@link #writeGrid} instead. */
     @Deprecated
     public HtmlWriter grid(Object object, String columns, String rows, String... template) throws IOException {
         return writeGrid(object, columns, rows, template);
     }
 
     /** @deprecated Use {@link #writeGrid(Object, HtmlGrid)} instead. */
     @Deprecated
     public HtmlWriter writeGrid(Object object, HtmlGrid grid, boolean inlineCss) throws IOException {
         return writeGrid(object, grid);
     }
 }
