 /*
  * Dec 2, 2005
  */
 package com.thinkparity.model.session;
 
 import java.io.StringReader;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.io.SAXReader;
 import org.xmpp.packet.IQ;
 import org.xmpp.packet.JID;
 
 import com.thinkparity.codebase.jabber.JabberIdBuilder;
 
 import com.thinkparity.model.AbstractModelImpl;
 import com.thinkparity.model.ParityServerModelException;
 import com.thinkparity.model.queue.QueueItem;
 
 import com.thinkparity.server.org.jivesoftware.messenger.JIDBuilder;
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 class SessionModelImpl extends AbstractModelImpl {
 
 	/**
 	 * A sax xml reader.
 	 * 
 	 */
 	private final SAXReader xmlReader;
 
 	/**
 	 * Synchronization lock for the sax xml reader.
 	 * 
 	 */
 	private final Object xmlReaderLock;
 
 	/**
 	 * Create a SessionModelImpl.
 	 * 
 	 * @param session
 	 *            The user session.
 	 */
 	SessionModelImpl(final Session session) {
 		super(session);
 		this.xmlReader = new SAXReader();
 		this.xmlReaderLock = new Object();
 	}
 
 	/**
 	 * Send all queued messages for the user.
 	 * 
 	 * @throws ParityServerModelException
 	 */
 	void send(final QueueItem queueItem) {
         logApiId();
 		logger.debug(queueItem);
 		final JID jid = JIDBuilder.build(queueItem.getUsername());
 		try {
 			final Document queueItemDocument =
 				readXml(queueItem.getQueueMessage());
 			final IQ iq = new IQ(queueItemDocument.getRootElement());
 			iq.setTo(jid);
			send(JabberIdBuilder.parseQualifiedJabberId(jid.toBareJID()), iq);
 		} catch (final Throwable t) {
             throw translateError(t);
 		}
 	}
 
 	/**
 	 * Parse the xml into a document.
 	 * 
 	 * @param xml
 	 *            Xml.
 	 * @return An xml document.
 	 * @throws DocumentException
 	 */
 	private Document readXml(final String xml) throws DocumentException {
 		synchronized(xmlReaderLock) {
 			return xmlReader.read(new StringReader(xml));
 		}
 	}
 }
