 package cc.warlock.rcp.ui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 import cc.warlock.core.client.ICommand;
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IStreamListener;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.IWarlockSkin;
 import cc.warlock.core.client.WarlockFont;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.internal.Property;
 import cc.warlock.core.client.settings.IClientSettings;
 import cc.warlock.core.client.settings.IWindowSettings;
 import cc.warlock.rcp.configuration.GameViewConfiguration;
 import cc.warlock.rcp.ui.client.SWTStreamListener;
 import cc.warlock.rcp.util.ColorUtil;
 import cc.warlock.rcp.util.FontUtil;
 import cc.warlock.rcp.views.GameView;
 
 public class StreamText extends WarlockText implements IStreamListener {
 
 	protected String streamName;
 	protected IStream stream;
 	protected boolean isPrompting = false;
 	protected String prompt = null;
 	protected Property<String> title = new Property<String>();
 	private IStreamListener listener = new SWTStreamListener(this);
 	
 	private WarlockString textBuffer;
 	
 	public StreamText(Composite parent, String streamName) {
 		super(parent);
 		this.streamName = streamName;
 	}
 	
 	public void setFocus() {
 		// Nothing to do
 	}
 	
 	protected synchronized void bufferText (WarlockString string)
 	{
 		if(textBuffer == null) {
 			textBuffer = new WarlockString();
 		}
 
 		textBuffer.append(string);
 	}
 	
 	public Property<String> getTitle() {
 		return title;
 	}
 	
 	public void streamCreated(IStream stream) {
 		this.stream = stream;
 		this.title.set(stream.getFullTitle());
 	}
 	
 	public void componentUpdated(IStream stream, String id, WarlockString value) {
 		replaceMarker(id, value);
 	}
 
 	public void streamCleared(IStream stream) {
 		clearText();
 	}
 
 	public void streamFlush(IStream stream) {
 		flushBuffer();
 	}
 
	private void flushBuffer() {
 		if(textBuffer != null) {
 			append(textBuffer);
 			textBuffer.clear();
 			textBuffer = null;
 		}
 	}
 	
 	private void showPrompt(String prompt) {
 		if(!GameViewConfiguration.instance().getSuppressPrompt()) {
 			WarlockString text = new WarlockString();
 			text.append(prompt);
 			append(text);
 		}
 	}
 	
 	public void streamPrompted(IStream stream, String prompt) {
 		if(!isPrompting)
 		{
 			isPrompting = true;
 			
 			flushBuffer();
 			
 			if(prompt != null)
 				showPrompt(prompt);
 		} else {
 			// if the new prompt is the same as the old one, do nothing.
 			// if the new prompt is null, just print the newline.
 			if(prompt == null) {
 				if(this.prompt != null)
 					append(new WarlockString("\n"));
 			} else if(this.prompt == null || !this.prompt.equals(prompt)) {
 				append(new WarlockString("\n" + prompt));
 			}	
 		}
 		this.prompt = prompt;
 	}
 
 	public void streamReceivedCommand(IStream stream, ICommand command) {
 		IWarlockClient client = stream.getClient();
 		WarlockString string = new WarlockString(command.getText());
 		
 		string.addStyle(client.getCommandStyle());
 		
 		if(!isPrompting && prompt != null)
 			append(new WarlockString(prompt));
 		
 		append(string);
 	}
 
 	public void streamReceivedText(IStream stream, WarlockString text) {
 		WarlockString string = new WarlockString();
 		
 		if (isPrompting) {
 			string.append("\n");
 			isPrompting = false;
 		}
 		
 		string.append(text);
 		
 		if(string.hasStyleNamed("echo") || string.hasStyleNamed("debug"))
 			append(string);
 		else
 			bufferText(string);
 	}
 	
 	public void streamTitleChanged(IStream stream, String title) {
 		this.title.set(stream.getFullTitle());
 	}
 
 	public void setClient(IWarlockClient client) {
 		if(this.client != null) {
 			assert (this.client == client);
 			return;
 		}
 		
 		this.client = client;
 		
 		GameView game = GameView.getGameViewForClient(client);
 		if (game == null) {
 			System.out.println("Couldn't find a gameview for this client! This view won't be setup to send keys over.");
 		} else {
 			this.getTextWidget().addVerifyKeyListener(game.getWarlockEntry().new KeyVerifier());
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().addKeyListener(game.getWarlockEntry().new KeyEventListener());
 		}
 
 		stream = client.getStream(streamName);
 		
 		if(stream != null)
 			this.title.set(stream.getFullTitle());
 		
 		IWarlockSkin skin = client.getSkin();
 		IClientSettings settings = client.getClientSettings();
 		
 		if(skin != null && settings != null) {
 			Color background = ColorUtil.warlockColorToColor(client.getSkin().getMainBackground());
 			Color foreground = ColorUtil.warlockColorToColor(client.getSkin().getMainForeground());
 			WarlockFont font = client.getClientSettings().getMainWindowSettings().getFont();
 			if (!streamName.equals(IWarlockClient.DEFAULT_STREAM_NAME)) {
 				IWindowSettings wsettings = settings.getWindowSettings(streamName);
 				if (wsettings != null) {
 					if (!wsettings.getBackgroundColor().isDefault())
 						background = ColorUtil.warlockColorToColor(wsettings.getBackgroundColor());
 					if (!wsettings.getForegroundColor().isDefault())
 						foreground = ColorUtil.warlockColorToColor(wsettings.getForegroundColor());
 					if (!wsettings.getFont().isDefaultFont())
 						font = wsettings.getFont();
 				}
 			}
 			this.setBackground(background);
 			this.setForeground(foreground);
 
 			String defaultFontFace = GameViewConfiguration.instance().getDefaultFontFace();
 			int defaultFontSize = GameViewConfiguration.instance().getDefaultFontSize();
 
 			if (font.isDefaultFont()) {
 				this.setFont(new Font(Display.getDefault(), defaultFontFace, defaultFontSize, SWT.NORMAL));
 			} else {
 				this.setFont(FontUtil.warlockFontToFont(font));
 			}
 		}
 		
 		if(stream != null) {
 			WarlockString history = stream.getHistory();
 			if(history != null)
 				this.streamReceivedText(stream, stream.getHistory());
 			this.flushBuffer();
 		}
 		
 		client.addStreamListener(streamName, listener);
 	}
 	
 	public void dispose() {
 		if(client != null) {
 			client.removeStreamListener(streamName, listener);
 		}
 	}
 }
