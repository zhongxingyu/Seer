 package org.mule.galaxy.atom;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.activation.MimeType;
 import javax.activation.MimeTypeParseException;
 import javax.xml.namespace.QName;
 
 import org.acegisecurity.context.SecurityContextHolder;
 import org.apache.abdera.Abdera;
 import org.apache.abdera.factory.Factory;
 import org.apache.abdera.i18n.iri.IRI;
 import org.apache.abdera.i18n.text.UrlEncoding;
 import org.apache.abdera.i18n.text.CharUtils.Profile;
 import org.apache.abdera.model.Content;
 import org.apache.abdera.model.Document;
 import org.apache.abdera.model.Element;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Person;
 import org.apache.abdera.model.Text;
 import org.apache.abdera.model.Text.Type;
 import org.apache.abdera.parser.ParseException;
 import org.apache.abdera.protocol.server.RequestContext;
 import org.apache.abdera.protocol.server.RequestContext.Scope;
 import org.apache.abdera.protocol.server.context.EmptyResponseContext;
 import org.apache.abdera.protocol.server.context.ResponseContextException;
 import org.apache.abdera.protocol.server.context.SimpleResponseContext;
 import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactPolicyException;
 import org.mule.galaxy.ArtifactResult;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.PropertyException;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.impl.jcr.UserDetailsWrapper;
 import org.mule.galaxy.lifecycle.Lifecycle;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.lifecycle.Phase;
 import org.mule.galaxy.lifecycle.TransitionException;
 import org.mule.galaxy.policy.ApprovalMessage;
 import org.mule.galaxy.security.User;
 
 public abstract class AbstractArtifactCollection 
     extends AbstractEntityCollectionAdapter<ArtifactVersion> {
     public static final String NAMESPACE = "http://galaxy.mule.org/1.0";
     public static final String ID_PREFIX = "urn:galaxy:artifact:";
 
     protected Factory factory = new Abdera().getFactory();
     protected Registry registry;
     protected LifecycleManager lifecycleManager;
     
     public AbstractArtifactCollection(Registry registry, LifecycleManager lifecycleManager) {
         super();
         this.registry = registry;
         this.lifecycleManager = lifecycleManager;
     }
 
     public Content getContent(ArtifactVersion doc, RequestContext request) {
         // Not used since these are media entries
         throw new UnsupportedOperationException();
     }
     
     @Override
     protected String addEntryDetails(RequestContext request, 
                                      Entry e, 
                                      IRI feedIri, 
                                      ArtifactVersion entryObj)
         throws ResponseContextException {
         String link = super.addEntryDetails(request, e, feedIri, entryObj);
 
         Element metadata = factory.newElement(new QName(NAMESPACE, "metadata"));
         
         for (Iterator<PropertyInfo> props = entryObj.getProperties(); props.hasNext();) {
             PropertyInfo p = props.next();
             if (p.isVisible()) {
                 Element prop = factory.newElement(new QName(NAMESPACE, "property"), metadata);
                 prop.setAttributeValue("name", p.getName());
                 prop.setAttributeValue("locked", new Boolean(p.isLocked()).toString());
                 
                 Object value = p.getValue();
                 if (value == null) {
                     value = "";
                 }
                 
                 if (value instanceof Collection) {
                     for (Object o : ((Collection) value)) {
                         Element valueEl = factory.newElement(new QName(NAMESPACE, "value"), prop);
                         
                         valueEl.setText(o.toString());
                     }
                 } else {
                     prop.setAttributeValue("value", value.toString());
                 }
             }
         }
         
         e.addExtension(metadata);
         
         Element lifecycle = factory.newElement(new QName(NAMESPACE, "lifecycle"));
         Phase phase = entryObj.getParent().getPhase();
         lifecycle.setAttributeValue("name", phase.getLifecycle().getName());
         lifecycle.setAttributeValue("phase", phase.getName());
         
         e.addExtension(lifecycle);
         
         e.addExtension(buildAvailablePhases(phase));
         return link;
     }
 
     private Element buildAvailablePhases(Phase phase) {
         Element availPhases = factory.newElement(new QName(NAMESPACE, "available-phases"));
         StringBuilder sb = new StringBuilder();
         boolean first = true;
         for (Phase p : phase.getNextPhases()) {
             if (!first) {
                 sb.append(", ");
             } else {
                 first = false;
             }
             
             sb.append(p.getName());
         }
         availPhases.setText(sb.toString());
         return availPhases;
     }
 
     @Override
     public Text getSummary(ArtifactVersion entry, RequestContext request) {
         Text summary = factory.newSummary();
         
         String d = entry.getParent().getDescription();
         if (d == null) d = "";
         
         summary.setText(d);
         summary.setTextType(Type.XHTML);
         
         return summary;
     }
 
     public InputStream getMediaStream(ArtifactVersion entry) throws ResponseContextException {
         return entry.getStream();
     }
     
     @Override
     public String getContentType(ArtifactVersion entry) {
         return entry.getParent().getContentType().toString();
     }
 
     public String getAuthor() {
         return "Mule Galaxy";
     }
 
     protected StringBuilder getBasePath(Artifact a) {
         StringBuilder sb = new StringBuilder();
         
         Workspace w = a.getWorkspace();
         while (w != null) {
             sb.insert(0, '/');
             sb.insert(0, UrlEncoding.encode(w.getName(), Profile.PATH.filter()));
             w = w.getParent();
         }
         return sb;
     }
     public Date getUpdated(ArtifactVersion doc) {
         return doc.getCreated().getTime();
     }
 
     @Override
     public ArtifactVersion postEntry(String arg0, IRI arg1, String arg2, Date arg3, List<Person> arg4, Content arg5, RequestContext request)
         throws ResponseContextException {
         throw new ResponseContextException(new EmptyResponseContext(500));
     }
 
     @Override
     public ArtifactVersion postMedia(MimeType mimeType, 
                                      String slug, 
                                      InputStream inputStream,
                                      RequestContext request) throws ResponseContextException {
         try {
             String version = getVersion(request);
             
             User user = getUser();
             
             ArtifactResult result = postMediaEntry(slug, mimeType, version, inputStream, user, request);
             
             return result.getArtifactVersion();
         } catch (RegistryException e) {
             throw new ResponseContextException(500, e);
         } catch (IOException e) {
             throw new ResponseContextException(500, e);
         } catch (MimeTypeParseException e) {
             throw new ResponseContextException(500, e);
         } catch (ArtifactPolicyException e) {
             throw createArtifactPolicyExceptionResponse(e);
         }
     }
 
     protected ResponseContextException createArtifactPolicyExceptionResponse(ArtifactPolicyException e) {
         final StringBuilder s = new StringBuilder();
         s.append("<html><head><title>Artifact Policy Failure</title></head><body>");
         
         List<ApprovalMessage> approvals = e.getApprovals();
         
         for (ApprovalMessage m : approvals) {
             if (m.isWarning()) {
                 s.append("<div class=\"warning\">");
             } else {
                 s.append("<div class=\"failure\">");
             }
             
             s.append(m.getMessage());
             s.append("</div>");
         }
         
         s.append("</html>");
         SimpleResponseContext rc = new SimpleResponseContext() {
             @Override
             protected void writeEntity(Writer writer) throws IOException {
                 writer.write(s.toString());
             }
 
             public boolean hasEntity() {
                 return true;
             }
         };
         rc.setContentType("application/xhtml");
         // bad request code
         rc.setStatus(400);
         
         return new ResponseContextException(rc);
     }
 
     protected User getUser() {
         UserDetailsWrapper wrapper = 
             (UserDetailsWrapper) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         User user = wrapper.getUser();
         return user;
     }
 
     protected String getVersion(RequestContext request) throws ResponseContextException {
         String version = request.getHeader("X-Artifact-Version");
         
         if (version == null || version.equals("")) {
             EmptyResponseContext ctx = new EmptyResponseContext(500);
             ctx.setStatusText("You must supply an X-Artifact-Version header!");
             
             throw new ResponseContextException(ctx);
         }
         return version;
     }
 
     protected abstract ArtifactResult postMediaEntry(String slug, MimeType mimeType, String version,
                                                      InputStream inputStream, User user, RequestContext ctx)
         throws RegistryException, ArtifactPolicyException, IOException, MimeTypeParseException, ResponseContextException;
     
     @Override
     public boolean isMediaEntry(ArtifactVersion entry) {
         return true;
     }
 
     @Override
     public List<Person> getAuthors(ArtifactVersion entry, RequestContext request) throws ResponseContextException {
         Person author = request.getAbdera().getFactory().newAuthor();
         author.setName("Galaxy");
         return Arrays.asList(author);
     }
     
     @Override
     public String getMediaName(ArtifactVersion version) {
         return UrlEncoding.encode(version.getParent().getName());
     }
 
     @Override
     public String getHref(RequestContext request) {
         String href = (String) request.getAttribute(Scope.REQUEST, ArtifactResolver.COLLECTION_HREF);
         if (href == null) {
             // this is the url we use when pulling down the services document
             href = request.getTargetBasePath() + "/registry";
         }
         return href;
     }
     
     @Override
     protected String getFeedIriForEntry(ArtifactVersion entryObj, RequestContext request) {
         Artifact a = entryObj.getParent();
         
         return request.getTargetBasePath() 
                + "/registry" 
                + UrlEncoding.encode(a.getWorkspace().getPath(), Profile.PATH.filter());
     }
 
     @Override
     public String getName(ArtifactVersion doc) {
         return UrlEncoding.encode(doc.getParent().getName(), Profile.PATH.filter()) + ";atom";
     }
     
     public ArtifactVersion getEntry(String name, RequestContext request) throws ResponseContextException {
         Artifact a = getArtifact(request);
         return selectVersion(a, request.getParameter("version"));
     }
 
     protected ArtifactVersion selectVersion(Artifact next, String version) throws ResponseContextException {
         if (version != null) {
             ArtifactVersion v = next.getVersion(version);
             
             if (v == null || "".equals(version)) {
                 EmptyResponseContext res = new EmptyResponseContext(404);
                 res.setStatusText("Version " + version + " was not found.");
                 throw new ResponseContextException(res);
             }
         }
         return next.getActiveVersion();
     }
 
     protected Artifact getArtifact(RequestContext request) {
         return (Artifact) request.getAttribute(Scope.REQUEST, ArtifactResolver.ARTIFACT);
     }
 
     @Override
     public void putEntry(ArtifactVersion av, 
                          String title, 
                          Date updated, 
                          List<Person> authors, 
                          String summary,
                          Content content, 
                          RequestContext request) throws ResponseContextException {
         Artifact artifact = av.getParent();
         artifact.setDescription(summary);
 //        artifact.setName(title);
         
         try {
             Document<Entry> entryDoc = request.getDocument();
             Entry entry = entryDoc.getRoot();
             
             for (Element e : entry.getElements()) {
                 QName q = e.getQName();
                 if (NAMESPACE.equals(q.getNamespaceURI())) {
                     if ("lifecycle".equals(q.getLocalPart())) {
                         updateLifecycle(av, e);
                     } else if ("metadata".equals(q.getLocalPart())) {
                         updateMetadata(av, e);
                     }
                 }
             }
         } catch (ParseException e) {
             throw new ResponseContextException(500, e);
         } catch (IOException e) {
             throw new ResponseContextException(500, e);
         }
     }
 
     private void updateLifecycle(ArtifactVersion av, Element e) throws ResponseContextException {
         String name = e.getAttributeValue("name");
         assertNotEmpty(name, "Lifecycle name attribute cannot be null.");
         
         String phaseName = e.getAttributeValue("phase");
         assertNotEmpty(phaseName, "Lifecycle phase attribute cannot be null.");
         
        Phase current = av.getParent().getPhase();
        if (name.equals(current.getLifecycle().getName()) 
            && phaseName.equals(current.getName())) {
            return;
        }
            
         Lifecycle lifecycle = lifecycleManager.getLifecycle(name);
         
         if (lifecycle == null)
             throwMalformed("Lifecycle \"" + name + "\" does not exist.");
         
         Phase phase = lifecycle.getPhase(phaseName);
 
         if (phase == null)
             throwMalformed("Lifecycle phase \"" + phaseName + "\" does not exist.");
         
         try {
             lifecycleManager.transition(av.getParent(), phase, getUser());
         } catch (TransitionException e1) {
             throwMalformed(e1.getMessage());
         } catch (ArtifactPolicyException e1) {
             throw createArtifactPolicyExceptionResponse(e1);
         }
     }
 
     protected void assertNotEmpty(String name, String message) throws ResponseContextException {
         if (name == null || "".equals(name)) {
             throwMalformed(message);
         }
     }
 
     protected void throwMalformed(final String message) throws ResponseContextException {
         SimpleResponseContext rc = new SimpleResponseContext() {
 
             @Override
             protected void writeEntity(Writer writer) throws IOException {
                 writer.write("<html><head><title>)");
                 writer.write("Malformed Atom Entry");
                 writer.write("</title></head><body><div class=\"error\">");
                 writer.write(message);
                 writer.write("</div></body></html>");
             }
 
             public boolean hasEntity() {
                 return true;
             }
         };
         
         rc.setStatus(400);
         
         throw new ResponseContextException(rc);
     }
 
     private void updateMetadata(ArtifactVersion av, Element e) throws ResponseContextException {
         for (Element propEl : e.getElements()) {
             String name = propEl.getAttributeValue("name");
             if (name == null)
                 throwMalformed("You must specify name attributes on metadata properties.");
             
             String value = propEl.getAttributeValue("value");
             if (value != null) {
                 try {
                     av.setProperty(name, value);
                 } catch (PropertyException e1) {
                     // Ignore as its probably because its locked
                 }
             } else {
                 List<Element> elements = propEl.getElements();
                 ArrayList<String> values = new ArrayList<String>();
                 for (Element valueEl : elements) {
                     if (valueEl.getQName().getLocalPart().equals("value")) {
                         values.add(valueEl.getText().trim());
                     }
                 }
                 try {
                     av.setProperty(name, values);
                 } catch (PropertyException e1) {
                     // Ignore as its probably because its locked
                 }
             }
         }
     }
 
 
 }
