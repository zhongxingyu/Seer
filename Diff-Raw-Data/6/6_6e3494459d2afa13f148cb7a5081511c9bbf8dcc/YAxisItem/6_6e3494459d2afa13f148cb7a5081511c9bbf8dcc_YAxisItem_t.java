 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.graph;
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Line2D;
 
 import util.misc.Fonts;
 
 /**
  * A Y axis. See {@link AxisItemBase} for most of the properties and a
  * more detailed explanation.
  */
 public class YAxisItem extends AxisItemBase
 {
 	/**
 	 * @param w coordinate system.
 	 * @throws GraphFormatException
 	 */
 	public YAxisItem(World w) throws GraphFormatException
 	{
 		super(w);
 
 		// Init range to full size
 		gr=new GraphRange(getWorld().getTopY(),getWorld().getBottomY());
 	}
 
 	/** Range (y) of axis */
 	private GraphRange gr;
 
 	/** X co-ordinate of axis */
 	private double dX=0.0;
 
 	/** If label is rotated */
 	private boolean bRotateLabel=true;
 
 	@Override
 	protected GraphRange getRange()
 	{
 		return gr;
 	}
 
 	@Override
 	protected void paintLine(Graphics2D g2,GraphRange range)
 	{
 		float
 			fX=getWorld().convertXFloat(dX),
 			fY1=getWorld().convertYFloat(range.getMin()),
 			fY2=getWorld().convertYFloat(range.getMax());
 
 		Line2D l=new Line2D.Float(fX,fY1,fX,fY2);
 		g2.draw(l);
 	}
 
 	@Override
 	protected boolean isLabelRotated()
 	{
 		return bRotateLabel;
 	}
 
 	@Override
 	protected int paintAxisText(Graphics2D g2,double dPoint,String sText,
 		boolean bRotate,boolean bFlip,boolean bNumbers,int iExtraOffset)
 	{
 		int
 		  iAscent=bNumbers
 				?	Fonts.getAscent(g2.getFont(),'0')
 				: Math.max(Fonts.getAscent(g2.getFont(),'0'),
 					Fonts.getAscent(g2.getFont(),'A')),
 		  iDescent=bNumbers ? 0 : g2.getFontMetrics().getDescent();
 
 		int iSize=(int)g2.getFontMetrics().getStringBounds(sText,g2).getWidth();
 
 		int iCentreX=getWorld().convertX(dX),iCentreY=getWorld().convertY(dPoint);
 
 		if(bRotate)
 		{
 			AffineTransform at=g2.getTransform();
 
 			if(!bFlip)
 			{
 				g2.rotate(-Math.PI/2.0,iCentreX,iCentreY);
 				g2.drawString(sText,
 					iCentreX-(iSize+1)/2,
 					iCentreY-iDescent-iExtraOffset);
 			}
 			else
 			{
 				g2.rotate(Math.PI/2.0,iCentreX,iCentreY);
 				g2.drawString(sText,
 					iCentreX-(iSize+1)/2,
 					iCentreY+iAscent+iExtraOffset);
 			}
 
 			g2.setTransform(at);
 
 			return iAscent+iDescent+iExtraOffset;
 		}
 		else
 		{
 			g2.drawString(sText,iCentreX-iSize-iExtraOffset,iCentreY+(iAscent+iDescent+1)/2-iDescent);
 
 			return iSize+iExtraOffset;
 		}
 	}
 
 	@Override
 	protected void paintTick(Graphics2D g2,double dPoint,int iSize,boolean bPos)
 	{
 		float
 			fX=getWorld().convertXFloat(dX),
 			fY=getWorld().convertYFloat(dPoint);
 
		Line2D l;
		if(bPos)
			l=new Line2D.Float(fX,fY,fX+iSize,fY);
		else
			l=new Line2D.Float(fX-iSize,fY,fX,fY);
 		g2.draw(l);
 	}
 
 	/**
 	 * @param d New maximum extent of axis
 	 */
 	public void setMaxY(double d)
 	{
 		gr=new GraphRange(gr.getMin(),d);
 	}
 
 	/**
 	 * @param d New minimum extent of axis
 	 */
 	public void setMinY(double d)
 	{
 		gr=new GraphRange(d,gr.getMax());
 	}
 
 	/**
 	 * @param d X co-ordinate of axis
 	 */
 	public void setx(double d)
 	{
 		dX=d;
 	}
 
 	/**
 	 * @param bRotateLabel If true (default), rotates the label (see also
 	 *   setRotateFlip() to control rotation direction) - otherwise leaves it
 	 *   horizontal.
 	 */
 	public void setRotateLabel(boolean bRotateLabel)
 	{
 		this.bRotateLabel=bRotateLabel;
 	}
 
 }
