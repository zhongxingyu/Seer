 package itemfiler.ui;
 import itemfiler.model.Item;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 
 
 public class ListItem extends Composite {
 
 	private Label iconLabel;
 	private Label nameLabel;
 	private boolean selected = false;
 	private Item myObject;
 
 	public ListItem(Composite parent, int style, Item current) {
 		super(parent, style | SWT.TRANSPARENT);
 		myObject = current;
 		this.setLayout(new GridLayout(2, false));
 
 		iconLabel = new Label(this, SWT.CENTER);
 		iconLabel.setImage(Display.getCurrent()
 				.getSystemImage(SWT.ICON_WORKING));
 		getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				iconLabel.setImage(myObject.getThumbnail());
 				iconLabel.redraw();
 			}
 		});
 
 		iconLabel.setBackground(Display.getCurrent().getSystemColor(
 				SWT.COLOR_LIST_BACKGROUND));
 		iconLabel.setLayoutData(new GridData(100, 100));
 		// nameLabel = new Label(this, SWT.NONE);
 		// nameLabel.setText(current.getName());
 		// nameLabel.setBackground(Display.getCurrent().getSystemColor(
 		// SWT.COLOR_LIST_BACKGROUND));
 		setSelected(false);
 		this.layout();
 	}
 
 	@Override
 	public void addMouseListener(MouseListener listener) {
 		super.addMouseListener(listener);
 
 		iconLabel.addMouseListener(listener);
 		// nameLabel.addMouseListener(listener);
 	}
 
 	public void setSelected(boolean b) {
 		selected = b;
 		if (selected) {
 			iconLabel.setBackground(Display.getCurrent().getSystemColor(
 					SWT.COLOR_LIST_SELECTION));
 			// nameLabel.setBackground(Display.getCurrent().getSystemColor(
 			// SWT.COLOR_LIST_SELECTION));
 			this.setBackground(Display.getCurrent().getSystemColor(
 					SWT.COLOR_LIST_SELECTION));
 		} else {
 			iconLabel.setBackground(Display.getCurrent().getSystemColor(
 					SWT.COLOR_DARK_GRAY));
 			// nameLabel.setBackground(Display.getCurrent().getSystemColor(
 			// SWT.COLOR_LIST_BACKGROUND));
 			this.setBackground(Display.getCurrent().getSystemColor(
 					SWT.COLOR_DARK_GRAY));
 		}
 
 	}
 
 	public boolean getSelected() {
 		return selected;
 	}
 
 	public Item getObject() {
 		return myObject;
 	}
 
 }
