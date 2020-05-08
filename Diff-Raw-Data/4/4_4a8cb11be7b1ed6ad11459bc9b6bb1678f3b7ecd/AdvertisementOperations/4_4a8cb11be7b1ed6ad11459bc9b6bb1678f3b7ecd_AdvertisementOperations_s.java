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
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.xml.ws.WebServiceException;
 
 import net.java.dev.cejug.classifieds.adapter.SoapOrmAdapter;
 import net.java.dev.cejug.classifieds.business.interfaces.AdvertisementAdapterLocal;
 import net.java.dev.cejug.classifieds.business.interfaces.AdvertisementOperationsLocal;
 import net.java.dev.cejug.classifieds.entity.AdvertisementEntity;
 import net.java.dev.cejug.classifieds.entity.facade.AdvertisementFacadeLocal;
 import net.java.dev.cejug.classifieds.entity.facade.EntityFacade;
 import net.java.dev.cejug.classifieds.exception.RepositoryAccessException;
 import net.java.dev.cejug_classifieds.metadata.attachments.AtavarImage;
 import net.java.dev.cejug_classifieds.metadata.attachments.AvatarImageOrUrl;
 import net.java.dev.cejug_classifieds.metadata.business.Advertisement;
 import net.java.dev.cejug_classifieds.metadata.business.AdvertisementCollection;
 import net.java.dev.cejug_classifieds.metadata.business.AdvertisementCollectionFilter;
 import net.java.dev.cejug_classifieds.metadata.business.PublishingHeader;
 import net.java.dev.cejug_classifieds.metadata.common.Customer;
 
 /**
  * TODO: to comment.
  * 
  * @author $Author$
  * @version $Rev$ ($Date$)
  */
 @Stateless
 public class AdvertisementOperations extends
 		AbstractCrudImpl<AdvertisementEntity, Advertisement> implements
 		AdvertisementOperationsLocal {
 
 	/**
 	 * Persistence façade of Advertisement entities.
 	 */
 	@EJB
 	private transient AdvertisementFacadeLocal advFacade;
 
 	@EJB
 	private transient AdvertisementAdapterLocal advAdapter;
 
 	/**
 	 * the global log manager, used to allow third party services to override
 	 * the default logger.
 	 */
 	private static final Logger logger = Logger.getLogger(
 			AdvertisementOperations.class.getName(), "i18n/log");
 
 	@Override
 	protected SoapOrmAdapter<Advertisement, AdvertisementEntity> getAdapter() {
 		return advAdapter;
 	}
 
 	@Override
 	protected EntityFacade<AdvertisementEntity> getFacade() {
 
 		return advFacade;
 	}
 
 	public AdvertisementCollection loadAdvertisementOperation(
 			final AdvertisementCollectionFilter filter) {
 
 		// TODO: load advertisements from timetable.... filtering with periods,
 		// etc..
 
 		try {
 			AdvertisementCollection collection = new AdvertisementCollection();
 			List<AdvertisementEntity> entities = advFacade.readByCategory(Long
 					.parseLong(filter.getCategory()));
 			for (AdvertisementEntity entity : entities) {
 				collection.getAdvertisement().add(advAdapter.toSoap(entity));
 			}
 			return collection;
 		} catch (Exception e) {
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 	}
 
 	public Advertisement publishOperation(final Advertisement advertisement,
 			final PublishingHeader header) {
 
 		// TODO: to implement the real code.
 		try {
 			// TODO: re-think a factory to reuse adapters...
 			Customer customer = new Customer();
 			customer.setLogin(header.getCustomerLogin());
 			customer.setDomainId(header.getCustomerDomainId());
 			advertisement.setCustomer(customer);
 			AvatarImageOrUrl avatar = advertisement.getAvatarImageOrUrl();
 			AtavarImage img = null;
 
 			/*
 			 * if (avatar.getGravatarEmail() != null) { avatar
 			 * .setUrl("http://www.gravatar.com/avatar/" +
 			 * hashGravatarEmail(avatar.getGravatarEmail()) + ".jpg"); } else if
 			 * (avatar.getUrl() != null) { img = avatar.getImage(); AtavarImage
 			 * temp = new AtavarImage();
 			 * temp.setContentType(img.getContentType()); temp.setValue(null);
 			 * avatar.setImage(temp); }
 			 */
 			String[] fakeAvatar = { "767fc9c115a1b989744c755db47feb60",
 					"5915fd742d0c26f6a584f9d21f991b9c",
 					"f4510afa5a1ceb6ae7c058c25051aed9",
 					"84987b436214f52ec0b04cd1f8a73c3c",
 					"bb29d699b5cba218c313b61aa82249da",
 					"b0b357b291ac72bc7da81b4d74430fe6",
 					"d212b7b6c54f0ccb2c848d23440b33ba",
 					"1a33e7a69df4f675fcd799edca088ac2",
 					"992df4737c71df3189eed335a98fa0c0",
 					"a558f2098da8edf67d9a673739d18aff",
 					"8379aabc84ecee06f48d8ca48e09eef4",
 					"4d346581a3340e32cf93703c9ce46bd4",
 					hashGravatarEmail("tusharvjoshi@gmail.com"),
 					hashGravatarEmail("fgaucho@gmail.com"),
 					hashGravatarEmail("arissonleal@gmail.com"),
 					"eed0e8d62f562daf038f182de7f1fd42",
 					"7acb05d25c22cbc0942a5e3de59392bb",
 					"df126b735a54ed95b4f4cc346b786843",
 					"aac7e0386facd070f6d4b817c257a958",
 					"c19b763b0577beb2e0032812e18e567d",
 					"06005cd2700c136d09e71838645d36ff" };
 
 			avatar
 					.setUrl("http://www.gravatar.com/avatar/"
 							// +
 							// hashGravatarEmail((Math.random()>.5?"tusharvjoshi@gmail.com":"fgaucho@gmail.com"))
 							+ fakeAvatar[(int) (Math.random()
 									* fakeAvatar.length - 0.0000000000001d)]
 							+ ".jpg");
 			/*
 			 * Copy resources to the content repository - the file system. try {
 			 * copyResourcesToRepository(advertisement); } catch (Exception e) {
 			 * e.printStackTrace(); }
 			 */
 
 			AdvertisementEntity entity = advAdapter.toEntity(advertisement);
 			advFacade.create(entity);
 			if (img != null) {
 				String reference = copyResourcesToRepository(avatar.getName(),
 						img.getValue(), entity.getId(), header
 								.getCustomerDomainId());
 				entity.getAvatar().setReference(reference);
 				advFacade.update(entity);
			}
 			logger.finest("Advertisement #" + entity.getId() + " published ("
 					+ entity.getTitle() + ")");
 			return advAdapter.toSoap(entity);
 		} catch (Exception e) {
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 
 	}
 
 	/**
 	 * A customer can submit a resource URL or a resource content, more common
 	 * in case of images (PNG/JPG). In this case, the server first store the
 	 * image contents in the file system and refer its location in the database.
 	 * This method receives an Avatar object, check it size and store it
 	 * contents in the file system.
 	 * 
 	 * @param l
 	 * @param advertisement
 	 *            the advertisement containing attachments.
 	 * @return the resource address.
 	 * @throws RepositoryAccessException
 	 *             problems accessing the content repository.
 	 */
 	private String copyResourcesToRepository(String name, byte[] contents,
 			long customerId, long domainId) throws RepositoryAccessException {
 
 		if (contents == null || contents.length < 1) {
 			return null;
 		} else if (customerId < 1 || domainId < 1) {
 			throw new RepositoryAccessException("Unaccepted ID (customer id:"
 					+ customerId + ", domain: " + domainId);
 		} else {
 			String glassfishHome = System.getenv("AS_HOME");
 			String path = glassfishHome
 					+ "/domains/domain1/applications/j2ee-apps/cejug-classifieds-server/cejug-classifieds-server_war/resource/"
 					+ domainId + '/' + customerId;
 
 			String file = path + "/" + name;
 			File pathF = new File(path);
 			File fileF = new File(file);
 
 			try {
 				if (pathF.mkdirs() && fileF.createNewFile()) {
 					FileOutputStream out;
 					out = new FileOutputStream(file);
 					out.write(contents);
 					out.close();
 					String resourcePath = "http://fgaucho.dyndns.org:8080/cejug-classifieds-server/resource/"
 							+ domainId + "/" + customerId + "/" + name;
 					logger.info("resource created: " + resourcePath);
 					return resourcePath;
 
 				} else {
 					throw new RepositoryAccessException(
 							"error trying tocreate the resource path '" + file
 									+ "'");
 				}
 			} catch (IOException e) {
 				logger.severe(e.getMessage());
 				throw new RepositoryAccessException(e);
 			}
 		}
 	}
 
 	private static final char[] HEXADECIMAL = { '0', '1', '2', '3', '4', '5',
 			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
 
 	private static String hashGravatarEmail(String email)
 			throws NoSuchAlgorithmException {
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		md.reset();
 
 		byte[] bytes = md.digest(email.getBytes());
 		StringBuilder sb = new StringBuilder(2 * bytes.length);
 		for (int i = 0; i < bytes.length; i++) {
 			int low = (int) (bytes[i] & 0x0f);
 			int high = (int) ((bytes[i] & 0xf0) >> 4);
 			sb.append(HEXADECIMAL[high]);
 			sb.append(HEXADECIMAL[low]);
 		}
 		return sb.toString();
 	}
 
 }
