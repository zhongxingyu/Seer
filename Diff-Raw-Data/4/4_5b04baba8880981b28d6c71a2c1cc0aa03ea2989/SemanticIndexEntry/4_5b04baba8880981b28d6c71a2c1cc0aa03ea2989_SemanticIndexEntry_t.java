 package org.spoofax.interpreter.library.language;
 
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.jsglr.client.imploder.ImploderAttachment;
 
 /**
  * A linked list representation of one or more index entries.
  * 
  * @see #getNext()
  * 
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class SemanticIndexEntry {
 	
 	private IStrategoConstructor constructor;
 
 	private IStrategoTerm namespace;
 	
 	private IStrategoList id;
 
 	private IStrategoTerm contentsType;
 
 	private IStrategoTerm contents;
 	
 	private SemanticIndexFile file;
 	
 	private SemanticIndexEntry next = null;
 	
 	private transient IStrategoAppl term;
 
 	/**
 	 * @param namespace The namespace of the entry, e.g., 'Foo()'
 	 * @param id        The identifier of the entry, e.g., '["foo", Foo()]'
 	 */
 	protected SemanticIndexEntry(IStrategoConstructor constructor, IStrategoTerm namespace,
 			IStrategoList id, IStrategoTerm contentsType, IStrategoTerm contents, SemanticIndexFile file) {
 		this.constructor = constructor;
 		this.id = id;
 		this.namespace = namespace;
 		this.contentsType = contentsType;
 		this.contents = contents;
 		this.file = file;
 		assert constructor != null && id != null && namespace != null;
 		assert contents != null || constructor.getArity() < 2 : "Contents can't be null for Use/2 or DefData/3";
 		assert contentsType == null || "DefData".equals(constructor.getName()) : "Contents type only expected for DefData";
 		assert constructor != SemanticIndexEntryParent.CONSTRUCTOR || this instanceof SemanticIndexEntryParent;
 	}
 	
 	public IStrategoConstructor getConstructor() {
 		return constructor;
 	}
 	
 	public IStrategoTerm getType() {
 		return contentsType;
 	}
 	
 	public IStrategoList getId() {
 		return id;
 	}
 	
 	public IStrategoTerm getNamespace() {
 		return namespace;
 	}
 	
 	public IStrategoTerm getContents() {
 		return contents;
 	}
 	
 	public SemanticIndexFile getFile() {
 		return file;
 	}
 	
 	/**
 	 * Gets the next entry in this list of entries, or null if there is none.
 	 */
 	public SemanticIndexEntry getNext() {
 		return next;
 	}
 	
 	public final SemanticIndexEntry getLast() {
 		SemanticIndexEntry result = this;
 		while (result.getNext() != null)
 			result = result.getNext();
 		return result;
 	}
 	
 	public boolean isParent() {
 		return constructor == SemanticIndexEntryParent.CONSTRUCTOR;
 	}
 	
 	public void setNext(SemanticIndexEntry next) {
 		this.next = next;
 	}
 	
 	boolean isReferenceInTail(SemanticIndexEntry entry) {
		for (SemanticIndexEntry tail = this; tail != null; tail = tail.getNext()) {
 			if (entry == tail) return true;
 		}
 		return false;
 	}
 	/**
 	 * Reinitialize this template. Used for maintaining a reusable lookup object
 	 * in the index.
 	 */
 	protected void internalReinit(IStrategoConstructor constructor, IStrategoTerm namespace, IStrategoList id, IStrategoTerm contentsType) {
 		this.constructor = constructor;
 		this.contentsType = contentsType;
 		this.namespace = namespace;
 		this.id = id;
 	}
 	
 	/**
 	 * Returns a term representation of this entry,
 	 * ignoring its tail.
 	 * (Null for {@link SemanticIndexEntryParent} terms.) 
 	 * 
 	 * @return null if this entry has no term representation
 	 *         (as in the case of a {@link SemanticIndexEntryParent}).
 	 */
 	public IStrategoAppl toTerm(ITermFactory factory) {
 		if (term != null)
 			return term;
 		
 		IStrategoList namespaceId = factory.makeListCons(namespace, id);
 		if (constructor.getArity() == 3) {
 			term = factory.makeAppl(constructor, namespaceId, contentsType, contents);
 		} else if (constructor.getArity() == 2) {
 			term = factory.makeAppl(constructor, namespaceId, contents);
 		} else {
 			term = factory.makeAppl(constructor, namespaceId);
 		}
 		return forceImploderAttachment(term);
 	}
 
 	/**
 	 * Returns a term representation of this entry and its tail as a list.
 	 * (Null for {@link SemanticIndexEntryParent} terms.)
 	 */
 	public final IStrategoList toTerms(ITermFactory factory) {
 		IStrategoList results = factory.makeList();
 		return toTerms(factory, results, true);
 	}
 
 	protected IStrategoList toTerms(ITermFactory factory, IStrategoList results, boolean lookAtNext) {
 		for (SemanticIndexEntry entry = this; entry != null; entry = entry.getNext()) {
 			IStrategoAppl result = entry.toTerm(factory);
 			if (result != null)
 				results = factory.makeListCons(result, results);
 			
 			if(!lookAtNext)
 				break;
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * Returns a term representation of given entries and their tails as a list.
 	 * (Null for {@link SemanticIndexEntryParent} terms.)
 	 */
 	public static IStrategoList toTerms(ITermFactory factory, Iterable<SemanticIndexEntry> entries, boolean lookAtNext) {
 		IStrategoList results = factory.makeList();
 		for (SemanticIndexEntry entry : entries) {
 			results = entry.toTerms(factory, results, lookAtNext);
 		}
 		return results;
 	}
 
 	/**
 	 * Force an imploder attachment for a term.
 	 * This ensures that there is always some form of position info,
 	 * and makes sure that origin info is not added to the term.
 	 * (The latter would be bad since we cache in {@link #term}.)
 	 */
 	private IStrategoAppl forceImploderAttachment(IStrategoAppl term) {
 		ImploderAttachment attach = ImploderAttachment.get(id);
 		if (attach != null) {
 			ImploderAttachment.putImploderAttachment(term, false, attach.getSort(), attach.getLeftToken(), attach.getRightToken());
 		} else {
 			String fn = file == null ? null : file.getURI().getPath();
 			attach = ImploderAttachment.createCompactPositionAttachment(fn, 0, 0, 0, -1);
 			term.putAttachment(attach);
 		}
 		return term;
 	}
 	
 	@Override
 	public String toString() {
 		String result = constructor.getName() + "([" + namespace + "|" + id + "]";
 		if (contentsType != null) result += "," + contentsType; 
 		if (contents != null) result += "," + contents; 
 		return result + ")";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id.hashCode();
 		result = prime * result + (contentsType == null ? 0 : contentsType.hashCode());
 		/* Not considered: data is not part of the key, makes it impossible to look up!
 		result = prime * result + (contents == null ? 0 : contents.hashCode());
 		*/
 		result = prime * result + namespace.hashCode();
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof SemanticIndexEntry))
 			return false;
 		SemanticIndexEntry other = (SemanticIndexEntry) obj;
 		if (constructor != other.constructor && !constructor.match(other.constructor))
 			return false;
 		if (namespace != other.namespace && !namespace.match(other.namespace))
 			return false;
 		if (contentsType != other.contentsType && contentsType != null && !contentsType.match(other.contentsType))
 			return false;
 		if (id != other.id && !id.match(other.id))
 			return false;
 		/* Not considered: data is not part of the key, makes it impossible to look up!
 		   (same for file)
 		if (contents != other.contents && contents != null && !contents.match(other.contents))
 			return false;
 		*/
 		return true;
 	}
 }
