 public abstract class Algorithm
 {
 	protected int minimumClusters;
 	protected int maximumClusters;
 	protected Field field;
 	
 	public Algorithm(int minimumClusters, int maximumClusters, Field field)
 	{
 		this.minimumClusters = minimumClusters;
 		this.maximumClusters = maximumClusters;
 		this.field = field;
 	}
 	
 	public abstract void run();
 	
 	public final void printPoints()
 	{
 		for(Point p : field)
 		{
			System.out.println(p.getX() + " "  + p.getY() + " " + p.getPointCategory().toString());
 		}
 	}
 }
