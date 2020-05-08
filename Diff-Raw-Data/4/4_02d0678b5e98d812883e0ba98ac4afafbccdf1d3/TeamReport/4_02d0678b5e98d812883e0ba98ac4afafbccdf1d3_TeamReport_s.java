 package reports;
 
 import java.util.List;
 import models.Iteration;
 
 
 /**
  * object model representing a burndown report- consumable by generators
  * 
  * @author dlange
  *
  */
 public class TeamReport extends Report {
 	
 	public String project;
 	public int avgVelocity;
 	public int avgPrecision;
 	public int avgStoryPts;
 	public List<Iteration> iterations;
 	public BurndownReport burndown;
 	
 	public TeamReport(Iteration iteration) {
 		this.project = iteration.team.name;
 		this.avgVelocity = iteration.completedPoints;
 		this.avgPrecision = calcPrecision(iteration.completedPoints, iteration.totalPoints);
 		this.avgStoryPts = 0;
 		// go back last 5 sprints
 		this.iterations = teamIterations(iteration.team.id, 5);
 		this.burndown = new BurndownReport(iteration);
 	}
 
 	/**
 	 * Answer back last n iterations
 	 * @param team
 	 * @return
 	 */
 	private List<Iteration> teamIterations(Long teamId, int n) {
 		int totalVelocity = 0;
 		int totalPlanned = 0;
 		List<Iteration> iterations = Iteration.getLastNTeamIterations(n, teamId);
		if (iterations.size() > 0) {
 			for (Iteration iteration : iterations) {
 				totalVelocity += iteration.completedPoints;
 				totalPlanned += iteration.totalPoints;
 			}
 			this.avgVelocity = (int) (totalVelocity / iterations.size());
 			this.avgPrecision = (int) ((totalVelocity * 100) / totalPlanned);
 		}
 		return iterations;
 	}
 	
 	private int calcPrecision(int completed, int total) {
 		// TODO needs to be real 
 		return (int) ((total > 0)? ((completed * 100.0)/total):0);
 	}
 	
 	public Report generate() {
 		htmlReport = views.html.teamreport.render(this).body();
 		generated = true;
 		return this;
 	}
 	
 }
