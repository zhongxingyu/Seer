 /*******************************************************************************
  * Copyright (c) 2006-2007, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.aggregator.engine.maven.indexer;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermEnum;
 import org.codehaus.plexus.PlexusContainer;
 import org.eclipse.b3.aggregator.engine.maven.MavenActivator;
 import org.eclipse.b3.aggregator.engine.maven.VersionUtil;
 import org.eclipse.b3.aggregator.engine.maven.loader.VersionEntry;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.sonatype.nexus.index.ArtifactInfo;
 import org.sonatype.nexus.index.NexusIndexer;
 import org.sonatype.nexus.index.context.IndexCreator;
 import org.sonatype.nexus.index.context.IndexingContext;
 import org.sonatype.nexus.index.packer.IndexPacker;
 import org.sonatype.nexus.index.packer.IndexPackingRequest;
 import org.sonatype.nexus.index.updater.IndexUpdateRequest;
 import org.sonatype.nexus.index.updater.IndexUpdater;
 
 /**
  * @author Filip Hrbek (filip.hrbek@cloudsmith.com)
  * 
  */
 public class MavenNexusIndexer implements IMaven2Indexer {
 	private class VersionEntryIterator implements Iterator<VersionEntry> {
 		private IndexReader indexReader;
 
 		private TermEnum termEnum;
 
 		private VersionEntry nextEntry;
 
 		public VersionEntryIterator(IndexReader indexReader) throws IOException {
 			this.indexReader = indexReader;
 			termEnum = indexReader.terms(new Term(ArtifactInfo.UINFO, ""));
 			nextEntry = getNextEntry(false);
 		}
 
 		public void close() throws IOException {
 			synchronized(MavenNexusIndexer.this) {
 				openIterators.remove(this);
 				if(termEnum != null)
 					termEnum.close();
 				if(indexReader != null)
 					indexReader.close();
 			}
 		}
 
 		public boolean hasNext() {
 			boolean hasNext = nextEntry != null;
 			if(!hasNext)
 				try {
 					close();
 				}
 				catch(IOException e) {
 					throw new RuntimeException("Unable to close index reader");
 				}
 
 			return hasNext;
 		}
 
 		public VersionEntry next() {
 			if(nextEntry != null) {
 				VersionEntry entry = nextEntry;
 				try {
 					nextEntry = getNextEntry(true);
 				}
 				catch(IOException e) {
 					throw new RuntimeException(e);
 				}
 				return entry;
 			}
 
 			throw new NoSuchElementException();
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		private VersionEntry getNextEntry(boolean moveToNext) throws IOException {
 			if(moveToNext && !termEnum.next())
 				return null;
 
 			while(termEnum.term() != null && ArtifactInfo.UINFO.equals(termEnum.term().field())) {
 				String record = termEnum.term().toString();
 				String[] tokens = record.split("[:|]");
 
 				// we look for something like "u:groupId|artifactId|version|NA"
 				if(tokens.length == 5) {
 					try {
 						return new VersionEntry(tokens[1], tokens[2], VersionUtil.createVersion(tokens[3]));
 					}
 					catch(CoreException e) {
 						IOException ioe = new IOException(e.getMessage());
 						ioe.initCause(e);
 						throw ioe;
 					}
 				}
 
 				if(!termEnum.next())
 					break;
 			}
 
 			return null;
 		}
 	}
 
 	private IndexingContext context;
 
 	private Set<VersionEntryIterator> openIterators;
 
 	public MavenNexusIndexer() {
 		openIterators = new HashSet<VersionEntryIterator>();
 	}
 
 	synchronized public void closeRemoteIndex() throws CoreException {
 		try {
 			for(VersionEntryIterator itor : openIterators)
 				itor.close();
 
 			if(context != null) {
 				context.close(false);
 				context = null;
 			}
 		}
 		catch(IOException e) {
 			throw ExceptionUtils.fromMessage(e, e.getMessage());
 		}
 	}
 
 	synchronized public Iterator<VersionEntry> getArtifacts() throws CoreException {
 		try {
 			IndexReader indexReader = IndexReader.open(context.getIndexDirectory());
 
 			VersionEntryIterator itor = new VersionEntryIterator(indexReader);
 			openIterators.add(itor);
 			return itor;
 		}
 		catch(IOException e) {
 			throw ExceptionUtils.fromMessage(e, e.getMessage());
 		}
 	}
 
 	synchronized public int getNumberOfEntries() throws CoreException {
 		// not very efficient but quite fast, may be improved later
 		Iterator<VersionEntry> artifacts = getArtifacts();
 		int counter = 0;
 		while(artifacts.hasNext()) {
 			artifacts.next();
 			counter++;
 		}
 
 		return counter;
 	}
 
 	public void openRemoteIndex(URI location, boolean clearLocalCache) throws IndexNotFoundException, CoreException {
 		closeRemoteIndex();
 		openIterators.clear();
 
 		File cacheDirectory = null;
 		try {
 			cacheDirectory = MavenActivator.getPlugin().getCacheDirectory(location);
 		}
 		catch(MalformedURLException e) {
 			throw ExceptionUtils.wrap(e);
 		}
 		File indexDirectory = new File(cacheDirectory, "index");
 		try {
 			PlexusContainer plexus = Activator.getPlugin().getPlexusContainer();
 
 			NexusIndexer indexer = plexus.lookup(NexusIndexer.class);
 			IndexUpdater updater = plexus.lookup(IndexUpdater.class);
 			IndexCreator creator = plexus.lookup(IndexCreator.class, "min");
 
 			String repoId = "mavenRepo";
 			if(!clearLocalCache)
 				context = indexer.addIndexingContext(repoId, // index id (usually the same as repository
 						// id)
 						repoId, // repository id
 						(File) null, // null for remote repo
 						indexDirectory, // Lucene directory where index is stored
 						location.toString(), // repository url, used by index updater
 						null, // null if derived from repositoryUrl
 						Collections.singletonList(creator));
 			else
 				context = indexer.addIndexingContextForced(repoId, // index id (usually the same as repository
 						// id)
 						repoId, // repository id
 						(File) null, // null for remote repo
 						indexDirectory, // Lucene directory where index is stored
 						location.toString(), // repository url, used by index updater
 						null, // null if derived from repositoryUrl
 						Collections.singletonList(creator));
 
 			IndexUpdateRequest request = new IndexUpdateRequest(context);
 			updater.fetchAndUpdateIndex(request);
 		}
 		catch(Exception e) {
 			throw new IndexNotFoundException(e);
 		}
 	}
 
 	public void updateLocalIndex(URI location, boolean createNew) throws CoreException {
 		try {
 			PlexusContainer plexus = Activator.getPlugin().getPlexusContainer();
 
 			NexusIndexer indexer = plexus.lookup(NexusIndexer.class);
 			IndexPacker packer = plexus.lookup(IndexPacker.class);
 			String repoId = "mavenRepo";
 			File repository = new File(location);
 			File index = new File(repository, ".index");
 			File internalIndex = new File(new File(repository.getParentFile().getParentFile(), "interim"),
 					"maven-index");
 			List<IndexCreator> creators = new ArrayList<IndexCreator>(2);
 			creators.add(plexus.lookup(IndexCreator.class, "min"));
 			creators.add(plexus.lookup(IndexCreator.class, "jarContent"));
 			IndexingContext context = indexer.addIndexingContext(repoId, repoId, repository, internalIndex, null, null,
 					creators);
 			try {
 				indexer.scan(context, !createNew);
 				IndexPackingRequest request = new IndexPackingRequest(context, index);
 				List<IndexPackingRequest.IndexFormat> formats = new ArrayList<IndexPackingRequest.IndexFormat>(2);
 				formats.add(IndexPackingRequest.IndexFormat.FORMAT_V1);
 				formats.add(IndexPackingRequest.IndexFormat.FORMAT_LEGACY);
 				request.setFormats(formats);
 				request.setCreateChecksumFiles(true);
 				request.setCreateIncrementalChunks(!createNew);
 				packer.packIndex(request);
 			}
 			finally {
 				if(context != null)
 					context.close(false);
 			}
 		}
 		catch(Exception e) {
 			throw ExceptionUtils.fromMessage(e, "Unable to create an index for %s", location.toString());
 		}
 	}
 }
