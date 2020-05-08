 package ho.core.model.match;
 
 
 public enum MatchType {
 
 	NONE((int) 0),
 	LEAGUE((int) 1),
 	QUALIFICATION((int) 2),
 	CUP((int) 3),
 	FRIENDLYNORMAL((int) 4),
	INTSPIEL((int) 5),
	FRIENDLYCUPRULES((int) 6),
 	MASTERS((int) 7),
 	INTFRIENDLYNORMAL((int) 8),
 	INTFRIENDLYCUPRULES((int) 9),
 	NATIONALCOMPNORMAL((int) 10),
 	NATIONALCOMPCUPRULES((int) 11),
 	NATIONALFRIENDLY((int) 12),
 	TOURNAMENTGROUP((int) 50),
 	TOURNAMENTPLAYOFF((int) 51);
 
 	private final int id;
 
 	private MatchType(int id) {
 		this.id = id;
 	}
 
 
 	public int getId() {
 		return id;
 	}
 
 
 	public static MatchType getById(int id) {
 		for (MatchType matchType : MatchType.values()) {
 			if (matchType.getId() == id) {
 				return matchType;
 			}
 		}
 		return null;
 	}
 
 	public String getSourceString() {
 		switch (this) {
 			case TOURNAMENTGROUP :
 			case TOURNAMENTPLAYOFF : {
 				return "htointegrated";
 			}
 			default: {
 				return "hattrick";
 			}
 		}
 	}
 
 	public boolean isCupRules() {
 		switch (this) {
 			case CUP :
 			case FRIENDLYCUPRULES :
 			case INTFRIENDLYCUPRULES :
 			case NATIONALCOMPCUPRULES :
 			case TOURNAMENTPLAYOFF : {
 				return true;
 			}
 			default: {
 				return false;
 			}
 		}
 	}
 
 	public boolean isFriendly() {
 		switch (this) {
 			case FRIENDLYNORMAL :
 			case FRIENDLYCUPRULES :
 			case INTFRIENDLYNORMAL :
 			case INTFRIENDLYCUPRULES : {
 				return true;
 			}
 			default : {
 				return false;
 			}
 		}
 	}
 
 	/** Returns true for all normal matches.
 	 *  Cup, League, friendlies, qualification, masters
 	 *
 	 * @return true if the match is official
 	 */
 	public boolean isOfficial() {
 		switch (this) {
 			case LEAGUE :
 			case QUALIFICATION :
 			case CUP :
 			case FRIENDLYNORMAL :
 			case FRIENDLYCUPRULES :
 			case INTFRIENDLYNORMAL :
 			case INTFRIENDLYCUPRULES :
 			case MASTERS : {
 				return true;
 			}
 			default:
 				return false;
 		}
 	}
 	
 	public boolean isTournament() {
 		switch (this) {
 			case TOURNAMENTGROUP:
 			case TOURNAMENTPLAYOFF:
 				return true;
 			default: return false;
 		}
 	}
 
 
 	public String getName() {
 		 switch (this) {
 	         case LEAGUE:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("LigaSpiel");
 
 	         case CUP:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("PokalSpiel");
 
 	         case QUALIFICATION:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("QualifikationSpiel");
 
 	         case NATIONALCOMPCUPRULES:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("LaenderCupSpiel");
 
 	         case MASTERS:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("IntCupSpiel");
 
 	         case NATIONALCOMPNORMAL:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("LaenderSpiel");
 
 	         case INTSPIEL:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("IntSpiel");
 
 	         case INTFRIENDLYCUPRULES:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("IntFriendlyCupSpiel");
 
 	         case INTFRIENDLYNORMAL:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("IntFriendlySpiel");
 
 	         case NATIONALFRIENDLY:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("LaenderFriendlySpiel");
 
 	         case FRIENDLYCUPRULES:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("FriendlyCupSpiel");
 
 	         case FRIENDLYNORMAL:
 	             return ho.core.model.HOVerwaltung.instance().getLanguageString("FriendlySpiel");
 
 	         case TOURNAMENTGROUP:
 	         	 return ho.core.model.HOVerwaltung.instance().getLanguageString("TournamentMatch");
 
 	         case TOURNAMENTPLAYOFF :
 	         	 return ho.core.model.HOVerwaltung.instance().getLanguageString("TournamentMatch");
 
 	         //Error?
 	         default:
 	             return "";
 
 		 }
 	}
 
 	public int getIconArrayIndex() {
 		switch (this) {
 			case TOURNAMENTGROUP :
 				return 13;
 			case TOURNAMENTPLAYOFF :
 				return 14;
 			default :
 				return id;
 		}
 	}
 }
