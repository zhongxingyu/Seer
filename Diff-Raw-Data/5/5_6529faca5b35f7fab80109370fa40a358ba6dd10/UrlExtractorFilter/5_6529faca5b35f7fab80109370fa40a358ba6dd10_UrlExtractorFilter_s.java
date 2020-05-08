 
 package org.paxle.data.db.impl;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.LinkInfo;
 import org.paxle.core.doc.LinkInfo.Status;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterContext;
 import org.paxle.core.queue.ICommand;
 
 public class UrlExtractorFilter implements IFilter<ICommand> {
 	
 	private static class Counter {
 		public int total = 0;
 		public int enqueued = 0;
 	}
 	
 	/**
 	 * A database to store known {@link ICommand commands}
 	 */
 	private final CommandDB db;
 	
 	/**
 	 * For logging
 	 */
 	private final Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * A queue to buffer all {@link URI} that were recently extracted
 	 * from an {@link IParserDocument} and are enqueued for insertion
 	 * into the {@link #db command-db} 
 	 */
 	private final BlockingQueue<URIQueueEntry> extractedUriQueue;
 
 	/**
 	 * A {@link Thread} used to listen for newly {@link #extractedUriQueue extracted-URI}
 	 * that should be stored into the {@link #db command-db}
 	 */
 	private final URIStorageThread storageThread;
 	
 	public UrlExtractorFilter(CommandDB db) {
 		this.db = db;
 		
 		// create the URI queue 
 		this.extractedUriQueue = new LinkedBlockingQueue<URIQueueEntry>();
 		
 		// create and start the worker thread
 		this.storageThread = new URIStorageThread();
 		this.storageThread.start();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void filter(ICommand command, IFilterContext context) {
 		if (command == null) return;
 
 		// getting the parser-doc
 		IParserDocument parserDoc = command.getParserDocument();
 		if (parserDoc == null) return;
 
 		// getting the link map
 		final Counter c = new Counter();
 		this.extractLinks(command, parserDoc, c);
 		logger.info(String.format(
 				"Selected %d URI out of %d URI from '%s' for storage to DB.",
 				Integer.valueOf(c.enqueued), 
 				Integer.valueOf(c.total), 
 				command.getLocation()
 		));
 	}
 	
 	private void extractLinks(final ICommand command, IParserDocument parserDoc, final Counter c) {
 		if (parserDoc == null) return;
 		
 		// getting the link map
 		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
 		if (linkMap != null) {
 			this.extractLinks(command, linkMap, c);
 		}
 		
 		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
 		if (subDocs != null) {
 			for (IParserDocument subDoc : subDocs.values()) {
 				this.extractLinks(command, subDoc, c);
 			}
 		}
 	}
 	
 	private void extractLinks(final ICommand command, Map<URI, LinkInfo> linkMap, final Counter c) {
 		if (linkMap == null) return;
 		
 		final ArrayList<URI> refs = new ArrayList<URI>();		
 		for (Entry<URI, LinkInfo> link : linkMap.entrySet()) {
 			URI ref = link.getKey();
 			LinkInfo meta = link.getValue();
 			c.total++;
 			
 			// check if the URI exceeds max length
 			if (ref.toString().length() > 512) {
 				this.logger.debug("Skipping too long URL: " + ref);
 				continue;
 			} else if (!meta.hasStatus(Status.OK)) {
 				this.logger.debug(String.format(
 						"Skipping URL because of status '%s' (%s): %s",
 						meta.getStatus(),
 						meta.getStatusText(),
 						ref
 				));
 				continue;
 			}
 			
 			c.enqueued++;
 			refs.add(ref);
 		}
 				
 		if (refs.size() > 0) {
 			// add command into URI queue
 			this.extractedUriQueue.add(new URIQueueEntry(
 					command.getLocation(),
 					command.getProfileOID(),
 					command.getDepth() + 1,
 					refs
 			));
 		}
 	}
 	
 	public void terminate() throws InterruptedException {
 		if (this.storageThread != null) {
 			// interrupt thread
 			this.storageThread.interrupt();
 			
 			// wait for shutdown
 			this.storageThread.join(1000);
 		}
 		
 		// clear URI queue
 		this.extractedUriQueue.clear();
 	}
 	
 	private class URIQueueEntry {
 		private final int profileID;
 		private final int commandDepth;
 		private final URI rootUri;
 		private final ArrayList<URI> refs;
 
 		public URIQueueEntry(URI rootUri, int profileID, int commandDepth, ArrayList<URI> refs) {
 			this.rootUri = rootUri;
 			this.profileID = profileID;
 			this.commandDepth = commandDepth;
 			this.refs = refs;
 		}
 
 		public int getProfileID() {
 			return this.profileID;
 		}
 
 		public int getDepth() {
 			return this.commandDepth;
 		}
 
 		public URI getRootURI() {
 			return this.rootUri;
 		}
 
 		public ArrayList<URI> getReferences() {
 			return this.refs;
 		}
 	}
 
 	private class URIStorageThread extends Thread {
 		public URIStorageThread() {
 			this.setName(this.getClass().getSimpleName());
 		}
 		
 		@Override
 		public void run() {
 			
 			while(!db.isClosed() && !this.isInterrupted()) {
 				try {
 					// waiting for the next job
 					URIQueueEntry entry = extractedUriQueue.take();
 
 					// store unknown URI
 					if (!db.isClosed()) {
 						int known = db.storeUnknownLocations(
 								entry.getProfileID(),
 								entry.getDepth(),
 								entry.getReferences()
 						);
 						
 						logger.info(String.format(
 								"Extracted %d new and %d already known URIs from '%s'",
								Integer.valueOf(entry.getReferences().size() - known), 
 								Integer.valueOf(known), 
 								entry.getRootURI().toASCIIString()
 						));
 					} else {
 						logger.error(String.format(
 								"Unable to write linkmap of location '%s' to db. Database already closed.",
 								entry.getRootURI().toASCIIString()
 						));
 					}					
 					
 				} catch (InterruptedException e) {
 					logger.info(String.format(
 							"Shutdown of' %s' finished. '%d' could not be stored.",
 							this.getName(),
 							Integer.valueOf(extractedUriQueue.size())
 					));
 					return;
 				} catch (Throwable e) {
 					logger.error(String.format(
 							"Unexpected '%s' while trying to store new URI into the command-db.",
 							e.getClass().getName()
 					), e);
 				}
 			}
 		}
 	}
 }
