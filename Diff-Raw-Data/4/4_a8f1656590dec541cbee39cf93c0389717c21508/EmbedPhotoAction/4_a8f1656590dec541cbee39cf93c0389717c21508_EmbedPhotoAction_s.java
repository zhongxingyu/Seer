 package edu.cmu.photogenome.actions;
 
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 import edu.cmu.photogenome.business.EmbedPhoto;
 import edu.cmu.photogenome.business.ViewInformation;
 import edu.cmu.photogenome.dao.PhotoCategoryDao;
 import edu.cmu.photogenome.dao.PhotoCategoryDaoImpl;
 import edu.cmu.photogenome.dao.PhotoCommentDao;
 import edu.cmu.photogenome.dao.PhotoCommentDaoImpl;
 import edu.cmu.photogenome.dao.PhotoDao;
 import edu.cmu.photogenome.dao.PhotoDaoImpl;
 import edu.cmu.photogenome.dao.RegionCategoryDao;
 import edu.cmu.photogenome.dao.RegionCategoryDaoImpl;
 import edu.cmu.photogenome.domain.Photo;
 import edu.cmu.photogenome.domain.PhotoCategory;
 import edu.cmu.photogenome.domain.PhotoComment;
 import edu.cmu.photogenome.domain.RegionCategory;
 import edu.cmu.photogenome.util.HibernateUtil;
 
 public class EmbedPhotoAction extends ActionSupport {
 
 	final Logger log = LoggerFactory.getLogger(EmbedPhotoAction.class);
 	
 	final String jsonKey = getText("json.key");
 	
 	private Integer photoCategoryId;
 	private int photoId;
 	private int userId;
 	private String photoCategoryName;
 	private String photoCategoryText;
 
 	private Integer regionCategoryId;
 	private String categoryName;
 	private String regionCategoryText;
 
 	private int photoCommentId;
 	private String photoCommentText;
 	
 	
 	public Integer getPhotoCategoryId() {
 		return photoCategoryId;
 	}
 
 	public void setPhotoCategoryId(Integer photoCategoryId) {
 		this.photoCategoryId = photoCategoryId;
 	}
 
 	public int getPhotoId() {
 		return photoId;
 	}
 
 	public void setPhotoId(int photoId) {
 		this.photoId = photoId;
 	}
 
 	public int getUserId() {
 		return userId;
 	}
 
 	public void setUserId(int userId) {
 		this.userId = userId;
 	}
 
 	public String getPhotoCategoryName() {
 		return photoCategoryName;
 	}
 
 	public void setPhotoCategoryName(String photoCategoryName) {
 		this.photoCategoryName = photoCategoryName;
 	}
 
 	public String getPhotoCategoryText() {
 		return photoCategoryText;
 	}
 
 	public void setPhotoCategoryText(String photoCategoryText) {
 		this.photoCategoryText = photoCategoryText;
 	}
 
 	public Integer getRegionCategoryId() {
 		return regionCategoryId;
 	}
 
 	public void setRegionCategoryId(Integer regionCategoryId) {
 		this.regionCategoryId = regionCategoryId;
 	}
 
 	public String getCategoryName() {
 		return categoryName;
 	}
 
 	public void setCategoryName(String categoryName) {
 		this.categoryName = categoryName;
 	}
 
 	public String getRegionCategoryText() {
 		return regionCategoryText;
 	}
 
 	public void setRegionCategoryText(String regionCategoryText) {
 		this.regionCategoryText = regionCategoryText;
 	}
 
 	public int getPhotoCommentId() {
 		return photoCommentId;
 	}
 
 	public void setPhotoCommentId(int photoCommentId) {
 		this.photoCommentId = photoCommentId;
 	}
 
 	public String getPhotoCommentText() {
 		return photoCommentText;
 	}
 
 	public void setPhotoCommentText(String photoCommentText) {
 		this.photoCommentText = photoCommentText;
 	}
 
 	EmbedPhoto embedPhoto = new EmbedPhoto();
 	PhotoDao photoDao = new PhotoDaoImpl();
 	RegionCategoryDao regionCategoryDao = new RegionCategoryDaoImpl();
 	PhotoCategoryDao photoCategoryDao = new PhotoCategoryDaoImpl();
 	PhotoCommentDao photoCommentDao = new PhotoCommentDaoImpl();
 
 	/** Variables to store/pass JSON data **/
 	private Map<String, Object> jsonAddPhotoComments = new LinkedHashMap<String, Object>();
 	public Map<String, Object> getJsonAddPhotoComments() {
 		return jsonAddPhotoComments;
 	}
 
 	public void setJsonAddPhotoComments(Map<String, Object> jsonAddPhotoComments) {
 		this.jsonAddPhotoComments = jsonAddPhotoComments;
 	}
 
 	public Map<String, Object> getJsonAddPhotoCategories() {
 		return jsonAddPhotoCategories;
 	}
 
 	public void setJsonAddPhotoCategories(Map<String, Object> jsonAddPhotoCategories) {
 		this.jsonAddPhotoCategories = jsonAddPhotoCategories;
 	}
 
 
 	private Map<String, Object> jsonAddPhotoCategories = new LinkedHashMap<String, Object>();
 
 	/**
 	 * Add comments to a photo
 	 * 
 	 * @return
 	 */
 
 	public String addPhotoComment(){
 
 		Photo photo = null;
 		PhotoComment photoComment = null;
 
 		Session session = HibernateUtil.getSessionFactory().openSession();
 		embedPhoto.setSession(session);
 		HibernateUtil.beginTransaction(session);
 		photoDao.setSession(session);
 		try{
 			photo = photoDao.findById(photoId);
 
 			if(photo != null) {
 				if((photoComment = embedPhoto.addPhotoComment(photoId, userId, photoCommentText)) == null) {
 					HibernateUtil.rollbackTransaction(session);
 					return ERROR;
 				}
 			}else {
 				HibernateUtil.rollbackTransaction(session);
 				return ERROR;
 			}
 
 			jsonAddPhotoComments.put(jsonKey, photoComment);
 			HibernateUtil.commitTransaction(session);
 			return SUCCESS;
 
 		}catch(Exception ex) {
 			return ERROR;
 		}
 	}
 
 	/**
 	 * Add category to a photo
 	 * 
 	 * @return true if category is added, otherwise false
 	 */
 
 	public String addPhotoCategory(){
 
 		Photo photo = null;
 		PhotoCategory photoCategory = null;
 		List<SimpleEntry<String, String>> categoryList = null;
 		
 		Session session = HibernateUtil.getSessionFactory().openSession();
 		embedPhoto.setSession(session);
 		HibernateUtil.beginTransaction(session);
 		photoDao.setSession(session);
 		
 		try{
 			categoryList = new ArrayList<SimpleEntry<String, String>>();
 			photo = photoDao.findById(photoId);
 			
 		if(photo != null) {
 			categoryList.add(new SimpleEntry<String, String>(photoCategoryName, photoCategoryText));
 					
 			if((photoCategory = embedPhoto.addPhotoCategory(photoId, userId, categoryList))==null) {
 				HibernateUtil.rollbackTransaction(session);
 				return ERROR;
 			}
 		}else{
 			HibernateUtil.rollbackTransaction(session);
 			return ERROR;
 		}
 		
 		jsonAddPhotoCategories.put(jsonKey, photoCategory);
 		HibernateUtil.commitTransaction(session);
 		return SUCCESS;
 		
 		}catch(Exception ex)
 		{
 			return ERROR;
 		}
 	}
 
 	/**
 	 * Update photo comments
 	 * 
 	 * @return true if comment is updated, otherwise false
 	 */
 
 	public String editPhotoComment(){
 
 		PhotoComment photoComment = null;
 		
 		Session session = HibernateUtil.getSessionFactory().openSession();
 		embedPhoto.setSession(session);
 		HibernateUtil.beginTransaction(session);
 		photoCommentDao.setSession(session);
 		
 		if((photoComment = photoCommentDao.findById(photoCommentId)) == null) {
 			HibernateUtil.rollbackTransaction(session);
 			return "invalid_photo_comment";
 		}
 		else {
 			photoComment.setPhotoCommentTimestamp(new Date());
 			photoComment.setPhotoCommentText(photoCommentText);
 			if (embedPhoto.editPhotoComment(photoComment)) {
 				HibernateUtil.commitTransaction(session);
 				return SUCCESS;
 			}
 			else {
 				HibernateUtil.rollbackTransaction(session);
 				return ERROR;
 			}
 		}
 	}
 
 	/**
 	 * Update photo category
 	 * 
 	 * @return true if category is updated, otherwise false
 	 */
 
 	public String editPhotoCategory(){
 
		PhotoCategory photoCategory = photoCategoryDao.findById(photoCategoryId);
		
 		Session session = HibernateUtil.getSessionFactory().openSession();
 		embedPhoto.setSession(session);
 		HibernateUtil.beginTransaction(session);
 		
 		photoCategoryDao.setSession(session);
 		
 		if(photoCategory != null) {
 			photoCategory.setPhotoCategoryName(photoCategoryName);
 			photoCategory.setPhotoCategoryText(photoCategoryText);
 			if(!embedPhoto.editPhotoCategory(photoCategory)) {
 				HibernateUtil.rollbackTransaction(session);
 				return ERROR;
 			}
 		}else {
 			HibernateUtil.rollbackTransaction(session);
 			return "invalid_photo_category";
 		}
 		HibernateUtil.commitTransaction(session);
 		return SUCCESS;
 
 	}
 
 	/**
 	 * Update region category
 	 * 
 	 * @return true if region category is updated, otherwise false
 	 */
 
 	public String editRegionCategory(){
 
 		RegionCategory regionCategory = null;
 		
 		Session session = HibernateUtil.getSessionFactory().openSession();
 		embedPhoto.setSession(session);
 		HibernateUtil.beginTransaction(session);
 		
 		regionCategoryDao.setSession(session);
 		
 		if((regionCategory = regionCategoryDao.findById(regionCategoryId)) == null) {
 			HibernateUtil.rollbackTransaction(session);
 			return "invalid_region_category";
 		}
 		else {
 			regionCategory.setCategoryName(categoryName);
 			regionCategory.setRegionCategoryText(regionCategoryText);
 			if (embedPhoto.editRegionCategory(regionCategory)) {
 				HibernateUtil.commitTransaction(session);
 				return SUCCESS;
 			}
 			else {
 				HibernateUtil.rollbackTransaction(session);
 				return ERROR;
 			}
 		}
 	}
 
 	/**
 	 * Delete a photo comment
 	 * 
 	 * @return true if comment is deleted, otherwise false
 	 */
 
 	public String deletePhotoComment(){
 		
 		Session session = HibernateUtil.getSessionFactory().openSession();
 		embedPhoto.setSession(session);
 		HibernateUtil.beginTransaction(session);
 		
 		if(embedPhoto.deletePhotoComment(photoCommentId)) {
 			HibernateUtil.commitTransaction(session);
 			return SUCCESS;
 		}
 		else {
 			HibernateUtil.rollbackTransaction(session);
 			return ERROR;
 		}
 	}
 
 
 	/**
 	 * Delete a photo category
 	 * 
 	 * @return true if photo category is deleted, otherwise false
 	 */
 
 	public String deletePhotoCategory(){
 
 		Session session = HibernateUtil.getSessionFactory().openSession();
 		embedPhoto.setSession(session);
 		HibernateUtil.beginTransaction(session);
 		
 		if(embedPhoto.deletePhotoCategory(photoCategoryId)) {
 			HibernateUtil.commitTransaction(session);
 			return SUCCESS;
 		}
 		else {
 			HibernateUtil.rollbackTransaction(session);
 			return ERROR;
 		}
 	}
 
 }
