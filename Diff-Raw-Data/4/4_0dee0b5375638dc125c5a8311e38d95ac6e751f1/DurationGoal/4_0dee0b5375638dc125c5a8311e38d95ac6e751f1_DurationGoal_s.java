 public class DurationGoal implements Goal {
 	private double goal;
 	private double startTime;
 
 	public DurationGoal(double dur, Session session) {
 		goal = dur*60;
 		startTime = session.getTimeElapsed();
 	}
 
 	/** {@inheritDoc} */
 	public boolean checkIfDone(Session session) {
 		return (session.getTimeElapsed() - startTime) >= goal;
 	}
 
 	public String getProgress(Session session) {
 		int timeElapsed = (int)((goal - (session.getTimeElapsed() - startTime)));
		return String.format("%02d:%02d:%02d", (int)(timeElapsed / 3600),
				(int)(timeElapsed / 60) % 60, (int)(timeElapsed) % 60);
 	}
 }
