 package com.sdzee.forums.beans;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 
 import com.sdzee.breadcrumb.beans.BreadCrumbHelper;
 import com.sdzee.breadcrumb.beans.BreadCrumbItem;
 import com.sdzee.dao.DAOException;
 import com.sdzee.forums.dao.ForumDao;
 import com.sdzee.forums.dao.ReponseDao;
 import com.sdzee.forums.dao.SujetDao;
 import com.sdzee.forums.entities.Forum;
 import com.sdzee.forums.entities.Reponse;
 import com.sdzee.forums.entities.Sujet;
 import com.sdzee.membres.entities.Membre;
 
 @ManagedBean( name = "sujetsBean" )
@RequestScoped
 public class SujetsBackingBean implements Serializable {
     private static final long   serialVersionUID     = 1L;
     private static final String HEADER_REQUETE_PROXY = "X-FORWARDED-FOR";
     private static final String URL_PAGE_SUJET       = "/sujet.jsf?sujetId=";
 
     private Sujet               sujet;
 
     private String              queryString;
 
     @EJB
     private SujetDao            sujetDao;
     @EJB
     private ForumDao            forumDao;
     @EJB
     private ReponseDao          reponseDao;
 
     @PostConstruct
     public void init() {
         sujet = new Sujet();
     }
 
     public Forum getForum( int forumId ) {
         return forumDao.trouver( forumId );
     }
 
     public List<Sujet> getSujets( int forumId ) {
         return sujetDao.lister( forumDao.trouver( forumId ) );
     }
 
     public Reponse getDerniereReponse( Sujet sujet ) {
         return reponseDao.trouverDerniere( sujet );
     }
 
     public Integer getDecompteReponses( Sujet sujet ) {
         return reponseDao.decompte( sujet );
     }
 
     public void creer( Membre membre, Forum forum ) throws IOException {
         FacesContext context = FacesContext.getCurrentInstance();
 
         // TODO: remplacer par la méthode propre issue de OmniFaces
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                 .getRequest();
         String adresseIP = request.getHeader( HEADER_REQUETE_PROXY );
         if ( adresseIP == null ) {
             adresseIP = request.getRemoteAddr();
         }
         sujet.setAdresseIP( adresseIP );
         sujet.setDateCreation( new Timestamp( System.currentTimeMillis() ) );
         sujet.setAuteur( membre );
         sujet.setForum( forum );
         try {
             sujetDao.creer( sujet );
             context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_INFO, "Nouveau sujet créé avec succès",
                     sujet.getTitre() ) );
             ExternalContext externalContext = context.getExternalContext();
             externalContext.redirect( externalContext.getRequestContextPath() + URL_PAGE_SUJET
                     + String.valueOf( sujet.getId() ) );
         } catch ( DAOException e ) {
             context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_ERROR, "Echec de la création du sujet",
                     "Une erreur est survenue..." ) );
             // TODO: logger
         }
     }
 
     public List<BreadCrumbItem> getBreadCrumb( int forumId ) {
         String chemin = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
         Forum forum = getForum( forumId );
         List<BreadCrumbItem> breadCrumb = BreadCrumbHelper.initBreadCrumb( chemin );
         BreadCrumbHelper.addForumsItem( breadCrumb, chemin, true );
         BreadCrumbHelper.addItem( breadCrumb, forum.getTitre(), null );
         return breadCrumb;
     }
 
     public String getQueryString() {
         return queryString;
     }
 
     public void setQueryString( String queryString ) {
         this.queryString = queryString;
     }
 
     public Sujet getSujet() {
         return sujet;
     }
 
     public void setSujet( Sujet sujet ) {
         this.sujet = sujet;
     }
 }
