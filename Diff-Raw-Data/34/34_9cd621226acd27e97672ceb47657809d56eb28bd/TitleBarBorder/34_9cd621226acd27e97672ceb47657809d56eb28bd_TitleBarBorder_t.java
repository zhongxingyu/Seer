 package rxr.component;
 
 import java.awt.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 /**
  * A border that presents an eclipse2-style title bar.
  */
 public class TitleBarBorder extends AbstractBorder
 {
 	private static final long serialVersionUID = 6374387987537426517L;
 
 	String label;
 	ImageIcon icon;
 
 	boolean showGradient;
 
 	int overflowHandleStyle;
 	public static final int OVERFLOW_NONE = 0;
 	public static final int OVERFLOW_ELLIPSIS = 1;
 	public static final int OVERFLOW_FADE = 2;
 	int fadeLength;
 
 	Color textColor = new Color(0, 0, 0);
 	Color textFadeColor = new Color(0, 0, 0, 0);
 	Color leftColor = new Color(200, 200, 200);
 
 	//Color rightColor = new Color(223, 222, 226);
 	Color rightColor = UIManager.getLookAndFeel().getDefaults().getColor("control");
 
 	public TitleBarBorder(String text)
 	{
 		this(text, null, true, OVERFLOW_FADE, 20);
 	}
 
 	public TitleBarBorder(String text, ImageIcon icon)
 	{
 		this(text, icon, true, OVERFLOW_FADE, 20);
 	}
 
 	public TitleBarBorder(String text, ImageIcon icon, boolean showGradient)
 	{
 		this(text, icon, showGradient, OVERFLOW_FADE, 20);
 	}
 
 	public TitleBarBorder(String text, ImageIcon icon, boolean showGradient, int overflowHandleStyle)
 	{
 		this(text, icon, showGradient, overflowHandleStyle, 20);
 	}
 
 	public TitleBarBorder(String text, ImageIcon icon, boolean showGradient, int overflowHandleStyle, int fadeLength)
 	{
 		this.label = text;
 		this.icon = icon;
 		this.showGradient = showGradient;
 		this.overflowHandleStyle = overflowHandleStyle;
 		this.fadeLength = fadeLength;
 	}
 
 	public ImageIcon getIcon()
 	{
 		return icon;
 	}
 
 	public void setIcon(ImageIcon icon)
 	{
 		this.icon = icon;
 	}
 
 	public String getLabel()
 	{
 		return label;
 	}
 
 	public void setLabel(String label)
 	{
 		this.label = label;
 	}
 
 	public boolean isShowGradient()
 	{
 		return showGradient;
 	}
 
 	public void setShowGradient(boolean showGradient)
 	{
 		this.showGradient = showGradient;
 	}
 
 	public Color getLeftColor()
 	{
 		return leftColor;
 	}
 
 	public void setLeftColor(Color leftColor)
 	{
 		this.leftColor = leftColor;
 	}
 
 	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
 	{
 		Graphics2D window = (Graphics2D)g;
 
 		window.translate(x, y);
 
 		int w = width - 1;
 		int h = 24 - 1;
 
 		//background
 		window.setColor(Color.WHITE);
 		window.drawLine(0, 0, w, 0);
 		window.drawLine(0, 0, 0, h - 1);
 
 		window.setColor(new Color(132, 130, 132));
 		window.drawLine(0, h, w, h);
 
 		if(showGradient)
 		{
 			window.setPaint(new GradientPaint(1, 1, leftColor, w, 1, rightColor));
 			window.fillRect(1, 1, w, h - 1);
 		}
 
 		boolean drawIcon = icon != null && icon.getIconHeight() > -1;
 
 		//icon
 		if(drawIcon && w > 16 + 4)
 		{
			window.drawImage(icon.getImage(), 4, 4, 16, 16, null);
 		}
 
 		//text
 		Font font = new Font("Tahoma", Font.BOLD, 11);
 		window.setFont(font);
 
 		String tempLabel;
 
 		int textPaddingX = 5;
 
 		if(overflowHandleStyle == OVERFLOW_ELLIPSIS)
 		{
 			window.setPaint(textColor);
 
 			FontMetrics fm = window.getFontMetrics();
 			char[] labelChars = label.toCharArray();
 
 			int iconWidth = (drawIcon ? 16 + 5 : 0);
 			int availableTextWidth = w - iconWidth - (textPaddingX * 2);
 
 			int len = labelChars.length;
 			while(fm.charsWidth(labelChars, 0, len) > availableTextWidth)
 			{
 				len--;
 				if(len < 1)
 				{
 					break;
 				}
 			}
 			if(len != labelChars.length)
 			{
 				for(int i = 0; i < 3; i++)
 				{
 					if(labelChars.length > (len - i - 1) && (len - i - 1) > 0)
 					{
 						labelChars[len - i - 1] = '.';
 					}
 				}
 			}
 
 			tempLabel = new String(labelChars, 0, len);
 		}
 		else if(overflowHandleStyle == OVERFLOW_FADE)
 		{
 			window.setPaint(new GradientPaint(w - textPaddingX - fadeLength, 0, textColor, w - textPaddingX, 0, textFadeColor));
 			tempLabel = label;
 		}
 		else
 		{
 			window.setPaint(textColor);
 			tempLabel = label;
 		}
 
 		if(drawIcon) //display icon, so move text
 		{
 			window.drawString(tempLabel, 20 + textPaddingX, 16);
 		}
 		else
 		{
 			window.drawString(tempLabel, textPaddingX, 16);
 		}
 
 		window.translate(-x, -y);
 	}
 
 	/**
 	 * Returns the insets of the border.
 	 * 
 	 * @param c
 	 *            the component for which this border insets value applies
 	 */
 	public Insets getBorderInsets(Component c)
 	{
 		return new Insets(24, 0, 0, 0);
 	}
 
 	/**
 	 * Reinitialize the insets parameter with this Border's current Insets.
 	 * 
 	 * @param c
 	 *            the component for which this border insets value applies
 	 * @param insets
 	 *            the object to be reinitialized
 	 */
 	public Insets getBorderInsets(Component c, Insets insets)
 	{
 		insets.left = 24;
 		insets.top = 0;
 		insets.right = 0;
 		insets.bottom = 0;
 		return insets;
 	}
 
 	public boolean isBorderOpaque()
 	{
 		return true;
 	}
 
 	public int getOverflowHandleStyle()
 	{
 		return overflowHandleStyle;
 	}
 
 	public void setOverflowHandleStyle(int overflowHandleStyle)
 	{
 		this.overflowHandleStyle = overflowHandleStyle;
 	}
 
 }
