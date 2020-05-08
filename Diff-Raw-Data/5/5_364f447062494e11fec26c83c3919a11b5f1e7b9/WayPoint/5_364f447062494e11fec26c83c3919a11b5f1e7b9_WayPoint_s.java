 package environment;
 
 
 public abstract class WayPoint implements IWayPoint {
 
 	protected ILane lane;
 	
 	public WayPoint (ILane lane){
 		this.lane = lane;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	
 	@Override
 	public ILane getLane() {
 		return lane;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	
 	@Override
 	public int compareTo(IWayPoint o) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 
 	@Override
	public abstract float getXPos() throws Exception;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	
 	@Override
	public abstract float getYPos() throws Exception;
 	
 	
 	public String toString(){
 		try {
 			return (int)this.getXPos()+"/" + (int)this.getYPos();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			return e.toString();
 		}
 	}
 }
