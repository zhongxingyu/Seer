 package lovelogic.gui.figure;
 
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class ProofFigure
 {
 	private static final int MIN_H_GAP = 40;
 	private int x;
 	private int y;
 	private int wholeHeight;
 	private int contentX;
 	private int contentY;
 	private int contentWidth;
 	private int contentHeight;
 	private int subWidth;
 	private int figureWidth;
 	private int labelWidth;
 	private String content;
 	private String deductionName;
 	private List<ProofFigure> subFigures = new ArrayList<ProofFigure>();
 
 	public ProofFigure(String content)
 	{
 		this.content = content;
 		this.deductionName = "";
 	}
 
 	public ProofFigure(String content, String deductionName, ProofFigure ... subFigures)
 	{
 		this.content = content;
 		this.deductionName = deductionName;
 		this.subFigures.addAll(Arrays.asList(subFigures));
 	}
 
 	public void add(ProofFigure pf)
 	{
 		subFigures.add(pf);
 	}
 
 	public void setX(int x)
 	{
 		this.x = x;
 	}
 
 	public int getX()
 	{
 		return x;
 	}
 
 	public void setY(int y)
 	{
 		this.y = y;
 	}
 
 	public int getY()
 	{
 		return y;
 	}
 
 	public String getContent()
 	{
 		return content;
 	}
 
 	public String getDeductionName()
 	{
 		return deductionName;
 	}
 
 	public boolean isSubFiguresEmpty()
 	{
 		return subFigures.isEmpty();
 	}
 
 	public int countSubFigures()
 	{
 		return subFigures.size();
 	}
 
 	public Iterable<ProofFigure> getSubFigures()
 	{
 		return subFigures;
 	}
 
 	public int getMaxDepth()
 	{
 		int d = 0;
 		for (ProofFigure sub : subFigures)
 		{
 			d = Math.max(sub.getMaxDepth(), d);
 		}
 		return d + 1;
 	}
 
 	protected int getWholeWidth()
 	{
 		return figureWidth + labelWidth;
 	}
 
 	protected boolean isAxiomNode()
 	{
 		return subFigures.isEmpty();
 	}
 
 	public void layout(Graphics g)
 	{
 		FontMetrics fm = g.getFontMetrics();
 		calcSize(fm);
 		locate(0, wholeHeight);
 	}
 
 	protected void calcSize(FontMetrics fm)
 	{
 		subWidth = (subFigures.size() - 1) * MIN_H_GAP;
 		int subHeightMax = 0;
 		for (ProofFigure pf : subFigures)
 		{
 			pf.calcSize(fm);
 			subWidth += pf.getWholeWidth();
 			subHeightMax = Math.max(pf.wholeHeight, subHeightMax);
 		}
 		contentWidth = fm.stringWidth(content);
 		contentHeight = fm.getHeight();
 		labelWidth = fm.stringWidth(deductionName);
 		figureWidth = Math.max(subWidth, contentWidth);
 		wholeHeight = subHeightMax + 2 * 4 + contentHeight;
 	}
 
 	protected void locate(int x0, int y0)
 	{
 		x = x0;
 		if (isAxiomNode())
 		{
 			contentX = x0;
 		}
 		else
 		{
 			int subY = y0 - contentHeight - 2 * 4;
 			if (subWidth < contentWidth)
 			{
 				int acsw = subWidth - ((subFigures.size() - 1) * MIN_H_GAP);
 				int sp = contentWidth - acsw;
 				contentX = x0;
 				//int subX = x0 + (contentWidth - subWidth) / 2;
 				if (subFigures.size() == 1)
 				{
 					locateSubtrees(x0 + sp / 2, subY, 0);
 				}
 				else
 				{
 					locateSubtrees(x0, subY, sp / (subFigures.size() - 1));
 				}
 			}
 			else
 			{
 				locateSubtrees(x0, subY, MIN_H_GAP);
 				//contentX = x0 + (subWidth - contentWidth) / 2;
 				ProofFigure l = subFigures.get(0);
 				ProofFigure r = subFigures.get(subFigures.size() - 1);
 				int w = r.contentX + r.contentWidth - l.contentX;
 				contentX = l.contentX + (w - contentWidth) / 2;
 			}
 		}
 		contentY = y0 - contentHeight;
 	}
 
 	private void locateSubtrees(int x0, int y0, int gap)
 	{
 		int x = x0;
 		for (ProofFigure sub : subFigures)
 		{
 			sub.locate(x, y0);
 			x += sub.getWholeWidth() + gap;
 		}
 	}
 
 	public void drawCenter(Graphics g, int x, int y, int width, int height)
 	{
 		draw(g, x + (width - getWholeWidth()) / 2, y + (height - wholeHeight) / 2);
 	}
 
 	public void draw(Graphics g, int x, int y)
 	{
 		g.translate(x, y);
 		draw(g);
 		g.translate(-x, -y);
 	}
 
 	public void draw(Graphics g)
 	{
		//g.drawRect(x, y, getWholeWidth(), wholeHeight);
 		//g.drawRect(contentX, contentY, contentWidth, contentHeight);
 
 		FontMetrics fm = g.getFontMetrics();
 		for (ProofFigure sub : subFigures)
 		{
 			sub.draw(g);
 		}
 		g.drawString(content, contentX, contentY + fm.getAscent());
 
 		if (!isAxiomNode())
 		{
 			g.drawLine(getLineLeft(), contentY - 4, getLineRight(), contentY - 4);
 			g.drawString(deductionName, getLineRight(), contentY - 4 - fm.getHeight() / 2 + fm.getAscent());
 		}
 	}
 
 	private int getLineLeft()
 	{
 		int x = contentX;
 		if (!subFigures.isEmpty())
 		{
 			x = Math.min(x, subFigures.get(0).contentX);
 		}
 		return x;
 	}
 
 	private int getLineRight()
 	{
 		int x = contentX + contentWidth;
 		if (!subFigures.isEmpty())
 		{
 			ProofFigure rightMost = subFigures.get(subFigures.size() - 1);
 			x = Math.max(x, rightMost.contentX + rightMost.contentWidth);
 		}
 		return x;
 	}
 }
