 package at.ac.ait.formatRegistry.gui.services;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import uk.bl.dpt.fido.*;
 import uk.gov.nationalarchives.pronom.PRONOMReport.IdentifierTypes;
 import uk.gov.nationalarchives.pronom.PRONOMReport.RelationshipTypes;
 import uk.gov.nationalarchives.pronom.PRONOMReport.ReportFormatDetail;
 import uk.gov.nationalarchives.pronom.PRONOMReport.SignatureTypes;
 import uk.gov.nationalarchives.pronom.PRONOMReport.ReportFormatDetail.FileFormat;
 import uk.gov.nationalarchives.pronom.PRONOMReport.ReportFormatDetail.FileFormat.ExternalSignature;
 import uk.gov.nationalarchives.pronom.PRONOMReport.ReportFormatDetail.FileFormat.FidoSignature;
 import uk.gov.nationalarchives.pronom.PRONOMReport.ReportFormatDetail.FileFormat.FileFormatIdentifier;
 import uk.gov.nationalarchives.pronom.PRONOMReport.ReportFormatDetail.FileFormat.InternalSignature;
 import uk.gov.nationalarchives.pronom.PRONOMReport.ReportFormatDetail.FileFormat.RelatedFormat;
 import uk.gov.nationalarchives.pronom.PRONOMReport.ReportFormatDetail.FileFormat.InternalSignature.ByteSequence;
 
 import javax.xml.bind.*;
 
 import at.ac.ait.formatRegistry.PronomTransformer;
 
 import uk.gov.nationalarchives.pronom.*;
 
 public class FileFormatDAOImpl implements FileFormatDAO {
 	private boolean dataLoaded = false;
 	private int highestFormatID;
 	private int highestSignatureID;
 	private int highestFidoSignatureID;
 	private int highestPronomOID;
 	private Hashtable<String, FileFormat> formatHash;
 	private Hashtable<String, FileFormat> puidFormatHash;
 	private Hashtable<String, PRONOMReport> reportHash;
 	private Set<String> _formatNames = new TreeSet<String>();
 	private File pronomXMLDir;
 	private String outputXMLpath;
 	private String formatXMLpath;
 	private String downloadPath;
 
 	public FileFormatDAOImpl() {
 		ResourceBundle registryResources = ResourceBundle.getBundle("registry");
 		if (registryResources != null) {
 			formatXMLpath = registryResources.getString("formatxmlpath");
 			outputXMLpath = registryResources.getString("outputxmlpath");
 			downloadPath = registryResources.getString("downloadpath");
 		}
 
 		File outputDir = new File(outputXMLpath);
 		if (!outputDir.exists()) {
 			System.out
 					.println("Improper property configuration, outputxmlpath property is missing or directory does not exist: "
 							+ outputXMLpath);
 		}
 
 		File downloadDir = new File(downloadPath);
 		if (!downloadDir.exists()) {
 			System.out
 					.println("Improper property configuration, downloadpath property is missing or  directory does not exist: "
 							+ downloadPath);
 		}
 
 		pronomXMLDir = new File(formatXMLpath);
 		if (!pronomXMLDir.exists()) {
 			System.out
 					.println("Improper property configuration, formatxmlpath property is missing or directory does not exist: "
 							+ formatXMLpath);
 		} else {
 			loadFormatData();
 		}
 	}
 
 	public void loadFormatData() {
 		JAXBContext context;
 		Unmarshaller unmarshaller;
 		if (dataLoaded)
 			return;
 		formatHash = new Hashtable<String, FileFormat>();
 		puidFormatHash = new Hashtable<String, FileFormat>();
 		reportHash = new Hashtable<String, PRONOMReport>();
 
 		try {
 			context = JAXBContext.newInstance("at.ac.ait.formatRegistry");
 			unmarshaller = context.createUnmarshaller();
 			File[] pronomXMLs = pronomXMLDir.listFiles();
 			for (int i = 0; i < pronomXMLs.length; i++) {
 				// for (int i = 0; i < 10; i++) {
 				File theFile = pronomXMLs[i];
 				if ( (!theFile.isDirectory()) && (theFile.getName().endsWith("xml"))) {
 					PRONOMReport report = (PRONOMReport) unmarshaller
 							.unmarshal(new FileReader(theFile));
 					List<PRONOMReport.ReportFormatDetail> listOfDetails = report
 							.getReportFormatDetail();
 					for (PRONOMReport.ReportFormatDetail item : listOfDetails) {
 						List<PRONOMReport.ReportFormatDetail.FileFormat> listOfFormats = item
 								.getFileFormat();
 						for (PRONOMReport.ReportFormatDetail.FileFormat format : listOfFormats) {
 							String formID = format.getFormatID();
 							int intID = new Integer(formID).intValue();
 							if (intID > highestFormatID)
 								highestFormatID = intID;
 							formatHash.put(formID, format);
 							reportHash.put(formID, report);
 							String puid = format.getPronomID();
 							if (puid.startsWith("o-")) {
 								int puidInt = new Integer(puid.substring(puid
 										.lastIndexOf("/") + 1)).intValue();
 								if (puidInt > highestPronomOID)
 									highestPronomOID = puidInt;
 							}
 							puidFormatHash.put(puid, format);
 							_formatNames.add(format.getFormatName());
 							List<InternalSignature> internalSignatures = format
 									.getInternalSignature();
 							for (InternalSignature signature : internalSignatures) {
 								int sigID = new Integer(signature.getSignatureID())
 										.intValue();
 								if (sigID > highestSignatureID)
 									highestSignatureID = sigID;
 							}
 							List<FidoSignature> fidoSignatures = format
 									.getFidoSignature();
 							for (FidoSignature fSignature : fidoSignatures) {
 								int fSigID = new Integer(
 										fSignature.getFidoSignatureID()).intValue();
 								if (fSigID > highestFidoSignatureID)
 									highestFidoSignatureID = fSigID;
 							}
 						}
 					}
 				}
 			}
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		dataLoaded = true;
 	}
 
 	public String getNewFormatID() {
 		highestFormatID = highestFormatID + 1;
 		return Integer.toString(highestFormatID);
 	}
 
 	public void delete(FileFormat format) {
 		String id = format.getFormatID();
 		String outputID = format.getPronomID();
 		formatHash.remove(id);
 		reportHash.remove(id);
 		puidFormatHash.remove(outputID);
 		outputID = outputID.replaceAll("/", ".");
 		File datafile = new File(formatXMLpath + "/" + "puid." + outputID
 				+ ".xml");
 		if (datafile.delete()) {
 			System.out.println("Successfully deleted format with ID: " + id + " and Pronom ID: " + outputID);;
 		} else {
 			System.out.println("Unable to delete format with ID: " + id + " and Pronom ID: " + outputID);;
 		}
 	}
 
 	public FileFormat find(String id) {
 		return formatHash.get(id);
 	}
 
 	public List<FileFormat> findAllFormats() {
 		if (formatHash!=null) {
 			return (List<FileFormat>) Collections
 				.synchronizedList(new ArrayList<FileFormat>(formatHash.values()));
 		} else {
 			return null;
 		}
 	}
 
 	public FileFormat findFormatByName(String name) {
 		FileFormat retFormat = null;
 		for (Enumeration<FileFormat> e = formatHash.elements(); e
 				.hasMoreElements();) {
 			FileFormat theFormat = e.nextElement();
 			String formName = theFormat.getFormatName();
 			if (formName.equals(name)) {
 				retFormat = theFormat;
 				break;
 			}
 		}
 		return retFormat;
 	}
 
 	public void save(FileFormat format) {
 		String formID = format.getFormatID();
 		String outputID = format.getPronomID();
 		outputID = outputID.replaceAll("/", ".");
 		PRONOMReport report = reportHash.get(formID);
 		JAXBContext context;
 		try {
 			context = JAXBContext.newInstance(PRONOMReport.class);
 			Marshaller marshaller = context.createMarshaller();
 			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 			marshaller.marshal(report, new FileWriter(formatXMLpath + "/"
 					+ "puid." + outputID + ".xml"));
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public Set<String> getFormatNamesSet() {
 		return Collections.unmodifiableSet(_formatNames);
 	}
 
 	public List<FileFormat> findFormatsByExtension(String fragment) {
		if (fragment==null) return null;
 		ArrayList<FileFormat> results = new ArrayList<FileFormat>();
 		Iterator<FileFormat> it = formatHash.values().iterator();
 		while (it.hasNext()) {
 			FileFormat format = it.next();
 			String extensions = format.getExtensionList();
 			if (extensions.contains(fragment))
 				results.add(format);
 		}
 		return results;
 	}
 
 	public List<FileFormat> findFormatsByName(String fragment) {
		if (fragment==null) return null;
 		ArrayList<FileFormat> results = new ArrayList<FileFormat>();
 		Iterator<FileFormat> it = formatHash.values().iterator();
 		while (it.hasNext()) {
 			FileFormat format = it.next();
 			String name = format.getFormatName();
 			if (name.contains(fragment))
 				results.add(format);
 		}
 		return results;
 	}
 
 	public List<FileFormat> findFormatsByPronomId(String fragment) {
		if (fragment==null) return null;
 		ArrayList<FileFormat> results = new ArrayList<FileFormat>();
 		Iterator<FileFormat> it = formatHash.values().iterator();
 		while (it.hasNext()) {
 			FileFormat format = it.next();
 			String id = format.getPronomID();
 			if (id.contains(fragment))
 				results.add(format);
 		}
 		return results;
 	}
 
 	public String getNewInternalSignatureID() {
 		highestSignatureID = highestSignatureID + 1;
 		return Integer.toString(highestSignatureID);
 	}
 
 	public String getNewFidoSignatureID() {
 		highestFidoSignatureID = highestFidoSignatureID + 1;
 		return Integer.toString(highestFidoSignatureID);
 	}
 
 	public File exportToFido() {
 		Formats fidoFormats = new Formats();
 		fidoFormats.setVersion(new BigDecimal("0.1"));
 		JAXBContext context;
 		File outputFile = new File(downloadPath + "/" + "formats.xml");
 		for (FileFormat fileFormat : formatHash.values()) {
 			if (fileFormat.getFidoSignature().size() > 0) {
 				Format fidoFormat = new Format();
 				fidoFormat.setName(fileFormat.getFormatName());
 				fidoFormat.setPuid(fileFormat.getPronomID());
 				fidoFormat
 						.setPronomId(new BigInteger(fileFormat.getFormatID()));
 				ContainerType ct = fileFormat.getContainer();
 				if (ct != null)
 					fidoFormat.setContainer(ct);
 				for (FileFormatIdentifier ffi : fileFormat
 						.getFileFormatIdentifier()) {
 					if (ffi.getIdentifierType() == IdentifierTypes.MIME) {
 						String mime = ffi.getIdentifier();
 						fidoFormat.getMime().add(mime);
 					}
 				}
 				for (ExternalSignature es : fileFormat.getExternalSignature()) {
 					String extension = es.getSignature();
 					fidoFormat.getExtension().add(extension);
 				}
 				for (RelatedFormat rf : fileFormat.getRelatedFormat()) {
 					if (rf.getRelationshipType() == RelationshipTypes.Has_priority_over) {
 						String id = rf.getRelatedFormatID();
 						FileFormat relatedFormat = this.find(id);
 						if (relatedFormat != null) {
 							String hasPriorityOver = relatedFormat
 									.getPronomID();
 							fidoFormat.getHasPriorityOver()
 									.add(hasPriorityOver);
 						}
 					}
 				}
 				for (FidoSignature fs : fileFormat.getFidoSignature()) {
 					if (fs.getFidoPrioritize()) {
 						Signature signature = new Signature();
 						String sName = fs.getFidoSignatureName();
 						signature.setName(sName);
 						signature.setNote(fs.getFidoSignatureNote());
 						InternalSignature correspondingInternalSignature = null;
 						for (InternalSignature is : fileFormat
 								.getInternalSignature()) {
 							if (is.getSignatureName().equals(sName)) {
 								correspondingInternalSignature = is;
 								break;
 							}
 						}
 						if (correspondingInternalSignature == null)
 							correspondingInternalSignature = new InternalSignature();
 						for (PRONOMReport.ReportFormatDetail.FileFormat.FidoSignature.Pattern p : fs
 								.getPattern()) {
 							uk.bl.dpt.fido.Pattern fPattern = new uk.bl.dpt.fido.Pattern();
 							PositionType pt = p.getPosition();
 							if (pt != null)
 								fPattern.setPosition(pt);
 							fPattern.setRegex(p.getRegex());
 							String fidoPatternId = p.getPatternID();
 							for (ByteSequence bs : correspondingInternalSignature
 									.getByteSequence()) {
 								if (bs.getByteSequenceID()
 										.equals(fidoPatternId)) {
 									fPattern.setPronomPattern(bs
 											.getByteSequenceValue());
 									break;
 								}
 							}
 							signature.getPattern().add(fPattern);
 						}
 						fidoFormat.getSignature().add(signature);
 					}
 				}
 				fidoFormats.getFormat().add(fidoFormat);
 			}
 		}
 		try {
 			context = JAXBContext.newInstance(Formats.class);
 			Marshaller marshaller = context.createMarshaller();
 			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 			marshaller.setProperty(
 					Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,
 					"fido-formats.xsd");
 
 			marshaller.marshal(fidoFormats, new FileWriter(outputFile));
 			// marshaller.marshal(fidoFormats, response);
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return outputFile;
 
 	}
 
 	public String importFromFido(File fidoFile) {
 		JAXBContext context;
 		Unmarshaller unmarshaller;
 		String returnString = "";
 		FileFormat format;
 		try {
 			context = JAXBContext.newInstance("uk.bl.dpt.fido");
 			unmarshaller = context.createUnmarshaller();
 			Formats fidoFormats = (Formats) unmarshaller
 					.unmarshal(new FileReader(fidoFile));
 
 			for (Format fidoFormat : fidoFormats.getFormat()) {
 				format = null;
 				String pId = null;
 				BigInteger puidInt = fidoFormat.getPronomId();
 				if (puidInt != null)
 					pId = puidInt.toString();
 				if (pId != null)
 					format = this.find(pId.trim());
 				if (format == null) {
 					String puid = fidoFormat.getPuid();
 					if (puid != null)
 						format = puidFormatHash.get(puid.trim());
 					if (format != null) {
 						returnString += "\nFound an existing format record with Pronom ID: "
 								+ puid;
 					} else {
 						format = new FileFormat();
 						String newID = this.getNewFormatID();
 						format.setFormatID(newID);
 						formatHash.put(newID, format);
 						PRONOMReport report = new PRONOMReport();
 						ReportFormatDetail rfd = new ReportFormatDetail();
 						rfd.getFileFormat().add(format);
 						report.getReportFormatDetail().add(rfd);
 						reportHash.put(newID, report);
 						String desc = "Imported from " + fidoFile.getName()
 								+ ".";
 						String oid = fidoFormat.getPuid();
 						if (!oid.equals("")) {
 							desc += " Previous working ID: " + oid;
 						}
 						format.setFormatDescription(desc);
 						FileFormatIdentifier ffi = new FileFormatIdentifier();
 						ffi.setIdentifierType(IdentifierTypes.PUID);
 						String newPUID = this.getNewPronomID();
 						ffi.setIdentifier(newPUID);
 						puidFormatHash.put(newPUID, format);
 						returnString += "\nCreated a new format record with Pronom ID: "
 								+ newPUID;
 						format.getFileFormatIdentifier().add(ffi);
 						format.setFormatName(fidoFormat.getName());
 						for (String relatedFormatPUID : fidoFormat
 								.getHasPriorityOver()) {
 							if (relatedFormatPUID != null) {
 								FileFormat relatedFormat = puidFormatHash
 										.get(relatedFormatPUID.trim());
 								if (relatedFormat != null) {
 									RelatedFormat rf = new RelatedFormat();
 									rf.setRelationshipType(RelationshipTypes.Has_priority_over);
 									rf.setRelatedFormatID(relatedFormat
 											.getFormatID());
 									rf.setRelatedFormatName(relatedFormat
 											.getFormatName());
 									rf.setRelatedFormatVersion(relatedFormat
 											.getFormatVersion());
 									format.getRelatedFormat().add(rf);
 								}
 							}
 						}
 						for (String ex : fidoFormat.getExtension()) {
 							ExternalSignature es = new ExternalSignature();
 							es.setSignatureType(SignatureTypes.File_extension);
 							es.setSignature(ex.trim());
 							format.getExternalSignature().add(es);
 						}
 						for (String mime : fidoFormat.getMime()) {
 							FileFormatIdentifier ffi2 = new FileFormatIdentifier();
 							ffi2.setIdentifierType(IdentifierTypes.MIME);
 							ffi2.setIdentifier(mime.trim());
 							format.getFileFormatIdentifier().add(ffi2);
 						}
 					}
 				} else {
 					returnString += "\nFound an existing format record with internal ID: "
 							+ pId;
 				}
 				ContainerType ct = fidoFormat.getContainer();
 				if (ct != null)
 					format.setContainer(ct);
 				List<InternalSignature> iss = format.getInternalSignature();
 				if (!fidoFormat.getSignature().isEmpty()) {
 					for (FidoSignature fSig : format.getFidoSignature()) {
 						fSig.setFidoPrioritize(false);
 					}
 				}
 				for (Signature fs : fidoFormat.getSignature()) {
 					String sName = fs.getName();
 					InternalSignature correspondingInternalSignature = null;
 					for (InternalSignature is : iss) {
 						if (is.getSignatureName().equals(sName)) {
 							correspondingInternalSignature = is;
 							break;
 						}
 					}
 					if (correspondingInternalSignature == null)
 						correspondingInternalSignature = new InternalSignature();
 					FidoSignature signature = new FidoSignature();
 					highestFidoSignatureID++;
 					signature.setFidoSignatureID(new Integer(
 							highestFidoSignatureID).toString());
 					signature.setFidoSignatureName(sName);
 					signature.setFidoSignatureNote(fs.getNote());
 					signature.setFidoPrioritize(true);
 					for (uk.bl.dpt.fido.Pattern p : fs.getPattern()) {
 						PRONOMReport.ReportFormatDetail.FileFormat.FidoSignature.Pattern fPattern = new PRONOMReport.ReportFormatDetail.FileFormat.FidoSignature.Pattern();
 						if (p.getPosition() != null)
 							fPattern.setPosition(p.getPosition());
 						String droidPattern = p.getPronomPattern();
 						String fidoPatternId = "";
 						for (ByteSequence bs : correspondingInternalSignature
 								.getByteSequence()) {
 							if (bs.getByteSequenceValue().equals(droidPattern)) {
 								fidoPatternId = bs.getByteSequenceID();
 							}
 						}
 						fPattern.setPatternID(fidoPatternId);
 						fPattern.setRegex(p.getRegex());
 						signature.getPattern().add(fPattern);
 					}
 					format.getFidoSignature().add(signature);
 				}
 				this.save(format);
 			}
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(returnString);
 		return returnString;
 	}
 
 	public String getNewPronomID() {
 		highestPronomOID++;
 		String poidString = "o-fmt/" + new Integer(highestPronomOID).toString();
 		return poidString;
 	}
 
 	public String getWorkingDirectoryPath() {
 		return outputXMLpath;
 	}
 
 	public File exportToPronom() {
 		Enumeration<FileFormat> en = formatHash.elements();
 		File downloadFile = new File(downloadPath + "/pronom.zip");
 		while (en.hasMoreElements()) {
 			FileFormat format = en.nextElement();
 			String outputID = format.getPronomID();
 			outputID = outputID.replaceAll("/", ".");
 			File datafile = new File(formatXMLpath + "/" + "puid." + outputID
 					+ ".xml");
 			File outputfile = new File(outputXMLpath + "/" + "puid." + outputID
 					+ ".xml");
 			PronomTransformer.transform(datafile, outputfile);
 		}
 
 		try {
 			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
 					downloadFile));
 			zipDir(outputXMLpath, zos);
 			// close the stream
 			zos.close();
 		} catch (Exception e) {
 			// handle exception
 		}
 
 		return downloadFile;
 	}
 
 	public void zipDir(String dir2zip, ZipOutputStream zos) {
 		try {
 			File zipDir = new File(dir2zip);
 			// get a listing of the directory content
 			String[] dirList = zipDir.list();
 			byte[] readBuffer = new byte[2156];
 			int bytesIn = 0;
 			// loop through dirList, and zip the files
 			for (int i = 0; i < dirList.length; i++) {
 				File f = new File(zipDir, dirList[i]);
 				if (f.isDirectory()) {
 					// if the File object is a directory, call this
 					// function again to add its content recursively
 					String filePath = f.getPath();
 					zipDir(filePath, zos);
 					// loop again
 					continue;
 				}
 
 				FileInputStream fis = new FileInputStream(f);
 				ZipEntry anEntry = new ZipEntry(f.getName());
 				// place the zip entry in the ZipOutputStream object
 				zos.putNextEntry(anEntry);
 				// now write the content of the file to the ZipOutputStream
 				while ((bytesIn = fis.read(readBuffer)) != -1) {
 					zos.write(readBuffer, 0, bytesIn);
 				}
 				// close the Stream
 				fis.close();
 				// delete the file
 				f.delete();
 			}
 		} catch (Exception e) {
 			// handle exception
 		}
 	}
 
 	public FileFormat getNewFormat() {
 		PRONOMReport report = new PRONOMReport();
 		ReportFormatDetail rfd = new ReportFormatDetail();
 		FileFormat format = new FileFormat();
 		String formatID = this.getNewFormatID();
 		String pronomID = this.getNewPronomID();
 		format.setFormatID(formatID);
 		FileFormatIdentifier ffi = new FileFormatIdentifier();
 		ffi.setIdentifierType(IdentifierTypes.PUID);
 		ffi.setIdentifier(pronomID);
 		format.getFileFormatIdentifier().add(ffi);
 		rfd.getFileFormat().add(format);
 		report.getReportFormatDetail().add(rfd);
 		formatHash.put(formatID, format);
 		reportHash.put(formatID, report);
 		puidFormatHash.put(pronomID, format);
 		return format;
 	}
 }
