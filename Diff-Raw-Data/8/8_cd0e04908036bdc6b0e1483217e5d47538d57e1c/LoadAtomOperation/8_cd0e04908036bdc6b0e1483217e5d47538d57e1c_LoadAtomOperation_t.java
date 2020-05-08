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
 package net.java.dev.cejug.classifieds.service.endpoint.impl;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.xml.ws.WebServiceException;
 
 import net.java.dev.cejug.classifieds.business.interfaces.LoadAtomOperationLocal;
 import net.java.dev.cejug.classifieds.entity.AdvertisementEntity;
 import net.java.dev.cejug.classifieds.entity.CategoryEntity;
 import net.java.dev.cejug.classifieds.entity.facade.AdvertisementFacadeLocal;
 import net.java.dev.cejug.classifieds.entity.facade.CategoryFacadeLocal;
 import net.java.dev.cejug_classifieds.metadata.business.SyndicationFilter;
 
 import org.w3._2005.atom.DateTimeType;
 import org.w3._2005.atom.EntryType;
 import org.w3._2005.atom.Feed;
 import org.w3._2005.atom.GeneratorType;
 import org.w3._2005.atom.IdType;
 import org.w3._2005.atom.LinkType;
 import org.w3._2005.atom.LogoType;
 import org.w3._2005.atom.ObjectFactory;
 import org.w3._2005.atom.PersonType;
 import org.w3._2005.atom.TextType;
 import org.w3._2005.atom.UriType;
 
 /**
  * TODO: to comment.
  * 
  * @author $Author$
  * @version $Rev$ ($Date$)
  */
 @Stateless
 public class LoadAtomOperation implements LoadAtomOperationLocal {
 	/**
 	 * Persistence façade of Advertisement entities.
 	 */
 	@EJB
 	private transient AdvertisementFacadeLocal advFacade;
 
 	@EJB
 	private transient CategoryFacadeLocal catFacade;
 
 	/**
 	 * the global log manager, used to allow third party services to override
 	 * the default logger.
 	 */
 	private static final Logger logger = Logger.getLogger(
 			LoadAtomOperation.class.getName(), "i18n/log");
 
 	public Feed loadAtomOperation(SyndicationFilter filter) {
 		try {
 			ObjectFactory factory = new ObjectFactory();
 			Feed atomFeed = factory.createFeed();
 
 			List<Object> feedAttributes = atomFeed
 					.getAuthorOrCategoryOrContributor();
 
 			// author.
 			PersonType author = factory.createPersonType();
 			List<Object> authorAttributes = author.getNameOrUriOrEmail();
 			authorAttributes.add(factory
 					.createPersonTypeName("Cejug-Classifieds"));
 			authorAttributes
 					.add(factory
 							.createPersonTypeEmail("dev@cejug-classifieds.dev.java.net"));
 			UriType uri = factory.createUriType();
 			uri.setValue("https://cejug-classifieds.dev.java.net/");
 			authorAttributes.add(factory.createPersonTypeUri(uri));
 			feedAttributes.add(factory.createFeedAuthor(author));
 			// Title
 			TextType title = factory.createTextType();
 			title.setType("text");
 
 			// Generator
 			GeneratorType generator = factory.createGeneratorType();
 			generator.setUri("https://cejug-classifieds.dev.java.net/");
 			generator.setValue("Cejug-Classifieds");
 			generator.setVersion("$Revision$");
 			feedAttributes.add(factory.createFeedGenerator(generator));
 			feedAttributes.add(factory.createFeedContributor(author));
 			LogoType logo = factory.createLogoType();
 			logo
 					.setValue("https://cejug-classifieds.dev.java.net/images/logo.jpg");
 			feedAttributes.add(factory.createFeedLogo(logo));
 			CategoryEntity category = catFacade.read(filter.getCategoryId());
 			IdType id = factory.createIdType();
 			id.setValue("cejug-classifieds hard code test. TODO: real data...");
 			feedAttributes.add(factory.createEntryTypeId(id));
 			if (category == null) {
 				long categoryId = filter.getCategoryId();
 				if (categoryId < 1) {
 					title.getContent().add("Missed category identifier.");
 				} else {
 					title.getContent().add(
 							"Category #" + categoryId + " does not exist.");
 				}
 				feedAttributes.add(factory.createFeedTitle(title));
 				TextType subtitle = factory.createTextType();
 				subtitle.setType("text");
 				subtitle
 						.getContent()
 						.add(
 								"Please check the category id or read our documentation.");
 				feedAttributes.add(factory.createFeedSubtitle(subtitle));
 				atomFeed.getAuthorOrCategoryOrContributor().add(
 						factory.createFeedEntry(factory.createEntryType()));
 			} else {
 				title.getContent().add(category.getName());
 				feedAttributes.add(factory.createFeedTitle(title));
 
 				TextType subtitle = factory.createTextType();
 				subtitle.setType("text");
 
 				subtitle.getContent().add(category.getDescription());
 				feedAttributes.add(factory.createFeedSubtitle(subtitle));
 
 				// TODO: converter filter in a map of parameters...
 				List<AdvertisementEntity> result = advFacade
 						.readByCategory(filter.getCategoryId());
 
 				for (AdvertisementEntity adv : result) {
 					/*
 					 * AttachmentEntity avatar = adv.getAvatar();
 					 * 
 					 * 
 					 * if (avatar != null) { byte[] img = avatar.getContent();
 					 * if (img != null && img.length > 0) { } }
 					 */
 					EntryType entry = factory.createEntryType();
 					List<Object> entryAttributes = entry
 							.getAuthorOrCategoryOrContent();
 
 					IdType entryId = factory.createIdType();
 					entryId.setValue("ADV" + adv.getId());
 					entryAttributes.add(factory.createEntryTypeId(entryId));
 
 					DateTimeType dt = factory.createDateTimeType();
 					dt.setValue(Calendar.getInstance());
 					entryAttributes.add(factory.createEntryTypeUpdated(dt));
 					entryAttributes.add(factory.createEntryTypePublished(dt));
 
 					TextType entryTitle = factory.createTextType();
 					entryTitle.setType("html");
 					entryTitle.getContent().add(adv.getTitle());
 					entryAttributes.add(factory
 							.createEntryTypeTitle(entryTitle));
 
 					TextType entrySummary = factory.createTextType();
 					entrySummary.setType("html");
 					entrySummary
 							.getContent()
 							.add(
									"<p><a href='http://devoxx.com/display/JV08/Home'><img src='"
 											+ adv.getAvatar().getReference()
											+ "' alt='devoxx.com' style='float:left;'></a>"
 											+ adv.getSummary() + "</p>");
 
 					entryAttributes.add(factory
 							.createEntryTypeSummary(entrySummary));
 					/*
 					 * byte[] avatar = adv.getAvatar().getContent(); if (avatar
 					 * != null && avatar.length > 0) { IconType icon =
 					 * factory.createIconType(); icon.setValue(new
 					 * String(adv.getAvatar().getContent()));
 					 * entryAttributes.add(factory.createFeedIcon(icon)); }
 					 */
 					LinkType link = factory.createLinkType();
 					link
							.setHref("http://devoxx.com/display/JV08/Home");
 					link.setRel("alternate");
 					// TODO: get from advertisement or domain or category
 					link.setHreflang("en");
 					link.setTitle(adv.getTitle());
 					link.setType("text");
 					entryAttributes.add(factory.createEntryTypeLink(link));
 
 					atomFeed.getAuthorOrCategoryOrContributor().add(
 							factory.createFeedEntry(entry));
 				}
 			}
 			return atomFeed;
 		} catch (Exception e) {
 			// TODO: log
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 	}
 }
