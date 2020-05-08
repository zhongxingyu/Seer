 package fr.frozen.iron.client.components;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.Color;
 
 import fr.frozen.game.ISprite;
 
 public class Button extends Component {
 
 	protected String label;
 	
 	protected ISprite normalSprite = null;
 	protected ISprite hoverSprite = null;
 	protected boolean enabled;
 	//protected ISprite selectedSprite = null;
 	
 	protected boolean hover;
 	
 	protected boolean clickedLastTick = false;
 
 	protected Color hoverColor = new Color(0x5e, 0x32, 0x03);
 	protected Color normalColor = new Color(0x29, 0x16,0x2);
 	
 	
 	public Button(String label, int x, int y, int w, int h) {
 		super (x,y, w, h);
 		this.label = label;
 		enabled = true;
 	}
 	
 	public void enable() {
 		enabled = true;
 	}
 	
 	public void disable() {
 		enabled = false;
 	}
 	
 	public void setNormalSprite(ISprite sprite) {
 		sprite.setWidth(getWidth());
 		sprite.setHeight(getHeight());
 		normalSprite = sprite;
 	}
 	
 	public void setHoverSprite(ISprite sprite) {
 		sprite.setWidth(getWidth());
 		sprite.setHeight(getHeight());
 		hoverSprite = sprite;
 	}
 	
 	@Override
 	public void setDim(Vector2f val) {
 		super.setDim(val);
 		
 		if (hoverSprite != null) {
 			hoverSprite.setWidth(getWidth());
 			hoverSprite.setHeight(getHeight());
 		}
 
 		if (normalSprite != null) {
 			normalSprite.setWidth(getWidth());
 			normalSprite.setHeight(getHeight());
 		}
 	}
 	
 	@Override
 	public void render(float deltaTime) {
 		
 		if (hoverSprite == null && normalSprite == null /*&& selectedSprite == null*/) {
 			System.out.println("button needs sprites to be displayed");
 			return;
 		}
 		ISprite sprite = normalSprite;
 
 		if (hover && enabled) {
 			sprite = hoverSprite;
 		}
 		/*if (selectedSprite) = selectedSprite;*/
 		if (enabled) {
 			sprite.setColor(0xffffff);
 		} else {
 			sprite.setColor(0xbbbbbb);
 		}
 		sprite.draw(pos.getX(), pos.getY());
 		
 		Color color = normalColor;
 		
 		if (hover && enabled) {
 			color = hoverColor;
 		}
 		
 		float x = (float) (pos.getX() + getWidth() / 2 - font.getWidth(label) / 2);
 		float y = (float)(pos.getY() + getHeight() / 2 - font.getHeight(label) / 2);
 		font.drawString(x, y, label, color);
 	}
 
 	@Override
 	public void onHover(int x, int y) {
 		hover = true;
 	}
 	
 	@Override
 	public void onExit() {
 		hover = false;
 	}
 
 	@Override
 	public void onLeftClick(int x, int y) {
 		if (enabled) {
 			notifyActionListeners();
 		}
 	}
 
 	@Override
 	public void onRightClick(int x, int y) {
 		if (enabled) {
 			notifyActionListeners();
 		}
 	}
 
 	@Override
 	public boolean update(float deltaTime) {
 		return false;
 	}
 	
 	
 	/*@Override
 	public boolean update(float deltaTime) {
 		int x = (int) (Mouse.getX() - pos.getX());
 		int y = (int) (Display.getDisplayMode().getHeight() - Mouse.getY() - pos.getY());
 		
 		if (x < 0 || x > size.getWidth() || y < 0 || y > size.getHeight()) { //outside
 			hover = false;
 			return false;
 		}
 		
 		if (Mouse.isButtonDown(0)) {
 			selected = true;
 			if (!clickedLastTick) {
 				notifyActionListeners();
 			} 
 			clickedLastTick = true;
 		} else {
 			hover = true;
 			clickedLastTick = false;
 		}
 		
 		return clickedLastTick;
 	}*/
 }
