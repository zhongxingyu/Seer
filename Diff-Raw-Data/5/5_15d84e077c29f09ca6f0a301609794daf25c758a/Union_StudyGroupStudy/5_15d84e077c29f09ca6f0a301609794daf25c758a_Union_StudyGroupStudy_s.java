 package org.gesis.discovery;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.OneToOne;
 
 import org.gesis.dcterms.Location;
 import org.gesis.dcterms.PeriodOfTime;
 import org.gesis.foaf.Agent;
 import org.gesis.foaf.Document;
 import org.gesis.rdf.LangString;
 import org.gesis.rdfs.Resource;
 import org.gesis.skos.Concept;
 
 @MappedSuperclass
 public abstract class Union_StudyGroupStudy extends Resource
 {
 	// properties
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	@JoinColumn( name = "abstract_id" )
 	private LangString abstract_;
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private LangString alternative;
 
 	@Column
 	private Date available;
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private LangString title;
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private LangString purpose;
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private LangString subtitle;
 
 	// relations
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private List<PeriodOfTime> temporal;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private List<Concept> subject;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private List<Location> spacial;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private List<Document> ddiFile;
 
 	@ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	@JoinColumn( name="concept_id" )
 	private Concept kindOfData;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private List<AnalysisUnit> analysisUnit;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private List<Universe> universe;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	@JoinTable(
 			name = "Study_Agent_Publisher",
 			joinColumns = @JoinColumn( name = "study_id" ),
 			inverseJoinColumns = @JoinColumn( name = "publisher_id" ) )
 	private List<Agent> publisher;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	@JoinTable(
 name = "Study_Agent_Contributor",
 			joinColumns = @JoinColumn( name = "study_id" ),
 inverseJoinColumns = @JoinColumn( name = "contributor_id" ) )
 	private List<Agent> contributor;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	@JoinTable(
 			name = "Study_Agent_Creator",
 			joinColumns = @JoinColumn( name = "study_id" ),
 			inverseJoinColumns = @JoinColumn( name = "creator_id" ) )
 	private List<Agent> creator;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	@JoinTable(
 			name = "Study_Agent_FundedBy",
 			joinColumns = @JoinColumn( name = "study_id" ),
 			inverseJoinColumns = @JoinColumn( name = "fundedBy_id" ) )
 	private List<Agent> fundedBy;
 
 	// getter/setter
 
 	public LangString getAbstract()
 	{
 		return abstract_;
 	}
 
 	public void setAbstract( final LangString dcterms_abstract )
 	{
 		abstract_ = dcterms_abstract;
 	}
 
 	public LangString getAlternative()
 	{
 		return alternative;
 	}
 
 	public void setAlternative( final LangString dcterms_alternative )
 	{
 		alternative = dcterms_alternative;
 	}
 
 	public Date getAvailable()
 	{
 		return available;
 	}
 
 	public void setAvailable( final Date dcterms_available )
 	{
 		available = dcterms_available;
 	}
 
 	public LangString getTitle()
 	{
 		return title;
 	}
 
 	public void setTitle( final LangString dcterms_title )
 	{
 		title = dcterms_title;
 	}
 
 	public LangString getPurpose()
 	{
 		return purpose;
 	}
 
 	public void setPurpose( final LangString purpose )
 	{
 		this.purpose = purpose;
 	}
 
 	public LangString getSubtitle()
 	{
 		return subtitle;
 	}
 
 	public void setSubtitle( final LangString subtitle )
 	{
 		this.subtitle = subtitle;
 	}
 
 	public List<PeriodOfTime> getTemporal()
 	{
 		return temporal;
 	}
 
 	public void setTemporal( final List<PeriodOfTime> dcterms_temporal )
 	{
 		temporal = dcterms_temporal;
 	}
 
 	public Union_StudyGroupStudy addTemporal( final PeriodOfTime periodOfTime )
 	{
 		if ( temporal == null )
 			temporal = new ArrayList<PeriodOfTime>();
 
 		temporal.add( periodOfTime );
 
 		return this;
 	}
 
 	public List<Concept> getSubject()
 	{
 		return subject;
 	}
 
 	public void setSubject( final List<Concept> dcterms_subject )
 	{
 		subject = dcterms_subject;
 	}
 
 	public Union_StudyGroupStudy addSubject( final Concept concept )
 	{
 		if ( subject == null )
 			subject = new ArrayList<Concept>();
 
 		subject.add( concept );
 
 		return this;
 	}
 
 	public List<Location> getSpacial()
 	{
 		return spacial;
 	}
 
 	public void setSpacial( final List<Location> dcterms_spacial )
 	{
 		spacial = dcterms_spacial;
 	}
 
 	public Union_StudyGroupStudy addSpatial( final Location location )
 	{
 		if ( spacial == null )
 			spacial = new ArrayList<Location>();
 
 		spacial.add( location );
 
 		return this;
 	}
 
 	public List<Document> getDdiFile()
 	{
 		return ddiFile;
 	}
 
 	public void setDdiFile( final List<Document> ddiFile )
 	{
 		this.ddiFile = ddiFile;
 	}
 
 	public Union_StudyGroupStudy addDdiFile( final Document document )
 	{
 		if ( ddiFile == null )
 			ddiFile = new ArrayList<Document>();
 
 		ddiFile.add( document );
 
 		return this;
 	}
 
 	public Concept getKindOfData()
 	{
 		return kindOfData;
 	}
 
 	public void setKindOfData( final Concept kindOfData )
 	{
 		this.kindOfData = kindOfData;
 	}
 
 	public List<Universe> getUniverse()
 	{
 		return universe;
 	}
 
 	public void setUniverse( final List<Universe> universe )
 	{
 		this.universe = universe;
 	}
 
 	public Union_StudyGroupStudy addUniverse( final Universe universe )
 	{
 		if ( this.universe == null )
 			this.universe = new ArrayList<Universe>();
 
 		this.universe.add( universe );
 
 		return this;
 	}
 
 	public List<AnalysisUnit> getAnalysisUnit()
 	{
 		return analysisUnit;
 	}
 
 	public void setAnalysisUnit( final List<AnalysisUnit> analysisUnit )
 	{
 		this.analysisUnit = analysisUnit;
 	}
 
 	public Union_StudyGroupStudy addAnalysisUnit( final AnalysisUnit analysisUnit )
 	{
 		if ( this.analysisUnit == null )
 			this.analysisUnit = new ArrayList<AnalysisUnit>();
 
 		this.analysisUnit.add( analysisUnit );
 
 		return this;
 	}
 
 	public List<Agent> getPublisher()
 	{
 		return publisher;
 	}
 
 	public void setPublisher( final List<Agent> publisher )
 	{
 		this.publisher = publisher;
 	}
 
 	public Union_StudyGroupStudy addPublisher( final Agent agent )
 	{
 		if ( publisher == null )
 			publisher = new ArrayList<Agent>();
 
 		publisher.add( agent );
 
 		return this;
 	}
 
 	public List<Agent> getContributor()
 	{
 		return contributor;
 	}
 
 	public void setContributor( final List<Agent> contributor )
 	{
 		this.contributor = contributor;
 	}
 
 	public Union_StudyGroupStudy addContributor( final Agent agent )
 	{
 		if ( contributor == null )
 			contributor = new ArrayList<Agent>();
 
 		contributor.add( agent );
 
 		return this;
 	}
 
 	public List<Agent> getCreator()
 	{
 		return creator;
 	}
 
 	public void setCreator( final List<Agent> creator )
 	{
 		this.creator = creator;
 	}
 
 	public Union_StudyGroupStudy addCreator( final Agent agent )
 	{
 		if ( creator == null )
 			creator = new ArrayList<Agent>();
 
 		creator.add( agent );
 
 		return this;
 	}
 
 	public List<Agent> getFundedBy()
 	{
 		return fundedBy;
 	}
 
 	public void setFundedBy( final List<Agent> fundedBy )
 	{
 		this.fundedBy = fundedBy;
 	}
 
 	public Union_StudyGroupStudy addFundedBy( final Agent agent )
 	{
 		if ( fundedBy == null )
 			fundedBy = new ArrayList<Agent>();
 
 		fundedBy.add( agent );
 
 		return this;
 	}
 
 }
