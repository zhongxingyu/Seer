 package org.s23m.cell.communication.xml;
 
 import java.util.UUID;
 import org.eclipse.xtext.xbase.lib.BooleanExtensions;
 import org.s23m.cell.Set;
 import org.s23m.cell.api.models.S23MSemanticDomains;
 import org.s23m.cell.communication.xml.IdentityTriple;
 import org.s23m.cell.communication.xml.NamespaceConstants;
 import org.s23m.cell.communication.xml.NamespaceExtensions;
 import org.s23m.cell.communication.xml.XmlSchemaTerminology;
 import org.s23m.cell.communication.xml.model.dom.Namespace;
 import org.s23m.cell.communication.xml.model.schemainstance.ArtifactSet;
 import org.s23m.cell.communication.xml.model.schemainstance.CategoryIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.Command;
 import org.s23m.cell.communication.xml.model.schemainstance.ContainerIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.Edge;
 import org.s23m.cell.communication.xml.model.schemainstance.EdgeEnd;
 import org.s23m.cell.communication.xml.model.schemainstance.FromIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.Identity;
 import org.s23m.cell.communication.xml.model.schemainstance.IsAbstractIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.IsContainerIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.IsNavigableIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.MaximumCardinalityIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.MinimumCardinalityIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.Model;
 import org.s23m.cell.communication.xml.model.schemainstance.Parameter;
 import org.s23m.cell.communication.xml.model.schemainstance.Query;
 import org.s23m.cell.communication.xml.model.schemainstance.SemanticDomain;
 import org.s23m.cell.communication.xml.model.schemainstance.SemanticIdentityIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.SuperSetReference;
 import org.s23m.cell.communication.xml.model.schemainstance.ToIdentityReference;
 import org.s23m.cell.communication.xml.model.schemainstance.Vertex;
 import org.s23m.cell.communication.xml.model.schemainstance.Visibility;
 
 @SuppressWarnings("all")
 public class InstanceBuilder {
   private ArtifactSet artifactSet;
   
   private Namespace namespace;
   
   private XmlSchemaTerminology terminology;
   
   private boolean populateIdentityNameAttributes;
   
   public InstanceBuilder(final Namespace namespace, final XmlSchemaTerminology terminology, final String languageIdentifier) {
       this.namespace = namespace;
       this.terminology = terminology;
       boolean _isMachineEncoding = terminology.isMachineEncoding();
       boolean _operator_not = BooleanExtensions.operator_not(_isMachineEncoding);
       this.populateIdentityNameAttributes = _operator_not;
       ArtifactSet _artifactSet = new ArtifactSet(namespace, terminology, languageIdentifier);
       this.artifactSet = _artifactSet;
       String _xmlns = NamespaceExtensions.xmlns(NamespaceConstants.INSTANCE_NAMESPACE_PREFIX);
       this.artifactSet.setAttribute(_xmlns, NamespaceConstants.INSTANCE_SCHEMA_URI);
       String _xmlns_1 = NamespaceExtensions.xmlns(NamespaceConstants.S23M);
       this.artifactSet.setAttribute(_xmlns_1, NamespaceConstants.S23M_SCHEMA_URI);
   }
   
   public ArtifactSet build() {
     return this.artifactSet;
   }
   
   /**
    * Model
    */
   public Model model(final Set set) {
     Model _xblockexpression = null;
     {
       SemanticIdentityIdentityReference _semanticIdentity = this.semanticIdentity(set);
       final SemanticIdentityIdentityReference semanticIdentity = _semanticIdentity;
       CategoryIdentityReference _category = this.category(set);
       final CategoryIdentityReference category = _category;
       ContainerIdentityReference _container = this.container(set);
       final ContainerIdentityReference container = _container;
       IsAbstractIdentityReference _isAbstract = this.isAbstract(set);
       final IsAbstractIdentityReference isAbstract = _isAbstract;
       Model _model = this.model(semanticIdentity, category, container, isAbstract);
       _xblockexpression = (_model);
     }
     return _xblockexpression;
   }
   
   public Model model(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category, final ContainerIdentityReference container, final IsAbstractIdentityReference isAbstract) {
     Model _xblockexpression = null;
     {
       Model _model = new Model(this.namespace, this.terminology);
       final Model result = _model;
       result.setSemanticIdentity(semanticIdentity);
       result.setCategory(category);
       result.setContainer(container);
       result.setIsAbstract(isAbstract);
       this.artifactSet.addModel(result);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public Vertex vertex(final Set set) {
     Vertex _xblockexpression = null;
     {
       SemanticIdentityIdentityReference _semanticIdentity = this.semanticIdentity(set);
       final SemanticIdentityIdentityReference semanticIdentity = _semanticIdentity;
       CategoryIdentityReference _category = this.category(set);
       final CategoryIdentityReference category = _category;
       IsAbstractIdentityReference _isAbstract = this.isAbstract(set);
       final IsAbstractIdentityReference isAbstract = _isAbstract;
       MaximumCardinalityIdentityReference _maxCardinality = this.maxCardinality(set);
       final MaximumCardinalityIdentityReference maxCardinality = _maxCardinality;
       Vertex _vertex = this.vertex(semanticIdentity, category, isAbstract, maxCardinality);
       _xblockexpression = (_vertex);
     }
     return _xblockexpression;
   }
   
   public Vertex vertex(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category, final IsAbstractIdentityReference isAbstract, final MaximumCardinalityIdentityReference maxCardinality) {
     Vertex _xblockexpression = null;
     {
       Vertex _vertex = new Vertex(this.namespace, this.terminology);
       final Vertex result = _vertex;
       result.setSemanticIdentity(semanticIdentity);
       result.setCategory(category);
       result.setIsAbstract(isAbstract);
       result.setMaxCardinality(maxCardinality);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public Visibility visibility(final Set set) {
     Visibility _xblockexpression = null;
     {
       SemanticIdentityIdentityReference _semanticIdentity = this.semanticIdentity(set);
       final SemanticIdentityIdentityReference semanticIdentity = _semanticIdentity;
       CategoryIdentityReference _category = this.category(set);
       final CategoryIdentityReference category = _category;
       IsAbstractIdentityReference _isAbstract = this.isAbstract(set);
       final IsAbstractIdentityReference isAbstract = _isAbstract;
       Set _from = set.from();
       FromIdentityReference _from_1 = this.from(_from);
       final FromIdentityReference from = _from_1;
       Set _to = set.to();
       ToIdentityReference _to_1 = this.to(_to);
       final ToIdentityReference to = _to_1;
       Visibility _visibility = this.visibility(semanticIdentity, category, isAbstract, from, to);
       _xblockexpression = (_visibility);
     }
     return _xblockexpression;
   }
   
   public Visibility visibility(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category, final IsAbstractIdentityReference isAbstract, final FromIdentityReference from, final ToIdentityReference to) {
     Visibility _xblockexpression = null;
     {
       Visibility _visibility = new Visibility(this.namespace, this.terminology);
       final Visibility result = _visibility;
       result.setSemanticIdentity(semanticIdentity);
       result.setCategory(category);
       result.setIsAbstract(isAbstract);
       result.setFrom(from);
       result.setTo(to);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public Edge edge(final Set set) {
     Edge _xblockexpression = null;
     {
       SemanticIdentityIdentityReference _semanticIdentity = this.semanticIdentity(set);
       final SemanticIdentityIdentityReference semanticIdentity = _semanticIdentity;
       CategoryIdentityReference _category = this.category(set);
       final CategoryIdentityReference category = _category;
       IsAbstractIdentityReference _isAbstract = this.isAbstract(set);
       final IsAbstractIdentityReference isAbstract = _isAbstract;
       Set _fromEdgeEnd = set.fromEdgeEnd();
       EdgeEnd _fromEdgeEnd_1 = this.fromEdgeEnd(_fromEdgeEnd);
       final EdgeEnd from = _fromEdgeEnd_1;
       Set _edgeEnd = set.toEdgeEnd();
       EdgeEnd _edgeEnd_1 = this.toEdgeEnd(_edgeEnd);
       final EdgeEnd to = _edgeEnd_1;
       Edge _edge = this.edge(semanticIdentity, category, isAbstract, from, to);
       _xblockexpression = (_edge);
     }
     return _xblockexpression;
   }
   
   public Edge edge(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category, final IsAbstractIdentityReference isAbstract, final EdgeEnd from, final EdgeEnd to) {
     Edge _xblockexpression = null;
     {
       Edge _edge = new Edge(this.namespace, this.terminology);
       final Edge result = _edge;
       result.setSemanticIdentity(semanticIdentity);
       result.setCategory(category);
       result.setIsAbstract(isAbstract);
       result.setFrom(from);
       result.setTo(to);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public EdgeEnd toEdgeEnd(final Set set) {
     EdgeEnd _xblockexpression = null;
     {
       SemanticIdentityIdentityReference _semanticIdentity = this.semanticIdentity(set);
       final SemanticIdentityIdentityReference semanticIdentity = _semanticIdentity;
       CategoryIdentityReference _category = this.category(set);
       final CategoryIdentityReference category = _category;
       IsAbstractIdentityReference _isAbstract = this.isAbstract(set);
       final IsAbstractIdentityReference isAbstract = _isAbstract;
       MinimumCardinalityIdentityReference _minCardinality = this.minCardinality(set);
       final MinimumCardinalityIdentityReference minCardinality = _minCardinality;
       MaximumCardinalityIdentityReference _maxCardinality = this.maxCardinality(set);
       final MaximumCardinalityIdentityReference maxCardinality = _maxCardinality;
       IsContainerIdentityReference _isContainer = this.isContainer(set);
       final IsContainerIdentityReference isContainer = _isContainer;
       IsNavigableIdentityReference _isNavigable = this.isNavigable(set);
       final IsNavigableIdentityReference isNavigable = _isNavigable;
       EdgeEnd _edgeEnd = this.toEdgeEnd(semanticIdentity, category, isAbstract, minCardinality, maxCardinality, isContainer, isNavigable);
       _xblockexpression = (_edgeEnd);
     }
     return _xblockexpression;
   }
   
   public EdgeEnd fromEdgeEnd(final Set set) {
     EdgeEnd _xblockexpression = null;
     {
       SemanticIdentityIdentityReference _semanticIdentity = this.semanticIdentity(set);
       final SemanticIdentityIdentityReference semanticIdentity = _semanticIdentity;
       CategoryIdentityReference _category = this.category(set);
       final CategoryIdentityReference category = _category;
       IsAbstractIdentityReference _isAbstract = this.isAbstract(set);
       final IsAbstractIdentityReference isAbstract = _isAbstract;
       MinimumCardinalityIdentityReference _minCardinality = this.minCardinality(set);
       final MinimumCardinalityIdentityReference minCardinality = _minCardinality;
       MaximumCardinalityIdentityReference _maxCardinality = this.maxCardinality(set);
       final MaximumCardinalityIdentityReference maxCardinality = _maxCardinality;
       IsContainerIdentityReference _isContainer = this.isContainer(set);
       final IsContainerIdentityReference isContainer = _isContainer;
       IsNavigableIdentityReference _isNavigable = this.isNavigable(set);
       final IsNavigableIdentityReference isNavigable = _isNavigable;
       EdgeEnd _fromEdgeEnd = this.fromEdgeEnd(semanticIdentity, category, isAbstract, minCardinality, maxCardinality, isContainer, isNavigable);
       _xblockexpression = (_fromEdgeEnd);
     }
     return _xblockexpression;
   }
   
   public EdgeEnd toEdgeEnd(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category, final IsAbstractIdentityReference isAbstract, final MinimumCardinalityIdentityReference minCardinality, final MaximumCardinalityIdentityReference maxCardinality, final IsContainerIdentityReference isContainer, final IsNavigableIdentityReference isNavigable) {
     EdgeEnd _edgeEnd = EdgeEnd.toEdgeEnd(this.namespace, this.terminology);
     EdgeEnd _initialise = this.initialise(_edgeEnd, semanticIdentity, category, isAbstract, minCardinality, maxCardinality, isContainer, isNavigable);
     return _initialise;
   }
   
   public EdgeEnd fromEdgeEnd(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category, final IsAbstractIdentityReference isAbstract, final MinimumCardinalityIdentityReference minCardinality, final MaximumCardinalityIdentityReference maxCardinality, final IsContainerIdentityReference isContainer, final IsNavigableIdentityReference isNavigable) {
     EdgeEnd _fromEdgeEnd = EdgeEnd.fromEdgeEnd(this.namespace, this.terminology);
     EdgeEnd _initialise = this.initialise(_fromEdgeEnd, semanticIdentity, category, isAbstract, minCardinality, maxCardinality, isContainer, isNavigable);
     return _initialise;
   }
   
   private EdgeEnd initialise(final EdgeEnd result, final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category, final IsAbstractIdentityReference isAbstract, final MinimumCardinalityIdentityReference minCardinality, final MaximumCardinalityIdentityReference maxCardinality, final IsContainerIdentityReference isContainer, final IsNavigableIdentityReference isNavigable) {
     EdgeEnd _xblockexpression = null;
     {
       result.setSemanticIdentity(semanticIdentity);
       result.setCategory(category);
       result.setIsAbstract(isAbstract);
       result.setMinCardinality(minCardinality);
       result.setMaxCardinality(maxCardinality);
       result.setIsContainer(isContainer);
       result.setIsNavigable(isNavigable);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public SuperSetReference superSetReference(final Set set) {
     SuperSetReference _xblockexpression = null;
     {
       SemanticIdentityIdentityReference _semanticIdentity = this.semanticIdentity(set);
       final SemanticIdentityIdentityReference semanticIdentity = _semanticIdentity;
       CategoryIdentityReference _category = this.category(set);
       final CategoryIdentityReference category = _category;
       IsAbstractIdentityReference _isAbstract = this.isAbstract(set);
       final IsAbstractIdentityReference isAbstract = _isAbstract;
       Set _from = set.from();
       FromIdentityReference _from_1 = this.from(_from);
       final FromIdentityReference from = _from_1;
       Set _to = set.to();
       ToIdentityReference _to_1 = this.to(_to);
       final ToIdentityReference to = _to_1;
       SuperSetReference _superSetReference = this.superSetReference(semanticIdentity, category, isAbstract, from, to);
       _xblockexpression = (_superSetReference);
     }
     return _xblockexpression;
   }
   
   public SuperSetReference superSetReference(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category, final IsAbstractIdentityReference isAbstract, final FromIdentityReference from, final ToIdentityReference to) {
     SuperSetReference _xblockexpression = null;
     {
       SuperSetReference _superSetReference = new SuperSetReference(this.namespace, this.terminology);
       final SuperSetReference result = _superSetReference;
       result.setSemanticIdentity(semanticIdentity);
       result.setCategory(category);
       result.setIsAbstract(isAbstract);
       result.setFrom(from);
       result.setTo(to);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public Command command(final Set set) {
     Command _xblockexpression = null;
     {
       SemanticIdentityIdentityReference _semanticIdentity = this.semanticIdentity(set);
       final SemanticIdentityIdentityReference semanticIdentity = _semanticIdentity;
       CategoryIdentityReference _category = this.category(set);
       final CategoryIdentityReference category = _category;
       Command _command = this.command(semanticIdentity, category);
       _xblockexpression = (_command);
     }
     return _xblockexpression;
   }
   
   public Command command(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category) {
     Command _xblockexpression = null;
     {
       Command _command = new Command(this.namespace, this.terminology);
       final Command result = _command;
       result.setSemanticIdentity(semanticIdentity);
       result.setCategory(category);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public Query query(final Set set) {
     Query _xblockexpression = null;
     {
       SemanticIdentityIdentityReference _semanticIdentity = this.semanticIdentity(set);
       final SemanticIdentityIdentityReference semanticIdentity = _semanticIdentity;
       CategoryIdentityReference _category = this.category(set);
       final CategoryIdentityReference category = _category;
       Query _query = this.query(semanticIdentity, category);
       _xblockexpression = (_query);
     }
     return _xblockexpression;
   }
   
   public Query query(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category) {
     Query _xblockexpression = null;
     {
       Query _query = new Query(this.namespace, this.terminology);
       final Query result = _query;
       result.setSemanticIdentity(semanticIdentity);
       result.setCategory(category);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public Parameter parameter(final SemanticIdentityIdentityReference semanticIdentity, final CategoryIdentityReference category) {
     Parameter _xblockexpression = null;
     {
       Parameter _parameter = new Parameter(this.namespace, this.terminology);
       final Parameter result = _parameter;
       result.setSemanticIdentity(semanticIdentity);
       result.setCategory(category);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public SemanticIdentityIdentityReference semanticIdentity(final Set set) {
     SemanticIdentityIdentityReference _xblockexpression = null;
     {
       Set _category = set.category();
       IdentityTriple _identityTriple = this.identityTriple(_category);
       final IdentityTriple identityTriple = _identityTriple;
       SemanticIdentityIdentityReference _semanticIdentityIdentityReference = new SemanticIdentityIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_semanticIdentityIdentityReference);
     }
     return _xblockexpression;
   }
   
   public CategoryIdentityReference category(final Set set) {
     CategoryIdentityReference _xblockexpression = null;
     {
       Set _category = set.category();
       IdentityTriple _identityTriple = this.identityTriple(_category);
       final IdentityTriple identityTriple = _identityTriple;
       CategoryIdentityReference _categoryIdentityReference = new CategoryIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_categoryIdentityReference);
     }
     return _xblockexpression;
   }
   
   public ContainerIdentityReference container(final Set set) {
     ContainerIdentityReference _xblockexpression = null;
     {
       IdentityTriple _identityTriple = this.identityTriple(set);
       final IdentityTriple identityTriple = _identityTriple;
       ContainerIdentityReference _containerIdentityReference = new ContainerIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_containerIdentityReference);
     }
     return _xblockexpression;
   }
   
   public IsAbstractIdentityReference isAbstract(final Set set) {
     IsAbstractIdentityReference _xblockexpression = null;
     {
       IdentityTriple _valueIdentityTriple = this.valueIdentityTriple(set, S23MSemanticDomains.isAbstract);
       final IdentityTriple identityTriple = _valueIdentityTriple;
       IsAbstractIdentityReference _isAbstractIdentityReference = new IsAbstractIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_isAbstractIdentityReference);
     }
     return _xblockexpression;
   }
   
   public FromIdentityReference from(final Set set) {
     FromIdentityReference _xblockexpression = null;
     {
       IdentityTriple _identityTriple = this.identityTriple(set);
       final IdentityTriple identityTriple = _identityTriple;
       FromIdentityReference _fromIdentityReference = new FromIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_fromIdentityReference);
     }
     return _xblockexpression;
   }
   
   public ToIdentityReference to(final Set set) {
     ToIdentityReference _xblockexpression = null;
     {
       IdentityTriple _identityTriple = this.identityTriple(set);
       final IdentityTriple identityTriple = _identityTriple;
       ToIdentityReference _toIdentityReference = new ToIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_toIdentityReference);
     }
     return _xblockexpression;
   }
   
   public MaximumCardinalityIdentityReference maxCardinality(final Set set) {
     MaximumCardinalityIdentityReference _xblockexpression = null;
     {
       IdentityTriple _valueIdentityTriple = this.valueIdentityTriple(set, S23MSemanticDomains.maxCardinality);
       final IdentityTriple identityTriple = _valueIdentityTriple;
       MaximumCardinalityIdentityReference _maximumCardinalityIdentityReference = new MaximumCardinalityIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_maximumCardinalityIdentityReference);
     }
     return _xblockexpression;
   }
   
   public MinimumCardinalityIdentityReference minCardinality(final Set set) {
     MinimumCardinalityIdentityReference _xblockexpression = null;
     {
       IdentityTriple _valueIdentityTriple = this.valueIdentityTriple(set, S23MSemanticDomains.minCardinality);
       final IdentityTriple identityTriple = _valueIdentityTriple;
       MinimumCardinalityIdentityReference _minimumCardinalityIdentityReference = new MinimumCardinalityIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_minimumCardinalityIdentityReference);
     }
     return _xblockexpression;
   }
   
   public IsContainerIdentityReference isContainer(final Set set) {
     IsContainerIdentityReference _xblockexpression = null;
     {
       IdentityTriple _valueIdentityTriple = this.valueIdentityTriple(set, S23MSemanticDomains.isContainer);
       final IdentityTriple identityTriple = _valueIdentityTriple;
       IsContainerIdentityReference _isContainerIdentityReference = new IsContainerIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_isContainerIdentityReference);
     }
     return _xblockexpression;
   }
   
   public IsNavigableIdentityReference isNavigable(final Set set) {
     IsNavigableIdentityReference _xblockexpression = null;
     {
       IdentityTriple _valueIdentityTriple = this.valueIdentityTriple(set, S23MSemanticDomains.isNavigable);
       final IdentityTriple identityTriple = _valueIdentityTriple;
       IsNavigableIdentityReference _isNavigableIdentityReference = new IsNavigableIdentityReference(this.namespace, this.terminology, identityTriple.uniqueRepresentationReference, identityTriple.identifier, identityTriple.nameAttribute);
       _xblockexpression = (_isNavigableIdentityReference);
     }
     return _xblockexpression;
   }
   
   /**
    * Semantic Domain
    */
   public SemanticDomain semanticDomain() {
     SemanticDomain _xblockexpression = null;
     {
       SemanticDomain _semanticDomain = new SemanticDomain(this.namespace, this.terminology);
       final SemanticDomain result = _semanticDomain;
       this.artifactSet.addSemanticDomain(result);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public Identity identity(final Set set) {
     Identity _xblockexpression = null;
     {
       Identity _identity = new Identity(this.namespace, this.terminology);
       final Identity result = _identity;
       org.s23m.cell.Identity _identity_1 = set.identity();
       final org.s23m.cell.Identity identity = _identity_1;
       UUID _identifier = identity.identifier();
       String _string = _identifier.toString();
       result.setIdentifier(_string);
       String _name = identity.name();
      result.setNameAttribute(_name);
       String _pluralName = identity.pluralName();
       result.setPluralName(_pluralName);
       String _technicalName = identity.technicalName();
       result.setTechnicalName(_technicalName);
       String _payload = identity.payload();
       result.setPayload(_payload);
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   private IdentityTriple valueIdentityTriple(final Set set, final Set variable) {
     IdentityTriple _xblockexpression = null;
     {
       Set _value = set.value(variable);
       final Set retrievedValue = _value;
       IdentityTriple _identityTriple = this.identityTriple(retrievedValue);
       _xblockexpression = (_identityTriple);
     }
     return _xblockexpression;
   }
   
   private IdentityTriple identityTriple(final Set set) {
     IdentityTriple _xblockexpression = null;
     {
       org.s23m.cell.Identity _identity = set.identity();
       final org.s23m.cell.Identity identity = _identity;
       UUID _uniqueRepresentationReference = identity.uniqueRepresentationReference();
       String _uuid = this.uuid(_uniqueRepresentationReference);
       final String uniqueRepresentationReference = _uuid;
       UUID _identifier = identity.identifier();
       String _uuid_1 = this.uuid(_identifier);
       final String identifier = _uuid_1;
       String _xifexpression = null;
       if (this.populateIdentityNameAttributes) {
         String _name = identity.name();
         _xifexpression = _name;
       } else {
         _xifexpression = null;
       }
       final String nameAttribute = _xifexpression;
       IdentityTriple _identityTriple = new IdentityTriple(uniqueRepresentationReference, identifier, nameAttribute);
       _xblockexpression = (_identityTriple);
     }
     return _xblockexpression;
   }
   
   private String uuid(final UUID uuid) {
     String _string = uuid.toString();
     return _string;
   }
 }
