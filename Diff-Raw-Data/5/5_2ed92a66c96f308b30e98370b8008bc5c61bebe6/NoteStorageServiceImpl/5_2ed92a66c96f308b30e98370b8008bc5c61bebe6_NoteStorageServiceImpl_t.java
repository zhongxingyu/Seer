 package org.biosemantics.disambiguation.service.impl;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import org.biosemantics.conceptstore.common.domain.Note;
 import org.biosemantics.conceptstore.common.service.NoteStorageService;
 import org.biosemantics.disambiguation.domain.impl.NoteImpl;
 import org.biosemantics.disambiguation.service.IndexService;
 import org.neo4j.graphdb.Node;
 
 public class NoteStorageServiceImpl implements NoteStorageService {
 
 	private final GraphStorageTemplate graphStorageTemplate;
 	private final Node noteParentNode;
 
 	private boolean checkExists;
 	private IndexService indexService;
 
 	public NoteStorageServiceImpl(GraphStorageTemplate graphStorageTemplate) {
 		this.graphStorageTemplate = checkNotNull(graphStorageTemplate);
		this.noteParentNode = this.graphStorageTemplate.getParentNode(DefaultRelationshipType.NOTES);
 		this.checkExists = false;
 	}
 
 	public void setCheckExists(boolean checkExists) {
 		this.checkExists = checkExists;
 	}
 
 	public void setIndexService(IndexService indexService) {
 		this.indexService = indexService;
 	}
 
 	@Override
 	public Note createDefinition(Note note) {
 		Note createdNote = null;
 		if (checkExists) {
 			// check if node exits in data store
 			createdNote = findNote(note);
 		}
 		if (createdNote == null) {
 			// create new node if none exists
 			Node node = graphStorageTemplate.getGraphDatabaseService().createNode();
			graphStorageTemplate.createRelationship(noteParentNode, node, DefaultRelationshipType.NOTE);
 			createdNote = new NoteImpl(node).withLanguage(note.getLanguage()).withText(note.getText());
 		}
 		return createdNote;
 	}
 
 	private Note findNote(Note note) {
 		throw new UnsupportedOperationException("findNote(note) is not supported yet");
 	}
 }
