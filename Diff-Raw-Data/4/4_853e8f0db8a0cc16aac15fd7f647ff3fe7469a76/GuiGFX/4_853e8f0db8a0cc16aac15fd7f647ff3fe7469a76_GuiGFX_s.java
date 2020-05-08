 
 /**************************************************************
  *
  * - CeCILL-B license
  * - (bsd-like, check http://www.cecill.info/faq.en.html#bsd)
  * 
  * Copyright CNRS
  * Contributors:
  * David Gauchard <gauchard@laas.fr>	2013-01-01
  * Adrien Thibaud <adrien.thibaud@etu.enseeiht.fr>	2013-09-01
  * 
  * This software is a computer program whose purpose is to
  * provide a "Graphical Access To Exterior" (GATE).  The goal
  * is to provide a generic GUI, within a javascript web
  * browser, through a TCP network using the websocket protocol. 
  * Plain text protocol (simple human readable graphic commands)
  * translators to websockets protocol are also provided to
  * connect user applications to the browser via a C library or
  * simple TCP server.
  * 
  * This software is governed by the CeCILL-B license under
  * French law and abiding by the rules of distribution of free
  * software.  You can use, modify and/ or redistribute the
  * software under the terms of the CeCILL-B license as
  * circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info".
  * 
  * As a counterpart to the access to the source code and rights
  * to copy, modify and redistribute granted by the license,
  * users are provided only with a limited warranty and the
  * software's author, the holder of the economic rights, and
  * the successive licensors have only limited liability.
  * 
  * In this respect, the user's attention is drawn to the risks
  * associated with loading, using, modifying and/or developing
  * or reproducing the software by the user in light of its
  * specific status of free software, that may mean that it is
  * complicated to manipulate, and that also therefore means
  * that it is reserved for developers and experienced
  * professionals having in-depth computer knowledge.  Users are
  * therefore encouraged to load and test the software's
  * suitability as regards their requirements in conditions
  * enabling the security of their systems and/or data to be
  * ensured and, more generally, to use and operate it in the
  * same conditions as regards security.
  * 
  * The fact that you are presently reading this means that you
  * have had knowledge of the CeCILL-B license and that you
  * accept its terms.
  * 
  *************************************************************/
 
 
 package fr.laas.gate;
 
 // http://code.google.com/p/gwt-graphics/wiki/Manual
 // http://hene.virtuallypreinstalled.com/gwt-graphics/javadoc/
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.vaadin.gwtgraphics.client.DrawingArea;
 import org.vaadin.gwtgraphics.client.shape.Ellipse;
 import org.vaadin.gwtgraphics.client.shape.Path;
 import org.vaadin.gwtgraphics.client.shape.Text;
 import org.vaadin.gwtgraphics.client.Image;
 import org.vaadin.gwtgraphics.client.shape.path.LineTo;
 import org.vaadin.gwtgraphics.client.shape.path.MoveTo;
 import org.vaadin.gwtgraphics.client.VectorObject;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 
 
 class GuiGFX extends GuiPanel
 {	
 	private static final int pixelWidth = 1;
 	private static final int arrowDivider = 3;
 	private static final float arrowPercent = 8f;
 	private String defColor = null, defBColor = null; 
 	
 	protected final class Gfx
 	{
 		public class xy
 		{
 			public xy (float x, float y)
 			{
 				this.x = x;
 				this.y = y;
 			}
 			float x, y;
 		};
 
 		public VectorObject gfx;
 		public float x1, y1, x2, y2;
 		public Boolean drawn = false;
 		public ArrayList<xy> path = null;
 		
 		public Gfx (final VectorObject gfx, final float x1, final float y1, final float x2, final float y2)
 		{
 			this.gfx = gfx;
 			this.x1 = x1;
 			this.y1 = y1;
 			this.x2 = x2;
 			this.y2 = y2;
 			this.drawn = false;
 		}
 	}
 	
 	protected final DrawingArea			area;
 	protected final HashMap<String, Gfx>	gfx = new HashMap<String, Gfx>();
 	
 	public GuiGFX (final IntfObject parent, final String name)
 	{
 		super(parent, name);
 		area = new DrawingArea(0, 0);
 		add(area);
 	}
 		
 	public static String help ()
 	{
 		return
 						  "# \tadd <name> <gfxtype*> <args..>\tadd a graphics object"
 			+ Gate.endl + "# \tdel <name>\t\t\tdelete the named object"
 			+ Gate.endl + "# \tcolor <name>|default <color*>\tchange color"
 			+ Gate.endl + "# \tbcolor <name>|default <color*>\tchange border color"
 			+ Gate.endl + "# \tenable <name>\t\t\tenable event"
 			+ Gate.endl + "# \t* all coordinates are in % only *"
 			+ Gate.endl + "# \t\t* gfxtype and args are:"
 			+ Gate.endl + "# \t\t  - circle    - x y radius"
 			+ Gate.endl + "# \t\t  - ellipse   - x y xradius yradius"
 			+ Gate.endl + "# \t\t  - rectangle - x1 y1 x2 y2"
 			+ Gate.endl + "# \t\t  - line      - x1 y1 x2 y2"
 			+ Gate.endl + "# \t\t  - arrow     - x1 y1 x2 y2"
 			+ Gate.endl + "# \t\t  - path      - x1 y1 x2 y2 [ x3 y3 [ ... ] ]"
 			+ Gate.endl + "# \t\t  - text      - x y yourText size"
 			+ Gate.endl + "# \t\t  - image      - x y width height href"
 			+ Gate.endl + "# \t\t* color is english/#rgb/#rrggbb[,opacity]"
 			+ Gate.endl + "# \t\t\tex: red,0.5 = #f00,0.5 = #ff0000,0.5"
 			+ Gate.endl + "# \t\t\tex: blue = blue,1 = #00f = #0000ff,1"
 			;
 	}
 	
 	public DrawingArea getDrawingArea ()
 	{
 		return area;
 	}
 	
 	private void updateColor (String color, boolean isFill, final Gfx g)
 	{
 		final int comma = color.indexOf(',');
 		float opacity = 1f;
 
 		// check if opacity is provided
 		if (comma >= 0)
 		{
 			opacity = new Float(color.substring(comma + 1)).floatValue();
 			color = color.substring(0, comma);
 		}
 		
 		if (isFill) // fill color
 		{
 			if (g.gfx instanceof Ellipse)
 			{
 				((Ellipse)g.gfx).setFillColor(color);
 				((Ellipse)g.gfx).setFillOpacity(opacity);
 			}
 			else if (g.gfx instanceof Text)
 			{
 				((Text)g.gfx).setFillColor(color);
 				((Text)g.gfx).setFillOpacity(opacity);
 			}
 			else // path
 			{
 				((Path)g.gfx).setFillColor(color);
 				((Path)g.gfx).setFillOpacity(opacity);
 			}
 		}
 		if (g.gfx instanceof Ellipse)
 			((Ellipse)g.gfx).setStrokeColor(color);
 		else if (g.gfx instanceof Text)
 			((Text)g.gfx).setStrokeColor(color);
 		else // path
 			((Path)g.gfx).setStrokeColor(color);
 	}
 		
 	public boolean update (final Words words) throws WordsException
 	{
 		Boolean isFirst;
 		
 		if (words == null)
 			return true;
 		
 		if  (words.checkNextAndForward("del"))
 		{
 			final String name = words.getString(Gate.cmdlineName);
 			if (gfx.remove(name) == null)
 				return Gate.getW().error(words, -1, Gate.cmdlineNotFound);
 			Gate.getW().uiNeedUpdate(this);
 		}
 		
 		else if (words.checkNextAndForward("add"))
 		{
 			final String name = words.getString(Gate.cmdlineName);
 			Gfx g =  null;
 			if (gfx.get(name) != null)
 				return Gate.getW().error(words, -1, Gate.errorNameAlreadyExists);
 			
 			final String type = words.getString(Gate.cmdlineUndefinedShape);
 			final float x1 = words.getPosFloat(Gate.cmdlineCenterX);
 			final float y1 = words.getPosFloat(Gate.cmdlineCenterY);
 			
 			if (type.equals("circle"))
 			{
 				final float radius = words.getPosFloat(Gate.cmdlineRadius);
 				gfx.put(name, g = new Gfx(new Ellipse(0,0,0,0), x1, y1, radius, radius));
 			}
 
 			else if (type.equals("ellipse"))
 			{
 				final float radiusx = words.getPosFloat(Gate.cmdlineRadius);
 				final float radiusy = words.getPosFloat(Gate.cmdlineRadius);
 				gfx.put(name, g = new Gfx(new Ellipse(0,0,0,0), x1, y1, radiusx, radiusy));
 			}
 
 			else if (type.equals("line"))
 			{
 				final float nextx = words.getPosFloat(Gate.cmdlineCenterX);
 				final float nexty = words.getPosFloat(Gate.cmdlineCenterY);
 				gfx.put(name, g = new Gfx(new Path(0,0), x1, y1, x1, nexty));
 				g.path = new ArrayList<Gfx.xy>();
 				g.path.add(g.new xy(nextx, nexty));
 			}
 			
 			else if (type.equals("arrow"))
 			{
 				final float nextx = words.getPosFloat(Gate.cmdlineCenterX);
 				final float nexty = words.getPosFloat(Gate.cmdlineCenterY);
 				final float norm = (float)Math.pow((x1-nextx)*(x1-nextx)+(y1-nexty)*(y1-nexty), 0.5);
 				if (norm <= 0)
 					return true;
 				final float arrowAlpha = arrowPercent / norm;
 				final float ax = (int)(arrowAlpha * x1 + (1.0 - arrowAlpha) * nextx);
 				final float ay = (int)(arrowAlpha * y1 + (1.0 - arrowAlpha) * nexty);
 				final float dirx = (int)(arrowAlpha / arrowDivider * (nexty - y1));
 				final float diry = (int)(arrowAlpha / arrowDivider * (x1 - nextx));
 
 				gfx.put(name, g = new Gfx(new Path(0,0), x1, y1, nextx, nexty));
 				g.path = new ArrayList<Gfx.xy>();
 				g.path.add(g.new xy(ax, ay));
 				g.path.add(g.new xy(ax + dirx, ay + diry));
 				g.path.add(g.new xy(nextx, nexty));
 				g.path.add(g.new xy(ax - dirx, ay - diry));
 				g.path.add(g.new xy(ax, ay));
 			}
 
 			else if (type.equals("rectangle"))
 			{
 				final float nextx = words.getPosFloat(Gate.cmdlineCenterX);
 				final float nexty = words.getPosFloat(Gate.cmdlineCenterY);
 				
				gfx.put(name, g = new Gfx(new Path(0,0), x1, y1, x1, nexty));
 				g.path = new ArrayList<Gfx.xy>();
 				g.path.add(g.new xy(nextx, nexty));
 				g.path.add(g.new xy(x1, nexty));
 				g.path.add(g.new xy(x1, y1));
 			}
 
 			else if (type.equals("path"))
 			{
 				gfx.put(name, g = new Gfx(new Path(0, 0), x1, y1, 0, 0));
 				g.path = new ArrayList<Gfx.xy>();
 				while (words.checkNextIsPosFloat())
 				{
 					final float x = words.getPosFloat(Gate.cmdlineCenterX);
 					if (!words.checkNextIsPosFloat())
 					{
 						words.rewind(1);
 						return Gate.getW().error(words, -1, Gate.cmdlineCenterY);
 					}
 					final float y = words.getPosFloat(Gate.cmdlineCenterX);
 					g.path.add(g.new xy(x, y));
 				}
 			}
 
 			else if (type.equals("text"))
 			{
 				final String text = words.getString(Gate.cmdlineCenterX);
 				final float size = words.getPosFloat(Gate.cmdlineCenterX);
 				gfx.put(name, g = new Gfx(new Text(0, 0, text), x1, y1, size, 0));
 			}
 
 			else if (type.equals("image"))
 			{
 				final float width = words.getPosFloat(Gate.cmdlineCenterX);
 				final float height = words.getPosFloat(Gate.cmdlineCenterY);
 				final String href = words.getString(Gate.cmdlineCenterX);
 				gfx.put(name, g = new Gfx(new Image(0, 0, 0, 0, href), x1, y1, width, height));
 			}
 			
 			else 
 				return Gate.getW().error(words, -1, Gate.cmdlineObjectType);
 			
 			if (g.gfx instanceof Ellipse)
 			{
 				((Ellipse)g.gfx).setStrokeWidth(pixelWidth);
 				((Ellipse)g.gfx).setStrokeOpacity(1);
 				((Ellipse)g.gfx).setFillOpacity(0);
 			}
 			else if (g.gfx instanceof Text)
 			{
 				((Text)g.gfx).setStrokeWidth(pixelWidth);
 				((Text)g.gfx).setStrokeOpacity(1);
 				((Text)g.gfx).setFillOpacity(1);
 			}
 			else if (g.gfx instanceof Image)
 			{
 				/*((Image)g.gfx).setStrokeWidth(pixelWidth);
 				((Image)g.gfx).setStrokeOpacity(1);
 				((Image)g.gfx).setFillOpacity(1);*/
 				// Rien a faire ?
 			}
 			else
 			{
 				((Path)g.gfx).setStrokeWidth(pixelWidth);
 				((Path)g.gfx).setStrokeOpacity(1);
 				((Path)g.gfx).setFillOpacity(0);
 			}
 			
 			if (defColor != null)
 				updateColor(defColor, /*fill*/true, g);
 			if (defBColor != null)
 				updateColor(defBColor, /*border*/false, g);
 
 			if (w100 > 0 && h100 > 0)
 				redraw1(g);
 			else
 				Gate.getW().uiNeedUpdate(this);
 		}
 		
 		else if ((isFirst = words.checkNextAndForward("color")) || words.checkNextAndForward("bcolor"))
 		{ 
 			final String name = words.getString(Gate.cmdlineName);
 			final String color = words.getString(Gate.cmdlineColor);
 			if (name.equals("default"))
 			{
 				if (isFirst)
 					defColor = color;
 				else
 					defBColor = color;
 				return true;
 			}
 			
 			final Gfx g = gfx.get(name);		
 			if (g == null)
 				return Gate.getW().error(words, -1, Gate.cmdlineNotFound);
 
 			updateColor(color, isFirst, g);
 		}
 		
 		else if (words.checkNextAndForward("enable"))
 		{ 
 			final String name = words.getString(Gate.cmdlineName);
 			final Gfx g = gfx.get(name);
 			if (g == null)
 				return Gate.getW().error(words, -1, Gate.cmdlineNotFound);
 			g.gfx.addClickHandler(new ClickHandler()
 			{
 				public void onClick (final ClickEvent event)
 				{
 					Gate.getW().send("'" + getName() + "' '" + name + "'");
 				}
 			});
 		}
 
 		else
 			return super.update(words);
 		
 		return true;
 	}
 		
 	float w100 = -1;
 	float h100 = -1;
 	
 	public void redraw1 (final Gfx g)
 	{
 		final int x = (int)(g.x1 * w100);
 		final int y = (int)(g.y1 * h100);
 		final int x2 = (int)(g.x2 * w100);
 		final int y2 = (int)(g.y2 * h100);
 
 		if (g.gfx instanceof Ellipse)
 		{
 			final Ellipse e = (Ellipse)g.gfx;
 			e.setX(x);
 			e.setY(y);
 			e.setRadiusX(x2);
 			e.setRadiusY(y2);
 		}
 		
 		else if (g.gfx instanceof Path)
 		{
 			final Path p = (Path)g.gfx;
 			if (!g.drawn)
 			{
                 p.moveTo(x, y);
 				for (Gfx.xy xy: g.path)
 					p.lineTo((int)(xy.x * w100), (int)(xy.y * h100));
 			}
 			else
 			{
 				int c = 1;
 				p.setStep(1, new MoveTo(false, x, y));
 				for (Gfx.xy xy: g.path)
 					p.setStep(++c, new LineTo(false, (int)(xy.x * w100), (int)(xy.y * h100)));
 			}
 		}
 
 		else if (g.gfx instanceof Text)
 		{
 			final Text t = (Text) g.gfx;
 			t.setX(x);
 			t.setY(y);
 			int size = x2;
 			t.setFontSize(size);
 			t.setFillOpacity(1);
 		}
 
 		else if (g.gfx instanceof Image)
 		{
 			final Image i = (Image) g.gfx;
 			i.setX(x);
 			i.setY(y);
 			i.setWidth(x2);
 			i.setHeight(y2);
 		}
 		
 		//XXX else bad bad bad ??		
 		
 		if (!g.drawn)
 		{
 			area.add(g.gfx);
 			g.drawn = true;
 		}
 	}
 	
 	public boolean redraw ()
 	{
 		int w, h; 
 		
 		area.setWidth(w = getPlace().c(Place.width).getPixel());
 		area.setHeight(h = getPlace().c(Place.height).getPixel());
 		
 		if (w == 0 || h == 0)
 			return false;
 		
 		w100 = w / 100f;
 		h100 = h / 100f;
 		for (final Entry<String, Gfx> it: gfx.entrySet())
 			redraw1(it.getValue());
 		
 		return true;
 	}
 
 	public void	setSonTitle (final IntfObject son, final String title)
 	{
 		// this widget do not have son or title
 	}
 	
 	public boolean addSon (final IntfObject son, final String name)
 	{
 		// this widget is not a container
 		return false;
 	}	
 	
 	public boolean setSonPosition (final IntfObject son)
 	{
 		return true;
 	}
 	
 } // class GuiGFX
