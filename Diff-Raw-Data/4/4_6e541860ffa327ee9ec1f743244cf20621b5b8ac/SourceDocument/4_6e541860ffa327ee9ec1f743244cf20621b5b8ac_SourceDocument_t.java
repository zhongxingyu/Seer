 package kkckkc.syntaxpane.model;
 
 import javax.swing.event.DocumentEvent;
 import javax.swing.text.PlainDocument;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import kkckkc.syntaxpane.model.FoldManager.FoldListener;
 import kkckkc.syntaxpane.parse.ContentCharProvider;
 import kkckkc.syntaxpane.parse.Parser;
 import kkckkc.syntaxpane.parse.grammar.Language;
 
 
 
 public class SourceDocument extends PlainDocument {
 	private static Logger logger = LoggerFactory.getLogger(SourceDocument.class);
 	
 	private static final long serialVersionUID = 1L;
 
 	private Parser parser;
 	private LineManager lineManager;
 	private FoldManager foldManager;
 	
 	public SourceDocument() {
 		this.lineManager = new LineManager(new ContentCharProvider(getContent()));
 		this.foldManager = new FoldManager(this.lineManager);
 
 		putProperty(PlainDocument.tabSizeAttribute, 4);
 	}
 	
 	public void setLanguage(Language lang) {
 		logger.debug("Changing to language: " + lang.getName());
 		
 		this.parser = new Parser(lang, this.lineManager, this.foldManager);
 		fireChangedUpdate(new DefaultDocumentEvent(0, getLength(), DocumentEvent.EventType.CHANGE));
 	}
 	
 	@Override
 	protected void fireChangedUpdate(DocumentEvent e) {
 		parser.parse(e.getOffset(), e.getOffset() + e.getLength(), Parser.ChangeEvent.UPDATE);
 		super.fireChangedUpdate(e);
 	}
 
 	@Override
 	protected void fireInsertUpdate(DocumentEvent e) {
 		parser.parse(e.getOffset(), e.getOffset() + e.getLength(), Parser.ChangeEvent.ADD);
 		super.fireInsertUpdate(e);
 	}
 
 	@Override
 	protected void fireRemoveUpdate(DocumentEvent e) {
 		parser.parse(e.getOffset(), e.getOffset() + e.getLength(), Parser.ChangeEvent.REMOVE);
 		super.fireRemoveUpdate(e);
 	}
 	
 	public Scope getScopeForPosition(int dot) {
 		LineManager.Line l = this.lineManager.getLineByPosition(dot);
		if (l == null || getLength() == 0) {
			return new Scope(0, 0, this.parser.getLanguage().getRootContext(), null);
		}
 		return l.getScope().getRoot().getForPosition(dot - l.getStart());
 	}
 	
 	public LineManager getLineManager() {
 		return this.lineManager;
 	}
 
 	public FoldManager getFoldManager() {
 		return foldManager;
 	}
 
 	public void addFoldListener(FoldListener foldListener) {
 		foldManager.addFoldListener(foldListener);
 	}
 
 	public Language getLanguage() {
 	    return this.parser.getLanguage();
     }
 
 }
