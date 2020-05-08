 package cc.warlock.rcp.views;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 import cc.warlock.client.IProperty;
 import cc.warlock.client.IStream;
 import cc.warlock.client.IStreamListener;
 import cc.warlock.client.IStyledString;
 import cc.warlock.client.IWarlockStyle;
 import cc.warlock.client.PropertyListener;
 import cc.warlock.client.stormfront.IStormFrontClient;
 import cc.warlock.client.stormfront.WarlockColor;
 import cc.warlock.configuration.server.HighlightString;
 import cc.warlock.configuration.server.ServerSettings;
 import cc.warlock.configuration.skin.IWarlockSkin;
 import cc.warlock.rcp.ui.StyleRangeWithData;
 import cc.warlock.rcp.ui.WarlockText;
 import cc.warlock.rcp.ui.client.SWTPropertyListener;
 import cc.warlock.rcp.ui.client.SWTStreamListener;
 import cc.warlock.rcp.ui.style.StyleMappings;
 
 public class StreamView extends ViewPart implements IStreamListener {
 	
 	public static final String STREAM_VIEW_PREFIX = "cc.warlock.rcp.views.stream.";
 	public static final String DEATH_VIEW_ID =  STREAM_VIEW_PREFIX + IStormFrontClient.DEATH_STREAM_NAME;
 	public static final String INVENTORY_VIEW_ID = STREAM_VIEW_PREFIX  + IStormFrontClient.INVENTORY_STREAM_NAME;
 	public static final String THOUGHTS_VIEW_ID = STREAM_VIEW_PREFIX + IStormFrontClient.THOUGHTS_STREAM_NAME ;
 	
 	protected static ArrayList<StreamView> openViews = new ArrayList<StreamView>();
 	
 	protected IStream mainStream;
 	protected ArrayList<IStream> streams;
 	protected IStormFrontClient client;
 	protected WarlockText text;
 	protected Composite mainComposite;
 	// This name is the 'suffix' part of the stream... so we will install listeners for each client
 	protected String mainStreamName;
 	protected SWTStreamListener streamListenerWrapper;
 	protected SWTPropertyListener<String> propertyListenerWrapper;
 	protected boolean appendNewlines = false;
 	protected boolean isPrompting = false;
 	
 	public StreamView() {
 		openViews.add(this);
 		streamListenerWrapper = new SWTStreamListener(this);
 		streams = new ArrayList<IStream>();
 	}
 
 	public static StreamView getViewForStream (String streamName) {
 		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		
 		for (StreamView view : openViews)
 		{
 			if (view.getStreamName().equals(streamName))
 			{
 				page.activate(view);
 				return view;
 			}
 		}
 		
 		// none of the already created views match, create a new one
 		try {
 			StreamView nextInstance = (StreamView) page.showView(STREAM_VIEW_PREFIX + streamName);
 			nextInstance.setStreamName(streamName);
 			
 			return nextInstance;
 		} catch (PartInitException e) {
 			e.printStackTrace();
 		}	
 		return null;
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		mainComposite = new Composite (parent, SWT.NONE);
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		mainComposite.setLayout(layout);
 		
 		text = new WarlockText(mainComposite, SWT.V_SCROLL);
 		text.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		text.setEditable(false);
 		text.setWordWrap(true);
 		text.setBackground(new Color(text.getDisplay(), 25, 25, 50));
 		text.setForeground(new Color(text.getDisplay(), 240, 240, 255));
 		text.setScrollDirection(SWT.DOWN);
 		
 		GameView currentGameView = GameView.getViewInFocus();
 		if (currentGameView != null && currentGameView.getStormFrontClient() != null)
 		{
 			setPartName(currentGameView.getStormFrontClient().getStream(mainStreamName).getTitle().get());
 		}
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public IStream getMainStream() {
 		return mainStream;
 	}
 
 	public void setMainStream(IStream stream) {
 		this.mainStream = stream;
 		
 		stream.addStreamListener(streamListenerWrapper);
 		propertyListenerWrapper = new SWTPropertyListener<String>(new PropertyListener<String>() {
 			@Override
 			public void propertyChanged(IProperty<String> property, String oldValue) {
 				if (property.getName().equals("streamTitle"))
 				{
 					setPartName(property.get());
 				}
 			}
 		});
 		stream.getTitle().addListener(propertyListenerWrapper);
 	}
 	
 	public void addStream (IStream stream) {
 		stream.addStreamListener(streamListenerWrapper);
 		streams.add(stream);
 	}
 	
 	public void streamCleared(IStream stream) {
 		if (this.mainStream.equals(stream))
 			text.setText("");
 	}
 	
 	private void scrollToBottom ()
 	{
 		int length = this.text.getContent().getCharCount();		
 		if (this.text.getCaretOffset() < length) {
 			this.text.setCaretOffset(length);
 			this.text.showSelection();
 		}
 	}
 	
 	private void applyUserHighlights (StyleRange parentStyle, String text, int start, int lineIndex)
 	{
 		if (client.getServerSettings().getHighlightStrings() == null)
 			return;
 		
 		Font font = this.text.getFont();
 		if (parentStyle != null)
 		{
 			if (parentStyle.font != null)
 				font = parentStyle.font;
 		}
 		
 		for (HighlightString highlight : client.getServerSettings().getHighlightStrings())
 		{
 			int highlightLength = highlight.isFillEntireLine() ? text.length() : highlight.getText().length();
 			int index = text.indexOf(highlight.getText());
 			while (index > -1)
 			{
 				StyleRangeWithData range = new StyleRangeWithData();
 				range.background = createColor(highlight.getBackgroundColor());
 				range.foreground = createColor(highlight.getForegroundColor());
 				range.start = highlight.isFillEntireLine() ? start : start + index;
 				range.length = highlightLength;
 				range.font = font;
 				
 				if (highlight.isFillEntireLine())
 				{
 					this.text.setLineBackground(lineIndex, range.background);
 					this.text.setLineForeground(lineIndex, range.foreground);
 				}
 				
 				this.text.setStyleRange(range);
 				
 				if (highlight.isFillEntireLine())
 					break;
 				
 				index = text.indexOf(highlight.getText(), index+1);
 			}
 		}
 	}
 	
 	public void streamReceivedText(IStream stream, IStyledString string) {
 		if (this.mainStream.equals(stream) || this.streams.contains(stream))
 		{
 			if (isPrompting) {
 				this.text.append("\n");
 				isPrompting = false;
 			}
 			
 			String streamText = string.getBuffer().toString();
 			
 			if (appendNewlines)
 				streamText += "\n";
 			
 			this.text.append(streamText);
 			
 			int charCount = this.text.getCharCount() - streamText.length();
 			StyleRangeWithData ranges[] = new StyleRangeWithData[string.getStyles().size()];
 			int i = 0;
 			for (IWarlockStyle style : string.getStyles())
 			{
 				ranges[i] = StyleMappings.getStyle(client.getServerSettings(), style, charCount + style.getStart(), style.getLength());
 				if (style.getStyleTypes().contains(IWarlockStyle.StyleType.LINK))
 				{
 					ranges[i].data.put("link.url", style.getLinkAddress().toString());
 					// skip any leading spaces
 					int j = 0;
 					while (string.getBuffer().charAt(j) == ' ') j++;
 					ranges[i].start = ranges[i].start + j;
 				}
 				i++;
 			}
 			
 			boolean userHighlightsApplied = false;
 			
 			for (StyleRangeWithData range : ranges)
 			{
 				if (range != null) {
 					int lineIndex = this.text.getLineAtOffset(range.start);
 					this.text.setStyleRange(range);
 					
 					applyUserHighlights(range, streamText, range.start, lineIndex);
 					userHighlightsApplied = true;
 				}
 			}
 			
 			if (!userHighlightsApplied) {
 				applyUserHighlights(null, streamText, charCount, this.text.getLineAtOffset(charCount));
 			}
 		}
 	}
 		
 	public void streamEchoed(IStream stream, String text) {
 		if (this.mainStream.equals(stream) || this.streams.contains(stream))
 		{
 			isPrompting = false;
 			
 			this.text.append(text + "\n");
 			
 			StyleRange echoStyle = StyleMappings.getEchoStyle(client.getServerSettings(), this.text.getCharCount() - text.length() - 1, text.length());
 			this.text.setStyleRange(echoStyle);
 		}
 	}
 	
 	public void streamPrompted(IStream stream, String prompt) {
		if (!isPrompting && (this.mainStream.equals(stream) || this.streams.contains(stream)))
 		{
 			isPrompting = true;
 			this.text.append(prompt);
 		}
 	}
 	
 	public void streamDonePrompting (IStream stream) {
 		isPrompting = false;
 	}
 
 	public static Collection<StreamView> getOpenViews ()
 	{
 		return openViews;
 	}
 	
 	private Color createColor (WarlockColor color)
 	{
 		return new Color(getSite().getShell().getDisplay(), color.getRed(), color.getGreen(), color.getBlue());
 	}
 	
 	public void setClient (IStormFrontClient client)
 	{
 		this.client = client;
 		
 		setMainStream(client.getStream(mainStreamName));
 	}
 	
 	public void loadServerSettings (ServerSettings settings)
 	{
 		// just inherit from the main window for now
 		WarlockColor bg = settings.getColorSetting(IWarlockSkin.ColorType.MainWindow_Background);
 		WarlockColor fg = settings.getColorSetting(IWarlockSkin.ColorType.MainWindow_Foreground);
 		String fontFace = settings.getFontFaceSetting(IWarlockSkin.FontFaceType.MainWindow_FontFace);
 		int fontSize = settings.getFontSizeSetting(IWarlockSkin.FontSizeType.MainWindow_FontSize);
 		
 		text.setBackground(createColor(bg));
 		text.setForeground(createColor(fg));
 		
 		Font normalFont = new Font(getSite().getShell().getDisplay(), fontFace, fontSize, SWT.NONE);
 		text.setFont(normalFont);
 	}
 	
 	@Override
 	public void dispose() {
 		if (mainStream != null) {
 			mainStream.removeStreamListener(streamListenerWrapper);
 			mainStream.getTitle().removeListener(propertyListenerWrapper);
 		}
 		
 		for (IStream stream : streams)
 		{
 			stream.removeStreamListener(streamListenerWrapper);
 		}
 		
 		super.dispose();
 	}
 
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (IStormFrontClient.class.equals(adapter))
 		{
 			return client;
 		}
 		return super.getAdapter(adapter);
 	}
 	
 	public String getStreamName() {
 		return mainStreamName;
 	}
 
 	public void setStreamName(String streamName) {
 		this.mainStreamName = streamName;
 	}
 
 	public void setAppendNewlines(boolean appendNewlines) {
 		this.appendNewlines = appendNewlines;
 	}
 	
 	public void setViewTitle (String title)
 	{
 		setPartName(title);
 	}
 }
