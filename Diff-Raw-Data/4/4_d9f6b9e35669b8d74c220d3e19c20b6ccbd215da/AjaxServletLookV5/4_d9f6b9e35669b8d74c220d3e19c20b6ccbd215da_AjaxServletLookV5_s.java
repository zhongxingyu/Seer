 /**
  * Copyright (C) 2000 - 2009 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of
  * the GPL, you may redistribute this Program in connection with Free/Libre
  * Open Source Software ("FLOSS") applications as described in Silverpeas's
  * FLOSS exception.  You should have received a copy of the text describing
  * the FLOSS exception, and it is also available here:
  * "http://repository.silverpeas.com/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.silverpeas.lookV5;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.silverpeas.external.filesharing.model.FileSharingInterface;
 import com.silverpeas.external.filesharing.model.FileSharingInterfaceImpl;
 import com.silverpeas.external.webConnections.dao.WebConnectionsImpl;
 import com.silverpeas.external.webConnections.model.WebConnectionsInterface;
 import com.silverpeas.jobStartPagePeas.JobStartPagePeasSettings;
 import com.silverpeas.look.LookHelper;
 import com.silverpeas.util.EncodeHelper;
 import com.silverpeas.util.StringUtil;
 import com.stratelia.silverpeas.pdc.control.PdcBm;
 import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
 import com.stratelia.silverpeas.pdc.model.PdcException;
 import com.stratelia.silverpeas.pdc.model.SearchAxis;
 import com.stratelia.silverpeas.pdc.model.SearchContext;
 import com.stratelia.silverpeas.pdc.model.Value;
 import com.stratelia.silverpeas.peasCore.MainSessionController;
 import com.stratelia.silverpeas.peasCore.URLManager;
 import com.stratelia.silverpeas.silvertrace.SilverTrace;
 import com.stratelia.silverpeas.util.SilverpeasSettings;
 import com.stratelia.webactiv.beans.admin.Admin;
 import com.stratelia.webactiv.beans.admin.ComponentInst;
 import com.stratelia.webactiv.beans.admin.OrganizationController;
 import com.stratelia.webactiv.beans.admin.SpaceInst;
 import com.stratelia.webactiv.beans.admin.SpaceInstLight;
 import com.stratelia.webactiv.beans.admin.UserFavoriteSpaceManager;
 import com.stratelia.webactiv.beans.admin.instance.control.Instanciateur;
 import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
 import com.stratelia.webactiv.organization.DAOFactory;
 import com.stratelia.webactiv.organization.UserFavoriteSpaceDAO;
 import com.stratelia.webactiv.organization.UserFavoriteSpaceVO;
 import com.stratelia.webactiv.util.FileRepositoryManager;
 import com.stratelia.webactiv.util.ResourceLocator;
 import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
 
 public class AjaxServletLookV5 extends HttpServlet {
 
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse res)
       throws ServletException, IOException {
     doPost(req, res);
   }
 
   @Override
   public void doPost(HttpServletRequest req, HttpServletResponse res)
       throws ServletException, IOException {
     HttpSession session = req.getSession(true);
 
     MainSessionController m_MainSessionCtrl = (MainSessionController) session
         .getAttribute("SilverSessionController");
     GraphicElementFactory gef = (GraphicElementFactory) session
         .getAttribute("SessionGraphicElementFactory");
     LookHelper helper = (LookHelper) session
         .getAttribute("Silverpeas_LookHelper");
     OrganizationController orgaController = m_MainSessionCtrl
         .getOrganizationController();
 
     String userId = m_MainSessionCtrl.getUserId();
     String language = m_MainSessionCtrl.getFavoriteLanguage();
 
     // Get ajax action
     String responseId = req.getParameter("ResponseId");
 
     String init = req.getParameter("Init");
     String spaceId = req.getParameter("SpaceId");
     String componentId = req.getParameter("ComponentId");
     String axisId = req.getParameter("AxisId");
     String valuePath = req.getParameter("ValuePath");
     String getPDC = req.getParameter("GetPDC");
     String pdc = req.getParameter("Pdc");
 
     // New request parameter to manage Bookmarks view or classical view
     String userMenuDisplayMode = req.getParameter("UserMenuDisplayMode");
 
     String defaultLook = gef.getCurrentLookName();
     boolean displayContextualPDC = helper.displayContextualPDC();
     boolean displayPDC = "true".equalsIgnoreCase(getPDC);
 
     // User favorite space DAO
     List<UserFavoriteSpaceVO> listUserFS = new ArrayList<UserFavoriteSpaceVO>();
     if (!"DISABLE".equalsIgnoreCase(helper.getDisplayUserFavoriteSpace())) {
       UserFavoriteSpaceDAO ufsDAO = DAOFactory.getUserFavoriteSpaceDAO();
       listUserFS = ufsDAO.getListUserFavoriteSpace(userId);
     }
 
     if (StringUtil.isDefined(componentId)) {
       helper.setComponentIdAndSpaceIds(null, null, componentId);
     } else if (StringUtil.isDefined(spaceId) && !spaceId.equals("spacePerso")) {
       helper.setSpaceIdAndSubSpaceId(spaceId);
     }
     if (StringUtil.isDefined(userMenuDisplayMode)) {
       helper.setDisplayUserFavoriteSpace(userMenuDisplayMode);
     } else {
       userMenuDisplayMode = helper.getDisplayUserFavoriteSpace();
     }
 
     res.setContentType("text/xml");
     res.setHeader("charset", "UTF-8");
 
     Writer writer = res.getWriter();
     writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
     writer.write("<ajax-response>");
     writer.write("<response type=\"object\" id=\"" + responseId + "\">");
 
     if (StringUtil.isDefined(init) && "1".equals(init)) {
       if (!StringUtil.isDefined(spaceId) && !StringUtil.isDefined(componentId)) {
         displayFirstLevelSpaces(userId, language, defaultLook, orgaController,
             helper, writer, listUserFS, userMenuDisplayMode);
       } else {
         // First get space's path cause it can be a subspace
         List<String> spaceIdsPath = getSpaceIdsPath(spaceId, componentId,
             orgaController);
 
         // space transverse
         displaySpace(spaceId, componentId, spaceIdsPath, userId, language,
             defaultLook, displayPDC, true, orgaController, helper, writer, listUserFS,
             userMenuDisplayMode);
 
         // other spaces
         displayTree(userId, componentId, spaceIdsPath, language,
             defaultLook, orgaController, helper, writer, listUserFS, userMenuDisplayMode);
 
         displayPDC(getPDC, spaceId, componentId, userId, displayContextualPDC,
             m_MainSessionCtrl, writer);
       }
     } else if (StringUtil.isDefined(axisId) && StringUtil.isDefined(valuePath)) {
       try {
         writer.write("<pdc>");
         getPertinentValues(spaceId, componentId, userId, axisId, valuePath,
             displayContextualPDC, m_MainSessionCtrl, writer);
         writer.write("</pdc>");
       } catch (PdcException e) {
         SilverTrace.error("lookSilverpeasV5", "Ajax", "root.ERROR");
       }
     } else if (StringUtil.isDefined(spaceId)) {
       if (spaceId.equals("spacePerso")) {
         // Affichage de l'espace perso
 
         ResourceLocator settings = gef.getFavoriteLookSettings();
         ResourceLocator message = new ResourceLocator(
             "com.stratelia.webactiv.homePage.multilang.homePageBundle",
             language);
 
         writer.write("<spacePerso id=\"" + spaceId
             + "\" type=\"space\" level=\"0\">");
 
         boolean isAnonymousAccess = helper.isAnonymousAccess();
 
         if (!isAnonymousAccess
             && SilverpeasSettings.readBoolean(settings,
             "personnalSpaceVisible", true)) {
           if (SilverpeasSettings.readBoolean(settings, "agendaVisible", true)) {
             writer
                 .write("<item id=\"agenda\" name=\""
                 + EncodeHelper.escapeXml(message.getString("Diary"))
                 +
                 "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                 + URLManager.getURL(URLManager.CMP_AGENDA) + "Main\"/>");
           }
           if (SilverpeasSettings.readBoolean(settings, "todoVisible", true)) {
             writer
                 .write("<item id=\"todo\" name=\""
                 + EncodeHelper.escapeXml(message.getString("ToDo"))
                 +
                 "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                 + URLManager.getURL(URLManager.CMP_TODO) + "todo.jsp\"/>");
           }
           if (SilverpeasSettings.readBoolean(settings, "notificationVisible",
               true)) {
             writer
                 .write("<item id=\"notification\" name=\""
                 + EncodeHelper.escapeXml(message.getString("Mail"))
                 +
                 "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                 + URLManager.getURL(URLManager.CMP_SILVERMAIL) + "Main\"/>");
           }
           if (SilverpeasSettings.readBoolean(settings, "interestVisible", true)) {
             writer
                 .write("<item id=\"subscriptions\" name=\""
                 + EncodeHelper.escapeXml(message
                 .getString("MyInterestCenters"))
                 +
                 "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                 + URLManager.getURL(URLManager.CMP_PDCSUBSCRIPTION)
                 + "subscriptionList.jsp\"/>");
           }
           if (SilverpeasSettings.readBoolean(settings, "favRequestVisible",
               true)) {
             writer
                 .write("<item id=\"requests\" name=\""
                 + EncodeHelper.escapeXml(message.getString("FavRequests"))
                 +
                 "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                 + URLManager.getURL(URLManager.CMP_INTERESTCENTERPEAS)
                 + "iCenterList.jsp\"/>");
           }
           if (SilverpeasSettings.readBoolean(settings, "linksVisible", true)) {
             writer
                 .write("<item id=\"links\" name=\""
                 + EncodeHelper.escapeXml(message.getString("FavLinks"))
                 +
                 "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                 + URLManager.getURL(URLManager.CMP_MYLINKSPEAS)
                 + "Main\"/>");
           }
 
           if (SilverpeasSettings.readBoolean(settings, "fileSharingVisible",
               false)) {
             FileSharingInterface fileSharing = new FileSharingInterfaceImpl();
             if (fileSharing.getTicketsByUser(userId).size() > 0) {
               writer
                   .write("<item id=\"fileSharing\" name=\""
                   + EncodeHelper
                   .escapeXml(message.getString("FileSharing"))
                   +
                   "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                   + URLManager.getURL(URLManager.CMP_FILESHARING)
                   + "Main\"/>");
             }
           }
           // mes connexions
           if (SilverpeasSettings.readBoolean(settings, "webconnectionsVisible",
               true)) {
             WebConnectionsInterface webConnections = new WebConnectionsImpl();
             if (webConnections.getConnectionsByUser(userId).size() > 0) {
               writer
                   .write("<item id=\"webConnections\" name=\""
                   + EncodeHelper
                   .escapeXml(message.getString("WebConnections"))
                   +
                   "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                   + URLManager.getURL(URLManager.CMP_WEBCONNECTIONS)
                   + "Main\"/>");
             }
           }
 
           if (SilverpeasSettings.readBoolean(settings, "customVisible", true)) {
             writer
                 .write("<item id=\"personalize\" name=\""
                 + EncodeHelper.escapeXml(message
                 .getString("Personalization"))
                 +
                 "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                 + URLManager.getURL(URLManager.CMP_PERSONALIZATION)
                 + "Main.jsp\"/>");
           }
           if (SilverpeasSettings.readBoolean(settings, "mailVisible", true)) {
             writer
                 .write("<item id=\"notifAdmins\" name=\""
                     +
                     EncodeHelper.escapeXml(message.getString("Feedback"))
                     +
                     "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\"javascript:notifyAdministrators()\"/>");
           }
           if (SilverpeasSettings
               .readBoolean(settings, "clipboardVisible", true)) {
             writer
                 .write("<item id=\"clipboard\" name=\""
                     +
                     EncodeHelper.escapeXml(message.getString("Clipboard"))
                     +
                     "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\"javascript:openClipboard()\"/>");
           }
 
           if (SilverpeasSettings.readBoolean(settings, "PersonalSpaceAddingsEnabled", true)) {
 
             PersonalSpaceController psc = new PersonalSpaceController();
             SpaceInst personalSpace = psc.getPersonalSpace(userId);
             if (personalSpace != null) {
               for (ComponentInst component : personalSpace.getAllComponentsInst()) {
                 String label =
                     helper.getString("lookSilverpeasV5.personalSpace." + component.getName());
                 if (!StringUtil.isDefined(label)) {
                   label = component.getName();
                 }
                 String url =
                    URLManager.getURL(component.getName(), null, component.getId()) + "Main";
                 writer
                     .write("<item id=\"" +
                         component.getName() +
                         component.getId() +
                         "\" name=\""
                         +
                         EncodeHelper.escapeXml(label)
                         +
                         "\" description=\"\" type=\"component\" kind=\"personalComponent\" level=\"1\" open=\"false\" url=\"" +
                         url + "\"/>");
               }
             }
             int nbComponentAvailables = psc.getVisibleComponents(orgaController).size();
             if (nbComponentAvailables > 0) {
               if (personalSpace == null ||
                   personalSpace.getAllComponentsInst().size() < nbComponentAvailables) {
                 writer
                     .write("<item id=\"addComponent\" name=\""
                         +
                         EncodeHelper.escapeXml(helper
                             .getString("lookSilverpeasV5.personalSpace.add"))
                         +
                         "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\"javascript:listComponents()\"/>");
               }
             }
           }
         }
 
         writer.write("</spacePerso>");
       } else {
         // First get space's path cause it can be a subspace
         List<String> spaceIdsPath = getSpaceIdsPath(spaceId, componentId,
             orgaController);
 
         displaySpace(spaceId, componentId, spaceIdsPath, userId, language,
             defaultLook, displayPDC, false, orgaController, helper, writer, listUserFS,
             userMenuDisplayMode);
 
         displayPDC(getPDC, spaceId, componentId, userId, displayContextualPDC,
             m_MainSessionCtrl, writer);
       }
     } else if (StringUtil.isDefined(componentId)) {
       displayPDC(getPDC, spaceId, componentId, userId, displayContextualPDC,
           m_MainSessionCtrl, writer);
     } else if (StringUtil.isDefined(pdc)) {
       displayNotContextualPDC(userId, m_MainSessionCtrl, writer);
     }
 
     writer.write("</response>");
     writer.write("</ajax-response>");
 
   }
 
   private void displayNotContextualPDC(String userId,
       MainSessionController mainSC, Writer writer) throws IOException {
     try {
       writer.write("<pdc>");
       getPertinentAxis(null, null, userId, mainSC, writer);
       writer.write("</pdc>");
     } catch (PdcException e) {
       SilverTrace.error("lookSilverpeasV5", "Ajax", "root.ERROR");
     }
   }
 
   private void displayPDC(String getPDC, String spaceId, String componentId,
       String userId, boolean displayContextualPDC, MainSessionController mainSC, Writer writer)
       throws IOException {
     try {
       writer.write("<pdc>");
       if ("true".equalsIgnoreCase(getPDC)) {
         getPertinentAxis(spaceId, componentId, userId, mainSC, writer);
       }
       writer.write("</pdc>");
     } catch (PdcException e) {
       SilverTrace.error("lookSilverpeasV5", "Ajax", "root.ERROR");
     }
   }
 
   private List<String> getSpaceIdsPath(String spaceId, String componentId,
       OrganizationController orgaController) {
     List<SpaceInst> spacePath = null;
     if (StringUtil.isDefined(spaceId)) {
       spacePath = orgaController.getSpacePath(spaceId);
     } else if (StringUtil.isDefined(componentId)) {
       spacePath = orgaController.getSpacePathToComponent(componentId);
     }
 
     List<String> spaceIdsPath = null;
     for (int s = 0; s < spacePath.size(); s++) {
       SpaceInst space = (SpaceInst) spacePath.get(s);
       if (spaceIdsPath == null) {
         spaceIdsPath = new ArrayList<String>();
       }
       if (!space.getId().startsWith(Admin.SPACE_KEY_PREFIX)) {
         spaceIdsPath.add(Admin.SPACE_KEY_PREFIX + space.getId());
       } else {
         spaceIdsPath.add(space.getId());
       }
     }
     return spaceIdsPath;
   }
 
   /**
    * @param spaceId : space identifier
    * @param listUFS : the list of user favorite space
    * @param orgaController : the OrganizationController object
    * @return true if the current space contains user favorites sub space, false else if
    */
   private boolean containsFavoriteSubSpace(String spaceId, List<UserFavoriteSpaceVO> listUFS,
       OrganizationController orgaController, String userId) {
     return UserFavoriteSpaceManager.containsFavoriteSubSpace(spaceId, listUFS, orgaController,
         userId);
   }
 
   /**
    * @param listUFS : the list of user favorite space
    * @param spaceId : space identifier
    * @return true if list of user favorites space contains spaceId identifier, false else if
    */
   private boolean isUserFavoriteSpace(List<UserFavoriteSpaceVO> listUFS, String spaceId) {
     return UserFavoriteSpaceManager.isUserFavoriteSpace(listUFS, spaceId);
   }
 
   /**
    * displaySpace build XML response tree of current spaceId
    * @param spaceId
    * @param componentId
    * @param spacePath
    * @param userId
    * @param language
    * @param defaultLook
    * @param displayPDC
    * @param displayTransverse
    * @param orgaController
    * @param helper
    * @param writer
    * @param listUFS
    * @param userMenuDisplayMode TODO
    * @throws IOException
    */
   private void displaySpace(String spaceId, String componentId, List<String> spacePath,
       String userId, String language, String defaultLook,
       boolean displayPDC, boolean displayTransverse,
       OrganizationController orgaController, LookHelper helper, Writer writer,
       List<UserFavoriteSpaceVO> listUFS, String userMenuDisplayMode) throws IOException {
     boolean isTransverse = false;
     // boolean displayBookmarks = false;
     // // First check user menu display mode
     // if ("BOOKMARKS".equalsIgnoreCase(helper.getUserMenuDisplayMode())) {
     // displayBookmarks = true;
     // }
 
     int i = 0;
     while (!isTransverse && i < spacePath.size()) {
       String spaceIdInPath = spacePath.get(i);
       isTransverse = helper.getTopSpaceIds().contains(spaceIdInPath);
       i++;
     }
 
     if (displayTransverse && !isTransverse) {
       return;
     }
 
     boolean open = (spacePath != null && spacePath.contains(spaceId));
     if (open) {
       spaceId = spacePath.remove(0);
     }
 
     // Affichage de l'espace collaboratif
     SpaceInstLight space = orgaController.getSpaceInstLightById(spaceId);
     if (space != null) {
       StringBuffer itemSB = new StringBuffer();
       itemSB.append("<item open=\"").append(open).append("\" ");
       itemSB.append(getSpaceAttributes(space, language, defaultLook, helper));
       itemSB.append(getFavoriteSpaceAttribute(userId, orgaController, listUFS, space, helper));
       itemSB.append(">");
 
       writer.write(itemSB.toString());
 
       if (open) {
         // Default display configuration
         boolean spaceBeforeComponent = true;
         // Display computing : First look at global configuration
         if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG.equalsIgnoreCase(
             JobStartPagePeasSettings.SPACEDISPLAYPOSITION_BEFORE)) {
           spaceBeforeComponent = true;
         } else if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG.equalsIgnoreCase(
             JobStartPagePeasSettings.SPACEDISPLAYPOSITION_AFTER)) {
           spaceBeforeComponent = false;
         } else if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG.equalsIgnoreCase(
             JobStartPagePeasSettings.SPACEDISPLAYPOSITION_TODEFINE)) {
           spaceBeforeComponent = space.isDisplaySpaceFirst();
         }
         if (spaceBeforeComponent) {
           getSubSpaces(spaceId, userId, spacePath, componentId, language,
               defaultLook, orgaController, helper, writer, listUFS, userMenuDisplayMode);
           getComponents(spaceId, componentId, userId, language, orgaController,
               writer, userMenuDisplayMode, listUFS);
         } else {
           getComponents(spaceId, componentId, userId, language, orgaController,
               writer, userMenuDisplayMode, listUFS);
           getSubSpaces(spaceId, userId, spacePath, componentId, language,
               defaultLook, orgaController, helper, writer, listUFS, userMenuDisplayMode);
         }
       }
     }
     writer.write("</item>");
   }
 
   /**
    * @param userId
    * @param orgaController
    * @param listUFS
    * @param space
    * @param helper
    * @return an XML user favorite space attribute only if User Favorite Space is enable
    */
   private String getFavoriteSpaceAttribute(String userId, OrganizationController orgaController,
       List<UserFavoriteSpaceVO> listUFS, SpaceInstLight space, LookHelper helper) {
     StringBuffer favSpace = new StringBuffer();
     if (!"DISABLE".equalsIgnoreCase(helper.getDisplayUserFavoriteSpace())) {
       favSpace.append(" favspace=\"");
       if (isUserFavoriteSpace(listUFS, space.getShortId())) {
         favSpace.append("true");
       } else {
         if (helper.isEnableUFSContainsState()) {
           if (containsFavoriteSubSpace(space.getShortId(), listUFS, orgaController, userId)) {
             favSpace.append("contains");
           } else {
             favSpace.append("false");
           }
         } else {
           favSpace.append("false");
         }
       }
       favSpace.append("\"");
     }
     return favSpace.toString();
   }
 
   private void displayTree(String userId, String targetComponentId,
       List<String> spacePath, String language, String defaultLook,
       OrganizationController orgaController, LookHelper helper, Writer out,
       List<UserFavoriteSpaceVO> listUFS, String userMenuDisplayMode) throws IOException {
     // Then get all first level spaces
     String[] availableSpaceIds = getRootSpaceIds(userId, orgaController, helper);
 
     out.write("<spaces menu=\"" + helper.getDisplayUserFavoriteSpace() + "\">");
     String spaceId = null;
     boolean loadCurSpace = false;
     for (int nI = 0; nI < availableSpaceIds.length; nI++) {
       spaceId = (String) availableSpaceIds[nI];
       // Check if user favorite space is enable/disable
       if ("DISABLE".equalsIgnoreCase(userMenuDisplayMode) ||
           "ALL".equalsIgnoreCase(userMenuDisplayMode)) {
         loadCurSpace = true;
       } else if ("BOOKMARKS".equalsIgnoreCase(userMenuDisplayMode)) {
         if (isUserFavoriteSpace(listUFS, spaceId) ||
             containsFavoriteSubSpace(spaceId, listUFS, orgaController, userId)) {
           loadCurSpace = true;
         }
       }
       if (loadCurSpace) {
         displaySpace(spaceId, targetComponentId, spacePath, userId, language,
             defaultLook, false, false, orgaController, helper, out, listUFS, userMenuDisplayMode);
       }
       loadCurSpace = false;
     }
     out.write("</spaces>");
   }
 
   private String getSpaceAttributes(SpaceInstLight space, String language,
       String defaultLook, LookHelper helper) {
     String spaceLook = space.getLook();
     if (!StringUtil.isDefined(spaceLook)) {
       spaceLook = defaultLook;
     }
     String spaceWallpaper = getWallPaper(space.getFullId());
 
     boolean isTransverse = helper.getTopSpaceIds().contains(space.getFullId());
 
     String attributeType = "space";
     if (isTransverse) {
       attributeType = "spaceTransverse";
     }
 
     return "id=\"" + space.getFullId() + "\" name=\""
         + EncodeHelper.escapeXml(space.getName(language)) + "\" description=\""
         + EncodeHelper.escapeXml(space.getDescription()) + "\" type=\""
         + attributeType + "\" kind=\"space\" level=\"" + space.getLevel()
         + "\" look=\"" + spaceLook + "\" wallpaper=\"" + spaceWallpaper + "\"";
   }
 
   private void displayFirstLevelSpaces(String userId, String language,
       String defaultLook, OrganizationController orgaController, LookHelper helper,
       Writer out, List<UserFavoriteSpaceVO> listUFS, String userMenuDisplayMode)
       throws IOException {
     String[] availableSpaceIds = getRootSpaceIds(userId, orgaController, helper);
 
     // Loop variable declaration
     SpaceInstLight space = null;
     String spaceId = null;
     boolean loadCurSpace = false;
     // Start writing XML spaces node
     out.write("<spaces menu=\"" + helper.getDisplayUserFavoriteSpace() + "\">");
     for (int nI = 0; nI < availableSpaceIds.length; nI++) {
       spaceId = (String) availableSpaceIds[nI];
       // Check if user favorite space is enable/disable
       if ("DISABLE".equalsIgnoreCase(userMenuDisplayMode) ||
           "ALL".equalsIgnoreCase(userMenuDisplayMode)) {
         loadCurSpace = true;
       } else if ("BOOKMARKS".equalsIgnoreCase(userMenuDisplayMode)) {
         if (isUserFavoriteSpace(listUFS, spaceId) ||
             containsFavoriteSubSpace(spaceId, listUFS, orgaController, userId)) {
           loadCurSpace = true;
         }
       }
       if (loadCurSpace) {
         space = orgaController.getSpaceInstLightById(spaceId);
         if (space != null) {
           StringBuffer itemSB = new StringBuffer();
           itemSB.append("<item ");
           itemSB.append(getSpaceAttributes(space, language, defaultLook, helper));
           itemSB.append(getFavoriteSpaceAttribute(userId, orgaController, listUFS, space, helper));
           itemSB.append("/>");
           out.write(itemSB.toString());
         }
       }
       loadCurSpace = false;
     }
     out.write("</spaces>");
   }
 
   private void getSubSpaces(String spaceId, String userId, List<String> spacePath,
       String targetComponentId, String language, String defaultLook,
       OrganizationController orgaController, LookHelper helper, Writer out,
       List<UserFavoriteSpaceVO> listUFS, String userMenuDisplayMode)
       throws IOException {
     String[] spaceIds = orgaController.getAllSubSpaceIds(spaceId, userId);
 
     SpaceInstLight space = null;
     String subSpaceId = null;
     boolean open = false;
     boolean loadCurSpace = false;
     for (int nI = 0; nI < spaceIds.length; nI++) {
       subSpaceId = spaceIds[nI];
       space = orgaController.getSpaceInstLightById(subSpaceId);
       if (space != null) {
         open = (spacePath != null && spacePath.contains(subSpaceId));
         // Check user favorite space
         if ("DISABLE".equalsIgnoreCase(userMenuDisplayMode) ||
             "ALL".equalsIgnoreCase(userMenuDisplayMode)) {
           loadCurSpace = true;
         } else if ("BOOKMARKS".equalsIgnoreCase(userMenuDisplayMode)) {
           if (isUserFavoriteSpace(listUFS, subSpaceId) ||
               containsFavoriteSubSpace(subSpaceId, listUFS, orgaController, userId)) {
             loadCurSpace = true;
           }
         }
         if (loadCurSpace) {
           StringBuffer itemSB = new StringBuffer();
           itemSB.append("<item ");
           itemSB.append(getSpaceAttributes(space, language, defaultLook, helper));
           itemSB.append(" open=\"").append(open).append("\"");
           itemSB.append(getFavoriteSpaceAttribute(userId, orgaController, listUFS, space, helper));
           itemSB.append(">");
 
           out.write(itemSB.toString());
 
           if (open) {
             // Default display configuration
             boolean spaceBeforeComponent = true;
             // Display computing : First look at global configuration
             if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG.equalsIgnoreCase(
                 JobStartPagePeasSettings.SPACEDISPLAYPOSITION_BEFORE)) {
               spaceBeforeComponent = true;
             } else if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG.equalsIgnoreCase(
                 JobStartPagePeasSettings.SPACEDISPLAYPOSITION_AFTER)) {
               spaceBeforeComponent = false;
             } else if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG.equalsIgnoreCase(
                 JobStartPagePeasSettings.SPACEDISPLAYPOSITION_TODEFINE)) {
               spaceBeforeComponent = space.isDisplaySpaceFirst();
             }
             // the subtree must be displayed
             // components of expanded space must be displayed too
             if (spaceBeforeComponent) {
               getSubSpaces(subSpaceId, userId, spacePath, targetComponentId,
                   language, defaultLook, orgaController, helper, out, listUFS, userMenuDisplayMode);
               getComponents(subSpaceId, targetComponentId, userId, language,
                   orgaController, out, userMenuDisplayMode, listUFS);
             } else {
               getComponents(subSpaceId, targetComponentId, userId, language,
                   orgaController, out, userMenuDisplayMode, listUFS);
               getSubSpaces(subSpaceId, userId, spacePath, targetComponentId,
                   language, defaultLook, orgaController, helper, out, listUFS, userMenuDisplayMode);
             }
 
           }
 
           out.write("</item>");
         }
         loadCurSpace = false;
       }
     }
   }
 
   private void getComponents(String spaceId, String targetComponentId,
       String userId, String language, OrganizationController orgaController,
       Writer out, String userMenuDisplayMode, List<UserFavoriteSpaceVO> listUFS) throws IOException {
     boolean loadCurComponent = false;
     if ("DISABLE".equalsIgnoreCase(userMenuDisplayMode) ||
         "ALL".equalsIgnoreCase(userMenuDisplayMode)) {
       loadCurComponent = true;
     } else if ("BOOKMARKS".equalsIgnoreCase(userMenuDisplayMode)) {
       if (isUserFavoriteSpace(listUFS, spaceId)) {
         loadCurComponent = true;
       }
     }
     if (loadCurComponent) {
       String[] componentIds = orgaController.getAvailCompoIdsAtRoot(spaceId,
           userId);
 
       SpaceInstLight space = orgaController.getSpaceInstLightById(spaceId);
 
       int level = space.getLevel() + 1;
 
       ComponentInst component = null;
       boolean open = false;
       String url = null;
       String kind = null;
       for (int c = 0; componentIds != null && c < componentIds.length; c++) {
         component = (ComponentInst) orgaController
             .getComponentInst(componentIds[c]);
 
         if (component != null && !component.isHidden()) {
           open = (targetComponentId != null && component.getId().equals(
               targetComponentId));
           url = URLManager.getURL(component.getName(), null, component.getId())
               + "Main";
 
           kind = component.getName();
           WAComponent descriptor = Instanciateur.getWAComponent(component
               .getName());
           if (descriptor != null
               && "RprocessManager"
               .equalsIgnoreCase(descriptor.getRequestRouter()))
             kind = "processManager";
 
           out.write("<item id=\"" + component.getId() + "\" name=\""
               + EncodeHelper.escapeXml(component.getLabel(language))
               + "\" description=\""
               + EncodeHelper.escapeXml(component.getDescription(language))
               + "\" type=\"component\" kind=\"" + EncodeHelper.escapeXml(kind)
               + "\" level=\"" + level + "\" open=\"" + open + "\" url=\"" + url
               + "\"/>");
         }
       }
     }
   }
 
   private void getPertinentAxis(String spaceId, String componentId,
       String userId, MainSessionController mainSC, Writer out)
       throws PdcException, IOException {
     List<SearchAxis> primaryAxis = null;
     SearchContext searchContext = new SearchContext();
 
     PdcBm pdc = new PdcBmImpl();
 
     if (StringUtil.isDefined(componentId)) {
       // L'item courant est un composant
       primaryAxis = pdc.getPertinentAxisByInstanceId(searchContext, "P",
           componentId);
     } else {
       List<String> cmps = null;
       if (StringUtil.isDefined(spaceId)) {
         // L'item courant est un espace
         cmps = getAvailableComponents(spaceId, userId, mainSC
             .getOrganizationController());
       } else {
         cmps = Arrays.asList(mainSC.getUserAvailComponentIds());
       }
 
       if (cmps != null && cmps.size() > 0)
         primaryAxis = pdc.getPertinentAxisByInstanceIds(searchContext, "P",
             cmps);
     }
 
     SearchAxis axis = null;
     if (primaryAxis != null) {
       for (int a = 0; a < primaryAxis.size(); a++) {
         axis = primaryAxis.get(a);
         if (axis != null && axis.getNbObjects() > 0) {
           out.write("<axis id=\"" + axis.getAxisId() + "\" name=\""
               + EncodeHelper.escapeXml(axis.getAxisName())
               + "\" description=\"\" level=\"0\" open=\"false\" nbObjects=\""
               + axis.getNbObjects() + "\"/>");
         }
       }
     }
 
     pdc = null;
     primaryAxis = null;
   }
 
   private List<String> getAvailableComponents(String spaceId, String userId,
       OrganizationController orgaController) {
     String a[] = orgaController.getAvailCompoIds(spaceId, userId);
     return Arrays.asList(a);
   }
 
   private String getValueId(String valuePath) {
     // cherche l'id de la valeur
     // valuePath est de la forme /0/1/2/
 
     String valueId = valuePath;
     int len = valuePath.length();
     valueId = valuePath.substring(0, len - 1); // on retire le slash
 
     if (valuePath.equals("/")) {
       valueId = valueId.substring(1);// on retire le slash
     } else {
       int lastIdx = valueId.lastIndexOf("/");
       valueId = valueId.substring(lastIdx + 1);
     }
     return valueId;
   }
 
   private List<Value> getPertinentValues(String spaceId, String componentId,
       String userId, String axisId, String valuePath,
       boolean displayContextualPDC, MainSessionController mainSC, Writer out) throws IOException,
       PdcException {
     List<Value> daughters = null;
     SearchContext searchContext = new SearchContext();
     searchContext.setUserId(userId);
 
     if (StringUtil.isDefined(axisId)) {
       PdcBm pdc = new PdcBmImpl();
 
       // TODO : some improvements can be made here !
       // daughters contains all pertinent values of axis instead of pertinent
       // daughters only
       if (displayContextualPDC) {
         if (StringUtil.isDefined(componentId)) {
           daughters = pdc.getPertinentDaughterValuesByInstanceId(searchContext,
               axisId, valuePath, componentId);
         } else {
           List<String> cmps = getAvailableComponents(spaceId, userId, mainSC
               .getOrganizationController());
           daughters = pdc.getPertinentDaughterValuesByInstanceIds(
               searchContext, axisId, valuePath, cmps);
         }
       } else {
         List<String> cmps = Arrays.asList(mainSC.getUserAvailComponentIds());
         daughters = pdc.getPertinentDaughterValuesByInstanceIds(searchContext,
             axisId, valuePath, cmps);
       }
 
       String valueId = getValueId(valuePath);
 
       Value value = null;
       for (int v = 0; v < daughters.size(); v++) {
         value = daughters.get(v);
         if (value != null && value.getMotherId().equals(valueId)) {
           out.write("<value id=\"" + value.getFullPath() + "\" name=\""
               + EncodeHelper.escapeXml(value.getName())
               + "\" description=\"\" level=\"" + value.getLevelNumber()
               + "\" open=\"false\" nbObjects=\"" + value.getNbObjects()
               + "\"/>");
         }
       }
 
       pdc = null;
     }
 
     return daughters;
   }
 
   private String getWallPaper(String spaceId) {
     String path = FileRepositoryManager.getAbsolutePath("Space"
         + spaceId.substring(2), new String[] { "look" });
 
     File file = new File(path + "wallPaper.jpg");
     if (file.isFile()) {
       return "1";
     } else {
       file = new File(path + "wallPaper.gif");
       if (file.isFile()) {
         return "1";
       }
     }
 
     return "0";
   }
 
   private String[] getRootSpaceIds(String userId,
       OrganizationController orgaController, LookHelper helper) {
     List<String> rootSpaceIds = new ArrayList<String>();
 
     List<String> topSpaceIds = helper.getTopSpaceIds();
     String[] availableSpaceIds = orgaController.getAllRootSpaceIds(userId);
     for (int i = 0; i < availableSpaceIds.length; i++) {
       if (!topSpaceIds.contains(availableSpaceIds[i])) {
         rootSpaceIds.add(availableSpaceIds[i]);
       }
     }
 
     return (String[]) rootSpaceIds.toArray(new String[0]);
   }
 }
