 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  ******************************************************************************/
 package org.sociotech.communitymashup.source.scaledimages;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.imgscalr.Scalr;
 import org.imgscalr.Scalr.Method;
 import org.imgscalr.Scalr.Mode;
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.application.Source;
 import org.sociotech.communitymashup.data.Content;
 import org.sociotech.communitymashup.data.DataFactory;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.data.Image;
 import org.sociotech.communitymashup.data.InformationObject;
 import org.sociotech.communitymashup.data.Organisation;
 import org.sociotech.communitymashup.data.Person;
 import org.sociotech.communitymashup.data.observer.dataset.DataSetChangeObserver;
 import org.sociotech.communitymashup.data.observer.dataset.DataSetChangedInterface;
 import org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl;
 import org.sociotech.communitymashup.source.scaledimages.meta.ScaledImagesTags;
 import org.sociotech.communitymashup.source.scaledimages.properties.ScaledImagesProperties;
 
 /**
  * Main class of the scaled images source service.
  * 
  * @author Peter Lachenmaier
  */
 public class ScaledImagesSourceService extends SourceServiceFacadeImpl implements
 		DataSetChangedInterface {
 
 	/**
 	 * Indicates if persons should be enriched
 	 */
 	private boolean enrichPersons;
 
 	/**
 	 * Indicates if contents should be enriched
 	 */
 	private boolean enrichContents;
 
 	/**
 	 * Indicates if organisations should be enriched
 	 */
 	private boolean enrichOrganisations;
 	
 	/**
 	 * Observe to react on data set changes
 	 */
 	private DataSetChangeObserver dataSetChangeObserver;
 
 	/**
 	 * MetaTag needed for an image to be processed
 	 */
 	private String neededImageMetaTag;
 
 	/**
 	 * MetaTag needed for an information object to be enriched
 	 */
 	private String neededIOMetaTag;
 
 	/**
 	 * Whether to create scaled images
 	 */
 	private boolean createScaledImages;
 
 	/**
 	 * Whether to write dimensions to orig image
 	 */
 	private boolean writeDimensions;
 
 	/**
 	 * The type of the resulting scaled image
 	 */
 	private String resultImageType;
 	
 	/**
 	 * The system specific slash
 	 */
 	private static final String FILE_SEPERATOR = System.getProperty("file.separator");
 	
 	/**
 	 * Width to scale images to
 	 */
 	private int scaledImageWidth;
 	
 	/**
 	 * Height to scale images to 
 	 */
 	private int scaledImageHeight;
 	
 	/**
 	 * Mode to use for scaling
 	 */
 	private Mode scaleMode;
 
 	/**
 	 * Method (quality) to scale image
 	 */
 	private Method scaleMethod;
 
 	/**
 	 * Reference to the data factory
 	 */
 	private final DataFactory dataFactory = DataFactory.eINSTANCE;
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#initialize
 	 * (org.sociotech.communitymashup.application.Source)
 	 */
 	@Override
 	public boolean initialize(Source configuration) {
 
 		boolean initialized = super.initialize(configuration);
 
 		if (initialized) {
 			
 			enrichPersons = source.isPropertyTrueElseDefault(
 					ScaledImagesProperties.ENRICH_PERSON_IMAGE_PROPERTY,
 					ScaledImagesProperties.ENRICH_PERSON_IMAGE_DEFAULT);
 
 			enrichContents = source.isPropertyTrueElseDefault(
 					ScaledImagesProperties.ENRICH_CONTENT_IMAGE_PROPERTY,
 					ScaledImagesProperties.ENRICH_CONTENT_IMAGE_DEFAULT);
 
 			enrichOrganisations = source.isPropertyTrueElseDefault(
 					ScaledImagesProperties.ENRICH_ORGANISATION_IMAGE_PROPERTY,
 					ScaledImagesProperties.ENRICH_ORGANISATION_IMAGE_DEFAULT);
 
 			createScaledImages = source.isPropertyTrueElseDefault(
 					ScaledImagesProperties.CREATE_SCALED_IMAGE_PROPERTY,
 					ScaledImagesProperties.CREATE_SCALED_IMAGE_DEFAULT);
 			
 			writeDimensions = source.isPropertyTrueElseDefault(
 					ScaledImagesProperties.WRITE_DIMENSIONS_TO_ORIG_IMAGE_PROPERTY,
 					ScaledImagesProperties.WRITE_DIMENSIONS_TO_ORIG_IMAGE_DEFAULT);
 			
 			neededImageMetaTag = source.getPropertyValue(
 					ScaledImagesProperties.PROCESS_ONLY_IMAGES_WITH_METATAG_PROPERTY);
 			
 			neededIOMetaTag = source.getPropertyValue(
 					ScaledImagesProperties.PROCESS_ONLY_WITH_METATAG_PROPERTY);
 
 			// try to parse dimensions
 			// width
 			try {
 				scaledImageWidth = Integer.parseInt(source.getPropertyValueElseDefault(
 															ScaledImagesProperties.SCALED_IMAGE_WIDTH_PROPERTY,
 															ScaledImagesProperties.SCALED_IMAGE_WIDTH_DEFAULT));	
 			} catch (Exception e) {
 				log("Could not parse configured width value (" + e.getMessage() + ")", LogService.LOG_ERROR);
 				initialized &= false;
 			}
 			// height
 			try {
 				scaledImageHeight = Integer.parseInt(source.getPropertyValueElseDefault(
 															ScaledImagesProperties.SCALED_IMAGE_HEIGHT_PROPERTY,
 															ScaledImagesProperties.SCALED_IMAGE_HEIGHT_DEFAULT));	
 			} catch (Exception e) {
 				log("Could not parse configured height value (" + e.getMessage() + ")", LogService.LOG_ERROR);
 				initialized &= false;
 			}
 
 			// get scale mode
 			try {
 				scaleMode = Mode.valueOf(source.getPropertyValueElseDefault(
 												ScaledImagesProperties.SCALE_MODE_PROPERTY,
 												ScaledImagesProperties.SCALE_MODE_DEFAULT));	
 			} catch (Exception e) {
 				log("Could not parse configured scale mode value (" + e.getMessage() + ")", LogService.LOG_ERROR);
 				initialized &= false;
 			}
 			
 			// get scale method
 			try {
 				scaleMethod = Method.valueOf(source.getPropertyValueElseDefault(
 												ScaledImagesProperties.SCALE_METHOD_PROPERTY,
 												ScaledImagesProperties.SCALE_METHOD_DEFAULT));	
 			} catch (Exception e) {
 				log("Could not parse configured scale method value (" + e.getMessage() + ")", LogService.LOG_ERROR);
 				initialized &= false;
 			}
 			
 			// get image type
 			resultImageType = source.getPropertyValueElseDefault(
 										ScaledImagesProperties.IMAGE_TYPE_PROPERTY,
 										ScaledImagesProperties.IMAGE_TYPE_DEFAULT);
 			if(resultImageType == null || resultImageType.isEmpty() || 
 					!(resultImageType.equalsIgnoreCase("jpg") || 
 					  resultImageType.equalsIgnoreCase("png") ||
 					  resultImageType.equalsIgnoreCase("gif"))) {
 				log("Configured image type is not valid", LogService.LOG_ERROR);
 				initialized &= false;
 			}
 		}
 
 		this.setInitialized(initialized);
 		return initialized;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#
 	 * enrichDataSet()
 	 */
 	@Override
 	protected void enrichDataSet() {
 		super.enrichDataSet();
 
 		DataSet dataSet = source.getDataSet();
 
 		if (dataSet == null) {
 			return;
 		}
 
 		
 		// do enrichment
 		// be nice and select types before
 		if(enrichPersons) {
 			List<Person> persons = dataSet.getPersons();
 			for(Person person : persons) {
 				enrichIO(person);
 			}
 		}
 		
 		if(enrichContents) {
 			List<Content> contents = dataSet.getContents();
 			for(Content content : contents) {
 				enrichIO(content);
 			}
 		}
 		
 		if(enrichOrganisations) {
 			List<Organisation> organisations = dataSet.getOrganisations();
 			for(Organisation organisation : organisations) {
 				enrichIO(organisation);
 			}
 		}
 	
 		// add adapter to get informed about new or changed items
 		dataSetChangeObserver = new DataSetChangeObserver(dataSet, this);
 	}
 
 	/**
 	 * Checks if the given information object can be enriched and then enriches it.
 	 * 
 	 * @param io Information object to be enriched.
 	 */
 	private void enrichIO(InformationObject io) {
 		if (!isAllowedToEnrich(io)) {
 			// enrichment switched off or nothing to enrich
 			// -> nothing to do
 			return;
 		}
 		
 		// get images
 		EList<Image> iosImages = io.getImages();
 		
 		// double list cause it will be modified during processing (new scaled images attached)
 		EList<Image> tmpImageList = new BasicEList<Image>(iosImages);
 		
 		// process every single image
 		for(Image image : tmpImageList) {
 			processImage(image, io);
 		}
 	}
 
 	/**
 	 * Checks if the given image is allowed to be processed and the scales it and
 	 * attaches the scaled image to the given information object.
 	 * 
 	 * @param image Image to scale
 	 * @param io Information object to attach the scaled image to
 	 */
 	private void processImage(Image image, InformationObject io) {
 		if(!isAllowedToProcess(image)) {
 			return;
 		}
 	
 		// create source specific ident
 		String sourceIdent = "si_" + source.getIdent() + "_" + image.getIdent();
 		
 		// look if created already before
 		Image oldImage = this.getImageWithSourceIdent(sourceIdent);
 		if(oldImage != null) {
 			// image already exists
 			return;
 		}
 		
 		String origFileUrl = image.getFileUrl();
 		BufferedImage origImage;
 		try {
 			log("Reading image from " + origFileUrl, LogService.LOG_DEBUG);
 			
 			URI localFileURI = new URI(origFileUrl);
 	
 			File imageFile = new File(localFileURI);
 			if(!imageFile.exists() || imageFile.length() == 0) {
 				log("Image " + origFileUrl + " does not exist or is empty and will not be scaled.", LogService.LOG_WARNING);
 				return;
 			}
 			else {
 				origImage = ImageIO.read(imageFile);
 				log("Got image from " + origFileUrl, LogService.LOG_DEBUG);
 			}
 		} catch (Exception e) {
 			this.log("Could not create scaled image of " + origFileUrl + " due to exception (" + e.getMessage() + ") while reading original image.", LogService.LOG_WARNING);
 			return;
 		}
 		
 		if(origImage == null) {
 			this.log("Could not create scaled image of " + origFileUrl + " cause original image could not be read.", LogService.LOG_WARNING);
 			return;
 		}
 		
 		if(writeDimensions) {
 			// write dimension to orig image
 			image.setWidth(origImage.getWidth());
 			image.setHeight(origImage.getHeight());
 		}
 		
 		// stop here if creation of scaled images is turned off
 		if(!createScaledImages) {
 			return;
 		}
 		
 		// Scale image
 		BufferedImage scaledImage = Scalr.resize(origImage, scaleMethod, scaleMode, scaledImageWidth, scaledImageHeight);
 		
 		String tmpFilePath = createTmpFilePath(image, resultImageType);
 		File newImageFile = new File(tmpFilePath);
 		
 		try {
 			log("Writing image to " + newImageFile.getAbsolutePath(), LogService.LOG_DEBUG);
 			ImageIO.write(scaledImage, resultImageType, newImageFile);
 			log("Wrote image.", LogService.LOG_DEBUG);
 		} catch (IOException e) {
 			this.log("Could not write scaled image to " + tmpFilePath + " due to exception (" + e.getMessage() + ").", LogService.LOG_WARNING);
 			return;
 		}
 				
 		// create image object
 		Image attachedImage = dataFactory.createImage();
 		
 		// set the url
 		attachedImage.setFileUrl("file://" + tmpFilePath);
 		
 		// set known file type
 		attachedImage.setFileExtension(resultImageType);
 		
 		// new image is only valid in the cache -> leads to direct copy
 		attachedImage.setCachedOnly(Boolean.TRUE);
 		
 		// set dimensions
 		attachedImage.setWidth(scaledImage.getWidth());
 		attachedImage.setHeight(scaledImage.getHeight());
 	
 		// add it to mark it from this source
 		attachedImage = this.add(attachedImage, sourceIdent);
 		
 		if(attachedImage == null) {
 			log("Scaled image " + tmpFilePath + " could not be added to data set", LogService.LOG_WARNING);
 			return;
 		}
 		
 		// attach it to io
 		io.getImages().add(attachedImage);
 		
 		// delete scaled image after orig is deleted
 		attachedImage.deleteOnDeleteOf(image);
 		
 		// add meta tag
 		attachedImage.metaTag(ScaledImagesTags.SCALEDIMAGES_METATAG);
 		
 		// TODO add specific meta tags like square etc.
 		
 		// write debug log message
 		log("Scaled image " + image.getIdent() + " of " + io.getIdent(), LogService.LOG_DEBUG);
 	}	
 
 	/**
 	 * Creates a unique tmp file path for a new image in the system temporary directory.
 	 *  
 	 * @param image Image to create tmp file path for.
 	 * @param fileExtension Type extension of the new file
 	 * @return The temporary file path.
 	 */
 	private String createTmpFilePath(Image image, String fileType) {
 		// get system tmp directory
 		String tmpDir = System.getProperty("java.io.tmpdir");
 		
 		// unique tmp name
 		String tmpName = "tmpsi_" + source.getIdent() + "_" + image.getFileUrl().hashCode() + "_" + System.currentTimeMillis() + "." + fileType;
 		
 		if(!tmpDir.endsWith(FILE_SEPERATOR)) {
 			tmpDir += FILE_SEPERATOR;
 		}
 		
 		return tmpDir + tmpName;
 	}
 
 	/**
 	 * Checks if the given image can and should be processed.
 	 * 
 	 * @param image Image to be checked
 	 * 
 	 * @return True if allowed, false otherwise
 	 */
 	private boolean isAllowedToProcess(Image image) {
 		if(image == null) {
 			return false;
 		}
 		
 		if(isItemOfThisSource(image)) {
 			// do not process own images
 			return false;
 		}
 		
 		// TODO skipMetaTag
 		
 		// check needed meta tag for image
 		if (neededImageMetaTag != null && !neededImageMetaTag.isEmpty() && !image.hasMetaTag(neededImageMetaTag)) {
 			// image does not have needed meta tag
 			return false;
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Checks if enrichment based on the given io is allowed.
 	 * 
 	 * @param io
 	 *            Information object for enrichment
 	 * @return True if enrichment allowed and possible, false otherwise.
 	 */
 	private boolean isAllowedToEnrich(InformationObject io) {
 		if (io == null) {
 			return false;
 		}
 		
 		// type checks
 		if((io instanceof Content) && !enrichContents) {
 			return false;
 		}
 		
 		if((io instanceof Organisation) && !enrichOrganisations) {
 			return false;
 		}
 	
 		if((io instanceof Person) && !enrichPersons) {
 			return false;
 		}
 		
 		EList<Image> iosImages = io.getImages();
 		if (iosImages == null || iosImages.isEmpty()) {
 			// nothing to enrich
 			return false;
 		}
 
 		if (neededIOMetaTag != null && !neededIOMetaTag.isEmpty() && !io.hasMetaTag(neededIOMetaTag)) {
 			// io does not have needed meta tag
 			return false;
 		}
 		
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.sociotech.communitymashup.data.observer.dataset.DataSetChangedInterface
 	 * #dataSetChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	@Override
 	public void dataSetChanged(Notification notification) {
 		if(notification == null)
 		{
 			return;
 		}
 		
		// new information object added to the data set
 		if(notification.getEventType() == Notification.ADD && notification.getNotifier() instanceof DataSet && notification.getNewValue() instanceof InformationObject)
 		{
 			InformationObject newIO = (InformationObject) notification.getNewValue();
 			// enrich new information object
 			enrichIO(newIO);
 		}
		// information object got new image
 		else if(notification.getEventType() == Notification.ADD && notification.getNotifier() instanceof InformationObject && notification.getNewValue() instanceof Image)
 		{
 			// attached image to information object
 			InformationObject changedIO = (InformationObject) notification.getNotifier();
 			
 			if(isAllowedToEnrich(changedIO)) {
 				// get attached image
 				Image attachedImage = (Image) notification.getNewValue();
 				
 				// process image
 				processImage(attachedImage, changedIO);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#stop()
 	 */
 	@Override
 	protected void stop() {
 		// disconnect data set observer
 		if (dataSetChangeObserver != null) {
 			dataSetChangeObserver.disconnect();
 		}
 
 		super.stop();
 	}
 
 }
