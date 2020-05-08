 package util.ui;
 
 import org.lwjgl.opengl.GL11;
 import util.geom.Point;
 import util.geom.Rectangle;
 import util.input.Input;
 import util.input.KeyEvent;
 import util.input.MouseEvent;
 
 public class GLSlider extends GLUIComponent {
 
 	protected Rectangle slider;
 
 	protected double lowerBound, upperBound;
 
 	protected int state;
 
 	protected boolean sliderUseBlocked;
 
 	private boolean vertical;
 
 	public GLSlider(GLUITheme theme, int x, int y, int w, int h,
 			int sliderWidth, double lowerBound, double upperBound,
 			boolean vertical) {
 
 		super(theme, x, y, w, h);
 
 		this.slider = new Rectangle(x, y, sliderWidth, h);
 
 		this.state = GLUIComponent.INACTIVE;
 
 		this.vertical = vertical;
 
 		if (vertical)
 			this.slider = new Rectangle(x, y, w, sliderWidth);
 		else
 			this.slider = new Rectangle(x, y, sliderWidth, h);
 
 		this.lowerBound = lowerBound;
 		this.upperBound = upperBound;
 	}
 
 	@Override
 	public void renderGL() {
 
 		// Draw Background
 
 		theme.getBackgroundColor(state).bind();
 		GL11.glBegin(GL11.GL_POLYGON);
 		{
 			GL11.glVertex2i(bounds.getX(), bounds.getY());
 			GL11.glVertex2i(bounds.getX() + bounds.getW(),
 					bounds.getY());
 			GL11.glVertex2i(bounds.getX() + bounds.getW(),
 					bounds.getY() + bounds.getH());
 			GL11.glVertex2i(bounds.getX(),
 					bounds.getY() + bounds.getH());
 		}
 		GL11.glEnd();
 		// Draw Slider
 
 		theme.getForegroundColor(state).bind();
 		GL11.glBegin(GL11.GL_POLYGON);
 		{
 			GL11.glVertex2i(slider.getX(), slider.getY());
 			GL11.glVertex2i(slider.getX() + slider.getW(),
 					slider.getY());
 			GL11.glVertex2i(slider.getX() + slider.getW(),
 					slider.getY() + slider.getH());
 			GL11.glVertex2i(slider.getX(),
 					slider.getY() + slider.getH());
 		}
 		GL11.glEnd();
 	}
 
 	public void processMouseEvent(MouseEvent e) {
 
 		if (e.getButtonCode() == Input.LEFT_MOUSE) {
 			if (e.getEvent() == MouseEvent.BUTTON_PRESSED) {
 				if (state != GLUIComponent.CLICKED) {
 					if (new Point(Input.getMouseLocation()
 							.getX(), Input
 							.getMouseLocation()
 							.getY()).inside(slider)) {
 						this.state = GLUIComponent.CLICKED;
 					} else {
 						sliderUseBlocked = true;
 					}
 				}
 			} else if (e.getEvent() == MouseEvent.BUTTON_RELEASED) {
 				state = GLUIComponent.INACTIVE;
 
 				sliderUseBlocked = false;
 			}
 		}
 
 		if (Input.isMouseButtonDown(Input.LEFT_MOUSE)) {
 
 			if (state == GLUIComponent.CLICKED) {
 
 				if (!vertical) {
 
 					if (Input.getMouseLocation().getX()
 							- (slider.getW() / 2) < bounds
								.getX()) {
 
 						slider.setX(bounds.getX());
 
 					} else if (Input.getMouseLocation()
 							.getX()
 							- (slider.getW() / 2) > (bounds
							.getX() + bounds.getW())
							- slider.getW()) {
 
 						slider.setX((bounds.getX() + bounds
 								.getW())
 								- slider.getW());
 
 					} else {
 
 						slider.setX(Input
 								.getMouseLocation()
 								.getX()
 								- (slider.getW() / 2));
 					}
 				} else {
 
 					if (Input.getMouseLocation().getY()
 							- (slider.getH() / 2) < bounds
								.getY()) {
 
 						slider.setY(bounds.getY());
 
 					} else if (Input.getMouseLocation()
 							.getY()
 							- (slider.getH() / 2) > (bounds
							.getY() + bounds.getH())
							- slider.getH()) {
 
 						slider.setY((bounds.getY() + bounds
 								.getH())
 								- slider.getH());
 
 					} else {
 
 						slider.setY(Input
 								.getMouseLocation()
 								.getY()
 								- (slider.getH() / 2));
 
 					}
 				}
 			}
 		}
 	}
 
 	public double getSliderValue() {
 
		double sliderBarWidth = (double) (bounds.getW() - slider.getW());
		double sliderRelativeX = (double) (slider.getX())
				- bounds.getX();
 
		return ((double) sliderRelativeX / (double) sliderBarWidth)
				* getRange() + lowerBound;
 	}
 
 	private double getRange() {
 
 		return (upperBound - lowerBound);
 	}
 
 	@Override
 	public void processKeyEvent(KeyEvent e) {
 
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void update(int delta) {
 
 		if (state != GLUIComponent.DISABLED) {
 			if (!Input.isMouseButtonDown(Input.LEFT_MOUSE)) {
 				if (this.contains(Input.getMouseLocation())) {
 					state = GLUIComponent.HOVER;
 				} else {
 					state = GLUIComponent.INACTIVE;
 				}
 			}
 		}
 	}
 }
