 package Main;
 import org.jdom.*;
 import org.jdom.output.*;
 import java.util.*;
 
 //TODO: elements should be created with the defaultNS. Technically a lot of elements have no NS.
 // 		I'll add a createElement method later.
 public class XAALScripter {
 	private Document document = new Document();
 	private final Namespace defaultNS = Namespace.getNamespace("http://www.cs.hut.fi/Research/SVG/XAAL");
 	
 	public final int DEFAULT_FONT_SIZE = 16;
 	public final String DEFAULT_FONT_FAMILY = "Lucida Bright";
 	public final int DEFAULT_STROKE_WIDTH = 1;
 	
 	private int rectNum = 0;
 	private int textNum = 0;
 	private int lineNum = 0;
 	private int triangleNum = 0;
 	private int arrowNum = 0;
 	
 	private Element currentSlide = null;
 	private Element currentPar = null;
 	
 	public XAALScripter()
 	{
 		Element xaalRoot = createElement("xaal");
 		 
 		xaalRoot.setAttribute("version", 1.0 + "");
 		
 		Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
 		xaalRoot.addNamespaceDeclaration(xsi);
 		
 		Attribute schemaLocation = new Attribute("schemaLocation", 
 				"http://www.cs.hut.fi/Research/SVG/XAAL xaal.xsd", xsi);
 		xaalRoot.setAttribute(schemaLocation);
 		
 		Element initial = createElement("initial");
 		xaalRoot.addContent(initial);
 		
 		Element animation = createElement("animation");
 		xaalRoot.addContent(animation);
 		
 		document.setRootElement(xaalRoot);
 	}
 	
 	/**
 	 * Adds a new non-hidden, solid, black rectangle with default line width
 	 * to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangle.
 	 * @param y y coordinate for the top left corner of the rectangle.
 	 * @param width width of the rectangle in pixels.
 	 * @param height height of the rectangle in pixels.
 	 * @return a String containing the id of the rectangle added.
 	 */
 	public String addRectangle(int x, int y, int width, int height)
 	{
 		return addRectangle(x, y, width, height, "black");
 	}
 	
 	/**
 	 * Adds a new non-hidden, solid rectangle with default line width
 	 * to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangle.
 	 * @param y y coordinate for the top left corner of the rectangle.
 	 * @param width width of the rectangle in pixels.
 	 * @param height height of the rectangle in pixels.
 	 * @param color color of the rectangle's border. Must be a named XAAL color.
 	 * @return a String containing the id of the rectangle added.
 	 */
 	public String addRectangle(int x, int y, int width, int height, String color)
 	{
 		return addRectangle(x, y, width, height, "black", false);
 	}
 	
 	/**
 	 * Adds a new solid rectangle with default line width
 	 * to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangle.
 	 * @param y y coordinate for the top left corner of the rectangle.
 	 * @param width width of the rectangle in pixels.
 	 * @param height height of the rectangle in pixels.
 	 * @param color color of the rectangle's border. Must be a named XAAL color.
 	 * @param hidden specifies whether the rectangle should be hidden initially.
 	 * @return a String containing the id of the rectangle added.
 	 */
 	public String addRectangle(int x, int y, int width, int height, String color, boolean hidden)
 	{
 		return addRectangle(x, y, width, height, color, hidden, StrokeType.solid);
 	}
 	
 	/**
 	 * Adds a new rectangle to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangle.
 	 * @param y y coordinate for the top left corner of the rectangle.
 	 * @param width width of the rectangle in pixels.
 	 * @param height height of the rectangle in pixels.
 	 * @param color color of the rectangle's border. Must be a named XAAL color.
 	 * @param hidden specifies whether the rectangle should be hidden initially.
 	 * @param strokeType whether the line should be solid, dashed or dotted.
 	 * @return a String containing the id of the rectangle added.
 	 */
 	public String addRectangle(int x, int y, int width, int height, String color, boolean hidden,
 			StrokeType strokeType)
 	{
 		return addRectangle(x, y, width, height, color, hidden, strokeType, DEFAULT_STROKE_WIDTH);
 	}
 	
 	/**
 	 * Adds a new solid rectangle to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangle.
 	 * @param y y coordinate for the top left corner of the rectangle.
 	 * @param width width of the rectangle in pixels.
 	 * @param height height of the rectangle in pixels.
 	 * @param color color of the rectangle's border. Must be a named XAAL color.
 	 * @param hidden specifies whether the rectangle should be hidden initially.
 	 * @param lineWidth the width of the rectangle's border.
 	 * @return a String containing the id of the rectangle added.
 	 */
 	public String addRectangle(int x, int y, int width, int height, String color, boolean hidden,
 			int lineWidth)
 	{
 		return addRectangle(x, y, width, height, color, hidden, StrokeType.solid, lineWidth);
 	}
 	
 	/**
 	 * Adds a new solid rectangle to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangle.
 	 * @param y y coordinate for the top left corner of the rectangle.
 	 * @param width width of the rectangle in pixels.
 	 * @param height height of the rectangle in pixels.
 	 * @param color color of the rectangle's border. Must be a named XAAL color.
 	 * @param hidden specifies whether the rectangle should be hidden initially.
 	 * @param strokeType
 	 * @param lineWidth the width of the rectangle's border.
 	 * @return
 	 */
 	public String addRectangle(int x, int y, int width, int height, String color, boolean hidden,
 			StrokeType strokeType, int lineWidth)
 	{
 		Element initial = document.getRootElement().getChild("initial", defaultNS);
 		
 		Element rect = createElement("polyline");
 		
 		String idVal = "rectangle" + rectNum;
 		rectNum++;
 		
 		rect.setAttribute("id", idVal );
 		
 		rect.setAttribute("hidden", hidden + "");
 		
 		Element x1y1 = createElement("coordinate");
 		x1y1.setAttribute("x", x + "");
 		x1y1.setAttribute("y", y + "");
 		rect.addContent(x1y1);
 		
 		Element x1y2 = createElement("coordinate");
 		x1y2.setAttribute("x", x + "" );
 		x1y2.setAttribute("y", (y+height) + "");
 		rect.addContent(x1y2);
 		
 		Element x2y2 = createElement("coordinate");
 		x2y2.setAttribute("x", (x+width) + "");
 		x2y2.setAttribute("y", (y+height) + "");
 		rect.addContent(x2y2);
 		
 		Element x2y1 = createElement("coordinate");
 		x2y1.setAttribute("x", (x+width) + "");
 		x2y1.setAttribute("y", y + "");
 		rect.addContent(x2y1);
 		
 		Element x1y1_2 = createElement("coordinate");
 		x1y1_2.setAttribute("x", x + "");
 		x1y1_2.setAttribute("y", y + "");
 		rect.addContent(x1y1_2);
 		
 		Element closed = createElement("closed");
 		closed.setAttribute("value", true + "");
 		rect.addContent(closed);
 		
 		Element style = createElement("style");
 		
 		Element colorElem = createElement("color");
 		colorElem.setAttribute("name", color);
 		style.addContent(colorElem);
 		
 		Element strokeElem = createElement("stroke");
 		strokeElem.setAttribute("width", lineWidth + "");
 		strokeElem.setAttribute("type", strokeType.name());
 		style.addContent(strokeElem);
 		
 		rect.addContent(style);
 		
 		initial.addContent(rect);
 		
 		return idVal;
 	}
 	
 	//TODO: what to call the text?
 	/**
 	 * Adds a new black, non-hidden, text(?) with default font size
 	 * and family to the initial element of the XAAL script.
 	 * @param x x coordinate for the top left corner of the text(?)
 	 * @param y y coordinate for the top left corner of the text(?)
 	 * @param contents the text string to be displayed.
 	 * @return a String containing the id of the text(?) added.
 	 */
 	public String addText(int x, int y, String contents)
 	{
 		return addText(x, y, contents, "black");
 	}
 	
 	/**
 	 * Adds a new non-hidden, text(?) with default font size
 	 * and family to the initial element of the XAAL script.
 	 * @param x x coordinate for the top left corner of the text(?)
 	 * @param y y coordinate for the top left corner of the text(?)
 	 * @param contents the text string to be displayed.
 	 * @param color the color of the text string. Must be a named XAAL color.
 	 * @return a String containing the id of the text(?) added.
 	 */
 	public String addText(int x, int y, String contents, String color)
 	{
 		return addText(x, y, contents, color, false);
 	}
 	
 	/**
 	 * Adds a new text(?) with default font size
 	 * and family to the initial element of the XAAL script.
 	 * @param x x coordinate for the top left corner of the text(?)
 	 * @param y y coordinate for the top left corner of the text(?)
 	 * @param contents the text string to be displayed.
 	 * @param color the color of the text string. Must be a named XAAL color.
 	 * @param hidden whether the text should be initially hidden.
 	 * @return a String containing the id of the text(?) added.
 	 */
 	public String addText(int x, int y, String contents, String color, boolean hidden)
 	{
 		return addText(x, y, contents, color, hidden, DEFAULT_FONT_SIZE);
 	}
 
 	/**
 	 * Adds a new text(?) with default font family to the initial 
 	 * element of the XAAL script.
 	 * @param x x coordinate for the top left corner of the text(?)
 	 * @param y y coordinate for the top left corner of the text(?)
 	 * @param contents the text string to be displayed.
 	 * @param color the color of the text string. Must be a named XAAL color.
 	 * @param hidden whether the text should be initially hidden.
 	 * @return a String containing the id of the text(?) added.
 	 * @param fontSize the font size of the text, in points.
 	 * @return a String containing the id of the text(?) added.
 	 */
 	public String addText(int x, int y, String contents, String color, boolean hidden, int fontSize)
 	{
 		return addText(x, y, contents, color, hidden, fontSize, DEFAULT_FONT_FAMILY);
 	}
 	
 	/**
 	 * Adds a new text(?) to the initial element of the XAAL script.
 	 * @param x x coordinate for the top left corner of the text(?)
 	 * @param y y coordinate for the top left corner of the text(?)
 	 * @param contents the text string to be displayed.
 	 * @param color the color of the text string. Must be a named XAAL color.
 	 * @param hidden whether the text should be initially hidden.
 	 * @return a String containing the id of the text(?) added.
 	 * @param fontSize the font size of the text, in points.
 	 * @param fontFamily the name of font family to use when displaying text.
 	 * @return a String containing the id of the text(?) added.
 	 */
 	public String addText(int x, int y, String contents, String color, boolean hidden,
 			int fontSize, String fontFamily)
 	{
 		Element initial = document.getRootElement().getChild("initial", defaultNS);
 		
 		Element text = createElement("text");
 		
 		String idVal = "text" + textNum;
 		textNum++;
 		text.setAttribute("id", idVal);
 		
 		text.setAttribute("hidden", hidden + "");
 		
 		Element coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", x + "");
 		coordinate.setAttribute("y", y + "");
 		text.addContent(coordinate);
 		
 		Element contentsElem = createElement("contents");
 		
 		Text contentsVal = new Text(contents);
 		contentsElem.addContent(contentsVal);
 		
 		text.addContent(contentsElem);
 		
 		Element style = createElement("style");
 		
 		Element colorElem = createElement("color");
 		colorElem.setAttribute("name", color);
 		style.addContent(colorElem);
 		
 		Element font = createElement("font");
 		font.setAttribute("size", fontSize + "");
 		font.setAttribute("family", fontFamily);
 		style.addContent(font);
 		
 		text.addContent(style);
 		
 		initial.addContent(text);
 		
 		return idVal;
 	}
 	
 	/**
 	 * Adds a new non-hidden, solid, black line with the default line width 
 	 * to the initial element of the XAAL script.
 	 * @param x1 x coordinate for first point.
 	 * @param y1 y coordinate for first point.
 	 * @param x2 x coordinate for second point.
 	 * @param y2 y coordinate for second point.
 	 * @return a String containing the id of the line added.
 	 */
 	public String addLine(int x1, int y1, int x2, int y2)
 	{
 		return addLine(x1, y1, x2, y2, "black");
 	}
 	
 	/**
 	 * Adds a new non-hidden, solid line with the default line width 
 	 * to the initial element of the XAAL script.
 	 * @param x1 x coordinate for first point.
 	 * @param y1 y coordinate for first point.
 	 * @param x2 x coordinate for second point.
 	 * @param y2 y coordinate for second point.
 	 * @param color the color of line. Must be a named XAAL color.
 	 * @return a String containing the id of the line added.
 	 */
 	public String addLine(int x1, int y1, int x2, int y2, String color)
 	{
 		return addLine(x1, y1, x2, y2, color, false);
 	}
 	
 	/**
 	 * Adds a new solid line with the default line width
 	 * to the initial element of the XAAL script.
 	 * @param x1 x coordinate for first point.
 	 * @param y1 y coordinate for first point.
 	 * @param x2 x coordinate for second point.
 	 * @param y2 y coordinate for second point.
 	 * @param color the color of line. Must be a named XAAL color.
 	 * @param hidden whether the line should be initially hidden.
 	 * @return a String containing the id of the line added.
 	 */
 	public String addLine(int x1, int y1, int x2, int y2, String color, boolean hidden)
 	{
 		return addLine(x1, y1, x2, y2, color, hidden, StrokeType.solid);
 	}
 	
 	/**
 	 * Adds a new line with the default line width
 	 * to the initial element of the XAAL script.
 	 * @param x1 x coordinate for first point.
 	 * @param y1 y coordinate for first point.
 	 * @param x2 x coordinate for second point.
 	 * @param y2 y coordinate for second point.
 	 * @param color the color of line. Must be a named XAAL color.
 	 * @param hidden whether the line should be initially hidden.
 	 * @param strokeType whether the line should be solid, dashed or dotted.
 	 * @return a String containing the id of the line added.
 	 */
 	public String addLine(int x1, int y1, int x2, int y2, String color, 
 			boolean hidden, StrokeType strokeType)
 	{
 		return addLine(x1, y1, x2, y2, color, hidden, strokeType, DEFAULT_STROKE_WIDTH);
 	}
 	
 	/**
 	 * Adds a new solid line to the initial element of the XAAL script.
 	 * @param x1 x coordinate for first point.
 	 * @param y1 y coordinate for first point.
 	 * @param x2 x coordinate for second point.
 	 * @param y2 y coordinate for second point.
 	 * @param color the color of line. Must be a named XAAL color.
 	 * @param hidden whether the line should be initially hidden.
 	 * @param lineWidth the width of the line.
 	 * @return a String containing the id of the line added.
 	 */
 	public String addLine(int x1, int y1, int x2, int y2, String color, 
 			boolean hidden, int lineWidth)
 	{
 		return addLine(x1, y1, x2, y2, color, hidden, StrokeType.solid, lineWidth );
 	}
 	
 	/**
 	 * Adds a new line to the initial element of the XAAL script.
 	 * @param x1 x coordinate for first point.
 	 * @param y1 y coordinate for first point.
 	 * @param x2 x coordinate for second point.
 	 * @param y2 y coordinate for second point.
 	 * @param color the color of line. Must be a named XAAL color.
 	 * @param hidden whether the line should be initially hidden.
 	 * @param strokeType whether the line should be solid, dashed or dotted.
 	 * @param lineWidth the width of the line.
 	 * @return a String containing the id of the line added.
 	 */
 	public String addLine(int x1, int y1, int x2, int y2, String color, 
 			boolean hidden, StrokeType strokeType, int lineWidth)
 	{
 		Element initial = document.getRootElement().getChild("initial", defaultNS);
 		
 		Element line = createElement("line");
 		
 		String idVal = "line" + lineNum;
 		lineNum++;
 		line.setAttribute("id", idVal);
 		
 		line.setAttribute("hidden", hidden + "");
 		
 		Element coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", x1 + "");
 		coordinate.setAttribute("y", y1 + "");
 		line.addContent(coordinate);
 		
 		coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", x2 + "");
 		coordinate.setAttribute("y", y2 + "");
 		line.addContent(coordinate);
 		
 		Element style = createElement("style");
 		
 		Element colorElem = createElement("color");
 		colorElem.setAttribute("name", color);
 		style.addContent(colorElem);
 		
 		Element stroke = createElement("stroke");
 		stroke.setAttribute("type", strokeType.name());
 		stroke.setAttribute("width", lineWidth + "");
 		style.addContent(stroke);
 		
 		line.addContent(style);
 		
 		initial.addContent(line);
 		
 		return idVal;
 	}
 	
 	/**
 	 * Adds a new non-hidden, black, solid equilateral triangle with the default line width
 	 * to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param y y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param width width (and height) of the triangle in pixels.
 	 * @return the String containing the id of the triangle added.
 	 */
 	public String addTriangle(int x, int y, int width)
 	{
 		return addTriangle(x, y, width, "black");
 	}
 	
 	/**
 	 * Adds a new non-hidden, solid equilateral triangle with the default line width
 	 * to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param y y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param width width (and height) of the triangle in pixels.
 	 * @param color the border color of the triangle
 	 * @return the String containing the id of the triangle added.
 	 */
 	public String addTriangle(int x, int y, int width, String color)
 	{
 		return addTriangle(x, y, width, color, false);
 	}
 	
 	/**
 	 * Adds a new equilateral, solid triangle with the default line width
 	 * to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param y y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param width width (and height) of the triangle in pixels.
 	 * @param color the border color of the triangle.
 	 * @param hidden whether the triangle is hidden initially.
 	 * @return the String containing the id of the triangle added.
 	 */
 	public String addTriangle(int x, int y, int width, String color, boolean hidden)
 	{
 		return addTriangle(x, y, width, color, hidden, StrokeType.solid);
 	}
 	
 	/**
 	 * Adds a new equilateral triangle with the default line width
 	 * to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param y y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param width width (and height) of the triangle in pixels.
 	 * @param color the border color of the triangle.
 	 * @param hidden whether the triangle is hidden initially.
 	 * @param strokeType whether the line should be solid, dashed or dotted.
 	 * @return the String containing the id of the triangle added.
 	 */
 	public String addTriangle(int x, int y, int width, String color, boolean hidden, 
 			StrokeType strokeType)
 	{
 		return addTriangle(x,y, width, color, hidden, strokeType, DEFAULT_STROKE_WIDTH);
 	}
 	
 	/**
 	 * Adds a new equilateral, solid triangle to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param y y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param width width (and height) of the triangle in pixels.
 	 * @param color the border color of the triangle.
 	 * @param hidden whether the triangle is hidden initially.
 	 * @param lineWidth the width of the border line.
 	 * @return the String containing the id of the triangle added.
 	 */
 	public String addTriangle(int x, int y, int width, String color, boolean hidden,
 			int lineWidth)
 	{
 		return addTriangle(x,y, width, color, hidden, StrokeType.solid, lineWidth);
 	}
 	
 	/**
 	 * Adds a new equilateral triangle with the default line width
 	 * to the initial element of a XAAL script.
 	 * @param x y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param y y coordinate for the top left corner of the rectangular box containing the triangle.
 	 * @param width width (and height) of the triangle in pixels.
 	 * @param color the border color of the triangle.
 	 * @param hidden whether the triangle is hidden initially.
 	 * @param strokeType whether the line should be solid, dashed or dotted.
 	 * @param lineWidth the width of the line.
 	 * @return the String containing the id of the triangle added.
 	 */
 	public String addTriangle(int x, int y, int width, String color, boolean hidden, 
 			StrokeType strokeType, int lineWidth)
 	{
 		Element initial = document.getRootElement().getChild("initial", defaultNS);
 		
 		Element triangle = createElement("polyline");
 		
		String idVal = "triangle" + lineNum;
 		triangleNum++;
 		triangle.setAttribute("id", idVal);
 		
 		triangle.setAttribute("hidden", hidden + "");
 		
 		Element coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", (x +(width/2)) + "");
 		coordinate.setAttribute("y", y + "");
 		triangle.addContent(coordinate);
 		
 		
 		coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", (x + width) + "");
 		coordinate.setAttribute("y", (y + width) + "");
 		triangle.addContent(coordinate);
 		
 		coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", (x) + "");
 		coordinate.setAttribute("y", (y + width) + "");
 		triangle.addContent(coordinate);
 		
 		coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", (x +(width/2)) + "");
 		coordinate.setAttribute("y", y + "");
 		triangle.addContent(coordinate);
 		
 		Element closed = createElement("closed");
 		closed.setAttribute("value", true + "");
 		triangle.addContent(closed);
 		
 		Element style = createElement("style");
 		
 		Element colorElem = createElement("color");
 		colorElem.setAttribute("name", color);
 		style.addContent(colorElem);
 		
 		Element stroke = createElement("stroke");
 		stroke.setAttribute("type", strokeType.name());
 		stroke.setAttribute("width", lineWidth + "");
 		style.addContent(stroke);
 		
 		triangle.addContent(style);
 		
 		initial.addContent(triangle);
 		
 		return idVal;
 	}
 	
 	/**
 	 * Adds a new solid arrow with default line width
 	 * to the initial element of the XAAL script.
 	 * @param originName the element the arrow comes from.
 	 * @param destName the element the arrow goes to.
 	 * @param padding padding around... something?
 	 * @param isDashed whether the line is dashed or solid
 	 * @param isHidden should the arrow be hidden initially.
 	 * @return the String containing the id of the arrow added.
 	 */
 	public String addArrow(String originName, String destName, int padding, boolean isDashed, 
 			boolean isHidden)
 	{
 		StrokeType type = StrokeType.solid;
 		
 		if (isDashed)
 			type = StrokeType.dashed;
 		
 		return addArrow(originName, destName, padding, "black", isHidden, type, DEFAULT_STROKE_WIDTH);
 	}	
 	
 	/**
 	 * Adds a new solid arrow with default line width
 	 * to the initial element of the XAAL script.
 	 * @param originName the element the arrow comes from.
 	 * @param destName the element the arrow goes to.
 	 * @param padding padding around... something?
 	 * @param color color of the arrow.
 	 * @param hidden whether the arrow is hidden initially.
 	 * @param strokeType whether the line is solid, dashed or dotted.
 	 * @param lineWidth the width of the line.
 	 * @return the String containing the id of the arrow added.
 	 */
 	public String addArrow(String originName, String destName, int padding, String color,
 			boolean hidden, StrokeType strokeType, int lineWidth)
 	{
 		Element initial = document.getRootElement().getChild("initial", defaultNS);
 		
 		List elements = initial.getChildren();
 		Element origin = null;
 		Element dest = null;
 		for (Object o : elements)
 		{
 			Element e = (Element) o;
 			Attribute a = e.getAttribute("id");
 			System.out.println(a);
 			if (e.getAttribute("id").getValue().equals(originName))
 			{
 				origin = e;
 			}
 			else if (e.getAttribute("id").getValue().equals(destName))
 			{
 				dest = e;
 			}
 		}
 		//find our starting point
 
 		Element startPos = origin.getChild("coordinate", defaultNS);
 		Element endPos = dest.getChild("coordinate", defaultNS);
 		int startX = 0;
 		int startY = 0;
 		int endX = 0;
 		int endY= 0;
 		try
 		{	
 			startX = startPos.getAttribute("x").getIntValue();
 			startY = startPos.getAttribute("y").getIntValue();
 			System.out.println("X: " + startX + " Y: " + startY);
 			
 			endX = endPos.getAttribute("x").getIntValue();
 			endY = endPos.getAttribute("y").getIntValue();
 			System.out.println("End X: " + endX + " End Y: " + endY);
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 		}
 		
 		Element arrow = createElement("polyline");
 		
 		Element coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", startX + "");
 		coordinate.setAttribute("y", startY + "");
 		arrow.addContent(coordinate);
 				
 		coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", (startX - padding) + "");
 		coordinate.setAttribute("y", startY + "");
 		arrow.addContent(coordinate);
 				
 		coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", (startX - padding) + "");
 		coordinate.setAttribute("y", endY + "");
 		arrow.addContent(coordinate);
 				
 		coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", endX + "");
 		coordinate.setAttribute("y", endY + "");
 		arrow.addContent(coordinate);
 				
 
 		coordinate = new Element("coordinate");
 		coordinate.setAttribute("x", (endX -10) + "");
 		coordinate.setAttribute("y", (endY -10) + "");
 
 		arrow.addContent(coordinate);
 				
 		coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", endX + "");
 		coordinate.setAttribute("y", endY + "");
 		arrow.addContent(coordinate);
 		
 
 		coordinate = new Element("coordinate");
 		coordinate.setAttribute("x", (endX -10) + "");
 		coordinate.setAttribute("y", (endY +10) + "");
 
 		arrow.addContent(coordinate);
 		
 		arrow.setAttribute("hidden", hidden + "");
 		
 		Element style = createElement("style");
 		
 		Element colorElem = createElement("color");
 		colorElem.setAttribute("name", color);
 		style.addContent(colorElem);
 		
 		Element stroke = createElement("stroke");
 		stroke.setAttribute("type", strokeType.name());
 		stroke.setAttribute("width", lineWidth + "");
 		style.addContent(stroke);
 		
 		arrow.addContent(style);
 		
 		String idVal = "arrow" + arrowNum++;
 		arrow.setAttribute("id", idVal);
 		
 		
 		
 		initial.addContent(arrow);
 		
 		return idVal;
 	}
 	
 	//TODO: do we want to create specific exceptions for when slides have 
 	// already been started or just general exceptions?
 	
 	/**
 	 * Begins a slide for the animation. Corresponds to the seq element.
 	 * @throws SlideException
 	 */
 	public void startSlide() throws SlideException
 	{
 		if (inSlide())
 			throw new SlideException("A slide has already been started. " +
 					"It must be ended before you can create another.");
 		
 		currentSlide = createElement("seq");
 	}
 	
 	/**
 	 * Closes the current open slide and inserts it in the animation section 
 	 * of the XAAL script.
 	 * @throws Exception
 	 */
 	public void endSlide() throws SlideException
 	{
 		if (!inSlide())
 			throw new SlideException("No slide has been started yet.");
 		
 		Element animation = document.getRootElement().getChild("animation", defaultNS);
 		
 		animation.addContent(currentSlide);
 		
 		currentSlide = null;
 	}
 	
 	/**
 	 * Tells us if a slide has been started but not ended.
 	 * @return whether a slide is started.
 	 */
 	public boolean inSlide()
 	{
 		return currentSlide != null;
 	}
 	
 	/**
 	 * Begins a section that runs multiple changes in parallel.
 	 * Corresponds to the par element in XAAL.
 	 * @throws SlideException
 	 * @throws ParException
 	 */
 	public void startPar() throws SlideException, ParException
 	{
 		if (!inSlide())
 			throw new SlideException("No slide is open. Parallel sections can only be " +
 					"added to open slides");
 		
 		if (inPar())
 			throw new ParException("Parallel section has already been started. " +
 					"It must be ended before you can create another.");
 		
 		currentPar = createElement("par");
 	}
 	
 	/**
 	 * Closes a parallel section and writes it to the animation section of 
 	 * the XAAL script.
 	 * @throws ParException
 	 */
 	public void endPar() throws ParException
 	{
 		if (!inPar())
 			throw new ParException("No parallel section");
 		
 		currentSlide.addContent(currentPar);
 		
 		currentPar = null;
 	}
 	
 	/**
 	 * Tells us if a parallel section has been started but not ended.
 	 * @return whether a parallel section is started.
 	 */
 	public boolean inPar()
 	{
 		return currentPar != null;
 	}
 	
 	/**
 	 * Adds a translate action to the open parallel section or creates one if necessary.
 	 * @param x the number pixels the objects should move right. Use negative if you
 	 * want to move left.
 	 * @param y the number pixels the objects should move down. Use negative if you want to move up.
 	 * @param ids a variable number of Strings containing the ids of objects to be moved.
 	 * @throws SlideException
 	 */
 	public void addTranslate(int x, int y, String...ids) throws SlideException
 	{
 		if (!inSlide())
 			throw new SlideException("You must create a slide before creating actions.");
 		
 		boolean closeParAtEnd = false;
 		
 		if(!inPar())
 		{
 			try {
 				startPar();
 			} catch (ParException e) {
 				
 				e.printStackTrace();
 			}
 			closeParAtEnd = true;
 		}
 		
 		Element parent = currentPar;
 		
 		Element move = createElement("move");
 		move.setAttribute("type", "translate");
 		
 		for (String id : ids)
 		{
 			Element objRef = createElement("object-ref");
 			objRef.setAttribute("id", id);
 			move.addContent(objRef);
 		}
 		
 		Element coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", x + "");
 		coordinate.setAttribute("y", y + "");
 		move.addContent(coordinate);
 		
 		parent.addContent(move);
 		
 		if (closeParAtEnd)
 			try {
 				endPar();
 			} catch (ParException e) {
 				
 				e.printStackTrace();
 			}
 	}
 	
 	/**
 	 * Adds a show action to the open parallel section or creates one if necessary.
 	 * @param ids a variable number of Strings containing the ids of objects to be shown.
 	 * @throws SlideException
 
 	 */
 	public void addShow(String...ids) throws SlideException
 	{
 		if (!inSlide())
 			throw new SlideException("You must create a slide before creating actions.");
 		
 		boolean closeParAtEnd = false;
 		
 		if(!inPar())
 		{
 			try {
 				startPar();
 			} catch (ParException e) {
 				
 				e.printStackTrace();
 			}
 			closeParAtEnd = true;
 		}
 		
 		Element parent = currentPar;
 		
 		Element show = createElement("show");
 		show.setAttribute("type", "selected");
 		
 		for (String id : ids)
 		{
 			Element objRef = createElement("object-ref");
 			objRef.setAttribute("id", id);
 			show.addContent(objRef);
 		}
 		
 		parent.addContent(show);		
 		
 		if (closeParAtEnd)
 			try {
 				endPar();
 			} catch (ParException e) {
 				
 				e.printStackTrace();
 			}
 	}
 	
 	/**
 	 * Adds a hide action to the open parallel section or creates one if necessary.
 	 * @param ids a variable number of Strings containing the ids of objects to be hidden.
 	 * @throws SlideException
 	 */
 	public void addHide(String...ids) throws SlideException
 	{
 		if (!inSlide())
 			throw new SlideException("You must create a slide before creating actions.");
 		
 		boolean closeParAtEnd = false;
 		
 		if(!inPar())
 		{
 			try {
 				startPar();
 			} catch (ParException e) {
 				
 				e.printStackTrace();
 			}
 			closeParAtEnd = true;
 		}
 		
 		Element parent = currentPar;
 		
 		Element hide = createElement("hide");
 		hide.setAttribute("type", "selected");
 		
 		for (String id : ids)
 		{
 			Element objRef = createElement("object-ref");
 			objRef.setAttribute("id", id);
 			hide.addContent(objRef);
 		}
 		
 		parent.addContent(hide);		
 		
 		if (closeParAtEnd)
 			try {
 				endPar();
 			} catch (ParException e) {
 				
 				e.printStackTrace();
 			}
 	}
 	
 	/**
 	 * Adds a change-style action to the open parallel section or creates one if necessary.
 	 * @param color the new color of the objects being changed.
 	 * @param ids a number of Strings containing the ids of the objects to be modified.
 	 * @throws SlideException
 	 */
 	public void addChangeStyle(String color, String...ids) throws SlideException
 	{
 		if (!inSlide())
 			throw new SlideException("You must create a slide before creating actions.");
 		
 		boolean closeParAtEnd = false;
 		
 		if(!inPar())
 		{
 			try {
 				startPar();
 			} catch (ParException e) {
 				
 				e.printStackTrace();
 			}
 			closeParAtEnd = true;
 		}
 		
 		Element parent = currentPar;
 		
 		Element changeStyle = createElement("change-style");
 		
 		for (String id : ids)
 		{
 			Element objRef = createElement("object-ref");
 			objRef.setAttribute("id", id);
 			changeStyle.addContent(objRef);
 		}
 		
 		Element style = createElement("style");
 		
 		Element colorElem = createElement("color");
 		colorElem.setAttribute("name", color);
 		
 		style.addContent(colorElem);
 		
 		changeStyle.addContent(style);
 		
 		parent.addContent(changeStyle);
 		
 		if (closeParAtEnd)
 			try {
 				endPar();
 			} catch (ParException e) {
 				
 				e.printStackTrace();
 			}
 	}
 	
 	/**
 	 * Adds a move for no object. Used for current pause hack. Move not implemented.
 	 * @param x x coordinate of point to move objects to. Currently the number of ms to pause.
 	 * @param y y coordinate of point to move objects to. Must be 0 for pause to work.
 	 * @throws SlideException
 	 */
 	private void addMove(int x, int y) throws SlideException
 	{
 		addMove(x, y, new String[0]);
 	}
 	
 	/**
 	 * Adds a move for no object. Used for current pause hack. Move not implemented.
 	 * @param x x coordinate of point to move objects to. Currently the number of ms to pause.
 	 * @param y y coordinate of point to move objects to. Must be 0 for pause to work.
 	 * @param ids list of Strings containing the ids of the objects to move. 
 	 * Make it empty for pause to work
 	 * @throws SlideException
 	 */
 	private void addMove(int x, int y, String...ids) throws SlideException
 	{
 		if (!inSlide())
 			throw new SlideException("You must create a slide before creating actions.");
 		
 		boolean closeParAtEnd = false;
 		
 		if(!inPar())
 		{
 			try {
 				startPar();
 			} catch (ParException e) {
 			
 				e.printStackTrace();
 			}
 			closeParAtEnd = true;
 		}
 		
 		Element parent = currentPar;
 		
 		Element move = createElement("move");
 		move.setAttribute("type", "move");
 		
 	
 		for (String id : ids)
 		{
 			Element objRef = createElement("object-ref");
 			objRef.setAttribute("id", id);
 			move.addContent(objRef);
 		}
 		
 		Element coordinate = createElement("coordinate");
 		coordinate.setAttribute("x", x + "");
 		coordinate.setAttribute("y", y + "");
 		move.addContent(coordinate);
 		
 		parent.addContent(move);
 		
 		if (closeParAtEnd)
 			try {
 				endPar();
 			} catch (ParException e) {
 				
 				e.printStackTrace();
 			}
 	}
 	
 	/**
 	 * Adds a pause to the open parallel section or creates one if necessary.
 	 * @param ms how long to pause in milliseconds.
 	 * @throws SlideException
 	 */
 	public void addPause(int ms) throws SlideException
 	{
 		addMove(ms, 0);
 	}
 	
 	/**
 	 * Adds narrative to the current slide. Currently is used for pseudocode url.
 	 * @param text the narrative for the slide. Currently is just pseudocode url.
 	 * @throws SlideException
 	 */
 	private void addNarrative(String text) throws SlideException
 	{
 		if (!inSlide())
 			throw new SlideException("You must create a slide before creating actions.");
 		
 		Element narrative = createElement("narrative");
 		
 		narrative.setText(text);
 		
 		currentSlide.addContent(narrative);
 	}
 	
 	/**
 	 * Adds a pseudocode URL to the current slide
 	 * @param url the URL of the page to be displayed in the pseudocode pane.
 	 * @throws Exception
 	 */
 	public void addPseudocodeUrl(String url) throws SlideException
 	{
 			addNarrative(url);
 	}
 	
 	/**
 	 * Adds a TF Question to the current slide
 	 * @param question
 	 * @param answer
 	 */
 	public void addTFQuestion(String question, boolean answer) throws SlideException
 	{
 		
 	}
 	
 	/**
 	 * Adds a Fill-in-the-blank question to the current slide
 	 * @param question
 	 * @param answers
 	 */
 	public void addFibQuestion(String question, String...answers) throws SlideException
 	{
 		
 	}
 	
 	/**
 	 * Adds a multiple choice, single selection question to the current slide
 	 * @param question
 	 * @param choices
 	 * @param answer
 	 */
 	public void addMCQuestion(String question, String[] choices,  
 			int answer) throws SlideException
 	{
 		
 	}
 	
 	/**
 	 * Adds a multiple choice, multiple selection question to the current slide
 	 * @param question
 	 * @param choices
 	 * @param answers
 	 */
 	public void	addMSQuestion(String question, String[] choices, 
 			int...answers) throws SlideException
 	{
 		
 	}
 	
 	private void createQuestion(String question, String[] choices, String[] answers){}
 	
 	public void createQuestion(String question, boolean answer)
 	{
 		
 	}
 	
 	public void createQuestion(String question, String[] answers)
 	{
 		
 	}
 	
 	public void createQuestion(String question, String[] choices, int answer)
 	{
 		
 	}
 	
 	public void createQuestion(String question, String[] choices, int...answers)
 	{
 		
 	}
 	
 	
 	/**
 	 * Creates an element in the defaultNS
 	 * @param name the name of the element
 	 * @return an Element with the specified name and belonging to defaultNS
 	 */
 	private Element createElement(String name)
 	{
 		return new Element(name, defaultNS);
 	}
 	
 	//TODO: just for testing!
 	public String toString()
 	{
 		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
 		return outputter.outputString(document);
 	}
 }
