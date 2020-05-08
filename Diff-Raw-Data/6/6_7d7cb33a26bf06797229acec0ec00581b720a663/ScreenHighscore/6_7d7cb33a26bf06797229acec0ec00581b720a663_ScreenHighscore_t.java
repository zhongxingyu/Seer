 package game.screen;
 
 import game.Game;
 import game.utils.FileSaver;
 import game.utils.SpriteSheet;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 public class ScreenHighscore extends Screen
 {
 	
 	private String[] names;
 	private float[] scores;
 	private float score;
 	private String name;
 	private String typedText = "";
 	private boolean isTextFocused = false;
 	private boolean hasSubmittedScore = false;
 	private boolean scoreFailed = false;
 	private DecimalFormat df = new DecimalFormat("#.##");
 	private ArrayList<Integer> levelTimes;
 
 	public ScreenHighscore(int width, int height, SpriteSheet sheet, float score, ArrayList<Integer> levelTimes)
 	{
 		super(width, height, sheet);
 		this.score = Float.valueOf(df.format(score));
 		this.levelTimes = levelTimes;
 		
 		addButton("focusBox", new Rectangle(150, 520, 400, 40));
 		addButton("submitScore", new Rectangle(553, 521, 100, 40));
 	}
 	
 	@Override
 	public void tick()
 	{
 		if(names == null && scores == null)
 			getHighscores();
 	
 	}
 	
 	
 	@Override
 	public void render(Graphics g)
 	{	
 		this.drawBackgroundScreen();
 		if(names == null && scores == null)
 			game.getFontRenderer().drawString("LOADING", game.getWidth()/2-20,game.getHeight()/2, 1);
 		game.getFontRenderer().drawString("Enter your username below to submit your highscore", 200, 80, 1);
 		ScreenTools.drawButton(150, 100, 500, 400, " ", g, game);
 		ScreenTools.drawButton(150, 520, 400, 40, " ", g, game, new Color(0,0,0,155), isTextFocused ? new Color(255, 100, 100)  : new Color(255,255,255));
 		if(!hasSubmittedScore)
 		{
 			game.getFontRenderer().drawString(" - "+score, 406, 528, 2);
 			game.getFontRenderer().drawString(typedText, 156, 528, 2);
 		}
 		ScreenTools.drawButton(553, 521, 100, 40, "SUBMIT", g, game, new Color(255, 0, 0, 100), Color.red);
 		
 		game.getFontRenderer().drawString("Name                        Time taken to complete game", 185, 110, 1);
 		if(scores != null)
 		{
 			for(int i = 0; i < scores.length; i++)
 			{
 				if(i > 11)break;
 				if(hasSubmittedScore && names[i].equals(name) && scores[i] == score)
 				{
 					game.getFontRenderer().drawString(names[i], 160,140 + 32 * i, 2, new Color(255,255,0));
 					game.getFontRenderer().drawString("-", 430,140 + 32 * i, 2, new Color(255,255,0));
 					game.getFontRenderer().drawString(scores[i]+" mins", 500,140 + 32 * i, 2, new Color(255,255,0));
 					//game.getFontRenderer().drawString("mins", 540,145 + 32 * i, 1, new Color(255,255,0));
 				}else
 				{
 					game.getFontRenderer().drawString(names[i], 160,140 + 32 * i, 2);
 					game.getFontRenderer().drawString("-", 430,140 + 32 * i, 2);
 					game.getFontRenderer().drawString(scores[i]+" mins", 500,140 + 32 * i, 2);
 					//game.getFontRenderer().drawString("mins", 540,145 + 32 * i, );
 				}
 				
 			}
 			if(scores.length > 11)
 				game.getFontRenderer().drawString("... and "+(scores.length-11)+" more.", 530,489, 1);
 		}
 		
 		if(scoreFailed)
 			ScreenTools.drawButton(220, 200, 400, 100, " COULD NOT SUBMIT SCORE \n        Press ESC\n  Resistance is futile", g, game, new Color(155,0,0), new Color(255,255,255,255));
 		
 	}
 	
 	public void getHighscores()
 	{
 		Game.log("Trying to get highscores...");
 		try{
 		String hsRaw = FileSaver.getURL("http://fightthetoast.co.uk/assets/scoreboard.php?type=DISPLAY");
 		Game.log("Connection successful");
 		String[] hsUnparsed = hsRaw.replace("\n","").split("<br>");
 		names = new String[hsUnparsed.length];
 		scores = new float[hsUnparsed.length];
 		for(int i = 0; i < hsUnparsed.length; i++)
 		{
 			String[] score = hsUnparsed[i].split(" ");
 			names[i] = score[0];
 			scores[i] = Float.valueOf(score[1]);
 		}
 		Game.log("Parsed "+names.length+" highscores");
 		
 		
 		
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			names[0] = "Highscore loading failed";
 		}
 	}
 	
 	public boolean submitHighscore(String name, float score)
 	{
 		String scoreResponse = "";
 		try
 		{
			String stats = levelTimes.get(0)/60+"";
			for(int i = 1; i < levelTimes.size(); i++)
 			{
 				stats = stats+":"+levelTimes.get(i)/60;
 			}
			System.out.println(stats);
 			scoreResponse = FileSaver.getURL("http://fightthetoast.co.uk/assets/scoreboard.php?type=UPDATE&name="+name+"&score="+score+"&stat="+stats);
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		System.out.println(scoreResponse);
 		return scoreResponse.contains("OK");
 	}
 	
 	
 	@Override
 	public void postAction(String action)
 	{
 		if(action.equals("focusBox"))
 			isTextFocused = true;
 		
 		if(action.equals("submitScore"))
 		{
 			if(typedText.length() == 0)
 			{
 				isTextFocused = !isTextFocused;
 				return;
 			}
 				
 			name = typedText;
 			if(submitHighscore(name, score))
 			{
 				isTextFocused = false;
 				hasSubmittedScore = true;
 				getHighscores();
 			}else
 			{
 				scoreFailed = true;
 			}
 		}
 			
 			
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		if(isTextFocused && typedText.length() < 16){
 			if(arg0.getKeyCode() == KeyEvent.VK_DELETE || arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE)
 			{
 				if(!typedText.equals(""))
 					typedText = typedText.substring(0, typedText.length()-1);
 				
 			}else
 			{
 				if(!arg0.isActionKey())
 					typedText = typedText+arg0.getKeyChar();
 				else if(arg0.getKeyCode() == KeyEvent.VK_SPACE)
 					typedText = typedText+" ";
 			}
 		}
 	}
 
 }
