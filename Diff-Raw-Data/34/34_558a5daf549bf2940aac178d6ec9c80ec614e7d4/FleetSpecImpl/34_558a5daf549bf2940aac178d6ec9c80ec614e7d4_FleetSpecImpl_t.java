 package de.abbaddie.wot.fleet.data.spec;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.abbaddie.wot.data.spec.Spec;
 import de.abbaddie.wot.data.spec.SpecImpl;
 import de.abbaddie.wot.data.spec.SpecPredicate;
 import de.abbaddie.wot.data.spec.SpecSet;
 import de.abbaddie.wot.fleet.data.spec.trait.FleetBound;
 
 public abstract class FleetSpecImpl extends SpecImpl implements FleetBound {
 	protected List<FleetDrive> drives;
 	
 	@Override
 	public List<FleetDrive> getDrives(SpecSet<?> technologies) {
 		List<FleetDrivePredicate> available = predicate.getAddition(FleetSpecPredicateAddition.class).getDrives();
 		List<FleetDrive> drives = new ArrayList<>(available.size());
 		
 		for(FleetDrivePredicate drivePredicate : available) {
 			FleetDriveImpl impl = new FleetDriveImpl(drivePredicate);
 			if(impl.isUsable(technologies)) {
 				drives.add(impl);
 			}
 		}
 		
 		return drives;
 	}
 	
 	@Override
 	public double getCapacity() {
 		return getPredicate().getAddition(FleetSpecPredicateAddition.class).getCapacity() * getCount();
 	}
 	
 	protected class FleetDriveImpl implements FleetDrive {
 		protected FleetDrivePredicate predicate;
 		
 		protected Spec driveSpec;
 		
 		protected FleetDriveImpl(FleetDrivePredicate predicate) {
 			this.predicate = predicate;
 		}
 		
 		protected boolean isUsable(SpecSet<?> technologies) {
 			SpecPredicate searchedTech = predicate.getDriveSpec();
 			
			Spec spec = technologies.get(searchedTech);
			
			if(spec == null) {
				return false;
 			}
			
			driveSpec = spec;
			
			boolean minAllowed = predicate.getMin() != null ? spec.getLevel() >= predicate.getMin() : true;
			boolean maxAllowed = predicate.getMax() != null ? spec.getLevel() <= predicate.getMax() : true;
			return minAllowed && maxAllowed;
 		}
 		
 		@Override
 		public FleetBound getFleetSpec() {
 			return FleetSpecImpl.this;
 		}
 		
 		@Override
 		public double getConsumption() {
 			return predicate.getConsumption() * getCount();
 		}
 		
 		@Override
 		public int getSpeed() {
 			if(driveSpec == null) {
 				throw new IllegalStateException("drive technology not set");
 			}
 			return (int) (predicate.getSpeed() * (1 + predicate.getFactor() * driveSpec.getLevel()));
 		}
 	}
 }
