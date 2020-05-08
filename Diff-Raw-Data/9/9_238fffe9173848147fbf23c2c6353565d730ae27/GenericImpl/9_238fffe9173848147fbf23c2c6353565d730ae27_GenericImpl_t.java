 package org.genericsystem.impl.core;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Objects;
 import java.util.Set;
 
 import org.genericsystem.api.annotation.SystemGeneric;
 import org.genericsystem.api.annotation.constraints.InstanceClassConstraint;
 import org.genericsystem.api.annotation.constraints.NotNullConstraint;
 import org.genericsystem.api.annotation.constraints.PropertyConstraint;
 import org.genericsystem.api.annotation.constraints.SingularConstraint;
 import org.genericsystem.api.annotation.constraints.SingularInstanceConstraint;
 import org.genericsystem.api.annotation.constraints.UniqueConstraint;
 import org.genericsystem.api.core.Cache;
 import org.genericsystem.api.core.Context;
 import org.genericsystem.api.core.Generic;
 import org.genericsystem.api.core.Snapshot;
 import org.genericsystem.api.core.Snapshot.Filter;
 import org.genericsystem.api.core.Snapshot.Projector;
 import org.genericsystem.api.exception.SuperRuleConstraintViolationException;
 import org.genericsystem.api.generic.Attribute;
 import org.genericsystem.api.generic.Link;
 import org.genericsystem.api.generic.Node;
 import org.genericsystem.api.generic.Property;
 import org.genericsystem.api.generic.Relation;
 import org.genericsystem.api.generic.Tree;
 import org.genericsystem.api.generic.Type;
 import org.genericsystem.api.generic.Value;
 import org.genericsystem.impl.constraints.InstanceClassConstraintImpl;
 import org.genericsystem.impl.constraints.RequiredConstraintImpl;
 import org.genericsystem.impl.constraints.axed.SingularConstraintImpl;
 import org.genericsystem.impl.constraints.simple.NotNullConstraintImpl;
 import org.genericsystem.impl.constraints.simple.PropertyConstraintImpl;
 import org.genericsystem.impl.constraints.simple.SingularInstanceConstraintImpl;
 import org.genericsystem.impl.constraints.simple.UniqueConstraintImpl;
 import org.genericsystem.impl.core.Statics.Primaries;
 import org.genericsystem.impl.iterator.AbstractFilterIterator;
 import org.genericsystem.impl.iterator.AbstractMagicIterator;
 import org.genericsystem.impl.iterator.AbstractPreTreeIterator;
 import org.genericsystem.impl.iterator.ArrayIterator;
 import org.genericsystem.impl.snapshot.AbstractSnapshot;
 import org.genericsystem.impl.system.CascadeRemoveSystemProperty;
 import org.genericsystem.impl.system.MultiDirectionalSystemProperty;
 import org.genericsystem.impl.system.NoInheritanceSystemProperty;
 import org.genericsystem.impl.system.ReferentialIntegritySystemProperty;
 import org.genericsystem.impl.system.SystemProperty;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @SuppressWarnings("unchecked")
 public class GenericImpl implements Generic, Type, Link, Relation, Value, Attribute, Property, Tree, Node {
 
 	protected static Logger log = LoggerFactory.getLogger(GenericImpl.class);
 
 	private LifeManager lifeManager;
 
 	Generic[] directSupers;
 	Generic[] components;
 
 	int metaLevel;
 	Serializable value;
 
 	final Generic initPrimary(Serializable value, int metaLevel, Generic primaryAncestor) {
 		return initialize(value, metaLevel, new Generic[] { primaryAncestor }, Statics.EMPTY_GENERIC_ARRAY);
 	}
 
 	final GenericImpl initialize(Serializable value, int metaLevel, Generic[] directSupers, Generic[] components) {
 		return restore(value, metaLevel, null, Long.MAX_VALUE, 0L, Long.MAX_VALUE, directSupers, components);
 	}
 
 	final GenericImpl restore(Serializable value, int metaLevel, Long designTs, long birthTs, long lastReadTs, long deathTs, Generic[] directSupers, Generic[] components) {
 		this.value = value;
 		this.metaLevel = metaLevel;
 		this.directSupers = directSupers;
 		this.components = components;
 
 		initSelfComponents();
 		lifeManager = new LifeManager(designTs == null ? getEngine().pickNewTs() : designTs, birthTs, lastReadTs, deathTs);
 		for (Generic g1 : directSupers)
 			for (Generic g2 : directSupers)
 				if (!g1.equals(g2))
 					assert !g1.inheritsFrom(g2) : "" + Arrays.toString(directSupers);
 		return this;
 	}
 
 	<T extends Generic> T plug() {
 		Set<Generic> componentSet = new HashSet<>();
 		for (Generic component : components)
 			if (componentSet.add(component))
 				((GenericImpl) component).lifeManager.engineComposites.add(this);
 
 		Set<Generic> effectiveSupersSet = new HashSet<>();
 		for (Generic effectiveSuper : directSupers)
 			if (effectiveSupersSet.add(effectiveSuper))
 				((GenericImpl) effectiveSuper).lifeManager.engineDirectInheritings.add(this);
 		return (T) this;
 	}
 
 	private void initSelfComponents() {
 		for (int i = 0; i < components.length; i++)
 			if (components[i] == null)
 				components[i] = this;
 	}
 
 	public LifeManager getLifeManager() {
 		return lifeManager;
 	}
 
 	@Override
 	public int compareTo(Generic generic) {
 		return Long.compare(this.getDesignTs(), ((GenericImpl) generic).getDesignTs());
 	}
 
 	@Override
 	public <T extends Generic> T getImplicit() {
 		if (isPrimary())
 			return (T) this;
 		for (Generic superGeneric : directSupers)
 			if (metaLevel == ((GenericImpl) superGeneric).metaLevel && Objects.hashCode(value) == Objects.hashCode(((GenericImpl) superGeneric).value) && Objects.equals(value, ((GenericImpl) superGeneric).value))
 				return ((GenericImpl) superGeneric).getImplicit();
 		throw new IllegalStateException(this.info());
 	}
 
 	@Override
 	public EngineImpl getEngine() {
 		return (EngineImpl) directSupers[0].getEngine();
 	}
 
 	@Override
 	public boolean isEngine() {
 		return false;
 	}
 
 	@Override
 	public boolean isInstanceOf(Generic generic) {
 		return getMetaLevel() - generic.getMetaLevel() == 1 ? this.inheritsFrom(generic) : false;
 	}
 
 	@Override
 	public int getMetaLevel() {
 		return metaLevel;
 	}
 
 	@Override
 	public boolean isConcrete() {
 		return SystemGeneric.CONCRETE == getMetaLevel();
 	}
 
 	@Override
 	public boolean isStructural() {
 		return SystemGeneric.STRUCTURAL == getMetaLevel();
 	}
 
 	@Override
 	public boolean isMeta() {
 		return SystemGeneric.META == getMetaLevel();
 	}
 
 	@Override
 	public boolean isType() {
 		return components.length == 0;
 	}
 
 	@Override
 	public boolean isAttribute() {
 		return components.length >= 1;
 	}
 
 	@Override
 	public boolean isReallyAttribute() {
 		return components.length == 1;
 	}
 
 	@Override
 	public boolean isAttributeOf(Generic generic) {
 		for (Generic component : components)
 			if (generic.inheritsFrom(component))
 				return true;
 		return false;
 	}
 
 	@Override
 	public boolean isAttributeOf(Generic generic, int componentPos) {
 		return componentPos >= components.length ? false : generic.inheritsFrom(components[componentPos]);
 	}
 
 	@Override
 	public boolean isRelation() {
 		return components.length > 1;
 	}
 
 	@Override
 	public <S extends Serializable> S getValue() {
 		return (S) value;
 	}
 
 	@Override
 	public <T extends Value> Snapshot<T> getValues(Context context, final T attribute) {
 		return getValues(context, attribute, false);
 	}
 
 	public <T extends Value> Snapshot<T> getValues(Context context, final T attribute, final boolean readPhantom) {
 		// TODO idem que getValue ?
 		Snapshot<T> mainSnapshot = mainSnapshot(context, attribute, SystemGeneric.CONCRETE, Statics.BASE_POSITION, readPhantom);
 		if (mainSnapshot.isEmpty()) {
 			getDefaultValue((Attribute) attribute);
 		}
 		return mainSnapshot;
 	}
 
 	@Override
 	public <T extends Serializable> T getValue(Context context, Property property) {
 		Link holder = getLink(context, property);
 		return holder != null ? holder.<T> getValue() : this.<T> getDefaultValue(property);
 	}
 
 	private <T extends Serializable> T getDefaultValue(Attribute attribute) {
 		try {
 			if (attribute.getValue() instanceof Class && SystemProperty.class.isAssignableFrom(((Class<?>) attribute.getValue())))
 				return ((Class<? extends SystemProperty>) attribute.getValue()).newInstance().getDefaultValue(this);
 		} catch (InstantiationException | IllegalAccessException e) {
 		}
 		return null;
 	}
 
 	@Override
 	public <T extends Value> T setValue(Cache cache, Property property, Serializable value) {
 		return setLink(cache, property, value);
 	}
 
 	@Override
 	public <T extends Link> T getLink(Context context, Property property, final Generic... targets) {
 		return getLink(context, property, Statics.BASE_POSITION, targets);
 	}
 
 	@Override
 	public <T extends Link> T getLink(Context context, Property property, final int basePos, final Generic... targets) {
 		Iterator<T> valuesIterator = new AbstractFilterIterator<T>(this.<T> mainIterator(context, property, SystemGeneric.CONCRETE, basePos, false)) {
 			@Override
 			public boolean isSelected() {
 				for (int i = 0; i < targets.length; i++)
 					if (!targets[i].equals(next.getComponent(i + (i >= basePos ? 1 : 0))))
 						return false;
 				return true;
 			}
 		};
 		if (!valuesIterator.hasNext())
 			return null;
 		T result = valuesIterator.next();
 		if (valuesIterator.hasNext())
 			throw new IllegalStateException("Ambigous request for property " + property + result.info() + " and " + valuesIterator.next().info() + " on : " + this);
 		return result;
 	}
 
 	@Override
 	public <T extends Link> T setLink(Cache cache, Link property, Serializable value, Generic... targets) {
 		return setLink(cache, property, value, Statics.BASE_POSITION, targets);
 	}
 
 	@Override
 	public <T extends Link> T setLink(Cache cache, Link property, Serializable value, int basePos, Generic... targets) {
 		return ((Relation) property).isSingularConstraintEnabled(cache, basePos) ? this.<T> setToOneLink(cache, property, value, basePos, targets) : this.<T> setToManyLink(cache, property, value, basePos, targets);
 	}
 
 	private <T extends Link> T setToOneLink(Cache cache, Link property, Serializable value, int basePos, Generic... targets) {
 		T link = getLink(cache, (Property) property, basePos);
 		if (link == null)
 			return addLink(cache, property, value, basePos, targets);
 		for (int i = 0; i < ((GenericImpl) link).components.length; i++)
 			if (i != basePos)
 				if (!((GenericImpl) link).components[i].equals(targets[i - (i >= basePos ? 1 : 0)])) {
 					if (this.equals(link.getComponent(basePos))) {
 						link.remove(cache);
 						return addLink(cache, property, value, basePos, targets);
 					}
 					cancel(cache, link);
 					return addLink(cache, property, value, basePos, targets);
 				}
 		if (!this.equals(link.getComponent(basePos)))
 			return addLink(cache, link, value, basePos, targets);
 		if (Objects.equals(value, link.getValue()))
 			return link;
 		return update(cache, link, value);
 	}
 
 	private <T extends Link> T setToManyLink(Cache cache, Link property, Serializable value, int basePos, Generic... targets) {
 		if (((Type) property).isPropertyConstraintEnabled(cache)) {
 			T link = getLink(cache, (Property) property, basePos, targets);
 			if (link == null)
 				return addLink(cache, property, value, basePos, targets);
 			if (!this.equals(link.getComponent(basePos)))
 				return addLink(cache, link, value, basePos, targets);
 			if (Objects.equals(value, link.getValue()))
 				return link;
 			return update(cache, link, value);
 		}
 		Snapshot<T> links = getLinks(cache, (Property) property, basePos, targets);
 		T link = links.findFirst(value);
 		if (link == null)
 			return addLink(cache, property, value, basePos, targets);
 		if (!this.equals(link.getComponent(basePos)))
 			return addLink(cache, link, value, basePos, targets);
 		return link;
 	}
 
 	private static <T extends Generic> T update(Cache cache, Generic old, Serializable value) {
 		return ((CacheImpl) cache).update(old, value);
 	}
 
 	public <T extends Generic> Iterator<T> directInheritingsIterator(Context context) {
 		return ((AbstractContext) context).directInheritingsIterator(this);
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getInheritings(final Context context) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return directInheritingsIterator(context);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> compositesIterator(Context context) {
 		return ((AbstractContext) context).compositesIterator(this);
 	}
 
 	private <T extends Generic> Iterator<T> compositesIterator(Context context, final int pos) {
 		return new AbstractFilterIterator<T>(this.<T> compositesIterator(context)) {
 			@Override
 			public boolean isSelected() {
 				return GenericImpl.this.equals(((Value) next).getComponent(pos));
 			}
 		};
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getComposites(final Context context) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return compositesIterator(context);
 			}
 		};
 	}
 
 	@Override
 	public <T extends Attribute> T getAttribute(Context context, Serializable value) {
 		// TODO to optimize !
 		for (T attribute : this.<T> getAttributes(context))
 			if (Objects.equals(attribute.getValue(), value))
 				return attribute;
 		return null;
 	}
 
 	@Override
 	public <T extends Relation> T getRelation(Context context, Serializable value) {
 		// TODO to optimize !
 		for (T relation : this.<T> getRelations(context))
 			if (Objects.equals(relation.getValue(), value))
 				return relation;
 		return null;
 	}
 
 	// TODO KK
 	public <T extends Generic> T reBind(Cache cache) {
 		return isAlive(cache) ? (T) this : ((CacheImpl) cache).<T> bind(value, metaLevel, getPrimariesArray(), components);
 	}
 
 	@Override
 	public <T extends Property> T getProperty(Context context, Serializable value) {
 		return getAttribute(context, value);
 	}
 
 	@Override
 	public <T extends Attribute> T addAttribute(Cache cache, Serializable value) {
 		return addSubAttribute(cache, getEngine(), value);
 	}
 
 	@Override
 	public <T extends Attribute> T addSubAttribute(Cache cache, Attribute attribute, Serializable value) {
 		assert !Objects.equals(value, attribute.getValue());
 		return ((CacheImpl) cache).add(attribute, value, SystemGeneric.STRUCTURAL, Statics.EMPTY_GENERIC_ARRAY, new Generic[] { this });
 	}
 
 	@Override
 	public <T extends Property> T addProperty(Cache cache, Serializable value) {
 		return (T) addAttribute(cache, value).enablePropertyConstraint(cache);
 	}
 
 	@Override
 	public <T extends Property> T addSubProperty(Cache cache, Property property, Serializable value) {
 		return (T) addSubAttribute(cache, property, value).enablePropertyConstraint(cache);
 	}
 
 	@Override
 	public <T extends Value> T addValue(Cache cache, Value attribute, Serializable value) {
 		return addLink(cache, (Link) attribute, value);
 	}
 
 	@Override
 	public <T extends Value> T flag(Cache cache, Value property) {
 		return setValue(cache, (Property) property, Statics.FLAG);
 	}
 
 	@Override
 	public <T extends Relation> T addRelation(Cache cache, Serializable value, Type... targets) {
 		return addSubRelation(cache, getEngine(), value, targets);
 	}
 
 	@Override
 	public <T extends Relation> T addSubRelation(Cache cache, Relation relation, Serializable value, Type... targets) {
 		assert !Objects.equals(value, relation.getValue());
 		return ((CacheImpl) cache).add(relation, value, SystemGeneric.STRUCTURAL, Statics.EMPTY_GENERIC_ARRAY, Statics.insertFirstIntoArray(this, targets));
 	}
 
 	@Override
 	public <T extends Generic> T newAnonymousInstance(Cache cache, Generic... components) {
 		return newInstance(cache, getEngine().pickNewAnonymousReference(), components);
 	}
 
 	@Override
 	public <T extends Generic> T newInstance(Cache cache, Serializable value, Generic... components) {
 		return ((CacheImpl) cache).add(this, value, getMetaLevel() + 1, getPrimariesArray(), components);
 	}
 
 	@Override
 	public <T extends Type> T newSubType(Cache cache, Serializable value, Generic... components) {
 		return ((CacheImpl) cache).add(this, value, getMetaLevel(), getPrimariesArray(), components);
 	}
 
 	@Override
 	public <T extends Link> T bind(Cache cache, Link relation, Generic... targets) {
 		return setLink(cache, relation, Statics.FLAG, targets);
 	}
 
 	@Override
 	public <T extends Link> T bind(Cache cache, Link relation, int basePos, Generic... targets) {
 		return setLink(cache, relation, Statics.FLAG, basePos, targets);
 	}
 
 	@Override
 	public <T extends Link> T addLink(Cache cache, Link relation, Serializable value, int basePos, Generic... targets) {
 		return ((CacheImpl) cache).add(relation, value, SystemGeneric.CONCRETE, Statics.EMPTY_GENERIC_ARRAY, Statics.insertIntoArray(this, targets, basePos));
 	}
 
 	@Override
 	public <T extends Link> T addLink(Cache cache, Link relation, Serializable value, Generic... targets) {
 		return addLink(cache, relation, value, Statics.BASE_POSITION, targets);
 	}
 
 	public <T extends Generic> Iterator<T> mainIterator(Context context, Generic origin, final int metaLevel, final int pos, boolean readPhantom) {
 		return ((GenericImpl) origin).safeIsEnabled(context, ((AbstractContext) context).<Property> find(NoInheritanceSystemProperty.class)) ? this.<T> noInheritanceIterator(context, origin, metaLevel, pos) : this.<T> inheritanceIterator(context, origin,
 				metaLevel, pos, readPhantom);
 	}
 
 	public <T extends Generic> Iterator<T> inheritanceIterator(Context context, final Generic origin, final int metaLevel, final int pos, final boolean readPhantom) {
 		return new AbstractFilterIterator<T>(this.<T> internalInheritanceIterator(context, origin, metaLevel, pos)) {
 			@Override
 			public boolean isSelected() {
 				return readPhantom ? true : !((GenericImpl) next).isPhantom();
 			}
 		};
 	}
 
 	public <T extends Generic> Iterator<T> internalInheritanceIterator(final Context context, final Generic origin, final int metaLevel, final int pos) {
 		return (Iterator<T>) new AbstractMagicIterator(context, origin) {

 			@Override
 			protected boolean isSelected(Generic candidate) {
 				return candidate.getMetaLevel() <= metaLevel && ((GenericImpl) candidate).safeIsEnabled(context, ((AbstractContext) context).<Property> find(MultiDirectionalSystemProperty.class)) ? candidate.isAttributeOf(GenericImpl.this) : candidate
 						.isAttributeOf(GenericImpl.this, pos);
 			}
 
 			@Override
 			public boolean isSelectable() {
 				return next.getMetaLevel() == metaLevel;
 			}
 		};
 	}
 
 	public boolean isPhantom() {
 		return Statics.PHAMTOM.equals(getValue());
 	}
 
 	public <T extends Generic> Iterator<T> noInheritanceIterator(Context context, final Generic origin, final int metaLevel, final int pos) {
 		return new AbstractFilterIterator<T>((((GenericImpl) origin).safeIsEnabled(context, ((AbstractContext) context).<Property> find(MultiDirectionalSystemProperty.class)) ? this.<T> compositesIterator(context) : this.<T> compositesIterator(context,
 				pos))) {
 			@Override
 			public boolean isSelected() {
 				return next.getMetaLevel() == metaLevel && next.inheritsFrom(origin);
 			}
 		};
 	}
 
 	boolean safeIsEnabled(Context context, Property property) {
 		Iterator<Generic> iterator = new AbstractMagicIterator(context, property) {
 			@Override
 			protected boolean isSelected(Generic candidate) {
 				return (candidate.getMetaLevel() <= SystemGeneric.CONCRETE) && candidate.isAttributeOf(GenericImpl.this);
 			}
 
 			@Override
 			public boolean isSelectable() {
 				return next.getMetaLevel() == SystemGeneric.CONCRETE;
 			}
 		};
 		return iterator.hasNext() ? Boolean.TRUE.equals(iterator.next().getValue()) : false;
 	}
 
 	@Override
 	public boolean inheritsFrom(Generic generic) {
 		if (generic == null)
 			return false;
 		boolean inheritance = ((GenericImpl) generic).new InheritanceCalculator().isSuperOf(this);
 		boolean superOf = ((GenericImpl) generic).isSuperOf(this);
 		assert inheritance == superOf : "" + this.info() + generic.info() + " : " + inheritance + " != " + superOf;
 		return superOf;
 	}
 
 	private Primaries getPrimaries() {
 		return new Primaries(this);
 	}
 
 	Generic[] getPrimariesArray() {
 		return getPrimaries().toArray();
 	}
 
 	private class InheritanceCalculator extends HashSet<Generic> {
 		private static final long serialVersionUID = -894665449193645526L;
 
 		public boolean isSuperOf(Generic subGeneric) {
 			if (GenericImpl.this.equals(subGeneric))
 				return true;
 			for (Generic directSuper : ((GenericImpl) subGeneric).directSupers)
 				if (add(directSuper) && isSuperOf(directSuper))
 					return true;
 			return false;
 		}
 	}
 
 	@Override
 	public boolean inheritsFromAll(Generic... generics) {
 		for (Generic generic : generics)
 			if (!inheritsFrom(generic))
 				return false;
 		return true;
 	}
 
 	public boolean isSuperOf(Generic generic) {
 		assert generic != null;
 		if (equals(generic))
 			return true;
 		if (((GenericImpl) generic).isEngine())
 			return isEngine();
 		if (((GenericImpl) generic).isPrimary())
 			return isSuperOf(((GenericImpl) generic).directSupers[0]);
 		return isSuperOf(getPrimariesArray(), components, ((GenericImpl) generic).getPrimariesArray(), ((GenericImpl) generic).components);
 	}
 
 	public static boolean isSuperOf(Generic[] interfaces, Generic[] components, final Generic[] subInterfaces, Generic[] subComponents) {
 		assert subInterfaces.length >= 1;
 		if (interfaces.length > subInterfaces.length || components.length > subComponents.length)
 			return false;
 
 		if (interfaces.length == subInterfaces.length && components.length == subComponents.length) {
 			for (int i = 0; i < subInterfaces.length; i++)
 				if (!((GenericImpl) interfaces[i]).isSuperOf(subInterfaces[i]))
 					return false;
 			for (int i = 0; i < subComponents.length; i++) {
 				if (components[i] == null) {
 					if (!Arrays.equals(subInterfaces, ((GenericImpl) subComponents[i]).getPrimariesArray()) || !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components)) {
 						assert false;// TODO reach with a good exemple
 						if (isSuperOf(interfaces, components, ((GenericImpl) subComponents[i]).getPrimariesArray(), ((GenericImpl) subComponents[i]).components))
 							return false;
 					}
 				} else if (subComponents[i] != null) {
 					if (!Arrays.equals(interfaces, ((GenericImpl) components[i]).getPrimariesArray()) || !Arrays.equals(subInterfaces, ((GenericImpl) subComponents[i]).getPrimariesArray())
 							|| !Arrays.equals(components, ((GenericImpl) components[i]).components) || !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components)) {
 						if (!((GenericImpl) components[i]).isSuperOf(subComponents[i])) {
 							return false;
 						}
 					}
 				}
 
 			}
 
 			return true;
 		}
 		if (subInterfaces.length > 1 && interfaces.length < subInterfaces.length)
 			for (int i = 0; i < subInterfaces.length; i++)
 				if (isSuperOf(interfaces, components, Statics.truncate(i, subInterfaces), subComponents))
 					return true;
 		if (components.length < subComponents.length)
 			for (int i = 0; i < subComponents.length; i++)
 				if (isSuperOf(interfaces, components, subInterfaces, Statics.truncate(i, subComponents)))
 					return true;
 		return false;
 	}
 
 	@Override
 	public void remove(Cache cache) {
 		((CacheImpl) cache).remove(this);
 	}
 
 	@Override
 	public boolean isAlive(Context context) {
 		return ((AbstractContext) context).isAlive(this);
 	}
 
 	public boolean isAlive(long ts) {
 		return lifeManager.isAlive(ts);
 	}
 
 	public long getDesignTs() {
 		return lifeManager.getDesignTs();
 	}
 
 	public long getBirthTs() {
 		return lifeManager.getBirthTs();
 	}
 
 	public long getDeathTs() {
 		return lifeManager.getDeathTs();
 	}
 
 	public long getLastReadTs() {
 		return lifeManager.getLastReadTs();
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getSupers() {
 		return new AbstractSnapshot<T>() {
 
 			@Override
 			public Iterator<T> iterator() {
 				return directSupersIterator();
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> directSupersIterator() {
 		return new ArrayIterator<T>((T[]) directSupers);
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getComponents() {
 		return new AbstractSnapshot<T>() {
 
 			@Override
 			public Iterator<T> iterator() {
 				return componentsIterator();
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> componentsIterator() {
 		return new ArrayIterator<T>((T[]) components);
 	}
 
 	@Override
 	public <T extends Link> Snapshot<T> getLinks(Context context, final Relation relation, Generic... targets) {
 		return getLinks(context, relation, Statics.BASE_POSITION, targets);
 	}
 
 	@Override
 	public <T extends Link> Snapshot<T> getLinks(final Context context, final Relation relation, final int basePos, final Generic... targets) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return new AbstractFilterIterator<T>(GenericImpl.this.<T> mainIterator(context, relation, SystemGeneric.CONCRETE, basePos, false)) {
 					@Override
 					public boolean isSelected() {
 						for (int i = 0; i < targets.length; i++)
 							if (!targets[i].equals(next.getComponent(i + (i >= basePos ? 1 : 0))))
 								return false;
 						return true;
 					}
 				};
 			}
 		};
 
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getTargets(Context context, Relation relation) {
 		return getLinks(context, relation).project(new Projector<T, Link>() {
 			@Override
 			public T project(Link element) {
 				return element.getComponent(Statics.TARGET_POSITION);
 			}
 		});
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getTargets(Context context, Relation relation, int basePos, final int targetPos) {
 		return getLinks(context, relation, basePos).project(new Projector<T, Link>() {
 			@Override
 			public T project(Link element) {
 				return element.getComponent(targetPos);
 			}
 		});
 	}
 
 	private <T extends Value> Snapshot<T> mainSnapshot(final Context context, final T attribute, final int metaLevel, final int pos, final boolean readPhantom) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return GenericImpl.this.<T> mainIterator(context, attribute, metaLevel, pos, readPhantom);
 			}
 		};
 	}
 
 	@Override
 	public void log() {
 		log.debug(info());
 	}
 
 	@Override
 	public String info() {
 		String s = "\n******************************" + System.identityHashCode(this) + "******************************\n";
 		s += "toString()     : " + this + "\n";
 		s += "Value          : " + value + "\n";
 		// s += "getMeta()      : " + getMeta() + "\n";
 		s += "getInstanciationLevel() : " + getMetaLevel() + "\n";
 		for (Generic interface_ : getPrimariesArray())
 			s += "Interface #" + "    : " + interface_ + " (" + System.identityHashCode(interface_) + ")\n";
 		for (Generic primary : getPrimaries())
 			s += "Primary #" + "       : " + primary + " (" + System.identityHashCode(primary) + ")\n";
 		for (Generic component : components)
 			s += "Component #" + "    : " + component + " (" + System.identityHashCode(component) + ")\n";
 		for (Generic superGeneric : directSupers) {
 			s += "Super #" + "        : " + superGeneric + " (" + System.identityHashCode(superGeneric) + ")\n";
 		}
 		s += "**********************************************************************\n";
 		return s;
 	}
 
 	@Override
 	public String toString() {
 		if (isPrimary())
 			return value instanceof Class ? ((Class<?>) value).getSimpleName() : value != null ? value.toString() : "null";
 		return Arrays.toString(getPrimariesArray()) + "/" + toString(components);
 	}
 
 	private String toString(Object[] a) {
 		if (a == null)
 			return "null";
 
 		int iMax = a.length - 1;
 		if (iMax == -1)
 			return "[]";
 
 		StringBuilder b = new StringBuilder();
 		b.append('[');
 		for (int i = 0;; i++) {
 			if (this.equals(a[i]))
 				b.append("this");
 			else
 				b.append(String.valueOf(a[i]));
 			if (i == iMax)
 				return b.append(']').toString();
 			b.append(", ");
 		}
 	}
 
 	public boolean isPrimary() {
 		return components.length == 0 && directSupers.length == 1;/*
 																 * && ((GenericImpl ) directSupers [0 ]).isPrimary ()
 																 */
 	}
 
 	@Override
 	public <T extends Generic> T getMeta() {
 		final int instanciationLevel = getMetaLevel() - 1;
 		if (equals(getEngine().getMetaAttribute()) || equals(getEngine().getMetaRelation()))
 			return (T) getEngine();
 		return levelFilter(new AbstractPreTreeIterator<T>((T) this) {
 			@Override
 			public Iterator<T> children(T node) {
 				return new AbstractFilterIterator<T>(((GenericImpl) node).<T> directSupersIterator()) {
 
 					@Override
 					public boolean isSelected() {
 						return next.getMetaLevel() >= instanciationLevel;
 					}
 				};
 			}
 		}, instanciationLevel).next();
 	}
 
 	@Override
 	public <T extends Generic> T getBaseComponent() {
 		return getComponent(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Generic> T getTargetComponent() {
 		return getComponent(Statics.TARGET_POSITION);
 	}
 
 	@Override
 	public <T extends Generic> T getComponent(int componentPos) {
 		return components.length <= componentPos ? null : (T) components[componentPos];
 	}
 
 	@Override
 	public <T extends Attribute> Snapshot<T> getStructurals(final Context context) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return structuralIterator(context);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> structuralIterator(Context context) {
 		return this.<T> mainIterator(context, ((AbstractContext) context).getMetaAttribute(), SystemGeneric.STRUCTURAL, Statics.BASE_POSITION, false);
 	}
 
 	@Override
 	public <T extends Attribute> Snapshot<T> getAttributes(final Context context) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return new AbstractFilterIterator<T>(GenericImpl.this.<T> structuralIterator(context)) {
 					@Override
 					public boolean isSelected() {
 						return next.isReallyAttribute();
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public <T extends Relation> Snapshot<T> getRelations(final Context context) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return new AbstractFilterIterator<T>(GenericImpl.this.<T> structuralIterator(context)) {
 					@Override
 					public boolean isSelected() {
 						return next.isRelation();
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getInstances(final Context context) {
 		// KK
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return (Iterator<T>) new AbstractMagicIterator(context, GenericImpl.this) {
 					@Override
 					protected boolean isSelected(Generic candidate) {
 						return candidate.isInstanceOf(GenericImpl.this);
 					}
 
 					@Override
 					public boolean isSelectable() {
 						return next.isInstanceOf(GenericImpl.this);
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getAllInstances(final Context context) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return allInstancesIterator(context);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> allInstancesIterator(Context context) {
 		return new AbstractFilterIterator<T>(this.<T> allInheritingsAboveIterator(context, getMetaLevel())) {
 			@Override
 			public boolean isSelected() {
 				return next.isInstanceOf(GenericImpl.this);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> allInheritingsAboveIterator(final Context context, final int metaLevel) {
 		final Set<Generic> sets = new HashSet<>();
 		return (Iterator<T>) new AbstractPreTreeIterator<Generic>(GenericImpl.this) {
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return new AbstractFilterIterator<Generic>(((GenericImpl) node).directInheritingsIterator(context)) {
 					@Override
 					public boolean isSelected() {
 						return metaLevel + 1 >= next.getMetaLevel() && sets.add(next);
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getSubTypes(final Context context) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return subTypesIterator(context);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> subTypesIterator(Context context) {
 		return levelFilter(this.<T> directInheritingsIterator(context), getMetaLevel());
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getAllSubTypes(final Context context) {
 		return new AbstractSnapshot<T>() {
 
 			@Override
 			public Iterator<T> iterator() {
 				return allSubTypesIterator(context);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> allSubTypesIterator(Context context) {
 		return levelFilter(this.<T> allInheritingsIterator(context), getMetaLevel());
 	}
 
 	private static <T extends Generic> Iterator<T> levelFilter(Iterator<T> iterator, final int instanciationLevel) {
 		return new AbstractFilterIterator<T>(iterator) {
 
 			@Override
 			public boolean isSelected() {
 				return instanciationLevel == next.getMetaLevel();
 			}
 		};
 	}
 
 	public <T extends Generic> Snapshot<T> getAllInheritings(final Context context) {
 		return new AbstractSnapshot<T>() {
 
 			@Override
 			public Iterator<T> iterator() {
 				return allInheritingsIterator(context);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> allInheritingsIterator(final Context context) {
 		final Set<Generic> sets = new HashSet<>();
 		return (Iterator<T>) new AbstractPreTreeIterator<Generic>(GenericImpl.this) {
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return new AbstractFilterIterator<Generic>(((GenericImpl) node).directInheritingsIterator(context)) {
 					@Override
 					public boolean isSelected() {
 						return sets.add(next);
 					}
 				};
 			}
 		};
 	}
 
 	void mountConstraints(Cache cache, Class<?> clazz) {
 		UniqueConstraint distinct = clazz.getAnnotation(UniqueConstraint.class);
 		if (distinct != null)
 			enableUniqueConstraint(cache);
 
 		InstanceClassConstraint instanceClass = clazz.getAnnotation(InstanceClassConstraint.class);
 		if (instanceClass != null)
 			setConstraintClass(cache, instanceClass.value());
 
 		NotNullConstraint notNull = clazz.getAnnotation(NotNullConstraint.class);
 		if (notNull != null)
 			enableNotNullConstraint(cache);
 
 		PropertyConstraint property = clazz.getAnnotation(PropertyConstraint.class);
 		if (property != null)
 			enablePropertyConstraint(cache);
 
 		SingularInstanceConstraint singularInstance = clazz.getAnnotation(SingularInstanceConstraint.class);
 		if (singularInstance != null)
 			enableSingularInstanceConstraint(cache);
 
 		SingularConstraint singularTarget = clazz.getAnnotation(SingularConstraint.class);
 		if (singularTarget != null)
 			for (int axe : singularTarget.value())
 				enableSingularConstraint(cache, axe);
 	}
 
 	/*********************************************/
 	/**************** PHANTOM ********************/
 	/*********************************************/
 
 	@Override
 	public void cancel(Cache cache, Value attribute) {
 		if (equals(attribute.getBaseComponent()))
 			throw new IllegalStateException("Only inherited attributes can be cancelled");
 		((CacheImpl) cache).add(attribute, Statics.PHAMTOM, attribute.getMetaLevel(), Statics.EMPTY_GENERIC_ARRAY, Statics.replace(Statics.BASE_POSITION, ((GenericImpl) attribute).components, this));
 	}
 
 	@Override
 	public void restore(Cache cache, Attribute attribute) {
 		for (Value nodeValue : mainSnapshot(cache, attribute, attribute.getMetaLevel(), Statics.BASE_POSITION, true))
 			if (isValuePhantomOverride(nodeValue, attribute.getValue()))
 				nodeValue.remove(cache);
 	}
 
 	private boolean isValuePhantomOverride(Value nodeValue, Serializable value) {
 		return (equals(nodeValue.getBaseComponent()) && ((GenericImpl) nodeValue).isPhantom() && Objects.equals(nodeValue.getImplicit().getSupers().first().getValue(), value));
 	}
 
 	/*********************************************/
 	/******************* TREE ********************/
 	/*********************************************/
 
 	@Override
 	public <T extends Node> T newRoot(Cache cache, Serializable value) {
 		return newRoot(cache, value, 1);
 	}
 
 	@Override
 	public <T extends Node> T newRoot(Cache cache, Serializable value, int dim) {
 		return ((CacheImpl) cache).add(this, value, getMetaLevel() + 1, Statics.EMPTY_GENERIC_ARRAY, new Generic[dim]);
 	}
 
 	@Override
 	public <T extends Node> T addNode(Cache cache, Serializable value, Generic... targets) {
 		return ((CacheImpl) cache).add(getMeta(), value, SystemGeneric.CONCRETE, Statics.EMPTY_GENERIC_ARRAY, Statics.insertFirstIntoArray(this, targets));
 	}
 
 	@Override
 	public <T extends Node> T addSubNode(Cache cache, Serializable value, Generic... targets) {
 		return ((CacheImpl) cache).add(this, value, SystemGeneric.CONCRETE, Statics.EMPTY_GENERIC_ARRAY, Statics.insertFirstIntoArray(this, targets));
 	}
 
 	@Override
 	public <T extends Node> Snapshot<T> getChildren(Context context) {
 		return this.<T> getValues(context, this.<T> getMeta()).filter(new Filter<T>() {
 			@Override
 			public boolean isSelected(T node) {
 				return !GenericImpl.this.equals(node);
 			}
 		});
 	}
 
 	@Override
 	public boolean isTree() {
 		for (int i = 0; i < components.length; i++)
 			if (equals(components[i]))
 				return true;
 		return false;
 	}
 
 	@Override
 	public void traverse(Visitor visitor) {
 		visitor.traverse(this);
 	}
 
 	/*********************************************/
 	/************** SYSTEM PROPERTY **************/
 	/*********************************************/
 
 	public Serializable getSystemPropertyValue(Context context, Class<?> systemPropertyClass) {
 		return getValue(context, ((AbstractContext) context).<Property> find(systemPropertyClass));
 	}
 
 	private Value setSystemPropertyValue(Cache cache, Class<?> systemPropertyClass, Serializable value) {
 		return setValue(cache, cache.<Property> find(systemPropertyClass), value);
 	}
 
 	@Override
 	public <T extends Generic> T enableSystemProperty(Cache cache, Class<?> systemPropertyClass) {
 		setSystemPropertyValue(cache, systemPropertyClass, Boolean.TRUE);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Generic> T disableSystemProperty(Cache cache, Class<?> systemPropertyClass) {
 		setSystemPropertyValue(cache, systemPropertyClass, Boolean.FALSE);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Generic> T enableSystemProperty(Cache cache, Class<?> systemPropertyClass, int componentPos) {
 		Property systemProperty = cache.find(systemPropertyClass);
 		for (Value nodeValue : getValues(cache, systemProperty, true)) {
 			if (Integer.valueOf(componentPos).equals(nodeValue.getValue()))
 				return (T) this;
 			if (isValuePhantomOverride(nodeValue, componentPos))
 				nodeValue.remove(cache);
 		}
 		addValue(cache, systemProperty, componentPos);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Generic> T disableSystemProperty(Cache cache, Class<?> systemPropertyClass, int componentPos) {
 		Property systemProperty = cache.find(systemPropertyClass);
 		for (Value nodeValue : getValues(cache, systemProperty))
 			if (nodeValue.getValue().equals(componentPos))
 				if (this.equals(nodeValue.getBaseComponent()))
 					nodeValue.remove(cache);
 				else
 					cancel(cache, nodeValue);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isSystemPropertyEnabled(Context context, Class<?> systemPropertyClass) {
 		return Boolean.TRUE.equals(getSystemPropertyValue(context, systemPropertyClass));
 	}
 
 	@Override
 	public boolean isSystemPropertyEnabled(Context context, Class<?> systemPropertyClass, int componentPos) {
 		for (Value nodeValue : getValues(context, ((AbstractContext) context).<Property> find(systemPropertyClass)))
 			if (Objects.equals(nodeValue.getValue(), componentPos))
 				return true;
 		return false;
 	}
 
 	@Override
 	public <T extends Attribute> T enableMultiDirectional(Cache cache) {
 		setSystemPropertyValue(cache, MultiDirectionalSystemProperty.class, Boolean.TRUE);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Attribute> T disableMultiDirectional(Cache cache) {
 		setSystemPropertyValue(cache, MultiDirectionalSystemProperty.class, Boolean.FALSE);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isMultiDirectional(Context context) {
 		return isSystemPropertyEnabled(context, MultiDirectionalSystemProperty.class);
 	}
 
 	@Override
 	public <T extends Relation> T enableCascadeRemove(Cache cache, int componentPos) {
 		return enableSystemProperty(cache, CascadeRemoveSystemProperty.class, componentPos);
 	}
 
 	@Override
 	public <T extends Relation> T disableCascadeRemove(Cache cache, int componentPos) {
 		return disableSystemProperty(cache, CascadeRemoveSystemProperty.class, componentPos);
 	}
 
 	@Override
 	public boolean isCascadeRemove(Context context, int componentPos) {
 		return isSystemPropertyEnabled(context, CascadeRemoveSystemProperty.class, componentPos);
 	}
 
 	@Override
 	public <T extends Generic> T enableReferentialIntegrity(Cache cache, int componentPos) {
 		return enableSystemProperty(cache, ReferentialIntegritySystemProperty.class, componentPos);
 	}
 
 	@Override
 	public <T extends Generic> T disableReferentialIntegrity(Cache cache, int componentPos) {
 		return disableSystemProperty(cache, ReferentialIntegritySystemProperty.class, componentPos);
 	}
 
 	@Override
 	public boolean isReferentialIntegrity(Context context, int componentPos) {
 		return isSystemPropertyEnabled(context, ReferentialIntegritySystemProperty.class, componentPos);
 	}
 
 	@Override
 	public <T extends Type> T enableSingularConstraint(Cache cache) {
 		return enableSingularConstraint(cache, Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Type> T disableSingularConstraint(Cache cache) {
 		return disableSingularConstraint(cache, Statics.BASE_POSITION);
 	}
 
 	@Override
 	public boolean isSingularConstraintEnabled(Context context) {
 		return isSingularConstraintEnabled(context, Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Type> T enableSingularConstraint(Cache cache, int componentPos) {
 		return enableSystemProperty(cache, SingularConstraintImpl.class, componentPos);
 	}
 
 	@Override
 	public <T extends Type> T disableSingularConstraint(Cache cache, int componentPos) {
 		return disableSystemProperty(cache, SingularConstraintImpl.class, componentPos);
 	}
 
 	@Override
 	public boolean isSingularConstraintEnabled(Context context, int componentPos) {
 		return isSystemPropertyEnabled(context, SingularConstraintImpl.class, componentPos);
 	}
 
 	@Override
 	public <T extends Type> T enablePropertyConstraint(Cache cache) {
 		return enableSystemProperty(cache, PropertyConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disablePropertyConstraint(Cache cache) {
 		return disableSystemProperty(cache, PropertyConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isPropertyConstraintEnabled(Context context) {
 		return isSystemPropertyEnabled(context, PropertyConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T enableRequiredConstraint(Cache cache) {
 		return enableSystemProperty(cache, RequiredConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disableRequiredConstraint(Cache cache) {
 		return disableSystemProperty(cache, RequiredConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isRequiredConstraintEnabled(Context context) {
 		return isSystemPropertyEnabled(context, RequiredConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T enableNotNullConstraint(Cache cache) {
 		return enableSystemProperty(cache, NotNullConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disableNotNullConstraint(Cache cache) {
 		return disableSystemProperty(cache, NotNullConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isNotNullConstraintEnabled(Context context) {
 		return isSystemPropertyEnabled(context, NotNullConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T enableUniqueConstraint(Cache cache) {
 		return enableSystemProperty(cache, UniqueConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disableUniqueConstraint(Cache cache) {
 		return disableSystemProperty(cache, UniqueConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isUniqueConstraintEnabled(Context context) {
 		return isSystemPropertyEnabled(context, UniqueConstraintImpl.class);
 	}
 
 	@Override
 	public Class<? extends Serializable> getConstraintClass(Context context) {
 		return (Class<? extends Serializable>) getSystemPropertyValue(context, InstanceClassConstraintImpl.class);
 	}
 
 	@Override
 	public void setConstraintClass(Cache cache, Class<?> constraintClass) {
 		this.setSystemPropertyValue(cache, InstanceClassConstraintImpl.class, constraintClass);
 	}
 
 	@Override
 	public <T extends Type> T enableSingularInstanceConstraint(Cache cache) {
 		return enableSystemProperty(cache, SingularInstanceConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disableSingularInstanceConstraint(Cache cache) {
 		return disableSystemProperty(cache, SingularInstanceConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isSingularInstanceConstraintEnabled(Context context) {
 		return isSystemPropertyEnabled(context, SingularInstanceConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T enableInheritance(Cache cache) {
 		return disableSystemProperty(cache, NoInheritanceSystemProperty.class);
 	}
 
 	@Override
 	public <T extends Type> T disableInheritance(Cache cache) {
 		return enableSystemProperty(cache, NoInheritanceSystemProperty.class);
 	}
 
 	@Override
 	public boolean isInheritanceEnabled(Context context) {
 		return !isSystemPropertyEnabled(context, NoInheritanceSystemProperty.class);
 	}
 
 	Generic[] nullComponentsArray() {
 		Generic[] nullComponents = components.clone();
 		for (int i = 0; i < nullComponents.length; i++)
 			if (this.equals(nullComponents[i]))
 				nullComponents[i] = null;
 		return nullComponents;
 	}
 
 	void checkSuperRule(Generic[] interfaces, Generic[] components) {
 		if (!GenericImpl.isSuperOf(getPrimariesArray(), this.components, interfaces, components))
 			throw new SuperRuleConstraintViolationException("Interfaces : " + Arrays.toString(interfaces) + " Components : " + Arrays.toString(components) + " should inherits from : " + this);
 	}
 
 	boolean equiv(Generic[] interfaces, Generic[] components) {
 		return Arrays.equals(getPrimariesArray(), interfaces) && Arrays.equals(nullComponentsArray(), components);
 	}
 }
