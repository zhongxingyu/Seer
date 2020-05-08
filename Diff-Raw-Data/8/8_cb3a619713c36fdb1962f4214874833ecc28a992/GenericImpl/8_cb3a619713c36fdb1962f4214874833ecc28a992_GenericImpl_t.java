 package org.genericsystem.impl.core;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Objects;
 import java.util.Set;
 import org.genericsystem.api.annotation.SystemGeneric;
 import org.genericsystem.api.annotation.constraints.InheritanceDisabled;
 import org.genericsystem.api.annotation.constraints.InstanceClassConstraint;
 import org.genericsystem.api.annotation.constraints.NotNullConstraint;
 import org.genericsystem.api.annotation.constraints.PropertyConstraint;
 import org.genericsystem.api.annotation.constraints.SingularConstraint;
 import org.genericsystem.api.annotation.constraints.SingularInstanceConstraint;
 import org.genericsystem.api.annotation.constraints.UniqueConstraint;
 import org.genericsystem.api.annotation.constraints.VirtualConstraint;
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
 import org.genericsystem.api.generic.Relation;
 import org.genericsystem.api.generic.Tree;
 import org.genericsystem.api.generic.Type;
 import org.genericsystem.api.generic.Value;
 import org.genericsystem.impl.constraints.InstanceClassConstraintImpl;
 import org.genericsystem.impl.constraints.RequiredConstraintImpl;
 import org.genericsystem.impl.constraints.VirtualConstraintImpl;
 import org.genericsystem.impl.constraints.axed.RequiredAxedConstraintImpl;
 import org.genericsystem.impl.constraints.axed.SingularConstraintImpl;
 import org.genericsystem.impl.constraints.simple.NotNullConstraintImpl;
 import org.genericsystem.impl.constraints.simple.PropertyConstraintImpl;
 import org.genericsystem.impl.constraints.simple.SingularInstanceConstraintImpl;
 import org.genericsystem.impl.constraints.simple.UniqueConstraintImpl;
 import org.genericsystem.impl.core.Statics.Primaries;
 import org.genericsystem.impl.iterator.AbstractFilterIterator;
 import org.genericsystem.impl.iterator.AbstractPreTreeIterator;
 import org.genericsystem.impl.iterator.AbstractSelectableLeafInheritedIterator;
 import org.genericsystem.impl.iterator.ArrayIterator;
 import org.genericsystem.impl.snapshot.AbstractSnapshot;
 import org.genericsystem.impl.system.CascadeRemoveSystemProperty;
 import org.genericsystem.impl.system.MultiDirectionalSystemProperty;
 import org.genericsystem.impl.system.NoInheritanceSystemProperty;
 import org.genericsystem.impl.system.ReferentialIntegritySystemProperty;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @SuppressWarnings("unchecked")
 public class GenericImpl implements Generic, Type, Link, Relation, Value, Attribute, Tree, Node {
 	
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
 		throw new IllegalStateException(info());
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
 		if (componentPos >= components.length)
 			return false;
 		// if (((GenericImpl) generic).isTree2(componentPos)) {
 		// if (equals(components[componentPos]))
 		// return true;
 		// return components[componentPos].isAttributeOf(generic, componentPos);
 		// } else
 		return generic.inheritsFrom(components[componentPos]);
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
 	public <T extends Value> Snapshot<T> getValueHolders(Context context, final T attribute) {
 		return mainSnapshot(context, attribute, SystemGeneric.CONCRETE, Statics.BASE_POSITION);
 	}
 	
 	@Override
 	public <T extends Serializable> T getValue(Context context, Attribute attribute) {
 		Link holder = getLink(context, (Relation) attribute);
 		return holder != null ? holder.<T> getValue() : null;
 	}
 	
 	@Override
 	public <T extends Value> T setValue(Cache cache, Attribute attribute, Serializable value) {
 		return setLink(cache, (Relation) attribute, value);
 	}
 	
 	@Override
 	public <T extends Link> T getLink(Context context, Relation relation, final Generic... targets) {
 		return getLink(context, relation, Statics.BASE_POSITION, targets);
 	}
 	
 	@Override
 	public <T extends Link> T getLink(Context context, Relation relation, final int basePos, final Generic... targets) {
 		// super KK targets[i + (i >= basePos ? 1 : 0)].mainIterator(...) etc ...
 		Iterator<T> valuesIterator = new AbstractFilterIterator<T>(this.<T> mainIterator(context, relation, SystemGeneric.CONCRETE, basePos/* , false */)) {
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
 			throw new IllegalStateException("Ambigous request for relation " + relation + result.info() + " and " + valuesIterator.next().info() + " on : " + this);
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
 		T link = getLink(cache, (Relation) property, basePos);
 		if (link == null)
 			return addLink(cache, property, value, SystemGeneric.CONCRETE, basePos, targets);
 		if (!this.equals(link.getComponent(basePos)))
 			return addLink(cache, link, value, SystemGeneric.CONCRETE, basePos, targets);
 		if (!Arrays.equals(Statics.insertIntoArray(this, targets, basePos), ((GenericImpl) link).components)) {
 			link.remove(cache);
 			return addLink(cache, property, value, SystemGeneric.CONCRETE, basePos, targets);
 		}
 		if (!Objects.equals(value, link.getValue()))
 			return update(cache, link, value);
 		return link;
 	}
 	
 	private <T extends Link> T setToManyLink(Cache cache, Link property, Serializable value, int basePos, Generic... targets) {
 		if (((Type) property).isPropertyConstraintEnabled(cache)) {
 			T link = getLink(cache, (Relation) property, basePos, targets);
 			if (link == null)
 				return addLink(cache, property, value, SystemGeneric.CONCRETE, basePos, targets);
 			if (!this.equals(link.getComponent(basePos)))
 				return addLink(cache, link, value, SystemGeneric.CONCRETE, basePos, targets);
 			if (Objects.equals(value, link.getValue()))
 				return link;
 			return update(cache, link, value);
 		}
 		T link = this.<T> getLinks(cache, (Relation) property, basePos, targets).findFirst(value);
 		if (link == null)
 			return addLink(cache, property, value, SystemGeneric.CONCRETE, basePos, targets);
 		if (!this.equals(link.getComponent(basePos)))
 			return addLink(cache, link, value, SystemGeneric.CONCRETE, basePos, targets);
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
 	
 	// public Iterator<Integer> treeComponentsPosIterator() {
 	// List<Integer> positions = new ArrayList<>();
 	// for (int i = 0; i < components.length; i++) {
 	// if (isTree2(i))
 	// positions.add(i);
 	// }
 	// return positions.iterator();
 	// }
 	
 	public <T extends Generic> Iterator<T> compositesIterator(Context context) {
 		return ((AbstractContext) context).compositesIterator(this);
 	}
 	
 	public <T extends Generic> Iterator<T> compositesIterator(Context context, final int pos) {
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
 	public <T extends Attribute> T getAttribute(final Context context, final Serializable value) {
 		for (T attribute : this.<T> getAttributes(context))
 			if (Objects.equals(attribute.getValue(), value))
 				return attribute;
 		return null;
 	}
 	
 	@Override
 	public <T extends Relation> T getRelation(final Context context, final Serializable value) {
 		for (T relation : this.<T> getRelations(context))
 			if (Objects.equals(relation.getValue(), value))
 				return relation;
 		return null;
 	}
 	
 	// TODO KK
 	public <T extends Generic> T reFind(Cache cache) {
 		if (isAlive(cache))
 			return (T) this;
 		Generic[] primariesArray = getPrimariesArray();
 		Generic[] boundPrimaries = new Generic[primariesArray.length];
 		for (int i = 0; i < primariesArray.length; i++)
 			boundPrimaries[i] = ((GenericImpl) primariesArray[i]).reFind(cache);
 		Generic[] boundComponents = new Generic[components.length];
 		for (int j = 0; j < components.length; j++)
 			boundComponents[j] = equals(components[j]) ? null : ((GenericImpl) components[j]).reFind(cache);
 		return ((CacheImpl) cache).<T> findByInterfaces(boundPrimaries, boundComponents);
 	}
 	
 	@Override
 	public <T extends Attribute> T getProperty(Context context, Serializable value) {
 		return getAttribute(context, value);
 	}
 	
 	@Override
 	public <T extends Attribute> T addAttribute(Cache cache, Serializable value) {
 		return addSubAttribute(cache, getEngine(), value);
 	}
 	
 	public <T extends Attribute> T addSubAttribute(Cache cache, Attribute attribute, Serializable value) {
 		assert !Objects.equals(value, attribute.getValue());
 		return addLink(cache, (Link) attribute, value, SystemGeneric.STRUCTURAL, 0, Statics.EMPTY_GENERIC_ARRAY);
 	}
 	
 	@Override
 	public <T extends Attribute> T addProperty(Cache cache, Serializable value) {
 		return (T) addAttribute(cache, value).enablePropertyConstraint(cache);
 	}
 	
 	public <T extends Attribute> T addSubProperty(Cache cache, Attribute property, Serializable value) {
 		return (T) addSubAttribute(cache, property, value).enablePropertyConstraint(cache);
 	}
 	
 	@Override
 	public <T extends Value> T flag(Cache cache, Value value) {
 		return setValue(cache, (Attribute) value, Statics.FLAG);
 	}
 	
 	@Override
 	public <T extends Relation> T addRelation(Cache cache, Serializable value, Type... targets) {
 		return addSubRelation(cache, getEngine(), value, targets);
 	}
 	
 	public <T extends Relation> T addSubRelation(Cache cache, Relation relation, Serializable value, Type... targets) {
 		assert !Objects.equals(value, relation.getValue());
 		return addLink(cache, getEngine(), value, SystemGeneric.STRUCTURAL, 0, targets);
 	}
 	
 	@Override
 	public <T extends Generic> T newAnonymousInstance(Cache cache, Generic... components) {
 		return newInstance(cache, getEngine().pickNewAnonymousReference(), components);
 	}
 	
 	@Override
 	public <T extends Generic> T newInstance(Cache cache, Serializable value, Generic... components) {
 		return ((CacheImpl) cache).bind(this.getImplicit(), this, value, getMetaLevel() + 1, getPrimariesArray(), components);
 	}
 	
 	@Override
 	public <T extends Type> T newSubType(Cache cache, Serializable value, Generic... components) {
 		return ((CacheImpl) cache).bind(this.getImplicit(), this, value, SystemGeneric.STRUCTURAL, getPrimariesArray(), components);
 	}
 	
 	@Override
 	public <T extends Link> T bind(Cache cache, Link relation, Generic... targets) {
 		return setLink(cache, relation, Statics.FLAG, targets);
 	}
 	
 	@Override
 	public <T extends Link> T bind(Cache cache, Link relation, int basePos, Generic... targets) {
 		return setLink(cache, relation, Statics.FLAG, basePos, targets);
 	}
 	
 	private <T extends Link> T addLink(Cache cache, Link relation, Serializable value, int metaLevel, int basePos, Generic... targets) {
 		Generic[] additionalInterfaces = Statics.EMPTY_GENERIC_ARRAY;
 		Generic[] components = Statics.insertIntoArray(this, targets, basePos);
 		Generic implicit = relation.getImplicit();
 		if (relation.isConcrete()) {
 			implicit = ((GenericImpl) implicit).directSupers[0];
 			if (!Objects.equals(relation.getValue(), value))
 				additionalInterfaces = new Generic[] { relation.getImplicit() };
 			for (int i = 0; i < ((GenericImpl) relation).components.length; i++) {
 				Generic superComponent = ((GenericImpl) relation).components[i];
 				Generic component = components[i];
 				if (!component.inheritsFrom(superComponent))
 					components = Statics.insertLastIntoArray(superComponent, components);
 			}
 		}
 		return ((CacheImpl) cache).bind(implicit, relation, value, metaLevel, additionalInterfaces, components);
 	}
 	
 	public <T extends Generic> Iterator<T> mainIterator(Context context, Generic origin, final int metaLevel, final int pos) {
 		return ((GenericImpl) origin).safeIsEnabled(context, ((AbstractContext) context).<Attribute> find(NoInheritanceSystemProperty.class)) ? this.<T> noInheritanceIterator(context, origin, metaLevel, pos) : this.<T> inheritanceIterator(context, origin,
 				metaLevel, pos);
 	}
 	
 	public <T extends Generic> Iterator<T> inheritanceIterator(final Context context, final Generic origin, final int metaLevel, final int pos) {
 		return (Iterator<T>) new AbstractSelectableLeafInheritedIterator(context, origin) {
 			
 			@Override
 			protected boolean isSelected(Generic father, Generic candidate) {
 				return candidate.getMetaLevel() <= metaLevel;
 			}
 			
 			@Override
 			public boolean isSelectable() {
 				return (next.getMetaLevel() == metaLevel)
 						&& (((GenericImpl) next).safeIsEnabled(context, ((AbstractContext) context).<Attribute> find(MultiDirectionalSystemProperty.class)) ? next.isAttributeOf(GenericImpl.this) : next.isAttributeOf(GenericImpl.this, pos));
 			}
 		};
 	}
 	
 	public <T extends Generic> Iterator<T> noInheritanceIterator(Context context, final Generic origin, final int metaLevel, final int pos) {
 		return new AbstractFilterIterator<T>((((GenericImpl) origin).safeIsEnabled(context, ((AbstractContext) context).<Attribute> find(MultiDirectionalSystemProperty.class)) ? this.<T> compositesIterator(context) : this.<T> compositesIterator(context,
 				pos))) {
 			@Override
 			public boolean isSelected() {
 				return next.getMetaLevel() == metaLevel && next.inheritsFrom(origin);
 			}
 		};
 	}
 	
 	boolean safeIsEnabled(Context context, Attribute attribute) {
 		Iterator<Generic> iterator = new AbstractSelectableLeafInheritedIterator(context, attribute) {
 			@Override
 			protected boolean isSelected(Generic father, Generic candidate) {
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
 			for (int i = 0; i < subInterfaces.length; i++) {
 				if (!((GenericImpl) interfaces[i]).isSuperOf(subInterfaces[i]))
					// if (!interfaces[i].isConcrete() || !subInterfaces[i].isConcrete() || !interfaces[i].<GenericImpl> getMeta().isSuperOf(subInterfaces[i].getMeta()))
					return false;
				// else if (Arrays.equals(components, subComponents))
				// return false;
 			}
 			for (int i = 0; i < subComponents.length; i++) {
 				if (components[i] != null && subComponents[i] != null)
 					if (!((GenericImpl) components[i]).isSuperOf(subComponents[i]))
 						return false;
 				if (components[i] == null) {
 					if (!Arrays.equals(subInterfaces, ((GenericImpl) subComponents[i]).getPrimariesArray()) || !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components))
 						return false;
 				} else if (subComponents[i] == null)
 					if (!components[i].isEngine() && (!Arrays.equals(interfaces, ((GenericImpl) components[i]).getPrimariesArray()) || !Arrays.equals(components, ((GenericImpl) components[i]).components)))
 						return false;
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
 	
 	@Override
 	public int getComponentsSize() {
 		return components.length;
 	}
 	
 	@Override
 	public int getSupersSize() {
 		return directSupers.length;
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
 				return new AbstractFilterIterator<T>(GenericImpl.this.<T> mainIterator(context, relation, SystemGeneric.CONCRETE, basePos)) {
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
 	
 	public <T extends Value> Snapshot<T> mainSnapshot(final Context context, final T attribute, final int metaLevel, final int pos) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return GenericImpl.this.<T> mainIterator(context, attribute, metaLevel, pos);
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
 		s += "getMeta()      : " + getMeta() + "\n";
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
 		return components.length == 0 && directSupers.length == 1;
 	}
 	
 	@Override
 	public <T extends Generic> T getMeta() {
 		final int instanciationLevel = getMetaLevel() == 0 ? 0 : getMetaLevel() - 1;
 		return levelFilter(new AbstractPreTreeIterator<T>((T) this) {
 			@Override
 			public Iterator<T> children(T node) {
 				return new AbstractFilterIterator<T>(((GenericImpl) node).<T> directSupersIterator()) {
 					@Override
 					public boolean isSelected() {
 						return instanciationLevel <= next.getMetaLevel();
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
 		return this.<T> mainIterator(context, ((AbstractContext) context).getMetaAttribute(), SystemGeneric.STRUCTURAL, Statics.BASE_POSITION);
 	}
 	
 	@Override
 	public <T extends Attribute> Snapshot<T> getAttributes(final Context context) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return new AbstractFilterIterator<T>(GenericImpl.this.<T> structuralIterator(context)) {
 					@Override
 					public boolean isSelected() {
 						return next.isAttribute();
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
 		// KK change to a prefixed traversal (see getAllInstances)
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return (Iterator<T>) new AbstractSelectableLeafInheritedIterator(context, GenericImpl.this) {
 					@Override
 					protected boolean isSelected(Generic father, Generic candidate) {
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
 		return levelFilter(this.<T> allInheritingsAboveIterator(context, getMetaLevel() + 1), getMetaLevel() + 1);
 	}
 	
 	private <T extends Generic> Iterator<T> allInheritingsAboveIterator(final Context context, final int metaLevel) {
 		final Set<Generic> alreadySelected = new HashSet<>();
 		return (Iterator<T>) new AbstractPreTreeIterator<Generic>(GenericImpl.this) {
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return new AbstractFilterIterator<Generic>(((GenericImpl) node).directInheritingsIterator(context)) {
 					@Override
 					public boolean isSelected() {
 						return next.getMetaLevel() <= metaLevel && alreadySelected.add(next);
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
 		InheritanceDisabled notInheritable = clazz.getAnnotation(InheritanceDisabled.class);
 		if (notInheritable != null)
 			disableInheritance(cache);
 		
 		VirtualConstraint virtualConstraint = clazz.getAnnotation(VirtualConstraint.class);
 		if (virtualConstraint != null)
 			enableVirtualConstraint(cache);
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
 	/******************* TREE ********************/
 	/*********************************************/
 	
 	@Override
 	public <T extends Node> T newRoot(Cache cache, Serializable value) {
 		return newRoot(cache, value, 1);
 	}
 	
 	@Override
 	public <T extends Node> T newRoot(Cache cache, Serializable value, int dim) {
 		return addLink(cache, this.<Link> getImplicit(), value, getMetaLevel() + 1, Statics.BASE_POSITION);
 		
 	}
 	
 	@Override
 	public <T extends Node> T addNode(Cache cache, Serializable value, Generic... targets) {
 		return addLink(cache, this.<Link> getMeta(), value, SystemGeneric.CONCRETE, Statics.BASE_POSITION, targets);
 	}
 	
 	@Override
 	public <T extends Node> T addSubNode(Cache cache, Serializable value, Generic... targets) {
 		return addLink(cache, this, value, SystemGeneric.CONCRETE, Statics.BASE_POSITION, targets);
 	}
 	
 	@Override
 	public <T extends Node> Snapshot<T> getChildren(Context context) {
 		return this.<T> getValueHolders(context, this.<T> getMeta()).filter(new Filter<T>() {
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
 	
 	// // TODO is it useful
 	// public boolean isTree2(int componentPos) {
 	// GenericImpl generic = ((GenericImpl) getComponent(componentPos));
 	// if (generic == null)
 	// return false;
 	// if (equals(generic))
 	// return true;
 	// return generic.isTree2(componentPos);
 	// }
 	
 	@Override
 	public void traverse(Visitor visitor) {
 		visitor.traverse(this);
 	}
 	
 	/*********************************************/
 	/************** SYSTEM PROPERTY **************/
 	/*********************************************/
 	
 	public Serializable getSystemPropertyValue(Context context, Class<?> systemPropertyClass) {
 		return getValue(context, ((AbstractContext) context).<Attribute> find(systemPropertyClass));
 	}
 	
 	private Value setSystemPropertyValue(Cache cache, Class<?> systemPropertyClass, Serializable value) {
 		return setValue(cache, cache.<Attribute> find(systemPropertyClass), value);
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
 		Attribute systemProperty = cache.find(systemPropertyClass);
 		for (Value nodeValue : getValueHolders(cache, systemProperty)) {
 			if (nodeValue.getValue().equals(componentPos))
 				return (T) this;
 		}
 		addLink(cache, (Link) systemProperty, componentPos, SystemGeneric.CONCRETE, Statics.BASE_POSITION, Statics.EMPTY_GENERIC_ARRAY);
 		return (T) this;
 	}
 	
 	@Override
 	public <T extends Generic> T disableSystemProperty(Cache cache, Class<?> systemPropertyClass, int componentPos) {
 		Attribute systemProperty = cache.<Attribute> find(systemPropertyClass);
 		for (Value nodeValue : getValueHolders(cache, systemProperty))
 			if (nodeValue.getValue().equals(componentPos))
 				if (equals(nodeValue.getBaseComponent()))
 					nodeValue.remove(cache);
 				else
 					setValue(cache, systemProperty, componentPos);
 		return (T) this;
 	}
 	
 	@Override
 	public boolean isSystemPropertyEnabled(Context context, Class<?> systemPropertyClass) {
 		return Boolean.TRUE.equals(getSystemPropertyValue(context, systemPropertyClass));
 	}
 	
 	@Override
 	public boolean isSystemPropertyEnabled(Context context, Class<?> systemPropertyClass, int componentPos) {
 		for (Value value : getValueHolders(context, ((AbstractContext) context).<Attribute> find(systemPropertyClass)))
 			if (Objects.equals(value.getValue(), componentPos))
 				return true;
 		return isReferentialIntegrity(systemPropertyClass) ? !isReallyAttribute() : false;
 	}
 	
 	private boolean isReferentialIntegrity(Class<?> systemPropertyClass) {
 		return ReferentialIntegritySystemProperty.class.isAssignableFrom(systemPropertyClass);
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
 	public <T extends Type> T enableRequiredConstraint(Cache cache, int componentPos) {
 		return enableSystemProperty(cache, RequiredAxedConstraintImpl.class, componentPos);
 	}
 	
 	@Override
 	public <T extends Type> T disableRequiredConstraint(Cache cache, int componentPos) {
 		return disableSystemProperty(cache, RequiredAxedConstraintImpl.class, componentPos);
 	}
 	
 	@Override
 	public boolean isRequiredConstraintEnabled(Context context, int componentPos) {
 		return isSystemPropertyEnabled(context, RequiredAxedConstraintImpl.class, componentPos);
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
 	public <T extends Type> T enableVirtualConstraint(Cache cache) {
 		return enableSystemProperty(cache, VirtualConstraintImpl.class);
 	}
 	
 	@Override
 	public <T extends Type> T disableVirtualConstraint(Cache cache) {
 		return disableSystemProperty(cache, VirtualConstraintImpl.class);
 	}
 	
 	@Override
 	public boolean isVirtualConstraintEnabled(Context context) {
 		return isSystemPropertyEnabled(context, VirtualConstraintImpl.class);
 	}
 	
 	@Override
 	public Class<? extends Serializable> getConstraintClass(Context context) {
 		return (Class<? extends Serializable>) getSystemPropertyValue(context, InstanceClassConstraintImpl.class);
 	}
 	
 	@Override
 	public void setConstraintClass(Cache cache, Class<?> constraintClass) {
 		setSystemPropertyValue(cache, InstanceClassConstraintImpl.class, constraintClass);
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
 	
 	public <T extends Generic> T reBind(Cache cache) {
 		assert !isPrimary();
 		return ((CacheImpl) cache).reBindNode(this);
 	}
 	
 }
