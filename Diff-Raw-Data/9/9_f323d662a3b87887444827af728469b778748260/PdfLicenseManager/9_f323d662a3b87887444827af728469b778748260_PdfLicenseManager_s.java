 /*
  * This file is part of PdfLicenseManager.
  *
  * Copyright (C) 2006  Enrico Masala
  *
  * PdfLicenseManager is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * PdfLicenseManager is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  *
  *
  * Author: Enrico Masala   < masala _at-symbol_ polito dot it >
  * Date: Jun 30, 2006
  *
  * Please cite the original author if you extend or modify this program
  */
 
 
 package pdflicense;
 
 import java.io.*;
 import java.util.*;
 
 import com.lowagie.text.*;
 import com.lowagie.text.pdf.*;
 
 
 public class PdfLicenseManager {
 
	public final String version = "2.1";
 
 	private String op = null;
 	private String fNameIn = null;
 	private String fNameOut = null;
 	private String licenseShortName = null;
 	private File fileIn = null;
 	private File fileOut = null;
 	private License currLic = null;
 	private String xmpText = null;
 	private XmpManager xm = null;
 	private PdfReader reader = null;
 	
 	public String getFileNameIn() { return fNameIn; }
 	public String getFileNameOut() { return fNameOut; }
 	public File getFileIn() { return fileIn; }
 	public File getFileOut() { return fileOut; }
 	public String getLicenseShortName() { return licenseShortName; }
 	public License getCurrLic() { return currLic; }
 	public String getXmpText() { return xmpText; }
 	
 	public void setFileIn(File f) { fileIn = f; }
 	public void setFileOut(File f) { fileOut = f; }
 	
 	public PdfLicenseManager() {
 		xm = new XmpManager();
 	}
 	
 	/**
 	 * Print usage info
 	 */
 	private void usage() {
 		System.out.print("\n");
 		System.out.print("PdfLicenseManager version "+version+", Copyright (C) 2006 Enrico Masala\n");
 		System.out.print("PdfLicenseManager comes with ABSOLUTELY NO WARRANTY; for details see the enclosed LICENSE.txt file\n");
 		System.out.print("This is free software, and you are welcome to redistribute it under certain conditions;\n");
 		System.out.print("for details see the enclosed LICENSE.txt file\n");
 		System.out.print("\n");
 		System.out.print("This software is licensed under GPL v.2.0\n");
 		System.out.print("Please note that you are using it at your own risk\n");
 		System.out.print("\n");
 		System.out.print("Usage: command  options\n");
 		System.out.print("\n");
 		System.out.print("Commands:\n");
 		System.out.print("help       \n                shows this help screen\n");
 		System.out.print("showXMP    file.pdf\n                shows the raw XMP info\n");
 		System.out.print("show       file.pdf\n                shows XMP licensing info\n");
 		System.out.print("showToc    file.pdf\n                shows pdf Toc (debug purposes only)\n");
 		System.out.print("put        filein.pdf  fileout.pdf  <license>\n                insert or update XMP licensing info, keep other info unchanged. <license> can be:\n");
 		System.out.print("                by  by-nc  by-nd  by-sa  by-nc-nd  by-nc-sa\n");
 		System.out.print("putforced  filein.pdf  fileout.pdf  <license>\n                insert XMP licensing info removing any XMP info - use to correct corrupt XMP info\n");
 		System.out.print("putXMP     filein.pdf  fileout.pdf  filein.xmp\n                remove existing XMP and insert XMP description from file\n");
 		System.out.print("                NB: It is safer to use put command to insert CC license\n");
 		System.out.print("                    because it preserves the already present XMP info");
 		System.out.print("\n\n");
 	}
 
 	/**
 	 * Update the XMP info with the insertion of a CC licence
 	 * 
 	 * @param args input pdf file, output pdf file, xmp file
 	 */
 	public void mainTextual(String[] args) {
 
 
 		if (args.length < 2) {
 			usage();
 			System.exit(1);
 		} else {
 			op = args[0];
 			if (op.equals("show") || op.equals("showXMP") || op.equals("showToc")) {
 				if (args.length != 2) {
 					System.out.print("\n\nERROR: incorrect number of parameters");
 					usage(); System.exit(1);
 				}
 				fNameIn = args[1];
 			} else if (op.equals("put") || op.equals("putXMP") || op.equals("putforce") ) {
 				if (args.length != 4) {
 					System.out.print("\n\nERROR: incorrect number of parameters");
 					usage(); System.exit(1);
 				}
 				fNameIn = args[1];
 				fNameOut = args[2];
 				licenseShortName = args[3];
 			} else {
 				System.out.print("ERROR: operation not known ("+op+")\n\n");
 				usage(); System.exit(1);
 			}
 		}
 
 		try {
 			PdfReader reader = new PdfReader(fNameIn);
 			XmpManager xm = new XmpManager();
 			byte [] xmpdata = reader.getMetadata();
 
 			if (op.equals("showXMP") || op.equals("show") ) {
 				if (xmpdata==null) {
 					System.out.print("No XMP Licensing info is present\n");
 				} else {
 					if (op.equals("showXMP")) {
 						String s = new String(xmpdata);
 						System.out.print(s);
 					} else if (op.equals("show")) {
 						String s = new String(xmpdata);
 						xm.parseXmp(s);
 						//System.out.println(xm.getXmpString());
 						License currLic = xm.getLicenseInfo();
 						System.out.print("***************************\n");
 						System.out.print("XMP Licensing info:\n");
 						System.out.print("***************************\n");
 						System.out.print(currLic.toString());
 						System.out.print("\n");
 					}
 				}
 			} else if (op.equals("showToc")) {
 				PdfDictionary cat = reader.getCatalog();
 				Set k = cat.getKeys();	
 				Object []arr = k.toArray();
 				for (int i=0; i<arr.length; i++) {
 					System.out.println(arr[i]);
 				}
 				//PdfObject p = cat.get(PdfName.METADATA);
 				//System.out.println(p.getName());
 				//System.out.println("/Metadata type="+p.type());
 				
 				//PRIndirectReference iref = (PRIndirectReference)p;
                 		//findAllObjects(reader, PdfReader.getPdfObject(obj), hits);
 
 
 				/*
 				HashMap map = reader.getInfo();
 				Set keys = map.keySet();	
 				Object []arr = keys.toArray();
 				for (int i=0; i<arr.length; i++) {
 					System.out.println(arr[i]);
 				}
 				*/
 			} else if (op.equals("put")) {
 				CCLicense lic = new CCLicense(licenseShortName);
 				if (xmpdata==null) {
 					xm.createLicense(lic);
 				} else {
 					String s = new String(xmpdata);
 					xm.parseXmp(s);
 					xm.updateLicense(lic);
 				}
 				String xmpString = xm.getXmpString();
 				FileOutputStream fos = new FileOutputStream(fNameOut);
 				writeXmp(reader, fos, xmpString.getBytes());
 			} else if (op.equals("putforce")) {
 				CCLicense lic = new CCLicense(licenseShortName);
 				xm.createLicense(lic);
 				String xmpString = xm.getXmpString();
 				FileOutputStream fos = new FileOutputStream(fNameOut);				
 				writeXmp(reader, fos, xmpString.getBytes());
 			} else if (op.equals("putXMP")) {
 				File f = new File(licenseShortName);
 				int length = (int)f.length();
 				FileInputStream fin = new FileInputStream (f);
 				BufferedInputStream data =  new BufferedInputStream(fin);
 				byte[] xmp = new byte[length]; 
 				data.read(xmp, 0, length);
 				fin.close();		
 
 				FileOutputStream fos = new FileOutputStream(fNameOut);
 				writeXmp(reader, fos, xmp);
 			}
 		} catch (DocumentException de) {
 			System.err.println(de.getMessage());
 		} catch (IOException ioe) {
 			System.err.println(ioe.getMessage());
 		} catch (LicenseException e) {
 			System.err.println(e.getMessage());
 		}
 
 	}
 
 	
 	private void writeXmp(PdfReader reader, FileOutputStream fos, byte[] xmpBytes) throws DocumentException, IOException {
 		PdfStamper stamp = new PdfStamper(reader, fos);
 
 		stamp.setXmpMetadata(xmpBytes);
 		stamp.close();
 	}
 
 	public void readFileInfo() throws IOException, LicenseException {
 		reader = new PdfReader(new FileInputStream(fileIn));
 		byte [] xmpdata = reader.getMetadata();
 		// reset current license info
 		currLic = null;
 
 		if (xmpdata != null) {
 			xmpText = new String(xmpdata);
 			xm.parseXmp(xmpText);
 			//System.out.println(xm.getXmpString());
 			currLic = xm.getLicenseInfo();
 //			System.out.print("***************************\n");
 //			System.out.print("XMP Licensing info:\n");
 //			System.out.print("***************************\n");
 //			System.out.print(currLic.toString());
 //			System.out.print("\n");
 		} else {
 			xmpText = null;
 		}
 	}
 
 	public void updateLicense(CCLicense lic) throws IOException, LicenseException {
 		//System.out.print("updateLicense called\n");
 		if (fileIn != null) {
 			if (xmpText != null) {
 				xm.updateLicense(lic);
 			} else {
 				xm.createLicense(lic);
 			}
 			xmpText = xm.getXmpString();
 			xm.parseXmp(xmpText);
 			currLic = xm.getLicenseInfo();
 			//writeXmp(reader, fNameOut, xmpText.getBytes());
 		}
 	}
 
 	public void writeFile(File f) throws DocumentException, IOException {
 		FileOutputStream fos = new FileOutputStream(f);
 		writeXmp(reader, fos, xmpText.getBytes());
 	}
 }
