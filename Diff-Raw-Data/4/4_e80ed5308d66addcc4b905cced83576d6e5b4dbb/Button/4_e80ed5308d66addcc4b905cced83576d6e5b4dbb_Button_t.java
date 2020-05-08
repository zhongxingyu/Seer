 package game.util.UI;
 
 import java.awt.Rectangle;
 
 import game.util.IO.Event.Event;
 import game.util.IO.Event.EventListner;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 
 public class Button extends BasicUIComponent {
 
 	private EventListner mouseOver, mouseDown, mouseUp, mouseLeave;
 	private Image graphic, normal, over, down;
 	private Color filter;
 	public Button() {
 		setupEventListners();
 	}
 	public Button(Image normal, Image over, Image down) {
 		setupEventListners();
 		this.normal = normal;
 		this.over = over;
 		this.down = down;
 		graphic = normal;
 	}
 	
 	private void setupEventListners() {
 		// Over
 		super.addMouseOverEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				if (graphic != down)
 					graphic = over;
 				if (mouseOver != null)
 					mouseOver.Invoke(sender, e);
 			}
 		});
 		// Down
 		super.addMouseDownEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				graphic = down;
 				if (mouseDown != null)
 					mouseDown.Invoke(sender, e);
 			}
 		});
 		// Up
 		super.addMouseUpEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				graphic = over;
 				if (mouseUp != null)
 					mouseUp.Invoke(sender, e);
 			}
 		});
 		// leave
 		super.addMouseLeaveEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				graphic = normal;
 				if (mouseLeave != null)
 					mouseLeave.Invoke(sender, e);
 			}
 		});
 	}
 
 	@Override
 	public void addMouseOverEventListner(EventListner delegate) {
 		if (mouseOver == null)
 			mouseOver = delegate;
 	}
 	@Override
 	public void addMouseUpEventListner(EventListner delegate) {
 		if (mouseDown == null)
 			mouseDown = delegate;
 	}
 	@Override
 	public void addMouseDownEventListner(EventListner delegate) {
 		if (mouseUp == null)
 			mouseUp = delegate;
 	}
 	@Override
 	public void addMouseLeaveEventListner(EventListner delegate) {
 		if (mouseLeave == null)
 			mouseLeave = delegate;
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g) {
 		if (!isEnabled()) return;
 		if (graphic == null) return;
 		Rectangle rec = getRectangle();
 		if (filter == null) {
 			g.drawImage(graphic, rec.x, rec.y, rec.x + rec.width, rec.y +rec.height,
 					0, 0, graphic.getWidth(), graphic.getHeight());
 		} else {
 			g.drawImage(graphic, rec.x, rec.y, rec.x + rec.width, rec.y +rec.height,
 					0, 0, graphic.getWidth(), graphic.getHeight(), filter);
 		}
 	}
 }
