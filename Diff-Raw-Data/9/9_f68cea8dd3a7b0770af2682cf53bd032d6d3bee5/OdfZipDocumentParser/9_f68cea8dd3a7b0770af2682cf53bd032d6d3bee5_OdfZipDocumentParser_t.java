 package com.googlecode.okapi.filter.odf;
 
import java.io.File;
import java.io.IOException;

 import com.googlecode.okapi.filter.zip.ZipDocumentParser;
import com.googlecode.okapi.resource.DocumentManager;
 
 public class OdfZipDocumentParser extends ZipDocumentParser{

	public OdfZipDocumentParser(DocumentManager documentManager, String inputFile) throws IOException {
 		super(documentManager, inputFile);
 	}
 }
