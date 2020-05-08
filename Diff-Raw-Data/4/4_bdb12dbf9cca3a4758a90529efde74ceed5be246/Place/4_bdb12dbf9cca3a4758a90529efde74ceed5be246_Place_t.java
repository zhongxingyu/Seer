 
 /**************************************************************
  *
  * - CeCILL-B license
  * - (bsd-like, check http://www.cecill.info/faq.en.html#bsd)
  * 
  * Copyright CNRS
  * Contributors:
  * David Gauchard <gauchard@laas.fr>	2013-01-01
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
 
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 
 class Place
 {
 	public enum Type 				{ PERCENT, PIXEL, MM, INCH, RELATIVE };
 	
 	private	int						recursive = 0;
 
 	private final IntfObject		object;	
 	private boolean 				keepRatio;
 	
 	// width/height/x/y
 	public static final int		width	= 0;
 	public static final int		height	= 1;
 	public static final int		x		= 2;
 	public static final int		y		= 3;
 	private final C				places [];
 	
 	// leftof/above/rightof/below
 	public static final int		leftOf	= 0;
 	public static final int		above	= 1;
 	public static final int		rightOf	= 2;	
 	public static final int		below	= 3;
 	private final IntfObject		relatives [];
 	private final boolean			stickToBorders [];	
 	
 	// border gap size (pixel)
 	private int					gap;
 	
 	// some parent (generally root) may change 'static updated' to false
 	// this is used to inform uiRefreshInsideParent's caller to later call it back
 	// remember: we are in a not multithreaded environment
 	static private boolean		updated;
 	
 	// X/Y ratio
 	private float					ratio = -1f;
 	
 	public class C // Coordinate
 	{
 		// keep in mind:
 		// pixel/percent are supposed to be updated after a call to reSetPlace()
 		// pixel is the _upper-left_ coordinate
 		// percent is the _center_ coordinate
 		
 		private final int		index;
 		private int			pixel;
 		private int			mm_in;
 		private float			percent;
 		private Type			type;
 		
 		public C (final int index)
 		{
 			this.index = index;
 			pixel = -1;
 			mm_in = -1;
 			switch (index)
 			{
 			case x:
 			case y: percent = 0.5f; break;
 			default: percent = 1.0f; break;
 			}
 			type = Type.RELATIVE;
 		}			
 		
 		public final boolean	isPercent		()	{ return type == Type.PERCENT; }
 		public final boolean	isPixel			()	{ return type == Type.PIXEL; }
 		public final boolean	isMm			()	{ return type == Type.MM; }
 		public final boolean	isRelative		()	{ return type == Type.RELATIVE; }
 		public final boolean	isStuckToBorder	()	{ return isRelative() && relatives[index] == null; }
 
 		public final float getPercent ()
 		{
 			if (!resetPercent())
 			{
 				//W.debug(object.getName() + ": place error (percent/" + index + " on " + object.getName() + ")");
 				updated = false;
 				return 0;
 			}
 			return percent;
 		}
 		
 		public final int getPixelNoRatio ()
 		{
 			if (!resetPixel())
 			{
 				//W.debug("place error (pixel/" + index + " on " + object.getName() + ")");
 				updated = false;
 				return 0;
 			}			
 			return pixel;
 		}
 
 		public final int getPixel ()
 		{
 			if (keepRatio)
 			{
 				// update cache
 				if (ratio <= 0)
 				{
         			if (!c(width).isRelative() && !c(width).isPixel() && c(width).type == c(height).type)
 		        	{
     				    // keep proportion
 
 						float ow = -1, oh = -1;
 						switch (c(width).type)
 						{
 						case INCH:
 						case MM:
 							ow = c(width).mm_in;
 							oh = c(height).mm_in;
 							break;
 						case PERCENT:
 							ow = c(width).percent;
 							oh = c(height).percent;
 							break; 
 						default:
 							break;
 						}
 						if (ow > 0 && oh > 0)
 							ratio = oh / ow;
 						Gate.debug("ratio " + object.getName() + " " + ratio);
 					}
 					else
 					    // can't keep proportion
 					    ratio = 1.0f;
                 }
 					
 				int cwpr = (int)(c(width).getPixelNoRatio() * ratio);
 					
 				if (cwpr > c(height).getPixelNoRatio())
 				{
 					int chpr = (int)(c(height).getPixelNoRatio() / ratio);
 
 					if (index == x)
 						// center on x
 						return getPixelNoRatio() + (c(width).getPixelNoRatio() - chpr) / 2;
 
 					if (index == width)
 						// height is smaller
 						return chpr;
 				}
 				else
 				{
 					if (index == y)
 						// center on y
 						return getPixelNoRatio() + (c(height).getPixelNoRatio() - cwpr) / 2;
 					
 					if (index == height)
 						// width is smaller
 						return cwpr;
 				}
 			}
 
 			return getPixelNoRatio();
 		}
 		
 		public final int getPixelMinus2Gap ()
 		{
 			return getPixel() - 2 * gap;
 		}
 
 		public void setPlace (float value, Type type)
 		{
 			switch (type)
 			{
 			case MM:
 			case INCH: mm_in = (int)value; break;
 			case PIXEL: pixel = (int)value; break;
 			case PERCENT: percent = value; break;
 
 			case RELATIVE: Gate.alert("internal error"); return;
 			}
 			this.type = type;
 			ratio = -1f;
 		}
 		
 		public void setTypeRelative ()
 		{
 			type = Type.RELATIVE;
 		}
 		
 		public boolean resetPixel ()
 		{
 			if (object.getGOParent() == null)
 			{
 				final Widget w = object.getWidget();
 				switch (index)
 				{
 				case width:		pixel = w.getOffsetWidth();		break;
 				case height:	pixel = (int)(w.getOffsetHeight() - Gate.TabLayoutPanelSize); break;
 				case x:			pixel = w.getAbsoluteLeft();	break;
 				case y:			pixel = w.getAbsoluteTop();		break;
 				}
 				// got something ? (width or height == 0 not accepted)
 				return (index != width && index != height) || pixel > 0;
 			}
 			else
 			{
     		    if (++recursive > 5)
     		    {
 					Gate.alert("recursive error implying at least object " + object.getName() + " on "
 	    		          + (index == width? "width":
 	     		             index == height? "height":
 	     		           	 index == x? "x":
 	     		           	 "y")
     		              + (type == Type.PERCENT? "(%)":
     		            	 type == Type.RELATIVE? "(relative)":
     		            	 type == Type.MM? "(mm)":
     		            	 "(inch)")
     		               );
 					pixel = 50; // ...
 					--recursive;
 					return true;
                 }
 				
 				final Place pPlace = object.getGOParent().getPlace();
 				switch (type)
 				{
 				case PERCENT:
 					switch (index)
 					{
 					case width:		pixel = (int)(pPlace.c(width).getPixelMinus2Gap() * percent); break;
 					case height:	pixel = (int)(pPlace.c(height).getPixelMinus2Gap() * percent); break;
 					case x:			pixel = (int)(pPlace.c(width).getPixelMinus2Gap() * (percent - (c(width).getPercent() / 2.0f))); break;
 					case y:			pixel = (int)(pPlace.c(height).getPixelMinus2Gap() * (percent - (c(height).getPercent() / 2.0f))); break;
 					}
 					break;
 					
 				case RELATIVE:
 					switch (index)
 					{
 					case width:		pixel = (int)(rightXPixel() - leftXPixel()); break;
 					case height:	pixel = (int)(lowYPixel() - highYPixel()); break;
 					case x:			pixel = (int)leftXPixel(); break;
 					case y:			pixel = (int)highYPixel(); break;
 					}
 					break;
 					
 				case MM:
 				{
 					int ppixel = pPlace.c(index).getPixelMinus2Gap();
 					pixel = (int)(mm_in * Gate.ppmm);
 					if (pixel > ppixel)
 						pixel = ppixel;
 					break;
 				}
 					
 				case INCH:
 				{
 					int ppixel = pPlace.c(index).getPixelMinus2Gap();
 					pixel = (int)(mm_in * Gate.ppi);
 					if (pixel > ppixel)
 						pixel = ppixel;
 					break;
 				}
 					
 				case PIXEL:
 					break;
 
 				} // switch type
 				
 
 				--recursive;
 				
 				return true;
 			}
 		}
 		
 		public boolean resetPercent ()
 		{
 			final IntfObject parent = object.getGOParent();
 			if (parent == null)
 			{
 				// if parent is null, percent are already initialized in constructor
 				return true;
 			}
 			else
 			{
 				final Place pPlace = parent.getPlace();
 				if (type != Type.PERCENT)
 				{
 					if (pPlace.c(width).getPixel() == 0 || pPlace.c(height).getPixel() == 0)
 					{
 						//W.debug(object.getName() + ": unknown parent (" + parent.getName() + ") pixel-size for percent calculation (place index=" + index + ")");
 						return false; // do it again
 					}
 					switch (index)
 					{
 					case width:	    percent = (float)pixel / pPlace.c(width).getPixelMinus2Gap(); break; 
 					case height: 	percent = (float)pixel / pPlace.c(height).getPixelMinus2Gap(); break;
 					case x:			percent = ((float)pixel / pPlace.c(width).getPixelMinus2Gap()) + (c(width).getPercent() / 2.0f); break;
 					case y:			percent = ((float)pixel / pPlace.c(height).getPixelMinus2Gap()) + (c(height).getPercent() / 2.0f); break;
 					}
 				}
 				
 				return true;
 			}
 		}
 		
 	} // class C
 
 	public Place (final IntfObject object)
 	{
 		this.object = object;
 		keepRatio = false;
 		gap = Gate.defaultGap;
 		places = new C[4];
 		relatives = new IntfObject [4];
 		stickToBorders = new boolean [4];
 		for (int i = 0; i < 4; i++)
 		{
 			places[i] = new C(i);
 			relatives[i] = null;
 			stickToBorders[i] = true;
 		}
 	}
 	
 	public void relativeHasVanished (IntfObject v)
 	{
 		for (int i = 0; i < 4; i++)
 			if (   c(i).isRelative()
 				&& !c(i).isStuckToBorder()
 				&& relatives[i].getName() == v.getName())
 			{
 				//W.debug(object.getName() + " detached from " + v.getName() + " index=" + i);
 				setStickToBorder(i, true);
 			}
 	}
 	
 	private void setRelativeType (int relativeId)
 	{
 		switch (relativeId)
 		{
 		case above:		c(height).setTypeRelative(); break;
 		case below:		c(y).setTypeRelative(); break;
 		case rightOf:	c(x).setTypeRelative(); break;
 		case leftOf:	c(width).setTypeRelative(); break;
 		}
 	}
 	
 	public void setRelative (int relativeId, final IntfObject relative)
 	{	
 		relatives[relativeId] = relative;
 		setRelativeType(relativeId);
 	}
 	
 	public void setStickToBorder (int relativeId, boolean stuck)
 	{
 		stickToBorders[relativeId] = stuck;
 		if (stuck)
 			relatives[relativeId] = null;
 		// no: setRelativeType(relativeId); breaks percentage if this is done after 
 	}
 
 	public void setKeepRatio (final boolean ratio)
 	{
 		keepRatio = ratio;
 	}
 	
 	public void setGap (int gap)
 	{
 		this.gap = gap;
 	}
 
 	public int getGap ()
 	{
 		return gap;
 	}
 	
 	public C c (final int index)
 	{
 		return places[index];
 	}
 	
 	public boolean uiRefreshInsideParent ()
 	{				
 		final int pxSize = c(Place.width).getPixelMinus2Gap();
 		final int pySize = c(Place.height).getPixelMinus2Gap();
 		final String sxSize = new Integer(pxSize).toString() + "px";
 		final String sySize = new Integer(pySize).toString() + "px";
 
 		updated = true;
 		
 		// update size
 		
 		object.getWidget().setSize(sxSize, sySize);
 		//object.getWidget().getElement().getStyle().setWidth(pxSize, Style.Unit.PX);
 		//object.getWidget().getElement().getStyle().setHeight(pySize, Style.Unit.PX);
 		
 		if (object instanceof TextArea)
 		{
 			// lock TextArea which is user-resizable
 			object.getWidget().getElement().getStyle().setProperty("minWidth", sxSize);
 			object.getWidget().getElement().getStyle().setProperty("maxWidth", sxSize);
 			object.getWidget().getElement().getStyle().setProperty("minHeight", sySize);
 			object.getWidget().getElement().getStyle().setProperty("maxHeight", sySize);
 		}
		//else if (object instanceof SliderBar)
		//	((SliderBar)object).onResize(pxSize, pySize);
 
 		// update position
 		object.getGOParent().setSonPosition(object);
 		
 		// some parent (generally root) may change 'updated' to false
 		// this is used to inform caller to later call us back
 		// (globally changed - we are in a not multithreaded environment)
 		//if (!updated)
 		//	W.debug(object.getName() + " not updated");
 						
 		return updated;
  	}
 	
 	// relative placement calculation
 	
 	public float highYPixel ()
 	{
 		if (c(y).isPixel())	{ Gate.alert("glo1"); return c(y).getPixel(); }
 		
 		if (relatives[below] != null)
 			return relatives[below].getPlace().c(y).getPixel() + relatives[below].getPlace().c(height).getPixel();
 		if (stickToBorders[below])
 			return 0;
 		return lowYPixel() - c(height).getPixel();
 	}
 	
 	public float lowYPixel ()
 	{
 		if (relatives[above] != null)
 			return relatives[above].getPlace().c(y).getPixel();
 		if (stickToBorders[above])
 			return object.getGOParent().getPlace().c(height).getPixelMinus2Gap();
 		return highYPixel() + c(height).getPixel();
 	}
 	
 	public float rightXPixel ()
 	{
 		if (c(x).isPixel()) { Gate.alert("glo2"); return c(x).getPixel(); }
 		
 		if (relatives[leftOf] != null)
 			return relatives[leftOf].getPlace().c(x).getPixel();
 		if (stickToBorders[leftOf])
 			return object.getGOParent().getPlace().c(width).getPixelMinus2Gap();
 		return leftXPixel() + c(width).getPixel();
 	}
 	
 	public float leftXPixel ()
 	{
 		if (relatives[rightOf] != null)
 			return relatives[rightOf].getPlace().c(x).getPixel() + relatives[rightOf].getPlace().c(width).getPixel();
 		if (stickToBorders[rightOf])
 			return 0;
 		return rightXPixel() - c(width).getPixel();
 	}
 	
 }
 
