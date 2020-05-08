 package org.zend.php.common;
 
 import java.net.URL;
 
 import org.eclipse.equinox.internal.p2.discovery.AbstractCatalogSource;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.Messages;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseTrackAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
 
 public class ToolTipHandler {
 
 	private Shell tipShell;
 
 	private Label tipLabelImage, tipLabelName, tipLabelText, tipLabelProvider;
 
 	private Widget tipWidget; // widget this tooltip is hovering over
 
 	private Point tipPosition; // the position being hovered over
 
 	Link link;
 
 	String url;
 
 	private int tipWidth = 250;
 
 	/**
 	 * Creates a new tooltip handler
 	 * 
 	 * @param parent
 	 *            the parent Shell
 	 */
 	public ToolTipHandler(Shell parent) {
 		createTooltip(parent);
 	}
 
 	private void createTooltip(Shell parent) {
 		Display display = parent.getDisplay();
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 2;
 		gridLayout.marginWidth = 2;
 		gridLayout.marginHeight = 2;
 		tipShell = new Shell(parent, SWT.ON_TOP | SWT.TOOL);
 		tipShell.setLayout(gridLayout);
 		GridData gds = new GridData();
 		gds.widthHint = tipWidth;
 		tipShell.setLayoutData(gds);
 
 		tipShell.setBackground(display
 				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
 
 		tipLabelImage = new Label(tipShell, SWT.NONE);
 		tipLabelImage.setForeground(display
 				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
 		tipLabelImage.setBackground(display
 				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
 		tipLabelImage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL,
 				GridData.VERTICAL_ALIGN_BEGINNING, false, true, 1, 1));
 
 		tipLabelName = new Label(tipShell, SWT.NONE);
 		tipLabelName.setForeground(display
 				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
 		tipLabelName.setBackground(display
 				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
 		tipLabelName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
 				| GridData.VERTICAL_ALIGN_CENTER));
 
 		FontData fd = tipLabelName.getFont().getFontData()[0];
 		fd.setStyle(SWT.BOLD);
 		tipLabelName.setFont(new Font(display, fd));
 
 		tipLabelText = new Label(tipShell, SWT.WRAP);
 		tipLabelText.setForeground(display
 				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
 		tipLabelText.setBackground(display
 				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
 		GridData textGD = new GridData();
 		textGD.widthHint = tipWidth;
 		textGD.horizontalSpan = 2;
 		tipLabelText.setLayoutData(textGD);
 
 		tipLabelProvider = new Label(tipShell, SWT.WRAP);
 		tipLabelProvider.setForeground(display
 				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
 		tipLabelProvider.setBackground(display
 				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		tipLabelProvider.setLayoutData(gd);
 
 		link = new Link(tipShell, SWT.NULL);
 		GridDataFactory.fillDefaults().grab(false, false)
 				.align(SWT.BEGINNING, SWT.CENTER).applyTo(link);
 		link.setText(Messages.ConnectorDescriptorToolTip_detailsLink);
 		link.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		link.setLayoutData(gd);
 		link.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				WorkbenchUtil
 						.openUrl(url, IWorkbenchBrowserSupport.AS_EXTERNAL);
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 	}
 
 	/**
 	 * Enables customized hover help for a specified control
 	 * 
 	 * @control the control on which to enable hoverhelp
 	 */
 	public void activateHoverHelp(final Control control) {
 		/*
 		 * Get out of the way if we attempt to activate the control underneath
 		 * the tooltip
 		 */
 		control.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				if (!tipShell.isDisposed() && tipShell.isVisible())
 					tipShell.setVisible(false);
 			}
 		});
 
 		/*
 		 * Trap hover events to pop-up tooltip
 		 */
 		control.addMouseTrackListener(new MouseTrackAdapter() {
 			public void mouseExit(MouseEvent e) {
 				if (!tipShell.isDisposed() && tipShell.isVisible()) {
 					/*
 					 * Check if the mouse exit happened because we move over the
 					 * tooltip
 					 */
 					Rectangle containerBounds = tipShell.getBounds();
 					if (containerBounds.contains(Display.getCurrent()
 							.getCursorLocation())) {
 						return;
 					}
 					tipShell.setVisible(false);
 					tipWidget = null;
 				}
 			}
 
 			public void mouseHover(MouseEvent event) {
 				Point pt = new Point(event.x, event.y);
 				Widget widget = event.widget;
 
 				if (widget instanceof Tree) {
 					Tree w = (Tree) widget;
 					widget = w.getItem(pt);
 				}
 				if (widget == null) {
 					tipShell.setVisible(false);
 					tipWidget = null;
 					return;
 				}
 				if (widget == tipWidget)
 					return;
 				if (widget.getData() instanceof CatalogItem) {
 					tipWidget = widget;
 					if (control != null && !control.isDisposed())
 						showToolTip(control, pt, (CatalogItem) widget.getData());
 				}
 			}
 		});
 
 	}
 
 	private void showToolTip(final Control control, Point pt,
 			final CatalogItem ci) {
 		if (control == null || control.isDisposed() || tipLabelName == null
 				|| tipLabelName.isDisposed() || tipLabelText == null
 				|| tipLabelText.isDisposed()) {
 			createTooltip(control.getShell());
 		}
 
 		tipPosition = control.toDisplay(pt);
 		tipLabelName.setText(ci.getName().toString());
 		tipLabelText.setText(ci.getDescription());
 		String prov = "Provider: " + ci.getProvider();
 		tipLabelProvider.setText(prov);
 		Image image = computeImage(ci.getSource(), ci.getIcon().getImage16());
 		tipLabelImage.setImage(image); // accepts null
 
 		if (ci.getOverview() != null && ci.getOverview().getUrl() != null) {
 			url = ci.getOverview().getUrl();
 			link.setToolTipText(NLS.bind(
 					Messages.ConnectorDescriptorToolTip_detailsLink_tooltip, ci
 							.getOverview().getUrl()));
 
 		}
 
 		tipShell.pack();
 		setHoverLocation(tipShell, tipPosition);
 		tipShell.setVisible(true);
 	}
 
 	public static Image computeImage(AbstractCatalogSource discoverySource,
 			String imagePath) {
 		if (imagePath == null || imagePath.isEmpty()) {
 			return null;
 		}
 		URL resource = discoverySource.getResource(imagePath);
 		if (resource != null) {
 			ImageDescriptor descriptor = ImageDescriptor
 					.createFromURL(resource);
 			if (descriptor != null) {
 				return descriptor.createImage();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Sets the location for a hovering shell
 	 * 
 	 * @param shell
 	 *            the object that is to hover
 	 * @param position
 	 *            the position of a widget to hover over
 	 * @return the top-left location for a hovering box
 	 */
 	private void setHoverLocation(Shell shell, Point position) {
 		Rectangle shellBounds = shell.getBounds();
 		Rectangle displayBounds = shell.getDisplay().getBounds();
 		shellBounds.x = Math.max(
 				Math.min(position.x, displayBounds.width - shellBounds.width),
 				0);
 		shellBounds.y = Math.max(
 				Math.min(position.y + 10, displayBounds.height
 						- shellBounds.height), 0);
 		shell.setBounds(shellBounds);
 	}
 
 }
