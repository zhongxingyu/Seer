 package de.kopis.twittercleaner;
 
 import java.text.DateFormat;
 
 import twitter4j.Status;
 
 public class HtmlStatusWriter {
 
 	private final Status status;
 	private boolean source = false;
 
 	public HtmlStatusWriter(final Status status) {
 		this(status, false);
 	}
 	
 	public HtmlStatusWriter(final Status status, final boolean withSource) {
 		this.status = status;
 		source = withSource;
 	}
 
 	public HtmlStatusWriter withSource() {
 		source = true;
 		return this;
 	}
 	
 	public HtmlStatusWriter withoutSource() {
 		source = false;
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		final StringBuilder build = new StringBuilder();
 
 		if (status != null) {
 			// user
 			appendUser(build);
 			
 			// text
 			appendContent(build);
 			
 			// time
 			appendDate(build);
 			
 			// source
 			appendSource(build);
 		}
 
 		return build.toString();
 	}
 
 	private StringBuilder appendSource(final StringBuilder build) {
 		if(source) {
 			build.append(" from ").append(status.getSource());
 		}
 		return build;
 	}
 
 	private StringBuilder appendDate(final StringBuilder build) {
		build.append("<a href=\"http://twitter.com/")
			.append(status.getUser().getScreenName())
			.append("/status/").append(Long.toString(status.getId())).append("\">")
 			.append(" at ").append(DateFormat.getInstance().format(status.getCreatedAt()))
 			.append("</a>");
 		return build;
 	}
 
 	private StringBuilder appendContent(final StringBuilder build) {
 		build.append(" ").append(status.getText()).append("<br />");
 		return build;
 	}
 
 	private StringBuilder appendUser(final StringBuilder build) {
 		build.append("<a href=\"").append(status.getUser().getURL()).append("\">")
 			.append(status.getUser().getName())
 			.append("</a>");
 		return build;
 	}
 }
