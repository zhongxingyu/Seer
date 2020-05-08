 package controllers.sql;
 
 import org.apache.commons.lang3.StringUtils;
 
 import play.Logger;
 
 import com.google.gson.annotations.Expose;
 
 public class EnvironmentChange implements Comparable<EnvironmentChange> {
 
 	@Expose
 	String name;
 	@Expose
 	String text_before;
 	@Expose
 	String text_after;
 	@Expose
 	String build_before;
 	@Expose
 	String build_after;
 
 	public EnvironmentChange(String name, String text_before, String build_before, String text_after, String build_after) {
 		this.name = name;
 		this.text_before = getDifferences(text_before, text_after);
 		this.build_before = getDifferences(build_before, build_after);
 		this.text_after = getDifferences(text_after, text_before);
 		this.build_after = getDifferences(build_after, build_before);
 		
		if ((this.build_after.length() != 0) && (this.text_after.length() == 0)) {
			this.text_after = text_after;
		}
		if ((this.build_before.length() != 0) && (this.text_before.length() == 0)) {
			this.text_before = text_before;
		}
		
 		//Logger.debug("---- "+name);  
 		//Logger.debug("---- "+this.text_before);  
 		//Logger.debug("---- "+this.text_after);  
 		//Logger.debug("---- "+this.build_before);  
 		//Logger.debug("---- "+this.build_after);  
 		
 		
 	}
 	private String getDifferences(String s1, String s2) {
 		String out = "";
 		String t1 = s1;
 		String t2 = s2;
 		
 		int cpt = 500;
 		
 		while (StringUtils.indexOfDifference(t1, t2) >=0 ) {
 			int i = StringUtils.indexOfDifference(t1, t2);
 			//Logger.debug(StringUtils.indexOfDifference(t1, t2)+" '"+t1+"'"+ StringUtils.difference(t1,t2));
 			//Logger.debug(StringUtils.indexOfDifference(t2, t1)+" '"+t2+"'"+ StringUtils.difference(t2,t1));
 			out+=t1.substring(0, i);
 			out+="<strong>"+t1.substring(i,Math.min(i+1, t1.length()))+"</strong>";
 			t1 = t1.substring(Math.min(i+1, t1.length()));
 			t2 = t2.substring(Math.min(i+1, t2.length()));
 			
 			cpt--;
 			if (cpt < 0) {
 				Logger.debug("ERREUR");
 				break;
 			}
 		}
 		out = out.replaceAll("</strong><strong>", "");
 		out = out.replaceAll("<strong></strong>", "");
 		return out;
 	}
 	
 	@Override
 	public int compareTo(EnvironmentChange o) {
 		int ret = name.compareTo(o.name);
 		
 		if ( ret == 0) {
 			ret = text_before.compareTo(o.text_before);
 		}
 
 		if ( ret == 0) {
 			ret = text_after.compareTo(o.text_after);
 		}
 		
 		if ( ret == 0) {
 			ret = build_before.compareTo(o.build_before);
 		}
 
 		if ( ret == 0) {
 			ret = build_after.compareTo(o.build_after);
 		}
 		
 		return ret;
 	}
 }
