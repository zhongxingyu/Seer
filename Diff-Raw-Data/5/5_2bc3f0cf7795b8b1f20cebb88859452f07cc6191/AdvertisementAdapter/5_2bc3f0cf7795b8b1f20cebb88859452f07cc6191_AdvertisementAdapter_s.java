 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 CEJUG - Ceará Java Users Group
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.adapter;
 
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.StringTokenizer;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.imageio.ImageIO;
 
 import net.java.dev.cejug.classifieds.business.interfaces.AdvertisementAdapterLocal;
 import net.java.dev.cejug.classifieds.entity.AdvertisementEntity;
 import net.java.dev.cejug.classifieds.entity.AdvertisementKeywordEntity;
 import net.java.dev.cejug.classifieds.entity.AttachmentEntity;
 import net.java.dev.cejug.classifieds.entity.CategoryEntity;
 import net.java.dev.cejug.classifieds.entity.CustomerEntity;
 import net.java.dev.cejug.classifieds.entity.facade.AdvertisementTypeFacadeLocal;
 import net.java.dev.cejug.classifieds.entity.facade.AttachmentFacadeLocal;
 import net.java.dev.cejug.classifieds.entity.facade.CategoryFacadeLocal;
 import net.java.dev.cejug.classifieds.entity.facade.CustomerFacadeLocal;
 import net.java.dev.cejug_classifieds.metadata.attachments.AtavarImage;
 import net.java.dev.cejug_classifieds.metadata.attachments.AvatarImageOrUrl;
 import net.java.dev.cejug_classifieds.metadata.attachments.ObjectFactory;
 import net.java.dev.cejug_classifieds.metadata.business.Advertisement;
 import net.java.dev.cejug_classifieds.metadata.business.Period;
 
 /**
  * TODO: to comment.
  * 
  * @author $Author$
  * @version $Rev$ ($Date$)
  */
 @Stateless
 public class AdvertisementAdapter implements AdvertisementAdapterLocal {
 	private static final String KEYWORDS_SEPARATOR = ";";
 
 	/**
 	 * The persistence facade for Category entities.
 	 */
 	@EJB
 	private transient CategoryFacadeLocal categoryFacade;
 
 	/**
 	 * The persistence facade for Customer entities.
 	 */
 	@EJB
 	private transient CustomerFacadeLocal customerFacade;
 
 	/**
 	 * The persistence facade for Advertisement Type entities.
 	 */
 	@EJB
 	private transient AdvertisementTypeFacadeLocal advTypeFacade;
 
 	/**
 	 * The persistence facade for Attachment entities.
 	 */
 	@EJB
 	private transient AttachmentFacadeLocal attachmentFacade;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @throws IOException
 	 */
 	public AdvertisementEntity toEntity(Advertisement soap)
 			throws IllegalStateException, IllegalArgumentException {
 		AdvertisementEntity entity = new AdvertisementEntity();
 		entity.setCategory(categoryFacade.read(soap.getCategoryId()));
 		long domainId = soap.getCustomer().getDomainId();
 		String customerLogin = soap.getCustomer().getLogin();
 		CustomerEntity customer = customerFacade.findOrCreate(domainId,
 				customerLogin);
 		entity.setCustomer(customer);
 		Period period = soap.getPublishingPeriod();
 		entity.setFinish(period.getFinish());
 		entity.setStart(period.getStart());
 		entity.setId(soap.getEntityId());
 
 		AttachmentEntity attachment = new AttachmentEntity();
 		AvatarImageOrUrl avatar = soap.getAvatarImageOrUrl();
 		if (avatar != null) {
 			// avatar is optional.
 			attachment.setName(avatar.getName());
 			attachment.setDescription(avatar.getDescription());
 			AtavarImage img = avatar.getImage();
 			if (img != null) {
 				attachment.setContentType(img.getContentType());
 			}
 			attachment.setContentType(avatar.getImage().getContentType());
 			attachment.setReference(avatar.getUrl());
 			attachmentFacade.create(attachment);
 			entity.setAvatar(attachment);
 		}
 		// TODO: split the string representation
 		entity.setKeywords(splitKeywords(soap.getKeywords()));
 		switch (soap.getStatus()) {
 		case 1:
 			entity.setState(AdvertisementEntity.AdvertisementStatus.ARCHIVE);
 			break;
 		case 2:
 			entity.setState(AdvertisementEntity.AdvertisementStatus.CANCELED);
 			break;
 		case 3:
 			entity.setState(AdvertisementEntity.AdvertisementStatus.ONLINE);
 			break;
 		}
 		entity.setSummary(soap.getSummary());
 		entity.setText(soap.getText());
 		entity.setTitle(soap.getHeadline());
 		entity.setType(advTypeFacade.read(soap.getTypeId()));
 		return entity;
 	}
 
 	/** {@inheritDoc} */
 	public Advertisement toSoap(AdvertisementEntity entity)
 			throws IllegalStateException, IllegalArgumentException {
 		Advertisement adv = new Advertisement();
 		CategoryEntity category = entity.getCategory();
 		if (category != null) {
 			adv.setCategoryId(category.getId());
 		}
 		CustomerAdapter adapter = new CustomerAdapter();
 		adv.setCustomer(adapter.toSoap(entity.getCustomer()));
 		adv.setEntityId(entity.getId());
 		adv.setHeadline(entity.getTitle());
 		adv.setKeywords(mergeKeywords(entity.getKeywords()));
 		// TODO: adv.setLocale(entity.get)
 		Period period = new Period();
 		period.setStart(entity.getStart());
 		period.setFinish(entity.getFinish());
 		adv.setPublishingPeriod(period);
 		// TODO: adv.setStatus(entity.getState());
 		adv.setSummary(entity.getSummary());
 		adv.setText(entity.getText());
 		adv.setTypeId(entity.getType().getId());
 
 		ObjectFactory attachmentsFactory = new ObjectFactory();
 
 		AttachmentEntity attachment = entity.getAvatar();
 
 		AvatarImageOrUrl avatar = attachmentsFactory.createAvatarImageOrUrl();
 		if (avatar != null) {
 			// Avatar is optional.
 			avatar.setDescription(attachment.getDescription());
 			avatar.setName(attachment.getName());
 			avatar.setUrl(attachment.getReference());
			// AtavarImage avtimg = attachmentsFactory.createAtavarImage();
			// avtimg.setValue(attachment.getContent()); // there is no more
			// BLOBs..
			// avtimg.setContentType(attachment.getContentType());
			// avatar.setImage(avtimg);
 			adv.setAvatarImageOrUrl(avatar);
 		}
 
 		return adv;
 	}
 
 	/**
 	 * Concatenates a collection of keywords in a single String formed by comma
 	 * separated tokens.
 	 * 
 	 * @param keywords
 	 *            the collection of
 	 * @return a single String formed by comma separated tokens.
 	 */
 	private String mergeKeywords(Collection<AdvertisementKeywordEntity> keywords) {
 		StringBuffer keyword = new StringBuffer();
 		for (AdvertisementKeywordEntity key : keywords) {
 			keyword.append(key);
 			keyword.append(KEYWORDS_SEPARATOR);
 		}
 		return keyword.toString();
 	}
 
 	/**
 	 * Splits a String formed by comma separated tokens in a collection of
 	 * advertisement keyword entities.
 	 * 
 	 * @param keywords
 	 *            the comma separated keyword.
 	 * @return the collection of entities.
 	 */
 	private Collection<AdvertisementKeywordEntity> splitKeywords(String keywords) {
 		Collection<AdvertisementKeywordEntity> collection = new ArrayList<AdvertisementKeywordEntity>();
 		StringTokenizer tokenizer = new StringTokenizer(keywords, ";,", false);
 		while (tokenizer.hasMoreTokens()) {
 			collection
 					.add(new AdvertisementKeywordEntity(tokenizer.nextToken()));
 		}
 		return collection;
 	}
 
 	/**
 	 * @see <a
 	 *      href='http://forums.sun.com/thread.jspa?messageID=9470374'>Convertin
 	 *      g a java.awt.Image to byte array. </a>
 	 * @param image
 	 *            an image.
 	 * @return the image bytes.
 	 * @throws IOException
 	 *             I/O general exception.
 	 */
 	public byte[] imageToByteArray(Image image) {
 		MediaTracker tracker = new MediaTracker(new Container());
 		tracker.addImage(image, 0);
 		try {
 			tracker.waitForAll();
 		} catch (InterruptedException e) {
 		}
 		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
 				image.getHeight(null), 1);
 		Graphics gc = bufferedImage.createGraphics();
 		gc.drawImage(image, 0, 0, null);
 
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		try {
 			ImageIO.write(bufferedImage, "jpeg", bos);
 		} catch (IOException e) {
 			throw new IllegalArgumentException(e);
 		}
 		return bos.toByteArray();
 	}
 }
