 package uk.ac.ebi.fg.core_model.expgraph;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSet.Builder;
 
 /**
  * 
  * <p>An experimental product is some initial entity used to perform a biomedical experiment, or some final outcome, such as a
  * data file, or some intermediate entitiy, such as a microarray hybridization output or a treated sample.</p>
  * 
  * <p>As explained in {@link Node}, a Product can be related to other products it derives from (or produces), either by 
  * a direct '{@link #getDerivedFrom() derived}' relation, or through the {@link Process} that determined such derivation.</p>
  * 
  * <p><b>PLEASE NOTE</b>: This means the 'derived' relation is new and different than upstream/downstream nodes and processes. 
  * In particular, when you make a product to derive from a process that has certain inputs, these inputs implicitly 'derives into' the
  * product. However we don't manage this implicit relation at all, cause it is too complicated and often not really useful. Concerning this
  * we just provide an '{@link #getAllDerivedFrom() all derived}' relation, which makes this type of inference (but it produces 
  * a copy, it's not a dynamic view).</p> 
  *
  * <p>EP is used to mark the type of property values a node can be associated to (e.g, bio-characteristics, data properties).</p>
  * 
  * <p>Product is mapped with {@link InheritanceType#SINGLE_TABLE} strategy, because: 1) TABLE_PER_CLASS makes it very
  * difficult to deal with the Java Generics used in several subclasses and 2) JOINED at the moment generates secondary
  * tables that are almost empty, being the attribute sets for the various subclasses very similar.</p>
  * 
  * <dl><dt>date</dt><dd>Jun 29, 2012</dd></dl>
  * @author Marco Brandizi
  *  
  */
 @Entity
 @Table ( name = "bio_product" )
 @Inheritance( strategy = InheritanceType.SINGLE_TABLE )
 @DiscriminatorColumn ( name = "product_type" )
 @DiscriminatorValue ( "generic_bio_product" )
 @SuppressWarnings ( { "rawtypes", "unchecked" } )
 public abstract class Product<EP extends ExperimentalPropertyValue> extends Node<Process, Process>
 {
 	private Set<Product> derivedFrom = new HashSet<Product> (); 
 	private Set<Product> derivedInto = new HashSet<Product> ();
 	
 	private Collection<EP> propertyValues = new ArrayList<EP> ();
 
 	public Product () {
 		super ();
 	}
 
 	public Product ( String acc )
 	{
 		super ( acc );
 	}
 	
 	
 	
 	/**
 	 * As in {@link Process#getOutputs()} this is managed via {@link #getUpstreamNodes()} and hence in a coordinate fashion.
 	 * See {@link Process#getInputs()} for information about ORM.
 	 * See {@link #getDerivedFrom()} for notes about cascading of persistence operations.
 	 */
 	@ManyToMany ( mappedBy = "outputs", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
 	public Set<Process> getUpstreamProcesses ()
 	{
 		return this.getUpstreamNodes ();
 	}
 
 	/**
 	 * As in {@link Process#getOutputs()} this is managed via {@link #getUpstreamNodes()} and hence in a coordinate fashion.
 	 */
 	protected void setUpstreamProcesses ( Set<Process> upstreamProcs )
 	{
 		this.setUpstreamNodes ( upstreamProcs );
 	}
 
 	/**
 	 * As in {@link Process#getOutputs()} this is managed via {@link #getUpstreamNodes()} and hence in a coordinate fashion.
 	 */
 	public boolean addUpstreamProcess ( Process proc ) 
 	{
 		return this.addUpstreamNode ( proc );
 	}
 	
 	/**
 	 * As in {@link Process#getOutputs()} this is managed via {@link #getUpstreamNodes()} and hence in a coordinate fashion.
 	 */
 	public boolean removeUpstreamProcess ( Process proc )
 	{
 		return this.removeUpstreamNode ( proc );
 	}
 
 	
 	/**
 	 * As in {@link Process#getInputs()} this is managed via {@link #getDownstreamNodes()} and hence in a coordinate fashion.
 	 * See {@link Process#getInputs()} for information about ORM.
 	 * See {@link #getDerivedFrom()} for notes about cascading of persistence operations.
 	 */
 	@ManyToMany ( mappedBy = "inputs", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
 	public Set<Process> getDownstreamProcesses ()
 	{
 		return this.getDownstreamNodes ();
 	}
 
 	/**
 	 * As in {@link Process#getInputs()} this is managed via {@link #getDownstreamNodes()} and hence in a coordinate fashion.
 	 */
 	protected void setDownstreamProcesses ( Set<Process> downstreamProcs )
 	{
 		this.setDownstreamNodes ( downstreamProcs );
 	}
 
 	/**
 	 * As in {@link Process#getInputs()} this is managed via {@link #getDownstreamNodes()} and hence in a coordinate fashion.
 	 */
 	public boolean addDownstreamProcess ( Process proc ) 
 	{
 		return this.addDownstreamNode ( proc );
 	}
 	
 	/**
 	 * As in {@link Process#getInputs()} this is managed via {@link #getDownstreamNodes()} and hence in a coordinate fashion.
 	 */
 	public boolean removeDownstreamProcess ( Process proc )
 	{
 		return this.removeDownstreamNode ( proc );
 	}
 	
 	/**
 	 * <p>As explained in the class header this is a new relation, which supports the simple provenance model see 
 	 * (see {@link Node} too) and is unrelated to {@link #getUpstreamProcesses()} or {@link #getUpstreamNodes()}.</p>
 	 * 
 	 * <p><b>PLEASE NOTE</b>: this relation doesn't cascade {@link CascadeType#REMOVE}. Wether you want or not this, depends 
 	 * on the way you updated workflows. If you always store/remove a whole graph at a time, you will want to cascade deletions. 
 	 * If you expect to remove single nodes from existing graphs, then you obviously don't want this type of update.</p>
 	 * 
 	 */
 	@ManyToMany ( targetEntity = Product.class, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
 	@JoinTable ( name = "product_direct_derivation", 
 	  joinColumns = @JoinColumn ( name = "to_id" ), inverseJoinColumns = @JoinColumn ( name = "from_id" ) )
 	public <P extends Product> Set<P> getDerivedFrom ()
 	{
 		return (Set<P>) derivedFrom;
 	}
 
 	/**
 	 * As explained in the class header this is a new relation, which supports the simple provenance model see 
 	 * (see {@link Node} too) and is unrelated to {@link #getUpstreamProcesses()} or {@link #getUpstreamNodes()}.
 	 */
 	protected void setDerivedFrom ( Set<Product> products )
 	{
 		this.derivedFrom = products;
 	}
 
 	/**
 	 * Changes are coordinated with {@link #getDerivedInto()}.
 	 */
 	public boolean addDerivedFrom ( Product product ) 
 	{
 		if ( !this.derivedFrom.add ( product ) ) return false;
		product.addDerivedInto ( this );
 		return true;
 	}
 	
 	/**
 	 * Changes are coordinated with {@link #getDerivedInto()}.
 	 */
 	public boolean removeDerivedFrom ( Product product )
 	{
 		if ( !this.derivedFrom.remove ( product ) ) return false;
		product.removeDerivedInto ( this );
 		return true;
 	}
 
 	
 	/**
 	 * As explained in the class header this is a new relation, which supports the simple provenance model see 
 	 * (see {@link Node} too) and is unrelated to {@link #getDownstreamProcesses()} or {@link #getDownstreamNodes()}.
 	 * 
 	 * See {@link #getDerivedFrom()} for notes about cascading of persistence operations.
 	 */
 	@ManyToMany ( targetEntity = Product.class, mappedBy = "derivedFrom", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
 	public <P extends Product> Set<P> getDerivedInto ()
 	{
 		return (Set<P>) derivedInto;
 	}
 
 	/**
 	 * As explained in the class header this is a new relation, which supports the simple provenance model see 
 	 * (see {@link Node} too) and is unrelated to {@link #getDownstreamProcesses()} or {@link #getDownstreamNodes()}.
 	 */
 	protected void setDerivedInto ( Set<Product> derivedInto )
 	{
 		this.derivedInto = derivedInto;
 	}
 
 	/**
 	 * Changes are coordinated with {@link #getDerivedFrom()}.
 	 */
 	public boolean addDerivedInto ( Product product ) 
 	{
 		if ( !this.derivedInto.add ( product ) ) return false;
		product.addDerivedFrom ( this );
 		return true;
 	}
 	
 	/**
 	 * Changes are coordinated with {@link #getDerivedFrom()}.
 	 */
 	public boolean removeDerivedInto ( Product product )
 	{
 		if ( !this.derivedInto.remove ( product ) ) return false;
		product.removeDerivedFrom ( this );
 		return true;
 	}
 
 	/**
 	 * Creates an immutable set containing the union of {@link #getUpstreamNodes()} and {@link #getDerivedFrom()}. Note this
 	 * is not a dynamic view over such relations, this would be too complicated to be implemented. 
 	 */
 	@Transient
 	public <P extends Product> Set<P> getAllDerivedFrom ()
 	{
 		Builder<P> builder = ImmutableSet.builder ();
 		builder.addAll ( (Set<P>) this.derivedFrom );
 		for ( Process proc: this.getUpstreamProcesses () )
 			builder.addAll ( proc.getInputs () );
 		return builder.build ();
 	}
 	
 	/**
 	 * Creates an immutable set containing the union of {@link #getUpstreamNodes()} and {@link #getDerivedFrom()}. Note this
 	 * is not a dynamic view over such relations, this would be too complicated to be implemented.
 	 */
 	@Transient
 	public <P extends Product> Set<P> getAllDerivedInto ()
 	{
 		Builder<P> builder = ImmutableSet.builder ();
 		builder.addAll ( (Set<P>) this.derivedInto );
 		for ( Process proc: this.getDownstreamProcesses () )
 			builder.addAll ( proc.getOutputs () );
 		return builder.build ();
 	}
 
 	/**
 	 * A product can have associated properties, such as bio-characteristics or data properties. More specific types 
 	 * are bound to the generic returned in the sub-classes of this class.
 	 * 
 	 */
 	@OneToMany ( targetEntity = ExperimentalPropertyValue.class, cascade = CascadeType.ALL, orphanRemoval = true )
 	@JoinColumn ( name = "owner_id" )
 	public Collection<EP> getPropertyValues ()
 	{
 		return propertyValues;
 	}
 
 	/**
 	 * A product can have associated properties, such as bio-characteristics or data properties. More specific types 
 	 * are bound to the generic returned her in the sub-classes of this class.
 	 */
 	public void setPropertyValues ( Collection<EP> propertyValues )
 	{
 		this.propertyValues = propertyValues;
 	}
 	
 	/**
 	 * A product can have associated properties, such as bio-characteristics or data properties. More specific types 
 	 * are bound to the generic returned her in the sub-classes of this class. 
 	 */
 	public boolean addPropertyValue ( EP pval ) {
 		return this.propertyValues.add ( pval );
 	}
 	
 
 	@Override
   public String toString() 
   {
   	return String.format ( 
   		"%s { id: %d, acc: %s, properties:\n  %s\n}", 
   		this.getClass ().getSimpleName (), this.getId (), this.getAcc (), this.getPropertyValues () 
   	);
   }
 
 }
