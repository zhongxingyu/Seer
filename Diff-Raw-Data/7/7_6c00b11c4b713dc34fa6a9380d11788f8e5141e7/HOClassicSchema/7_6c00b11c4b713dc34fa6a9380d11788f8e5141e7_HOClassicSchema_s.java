 /**
  * 
  */
 package de.hattrickorganizer.gui.theme.ho;
 
 import gui.HOBooleanName;
 import gui.HOColorName;
 import gui.HOIconName;
 
 import java.awt.Color;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.swing.ImageIcon;
 
 import de.hattrickorganizer.gui.theme.Schema;
 import de.hattrickorganizer.tools.HOLogger;
 
 public class HOClassicSchema extends Schema implements HOIconName,HOColorName,HOBooleanName {
 
 	public HOClassicSchema(){
 		initialize();
 	}
 	
 	private void initialize(){
 		setName("Classic");
 		initCachedColors();
 		initColors();
 		initBooleans();
 		initIcons();
 	}
 	
 	private void initIcons() {
 		put(MATCHTYPES[1],"gui/bilder/Meisterschale.gif");
 		put(MATCHTYPES[2],"gui/bilder/relegation.gif");
 		put(MATCHTYPES[3],"gui/bilder/Pokal.gif");
 		put(MATCHTYPES[4],"gui/bilder/freundschaft.gif");
 		put(MATCHTYPES[5],"gui/bilder/freundschaft.gif");
 		put(MATCHTYPES[6],"gui/bilder/Pokal2.gif");
 		put(MATCHTYPES[7],"gui/bilder/Pokal2.gif");
 		put(MATCHTYPES[8],"gui/bilder/freunschaft_intern.gif");
 		put(MATCHTYPES[9],"gui/bilder/freunschaft_intern.gif");
 		put(MATCHTYPES[10],"gui/bilder/Meisterschale2.gif");
 		put(MATCHTYPES[11],"gui/bilder/Meisterschale2.gif");
 		put(MATCHTYPES[12],"gui/bilder/freundschaft.gif");
 		
 		put(MANUELLSMILIES[1],"gui/bilder/smilies/1bigsmile.gif");
 		put(MANUELLSMILIES[2],"gui/bilder/smilies/2smile.gif");
 		put(MANUELLSMILIES[3],"gui/bilder/smilies/3normal.gif");
 		put(MANUELLSMILIES[4],"gui/bilder/smilies/4sad.gif");
 		put(MANUELLSMILIES[5],"gui/bilder/smilies/5verysad.gif");
 		put(MANUELLSMILIES[6],"gui/bilder/smilies/6clown.gif");
 		put(MANUELLSMILIES[7],"gui/bilder/smilies/7trainer.gif");
 		put(MANUELLSMILIES[8],"gui/bilder/smilies/8dollar.gif");
 		put(MANUELLSMILIES[9],"gui/bilder/smilies/9coach.gif");
 		
 		put(NO_TEAM,"gui/bilder/smilies/No-Team.png");
 		put(TEAMSMILIES[1],"gui/bilder/smilies/A-Team.png");
 		put(TEAMSMILIES[2],"gui/bilder/smilies/B-Team.png");
 		put(TEAMSMILIES[3],"gui/bilder/smilies/C-Team.png");
 		put(TEAMSMILIES[4],"gui/bilder/smilies/D-Team.png");
 		put(TEAMSMILIES[5],"gui/bilder/smilies/E-Team.png");
 		put(TEAMSMILIES[6],"gui/bilder/smilies/F-Team.png");
 		
 		
 		put(WEATHER[0],"gui/bilder/wetter/regen.gif");
 		put(WEATHER[1],"gui/bilder/wetter/bewoelkt.gif");
 		put(WEATHER[2],"gui/bilder/wetter/leichtbewoelkt.gif");
 		put(WEATHER[3],"gui/bilder/wetter/sonnig.gif");
 		put("weatherEffect1","gui/bilder/wetter/gut.gif");
 		put("weatherEffect-1","gui/bilder/wetter/schlecht.gif");
 		put(WEATHER_RAIN_POS,"gui/bilder/wetter/se_rain_positive.gif");
 		put(WEATHER_RAIN_NEG,"gui/bilder/wetter/se_rain_negative.gif");
 		put(WEATHER_SUN_POS,"gui/bilder/wetter/se_sun_positive.gif");
 		put(WEATHER_SUN_NEG,"gui/bilder/wetter/se_sun_negative.gif");
 
 		put(SPECIAL[1],"gui/bilder/spec1.png");
 		put(SPECIAL[2],"gui/bilder/spec2.png");
 		put(SPECIAL[3],"gui/bilder/spec3.png");
 		put(SPECIAL[4],"gui/bilder/spec4.png");
 		put(SPECIAL[5],"gui/bilder/spec5.png");
 		put(SPECIAL[6],"gui/bilder/spec6.png");
 		
 		put(TOOTHEDWHEEL,"gui/bilder/zahnrad.png");
 		put(HAND,"gui/bilder/Manuell.png");
 		
 		put(NO_MATCH,"gui/bilder/NoMatch.gif");
 		put(SHOW_MATCH,"gui/bilder/ShowMatch.gif");
 		put(DOWNLOAD_MATCH,"gui/bilder/DownloadMatch.gif");
 		
 		put(REDCARD,"gui/bilder/disqualifiziert.gif");
 		put(YELLOWCARD,"gui/bilder/verwarnung_1.png");
 		put(TWOCARDS,"gui/bilder/verwarnung_2.png"); 
 		
 		put(PATCH,"gui/bilder/angeschlagen.png");
 		put(INJURED,"gui/bilder/verletzt.png");
 		put(PATCHSMALL,"gui/bilder/angeschlagen_klein.png");
 		put(INJUREDSMALL,"gui/bilder/verletzt_klein.png");
 		
 		put("birthdayVolker","gui/bilder/vf.jpg");
 		put("birthdayTom","gui/bilder/tw.jpg");
 		
 		
 		put(PRINTER,"gui/bilder/Drucken.png");
 		put(CHECKBOXSELECTED,"gui/bilder/CheckBoxSelected.gif");
 		put(CHECKBOXNOTSELECTED,"gui/bilder/CheckBoxNotSelected.gif");
 		put(DISK,"gui/bilder/disk.png");
 		put(LOCKED,"gui/bilder/Locked.gif");
 		put(EMPTY,"gui/bilder/empty.gif");
 		put(INFO,"gui/bilder/info.gif");
 		put(GOTOANALYSETOP,"gui/bilder/gotoAnalyseTop.png");
 		put(GOTOANALYSEBOTTOM,"gui/bilder/gotoAnalyseBottom.png");
 		put(OFFSET,"gui/bilder/offset.png");
 		put(GOTOSTATISTIK,"gui/bilder/gotoStatistik.png");
 		put(TRAININGBLOCK,"gui/bilder/trainingblock.png");
 		
 		put(MAXLINEUP,"gui/bilder/MaxAufstellung.png");
 		put(RELOAD,"gui/bilder/Reload.png");
 		put(SIMULATEMATCH,"gui/bilder/simulate_match.png");
 		put(GETLINEUP,"gui/bilder/AufstellungUebernehmen.png");
 		put(MIDLINEUPFRAME,"gui/bilder/MidiAufstellung.png");
 		put(MINLINEUPFRAME,"gui/bilder/MiniAufstellung.png");
 		put(SWAP,"gui/bilder/swap.png");
 		put(SWAPPRESSED,"gui/bilder/swap-pressed.png");
 		put(TURN,"gui/bilder/drehen.png");
 		
 		put(CLEARASSIST,"gui/bilder/Assist_leeren.png");
 		put(STARTASSIST,"gui/bilder/Assist_start.png");
 		put(CLEARRESERVE,"gui/bilder/Assist_reserveleeren.png");
 		
 		put(LOGO16,"gui/bilder/Logo-16px.png");
 		put(TRICKOT,"gui/bilder/Trickot.png");
 		
 		// Highlights
 		put(GOAL,"gui/bilder/highlights/Fussball.png");
 		put(GOAL_FREEKICK,"gui/bilder/highlights/Fussball_Freistoss.png");
 		put(GOAL_MID,"gui/bilder/highlights/Fussball_Mitte.png");
 		put(GOAL_LEFT,"gui/bilder/highlights/Fussball_Links.png");
 		put(GOAL_RIGHT,"gui/bilder/highlights/Fussball_Rechts.png");
 		put(GOAL_PENALTY,"gui/bilder/highlights/Fussball_Elfmeter.png");
 		put(GOAL_FREEKICK2,"gui/bilder/highlights/Fussball_FreistossIndirekt.png");
 		put(GOAL_LONGSHOT,"gui/bilder/highlights/Fussball_Longshot.png");
 		put(GOAL_SPECIAL,"gui/bilder/highlights/Fussball_Spezial.png");
 		put(GOAL_COUNTER,"gui/bilder/highlights/Fussball_Konter.png");
 		put(NOGOAL,"gui/bilder/highlights/KeinFussball.png");
 		put(NOGOAL_FREEKICK,"gui/bilder/highlights/KeinFussball_Freistoss.png");
 		put(NOGOAL_MID,"gui/bilder/highlights/KeinFussball_Mitte.png");
 		put(NOGOAL_LEFT,"gui/bilder/highlights/KeinFussball_Links.png");
 		put(NOGOAL_RIGHT,"gui/bilder/highlights/KeinFussball_Rechts.png");
 		put(NOGOAL_PENALTY,"gui/bilder/highlights/KeinFussball_Elfmeter.png");
 		put(NOGOAL_FREEKICK,"gui/bilder/highlights/KeinFussball_FreistossIndirekt.png");
 		put(NOGOAL_LONGSHOT,"gui/bilder/highlights/KeinFussball_Longshot.png");
 		put(NOGOAL_SPECIAL,"gui/bilder/highlights/KeinFussball_Spezial.png");
 		put(NOGOAL_COUNTER,"gui/bilder/highlights/KeinFussball_Konter.png");
 		
 		put(STAR,"gui/bilder/star.gif");
 		put(STAR_GRAY,"gui/bilder/star_grey.png");
 		
 		put(HOMEGROWN, "gui/bilder/motherclub.png");
 		put(IMAGEPANEL_BACKGROUND,"gui/bilder/Background.jpg");
 		}
 
 	private void initBooleans() {
 		put(IMAGEPANEL_BG_PAINTED,Boolean.TRUE);
 		
 	}
 
 	private void initCachedColors(){
 		// don´t use UIManager keys !!
 		put("black", Color.BLACK);
 		put("white", Color.WHITE);
 		put("gray", Color.GRAY);
 		put("green", Color.GREEN);
 		put("yellow", Color.YELLOW);
 		put("dark_gray", Color.DARK_GRAY);
 		put("light_gray", Color.LIGHT_GRAY);
 		put("lightGreen",new Color(220, 255, 220));
 		put("lightYellow",new Color(255, 255, 200));
 		put("ho_gray1", new Color(230,230,230));
 	}
 	
 	/**
 	 * key-Syntax => javaComponent.[hoComponent].property || name
 	 *  	
 	 */
 	private void initColors(){
 		put(PANEL_BG,"white");
 		put(PANEL_BORDER,"dark_gray");
 		put(BUTTON_BG,"white");
 		put(BUTTON_ASSIST_BG,"yellow");
 		
 		put(LABEL_ERROR_FG,Color.RED);
 		put(LABEL_SUCCESS_FG,"green");
 		put(LABEL_ONGREEN_FG,"white");
 		put(LABEL_FG,"black");
 		put(LIST_FG,"black");
 		put(LIST_CURRENT_FG,new Color(0, 0, 150));
 		put(TABLE_SELECTION_BG,new Color(235, 235, 235));
 		put(TABLE_SELECTION_FG,LABEL_FG);
 		put(LIST_SELECTION_BG,new Color(220, 220, 255));
 
 		put(MATCHHIGHLIGHT_FAILED_FG,"gray");
 		
 		//player
 		put(PLAYER_SKILL_SPECIAL_BG,"lightGreen");
 		put(PLAYER_SKILL_BG,"lightYellow");
 		put(TABLEENTRY_BG,"white");
 		put(TABLEENTRY_FG,"black");
 		put(PLAYER_POS_BG,new Color(220, 220, 255));
 		put(PLAYER_SUBPOS_BG,new Color(235, 235, 255));
 		put(PLAYER_OLD_FG,"gray");
 		put(TABLEENTRY_IMPROVEMENT_FG,new Color(0, 200, 0));
 		put(TABLEENTRY_DECLINE_FG,new Color(200, 0, 0));
 		put(SKILLENTRY2_BG,"gray");
 		
 		
 		// league Table
 		put(TEAM_FG,new Color(50, 50, 150));
 		put(LEAGUE_TITLE_BG,"ho_gray1");
 		put(LEAGUE_PROMOTED_BG,"lightGreen");
 		put(LEAGUE_RELEGATION_BG,"lightYellow");
 		put(LEAGUE_DEMOTED_BG,new Color(255, 220, 220));
 		put(LEAGUE_BG,"white");
 		put(LEAGUE_FG,"black");
 		
 		// league history panel
 		put(LEAGUEHISTORY_LINE1_FG, Color.GREEN);
 		put(LEAGUEHISTORY_LINE2_FG,  Color.CYAN);
 		put(LEAGUEHISTORY_LINE3_FG,  Color.GRAY);
 		put(LEAGUEHISTORY_LINE4_FG,  "black");
 		put(LEAGUEHISTORY_LINE5_FG,  Color.ORANGE);
 		put(LEAGUEHISTORY_LINE6_FG,  Color.PINK);
 		put(LEAGUEHISTORY_LINE7_FG,  Color.RED);
 		put(LEAGUEHISTORY_LINE8_FG,  Color.MAGENTA);
 		put(LEAGUEHISTORY_CROSS_FG, Color.DARK_GRAY);
 		put(LEAGUEHISTORY_GRID_FG,"light_gray");
 		
 		
 		//lineup
 		
 		put(SEL_OVERLAY_SELECTION_BG,new Color(10, 255, 10, 40));
 		put(SEL_OVERLAY_BG,new Color(255, 10, 10, 40));
 		put(LINEUP_POS_MIN_BG,PANEL_BG);
 		put(LINEUP_POS_MIN_BORDER,"light_gray");
 		
 		//shirts
 	    put(SHIRT_KEEPER,"black");
 	    put(SHIRT_CENTRALDEFENCE,new Color(0, 0, 220));
 	    put(SHIRT_WINGBACK,new Color(0, 220, 0));
 	    put(SHIRT_MIDFIELD,new Color(220, 220, 0));
 	    put(SHIRT_WING,new Color(220, 140, 0));
 	    put(SHIRT_FORWARD,new Color(220, 0, 0));
 	    put(SHIRT_SUBKEEPER,new Color(200, 200, 200));
 	    put(SHIRT_SUBDEFENCE,new Color(200, 200, 255));
 	    put(SHIRT_SUBMIDFIELD,new Color(255, 255, 180));
 	    put(SHIRT_SUBWING,new Color(255, 225, 180));
 	    put(SHIRT_SUBFORWARD,new Color(255, 200, 200));
 	    put(SHIRT,"ho_gray1");
 	    
 	    put(STAT_LEADERSHIP,Color.GRAY);
 	    put(STAT_EXPERIENCE,Color.DARK_GRAY);
 	    put(STAT_FORM,Color.PINK);
 	    put(STAT_STAMINA,Color.MAGENTA);
 	    put(STAT_LOYALTY,new Color(180, 180, 0));
 	    put(STAT_KEEPER,"black");
 	    put(STAT_DEFENDING,Color.BLUE);
 	    put(STAT_PLAYMAKING,"yellow");
 	    put(STAT_PASSING,Color.GREEN);
 	    put(STAT_WINGER,Color.ORANGE);
 	    put(STAT_SCORING,Color.RED);
 	    put(STAT_SET_PIECES,Color.CYAN);
 	    
 	    put(STAT_CASH,"black");
 	    put(STAT_WINLOST,Color.GRAY);
 	    put(STAT_INCOMESUM,Color.GREEN);
 	    put(STAT_COSTSUM,Color.RED);
 	    put(STAT_INCOMESPECTATORS,new Color(0, 180, 0));
 	    put(STAT_INCOMESPONSORS,new Color(0, 120, 60));
 	    put(STAT_INCOMEFINANCIAL,new Color(0, 60, 120));
 	    put(STAT_INCOMETEMPORARY,new Color(0, 0, 180));
 	    put(STAT_COSTARENA,new Color(180, 0, 0));
 	    put(STAT_COSTSPLAYERS,new Color(180, 36, 0));
 	    put(STAT_COSTFINANCIAL,new Color(180, 72, 0));
 	    put(STAT_COSTTEMPORARY,new Color(180, 108, 0));
 	    put(STAT_COSTSTAFF,new Color(180, 144, 0));
 	    put(STAT_COSTSYOUTH,new Color(180, 180, 0));
 	    put(STAT_FANS,Color.CYAN);
 	    put(STAT_MARKETVALUE,Color.BLUE);
 	    put(STAT_RATING,new Color(100, 200, 0));
 	    put(STAT_WAGE,new Color(150, 20, 20));
 	    put(STAT_RATING2,"black");
 	    put(STAT_TOTAL,Color.GRAY);
 	    put(STAT_MOOD,Color.PINK);
 	    put(STAT_CONFIDENCE,Color.CYAN);
 	    
 	    
 	    //matchtypes
 	    put(MATCHTYPE_LEAGUE_BG,"lightYellow");
	    put(MATCHTYPE_QUALIFIKATION_BG,"lightYellow");
	    put(MATCHTYPE_CUP_BG,"lightYellow");
 	    put(MATCHTYPE_BG,"white");
	    put(MATCHTYPE_FRIENDLY_BG,"white");
 	    put(MATCHTYPE_INT_BG,"light_gray");
 	    put(MATCHTYPE_MASTERS_BG,"light_gray");
 	    put(MATCHTYPE_INTFRIENDLY_BG,"white");
 	    put(MATCHTYPE_NATIONAL_BG,new Color(220, 220, 255));
 	    
 	}
 	
 	public Color getDefaultColor(String key){
 		return key.contains("fg")?Color.BLACK:Color.WHITE; 
 	}
 	
 	public ImageIcon getThemeIcon(String key){
 		return (ImageIcon)cache.get(key);
 	}
 	@Override
 	public ImageIcon loadImageIcon(String path) {
 		ImageIcon image = null;
 
 		image = (ImageIcon)cache.get(path);
 		if (image == null) {
 			try {
 				URL resource = HOClassicSchema.class.getClassLoader().getResource(path);
 				if (resource == null) {
 					HOLogger.instance().log(Schema.class, path + " Not Found!!!");
 					return loadImageIcon("gui/bilder/Unknownflag.png");
 				}
 
 				image = new ImageIcon(resource);
 				cache.put(path, image);
 
 				return image;
 			} catch (Throwable e) {
 				HOLogger.instance().log(Schema.class, e);
 			}
 		}
 		return image;
 	}
 }
