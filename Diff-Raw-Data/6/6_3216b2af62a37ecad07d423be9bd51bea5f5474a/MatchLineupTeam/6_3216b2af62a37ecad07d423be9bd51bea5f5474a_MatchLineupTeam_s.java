 // %716628770:de.hattrickorganizer.model.matches%
 /*
  * MatchLineupTeam.java
  *
  * Created on 20. Oktober 2003, 08:55
  */
 package de.hattrickorganizer.model.matches;
 
 import java.util.Vector;
 
 import plugins.ILineUp;
 import plugins.IMatchLineupPlayer;
 import plugins.IMatchLineupTeam;
 import plugins.ISpielerPosition;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author thomas.werth
  */
 public class MatchLineupTeam implements IMatchLineupTeam {
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** TODO Missing Parameter Documentation */
     protected String m_sTeamName;
 
     /** TODO Missing Parameter Documentation */
     protected Vector m_vAufstellung = new Vector();
 
     /** TODO Missing Parameter Documentation */
     protected int m_iErfahrung;
 
     /** TODO Missing Parameter Documentation */
     protected int m_iTeamID;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new instance of MatchLineupTeam
      *
      * @param teamName TODO Missing Constructuor Parameter Documentation
      * @param teamID TODO Missing Constructuor Parameter Documentation
      * @param erfahrung TODO Missing Constructuor Parameter Documentation
      */
     public MatchLineupTeam(String teamName, int teamID, int erfahrung) {
         m_sTeamName = teamName;
         m_iErfahrung = erfahrung;
         m_iTeamID = teamID;
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Setter for property m_vAufstellung.
      *
      * @param m_vAufstellung New value of property m_vAufstellung.
      */
     public final void setAufstellung(java.util.Vector m_vAufstellung) {
         this.m_vAufstellung = m_vAufstellung;
     }
 
     /**
      * Getter for property m_vAufstellung.
      *
      * @return Value of property m_vAufstellung.
      */
     public final java.util.Vector getAufstellung() {
         return m_vAufstellung;
     }
 
     /**
      * Setter for property m_iErfahrung.
      *
      * @param m_iErfahrung New value of property m_iErfahrung.
      */
     public final void setErfahrung(int m_iErfahrung) {
         this.m_iErfahrung = m_iErfahrung;
     }
 
     /**
      * Getter for property m_iErfahrung.
      *
      * @return Value of property m_iErfahrung.
      */
     public final int getErfahrung() {
         return m_iErfahrung;
     }
 
     /**
      * Liefert Einen Spieler per ID aus der Aufstellung
      *
      * @param id TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final IMatchLineupPlayer getPlayerByID(int id) {
         MatchLineupPlayer player = null;
 
         for (int i = 0; (m_vAufstellung != null) && (i < m_vAufstellung.size()); i++) {
             player = (MatchLineupPlayer) m_vAufstellung.elementAt(i);
 
             if (player.getSpielerId() == id) {
                 return player;
             }
         }
 
         return null;
     }
 
     /**
      * Liefert Einen Spieler per PositionsID aus der Aufstellung
      *
      * @param id TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final IMatchLineupPlayer getPlayerByPosition(int id) {
         MatchLineupPlayer player = null;
 
         for (int i = 0; (m_vAufstellung != null) && (i < m_vAufstellung.size()); i++) {
             player = (MatchLineupPlayer) m_vAufstellung.elementAt(i);
 
             if (player.getId() == id) {
                 return player;
             }
         }

        return null;
     }
 
     /**
      * Setter for property m_iTeamID.
      *
      * @param m_iTeamID New value of property m_iTeamID.
      */
     public final void setTeamID(int m_iTeamID) {
         this.m_iTeamID = m_iTeamID;
     }
 
     /**
      * Getter for property m_iTeamID.
      *
      * @return Value of property m_iTeamID.
      */
     public final int getTeamID() {
         return m_iTeamID;
     }
 
     /**
      * Setter for property m_sTeamName.
      *
      * @param m_sTeamName New value of property m_sTeamName.
      */
     public final void setTeamName(java.lang.String m_sTeamName) {
         this.m_sTeamName = m_sTeamName;
     }
 
     /**
      * Getter for property m_sTeamName.
      *
      * @return Value of property m_sTeamName.
      */
     public final java.lang.String getTeamName() {
         return m_sTeamName;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param player TODO Missing Method Parameter Documentation
      */
     public final void add2Aufstellung(IMatchLineupPlayer player) {
         m_vAufstellung.add(player);
     }
 
     /**
      * determinates played System
      *
      * @return TODO Missing Return Method Documentation
      */
     public final byte determinateSystem() {
         short abw = 0;
         short mf = 0;
         short st = 0;
 
         MatchLineupPlayer player = null;
 
         for (int i = 0; i < m_vAufstellung.size(); i++) {
             player = (MatchLineupPlayer) m_vAufstellung.get(i);
 
             if ((player != null) && (player.getId() < ISpielerPosition.beginnReservere)) {
                 switch (player.getPosition()) {
                     case ISpielerPosition.UNBESTIMMT:
                         break;
 
                     case ISpielerPosition.AUSSENVERTEIDIGER:
                     case ISpielerPosition.AUSSENVERTEIDIGER_IN:
                     case ISpielerPosition.AUSSENVERTEIDIGER_OFF:
                     case ISpielerPosition.AUSSENVERTEIDIGER_DEF:
                     case ISpielerPosition.INNENVERTEIDIGER:
                     case ISpielerPosition.INNENVERTEIDIGER_AUS:
                     case ISpielerPosition.INNENVERTEIDIGER_OFF:
                         abw++;
                         break;
 
                     case ISpielerPosition.MITTELFELD:
                     case ISpielerPosition.MITTELFELD_OFF:
                     case ISpielerPosition.MITTELFELD_DEF:
                     case ISpielerPosition.MITTELFELD_AUS:
                     case ISpielerPosition.FLUEGELSPIEL:
                     case ISpielerPosition.FLUEGELSPIEL_IN:
                     case ISpielerPosition.FLUEGELSPIEL_OFF:
                     case ISpielerPosition.FLUEGELSPIEL_DEF:
                         mf++;
                         break;
 
                     case ISpielerPosition.STURM:
                     case ISpielerPosition.STURM_AUS:
                     case ISpielerPosition.STURM_DEF:
                         st++;
                         break;
                 }
             }
         }
 
         //343
         if (abw == 3) {
             if (mf == 4) {
                 return ILineUp.SYS_343;
             } //352
             else if ((mf == 5) && (st == 2)) {
                 return ILineUp.SYS_352;
             }
             //MURKS
             else {
                 return ILineUp.SYS_MURKS;
             }
         } else if (abw == 4) {
             //433
             if ((mf == 3) && (st == 3)) {
                 return ILineUp.SYS_433;
             } //442
             else if ((mf == 4) && (st == 2)) {
                 return ILineUp.SYS_442;
             } //451
             else if ((mf == 5) && (st == 1)) {
                 return ILineUp.SYS_451;
             }
             //MURKS
             else {
                 return ILineUp.SYS_MURKS;
             }
         } else if (abw == 5) {
             //532
             if ((mf == 3) && (st == 2)) {
                 return ILineUp.SYS_532;
             } //541
             else if ((mf == 4) && (st == 1)) {
                 return ILineUp.SYS_541;
             }
             //MURKS
             else {
                 return ILineUp.SYS_MURKS;
             }
         } //MURKS
         else {
             return ILineUp.SYS_MURKS;
         }
     }
 }
