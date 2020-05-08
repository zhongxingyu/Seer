 package jdressel.Derporia64;
 // James Dressel and James Robertson
 // Based on the Hello.java example
 // Import Servlet Libraries
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 // Import Java Libraries
 import java.io.*;
 import java.util.*;
 
 
 public class Voting extends HttpServlet {
  
 private final String styleSheet = "";
 
 public void doGet(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
 	res.setContentType ("text/html");
 	PrintWriter out = res.getWriter ();
 	HttpSession session = request.getSession();
 	
 	
 	Object d = getServletContext().getAttribute("jdresselAssertionSet");
 	
 	if(d==null){
 		//TODO
 	} else {
 Set<Assertion> assertions = (Set<Assertion>)d;
 
 out.println("<!DOCTYPE html>");
 out.println("<html><head>");
out.println("<meta http-equiv=\"content-type\" content=\"text/html\"; charset=UTF-8">");
out.println("<meta charset="UTF-8">");
 out.println("<title>Derporia: the never-ending land of claims</title>");
 
 out.println("<!-- A project by James Dressel and James Robertson -->");
 out.println("<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/Derporia64/derporiaVotingStyle.css\" />");
 
 out.println("<script src=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/Derporia64/derporiaVoting.js\" /> </script>");
 out.println("</head>");
 
 out.println("<body>");
 out.println("<h1>Derporia: the never-ending land of <del>baseless</del> claims</h1>");
 
 out.println("<div class=\"username\">");
 out.println("Log In: <input type=\"text\" name=\"username\" placeholder=\"Username\" onkeypress=\"checkEnter(event)\">");
 out.println("<br />");
 out.println("<button type=\"button\" class=\"login\" name=\"loginButton\" align=\"right\">Log In</button>");
 out.println("</div>");
 	
 	
 	
 	
 	
 	
 	for(Iterator<Assertion> assertionIterator = assertions.iterator(); assertionIterator.hasNext();){
 out.println("							<table class=\"mega\">");
 out.println("								<tr>");
 out.println("									<td class=\"mega\">");
 out.println("											<table class=\"center\">");
 out.println("											<tbody>");
 out.println("												<tr>");
 out.println("													<td>");
 out.println("														<p>Post by <a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/username.html\">UserName</a></p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("												<tr>");
 out.println("													<td> <!--TODO make this dynamic change class between colors depending on vote-->");
 out.println("													<p class=\"center\"><b>Claim needs to go here</b></p>");
 out.println("													<p class=\"center\"><i>Assertion needs to go here</i></p>");
 out.println("													<p>Convinced: 0<br>");
 out.println("														Unsure:    0<br>");
 out.println("														Disagree:  0</p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("											</tbody>");
 out.println("											</table>		");
 out.println("											<table class=\"center\">");
 out.println("											<tbody><tr>");
 out.println("												<td>");
 out.println("												<table width=\"100%\">");
 out.println("													<tbody><tr>");
 out.println("													<td align=\"left\">");
 out.println("														<button type=\"button\" class=\"red\" name=\"disagree\">Disagree</button> ");
 out.println("													</td>");
 out.println("													<td align=\"center\">");
 out.println("														<button type=\"button\" class=\"yellow\" name=\"unsure\">Unsure</button> ");
 out.println("													</td>");
 out.println("													<td align=\"right\">");
 out.println("														<button type=\"button\" class=\"green\" name=\"convinced\">Convinced</button> ");
 out.println("													</td>");
 out.println("													</tr>");
 out.println("												</tbody></table>");
 out.println("												</td>");
 out.println("											</tr>");
 out.println("											</tbody>");
 out.println("											</table>");
 out.println("									</td>");
 out.println("									<td class=\"mega\">");
 out.println("											<table class=\"center\">");
 out.println("											<tbody>");
 out.println("												<tr>");
 out.println("													<td>");
 out.println("														<p>Post by <a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/username.html\">UserName</a></p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("												<tr>");
 out.println("													<td> <!--TODO make this dynamic change class between colors depending on vote-->");
 out.println("													<p class=\"center\"><b>Claim needs to go here</b></p>");
 out.println("													<p class=\"center\"><i>Assertion needs to go here</i></p>");
 out.println("													<p>Convinced: 0<br>");
 out.println("														Unsure:    0<br>");
 out.println("														Disagree:  0</p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("											</tbody>");
 out.println("											</table>		");
 out.println("											<table class=\"center\">");
 out.println("											<tbody><tr>");
 out.println("												<td>");
 out.println("												<table width=\"100%\">");
 out.println("													<tbody><tr>");
 out.println("													<td align=\"left\">");
 out.println("														<button type=\"button\" class=\"red\" name=\"disagree\">Disagree</button> ");
 out.println("													</td>");
 out.println("													<td align=\"center\">");
 out.println("														<button type=\"button\" class=\"yellow\" name=\"unsure\">Unsure</button> ");
 out.println("													</td>");
 out.println("													<td align=\"right\">");
 out.println("														<button type=\"button\" class=\"green\" name=\"convinced\">Convinced</button> ");
 out.println("													</td>");
 out.println("													</tr>");
 out.println("												</tbody></table>");
 out.println("												</td>");
 out.println("											</tr>");
 out.println("											</tbody>");
 out.println("											</table>");
 out.println("									</td>");
 out.println("									<td class=\"mega\">");
 out.println("											<table class=\"center\">");
 out.println("											<tbody>");
 out.println("												<tr>");
 out.println("													<td>");
 out.println("														<p>Post by <a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/username.html\">UserName</a></p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("												<tr>");
 out.println("													<td> <!--TODO make this dynamic change class between colors depending on vote-->");
 out.println("													<p class=\"center\"><b>Claim needs to go here</b></p>");
 out.println("													<p class=\"center\"><i>Assertion needs to go here</i></p>");
 out.println("													<p>Convinced: 0<br>");
 out.println("														Unsure:    0<br>");
 out.println("														Disagree:  0</p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("											</tbody>");
 out.println("											</table>		");
 out.println("											<table class=\"center\">");
 out.println("											<tbody><tr>");
 out.println("												<td>");
 out.println("												<table width=\"100%\">");
 out.println("													<tbody><tr>");
 out.println("													<td align=\"left\">");
 out.println("														<button type=\"button\" class=\"red\" name=\"disagree\">Disagree</button> ");
 out.println("													</td>");
 out.println("													<td align=\"center\">");
 out.println("														<button type=\"button\" class=\"yellow\" name=\"unsure\">Unsure</button> ");
 out.println("													</td>");
 out.println("													<td align=\"right\">");
 out.println("														<button type=\"button\" class=\"green\" name=\"convinced\">Convinced</button> ");
 out.println("													</td>");
 out.println("													</tr>");
 out.println("												</tbody></table>");
 out.println("												</td>");
 out.println("											</tr>");
 out.println("											</tbody>");
 out.println("											</table>");
 out.println("									</td>");
 out.println("								</tr>");
 out.println("								<tr>");
 out.println("									<td class=\"mega\">");
 out.println("											<table class=\"center\">");
 out.println("											<tbody>");
 out.println("												<tr>");
 out.println("													<td>");
 out.println("														<p>Post by <a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/username.html\">UserName</a></p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("												<tr>");
 out.println("													<td class=\"red\"> <!--TODO make this dynamic change class between colors depending on vote-->");
 out.println("													<p class=\"center\"><b>Claim needs to go here</b></p>");
 out.println("													<p class=\"center\"><i>Assertion needs to go here</i></p>");
 out.println("													<p>Convinced: 0<br>");
 out.println("														Unsure:    0<br>");
 out.println("														Disagree:  1</p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("											</tbody>");
 out.println("											</table>		");
 out.println("											<table class=\"center\">");
 out.println("											<tbody><tr>");
 out.println("												<td>");
 out.println("												<table width=\"100%\">");
 out.println("													<tbody><tr>");
 out.println("													<td align=\"left\">");
 out.println("														<button type=\"button\" class=\"red\" name=\"disagree\">Disagree</button> ");
 out.println("													</td>");
 out.println("													<td align=\"center\">");
 out.println("														<button type=\"button\" class=\"yellow\" name=\"unsure\">Unsure</button> ");
 out.println("													</td>");
 out.println("													<td align=\"right\">");
 out.println("														<button type=\"button\" class=\"green\" name=\"convinced\">Convinced</button> ");
 out.println("													</td>");
 out.println("													</tr>");
 out.println("												</tbody></table>");
 out.println("												</td>");
 out.println("											</tr>");
 out.println("											</tbody>");
 out.println("											</table>");
 out.println("									</td>");
 out.println("									<td class=\"mega\">");
 out.println("											<table class=\"center\">");
 out.println("											<tbody>");
 out.println("												<tr>");
 out.println("													<td>");
 out.println("														<p>Post by <a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/username.html\">UserName</a></p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("												<tr>");
 out.println("													<td class=\"yellow\"><!--TODO make this dynamic change class between colors depending on vote-->");
 out.println("													<p class=\"center\"><b>Claim needs to go here</b></p>");
 out.println("													<p class=\"center\"><i>Assertion needs to go here</i></p>");
 out.println("													<p>Convinced: 0<br>");
 out.println("														Unsure:    1<br>");
 out.println("														Disagree:  0</p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("											</tbody>");
 out.println("											</table>		");
 out.println("											<table class=\"center\">");
 out.println("											<tbody><tr>");
 out.println("												<td>");
 out.println("												<table width=\"100%\">");
 out.println("													<tbody><tr>");
 out.println("													<td align=\"left\">");
 out.println("														<button type=\"button\" class=\"red\" name=\"disagree\">Disagree</button> ");
 out.println("													</td>");
 out.println("													<td align=\"center\">");
 out.println("														<button type=\"button\" class=\"yellow\" name=\"unsure\">Unsure</button> ");
 out.println("													</td>");
 out.println("													<td align=\"right\">");
 out.println("														<button type=\"button\" class=\"green\" name=\"convinced\">Convinced</button> ");
 out.println("													</td>");
 out.println("													</tr>");
 out.println("												</tbody></table>");
 out.println("												</td>");
 out.println("											</tr>");
 out.println("											</tbody>");
 out.println("											</table>");
 out.println("									</td>");
 out.println("									<td class=\"mega\">");
 out.println("											<table class=\"center\">");
 out.println("											<tbody>");
 out.println("												<tr>");
 out.println("													<td>");
 out.println("														<p>Post by <a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/username.html\">UserName</a></p>");
 out.println("														");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("												<tr>");
 out.println("													<td class=\"green\"><!--TODO make this dynamic change class between colors depending on vote-->");
 out.println("													<p class=\"center\"><b>Claim needs to go here</b></p>");
 out.println("													<p class=\"center\"><i>Assertion needs to go here</i></p>");
 out.println("													<p>Convinced: 1<br>");
 out.println("														Unsure:    0<br>");
 out.println("														Disagree:  0</p>");
 out.println("													</td>");
 out.println("												</tr>");
 out.println("											</tbody>");
 out.println("											</table>		");
 out.println("											<table class=\"center\">");
 out.println("											<tbody><tr>");
 out.println("												<td>");
 out.println("												<table width=\"100%\">");
 out.println("													<tbody><tr>");
 out.println("													<td align=\"left\">");
 out.println("														<button type=\"button\" class=\"red\" name=\"disagree\">Disagree</button> ");
 out.println("													</td>");
 out.println("													<td align=\"center\">");
 out.println("														<button type=\"button\" class=\"yellow\" name=\"unsure\">Unsure</button> ");
 out.println("													</td>");
 out.println("													<td align=\"right\">");
 out.println("														<button type=\"button\" class=\"green\" name=\"convinced\">Convinced</button> ");
 out.println("													</td>");
 out.println("													</tr>");
 out.println("												</tbody></table>");
 out.println("												</td>");
 out.println("											</tr>");
 out.println("											</tbody>");
 out.println("											</table>");
 out.println("									</td>");
 out.println("								</tr>");
 out.println("							</table>");
 		
 		Assertion assertion = assertionIterator.next();
 		out.println(assertion);
 		out.println("<a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.ProcessVote?vote=convinced&id=" + assertion.getId() +" \">Convinced</a>");
 		out.println("<a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.ProcessVote?vote=disagree&id=" + assertion.getId() +" \">disagree</a>");
 		out.println("<a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.ProcessVote?vote=unsure&id=" + assertion.getId() +" \">unsure</a>");
 		out.println("<br>");
 	}
 	}
 
 	
 
 	out.println("<a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/Derporia64/Derporia.jsp\"> home</a>");
         out.close();
     }
 }
