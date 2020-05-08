 package uk.ac.ebi.fg.core_model.xref;
 
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import org.hibernate.annotations.Index;
 
 import uk.ac.ebi.fg.core_model.resources.Const;
 import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
 import uk.ac.ebi.fg.core_model.toplevel.Accessible;
 import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
 
 
 /**
  * The description of a (external) reference source, such as PUBMED, GEO, the EFO ontology. This provides context to 
  * other reference entities, such as {@link XRef} or {@link OntologyEntry}. 
  *
  * It is assumed that every source has a different accession, presumably assigned by the database curators.
  * 
  * Note that this class doesn't extend {@link Accessible}, because in general you need both an accession and a version 
  * to identify a source. It's up to the application to be such fine-grained or to assume unique accessions for reference
  * sources.  
  * 
  * TODO: this class has a version property, but, apart from that, it doesn't really support versioning. You cannot
  * define a version order and the only mechanism you can use to refer to the last version is give null as version value.   
  *
  * <dl><dt>date</dt><dd>Jun 14, 2012, imported from the AE2 model</dd></dl>
  * 
  * @author Nataliya Sklyar
  * @author Marco brandizi
  *
  */
 @Entity
 @Inheritance ( strategy = InheritanceType.TABLE_PER_CLASS )
 @Table( name = "reference_source", uniqueConstraints = @UniqueConstraint ( columnNames = { "acc", "version" } ) )
 /* This is only needed when you remove the unique constraint above, otherwise the index it's created automatically 
 @org.hibernate.annotations.Table (   
 	appliesTo = "reference_source", 
 	indexes = {	@Index ( name = "refsrc_acc_ver", columnNames = { "acc", "version" } ) }
 )*/
 public class ReferenceSource extends Identifiable
 {
   private String acc;
 
   private String name;
   private String url;
   private String version;
   private String description;
 
   protected ReferenceSource() {
   }
 
   public ReferenceSource ( String acc, String version ) {
   	this.acc = acc;
   	this.version = version;
   }
 
   @Index ( name = "refsrc_acc" )
 	@Column( unique = false, nullable = false, length = Const.COL_LENGTH_L) // We need it long, cause it can contains URIs
 	public String getAcc ()
 	{
 		return acc;
 	}
 
 	protected void setAcc ( String acc )
 	{
 		this.acc = acc;
 	}
 
   @Index ( name = "refsrc_name" )
   @Column ( length = Const.COL_LENGTH_M )
 	public String getName ()
 	{
 		return name;
 	}
 
 	public void setName ( String name )
 	{
 		this.name = name;
 	}
 
   @Column ( length = 255 )
   @Index ( name = "refsrc_url" )
 	public String getUrl ()
 	{
 		return url;
 	}
 
 	public void setUrl ( String url )
 	{
 		this.url = url;
 	}
 
   @Column ( length = Const.COL_LENGTH_S )
   @Index ( name = "refsrc_ver" )
 	public String getVersion ()
 	{
 		return version;
 	}
 
 	protected void setVersion ( String version )
 	{
 		this.version = version;
 	}
 
   @Column ( length = Const.COL_LENGTH_XL )
   @Index ( name = "refsrc_descr" )
 	public String getDescription ()
 	{
 		return description;
 	}
 
 	public void setDescription ( String description )
 	{
 		this.description = description;
 	}
 
   /**
    * If either accession is null (which should never happen), they're different. If they've the same non null accession,  
    * it compares the versions too.
    * 
    */
   @Override
   public boolean equals ( Object o ) 
   {
   	if ( o == null ) return false;
   	if ( this == o ) return true;
   	if ( this.getClass () != o.getClass () ) return false;
   	
     // Compare accessions if both are non-null, use identity otherwise
   	ReferenceSource that = (ReferenceSource) o;
    	
   	if ( this.getAcc() == null ) return false;
   	if ( !this.acc.equals ( that.getAcc () ) ) return false;
   	
    return this.getVersion () != null ? this.version.equals ( that.getVersion () ) : that.getVersion () == null; 
   }
   
   @Override
   public int hashCode() 
   {
   	return this.getAcc () == null 
   		? super.hashCode () 
   		: this.acc.hashCode () * 31 + ( this.getVersion () == null ? 0 : this.version.hashCode () );
   }	
 	
 	
 	@Override
 	public String toString ()
 	{
 		return String.format ( 
 			"%s { id: %d, acc: '%s', name: '%s', url: '%s', version: '%s', description: '%.15s' }",
 			this.getClass ().getSimpleName (), getId (), getAcc (), getName (), getUrl (), getVersion (), getDescription () 
 		);
 	}
 
 }
