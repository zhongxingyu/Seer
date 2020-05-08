 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.service.engine;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import net.wazari.dao.UtilisateurFacadeLocal;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.Carnet;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.Tag;
 import net.wazari.dao.entity.Theme;
 import net.wazari.dao.entity.Utilisateur;
 import net.wazari.service.WebPageLocal;
 import net.wazari.service.entity.util.PhotoUtil;
 import net.wazari.service.exception.WebAlbumsServiceException;
 import net.wazari.service.exchange.ViewSession;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoDisplay;
 import net.wazari.service.exchange.xml.XmlTheme;
 import net.wazari.service.exchange.xml.XmlThemeList;
 import net.wazari.service.exchange.xml.album.XmlAlbum;
 import net.wazari.service.exchange.xml.album.XmlGpx;
 import net.wazari.service.exchange.xml.carnet.XmlCarnet;
 import net.wazari.service.exchange.xml.common.XmlDetails;
 import net.wazari.service.exchange.xml.common.XmlPhotoAlbumUser;
 import net.wazari.service.exchange.xml.common.XmlWebAlbumsList;
 import net.wazari.service.exchange.xml.photo.XmlPhoto;
 import net.wazari.service.exchange.xml.photo.XmlPhotoId;
 
 /**
  *
  * @author kevin
  */
 @Stateless
 public class DaoToXmlBean {
     @EJB
     private UtilisateurFacadeLocal userDAO;
     @EJB
     private WebPageLocal webPageService;
     @EJB
     PhotoUtil photoUtil;
    
     public void convertPhotoDetails(ViewSession vSession, Photo enrPhoto, 
             XmlDetails details, boolean withAlbumDetails) throws WebAlbumsServiceException {
         details.photoId = new XmlPhotoId(enrPhoto.getId());
         if (vSession.directFileAccess()) {
             details.photoId.path = enrPhoto.getPath(true) ;
         }
 
         if (enrPhoto.isGpx()) {
             //keep null if false
             details.isGpx =  true; 
         }
 
         details.setDescription(enrPhoto.getDescription());
 
         //tags de cette photo
         details.tag_used = webPageService.displayListIBTD(ViewSession.Mode.TAG_USED, vSession, enrPhoto,
                 ViewSession.Box.NONE, enrPhoto.getAlbum().getDate());
 
         details.albumId = enrPhoto.getAlbum().getId();
         
         if (withAlbumDetails) {
             details.albumName = enrPhoto.getAlbum().getNom();
             details.albumDate = enrPhoto.getAlbum().getDate();
         }
 
         details.stars = enrPhoto.getStars();
     }
     
     public void addUserOutside(ViewSession vSession, Photo enrPhoto, 
             XmlDetails details) {
         String name;
         boolean outside;
         Utilisateur enrUser = userDAO.find(enrPhoto.getDroit());
         if (enrUser != null) {
             name = enrUser.getNom();
             outside = false;
         } else {
             name = userDAO.loadUserOutside(enrPhoto.getAlbum().getId()).getNom();
             outside = true;
         }
 
         details.user = new XmlPhotoAlbumUser(name, outside);
     }
     
     public void addAuthorDetails(ViewSession vSession, Photo enrPhoto, XmlPhoto photo) {
         if (enrPhoto.getTagAuthor() != null) {
                 Tag enrAuthor = enrPhoto.getTagAuthor();
                 photo.author = new XmlWebAlbumsList.XmlWebAlbumsTagWho();
                 photo.author.name = enrAuthor.getNom();
                 photo.author.id = enrAuthor.getId();
                 if (enrAuthor.getPerson() != null) {
                     photo.author.contact = enrAuthor.getPerson().getContact();
                 }
             }
     }
 
     public void addExifDetails(ViewSessionPhotoDisplay vSession, Photo enrPhoto, XmlPhoto photo) {
         photo.exif = photoUtil.getXmlExif(enrPhoto) ;
     }
 
     public void convertAlbum(ViewSession vSession, Album enrAlbum, XmlAlbum album, boolean withTags) throws WebAlbumsServiceException {
         album.id = enrAlbum.getId();
         album.name = enrAlbum.getNom();
         album.droit = enrAlbum.getDroit().getNom();
         album.date = webPageService.xmlDate(enrAlbum.getDate());
         
         //tags de l'album
         if (withTags) {
            album.details.tag_used = webPageService.displayListIBTD(ViewSession.Mode.TAG_GEO, 
                                       vSession, enrAlbum, ViewSession.Box.NONE, enrAlbum.getDate());
         }
         album.details.setDescription(enrAlbum.getDescription());
         
         if (enrAlbum.getPicture() != null) {
             album.details.photoId = new XmlPhotoId(enrAlbum.getPicture().getId()) ;
             if (vSession.directFileAccess()) {
                 album.details.photoId.path = enrAlbum.getPicture().getPath(true) ;
             }
         }
     }
     
     public void addAlbumRight(ViewSession vSession, Album enrAlbum, XmlAlbum album) throws WebAlbumsServiceException {
         album.rights = webPageService.displayListDroit(enrAlbum.getDroit(), null);
     }
     
     public void addAlbumGpx(ViewSession vSession, Album enrAlbum, XmlAlbum album) throws WebAlbumsServiceException {
         for (Photo enrGpx : enrAlbum.getGpxList()) {
             if (album.gpx == null) {
                 album.gpx = new ArrayList(enrAlbum.getGpxList().size()) ;
             }
             XmlGpx gpx = new XmlGpx();
             convertGpx(vSession, enrGpx, gpx);
             album.gpx.add(gpx);
         }
     }
 
     public void convertCarnet(ViewSession vSession, Carnet enrCarnet, XmlCarnet carnet) {
         carnet.date = webPageService.xmlDate(enrCarnet.getDate());
         carnet.id = enrCarnet.getId();
         carnet.name = enrCarnet.getNom();
         if (enrCarnet.getPicture() != null) {
             carnet.picture = new XmlPhotoId(enrCarnet.getPicture().getId());
             if (vSession.directFileAccess()) {
                 carnet.picture.path = enrCarnet.getPicture().getPath(true);
             }
         }
     }
 
     public void convertGpx(ViewSession vSession, Photo enrGpx, XmlGpx gpx) {
         gpx.id = enrGpx.getId();
         if (vSession.directFileAccess()) {
             gpx.path = enrGpx.getPath(true);
         }
         gpx.setDescription(enrGpx.getDescription());
     }
     
     public void convertTheme(ViewSession vSession, Theme enrTheme, XmlTheme theme) {
         theme.id = enrTheme.getId() ;
         theme.name = enrTheme.getNom() ;
         if (enrTheme.getPicture() != null) {
             theme.picture = new XmlPhotoId(enrTheme.getPicture().getId());
             if (vSession.directFileAccess())
                 theme.picture.path = enrTheme.getPicture().getPath(true);
         }
     }
     
     public void convertThemes(ViewSession vSession, List<Theme> lstThemes, XmlThemeList themes) {
         for (Theme enrTheme : lstThemes) {
             XmlTheme theme = new XmlTheme() ;
             convertTheme(vSession, enrTheme, theme);
             themes.theme.add(theme);
         }
     }
 }
