 package de.hattrickorganizer.gui.model;
 
 import java.awt.Color;
 
 import javax.swing.SwingConstants;
 import javax.swing.table.TableColumn;
 
 import plugins.IEPVData;
 import plugins.IMatchKurzInfo;
 import plugins.ISpieler;
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.gui.playeroverview.SpielerStatusLabelEntry;
 import de.hattrickorganizer.gui.templates.ColorLabelEntry;
 import de.hattrickorganizer.gui.templates.DoppelLabelEntry;
 import de.hattrickorganizer.gui.templates.RatingTableEntry;
 import de.hattrickorganizer.gui.templates.SpielerLabelEntry;
 import de.hattrickorganizer.gui.templates.TableEntry;
 import de.hattrickorganizer.model.HOModel;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.Spieler;
 import de.hattrickorganizer.model.SpielerPosition;
 import de.hattrickorganizer.model.matches.MatchKurzInfo;
 import de.hattrickorganizer.model.matches.Matchdetails;
 import de.hattrickorganizer.tools.Helper;
 import de.hattrickorganizer.tools.HelperWrapper;
 import de.hattrickorganizer.tools.PlayerHelper;
 
 /**
  * Create the userColumns
  * @author Thorsten Dietz
  *
  */
 final public class UserColumnFactory {
 
     //~ Static fields/initializers -----------------------------------------------------------------
 	/** id from the column NAME **/
 	public static final int NAME = 1;
 	
 	/** id from the column BEST_POSITION **/
 	public static final int BEST_POSITION = 40;
 
 	/** id from the column LINUP **/
 	public static final int LINUP = 50;
 	
 	/** id from the column GROUP **/
 	public static final int GROUP = 60;
 	
 	/** id from the column ID **/
 	public static final int ID = 440;
 	
 	/** id from the column DATUM **/
 	public static final int DATUM = 450;
 	
 	/** id from the column RATING **/
 	public static final int RATING = 435;
 	
 	/** id from the column DATUM **/
 	public static final int AUTO_LINEUP = 510;
 	
 	/**
 	 * 
 	 * @return PlayerCBItem[]
 	 */
 	protected static PlayerCBItem[] createPlayerCBItemArray(){
 		final PlayerCBItem[] playerCBItemArray = new PlayerCBItem[4];
 		playerCBItemArray[0] = new PlayerCBItem(590,"Stimmung"){
 			public TableEntry getTableEntry(SpielerMatchCBItem spielerCBItem){
 				return new ColorLabelEntry(spielerCBItem.getStimmung(),
                         ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD, SwingConstants.LEFT);
 			}
 		};
 		
 		playerCBItemArray[1] = new PlayerCBItem(600,"Selbstvertrauen"){
 			public TableEntry getTableEntry(SpielerMatchCBItem spielerCBItem){
 				return new ColorLabelEntry(spielerCBItem.getSelbstvertrauen(),
                         ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD, SwingConstants.LEFT);
 			}
 		};
 		
 		playerCBItemArray[2] = new PlayerCBItem(601,"Position"){
 			public TableEntry getTableEntry(SpielerMatchCBItem spielerCBItem){
 				ColorLabelEntry colorLabelEntry = new ColorLabelEntry(Helper
                         .getImage4Position(SpielerPosition
                                            .getHTPosidForHOPosition4Image((byte) spielerCBItem
                                                                           .getPosition()),
                                            (byte) 0, 0),
                         -SpielerPosition.getSortId((byte) spielerCBItem
                                                    .getPosition(), false),
                         ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD, SwingConstants.LEFT);
 				colorLabelEntry.setText(SpielerPosition.getNameForPosition((byte) spielerCBItem
                         .getPosition())
                         + " ("
                         + spielerCBItem.getSpieler().calcPosValue((byte) spielerCBItem
                                                         .getPosition(),
                                                         true) + ")");
 				return colorLabelEntry;
 			}
 		};
 		
 		playerCBItemArray[3] = new PlayerCBItem(RATING,"Bewertung"){
 			public TableEntry getTableEntry(SpielerMatchCBItem spielerCBItem){
 				return new RatingTableEntry(spielerCBItem.getRating(), false);		            
 			}				
 		};
 				
 		return playerCBItemArray;
 	}
 	
 	/**
 	 * 
 	 * @return MatchDetailsColumn[]
 	 */
 	protected static MatchDetailsColumn[] createMatchDetailsColumnsArray(){
 		final MatchDetailsColumn[] matchDetailsColumnsArray = new MatchDetailsColumn[4];
 		matchDetailsColumnsArray[0] = new MatchDetailsColumn(550,"Wetter",30){
 			public TableEntry getTableEntry(Matchdetails matchdetails){
 				return new ColorLabelEntry(de.hattrickorganizer.tools.Helper
                         .getImageIcon4Wetter(matchdetails.getWetterId()),
                         matchdetails.getWetterId(),
                         ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD, SwingConstants.RIGHT);
 			}
 		};// Wetter
 		matchDetailsColumnsArray[1] = new MatchDetailsColumn(560,"Einstellung"){
 			public TableEntry getTableEntry(Matchdetails matchdetails){
 				final int teamid = HOVerwaltung.instance().getModel()
                 .getBasics().getTeamId();
 				int einstellung = (matchdetails.getHeimId() == teamid)?matchdetails.getHomeEinstellung():matchdetails.getGuestEinstellung(); 
 				return new ColorLabelEntry(Matchdetails.getNameForEinstellung(einstellung), ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD, SwingConstants.LEFT);
 			}
 		};
 		matchDetailsColumnsArray[2] = new MatchDetailsColumn(570,"Taktik"){
 			public TableEntry getTableEntry(Matchdetails matchdetails){
 				final int teamid = HOVerwaltung.instance().getModel()
                 .getBasics().getTeamId();
 				int tactic = (matchdetails.getHeimId() == teamid)?matchdetails.getHomeTacticType():matchdetails.getGuestTacticType(); 
 				return new ColorLabelEntry(Matchdetails.getNameForTaktik(tactic), ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD, SwingConstants.LEFT);
 			}
 		};
 		matchDetailsColumnsArray[3] = new MatchDetailsColumn(580,"Taktikstaerke"){
 			public TableEntry getTableEntry(Matchdetails matchdetails){
 				final int teamid = HOVerwaltung.instance().getModel()
                 .getBasics().getTeamId();
 				int tacticSkill = (matchdetails.getHeimId() == teamid)?matchdetails.getHomeTacticSkill():matchdetails.getGuestTacticSkill(); 
 				return new ColorLabelEntry(PlayerHelper.getNameForSkill(tacticSkill), ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD, SwingConstants.LEFT);
 			}
 		};
 		
 		return matchDetailsColumnsArray;
 	}
 	
 	/**
 	 * 
 	 * @return PlayerColumn[]
 	 */
 	protected static PlayerColumn[] createGoalsColumnsArray(){
 		final PlayerColumn[] playerGoalsArray = new PlayerColumn[4];
 		playerGoalsArray[0] = new PlayerColumn(380,"TG","ToreGesamt",20){
 			public int getValue(Spieler player){
 				return player.getToreGesamt();
 			}
 		}; 
 		
 		playerGoalsArray[1] = new PlayerColumn(390,"TF","ToreFreund",20){
 			public int getValue(Spieler player){
 				return player.getToreFreund();
 			}
 		}; 
 		
 		playerGoalsArray[2] = new PlayerColumn(400,"TL","ToreLiga",20){
 			public int getValue(Spieler player){
 				return player.getToreLiga();
 			}
 		}; 
 		
 		playerGoalsArray[3] = new PlayerColumn(410,"TP","TorePokal",20){
 			public int getValue(Spieler player){
 				return player.getTorePokal();
 			}
 		}; 
 		return playerGoalsArray;
 	}
 	/**
 	 * 
 	 * @return PlayerSkillColumn []
 	 */
 	protected static PlayerSkillColumn[] createPlayerSkillArray(){
 		final PlayerSkillColumn[] playerSkillArray = new PlayerSkillColumn[11];
 		playerSkillArray[0]  = new PlayerSkillColumn( 	80,  "FUE",		"Fuehrung",				ISpieler.SKILL_LEADERSHIP); 
 		playerSkillArray[1]  = new PlayerSkillColumn( 	90,  "ER",		"Erfahrung",			ISpieler.SKILL_EXPIERIENCE); 
 		playerSkillArray[2]  = new PlayerSkillColumn( 	100, "FO",		"Form",					ISpieler.SKILL_FORM); 
 		playerSkillArray[3]  = new PlayerSkillColumn( 	110, "KO",		"Kondition",			ISpieler.SKILL_KONDITION); 
 		playerSkillArray[4]  = new PlayerSkillColumn( 	120, "TW",		"Torwart",				ISpieler.SKILL_TORWART); 
 		playerSkillArray[5]  = new PlayerSkillColumn( 	130, "VE",		"Verteidigung",			ISpieler.SKILL_VERTEIDIGUNG); 
 		playerSkillArray[6]  = new PlayerSkillColumn( 	140, "SA",		"Spielaufbau",			ISpieler.SKILL_SPIELAUFBAU); 
 		playerSkillArray[7]  = new PlayerSkillColumn( 	150, "PS",		"Passpiel",				ISpieler.SKILL_PASSSPIEL); 
 		playerSkillArray[8]  = new PlayerSkillColumn( 	160, "FL",		"Fluegelspiel",			ISpieler.SKILL_FLUEGEL); 
 		playerSkillArray[9]  = new PlayerSkillColumn( 	170, "TS",		"Torschuss",			ISpieler.SKILL_TORSCHUSS); 
 		playerSkillArray[10] = new PlayerSkillColumn( 	180, "ST",		"Standards",			ISpieler.SKILL_STANDARDS); 
 
 		return playerSkillArray;
 	}
 	
 	/**
 	 * 
 	 * @return PlayerColumn []
 	 */
 	protected static PlayerColumn[] createPlayerBasicArray(){
 		final PlayerColumn[] playerBasicArray = new PlayerColumn[2];
 		playerBasicArray[0] = new PlayerColumn(NAME,"Name",0){
 			public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 				return new SpielerLabelEntry(player,
                         HOVerwaltung.instance().getModel()
                         .getAufstellung()
                         .getPositionBySpielerId(player.getSpielerID()),
             0f, false, false);
 			}
 			public boolean isEditable(){
 				return false;
 			}
 		};
 		
 		playerBasicArray[1] = new PlayerColumn(ID,"ID",0){
 			public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 				return new ColorLabelEntry(player.getSpielerID(),
 						player.getSpielerID() + "",
                         ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD, SwingConstants.RIGHT);
 			}
 			public boolean isEditable(){
 				return false;
 			}
 			
 			public void setSize(TableColumn column){
 				column.setMinWidth(0);
 				column.setPreferredWidth(0);
 			} 
 		};
 		return playerBasicArray;
 	}
 	
 	/**
 	 * 
 	 * @return PlayerPositionColumn[]
 	 */
 	protected static PlayerPositionColumn[] createPlayerPositionArray(){
 		final PlayerPositionColumn[] playerPositionArray = new PlayerPositionColumn[19];
 		playerPositionArray[0] = new PlayerPositionColumn( 190, "TORW",	"Torwart",				ISpielerPosition.TORWART ); 
 		playerPositionArray[1] = new PlayerPositionColumn( 200, "IV",		"Innenverteidiger",		ISpielerPosition.INNENVERTEIDIGER ); 
 		playerPositionArray[2] = new PlayerPositionColumn( 210, "IVA",		"Innenverteidiger_Aus",	ISpielerPosition.INNENVERTEIDIGER_AUS ); 
 		playerPositionArray[3] = new PlayerPositionColumn( 220, "IVO",		"Innenverteidiger_Off",	ISpielerPosition.INNENVERTEIDIGER_OFF ); 
 		playerPositionArray[4] = new PlayerPositionColumn( 230, "AV",		"Aussenverteidiger",	ISpielerPosition.AUSSENVERTEIDIGER ); 
 		playerPositionArray[5] = new PlayerPositionColumn( 240, "AVI",		"Aussenverteidiger_In",	ISpielerPosition.AUSSENVERTEIDIGER_IN ); 
 		playerPositionArray[6] = new PlayerPositionColumn( 250, "AVO",		"Aussenverteidiger_Off",ISpielerPosition.AUSSENVERTEIDIGER_OFF ); 
 		playerPositionArray[7] = new PlayerPositionColumn( 260, "AVD",		"Aussenverteidiger_Def",ISpielerPosition.AUSSENVERTEIDIGER_DEF ); 
 		playerPositionArray[8] = new PlayerPositionColumn( 270, "MIT",		"Mittelfeld",			ISpielerPosition.MITTELFELD ); 
 		playerPositionArray[9] = new PlayerPositionColumn( 280, "MITA",		"Mittelfeld_Aus",		ISpielerPosition.MITTELFELD_AUS ); 
 		playerPositionArray[10] = new PlayerPositionColumn( 290, "MITO",	"Mittelfeld_Off",		ISpielerPosition.MITTELFELD_OFF ); 
 		playerPositionArray[11] = new PlayerPositionColumn( 300, "MITD",	"Mittelfeld_Def",		ISpielerPosition.MITTELFELD_DEF ); 
 		playerPositionArray[12] = new PlayerPositionColumn( 310, "FLG",		"Fluegelspiel",			ISpielerPosition.FLUEGELSPIEL ); 
 		playerPositionArray[13] = new PlayerPositionColumn( 320, "FLGI",	"Fluegelspiel_In",		ISpielerPosition.FLUEGELSPIEL_IN ); 
 		playerPositionArray[14] = new PlayerPositionColumn( 330, "FLGO",	"Fluegelspiel_Off",		ISpielerPosition.FLUEGELSPIEL_OFF ); 
 		playerPositionArray[15] = new PlayerPositionColumn( 340, "FLGD",	"Fluegelspiel_Def",		ISpielerPosition.FLUEGELSPIEL_DEF ); 
 		playerPositionArray[16] = new PlayerPositionColumn( 350, "STU",		"Sturm",				ISpielerPosition.STURM ); 
 		playerPositionArray[17] = new PlayerPositionColumn( 360, "STUA",	"Sturm_Aus",			ISpielerPosition.STURM_AUS ); 
 		playerPositionArray[18] = new PlayerPositionColumn( 370, "STUD",	"Sturm_Def",			ISpielerPosition.STURM_DEF ); 
 		return playerPositionArray;
 	}
 	
 	/**
 	 * 
 	 * @return MatchKurzInfoColumn[]
 	 */
 	protected static MatchKurzInfoColumn[] createMatchesArray(){
 		final MatchKurzInfoColumn[] matchesArray = new MatchKurzInfoColumn[6];
 		matchesArray[0] = new MatchKurzInfoColumn(450,"Datum",70){
 			public TableEntry getTableEntry(MatchKurzInfo match){
 				final Color background = getColor4Matchtyp(match.getMatchTyp());
 				return new ColorLabelEntry(match.getMatchDateAsTimestamp().getTime(),
                         java.text.DateFormat.getDateTimeInstance().format(match
                                 .getMatchDateAsTimestamp()),
                                 ColorLabelEntry.FG_STANDARD, background,
                                 SwingConstants.LEFT);
 			}
 			
 			public TableEntry getTableEntry(SpielerMatchCBItem spielerCBItem){
 				return new ColorLabelEntry(spielerCBItem.getMatchdate(),
                     ColorLabelEntry.FG_STANDARD,
                     ColorLabelEntry.BG_STANDARD, SwingConstants.CENTER);
 			}
 		};
 		
 		matchesArray[1] = new MatchKurzInfoColumn(460," ","Spielart",20){
 			public TableEntry getTableEntry(MatchKurzInfo match){
 				final Color background = getColor4Matchtyp(match.getMatchTyp());
 				return new ColorLabelEntry(de.hattrickorganizer.tools.Helper
                         .getImageIcon4Spieltyp(match.getMatchTyp()),
                         match.getMatchTyp(), ColorLabelEntry.FG_STANDARD,
                         background, SwingConstants.CENTER);
 			}
 			
 			public TableEntry getTableEntry(SpielerMatchCBItem spielerCBItem){
 				final Color background = getColor4Matchtyp(spielerCBItem.getMatchTyp());
 				return new ColorLabelEntry(de.hattrickorganizer.tools.Helper
                         .getImageIcon4Spieltyp(spielerCBItem.getMatchTyp()),
                         spielerCBItem.getMatchTyp(),
                         ColorLabelEntry.FG_STANDARD, background,
                         SwingConstants.CENTER);
 			}
 		};
 		
 		matchesArray[2] = new MatchKurzInfoColumn(470,"Heim",60){
 			public TableEntry getTableEntry(MatchKurzInfo match){
 				final Color background = getColor4Matchtyp(match.getMatchTyp());
 				ColorLabelEntry entry = new ColorLabelEntry(match.getHeimName(), ColorLabelEntry.FG_STANDARD,
                         background, SwingConstants.LEFT);
 				entry.setFGColor((match.getHeimID() == HOVerwaltung.instance().getModel().getBasics()
                         .getTeamId())?MatchesColumnModel.FG_EIGENESTEAM:Color.black);
 				
 				if (match.getMatchStatus() != IMatchKurzInfo.FINISHED) 
 					entry.setIcon(Helper.NOIMAGEICON);
 				else if (match.getHeimTore() > match.getGastTore())
 					entry.setIcon(Helper.YELLOWSTARIMAGEICON);
 				else if (match.getHeimTore() < match.getGastTore())
 					entry.setIcon(Helper.NOIMAGEICON);
 				else
 					entry.setIcon(Helper.GREYSTARIMAGEICON);
 				return entry;
 			}
 			
 			public TableEntry getTableEntry(SpielerMatchCBItem spielerCBItem){
 				final Color background = getColor4Matchtyp(spielerCBItem.getMatchTyp());
 				ColorLabelEntry entry = new ColorLabelEntry(spielerCBItem.getHeimteam() + "",
                         ColorLabelEntry.FG_STANDARD, background,
                         SwingConstants.LEFT);
 				entry.setFGColor((spielerCBItem.getHeimID() == HOVerwaltung.instance().getModel().getBasics()
                         .getTeamId())?MatchesColumnModel.FG_EIGENESTEAM:Color.black);
 				return entry;
 			}
 			
 			public void setSize(TableColumn column){
 				column.setMinWidth(60);
 				column.setPreferredWidth((preferredWidth==0)?160:preferredWidth);
 			}
 		};
 		
 		matchesArray[3] = new MatchKurzInfoColumn(480,"Gast",60){
 			public TableEntry getTableEntry(MatchKurzInfo match){
 				final Color background = getColor4Matchtyp(match.getMatchTyp());
 				ColorLabelEntry entry = new ColorLabelEntry(match.getGastName(), ColorLabelEntry.FG_STANDARD,
                         background, SwingConstants.LEFT);
 				entry.setFGColor((match.getGastID() == HOVerwaltung.instance().getModel().getBasics()
                         .getTeamId())?MatchesColumnModel.FG_EIGENESTEAM:Color.black);
 				
 				if (match.getMatchStatus() != IMatchKurzInfo.FINISHED) 
 					entry.setIcon(Helper.NOIMAGEICON);
 				else if (match.getHeimTore() > match.getGastTore())
 					entry.setIcon(Helper.NOIMAGEICON);
 				else if (match.getHeimTore() < match.getGastTore())
 					entry.setIcon(Helper.YELLOWSTARIMAGEICON);
 				else
 					entry.setIcon(Helper.GREYSTARIMAGEICON);
 				
 				return entry;
 			}
 			
 			public TableEntry getTableEntry(SpielerMatchCBItem spielerCBItem){
 				final Color background = getColor4Matchtyp(spielerCBItem.getMatchTyp());
 				ColorLabelEntry entry = new ColorLabelEntry(spielerCBItem.getGastteam() + "",
                         ColorLabelEntry.FG_STANDARD, background,
                         SwingConstants.LEFT);
 				entry.setFGColor((spielerCBItem.getGastID() == HOVerwaltung.instance().getModel().getBasics()
                         .getTeamId())?MatchesColumnModel.FG_EIGENESTEAM:Color.black);
 				return entry;
 			}
 			
 			public void setSize(TableColumn column){
 				column.setMinWidth(60);
 				column.setPreferredWidth((preferredWidth==0)?160:preferredWidth);
 			}
 		};
 		
 		matchesArray[4] = new MatchKurzInfoColumn(490,"Ergebnis",45){
 			public TableEntry getTableEntry(MatchKurzInfo match){
 				final Color background = getColor4Matchtyp(match.getMatchTyp());
 				return new ColorLabelEntry(createTorString(match.getHeimTore(),
                         match.getGastTore()),
                         	ColorLabelEntry.FG_STANDARD, background,
                         	SwingConstants.CENTER);
 			}
 			
 			public TableEntry getTableEntry(SpielerMatchCBItem spielerCBItem){
 				final Color background = getColor4Matchtyp(spielerCBItem.getMatchTyp());
 				return new ColorLabelEntry(createTorString(spielerCBItem.getMatchdetails().getHomeGoals(),
 						spielerCBItem.getMatchdetails().getGuestGoals()),
                         	ColorLabelEntry.FG_STANDARD, background,
                         	SwingConstants.CENTER);
 			}
 
 		};
 		
 		matchesArray[5] = new MatchKurzInfoColumn(500,"ID",55){
 			
 			public TableEntry getTableEntry(MatchKurzInfo match){
 				final Color background = getColor4Matchtyp(match.getMatchTyp());
 				return new ColorLabelEntry(match.getMatchID(), match.getMatchID() + "",
                         ColorLabelEntry.FG_STANDARD, background,
                         SwingConstants.RIGHT);
 			}
 		};
 		
 		return matchesArray;
 	}
 	
 	/**
 	 * creates an array of various player columns
 	 * @return PlayerColumn[]
 	 */
 	protected static PlayerColumn[] createPlayerAdditionalArray(){
 			final PlayerColumn [] playerAdditionalArray = new PlayerColumn[11];
 			
 			playerAdditionalArray[0] =new PlayerColumn(10," "," ",0){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					int sort = player.getTrikotnummer();
 					if (sort <= 0) {
 		                //Damit die Spieler ohne Trickot nach den andern kommen
 		                sort = 10000;
 		            }
 					
 					return new ColorLabelEntry(Helper.getImageIcon4Trickotnummer(player
                                     .getTrikotnummer()),
                                     	sort, java.awt.Color.black, java.awt.Color.white,
                                     		SwingConstants.LEFT);
 							}
 				
 				public boolean isEditable(){
 					return false;
 				}
 			}; 
 			
 			playerAdditionalArray[1] =new PlayerColumn(20," ","Nationalitaet",25){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					return new ColorLabelEntry(Helper.getImageIcon4Country(player.getNationalitaet()),
                             player.getNationalitaet(),
                             ColorLabelEntry.FG_STANDARD,
                             ColorLabelEntry.BG_FLAGGEN, SwingConstants.CENTER);
 				}
 			}; 
 			
 			playerAdditionalArray[2] = new PlayerColumn(30, "Alter", 40){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					String ageString = player.getAlterWithAgeDaysAsString();
 					int birthdays = 0;
 					boolean playerExists;
 
 					if(playerCompare == null){
 						// Birthdays since last HRF
 						birthdays = (int) (Math.floor(player.getAlterWithAgeDays()) - player.getAlter());
 						playerExists = false;
 					} else {
 						// Birthdays since compare
 						birthdays = (int) (Math.floor(player.getAlterWithAgeDays()) - Math.floor(playerCompare.getAlterWithAgeDays()));
 						if (playerCompare.isOld())
 							// Player was not in our team at compare date
 							playerExists = false;
 						else
 							// Player was in our team at compare date
 							playerExists = true;
 					}
 					return new ColorLabelEntry(
 							birthdays,
 							ageString,
 							player.getAlterWithAgeDays(),
 							playerExists,
 							ColorLabelEntry.BG_STANDARD,
 							true);
 				}
 			};
 
 			playerAdditionalArray[3] =new PlayerColumn(40,"BestePosition",100){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					
 					ColorLabelEntry tmp = new ColorLabelEntry(-SpielerPosition.getSortId(player
 											                            .getIdealPosition(),
 											                            false)
 											 - (player.getIdealPosStaerke(true) / 100.0f),
 											 SpielerPosition.getNameForPosition(player
 											                                    .getIdealPosition())
 											 + " ("
 											 + player.calcPosValue(player
 											                                 .getIdealPosition(),
 											                                 true) + ")",
 											 ColorLabelEntry.FG_STANDARD,
 											 ColorLabelEntry.BG_STANDARD, SwingConstants.LEFT);
 					
 					tmp.setIcon((player.getUserPosFlag() < 0)?Helper.ZAHNRAD:Helper.MANUELL);
 					return tmp;
 				}
 				public boolean isEditable(){
 					return false;
 				}
 				
 			}; 
 			
 			// Position
 			playerAdditionalArray[4] =new PlayerColumn(LINUP," ","Aufgestellt",28){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					final HOModel model = HOVerwaltung.instance().getModel();
 					if (model.getAufstellung().isSpielerAufgestellt(player
                             .getSpielerID())
                             	&& (model.getAufstellung().getPositionBySpielerId(player
                                   .getSpielerID()) != null)) {
 						return new ColorLabelEntry(Helper.getImage4Position(model.getAufstellung()
                        .getPositionBySpielerId(player.getSpielerID()),
                                                player.getTrikotnummer()),
                                                -model.getAufstellung()
                                                .getPositionBySpielerId(player
                                             		   .getSpielerID())
                                             		   .getSortId(),
                                             		   ColorLabelEntry.FG_STANDARD,
                                             		   ColorLabelEntry.BG_STANDARD, SwingConstants.CENTER);
 					} 
 
 					return new ColorLabelEntry(de.hattrickorganizer.tools.Helper
 												.getImage4Position(null,
 														player
 												           .getTrikotnummer()),
 												-player.getTrikotnummer() - 1000,
 												ColorLabelEntry.FG_STANDARD,
 												ColorLabelEntry.BG_STANDARD,
 												SwingConstants.CENTER);
 												}
 			}; 
 				
 			playerAdditionalArray[5] = new PlayerColumn(GROUP,"Gruppe",50){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					SmilieEntry smilieEntry = new SmilieEntry();
 					smilieEntry.setSpieler(player);
 					return  smilieEntry;
 				}
 			}; 
 				
 			playerAdditionalArray[6] = new PlayerColumn(70,"Status",50){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					SpielerStatusLabelEntry entry = new SpielerStatusLabelEntry();
 					entry.setSpieler(player);
 					return  entry;
 				}
 			}; 
 			
 
 			
			playerAdditionalArray[7] = new PlayerColumn(420,"Gehalt",100){
 			public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 				final String bonus = "";
 				final int gehalt = (int) (player.getGehalt() / gui.UserParameter.instance().faktorGeld);
 				final String gehalttext = Helper.getNumberFormat(true, 0).format(gehalt);
 				if(playerCompare == null){
 					return new DoppelLabelEntry(new ColorLabelEntry(gehalt,
                         gehalttext + bonus,
                         ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD,
                         SwingConstants.RIGHT),
                         new ColorLabelEntry("",
                         ColorLabelEntry.FG_STANDARD,
                         ColorLabelEntry.BG_STANDARD,
                         SwingConstants.RIGHT));
 				}
 
                 final int gehalt2 = (int) (playerCompare.getGehalt() / gui.UserParameter
                                                                            .instance().faktorGeld);
 				return new DoppelLabelEntry(new ColorLabelEntry(gehalt,
 									                        gehalttext + bonus,
 									                        ColorLabelEntry.FG_STANDARD,
 									                        ColorLabelEntry.BG_STANDARD,
 									                        SwingConstants.RIGHT),
 									                        new ColorLabelEntry(gehalt - gehalt2,
 									                        ColorLabelEntry.BG_STANDARD,
 									                        true, false, 0));
 			}
 			}; 
 			playerAdditionalArray[8] = new PlayerColumn(430,"TSI",0){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					final String text = Helper.getNumberFormat(false, 0).format(player.getTSI());
 					if(playerCompare == null){
 						return new DoppelLabelEntry(new ColorLabelEntry(player
                             .getTSI(),
                             text,
                             ColorLabelEntry.FG_STANDARD,
                             ColorLabelEntry.BG_STANDARD,
                             SwingConstants.RIGHT),
                             new ColorLabelEntry("",
                             ColorLabelEntry.FG_STANDARD,
                             ColorLabelEntry.BG_STANDARD,
                             SwingConstants.RIGHT));
 					}
 					
 					
 					return new DoppelLabelEntry(new ColorLabelEntry(player
                             .getTSI(),
                             text,
                             ColorLabelEntry.FG_STANDARD,
                             ColorLabelEntry.BG_STANDARD,
                             SwingConstants.RIGHT),
                             new ColorLabelEntry(player.getTSI()
                                    - playerCompare.getTSI(), ColorLabelEntry.BG_STANDARD,
                                    false, false, 0));
 				}
 				public void setSize(TableColumn column){
 					column.setMinWidth(Helper.calcCellWidth(90));
 					
 				}
 			};
 				
 			playerAdditionalArray[9] = new PlayerColumn(RATING,"Bewertung",50){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					if (player.getBewertung() > 0) {
 		                //Hat im letzen Spiel gespielt
 		                return new RatingTableEntry(player.getBewertung(), true);
 		            } 
 		            
 					return new RatingTableEntry(player.getLetzteBewertung(), false);
 		            
 				}
 				
 			};
 			
			playerAdditionalArray[10] = new PlayerColumn(436,"Marktwert",140){
 				public TableEntry getTableEntry(Spieler player,Spieler playerCompare){
 					IEPVData data = HOVerwaltung.instance().getModel().getEPV().getEPVData(player);
 					double price = HOVerwaltung.instance().getModel().getEPV().getPrice(data);
 					final String text = Helper.getNumberFormat(true, 0).format(price);
 
 					if(playerCompare == null){
 					
 					return new DoppelLabelEntry(new ColorLabelEntry(price,
                             text,
                             ColorLabelEntry.FG_STANDARD,
                             ColorLabelEntry.BG_STANDARD,
                             SwingConstants.RIGHT),
                             new ColorLabelEntry("",
                             ColorLabelEntry.FG_STANDARD,
                             ColorLabelEntry.BG_STANDARD,
                             SwingConstants.RIGHT));
 					
 					}
 					IEPVData comparedata = HOVerwaltung.instance().getModel().getEPV().getEPVData(playerCompare);
 					int htweek = HelperWrapper.instance().getHTWeek(playerCompare.getHrfDate());
 					double compareepv = HOVerwaltung.instance().getModel().getEPV().getPrice(comparedata, htweek);
 					return new DoppelLabelEntry(new ColorLabelEntry(price,
                             text,
                             ColorLabelEntry.FG_STANDARD,
                             ColorLabelEntry.BG_STANDARD,
                             SwingConstants.RIGHT),
                             new ColorLabelEntry((float)(price-compareepv),
                             		ColorLabelEntry.BG_STANDARD, 
                             		true, false, 0)
                             );
 				}
 			};
 			return playerAdditionalArray;
 
 			
 			
 			
 	}
 	
 }
