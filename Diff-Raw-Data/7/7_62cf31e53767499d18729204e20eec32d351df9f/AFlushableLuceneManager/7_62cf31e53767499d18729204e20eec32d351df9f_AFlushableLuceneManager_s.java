 
 package org.paxle.se.index.lucene.impl;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.ParseException;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermDocs;
 import org.apache.lucene.index.TermEnum;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 
 import org.paxle.core.doc.Field;
 import org.paxle.core.doc.IIndexerDocument;
 import org.paxle.se.index.IIndexIteratable;
 
 public class AFlushableLuceneManager implements IIndexIteratable {
 	
 	public final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock(true);
 	public final ReentrantReadWriteLock.ReadLock rlock = this.rwlock.readLock();
 	public final ReentrantReadWriteLock.WriteLock wlock = this.rwlock.writeLock();
 	
 	protected final String path;
 	protected final IndexWriter writer;
 	protected final Log logger = LogFactory.getLog(AFlushableLuceneManager.class);
 	protected final PaxleAnalyzer analyzer;
 	protected IndexReader reader;
 	
 	private boolean dirty = false;
 	
 	public AFlushableLuceneManager(final String path, final PaxleAnalyzer analyzer) throws IOException {
 		this.path = path;
 		this.analyzer = analyzer;
 		this.writer = new IndexWriter(path, analyzer);
 		this.writer.setMaxFieldLength(Integer.MAX_VALUE);
 		this.reader = IndexReader.open(path);
 	}
 	
 	/**
 	 * This function 
 	 * <ul>
 	 * 	<li>closes an old {@link IndexReader index-reader}</li>
 	 * 	<li>causes the {@link IndexWriter index-writer} to flush recently added data</li>
 	 * 	<li>creates a new {@link IndexReader index-reader}</li>
 	 * </ul>
 	 * 
 	 * Re-opening of the index-reader is required because the index-reader 
 	 * <i>"only searches the index as of the "point in time" that it was opened.
 	 * Any updates to the index, either added or deleted documents, will not 
 	 * be visible until the IndexReader is re-opened."</i><br />
 	 * See: 
 	 * <a href="http://wiki.apache.org/lucene-java/LuceneFAQ#head-6c56b0449d114826586940dcc6fe51582676a36e">
 	 * 	Lucene FAQ - Does Lucene allow searching and indexing simultaneously?
 	 * </a>
 	 * 
 	 * @throws IOException
 	 */
 	private void flush() throws IOException {
 		this.logger.debug("Flushing index writer and reopening index reader");
 		this.writer.flush();
 		IndexReader newReader = this.reader.reopen();
 		if (newReader != reader) {
 			reader.close(); 
 		}
 		this.reader = newReader;
 	}
 	
 	protected void checkFlush() throws IOException {
 		this.rlock.lock();
 		try {
 			if (this.dirty) {
 				this.rlock.unlock();
 				this.wlock.lock();
 				try {
 					if (this.dirty) {
 						flush();
 						this.dirty = false;
 					}
 				} finally {
 					this.rlock.lock();
 					this.wlock.unlock();
 				}
 			}
 		} finally {
 			this.rlock.unlock();
 		}
 	}
 	
 	protected boolean isDirty() {
 		return this.dirty;
 	}
 	
 	protected void setDirty() {
 		this.dirty = true;
 	}
 	
 	public void close() throws IOException {
 		this.wlock.lock();
 		try {
 			this.writer.close();
 			this.reader.close();
 		} finally { this.wlock.unlock(); }
 	}
 	
 	public Document getDocument(int doc) throws IOException {
 		this.rlock.lock();
 		try {
 			return this.reader.document(doc);
 		} finally { this.rlock.unlock(); } 
 	}
 	
 	public int getDocCount() throws IOException {
 		this.rlock.lock();
 		try {
 			return writer.docCount();
 		} finally { this.rlock.unlock(); }
 	}
 	
 	public void search(Query query, AHitCollector collector) throws IOException {
 		checkFlush();
 		this.rlock.lock();
 		final IndexSearcher searcher = new IndexSearcher(this.reader);
 		collector.init(searcher);
 		try {
 			searcher.search(query, collector);
 		} finally {
 			collector.reset();
 			this.rlock.unlock();
 		}
 	}
 	
 	public void write(final Document document, final Analyzer analyzer) throws IOException, CorruptIndexException {
 		wlock.lock();
 		try {
			//writer.addDocument(document, analyzer);
			
 			String location = document.getField(IIndexerDocument.LOCATION.getName()).stringValue();
 			Term term = new Term(IIndexerDocument.LOCATION.getName(), location);
 			System.out.println(IIndexerDocument.LOCATION.getName());
 			System.out.println(document.getField(IIndexerDocument.LOCATION.getName()).stringValue());
 			//writer.updateDocument(term, document, analyzer);
			TermDocs tmp = reader.termDocs();
			tmp.skipTo(3);
			System.out.println("test"+tmp.doc());
 			while(tmp.next()) {
 				System.out.println("test"+tmp.doc());
 			}
 			int deleteCount = reader.deleteDocuments(term);
 			System.out.println(deleteCount);
 			writer.addDocument(document, analyzer);
 			dirty = true;
 		} finally { wlock.unlock(); }
 	}
 	
 	public void write(Document document) throws IOException, CorruptIndexException {
 		write(document, analyzer);
 	}
 	
 	public void delete(Term term) throws IOException, CorruptIndexException {
 //        this.wlock.lock();
 //        try {
             this.writer.deleteDocuments(term);
             this.dirty = true;
 //        } finally { this.wlock.unlock(); }
     }
     
 	/* ================================================================================
 	 * Iterators
 	 * ================================================================================ */
 	
 	/** @deprecated does not work correctly */
 	public synchronized <E extends Serializable> Iterator<E> iterator(Field<E> field) throws IOException {
 		return new FieldIterator<E>(new DocumentIterator(), field);
 	}
 	
 	/** @deprecated does not work correctly */
 	public synchronized <E extends Serializable> Iterator<E> iterator(Field<E> field, String contains) throws IOException {
 		return new FieldIterator<E>(new DocumentIterator(contains), field);
 	}
 	
 	/** @deprecated does not work correctly */
 	public synchronized Iterator<IIndexerDocument> docIterator() throws IOException {
 		return new DocumentIterator();
 	}
 	
 	/** @deprecated does not work correctly */
 	public synchronized Iterator<IIndexerDocument> docIterator(String contains) throws IOException {
 		return new DocumentIterator(contains);
 	}
 	
 	public synchronized Iterator<String> wordIterator() throws IOException {
 		return new WordIterator(this.reader.terms());
 	}
 	
 	/** @deprecated does not work correctly */
 	public synchronized Iterator<String> wordIterator(String start) throws IOException {
 		return new WordIterator(this.reader.terms(new Term(null, start)));
 	}
 	
 	public synchronized Iterator<String> wordIterator(Field<? extends Serializable> field) throws IOException {
 		return new FieldLimitedWordIterator(this.reader.terms(new Term(field.getName(), "")), field);
 	}
 	
 	public synchronized Iterator<String> wordIterator(String start, Field<? extends Serializable> field) throws IOException {
 		return new FieldLimitedWordIterator(this.reader.terms(new Term(field.getName(), start)), field);
 	}
 	
 	private class DocumentIterator implements Iterator<IIndexerDocument> {
 		
 		private final TermDocs tenum;
 		private int current = -1;
 		private int next;
 		
 		public DocumentIterator() throws IOException {
 			this(null);
 		}
 		
 		public DocumentIterator(String word) throws IOException {
 			this.tenum = (word == null) ? reader.termDocs() : reader.termDocs(new Term("*", word));
 			next0();
 		}
 		
 		private int next0() {
 			try {
 				final int lnext = this.next;
 				
 					this.next = (this.tenum.next()) ? this.tenum.doc() : -1;
 				
 				return lnext;
 			} catch (IOException e) {
 				throw new RuntimeException("I/O error iterating through terms", e);
 			}
 		}
 		
 		public boolean hasNext() {
 			return this.next >= 0;
 		}
 		
 		public IIndexerDocument next() {
 			this.current = this.next;
 			if (this.current < 0) {
 				throw new NoSuchElementException();
 			} else try {
 				this.next = next0();
 				final Document doc;
 				
 					doc = AFlushableLuceneManager.this.reader.document(this.current);
 				
 				return Converter.luceneDoc2IIndexerDoc(doc);
 			} catch (IOException e) {
 				throw new RuntimeException("I/O error iterating through documents", e);
 			} catch (ParseException e) {
 				throw new RuntimeException("Converter error iterating through documents", e);
 			}
 		}
 		
 		public void remove() {
 			try {
 				
 					AFlushableLuceneManager.this.reader.deleteDocument(this.current);
 				
 			} catch (IOException e) {
 				throw new RuntimeException("I/O error removing document", e);
 			}
 		}
 	}
 	
 	private static class FieldIterator<E extends Serializable> implements Iterator<E> {
 		
 		private final Field<E> field;
 		private final DocumentIterator it;
 		
 		public FieldIterator(DocumentIterator it, Field<E> field) {
 			this.field = field;
 			this.it = it;
 		}
 		
 		public boolean hasNext() {
 			return this.it.hasNext();
 		}
 		
 		public E next() {
 			return this.it.next().get(this.field);
 		}
 		
 		public void remove() {
 			// It is doable, but I'd have to rebuild the whole index-lucene bundle for it
 			// to get an reference to a currently active writer
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	private class WordIterator implements Iterator<String> {
 		
 		protected final TermEnum tenum;
 		protected Term current;
 		protected Term next;
 		
 		public WordIterator(TermEnum tenum) {
 			this.tenum = tenum;
 			next0();
 		}
 		
 		protected Term next0() {
 			try {
 				final Term lnext = this.next;
 				
 					this.next = (this.tenum.next()) ? this.tenum.term() : null;
 				
 				return lnext;
 			} catch (IOException e) {
 				throw new RuntimeException("I/O error iterating through terms", e);
 			}
 		}
 		
 		public boolean hasNext() {
 			return this.next != null;
 		}
 		
 		public String next() {
 			this.current = this.next;
 			if (this.current == null) {
 				throw new NoSuchElementException();
 			} else {
 				String text = this.current.text();
 				this.current = next0();
 				return text;
 			}
 		}
 		
 		public void remove() {
 			// XXX: is urgently needed, but I don't know how to do currently
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	private class FieldLimitedWordIterator extends WordIterator implements Iterator<String> {
 		
 		protected Field<?> field = null;
 		
 		public FieldLimitedWordIterator(TermEnum tenum, Field<?> field) {
 			super(tenum);
 			this.field = field;
 		}
 		
 		@Override
 		protected Term next0() {
 			Term ret;
 			do {
 				ret = super.next0();
 			} while (this.field != null && ret != null && !this.field.getName().equals(ret.field()));
 			return ret;
 		}
 	}
 }
