 package atl.space.components.heat;
 
 //Not done.
 
 public abstract class HeatVent implements HeatReleaser{
 
 	//Heat loss = releaseAmount. No fancy math for vents.
   
   private double releaseAmount;
   private HeatContainerComponent ventingReference;
 
	public HeatHull(){
 		this(1, null);
 	}
	public HeatHull(double releaseAmount, HeatContainerComponent thingToVent){
 		this.releaseAmount = releaseAmount;
     this.ventingReference = thingToVent;
 	}
 
 	private double calculateHeatRelease(){
 		return releaseAmount;
 	}
 
 
 	@Override
 	public void releaseHeat(double factor) {
 		ventingReference.loseHeat(calculateHeatRelease() * factor);
 		//do other stuff?
 	}
 
 	@Override
 	public void releaseHeat() {
 		releaseHeat(1);
 	}
 
 
 }
