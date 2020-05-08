 // %3292955215:plugins%
 /*
  * IMatchHighlight.java
  *
  * Created on 6. September 2004, 09:04
  */
 package plugins;
 
 /**
  * Const
  *
  * @author volker.fischer
  */
 public interface IMatchHighlight {
     //~ Static fields/initializers -----------------------------------------------------------------
 
     //highlight-base-constants
 
     //Information
     public static final int HIGHLIGHT_INFORMATION = 0;
 
     //Success -> Goal
     public static final int HIGHLIGHT_ERFOLGREICH = 1;
 
     //Failure -> No Goal
     public static final int HIGHLIGHT_FEHLGESCHLAGEN = 2;
 
     //Special
     public static final int HIGHLIGHT_SPEZIAL = 3;
 
     //Cards
     public static final int HIGHLIGHT_KARTEN = 5;
 
     //highlight-sub-constants
     //Informal events (HIGHLIGHT_INFORMATION only)
 
     //formation, e.g. 3-4-3
     public static final int HIGHLIGHT_SUB_FORMATION = 20;
 
     //List of players
     public static final int HIGHLIGHT_SUB_SPIELER_AUFLISTUNG = 21;
 
     //Its a Derby (not in friendlies)
     public static final int HIGHLIGHT_SUB_DERBY = 25;
 
     //Neutral Ground (not in friendlies)
     public static final int HIGHLIGHT_SUB_NEUTRAL_GROUND= 26;
 
     //Rain
     public static final int HIGHLIGHT_SUB_REGEN = 30;
 
     //Clouded
     public static final int HIGHLIGHT_SUB_BEWOELKT = 31;
 
     //Fine
     public static final int HIGHLIGHT_SUB_SCHOEN = 32;
 
     //Sunny
     public static final int HIGHLIGHT_SUB_SONNIG = 33;
 
     //Ballcontrol
     public static final int HIGHLIGHT_SUB_BALLBESITZ = 40;
 
     //Best Player
     public static final int HIGHLIGHT_SUB_BESTER_SPIELER = 41;
 
     //Worst Player
     public static final int HIGHLIGHT_SUB_SCHLECHTESTER_SPIELER = 42;
 
     //Halftime ;)
     public static final int HIGHLIGHT_SUB_HALBZEIT = 45;
 
     //Hattrick
     public static final int HIGHLIGHT_SUB_HATTRICK = 46;
 
     //Equal Ballcontrol
     public static final int HIGHLIGHT_SUB_BALLBESITZ_AUSGEGLICHEN = 47;
 
     //Penalty Goal
     public static final int HIGHLIGHT_SUB_PENALTY_TOR = 56;
 
     //Penalty nervous Goal
     public static final int HIGHLIGHT_SUB_PENALTY_NERVOES_TOR = 57;
 
     //Penalty nervouse no Goal
     public static final int HIGHLIGHT_SUB_PENALTY_NERVOES_KEIN_TOR = 58;
 
     //Penalty Keeper get it
     public static final int HIGHLIGHT_SUB_PENALTY_TORHUETER_HAELT = 59;
 
     //Underestimated
     public static final int HIGHLIGHT_SUB_UNTERSCHAETZT = 60;
 
     //tactical problems
     public static final int HIGHLIGHT_SUB_TAKTISCHE_PROBLEME = 61;
 
     //keep lead
     public static final int HIGHLIGHT_SUB_FUEHRUNG_HALTEN = 62;
 
     //more concentrated
     public static final int HIGHLIGHT_SUB_KONZENTRIERTER = 63;
 
     //better organized
     public static final int HIGHLIGHT_SUB_BESSER_ORGANISIERT = 64;
 
     //missing experience
     public static final int HIGHLIGHT_SUB_FEHLENDE_ERFAHRUNG = 65;
 
     //pressing successfull
     public static final int HIGHLIGHT_SUB_PRESSING_ERFOLGREICH = 68;
 
     //extension
     public static final int HIGHLIGHT_SUB_VERLAENGERUNG = 70;
 
     //11m
     public static final int HIGHLIGHT_SUB_ELFMETERSCHIESSEN = 71;
 
     //won in extension
     public static final int HIGHLIGHT_SUB_SIEG_IN_VERLAENGERUNG = 72;
 
     //bruised
     public static final int HIGHLIGHT_SUB_PFLASTER = 90;
 
     //light injured
     public static final int HIGHLIGHT_SUB_VERLETZT_LEICHT = 91;
 
     //heavy injured
     public static final int HIGHLIGHT_SUB_VERLETZT_SCHWER = 92;
 
     //injured
     public static final int HIGHLIGHT_SUB_VERLETZT_KEIN_ERSATZ_EINS = 93;
 
     //bruised
     public static final int HIGHLIGHT_SUB_PFLASTER_BEHANDLUNG = 94;
 
     //injured
     public static final int HIGHLIGHT_SUB_VERLETZT = 95;
 
     //injured
     public static final int HIGHLIGHT_SUB_VERLETZT_KEIN_ERSATZ_ZWEI = 96;
 
     //Basic events (HIGHLIGHT_ERFOLGREICH and HIGHLIGHT_FEHLGESCHLAGEN only)
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_FREISTOSS = 0;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_DURCH_MITTE = 1;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_LINKS = 2;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_RECHTS = 3;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ELFMETER = 4;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_FREISTOSS_2 = 10;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_DURCH_MITTE_2 = 11;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_LINKS_2 = 12;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_RECHTS_2 = 13;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ELFMETER_2 = 14;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_FREISTOSS_3 = 20;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_DURCH_MITTE_3 = 21;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_LINKS_3 = 22;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_RECHTS_3 = 23;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ELFMETER_3 = 24;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_FREISTOSS_4 = 30;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_DURCH_MITTE_4 = 31;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_LINKS_4 = 32;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_RECHTS_4 = 33;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ELFMETER_4 = 34;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_FREISTOSS_5 = 50;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_DURCH_MITTE_5 = 51;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_LINKS_5 = 52;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_RECHTS_5 = 53;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ELFMETER_5 = 54;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_FREISTOSS_6 = 60;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_DURCH_MITTE_6 = 61;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_LINKS_6 = 62;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_RECHTS_6 = 63;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ELFMETER_6 = 64;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_FREISTOSS_7 = 70;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_DURCH_MITTE_7 = 71;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_LINKS_7 = 72;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_RECHTS_7 = 73;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ELFMETER_7 = 74;
 
    public static final int HIGHLIGHT_SUB_INDIRECT_FREEKICK_7 = 75;

    /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_FREISTOSS_8 = 80;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_DURCH_MITTE_8 = 81;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_LINKS_8 = 82;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UEBER_RECHTS_8 = 83;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ELFMETER_8 = 84;
 
    public static final int HIGHLIGHT_SUB_INDIRECT_FREEKICK_8 = 85;
 
     //Special events (HIGHLIGHT_ERFOLGREICH and HIGHLIGHT_FEHLGESCHLAGEN only)
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UNVORHERSEHBAR_PASS_VORLAGE_TOR = 5;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UNVORHERSEHBAR_PASS_ABGEFANGEN_TOR = 6;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_WEITSCHUSS_TOR = 7;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UNVORHERSEHBAR_BALL_ERKAEMPFT_TOR = 8;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UNVORHERSEHBAR_BALLVERLUST_TOR = 9;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_SCHNELLER_ANGREIFER_TOR = 15;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_SCHNELLER_ANGREIFER_PASS_TOR = 16;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_SCHLECHTE_KONDITION_BALLVERLUST_TOR = 17;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ECKBALL_TOR = 18;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ECKBALL_KOPFTOR = 19;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ERFAHRENER_ANGREIFER_TOR = 35;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_UNERFAHREN_TOR = 36;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_QUERPASS_TOR = 37;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_AUSSERGEWOEHNLICHER_PASS_TOR = 38;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_TECHNIKER_ANGREIFER_TOR = 39;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_KONTERANGRIFF_EINS = 40;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_KONTERANGRIFF_ZWEI = 41;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_KONTERANGRIFF_DREI = 42;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_KONTERANGRIFF_VIER = 43;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_KONTERANGRIFF_FUENF = 44;
 
     //Informal events (HIGHLIGHT_SPEZIAL only)
 
     /**
      * Negative weather SE for technical players on rainy days
      */
     public static final int HIGHLIGHT_SUB_PLAYER_TECHNICAL_RAINY = 1;
 
     /**
      * Positive weather SE for powerful players on rainy days
      */
     public static final int HIGHLIGHT_SUB_PLAYER_POWERFUL_RAINY = 2;
 
     /**
      * Positve weather SE for technical players on sunny days
      */
     public static final int HIGHLIGHT_SUB_PLAYER_TECHNICAL_SUNNY = 3;
 
     /**
      * Negative weather SE for powerful players on sunny days
      */
     public static final int HIGHLIGHT_SUB_PLAYER_POWERFUL_SUNNY = 4;
 
     /**
      * Negative weather SE for quick players on rainy days
      */
     public static final int HIGHLIGHT_SUB_PLAYER_QUICK_RAINY = 5;
 
     /**
      * Negative weather SE for quick players on sunny days
      */
     public static final int HIGHLIGHT_SUB_PLAYER_QUICK_SUNNY = 6;
 
     /**
      * Negative weather SE for powerful players on sunny days
      * @deprecated	use HIGHLIGHT_SUB_PLAYER_POWERFUL_SUNNY instead
      */
     public static final int HIGHLIGHT_SUB_SPIELER_KANN_SICH_NICHT_DURCHSETZEN = 4;
 
     /**
      * Negative weather SE for quick players on sunny days
      * @deprecated	use HIGHLIGHT_SUB_PLAYER_QUICK_SUNNY instead
      */
     public static final int HIGHLIGHT_SUB_SPIELER_MUEDE = 6;
 
     /** Tactic Pressing */
     public static final int HIGHLIGHT_SUB_SPIELT_PRESSING = 31;
 
     /** Tactic Counter Attack */
     public static final int HIGHLIGHT_SUB_SPIELT_KONTER = 32;
 
     /** Tactic Attack in the middle */
     public static final int HIGHLIGHT_SUB_SPIELT_DURCH_MITTE = 33;
 
     /** Tactic Attack over wings */
     public static final int HIGHLIGHT_SUB_SPIELT_UEBER_FLUEGEL = 34;
 
     //Informal events (HIGHLIGHT_KARTEN only)
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_GELB_HARTER_EINSATZ = 10;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_GELB_UNFAIR = 11;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_GELB_ROT_HARTER_EINSATZ = 12;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_GELB_ROT_UNFAIR = 13;
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_ROT = 14;
 
     //game over (HIGHLIGHT_KARTEN only)
 
     /** TODO Missing Parameter Documentation */
     public static final int HIGHLIGHT_SUB_SPIELENDE = 99;
 
     //walk over (HIGHLIGHT_KARTEN only)
     /** walkover - home team wins */
     public static final int HIGHLIGHT_SUB_WALKOVER_HOMETEAM_WINS = 1;
 
     /** walkover - home team wins */
     public static final int HIGHLIGHT_SUB_WALKOVER_AWAYTEAM_WINS = 2;
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Getter for property m_sEventText.
      *
      * @return Value of property m_sEventText.
      */
     public String getEventText();
 
     /**
      * Getter for property m_iGastTore.
      *
      * @return Value of property m_iGastTore.
      */
     public int getGastTore();
 
     /**
      * Getter for property m_sGehilfeHeim.
      *
      * @return Value of property m_sGehilfeHeim.
      */
     public boolean getGehilfeHeim();
 
     /**
      * Getter for property m_iGehilfeID.
      *
      * @return Value of property m_iGehilfeID.
      */
     public int getGehilfeID();
 
     /**
      * Getter for property m_sGehilfeName.
      *
      * @return Value of property m_sGehilfeName.
      */
     public String getGehilfeName();
 
     /**
      * Getter for property m_iHeimTore.
      *
      * @return Value of property m_iHeimTore.
      */
     public int getHeimTore();
 
     /**
      * Getter for property m_iHighlightSubTyp.
      *
      * @return Value of property m_iHighlightSubTyp.
      */
     public int getHighlightSubTyp();
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public int getHighlightTyp();
 
     /**
      * Getter for property m_iMinute.
      *
      * @return Value of property m_iMinute.
      */
     public int getMinute();
 
     /**
      * Getter for property m_sSpielerHeim.
      *
      * @return Value of property m_sSpielerHeim.
      */
     public boolean getSpielerHeim();
 
     /**
      * Getter for property m_iSpielerID.
      *
      * @return Value of property m_iSpielerID.
      */
     public int getSpielerID();
 
     /**
      * Getter for property m_sSpielerName.
      *
      * @return Value of property m_sSpielerName.
      */
     public String getSpielerName();
 
     /**
      * Getter for property m_iTeamID.
      *
      * @return Value of property m_iTeamID.
      */
     public int getTeamID();
 }
