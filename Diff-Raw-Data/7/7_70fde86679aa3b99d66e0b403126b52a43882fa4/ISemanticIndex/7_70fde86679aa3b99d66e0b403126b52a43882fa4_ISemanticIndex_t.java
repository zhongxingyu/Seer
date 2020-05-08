 package org.spoofax.interpreter.library.language;
 
 import java.util.Collection;
 
 import org.spoofax.interpreter.library.IOAgent;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 
 /**
  * @author Gabriel Konat
  */
 public interface ISemanticIndex {
 
 	/**
 	 * Initializes this semantic index.
 	 * 
 	 * @param revisionProvider  An atomic revision number provider, should be shared between copies
 	 *                          of the same index.
 	 */
 	public abstract void initialize(ITermFactory factory, IOAgent agent);
 
 	/**
 	 * Gets the entry factory used by this semantic index.
 	 */
 	public abstract SemanticIndexEntryFactory getFactory();
 
 	/**
 	 * Adds a new entry to the index.
 	 * 
 	 * @param entry				The entry to add.
 	 * @param fileDescriptor  	The file to associate the entry with.
 	 */
 	public abstract void add(IStrategoAppl entry, SemanticIndexFileDescriptor fileDescriptor);
 	
 	/**
 	 * Adds a new entry to the index.
 	 * 
 	 * @param entry	The entry to add.
 	 */
 	public abstract void add(SemanticIndexEntry entry);
 
 	/**
 	 * Adds a list of entries to the index.
 	 * 
 	 * @param entries			The entries to add.
 	 * @param fileDescriptor	The file to associate the entries with.
 	 */
 	public abstract void addAll(IStrategoList entries, SemanticIndexFileDescriptor fileDescriptor);
 	
 	/**
 	 * Removes all entries that match given template and are from given file.
 	 * 
 	 * @param template			The template to match entries against.
 	 * @param fileDescriptor	The file entries must be from.
 	 */
 	public abstract void remove(IStrategoAppl template, SemanticIndexFileDescriptor fileDescriptor);
 	
 	/**
 	 * Gets all entries that match given template.
 	 * 
 	 * @param template	The template to match entries against.
 	 */
 	public abstract Collection<SemanticIndexEntry> getEntries(IStrategoAppl template);
 	
 	/**
 	 * Gets all entries.
 	 */
 	public abstract Collection<SemanticIndexEntry> getAllEntries();
 
 	/**
 	 * Gets all child entries for URI in given template.
 	 * 
 	 * @param template	The template to match entries against.
 	 */
 	public abstract Collection<SemanticIndexEntry> getEntryChildTerms(IStrategoAppl template);
 	
 	/**
 	 * Gets all entries for given file descriptor.
 	 * 
 	 * @param fileDescriptor	The file descriptor to match entries against.
 	 */
 	public abstract Collection<SemanticIndexEntry> getEntriesInFile(SemanticIndexFileDescriptor fileDescriptor);
 
 	/**
 	 * Gets a semantic index file for given file descriptor.
 	 * 
 	 * @param fileDescriptor	A file descriptor.
 	 */
 	public abstract SemanticIndexFile getFile(SemanticIndexFileDescriptor fileDescriptor);
 	
 	/**
 	 * Gets a semantic index file descriptor for given file term.
 	 * 
 	 * @param fileTerm	A string or (string, string) tuple with the filename or the filename and subfilename.
 	 */
 	public abstract SemanticIndexFileDescriptor getFileDescriptor(IStrategoTerm fileTerm);
 	
 	/**
 	 * Removes all entries in given file term and removes the file itself.
 	 * 
 	 * @param fileTerm	A string or (string, string) tuple with the filename or the filename and subfilename.
 	 */
 	public abstract void removeFile(IStrategoTerm fileTerm);
 	
 	/**
 	 * Removes all entries for given file and removes the file itself.
 	 * 
 	 * @param fileDescriptor	A file descriptor.
 	 */
 	public abstract void removeFile(SemanticIndexFileDescriptor fileDescriptor);
 
 	/**
 	 * Gets all files that are in the semantic index.
 	 */
 	public abstract Collection<SemanticIndexFile> getAllFiles();
 	
 	/**
 	 * Gets all file descriptors that are in the semantic index.
 	 */
 	public abstract Collection<SemanticIndexFileDescriptor> getAllFileDescriptors();
 
 	/**
 	 * Clears the entire semantic index.
 	 */
 	public abstract void clear();
 
 	/**
 	 * Returns the semantic index as a stratego term.
 	 * 
 	 * @param includePositions
 	 */
 	public abstract IStrategoTerm toTerm(boolean includePositions);
 
 	public abstract String toString();
 
 }
