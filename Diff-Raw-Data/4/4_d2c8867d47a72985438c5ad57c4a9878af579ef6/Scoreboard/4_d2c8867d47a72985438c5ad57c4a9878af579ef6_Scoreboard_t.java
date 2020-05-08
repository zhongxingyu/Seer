 package jgossit.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class Scoreboard extends HttpServlet
 {
 	private static final long serialVersionUID = 1L;
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
 		String league = req.getParameter("league");
 		String date = req.getParameter("date");
 		if (league == null || date == null)
 			resp.sendRedirect("scoreboard.html");
 		
 		getScoreboard(league, date, resp.getWriter());
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
 		doPost(req, resp);
 	}
 	
 	
 	private static final String TEAMSCORE_CLASS = "ysptblclbg5";
 	private static final String SCORES_CLASS = "yspscores";
 	private static final String SCOREBOARD_CLASS = "scores";
 	private static final String SCOREBOARD_DIV_ID = "nba:scoreboard";
 	private static final String JQUERY_CODE = 
 	"<link type=\"text/css\" href=\"jquery-ui.css\" rel=\"Stylesheet\"/>\n" + 
 	"<script type=\"text/javascript\" src=\"jquery-1.9.1.js\"></script>\n" + 
 	"<script type=\"text/javascript\" src=\"jquery-ui.js\"></script>\n" + 
 	"<script>\n" + 
 	"	window.onload=function()\n" + 
 	"	{\n" + 
 	"		$(\"tr.ysptblclbg5 td[class='yspscores'], tr.ysptblclbg5 td span[class='yspscores']:even\").click(function () {\n" + 
 	"			$(this).css(\"border\", \"none\").css(\"color\",\"black\");\n" + 
 	"		});\n" + 
 	"\n" + 
 	"		$(\"table.scores + table\").click(function () {\n" + 
 	"			$(this).children().css(\"visibility\",\"visible\");\n" + 
 	"			$(this).css(\"border\", \"none\");\n" + 
 	"		});\n" + 
 	"	};\n" + 
 	"</script>"; 
 	
 	private void getScoreboard(String league, String date, PrintWriter printWriter) throws IOException
 	{
 		String address = "http://sports.yahoo.com/" + league + "/scoreboard?d=" + date;
 		URL url = new URL(address);
 		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
 		urlConnection.setUseCaches(false);
 		urlConnection.setDefaultUseCaches(false);
 		urlConnection.addRequestProperty("Cache-Control", "no-cache,max-age=0");
 		urlConnection.addRequestProperty("Pragma", "no-cache");
 		BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
 		String line = null;
 		boolean inScoreHeader = false;
 		boolean inTeamScore = false;
 		boolean recapTableNext = false;
 		boolean inRecapTable = false;
 		boolean inBody = false;
 		boolean inScoreboard = false;
 		int periods = 0;
 		int[] homeScores = {0,0,0,0};
 		int[] awayScores = {0,0,0,0};
 		boolean awayTeam = true;
 		boolean overtime = false;
 		while ((line = br.readLine()) != null)
 		{
 			if (line.equals("</head>"))
 				printWriter.println(JQUERY_CODE);
 			
 			if (line.contains("class=\"" + TEAMSCORE_CLASS + "\""))
 				inTeamScore = true;
 			
 			if (line.contains("class=\"" + SCOREBOARD_CLASS + "\""))
 				inScoreHeader = true;
 			
 			if(inBody)
 			{
 				if (!line.contains(SCOREBOARD_DIV_ID))
 					continue;
 				else
 				{
 					inBody = false;
 					inScoreboard = true;
 				}
 			}
 			
 			if (inScoreboard && line.startsWith("</div><script")) // end of scoreboard
 			{
 				line = "</div>\n</body>\n</html>";
 				printWriter.println(line);
 				break;
 			}
 			
 			if(inTeamScore)
 			{
 				if (line.contains("</tr>"))
 				{
 					inTeamScore = false;
 					recapTableNext = true;
 				}
 				else
 				{
 					line = line.replaceFirst("img ", "img style='visibility:hidden' "); // hide winner image
 					
 					if (line.contains("td class=\"" + SCORES_CLASS + "\""))
 					{
 						line = line.replaceFirst("class=", "style='border:1px dotted red;color:EEEEDD' class=");
 						if (line.indexOf(">")+1 == line.indexOf("</")) // pad quarter scores
 							line = line.replaceFirst(">",">00");
 						else if (line.indexOf(">")+2 == line.indexOf("</"))
 							line = line.replaceFirst(">",">0");
 						
 						periods++;
 						if (periods < 5)
 						{
							int periodScore = 0;
							if (!line.contains("&nbsp;"))
								periodScore = Integer.parseInt(line.substring(line.indexOf(">")+1, line.indexOf("</")));
 							int cumulativeScore = 0;
 							if (awayTeam)
 							{
 								awayScores[periods-1] = periodScore;
 								for (int i=0;i<periods;i++)
 									cumulativeScore += awayScores[i]; 
 							}
 							else
 							{
 								homeScores[periods-1] = periodScore;
 								for (int i=0;i<periods;i++)
 									cumulativeScore += homeScores[i];
 							}
 							if (periods > 1)
 								line = line.replaceFirst("</","(" + cumulativeScore + ")</");
 						}
 					}
 					else if(!line.contains("<td") && line.contains("span class=\"" + SCORES_CLASS + "\""))
 					{
 						line = line.replaceFirst("class=", "style='border:1px dotted red;color:FFFFCC' class=");
 						
 						if (line.contains("<b>")) // pad winner total score
 						{
 							if (line.indexOf(">")+10 == line.indexOf("</span")) // less than 100
 								line = line.replaceFirst(">",">0");
 							else if (line.indexOf(">")+9 == line.indexOf("</span")) // less than 10?
 								line = line.replaceFirst(">",">00");
 						}
 						else // pad loser total score
 						{
 							if (line.indexOf(">")+3 == line.indexOf("</span")) // less than 100
 								line = line.replaceFirst(">",">0");
 							else if (line.indexOf(">")+2 == line.indexOf("</span")) // less than 10?
 								line = line.replaceFirst(">",">00");
 						}
 						awayTeam = false;
 					}
 					else if (line.contains("<td") && line.contains("span class=\"" + SCORES_CLASS + "\""))
 					{
 						line = line.replaceFirst(">\\d?OT<","><"); // hide OT/2OT/3OT text
 					}
 					else if (periods > 0 && !line.contains("<td")) // add up to 3 dummy OT period scores
 					{
 						for (int i=7;i>periods;i--)
 							line = "<td style='border:1px dotted red;color:EEEEDD' class=\"yspscores\">00</td>\n" + line;
 						if (periods > 4)
 							overtime = true;
 						periods = 0;
 					}
 				}
 			}
 			
 			if (inScoreHeader)
 			{
 				line = line.replaceFirst("width=\"25\"","width=\"40\""); // resize to fit cumulative scores
 						
 				if (line.contains("colspan")) // minor border glitch
 					line = line.substring(0, line.indexOf("colspan")+9) + "21" + line.substring(line.indexOf("colspan")+11);
 						
 				if (line.endsWith(">4</td>"))  // add 3 dummy(?) OT period columns
 				{
 					line += "\n<td rowspan=\"5\" width=\"1\" class=\"yspwhitebg\"></td>\n<td width=\"40\" class=\"yspscores\">OT?</td>";
 					line += "\n<td rowspan=\"5\" width=\"1\" class=\"yspwhitebg\"></td>\n<td width=\"40\" class=\"yspscores\">2OT?</td>";
 					line += "\n<td rowspan=\"5\" width=\"1\" class=\"yspwhitebg\"></td>\n<td width=\"40\" class=\"yspscores\">3OT?</td>";
 					
 					while (br.readLine().contains("<td")) // get to the empty line between quarters/overtimes and total
 						continue;
 					inScoreHeader = false;
 				}
 			}
 			
 			if (inRecapTable && line.contains("</table>")) // finished this game
 			{
 				printWriter.println("</tbody>\n</table>\n</td>\n</tr>\n<tr><td>\n<table>");
 				// additional convenience items
 				int awayAfter3 = awayScores[0]+awayScores[1]+awayScores[2];
 				int homeAfter3 = homeScores[0]+homeScores[1]+homeScores[2];
 				int awayAfter4 = awayAfter3+awayScores[3];
 				int homeAfter4 = homeAfter3+homeScores[3];
 				boolean within8After3 = (awayAfter3 - 8 <= homeAfter3) && (homeAfter3 - 8 <= awayAfter3);
 				boolean within8After4 = (awayAfter4 - 8 <= homeAfter4) && (homeAfter4 - 8 <= awayAfter4);
 				printWriter.println("<tr class=\"ysptblclbg5\" style=\"background-color:F4F5F1\"><td>Within 8 after third quarter?</td><td class=\"yspscores\" style=\"font-family: 'Courier New', monospace;border:1px dotted red;color:F4F5F1\">" + (within8After3 ? "Yes" : "No&nbsp;") + "</td></tr>");
 				printWriter.println("<tr class=\"ysptblclbg5\" style=\"background-color:F4F5F1\"><td>Within 8 after fourth quarter?</td><td class=\"yspscores\" style=\"font-family: 'Courier New', monospace;border:1px dotted red;color:F4F5F1\">" + (within8After4 ? "Yes" : "No&nbsp;") + "</td></tr>");
 				printWriter.println("<tr class=\"ysptblclbg5\" style=\"background-color:F4F5F1\"><td>Overtime?</td><td class=\"yspscores\" style=\"font-family: 'Courier New', monospace;border:1px dotted red;color:F4F5F1\">" + (overtime ? "Yes" : "No&nbsp;") + "</td></tr>");
 				overtime = false;
 				inRecapTable = false;
 				awayTeam = true;
 				homeScores = new int[]{0,0,0,0};
 				awayScores = new int[]{0,0,0,0};
 			}
 			
 			if (recapTableNext)
 			{
 				if (!awayTeam && line.contains("colspan")) // minor border glitch
 						line = line.substring(0, line.indexOf("colspan")+9) + "21" + line.substring(line.indexOf("colspan")+11);
 
 				if (line.contains("<table"))
 				{
 					line = line.replaceFirst("table ", "table style='border:1px dotted red' ");
 					line += "\n<tbody style=\"visibility: hidden\">";
 					inRecapTable = true;
 					recapTableNext = false;
 				}
 			}
 			
 			if (line.startsWith("<body"))
 				inBody = true;
 			
 			printWriter.println(line);
 		}
 		br.close();
 		urlConnection.disconnect();
 	}
 }
