 package glui;
 
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.GL11;
 
 public abstract class GLButton extends GLUIComponent {
 
 	public static final int BUTTON_DISABLED = 0, BUTTON_INACTIVE = 1, BUTTON_HOVER = 2, BUTTON_CLICKED=3;
 	
 	private int state;
 	private String label;
 	private GLUITheme theme;
 
 	private Color backgroundColor;
 	private Color textColor;
 	private Font font;
 	
 	public GLButton(String label, GLUITheme theme, int x, int y, int w, int h)
 	{
 		this.setCurrentState(BUTTON_INACTIVE);
 		this.label = label;
 		this.theme = theme;
 		this.font  = this.theme.getFont();
 		this.bounds = new Rectangle(x, y, w, h);
 	}
	
 	public void update(int delta)
 	{
 		if(!this.isDisabled())
 		{
			if(new Point(Mouse.getX(), TestGLUI.SCREEN_HEIGHT-Mouse.getY()).inside(this.getBounds()))
 			{
 				if(Mouse.isButtonDown(0))
 				{
 					setCurrentState(BUTTON_CLICKED);
 				}
 				else if(this.currentState() == BUTTON_CLICKED)
 				{
 					this.onClick();
 					setCurrentState(BUTTON_HOVER);
 				}
 				else
 				{
 					setCurrentState(BUTTON_HOVER);
 				}
 			}
 			else
 			{
 				setCurrentState(BUTTON_INACTIVE);
 			}
 		}
 		
 		updateAppearance();
 	}
 	
 	public boolean isDisabled() {
 		if(currentState() == BUTTON_DISABLED)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	protected void updateAppearance()
 	{
 		switch(currentState())
 		{
 		case BUTTON_INACTIVE:
 			this.textColor       = theme.getTextColor();
 			this.backgroundColor = theme.getBackgroundColor();
 			break;
 		case BUTTON_HOVER:
 			this.textColor       = theme.getTextHoverColor();
 			this.backgroundColor = theme.getBackgroundHoverColor();
 			break;
 		case BUTTON_CLICKED:
 			this.textColor       = theme.getTextClickedColor();
 			this.backgroundColor = theme.getBackgroundClickedColor();
 			break;
 		case BUTTON_DISABLED:
 			this.textColor       = theme.getTextDisabledColor();
 			this.backgroundColor = theme.getBackgroundDisabledColor();
 			break;
 		default:
 			break;
 		}
 	}
 
 	public int currentState() {
 		return state;
 	}
 
 	public void setCurrentState(int state) {
 		this.state = state;		
 	}
 
 	@Override
 	public void renderGL() {
 		backgroundColor.bind();
 		GL11.glBegin(GL11.GL_POLYGON);
 			GL11.glVertex2i(bounds.x           , bounds.y           );
 			GL11.glVertex2i(bounds.x + bounds.w, bounds.y           );
 			GL11.glVertex2i(bounds.x + bounds.w, bounds.y + bounds.h);
 			GL11.glVertex2i(bounds.x           , bounds.y + bounds.h);
 		GL11.glEnd();
 		
 		int labelWidth = font.getStringWidth(textColor.escapeSeq()+" "+label);
 		int labelHeight = font.getStringHeight(textColor.escapeSeq()+" "+label);
 		
 		int labelX = (this.bounds.x + (this.bounds.w/2)) - (labelWidth/2);
 		int labelY = (this.bounds.y + (this.bounds.h/2)) - (labelHeight/2);
 		
 		try {
 			font.glDrawText(textColor.escapeSeq()+" "+label, labelX, labelY);
 		} catch (InvalidEscapeSequenceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected abstract void onClick();
 }
