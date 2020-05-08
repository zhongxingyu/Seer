 package itemfiler.ui;
 
 import itemfiler.model.Item;
 
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 public class ImageViewer extends ApplicationWindow {
 
 	private static Label imageLabel;
 	private static Image fullImage;
 	private Image currentImage;
 
 	public static void showItem(Item item) {
		if (null != imageLabel && !imageLabel.isDisposed()) {
 			fullImage = item.getImage();
 			imageLabel.redraw();
 		}
 	}
 
 	public static void show() {
 		ImageViewer iv = new ImageViewer(null);
 		iv.open();
 	}
 
 	public ImageViewer(Shell parentShell) {
 		super(parentShell);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	protected void configureShell(Shell shell) {
 		shell.setSize(400, 400);
 		super.configureShell(shell);
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite container = (Composite) super.createContents(parent);
 		container.setLayout(new FillLayout());
 
 		imageLabel = new Label(container, SWT.CENTER);
 		imageLabel.setBackground(Display.getCurrent().getSystemColor(
 				SWT.COLOR_DARK_GRAY));
 		
 		imageLabel.addPaintListener(new PaintListener() {
 			
 
 			@Override
 			public void paintControl(PaintEvent e) {
 				if (null != fullImage && null != imageLabel) {
 					if (null != currentImage)
 						currentImage.dispose();
 
 
 					int targetWidth = imageLabel.getBounds().width;
 					int targetHeight = imageLabel.getBounds().height;
 
 					// get target size
 					if (fullImage.getBounds().width > fullImage.getBounds().height)
 						targetHeight = (int) ((float) fullImage.getBounds().height
 								/ fullImage.getBounds().width * targetWidth);
 					else
 						targetWidth = (int) ((float) fullImage.getBounds().width
 								/ fullImage.getBounds().height * targetHeight);
 
 					// consider window size
 					if (imageLabel.getBounds().width < targetWidth) {
 						targetHeight = (int) ((float) targetHeight
 								/ targetWidth * imageLabel.getBounds().width);
 						targetWidth = imageLabel.getBounds().width;
 					} else if (imageLabel.getBounds().height < targetHeight) {
 						targetWidth = (int) ((float) targetWidth / targetHeight * imageLabel
 								.getBounds().height);
 						targetHeight = imageLabel.getBounds().height;
 					}
 
 					currentImage = new Image(Display.getCurrent(), targetWidth,
 							targetHeight);
 
 					GC gc = new GC(currentImage);
 					gc.setBackground(Display.getCurrent().getSystemColor(
 							SWT.COLOR_WIDGET_DARK_SHADOW));
 					gc.fillRectangle(currentImage.getBounds());
 					gc.setAdvanced(true);
 					gc.setAntialias(SWT.ON); // is about 10% slower if activated
 					gc.setInterpolation(SWT.HIGH);
 					gc.drawImage(fullImage, 0, 0, fullImage.getBounds().width,
 							fullImage.getBounds().height, 20, 20,
 							targetWidth - 40, targetHeight - 40);
 					gc.dispose();
 					imageLabel.setImage(currentImage);
 				}
 			}
 		});
 
 		container.layout();
 		return container;
 	}
 
 }
