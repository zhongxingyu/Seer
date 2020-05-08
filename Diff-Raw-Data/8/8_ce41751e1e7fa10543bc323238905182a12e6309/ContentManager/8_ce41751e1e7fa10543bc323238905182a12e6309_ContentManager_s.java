 package com.mediaplatform.manager.media;
 
 import com.mediaplatform.event.*;
 import com.mediaplatform.jsf.fileupload.FileAcceptor;
 import com.mediaplatform.jsf.fileupload.FileUploadBean;
 import com.mediaplatform.jsf.fileupload.UploadedFile;
 import com.mediaplatform.manager.AntiSamyBean;
 import com.mediaplatform.manager.file.FileStorageManager;
 import com.mediaplatform.model.*;
 import com.mediaplatform.security.*;
 import com.mediaplatform.security.User;
 import com.mediaplatform.util.*;
 import com.mediaplatform.util.jsf.FacesUtil;
 import org.jboss.solder.servlet.http.RequestParam;
 
 import javax.ejb.Stateful;
 import javax.ejb.TransactionAttribute;
 import javax.el.ValueExpression;
 import javax.enterprise.context.ConversationScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.inject.Instance;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * User: timur
  * Date: 11/26/12
  * Time: 3:41 PM
  */
 @Stateful
 @ConversationScoped
 @Named
 public class ContentManager extends AbstractContentManager implements Serializable {
     private Content selectedContent;
     private Genre selectedGenre;
     @Inject
     private Event<UpdateContentEvent> updateEvent;
     @Inject
     private Event<CreateContentEvent> createEvent;
     @Inject
     private Event<SelectGenreEvent> selectGenreEventEvent;
     @Inject
     private Event<UpdateCatalogEvent> updateCatalogEvent;
     @Inject
     private Event<DeleteContentEvent> deleteEvent;
     @Inject
     private CatalogManager catalogManager;
 
     private static final AtomicLong dirtyId = new AtomicLong(-1);
 
     private FileEntry videoFile;
     private FileEntry cover;
 
     private FileUploadBean fileUploadBean = new FileUploadBean(new FileAcceptor() {
         @Override
         public void accept(UploadedFile file) {
 
             videoFile = fileStorageManager.get().saveFile(
                     new ParentRef(dirtyId.decrementAndGet(), selectedContent.getEntityType()),
                     file,
                     DataType.MEDIA_CONTENT);
         }
     });
 
     private FileUploadBean imgFileUploadBean = new FileUploadBean(new FileAcceptor() {
         @Override
         public void accept(UploadedFile file) {
             cover = fileStorageManager.get().saveFile(
                     new ParentRef(dirtyId.decrementAndGet(),
                             selectedContent.getEntityType()),
                     file,
                     DataType.COVER);
         }
     });
 
     @Inject
     private Instance<FileStorageManager> fileStorageManager;
 
     @Inject
     private Instance<ViewHelper> viewHelper;
 
     private static final int HOME_PAGE_LIST_MAX_SIZE = 10;
 
     @Inject
     @RequestParam("cntid")
     private Instance<Long> selectedContentId;
 
     private List<Content> authorTopContentList;
     private List<Content> contentList;
     private com.mediaplatform.model.User selectedUser;
     private String outcome;
 
 
     public Content getSelectedContent() {
         if (selectedContent == null) {
             selectedContent = new Content();
         }
         return selectedContent;
     }
 
     public String getMediaFileName() {
         if (selectedContent != null && selectedContent.getMediaFile() != null)
             return selectedContent.getMediaFile().getName();
         return "empty";
     }
 
     public String getMediaFileFullName() {
         if (selectedContent != null && selectedContent.getMediaFile() != null)
             return selectedContent.getMediaFile().getFullName();
         return "empty";
     }
 
     public void setSelectedContent(Content selectedContent) {
         this.selectedContent = selectedContent;
     }
 
     public Genre getSelectedGenre() {
         return selectedGenre;
     }
 
     public void setSelectedGenre(Genre selectedGenre) {
         this.selectedGenre = selectedGenre;
     }
 
     public List<Content> getContentList() {
         return contentList;
     }
 
     private void fillChildrensContent(List<Content> contentList, List<Genre> children) {
         for(Genre child:children){
             Genre genre = catalogManager.getById(child.getId());
             contentList.addAll(genre.getContentListByStatus(ModerationStatus.ALLOWED));
             fillChildrensContent(contentList, genre.getChildren());
         }
     }
 
     public void viewByGenreId(String genreIdStr) {
         Long genreId = Long.parseLong(genreIdStr);
         ConversationUtils.safeBegin(conversation);
 
         TwoTuple<Genre, List<Content>> result = catalogManager.getCatalogContentList(genreId);
         selectedGenre = result.first;
         selectGenreEventEvent.fire(new SelectGenreEvent(selectedGenre.getId(), getExpandedCatalogIds()));
         contentList = new ArrayList<Content>(selectedGenre.getContentList());
         fillChildrensContent(contentList, selectedGenre.getChildren());
     }
 
     public void viewByUsername(String username) {
         ConversationUtils.safeBegin(conversation);
         selectedUser = userManagerInstance.get().findByUsername(username);
         contentList = findByUserName(username);
     }
 
 
     @com.mediaplatform.security.User
     public void add() {
         ConversationUtils.safeBegin(conversation);
         selectedContent = new Content();
     }
 
     public void viewVideoOnDemand(String id) {
         ConversationUtils.safeBegin(conversation);
         view(Long.parseLong(id));
     }
 
     public int getTestId(){
         return 34;
     }
 
     public void validateContentId(javax.faces.context.FacesContext facesContext, javax.faces.component.UIComponent uiComponent, java.lang.Object obj) {
         boolean ok = FacesUtil.validateLong(facesContext, uiComponent, obj, "Не указан видео ID");
         if (ok) {
             Long id = Long.parseLong(String.valueOf(obj));
             if (getContentById(id) == null) {
                 facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Контент с ID '" + id + "' не найден", null));
                 ok = false;
             }
         }
         if (!ok) {
             FacesUtil.redirectToHomePage();
         }
     }
 
     @com.mediaplatform.security.User
     public String editContent(Long id) {
         return editContent(id, null);
     }
 
     @com.mediaplatform.security.User
     public String editContent(Long id, String outcome) {
         view(id);
         if (!Restrictions.isAdminOrOwner(identity, currentUser, selectedContent.getAuthor())) {
             FacesUtil.redirectToDeniedPage();
             return outcome;
         }
         ConversationUtils.safeBegin(conversation);
         this.outcome = outcome;
 
         return "edit-content";
     }
 
     @TransactionAttribute
     private void view(Long id) {
         selectedContent = getContentById(id);
         selectedContent.incViewCount();
         update(selectedContent);
         selectedGenre = catalogManager.getById(selectedContent.getGenre().getId());
         authorTopContentList = null;
     }
 
     public void validateGenreId(javax.faces.context.FacesContext facesContext, javax.faces.component.UIComponent uiComponent, java.lang.Object obj) {
         boolean ok = FacesUtil.validateLong(facesContext, uiComponent, obj, "Не указан ИД жанра");
         if (ok) {
             Long id = Long.parseLong(String.valueOf(obj));
             if (catalogManager.getById(id) == null) {
                 facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Жанр с ИД '" + id + "' не найден", null));
                 ok = false;
             }
         }
         if (!ok) {
             FacesUtil.redirectToHomePage();
         }
     }
 
     @com.mediaplatform.security.User
     @TransactionAttribute
     public String saveOrUpdate() {
         boolean edit = selectedContent.getId() != null;
         if (edit && !Restrictions.isAdminOrOwner(identity, currentUser, selectedContent.getAuthor())) {
             FacesUtil.redirectToDeniedPage();
             return outcome;
         }
         boolean ok = true;
         if(!edit && cover == null){
             FacesUtil.addError(null, "Добавьте пожалуйста обложку для видео.");
             ok = false;
         }
         if(!edit && videoFile == null){
             FacesUtil.addError(null, "Добавьте пожалуйста видео.");
             ok = false;
         }
         if(!ok) return null;
         selectedContent.setModerationStatus(ModerationStatus.WAITING_FOR_MODERATION);
         super.saveOrUpdate(selectedContent, selectedGenre, videoFile, cover);
         if (edit) {
             messages.info("Обновленно успешно! После модерации пост будет доступен другим пользователям.");
             updateEvent.fire(new UpdateContentEvent(selectedContent.getId()));
         } else {
             messages.info("Сохранено успешно! После модерации пост будет доступен другим пользователям.");
             createEvent.fire(new CreateContentEvent(selectedContent.getId(), getExpandedCatalogIds()));
         }
 
         updateCatalogEvent.fire(new UpdateCatalogEvent(selectedGenre.getId()));
         fileUploadBean.clearUploadData();
         imgFileUploadBean.clearUploadData();
         videoFile = null;
         cover = null;
         conversation.end();
         if(outcome == null) return null;
         return outcome;
     }
 
     @com.mediaplatform.security.User
     @TransactionAttribute
     public void delete() {
         if (!Restrictions.isAdminOrOwner(identity, currentUser, selectedContent.getAuthor())) {
             FacesUtil.redirectToDeniedPage();
             return;
         }
         DeleteContentEvent event = new DeleteContentEvent(selectedContent.getId(), getExpandedCatalogIds());
         super.delete(selectedContent);
         messages.info("Удалено успешно!");
         deleteEvent.fire(event);
         selectedContent = null;
         selectedGenre = catalogManager.getById(selectedGenre.getId());
         fileUploadBean.clearUploadData();
         imgFileUploadBean.clearUploadData();
     }
 
     @com.mediaplatform.security.User
     @TransactionAttribute
     public void addRate(boolean direction) {
         RateInfo rateInfo = new RateInfo(currentUser.getId(), direction);
         selectedContent = getContentById(selectedContent.getId());
         if (selectedContent.getContentRates().contains(rateInfo)) {
             return;
         }
         selectedContent.getContentRates().add(rateInfo);
         selectedContent.addRate(direction);
         update(selectedContent);
         updateEvent.fire(new UpdateContentEvent(selectedContent.getId()));
     }
 
     @Admin
     @TransactionAttribute
     public void moderationAllow() {
         selectedContent.setModerationStatus(ModerationStatus.ALLOWED);
         update(selectedContent);
         messages.info("Статус модерации изменен на Принято.");
         updateCatalogEvent.fire(new UpdateCatalogEvent(selectedContent.getGenre().getId()));
     }
 
     @Admin
     @TransactionAttribute
     public void moderationDisallow() {
         selectedContent.setModerationStatus(ModerationStatus.DISALLOWED);
         update(selectedContent);
         messages.info("Статус модерации изменен Отклонено.");
         updateCatalogEvent.fire(new UpdateCatalogEvent(selectedContent.getGenre().getId()));
     }
 
     //TODO redirect to list view
     public void endConversation() {
         ConversationUtils.safeEnd(conversation);
     }
 
     private Set<Long> getExpandedCatalogIds() {
         Set<Long> expandedCatalogIds = new HashSet<Long>();
         Genre parent = selectedGenre;
         while (parent != null) {
             expandedCatalogIds.add(parent.getId());
             parent = parent.getParent();
         }
         return expandedCatalogIds;
     }
 
     @Admin
     public void publish(boolean highQuality) {
         String absFilePath = fileStorageManager.get().getMediaFileUrl(selectedContent.getMediaFile(), true);
         LiveStream liveStream = new LiveStream(absFilePath, selectedContent.getMediaFile().getName(), selectedContent.getDescription(), true);
         appEm.persist(liveStream);
         RunShellCmdHelper.publish(absFilePath, selectedContent.getMediaFile().getName(), highQuality ? RtmpPublishFormat.FLV_HIGH : RtmpPublishFormat.FLV_LOW);
         messages.info("Published successfull!");
     }
 
     @Admin
     public void publish() {
         publish(true);
     }
 
     public com.mediaplatform.model.User getSelectedUser() {
         return selectedUser;
     }
 
     public List<Content> getLatestContentList() {
         return findLatestList(HOME_PAGE_LIST_MAX_SIZE);
     }
 
     public List<Content> getPopularContentList() {
         return findPopularList(HOME_PAGE_LIST_MAX_SIZE);
     }
 
     public List<Content> getAuthorTopContentList() {
         if (authorTopContentList == null) {
             authorTopContentList = appEm.createQuery("select c from Content c where c.author.id= :userId and c.id <> :currentContentId order by c.rate desc").
                     setParameter("userId", selectedContent.getAuthor().getId()).
                     setParameter("currentContentId", selectedContent.getId()).getResultList();
 
         }
         return authorTopContentList;
     }
 
     public String getVideoUrl() {
         if(videoFile != null) return viewHelper.get().getVideoUrl(videoFile);
         if (selectedContent == null || selectedContent.getMediaFile() == null) return null;
         return viewHelper.get().getVideoUrl(selectedContent.getMediaFile());
     }
 
     @Override
     protected String getCurrentContentName() {
         return getMediaFileFullName();
     }
 
     public String getHeader() {
         boolean edit = selectedContent != null && selectedContent.getId() != null;
         if (edit) {
             return "Редактирование " + selectedContent.getTitle();
         }
         return "Создание";
     }
 
     public FileUploadBean getFileUploadBean() {
         return fileUploadBean;
     }
 
     public FileUploadBean getImgFileUploadBean() {
         return imgFileUploadBean;
     }
 
     public String getCoverUrl(String format) {
         if(cover != null) return viewHelper.get().getImgUrlExt(cover, format);
         if (selectedContent == null || selectedContent.getCover() == null) return null;
         return viewHelper.get().getImgUrlExt(selectedContent.getCover(), format);
     }
 
     public boolean canRate() {
         if (selectedContent == null || identity == null || !identity.isLoggedIn()) return false;
         if (selectedContent.getContentRates().contains(new RateInfo(currentUser.getId(), false))) return false;
         return true;
     }
 
 
 }
 
