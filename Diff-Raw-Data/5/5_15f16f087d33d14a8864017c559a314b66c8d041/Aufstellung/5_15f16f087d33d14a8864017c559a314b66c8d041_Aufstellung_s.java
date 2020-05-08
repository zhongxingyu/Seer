 // %2598623053:de.hattrickorganizer.model%
 /*
  * Aufstellung.java
  *
  * Created on 20. M�rz 2003, 14:35
  */
 package de.hattrickorganizer.model;
 
 import gui.UserParameter;
 
 import java.util.Vector;
 
 import plugins.ILineUp;
 import plugins.ISpieler;
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.database.DBZugriff;
 import de.hattrickorganizer.logik.Aufstellungsassistent;
 import de.hattrickorganizer.prediction.RatingPredictionConfig;
 import de.hattrickorganizer.prediction.RatingPredictionManager;
 import de.hattrickorganizer.tools.HOLogger;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author thomas.werth
  */
 public  class Aufstellung implements plugins.ILineUp {
 	//~ Static fields/initializers -----------------------------------------------------------------
 
     //Systeme
 
     /** TODO Missing Parameter Documentation */
     public static final String DEFAULT_NAME = "HO!";
 
     /** TODO Missing Parameter Documentation */
     public static final String DEFAULT_NAMELAST = "HO!LastLineup";
 
     /** TODO Missing Parameter Documentation */
     public static final int NO_HRF_VERBINDUNG = -1;
 
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** Aufstellungsassistent */
     private Aufstellungsassistent m_clAssi = new Aufstellungsassistent();
 
     /** h�lt die Positionen */
     private Vector m_vPositionen = new Vector();
 
     /** Attitude */
     private int m_iAttitude;
 
     //protected Vector    m_vSpieler      =   null;
 
     /** wer ist Kapit�n */
     private int m_iKapitaen = -1;
 
     /** wer schie�t Standards */
     private int m_iKicker = -1;
 
     /** TacticType */
     private int m_iTacticType;
 
     /** Home/Away/AwayDerby */
     private short m_sLocation = -1;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new Aufstellung object.
      */
     public Aufstellung() {
         initPositionen442();
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Konstruktor
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Creates a new instance of Aufstellung
      *
      * @param properties TODO Missing Constructuor Parameter Documentation
      *
      * @throws Exception TODO Missing Constructuor Exception Documentation
      */
     public Aufstellung(java.util.Properties properties) throws Exception {
         try {
             //Positionen erzeugen
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.keeper,
                                                   Integer.parseInt(properties.getProperty("keeper",
                                                                                           "0")),
                                                   (byte) 0));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightBack,
                                                   Integer.parseInt(properties.getProperty("rightback",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behrightback",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideBack1,
                                                   Integer.parseInt(properties.getProperty("insideback1",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behinsideback1",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideBack2,
                                                   Integer.parseInt(properties.getProperty("insideback2",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behinsideback2",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftBack,
                                                   Integer.parseInt(properties.getProperty("leftback",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behleftback",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightWinger,
                                                   Integer.parseInt(properties.getProperty("rightwinger",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behrightwinger",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideMid1,
                                                   Integer.parseInt(properties.getProperty("insidemid1",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behinsidemid1",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideMid2,
                                                   Integer.parseInt(properties.getProperty("insidemid2",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behinsidemid2",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftWinger,
                                                   Integer.parseInt(properties.getProperty("leftwinger",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behleftwinger",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.forward1,
                                                   Integer.parseInt(properties.getProperty("forward1",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behforward1",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.forward2,
                                                   Integer.parseInt(properties.getProperty("forward2",
                                                                                           "0")),
                                                   Byte.parseByte(properties.getProperty("behforward2",
                                                                                         "0"))));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.substBack,
                                                   Integer.parseInt(properties.getProperty("substback",
                                                                                           "0")),
                                                   (byte) 0));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.substInsideMid,
                                                   Integer.parseInt(properties.getProperty("substinsidemid",
                                                                                           "0")),
                                                   (byte) 0));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.substWinger,
                                                   Integer.parseInt(properties.getProperty("substwinger",
                                                                                           "0")),
                                                   (byte) 0));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.substKeeper,
                                                   Integer.parseInt(properties.getProperty("substkeeper",
                                                                                           "0")),
                                                   (byte) 0));
             m_vPositionen.add(new SpielerPosition(ISpielerPosition.substForward,
                                                   Integer.parseInt(properties.getProperty("substforward",
                                                                                           "0")),
                                                   (byte) 0));
             m_iTacticType = Integer.parseInt(properties.getProperty("tactictype", "0"));
             m_iAttitude = Integer.parseInt(properties.getProperty("installning", "0"));
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),"Aufstellung.<init1>: " + e);
             m_vPositionen.removeAllElements();
             initPositionen442();
         }
 
         try {
             //Kapit�n + kicker
             m_iKicker = Integer.parseInt(properties.getProperty("kicker1", "0"));
             m_iKapitaen = Integer.parseInt(properties.getProperty("captain", "0"));
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),"Aufstellung.<init2>: " + e);
         }
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * errechnet die Gesamt AW St�rke
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getAWTeamStk(Vector spieler, boolean mitForm) {
         float stk = 0.0f;
         stk += calcTeamStk(spieler, ISpielerPosition.INNENVERTEIDIGER, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.AUSSENVERTEIDIGER_OFF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.INNENVERTEIDIGER_OFF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.AUSSENVERTEIDIGER, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.INNENVERTEIDIGER_AUS, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.AUSSENVERTEIDIGER_IN, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.AUSSENVERTEIDIGER_DEF, mitForm);
 
         return de.hattrickorganizer.tools.Helper.round(stk, 1);
     }
 
     /**
      * ermittelt die Gesamt KonterStk der aufgestellten Spieler
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getAttackSTK() {
         return getAttackSTK(null);
     }
 
     /**
      * ermittelt die Gesamt KonterStk der aufgestellten Spieler
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getAttackSTK(Vector spieler) {
 
         return Math.max(1,
                         new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() ).getAow_AimRatings());
     }
 
     //    /**
     //     * Calculate the HO-Index for playing creatively
     //     * @author Thorsten Dietz
     //     * @param Vector spieler
     //     * @return tacticStrength
     //     */
     //    public float getCreativeSTK(Vector spieler){
     //    	float strength = 0f;
     //    	boolean isHeadmanInLineUp	= 	false;
     //    	for (int i = ISpielerPosition.rightBack; i < ISpielerPosition.beginnReservere; i++) {
     //            ISpieler player = HOVerwaltung.instance().getModel().getAufstellung()
     //                                          .getPlayerByPositionID(i);
     //
     //            if(player != null && player.getSpezialitaet() == ISpieler.KOPFBALLSTARK)
     //            	isHeadmanInLineUp = true;
     //            byte tactic = HOVerwaltung.instance().getModel().getAufstellung().getTactic4PositionID(i);
     //            strength+=PlayerHelper.getSpecialEventEffect(player,i,tactic);
     //
     //
     //    	}
     //    	if(isHeadmanInLineUp)
     //    		strength++;
     //    	return strength;
     //    }
 
     /**
      * Setter for property m_iAttitude.
      *
      * @param m_iAttitude New value of property m_iAttitude.
      */
     public final void setAttitude(int m_iAttitude) {
         this.m_iAttitude = m_iAttitude;
     }
 
     /**
      * Getter for property m_iAttitude.
      *
      * @return Value of property m_iAttitude.
      */
     public final int getAttitude() {
         return m_iAttitude;
     }
 
     /**
      * bestimmt automatisch den Kapit�n
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      */
     public final void setAutoKapitaen(Vector spieler) {
         Spieler player = null;
         float maxValue = -1;
 
         if (spieler == null) {
             spieler = HOVerwaltung.instance().getModel().getAllSpieler();
         }
 
         for (int i = 0; (spieler != null) && (i < spieler.size()); i++) {
             player = (Spieler) spieler.elementAt(i);
 
             if (m_clAssi.isSpielerInAnfangsElf(player.getSpielerID(), m_vPositionen)) {
                 if (maxValue < player.calcKapitaensValue()) {
                     maxValue = player.calcKapitaensValue();
                     m_iKapitaen = player.getSpielerID();
                 }
             }
         }
     }
 
     /**
      * bestimmt den Standard sch�tzen
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      */
     public final void setAutoKicker(Vector spieler) {
         int maxStandard = -1;
         int form = -1;
         Spieler player = null;
 
         if (spieler == null) {
             spieler = HOVerwaltung.instance().getModel().getAllSpieler();
         }
 
         for (int i = 0; (spieler != null) && (i < spieler.size()); i++) {
             player = (Spieler) spieler.elementAt(i);
 
             if (m_clAssi.isSpielerInAnfangsElf(player.getSpielerID(), m_vPositionen)) {
                 if (player.getStandards() > maxStandard) {
                     maxStandard = player.getStandards();
                     form = player.getForm();
                     m_iKicker = player.getSpielerID();
                 } else if ((player.getStandards() == maxStandard) && (form < player.getForm())) {
                     maxStandard = player.getStandards();
                     form = player.getForm();
                     m_iKicker = player.getSpielerID();
                 }
             }
         }
     }
 
     /**
      * returns average Expierence of all players in lineup
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getAvgExpierence() {
         Vector spieler;
         Spieler player = null;
         float value = 0;
         int numPlayers = 0;
 
         spieler = HOVerwaltung.instance().getModel().getAllSpieler();
 
         for (int i = 0; (spieler != null) && (i < spieler.size()); i++) {
             player = (Spieler) spieler.elementAt(i);
 
             if (m_clAssi.isSpielerInAnfangsElf(player.getSpielerID(), m_vPositionen)) {
                 value += player.getErfahrung();
                 ++numPlayers;
             }
         }
 
         value /= numPlayers;
         return value;
     }
 
     /**
      * Get the average experience of all players in lineup
      * using the formula from kopsterkespits:
      * teamxp = ((sum of teamxp + xp of captain)/12)*(1-(7-leadership of captain)*5%)
      */
     public final float getAverageExperience() {
     	float value = 0;
     	try {
     		Spieler pl = null;
     		Spieler captain = null;
     		Vector players = HOVerwaltung.instance().getModel().getAllSpieler();
 
     		for (int i = 0; (players != null) && (i < players.size()); i++) {
     			pl = (Spieler) players.elementAt(i);
     			if (m_clAssi.isSpielerInAnfangsElf(pl.getSpielerID(), m_vPositionen)) {
     				value += pl.getErfahrung();
     				if (m_iKapitaen == pl.getSpielerID()) {
     					captain = pl;
     				}
     			}
     		}
     		if (captain != null) {
    			value = ((float)(value + pl.getErfahrung())/12) * (1f-(float)(7-captain.getFuehrung())*0.05f);
     		} else {
     			HOLogger.instance().log(getClass(), "Can't calc average experience, captain not set.");
     			value = -1f;
     		}
     	} catch (Exception e) {
     		HOLogger.instance().error(getClass(), e);
     	}
         return value;
     }
 
     /**
      * errechnet anhand der aktuellen Aufstellung die besten Elfersch�tzen
      *
      * @return TODO Missing Return Method Documentation
      */
     public final int[] getBestElferKicker() {
         return m_clAssi.setElferKicker(HOVerwaltung.instance().getModel().getAllSpieler(),
                                        m_vPositionen);
     }
 
     /**
      * Predicts Central Attack-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getCentralAttackRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 	        //ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 	        double value = Math.max(1, rpManager.getCentralAttackRatings());
 	        if (value>1) {
 	        	value += UserParameter.instance().middleAttackOffset;
 	        }
 	        return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * Predicts cd-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getCentralDefenseRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 	        //ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getCentralDefenseRatings());
 			if (value>1) {
 				value += UserParameter.instance().middleDefenceOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
     * errechnet die Gesamt St�rke
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getGesamtStaerke(Vector spieler, boolean mitForm) {
         return de.hattrickorganizer.tools.Helper.round(getTWTeamStk(spieler, mitForm)
                                                        + getAWTeamStk(spieler, mitForm)
                                                        + getMFTeamStk(spieler, mitForm)
                                                        + getSTTeamStk(spieler, mitForm), 1);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final int getHATStats() {
 		int sum;
 		final int MFfactor = 3;
 
 		sum = HTfloat2int(getMidfieldRating()) * MFfactor;
 
 		sum += HTfloat2int(getLeftDefenseRating());
 		sum += HTfloat2int(getCentralDefenseRating());
 		sum += HTfloat2int(getRightDefenseRating());
 
 		sum += HTfloat2int(getLeftAttackRating());
 		sum += HTfloat2int(getCentralAttackRating());
 		sum += HTfloat2int(getRightAttackRating());
 
 		return sum;
     }
 
     /**
      * Setter for property m_iKapitaen.
      *
      * @param m_iKapitaen New value of property m_iKapitaen.
      */
     public final void setKapitaen(int m_iKapitaen) {
         this.m_iKapitaen = m_iKapitaen;
     }
 
     /**
      * Getter for property m_iKapitaen.
      *
      * @return Value of property m_iKapitaen.
      */
     public final int getKapitaen() {
         return m_iKapitaen;
     }
 
     /**
      * Setter for property m_iKicker.
      *
      * @param m_iKicker New value of property m_iKicker.
      */
     public final void setKicker(int m_iKicker) {
         this.m_iKicker = m_iKicker;
     }
 
     /**
      * Getter for property m_iKicker.
      *
      * @return Value of property m_iKicker.
      */
     public final int getKicker() {
         return m_iKicker;
     }
 
     /**
      * ermittelt die Gesamt KonterStk der aufgestellten Spieler
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getKonterSTK() {
         return getKonterSTK(null /* HOVerwaltung.instance ().getModel ().getAllSpieler ()*/);
     }
 
     /**
      * ermittelt die Gesamt KonterStk der aufgestellten Spieler
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getKonterSTK(Vector spieler) {
         return Math.max(1,
         		new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() ).getKonterRatings() );
     }
 
     /**
      * Predicts LeftAttack-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getLeftAttackRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getLeftAttackRatings());
 			if (value>1) {
 				value += UserParameter.instance().leftAttackOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * Predicts ld-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getLeftDefenseRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getLeftDefenseRatings());
 			if (value>1) {
 				value += UserParameter.instance().leftDefenceOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getLoddarStats() {
 		// Version 3.1 formulars (gives values between 1....80), http://oeufi-foed.doesel.ch/LoddarStats/LoddarStats-Inside-3.htm
 		float Abwehrstaerke;
 		float Angriffsstaerke;
 		float MFF;
 		float KK;
 		float KZG;
 		float KAG;
 		float AG;
 		float AF;
 		float CA;
 		float AoW;
 		float AiM;
 
 		// constants
 		// float MFS=0.2f, VF=0.45f, ZG=0.5f, KG=0.25f;
 		// updated to V3.2
 		final float MFS = 0.0f;
 
 		// constants
 		// float MFS=0.2f, VF=0.45f, ZG=0.5f, KG=0.25f;
 		// updated to V3.2
 		final float VF = 0.47f;
 
 		// constants
 		// float MFS=0.2f, VF=0.45f, ZG=0.5f, KG=0.25f;
 		// updated to V3.2
 		float ZG = 0.37f;
 
 		// constants
 		// float MFS=0.2f, VF=0.45f, ZG=0.5f, KG=0.25f;
 		// updated to V3.2
 		final float KG = 0.25f;
 
 		MFF = MFS + ((1.0f - MFS) * HQ(getMidfieldRating()));
 
 		AG = (1.0f - ZG) / 2.0f;
 		Abwehrstaerke =
 			VF
 				* ((ZG * HQ(getCentralDefenseRating()))
 					+ (AG * (HQ(getLeftDefenseRating()) + HQ(getRightDefenseRating()))));
 
 		// AiM or AoW or CA?
 		if (getTacticType() == plugins.IMatchDetails.TAKTIK_MIDDLE) {
 			AiM = (float) HTfloat2int(getAttackSTK());
 			KZG = ZG + (((0.2f * (AiM - 1.0f)) / 19.0f) + 0.2f);
 			KK = 0.0f;
 		} else if (getTacticType() == plugins.IMatchDetails.TAKTIK_WINGS) {
 			AoW = (float) HTfloat2int(getAttackSTK());
 			KZG = ZG - (((0.2f * (AoW - 1.0f)) / 19.0f) + 0.2f);
 			KK = 0.0f;
 		} else if (getTacticType() == plugins.IMatchDetails.TAKTIK_KONTER) {
 			CA = (float) HTfloat2int(getKonterSTK());
 			KK = KG * 2.0f * (CA / (CA + 20.0f));
 			KZG = ZG;
 		} else {
 			KZG = ZG;
 			KK = 0.0f;
 		}
 
 		KAG = (1.0f - KZG) / 2.0f;
 		AF = 1.0f - VF;
 		Angriffsstaerke =
 			(AF + KK)
 				* ((KZG * HQ(getCentralAttackRating()))
 					+ (KAG * (HQ(getLeftAttackRating()) + HQ(getRightAttackRating()))));
 
 		return 80.0f * (MFF * (Abwehrstaerke + Angriffsstaerke));
     }
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param x TODO Missing Method Parameter Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public final float HQ(double x) {
 		// first convert to original HT rating (1...80)
 		x = (float) HTfloat2int(x);
 
 		// and now LoddarStats Hattrick-Quality function (?)
 		double v = (2.0f * x) / (x + 80.0f);
 		return (float) v;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param x TODO Missing Method Parameter Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public final int HTfloat2int(double x) {
 		// convert reduced float rating (1.00....20.99) to original integer HT rating (1...80)
 		// one +0.5 is because of correct rounding to integer
 		return (int) (((x - 1.0f) * 4.0f) + 1.0f);
 	}
     /**
      * errechnet die Gesamt MF St�rke
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getMFTeamStk(Vector spieler, boolean mitForm) {
         float stk = 0.0f;
         stk += calcTeamStk(spieler, ISpielerPosition.MITTELFELD, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.FLUEGELSPIEL, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.MITTELFELD_OFF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.FLUEGELSPIEL_OFF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.MITTELFELD_DEF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.FLUEGELSPIEL_DEF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.MITTELFELD_AUS, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.FLUEGELSPIEL_IN, mitForm);
 
         return de.hattrickorganizer.tools.Helper.round(stk, 1);
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Ratings
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Predicts MF-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getMidfieldRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getMFRatings());
 			if (value>1) {
 				value += UserParameter.instance().midfieldOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * Gibt den Namen f�r das System zur�ck
      *
      * @param system TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public static String getNameForSystem(byte system) {
         String name;
 
         switch (system) {
             case SYS_451:
                 name = "4-5-1";
                 break;
 
             case SYS_352:
                 name = "3-5-2";
                 break;
 
             case SYS_442:
                 name = "4-4-2";
                 break;
 
             case SYS_343:
                 name = "3-4-3";
                 break;
 
             case SYS_433:
                 name = "4-3-3";
                 break;
 
             case SYS_532:
                 name = "5-3-2";
                 break;
 
             case SYS_541:
                 name = "5-4-1";
                 break;
 
             default:
                 name = de.hattrickorganizer.model.HOVerwaltung.instance().getResource().getProperty("Unbestimmt");
                 break;
         }
 
         return name;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param positionsid TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final byte getEffectivePos4PositionID(int positionsid) {
         try {
             return getPositionById(positionsid).getPosition();
         } catch (Exception e) {
             return ISpielerPosition.UNBESTIMMT;
         }
     }
 
     /**
      * Setter for property m_sHeimspiel.
      * @param m_sHeimspiel New value of property m_sHeimspiel.
      */
     public final void setHeimspiel(short location) {
         this.m_sLocation = location;
     }
 
     /**
      * Get the location constant for the match (home/away/awayderby)
      * TODO: determine away derby (aik) !!!
      * @return the location constant for the match
      */
     public final short getHeimspiel() {
         if (m_sLocation < 0) {
             try {
                 final plugins.IMatchKurzInfo[] matches = DBZugriff.instance().getMatchesKurzInfo(
                 		HOVerwaltung.instance().getModel().getBasics().getTeamId());
                 plugins.IMatchKurzInfo match = null;
 
                 for (int i = 0; (matches != null) && (matches.length > i); i++) {
                     if ((matches[i].getMatchStatus() == plugins.IMatchKurzInfo.UPCOMING)
                         && ((match == null)
                         || match.getMatchDateAsTimestamp().after(matches[i].getMatchDateAsTimestamp()))) {
                         match = matches[i];
                     }
                 }
 
                 m_sLocation = (match.getHeimID() == HOVerwaltung.instance().getModel().getBasics()
                                                                  .getTeamId()) ? (short) 1 : (short) 0;
             } catch (Exception e) {
             	m_sLocation = 0;
             }
         }
 
         return m_sLocation;
     }
 
     /* Umrechnung von double auf 1-80 int*/
     public final int getIntValue4Rating(double rating) {
         return (int) (((float) (rating - 1) * 4f) + 1);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param positionsid TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public plugins.ISpieler getPlayerByPositionID(int positionsid) {
         try {
             return HOVerwaltung.instance().getModel().getSpieler(getPositionById(positionsid)
                                                                      .getSpielerId());
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Gibt die Spielerposition zu der Id zur�ck
      *
      * @param id TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final SpielerPosition getPositionById(int id) {
         for (int i = 0; i < m_vPositionen.size(); i++) {
             if (((SpielerPosition) m_vPositionen.get(i)).getId() == id) {
                 return (SpielerPosition) m_vPositionen.get(i);
             }
         }
 
         //Nix gefunden
         return null;
     }
 
     /**
      * Gibt die Spielerposition zu der SpielerId zur�ck
      *
      * @param spielerid TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final SpielerPosition getPositionBySpielerId(int spielerid) {
         for (int i = 0; i < m_vPositionen.size(); i++) {
             if (((SpielerPosition) m_vPositionen.get(i)).getSpielerId() == spielerid) {
                 return (SpielerPosition) m_vPositionen.get(i);
             }
         }
 
         //Nix gefunden
         return null;
     }
 
     /**
      * Setter for property m_vPositionen.
      *
      * @param m_vPositionen New value of property m_vPositionen.
      */
     public final void setPositionen(java.util.Vector m_vPositionen) {
         this.m_vPositionen = m_vPositionen;
 
         //m_clAssi.setPositionen ( m_vPositionen );
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Accessor
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Getter for property m_vPositionen.
      *
      * @return Value of property m_vPositionen.
      */
     public final java.util.Vector getPositionen() {
         return m_vPositionen;
     }
 
     /**
      * ermittelt die Gesamt PressingStk der aufgestellten Spieler original Formel von Pressing
      * Spezialist
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getPressingSTK() {
         return getPressingSTK(HOVerwaltung.instance().getModel().getAllSpieler());
     }
 
     /**
      * ermittelt die Gesamt PressingStk der aufgestellten Spieler original Formel von Pressing
      * Spezialist
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getPressingSTK(Vector spieler) {
         float pressing = 0.0f;
         SpielerPosition pos = null;
         int spez = -1;
         float defense = 0.0f;
         float kondi = 0.0f;
         float form = 0.0f;
         float exp = 0.0f;
         int anzSpieler = 0;
 
         for (int i = 0; i < spieler.size(); i++) {
         	ISpieler player = (ISpieler) spieler.elementAt(i);
             spez = player.getSpezialitaet();
             pos = getPositionBySpielerId(player.getSpielerID());
 
             //nur jene Ber�cksichtigen die auch aufgestellt sind
             if ((pos != null)
                 && (pos.getId() < ISpielerPosition.beginnReservere)
                 && (pos.getPosition() != ISpielerPosition.TORWART)) {
                 //Verteidigung
                 //durchsetzungsstark z�hlt doppelt
                 if (spez == 3) {
                     defense += 2 * player.getVerteidigung();
                 } else {
                     defense += player.getVerteidigung();
                 }
 
                 //Erfahrung
                 exp += player.getErfahrungsBonus(player.getVerteidigung());
 
                 //Kondition
                 kondi += player.getKondition();
 
                 //Form
                 form += player.getForm();
                 anzSpieler++;
             }
         }
 
         //pressing = gesamtDefense/8 * Avg Kondi / 7 * Avg Form / 11 +
         pressing = ((defense / 8.0f) * ((kondi / (float) anzSpieler) / 7.0f) * ((form / (float) anzSpieler) / 11))
                    + ((exp / (float) anzSpieler) / 10);
 
         //pressing = Math.round ( pressing );
         return pressing;
     }
 
     /**
      * Predicts Right-Attack-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getRightAttackRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getRightAttackRatings());
 			if (value>1) {
 				value += UserParameter.instance().rightAttackOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * Predicts rd-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getRightDefenseRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getRightDefenseRatings());
 			if (value>1) {
 				value += UserParameter.instance().rightDefenceOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * errechnet die Gesamt ST St�rke
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getSTTeamStk(Vector spieler, boolean mitForm) {
         float stk = 0.0f;
         stk += calcTeamStk(spieler, ISpielerPosition.STURM, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.STURM_DEF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.STURM_AUS, mitForm);
 
         return de.hattrickorganizer.tools.Helper.round(stk, 1);
     }
 
     /**
      * Setzt einen Spieler in eine Position und sorgt daf�r, da� er nicht noch woanders
      * aufgestellt ist
      *
      * @param positionsid TODO Missing Constructuor Parameter Documentation
      * @param spielerid TODO Missing Constructuor Parameter Documentation
      * @param tactic TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final byte setSpielerAtPosition(int positionsid, int spielerid, byte tactic) {
         final SpielerPosition pos = getPositionById(positionsid);
 
         if (pos != null) {
             setSpielerAtPosition(positionsid, spielerid);
             pos.setTaktik(tactic);
 
             return pos.getPosition();
         }
 
         return ISpielerPosition.UNBESTIMMT;
     }
 
     /**
      * Setzt einen Spieler in eine Position und sorgt daf�r, da� er nicht noch woanders
      * aufgestellt ist
      *
      * @param positionsid TODO Missing Constructuor Parameter Documentation
      * @param spielerid TODO Missing Constructuor Parameter Documentation
      */
     public final void setSpielerAtPosition(int positionsid, int spielerid) {
         //Ist der Spieler noch aufgestellt?
         if (this.isSpielerAufgestellt(spielerid)) {
             //Den Spieler an der alten Position entfernen
             for (int i = 0; i < m_vPositionen.size(); i++) {
                 if (((SpielerPosition) m_vPositionen.get(i)).getSpielerId() == spielerid) {
                     //Spieler entfernen
                     ((SpielerPosition) m_vPositionen.get(i)).setSpielerId(0);
                 }
             }
         }
 
         //Spieler an die neue Position setzten
         final SpielerPosition position = getPositionById(positionsid);
         position.setSpielerId(spielerid);
 
         //Ist der Spielf�hrer und der Kicker noch aufgestellt?
         if (!isSpielerAufgestellt(m_iKapitaen)) {
             //Spielf�hrer entfernen
             m_iKapitaen = 0;
         }
 
         if (!isSpielerAufgestellt(m_iKicker)) {
             //Spielf�hrer entfernen
             m_iKicker = 0;
         }
     }
 
     /**
      * ist der SPieler aufgestellt
      *
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerAufgestellt(int spielerId) {
         return m_clAssi.isSpielerAufgestellt(spielerId, m_vPositionen);
     }
 
     /**
      * spielt der Spieler von Beginn an
      *
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerInAnfangsElf(int spielerId) {
         return m_clAssi.isSpielerInAnfangsElf(spielerId, m_vPositionen);
     }
 
     /**
      * sitzt der SPierl auf der Bank
      *
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerInReserve(int spielerId) {
         return (m_clAssi.isSpielerAufgestellt(spielerId, m_vPositionen)
                && !m_clAssi.isSpielerInAnfangsElf(spielerId, m_vPositionen));
     }
 
     /**
      * Gibt den Namen f�r das System zur�ck
      *
      * @param system TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final String getSystemName(byte system) {
         return getNameForSystem(system);
     }
 
     /**
      * errechnet die Gesamt TW St�rke
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getTWTeamStk(Vector spieler, boolean mitForm) {
         return calcTeamStk(spieler, ISpielerPosition.TORWART, mitForm);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param positionsid TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final byte getTactic4PositionID(int positionsid) {
         try {
             return getPositionById(positionsid).getTaktik();
         } catch (Exception e) {
             return plugins.ISpielerPosition.UNBESTIMMT;
         }
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    STK Funcs
     /////////////////////////////////////////////////////////////////////////////////
     public final float getTacticLevel(int type) {
         float value = 0.0f;
 
         switch (type) {
             case plugins.IMatchDetails.TAKTIK_PRESSING:
                 value = getPressingSTK();
                 break;
 
             case plugins.IMatchDetails.TAKTIK_KONTER:
                 value = getKonterSTK();
                 break;
 
             case plugins.IMatchDetails.TAKTIK_MIDDLE:
                 value = getAttackSTK();
                 break;
 
             case plugins.IMatchDetails.TAKTIK_WINGS:
                 value = getAttackSTK();
                 break;
         }
 
         return value;
     }
 
     /**
      * Setter for property m_iTacticType.
      *
      * @param m_iTacticType New value of property m_iTacticType.
      */
     public final void setTacticType(int m_iTacticType) {
         this.m_iTacticType = m_iTacticType;
     }
 
     /**
      * Getter for property m_iTacticType.
      *
      * @return Value of property m_iTacticType.
      */
     public final int getTacticType() {
         return m_iTacticType;
     }
 
     /**
      * liefert die Team erfahrung des Systems
      *
      * @return ERfahrung -1 wenn unsinniges System gew�hlt ist
      */
     public final int getTeamErfahrung4AktuellesSystem() {
         int erfahrung = -1;
 
         switch (ermittelSystem()) {
             case SYS_MURKS:
                 erfahrung = -1;
                 break;
 
             case SYS_451:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung451();
                 break;
 
             case SYS_352:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung352();
                 break;
 
             case SYS_442:
                 erfahrung = ISpieler.sehr_gut;
                 break;
 
             case SYS_343:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung343();
                 break;
 
             case SYS_433:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung433();
                 break;
 
             case SYS_532:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung532();
                 break;
 
             case SYS_541:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung541();
                 break;
         }
 
         return erfahrung;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param name TODO Missing Method Parameter Documentation
      */
     public final void AufstellungsSystemLoeschen(String name) {
         de.hattrickorganizer.database.DBZugriff.instance().deleteSystem(NO_HRF_VERBINDUNG, name);
     }
 
     /**
      * pr�ft ob die aufgestellten Spieler noch implements KAder sind
      */
     public final void checkAufgestellteSpieler() {
         SpielerPosition pos = null;
 
         for (int i = 0; (m_vPositionen != null) && (i < m_vPositionen.size()); i++) {
             pos = (SpielerPosition) m_vPositionen.elementAt(i);
 
             //existiert Spieler noch ?
             if ((HOVerwaltung.instance().getModel() != null)
                 && (HOVerwaltung.instance().getModel().getSpieler(pos.getSpielerId()) == null)) {
                 //nein dann zuweisung aufheben
                 pos.setSpielerId(0);
             }
         }
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Aktions Funcs
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * erstellt die automatische Aufstellung
      *
      * @param spieler die aufszustellenden Spieler
      * @param reihenfolge Reihenfolge in der die Mannschaftsteile besetzt werden sollen
      * @param mitForm Formber�cksichtigung
      * @param idealPosFirst IdealPosition ber�cksichtigen ?
      * @param ignoreVerletzung TODO Missing Constructuor Parameter Documentation
      * @param ignoreSperren TODO Missing Constructuor Parameter Documentation
      * @param wetterBonus TODO Missing Constructuor Parameter Documentation
      * @param wetter TODO Missing Constructuor Parameter Documentation
      */
     public final void doAufstellung(Vector spieler, byte reihenfolge, boolean mitForm,
                                     boolean idealPosFirst, boolean ignoreVerletzung,
                                     boolean ignoreSperren, float wetterBonus, int wetter) {
         m_clAssi.doAufstellung(m_vPositionen, spieler, reihenfolge, mitForm, idealPosFirst,
                                ignoreVerletzung, ignoreSperren, wetterBonus, wetter);
         setAutoKicker(null);
         setAutoKapitaen(null);
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Debug Funcs
     /////////////////////////////////////////////////////////////////////////////////
     public final void dump() {
         HOLogger.instance().log(getClass(),"Std-aufstellung");
         dumpValues();
         HOLogger.instance().log(getClass(),"idelaPos");
 
         //3-5-2
         initPositionen352();
 
         //353 mit idealpos first
         doAufstellung(HOVerwaltung.instance().getModel().getAllSpieler(),
                       ILineUp.MF_AW_ST, true, true, false,
                       false, 0.2f, ISpieler.LEICHTBEWOELKT);
 
         //dumpen
         dumpValues();
         HOLogger.instance().log(getClass(),"Ohne idelaPos");
 
         //3-5-2 aufgestellte SPieler leeren
         resetAufgestellteSpieler();
 
         //353 mit idealpos first
         doAufstellung(HOVerwaltung.instance().getModel().getAllSpieler(),
                       ILineUp.MF_AW_ST, true, false,
                       false, false, 0.2f, ISpieler.LEICHTBEWOELKT);
         dumpValues();
     }
 
     /**
      * Cloned diese Aufstellung, erzeugt neues Objekt
      *
      * @return TODO Missing Return Method Documentation
      */
     public final Aufstellung duplicate() {
         final java.util.Properties properties = new java.util.Properties();
         Aufstellung clone = null;
 
         try {
             properties.setProperty("keeper",
                                    getPositionById(ISpielerPosition.keeper).getSpielerId() + "");
             properties.setProperty("rightback",
                                    getPositionById(ISpielerPosition.rightBack).getSpielerId() + "");
             properties.setProperty("insideback1",
                                    getPositionById(ISpielerPosition.insideBack1).getSpielerId()
                                    + "");
             properties.setProperty("insideback2",
                                    getPositionById(ISpielerPosition.insideBack2).getSpielerId()
                                    + "");
             properties.setProperty("leftback",
                                    getPositionById(ISpielerPosition.leftBack).getSpielerId() + "");
             properties.setProperty("rightwinger",
                                    getPositionById(ISpielerPosition.rightWinger).getSpielerId()
                                    + "");
             properties.setProperty("insidemid1",
                                    getPositionById(ISpielerPosition.insideMid1).getSpielerId() + "");
             properties.setProperty("insidemid2",
                                    getPositionById(ISpielerPosition.insideMid2).getSpielerId() + "");
             properties.setProperty("leftwinger",
                                    getPositionById(ISpielerPosition.leftWinger).getSpielerId() + "");
             properties.setProperty("forward1",
                                    getPositionById(ISpielerPosition.forward1).getSpielerId() + "");
             properties.setProperty("forward2",
                                    getPositionById(ISpielerPosition.forward2).getSpielerId() + "");
             properties.setProperty("substback",
                                    getPositionById(ISpielerPosition.substBack).getSpielerId() + "");
             properties.setProperty("substinsidemid",
                                    getPositionById(ISpielerPosition.substInsideMid).getSpielerId()
                                    + "");
             properties.setProperty("substwinger",
                                    getPositionById(ISpielerPosition.substWinger).getSpielerId()
                                    + "");
             properties.setProperty("substkeeper",
                                    getPositionById(ISpielerPosition.substKeeper).getSpielerId()
                                    + "");
             properties.setProperty("substforward",
                                    getPositionById(ISpielerPosition.substForward).getSpielerId()
                                    + "");
 
             properties.setProperty("behrightback",
                                    getPositionById(ISpielerPosition.rightBack).getTaktik() + "");
             properties.setProperty("behinsideback1",
                                    getPositionById(ISpielerPosition.insideBack1).getTaktik() + "");
             properties.setProperty("behinsideback2",
                                    getPositionById(ISpielerPosition.insideBack2).getTaktik() + "");
             properties.setProperty("behleftback",
                                    getPositionById(ISpielerPosition.leftBack).getTaktik() + "");
             properties.setProperty("behrightwinger",
                                    getPositionById(ISpielerPosition.rightWinger).getTaktik() + "");
             properties.setProperty("behinsidemid1",
                                    getPositionById(ISpielerPosition.insideMid1).getTaktik() + "");
             properties.setProperty("behinsidemid2",
                                    getPositionById(ISpielerPosition.insideMid2).getTaktik() + "");
             properties.setProperty("behleftwinger",
                                    getPositionById(ISpielerPosition.leftWinger).getTaktik() + "");
             properties.setProperty("behforward1",
                                    getPositionById(ISpielerPosition.forward1).getTaktik() + "");
             properties.setProperty("behforward2",
                                    getPositionById(ISpielerPosition.forward2).getTaktik() + "");
 
             properties.setProperty("kicker1", getKicker() + "");
             properties.setProperty("captain", getKapitaen() + "");
 
             properties.setProperty("tactictype", getTacticType() + "");
             properties.setProperty("installning", getAttitude() + "");
 
             clone = new Aufstellung(properties);
             clone.setHeimspiel(getHeimspiel());
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),"Aufstellung.duplicate: " + e);
         }
 
         return clone;
     }
 
     /**
      * ermittelt das aktuelle System
      *
      * @return TODO Missing Return Method Documentation
      */
     public final byte ermittelSystem() {
         final int abw = getAnzAbwehr();
         final int mf = getAnzMittelfeld();
 
         //int st  =   getAnzSturm();
         //343
         if (abw == 3) {
             if (mf == 4) {
                 return SYS_343;
             } //352
             else if (mf == 5) {
                 return SYS_352;
             }
             //MURKS
             else {
                 return SYS_MURKS;
             }
         } else if (abw == 4) {
             //433
             if (mf == 3) {
                 return SYS_433;
             } //442
             else if (mf == 4) {
                 return SYS_442;
             } //451
             else if (mf == 5) {
                 return SYS_451;
             }
             //MURKS
             else {
                 return SYS_MURKS;
             }
         } else if (abw == 5) {
             //532
             if (mf == 3) {
                 return SYS_532;
             } //541
             else if (mf == 4) {
                 return SYS_541;
             }
             //MURKS
             else {
                 return SYS_MURKS;
             }
         } //MURKS
         else {
             return SYS_MURKS;
         }
 
         //  return SYS_MURKS;
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public final void flipSide() {
         Vector tmp = new Vector(m_vPositionen);
         m_vPositionen.removeAllElements();
         m_vPositionen.add(tmp.get(0));
         m_vPositionen.add(swap(tmp.get(1), tmp.get(4)));
         m_vPositionen.add(swap(tmp.get(2), tmp.get(3)));
         m_vPositionen.add(swap(tmp.get(3), tmp.get(2)));
         m_vPositionen.add(swap(tmp.get(4), tmp.get(1)));
         m_vPositionen.add(swap(tmp.get(5), tmp.get(8)));
         m_vPositionen.add(swap(tmp.get(6), tmp.get(7)));
         m_vPositionen.add(swap(tmp.get(7), tmp.get(6)));
         m_vPositionen.add(swap(tmp.get(8), tmp.get(5)));
         m_vPositionen.add(swap(tmp.get(9), tmp.get(10)));
         m_vPositionen.add(swap(tmp.get(10), tmp.get(9)));
         m_vPositionen.add(tmp.get(11));
         m_vPositionen.add(tmp.get(12));
         m_vPositionen.add(tmp.get(13));
         m_vPositionen.add(tmp.get(14));
         m_vPositionen.add(tmp.get(15));
         tmp = null;
     }
 
     /**
      * l�dt dei Aufstellung
      *
      * @param name TODO Missing Constructuor Parameter Documentation
      */
     public final void load(String name) {
         final Aufstellung temp = de.hattrickorganizer.database.DBZugriff.instance().getAufstellung(NO_HRF_VERBINDUNG,
                                                                                                    name);
 
         m_vPositionen = null;
         m_vPositionen = temp.getPositionen();
         m_iKicker = temp.getKicker();
         m_iKapitaen = temp.getKapitaen();
     }
 
     /**
      * l�dt dei Aufstellung
      */
     public final void load4HRF() {
         final Aufstellung temp = de.hattrickorganizer.database.DBZugriff.instance().getAufstellung(HOVerwaltung.instance()
                                                                                                                .getModel()
                                                                                                                .getID(),
                                                                                                    "HRF");
 
         m_vPositionen = null;
         m_vPositionen = temp.getPositionen();
         m_iKicker = temp.getKicker();
         m_iKapitaen = temp.getKapitaen();
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Datenbank Funcs
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * L�dt ein System aus der Datenbank ein
      *
      * @param name TODO Missing Constructuor Parameter Documentation
      */
     public final void loadAufstellungsSystem(String name) {
         //Aius DB laden
         m_vPositionen = de.hattrickorganizer.database.DBZugriff.instance().getSystemPositionen(NO_HRF_VERBINDUNG,
                                                                                                name);
         checkAufgestellteSpieler();
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Helper Funcs
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * l�scht die aufgestellten Spieler
      */
     public final void resetAufgestellteSpieler() {
         m_clAssi.resetPositionsbesetzungen(m_vPositionen);
     }
 
     /**
      * l�scht die Reservespieler
      */
     public final void resetReserveBank() {
         //Nur Reservespieler
         final Vector vReserve = new Vector();
 
         for (int i = 0; i < m_vPositionen.size(); i++) {
             if (((SpielerPosition) m_vPositionen.get(i)).getId() >= ISpielerPosition.beginnReservere) {
                 vReserve.add(m_vPositionen.get(i));
             }
         }
 
         m_clAssi.resetPositionsbesetzungen(vReserve);
     }
 
     /**
      * spiechert die gesamte Aufstellung unter dem angegebenen Namen ab
      *
      * @param name TODO Missing Constructuor Parameter Documentation
      */
     public final void save(String name) {
         de.hattrickorganizer.database.DBZugriff.instance().saveAufstellung(NO_HRF_VERBINDUNG, this,
                                                                            name);
     }
 
     /**
      * spiechert die gesamte Aufstellung unter dem angegebenen Namen ab
      */
     public final void save4HRF() {
         de.hattrickorganizer.database.DBZugriff.instance().saveAufstellung(HOVerwaltung.instance()
                                                                                        .getModel()
                                                                                        .getID(),
                                                                            this, "HRF");
     }
 
     /**
      * speichert das aktuelle System unter dem "name" in der DB
      *
      * @param name TODO Missing Constructuor Parameter Documentation
      */
     public final void saveAufstellungsSystem(String name) {
         de.hattrickorganizer.database.DBZugriff.instance().saveSystemPositionen(NO_HRF_VERBINDUNG,
                                                                                 m_vPositionen, name);
     }
 
     /**
      * berechnet Anzahl Abwehr im System
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getAnzAbwehr() {
         int anzahl = 0;
 
         anzahl += getAnzPosImSystem(ISpielerPosition.AUSSENVERTEIDIGER);
         anzahl += getAnzPosImSystem(ISpielerPosition.AUSSENVERTEIDIGER_IN);
         anzahl += getAnzPosImSystem(ISpielerPosition.AUSSENVERTEIDIGER_OFF);
         anzahl += getAnzPosImSystem(ISpielerPosition.AUSSENVERTEIDIGER_DEF);
         anzahl += getAnzPosImSystem(ISpielerPosition.INNENVERTEIDIGER);
         anzahl += getAnzPosImSystem(ISpielerPosition.INNENVERTEIDIGER_AUS);
         anzahl += getAnzPosImSystem(ISpielerPosition.INNENVERTEIDIGER_OFF);
 
         return anzahl;
     }
 
     /**
      * berechnet Anzahl Abwehr im System
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getAnzMittelfeld() {
         int anzahl = 0;
 
         anzahl += getAnzPosImSystem(ISpielerPosition.MITTELFELD);
         anzahl += getAnzPosImSystem(ISpielerPosition.MITTELFELD_OFF);
         anzahl += getAnzPosImSystem(ISpielerPosition.MITTELFELD_DEF);
         anzahl += getAnzPosImSystem(ISpielerPosition.MITTELFELD_AUS);
         anzahl += getAnzPosImSystem(ISpielerPosition.FLUEGELSPIEL);
         anzahl += getAnzPosImSystem(ISpielerPosition.FLUEGELSPIEL_IN);
         anzahl += getAnzPosImSystem(ISpielerPosition.FLUEGELSPIEL_OFF);
         anzahl += getAnzPosImSystem(ISpielerPosition.FLUEGELSPIEL_DEF);
 
         return anzahl;
     }
 
     //    /**berechnet Anzahl Abwehr im System*/
     //    private int getAnzSturm()
     //    {
     //        int anzahl  =   0;
     //
     //        anzahl  +=  getAnzPosImSystem( ISpielerPosition.STURM );
     //        anzahl  +=  getAnzPosImSystem( ISpielerPosition.STURM_DEF );
     //
     //        return anzahl;
     //    }
 
     /**
      * ermittelt ANzahl der gesuchten Pos im aktuellen System
      *
      * @param position TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getAnzPosImSystem(byte position) {
         SpielerPosition pos = null;
         int anzahl = 0;
 
         for (int i = 0; (m_vPositionen != null) && (i < m_vPositionen.size()); i++) {
             pos = (SpielerPosition) m_vPositionen.elementAt(i);
 
             if ((position == pos.getPosition()) && (pos.getId() < ISpielerPosition.beginnReservere)) {
                 ++anzahl;
             }
         }
 
         return anzahl;
     }
 
     /**
      * berechnet die stk des Spielers f�r die angegebene Position
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private float calcPlayerStk(Vector spieler, int spielerId, byte position, boolean mitForm) {
         Spieler player = null;
 
         for (int i = 0; (spieler != null) && (i < spieler.size()); i++) {
             player = (Spieler) spieler.elementAt(i);
 
             if (player.getSpielerID() == spielerId) {
                 return player.calcPosValue(position, mitForm);
             }
         }
 
         return 0.0f;
     }
 
     /**
      * berechnet die STK-Summe aller aufgestllten Spieler f�r diese Position
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private float calcTeamStk(Vector spieler, byte position, boolean mitForm) {
         float stk = 0.0f;
         de.hattrickorganizer.model.SpielerPosition pos = null;
 
         for (int i = 0; (m_vPositionen != null) && (spieler != null) && (i < m_vPositionen.size());
              i++) {
             pos = (SpielerPosition) m_vPositionen.elementAt(i);
 
             if ((pos.getPosition() == position) && (pos.getId() < ISpielerPosition.beginnReservere)) {
                 stk += calcPlayerStk(spieler, pos.getSpielerId(), position, mitForm);
             }
         }
 
         return de.hattrickorganizer.tools.Helper.round(stk, 1);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void dumpValues() {
         //dumpen
         for (int i = 0; (m_vPositionen != null) && (i < m_vPositionen.size()); i++) {
             final de.hattrickorganizer.model.Spieler temp = HOVerwaltung.instance().getModel()
                                                                         .getSpieler(((SpielerPosition) m_vPositionen
                                                                                      .elementAt(i))
                                                                                     .getSpielerId());
             String name = "";
             float stk = 0.0f;
 
             if (temp != null) {
                 name = temp.getName();
                 stk = temp.calcPosValue(((SpielerPosition) m_vPositionen.elementAt(i)).getPosition(),
                                         true);
             }
 
             HOLogger.instance().log(getClass(),"PosID: "
                                + SpielerPosition.getNameForID(((SpielerPosition) m_vPositionen.elementAt(i)).getId())
                                + " ,Spieler :" + name + " , Stk : " + stk);
         }
 
         if (m_iKapitaen > 0) {
             HOLogger.instance().log(getClass(),"Kapit�n : "
                                + HOVerwaltung.instance().getModel().getSpieler(m_iKapitaen).getName());
         }
 
         if (m_iKicker > 0) {
             HOLogger.instance().log(getClass(),"Standards : "
                                + HOVerwaltung.instance().getModel().getSpieler(m_iKicker).getName());
         }
 
         HOLogger.instance().log(getClass(),"TW : "
                            + getTWTeamStk(HOVerwaltung.instance().getModel().getAllSpieler(), true)
                            + " AW : "
                            + getAWTeamStk(HOVerwaltung.instance().getModel().getAllSpieler(), true)
                            + " MF : "
                            + getMFTeamStk(HOVerwaltung.instance().getModel().getAllSpieler(), true)
                            + " ST : "
                            + getSTTeamStk(HOVerwaltung.instance().getModel().getAllSpieler(), true));
     }
 
     /**
      * stellt das 4-4-2 Grundsystem ein
      */
     private void initPositionen352() {
         if (m_vPositionen != null) {
             m_vPositionen.removeAllElements();
         } else {
             m_vPositionen = new Vector();
         }
 
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.keeper, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightBack, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideBack1, 0,
                                               ISpielerPosition.ZUS_MITTELFELD));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideBack2, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftBack, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightWinger, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideMid1, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideMid2, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftWinger, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.forward1, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.forward2, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substBack, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substInsideMid, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substWinger, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substKeeper, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substForward, 0, (byte) 0));
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //  INIT
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * stellt das 4-4-2 Grundsystem ein
      */
     private void initPositionen442() {
         if (m_vPositionen != null) {
             m_vPositionen.removeAllElements();
         } else {
             m_vPositionen = new Vector();
         }
 
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.keeper, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightBack, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideBack1, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideBack2, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftBack, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightWinger, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideMid1, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.insideMid2, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftWinger, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.forward1, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.forward2, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substBack, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substInsideMid, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substWinger, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substKeeper, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substForward, 0, (byte) 0));
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param object TODO Missing Method Parameter Documentation
      * @param object2 TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private SpielerPosition swap(Object object, Object object2) {
         final SpielerPosition sp = (SpielerPosition) object;
         final SpielerPosition sp2 = (SpielerPosition) object2;
 
         // TODO Auto-generated method stub
         return new SpielerPosition(sp.getId(), sp2.getSpielerId(), sp2.getTaktik());
     }
 
     /**
      * Debug logging.
      */
 //    private static void debug(String txt) {
 //    	HOLogger.instance().debug(Aufstellung.class, txt);
 //    }
 
 }
