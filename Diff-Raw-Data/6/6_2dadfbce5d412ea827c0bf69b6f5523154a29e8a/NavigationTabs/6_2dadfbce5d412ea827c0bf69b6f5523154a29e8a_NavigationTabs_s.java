 /**
  * Copyright (C) 2011 / cube-team <https://cube.forge.osor.eu>
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.admin.vbs.cube.client.wm.ui.tabs;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.TreeSet;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.client.wm.client.ICubeClient;
 import ch.admin.vbs.cube.client.wm.client.IVmControl;
 import ch.admin.vbs.cube.client.wm.client.IVmMonitor;
 import ch.admin.vbs.cube.client.wm.client.VmHandle;
 import ch.admin.vbs.cube.client.wm.client.VmHandleHumanComparator;
 import ch.admin.vbs.cube.client.wm.ui.CubeUI.CubeScreen;
 import ch.admin.vbs.cube.client.wm.ui.ICubeUI;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.CubeLayoutAction;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.CubeLogoutAction;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.CubeShutdownAction;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmAttachUsbDevice;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmConnectNic;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmDeleteAction;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmDetachUsbDevice;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmHideAction;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmInstallAdditionsAction;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmPoweroffAction;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmSaveAction;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmStageAction;
 import ch.admin.vbs.cube.client.wm.ui.tabs.action.VmStartAction;
 import ch.admin.vbs.cube.client.wm.utils.I18nBundleProvider;
 import ch.admin.vbs.cube.client.wm.utils.IconManager;
 import ch.admin.vbs.cube.client.wm.xrandx.impl.XrandrTwoDisplayLayout.Layout;
 import ch.admin.vbs.cube.common.CubeClassification;
 import ch.admin.vbs.cube.core.ICoreFacade;
 import ch.admin.vbs.cube.core.network.INetworkManager;
 import ch.admin.vbs.cube.core.usb.UsbDeviceEntry;
 import ch.admin.vbs.cube.core.usb.UsbDeviceEntryList;
 import ch.admin.vbs.cube.core.vm.VmState;
 import ch.admin.vbs.cube.core.vm.vbox.VBoxProduct;
 
 import com.jidesoft.swing.JideMenu;
 import com.jidesoft.swing.JidePopupMenu;
 import com.jidesoft.swing.JideTabbedPane;
 
 public class NavigationTabs extends JideTabbedPane {
 	private static final long serialVersionUID = 1L;
 	/** Logger */
 	private static final Logger LOG = LoggerFactory.getLogger(NavigationTabs.class);
 	private IVmControl vmCtrl;
 	private IVmMonitor vmMon;
 	private TabColorProvider colorProvider;
 	private JidePopupMenu cubePopupMenu;
 	private JPopupMenu vmPopupMenu;
 	private final String monitorId;
 	private ICoreFacade core;
 	private ICubeClient client;
 	private ICubeUI cubeUI;
 	private LogoPanel logo;
 	private INetworkManager networkMgr;
 	private HashMap<String, String> selectedNics = new HashMap<String, String>();
 	private Executor exec = Executors.newCachedThreadPool();
 
 	public NavigationTabs(String monitorId) {
 		this.monitorId = monitorId;
 		// default settings
 		setTabResizeMode(RESIZE_MODE_FIXED); // exact size set in CubeUIDefaults
 		setShowGripper(false); //
 		setShowTabButtons(true);
 		setTabShape(SHAPE_DEFAULT);
 		setColorTheme(COLOR_THEME_DEFAULT);
 		setRightClickSelect(true);
 		colorProvider = new TabColorProvider(this);
 		setTabColorProvider(colorProvider);
 		// setShowGripper(false);
 		setShowTabArea(true);
 		setShowTabContent(false);
 		// we are interested in mouse clicks
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent mouseEvent) {
 				int tabIndex = getTabAtLocation(mouseEvent.getX(), mouseEvent.getY());
 				if (tabIndex >= 0) {
 					TabComponent tabIdentifier = (TabComponent) getComponentAt(tabIndex);
 					if (mouseEvent.isPopupTrigger()) {
 						showTabPopupMenu(NavigationTabs.this, mouseEvent, tabIdentifier.getVmHandle(), tabIndex);
 					}
 				}
 			}
 		});
 		// we are interested in select changes for example: first tab, tab
 		// change per keyboard, ...
 		addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				if (isVisible()) {
 					LOG.trace("stateChanged");
 					synchronized (NavigationTabs.this) {
 						if (NavigationTabs.this.equals(e.getSource()) && vmCtrl != null) {
 							int tabIndex = getSelectedIndex();
 							if (tabIndex >= 0) {
 								Object o = getComponentAt(tabIndex);
 								if (o instanceof TabComponent) {
 									TabComponent comp = (TabComponent) getComponentAt(tabIndex);
 									VmHandle h = comp.getVmHandle();
 									if (comp != null && h != null) {
 										vmCtrl.showVm(h);
 									} else {
 										vmCtrl.hideAllVms(NavigationTabs.this.monitorId);
 									}
 								}
 							} else {
 								vmCtrl.hideAllVms(NavigationTabs.this.monitorId);
 							}
 						}
 					}
 				}
 			}
 		});
 		// add header logo
 		logo = new LogoPanel();
 		logo.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				showLogoPopupMenu(e);
 			}
 		});
 		setTabLeadingComponent(logo);
 	}
 
 	public void setLogo(String iconName) {
 		logo.setIcon(IconManager.getInstance().getIcon(iconName));
 	}
 
 	/**
 	 * Paint a custom line in order to hide unnecessary gray border between the
 	 * tab and tab content. The tab, the content border and the line we draw in
 	 * between match the classification color.
 	 */
 	@Override
 	protected void paintChildren(Graphics g) {
 		super.paintChildren(g);
 		// get color of selected tab
 		int i = getSelectedIndex();
 		if (i >= 0) {
 			Color c = colorProvider.getBackgroundAt(i);
 			if (c != null) {
 				g.setColor(c);
 				g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
 			}
 		}
 	}
 
 	/**
 	 * Shows the popup menu for the given tab.
 	 * 
 	 * @param parent
 	 *            parent which the popup menu belongs to
 	 * @param event
 	 *            mouse event with the coordinates
 	 * @param h
 	 *            vmId of the virtual machine
 	 * @param tabIndex
 	 *            the index of the tab
 	 */
 	private void showTabPopupMenu(final Component parent, final MouseEvent event, final VmHandle h, final int tabIndex) {
 		LOG.debug("showTabPopupMenu [{}]", h);
 		if (h == null)
 			return;
 		ResourceBundle resourceBundle = I18nBundleProvider.getBundle();
 		VmState state = vmMon.getVmState(h);
 		// prepare popup menu
 		// JidePopupMenu popupMenu = new JidePopupMenu();
 		// @03032010: JidePopupMenu is to slow (openjdk6) replace with
 		// JPopupMenu
 		vmPopupMenu = new JPopupMenu();
 		switch (state) {
 		case STOPPED:
 			vmPopupMenu.add(new VmStartAction(h));
 			vmPopupMenu.add(new VmDeleteAction(h));
 			break;
 		case RUNNING:
 			vmPopupMenu.add(new VmSaveAction(h));
 			vmPopupMenu.add(new VmPoweroffAction(h));
 			vmPopupMenu.addSeparator();
 			vmPopupMenu.add(new VmInstallAdditionsAction(h));
 			// allows to connect USB device to VM
 			JideMenu usbMenu = new JideMenu(I18nBundleProvider.getBundle().getString("cube.action.connectusb.text"));
 			vmPopupMenu.add(usbMenu);
 			populateUsbMenu(h, usbMenu);
 			// allows unclassified VMs to be reconnected to local NICs
 			if (vmMon.getVmClassification(h) == CubeClassification.UNCLASSIFIED) {
 				JMenu nicMenu = new JMenu(I18nBundleProvider.getBundle().getString("vm.action.connectnic.text"));
 				vmPopupMenu.add(nicMenu);
 				populateNicMenu(h, nicMenu);
 			}
 			break;
 		case STAGABLE:
 			vmPopupMenu.add(new VmStageAction(h));
 			break;
 		case STAGING:
 			vmPopupMenu.add(new VmStageAction(h, false));
 			break;
 		case STARTING:
 		case STOPPING:
 			vmPopupMenu.add(new VmSaveAction(h, false));
 			// popupMenu.add(new VmStopAction(vmId, false));
 			vmPopupMenu.add(new VmPoweroffAction(h, false));
 			break;
 		// internal errors
 		case ERROR:
 		case UNKNOWN:
 			vmPopupMenu.add(new VmPoweroffAction(h));
 			vmPopupMenu.add(new VmDeleteAction(h));
 			break;
 		default:
 			LOG.error("No menu for state [" + state + "]");
 			return;
 		}
 		// add USB menu
 		if (state == VmState.RUNNING) {
 		}
 		// add Hide menu
 		if (state == VmState.STOPPED || state == VmState.STAGABLE) {
 			vmPopupMenu.addSeparator();
 			vmPopupMenu.add(new VmHideAction(h, false));
 		}
 		// add Monitor move menu
 		List<CubeScreen> screens = cubeUI.getScreens();
 		if (screens.size() > 1) {
 			vmPopupMenu.addSeparator();
 			JideMenu monitorMenu = new JideMenu(resourceBundle.getString("vm.move.monitor.menu"));
 			vmPopupMenu.add(monitorMenu);
 			for (CubeScreen c : screens) {
 				if (!c.getId().equals(monitorId) && c.isActive()) {
 					final String destMonitorId = c.getId();
 					JMenuItem item = new JMenuItem(MessageFormat.format(resourceBundle.getString("vm.move.monitor.menu.item"), destMonitorId));
 					item.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							vmCtrl.moveVm(h, destMonitorId);
 						}
 					});
 					monitorMenu.add(item);
 				}
 			}
 		}
 		//
 		//
		LOG.debug("show mouse popup at [{}:{}]", event.getX(), event.getY());
 		// show popup
 		JComponent comp = (JComponent) event.getSource();
		vmPopupMenu.show(parent, event.getX(), event.getY());
 	}
 
 	private void populateNicMenu(final VmHandle h, final JMenu nicMenu) {
 		nicMenu.setEnabled(false);
 		// load list in another thread since or it will block pop-up
 		// menu for a few milliseconds.
 		exec.execute(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					List<String> nics = networkMgr.getNetworkInterfaces();
 					if (nics.size() > 0) {
 						String selected = selectedNics.get(h.getVmId());
 						JCheckBoxMenuItem chk = new JCheckBoxMenuItem(new VmConnectNic(h, VBoxProduct.ORIGINAL_NETWORK_CONFIG, selectedNics));
 						chk.setSelected(VBoxProduct.ORIGINAL_NETWORK_CONFIG.equals(selected) || selected == null);
 						nicMenu.add(chk);
 						for (String nic : nics) {
 							chk = new JCheckBoxMenuItem(new VmConnectNic(h, nic, selectedNics));
 							chk.setSelected(nic.equals(selected));
 							nicMenu.add(chk);
 						}
 						nicMenu.setEnabled(true);
 					}
 				} catch (Exception e) {
 					LOG.error("Failed to load NIC list");
 				}
 			}
 		});
 	}
 
 	private void populateUsbMenu(final VmHandle h, final JideMenu usbMenu) {
 		usbMenu.setEnabled(false);
 		usbMenu.setIcon(IconManager.getInstance().getIcon("usb_icon16.png"));
 		// load list in another thread since or it will block pop-up
 		// menu for a few milliseconds.
 		exec.execute(new Runnable() {
 			@Override
 			public void run() {
 				UsbDeviceEntryList list = core.getUsbDeviceList(h.getVmId());
 				if (list.size() == 0) {
 					usbMenu.setEnabled(false);
 				} else {
 					usbMenu.setEnabled(true);
 					// add usb device as checkbox items
 					for (UsbDeviceEntry e : list) {
 						switch (e.getState()) {
 						case ALREADY_ATTACHED: {
 							JCheckBoxMenuItem chk = new JCheckBoxMenuItem(new VmDetachUsbDevice(h, e));
 							chk.setSelected(true);
 							usbMenu.add(chk);
 							break;
 						}
 						case AVAILABLE: {
 							JCheckBoxMenuItem chk = new JCheckBoxMenuItem(new VmAttachUsbDevice(h, e));
 							chk.setSelected(false);
 							usbMenu.add(chk);
 							break;
 						}
 						case ATTACHED_TO_ANOTHER_VM: {
 							JMenuItem not = new JMenuItem(e.getDevice().toString());
 							not.setEnabled(false);
 							usbMenu.add(not);
 							break;
 						}
 						default:
 							LOG.error("State not supported [{}]", e.getState());
 							break;
 						}
 					}
 				}
 			}
 		});
 	}
 
 	private void showLogoPopupMenu(final MouseEvent mouseEvent) {
 		// fill popup menu with actions
 		cubePopupMenu = new JidePopupMenu();
 		// cubePopupMenu.add(new CubeLockAction());
 		cubePopupMenu.add(new CubeLogoutAction());
 		cubePopupMenu.addSeparator();
 		cubePopupMenu.add(new CubeShutdownAction());
 		// show VMs menu
 		// get list of hidden VMs
 		JideMenu showMenu = new JideMenu(I18nBundleProvider.getBundle().getString("vm.action.show.text"));
 		showMenu.setIcon(IconManager.getInstance().getIcon("hide_icon16.png"));
 		TreeSet<VmHandle> hidden = new TreeSet<VmHandle>(new VmHandleHumanComparator(vmMon));
 		for (VmHandle h : client.listVms()) {
 			if ("true".equalsIgnoreCase(vmMon.getVmProperty(h, "hidden"))) {
 				hidden.add(h);
 			}
 		}
 		if (hidden.size() == 0) {
 			showMenu.setEnabled(false);
 		} else {
 			for (VmHandle h : hidden) {
 				final String label = String.format("%s, %s (%s)", vmMon.getVmName(h), vmMon.getVmDomain(h), vmMon.getVmClassification(h).name());
 				showMenu.add(new VmHideAction(label, h, true, false));
 			}
 			showMenu.setEnabled(true);
 		}
 		// add multi-screen layout menu
 		List<CubeScreen> screens = cubeUI.getScreens();
 		if (screens.size() > 1) {
 			JideMenu screensMenu = new JideMenu(I18nBundleProvider.getBundle().getString("vm.action.screens.text"));
 			cubePopupMenu.add(screensMenu);
 			screensMenu.add(new CubeLayoutAction(cubeUI, Layout.A, I18nBundleProvider.getBundle().getString("vm.action.screens.a0.text"), IconManager
 					.getInstance().getIcon("screensA0_icon16.png")));
 			screensMenu.add(new CubeLayoutAction(cubeUI, Layout.B, I18nBundleProvider.getBundle().getString("vm.action.screens.0b.text"), IconManager
 					.getInstance().getIcon("screens0B_icon16.png")));
 			screensMenu.add(new CubeLayoutAction(cubeUI, Layout.AB, I18nBundleProvider.getBundle().getString("vm.action.screens.ab.text"), IconManager
 					.getInstance().getIcon("screensAB_icon16.png")));
 			screensMenu.add(new CubeLayoutAction(cubeUI, Layout.BA, I18nBundleProvider.getBundle().getString("vm.action.screens.ba.text"), IconManager
 					.getInstance().getIcon("screensBA_icon16.png")));
 		}
 		// hidden VMs
 		cubePopupMenu.add(showMenu);
 		//
 		cubePopupMenu.addSeparator();
 		cubePopupMenu.add(new JideMenu("WiFi Configuration..."));
 		// place menu under the logo button
 		JComponent comp = (JComponent) mouseEvent.getSource();
 		cubePopupMenu.show(comp, comp.getX(), comp.getY() + comp.getHeight());
 	}
 
 	public void setup(IVmControl vmCtrl, IVmMonitor vmMon, ICoreFacade core, ICubeClient client, ICubeUI cubeUI, INetworkManager networkMgr) {
 		this.vmCtrl = vmCtrl;
 		this.vmMon = vmMon;
 		this.client = client;
 		this.cubeUI = cubeUI;
 		this.networkMgr = networkMgr;
 		colorProvider.setVmMon(vmMon);
 		this.core = core;
 	}
 }
