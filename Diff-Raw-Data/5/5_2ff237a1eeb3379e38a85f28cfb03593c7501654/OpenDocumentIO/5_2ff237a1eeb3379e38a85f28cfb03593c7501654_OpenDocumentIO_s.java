 package net.sf.jooreports.opendocument;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.zip.CRC32;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 
 import org.apache.commons.io.IOUtils;
 
 public class OpenDocumentIO {
 	
 	public static final Charset UTF_8 = Charset.forName("UTF-8");
 
 	public static InputStreamReader toUtf8Reader(InputStream inputStream) {
 		return new InputStreamReader(inputStream, UTF_8);
 	}
 
 	public static OutputStreamWriter toUtf8Writer(OutputStream outputStream) {
 		return new OutputStreamWriter(outputStream, UTF_8);
 	}
 
 	public static OpenDocumentArchive readZip(InputStream inputStream) throws IOException {
 		OpenDocumentArchive archive = new OpenDocumentArchive();
 		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
 		while (true) {
 			ZipEntry zipEntry = zipInputStream.getNextEntry();
 			if (zipEntry == null) {
 				break;
 			}
 			OutputStream entryOutputStream = archive.getEntryOutputStream(zipEntry.getName());
 			IOUtils.copy(zipInputStream, entryOutputStream);
 			entryOutputStream.close();
 			zipInputStream.closeEntry();
 		}
 		zipInputStream.close();
 		return archive;
 	}
 
 	public static OpenDocumentArchive readDirectory(File directory) throws IOException {
 		if (!(directory.isDirectory() && directory.canRead())) {
 			throw new IllegalArgumentException("not a readable directory: " + directory);
 		}
 		OpenDocumentArchive archive = new OpenDocumentArchive();
 		readSubDirectory(directory, "", archive);
 		return archive;
 	}
 
 	private static void readSubDirectory(File subDirectory, String parentName, OpenDocumentArchive archive) throws IOException {
 		String[] fileNames = subDirectory.list();
 		for (int i = 0; i < fileNames.length; i++) {
 			File file = new File(subDirectory, fileNames[i]);
 			String relativeName = parentName + fileNames[i];
 			if (file.isDirectory()) {
 				readSubDirectory(file, relativeName + "/", archive);
 			} else {
 				InputStream fileInputStream = new FileInputStream(file);
 				OutputStream entryOutputStream = archive.getEntryOutputStream(relativeName);
 				IOUtils.copy(fileInputStream, entryOutputStream);
 				entryOutputStream.close();
 				fileInputStream.close();
 			}
 		}
 	}
 
 	public static void writeZip(OpenDocumentArchive archive, OutputStream outputStream) throws IOException {
 		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
 		Set entryNames = archive.getEntryNames();
 		
 		// OpenDocument spec requires 'mimetype' to be the first entry
 		writeZipEntry(zipOutputStream, archive, "mimetype", ZipEntry.STORED);
 		
 		for (Iterator it = entryNames.iterator(); it.hasNext();) {
 			String entryName = (String) it.next();
 			if (!"mimetype".equals(entryName)) {
 				writeZipEntry(zipOutputStream, archive, entryName, ZipEntry.DEFLATED);
 			}
 		}
 		zipOutputStream.close();
 	}
 
 	private static void writeZipEntry(ZipOutputStream zipOutputStream, OpenDocumentArchive archive, String entryName, int method) throws IOException {
 		ZipEntry zipEntry = new ZipEntry(entryName);
		zipOutputStream.putNextEntry(zipEntry);
 		InputStream entryInputStream = archive.getEntryInputStream(entryName);
 		zipEntry.setMethod(method);
 		if (method == ZipEntry.STORED) {
 			byte[] inputBytes = IOUtils.toByteArray(entryInputStream);
 			CRC32 crc = new CRC32();
 			crc.update(inputBytes);
 			zipEntry.setCrc(crc.getValue());
 			zipEntry.setSize(inputBytes.length);
 			zipEntry.setCompressedSize(inputBytes.length);
 			IOUtils.write(inputBytes, zipOutputStream);
 		} else {
 			IOUtils.copy(entryInputStream, zipOutputStream);
 		}
 		IOUtils.closeQuietly(entryInputStream);
 		zipOutputStream.closeEntry();
 	}
 }
