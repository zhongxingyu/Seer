 package com.versionone.taskview.views.htmleditor;
 
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.ui.PlatformUI;
 
 import com.versionone.common.Activator;
 
 import de.spiritlink.richhtml4eclipse.widgets.HtmlComposer;
 import de.spiritlink.richhtml4eclipse.widgets.JavaScriptCommands;
 
 public class HTMLEditor extends Dialog {
 
     static int WINDOW_HEIGHT = 300;
     static int WINDOW_WIDTH = 450;
     private final String richText;
     private String value = "";
     private HtmlComposer composer;
 
     public HTMLEditor(Shell parentShell, String richText) {
         super(parentShell);
 
         this.richText = richText;
         setShellStyle(this.getShellStyle() | SWT.RESIZE);
     }
 
     /**
      * {@link #createDialogArea(Composite)}
      */
     @Override
     protected Control createDialogArea(Composite parent) {
         final Composite container = (Composite) super.createDialogArea(parent);        
         container.setLayout(new GridLayout(1, true));
         container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));//SWT.FILL, SWT.FILL
         
         ToolBar menu = new ToolBar(container, SWT.HORIZONTAL | SWT.FLAT);//
         ToolBarManager manager = new ToolBarManager(menu);
 
         composer = new HtmlComposer(container, SWT.BORDER | SWT.SCROLL_LINE);
         composer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         composer.setFocus();

        //TODO debug info
        Activator.logError(richText, new Exception());
        Activator.logError(JavaScriptCommands.SET_HTML(richText), new Exception());
         
         composer.execute(JavaScriptCommands.SET_HTML(richText));
 
 
         manager.add(new BoldAction(composer));
         manager.add(new ItalicAction(composer));
         manager.add(new UnderLineAction(composer));
         manager.add(new StrikeThroughAction(composer));
         manager.add(new RemoveFormatAction(composer));
         manager.add(new Separator());
         manager.add(new JustifyLeftAction(composer));
         manager.add(new JustifyCenterAction(composer));
         manager.add(new JustifyRightAction(composer));
         manager.add(new JustifyFullAction(composer));
         manager.add(new Separator());
         manager.add(new BulletListAction(composer));
         manager.add(new NumListAction(composer));
         manager.add(new OutdentAction(composer));
         manager.add(new IndentAction(composer));
         manager.add(new Separator());
         manager.add(new Separator());
         manager.add(new UndoAction(composer));
         manager.add(new RedoAction(composer));        
         
         manager.update(true);
 
         return container;
     }
     
 
     @Override
     protected void createButtonsForButtonBar(Composite parent) {
         // create OK and Cancel buttons by default
         createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
         createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
     }
 
     /**
      * {@link #configureShell(Shell)}
      */
     @Override
     protected void configureShell(Shell newShell) {
         super.configureShell(newShell);
         newShell.setText("Description editor");
         Display display = PlatformUI.getWorkbench().getDisplay();
         Point size = newShell.computeSize(WINDOW_WIDTH, WINDOW_HEIGHT);
         Rectangle screen = display.getMonitors()[0].getBounds();
         newShell.setBounds((screen.width - size.x) / 2, (screen.height - size.y) / 2, size.x, size.y);
         newShell.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
         newShell.setMinimumSize(WINDOW_WIDTH, WINDOW_HEIGHT);
     }
 
     @Override
     protected void okPressed() {
         value = composer.getHtml();
         super.okPressed();
     }
     
     public String getValue() {
         return value;
     }
     
 }
