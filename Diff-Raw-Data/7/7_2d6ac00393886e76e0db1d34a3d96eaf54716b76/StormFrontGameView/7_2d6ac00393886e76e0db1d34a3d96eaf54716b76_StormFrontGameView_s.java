 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.rcp.stormfront.ui.views;
 
 import java.net.URL;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MenuAdapter;
 import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Caret;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 import cc.warlock.core.client.ICompass;
 import cc.warlock.core.client.IPropertyListener;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.WarlockClientRegistry;
 import cc.warlock.core.client.WarlockColor;
 import cc.warlock.core.client.WarlockFont;
 import cc.warlock.core.client.internal.WarlockClientListener;
 import cc.warlock.core.configuration.Profile;
 import cc.warlock.core.stormfront.ProfileConfiguration;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.client.IStormFrontClientViewer;
 import cc.warlock.core.stormfront.settings.IStormFrontClientSettings;
 import cc.warlock.rcp.stormfront.StormFrontGameViewConfiguration;
 import cc.warlock.rcp.stormfront.adapters.SWTStormFrontClientViewer;
 import cc.warlock.rcp.stormfront.ui.StormFrontSharedImages;
 import cc.warlock.rcp.stormfront.ui.StormFrontStatus;
 import cc.warlock.rcp.stormfront.ui.actions.ProfileConnectAction;
 import cc.warlock.rcp.stormfront.ui.style.StormFrontStyleProvider;
 import cc.warlock.rcp.stormfront.ui.wizards.SGEConnectWizard;
 import cc.warlock.rcp.ui.IStyleProvider;
 import cc.warlock.rcp.ui.WarlockCompass;
 import cc.warlock.rcp.ui.WarlockPopupAction;
 import cc.warlock.rcp.ui.WarlockSharedImages;
 import cc.warlock.rcp.ui.WarlockWizardDialog;
 import cc.warlock.rcp.ui.client.SWTPropertyListener;
 import cc.warlock.rcp.ui.client.SWTWarlockClientListener;
 import cc.warlock.rcp.ui.style.CompassThemes;
 import cc.warlock.rcp.ui.style.StyleProviders;
 import cc.warlock.rcp.util.ColorUtil;
 import cc.warlock.rcp.util.RCPUtil;
 import cc.warlock.rcp.views.ConnectionView;
 import cc.warlock.rcp.views.GameView;
 
 public class StormFrontGameView extends GameView implements IStormFrontClientViewer {
 	
 	public static final String VIEW_ID = "cc.warlock.rcp.stormfront.ui.views.StormFrontGameView";
 	
 	protected IStormFrontClient sfClient;
 	protected StormFrontStatus status;
 	protected WarlockPopupAction reconnectPopup;
 	private WarlockCompass compass;
 	//protected StormFrontTextBorder textBorder;
 	
 	public StormFrontGameView ()
 	{
 		super();
 
 		wrapper = new SWTStormFrontClientViewer(this);
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		
 		compass = new WarlockCompass(streamText.getTextWidget(), CompassThemes.getCompassTheme("small"));
 		//textBorder = new StormFrontTextBorder(text);
 		
 		((GridLayout)entryComposite.getLayout()).numColumns = 2;
 		status = new StormFrontStatus(entryComposite);
 		
 		String fullId = getViewSite().getId() + ":" + getViewSite().getSecondaryId();
 		String characterName = StormFrontGameViewConfiguration.instance().getProfileId(fullId);
 		
 		final Profile profile = ProfileConfiguration.instance().getProfileByCharacterName(characterName);
 		createReconnectPopup();
 		
 		if (profile != null) {
 			setReconnectProfile(profile);
 			showPopup(reconnectPopup);
 		}
 		else {			
 			setNoReconnectProfile(characterName);
 		}
 	}
 	
 	protected Label reconnectLabel;
 	
 	protected void createReconnectPopup()
 	{
 		reconnectPopup = createPopup();
 		reconnectPopup.setLayout(new GridLayout(2, false));
 		reconnectLabel = new Label(reconnectPopup, SWT.WRAP);
 		
 		final Composite buttons = new Composite(reconnectPopup, SWT.NONE);
 		GridLayout layout = new GridLayout(2, false);
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		buttons.setLayout(layout);
 		
 		reconnect = new Button(buttons, SWT.PUSH);
 		
 		final Button connections = new Button(buttons, SWT.TOGGLE);
 		connections.setText("Connections...");
 		connections.setImage(WarlockSharedImages.getImage(WarlockSharedImages.IMG_CONNECT));
 
 		final Menu profileMenu = createProfileMenu(connections);
 		
 		connections.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Rectangle rect = connections.getBounds();
 				Point pt = new Point(rect.x, rect.y + rect.height);
 				pt = buttons.toDisplay(pt);
 				profileMenu.setLocation(pt.x, pt.y);
 				profileMenu.setVisible(true);
 			}
 		});
 	}
 	
 	protected SelectionListener currentListener;
 	protected void setReconnectProfile (final Profile profile)
 	{
 		String characterName = profile.getName();
 		reconnectLabel.setText("The character \"" + characterName + "\" is currently disconnected.");
 		reconnectLabel.setBackground(reconnectPopup.getBackground());
 
 		reconnect.setText("Login as \"" + characterName + "\"");
 		reconnect.setImage(WarlockSharedImages.getImage(WarlockSharedImages.IMG_RECONNECT));
 		if (currentListener != null)
 			reconnect.removeSelectionListener(currentListener);
 		currentListener = new SelectionAdapter () {
 			public void widgetSelected(SelectionEvent e) {
 				ProfileConnectAction action = new ProfileConnectAction(profile);
 				action.run();
 				hidePopup(reconnectPopup);
 //				reconnectPopup.setVisible(false);
 			}
 		};
 		
 		reconnect.addSelectionListener(currentListener);
 	}
 	
 	protected void setNoReconnectProfile (String characterName)
 	{
 		reconnectLabel.setText("The character \"" + characterName +
 			"\" is not a saved profile, you will need to re-login:");
 		reconnectLabel.setBackground(reconnectPopup.getBackground());
 		reconnect.setText("Login");
 		reconnect.setImage(StormFrontSharedImages.getImage(StormFrontSharedImages.IMG_GAME));
 		if (currentListener != null)
 			reconnect.removeSelectionListener(currentListener);
 		currentListener = new SelectionAdapter () {
 			public void widgetSelected(SelectionEvent e) {
 				WarlockWizardDialog dialog = new WarlockWizardDialog(Display.getDefault().getActiveShell(),
 						new SGEConnectWizard());
 			
 				dialog.open();
 				hidePopup(reconnectPopup);
 			}
 		};
 		
 		reconnect.addSelectionListener(currentListener);
 	}
 	
 	protected Menu createProfileMenu (final Button connections)
 	{
 		Menu menu = new Menu(connections);
 		menu.addMenuListener(new MenuAdapter() {
 			public void menuShown(MenuEvent e) {
 				connections.setSelection(true);
 			}
 			public void menuHidden(MenuEvent e) {
 				connections.setSelection(false);
 			}
 		});
 		
 		for (final Profile profile : ProfileConfiguration.instance().getAllProfiles())
 		{
 			MenuItem item = new MenuItem (menu, SWT.PUSH);
 			item.setText(profile.getName());
 			item.setImage(WarlockSharedImages.getImage(WarlockSharedImages.IMG_CHARACTER));
 			item.addSelectionListener(new SelectionAdapter () {
 				public void widgetSelected(SelectionEvent e) {
 					ProfileConnectAction action = new ProfileConnectAction(profile);
 					action.run();
 					hidePopup(reconnectPopup);
 //					reconnectPopup.setVisible(false);
 				}
 			});
 		}
 		
 		MenuItem item = new MenuItem(menu, SWT.SEPARATOR);
 		item = new MenuItem(menu, SWT.PUSH);
 		item.setText("All Connections...");
 		item.setImage(WarlockSharedImages.getImage(WarlockSharedImages.IMG_CONNECT));
 		item.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ConnectionView.VIEW_ID);
 				} catch (PartInitException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		return menu;
 	}
 	
 	private ProgressMonitorDialog settingsProgressDialog;
 	public void startedDownloadingServerSettings() {
 		settingsProgressDialog = new ProgressMonitorDialog(getSite().getShell());
 		settingsProgressDialog.setBlockOnOpen(false);
 		settingsProgressDialog.open();
 		
 		IProgressMonitor monitor = settingsProgressDialog.getProgressMonitor();
 		monitor.beginTask("Downloading server settings...", IProgressMonitor.UNKNOWN);
 	}
 	
 	public void receivedServerSetting(String setting)
 	{
 		if (settingsProgressDialog == null) {
 			Display.getDefault().readAndDispatch();
 		}
 		
 		IProgressMonitor monitor = settingsProgressDialog.getProgressMonitor();
 		monitor.subTask("Downloading " + setting + "...");
 		
 		monitor.worked(1);
 	}
 	
 	public void finishedDownloadingServerSettings() {
 		IProgressMonitor monitor = settingsProgressDialog.getProgressMonitor();
 		monitor.done();
 		settingsProgressDialog.close();
 	}
 	
 	@Override
 	public void setClient(IWarlockClient client) {
 		client.getCompass().addListener(new SWTPropertyListener<ICompass>(compass));
 		
 		hidePopup(reconnectPopup);
 //		reconnectPopup.setVisible(false);
 		if (client instanceof IStormFrontClient)
 		{
 			sfClient = (IStormFrontClient) client;
 			
 			if (status != null) {
 				status.setActiveClient(sfClient);
 				// textBorder.setActiveClient(sfClient);
 			}
 			
 			IStormFrontClientSettings settings = sfClient.getStormFrontClientSettings();
 			if(settings != null)
 				loadClientSettings(settings);
 			
 			WarlockClientRegistry.addWarlockClientListener(new SWTWarlockClientListener(new WarlockClientListener() {
 				@Override
 				public void clientActivated(IWarlockClient client) {}
 				@Override
 				public void clientConnected(IWarlockClient client) {}
 				@Override
 				public void clientRemoved(IWarlockClient client) {}
 				@Override
 				public void clientDisconnected(IWarlockClient client) {
 					if (client == StormFrontGameView.this.client) {
 						Display.getDefault().asyncExec(new Runnable() {
 							public void run () {
 								StormFrontGameView.this.disconnected();
 							}
 						});
 					}
 				}
 				@Override
 				public void clientSettingsLoaded(IWarlockClient client) {
 					// TODO Auto-generated method stub
 					
 				}
 			}));
 		}
 		
 		super.setClient(client);
 	}
 	
 	protected void disconnected ()
 	{	
 		showPopup(reconnectPopup);
 //		reconnectPopup.setVisible(true);
 	}
 	
 	protected Font normalFont;
 
 	private Button reconnect;
 	
 	public void clientSettingsLoaded(IWarlockClient client) {
 		if(client == sfClient) {
 			loadClientSettings(sfClient.getStormFrontClientSettings());
 		}
 	}
 	
 	public void loadClientSettings(IStormFrontClientSettings settings)
 	{	
 		IStyleProvider styleProvider = new StormFrontStyleProvider(settings);
 		
 		StyleProviders.setStyleProvider(client, styleProvider);
 		
 		sfClient.getCharacterName().addListener(new IPropertyListener<String>() {
 			public void propertyChanged(String value) {
 				String viewId = getViewSite().getId() + ":" + getViewSite().getSecondaryId();
 				
 				/* FIXME: Marshall, can you look the following. I _think_
 				 * sfClient.getCharacterName().get() should just be replaced
 				 * by value (the parameter to this method).
 				 */
 				StormFrontGameViewConfiguration.instance().addProfileMapping(viewId,
 						sfClient.getCharacterName().get());
 				
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						Profile profile = ProfileConfiguration.instance()
 								.getProfileByCharacterName(sfClient.getCharacterName().get());
 						if (profile != null) {
 							setReconnectProfile(profile);
 						} else {
 							setNoReconnectProfile(sfClient.getCharacterName().get());
 						}	
 					}
 				});
 			}
 		});
 		
 		WarlockColor bg = sfClient.getStormFrontSkin().getMainBackground();
 		WarlockColor fg = sfClient.getStormFrontSkin().getMainForeground();
 		
 		WarlockFont mainFont = settings.getMainWindowSettings().getFont();
 		String fontFace = mainFont.getFamilyName();
 		int fontSize = mainFont.getSize();
 //		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
 //			fontSize = settings.getMainWindowSettings().getFontSizeInPixels();
 //		}
 		
 		normalFont = mainFont.isDefaultFont() ? JFaceResources.getDefaultFont() : new Font(getSite().getShell().getDisplay(), fontFace, fontSize, SWT.NONE);
 		streamText.setFont(normalFont);
 		
 		WarlockColor entryBG = settings.getCommandLineSettings().getBackgroundColor();
 		WarlockColor entryFG = settings.getCommandLineSettings().getForegroundColor();
 		WarlockColor entryBarColor = settings.getCommandLineSettings().getBarColor();
 		
 		entry.getWidget().setForeground(ColorUtil.warlockColorToColor(entryFG.isDefault() ? fg  : entryFG));
 		entry.getWidget().setBackground(ColorUtil.warlockColorToColor(entryBG.isDefault() ? bg : entryBG));
 		entry.getWidget().redraw();
 		
 		Caret newCaret = createCaret(1, ColorUtil.warlockColorToColor(entryBarColor.isDefault() ? fg : entryBarColor));
 		entry.getWidget().setCaret(newCaret);
 		
 		streamText.setClient(sfClient);
 		streamText.setBackground(ColorUtil.warlockColorToColor(bg));
 		streamText.setForeground(ColorUtil.warlockColorToColor(fg));
 		streamText.getTextWidget().redraw();
 		
 		if (HandsView.getDefault() != null)
 		{
 			HandsView.getDefault().loadSettings(settings);
 		}
 		
 		if (status != null) {
 			status.loadSettings(settings);
 		}
 	}
 	
 	public void launchURL(final URL url) {
 		Display.getDefault().asyncExec(new Runnable () {
 			public void run () {
 				RCPUtil.openURL(url.toString());
 			}
 		});
 	}
 	
 	public void appendImage(final URL imageURL) {
 //		Display.getDefault().asyncExec(new Runnable () {
 //			public void run () {
 //				text.append(""+WarlockText.OBJECT_HOLDER);
 //				
 //				try {
 //					InputStream imageStream = imageURL.openStream();
 //					
 //					Image image = new Image(text.getDisplay(), imageStream);
 //					imageStream.close();
 //					
 //					text.addImage(image);
 //					
 //				} catch (IOException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}
 //			}
 //		});
 	}
 	
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (IStormFrontClient.class.equals(adapter))
 		{
 			return client;
 		}
 		return super.getAdapter(adapter);
 	}
 	
 	public IStormFrontClient getStormFrontClient() {
 		return sfClient;
 	}
 
 	public void clientActivated(IWarlockClient client) {}
 
 	public void clientConnected(IWarlockClient client) {}
 
 	public void clientDisconnected(IWarlockClient client) {}
 
 	public void clientRemoved(IWarlockClient client) {}
 	
 	protected void setViewTitle(String title) {
 		String prefix = "";
 		String game = sfClient.getGameCode();
 		if(game != null)
 			prefix += "[" + game + "] ";
 		String name = client.getCharacterName().get();
 		if(name != null)
 			prefix += name + " - ";
 		this.setPartName(prefix + title);
 	}
 }
