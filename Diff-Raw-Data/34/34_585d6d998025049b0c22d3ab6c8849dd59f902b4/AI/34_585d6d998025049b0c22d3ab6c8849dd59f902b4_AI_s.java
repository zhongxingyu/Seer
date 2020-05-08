 import java.net.*;
 import java.io.*;
 public class AI
 {
 	private String botName = "";
	public AI(String botName)
 	{
		botName = this.botName;
 	}
 
	public String getAIResponse(String message)
 	{
 			String toAI = message.split(botName+": ")[1];
			return ai(toAI);
 	}
	public String ai(String message)
 	{
 		String result =  "";
 		try{ 
 			URL url = new URL("http://kato.botdom.com");
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setDoOutput(true);
 			conn.setRequestMethod("POST");
 			conn.setInstanceFollowRedirects(true);	
 			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 			String cleanMessage = message.replace("?", "%3F");
 			wr.write("m="+cleanMessage);
 			wr.flush();
 			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;	
 			while ((line = rd.readLine()) != null) 
 			{
 				result += line + "\r\n";
 			}
 			rd.close();
 			wr.close();
 		}
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage());
 		}
 		try{
 		String splitResult = result.split("<h2>")[1];
 		String aiMessage = splitResult.split("</h2>")[0];
		aiMessage = aiMessage.replace("&#8217;", "'").replace("&#8221;", "\"").replace("&#8220;", "\"").replace("#&8212;", "-").replace("&lt;br/&gt;", "\n");
 		return aiMessage;
 		}
 	catch(Exception e){System.out.println(e.getMessage());return "Message not found";}
 
 	}
 
 }
