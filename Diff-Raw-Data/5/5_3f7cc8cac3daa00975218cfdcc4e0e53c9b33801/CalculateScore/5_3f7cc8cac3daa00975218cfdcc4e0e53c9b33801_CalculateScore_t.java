 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package logic;
 
 import extractor.VoteExtractor;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 import model.Formation;
 import model.Match;
 import model.Player;
 
 /**
  *
  * @author kix
  */
 public class CalculateScore {
 
 
 	private VoteExtractor ve;
 	private boolean bonusHome;
 	private String path;
 	private List<Match> matches;
 
 	public CalculateScore(boolean bonusHome, String path, List<Match> matches) {
 		try {
 			this.ve = new VoteExtractor(path);
 			this.bonusHome = bonusHome;
 			ve.extractVotes();
 			this.matches = matches;
 		} catch (IOException ex) {
 			Logger.getLogger(CalculateScore.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	public void calculateOnly(String fantaSquadra, Formation formation) throws IOException {
 
 
 
 
 		double score = 0;
 
 
 
 		for (Player p : formation.getFormation()) {
 
 
 			double app = 0.0;
 
 			if (p.getNome().equals("Office")) {
 
 				app += 4.0;
 
 			} else if (p.getNome().equals("OfficeP")) {
 				app += 3.0;
 			} else if (p.getNome().equals("Over")) {
 
 				app += 0.0;
 
 			} else {
 
 				app += ve.getVotes().get(p.getNome()).getVote();
 				app += 3.0 * ve.getVotes().get(p.getNome()).getGf();
 				app += ve.getVotes().get(p.getNome()).getAs();
 				app += -2.0 * ve.getVotes().get(p.getNome()).getAu();
 				app += 3.0 * ve.getVotes().get(p.getNome()).getGr();
 				app += -1.0 * ve.getVotes().get(p.getNome()).getGs();
 				app += 3.0 * ve.getVotes().get(p.getNome()).getRp();
 				app += -3.0 * ve.getVotes().get(p.getNome()).getRs();
 				if (ve.getVotes().get(p.getNome()).isAm()) {
 					app -= 0.5;
 				}
 				if (ve.getVotes().get(p.getNome()).isEs()) {
 					app -= 1.0;
 				}
 			}
 
 
 
 
 			score += app;
 
 		}
 		double mod = modifier(formation);
 
 		score += mod;
 
 		if (bonusHome && checkIsHome(fantaSquadra)) {
 			score += 2;
 
 		}
 
 
 		int golSquadra = calculateGol(score);
 
		//fixBenches(matches);
 		sortFormations(matches);
 
 		for (Match m : matches) {
 			if (m.getHomeTeam().equals(fantaSquadra)) {
 				m.setGolHome(golSquadra);
 				m.setScoreHome(score);
 				m.setUsedFormationHome(formation);
 				m.setModHome(mod);
 
 			} else if (m.getAwayTeam().equals(fantaSquadra)) {
 				m.setGolAway(golSquadra);
 				m.setScoreAway(score);
 				m.setUsedFormationAway(formation);
 				m.setModAway(mod);
 
 
 			}
 
 
 		}
 	}
 
 	public void calculate(String fantaSquadra, Formation formation) throws IOException {
 
 
 		Formation f = fixFormation(formation);
 
 		double score = 0;
 
 
 
 		for (Player p : f.getFormation()) {
 
 
 			double app = 0.0;
 
 			if (p.getNome().equals("Office")) {
 
 				app += 4.0;
 
 			} else if (p.getNome().equals("OfficeP")) {
 				app += 3.0;
 			} else if (p.getNome().equals("Over")) {
 
 				app += 0.0;
 
 			} else {
 
 				app += ve.getVotes().get(p.getNome()).getVote();
 				app += 3.0 * ve.getVotes().get(p.getNome()).getGf();
 				app += ve.getVotes().get(p.getNome()).getAs();
 				app += -2.0 * ve.getVotes().get(p.getNome()).getAu();
 				app += 3.0 * ve.getVotes().get(p.getNome()).getGr();
 				app += -1.0 * ve.getVotes().get(p.getNome()).getGs();
 				app += 3.0 * ve.getVotes().get(p.getNome()).getRp();
 				app += -3.0 * ve.getVotes().get(p.getNome()).getRs();
 				if (ve.getVotes().get(p.getNome()).isAm()) {
 					app -= 0.5;
 				}
 				if (ve.getVotes().get(p.getNome()).isEs()) {
 					app -= 1.0;
 				}
 			}
 
 
 
 
 			score += app;
 
 		}
 		double mod = modifier(f);
 
 		score += mod;
 
 		if (bonusHome && checkIsHome(fantaSquadra)) {
 			score += 2;
 
 		}
 
 
 		int golSquadra = calculateGol(score);
                 
		//fixBenches(matches);
 		sortFormations(matches);
 
 		for (Match m : matches) {
 			if (m.getHomeTeam().equals(fantaSquadra)) {
 				m.setGolHome(golSquadra);
 				m.setScoreHome(score);
 				m.setUsedFormationHome(f);
 				m.setModHome(mod);
 
 			} else if (m.getAwayTeam().equals(fantaSquadra)) {
 				m.setGolAway(golSquadra);
 				m.setScoreAway(score);
 				m.setUsedFormationAway(f);
 				m.setModAway(mod);
 
 
 			}
 
 
 		}
 	}
 
 	public Formation fixFormation(Formation formation) throws IOException {
 
 		Formation fixedForm = new Formation();
 		boolean officeIn = false;
 		int maxSub = 3;
 
 
 		for (Player panchinaro : formation.getBench()) {
 			if (ve.getVotes().containsKey(panchinaro.getNome()) && ve.getVotes().get(panchinaro.getNome()).getVote() != 0.0) {
 				for (Player titolare : formation.getFormation()) {
 					if (titolare.getRuolo().equals(panchinaro.getRuolo()) && !fixedForm.getFormation().contains(panchinaro) && !fixedForm.getBench().contains(titolare)) {
 						if (!ve.getVotes().containsKey(titolare.getNome()) || ve.getVotes().get(titolare.getNome()).getVote() == 0.0) {
 							if (maxSub > 0) {
 								fixedForm.getFormation().add(panchinaro);
 								fixedForm.getBench().add(titolare);
 
 								//								formation.getFormation().add(panchinaro);
 								//								formation.getFormation().remove(titolare);
 
 								maxSub -= 1;
 
 							}
 						}
 					}
 				}
 			}
 
 		}
 		int dif = 0;
 		int cc = 0;
 		int att = 0;
 		for (Player p : formation.getFormation()) {
 			if (ve.getVotes().containsKey(p.getNome()) && ve.getVotes().get(p.getNome()).getVote() != 0.0) {
 				if(p.getRuolo().equals("difensore"))
 					dif += 1;
 				else if(p.getRuolo().equals("centrocampista"))
 					cc += 1;
 				else if(p.getRuolo().equals("attaccante"))
 					att += 1;
 			}
 		} 
 		for (Player p : formation.getFormation()) {
 			if (ve.getVotes().containsKey(p.getNome()) && ve.getVotes().get(p.getNome()).getVote() != 0.0) {
 				fixedForm.getFormation().add(p);
 			} else if (!fixedForm.getBench().contains(p)) {
 
 				Player playerIn = subsitute(p, formation, fixedForm, maxSub, officeIn, dif, cc, att);
 				if(!playerIn.getNome().equals("Office") || playerIn.getNome().equals("Over")){
 
 					if(playerIn.getRuolo().equals("difensore")) {
 						dif += 1;
 
 					}
 					else if(playerIn.getRuolo().equals("centrocampista")){
 						cc += 1;
 					}
 					else if(playerIn.getRuolo().equals("attaccante")) {
 						att += 1;
 					}
 				}
 				try {
 					if (playerIn.getNome().equals("Office") || playerIn.getNome().equals("OfficeP")) {
 						officeIn = true;
 					}
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					officeIn = true;
 				}
 				fixedForm.getFormation().add(playerIn);
 				fixedForm.getBench().add(p);
 				//				formation.getFormation().add(playerIn);
 				//				formation.getFormation().remove(p);
 				//				formation.getBench().remove(playerIn);
 				//				formation.getBench().add(p);
 				if(!playerIn.getNome().equals("Over"))
 					maxSub -= 1;
 				
 			}
 
 
 		}
 
 		for (Player p : formation.getBench()) {
 			fixedForm.getBench().add(p);
 		}
 
 		Collections.sort(fixedForm.getFormation(),new RoleComparator());
 
 		return fixedForm;
 
 
 	}
 
 	private Player subsitute(Player p, Formation formation, Formation fixed, int subs, boolean officeIn, int dif, int cc, int att) {
 
 
 		String role = p.getRuolo();
 		Player playerIn = new Player();
 		boolean found = false;
 
 
 		if (subs < 0) {
 
 			playerIn = new Player();
 			playerIn.setNome("Over");
 
 		} else {
 
 
 			int i;
 
 			for (i = 0; !found && i < formation.getBench().size(); i++) {
 
 
 
 				if (role.equals("portiere")) {
 					playerIn = new Player();
 					playerIn.setNome("OfficeP");
 					playerIn.setRuolo("portiere");
 					found = true;
 					officeIn = true;
 				} else if (ve.getVotes().containsKey(formation.getBench().get(i).getNome()) && ve.getVotes().get(formation.getBench().get(i).getNome()).getVote() != 0.0) {
 
 					if (!formation.getBench().get(i).getRuolo().equals("portiere") && !fixed.getFormation().contains(formation.getBench().get(i))) {
 
 
 						// int conta = countByRole(fixed, formation.getBench().get(i).getRuolo());
 
 						if (formation.getBench().get(i).getRuolo().equals("difensore") && checkModule(dif, cc, att, formation.getBench().get(i).getRuolo())) {
 							playerIn = formation.getBench().get(i);
 							playerIn.setRuolo("difensore");
 							found = true;
 							dif += 1;
 						} else if (formation.getBench().get(i).getRuolo().equals("centrocampista") && checkModule(dif, cc, att, formation.getBench().get(i).getRuolo())) {
 							playerIn = formation.getBench().get(i);
 							playerIn.setRuolo("centrocampista");
 							found = true;
 							cc += 1;
 						} else if (formation.getBench().get(i).getRuolo().equals("attaccante") && checkModule(dif, cc, att, formation.getBench().get(i).getRuolo())) {
 							playerIn = formation.getBench().get(i);
 							playerIn.setRuolo("attaccante");
 							found = true;
 							att += 1;
 						} 
 					}
 				}
 			}
 		}
 
 		if (!found && !officeIn) {
 
 			playerIn = new Player();
 			playerIn.setNome("Office");
 			playerIn.setRuolo(p.getRuolo());
 		} else if (!found && officeIn) {
 			playerIn = new Player();
 			playerIn.setNome("Over");
 			playerIn.setRuolo(p.getRuolo());
 
 		}
 		return playerIn;
 	}
 
 	public int countByRole(Formation formation, String ruolo) {
 		int cont = 0;
 		for (Player p : formation.getFormation()) {
 			if (!p.getNome().equals("Office") && !p.getNome().equals("Over") && p.getRuolo().equals(ruolo)) {
 				cont++;
 			}
 		}
 		return cont;
 	}
 
 	private double modifier(Formation f) {
 		double mod = 0;
 		boolean found = false;
 		ArrayList<Player> difensori = new ArrayList<Player>();
 
 
 		for (Player p : f.getFormation()) {
 			if (p.getNome().equals("OfficeP") ) {
 				mod = 0.0;
 				found = true;
 			}
 
 		}
 
 		if (countByRole(f, "difensore") >= 4 && !found) {
 
 			double keeperVote = 0;
 			for (Player d : f.getFormation()) {
 				if (!d.getNome().equals("Office") && !d.getNome().equals("Over") && d.getRuolo().equals("difensore")  ) {
 					difensori.add(d);
 
 				}
 				if (!d.getNome().equals("Office") && !d.getNome().equals("Over") && d.getRuolo().equals("portiere")) {
 					keeperVote = ve.getVotes().get(d.getNome()).getVote();
 
 				}
 
 			}
 
 			minimumList(difensori);
 			double avg = (ve.getVotes().get(difensori.get(0).getNome()).getVote() + ve.getVotes().get(difensori.get(1).getNome()).getVote() + ve.getVotes().get(difensori.get(2).getNome()).getVote() + keeperVote) / 4;
 			if (avg >= 6.0 && avg < 6.5) {
 				mod = 1;
 			} else if (avg >= 6.5 && avg < 7) {
 				mod = 3;
 			} else if (avg >= 7) {
 				mod = 6;
 			} else {
 				mod = 0;
 			}
 		}
 		return mod;
 	}
 
 	private void minimumList(ArrayList<Player> difensori) {
 
 		while (difensori.size() > 3) {
 			Player min = difensori.get(0);
 			for (Player p : difensori) {
 				if (ve.getVotes().get(min.getNome()).getVote() > ve.getVotes().get(p.getNome()).getVote()) {
 					min = p;
 				}
 			}
 			difensori.remove(min);
 		}
 	}
 
 	public VoteExtractor getVe() {
 		return ve;
 	}
 
 	public void setVe(VoteExtractor ve) {
 		this.ve = ve;
 	}
 
 	private boolean checkIsHome(String fantaSquadra) {
 		boolean isHome = false;
 		for (Match m : this.matches) {
 			if (m.getHomeTeam().equals(fantaSquadra)) {
 				isHome = true;
 			}
 		}
 		return isHome;
 	}
 
 	private int calculateGol(double score) {
 		int gol;
 		if (score <= 65.5) {
 			gol = 0;
 		} else if (score >= 66.0 && score <= 71.5) {
 			gol = 1;
 		} else if (score >= 72.0 && score <= 77.5) {
 			gol = 2;
 		} else if (score >= 78.0 && score <= 83.5) {
 			gol = 3;
 		} else if (score >= 84.0 && score <= 89.5) {
 			gol = 4;
 		} else if (score >= 90.0 && score <= 95.5) {
 			gol = 5;
 		} else if (score >= 96.0 && score <= 101.5) {
 			gol = 6;
 		} else if (score >= 102.0 && score <= 107.5) {
 			gol = 7;
 		} else if (score >= 108.0 && score <= 113.5) {
 			gol = 8;
 		} else if (score >= 114.0 && score <= 119.5) {
 			gol = 9;
 		} else {
 			gol = 10;
 		}
 		return gol;
 	}
 
 	private void fixBenches(List<Match> matches) {
 		for(Match m : matches){
                     ArrayList<Player> fixedBench = new ArrayList<Player>(m.getUsedFormationHome().getBench());
                         
 			for(Player p : m.getUsedFormationHome().getFormation()){
                             for(Player p1 : m.getUsedFormationHome().getBench()){
                                 if(p.getNome().equals(p1.getNome())){
 					fixedBench.remove(p1);
                             }
                             
 				}
 			}
                         m.getUsedFormationHome().setBench(fixedBench);
                         
                         ArrayList<Player> fixedBench1 = new ArrayList<Player>(m.getUsedFormationAway().getBench());
 			for(Player p : m.getUsedFormationAway().getFormation()){
                             for(Player p1 : m.getUsedFormationAway().getBench()){
                                 if(p.getNome().equals(p1.getNome())){
 					fixedBench1.remove(p1);
                             }
                             
 				}
 			}
                         m.getUsedFormationAway().setBench(fixedBench1);
 		}
                 
 	}
 
         
         
 	private boolean checkModule(int dif, int cc, int att, String ruoloIn) {
 		boolean moduloOk = false;
 		if(ruoloIn.equals("difensore")) {
 			if(dif+1 <= 5) {
 				if(dif+1 == 5) {
 					if((cc <= 4 && att <= 1) || (cc <= 3 && att <= 2))
 						moduloOk = true;
 				}
 				else if(dif+1 == 4) {
 					if((cc <= 5 && att <= 1) || (cc <= 4 && att <= 2) || (cc <= 3 && att <= 3))
 						moduloOk = true;    
 				}
 				else if(dif + 1 <= 3) {
 					if((cc <= 5 && att <= 2) || (cc <= 4 && att <= 3))
 						moduloOk = true;
 				}
 
 			}
 
 
 		}
 		else if(ruoloIn.equals("centrocampista")) {
 			if(cc+1 <= 5) {
 				if(cc+1 == 5) {
 					if((dif <= 4 && att <= 1) || (dif <= 3 && att <= 2))
 						moduloOk = true;
 				}
 				else if(cc+1 == 4) {
 					if((dif <= 5 && att <= 1) || (dif <= 4 && att <= 2) || (dif <= 3 && att <= 3))
 						moduloOk = true;    
 				}
 				else if(cc + 1 <= 3) {
 					if((dif <= 5 && att <= 2) || (dif <= 4 && att <= 3))
 						moduloOk = true;
 				}
 
 			}
 
 
 		}
 		else {
 			if(att+1 <= 3) {
 				if(att+1 == 3) {
 					if((dif <= 4 && cc <= 3) || (dif <= 3 && cc <= 4))
 						moduloOk = true;
 				}
 				else if(att+1 == 2) {
 					if((dif <= 5 && cc <= 3) || (dif <= 4 && cc <= 4) || (dif <= 3 && cc <= 5))
 						moduloOk = true;    
 				}
 				else if(att + 1 <= 1) {
 					if((dif <= 5 && cc <= 4) || (dif <= 4 && cc <= 5))
 						moduloOk = true;
 				}
 
 			}
 		}
 
 
 		return moduloOk;
 	}
 
 
 
 
 
 
 
 
 
 
 	private void sortFormations(List<Match> matches) {
 		for(int i = 0; i < matches.size(); i++){
 			Collections.sort(matches.get(i).getUsedFormationHome().getFormation(), new RoleComparator());
 			Collections.sort(matches.get(i).getUsedFormationAway().getFormation(), new RoleComparator());
 		}
 	}
 }
