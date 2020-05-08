 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.view.servlet.exchange;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import net.wazari.dao.entity.Theme;
 import net.wazari.dao.entity.Utilisateur;
 import net.wazari.dao.exchange.ServiceSession;
 import net.wazari.service.exchange.Configuration;
 import net.wazari.service.exchange.ViewSession.Action;
 import net.wazari.service.exchange.ViewSession.EditMode;
 import net.wazari.service.exchange.ViewSession.Special;
 import net.wazari.service.exchange.ViewSessionLogin;
 import net.wazari.service.exchange.ViewSessionSession;
 import net.wazari.service.exchange.ViewSessionAlbum;
 import net.wazari.service.exchange.ViewSessionAlbum.ViewSessionAlbumDisplay;
 import net.wazari.service.exchange.ViewSessionAlbum.ViewSessionAlbumEdit;
 import net.wazari.service.exchange.ViewSessionAlbum.ViewSessionAlbumSubmit;
 import net.wazari.service.exchange.ViewSessionConfig;
 import net.wazari.service.exchange.ViewSessionDatabase;
 import net.wazari.service.exchange.ViewSessionImages;
 import net.wazari.service.exchange.ViewSessionMaint;
 import net.wazari.service.exchange.ViewSessionPhoto;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoDisplay;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoDisplay.ViewSessionPhotoDisplayMassEdit;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoDisplay.ViewSessionPhotoDisplayMassEdit.Turn;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoEdit;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoFastEdit;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoSubmit;
 import net.wazari.service.exchange.ViewSessionTag;
 
 /**
  *
  * @author kevin
  */
 public class ViewSessionImpl implements
         ServiceSession,
         ViewSessionLogin,
         ViewSessionAlbum, ViewSessionAlbumDisplay, ViewSessionAlbumEdit, ViewSessionAlbumSubmit,
         ViewSessionConfig,
         ViewSessionPhoto, ViewSessionPhotoDisplay, ViewSessionPhotoEdit, ViewSessionPhotoSubmit,
         ViewSessionTag,
         ViewSessionImages, ViewSessionPhotoDisplayMassEdit, ViewSessionPhotoFastEdit,
         ViewSessionDatabase, 
         ViewSessionMaint{
 
     private static final Logger log = LoggerFactory.getLogger(ViewSessionImpl.class.getCanonicalName());
     private HttpServletRequest request;
     private HttpServletResponse response;
     
     public ViewSessionImpl(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
         this.request = request;
         this.response = response;
     }
 
     @Override
     public String getDesc() {
         return getString("desc");
     }
 
     @Override
     public String getNom() {
         return getString("nom");
     }
 
     @Override
     public String getDate() {
         return getString("date");
     }
 
     @Override
     public Integer[] getTags() {
         return getIntArray("tags");
     }
 
     @Override
     public Integer[] getNewTag() {
         return getIntArray("newTag");
     }
 
     @Override
     public boolean getForce() {
         return "yes".equals(getString("force"));
     }
 
     @Override
     public boolean getSuppr() {
         String suppr = getString("suppr") ;
         return "Oui je veux supprimer cette photo".equals(suppr) ||
                 "Oui je veux supprimer cet album".equals(suppr);
     }
 
     @Override
     public Integer getPage() {
         return getInteger("page");
     }
 
     @Override
     public Integer getCount() {
         return getInteger("count");
     }
 
     @Override
     public Integer getCountAlbm() {
         return getInteger("countAlbm");
     }
 
     @Override
     public Integer getUserAllowed() {
         return getInteger("user");
     }
 
     @Override
     public Special getSpecial() {
         return getEnum("special", Special.class);
     }
 
     @Override
     public Action getAction() {
         return getEnum("action", Action.class);
     }
 
     @Override
     public String getUserPass() {
         return getString("userPass");
     }
 
     @Override
     public String getUserName() {
         return getString("userName") ;
     }
 
     /** ** **/
     @Override
     public Integer getThemeId() {
         return getInteger("themeId");
     }
 
     @Override
     public void setTheme(Theme enrTheme) {
         setSessionObject("theme", enrTheme);
     }
 
     @Override
     public Theme getTheme() {
         return getSessionObject("theme", Theme.class);
     }
 
     /** ** **/
     @Override
     public boolean getDetails() {
         Boolean ret = getSessionObject("details", Boolean.class);
         if (ret == null) {
             ret = false;
         }
         return ret;
     }
 
     @Override
     public void setDetails(Boolean newValue) {
         setSessionObject("details", newValue);
     }
 
     @Override
     public File getTempDir() {
         return getSessionObject("tempDir", File.class);
     }
 
     /** ** **/
     @Override
     public Utilisateur getUser() {
         return getSessionObject("user", Utilisateur.class);
     }
     
     @Override
     public void setUser(Utilisateur enrUser) {
         setSessionObject("user", enrUser);
     }
 
     /** ** **/
     @Override
     public EditMode getEditionMode() {
         return getSessionObject("editionMode", EditMode.class);
     }
 
     @Override
     public void setEditionMode(EditMode editMode) {
         setSessionObject("editionMode", editMode);
     }
 
     /** ** **/
     @Override
     public boolean isRootSession() {
         Boolean val = getSessionObject("rootSession", Boolean.class);
         if (val == null) {
             val = false;
         }
         return val;
     }
 
     @Override
     public void setRootSession(Boolean rootSession) {
         setSessionObject("rootSession", rootSession);
     }
 
     /** ** **/
     @Override
     public boolean isSessionManager() {
         Boolean ret = getSessionObject("sessionManager", Boolean.class);
         if (ret == null) {
             ret = false;
         }
         return ret;
     }
     
     @Override
     public boolean isAdminSession() {
         return this.getUser() != null && this.getUser().getId() == 1 ;
     }
 
     @Override
     public void setSessionManager(Boolean sessionManager) {
         setSessionObject("sessionManager", sessionManager);
     }
 
     /** ** **/
     @Override
     public Integer getId() {
         return getInteger("id");
     }
 
     @Override
     public String getNouveau() {
         return getString("nouveau");
     }
 
     @Override
     public Integer getTag() {
         return getInteger("tag");
     }
 
     @Override
     public String getLng() {
         return getString("lng");
     }
 
     @Override
     public String getLat() {
         return getString("lat");
     }
 
     @Override
     public boolean getVisible() {
         return "y".equals(getString("visible"));
     }
 
     @Override
     public String getImportTheme() {
         return getString("importTheme");
     }
 
     @Override
     public Integer getType() {
         return getInteger("type");
     }
 
     @Override
     public Integer getWidth() {
         return getInteger("width");
     }
 
     @Override
     public boolean getRepresent() {
         return "y".equals(getString("represent"));
     }
 
     @Override
     public boolean getThemeBackground() {
         return "y".equals(getString("themeBackground"));
     }
 
     @Override
     public Integer getTagPhoto() {
         return getInteger("tagPhoto");
     }
 
     @Override
     public Turn getTurn() {
         return getEnum("turn", Turn.class);
     }
 
     @Override
     public Integer getAddTag() {
         return getInteger("addTag");
     }
 
     @Override
     public boolean getChk(Integer id) {
         return "modif".equals(getString("chk" + id));
     }
 
     @Override
     public Integer getRmTag() {
         return getInteger("rmTag");
     }
 
     @Override
     public boolean wantsDetails() {
         Boolean val = getSessionObject("details", Boolean.class);
         if (val == null) {
             val = false;
         }
         return val;
     }
 
     @Override
     public Mode getMode() {
         return getEnum("mode", Mode.class);
     }
 
     @Override
     public Integer getAlbum() {
         return getInteger("album");
     }
 
     @Override
     public Integer getAlbmCount() {
         return getInteger("albmCount");
     }
 
     @Override
     public Integer[] getTagAsked() {
         return getIntArray("tagAsked");
     }
 
     @Override
     public boolean getWantTagChildren() {
         return getString("wantTagChildren") != null;
     }
 
     @Override
     public ImgMode getImgMode() {
         return getEnum("mode", ImgMode.class);
     }
 
     @Override
     public Configuration getConfiguration() {
         return ConfigurationXML.getConf();
     }
 
     @Override
     public void setContentDispositionFilename(String name) {
         response.addHeader("Content-Disposition", "filename="+name);
     }
 
     @Override
     public void setContentLength(int contentLength) {
         response.setContentLength(contentLength);
     }
 
     @Override
     public void setContentType(String type) {
         response.setContentType(type);
     }
 
     @Override
     public OutputStream getOutputStream() throws IOException {
         return response.getOutputStream();
     }
 
     @Override
     public Integer getParentTag() {
         return getInteger("parentTag") ;
     }
 
     @Override
     public Integer[] getSonTags() {
         return getIntArray("sonTag") ;
     }
 
     @Override
     public Integer getBorderWidth() {
         return getInteger("borderWidth") ;
     }
 
     @Override
     public String getBorderColor() {
         return getString("borderColor") ;
     }
 
     /** ** ** ** **/
     private Integer getInteger(String name) {
         return getObject(name, Integer.class);
     }
 
     private String getString(String name) {
         return getObject(name, String.class);
     }
 
     private Boolean getBoolean(String name) {
         return getObject(name, Boolean.class);
     }
 
     private <T extends Enum<T>> T getEnum(String value, Class<T> type) {
         return getObject(value, type);
     }
 
     @Override
     public int getAlbumSize() {
         return getConfiguration().getAlbumSize();
     }
 
     @Override
     public int getPhotoSize() {
         return getConfiguration().getPhotoSize();
     }
 
     private Integer[] getIntArray(String key) {
         return castToIntArray(getParamArray(key));
     }
 
     private String[] getParamArray(String name) {
         String[] ret = request.getParameterValues(name);
         log.info( "getParamArray param:{} returned {}", new Object[]{name, Arrays.toString(ret)});
         return ret;
     }
 
     private Integer[] castToIntArray(String[] from) {
         if (from == null) {
             return new Integer[]{};
         }
         ArrayList<Integer> ret = new ArrayList<Integer>(from.length);
         for (int i = 0; i < from.length; i++) {
             try {
                 ret.add(Integer.parseInt(from[i]));
             } catch (NumberFormatException e) {
             }
         }
         return ret.toArray(new Integer[0]);
     }
 
     @Override
     public boolean isAuthenticated() {
         return getUserPrincipal() != null;
     }
 
     @Override
     public Principal getUserPrincipal() {
         return request.getUserPrincipal();
     }
 
     @Override
     public void login(String user, String passwd) {
         try {
             request.login(user, passwd);
         } catch (ServletException ex) {
             log.warn("ServletException", ex);
         }
     }
 
     @Override
     public ViewSessionPhotoDisplayMassEdit getMassEdit() {
         return (ViewSessionPhotoDisplayMassEdit) this;
     }
 
     @Override
     public MaintAction getMaintAction() {
         return getObject("action", MaintAction.class);
     }
     
     @Override
     public TagAction getTagAction() {
         return getObject("tagAction", TagAction.class);
     }
 
     private void setSessionObject(String string, Object value) {
         setSessionObject(string, value, request.getSession());
     }
 
     private <T> T getObject(String string, Class<T> aClass) {
         return getObject(string, aClass, request) ;
     }
 
     private <T> T getSessionObject(String key, Class<T> aClass) {
         return getSessionObject(key, aClass, request.getSession(), request) ;
     }
 
     private static <T> T getObject(String name, Class<T> type, HttpServletRequest request) {
         T ret = null;
         String val = request.getParameter(name);
 
         try {
             if (val == null) {
                 ret = null ;
             } else if (type == String.class) {
                 ret = type.cast(val);
             } else if (type == Integer.class) {
                 ret = type.cast(Integer.parseInt(val));
             } else if (type == Boolean.class) {
                 ret = type.cast(Boolean.parseBoolean(val));
             } else if (type.isEnum()) {
                 ret = (T) Enum.valueOf((Class) type, val);
             } else {
                 log.info( "Unknown class {} for parameter {}", new Object[]{type, name});
             }
         } catch (ClassCastException e) {
             log.info( "Can''t cast value {} into class {}", new Object[]{val, type});
         } catch (NullPointerException e) {
            log.info( "NullPointerException with {} for class {} ({})", new Object[]{val, type, name});
         } catch (NumberFormatException e) {
            log.info( "NumberFormatException with  '{}' for class {} ({})", new Object[]{val, type, name});
         } catch (IllegalArgumentException e) {
             log.info( "IllegalArgumentException with {} for class {}", new Object[]{val, type});
         }
         log.debug( "getObject param:{} type:{} returned {}", new Object[]{name, type, ret});
         return ret;
     }
 
     private static <T> T getSessionObject(String name, Class<T> type, HttpSession session, HttpServletRequest request) {
         T ret = type.cast(session.getAttribute(name));
         if (ret == null && request != null) {
             ret = getObject(name, type, request);
         }
         log.debug( "getSessionObject param:{} type:{} returned {}", new Object[]{name, type, ret});
         return ret;
     }
 
     private static void setSessionObject(String key, Object val, HttpSession session) {
         log.info( "setSessionObject param:{} val:{}", new Object[]{key, val});
         session.setAttribute(key, val);
     }
 
     @Override
     public String getDroit() {
         return getString("user") ;
     }
 
     @Override
     public boolean isRemoteAccess() {
         log.info( "local: {}", request.getLocalAddr()) ;
         log.info( "remote: {}", request.getRemoteAddr()) ;
         return !request.getLocalAddr().equals(request.getRemoteHost()) ;
     }
 
     @Override
     public void redirect(String filepath) {
         try {
             response.sendRedirect(filepath);
         } catch (IOException ex) {
             log.error("IOException", ex);
         }
     }
 
     public static class ViewSessionLoginImpl implements ViewSessionSession {
         private HttpSession session ;
         public ViewSessionLoginImpl (HttpSession session) {
             this.session = session ;
         }
         @Override
         public void setTempDir(File temp) {
             setSessionObject("tempDir", temp, session);
         }
         @Override
         public File getTempDir() {
             return ViewSessionImpl.getSessionObject("tempDir", File.class, session, null) ;
         }
 
         @Override
         public Configuration getConfiguration() {
             return ConfigurationXML.getConf() ;
         }
     }
 }
