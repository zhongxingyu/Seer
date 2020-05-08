 package com.ted.xplatform.service;
 
 import java.io.Serializable;
 import java.util.List;
import java.util.ArrayList;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.google.common.collect.Lists;
 import com.ted.common.dao.jdbc.JdbcTemplateDao;
 import com.ted.common.dao.jpa.JpaSupportDao;
 import com.ted.common.dao.jpa.JpaTemplateDao;
 import com.ted.common.util.BeanUtils;
 import com.ted.common.util.CollectionUtils;
 import com.ted.xplatform.pojo.common.MenuResource;
 
 /**
  * 菜单的Service
  */
 @Transactional
 @Service("menuResourceService")
 public class MenuResourceService {
     final Logger               logger        = LoggerFactory.getLogger(MenuResourceService.class);
     public static final String SUBMENUS_JPQL = "from MenuResource m where m.parent.id=:resourceId or (m.parent.id is null and :resourceId is null)  order by m.idx asc";
 
     @Inject
     JdbcTemplateDao            jdbcTemplateDao;
 
     @Inject
     JpaSupportDao              jpaSupportDao;
 
     @Inject
     JpaTemplateDao             jpaTemplateDao;
 
     @Inject
     MessageSource              messageSource;
     
     @Inject
     ResourceService            resourceService;
 
     public void setJdbcTemplateDao(JdbcTemplateDao jdbcTemplateDao) {
         this.jdbcTemplateDao = jdbcTemplateDao;
     }
 
     public void setJpaSupportDao(JpaSupportDao jpaSupportDao) {
         this.jpaSupportDao = jpaSupportDao;
     }
 
     public void setJpaTemplateDao(JpaTemplateDao jpaTemplateDao) {
         this.jpaTemplateDao = jpaTemplateDao;
     }
 
     public void setMessageSource(MessageSource messageSource) {
         this.messageSource = messageSource;
     }
 
     public void setResourceService(ResourceService resourceService) {
         this.resourceService = resourceService;
     }
 
     //-----------------工具方法-----------------//
     /**
      * 工具方法:根据当前用户过滤菜单
      * 注意：这个只能在事务中运行。并且不过滤admin
      * @param List<MenuResource> menuResourceList
      * @return List<MenuResource>
      */
     @Transactional(readOnly = true)
     private List<MenuResource> filterMenuResourceByCurrentSubject(List<MenuResource> menuResourceList) {
         Subject currentUser = SecurityUtils.getSubject();
         List<MenuResource> filteredMenuList = Lists.newArrayList();
         for (MenuResource menu : menuResourceList) {
             if (resourceService.hasViewPermission(currentUser, menu)) {
                 filteredMenuList.add(menu);
             }
         }
         return filteredMenuList;
     };
 
     //--------------------业务方法用public,否则用private,但是除了依赖注入方法menuResource的显示,后台管理的级联授权的部分--------------------//
     /**
      * 根据上一级MenuId，根据当前用户，得到所有的一级孩子,并且过滤掉没有权限的菜单。
      * @param String menuid
      * @return List<MenuResource>
      */
     @Transactional(readOnly = true)
     public List<MenuResource> getMenusByParentIdFilterByCurrentSubject(Serializable menuid) {
         List<MenuResource> menuResourceList = getSubMenuResourceListByResourceId(menuid);
         return filterMenuResourceByCurrentSubject(menuResourceList);
     };
 
     /**
      * 根据上一级MenuId，根据当前用户，得到所有的一级孩子,并且过滤掉没有权限的菜单。并且是带checkbox的acl
      * @param menuid
      * @return List<MenuResource>
      */
     @Transactional(readOnly = true)
     public List<MenuResource> getMenusLoadOperationsByParentIdFilterByCurrentSubject(Serializable menuid) {
         List<MenuResource> filteredMenuList = getMenusByParentIdFilterByCurrentSubject(menuid);
         resourceService.loadOperations(filteredMenuList);
         return filteredMenuList;
     };
 
     /**
      * 级联Menu：根据上一级MenuId，根据当前用户，得到所有的一级孩子,并且过滤掉没有权限的菜单。并且是带checkbox的acl
      * <b>注意:</b>由于这部分涉及到系统管理员admin，他建了一个menu后，是看不到的，因为还没有权限，故，admin有权限看到所有，也就是不过滤admin
      * @param menuid
      * @return List<MenuResource>
      */
     @Transactional(readOnly = true)
     public List<MenuResource> getSubMenusCascadeLoadOperationsByParentIdFilterByCurrentSubject(Serializable menuid) {
         List<MenuResource> menuResourceList = getMenusLoadOperationsByParentIdFilterByCurrentSubject(menuid);
         for (MenuResource menu : menuResourceList) {
 			if(!menu.isLeaf()){ //modify 20130524
 				List<MenuResource> subMenuResourceList = getSubMenusCascadeLoadOperationsByParentIdFilterByCurrentSubject(menu.getId());
 				menu.setSubMenuResources(subMenuResourceList);
 			}else{
        		menu.setSubMenuResources(new ArrayList<MenuResource>());//zhang add 20130524
         	}
         }
         return menuResourceList;
     };
 
     /**
      * 级联Menu：根据上一级MenuId，根据当前用户，得到所有的一级孩子,并且过滤掉没有权限的菜单。不带checkbox,without operation
      * @param menuid
      * @return List<MenuResource>
      */
     @Transactional(readOnly = true)
     public List<MenuResource> getSubMenusCascadeByParentIdFilterByCurrentSubject(Serializable menuid) {
         List<MenuResource> menuResourceList = getMenusByParentIdFilterByCurrentSubject(menuid);
         for (MenuResource menu : menuResourceList) {
 			if(!menu.isLeaf()){ //modify 20130524
 				List<MenuResource> subMenuResourceList = getSubMenusCascadeByParentIdFilterByCurrentSubject(menu.getId());
 				menu.setSubMenuResources(subMenuResourceList);
 			}else{
        		menu.setSubMenuResources(new ArrayList<MenuResource>());//zhang add 20130524
         	}
         }
         return menuResourceList;
     };
 
     ///===========================MenuResource的管理================================//
     /**
      * 根据resourceId(MenuResource 中的ID)得到下面的所有的MenuResource，不级联。
      * @param resourceId
      * @return List<MenuResource>
      */
     @Transactional(readOnly = true)
     public List<MenuResource> getSubMenuResourceListByResourceId(Serializable resourceId) {
         // List<MenuResource> all = jpaSupportDao.getAll(MenuResource.class);
         //List<MenuResource> subMenuResources = all.get(0).getSubMenuResources();
         Map<String, Object> newMap = CollectionUtils.newMap("resourceId", resourceId);
         List<MenuResource> subMenuResourceList = jpaSupportDao.find(SUBMENUS_JPQL, newMap);
         return subMenuResourceList;
     };
 
     /**
      * 根据resourceId得到一个菜单的详细信息
      * @param resourceId
      * @return List<MenuResource>
      */
     @Transactional(readOnly = true)
     public MenuResource getMenuResourceById(Long resourceId) {
         if (null == resourceId) {
             return null;
         }
         MenuResource menuResource = (MenuResource) jpaSupportDao.getEntityManager().find(MenuResource.class, resourceId);
         menuResource.loadParentName();
         menuResource.loadOperations2Properties();
         return menuResource;
     };
 
     /**
      * 保存，包括新增和修改
      * 注意：要saveParentId
      */
     @Transactional
     public MenuResource save(MenuResource menuResource) {
         //判断是add还是update
         if (menuResource.isNew()) {//add
             //if (null != menuResource.getParent() && null != menuResource.getParent().getId()) {
             if (null != menuResource.getParentId()) {
                 MenuResource parentMenuResource = (MenuResource) jpaSupportDao.getEntityManager().find(MenuResource.class, menuResource.getParentId());
                 if (null != parentMenuResource) {
                     parentMenuResource.setLeaf(false);
                 }
                 menuResource.setParent(parentMenuResource);
             }
             resourceService.addOperationProperties2Operations(menuResource);
             jpaSupportDao.getEntityManager().persist(menuResource);
         } else {//update
             MenuResource dbMenuResource = (MenuResource) jpaSupportDao.getEntityManager().find(MenuResource.class, menuResource.getId());
             //DozerUtils.copy(menuResource, dbMenuResource);//这个不好用,copy all
             BeanUtils.copyPropertiesByInclude(dbMenuResource, menuResource, new String[] { "code", "name", "description", "path", "iconCls", "icon", "icon2", "icon3", "favorite", "quicktip", "idx", "leaf", "canView" });
             resourceService.updateOperationProperties2Operations(dbMenuResource);
             jpaSupportDao.getEntityManager().merge(dbMenuResource);
         }
         return menuResource;
     };
 
     /**
      * 删除，
      * 注意：是否要级联删除: 级联删除下属组织菜单。这个是通过MenuResource的pojo配置来实现的。
      * 要手工删除角色对应的acl关系，也就是通过控制Role来达到删除role_acl的目的。
      */
     @Transactional
     public void delete(Long resourceId) {
         resourceService.delete(resourceId);
     }
 
     /**
      * Menu的移动:逻辑是这样的：把sourceMenuResource的所有孩子，都放到sourceMenuResource.parent下。然后把sourceMenuResource放到destMenuResource下。
      */
     public void move(Long sourceResourceId, Long destResourceId) {
         MenuResource sourceMenu = (MenuResource) jpaSupportDao.getEntityManager().find(MenuResource.class, sourceResourceId);
         MenuResource destMenu = (MenuResource) jpaSupportDao.getEntityManager().find(MenuResource.class, destResourceId);
 
         //把sourceMenu的所有孩子，都放到sourceMenu.parent下
         MenuResource sourceParentMenu = sourceMenu.getParent();
         sourceParentMenu.getSubMenuResources().addAll(sourceMenu.getSubMenuResources());
         for (MenuResource son : sourceMenu.getSubMenuResources()) {
             son.setParent(sourceParentMenu);
         }
 
         //然后把sourceMenu放到destMenu下。
         sourceMenu.setSubMenuResources(null);
         sourceMenu.setParent(destMenu);
         destMenu.setLeaf(false);
         destMenu.getSubMenuResources().add(sourceMenu);
 
         //保存sourceParentMenu, destMenu
         jpaSupportDao.getEntityManager().persist(sourceParentMenu);
         jpaSupportDao.getEntityManager().persist(destMenu);
     };
 }
