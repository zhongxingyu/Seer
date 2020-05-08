 package presenter.ui;
 
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.ColorDialog;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Tracker;
 
 
 public class PresenterWindow extends ApplicationWindow {
 
 	private Canvas canvas;
 	private Image image;
 	private boolean goTracker = false;
 	private PresenterControl parentWindow;
	private double x = 0, y = 0, width = 1, height = 1;
 	
 	protected PresenterWindow(PresenterControl controlWindow) {
 		super(null);
 	    setShellStyle(SWT.RESIZE | SWT.MIN | SWT.MAX);
 		parentWindow = controlWindow;
 	}
 	
 	public int getXOffset() {
 		return x;
 	}
 	
 	public int getYOffset() {
 		return y;
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setLayout(int xoffset, int yoffset, int width, int height)
 	{
 		x = xoffset;
 		y = yoffset;
 		this.width = width;
 		this.height = height;
 		refreshLayout();
 	}
 
 	public void refreshLayout() {
		canvas.setBounds((int) (getShell().getBounds().width * x),
				(int) (getShell().getBounds().height * y), (int) (getShell()
						.getBounds().width * width), (int) (getShell()
						.getBounds().height * height));
 		canvas.update();
 	}
 
 	public void setImage(Image image) {
 		this.image = image;
 		canvas.redraw();
 		canvas.update();
 	}
 
 	public void toggleFullscreen() {
 		getShell().setFullScreen(!getShell().getFullScreen());
 	}
 
 	protected Control createContents(Composite parent) {
 	    getShell().setText("Präsentation");
 	    getShell().setSize(500, 500);
 	    getShell().setBackground(new Color(getShell().getDisplay(), 0, 0, 0));
 	    parent.setLayout(null);
 	    
 	    // canvas neu einpassen wenn sich fenstergröße geändert hat
 		getShell().addControlListener(new ControlListener() {
 			
 			@Override
 			public void controlResized(ControlEvent e) {
 				refreshLayout();
 			}
 			
 			@Override
 			public void controlMoved(ControlEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 
 	    canvas = new Canvas(parent, SWT.NONE);
 	    this.setLayout(0, 0, 100, 100);
 	    canvas.setBackground(new Color(getShell().getDisplay(), 200, 200, 200));
 
 	    canvas.addPaintListener(new PaintListener() {
 	      public void paintControl(PaintEvent e) {
 	        e.gc.setAdvanced(true);
 	        e.gc.setInterpolation(SWT.HIGH);
 	        if(null != image) {
 	        	int width = image.getBounds().width, height = image.getBounds().height;
 	        	
 	        	//aspect ratio
 	        	double ratio = (double)(width)/height;
 	        	
 	        	// breite einpassen
 	        	width = canvas.getBounds().width;
 	        	height = (int)(width/ratio);
 	        	
 	        	// höhe einpassen
 	        	if(canvas.getBounds().height < height) {
 	        		height = canvas.getBounds().height;
 	        		width = (int)(ratio*height);
 	        	}
 	        	
 	        	// zentrieren
 	        	int xoffset = (int)((canvas.getBounds().width - width)/2.0);
 	        	int yoffset = (int)((canvas.getBounds().height - height)/2.0);
 	        	
 	        	e.gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, xoffset, yoffset, width, height);
 	        }
 	      }
 	    });
     
 	    Menu contextMenu = new Menu(parent);
 	    MenuItem item1 = new MenuItem(contextMenu, SWT.PUSH);
 	    item1.setText("edit Boundingbox");
 	    item1.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				goTracker = true;
				canvas.setVisible(false);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	    
 	    MenuItem item2 = new MenuItem(contextMenu, SWT.PUSH);
 	    item2.setText("set background color");
 	    item2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ColorDialog cd = new ColorDialog(getShell());
 				cd.setText("Select Color");
 				RGB newColor = cd.open();
 				if (newColor == null) {
 				   return;
 				}
 				canvas.setBackground(new Color(getShell().getDisplay(), newColor));
 			}
 		});
 	    MenuItem item3 = new MenuItem(contextMenu, SWT.PUSH);
 	    item3.setText("toggle fullscreen");
 	    item3.addSelectionListener(new SelectionAdapter() {
 	    	@Override
 	    	public void widgetSelected(SelectionEvent e) {
 	    		toggleFullscreen();
 	    	}
 		});
 	    
 	    parent.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseDown(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				toggleFullscreen();
 			}
 		});
 	    
		canvas.getParent().addMouseListener(new MouseListener() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseDown(MouseEvent e) {
 				if(goTracker) {
					Tracker tracker = new Tracker(canvas.getParent(),
							SWT.RESIZE);
 					tracker.setRectangles(new Rectangle[] { new Rectangle(e.x, e.y,
 							1, 1), });
 					tracker.open();
 					goTracker = false;
 					Rectangle result = tracker.getRectangles()[0];

					x = (double) result.x / getShell().getBounds().width;
					y = (double) result.y / getShell().getBounds().height;

					width = (double) result.width
							/ getShell().getBounds().width;
					height = (double) result.height
							/ getShell().getBounds().height;

					canvas.setVisible(true);
 					refreshLayout();
 				}
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				toggleFullscreen();	
 			}
 		});
 
 	    // Das Kontextmenü der Liste "bekannt machen"
 	    canvas.setMenu(contextMenu);
 	    parent.setMenu(contextMenu);
 
 	    parent.addDisposeListener(new DisposeListener() {
 
 			@Override
 			public void widgetDisposed(DisposeEvent arg0) {
 				parentWindow.close();
 			}
 		});
 
 		return canvas;
 	}
 }
