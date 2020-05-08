 	package com.redbugz.maf.jdom;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.xpath.XPath;
 
 import com.apple.cocoa.foundation.*;
 import com.redbugz.maf.*;
 import com.redbugz.maf.util.CocoaUtils;
 import com.redbugz.maf.util.StringUtils;
 
 public class GedcomLoaderJDOM {
 	private static final Logger log = Logger.getLogger(GedcomLoaderJDOM.class);
 
 	/**
 	 * This is the main jdom document that holds all of the data
 	 */
 	private MAFDocumentJDOM _doc;
 	NSObject progressDelegate;
 	private double currentProgressValue = 0;
 	private double maxProgressValue = 0;
 	private String status = "Loading data...";
 
 	private Document newDoc;
 
 	private static SAXBuilder builder;
 
 	private NSMutableDictionary statusDict = new NSMutableDictionary();
 	
 	static {
 		builder = new SAXBuilder("gedml.GedcomParser");
 		// need to load the parser in the main thread to avoid classloader issues
 		// in separate threads later
 		try {
 			builder.build("");
 		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
 	}
 	
 	public GedcomLoaderJDOM() {}
 		
 	public GedcomLoaderJDOM(MAFDocumentJDOM document, NSObject progressDelegate) {
 		_doc = document;
 		this.progressDelegate = progressDelegate;
 	}
 	
 //	public String test() {return ""+_doc+progressDelegate;}
 	
 	public static void loadDataForDocumentWithUpdateDelegate(NSDictionary dictionary) {
 		System.out.println("GedcomLoaderJDOM.loadDataForDocumentWithUpdateDelegate()"+dictionary);
 		//return
 		loadDataForDocumentWithUpdateDelegate((NSData)dictionary.valueForKey("data"), (MAFDocumentJDOM)dictionary.valueForKey("document"), (NSObject)dictionary.valueForKey("delegate"), (NSObject)dictionary.valueForKey("controller"));
 	}
 
 	public static void loadDataForDocumentWithUpdateDelegate(NSData data, MAFDocumentJDOM document, NSObject delegate, NSObject controller) {
 		System.out.println("GedcomLoaderJDOM.loadDataForDocumentWithUpdateDelegate():"+delegate);
 		GedcomLoaderJDOM gedcomLoaderJDOM = new GedcomLoaderJDOM(document, delegate);
 //		NSNotificationCenter.defaultCenter().addObserver(delegate, CocoaUtils.UPDATE_PROGRESS_SELECTOR, CocoaUtils.UPDATE_PROGRESS_NOTIFICATION, gedcomLoaderJDOM);
 		try {
 			NSNotificationCenter.defaultCenter().addObserver(controller, CocoaUtils.TASK_DONE_SELECTOR, CocoaUtils.TASK_DONE_NOTIFICATION, delegate);
 			gedcomLoaderJDOM.loadXMLData(data);
 //		gedcomLoaderJDOM.importXMLData();
 			NSNotificationCenter.defaultCenter().postNotification(CocoaUtils.TASK_DONE_NOTIFICATION, delegate);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			NSNotificationCenter.defaultCenter().removeObserver(controller, CocoaUtils.TASK_DONE_NOTIFICATION, delegate);
 			gedcomLoaderJDOM.progressDelegate = null;
 		}
 //		return gedcomLoaderJDOM;
 	}
 	
 	public void loadXMLFile(File file) {
 	if (file == null) {
 		throw new IllegalArgumentException("Cannot load XML file because file is null.");
 	}
 	try {
 		loadXMLData(new NSData(file));
 	} catch (RuntimeException e) {
 		e.printStackTrace();
 		throw e;
 	}
 }
 
 	// uses parser from Kay (gedml)
 	public void loadXMLData(NSData data) {
 		try {
 			log.debug(Thread.currentThread()+":loadXMLData:"+data);
 			long start = System.currentTimeMillis();
 			//      Document doc = XMLTest.parseGedcom(file);
 //			Document newDoc = XMLTest.docParsedWithKay(file);
 //			try {
 //				System.out.println("parserclass:"+Class.forName("gedml.GedcomParser"));
 //				System.out.println("newinstance:"+XMLReaderFactory.createXMLReader("gedml.GedcomParser"));
 //				
 //				System.out.println("classloader:"+Thread.currentThread().getContextClassLoader());
 ////				System.getProperties().list(System.out);
 //				
 //			} catch (ClassNotFoundException e3) {
 //				// TODO Auto-generated catch block
 //				e3.printStackTrace();
 //			} catch (SAXException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 			try {
 				newDoc = builder.build(new ByteArrayInputStream(data.bytes(0, data.length())));
 			}
 			catch (JDOMException e2) {
 			  log.error("Exception: ", e2);
 			  throw new IllegalArgumentException("Cannot parse GEDCOM data "+data+". Cause:"+e2.getLocalizedMessage());
 			}
 			catch (IOException e1) {
 			  log.error("Exception: ", e1); //To change body of catch statement use Options | File Templates.
 			  throw new IllegalArgumentException("Cannot parse GEDCOM data "+data+". Cause:"+e1.getLocalizedMessage());
 			}
 			long end = System.currentTimeMillis();
 			log.debug("Time to parse: " + (end - start) / 1000D + " seconds.");
 			importXMLData();
 		} catch (RuntimeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw e;
 		}
 	}
 
 		public void importXMLData() {
 			
 			try {
 				if(newDoc == null) {
 					log.error("GedcomLoaderJDOM.importXMLData() failed because newDoc is null");
 					return;
 				}
 				Element root = newDoc.getRootElement();
 			List familyElements = root.getChildren("FAM");
 			List individualElements = root.getChildren("INDI");
 			List multimediaElements = root.getChildren("OBJE");
 			List noteElements = root.getChildren("NOTE");
 			List repositoryElements = root.getChildren("REPO");
 			List sourceElements = root.getChildren("SOUR");
 			List submitterElements = root.getChildren("SUBM");
 			// detach the root from the children nodes so they can be imported into the new document
 			root.detach();
 			maxProgressValue = familyElements.size() + individualElements.size() + multimediaElements.size() + noteElements.size() + repositoryElements.size() + sourceElements.size() + submitterElements.size();
 			status = "Loading data...";
 			notifyDelegateOfStatus();
 			
 //			MAFDocumentJDOM importedDoc = new MAFDocumentJDOM();
 			HeaderJDOM header = new HeaderJDOM(root.getChild("HEAD"), _doc);//importedDoc);
 			try {
 				log.debug("header: " + header);
 				log.debug("Header charset:"+header.getCharacterSet());
 				log.debug("Header charversion:"+header.getCharacterVersionNumber());
 				log.debug("Header copyright:"+header.getCopyright());
 				log.debug("Header destination:"+header.getDestination());
 				log.debug("Header filename:"+header.getFileName());
 				log.debug("Header gedcomform:"+header.getGedcomForm());
 				log.debug("Header gedcomversion:"+header.getGedcomVersion());
 				log.debug("Header language:"+header.getLanguage());
 				log.debug("Header placeformat:"+header.getPlaceFormat());
 				log.debug("Header sourcecorp:"+header.getSourceCorporation());
 				log.debug("Header sourcecorpaddr:"+header.getSourceCorporationAddress());
 				log.debug("Header sourcedata:"+header.getSourceData());
 				log.debug("Header sourcedatacopyright:"+header.getSourceDataCopyright());
 				log.debug("Header sourceid:"+header.getSourceId());
 				log.debug("Header sourcename:"+header.getSourceName());
 				log.debug("Header sourceversion:"+header.getSourceVersion());
 				log.debug("Header sourcedatadate:"+header.getSourceDataDate());
 				log.debug("Header creationdate:"+header.getCreationDate());
 				log.debug("Header note:"+header.getNote());
 				log.debug("Header element:"+header.getElement());
 				log.debug("Header submission:"+header.getSubmission());
 				log.debug("Header submitter:"+header.getSubmitter());
 			} catch (RuntimeException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 //		log.debug("submission record: " + new SubmitterJDOM(root.getChild("SUBN"), this));
 			log.debug("file has:\n\t" + individualElements.size() + " individuals\n\t" + familyElements.size() + " families\n\t" + noteElements.size() +
 					" notes\n\t" + multimediaElements.size() + " multimedia objects\n\t" + sourceElements.size() + " sources\n\t" +
 					repositoryElements.size() + " repositories\n\t" + submitterElements.size() + " submitters\n");
 			
 			boolean oldGedcom = false;
 			if ( ! header.getGedcomVersion().startsWith(Header.GEDCOM_VERSION_55)) {
 				// update old GEDCOM structures to match new GEDCOM structures
 //				updateOldIndividualGEDCOMData(element);
 				oldGedcom = true;
 			}
 //		   Executor executor = new ThreadedExecutor();
 //		     try {
 //		       FutureResult familyList = new FutureResult();
 //		       Runnable command = familyList.setter(new Callable() {
 //		          public Object call() { return getFamilies(); }
 //		       });
 //		       executor.execute(command);
 //		       drawBorders();             // do other things while executing
 //		       drawCaption();
 //		       drawImage((Image)(familyList.get())); // use future
 //		     }
 //		     catch (InterruptedException ex) { return; }
 //		     catch (InvocationTargetException ex) { cleanup(); return; }
 			   
 			
 			boolean debug = false;
 			Runtime rt = Runtime.getRuntime();
 			status = "Loading "+individualElements.size()+" individuals ...";
 			log.debug("------------------------------");
 			log.debug("-------------------Individuals");
 			log.debug("------------------------------");
 			Individual firstIndi = Individual.UNKNOWN;
 			for (Iterator iterator = individualElements.iterator(); iterator.hasNext();) {
 //			type element = (type) iter.next();
 				
 //		}
 //		for (int i = 0; i < individualElements.size(); i++) {
 				Element element = (Element) iterator.next();
 				// this removes this element from it's parent so it can be inserted into the new doc
 				iterator.remove();
 				if (debug) {
 					log.debug("element:" + element.getContent());
 				}
 				if (debug) {
 					log.debug("name:" + element.getChildText("NAME"));
 					//log.debug("bday:" + element.getChild("BIRT").getChildText("DATE"));
 				}
 				Individual indi = new IndividualJDOM(element, _doc);
 				String oldKey = indi.getId();
 				_doc.addIndividual(indi);
 				incrementAndUpdateProgress();
 				String newKey = indi.getId();
 				// ID may change when individual is inserted if it is a duplicate
 				log.debug("indi oldkey="+oldKey+" newkey="+newKey);
 				if (!newKey.equals(oldKey)) {
 					/** @todo change all instances of the old key to new key in import document */
 					try {
 						String ref = "[@REF='" + oldKey + "']";
 //						try {
 //							log.debug("dumping newDoc:"+newDoc);
 //							log.debug("dumping newDoc:"+newDoc.getRootElement());
 //							new XMLOutputter().output(newDoc, System.out);
 //						} catch (IOException e) {
 							// TODO Auto-generated catch block
 //							e.printStackTrace();
 //						}
 						List needsFixing = XPath.selectNodes(newDoc, "//HUSB"+ref+" | //WIFE"+ref+" | //CHIL"+ref+" | //ALIA"+ref);
 						log.debug("needsFixing: " + needsFixing);
 						Iterator iter = needsFixing.iterator();
 						while (iter.hasNext()) {
 							Element item = (Element) iter.next();
 							log.debug("setting REF from " + item.getAttribute("REF") + " to " + newKey);
 							item.setAttribute("REF", newKey);
 						}
 					}
 					catch (JDOMException ex1) {
 						log.error("problem with xpath during duplicate key fixing", ex1);
 					}
 				}
 				if (oldGedcom) {
 					// check for old GEDCOM format for sealings
 					log.debug("Checking old GEDCOM format for old-style sealings");
 					Element indiElement = ((IndividualJDOM) indi).getElement();
 					log.debug("indi elem:"+indiElement);
 					if (indiElement != null) {
 						Element famElement = indiElement.getChild(IndividualJDOM.FAMILY_CHILD_LINK);
 						log.debug("fam elem:"+famElement);
 						if (famElement != null) {
 							Element oldSealing = famElement.getChild("SLGC");
 							log.debug("slgc elem:"+indiElement);
 							if (oldSealing != null) {
 								// has old sealing format at INDI.FAMC.SLGC, move to correct location at INDI.SLGC
 								log.warn("Updating old INDI.FAMC.SLGC to INDI.SLGC:"+oldSealing);
 								oldSealing.detach();
 								indiElement.addContent(oldSealing);
 							}
 						}
 					}
 				}
 				if (indi.getRin() > 0 && indi.getRin() < firstIndi.getRin()) {
 					log.debug("Setting 1st Individual to: " + indi);
 					firstIndi = indi;
 				}
 				if (debug) {
 					log.info("\n *** " + " mem:" + rt.freeMemory() / 1024 + " Kb\n");
 				}
 				if (rt.freeMemory() < 50000) {
 					if (debug) {
 						log.info("gc");
 					}
 					rt.gc();
 				}
 			}
 			log.info("individuals: "+_doc.individuals.size());
 			
 			status = "Loading "+familyElements.size()+" families ...";
 			log.debug("------------------------------");
 			log.debug("----------------------Families");
 			log.debug("------------------------------");
 			for (Iterator iterator = familyElements.iterator(); iterator.hasNext();) {
 				Element element = (Element) iterator.next();
 				// this removes this element from it's parent so it can be inserted into the new doc
 				iterator.remove();
 //		}
 //		for (int i = 0; i < familyElements.size(); i++) {
 //			Element element = (Element) familyElements.get(i);
 				Family fam = new FamilyJDOM(element, _doc);
 				String oldKey = fam.getId();
 				_doc.addFamily(fam);
 				incrementAndUpdateProgress();
 				// ID may change when family is inserted if there is a duplicate
 				String newKey = fam.getId();
 				log.debug("oldkey="+oldKey+" newkey="+newKey);
 				if (!newKey.equals(oldKey)) {
 					/** @todo change all instances of the old key to new key in import document */
 					try {
 						String ref = "[@REF='" + oldKey + "']";
 						List needsFixing = XPath.selectNodes(newDoc, "//FAMC"+ref+" | //FAMS"+ref);
 						log.debug("needsFixing: "+needsFixing);
 						Iterator iter = needsFixing.iterator();
 						while (iter.hasNext()) {
 							Element item = (Element)iter.next();
 							log.debug("setting REF from "+item.getAttribute("REF") + " to "+newKey);
 							item.setAttribute("REF", newKey);
 						}
 					}
 					catch (JDOMException ex1) {
 						log.error("problem with xpath during duplicate key fixing", ex1);
 					}
 				}
 				if (oldGedcom) {
 					// check for old GEDCOM format for sealings in FAM.CHIL.SLGC
 					List oldSealings = Collections.EMPTY_LIST;
 					try {
 						oldSealings = XPath.selectNodes(((FamilyJDOM) fam).getElement(), "//CHIL/SLGC");
 						log.debug("old sealings from xpath:"+oldSealings);
 					} catch (JDOMException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					for (Iterator iter = oldSealings.iterator(); iter.hasNext();) {
 						Element oldSealing = (Element) iter.next();						
 						String childId = oldSealing.getParentElement().getAttributeValue(IndividualJDOM.REF);
 						log.warn("Found old FAM.CHIL.SLGC for child "+childId+":"+oldSealing);
 						// has old sealing format at FAM.CHIL.SLGC, move to correct location at INDI.SLGC
 						if (StringUtils.notEmpty(childId)) {
 							IndividualJDOM indi = _doc.getIndividualJDOM(childId);
 							if (indi != null) {
 								log.warn("Moving old FAM.CHIL.SLGC to correct location at INDI.SLGC for child "+childId+":"+oldSealing);
 								oldSealing.detach();
 								indi.getElement().addContent(oldSealing);
 							}
 						}
 					}
 				}
 
 				if (debug) {
 					log.info("\n *** " + " mem:" + rt.freeMemory() / 1024 + " Kb\n");
 				}
 				if (rt.freeMemory() < 50000) {
 					if (debug) {
 						log.info("gc");
 					}
 					rt.gc();
 				}
 				log.info("families: "+_doc.families.size());			
 			}
 			//      log.debug("setting individuals");
 			//      individuals = new NSMutableDictionary(new NSDictionary(indiMap.values().toArray(), indiMap.keySet().toArray()));
 			//      log.debug("individuals: "+individuals.count()+"("+individuals+")");
 			//        for (Iterator iterator = tempFams.iterator(); iterator.hasNext();) {
 			//            Fam fam = (Fam) iterator.next();
 			//            familyList.add(fam);
 			//        }
 			//        for (Iterator iterator = indiMap.values().iterator(); iterator.hasNext();) {
 			//            Indi indi = (Indi) iterator.next();
 			//            individualList.add(indi);
 			//            surnameList.add(indi.getSurname());
 			//        }
 			log.debug("GLJ.loadXMLFile setindividual:" + firstIndi);
 			//            assignIndividualToButton(firstIndi, individualButton);
 			//            setIndividual(individualButton);
 			_doc.setPrimaryIndividual(firstIndi);
 			//        final List objects = doc.getRootElement().getChildren("OBJE");
 			
 			status = "Loading "+multimediaElements.size()+" multimedia records ...";
 			log.debug("------------------------------");
 			log.debug("--------------------Multimedia");
 			log.debug("------------------------------");
 			for (Iterator iterator = multimediaElements.iterator(); iterator.hasNext();) {
 				Element obje = (Element) iterator.next();
 				// this removes this element from it's parent so it can be inserted into the new doc
 				iterator.remove();
 			
 //		for (int i = 0; i < multimediaElements.size(); i++) {
 //			Element obje = (Element) multimediaElements.get(i);
 				if (obje != null) {
 					_doc.addMultimedia(new MultimediaJDOM(obje, _doc));
 					incrementAndUpdateProgress();
 //			    byte[] raw = Base64.decode(buf.toString());
 //			    log.debug("decoded=" + raw);
 //			    NSImageView nsiv = new NSImageView(new NSRect(0, 0, 100, 100));
 //			    nsiv.setImage(new NSImage(new NSData(raw)));
 //			    individualButton.superview().addSubview(nsiv);
 //			    nsiv.display();
 				}
 			}
 			log.debug("multimedia: " + multimediaElements.size());
 			
 			status = "Loading "+noteElements.size()+" notes ...";
 			log.debug("------------------------------");
 			log.debug("-------------------------Notes");
 			log.debug("------------------------------");
 			for (Iterator iterator = noteElements.iterator(); iterator.hasNext();) {
 				Element element = (Element) iterator.next();
 				// this removes this element from it's parent so it can be inserted into the new doc
 				iterator.remove();
 //		for (int i = 0; i < noteElements.size(); i++) {
 //			Element element = (Element) noteElements.get(i);
 				if (debug) {
 					log.debug("element:" + element.getContent());
 				}
 				Note note = new NoteJDOM(element, _doc);
 				_doc.addNote(note);
 				incrementAndUpdateProgress();
 			}
 			log.debug("notes: " + noteElements.size());
 			
 			status = "Loading "+sourceElements.size()+" sources ...";
 			log.debug("------------------------------");
 			log.debug("-----------------------Sources");
 			log.debug("------------------------------");
 			for (Iterator iterator = sourceElements.iterator(); iterator.hasNext();) {
 				Element element = (Element) iterator.next();
 				// this removes this element from it's parent so it can be inserted into the new doc
 				iterator.remove();
 //		for (int i = 0; i < sourceElements.size(); i++) {
 //			Element element = (Element) sourceElements.get(i);
 				if (debug) {
 					log.debug("element:" + element.getContent());
 				}
 				Source source = new SourceJDOM(element, _doc);
 				_doc.addSource(source);
 				incrementAndUpdateProgress();
 			}
 			log.debug("sources: " + sourceElements.size());
 			
 			status = "Loading "+repositoryElements.size()+" repositories ...";
 			log.debug("------------------------------");
 			log.debug("------------------Repositories");
 			log.debug("------------------------------");
 			for (Iterator iterator = repositoryElements.iterator(); iterator.hasNext();) {
 				Element element = (Element) iterator.next();
 				// this removes this element from it's parent so it can be inserted into the new doc
 				iterator.remove();
 //		for (int i = 0; i < repositoryElements.size(); i++) {
 //			Element element = (Element) repositoryElements.get(i);
 				if (debug) {
 					log.debug("element:" + element.getContent());
 				}
 				Repository repository = new RepositoryJDOM(element, _doc);
 				_doc.addRepository(repository);
 				incrementAndUpdateProgress();
 			}
 			log.debug("repositories: " + repositoryElements.size());
 			
 			status = "Loading "+submitterElements.size()+" submitters ...";
 			log.debug("------------------------------");
 			log.debug("--------------------Submitters");
 			log.debug("------------------------------");
 			for (Iterator iterator = submitterElements.iterator(); iterator.hasNext();) {
 				Element element = (Element) iterator.next();
 				// this removes this element from it's parent so it can be inserted into the new doc
 				iterator.remove();
 //		for (int i = 0; i < submitterElements.size(); i++) {
 //			Element element = (Element) submitterElements.get(i);
 				if (debug) {
 					log.debug("element:" + element.getContent());
 				}
 				Submitter submitter = new SubmitterJDOM(element, _doc);
 				_doc.addSubmitter(submitter);
 				incrementAndUpdateProgress();
 			}	
 			log.debug("submitters: " + submitterElements.size());
 			status = "Finishing loading data ...";
 			notifyDelegateOfStatus();
 		} catch (RuntimeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw e;
 		}
 	}
 	
 	private void notifyDelegateOfStatus() {
 		log.info("progress: "+currentProgressValue + " out of "+maxProgressValue);
 		statusDict.setObjectForKey(new Double(currentProgressValue), "currentValue");
 		statusDict.setObjectForKey(new Double(maxProgressValue), "maxValue");
 		statusDict.setObjectForKey(status+": "+(int)currentProgressValue+" of "+(int)maxProgressValue+" records loaded.", "status");
 		//		NSNotificationCenter.defaultCenter().postNotification(CocoaUtils.UPDATE_PROGRESS_NOTIFICATION, this, statusDict);
 		if (maxProgressValue > 0) {
 			progressDelegate.takeValueForKey(new Integer(0), "indeterminate");
 		}
 		progressDelegate.takeValueForKey(statusDict.valueForKey("currentValue"), "doubleValue");
 		progressDelegate.takeValueForKey(statusDict.valueForKey("maxValue"), "maxValue");
 		progressDelegate.takeValueForKey(statusDict.valueForKey("status"), "status");
 //		log.debug(progressDelegate.valueForKey("stopped"));
 //		log.debug(progressDelegate.valueForKey("stopped").getClass());
 		boolean isStopped = ((Integer) progressDelegate.valueForKey("stopped")).intValue() != 0;
 		if (isStopped) {
 			throw new RuntimeException("Task Cancelled");
 		}
 	}
 
 	private void incrementAndUpdateProgress() {
 		++currentProgressValue;
 		notifyDelegateOfStatus();
 	}
 }
