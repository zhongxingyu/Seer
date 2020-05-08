 package cz.muni.fi.fresneleditor.gui.mod.lens2;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.DefaultListModel;
 
 import org.openrdf.model.Literal;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.LiteralImpl;
 import org.openrdf.model.impl.StatementImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 
 import cz.muni.fi.fresneleditor.common.ContextHolder;
 import cz.muni.fi.fresneleditor.common.utils.FresnelUtils;
 import cz.muni.fi.fresneleditor.gui.mod.lens2.model.LensSelector;
 import cz.muni.fi.fresneleditor.gui.mod.lens2.model.LensSelector.LensDomain;
 import cz.muni.fi.fresneleditor.model.IModel;
 import fr.inria.jfresnel.Group;
 import fr.inria.jfresnel.Lens;
 import fr.inria.jfresnel.PropertyVisibility;
 import fr.inria.jfresnel.fsl.FSLPath;
 import fr.inria.jfresnel.sparql.SPARQLQuery;
 import java.util.Collection;
 
 /**
  * Class that represents Fresnel Lens object.
  * 
  * @author Igor Zemsky (zemsky@mail.muni.cz)
  */
 public class LensModel implements IModel {
 
 	private static final Logger LOG = LoggerFactory.getLogger(LensModel.class);
 
 	private static final int ALL_PROPERTIES_NOT_DEFINED = -1;
 
 	private int allPropertiesIndex = ALL_PROPERTIES_NOT_DEFINED;
 
 	/**
 	 * The {@link Lens} that constitutes the data of this model. Can be null if
 	 * this model is custom created.
 	 */
 	// exclude the fresnelLens from equals() method
 	private final Lens fresnelLens;
 
 	private final String lensUri;
 
 	private Literal comment;
 
 	private LensSelector selector;
 	private short purpose = Lens.PURPOSE_UNDEFINED;
 
 	private List<PropertyVisibilityWrapper> showProperties = new ArrayList<PropertyVisibilityWrapper>();
 	private List<PropertyVisibilityWrapper> hideProperties = new ArrayList<PropertyVisibilityWrapper>();
 
 	private List<Group> groups;
 	private Literal label;
 
 	/**
 	 * Create new lens model backed up with data from lens.
 	 * 
 	 * @param lens
 	 */
 	public LensModel(Lens lens) {
 		this(lens, null);
 	}
 
 	/**
 	 * Creates new empty lens model.
 	 * 
 	 * @param lensUri
 	 */
 	public LensModel(String lensUri) {
 		this(null, lensUri);
 	}
 
 	/**
 	 * Creates empty model (lens is null) or a model which is constituted from
 	 * lens data (lensUri is null).
 	 * 
 	 * @param lens
 	 * @param lensUri
 	 * <br>
 	 *            either lens or lensUri has to be null
 	 */
 	private LensModel(Lens lens, String lensUri) {
 		Assert.isTrue((lens == null && lensUri != null)
 				|| (lens != null && lensUri == null));
 
 		this.fresnelLens = lens;
 		this.lensUri = lens != null ? lens.getURI() : lensUri;
 
 		if (lens != null) {
 			initLensModel(lens);
 		}
 	}
 
 	private void initLensModel(Lens lens) {
 		initDomain(lens);
 		initProperties(lens);
 		this.purpose = lens.getPurpose();
		this.groups = (List<Group>)lens.getAssociatedGroups();
 		if (StringUtils.hasText(lens.getComment())) {
 			// fixme igor: jFresnel does not parse the language information for
 			// comment
 			this.setComment(new LiteralImpl(lens.getComment()));
 		}
 		// fixme igor: jFresnel does not parse the language information for
 		// label
 		// so we are setting a label this way
 		if (StringUtils.hasText(lens.getLabel())) {
 			this.setLabel(new LiteralImpl(lens.getLabel()));
 		}
 
 	}
 
 	public static DefaultListModel getProperties(URI uri, boolean isForClass) {
 		DefaultListModel ret = new DefaultListModel();
 		List<Value> properties;
 		if (isForClass)
 			properties = ContextHolder.getInstance().getDataRepositoryDao()
 					.getPropertiesForClass(uri);
 		else
 			properties = ContextHolder.getInstance().getDataRepositoryDao()
 					.getPropertiesForInstance(uri);
 		PropertyVisibilityWrapper propVis;
 		for (Value mem : properties) {
 			propVis = new PropertyVisibilityWrapper();
 			propVis.setFresnelPropertyValueURI(mem.toString());
 			ret.addElement(propVis);
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the lens which backs up this model.
 	 * 
 	 * @return the lens which backs up this model. Can return null if this model
 	 *         was initially created as empty model with
 	 *         {@link LensModel#LensModel(String)}
 	 */
 	public Lens getLens() {
 		return fresnelLens;
 	}
 
 	public void setPurpose(short purpose) {
 		this.purpose = purpose;
 	}
 
 	public short getPurpose() {
 		return purpose;
 	}
 
 	public void setSelector(LensSelector selector) {
 		this.selector = selector;
 	}
 
 	/**
 	 * Returns the wrapper object for the selector value. To access the value it
 	 * self use {@link LensSelector#asObject()}.
 	 * 
 	 * @return the wrapper object of the selector value <br>
 	 *         Can return null if the selector is not defined for this lens
 	 * 
 	 * @see #getSelectorAsObject()
 	 * @see #getSelectorAsString()
 	 */
 	public LensSelector getSelector() {
 		return selector;
 	}
 
 	/**
 	 * Shortcut method for {@link LensSelector#asString()} method.
 	 * 
 	 * @return the string value of the selector for this lens <br>
 	 *         can return null in case the selector is not defined
 	 */
 	public String getSelectorAsString() {
 		return selector != null ? selector.asString() : null;
 	}
 
 	/**
 	 * Shortcut method for {@link LensSelector#asObject()} method.
 	 * 
 	 * @return the selector object value for this lens <br>
 	 *         can return null in case the selector is not defined
 	 */
 	public Object getSelectorAsObject() {
 		return selector != null ? selector.asObject() : null;
 	}
 
 	/**
 	 * Sets the groups associated with this lens.
 	 * 
 	 * @param group
 	 */
 	public void setGroups(List<Group> groups) {
 		this.groups = groups;
 	}
 
 	/**
 	 * Returns the groups associated with this lens.
 	 * 
 	 * @return
 	 */
 	public List<Group> getGroups() {
 		return groups;
 	}
 
 	public void setComment(Literal comment) {
 		this.comment = comment;
 	}
 
 	public void setComment(String comment, String language) {
 		setComment(new LiteralImpl(comment, language));
 	}
 
 	public Literal getComment() {
 		return comment;
 	}
 
 	public void setShowProperties(List<PropertyVisibilityWrapper> showProps) {
 		this.showProperties = showProps;
 	}
 
 	public List<PropertyVisibilityWrapper> getShowProperties() {
 		return showProperties;
 	}
 
 	public List<PropertyVisibilityWrapper> getHideProperties() {
 		return hideProperties;
 	}
 
 	public void setHideProperties(List<PropertyVisibilityWrapper> hideProps) {
 		this.hideProperties = hideProps;
 	}
 
 	/**
 	 * Returns list of statements that represent this lens model.
 	 */
 	public List<StatementImpl> asStatements() {
 		return LensesUtils.modelAsStatements(this);
 	}
 
 	/**
 	 * Returns the lens URI.
 	 * 
 	 * @return the lens URI
 	 */
 	@Override
 	public String toString() {
 		return lensUri;
 	}
 
 	public String getModelUri() {
 		return lensUri;
 	}
 
 	private void initDomain(Lens lens) {
 		Collection<String> classDomains = lens.getBasicClassDomains();
 		Collection<String> instanceDomains = lens.getBasicInstanceDomains();
 		Collection<SPARQLQuery> sparqlDomains = lens.getSPARQLInstanceDomains();
 		Collection<FSLPath> fslDomains = lens.getFSLInstanceDomains();
 
 		Set<Collection<? extends Object>> domains = new HashSet<Collection<? extends Object>>();
 		if (!classDomains.isEmpty())
 			domains.add(classDomains);
 		if (!instanceDomains.isEmpty())
 			domains.add(instanceDomains);
 		if (!sparqlDomains.isEmpty())
 			domains.add(sparqlDomains);
 		if (!fslDomains.isEmpty())
 			domains.add(fslDomains);
 
 		if (domains.size() > 1) {
 			// fixme igor: add error message
 			throw new InvalidLensDomainDefinitionException(lens,
 					"There are more domains than 1 which is not possible. Number of domains: "
 							+ domains.size());
 		}
 		if (domains.size() == 1) {
 			Collection<? extends Object> domArray = domains.iterator().next();
 			if (domArray.size() == 1) {
 				// only now is the definition valid
 				LensDomain domainType;
 				domainType = domArray.equals(classDomains) ? LensDomain.CLASS
 						: LensDomain.INSTANCE;
 				selector = new LensSelector(domainType, domArray.iterator().next());
 				return;
 			}
 		}
 		// do not throw anything - this state is perfectly legal e.g. for lens
 		// :emptyLens rdf:type fresnel:Lens
 		selector = null;
 	}
 
 	/**
 	 * Initialises the show and hide properties values from the given lens. <br>
 	 * Initializes the {@link #showProperties}, {@link #hideProperties}
 	 * 
 	 * @param lens
 	 *            from which the property visibilities are initialized
 	 */
 	private void initProperties(Lens lens) {
 		if (lens.getShowProperties() != null) {
 			showProperties = loadVisibility(lens.getShowProperties());
 		}
 		if (lens.getHideProperties() != null) {
 			hideProperties = loadVisibility(lens.getHideProperties());
 		}
 	}
 
 	/**
 	 * Transforms PropertyVisibility instances into GUI wrappers.
 	 * 
 	 * @returns list of wrapped PropertyVisibility
 	 */
 	private List<PropertyVisibilityWrapper> loadVisibility(
 			List<PropertyVisibility> propertyVisibilities) {
 		List<PropertyVisibilityWrapper> wrappers = new ArrayList<PropertyVisibilityWrapper>();
 		for (int i = 0; i < propertyVisibilities.size(); i++) {
 			PropertyVisibility visibility = propertyVisibilities.get(i);
 			PropertyVisibilityWrapper provVisGW = new PropertyVisibilityWrapper(
 					visibility);
 			if (provVisGW.isAllProperties()) {
 				allPropertiesIndex = i;
 			}
 			wrappers.add(provVisGW);
 		}
 		return wrappers;
 	}
 
 	/**
 	 * @return {@link #ALL_PROPERTIES_NOT_DEFINED} if the lens does not have
 	 *         showAll property defined or <br>
 	 *         index on which the showAll properties is in list of
 	 *         showProperties if lens does have showAll property defined
 	 */
 	public int getAllPropertiesIndex() {
 		return allPropertiesIndex;
 	}
 
 	public void setLabel(Literal label) {
 		this.label = label;
 	}
 
 	/**
 	 * Sets a label with a given language.
 	 * 
 	 * @param label
 	 * @param language
 	 */
 	public void setLabel(String label, String language) {
 		this.label = new LiteralImpl(label, language);
 	}
 
 	public Literal getLabel() {
 		return label;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + allPropertiesIndex;
 		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
 		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
 		result = prime * result
 				+ ((hideProperties == null) ? 0 : hideProperties.hashCode());
 		result = prime * result + ((label == null) ? 0 : label.hashCode());
 		result = prime * result + ((lensUri == null) ? 0 : lensUri.hashCode());
 		result = prime * result + purpose;
 		result = prime * result
 				+ ((selector == null) ? 0 : selector.hashCode());
 		result = prime * result
 				+ ((showProperties == null) ? 0 : showProperties.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof LensModel)) {
 			LOG.debug("Not equal: {} was instance of {}", obj, obj.getClass());
 			return false;
 		}
 		LensModel other = (LensModel) obj;
 		if (allPropertiesIndex != other.allPropertiesIndex) {
 			LOG.debug(
 					"Not equal: allPropertiesIndex={},other.allPropertiesIndex={}",
 					allPropertiesIndex, other.allPropertiesIndex);
 			return false;
 		}
 		if (comment == null) {
 			if (other.comment != null) {
 				LOG.debug("Not equal: other.comment={}", other.comment);
 				return false;
 			}
 		} else if (!comment.equals(other.comment)) {
 			LOG.debug("Not equal: comment={},other.comment={}", comment,
 					other.comment);
 			return false;
 		}
 		if (groups == null) {
 			if (other.groups != null) {
 				LOG.debug("Not equal: other.groups={}", other.groups);
 				return false;
 			}
 		} else if (!FresnelUtils.equalsGroups(groups, other.groups)) {
 			LOG.debug("Not equal: groups={},other.groups={}", groups,
 					other.groups);
 			return false;
 		}
 		if (hideProperties == null) {
 			if (other.hideProperties != null) {
 				LOG.debug("Not equal: other.hideProperties={}",
 						other.hideProperties);
 				return false;
 			}
 		} else if (!LensesUtils.equalsProperties(hideProperties,
 				other.hideProperties, false)) {
 			LOG.debug("Not equal: hideProperties={},other.hideProperties={}",
 					hideProperties, other.hideProperties);
 			return false;
 		}
 		if (label == null) {
 			if (other.label != null) {
 				LOG.debug("Not equal: other.label={}", other.label);
 				return false;
 			}
 		} else if (!label.equals(other.label))
 			return false;
 		if (lensUri == null) {
 			if (other.lensUri != null) {
 				LOG.debug("Not equal: other.lensUri={}", other.lensUri);
 				return false;
 			}
 		} else if (!lensUri.equals(other.lensUri)) {
 			LOG.debug("Not equal: lensUri={},other.lensUri={}", lensUri,
 					other.lensUri);
 			return false;
 		}
 		if (purpose != other.purpose)
 			return false;
 		if (selector == null) {
 			if (other.selector != null) {
 				LOG.debug("Not equal: other.selector={}", other.selector);
 				return false;
 			}
 		} else if (!selector.equals(other.selector)) {
 			LOG.debug("Not equal: selector={},other.selector={}",
 					selector.asObject(), other.selector.asObject());
 			return false;
 		}
 		if (showProperties == null) {
 			if (other.showProperties != null) {
 				LOG.debug("Not equal: other.showProperties={}",
 						other.showProperties);
 				return false;
 			}
 		} else if (!LensesUtils.equalsProperties(showProperties,
 				other.showProperties, true)) {
 			LOG.debug("Not equal: showProperties={},other.showProperties={}",
 					showProperties, other.showProperties);
 			return false;
 		}
 		return true;
 	}
 
 }
