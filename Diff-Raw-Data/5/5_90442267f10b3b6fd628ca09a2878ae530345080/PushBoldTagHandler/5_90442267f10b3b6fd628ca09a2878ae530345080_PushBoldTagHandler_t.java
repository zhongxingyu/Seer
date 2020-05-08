 package cc.warlock.core.stormfront.internal;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import cc.warlock.core.client.IStyledString;
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.internal.WarlockStyle;
 import cc.warlock.core.stormfront.IStormFrontProtocolHandler;
 
 
 public class PushBoldTagHandler extends DefaultTagHandler {
 
 	protected IWarlockStyle style;
 	protected static final Pattern linkPattern = Pattern.compile("(www\\.|http://).+");
 	
 	public PushBoldTagHandler (IStormFrontProtocolHandler handler)
 	{
 		super(handler);
 	}
 	
 	@Override
 	public String[] getTagNames() {
 		return new String[] { "pushBold", "popBold" };
 	}
 	
 	public void handleEnd() {
 		if (getCurrentTag().equals("pushBold"))
 		{
 			handler.pushBuffer();
 			style = WarlockStyle.createBoldStyle(0, -1);
 		}
 		
 		else if (getCurrentTag().equals("popBold"))
 		{
			if(style == null)
				return;
			
 			IStyledString buffer = handler.peekBuffer();
 			
 			style.setLength(buffer.getBuffer().length());
 			buffer.addStyle(style);
 			
 			String matchText = buffer.getBuffer().toString();
 			
 			Matcher linkMatcher = linkPattern.matcher(matchText.trim());
 			if (linkMatcher.matches())
 			{
 				style.addStyleType(IWarlockStyle.StyleType.LINK);
 				String address = linkMatcher.group();
 				if (address.startsWith("www")) { address = "http://" + address; }
 				
 				try {
 					style.setLinkAddress(new URL(address));
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			handler.sendAndPopBuffer();
			style = null;
 		}
 	}
 }
