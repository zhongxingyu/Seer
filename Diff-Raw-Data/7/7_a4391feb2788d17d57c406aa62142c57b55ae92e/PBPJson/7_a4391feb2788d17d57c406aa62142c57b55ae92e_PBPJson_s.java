 package jsonObjects;
 
 import java.io.BufferedReader;
 import java.util.ArrayList;
 import java.util.Comparator;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonNull;
 
 public class PBPJson extends NBAJsonObject
 {
 	private String gameID, actualTime, gameTime,
 					homeDesc, neutralDesc, awayDesc,
 					score, margin;
 	private int eventNum, eventMsgType, eventMsgActionType,
 				period;
 	
 	public PBPJson()
 	{
 		this.gameID = "";
 		this.actualTime = "";
 		this.gameTime = "";
 		this.homeDesc = "";
 		this.neutralDesc = "";
 		this.awayDesc = "";
 		this.score = "";
 		this.margin = "";
 		this.eventNum = 0;
 		this.eventMsgType = 0;
 		this.eventMsgActionType = 0;
 		this.period = 0;
 	}
 	
 	public PBPJson(String gameID, String actualTime, String gameTime, 
 					String homeDesc, String neutralDesc, String awayDesc,
 					String score, String margin, int eventNum, 
 					int eventMsgType, int eventMsgActionType, int period)
 	{
 		this.gameID = gameID;
 		this.actualTime = actualTime;
 		this.gameTime = gameTime;
 		this.homeDesc = homeDesc;
 		this.neutralDesc = neutralDesc;
 		this.awayDesc = awayDesc;
 		this.score = score;
 		this.margin = margin;
 		this.eventNum = eventNum;
 		this.eventMsgType = eventMsgType;
 		this.eventMsgActionType = eventMsgActionType;
 		this.period = period;
 	}
 	
 	public String getGameID() { return gameID; }
 	public String getActualTime() { return actualTime; }
 	public String getGameTime() { return gameTime; }
 	public String getHomeDesc() { return homeDesc; }
 	public String getNeutralDesc() { return neutralDesc; }
 	public String getAwayDesc() { return awayDesc; }
 	public String getScore() { return score; }
 	public String getMargin() { return margin; }
 	public int getEventNum() { return eventNum; }
 	public int getEventMsgType() { return eventMsgType; }
 	public int getEventMsgActionType() { return eventMsgActionType; }
 	public int getPeriod() { return period; }
 	
 	public void setEventNum(int playID)
 	{
 		this.eventNum = playID;
 	}
 	
 	public static ArrayList<PBPJson> parsePBPJson(String json)
 	{
 		Gson gson = new Gson();
 		ArrayList<PBPJson> pbp = new ArrayList<PBPJson>();
 		String gameID, actualTime, gameTime,
 			homeDesc, neutralDesc, awayDesc,
 			score, margin;
 		int eventNum, eventMsgType, eventMsgActionType,
 			period;
 		
 		JsonArray array = preProcessJson(json);
 		
 		for(JsonElement element : array)
 		{
 			JsonArray pbpArray = element.getAsJsonArray();
 			gameID = gson.fromJson(pbpArray.get(0), String.class);
 			eventNum = gson.fromJson(pbpArray.get(1), int.class);
 			eventMsgType = gson.fromJson(pbpArray.get(2), int.class);
 			eventMsgActionType = gson.fromJson(pbpArray.get(3), int.class);
 			period = gson.fromJson(pbpArray.get(4), int.class);
 			actualTime = gson.fromJson(pbpArray.get(5), String.class);
 			gameTime = gson.fromJson(pbpArray.get(6), String.class);
 			
 			if (pbpArray.get(7) != JsonNull.INSTANCE)
 				homeDesc = gson.fromJson(pbpArray.get(7), String.class);
 			else
 				homeDesc = "";
 			
 			if (pbpArray.get(8) != JsonNull.INSTANCE)
 				neutralDesc = gson.fromJson(pbpArray.get(8), String.class);
 			else
 				neutralDesc = "";
 			
 			if (pbpArray.get(9) != JsonNull.INSTANCE)
 				awayDesc = gson.fromJson(pbpArray.get(9), String.class);
 			else
 				awayDesc = "";
 			
 			if (pbpArray.get(10) != JsonNull.INSTANCE)
 				score = gson.fromJson(pbpArray.get(10), String.class);
 			else
 				score = "";
 			
 			if (pbpArray.get(11) != JsonNull.INSTANCE)
 				margin = gson.fromJson(pbpArray.get(11), String.class);
 			else
 				margin = "";
 			
 			pbp.add(new PBPJson(gameID, actualTime, gameTime, homeDesc, 
 					neutralDesc, awayDesc, score, margin, eventNum, 
 					eventMsgType, eventMsgActionType, period));
 		}
 		
 		return pbp;
 	}
 	
 	public static ArrayList<PBPJson> getPBP(BufferedReader reader)
 	{
 		String json = readJson(reader);
 		return parsePBPJson(json);
 	}
 	
 	@Override
     public String toString() 
 	{
 		return String.format("(GAME_ID=%s, EVENTNUM=%s, EVENTMSGTYPE=%s, "+
 				"EVENTMSGACTIONTYPE=%s, PERIOD=%s, WCTIMESTRING=%s, " +
 				"PCTIMESTRING=%s, HOMEDESCRIPTION=%s, NEUTRALDESCRIPTION=%s, " +
 				"VISITORDESCRIPTION=%s, SCORE=%s, SCOREMARGIN=%s)",
 				this.gameID, this.eventNum, this.eventMsgType,
 				this.eventMsgActionType, this.period, this.actualTime,
     			this.gameTime, this.homeDesc, this.neutralDesc,
     			this.awayDesc, this.score, this.margin); 
     }
 	
 	public String printPBP()
 	{
 		
 		String pbpString = "";
 		if (!this.homeDesc.trim().equals(""))
 		{
 			pbpString += this.homeDesc + " {" + this.eventNum + ",H}";
 		}
 		
 		if (!this.awayDesc.trim().equals(""))
 		{
 			if (pbpString != "")
 				pbpString += "\n" + this.awayDesc + " {" + this.eventNum + ",A}";
 			else
 				pbpString += this.awayDesc + " {" + this.eventNum + ",A}";
 		}
 		
 		if (!this.neutralDesc.trim().equals(""))
 		{
 			if (pbpString != "")
 				pbpString += "\n" + this.neutralDesc + " {" + this.eventNum + ",N}";
 			else
 				pbpString += this.neutralDesc + " {" + this.eventNum + ",N}";
 		}
 		
 		return pbpString;
 	}
 	
	private static int convertStringTime(String time)
 	{
 		String[] timeParts = time.split(":");
 		String min = timeParts[0];
 		String tens = timeParts[1].substring(0,1);
 		String singles = timeParts[1].substring(1, 2);
 		return 7200 - ((Integer.parseInt(min) * 60) + (Integer.parseInt(tens) * 10) +
 				Integer.parseInt(singles)) * 10;
 	}
 	
 	public int getConvertedStringTime()
 	{
 		String[] timeParts = this.gameTime.split(":");
 		String min = timeParts[0];
 		String tens = timeParts[1].substring(0,1);
 		String singles = timeParts[1].substring(1, 2);
 		return 7200 - ((Integer.parseInt(min) * 60) + (Integer.parseInt(tens) * 10) +
 				Integer.parseInt(singles)) * 10;
 	}
 	
 	private static int addPeriodTime(int period)
 	{
 		if (period < 5)
 		{
 			return (period - 1) * (12 * 60 * 10);
 		}
 		else
 		{
 			return ((period - 5) * (5 * 60 * 10)) +
 					((period - 1) * (12 * 60 * 10));
 		}
 	}
 
 	//Sort by play_id
     public static Comparator<PBPJson> COMPARE_BY_PLAY_ID = new Comparator<PBPJson>() {
         public int compare(PBPJson one, PBPJson other) {
             return Integer.compare(one.getEventNum(), other.getEventNum());
         }
     };
     
     public static Comparator<PBPJson> COMPARE_BY_GAME_TIME = new Comparator<PBPJson>() {
         public int compare(PBPJson one, PBPJson other) {
            return Integer.compare(convertStringTime(one.getGameTime()) + addPeriodTime(one.getPeriod()), 
            		convertStringTime(other.getGameTime()) + addPeriodTime(other.getPeriod()));
         }
     };
 }
