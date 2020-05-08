 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.transport.sftp;
 
 
 import org.mule.api.MuleEvent;
 import org.mule.api.MuleMessage;
 import org.mule.api.endpoint.OutboundEndpoint;
 import org.mule.transport.AbstractMessageDispatcher;
 import org.mule.transport.sftp.notification.SftpNotifier;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 /**
  * <code>SftpMessageDispatcher</code> dispatches files via sftp to a remote sftp service.
  * This dispatcher reads an InputStream, byte[], or String payload, which is then
  * streamed to the SFTP endpoint.
  */
 
 public class SftpMessageDispatcher extends AbstractMessageDispatcher
 {
 
 	private SftpConnector connector;
 	private SftpUtil sftpUtil = null;
 
 	public SftpMessageDispatcher(OutboundEndpoint endpoint)
 	{
 		super(endpoint);
 		connector = (SftpConnector) endpoint.getConnector();
 		sftpUtil = new SftpUtil(endpoint);
 	}
 
 	protected void doConnect() throws Exception
 	{
 		//no op
 	}
 
 	protected void doDisconnect() throws Exception
 	{
 		//no op
 	}
 
 //	protected MuleMessage doReceive(long l)
 //	{
 //		throw new UnsupportedOperationException("doReceive");
 //	}
 
 	protected void doDispose()
 	{
 		//no op
 	}
 
 	protected void doDispatch(MuleEvent event) throws Exception
 	{
 		Object data = event.transformMessage();
 		String filename = (String) event.getProperty(SftpConnector.PROPERTY_FILENAME);
 
 		//If no name specified, set filename according to output pattern specified on
 		//endpoint or connector
 		if (filename == null)
 		{
 			MuleMessage message = event.getMessage();
 
 			String outPattern = (String) endpoint.getProperty(SftpConnector.PROPERTY_OUTPUT_PATTERN);
 			if (outPattern == null)
 			{
 				outPattern = message.getStringProperty(
 					SftpConnector.PROPERTY_OUTPUT_PATTERN, connector.getOutputPattern());
 			}
 			filename = generateFilename(message, outPattern);
 		}
 
 		//byte[], String, or InputStream payloads supported.
 
 		byte[] buf;
 		InputStream inputStream;
 
 		if (data instanceof byte[])
 		{
 			buf = (byte[]) data;
 			inputStream = new ByteArrayInputStream(buf);
 		} else if (data instanceof InputStream)
 		{
 			inputStream = (InputStream) data;
 
 		} else if (data instanceof String)
 		{
 			inputStream = new ByteArrayInputStream(((String) data).getBytes());
 
 		} else
 		{
			throw new IllegalArgumentException("Unexpected message type: java.io.InputStream or byte[] expected ");
 		}
 
 		if (logger.isDebugEnabled())
 		{
 			logger.debug("Writing file to: " + endpoint.getEndpointURI());
 		}
 
 		SftpClient client = null;
 		boolean useTempDir = false;
 		String transferFilename = null;
 
 		try
 		{
 			String serviceName = (event.getService() == null) ? "UNKNOWN SERVICE" : event.getService().getName();
 			SftpNotifier notifier = new SftpNotifier(connector, event.getMessage(), endpoint, serviceName);
 			client = connector.createSftpClient(endpoint, notifier);
 			String destDir = endpoint.getEndpointURI().getPath();
 
 			if (logger.isDebugEnabled())
 			{
 				logger.debug("Connection setup successful, writing file.");
 			}
 
 			// Duplicate Handling
 			filename = client.duplicateHandling(destDir, filename, sftpUtil.getDuplicateHandling());
 			transferFilename = filename;
 
 			useTempDir = sftpUtil.isUseTempDir();
 			if (useTempDir)
 			{
 				sftpUtil.createSftpDirIfNotExists(client, destDir);
 
 				// Add unique file-name (if configured) for use during transfer to temp-dir
 				boolean addUniqueSuffix = sftpUtil.isUseTempFileTimestampSuffix();
 				if (addUniqueSuffix)
 				{
 					transferFilename = sftpUtil.createUniqueSuffix(transferFilename);
 				}
 			}
 
 			// send file over sftp
 			client.storeFile(transferFilename, inputStream);
 
 			if (useTempDir)
 			{
 				// Move the file to its final destination
 				client.rename(transferFilename, destDir + "/" + filename);
 			}
 
 			logger.info("Successfully wrote file '" + filename + "' to " + endpoint.getEndpointURI());
 		}
 		catch (Exception e)
 		{
 			logger.error("Unexpected exception attempting to write file, message was: " + e.getMessage(), e);
       if(sftpUtil.isKeepFileOnError()) {
           // If an exception occurs and the keepFileOnError property is true, keep the file on the originating endpoint
           // Note: this is only supported when using the sftp transport on both inbound & outbound
         if (inputStream != null)
         {
           if (inputStream instanceof SftpInputStream)
           {
             // Ensure that the SftpInputStream knows about the error and dont delete the file
             ((SftpInputStream) inputStream).setErrorOccurred();
 
           } else if (inputStream instanceof SftpFileArchiveInputStream)
           {
             // Ensure that the SftpFileArchiveInputStream knows about the error and don't delete the file
             ((SftpFileArchiveInputStream) inputStream).setErrorOccurred();
 
           } else
           {
             logger.warn("Neither SftpInputStream nor SftpFileArchiveInputStream used, errorOccured=true could not be set. Type is " + inputStream.getClass().getName());
           }
         }
       }
 			if (useTempDir)
 			{
 				// Cleanup the remote temp dir!
 				sftpUtil.cleanupTempDir(client, transferFilename);
 			}
 			throw e;
 		}
 		finally
 		{
 			if (client != null)
 			{
 				// If the connection fails, the client will be null, otherwise disconnect.
 				connector.releaseClient(endpoint, client);
 			}
 //		    else
 //		    {
 //		        logger.warn("Unexpected null SFTPClient instance - operation probably failed ...");
 //		    }
 
 			inputStream.close();
 
 		}
 
 	}
 
 	protected MuleMessage doSend(MuleEvent event) throws Exception
 	{
 		doDispatch(event);
 		return event.getMessage();
 	}
 
 //	public Object getDelegateSession()
 //	{
 //		return null;
 //	}
 
 	private String generateFilename(MuleMessage message, String pattern)
 	{
 		if (pattern == null)
 		{
 			pattern = connector.getOutputPattern();
 		}
 		return connector.getFilenameParser().getFilename(message, pattern);
 	}
 }
