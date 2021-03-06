 package xuml.tools.model.compiler;
 
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.collect.HashMultimap.create;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Sets.newHashSet;
 
 import java.math.BigInteger;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.bind.JAXBElement;
 
 import miuml.jaxb.Association;
 import miuml.jaxb.AsymmetricPerspective;
 import miuml.jaxb.Attribute;
 import miuml.jaxb.BinaryAssociation;
 import miuml.jaxb.Class;
 import miuml.jaxb.CreationEvent;
 import miuml.jaxb.Event;
 import miuml.jaxb.Generalization;
 import miuml.jaxb.IdentifierAttribute;
 import miuml.jaxb.IndependentAttribute;
 import miuml.jaxb.NativeAttribute;
 import miuml.jaxb.Reference;
 import miuml.jaxb.ReferentialAttribute;
 import miuml.jaxb.Relationship;
 import miuml.jaxb.State;
 import miuml.jaxb.StateModelParameter;
 import miuml.jaxb.StateModelSignature;
 import miuml.jaxb.Transition;
 import miuml.jaxb.UnaryAssociation;
 
 import com.google.common.base.Function;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 public class ClassInfoFromJaxb extends ClassInfo {
 
 	private final Class cls;
 	private final String packageName;
 	private final String classDescription;
 	private final String schema;
 	private final TypeRegister typeRegister = new TypeRegister();
 	private final Lookups lookups;
 	private static NameManager nameManager = NameManager.getInstance();
 
 	public ClassInfoFromJaxb(Class cls, String packageName,
 			String classDescription, String schema, Lookups lookups) {
 		this.cls = cls;
 		this.packageName = packageName;
 		// TODO is this property needed?
 		this.classDescription = classDescription;
 		this.schema = schema;
 		this.lookups = lookups;
 	}
 
 	@Override
 	String getPackage() {
 		return packageName;
 	}
 
 	@Override
 	String getClassDescription() {
 		return classDescription;
 	}
 
 	@Override
 	List<List<String>> getUniqueConstraintColumnNames() {
 		HashMultimap<BigInteger, String> map = getIdentifierAttributeNames();
 		List<List<String>> list = newArrayList();
 		for (BigInteger i : map.keySet())
 			list.add(newArrayList(map.get(i)));
 		return list;
 	}
 
 	private HashMultimap<BigInteger, String> getIdentifierAttributeNames() {
 		HashMultimap<BigInteger, Attribute> map = getIdentifierAttributes();
 		HashMultimap<BigInteger, String> m = create();
 		for (BigInteger i : map.keySet()) {
 			m.putAll(i, getNames(map.get(i)));
 		}
 		return m;
 	}
 
 	private static Function<Attribute, String> attributeName = new Function<Attribute, String>() {
 		@Override
 		public String apply(Attribute a) {
 			return a.getName();
 		}
 	};
 
 	private Set<String> getNames(Set<Attribute> attributes) {
 		return newHashSet(transform(attributes, attributeName));
 	}
 
 	private HashMultimap<BigInteger, Attribute> getIdentifierAttributes() {
 		HashMultimap<BigInteger, Attribute> map = HashMultimap.create();
 		for (JAXBElement<? extends Attribute> element : cls.getAttribute()) {
 			Attribute attribute = element.getValue();
 			for (IdentifierAttribute id : attribute.getIdentifier()) {
 				map.put(id.getNumber(), attribute);
 			}
 		}
 		return map;
 	}
 
 	@Override
 	String getSchema() {
 		return schema;
 	}
 
 	@Override
 	String getTable() {
 		return nameManager.toTableName(schema, cls.getName());
 	}
 
 	@Override
 	String getJavaClassSimpleName() {
 		return Util.toClassSimpleName(cls.getName());
 	}
 
 	@Override
 	List<String> getOperations() {
 		// TODO review operations, not supported by miUML. Should be using
 		// derived attributes.
 		return Lists.newArrayList();
 	}
 
 	@Override
 	List<MyPrimaryIdAttribute> getPrimaryIdAttributeMembers() {
 		Set<Attribute> list = getIdentifierAttributes().get(BigInteger.ONE);
 		List<MyPrimaryIdAttribute> result = newArrayList();
 		for (Attribute attribute : list) {
 			MyPrimaryIdAttribute id;
 			if (attribute instanceof NativeAttribute) {
 				NativeAttribute a = (NativeAttribute) attribute;
 				id = createMyPrimaryIdAttribute(a);
 			} else {
 				ReferentialAttribute a = (ReferentialAttribute) attribute;
 				id = createMyPrimaryIdAttribute(a);
 			}
 			result.add(id);
 		}
 		return result;
 	}
 
 	private MyPrimaryIdAttribute createMyPrimaryIdAttribute(
 			ReferentialAttribute a) {
 		Reference ref = a.getReference().getValue();
 		Relationship rel = lookups.getRelationship(ref.getRelationship());
 		String otherClassName = getOtherClassName(rel);
 		return getPrimaryIdAttribute(a, ref, otherClassName);
 	}
 
 	private String getOtherClassName(Relationship rel) {
 		String otherClassName;
 		if (rel instanceof BinaryAssociation) {
 			BinaryAssociation b = (BinaryAssociation) rel;
 			if (isActiveSide(b))
 				otherClassName = b.getPassivePerspective().getViewedClass();
 			else
 				otherClassName = b.getActivePerspective().getViewedClass();
 		} else if (rel instanceof UnaryAssociation) {
 			// TODO
 			throw new RuntimeException("not sure how to do this one yet");
 		} else if (rel instanceof Generalization) {
 			Generalization g = (Generalization) rel;
 			if (cls.getName().equals(g.getSuperclass()))
 				throw new RuntimeException(
 						"cannot use an id from a specialization as primary id member: "
 								+ g.getRnum());
 			else
 				otherClassName = g.getSuperclass();
 		} else
 			throw new RuntimeException(
 					"this relationship type not implemented: "
 							+ rel.getClass().getName());
 		return otherClassName;
 	}
 
 	private MyPrimaryIdAttribute getPrimaryIdAttribute(ReferentialAttribute a,
 			Reference ref, String otherClassName) {
 		MyPrimaryIdAttribute p = getOtherPrimaryIdAttribute(a, ref,
 				otherClassName);
 		if (p != null)
 			return new MyPrimaryIdAttribute(a.getName(),
 					nameManager.toFieldName(cls.getName(), a.getName()),
 					nameManager.toColumnName(cls.getName(), a.getName()),
 					otherClassName, nameManager.toColumnName(otherClassName,
 							p.getAttributeName()), p.getType());
 		else
 			throw new RuntimeException("attribute not found!");
 	}
 
 	private MyPrimaryIdAttribute getOtherPrimaryIdAttribute(
 			ReferentialAttribute a, Reference ref, String otherClassName) {
 		ClassInfoFromJaxb otherInfo = getClassInfo(otherClassName);
 		// look for attribute
 		String otherAttributeName;
 		if (ref.getAttribute() == null)
 			otherAttributeName = a.getName();
 		else
 			otherAttributeName = ref.getAttribute();
 		List<MyPrimaryIdAttribute> members = otherInfo
 				.getPrimaryIdAttributeMembers();
 		for (MyPrimaryIdAttribute p : members) {
 			if (otherAttributeName.equals(p.getAttributeName())) {
 				return p;
 			}
 		}
 		// not found
 		throw new RuntimeException("could not find attribute <"
 				+ ref.getAttribute() + " in class " + otherClassName);
 
 	}
 
 	private ClassInfoFromJaxb getClassInfo(String otherClassName) {
 		ClassInfoFromJaxb otherInfo = new ClassInfoFromJaxb(
 				lookups.getClassByName(otherClassName), packageName, "unknown",
 				schema, lookups);
 		return otherInfo;
 	}
 
 	private boolean isActiveSide(BinaryAssociation b) {
 		return b.getActivePerspective().getViewedClass().equals(cls.getName());
 	}
 
 	private MyPrimaryIdAttribute createMyPrimaryIdAttribute(NativeAttribute a) {
 		return new MyPrimaryIdAttribute(a.getName(), Util.toJavaIdentifier(a
 				.getName()), Util.toColumnName(a.getName()),
 				getType(a.getType()));
 	}
 
 	private MyIndependentAttribute createMyIndependentAttribute(
 			NativeAttribute a) {
 		// TODO what to do with isNullable
 		boolean isNullable = true;
 		return new MyIndependentAttribute(Util.toJavaIdentifier(a.getName()),
 				Util.toColumnName(a.getName()), getType(a.getType()),
 				isNullable, "description");
 	}
 
 	@Override
 	List<MyIndependentAttribute> getNonIdIndependentAttributeMembers() {
 		List<MyIndependentAttribute> list = newArrayList();
 		for (JAXBElement<? extends Attribute> element : cls.getAttribute()) {
 			if (element.getValue() instanceof IndependentAttribute) {
 				IndependentAttribute a = (IndependentAttribute) element
 						.getValue();
 				if (!isMemberOfPrimaryIdentifier(a)) {
 					list.add(createMyIndependentAttribute(a));
 				}
 			}
 		}
 		return list;
 	}
 
 	private boolean isMemberOfPrimaryIdentifier(IndependentAttribute a) {
 		for (IdentifierAttribute idAttribute : a.getIdentifier()) {
 			if (idAttribute.getNumber().intValue() == 1) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	List<MyEvent> getEvents() {
 		if (cls.getLifecycle() == null)
 			return newArrayList();
 		List<MyEvent> list = newArrayList();
 		CreationEvent creationEvent = getCreationEvent();
 		for (JAXBElement<? extends Event> element : cls.getLifecycle()
 				.getEvent()) {
 			Event event = element.getValue();
 
 			final StateModelSignature signature;
 			final String stateName;
 
 			if (event.getEventSignature() != null) {
 				signature = event.getEventSignature();
 				stateName = null;
 			} else {
 				// TODO of eventSignature is null then get signature from
 				// destination state
 				State destinationState = null;
 
 				for (MyTransition transition : getTransitions()) {
 					if (transition.getEventId()
 							.equals(event.getID().toString())) {
 						for (State state : cls.getLifecycle().getState()) {
 							if (transition.getToState().equals(state.getName())) {
 								destinationState = state;
 							}
 						}
 					}
 				}
 				if (destinationState != null) {
 					signature = destinationState.getStateSignature();
 					stateName = destinationState.getName();
 				} else {
 					signature = null;
 					stateName = null;
 				}
 			}
 
 			if (signature == null)
 				throw new RuntimeException("signature not found for class="
 						+ cls.getName() + ",event=" + event.getName());
 
 			List<MyParameter> parameters = Lists.newArrayList();
 			for (StateModelParameter p : signature.getStateModelParameter()) {
 				parameters.add(new MyParameter(Util.toJavaIdentifier(p
 						.getName()), lookups.getJavaType(p.getType())));
 			}
 
 			MyEvent myEvent = new MyEvent(event.getName(),
 					Util.toClassSimpleName(event.getName()), parameters,
 					stateName, getStateSignatureInterfaceName(stateName),
 					event == creationEvent);
 			list.add(myEvent);
 		}
 		return list;
 	}
 
 	private String getStateSignatureInterfaceName(final String stateName) {
 		if (stateName == null)
 			return null;
 		else
 			return "StateSignature_"
 					+ Util.upperFirst(Util.toJavaIdentifier(stateName));
 	}
 
 	@Override
 	List<String> getStateNames() {
 		List<String> list = Lists.newArrayList();
 		if (cls.getLifecycle() == null)
 			return newArrayList();
 		else {
 			for (State state : cls.getLifecycle().getState())
 				list.add(state.getName());
 			return list;
 		}
 	}
 
 	@Override
 	List<MyTransition> getTransitions() {
 		List<MyTransition> list = Lists.newArrayList();
 		for (Transition transition : cls.getLifecycle().getTransition()) {
 			// TODO what to do about event name? Event inheritance is involved.
 			String eventName = getEventName(transition.getEventID());
 			list.add(new MyTransition(eventName, Util
 					.toClassSimpleName(eventName), transition.getEventID()
 					.toString(), transition.getState(), transition
 					.getDestination()));
 
 		}
 		CreationEvent creation = getCreationEvent();
 		if (creation != null) {
 			String eventName = getEventName(creation.getID());
 			list.add(new MyTransition(eventName, Util
 					.toClassSimpleName(eventName), creation.getID().toString(),
 					null, creation.getState()));
 		}
 		return list;
 	}
 
 	private CreationEvent getCreationEvent() {
 		for (JAXBElement<? extends Event> element : cls.getLifecycle()
 				.getEvent()) {
 			if (element.getValue() instanceof CreationEvent)
 				return (CreationEvent) element.getValue();
 		}
 		return null;
 	}
 
 	private String getEventName(BigInteger eventId) {
 		for (JAXBElement<? extends Event> ev : cls.getLifecycle().getEvent()) {
 			if (ev.getValue().getID().equals(eventId))
 				return ev.getValue().getName();
 		}
 		return null;
 	}
 
 	@Override
 	String getStateAsJavaIdentifier(String stateName) {
 		for (State state : cls.getLifecycle().getState())
 			if (state.getName().equals(stateName))
 				// TODO use nameManager
 				return Util.toJavaConstantIdentifier(stateName);
 		throw new RuntimeException("state not found: " + stateName);
 	}
 
 	@Override
 	boolean isSuperclass() {
 		return lookups.isSuperclass(cls.getName());
 	}
 
 	@Override
 	boolean isSubclass() {
 		return lookups.isSpecialization(cls.getName());
 	}
 
 	@Override
 	MySubclassRole getSubclassRole() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	List<MyReferenceMember> getReferenceMembers() {
 
 		List<MyReferenceMember> list = Lists.newArrayList();
 		List<Association> associations = lookups.getAssociations(cls);
 		for (Association a : associations) {
 			MyReferenceMember m = createMyReferenceMember(a, cls);
 			System.out.println("created " + m);
 			list.add(m);
 		}
 		return list;
 	}
 
 	private MyReferenceMember createMyReferenceMember(Association a, Class cls) {
 		if (a instanceof BinaryAssociation)
 			return createMyReferenceMember((BinaryAssociation) a, cls);
 		else
 			return createMyReferenceMember((UnaryAssociation) a, cls);
 	}
 
 	private MyReferenceMember createMyReferenceMember(UnaryAssociation a,
 			Class cls) {
 		// TODO implement unary association to MyReferenceMember
 		return null;
 	}
 
 	private MyReferenceMember createMyReferenceMember(BinaryAssociation a,
 			Class cls) {
 		AsymmetricPerspective pThis;
 		AsymmetricPerspective pThat;
 
 		if (a.getActivePerspective().getViewedClass().equals(cls.getName())) {
 			pThis = a.getActivePerspective();
 			pThat = a.getPassivePerspective();
 		} else {
 			pThis = a.getPassivePerspective();
 			pThat = a.getActivePerspective();
 		}
 		String otherClassName = pThat.getViewedClass();
 		ClassInfo infoOther = getClassInfo(otherClassName);
 		List<JoinColumn> joins = newArrayList();
 		if (pThat.isOnePerspective())
 			for (MyPrimaryIdAttribute member : infoOther
 					.getPrimaryIdAttributeMembers()) {
 				String attributeName = getMatchingAttributeName(a.getRnum(),
 						member.getAttributeName());
 				JoinColumn jc = new JoinColumn(nameManager.toColumnName(
 						cls.getName(), attributeName), member.getColumnName());
 				System.out.println(jc);
 				joins.add(jc);
 			}
 
 		String fieldName = nameManager.toFieldName(cls.getName(),
 				pThat.getViewedClass(), a.getRnum());
 		// now establish the name of the field for this class as seen in the
 		// other class
 		String thisFieldName = nameManager.toFieldName(otherClassName,
 				cls.getName(), a.getRnum());
 		return new MyReferenceMember(pThat.getViewedClass(),
 				infoOther.getClassFullName(), toMult(pThis), toMult(pThat),
 				pThis.getPhrase(), pThat.getPhrase(), fieldName, joins,
 				thisFieldName, "thatName", (MyManyToMany) null);
 	}
 
 	private String getMatchingAttributeName(BigInteger rNum,
 			String otherAttributeName) {
 		for (JAXBElement<? extends Attribute> element : cls.getAttribute()) {
 			Attribute a = element.getValue();
 			if (a instanceof ReferentialAttribute) {
 				ReferentialAttribute r = (ReferentialAttribute) a;
 				if (r.getReference().getValue().getRelationship().equals(rNum)
 						&& r.getReference().getValue().getAttribute()
 								.equals(otherAttributeName))
 					return r.getName();
 			}
 		}
 		throw new RuntimeException("could not find matching attribute "
 				+ cls.getName() + " R" + rNum + " " + otherAttributeName);
 	}
 
 	private static Mult toMult(AsymmetricPerspective p) {
 		if (p.isConditional() && p.isOnePerspective())
 			return Mult.ZERO_ONE;
 		else if (p.isConditional() && !p.isOnePerspective())
			return Mult.ONE_MANY;
 		else if (p.isOnePerspective())
 			return Mult.ONE;
 		else
			return Mult.MANY;
 	}
 
 	@Override
 	Set<String> getAtLeastOneFieldChecks() {
 		// TODO Auto-generated method stub
 		return Sets.newHashSet();
 	}
 
 	@Override
 	String getImports(String relativeToClass) {
 		return getTypes().getImports(relativeToClass);
 	}
 
 	@Override
 	String getIdColumnName() {
 		// TODO Auto-generated method stub
 		return "ID";
 	}
 
 	@Override
 	String getContextPackageName() {
 		// TODO Auto-generated method stub
 		return packageName;
 	}
 
 	@Override
 	TypeRegister getTypes() {
 		return typeRegister;
 	}
 
 	@Override
 	Type getType(String name) {
 		String javaClassName = lookups.getJavaType(name);
 		return new Type(javaClassName);
 	}
 }
