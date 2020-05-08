 package org.neo4j.rdf.store.representation.standard;
 
 import java.text.ParseException;
 
 import org.neo4j.meta.model.MetaModel;
 import org.neo4j.meta.model.MetaModelProperty;
 import org.neo4j.meta.model.PropertyRange;
 import org.neo4j.rdf.model.Literal;
 import org.neo4j.rdf.model.Resource;
 import org.neo4j.rdf.model.Statement;
 import org.neo4j.rdf.model.Uri;
 import org.neo4j.rdf.model.Value;
 import org.neo4j.rdf.model.Wildcard;
 import org.neo4j.rdf.store.representation.AbstractNode;
 import org.neo4j.rdf.store.representation.AbstractRelationship;
 import org.neo4j.rdf.store.representation.AbstractRepresentation;
 import org.neo4j.rdf.store.representation.RepresentationExecutor;
 import org.neo4j.rdf.store.representation.RepresentationStrategy;
 
 /**
  * Abstract class which holds common functionality for
  * {@link RepresentationStrategy} implementations using an
  * {@link UriBasedExecutor}.
  */
 abstract class StandardAbstractRepresentationStrategy
     implements RepresentationStrategy
 {
     private final RepresentationExecutor executor;
     private final MetaModel model;
 
     public StandardAbstractRepresentationStrategy(
         RepresentationExecutor executor, MetaModel model )
     {
         this.executor = executor;
         this.model = model;
     }
 
     public AbstractRepresentation getAbstractRepresentation(
         Statement statement, AbstractRepresentation representation )
     {
 //        String predicate =
 //            ( ( Uri ) statement.getPredicate() ).getUriAsString();
 //        if ( meta != null &&
 //            predicate.equals( AbstractUriBasedExecutor.RDF_TYPE_URI ) )
 //        {
 //            getMetaInstanceOfRepresentation( statement, representation );
 //            return representation;
 //        }
         
         // Just so that overriding classes can see if something happened in
         // here or not.
         return null;
     }
 
     public RepresentationExecutor getExecutor()
     {
         return this.executor;
     }
 
     protected AbstractRepresentation newRepresentation()
     {
         return new AbstractRepresentation();
     }
     
 //    protected void getMetaInstanceOfRepresentation(
 //        Statement statement, AbstractRepresentation representation )
 //    {
 //        AbstractNode subjectNode = getOrCreateNode( representation,
 //            statement.getSubject() );
 //        AbstractNode classNode = getOrCreateNode( representation,
 //            statement.getObject(), statement.getObject() );
 //        classNode.addExecutorInfo(
 //            AbstractUriBasedExecutor.META_EXECUTOR_INFO_KEY, "class" );
 //        AbstractRelationship instanceOfRelationship =
 //            new AbstractRelationship( subjectNode,
 //                MetaStructureRelTypes.META_IS_INSTANCE_OF.name(),
 //                classNode );
 //        representation.addRelationship( instanceOfRelationship );
 //    }
 
     protected void getOneNodeWithLiteralsAsProperties( Statement statement,
         AbstractRepresentation representation )
     {
         AbstractNode subjectNode = getOrCreateNode( representation,
             statement.getSubject() );
         addPropertyFromObjectLiteral( statement, subjectNode );
     }
 
     protected void addPropertyFromObjectLiteral( Statement statement,
         AbstractNode node )
     {
         Value object = statement.getObject();
         String predicate =
             ( ( Uri ) statement.getPredicate() ).getUriAsString();
         if ( object instanceof Wildcard )
         {
             node.addProperty( predicate, object );
             return;
         }
         else
         {
             Object literalValue = convertLiteralValueToRealValue(
                 statement, ( ( Literal ) statement.getObject() ).getValue() );
             node.addProperty( predicate, literalValue );
             
             // Tell the executor that this is a literal
             node.addExecutorInfo(
                 UriBasedExecutor.EXEC_INFO_KEYS_WHICH_ARE_LITERALS,
                 predicate );
         }
 
 //        node.addProperty( predicate, literalValue );
 //        String predicateContext = UriBasedExecutor.formContextPropertyKey(
 //            predicate, literalValue );
 //        if ( !statement.getContext().isWildcard() )
 //        {
 //            node.addProperty( predicateContext,
 //                ( ( Context ) statement.getContext() ).getUriAsString() );
 //        }
 //        Map<String, String> contextKeys = new HashMap<String, String>();
 //        contextKeys.put( predicateContext, predicate );
 //        node.addExecutorInfo( UriBasedExecutor.LOOKUP_CONTEXT_KEYS,
 //            contextKeys );
     }
 
    private PropertyRange getPropertyRange( Uri predicate )
     {
         if ( model == null )
         {
             return null;
         }
 
         MetaModelProperty property =
             model.getGlobalNamespace().getMetaProperty(
                 predicate.getUriAsString(), false );
        return property == null ? null :
            model.lookup( property, MetaModel.LOOKUP_PROPERTY_RANGE );
     }
 
     private boolean pointsToObjectType( Uri predicate )
     {
        PropertyRange range = getPropertyRange( predicate );
         if ( range == null )
         {
         	return false;
         }
        return !range.isDatatype();
     }
 
     protected Object convertLiteralValueToRealValue( Statement statement,
         Object literalValue )
     {
         Object result = literalValue;
         if ( result != null && result instanceof String && model != null )
         {
            PropertyRange range = getPropertyRange(
                 ( Uri ) statement.getPredicate() );
             if ( range != null && range.isDatatype() )
             {
                 try
                 {
                     result = range.rdfLiteralToJavaObject(
                         literalValue.toString() );
                 }
                 catch ( ParseException e )
                 {
                     // Ok?
                 }
             }
         }
         return result;
     }
 
     protected String asUri( Value value )
     {
         return ( ( Uri ) value ).getUriAsString();
     }
 
     protected String asString( Value value )
     {
         String string = null;
         if ( value instanceof Wildcard )
         {
             string = ( ( Wildcard ) value ).getVariableName();
         }
         else if ( value instanceof Uri )
         {
             string = ( ( Uri ) value ).getUriAsString();
         }
         else if ( value instanceof Literal )
         {
             string = ( ( Literal ) value ).getValue().toString();
         }
         return string;
     }
     
     protected AbstractNode getOrCreateNode(
         AbstractRepresentation representation, Value uriOrSomething )
     {
         return getOrCreateNode( representation, uriOrSomething,
             uriOrSomething );
     }
     
     protected AbstractNode getOrCreateNode(
         AbstractRepresentation representation, Value uriOrSomething,
         Object key )
     {
         AbstractNode node = representation.node( key );
         if ( node == null )
         {
             node = new AbstractNode( uriOrSomething, key );
             representation.addNode( node );
         }
         return node;
     }
 
     protected boolean objectIsObjectType( Statement statement )
     {
         return statement.getObject() instanceof Resource ||
             pointsToObjectType( ( Uri ) statement.getPredicate() );
     }
 
     protected AbstractRepresentation getTwoNodeObjectTypeFragment(
         Statement statement, AbstractRepresentation representation )
     {
         AbstractNode subjectNode = getOrCreateNode( representation,
             statement.getSubject() );
         AbstractNode objectNode = getOrCreateNode( representation,
             statement.getObject() );
         AbstractRelationship relationship = new AbstractRelationship(
             subjectNode, asUri( statement.getPredicate() ), objectNode );
         representation.addRelationship( relationship );
         return representation;
     }
     
     protected String formTripleNodeKey( Statement statement )
     {
         String s = "S" + statement.getSubject().toString();
         String p = "P" + ( ( Uri ) statement.getPredicate() ).getUriAsString();
         String o = null;
         if ( statement.getObject() instanceof Literal )
         {
             Object literalValue =
                 ( ( Literal ) statement.getObject() ).getValue();
             o = "L" + literalValue.getClass().getSimpleName() + literalValue;
         }
         else
         {
             o = "O" + statement.getObject().toString();
         }
         return s + p + o;
     }
 }
