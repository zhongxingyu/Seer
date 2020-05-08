 package lambda.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import lambda.Environment;
 import lambda.ast.IRedexNode;
 import lambda.ast.Lambda;
 import lambda.gui.lambdalabel.LambdaLabel;
 import lambda.gui.lambdalabel.LambdaLabelBuilder;
 import lambda.gui.lambdalabel.LambdaLabelDrawer;
 import lambda.gui.lambdalabel.LambdaLabelMetrics;
 import lambda.reduction.RedexFinder;
 import util.Pair;
 
 @SuppressWarnings("serial")
 public class RedexView extends JPanel
 {
 	private static final Color HOVER_BACK_COLOR = new Color(255, 255, 200);
 	private static final Color TRIANGLE_COLOR = new Color(192, 192, 255);
 
 	private LambdaLabelBuilder builder = new LambdaLabelBuilder();
 	private List<Pair<IRedexNode, LambdaLabel>> labels = new ArrayList<Pair<IRedexNode, LambdaLabel>>();
 	private Insets margin = new Insets(0, 0, 0, 0);
 	private int height = -1;
 	private int maxWidth;
 	private int hoverIndex = -1;
 	private int selectedIndex = -1;
 
 	public RedexView()
 	{
 		MouseHandler mh = new MouseHandler();
 		addMouseListener(mh);
 		addMouseMotionListener(mh);
 	}
 
 	public void setFont(Font font)
 	{
 		super.setFont(font);
 		height = getFontMetrics(font).getHeight();
 	}
 
 	public void setMargin(int top, int left, int bottom, int right)
 	{
 		margin.set(top, left, bottom, right);
 		calcPreferredSize();
 	}
 
 	public void clearLabels()
 	{
 		labels.clear();
 		maxWidth = 0;
 		hoverIndex = -1;
 		selectedIndex = -1;
 	}
 
 	private void addLabel(IRedexNode r, LambdaLabel l)
 	{
 		labels.add(Pair.of(r, l));
 		calcPreferredSize();
 	}
 
 	private void calcPreferredSize()
 	{
 		Graphics g = getGraphics();
 		maxWidth = 0;
 		for (Pair<IRedexNode, LambdaLabel> p : labels)
 		{
 			maxWidth = Math.max(maxWidth, LambdaLabelMetrics.getWidth(g, p._2));
 		}
 		int w = maxWidth + height / 2 + margin.left + margin.right;
 		int h = height * labels.size() + margin.top + margin.bottom;
 		setPreferredSize(new Dimension(w, h));
 	}
 
 	public void setRedexes(Lambda lambda)
 	{
 		clearLabels();
 		for (IRedexNode redex : RedexFinder.getRedexList(lambda, Environment.getEnvironment().getBoolean(Environment.KEY_ETA_REDUCTION)))
 		{
 			LambdaLabel label = builder.createLambdaLabel(lambda, redex);
 			addLabel(redex, label);
 		}
 		setSelectedIndex(0);
 	}
 
 	public IRedexNode getSelectedRedex()
 	{
 		int i = getSelectedIndex();
 		if (0 <= i && i < labels.size())
 		{
 			return labels.get(i)._1;
 		}
 		return null;
 	}
 
 	public int getLabelCount()
 	{
 		return labels.size();
 	}
 
 	public int getSelectedIndex()
 	{
 		return selectedIndex;
 	}
 
 	public void setSelectedIndex(int index)
 	{
 		if (0 <= index && index < labels.size())
 		{
 			selectedIndex = index;
 			repaint();
 		}
 	}
 
 	public void addActionListener(ActionListener l)
 	{
 		listenerList.add(ActionListener.class, l);
 	}
 
 	public void removeActionListener(ActionListener l)
 	{
 		listenerList.remove(ActionListener.class, l);
 	}
 
 	private void dispatchActionEvent()
 	{
 		ActionEvent e = new ActionEvent(this, 0, "selected");
 		for (ActionListener l : listenerList.getListeners(ActionListener.class))
 		{
 			l.actionPerformed(e);
 		}
 	}
 
 	protected void paintComponent(Graphics g)
 	{
 		g.setColor(getBackground());
 		g.fillRect(0, 0, getWidth(), getHeight());
 
 		if (labels.isEmpty())
 		{
 			String s = "No Redexes";
 			FontMetrics fm = g.getFontMetrics();
 			int sw = fm.stringWidth(s);
 			int sh = fm.getHeight();
 			g.setColor(Color.LIGHT_GRAY);
 			g.drawString(s, (getWidth() - sw) / 2, (getHeight() - sh) / 2 + fm.getLeading() + fm.getAscent());
 			return;
 		}
 
 		g.translate(margin.left, margin.top);
 
 		int width = getWidth() - (margin.left + margin.right);
 
 		if (hoverIndex != -1)
 		{
 			g.setColor(HOVER_BACK_COLOR);
 			g.fillRect(height / 2, hoverIndex * height, width - height / 2, height);
 		}
 
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
 		g.translate(height / 2, 0);
 		LambdaLabelDrawer drawer = new LambdaLabelDrawer();
 		for (int i = 0; i < labels.size(); i++)
 		{
 			Pair<IRedexNode, LambdaLabel> p = labels.get(i);
 			drawer.draw(g, p._2, 0, i * height, height);
 		}
 		g.translate(-height / 2, 0);
 
 		if (selectedIndex != -1)
 		{
 			g.setColor(TRIANGLE_COLOR);
 			drawTriangle(g, 0, selectedIndex * height + height / 2);
 		}
 
 		g.translate(-margin.left, -margin.top);
 	}
 
 	private void drawTriangle(Graphics g, int x, int y)
 	{
 		int s = Math.max(height / 3, 1);
 		g.translate(x, y);
 		g.fillPolygon(new int[] {  0, s, 0 }, new int[] { -s, 0, s }, 3);
 		g.translate(-x, -y);
 	}
 
 	private class MouseHandler extends MouseAdapter
 	{
 		public void mouseMoved(MouseEvent e)
 		{
 			updateHoverIndex(e);
 			repaint();
 		}
 
 		public void mouseExited(MouseEvent e)
 		{
 			hoverIndex = -1;
 			repaint();
 		}
 
 		public void mousePressed(MouseEvent e)
 		{
 			if (SwingUtilities.isLeftMouseButton(e))
 			{
 				updateHoverIndex(e);
				selectedIndex = hoverIndex;
 				repaint();
				if (e.getClickCount() == 2)
 				{
 					dispatchActionEvent();
 				}
 			}
 		}
 
 		private void updateHoverIndex(MouseEvent e)
 		{
 			hoverIndex = (e.getY() - margin.top) / height;
 			if (hoverIndex < 0 || labels.size() <= hoverIndex)
 			{
 				hoverIndex = -1;
 			}
 		}
 	}
 }
