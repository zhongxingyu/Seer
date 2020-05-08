 package com.mtgi.analytics;
 
 import static java.util.UUID.randomUUID;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Queue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPOutputStream;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedOperation;
 import org.springframework.jmx.export.annotation.ManagedOperationParameter;
 import org.springframework.jmx.export.annotation.ManagedResource;
 
 import com.mtgi.io.RelocatableFile;
 import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
 
 /**
  * Behavior Tracking persister which writes events to an XML log file,
  * either as plain text or FastInfoset binary XML.  Which format is
  * selected by {@link #setBinary(boolean)}.  Log rotation can be accomplished
  * by {@link #rotateLog()}.
  */
 @ManagedResource(objectName="com.mtgi:group=analytics,name=BehaviorTrackingLog", 
 		 		 description="Perform maintenance on BehaviorTracking XML logfiles")
 public class XmlBehaviorEventPersisterImpl 
 	implements BehaviorEventPersister, InitializingBean, DisposableBean 
 {
 	private static final Log log = LogFactory.getLog(XmlBehaviorEventPersisterImpl.class);
 
 	private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
 	private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^(.+)\\.b?xml(?:\\.gz)?");
 	
 	private boolean binary;
 	private boolean compress;
 	private File file;
 	private SimpleDateFormat dateFormat = DEFAULT_DATE_FORMAT;
 
 	private XMLStreamWriter writer;
 	private OutputStream stream;
 	
 	/** Set to true to log in FastInfoset binary XML format.  Defaults to false. */
 	@ManagedAttribute(description="Can be used to switch between binary and text XML.  Changes take affect after the next log rotation.")
 	public void setBinary(boolean binary) {
 		this.binary = binary;
 	}
 	@ManagedAttribute(description="Can be used to switch between binary and text XML.  Changes take affect after the next log rotation.")
 	public boolean isBinary() {
 		return binary;
 	}
 
 	@ManagedAttribute(description="Can be used to turn on/off log file compression.  Changes take affect after the next log rotation.")
 	public boolean isCompress() {
 		return compress;
 	}
 	/** Set to true to log in ZLIB compressed format.  Changes take affect after the next log rotation.  Defaults to false. */
 	@ManagedAttribute(description="Can be used to turn on/off log file compression.  Changes take affect after the next log rotation.")
 	public void setCompress(boolean compress) {
 		this.compress = compress;
 	}
 	
 	/** override the default log name date format */
 	public void setDateFormat(String dateFormat) {
 		this.dateFormat = new SimpleDateFormat(dateFormat);
 	}
 
 	/**
 	 * JMX operation to list archived xml data files available for download.
 	 */
 	@ManagedOperation(description="List all archived performance data log files available for download")
 	public StringBuffer listLogFiles() {
 
 		final Pattern pattern = getArchiveNamePattern();
 
 		//scan parent directory for matches.
 		File[] files = file.getParentFile().listFiles(new FileFilter() {
 			public boolean accept(File child) {
 				String childName = child.getName();
 				return pattern.matcher(childName).matches();
 			}
 		});
 		Arrays.sort(files, FileOrder.INST);
 
 		StringBuffer ret = new StringBuffer("<html><body><table><tr><th>File</th><th>Size</th><th>Modified</th></tr>");
 		for (File f : files)
 			ret.append("<tr><td>").append(f.getName()).append("</td><td>")
 			   .append(f.length()).append("</td><td>")
 			   .append(new Date(f.lastModified()).toString()).append("</td></tr>");
 		ret.append("</table></body></html>");
 		return ret;
 	}
 	
 	@ManagedOperation(description="directly access a performance log file by name; use listLogFiles to determine valid file names")
 	@ManagedOperationParameter(name="name", description="The relative name of the file to download; see listLogFiles")
 	public RelocatableFile downloadLogFile(String name) throws IOException {
 		if (!getArchiveNamePattern().matcher(name).matches())
 			throw new IllegalArgumentException(name + " does not appear to be a performance data file");
 		File data = new File(file.getParentFile(), name);
 		if (!data.isFile())
 			throw new FileNotFoundException(file.getAbsolutePath());
 		return new RelocatableFile(data);
 	}
 	
 	/** set the destination log file.  The file extension will be modified based on compression / binary settings. */
 	@Required
 	public void setFile(String path) {
 		this.file = new File(path);
 	}
 	
 	public String getFile() {
 		return file == null ? null : file.getAbsolutePath();
 	}
 	
 	public void afterPropertiesSet() throws Exception {
 		File dir = file.getCanonicalFile().getParentFile();
 		if (!dir.isDirectory() && !dir.mkdirs())
 			throw new IOException("Unable to create directories for log file " + file.getAbsolutePath());
 		
 		//open for business.
 		rotateLog();
 	}
 
 	public void destroy() throws Exception {
 		closeWriter();
 	}
 	
 	@ManagedAttribute(description="Report the current size of the XML log, in bytes")
 	public long getFileSize() {
 		return file.length();
 	}
 
 	public int persist(Queue<BehaviorEvent> events) {
 		int count = 0;
 		try {
 			BehaviorEventSerializer serializer = new BehaviorEventSerializer();
 			
 			while (!events.isEmpty()) {
 				BehaviorEvent event = events.remove();
 				if (event.getId() == null)
 					event.setId(randomUUID());
 				
 				BehaviorEvent parent = event.getParent();
 				if (parent != null && parent.getId() == null)
 					parent.setId(randomUUID());
 				
 				synchronized (this) {
 					serializer.serialize(writer, event);
 					writer.writeCharacters("\n");
 				}
 				
 				++count;
 			}
 			
 			synchronized (this) {
 				writer.flush();
 				stream.flush();
 			}
 			
 		} catch (Exception error) {
 			log.error("Error persisting events; discarding " + events.size() + " events without saving", error);
 			events.clear();
 		}
 		
 		return count;
 	}
 
 	/**
 	 * Force a rotation of the log.  The new archive log will be named <code>[logfile].yyyyMMddHHmmss</code>.
 	 * If a file of that name already exists, an extra _N will be appended to it, where N is an
 	 * integer sufficiently large to make the name unique.  This method can be called
 	 * externally by the Quartz scheduler for periodic rotation, or by an administrator
 	 * via JMX.
 	 */
 	@ManagedOperation(description="Force a rotation of the behavior tracking log")
 	public String rotateLog() throws IOException, XMLStreamException {
 		StringBuffer msg = new StringBuffer();
 		synchronized (this) {
 			//flush current writer and close streams.
 			closeWriter();
 			
			//update output file name based on current settings.
			file = getLogFile(file);
			
 			//archive existing contents
 			if (file.exists()) {
 				File archive = getArchiveFile();
 				if (!file.renameTo(archive))
 					throw new IOException("Unable to rename log to " + archive.getAbsolutePath());
 				msg.append(archive.getAbsolutePath());
 			} else {
 				msg.append("No existing log data.");
 			}
 
 			//open a new stream, optionally compressed.
 			if (compress)
 				stream = new GZIPOutputStream(new FileOutputStream(file));
 			else
 				stream = new BufferedOutputStream(new FileOutputStream(file));
 			
 			//open a new writer over the stream.
 			if (binary) {
 				StAXDocumentSerializer sds = new StAXDocumentSerializer();
 				sds.setOutputStream(stream);
 				writer = sds;
 				writer.writeStartDocument();
 				writer.writeStartElement("event-log");
 			} else {
 				writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
 			}
 		}
 		return msg.toString();
 	}
 	
 	private void closeWriter() {
 		if (writer != null) {
 			//finish writing XML document.
 			try {
 				if (binary)
 					writer.writeEndDocument();
 				writer.flush();
 			} catch (XMLStreamException xse) {
 				log.error("Error flushing log for rotation", xse);
 			} finally {
 				
 				//finish the compressed stream, if applicable.
 				try {
 					if (stream instanceof GZIPOutputStream)
 						((GZIPOutputStream)stream).finish();
 				} catch (IOException ioe) {
 					log.error("Error finishing zip stream", ioe);
 				} finally {
 
 					//close the file
 					try {
 						stream.close();
 					} catch (IOException ioe) {
 						log.error("Error closing log stream", ioe);
 					} finally {
 						writer = null;
 						stream = null;
 					}
 					
 				}
 			}
 		}
 	}
 	
 	/** get the active log file, modifying the supplied base name to reflect compressed / binary options. */
 	public File getLogFile(File file) {
 
 		//generate an extension based on output options.
 		String ext = isBinary() ? ".bxml" : ".xml";
 		if (isCompress())
 			ext += ".gz";
 		
 		//strip off current extension if applicable.
 		String baseName = file.getName();
 		Matcher matcher = FILE_NAME_PATTERN.matcher(baseName);
 		if (matcher.matches())
 			baseName = matcher.group(1);
 		
 		return new File(file.getParentFile(), baseName + ext);
 	}
 	
 	/** get the archive file to which current log data should be moved on rotation */
 	private File getArchiveFile() {
 		String baseName = file.getPath() + "." + dateFormat.format(new Date());
 		File ret = new File(baseName);
 		for (int i = 1; ret.exists() && i < 11; ++i)
 			ret = new File(baseName + "_" + i);
 		if (ret.exists())
 			throw new IllegalStateException("Unable to create a unique file name starting with " + baseName + "; maybe system date is off?");
 		return ret;
 	}
 	
 	private Pattern getArchiveNamePattern() {
 		//build a regex that matches files with the configured basename, plus a non-binary or binary xml extension,
 		//plus a date suffix.
 		String filePattern = file.getName();
 		Matcher m = FILE_NAME_PATTERN.matcher(filePattern);
 		if (m.matches())
 			filePattern = m.group(1);
 		filePattern += "\\.b?xml\\..+";
 		return Pattern.compile(filePattern);
 	}
 	
 	protected static class FileOrder implements Comparator<File> {
 
 		public static final FileOrder INST = new FileOrder();
 		
 		public int compare(File f0, File f1) {
 			long delta = f0.lastModified() - f1.lastModified();
 			return delta == 0 ? f0.compareTo(f1)
 							  : delta > 0 ? -1
 							  : 0;
 		}
 		
 	}
 }
