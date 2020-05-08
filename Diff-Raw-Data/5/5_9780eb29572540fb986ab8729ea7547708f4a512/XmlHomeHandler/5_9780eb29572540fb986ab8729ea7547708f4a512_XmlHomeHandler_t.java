 package tice.twitterwalk.XmlParse;
 
 import java.util.Date;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.text.Html;
 
 import tice.twitterwalk.Util.TweetsData;
 import tice.twitterwalk.Util.TwitterItem;
 
 public class XmlHomeHandler extends DefaultHandler {
 
 	private int mType;
     private boolean in_status = false; 
     private boolean in_user = false; 
     private boolean in_retweet_details = false;
     private boolean in_error = false;
     
     private StringBuilder builder;
     private TwitterItem mItem = null;
 	private TweetsData mTweetsData = null;
     
 	public TweetsData GetParsedData() { 
 		return mTweetsData; 
 	} 
     
 	public XmlHomeHandler(int type){
 		mType = type;
 	}
 	
     @Override 
     public void startDocument() throws SAXException { 
          mTweetsData = new TweetsData(); 
     } 
 
     @Override 
     public void endDocument() throws SAXException { 
          // Nothing to do 
     } 
     
     @Override 
     public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException { 
     	if (localName.equals("status")){
         	in_status = true;
         	mItem = new TwitterItem();
         	mItem.mType = mType;
         	builder = new StringBuilder();
         }else if (localName.equals("user")){
         	in_user = true;
        }else if (localName.equals("retweeted_status")){
         	in_retweet_details = true;
         }else if (localName.equals("error")){
         	in_error = true;
         	builder = new StringBuilder();
         }
     } 
      
     @Override 
     public void endElement(String namespaceURI, String localName, String qName) throws SAXException { 
         
     	if (localName.equals("status")){
         	in_status = false;
         	mTweetsData.items.add(mItem);
         } else if (localName.equals("user")){
         	in_user = false;
        } else if (localName.equals("retweeted_status")){
         	in_retweet_details = false;
         }else if (localName.equals("error")){
         	in_error = false;
         	mTweetsData.mError = builder.toString();
         	builder.setLength(0);
         }
     	
     	if(in_status && !in_retweet_details){
 
     		String body = builder.toString().trim();
 
     		if (localName.equals("created_at") && in_user == false){
 	        	mItem.mTime = Date.parse(body);
 	        } else if (localName.equals("id") && in_user == false){
 	        	mItem.mID = Long.valueOf(body);
 	        } else if (localName.equals("text") && in_user == false){
 	        	mItem.mText = String.format("%s", Html.fromHtml(body));
 	        } else if (localName.equals("source") && in_user == false){
 	        	mItem.mSource = String.format("%s",Html.fromHtml(body));
 	        } else if (localName.equals("in_reply_to_status_id") && in_user == false){
 	        	mItem.mReplyID = body;
 	        } else if (localName.equals("favorited") && in_user == false){
 	        	mItem.mFavorite = Boolean.parseBoolean(body);
 	        } else if (localName.equals("screen_name") && in_user == true){
 	        	mItem.mScreenname = body;
 	        } else if (localName.equals("name") && (in_user == true)){
 	        	mItem.mTitle = body;
 	        } else if (localName.equals("profile_image_url") && in_user == true ){
 	        	mItem.mImageurl = body;
 	        } else if (localName.equals("bmiddle_pic") && in_user == false ){
 	        	mItem.mPicurl = body;
 	        }
 	    
 	    	builder.setLength(0);
 	    }
     } 
      
     @Override 
 	public void characters(char ch[], int start, int length) { 
     	if (in_status && !in_retweet_details || in_error == true){
     		if(ch[start] == '\r' || ch[start] == '\n') return;
     		builder.append(ch, start, length);
     	}
     } 
 }
