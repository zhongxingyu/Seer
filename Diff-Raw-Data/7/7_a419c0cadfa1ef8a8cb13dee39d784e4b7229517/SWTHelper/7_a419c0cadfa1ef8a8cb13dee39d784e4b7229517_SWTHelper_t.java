 package nz.ac.auckland.netlogin.gui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.*;
 
 public class SWTHelper {
 
 	public static final int MINIMUM_BUTTON_WIDTH = 80;
 	
 	/** Highlight different components of the layout to make it easier to find problems */
 	private static final boolean DEBUG_LAYOUT = false;
 	
 	private static Font strongLabelFont;
 	
 	public static void addHorizontalSeparator(Composite panel) {
 		Label separator = new Label(panel, SWT.SEPARATOR | SWT.HORIZONTAL);
 		separator.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 	}
 
 	public static void addHorizontalGap(Composite buttonPanel) {
 		Label padding = new Label(buttonPanel, SWT.HORIZONTAL);
 		padding.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 		if (DEBUG_LAYOUT) padding.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 	}
 
 	public static void addMenuSeparator(Menu menu) {
 		new MenuItem(menu, SWT.SEPARATOR);
 	}
 
 	public static MenuItem createMenuItem(Menu menu, String label, int accelerator) {
 		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(label); // + "\tCtrl+" + String.valueOf(accelerator).toUpperCase()); // will this help?
		menuItem.setAccelerator(SWT.MOD1 | accelerator);
 		return menuItem;
 	}
 
 	public static Menu createMenu(Shell window, Menu menuBar, String label, int accelerator) {
 		Menu menu = new Menu(window, SWT.DROP_DOWN);
 		MenuItem menuItem = new MenuItem(menuBar, SWT.CASCADE);
 		menuItem.setMenu(menu);
        menuItem.setText(label); // + "\tCtrl+" + String.valueOf(accelerator).toUpperCase()); // will this help?
 		menuItem.setAccelerator(accelerator);
 		return menu;
 	}
 
 	public static Font getStrongLabelFont() {
 		if (strongLabelFont == null) {
 			Display display = Display.getCurrent();
 
 			FontData fontData = getDefaultLabelFontData(display);
 			fontData.setStyle(fontData.getStyle() | SWT.BOLD);
 			strongLabelFont = new Font(display, fontData);
 
 			// JFace has a FontRegistry that does this, but the Eclipse artifacts aren't in the Maven repositories (WHY?!),
 			// which makes it difficult to add as a dependency
 			display.addListener(SWT.Dispose, new Listener() {
 				public void handleEvent(Event event) {
 					strongLabelFont.dispose();
 				}
 			});
 		}
 		return strongLabelFont;
 	}
 
 	private static FontData getDefaultLabelFontData(Display display) {
 		// a bit of a hack, can't find a cleaner way to get this
 		Shell shell = new Shell(display, SWT.NONE);
 		Label label = new Label(shell, SWT.NONE);
 		Font labelFont = label.getFont();
 		FontData labelFontData = labelFont.getFontData()[0];
 		shell.dispose();
 		label.dispose();
 		return labelFontData;
 	}
 
 	public static Label createFormLabel(Composite panel, String labelText) {
 		Label label = new Label(panel, SWT.RIGHT);
 		label.setText(labelText);
 		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 		return label;
 	}
 
 	public static Label createStrongLabel(Composite panel, String labelText) {
 		Label label = new Label(panel, SWT.NONE);
 		label.setText(labelText);
 		label.setFont(getStrongLabelFont());
 		label.pack();
 		return label;
 	}
 
 	public static GridLayout createMinimalGridLayout() {
 		GridLayout windowLayout = new GridLayout();
 		windowLayout.marginWidth = 0;
 		windowLayout.marginHeight = 0;
 		windowLayout.verticalSpacing = 0;
 		windowLayout.horizontalSpacing = 0;
 		return windowLayout;
 	}
 
 	public static Composite createButtonPanel(Composite parent) {
 		SWTHelper.addHorizontalSeparator(parent);
 
 		Composite buttonPanel = new Composite(parent, SWT.EMBEDDED);
 		buttonPanel.setLayoutData(new GridData(GridData.FILL, GridData.END, true, false));
 
 		GridLayout buttonPanelLayout = new GridLayout(2, false);
 		buttonPanelLayout.marginWidth = 25;
 		buttonPanelLayout.marginHeight = 7;
 		buttonPanel.setLayout(buttonPanelLayout);
 
 		addHorizontalGap(buttonPanel);
 
 		Composite buttonHolder = new Composite(buttonPanel, SWT.EMBEDDED);
 		RowLayout buttonLayout = new RowLayout(SWT.HORIZONTAL);
 		buttonLayout.fill = true;
 		buttonLayout.spacing = 10;
 		buttonHolder.setLayout(buttonLayout);
 
 		return buttonHolder;
 	}
 
 	public static Button createButton(Composite buttonPanel, String label) {
 		Button button = new Button(buttonPanel, SWT.PUSH);
 		button.setText(label);
 
 		// set the button size to have a minimum
 		// the normal windows default is wider than SWT creates
 		button.pack();
 		int buttonWidth = Math.max(MINIMUM_BUTTON_WIDTH, button.getSize().x);
 		button.setLayoutData(new RowData(buttonWidth, SWT.DEFAULT));
 
 		return button;
 	}
 
 	public static void selectComboItem(Combo combo, String item) {
 		combo.deselectAll();
 		int index = combo.indexOf(item);
 		if (index != -1) combo.select(index);
 	}
 
 	public static GridData formLayoutData() {
 		return new GridData(GridData.FILL, GridData.CENTER, true, false);
 	}
 
 	public static Composite createForm(Composite parent) {
 		Composite formPanel = new Composite(parent, SWT.EMBEDDED);
 		if (DEBUG_LAYOUT) formPanel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
 		GridLayout formLayout = new GridLayout(2, false);
 		formLayout.marginTop = 5;
 		formLayout.marginBottom = 5;
 		formLayout.marginLeft = 10;
 		formLayout.marginRight = 10;
 		formLayout.horizontalSpacing = 10;
 		formLayout.verticalSpacing = 3;
 		formPanel.setLayout(formLayout);
 		return formPanel;
 	}
 
 	public static Composite createMargin(Shell dialog, int width, int height) {
 		Composite margin = new Composite(dialog, SWT.EMBEDDED);
 		FillLayout marginLayout = new FillLayout();
 		marginLayout.marginWidth = width;
 		marginLayout.marginHeight = height;
 		margin.setLayout(marginLayout);
 		return margin;
 	}
 }
