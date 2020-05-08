 package utilities;
 
 import java.util.Vector;
 
 import utilities.WikiConstants;
 import parser.WikiContentParser;
 import datatypes.*;
 
 public class Page {
 
 	//Actual content of the page
 	StringBuffer page_title;
 	StringBuffer content_raw,content_processed;
 	StringBuffer timestamp;
	WikiUrl []ref_url_list;
 	Vector <WikiPhrase> bold_and_italic_text;
 	Vector <WikiPhrase> bold_text;
 	Vector <WikiPhrase> italic_text;
 	StringBuffer summary_text;
	Vector<WikiLinks> wikiLinkvctr = null;
 	//!!!!!!!!Note - In case you add a new variable here, reset it in the resetPage() function!!!!!!!!!!!
 
 
 	//Meta data -
 	int flag , page_type;
 	Boolean redirect, raw_text_processed;
 
 	public Page(){
 		flag = 0;
 		redirect = false;
 		page_type = WikiConstants.UNKNOWN_PAGE;
 		resetContentProcessedFlag();
 		ref_url_list = new WikiUrl[0];
 		wikiLinkvctr = new Vector<WikiLinks>();
 	}
 
 	//This is a function which can be used to check if the String content has been processed or not. It will be used 
 	//in the getContent() function to decide whether to process the content again or not
 	public boolean hasContentBeenProcessed(){
 		return raw_text_processed;
 	}
 
 	public void resetContentProcessedFlag(){
 		raw_text_processed = false;
 	}
 	
 	public Boolean isRedirect() {
 		return redirect;
 	}
 
 	public void setRedirect(Boolean redirect) {
 		this.redirect = redirect;
 	}
 
 	public StringBuffer getTimestamp() {
 		return timestamp;
 	}
 
 	public void setTimestamp(StringBuffer timestamp) {
 		this.timestamp = timestamp;
 	}
 	
 	public StringBuffer getTitle() {
 		return page_title;
 	}
 
 	public int getFlag() {
 		return flag;
 	}
 
 	public void setFlag(int flag) {
 		this.flag = flag;
 	}
 
 	public void setTitle(StringBuffer title) {
 		this.page_title = title;
 	}
 
 	public StringBuffer getContent() {
 		if(hasContentBeenProcessed() == false ){
 			WikiContentParser content_parser = new WikiContentParser(content_raw);
 
 			//This is where the parsing function calls go 
 
 			//Extract the Urls from <ref> tags
 			ref_url_list = content_parser.extractRefTagsFromContent( ref_url_list);
 		
 			//Extract all the strings in Bold, Italics , Bold & italics
 			bold_and_italic_text = content_parser.extractBoldAndItalicText(); //Bold & Italics
 			//System.out.println( content_parser.getContentText() );
 			
 			// the order of execution of bold_italic, bold and italic are important
 			bold_text = content_parser.extractBoldText();
 			italic_text = content_parser.extractItalicText();
 			
 						
 			//Extract the links from the document
 
 			//Extract the summary text (This should be called after extracting links)
 			summary_text = content_parser.getSummaryText();
 			//System.out.println("\n\n\nSummary : " + summary_text );
 			
   
 
 			// The return type of the function is WikiLinks.
 			// Creating a vector of objects so that we have the list with respect to each Page.
 			WikiLinks localObj;
 			localObj = content_parser.ExtractOutLinks();
 			wikiLinkvctr.add(localObj);
 			
 			raw_text_processed = true;	//Set the content processed flag to true
 			content_processed = content_raw;//Remove this after the processing has been done.
 		}
 		return content_processed;
 	}
 
 	public StringBuffer getContentRaw(){
 		return content_raw;
 	}
 
 	public void setContent(StringBuffer content) {
 		this.content_raw = content;
 	}
 
 	public WikiUrl[] getRefUrlsList(){
 		return ref_url_list;
 	}
 
 	public WikiUrl getRefUrl(int num){
 		return ref_url_list[num];
 	}
 
 	public int getNumRefUrls(){
 		return ref_url_list.length;
 	}
 
 	// WikiLinks Info Returned
 	public  Vector<WikiLinks> getWikiLinkInfoObj()
 	{
 		return wikiLinkvctr;
 	}
 
 	public void resetPage(){
 	//This function can be used to set all the properties of the Page object to blank. This is useful when the same Page object is used for different Wiki articles, without instantiating new Objs
 		page_title = new StringBuffer();
  		content_raw = new StringBuffer();
 		redirect = false;
 		timestamp=new StringBuffer();
 		flag = 0;
 		resetContentProcessedFlag();
 		ref_url_list = new WikiUrl[0];
 		bold_and_italic_text = null;
 		bold_text = null;
 		italic_text = null;
 		summary_text = new StringBuffer();
 	}
 
 	public int getPageType(){
 		if(page_type != WikiConstants.UNKNOWN_PAGE )
 		 	return page_type;	//Return the page type if it is known already
 
 		//Else parse the title field and get the page type
 		StringBuffer page_title = this.page_title;
 		if(page_title.toString().contains(":") == false){
 			page_type =  WikiConstants.CONTENT_PAGE;	// This is a normal wikipedia article page
 			return WikiConstants.CONTENT_PAGE; 	
 		}
 		
 		//Get the part of the string before : 
 		//The format of the <title> tag is - <title>File:abc.jpg</title>
 
 		String type = page_title.substring(0, page_title.indexOf(":") );
 
 		if(type.equals("Talk"))
 			page_type=WikiConstants.TALK_PAGE;
 		else if( type.equals( "Wikipedia" ))
 			page_type=WikiConstants.WIKI_PAGE;
 		else if( type.equals( "User" ))
 			page_type= WikiConstants.USER_PAGE;
 		else if( type.equals( "User talk" ))
 			page_type= WikiConstants.USER_TALK_PAGE;
 		else if( type.equals( "Category talk" ))
 			page_type= WikiConstants.CATEGORY_TALK_PAGE;
 		else if( type.equals( "Category" ))
 			page_type= WikiConstants.CATEGORY_PAGE;
 		else if( type.equals( "Template" ))
 			page_type= WikiConstants.TEMPLATE_PAGE;
 		else if( type.equals( "File" ))
 			page_type= WikiConstants.FILE_PAGE;
 
 		if(page_type == WikiConstants.UNKNOWN_PAGE)
 			page_type= WikiConstants.CONTENT_PAGE;
 		return page_type;
 	}
 }
 
 
