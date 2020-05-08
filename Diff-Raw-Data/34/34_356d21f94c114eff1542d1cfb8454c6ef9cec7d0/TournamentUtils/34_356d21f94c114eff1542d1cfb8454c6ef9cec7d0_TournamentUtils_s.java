 package fr.meijin.run4win.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.zkoss.zul.Messagebox;
 import org.zkoss.zul.Tab;
 
 import fr.meijin.run4win.model.Game;
 import fr.meijin.run4win.model.Player;
 import fr.meijin.run4win.model.PlayerRanking;
 import fr.meijin.run4win.model.Ranking;
 import fr.meijin.run4win.model.Round;
 import fr.meijin.run4win.model.Tournament;
 import fr.meijin.run4win.util.identity.CorporationIdentityEnum;
 import fr.meijin.run4win.util.identity.RunnerIdentityEnum;
 import fr.meijin.run4win.util.lang.LangEnum;
 import fr.meijin.run4win.util.lang.LangUtils;
 
 
 
 public class TournamentUtils {
 
 	public static void reloadRanking(Tournament tournament){
 		tournament.rankings = new ArrayList<Ranking>();
 		RankingUtils.updatePlayersRanking(tournament.roundsList, tournament.players);
 		if(tournament.rounds >= 1){
 			for(int i = 1; i < tournament.roundsList.size(); i++){
 				Ranking ranking = new Ranking();
 				ranking.roundNumber = i;
 				for(Player p : tournament.players){
 					PlayerRanking pr = new PlayerRanking();
 					pr.nickname = p.nickname;
 					pr.prestige = p.getPrestigeByRound(i);
 					pr.weakestSideWins = p.getWeakestSideWins(i);
 					pr.opponentsStrength = p.getOpponentsStrengthByRound(i);
 					ranking.playerRankings.add(pr);
 				}
 				
 				tournament.rankings.add(ranking);
 			}
 		}
 	}
 	
 	public static Player createByePlayer(){
 		Player p = new Player();
 		p.id=0;
 		p.nickname = "BYE";
 		p.forfeit = true;
 		p.idCorporation = CorporationIdentityEnum.BLANK;
 		p.idRunner = RunnerIdentityEnum.BLANK;
 		return p;
 	}
 	
	public static Tournament nextRound(Tournament tournament){
 		RankingUtils.updatePlayersRanking(tournament.roundsList, tournament.players);
 		
 		Collections.sort(tournament.players);
 		if(tournament.rounds >= 1){
 			Ranking ranking = new Ranking();
 			ranking.roundNumber = tournament.rounds;
 			for(Player p : tournament.players){
 				PlayerRanking pr = new PlayerRanking();
 				pr.nickname = p.nickname;
 				pr.prestige = p.getPrestige();
 				pr.weakestSideWins = p.getWeakestSideWins();
 				pr.opponentsStrength = p.getOpponentsStrength();
 				ranking.playerRankings.add(pr);
 			}
 			tournament.rankings.add(ranking);
 		}
 		
 		List<Player> toMatch = new ArrayList<Player>(tournament.players);
 		List<Player> forfeitPlayers = new ArrayList<Player>();
 		for(Player p : toMatch){
 			System.out.println("Player to match "+p.nickname);
 			if (p.forfeit && p.id !=0)
 				forfeitPlayers.add(p);
 		}
 		toMatch.removeAll(forfeitPlayers);
 
 		if(toMatch.size()%2 == 1){
 			toMatch.add(TournamentUtils.createByePlayer());
 		} else if (tournament.players.isEmpty()){
 			Messagebox.show(LangUtils.getMessage(LangEnum.NO_PLAYERS), LangUtils.getMessage(LangEnum.ERROR), Messagebox.OK, Messagebox.ERROR);
			return tournament;
 		}
 
 		tournament.rounds++;
 		
 		Collections.sort(toMatch);
 		List<Game> matched = new ArrayList<Game>();
 		if(tournament.rounds == 1){
 			List<Player> byePlayers = new ArrayList<Player>();
 			for(Player p : toMatch){
 				if (p.byeFirstRound){
 					byePlayers.add(p);
 				}
 			}
 			
 			toMatch.removeAll(byePlayers);
 			
 			for(Player p : byePlayers){
 				Game game = new Game();
 				game.roundNumber = tournament.rounds;
 				game.tableNumber = 0;
 				game.player1 = p;
 				game.player2 = TournamentUtils.createByePlayer();
 				game.p1Result.resultCorporation = 2;
 				game.p1Result.resultRunner = 2;
 				matched.add(game);
 			}
 			
 			Collections.shuffle(toMatch);
 		}
 		
 		Round r = MatchingUtils.doSingleMatch(tournament.rounds,tournament.roundsList, toMatch, matched);
 		tournament.roundsList.add(r);
 		
		return tournament;
 	}
 }
