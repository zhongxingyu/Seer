 /*
  * The contents of this file are subject to the Open Software License
  * Version 3.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.opensource.org/licenses/osl-3.0.txt
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  *
  */
 
 package org.mulgara.resolver.distributed;
 
 import org.mulgara.query.Answer;
 import org.mulgara.query.Constraint;
 import org.mulgara.query.ConstraintElement;
 import org.mulgara.query.ConstraintImpl;
 import org.mulgara.query.LocalNode;
 import org.mulgara.query.ModelResource;
 import org.mulgara.query.Query;
 import org.mulgara.query.QueryException;
 import org.mulgara.query.UnconstrainedAnswer;
 import org.mulgara.query.Variable;
 import org.mulgara.query.rdf.URIReferenceImpl;
 import org.mulgara.server.Session;
 import org.mulgara.server.SessionFactory;
 import org.mulgara.server.ServerInfo;
 import org.mulgara.server.NonRemoteSessionException;
 import org.mulgara.server.driver.SessionFactoryFinder;
 import org.mulgara.server.driver.SessionFactoryFinderException;
 import org.mulgara.resolver.spi.GlobalizeException;
 import org.mulgara.resolver.spi.Resolution;
 import org.mulgara.resolver.spi.ResolverException;
 import org.mulgara.resolver.spi.ResolverSession;
 
 import org.jrdf.graph.Node;
 import org.jrdf.graph.URIReference;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.*;
 
 /**
  * Resolve a constraint across a socket.
  *
  * @created 2007-03-20
  * @author <a href="mailto:gearon@users.sourceforge.net">Paul Gearon</a>
  * @version $Revision: $
  * @modified $Date: $
  * @maintenanceAuthor $Author: $
  * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
  */
 public class NetworkDelegator implements Delegator {
 
   /** The session to delegate resolutions through. */
   private ResolverSession session;
 
   /** A cache of distributed sessions. */
   private Map<URI,Session> sessionCache = new HashMap<URI,Session>();
 
   /**
    * A cache of distributed session factories.
    * Each entry matches an entry in sessionCache, but a separate set for cleaner code.
    */
   private List<SessionFactory> factoryCache = new ArrayList<SessionFactory>();
 
 
   /**
    * Constructs a delegator, using a given session.
    * @param session The session to delegate resolution through.
    */
   public NetworkDelegator(ResolverSession session) {
     this.session = session;
   }
 
 
   /**
    * Resolve a given constraint down to the appropriate resolution.
    * @param localConstraint The constraint to resolve in local form.
    * @param localModel The LocalNode containing the model.
    * @throws QueryException A error occurred resolving the constraint.
    */
   public Resolution resolve(Constraint localConstraint, LocalNode localModel) throws QueryException {
     // globalize the model
     Node modelNode = globalizeNode(localModel);
     if (!(modelNode instanceof URIReference)) throw new QueryException("Unexpected model type in constraint: (" + modelNode.getClass() + ")" + modelNode.toString());
     // convert the node to a URIReferenceImpl, which includes the Value interface
     URIReferenceImpl model = makeRefImpl((URIReference)modelNode);
 
     // check if this model is really on a remote server
     URI modelUri = model.getURI();
     testForLocality(modelUri);
 
     Answer ans = getModelSession(modelUri).query(globalizedQuery(localConstraint, model));
     return new AnswerResolution(session, ans, localConstraint);
   }
 
 
   /**
    * Create a query for a single constraint.
    * @param constraint The local constraint to query for.
    * @return The globalized query, looking for the single constraint.
    */
   protected Query globalizedQuery(Constraint localConstraint, URIReferenceImpl model) throws QueryException {
     // convert the constraint to network compatible form
     Constraint globalConstraint = new ConstraintImpl(
             globalizeConstraintElement(localConstraint.getElement(0)),
             globalizeConstraintElement(localConstraint.getElement(1)),
             globalizeConstraintElement(localConstraint.getElement(2)),
             model
     );
 
     // convert the variable set to a variable list
     List<Variable> variables = new ArrayList<Variable>((Set<Variable>)globalConstraint.getVariables());
     // build the new query
     return new Query(variables, new ModelResource(model.getURI()), globalConstraint, null, Collections.EMPTY_LIST, null, 0, new UnconstrainedAnswer());
   }
 
 
   /**
    * Convert a local node to a global value.
    * @param localNode The node to globalize.
    * @return The globalized node, either a BlankNode, a URIReference, or a Literal.
    * @throws QueryException An error occurred while globalizing
    */
   protected Node globalizeNode(LocalNode localNode) throws QueryException {
     try {
       return session.globalize(localNode.getValue());
     } catch (GlobalizeException ge) {
       throw new QueryException("Error globalizing node: " + localNode, ge);
     }
   }
 
 
   /**
    * Converts a constraint element from local form into global form.
    * @param localElement The constraint element in local form.
    * @throws QueryException The constraint element could not be globalized.
    */
   protected ConstraintElement globalizeConstraintElement(ConstraintElement localElement) throws QueryException {
     // return the element if it does not need to be converted
     if (!(localElement instanceof LocalNode) || (localElement instanceof URIReferenceImpl)) return localElement;
 
     // try {
       // convert the reference to a Value
       return makeRefImpl((URIReference)globalizeNode((LocalNode)localElement));
     // } catch (ResolverException re) {
       // throw new QueryException("Unable to globalize constraint element: " + localElement, re);
     // }
   }
 
 
   /**
    * Guarantee that a URIReference is a URIReferenceImpl, wrapping in a new URIReferenceImpl if needed.
    * This method is required since URIReferenceImpl meets the Value interface when URIReference does not.
    * @param ref The reference to convert if needed.
    * @return A URIReferenceImpl matching ref.
    */
   protected URIReferenceImpl makeRefImpl(URIReference ref) {
     return (ref instanceof URIReferenceImpl) ? (URIReferenceImpl)ref : new URIReferenceImpl(ref.getURI());
   }
 
 
   /**
    * Tests if a model is really on a different server.  If the model is local then throw an exception.
    * @param modelUri The URI of the model to test.
    * @throws QueryException Thrown when the model is on the current system.
    */
   protected void testForLocality(URI modelUri) throws QueryException {
     String protocol = modelUri.getScheme();
     if (!DistributedResolverFactory.getProtocols().contains(protocol)) {
       throw new IllegalStateException("Bad Protocol sent to distributed resolver.");
     }
     String host = modelUri.getHost();
     if (ServerInfo.getHostnameAliases().contains(host)) {
      throw new QueryException("Attempt to resolve a local model through the distributed resolver.");
     }
   }
 
 
   /**
    * Gets a remote session on a server specified by a given model URI.
    * @param modelUri The URI of the model to get a session for.
    * @return a remote session on the host found in the model.
    * @throws QueryException Thrown when the model is a bad URI, or the session cannot be created.
    */
   protected Session getModelSession(URI modelUri) throws QueryException {
     try {
       // use the URI without the model fragment
      return getServerSession(new URI(modelUri.getScheme(), modelUri.getSchemeSpecificPart(), ""));
     } catch (URISyntaxException use) {
       throw new AssertionError(use);
     }
   }
 
 
   /**
    * Retrieves a session for a given server URI, using a cached value if possible.
    * @param serverUri The URI of the server to get a session for.
    * @return a remote session on the host specified in serverUri.
    * @throws QueryException Thrown when the session cannot be created.
    */
   protected Session getServerSession(URI serverUri) throws QueryException {
     Session session = sessionCache.get(serverUri);
     return (session != null) ? session : newSession(serverUri);
   }
 
 
   /**
    * Get a new session and save in the cache.
    * @param serverUri The URI of the server to create a session for.
    * @return A new remote session.
    * @throws QueryException There was a problem creating the session.
    */
   protected Session newSession(URI serverUri) throws QueryException {
     try {
       // The factory won't be in the cache, as a corresponding session would have already been created.
       SessionFactory sessionFactory = SessionFactoryFinder.newSessionFactory(serverUri, true);
       factoryCache.add(sessionFactory);
       // now create the session
       Session session = sessionFactory.newSession();
       sessionCache.put(serverUri, session);
       return session;
     } catch (NonRemoteSessionException nrse) {
       throw new QueryException("State Error: non-local URI was mapped to a local session", nrse);
     } catch (SessionFactoryFinderException sffe) {
       throw new QueryException("Unable to get a session to the server", sffe);
     }
   }
 
 }
