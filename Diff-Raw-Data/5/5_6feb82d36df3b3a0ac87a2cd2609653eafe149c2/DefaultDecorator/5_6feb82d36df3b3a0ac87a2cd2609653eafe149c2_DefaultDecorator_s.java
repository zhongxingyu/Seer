 package org.neuro4j.web.console.controller.vd;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.neuro4j.core.Entity;
 import org.neuro4j.core.Relation;
 
 
 public class DefaultDecorator implements ViewDecorator {
 
 	public String render(Entity displayedEntity, String groupName, List<Relation> relations, HttpServletRequest request) {
 		StringBuffer sb = new StringBuffer();
 
 		
 		for (Relation r : relations)
 		{
    		sb.append("<b><a href='/n4j-nms/relation-details?storage=" + request.getParameter("storage") + "&vt=graph&uuid=" + r.getUuid() +"'>" + r.getName() + "</a></b><br/>");
     		sb.append("<br/>");
 		    for (Entity rp : r.getParticipants()) {
	    		sb.append("<a href='/n4j-nms/entity-details?storage=" + request.getParameter("storage") + "&vt=graph&eid=" + rp.getUuid() +"'>" + rp.getName() + "</a><br/>");
 	    		sb.append("");
 	    		sb.append("");
 	    		sb.append("");
 		    }
 
     		sb.append("<br/>");
 		}			
 		
 		
 		return sb.toString();
 	}
 
 }
