 package se2.e.engine3d.j3d;
 
 import javax.media.j3d.Behavior;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.TransformGroup;
 
 /**
  * The DynamicBranch holds all the required references for a dynamic object represented in the 3D space. It is only a
  * holder and it's the responsibility of the user to make the appropriate connections.
  * 
  * @author cosmin
  */
 public class DynamicBranch {
 
 	/** The {@link TransformGroup} holding the 3D object. */
 	private TransformGroup transformGroup;
 
 	/** The branch group. */
 	private BranchGroup branchGroup;
 
 	/** The behavior node. */
 	private Behavior behaviorNode;
 
 	/**
 	 * Gets the behavior node.
 	 * 
 	 * @return the behavior node
 	 */
 	public Behavior getBehaviorNode() {
 		return behaviorNode;
 	}
 
 	/**
 	 * Sets the behavior node.
 	 * 
 	 * @param behaviorNode the new behavior node
 	 */
 	public void setBehaviorNode(Behavior behaviorNode) {
 		this.behaviorNode = behaviorNode;
 	}
 
 	/**
 	 * Gets the transform group.
 	 * 
 	 * @return the transform group
 	 */
 	public TransformGroup getTransformGroup() {
 		return transformGroup;
 	}
 
 	/**
 	 * Gets the branch group.
 	 * 
 	 * @return the branch group
 	 */
 	public BranchGroup getBranchGroup() {
 		return branchGroup;
 	}
 
 	/**
 	 * Instantiates a new dynamic branch.
 	 * 
 	 * @param transformGroup the transform group
 	 * @param branchGroup the branch group
 	 */
 	public DynamicBranch(BranchGroup branchGroup, TransformGroup transformGroup) {
 		super();
 		this.transformGroup = transformGroup;
 		this.branchGroup = branchGroup;
 		this.behaviorNode = null;
 	}
 
 }
