 package org.geogit.web.api;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.annotation.Nullable;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.jettison.AbstractXMLStreamWriter;
 import org.geogit.api.GeogitSimpleFeature;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.Remote;
 import org.geogit.api.RevCommit;
 import org.geogit.api.RevPerson;
 import org.geogit.api.RevTag;
 import org.geogit.api.SymRef;
 import org.geogit.api.plumbing.DiffIndex;
 import org.geogit.api.plumbing.DiffWorkTree;
 import org.geogit.api.plumbing.diff.AttributeDiff;
 import org.geogit.api.plumbing.diff.DiffEntry;
 import org.geogit.api.plumbing.diff.DiffEntry.ChangeType;
 import org.opengis.feature.type.PropertyDescriptor;
 
 import com.google.common.collect.ImmutableList;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  *
  */
 public class ResponseWriter {
 
     protected final XMLStreamWriter out;
 
     public ResponseWriter(XMLStreamWriter out) {
         this.out = out;
         if (out instanceof AbstractXMLStreamWriter) {
             configureJSONOutput((AbstractXMLStreamWriter) out);
         }
     }
 
     private void configureJSONOutput(AbstractXMLStreamWriter out) {
     }
 
     public void finish() throws XMLStreamException {
         out.writeEndElement(); // results
         out.writeEndDocument();
     }
 
     public void start() throws XMLStreamException {
         start(true);
     }
 
     public void start(boolean success) throws XMLStreamException {
         out.writeStartDocument();
         out.writeStartElement("response");
         writeElement("success", Boolean.toString(success));
     }
 
     public void writeHeaderElements(String... els) throws XMLStreamException {
         out.writeStartElement("header");
         for (int i = 0; i < els.length; i += 2) {
             writeElement(els[i], els[i + 1]);
         }
         out.writeEndElement();
     }
 
     public void writeErrors(String... errors) throws XMLStreamException {
         out.writeStartElement("errors");
         for (int i = 0; i < errors.length; i += 2) {
             writeElement(errors[i], errors[i + 1]);
         }
         out.writeEndElement();
     }
 
     public XMLStreamWriter getWriter() {
         return out;
     }
 
     public void writeElement(String element, @Nullable String content) throws XMLStreamException {
         out.writeStartElement(element);
         if (content != null) {
             out.writeCharacters(content);
         }
         out.writeEndElement();
     }
 
     public void writeStaged(DiffIndex setFilter, int start, int length) throws XMLStreamException {
         writeDiffEntries("staged", start, length, setFilter.call());
     }
 
     public void writeUnstaged(DiffWorkTree setFilter, int start, int length)
             throws XMLStreamException {
         writeDiffEntries("unstaged", start, length, setFilter.call());
     }
 
     @SuppressWarnings("rawtypes")
     private void advance(Iterator it, int cnt) {
         for (int i = 0; i < cnt && it.hasNext(); i++) {
             it.next();
         }
     }
 
     public void writeDiffEntries(String name, int start, int length, Iterator<DiffEntry> entries)
             throws XMLStreamException {
         advance(entries, start);
         if (length < 0) {
             length = Integer.MAX_VALUE;
         }
         for (int i = 0; i < length && entries.hasNext(); i++) {
             DiffEntry entry = entries.next();
             out.writeStartElement(name);
             writeElement("changeType", entry.changeType().toString());
             NodeRef oldObject = entry.getOldObject();
             NodeRef newObject = entry.getNewObject();
             if (oldObject == null) {
                 writeElement("newPath", newObject.path());
                 writeElement("newObjectId", newObject.objectId().toString());
                 writeElement("path", "");
                 writeElement("oldObjectId", ObjectId.NULL.toString());
             } else if (newObject == null) {
                 writeElement("newPath", "");
                 writeElement("newObjectId", ObjectId.NULL.toString());
                 writeElement("path", oldObject.path());
                 writeElement("oldObjectId", oldObject.objectId().toString());
             } else {
                 writeElement("newPath", newObject.path());
                 writeElement("newObjectId", newObject.objectId().toString());
                 writeElement("path", oldObject.path());
                 writeElement("oldObjectId", oldObject.objectId().toString());
 
             }
             out.writeEndElement();
         }
     }
 
     public void writeCommits(Iterator<RevCommit> entries, int page, int elementsPerPage)
             throws XMLStreamException {
         advance(entries, page * elementsPerPage);
         int counter = 0;
         while (entries.hasNext() && counter < elementsPerPage) {
             RevCommit entry = entries.next();
             out.writeStartElement("commit");
             writeElement("id", entry.getId().toString());
             writeElement("tree", entry.getTreeId().toString());
 
             ImmutableList<ObjectId> parentIds = entry.getParentIds();
             out.writeStartElement("parents");
             for (ObjectId parentId : parentIds) {
                 writeElement("id", parentId.toString());
             }
             out.writeEndElement();
 
             writePerson("author", entry.getAuthor());
             writePerson("committer", entry.getCommitter());
 
             out.writeStartElement("message");
             if (entry.getMessage() != null) {
                 out.writeCData(entry.getMessage());
             }
             out.writeEndElement();
 
             out.writeEndElement();
             counter++;
         }
     }
 
     public void writePerson(String enclosingElement, RevPerson p) throws XMLStreamException {
         out.writeStartElement(enclosingElement);
         writeElement("name", p.getName().orNull());
         writeElement("email", p.getEmail().orNull());
         writeElement("timestamp", Long.toString(p.getTimestamp()));
         writeElement("timeZoneOffset", Long.toString(p.getTimeZoneOffset()));
         out.writeEndElement();
     }
 
     public void writeCommitResponse(Iterator<DiffEntry> diff) throws XMLStreamException {
         int adds = 0, deletes = 0, changes = 0;
         DiffEntry diffEntry;
         while (diff.hasNext()) {
             diffEntry = diff.next();
             switch (diffEntry.changeType()) {
             case ADDED:
                 ++adds;
                 break;
             case REMOVED:
                 ++deletes;
                 break;
             case MODIFIED:
                 ++changes;
                 break;
             }
         }
         writeElement("added", Integer.toString(adds));
         writeElement("changed", Integer.toString(changes));
         writeElement("deleted", Integer.toString(deletes));
     }
 
     public void writeLsTreeResponse(Iterator<NodeRef> iter, boolean verbose)
             throws XMLStreamException {
 
         while (iter.hasNext()) {
             NodeRef node = iter.next();
             out.writeStartElement("node");
             writeElement("path", node.path());
             if (verbose) {
                 writeElement("metadataId", node.getMetadataId().toString());
                 writeElement("type", node.getType().toString().toLowerCase());
                 writeElement("objectId", node.objectId().toString());
             }
             out.writeEndElement();
         }
 
     }
 
     public void writeUpdateRefResponse(Ref ref) throws XMLStreamException {
         out.writeStartElement("ChangedRef");
         writeElement("name", ref.getName());
         writeElement("objectId", ref.getObjectId().toString());
         if (ref instanceof SymRef) {
             writeElement("target", ((SymRef) ref).getTarget());
         }
         out.writeEndElement();
     }
 
     public void writeRefParseResponse(Ref ref) throws XMLStreamException {
         out.writeStartElement("Ref");
         writeElement("name", ref.getName());
         writeElement("objectId", ref.getObjectId().toString());
         if (ref instanceof SymRef) {
             writeElement("target", ((SymRef) ref).getTarget());
         }
         out.writeEndElement();
     }
 
     public void writeEmptyRefResponse() throws XMLStreamException {
         out.writeStartElement("RefNotFound");
         out.writeEndElement();
     }
 
     public void writeBranchListResponse(List<Ref> localBranches, List<Ref> remoteBranches)
             throws XMLStreamException {
 
         out.writeStartElement("Local");
         for (Ref branch : localBranches) {
             out.writeStartElement("Branch");
             writeElement("name", branch.localName());
             out.writeEndElement();
         }
         out.writeEndElement();
 
         out.writeStartElement("Remote");
         for (Ref branch : remoteBranches) {
             if (!(branch instanceof SymRef)) {
                 out.writeStartElement("Branch");
                 writeElement("remoteName", branch.namespace().replace(Ref.REMOTES_PREFIX + "/", ""));
                 writeElement("name", branch.localName());
                 out.writeEndElement();
             }
         }
         out.writeEndElement();
 
     }
 
     public void writeRemoteListResponse(List<Remote> remotes) throws XMLStreamException {
         for (Remote remote : remotes) {
             out.writeStartElement("Remote");
             writeElement("name", remote.getName());
             out.writeEndElement();
         }
     }
 
     public void writeTagListResponse(List<RevTag> tags) throws XMLStreamException {
         for (RevTag tag : tags) {
             out.writeStartElement("Tag");
             writeElement("name", tag.getName());
             out.writeEndElement();
         }
     }
 
     public void writeFeatureDiffResponse(Map<PropertyDescriptor, AttributeDiff> diffs)
             throws XMLStreamException {
         Set<Entry<PropertyDescriptor, AttributeDiff>> entries = diffs.entrySet();
         Iterator<Entry<PropertyDescriptor, AttributeDiff>> iter = entries.iterator();
         while (iter.hasNext()) {
             Entry<PropertyDescriptor, AttributeDiff> entry = iter.next();
             out.writeStartElement("diff");
             writeElement("attributename", entry.getKey().getName().toString());
             writeElement("changetype", entry.getValue().getType().toString());
             if (entry.getValue().getOldValue() != null
                     && entry.getValue().getOldValue().isPresent()) {
                 writeElement("oldvalue", entry.getValue().getOldValue().get().toString());
             }
             if (entry.getValue().getNewValue() != null
                     && entry.getValue().getNewValue().isPresent()) {
                 writeElement("newvalue", entry.getValue().getNewValue().get().toString());
             }
             out.writeEndElement();
         }
     }
 
     public void writeDiffResponse(Iterator<GeogitSimpleFeature> features,
             Iterator<ChangeType> changes) throws XMLStreamException {
 
         while (features.hasNext()) {
             GeogitSimpleFeature feature = features.next();
             ChangeType change = changes.next();
             out.writeStartElement("Feature");
             writeElement("change", change.toString());
             writeElement("id", feature.getID().toString());
            List<Object> attributes = feature.getAttributes();
            for (Object attribute : attributes) {
                if (attribute instanceof Geometry) {
                    writeElement("geometry", ((Geometry) attribute).toText());
                    break;
                }
            }

             out.writeEndElement();
         }
 
     }
 }
