 package au.gov.nsw.records.search.service;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class StringService {
 
 	public static boolean isNumeric(String str)
 	{
 		if (str==null || str.isEmpty()){
 			return false;
 		}
 		Pattern p = Pattern.compile( "([0-9]*)+" );
 		Matcher m = p.matcher(str);
 		return m.matches();
 	}
 	
 	public static String formatEacCpf(String field){
 		
 		field = field.replaceAll("\n", "</p><p>");
    field = field.replaceAll("<br />", "</p><p>");
    field = field.replaceAll("<br/>", "</p><p>");
     field = field.replaceAll("nbsp;", " ");
     field = field.replaceAll("&middot;", "-");
     field = field.replaceAll("&ndash;", "-");
     field = field.replaceAll("&emdash;", "-");
     field = field.replaceAll("&quot;", "\"");
     field = field.replaceAll("&hellip;", "...");
    field = field.replaceAll("&lsquo;", "'");
    field = field.replaceAll("&ldquo;", "'");
    field = field.replaceAll("&rdquo;", "'");
     field = field.replaceAll("&rsquo;", "'");
     field = field.replaceAll("&ldquo;", "'");
     field = field.replaceAll("/<p>\\s*<\\/p>/", "");
     //field = field.replaceAll("/<\\/?(?(em|[\\/pbi])).*?>/", ""); // # ... HErE bE DRAgONs [should strip any html except - p, em, b, i tags. Maybe requiring nokogiri/loofah would be saner]
     field = field.replaceAll("<i>", "<span style=\"font-style:italic\">");
     field = field.replaceAll("</i>", "</span>");
     field = field.replaceAll("<em>", "<span style=\"font-style:italic\">");
     field = field.replaceAll("</em>", "</span>");
     field = field.replaceAll("<b>", "<span style=\"font-weight:bold\">");
     field = field.replaceAll("</b>", "</span>");
     field = field.replaceAll("<p></p>", "");
     field = field.replaceAll("& ", "&amp; ");
 		return "<p>" + field + "</p>";
 	}
 }
