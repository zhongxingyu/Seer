 package uk.ac.ebi.fg.core_model.expgraph;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Transient;
 
 import uk.ac.ebi.fg.core_model.toplevel.DefaultAccessibleAnnotatable;
 
 /**
  * <p>An experimental workflow graph node. This may be things like Biomaterial, Processing/ProtocolApplication etc.
  * A node in the core model is essentially something able to tell provenance information, i.e. upstream/downstream 
  * nodes. The core model supports two different approaches to track provenance in bimedical experiments: 
  * 
  * <ul>
  *   <li>One is simplified, only experimental {@link Product} are represented, together with information about how they
  *   are derived each other, via the {@link Product#getDerivedFrom() derived relation}.</li> 
  *   <li>The other is more detailed and gives account of the processes that allowed to achieve an experimental product 
  *   from another one. This is done via the concept of {@link Process}, which sits between two (or many) proucts.</li>
  * </ul>
  * 
  * Look at lower levels of the hierarchy for more information about the two different approaches. Note that while mixing the 
  * two approaches is technically feasible, we have not designed this model with this scenario in mind (i.e., you'll need
  * some testing).</p>
  *
  * <p>U and D are the type of nodes accepted by the current one as upstream nodes (which the node is derived from) and 
  * downstream nodes (which derives from the current node). More specific subclasses of this bind these generic types 
  * to suitable classes.</p>
  * 
  * <p>Note that that these properties are {@link Transient} wrt JPA, we cannot easily manage a polymorphic ORM for it. 
  * This (unfortunately) implies that it won't be available in HQL or similar OQ languages. You will have to use the 
  * specific properties in them.</p> 
  *
  * <dl><dt>date</dt><dd>Jun 28, 2012</dd></dl>
  * @author Marco Brandizi. This is roughly equivalent to GraphElement in the original AE2 model.
  * 
  * TODO: null nodes validation. They should never be used.
  *
  */
 @MappedSuperclass
 @SuppressWarnings ( "rawtypes" )
 public abstract class Node<U extends Node, D extends Node> extends DefaultAccessibleAnnotatable
 {
 	private Set<U> upstreamNodes = new HashSet<U> ();
 	private Set<D> downstreamNodes = new HashSet<D> ();
 
 	public Node () {
 		super ();
 	}
 
 	public Node ( String acc )
 	{
 		super ( acc );
 	}
 
 	@Override
 	@Transient
 	public String getAcc ()
 	{
 		String acc = super.getAcc ();
 		return acc == null ? getId ().toString () : acc;
 	}
 
 	@Transient
 	public Set<U> getUpstreamNodes ()
 	{
 		//return Collections.unmodifiableSet ( upstreamNodes );
 		return upstreamNodes;
 	}
 
 	/**
 	 * This is made protected (i.e., left accessible to JPA/Hibernate) cause changes to this relation are coordinated with 
 	 * its symmetric side (down-stream nodes). 
 	 */
 	protected void setUpstreamNodes ( Set<U> upstreamNodes )
 	{
 		this.upstreamNodes = upstreamNodes;
 	}
 
 	/**
 	 * Adds 'this' to node.upstreamNodes too. True if the node weren't already in the upstreams. 
 	 */
 	@SuppressWarnings ( "unchecked" )
 	public boolean addUpstreamNode ( U node )
 	{
 		if ( !this.upstreamNodes.add ( node ) ) return false;
		node.downstreamNodes.add ( this );
 		return true;
 	}
 	
 	/**
 	 * Remove 'this' from node.upstreamNodes too. True if the node were actually in the upstreams.
 	 */
 	public boolean removeUpstreamNode ( U node )
 	{
 		if ( !this.upstreamNodes.remove ( node ) ) return false;
		node.downstreamNodes.remove ( this );
 		return true;
 	}
 	
 	@Transient
 	public Set<D> getDownstreamNodes ()
 	{
 		// return Collections.unmodifiableSet ( downstreamNodes );
 		return downstreamNodes;
 	}
 
 	/**
 	 * This is made protected (i.e., left accessible to JPA/Hibernate) cause changes to this relation are coordinated with 
 	 * its symmetric side (up-stream nodes). 
 	 */
 	protected void setDownstreamNodes ( Set<D> downstreamNodes )
 	{
 		this.downstreamNodes = downstreamNodes;
 	}
 	
 	/**
 	 * Adds the 'this' to node.upstreamNodes too. True if the node weren't already in the downstreams. 
 	 */
 	@SuppressWarnings ( "unchecked" )
 	public boolean addDownstreamNode ( D node )
 	{
 		if ( !this.downstreamNodes.add ( node ) ) return false;
		node.upstreamNodes.add ( this );
 		return true;
 	}
 	
 	/**
 	 * Remove 'this' from node.upstreamNodes too. True if the node were actually in the upstreams.
 	 */
 	public boolean removeDownstreamNode ( D node )
 	{
 		if ( !this.downstreamNodes.remove ( node ) ) return false;
		node.upstreamNodes.remove ( this );
 		return true;
 	}
 
 }
