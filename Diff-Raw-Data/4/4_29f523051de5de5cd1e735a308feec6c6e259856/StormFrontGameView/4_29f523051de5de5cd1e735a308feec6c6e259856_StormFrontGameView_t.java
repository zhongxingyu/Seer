 package cc.warlock.rcp.stormfront.ui.views;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Caret;
 
 import cc.warlock.core.client.IProperty;
 import cc.warlock.core.client.IPropertyListener;
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.WarlockColor;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.client.IStormFrontClientViewer;
 import cc.warlock.core.stormfront.client.StormFrontColor;
 import cc.warlock.core.stormfront.client.IStormFrontClient.GameMode;
 import cc.warlock.core.stormfront.serversettings.server.ServerSettings;
 import cc.warlock.core.stormfront.style.IHighlightStringStyle;
 import cc.warlock.rcp.stormfront.adapters.SWTStormFrontClientViewer;
 import cc.warlock.rcp.stormfront.ui.StormFrontMacros;
 import cc.warlock.rcp.stormfront.ui.style.StormFrontStyleProvider;
 import cc.warlock.rcp.ui.IStyleProvider;
 import cc.warlock.rcp.ui.StyleRangeWithData;
 import cc.warlock.rcp.ui.WarlockText;
 import cc.warlock.rcp.ui.style.StyleProviders;
 import cc.warlock.rcp.util.ColorUtil;
 import cc.warlock.rcp.views.GameView;
 
 public class StormFrontGameView extends GameView implements IStormFrontClientViewer {
 	
 	public static final String VIEW_ID = "cc.warlock.rcp.stormfront.ui.views.StormFrontGameView";
 	
 	protected IStormFrontClient sfClient;
 	
 	public StormFrontGameView ()
 	{
 		super();
 
 		wrapper = new SWTStormFrontClientViewer(this);
 	}
 	
 	private ProgressMonitorDialog settingsProgressDialog;
 	public void startDownloadingServerSettings() {
 		settingsProgressDialog = new ProgressMonitorDialog(getSite().getShell());
 		settingsProgressDialog.setBlockOnOpen(false);
 		settingsProgressDialog.open();
 		
 		IProgressMonitor monitor = settingsProgressDialog.getProgressMonitor();
 		monitor.beginTask("Downloading server settings...", SettingType.values().length);
 	}
 	
 	public void receivedServerSetting(SettingType settingType)
 	{
 		IProgressMonitor monitor = settingsProgressDialog.getProgressMonitor();
 		monitor.subTask("Downloading " + settingType.toString() + "...");
 		
 		monitor.worked(1);
 	}
 	
 	public void finishedDownloadingServerSettings() {
 		IProgressMonitor monitor = settingsProgressDialog.getProgressMonitor();
 		monitor.done();
 		settingsProgressDialog.close();
 	}
 	
 	@Override
 	public void setClient(IWarlockClient client) {
 		super.setClient(client);
 		
 		if (client instanceof IStormFrontClient)
 		{
 			addStream(client.getStream(IStormFrontClient.DEATH_STREAM_NAME));
 			sfClient = (IStormFrontClient) client;
 
 			StyleProviders.setStyleProvider(client, new StormFrontStyleProvider(sfClient.getServerSettings()));
 			
 			compass.setCompass(sfClient.getCompass());
 			sfClient.getGameMode().addListener(new IPropertyListener<GameMode>() {
 				public void propertyActivated(IProperty<GameMode> property) {}
 				public void propertyChanged(IProperty<GameMode> property, GameMode oldValue) {
 					setBufferingStyles(property.get() == GameMode.Game);
 				}
 				public void propertyCleared(IProperty<GameMode> property,	GameMode oldValue) {}
 			});
 		}
 	}
 	
 	protected Font normalFont;
 	
 	public void loadServerSettings (ServerSettings settings)
 	{
 		WarlockColor bg = settings.getMainWindowSettings().getBackgroundColor();
 		WarlockColor fg = settings.getMainWindowSettings().getForegroundColor();
 		
 		String fontFace = settings.getMainWindowSettings().getFontFace();
 		int fontSize = settings.getMainWindowSettings().getFontSizeInPoints();
 		
 		boolean fontFaceEmpty = (fontFace == null || fontFace.length() == 0);
 		normalFont = fontFaceEmpty ? JFaceResources.getDefaultFont() : new Font(getSite().getShell().getDisplay(), fontFace, fontSize, SWT.NONE);
 		text.setFont(normalFont);
 		
 		WarlockColor entryBG = settings.getCommandLineSettings().getBackgroundColor();
 		WarlockColor entryFG = settings.getCommandLineSettings().getForegroundColor();
 		WarlockColor entryBarColor = settings.getCommandLineSettings().getBarColor();
 		
 		entry.setForeground(ColorUtil.warlockColorToColor(entryFG.equals(StormFrontColor.DEFAULT_COLOR) ? fg  : entryFG));
 		entry.setBackground(ColorUtil.warlockColorToColor(entryBG.equals(StormFrontColor.DEFAULT_COLOR) ? bg : entryBG));
 		
 		Caret newCaret = createCaret(1, ColorUtil.warlockColorToColor(entryBarColor));
 		entry.setCaret(newCaret);
 		
 		text.setBackground(ColorUtil.warlockColorToColor(bg));
 		text.setForeground(ColorUtil.warlockColorToColor(fg));
 		
 		StormFrontMacros.addMacrosFromServerSettings(settings);
 		
 		if (HandsView.getDefault() != null)
 		{
 			HandsView.getDefault().loadServerSettings(settings);
 		}
 		if (StatusView.getDefault() != null)
 		{
 			StatusView.getDefault().loadServerSettings(settings);
 		}
 	}
 	
 	@Override
 	public void streamReceivedStyle(IStream stream, IWarlockStyle style) {
 		if (!(style instanceof IHighlightStringStyle)) {
 			super.streamReceivedStyle(stream, style);
 			return;
 		}
 		
 		IHighlightStringStyle highlightStyle = (IHighlightStringStyle) style;
 		WarlockText text = getTextForClient(client);
		int charCount = getCharCount(text);
 		
 		if (!highlightStyle.isEndStyle())
 		{	
 			StyleRangeWithData range = new StyleRangeWithData();
 			range.start = charCount;
 			range.background = ColorUtil.warlockColorToColor(highlightStyle.getHighlightString().getBackgroundColor());
 			range.foreground = ColorUtil.warlockColorToColor(highlightStyle.getHighlightString().getForegroundColor());
 			if (unendedRanges.size() > 0)
 			{
 				range.font = unendedRanges.peek().font;
 			}
 			
 			if (highlightStyle.getHighlightString().isFillEntireLine())
 			{
 				range.data.put(IStyleProvider.FILL_ENTIRE_LINE, true+"");
 			}
 			unendedRanges.push(range);
 		}
 		else
 		{
 			StyleRangeWithData range = unendedRanges.pop();
 			range.length = charCount - range.start;
 			
 			if (unendedRanges.size() == 0) {
 				addStyleRange(text, range);
 			} else {
 				innerRanges.push(range);
 			}
 		}
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
 }
