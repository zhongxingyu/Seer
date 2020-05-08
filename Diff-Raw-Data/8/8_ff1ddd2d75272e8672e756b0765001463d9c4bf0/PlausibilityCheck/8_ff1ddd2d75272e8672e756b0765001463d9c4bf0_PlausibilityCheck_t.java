 package ho.module.lineup.substitution.plausibility;
 
 import ho.core.model.HOVerwaltung;
 import ho.core.model.player.Spieler;
 import ho.core.model.player.SpielerPosition;
 import ho.module.lineup.Lineup;
 import ho.module.lineup.substitution.LanguageStringLookup;
 import ho.module.lineup.substitution.model.MatchOrderType;
 import ho.module.lineup.substitution.model.Substitution;
 
 public class PlausibilityCheck {
 
 	public static Problem checkForProblem(Lineup lineup, Substitution substitution) {
 		if (substitution.getOrderType() == MatchOrderType.SUBSTITUTION
				&& (substitution.getObjectPlayerID() <= 0 || substitution.getSubjectPlayerID() <= 0)) {
 			return Error.SUBSTITUTION_PLAYER_MISSING;
 		}
 		if (substitution.getOrderType() == MatchOrderType.POSITION_SWAP
				&& (substitution.getObjectPlayerID() <= 0 || substitution.getSubjectPlayerID() <= 0)) {
 			return Error.POSITIONSWAP_PLAYER_MISSING;
 		}
 		if (substitution.getOrderType() == MatchOrderType.NEW_BEHAVIOUR
				&& substitution.getSubjectPlayerID() <= 0) {
 			return Error.NEWBEHAVIOUR_PLAYER_MISSING;
 		}
 
 		// first, check if players in lineup. note: when NEW_BEHAVIOUR, there is
 		// only one player involved
 		if (substitution.getOrderType() != MatchOrderType.NEW_BEHAVIOUR
 				&& !lineup.isSpielerAufgestellt(substitution.getObjectPlayerID())) {
 			return Error.PLAYERIN_NOT_IN_LINEUP;
 		} else if (!lineup.isSpielerAufgestellt(substitution.getSubjectPlayerID())) {
 			return Error.PLAYEROUT_NOT_IN_LINEUP;
 		}
 
 		// when NEW_BEHAVIOUR, check that behaviour there is really a change
 		if (substitution.getOrderType() == MatchOrderType.NEW_BEHAVIOUR) {
 			SpielerPosition pos = lineup.getPositionBySpielerId(substitution.getSubjectPlayerID());
 			if (pos.getTaktik() == substitution.getBehaviour()) {
 				return Uncertainty.SAME_TACTIC;
 			}
 		}
 		return null;
 	}
 
 	public static String getComment(Problem problem, Substitution substitution) {
 		if (problem instanceof Error) {
 			switch ((Error) problem) {
 			case PLAYERIN_NOT_IN_LINEUP:
 				return HOVerwaltung.instance().getLanguageString(problem.getLanguageKey(),
 						getPlayerIn(substitution).getName());
 			case PLAYEROUT_NOT_IN_LINEUP:
 				return HOVerwaltung.instance().getLanguageString(problem.getLanguageKey(),
 						getPlayerOut(substitution).getName());
 			default:
 				return HOVerwaltung.instance().getLanguageString(problem.getLanguageKey());
 			}
 		} else if (problem instanceof Uncertainty) {
 			switch ((Uncertainty) problem) {
 			case SAME_TACTIC:
 				return HOVerwaltung.instance().getLanguageString(problem.getLanguageKey(),
 						getPlayerOut(substitution).getName(),
 						LanguageStringLookup.getBehaviour(substitution.getBehaviour()));
 			}
 		}
 
 		return null;
 	}
 
 	private static Spieler getPlayerIn(Substitution substitution) {
 		return HOVerwaltung.instance().getModel().getSpieler(substitution.getObjectPlayerID());
 	}
 
 	private static Spieler getPlayerOut(Substitution substitution) {
 		return HOVerwaltung.instance().getModel().getSpieler(substitution.getSubjectPlayerID());
 	}
 }
