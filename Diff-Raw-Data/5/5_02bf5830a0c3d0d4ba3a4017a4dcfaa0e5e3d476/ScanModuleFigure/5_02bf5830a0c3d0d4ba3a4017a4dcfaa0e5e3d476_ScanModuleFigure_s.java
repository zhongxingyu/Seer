 package de.ptb.epics.eve.editor.gef.figures;
 
 import org.apache.log4j.Logger;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.XYAnchor;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Path;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 import de.ptb.epics.eve.data.scandescription.Connector;
 import de.ptb.epics.eve.data.scandescription.ScanModuleTypes;
 import de.ptb.epics.eve.editor.Activator;
 
 /**
  * 
  * @author Marcus Michalsky
  * @since 1.6
  */
 public class ScanModuleFigure extends Shape {
 
 	private static Logger logger = Logger.getLogger(ScanModuleFigure.class
 			.getName());
 	
 	private String name;
 	private int x;
 	private int y;
 	private int width;
 	private int height;
 	
 	private boolean selected_primary;
 	private boolean contains_errors;
 	
 	private ScanModuleTypes type;
 	
 	private boolean appended_feedback;
 	private boolean nested_feedback;
 	private boolean parent_feedback;
 	
 	// anchor points for incoming, outgoing and nested modules
 	private XYAnchor targetAnchor;
 	private XYAnchor appendedAnchor;
 	private XYAnchor nestedAnchor;
 	
 	private Shape self;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param name the name of the scan module
 	 * @param type the scan module type
 	 * @param x the x coordinate of its initial position
 	 * @param y the y coordinate of its initial position
 	 * @param width (initial) width of the figure
 	 * @param height (initial) height of the figure
 	 */
 	public ScanModuleFigure(String name, ScanModuleTypes type, int x, int y,
 			int width, int height) {
 		this.name = name;
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		
 		this.type = type;
 		
 		this.selected_primary = false;
 		this.contains_errors = false;
 		this.appended_feedback = false;
 		this.nested_feedback = false;
 		this.parent_feedback = false;
 		
 		this.setBackgroundColor(ColorConstants.white);
 		this.setOpaque(true);
 		this.setSize(this.width, this.height);
 		this.setLocation(new Point(this.x, this.y));
 		
 		this.self = this;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		if (name == null) {
 			this.name = "";
 		} else {
 			this.name = name;
 		}
 		this.repaint();
 	}
 
 	/**
 	 * @param x the x to set
 	 */
 	public void setX(int x) {
 		this.x = x;
 		this.refreshAnchors();
 	}
 
 	/**
 	 * @param y the y to set
 	 */
 	public void setY(int y) {
 		this.y = y;
 		this.refreshAnchors();
 	}
 	
 	/**
 	 * @param appended_feedback the appended_feedback to set
 	 */
 	public void setAppended_feedback(boolean appended_feedback) {
 		this.appended_feedback = appended_feedback;
 	}
 
 	/**
 	 * @param nested_feedback the nested_feedback to set
 	 */
 	public void setNested_feedback(boolean nested_feedback) {
 		this.nested_feedback = nested_feedback;
 	}
 
 	/**
 	 * @param parent_feedback the parent_feedback to set
 	 */
 	public void setParent_feedback(boolean parent_feedback) {
 		this.parent_feedback = parent_feedback;
 	}
 
 	/**
 	 * 
 	 * @param p the point to check
 	 * @return {@link de.ptb.epics.eve.data.scandescription.Connector#APPENDED} if 
 	 * <code>x + 2 * width / 3 < p.x < x + width</code>, <br>
 	 * {@link de.ptb.epics.eve.data.scandescription.Connector#NESTED} if 
 	 * <code>y + height / 2 < p.y < y + height</code>.<br>
 	 * Empty String otherwise.
 	 */
 	public String getConnectionType(Point p) {
 		Rectangle bounds = Rectangle.SINGLETON;
 		bounds.setLocation(p.x, p.y);
 		self.translateToRelative(bounds);
 		
 		if (logger.isDebugEnabled()) {
 			logger.debug("SM Location: (" + this.x + ", " + this.y + ")");
 			logger.debug("Location (absolute): (" + p.x + ", " + p.y + ")");
 			logger.debug("Location (relative): (" + bounds.x + ", " + bounds.y
 					+ ")");
 		}
 		
 		if (bounds.x < this.x + this.width
 				&& bounds.x > this.x + 2 * this.width / 3) {
 			return Connector.APPENDED;
 		} else if (bounds.y < this.y + this.height
 				&& bounds.y > this.y + this.height / 2) {
 			return Connector.NESTED;
 		}
 		return "";
 	}
 	
 	/*
 	 * 
 	 */
 	private void refreshAnchors() {
 		if (this.targetAnchor != null) {
 			this.targetAnchor.setLocation(new Point(this.x, this.y
 					+ this.height / 2));
 		}
 		if (this.appendedAnchor != null) {
 			this.appendedAnchor.setLocation(new Point(this.x + this.width,
 					this.y + this.height / 2));
 		}
 		if (this.nestedAnchor != null) {
 			this.nestedAnchor.setLocation(new Point(this.x + this.width / 2,
 					this.y + this.height));
 		}
 	}
 	
 	/**
 	 * @param selected <code>true</code> if
 	 */
 	public void setSelected(boolean selected) {
 		logger.debug(this.name + " is primary ?: " + Boolean.toString(selected));
 		this.selected_primary = selected;
 		this.repaint();
 	}
 
 	/**
 	 * @param type the scan module type
 	 */
 	public void setType(ScanModuleTypes type) {
 		this.type = type;
 	}
 	
 	/**
 	 * @param contains_errors the contains_errors to set
 	 */
 	public void setContains_errors(boolean contains_errors) {
 		this.contains_errors = contains_errors;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void paint(Graphics graphics) {
 		super.paint(graphics);
 		
 		graphics.setAntialias(SWT.ON);
 		graphics.setTextAntialias(SWT.ON);
 		
 		Display display = PlatformUI.getWorkbench().getDisplay();
 		if(selected_primary) {
 			// if it is active (selected) -> change color
 			graphics.setForegroundColor(ColorConstants.titleGradient);
 			graphics.setBackgroundColor(ColorConstants.white);
 		} else {
 			// inactive ones are colorless
 			graphics.setForegroundColor(ColorConstants.white);
 			graphics.setBackgroundColor(ColorConstants.lightGray);
 		}
 		
 		// save the old clipping
 		Rectangle oldClipping = graphics.getClip(new Rectangle());
 
 		Path path = new Path(display);
 		int spline_control_point = 8;
 		
 		path.moveTo(this.bounds.width + this.bounds.x - spline_control_point, 
 					this.bounds.y);
 		path.quadTo(this.bounds.width + this.bounds.x, this.bounds.y, 
 					this.bounds.width + this.bounds.x, this.bounds.y + 
 					spline_control_point);
 		// right
 		path.lineTo(this.bounds.width + this.bounds.x, 
 					this.bounds.y + this.bounds.height - spline_control_point);
 		path.quadTo(this.bounds.width + this.bounds.x, 
 					this.bounds.y + this.bounds.height, 
 					this.bounds.width + this.bounds.x - spline_control_point, 
 					this.bounds.y + this.bounds.height);
 		// bottom
 		path.lineTo(this.bounds.x + spline_control_point, 
 					this.bounds.y + this.bounds.height);
 		path.quadTo(this.bounds.x, this.bounds.y + this.bounds.height, 
 					this.bounds.x, this.bounds.y + this.bounds.height - 
 					spline_control_point);
 		// left
 		path.lineTo(this.bounds.x, this.bounds.y + spline_control_point);
 		path.quadTo(this.bounds.x, this.bounds.y, 
 					this.bounds.x + spline_control_point, this.bounds.y);
 		// top
 		path.close();
 		graphics.setClip(path);
 		graphics.fillGradient(this.bounds, true);
 		
 		// red error bar if module contains errors
 		if(contains_errors) {
 			graphics.setForegroundColor(ColorConstants.white);
 			graphics.setBackgroundColor(ColorConstants.red);
 			graphics.fillGradient(getLocation().x, getLocation().y, 
 								this.bounds.width, 5, false);
 		}
 		
 		if (this.appended_feedback) {
 			graphics.setForegroundColor(ColorConstants.lightGray);
 			graphics.setBackgroundColor(ColorConstants.black);
 			graphics.fillGradient(new Rectangle(this.x + 6* this.width/7,
 					this.y, this.width/7, this.height), false);
 		}
 		if (this.nested_feedback) {
 			if (selected_primary) {
 				graphics.setForegroundColor(ColorConstants.white);
 				graphics.setBackgroundColor(ColorConstants.darkGray);
 			} else {
 				graphics.setForegroundColor(ColorConstants.lightGray);
 				graphics.setBackgroundColor(ColorConstants.darkGray);
 			}
 			graphics.fillGradient(new Rectangle(this.x,
 					this.y + 3*this.height/4, this.width, this.height/4), true);
 		}
 		if (this.parent_feedback) {
 			graphics.setForegroundColor(ColorConstants.black);
 			graphics.setBackgroundColor(ColorConstants.lightGray);
 			graphics.fillGradient(new Rectangle(this.x,
 					this.y, this.width/7, this.height), false);
 		}
 		
 		// draw border
 		graphics.setForegroundColor(ColorConstants.black);
 		graphics.setBackgroundColor(ColorConstants.white);
 		graphics.setLineWidth(2);
 		graphics.drawPath(path);
 		
 		// draw scan module name
 		if (this.type.equals(ScanModuleTypes.CLASSIC)) {
 			graphics.drawText(
 				this.name, this.getLocation().x + 5, this.getLocation().y + 8);
 		} else if (this.type.equals(ScanModuleTypes.SAVE_AXIS_POSITIONS)) {
 			Image save = Activator.getDefault().getImageRegistry().get("SAVE");
 			Image axis = Activator.getDefault().getImageRegistry().get("AXIS");
 			graphics.drawImage(save, 
 					new Point(this.x + this.width/2 - save.getBounds().width - 3,
 							this.y + this.height/2 - save.getBounds().height/2));
 			graphics.drawImage(axis, 
 					new Point(this.x + this.width/2 + 3, 
 							this.y + this.height/2 - save.getBounds().height/2));
 		} else if (this.type.equals(ScanModuleTypes.SAVE_CHANNEL_VALUES)) {
 			Image save = Activator.getDefault().getImageRegistry().get("SAVE");
 			Image channel = Activator.getDefault().getImageRegistry().get("CHANNEL");
 			graphics.drawImage(save, 
 					new Point(this.x + this.width/2 - save.getBounds().width - 3,
 							this.y + this.height/2 - save.getBounds().height/2));
 			graphics.drawImage(channel, 
 					new Point(this.x + this.width/2 + 3, 
 							this.y + this.height/2 - save.getBounds().height/2));
 		}
 		
 		// restore old clipping
 		graphics.setClip(oldClipping);
 		// free
 		path.dispose();
 		
 		if(logger.isDebugEnabled()) {
 			logger.debug("painted ScanModule: " + this.name);
 		}
 	}
 	
 	/**
 	 * Returns the anchor for appended scan modules.
 	 * 
 	 * @return the <code>XYAnchor</code> for appended scan modules
 	 */
 	public XYAnchor getAppendedAnchor() {
 		if (this.appendedAnchor == null) {
 			this.appendedAnchor = new XYAnchor(new Point(this.x + this.width,
 					this.y + this.height / 2)) {
 				@Override
 				public Point getLocation(Point reference) {
 					Rectangle bounds = Rectangle.SINGLETON;
 					bounds.setBounds(self.getBounds());
 					self.translateToAbsolute(bounds);
 					return new Point(bounds.x + bounds.width, bounds.y
 							+ bounds.height / 2);
 				}
 			};
 		}
 		return appendedAnchor;
 	}
 
 	/**
 	 * Returns the anchor for nested scan modules.
 	 * 
 	 * @return the <code>XYAnchor</code> for nested scan modules
 	 */
 	public XYAnchor getNestedAnchor() {
 		if (this.nestedAnchor == null) {
 			this.nestedAnchor = new XYAnchor(new Point(this.x + this.width / 2,
 					this.y + this.height)) {
 				@Override
 				public Point getLocation(Point reference) {
 					Rectangle bounds = Rectangle.SINGLETON;
 					bounds.setBounds(self.getBounds());
 					self.translateToAbsolute(bounds);
 				return new Point(bounds.x + bounds.width / 2, bounds.y
 						+ bounds.height);
 				}
 			};
 		}
 		return nestedAnchor;
 	}
 
 	/**
 	 * Returns the anchor targeting the scan module.
 	 * 
 	 * @return the <code>XYAnchor</code> targeting the scan module
 	 */
 	public XYAnchor getTargetAnchor() {
 		if (this.targetAnchor == null) {
 			this.targetAnchor = new XYAnchor(new Point(this.x, this.y
 					+ this.height / 2)){
 				@Override
 				public Point getLocation(Point reference) {
 					Rectangle bounds = Rectangle.SINGLETON;
 					bounds.setBounds(self.getBounds());
 					self.translateToAbsolute(bounds);
 					return new Point(bounds.x, bounds.y + bounds.height/2);
 				}
 			};
 		}
 		return targetAnchor;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void fillShape(Graphics graphics) {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void outlineShape(Graphics graphics) {
 	}
 }
