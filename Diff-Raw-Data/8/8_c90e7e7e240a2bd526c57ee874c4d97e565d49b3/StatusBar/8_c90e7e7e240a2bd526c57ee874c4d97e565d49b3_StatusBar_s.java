 package org.xtest.runner.statusbar;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
 
 import com.google.inject.Inject;
 
 /**
  * Status bar providing feedback to the user on the status of running Xtest tests.
  * 
  * @author Michael Barry
  */
 public class StatusBar extends WorkbenchWindowControlContribution implements
         IStatusBarRepaintListener {
     private Composite composite;
     private final StatusBarController controller;
     private final RGB green = new RGB(0x51, 0xa3, 0x51);
     private Label label;
     private Image progressBackground;
     private final RGB red = new RGB(0xbd, 0x36, 0x2f);
 
     @Inject
     private StatusBar(StatusBarController controller) {
         this.controller = controller;
         controller.addListener(this);
     }
 
     @Override
     public void dispose() {
         super.dispose();
         controller.removeListener(this);
     }
 
     @Override
     public void schedulePaint() {
         Display.getDefault().asyncExec(new Runnable() {
             @Override
             public void run() {
                 paint();
             }
 
         });
     }
 
     @Override
     protected Control createControl(Composite parent) {
         // create components and apply layout
         composite = parent;
         composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
         composite.addListener(SWT.Resize, new Listener() {
             @Override
             public void handleEvent(Event e) {
                 paint();
             }
         });
 
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayoutFactory.fillDefaults().numColumns(1).margins(3, 3).applyTo(composite);
         label = new Label(composite, SWT.BOLD);
         label.setText("9999/9999");
         GridDataFactory.fillDefaults().applyTo(label);
         schedulePaint();
         return composite;
     }
 
     private void paint() {
         Image oldImage = progressBackground;
         if (!composite.isDisposed()) {
             Display display = composite.getDisplay();
             Rectangle rect = composite.getClientArea();
             boolean horizontal = rect.width > rect.height;
             int boundWidth = rect.width;
             int boundHeight = rect.height;
             progressBackground = new Image(display, rect.width, rect.height);
             GC gc = new GC(progressBackground);
             try {
                 RGB rgb = controller.isPassing() ? green : red;
                 Color color = new Color(Display.getDefault(), rgb);
                 try {
 
                     Rectangle unknown;
                     Rectangle progress;
                     double completionRatio = controller.getCompletionRatio();
                     if (horizontal) {
                         boundWidth -= 1;
                     }
                     if (horizontal) {
                         int horizontalDivide = (int) (completionRatio * boundWidth);
                         int remainder = boundWidth - horizontalDivide;
                         unknown = new Rectangle(horizontalDivide, 0, remainder, boundHeight);
                         progress = new Rectangle(0, 0, horizontalDivide, boundHeight);
                     } else {
                         int verticalDivide = (int) (completionRatio * boundHeight);
                         int remainder = boundHeight - verticalDivide;
                         unknown = new Rectangle(0, 0, boundWidth, remainder);
                         progress = new Rectangle(0, remainder, boundWidth, verticalDivide);
                     }
 
                     gc.setBackground(color);
                     gc.fillRectangle(progress);
 
                     gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
                     gc.fillRectangle(unknown);
                     label.setText(controller.getText());
 
                     if (horizontal) {
                         gc.setBackground(Display.getDefault().getSystemColor(
                                 SWT.COLOR_WIDGET_BACKGROUND));
                         gc.fillRectangle(boundWidth, 0, 4, boundHeight);
                     }
                 } finally {
                     color.dispose();
                 }
             } finally {
                 gc.dispose();
             }
             composite.setBackgroundImage(progressBackground);
 
             // If there was an old image, get rid of it now
             if (oldImage != null) {
                 oldImage.dispose();
             }
         }
     }
 }
