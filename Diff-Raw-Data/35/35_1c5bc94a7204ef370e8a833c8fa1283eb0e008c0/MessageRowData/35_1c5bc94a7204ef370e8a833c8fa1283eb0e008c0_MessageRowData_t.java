 package com.ioabsoftware.gameraven.views.rowdata;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import net.margaritov.preference.colorpicker.ColorPickerPreference;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.graphics.drawable.ShapeDrawable;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.TextPaint;
 import android.text.format.DateUtils;
 import android.text.format.Time;
 import android.text.style.BackgroundColorSpan;
 import android.text.style.CharacterStyle;
 import android.text.style.ClickableSpan;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.StyleSpan;
 import android.text.style.TypefaceSpan;
 import android.text.style.URLSpan;
 import android.text.style.UnderlineSpan;
 import android.text.util.Linkify;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.ioabsoftware.gameraven.AllInOneV2;
 import com.ioabsoftware.gameraven.MessageLinkSpan;
 import com.ioabsoftware.gameraven.R;
 import com.ioabsoftware.gameraven.RichTextUtils;
 import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;
 import com.ioabsoftware.gameraven.networking.Session;
 import com.ioabsoftware.gameraven.views.BaseRowData;
 import com.ioabsoftware.gameraven.views.ClickableLinksTextView;
 import com.ioabsoftware.gameraven.views.GRQuoteSpan;
 import com.ioabsoftware.gameraven.views.RowType;
 
 public class MessageRowData extends BaseRowData {
 
 	private String username, userTitles, postNum, postTime, messageID, boardID, topicID;
 	
 	private Element messageElem, messageElemNoPoll;
 	
 	private LinearLayout poll = null;
 	
 	private Spannable spannedMessage;
 	
 	private int hlColor;
 	
 	private boolean topClickable = true;
 	public void disableTopClick() {topClickable = false;}
 	public boolean topClickable() {return topClickable;}
 	
 	public String getUser() {return username;}
 	public String getUserTitles() {return userTitles;}
 	public boolean hasTitles() {return userTitles != null;}
 	public String getPostNum() {return postNum;}
 	public String getPostTime() {return postTime;}
 	public String getMessageID() {return messageID;}
 	public boolean hasMsgID() {return messageID != null;}
 	public String getTopicID() {return topicID;}
 	public String getBoardID() {return boardID;}
 
 	public Element getMessage() {return messageElem;}
 	public Element getMessageNoPoll() {return messageElemNoPoll;}
 	
 	public LinearLayout getPoll() {
 		if (poll.getParent() != null)
 			((ViewGroup) poll.getParent()).removeView(poll);
 		
 		return poll;
 	}
 	public boolean hasPoll() {return poll != null;}
 	
 	public Spannable getSpannedMessage() {return spannedMessage;}
 	
 	public int getHLColor() {return hlColor;}
 	
 	public String getMessageDetailLink() {
 		return Session.ROOT + "/boards/" + boardID + "/" + topicID + "/" + messageID;
 	}
 	
 	public String getUserDetailLink() {
 		return Session.ROOT + "/users/" + username.replace(' ', '+') + "/boards";
 	}
 	
 	@Override
 	public RowType getRowType() {
 		return RowType.MESSAGE;
 	}
 	
 	public MessageRowData(String userIn, String userTitlesIn, String postNumIn, String postTimeIn,
 			Element messageIn, String BID, String TID, String MID, int hlColorIn) {
 		AllInOneV2 aio = AllInOneV2.get();
 		
 		aio.wtl("setting values");
 		username = userIn;
 		userTitles = userTitlesIn;
 		postNum = postNumIn;
 		postTime = postTimeIn.replace('\u00A0', ' ');
 		messageElem = messageIn;
 		boardID = BID;
 		topicID = TID;
 		messageID = MID;
 		hlColor = hlColorIn;
 
 		aio.wtl("checking for poll");
 		if (messageElem.getElementsByClass("board_poll").isEmpty())
     		messageElemNoPoll = messageElem.clone();
 		else {
 			aio.wtl("there is a poll");
 			
 			messageElemNoPoll = messageElem.clone();
 			messageElemNoPoll.getElementsByClass("board_poll").first().remove();
         	
         	Element pollElem = messageElem.getElementsByClass("board_poll").first();
         	
         	poll = new LinearLayout(AllInOneV2.get());
     		poll.setOrientation(LinearLayout.VERTICAL);
         	
         	LinearLayout pollInnerWrapper = new LinearLayout(AllInOneV2.get());
         	pollInnerWrapper.setPadding(15, 0, 15, 15);
         	pollInnerWrapper.setOrientation(LinearLayout.VERTICAL);
     		
     		ShapeDrawable s = new ShapeDrawable();
     		Paint p = s.getPaint();
     		p.setStyle(Paint.Style.STROKE);
     		p.setStrokeWidth(10);
     		p.setColor(Color.parseColor(ColorPickerPreference.convertToARGB(AllInOneV2.getAccentColor())));
     		
     		poll.setBackgroundDrawable(s);
     		poll.addView(new HeaderView(AllInOneV2.get(), pollElem.getElementsByClass("poll_head").first().text()));
     		poll.addView(pollInnerWrapper);
     		
         	if (pollElem.getElementsByTag("form").isEmpty()) {
         		// poll has been voted in
         		// poll_foot_left
         		TextView t;
         		for (Element e : pollElem.select("div.row")) {
         			Elements c = e.children();
         			t = new TextView(AllInOneV2.get());
         			String text = c.get(0).text() + ": " + c.get(1).text();
         			if (!c.get(0).children().isEmpty()) {
         				SpannableStringBuilder votedFor = new SpannableStringBuilder(text);
         				votedFor.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), 0);
         				votedFor.setSpan(new ForegroundColorSpan(AllInOneV2.getAccentColor()), 0, text.length(), 0);
         				t.setText(votedFor);
         			}
         			else
             			t.setText(text);
         			
         			pollInnerWrapper.addView(t);
         		}
         		
         		String foot = pollElem.getElementsByClass("poll_foot_left").text();
         		if (foot.length() > 0) {
         			t = new TextView(AllInOneV2.get());
         			t.setText(foot);
         			pollInnerWrapper.addView(t);
         		}
         		
         	}
         	else {
         		// poll has not been voted in
             	final String action = "/boards/" + boardID + "/" + topicID;
     			String key = pollElem.getElementsByAttributeValue("name", "key").attr("value");
     			
     			int x = 0;
     			for (Element e : pollElem.getElementsByAttributeValue("name", "poll_vote")) {
     				x++;
     				Button b = new Button(AllInOneV2.get());
     				b.setText(e.nextElementSibling().text());
     				final HashMap<String, String> data = new HashMap<String, String>();
     				data.put("key", key);
     				data.put("poll_vote", Integer.toString(x));
     				data.put("submit", "Vote");
     				b.setOnClickListener(new OnClickListener() {
     					@Override
     					public void onClick(View v) {
     						AllInOneV2.get().getSession().post(NetDesc.TOPIC, action, data);
     					}
     				});
     				pollInnerWrapper.addView(b);
     			}
     			
     			Button b = new Button(AllInOneV2.get());
     			b.setText("View Results");
     			b.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						AllInOneV2.get().getSession().get(NetDesc.TOPIC, action + "?results=1", null);
 					}
 				});
     			pollInnerWrapper.addView(b);
         	}
         }
 
 		aio.wtl("creating ssb");
 		SpannableStringBuilder ssb = new SpannableStringBuilder(processContent(false, true));
 
 		aio.wtl("adding <b> spans");
         addSpan(ssb, "<b>", "</b>", new StyleSpan(Typeface.BOLD));
 		aio.wtl("adding <i> spans");
         addSpan(ssb, "<i>", "</i>", new StyleSpan(Typeface.ITALIC));
 		aio.wtl("adding <code> spans");
         addSpan(ssb, "<code>", "</code>", new TypefaceSpan("monospace"));
 		aio.wtl("adding <cite> spans");
         addSpan(ssb, "<cite>", "</cite>", new UnderlineSpan(), new StyleSpan(Typeface.ITALIC));
 
 		aio.wtl("adding <quote> spans");
         // quotes don't use CharacterStyles, so do it manually
         while (ssb.toString().contains("<blockquote>")) {
         	int start = ssb.toString().indexOf("<blockquote>");
         	ssb.replace(start, start + "<blockquote>".length(), "\n");
         	start++;
         	
         	int stackCount = 1;
         	int closer;
         	int opener;
         	int innerStartPoint = start;
         	do {
         		opener = ssb.toString().indexOf("<blockquote>", innerStartPoint + 1);
         		closer = ssb.toString().indexOf("</blockquote>", innerStartPoint + 1);
         		if (opener != -1 && opener < closer) {
         			// found a nested quote
         			stackCount++;
         			innerStartPoint = opener;
         		}
         		else {
         			// this closer is the right one
         			stackCount--;
         			innerStartPoint = closer;
         		}
         	} while (stackCount > 0);
         	
         	
         	ssb.replace(closer, closer + "</blockquote>".length(), "\n");
         	ssb.setSpan(new GRQuoteSpan(), start, closer, 0);
         }
         
 		aio.wtl("getting text colors for spoilers");
         final int defTextColor;
         final int color;
 		if (AllInOneV2.getUsingLightTheme()) {
 			color = Color.WHITE;
 			defTextColor = Color.BLACK;
 		}
 		else {
 			color = Color.BLACK;
 			defTextColor = Color.WHITE;
 		}
        
        ssb.append('\n');

		aio.wtl("replacing &gameravenlt; with <");
        while (ssb.toString().contains("&gameravenlt;")) {
        	int start = ssb.toString().indexOf("&gameravenlt;");
        	ssb.replace(start, start + "&gameravenlt;".length(), "<");
        }

		aio.wtl("replacing &gameravengt; with >");
        while (ssb.toString().contains("&gameravengt;")) {
        	int start = ssb.toString().indexOf("&gameravengt;");
        	ssb.replace(start, start + "&gameravengt;".length(), ">");
        }

		aio.wtl("linkifying");
        Linkify.addLinks(ssb, Linkify.WEB_URLS);
 
 		aio.wtl("adding <spoiler> spans");
 		// do spoiler tags manually instead of in the method, as the clickablespan needs
 		// to know the start and end points
         while (ssb.toString().contains("<spoiler>")) {
         	final int start = ssb.toString().indexOf("<spoiler>");
         	ssb.delete(start, start + 9);
         	final int end = ssb.toString().indexOf("</spoiler>", start);
         	ssb.delete(end, end + 10);
         	ssb.setSpan(new BackgroundColorSpan(defTextColor), start, end, 0);
         	ssb.setSpan(new ClickableSpan() {
 				@Override
 				public void onClick(View widget) {
 					((Spannable) ((ClickableLinksTextView) widget).getText()).setSpan(new BackgroundColorSpan(color), start, end, 0);
 				}
 				
 				@Override
 				public void updateDrawState(TextPaint ds) {
 					ds.setColor(defTextColor);
 					ds.setUnderlineText(false);
 				}
 			}, start, end, 0);
         	
         }
 
 		aio.wtl("setting spannedMessage");
         spannedMessage = RichTextUtils.replaceAll(ssb, URLSpan.class, new URLSpanConverter());
 	}
 	
 	public boolean isEdited() {
 		if (userTitles != null && userTitles.contains("(edited)"))
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean isEditable() {
 		// Posted 8/1/2013 1:40:38 AM
 		// Posted 8/1/2013 1:02:05 AM
 		// Posted 3/18/2007 11:42:33 PM
 		try {
 			String id = AllInOneV2.getSettingsPref().getString("timezone", TimeZone.getDefault().getID());
 			
 			SimpleDateFormat sdf = new SimpleDateFormat("'Posted 'M/d/yyyy h:mm:ss a", Locale.US);
 			sdf.setTimeZone(TimeZone.getTimeZone(id));
 			Date date = sdf.parse(postTime);
 			
 			Time now  = new Time(id);
 			Time then = new Time(id);
 			
 			then.set(date.getTime());
 			then.normalize(true);
 			
 			now.setToNow();
 			now.normalize(true);
 			
 			if ((now.toMillis(false) - then.toMillis(false)) / ((float) DateUtils.HOUR_IN_MILLIS) < 1f)
 				return true;
 			else
 				return false;
 			
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	private static void addSpan(SpannableStringBuilder ssb, String tag, String endTag, CharacterStyle... cs) {
 		while (ssb.toString().contains(tag)) {
         	int start = ssb.toString().indexOf(tag);
         	ssb.delete(start, start + tag.length());
         	int end = ssb.toString().indexOf(endTag, start);
         	ssb.delete(end, end + endTag.length());
         	for (CharacterStyle c : cs)
         		ssb.setSpan(CharacterStyle.wrap(c), start, end, 0);
         }
 	}
 	
 	public String getMessageForQuoting() {
 		return processContent(true, false);
 	}
 	
 	public String getMessageForEditing() {
 		return processContent(false, false);
 	}
 	
 	private String processContent(boolean removeSig, boolean ignoreLtGt) {
 		String finalBody = messageElemNoPoll.html();
 		
 		finalBody = finalBody.replace("<span class=\"fspoiler\">", "<spoiler>").replace("</span>", "</spoiler>");
 		
 		while (finalBody.contains("<a href")) {
 			int start = finalBody.indexOf("<a href");
 			int end = finalBody.indexOf(">", start) + 1;
 			finalBody = finalBody.replace(finalBody.substring(start, end),
 					"");
 		}
 		
 		finalBody = finalBody.replace("</a>", "");
 		
 		if (finalBody.endsWith("<br />"))
 			finalBody = finalBody.substring(0, finalBody.length() - 6);
 		finalBody = finalBody.replace("\n", "");
 		finalBody = finalBody.replace("<br />", "\n");
 
 		if (removeSig) {
 			int sigStart = finalBody.lastIndexOf("\n---\n");
 			if (sigStart != -1)
 				finalBody = finalBody.substring(0, sigStart);
 		}
 
 		if (ignoreLtGt)
 			finalBody = finalBody.replace("&lt;", "&gameravenlt;").replace("&gt;", "&gameravengt;");
 		
 		finalBody = StringEscapeUtils.unescapeHtml4(finalBody);
 		
 		return finalBody;
 	}
 	
 	class URLSpanConverter implements RichTextUtils.SpanConverter<URLSpan, MessageLinkSpan> {
 		@Override
 		public MessageLinkSpan convert(URLSpan span) {
 			return(new MessageLinkSpan(span.getURL(), AllInOneV2.get()));
 		}
 	}
 	
 	class HeaderView extends LinearLayout {
 
 		public HeaderView(Context context, String text) {
 			super(context);
 			
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	        inflater.inflate(R.layout.headerview, this);
 	        
 	        setBackgroundColor(AllInOneV2.getAccentColor());
 	        
 	        TextView tView = (TextView) findViewById(R.id.hdrText);
 	        tView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tView.getTextSize() * AllInOneV2.getTextScale());
 	        tView.setTextColor(AllInOneV2.getAccentTextColor());
 	        tView.setText(text);
 		}
 		
 	}
 
 }
