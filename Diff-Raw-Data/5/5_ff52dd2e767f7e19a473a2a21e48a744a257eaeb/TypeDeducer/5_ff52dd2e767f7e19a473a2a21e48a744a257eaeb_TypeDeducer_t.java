 package lapd.databases.neo4j;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.type.TypeFactory;
 import org.eclipse.imp.pdb.facts.type.TypeStore;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 
 // constructs pdb.values types based on the neo4j graph
 public class TypeDeducer {
 	
 	private static final TypeFactory typeFactory = TypeFactory.getInstance();
 	private final TypeStore typeStore;
 	private Node currentNode;	
 	
 	public TypeDeducer(Node node, TypeStore typeStore) {
 		this.currentNode = node;
 		this.typeStore = typeStore;
 	}
 	
 	public Type getType() {
 		String typeName = currentNode.getProperty(PropertyNames.TYPE).toString();
 		switch (typeName) {
 			case TypeNames.BOOLEAN:			return typeFactory.boolType();
 			case TypeNames.DATE_TIME: 		return typeFactory.dateTimeType();
 			case TypeNames.INTEGER: 		return typeFactory.integerType();
 			case TypeNames.LIST: 			return getListType();
 			case TypeNames.MAP: 			return getMapType();
 			case TypeNames.NODE: 			return typeFactory.nodeType();
 			case TypeNames.CONSTRUCTOR: 	return getConstructorType();
 			case TypeNames.RATIONAL: 		return typeFactory.rationalType();
 			case TypeNames.REAL: 			return typeFactory.realType();
 			case TypeNames.SET: 			return getSetType();
 			case TypeNames.SOURCE_LOCATION: return typeFactory.sourceLocationType();
 			case TypeNames.STRING: 			return typeFactory.stringType();
 			case TypeNames.TUPLE: 			return getTupleType();
 			case TypeNames.BINARY_RELATION: return getBinaryRelType();
 			default: 						return typeFactory.valueType();
 		}
 	}
 
 	private Type getBinaryRelType() {
 		Iterator<Relationship> rels = currentNode.getRelationships(RelTypes.PART, Direction.OUTGOING).iterator();
 		if (!rels.hasNext())
 			return typeFactory.setType(typeFactory.voidType());
 		List<Type> argumentList = new ArrayList<Type>();
 		Relationship rel = rels.next().getEndNode().getRelationships(RelTypes.TO, Direction.OUTGOING).iterator().next();
 		currentNode = rel.getStartNode();
 		argumentList.add(getType());
 		currentNode = rel.getEndNode();
 		argumentList.add(getType());		
 		return typeFactory.relType(argumentList.toArray(new Type[argumentList.size()]));
 	}
 
 	private Type getConstructorType() {
 		String name = currentNode.getProperty(PropertyNames.NODE).toString();
 		String adtName = currentNode.getProperty(PropertyNames.ADT).toString();
 		Type adt = typeStore.lookupAbstractDataType(adtName);		
 		if (adt == null)
 			return typeFactory.nodeType();
 		Set<Type> potentialTypes = typeStore.lookupConstructor(adt, name);
 		if (potentialTypes.size() == 1)
 			return typeStore.lookupConstructor(adt, name).iterator().next();
		else if (potentialTypes.size() > 1) {
 			String[] parameterTypeNames = new String[0];
 			if(currentNode.hasProperty(PropertyNames.PARAMETERS))
 				parameterTypeNames = (String[])currentNode.getProperty(PropertyNames.PARAMETERS);			
 			for(Type type : potentialTypes) {
 				Type tupleType = type.getFieldTypes();
 				int arity = tupleType.getArity();
 				if (arity == parameterTypeNames.length) {
 					boolean typeFound = true;
 					for (int i = 0; i < arity; i++) {
 						if (!parameterTypeNames[i].equals(tupleType.getFieldType(i).toString()))
 							typeFound = false;
 					}
 					if (typeFound)
 						return type;
 				}
 			}
 		}
		return typeFactory.nodeType();
 	}
 
 	private Type getTupleType() {
 		if (!hasHead())
 			return typeFactory.tupleEmpty();	
 		currentNode = currentNode.getSingleRelationship(RelTypes.HEAD, Direction.OUTGOING).getEndNode();
 		List<Type> argumentList = new ArrayList<Type>();
 		argumentList.add(getType());
 		while (currentNode.hasRelationship(Direction.OUTGOING, RelTypes.TO)) {
 			currentNode = currentNode.getSingleRelationship(RelTypes.TO, 
 					Direction.OUTGOING).getEndNode();
 			argumentList.add(getType());
 		}
 		return typeFactory.tupleType(argumentList.toArray(new Type[argumentList.size()]));
 	}
 
 	private Type getMapType() {
 		if (!currentNode.hasRelationship(Direction.OUTGOING, RelTypes.ELE))
 			return typeFactory.mapType(typeFactory.voidType(), typeFactory.voidType());		
 		Type leastUpperBoundKeyType = typeFactory.voidType();
 		Type leastUpperBoundValueType = typeFactory.voidType();
 		Iterable<Relationship> rels = currentNode.getRelationships(RelTypes.ELE, Direction.OUTGOING);
 		for (Relationship rel : rels) {
 			Node keyNode = rel.getEndNode();
 			Node valueNode = keyNode.getSingleRelationship(RelTypes.VALUE, Direction.OUTGOING).getEndNode();
 			leastUpperBoundKeyType = leastUpperBoundKeyType.lub(new TypeDeducer(keyNode, typeStore).getType());
 			leastUpperBoundValueType = leastUpperBoundKeyType.lub(new TypeDeducer(valueNode, typeStore).getType());
 		}
 		return typeFactory.mapType(leastUpperBoundKeyType, leastUpperBoundValueType);
 	}
 
 	private Type getSetType() {
 		return (!currentNode.hasRelationship(Direction.OUTGOING, RelTypes.ELE)) ? typeFactory.setType(typeFactory.voidType()) : typeFactory.setType(getSetLub());
 	}
 
 	private Type getListType() {
 		return (!hasHead()) ? typeFactory.listType(typeFactory.voidType()) : typeFactory.listType(getListLub());
 	}
 	
 	private Type getSetLub() {
 		Iterable<Relationship> rels = currentNode.getRelationships(RelTypes.ELE, Direction.OUTGOING);
 		Type leastUpperBoundType = typeFactory.voidType();
 		for (Relationship rel : rels) {
 			currentNode = rel.getEndNode();
 			leastUpperBoundType = leastUpperBoundType.lub(getType());
 		}
 		return leastUpperBoundType;
 	}
 
 	private Type getListLub() {
 		currentNode = currentNode.getSingleRelationship(RelTypes.HEAD, Direction.OUTGOING).getEndNode();
 		Type leastUpperBoundType = getType();
 		while (currentNode.hasRelationship(Direction.OUTGOING, RelTypes.TO)) {
 			currentNode = currentNode.getSingleRelationship(RelTypes.TO, 
 					Direction.OUTGOING).getEndNode();
 			leastUpperBoundType = leastUpperBoundType.lub(getType());
 		}
 		return leastUpperBoundType;
 	}
 
 	private boolean hasHead() {
 		return currentNode.hasRelationship(Direction.OUTGOING, RelTypes.HEAD);
 	}
 	
 }
