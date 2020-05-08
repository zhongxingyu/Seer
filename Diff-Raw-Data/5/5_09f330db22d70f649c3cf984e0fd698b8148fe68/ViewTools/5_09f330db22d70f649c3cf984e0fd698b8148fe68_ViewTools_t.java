 package fr.cpcgifts.utils;
 
 import fr.cpcgifts.model.Comment;
 import fr.cpcgifts.model.CpcUser;
 import fr.cpcgifts.model.Giveaway;
 import fr.cpcgifts.persistance.CpcUserPersistance;
 
 public class ViewTools {
 
 	public static String userView(CpcUser u) {
 
 		String res = "<div class=\"row\">"
 				+ "<div class=\"span1\"><a href=\"/user?userID="
 				+ u.getKey().getId()
 				+ "\"><img class=\"img-rounded img-small-avatar\" src=\""
 				+ u.getAvatarUrl() + "\" /></a></div>"
 				+ "<div class=\"span3\"><a  href=\"/user?userID="
 				+ u.getKey().getId() + "\">" + u.getCpcNickname()
 				+ "</a></div>" + "</div>";
 
 		return res;
 	}
 
 	public static String gaView(Giveaway ga) {
 
 		CpcUser auth = CpcUserPersistance.getCpcUserByKey(ga.getAuthor());
 
		String res = "<div class=\"row-fluid\">" + "<div class=\"span2\">"
 				+ "<a href=\"/giveaway?gaID="
 				+ ga.getKey().getId()
 				+ "\" ><img class=\"img-rounded img-small-ga\" src=\""
 				+ ga.getImgUrl()
 				+ "\"></a>"
 				+ "</div>"
				+ "<div class=\"span8 offset1\">"
 				+ "<div class=\"row\">"
 				+ "<h2 class='span8'><a href=\"/giveaway?gaID="
 				+ ga.getKey().getId()
 				+ "\" >"
 				+ ga.getTitle()
 				+ "</a></h2>"
 				+ "<div class=\"offset7\">"
 				+ "<div class=\"media\">"
 				+ "<a class=\"pull-left\""
 				+ "href=\"/user?userID="
 				+ auth.getKey().getId()
 				+ "\"> <img "
 				+ "class=\"media-object img-small-avatar\""
 				+ "src=\" "
 				+ auth.getAvatarUrl()
 				+ "\">"
 				+ "</a>"
 				+ "<div class=\"media-body\">"
 				+ "<h4 class=\"media-heading\">"
 				+ "<a href=\"/user?userID="
 				+ auth.getKey().getId()
 				+ "\">"
 				+ auth.getCpcNickname()
 				+ "</a>"
 				+ "</h4>"
 				+ "</div>"
 				+ "</div>" + "</div></div>";
 
 		res += "<div class=\"row\">";
 		
 		res += "<img class=\"img-small-icon\" src=\"img/clock.png\" /> ";
 		
 		if(ga.isOpen()) {
 			res += " Ouvert encore ";
 		} else {
 			res += " Fermé depuis ";
 		}
 		
 		res += DateTools.dateDifference(ga.getEndDate());
 
 		res += "</div>" + "</div>" + "</div>";
 
 		return res;
 	}
 
 	public static String gaCarouselView(Giveaway ga, boolean active) {
 
 		String res = "<div class=\"item";
 
 		if (active) {
 			res += " active";
 		}
 
 		res += "\">";
 
 		res += "<a href=\"/giveaway?gaID=" + ga.getKey().getId() + "\" >"
 				+ "<img class=\"img-steam-game\" src=\"" + ga.getImgUrl()
 				+ "\" alt=\"\">" + "</a>" + "<div class=\"carousel-caption\">"
 				+ "<h4>" + ga.getTitle() + "</h4>" + "<p>"
 				+ ga.getDescription() + "</p>" + "</div>" + "</div>";
 
 		return res;
 	}
 
 	public static String commentView(Comment c) {
 		CpcUser u = CpcUserPersistance.getCpcUserByKey(c.getAuthor());
 
 		String res = "<div class='media'>";
 		res += "<a class='pull-left' href='/user?userID=" + u.getKey().getId()
 				+ "'>" + "<img class='media-object img-small-avatar' src='"
 				+ u.getAvatarUrl() + "'>" + "</a>";
 		res += "<div class='media-body'>";
 		res += "<h6 class='media-heading'>" + "<a href='/user?userID="
 				+ u.getKey().getId() + "'>" + u.getCpcNickname() + "</a>"
 				+ "</h6>";
 		res += c.getCommentText();
 		res += "</div>"; // /media-body
 		res += "</div>"; // /media
 		return res;
 	}
 
 }
