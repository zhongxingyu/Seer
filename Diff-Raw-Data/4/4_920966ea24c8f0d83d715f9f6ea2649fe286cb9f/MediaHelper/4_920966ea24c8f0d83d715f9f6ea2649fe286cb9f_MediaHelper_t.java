 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.samples.mediacast;
 
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.Formatter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.imageio.ImageIO;
 
 
 /**
  * Helper class for processing and handling media files, mostly handler code best
  * kept out of the controller and model beans.
  *
  */
 @ManagedBean(eager=true)
 @ApplicationScoped
 public class MediaHelper implements Serializable{
 	
 	private static final Logger logger = Logger.getLogger(MediaHelper.class
 			.toString());
 	
 	private String videoConvertCommand;
 	private String audioConvertCommand;
 	private String thumbConvertCommand;
 	
 	private Media soundIcon;
 	private Media movieIcon;
 	private Media soundIconSmall;
 	private Media movieIconSmall;
 	
 	
 	@PostConstruct
 	public void init(){
 		videoConvertCommand = FacesContext.getCurrentInstance()
 				.getExternalContext()
 				.getInitParameter("org.icemobile.videoConvertCommand");
 
 		audioConvertCommand = FacesContext.getCurrentInstance()
 				.getExternalContext()
 				.getInitParameter("org.icemobile.audioConvertCommand");
 
 		thumbConvertCommand = FacesContext.getCurrentInstance()
 				.getExternalContext()
 				.getInitParameter("org.icemobile.thumbnailCommand");
 
 		logger.fine("video convert command: " + videoConvertCommand);
 		logger.fine("audio convert command: " + audioConvertCommand);
 		logger.fine("thumbnail convert command: " + thumbConvertCommand);
 		
 		/**
 		 * Video and Audio files don't have default thumbnail icons for preview
 		 * so we load the following thumbnails.
 		 */
 		BufferedImage image;
 		InputStream imageStream;
 		ExternalContext externalContext = FacesContext.getCurrentInstance()
 				.getExternalContext();
 		try {
 			imageStream = externalContext
 					.getResourceAsStream("/resources/images/soundIcon.png");
 			image = ImageIO.read(imageStream);
 			soundIcon = createPhoto(image, image.getWidth(), image.getHeight());
 
 			imageStream = externalContext
 					.getResourceAsStream("/resources/images/movieIcon.png");
 			image = ImageIO.read(imageStream);
 			movieIcon = createPhoto(image, image.getWidth(), image.getHeight());
 
 			imageStream = externalContext
 					.getResourceAsStream("/resources/images/soundIconSmall.png");
 			image = ImageIO.read(imageStream);
 			soundIconSmall = createPhoto(image, image.getWidth(),
 					image.getHeight());
 
 			imageStream = externalContext
 					.getResourceAsStream("/resources/images/movieIconSmall.png");
 			image = ImageIO.read(imageStream);
 			movieIconSmall = createPhoto(image, image.getWidth(),
 					image.getHeight());
 
 		} catch (IOException e) {
 			logger.log(Level.WARNING,
 					"Error loading audio and video thumbnails.", e);
 		}
 	}
 	
 	
 	public void processUploadedAudio(UploadModel model, MediaStore store) {
 		if (model.getAudioFile() == null) {
 			return;
 		}
 		try {
 			if (null != audioConvertCommand) {
 				File converted = processFile(model.getAudioFile(),
 						audioConvertCommand, ".m4a");
 				File audioDir = model.getAudioFile().getParentFile();
 				File newAudio = new File(audioDir, converted.getName());
 				model.getAudioFile().delete();
 				converted.renameTo(newAudio);
 				model.setAudioFile(newAudio);
 			}
 			model.getCurrentMediaMessage().setAudioMedia(
 					createMedia("audio/mp4", model.getAudioFile()));
 		} catch (Exception e) {
 			// conversion fails, but we may proceed with original file
 			logger.log(Level.WARNING, "Error processing audio.", e);
 		}
 	}
 	
 	private File processFile(File inputFile, String commandTemplate,
 			String outputExtension) {
 		StringBuilder command = new StringBuilder();
 		try {
 			File converted = File.createTempFile("out", outputExtension);
 			Formatter formatter = new Formatter(command);
 			formatter.format(commandTemplate, inputFile.getAbsolutePath(),
 					converted.getAbsolutePath());
 			Process process = Runtime.getRuntime().exec(command.toString());
 			int exitValue = process.waitFor();
 			if (0 != exitValue) {
 				logger.log(Level.WARNING, "Transcoding failure: " + command);
 				StringBuilder errorString = new StringBuilder();
 				InputStream errorStream = process.getErrorStream();
 				byte[] buf = new byte[1000];
 				int len = -1;
 				while ((len = errorStream.read(buf)) > 0) {
 					errorString.append(new String(buf, 0, len));
 				}
 				logger.log(Level.WARNING, errorString.toString());
 			}
 			return converted;
 		} catch (Exception e) {
 			// conversion fails, but we may proceed with original file
 			logger.log(Level.WARNING, command + " Error processing file.", e);
 		}
 		return null;
 	}
 	
 	public void processUploadedVideo(UploadModel model, MediaStore store) {
 
 		if (model.getVideoFile() == null) {
 			return;
 		}
 
 		Media customMovieIcon = movieIcon;
 		Media customMovieIconSmall = movieIconSmall;
 
 		try {
 			// check to see if we can process the video file into an mp4 format
 			// this is runtime config controlled in the web.xml
 			if (null != videoConvertCommand) {
 				File converted = processFile(model.getVideoFile(),
 						videoConvertCommand, ".mp4");
 				File videoDir = model.getVideoFile().getParentFile();
 				File newVideo = new File(videoDir, converted.getName());
 				model.getVideoFile().delete();
 				converted.renameTo(newVideo);
 				model.setVideoFile(newVideo);
 			}
 
 			// check if a thumb nail can be generated for the uploaded video
 			// if so show it.
 			if (null != thumbConvertCommand) {
 				File thumbImage = processFile(model.getVideoFile(),
 						thumbConvertCommand, ".jpg");
 				customMovieIcon = createPhoto(thumbImage);
 				customMovieIconSmall = createPhoto(thumbImage);
                 customMovieIconSmall.setWidth(movieIconSmall.getWidth());
                 customMovieIconSmall.setHeight(movieIconSmall.getHeight());
 				thumbImage.delete();
 			}
 
 			model.getCurrentMediaMessage().setVideoMedia(
 					createMedia("video/mp4", model.getVideoFile()));
 			model.getCurrentMediaMessage()
 					.setVideoThumbnailMed(customMovieIcon);
 			model.getCurrentMediaMessage()
					.setVideoThumbnailSmall(customMovieIconSmall);
 		} catch (Exception e) {
 			// conversion fails, but we may proceed with original file
 			logger.log(Level.WARNING, "Error processing video.", e);
 		}
 	}
 
 	public void processUploadedImage(UploadModel model, MediaStore store) {
 
 		if (model.getCameraFile() == null) {
 			return;
 		}
 		try {
 			BufferedImage image = ImageIO.read(model.getCameraFile());
 			// scale the original file into a small thumbNail and the other
 			// into a 1 megapixelish sized image.
 			int width = image.getWidth();
 			int height = image.getHeight();
 
 			// create the thumbnail
 			AffineTransform tx = new AffineTransform();
 			double imageScale = calculateThumbNailSize(width, height);
 			tx.scale(imageScale, imageScale);
 			AffineTransformOp op = new AffineTransformOp(tx,
 					AffineTransformOp.TYPE_BILINEAR);
 			BufferedImage thumbNailImage = op.filter(image, null);
 
 			// create the small thumbnail.
 			imageScale = calculateSmThumbNailSize(width, height);
 			tx = new AffineTransform();
 			tx.scale(imageScale, imageScale);
 			op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
 			BufferedImage smThumbNailImage = op.filter(image, null);
 
 			// create the smaller image.
 			imageScale = calculateSmallImageSize(width, height);
 			tx = new AffineTransform();
 			tx.scale(imageScale, imageScale);
 			op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
 			BufferedImage scaledImage = op.filter(image, null);
 
 			// clean up the original image.
 			image.flush();
 
 			model.getCurrentMediaMessage().addLargePhoto(
 					createPhoto(scaledImage, scaledImage.getTileWidth(),
 							scaledImage.getHeight()));
 			model.getCurrentMediaMessage().addSmallPhoto(
 					createPhoto(smThumbNailImage,
 							smThumbNailImage.getTileWidth(),
 							smThumbNailImage.getHeight()));
 			model.getCurrentMediaMessage().addMediumPhoto(
 					createPhoto(thumbNailImage, thumbNailImage.getTileWidth(),
 							thumbNailImage.getHeight()));
 			model.getCurrentMediaMessage().setPhotoFile(
 					model.getCameraFile());
 
 		} catch (Throwable e) {
 			logger.log(Level.WARNING, "Error processing camera image upload.",
 					e);
 		}
 	}
 
 	private Media createPhoto(File imageFile) throws IOException {
 		BufferedImage image = ImageIO.read(imageFile);
 		return createPhoto(image, image.getWidth(), image.getHeight());
 	}
 
 	private Media createPhoto(BufferedImage image, int width, int height)
 			throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ImageIO.write(image, "png", baos);
 		byte[] fileContent = baos.toByteArray();
 		baos.close();
 		return new Media(fileContent, width, height);
 	}
 
 	private Media createMedia(String contentType, File mediaFile)
 			throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		InputStream in = new FileInputStream(mediaFile);
 		byte[] buf = new byte[1000];
 		int l = 1;
 		while (l > 0) {
 			l = in.read(buf);
 			if (l > 0) {
 				baos.write(buf, 0, l);
 			}
 		}
 		byte[] fileContent = baos.toByteArray();
 		baos.close();
 		return new Media(fileContent, contentType, 0, 0);
 	}
 	
 	/**
 	 * Utility to scale the image to a rough size of 96x96 pixels but still
 	 * maintaining the original aspect ratio.
 	 * 
 	 * @param width
 	 *            original width of image
 	 * @param height
 	 *            original height of image
 	 * @return scale factor to achieve "thumbnail" size.
 	 */
 	private double calculateThumbNailSize(int width, int height) {
 		double thumbSize = 96.0;
 		return calculateImageSize(thumbSize, width, height);
 	}
 	
 	/**
 	 * Utility to scale the image to a rough size of 96x96 pixels but still
 	 * maintaining the original aspect ratio.
 	 * 
 	 * @param width
 	 *            original width of image
 	 * @param height
 	 *            original height of image
 	 * @return scale factor to achieve "thumbnail" size.
 	 */
 	private double calculateSmallImageSize(int width, int height) {
 		double thumbSize = 320; // 320 x 480
 		return calculateImageSize(thumbSize, width, height);
 	}
 
 	// utility to scale image to desired size.
 	private double calculateImageSize(double intendedSize, int width, int height) {
 		double scaleHeight = height / intendedSize;
 		// change the algorithm, so height is always the same
 		return 1 / scaleHeight;
 	}
 	
 	/**
 	 * Utility to scale the image to a rough size of 96x96 pixels but still
 	 * maintaining the original aspect ratio.
 	 * 
 	 * @param width
 	 *            original width of image
 	 * @param height
 	 *            original height of image
 	 * @return scale factor to achieve "thumbnail" size.
 	 */
 	private double calculateSmThumbNailSize(int width, int height) {
 		double thumbSize = 16.0;
 		return calculateImageSize(thumbSize, width, height);
 	}
 
 
 	public Media getSoundIcon() {
 		return soundIcon;
 	}
 
 
 	public Media getMovieIcon() {
 		return movieIcon;
 	}
 
 
 	public Media getSoundIconSmall() {
 		return soundIconSmall;
 	}
 
 
 	public Media getMovieIconSmall() {
 		return movieIconSmall;
 	}
 
 
 }
