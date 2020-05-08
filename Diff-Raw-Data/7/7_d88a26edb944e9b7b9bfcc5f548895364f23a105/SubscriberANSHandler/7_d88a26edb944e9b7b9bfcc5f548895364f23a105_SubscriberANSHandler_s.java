 /*
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  *
  * All other source code is copyright Tresys Technology and licensed as below.
  *
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  *
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.tresys.jalop.jnl.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.xml.soap.MimeHeader;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.beepcore.beep.core.AbortChannelException;
 import org.beepcore.beep.core.BEEPException;
 import org.beepcore.beep.core.InputDataStream;
 import org.beepcore.beep.core.InputDataStreamAdapter;
 import org.beepcore.beep.core.Message;
 import org.beepcore.beep.core.ReplyListener;
 
 import com.tresys.jalop.jnl.IncompleteRecordException;
 import com.tresys.jalop.jnl.RecordInfo;
 import com.tresys.jalop.jnl.RecordType;
 import com.tresys.jalop.jnl.Subscriber;
 import com.tresys.jalop.jnl.exceptions.UnexpectedMimeValueException;
 import com.tresys.jalop.jnl.impl.messages.Utils;
 import com.tresys.jalop.jnl.impl.subscriber.SubscriberSessionImpl;
 
 /**
  * This class reads the ANS reply for a 'journal-resume' or 'subscribe' message
  * and can be used to present the various data segments as
  * {@link java.io.InputStream}s.
  */
 public class SubscriberANSHandler implements ReplyListener {
 
 	private static final String[] KNOWN_HEADERS = { Utils.HDRS_APP_META_LEN.toLowerCase(),
 			Utils.HDRS_SYS_META_LEN.toLowerCase(), Utils.HDRS_LOG_LEN.toLowerCase(), Utils.HDRS_AUDIT_LEN.toLowerCase(), Utils.HDRS_JOURNAL_LEN.toLowerCase(),
 			Utils.HDRS_CONTENT_TYPE.toLowerCase(), Utils.HDRS_MESSAGE.toLowerCase(), Utils.HDRS_SERIAL_ID.toLowerCase() };
 	/**
 	 * The MessageDigest to use for calculating the JALoP digest.
 	 */
 	private final MessageDigest md;
 	/**
 	 * The {@link InputDataStream} for a particular ANS message.
 	 */
 	private InputDataStream ds;
 
 	/**
 	 * A boolean indicating whether the payload size was what was expected. This
 	 * is used so that MessageDigest is able to know whether or not to send an
 	 * IncompleteRecordException
 	 */
 	private boolean payloadCorrect;
 	private boolean payloadComplete;
 
 	private JalopDataStream js;
 	private final SubscriberSessionImpl subsess;
 	static Logger log = Logger.getLogger(SubscriberANSHandler.class);
 
 	/**
 	 * Create a SubscriberANSHandler for a record using the
 	 * {@link InputDataStream}
 	 *
 	 * @param md
 	 *            the {@link MessageDigest} that should be used for digest
 	 *            calculations.
 	 * @param ds
 	 *            the {@link InputDataStream} to read
 	 */
 	public SubscriberANSHandler(final MessageDigest md,
 			final SubscriberSessionImpl subsess) {
 		super();
 		this.md = md;
 		this.subsess = subsess;
 		this.payloadCorrect = true;
 		this.payloadComplete = false;
 	}
 
 	private class JalopDataStream extends InputStream {
 		private boolean finishedReading;
 		InputDataStreamAdapter dsa;
 		private final long dataSize;
 		private long bytesRead;
 
 		public JalopDataStream(final long dataSize) throws IOException,
 				BEEPException, UnexpectedMimeValueException {
 			this.finishedReading = false;
 			this.dsa = ds.getInputStream();
 			this.bytesRead = 0;
 			if (dataSize < 0) {
 				throw new IllegalArgumentException(
 						"dataSize must be 0 or greater");
 			}
 			this.dataSize = dataSize;
 		}
 
 		@Override
 		public int read() throws IOException {
 			if (this.finishedReading == true)
 				return -1;
 
 			int ret = 0;
 			// only read if data is expected
 			if (this.dataSize != 0) {
 				ret = this.dsa.read();
 				if (ret == -1) {
 					throw new IOException();
 				}
 
 				md.update((byte) ret);
 				this.bytesRead++;
 			}
 
 			if (this.bytesRead == this.dataSize) {
 				// we should be done. Try to read the BREAK string
 				final byte b[] = new byte[5];
 				this.dsa.read(b, 0, 5);
 				if (!Arrays.equals("BREAK".getBytes("utf-8"), b)) {
 					payloadCorrect = false;
 					throw new IOException("BREAK string is not where it is expected");
 				} else {
 					payloadCorrect = true;
 				}
 				this.finishedReading = true;
 			}
 			return ret;
 		}
 
 		@Override
 		public int read(final byte[] b) throws IOException {
 			return read(b, 0, b.length);
 		}
 
 		@Override
 		public int read(final byte[] b, final int off, final int len) throws IOException {
 			if (this.finishedReading == true)
 				return -1;
 
 			int ret = 0;
 			int new_len = len;
 			if ((len + this.bytesRead) > this.dataSize) {
 				new_len = (int) (this.dataSize - this.bytesRead);
 			}
 
 			ret = this.dsa.read(b, off, new_len);
 			this.bytesRead+=ret;
 			if (ret != new_len) {
 				throw new IOException(
 						"Could not read data of requested length");
 			}
 			md.update(b, off, new_len);
 			if (this.bytesRead == this.dataSize) {
 				// check for break string
 				final byte brk[] = new byte[5];
 				this.dsa.read(brk, 0, 5);
 				if (!Arrays.equals("BREAK".getBytes("utf-8"), brk)) {
 					payloadCorrect = false;
 					throw new IOException("BREAK string is not where it is expected");
 				} else {
 					payloadCorrect = true;
 				}
 				this.finishedReading = true;
 			}
 			return ret;
 		}
 
 		public void flush() throws IOException {
 			if (!this.finishedReading) {
 				int ret = read();
 				while (ret != -1) {
 					ret = read();
 				}
 			}
 
 			return;
 		}
 	}
 
 	/*
 	 * This function is only used for testing purposes. It returns a {@link
 	 * JalopDataStream}
 	 *
 	 * @param size the size to give the instance of JalopDataStream
 	 */
 	JalopDataStream getJalopDataStreamInstance(final long size)
 			throws UnexpectedMimeValueException, IOException, BEEPException {
 		return new JalopDataStream(size);
 	}
 
 	/**
 	 * Retrieve any additional MIME headers that were sent as part of this ANS
 	 * reply. Recognized MIME headers (i.e. Content-Type, Transfer-Encoding, and
 	 * JALoP headers) are not included in the returned map. If there are no
 	 * additional MIME headers, then this function returns {@link MimeHeaders}
 	 * object.
 	 *
 	 * @return The {@link MimeHeaders}
 	 * @throws BEEPException
 	 */
 	@SuppressWarnings("unchecked")
 	public List<MimeHeader> getAdditionalHeaders() throws BEEPException {
 		final InputDataStreamAdapter dsa = this.ds.getInputStream();
 		final Enumeration<String> headers = dsa.getHeaderNames();
 		final ArrayList<MimeHeader> ret = new ArrayList<MimeHeader>();
 		String hdr = "";
 		while (headers.hasMoreElements()) {
 			hdr = headers.nextElement();
 			if (!Arrays.asList(KNOWN_HEADERS).contains(hdr.toLowerCase()))
 				ret.add(new MimeHeader(hdr, dsa.getHeaderValue(hdr)));
 		}
 		return ret;
 	}
 
 	/**
 	 * Retrieve the calculated digest.
 	 *
 	 * @return The digest of the record according to the JALoP specification.
 	 * @throws IncompleteRecordException
 	 *             is thrown if this method is called before reading all data
 	 *             has not been read from the stream, or if the ANS message from
 	 *             the remote is invalid (i.e. reports 100 bytes of payload, but
 	 *             only contains 50).
 	 */
 	public byte[] getRecordDigest() throws IncompleteRecordException {
 		if (!this.payloadComplete || !this.payloadCorrect)
 			throw new IncompleteRecordException();
 
 		return this.md.digest();
 	}
 
 	@Override
 	public void receiveRPY(final Message message) throws AbortChannelException {
 
 		if (log.isEnabledFor(Level.ERROR)) {
 			log.error("SubscriberANSHandler received RPY, which should not happen");
 		}
 
 		throw new AbortChannelException(
 				"SubscriberANSHandler should not receive RPY");
 
 	}
 
 	@Override
 	public void receiveERR(final Message message) throws AbortChannelException {
 
 		if (log.isEnabledFor(Level.ERROR)) {
 			log.error("SubscriberANSHandler received ERR");
 		}
 		throw new AbortChannelException("SubscriberANSHandler received ERR");
 
 	}
 
 	@Override
 	public void receiveANS(final Message message) throws AbortChannelException {
 		this.ds = message.getDataStream();
 		final InputDataStreamAdapter dsa = this.ds.getInputStream();
 		final long sysMetadataSize;
 		final long appMetadataSize;
 		final long payloadSize;
 		final String payloadType;
 		final RecordType recType;
 		// Get the segment lengths from the header
 		try {
 			sysMetadataSize = new Long(dsa
 					.getHeaderValue(Utils.HDRS_SYS_META_LEN)).longValue();
 			appMetadataSize = new Long(dsa.getHeaderValue(Utils.HDRS_APP_META_LEN)).longValue();
 
 			payloadType = dsa.getHeaderValue(Utils.HDRS_MESSAGE);
 			if (payloadType.equalsIgnoreCase("log-record")) {
 				payloadSize = new Long(dsa.getHeaderValue(Utils.HDRS_LOG_LEN)).longValue();
 				recType = RecordType.Log;
 			} else if (payloadType.equalsIgnoreCase("audit-record")) {
 				payloadSize = new Long(dsa.getHeaderValue(Utils.HDRS_AUDIT_LEN)).longValue();
 				recType = RecordType.Audit;
 			} else if (payloadType.equalsIgnoreCase("journal-record")) {
 				payloadSize = new Long(dsa.getHeaderValue(Utils.HDRS_JOURNAL_LEN)).longValue();
 				recType = RecordType.Journal;
 			} else {
 				throw new AbortChannelException(
 						"Did not receive appropriate headers");
 			}
 		} catch (final BEEPException e) {
 			throw new AbortChannelException(e.getMessage());
 		}
 
 		final Subscriber sub = this.subsess.getSubscriber();
 		try {
 			final RecordInfo recInfo = new RecordInfo() {
 
 				private final long sysMetadataLength = sysMetadataSize;
 				private final long appMetadataLength = appMetadataSize;
 				private final long payloadLength = payloadSize;
 				private final String serialId = dsa.getHeaderValue(Utils.HDRS_SERIAL_ID);
 				private final RecordType recordType = recType;
 
 				@Override
 				public long getSysMetaLength() {
 					return this.sysMetadataLength;
 				}
 
 				@Override
 				public String getSerialId() {
 					return this.serialId;
 				}
 
 				@Override
 				public RecordType getRecordType() {
 					return this.recordType;
 				}
 
 				@Override
 				public long getPayloadLength() {
 					return this.payloadLength;
 				}
 
 				@Override
 				public long getAppMetaLength() {
 					return this.appMetadataLength;
 				}
 			};
 
 			js = new JalopDataStream(sysMetadataSize);
 			if (!sub.notifySysMetadata(subsess, recInfo, js)) {
 				throw new AbortChannelException("Error in notifySysMetadata");
 			}
 			js.flush();
 
 			js = new JalopDataStream(appMetadataSize);
 			if (!sub.notifyAppMetadata(subsess, recInfo, js)) {
 				throw new AbortChannelException("Error in notifyAppMetadata");
 			}
 			js.flush();
 
 			js = new JalopDataStream(payloadSize);
 			if (!sub.notifyPayload(subsess, recInfo, js)) {
 				throw new AbortChannelException("Error in notifyPayload");
 			}
 			js.flush();
 
 			if (ds.getInputStream().read() != -1) {
 				throw new IOException(
 						"Additional data exists when none is expected");
 			}
 			payloadComplete = true;
 			if (!sub.notifyDigest(subsess, recInfo, getRecordDigest())) {
 				throw new AbortChannelException("Error in notifyDigest");
 			}
 		} catch (final BEEPException e) {
 			throw new AbortChannelException(e.getMessage());
 		} catch (final UnexpectedMimeValueException e) {
 			throw new AbortChannelException(e.getMessage());
 		} catch (final IOException e) {
 			throw new AbortChannelException(e.getMessage());
 		} catch (final IncompleteRecordException e) {
 			throw new AbortChannelException(e.getMessage());
 		}
 
 	}
 
 	@Override
 	public void receiveNUL(final Message message) throws AbortChannelException {
 		if (log.isEnabledFor(Level.ERROR)) {
 			log.error("SubscriberANSHandler received NUL");
 		}
 		throw new AbortChannelException(
 				"SubscriberANSHandler should not receive NUL");
 	}
 }
