 package org.otherobjects.cms.controllers;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang.StringUtils;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.Url;
 import org.otherobjects.cms.binding.BindService;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.dao.GenericDao;
 import org.otherobjects.cms.dao.GenericJcrDao;
 import org.otherobjects.cms.datastore.DataStore;
 import org.otherobjects.cms.datastore.HibernateDataStore;
 import org.otherobjects.cms.datastore.JackrabbitDataStore;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.model.DbFolder;
 import org.otherobjects.cms.model.Editable;
 import org.otherobjects.cms.model.Folder;
 import org.otherobjects.cms.model.FolderDao;
 import org.otherobjects.cms.model.SiteFolder;
 import org.otherobjects.cms.model.SmartFolder;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.util.ActionUtils;
 import org.otherobjects.cms.util.IdentifierUtils;
 import org.otherobjects.cms.util.RequestUtils;
 import org.otherobjects.cms.validation.ValidatorService;
 import org.otherobjects.cms.views.JsonView;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Main workbench controller. Handles critical list/view/edit functions.
  * 
  * <p>Currently only supports SiteFolders, DbFolders and SmartFolders.
  * 
  * <p>TODO May need a dedicated XHR version of this
  * 
  * @author rich
  */
 @Controller
 public class WorkbenchController
 {
     private static final int ITEMS_PER_PAGE = 50;
 
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     @Resource
     private TypeService typeService;
 
     @Resource
     private DaoService daoService;
 
     @Resource
     private HibernateDataStore hibernateDataStore;
 
     @Resource
     private JackrabbitDataStore jackrabbitDataStore;
 
     @Resource
     private BindService bindService;
 
     @Resource
     private ValidatorService validatorService;
 
     //    @Resource
     //    private LocaleResolver localeResolver;
 
     @RequestMapping({"", "/", "/workbench/*"})
     public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         //        String newLocale = ServletRequestUtils.getStringParameter(request, "locale");
         //        if (newLocale != null)
         //        {
         //            this.localeResolver.setLocale(request, response, StringUtils.parseLocaleString(newLocale));
         //            this.logger.info("Locale set to: " + this.localeResolver.resolveLocale(request));
         //        }
 
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/overview");
         return mav;
     }
 
     @RequestMapping({"/workbench/view/*"})
     public ModelAndView view(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String id = RequestUtils.getId(request);
         DataStore store = getDataStore(detectStore(id));
         Object item = store.get(id);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/view");
         mav.addObject("id", id);
         mav.addObject("item", item);
         mav.addObject("typeDef", getTypeDef(item));
         return mav;
     }
 
     @RequestMapping({"/workbench/edit/*"})
     public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String id = RequestUtils.getId(request);
         DataStore store = getDataStore(detectStore(id));
         Object item = store.get(id);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/edit");
         mav.addObject("id", id);
         mav.addObject("object", item);
         mav.addObject("typeDef", getTypeDef(item));
         return mav;
     }
 
     @RequestMapping({"/workbench/create/*"})
     public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         String type = RequestUtils.getId(request);
         String containerId = request.getParameter("container");
         TypeDef typeDef = typeService.getType(type);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/edit");
 
         DataStore store = getDataStore(typeDef.getStore());
         Object create = store.create(typeDef, containerId);
 
         if (request.getParameter("code") != null)
             PropertyUtils.setProperty(create, "code", request.getParameter("code"));
         mav.addObject("object", create);
         mav.addObject("containerId", containerId);
         mav.addObject("typeDef", typeDef);
         return mav;
     }
 
     @SuppressWarnings("unchecked")
     @RequestMapping({"/workbench/list/*"})
     public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String id = RequestUtils.getId(request);
         int requestedPage = RequestUtils.getInt(request, "page", 1);
         String q = null; // TODO 
 
         FolderDao folderDao = (FolderDao) this.daoService.getDao(Folder.class);
        Folder folder = folderDao.get(id);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/list");
         mav.addObject("id", id);
         mav.addObject("folder", folder);
 
         if (folder instanceof SiteFolder)
         {
             UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
             mav.addObject("items", universalJcrDao.getAllByPath(((SiteFolder) folder).getJcrPath()));
         }
         else if (folder instanceof DbFolder)
         {
             DbFolder dbFolder = (DbFolder) folder;
             String dbType = dbFolder.getMainType();
             String dbQuery = dbFolder.getMainTypeQuery();
 
             GenericDao genericDao = this.daoService.getDao(dbType);
 
             Assert.notNull(genericDao, "No DAO found for: " + dbType);
             Assert.isTrue(!(genericDao instanceof GenericJcrDao), "Dao must be defined for this database object.");
 
             String sort = null;
             boolean asc = true;
             if (StringUtils.isNotBlank(dbQuery))
                 mav.addObject("items", genericDao.getPagedByQuery(dbQuery, ITEMS_PER_PAGE, requestedPage, q, sort, asc).getItems());
             else
                 mav.addObject("items", genericDao.getAllPaged(ITEMS_PER_PAGE, requestedPage, q, sort, asc).getItems());
         }
         else if (folder instanceof SmartFolder)
         {
             //            SmartFolder smartFolder = (SmartFolder) node;
             //            String query = smartFolder.getSearchTerm();
             //            String jcr = smartFolder.getQuery();
             //            
             //            if (StringUtils.isNotEmpty(jcr))
             //                query = jcr;
             //            else
             //                query = "/jcr:root/site//*[jcr:contains(., '" + query + "') and not(jcr:like(@ooType,'%Folder'))] order by @modificationTimestamp descending";
             //            
             //            Assert.hasText(query, "SmartFolder must have a query.");
             //            pagedResult = this.universalJcrDao.pageByJcrExpression(query, ITEMS_PER_PAGE, getRequestedPage(request));
 
         }
         else
             throw new OtherObjectsException("Currently only listings for SiteFolder, DbFolder and SmartFolder are supported.");
 
         return mav;
     }
 
     @RequestMapping({"/workbench/search*"})
     public ModelAndView search(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         // Search JCR
         String q = request.getParameter("q");
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         List<BaseNode> results = universalJcrDao.getAllByJcrExpression("/jcr:root/site//(*) [jcr:contains(data/., '" + q + "')]");
 
         // Search DB
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/search");
         mav.addObject("results", results);
         return mav;
     }
 
     @RequestMapping({"/workbench/history/*"})
     public ModelAndView history(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String id = RequestUtils.getId(request);
         DataStore store = getDataStore(detectStore(id));
         BaseNode item = (BaseNode) store.get(id);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/history");
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         mav.addObject("id", id);
         mav.addObject("item", item);
         mav.addObject("versions", universalJcrDao.getVersions(item));
         mav.addObject("typeDef", getTypeDef(item));
         return mav;
     }
 
     @RequestMapping({"/workbench/diff/*"})
     public ModelAndView diff(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String id = RequestUtils.getId(request);
         int versionNumber = RequestUtils.getInt(request, "version");
         DataStore store = getDataStore(detectStore(id));
         BaseNode item = (BaseNode) store.get(id);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/diff");
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         mav.addObject("id", id);
         mav.addObject("item", item);
         mav.addObject("item1", item);
         mav.addObject("item2", universalJcrDao.getVersionByChangeNumber(item, versionNumber));
         mav.addObject("versions", universalJcrDao.getVersions(item));
         mav.addObject("typeDef", getTypeDef(item));
         return mav;
     }
 
     /* Actions */
 
     @SuppressWarnings("unchecked")
     @RequestMapping({"/workbench/save"})
     public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         ActionUtils actionUtils = new ActionUtils(request, response, null, null);
         
         RequestUtils.logParameters(logger, request);
 
         // Prepare object (create or fetch)
         String id = request.getParameter("_oo_id");
         String typeName = request.getParameter("_oo_type");
         String containerId = request.getParameter("_oo_containerId");
         Assert.notNull(typeName, "Type name not specified in _oo_type parameter.");
         TypeDef typeDef = typeService.getType(typeName);
         Assert.notNull(typeDef, "No typeDef found for type: " + typeName);
 
         Editable item = null;
         DataStore store = getDataStore(typeDef.getStore());
         GenericDao genericDao = store.getDao(typeDef);
 
         if (StringUtils.isNotBlank(id))
         {
             item = (Editable) store.get(id);
         }
         else
         {
             item = (Editable) store.create(typeDef, containerId);
         }
 
         Assert.notNull(item, "Could not prepare item for binding: " + typeName);
 
         // Bind and validate
         BindingResult errors = null;
         errors = bindService.bind(item, typeDef, request);
         Validator validator = validatorService.getValidator(item);
         if (validator != null)
             validator.validate(item, errors);
         else
             logger.warn("No validator for item of class " + item.getClass() + " found");
 
         // Save
         boolean success = false;
         if (!(errors != null && errors.hasErrors()))
         {
             // Save new object
             item = (Editable) genericDao.save(item, false);
             success = true;
         }
 
         // Prepare return data
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("success", success);
         data.put("formObject", item);
         if (!success)
         {
             List<Object> jsonErrors = new ArrayList<Object>();
             for (FieldError e : (List<FieldError>) errors.getFieldErrors())
             {
                 Map<String, String> error = new HashMap<String, String>();
                 error.put("id", e.getField());
                 //error.put("msg", requestContext.getMessage(e.getCode(), e.getArguments()));
                 jsonErrors.add(error);
             }
             data.put("success", false);
             data.put("errors", jsonErrors);
         }
 
         // Return
         if (RequestUtils.isXhr(request))
         {
             // Return via XHR
             ModelAndView view = new ModelAndView("jsonView");
             view.addObject(JsonView.JSON_DATA_KEY, data);
             return view;
         }
         else
         {
             // Return normally
             if (success)
             {
                 actionUtils.flashInfo("Your object was saved.");
                 Url u = new Url("/otherobjects/workbench/view/" + item.getEditableId());
                 response.sendRedirect(u.toString());
                 return null;
             }
             else
             {
                 actionUtils.flashWarning("Your object could not be saved. See below for errors.");
                 ModelAndView view = new ModelAndView("/otherobjects/templates/legacy/pages/edit");
                 view.addObject("success", success);
                 view.addObject("id", item.getEditableId());
                 view.addObject("typeDef", typeDef);
                 view.addObject("object", item);
                 view.addObject("containerId", containerId);
                 view.addObject("org.springframework.validation.BindingResult.object", errors);
                 return view;
             }
         }
     }
 
     @RequestMapping({"/workbench/publish/*"})
     public ModelAndView publish(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         // Publish
         String id = RequestUtils.getId(request);
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         BaseNode item = universalJcrDao.get(id);
         universalJcrDao.publish(item, null);
 
         // FIXME - Catch errors
 
         // Response
         ActionUtils actionUtils = new ActionUtils(request, response, null, null);
         actionUtils.flashInfo("Your object was published.");
         Url u = new Url("/otherobjects/workbench/view/" + item.getId());
         response.sendRedirect(u.toString());
         return null;
     }
 
     @RequestMapping({"/workbench/unpublish/*"})
     public ModelAndView unpublish(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         return null;
     }
 
     @RequestMapping({"/workbench/delete/*"})
     public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         // FIXME Need to publish this delete too.
         String id = RequestUtils.getId(request);
         FolderDao folderDao = (FolderDao) this.daoService.getDao(Folder.class);
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
 
         BaseNode item = universalJcrDao.get(id);
         universalJcrDao.remove(id);
 
         ActionUtils actionUtils = new ActionUtils(request, response, null, null);
         actionUtils.flashInfo("Your object was deleted.");
 
        SiteFolder folder = folderDao.getByPath(item.getPath());
         Url u = new Url("/otherobjects/workbench/list/" + folder.getId());
         response.sendRedirect(u.toString());
         return null;
     }
 
     @RequestMapping({"/workbench/restore/*"})
     public ModelAndView restore(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         // FIXME Need to publish this delete too.
         String id = RequestUtils.getId(request);
         int versionNumber = RequestUtils.getInt(request, "version");
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
 
         BaseNode item = universalJcrDao.get(id);
         universalJcrDao.restoreVersionByChangeNumber(item, versionNumber);
 
         ActionUtils actionUtils = new ActionUtils(request, response, null, null);
         actionUtils.flashInfo("Your object was restored.");
 
         Url u = new Url("/otherobjects/workbench/view/" + item.getId());
         response.sendRedirect(u.toString());
         return null;
     }
 
     @RequestMapping({"/workbench/reorder/*"})
     public ModelAndView reorder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         return null;
     }
 
     private DataStore getDataStore(String store)
     {
         if (store.equals(TypeDef.JACKRABBIT))
             return this.jackrabbitDataStore;
         else if (store.equals(TypeDef.HIBERNATE))
             return this.hibernateDataStore;
         else
             throw new OtherObjectsException("No dataStore configured for: " + store);
     }
 
     /**
      * Temporary method to detect store type base on id format. This only suports
      * hibernate and jackrabbit stores at the moment.
      * 
      * @param id
      * @return
      */
     private String detectStore(String id)
     {
         if (IdentifierUtils.isUUID(id))
             return TypeDef.JACKRABBIT;
         else
             return TypeDef.HIBERNATE;
     }
 
     private Object getTypeDef(Object item)
     {
         if (item instanceof BaseNode)
         {
             return ((BaseNode) item).getTypeDef();
         }
         else
         {
             return typeService.getType(item.getClass());
         }
     }
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
     public void setDaoService(DaoService daoService)
     {
         this.daoService = daoService;
     }
 
     public void setBindService(BindService bindService)
     {
         this.bindService = bindService;
     }
 
     public void setValidatorService(ValidatorService validatorService)
     {
         this.validatorService = validatorService;
     }
 
     public void setHibernateDataStore(HibernateDataStore hibernateDataStore)
     {
         this.hibernateDataStore = hibernateDataStore;
     }
 
     public void setJackrabbitDataStore(JackrabbitDataStore jackrabbitDataStore)
     {
         this.jackrabbitDataStore = jackrabbitDataStore;
     }
 
 }
