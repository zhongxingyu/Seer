 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 package org.eclipse.imp.smapi;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import com.ibm.wala.shrikeBT.shrikeCT.ClassInstrumenter;
 import com.ibm.wala.shrikeBT.shrikeCT.OfflineInstrumenter;
 import com.ibm.wala.shrikeCT.ClassReader;
 import com.ibm.wala.shrikeCT.ClassWriter;
 import com.ibm.wala.shrikeCT.InvalidClassFileException;
 import com.ibm.wala.shrikeCT.SourceDebugExtensionWriter;
 import com.ibm.wala.shrikeCT.ClassReader.AttrIterator;
 
 public class Main {
     public static boolean debug= false;
 
     /**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String MAIN_CLASS = args[0];
 		smapify(MAIN_CLASS, null, null);
 	}
 
 	/**
 	 * @param filename: the name of the file, possibly with path information
 	 * @param relPathPrefix: the portion of the filename we wish to conserve
 	 * @param outputfile: the output filename
 	 * <br>E.g.:
 	 * <ul>
 	 * <li>filename = C:/foo/bar/bla.x
 	 * <li>relPathPrefix = bar  
 	 * <li>outputfile = C:/foo/bin/bar/bla.class
 	 * </ul>
 	 * information in SMAP becomes: bar/bla.x
 	 */
 	public static void smapify(String filename, String relPathPrefix, String outputfile) {
 		String prefix = removeExt(filename);
 		String origExten = filename.substring(filename.lastIndexOf('.')+1);
 
 		if (debug) {
 			System.out.println("origExten=" + origExten);
 			System.out.println("smapify filename: " + filename);
 			System.out.println("with pathPrefix: " + relPathPrefix);
 			System.out.println("and outputfile: " + outputfile);
 		}
 		
 		LineMapBuilder lmb = new LineMapBuilder(prefix);
 		String smap = SMAPCreator.get(prefix, relPathPrefix, lmb.get(), origExten);
 
 		if (debug)
 			System.out.println(smap);
 		
 		try {
 			String inputName = (outputfile == null) ? prefix + ".class" : outputfile;
 			File input = new File(inputName);
             OfflineInstrumenter oi = new OfflineInstrumenter();
 			oi.addInputClass(input);
 			oi.beginTraversal();
 			ClassInstrumenter ci = oi.nextClass();
 			ClassReader cr = ci.getReader();
 			ClassWriter w = new ClassWriter();
 
 			copyClassProperties(cr, w);
 			copyMembersAndAttributes(cr, w);
			addSMAPAttribute(smap, w);
 
 			FileOutputStream fw = new FileOutputStream(new File(inputName));
 			fw.write(w.makeBytes());
 			fw.close();
 
 			oi.close();
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 	}
 
     private static void copyClassProperties(ClassReader cr, ClassWriter w) throws InvalidClassFileException {
         w.setRawCP(cr.getCP(), true);
         w.setMajorVersion(cr.getMajorVersion());
         w.setMinorVersion(cr.getMinorVersion());
         w.setAccessFlags(cr.getAccessFlags());
         w.setName(cr.getName());
         w.setSuperName(cr.getSuperName());
         w.setInterfaceNames(cr.getInterfaceNames());
     }
 
     private static void copyMembersAndAttributes(ClassReader cr, ClassWriter w) throws InvalidClassFileException, Exception {
         ClassReader.AttrIterator iter = new ClassReader.AttrIterator();
 
         int fieldCount = cr.getFieldCount();	
         for (int i = 0; i < fieldCount; i++) {
         	cr.initFieldAttributeIterator(i, iter);
         	w.addField(cr.getFieldAccessFlags(i), cr.getFieldName(i), cr.getFieldType(i), collectAttributes(cr, iter)); 
         }
 
         int methodCount = cr.getMethodCount();
         for (int i = 0; i < methodCount; i++) {
         	cr.initMethodAttributeIterator(i, iter);
         	w.addMethod(cr.getMethodAccessFlags(i), cr.getMethodName(i), cr.getMethodType(i), collectAttributes(cr, iter));
         }
 
         cr.initClassAttributeIterator(iter);
         for (; iter.isValid(); iter.advance()) {
         	w.addClassAttribute(getRawAttribute(cr, iter));
         }
     }
 
    private static void addSMAPAttribute(String smap, ClassWriter w) {
         SourceDebugExtensionWriter sw = new SourceDebugExtensionWriter(w);
         sw.setDebugInfo(smap);
         w.addClassAttribute(sw);
     }
 
 	private static String removeExt(String filename){
 		int i = filename.lastIndexOf(".");
 		return filename.substring(0,i);
 	}
 
 	private static ClassWriter.Element[] collectAttributes(ClassReader cr, AttrIterator iter) throws Exception {
 		ClassWriter.Element[] elems = new ClassWriter.Element[iter.getRemainingAttributesCount()];
 		for (int i = 0; i < elems.length; i++) {			
 		    elems[i] = getRawAttribute(cr, iter);
 			iter.advance();
 		}
 		return elems;
 	}
 
 	private static ClassWriter.Element getRawAttribute(ClassReader cr, AttrIterator iter) {
 		int offset = iter.getRawOffset();
 		int end = offset + iter.getRawSize();
 		return new ClassWriter.RawElement(cr.getBytes(), offset, end - offset);
 	}
 }
