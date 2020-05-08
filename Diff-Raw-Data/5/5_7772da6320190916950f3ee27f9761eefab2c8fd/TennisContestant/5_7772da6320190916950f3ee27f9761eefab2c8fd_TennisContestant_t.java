 
 public class TennisContestant implements Contestant, Ranked {
 	String name;
 	int ranking;
 	
 	public TennisContestant(String name, int ranking){
 		this.name = name;
 		this.ranking = ranking;
 	}
 	
 	public String getName(){
 		return name;
 	}
 	
 	public int getRanking(){
		if(ranking <= 1)
			return 2;
		else 
			return ranking;
 	}
 	
 	public boolean hasBetterRanking(Ranked second){
 		return this.getRanking() < second.getRanking();
 	}
 }
