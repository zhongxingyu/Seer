 package com.orangeleap.tangerine.service.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import com.orangeleap.tangerine.util.OLLogger;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.orangeleap.tangerine.dao.CacheGroupDao;
 import com.orangeleap.tangerine.dao.PicklistDao;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.domain.customization.PicklistItem;
 import com.orangeleap.tangerine.service.AuditService;
 import com.orangeleap.tangerine.service.PicklistItemService;
 import com.orangeleap.tangerine.type.CacheGroupType;
 import com.orangeleap.tangerine.util.OLLogger;
 
 /*
  * Manages picklist items for site.
  */
 
 @Service("picklistItemService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class PicklistItemServiceImpl extends AbstractTangerineService implements PicklistItemService {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
     
     
     @Resource(name = "auditService")
     private AuditService auditService;
 
     @Resource(name = "picklistDAO")
     private PicklistDao picklistDao;
     
     @Resource(name = "cacheGroupDAO")
     private CacheGroupDao cacheGroupDao;
     
 
 	private boolean exclude(Picklist picklist) {
 		String name = picklist.getPicklistName();
 		return name == null 
 			|| name.equals("frequency")
 			|| name.equals("constituentType")
 		;
 	}
 
 	@Override
     @Transactional(propagation = Propagation.REQUIRED)
 	public Picklist getPicklistById(Long picklistId) {
		return picklistDao.readPicklistById(picklistId);
 	}
 	
 	@Override
     @Transactional(propagation = Propagation.REQUIRED)
 	public Picklist getPicklist(String picklistNameId) {
 		
 		if (picklistNameId == null) {
             return null;
         }
 		
 		Picklist picklist = populateCustomFieldsOnDependentItems(picklistDao.readPicklistByNameId(picklistNameId));
 		if (picklist == null) {
             return null;
         }
 		
 		if (picklist.getSite() == null) {
 			picklist = createCopy(picklist);
 			return picklistDao.maintainPicklist(picklist);
 		} else if (picklist.getSite().getName().equals(getSiteName())) {
 			return picklist;
 		} else {
 			return null;
 		}
 		
 	}
 	
 	@Override
     @Transactional(propagation = Propagation.REQUIRED)
 	public PicklistItem getPicklistItem(String picklistNameId, String picklistItemName) {
 		return picklistDao.readPicklistItemByName(picklistNameId, picklistItemName);
 	}
 	
 	@Override
 	public PicklistItem getPicklistItemByDefaultDisplayValue(String picklistNameId, String defaultDisplayValue) {
 		return picklistDao.readPicklistItemByDefaultValue(picklistNameId, defaultDisplayValue);
 	}
 	
 	@Override
     @Transactional(propagation = Propagation.REQUIRED)
 	public PicklistItem getPicklistItem(Long picklistItemId) {
 		return picklistDao.readPicklistItemById(picklistItemId);
 	}
 	
 	@Override
     @Transactional(propagation = Propagation.REQUIRED)
 	public List<PicklistItem> getPicklistItems(String picklistNameId, String picklistItemName, String description, Boolean showInactive) {
 		Picklist picklist = populateCustomFieldsOnDependentItems(picklistDao.readPicklistByNameId(picklistNameId));
 		List<PicklistItem> result = new ArrayList<PicklistItem>();
 		for (PicklistItem item : picklist.getPicklistItems()) {
 			if (showInactive || !item.isInactive()) {
 				if (description.length() > 0) {
 					if (item.getDefaultDisplayValue().contains(description)) {
 						result.add(item);
 					}
 				} else {
 					if (item.getItemName().contains(picklistItemName)) {
 						result.add(item);
 					}
 				}
 			}
 		}
 		return result;
 	}
 	
 	@Override
 	public List<PicklistItem> findCodeByDescription(String picklistNameId, String description, Boolean showInactive) {
 		Picklist picklist = populateCustomFieldsOnDependentItems(picklistDao.readPicklistByNameId(picklistNameId));
 		List<PicklistItem> result = new ArrayList<PicklistItem>();
 		for (PicklistItem item : picklist.getPicklistItems()) {
 			if (showInactive || !item.isInactive()) {
 				if (description != null && item.getLongDescription() != null) {
 					if (item.getLongDescription().toUpperCase().contains(description.toUpperCase())) {
 						result.add(item);
 					}
 				} 
 			}
 		}
 		return result;
 	}
 	
 	@Override
 	public List<PicklistItem> findCodeByValue(String picklistNameId, String value, Boolean showInactive) {
 		Picklist picklist = populateCustomFieldsOnDependentItems(picklistDao.readPicklistByNameId(picklistNameId));
 		List<PicklistItem> result = new ArrayList<PicklistItem>();
 		for (PicklistItem item : picklist.getPicklistItems()) {
 			if (showInactive || !item.isInactive()) {
 				if (value != null && item.getDefaultDisplayValue() != null) {
 					if (item.getDefaultDisplayValue().toUpperCase().contains(value.toUpperCase())) {
 						result.add(item);
 					}
 				} 
 			}
 		}
 		return result;
 	}
     
 	@Override
     @Transactional(propagation = Propagation.REQUIRED)
 	public List<Picklist> listPicklists() {
 		
 		List<Picklist> list = picklistDao.listPicklists();
 		Iterator<Picklist> it = list.iterator();
 		while (it.hasNext()) {
 			if (exclude(it.next())) {
                 it.remove();
             }
 		}
 		
 		// Overridden, site-specific picklists
 		List<Picklist> result = new ArrayList<Picklist>();
 		for (Picklist picklist : list) {
 			if (picklist.getSite() != null && picklist.getPicklistName().length() > 0) {
 				result.add(populateCustomFieldsOnDependentItems(picklist));
 			}
 		}
 		
 		// Non-overriden, global picklists
 		for (Picklist picklist : list) {
 			if (picklist.getSite() == null && picklist.getPicklistName().length() > 0) {
 				boolean found = false;
 				for (Picklist apicklist : result) {
                     if (apicklist.getPicklistDesc().equals(picklist.getPicklistDesc())) {
                         found = true;
                     }
                 }
 				if (!found) {
 					result.add(createCopy(picklist));
 				}
 			}
 		}
 		
 		Collections.sort(result, new Comparator<Picklist>() {
 			@Override
 			public int compare(Picklist o1, Picklist o2) {
 				return o1.getPicklistDesc().compareTo(o2.getPicklistDesc());
 			}
 		});
 		
 		return result;
 	}
 	
 	private Picklist createCopy(Picklist template) {
 	
 		// Need to create editable clones for all site == null picklists 
 		// Site == null items are global and should not be allowed to be edited.
 		
 		Picklist result = null;
 		try {
 			result = (Picklist)BeanUtils.cloneBean(template);
 			result.setId(null);
 			result.setSite(new Site(getSiteName()));
 			result.setPicklistItems(new ArrayList<PicklistItem>());
 			List<PicklistItem> items = template.getPicklistItems();
 			for (PicklistItem item: items) {
 				if (item != null ) item = picklistDao.readPicklistItemById(item.getId());  // This currently the only way to get custom fields on dependent objects
                 if (item != null ) {
                     PicklistItem newItem = (PicklistItem)item.createCopy();
                     newItem.setPicklistId(result.getId());
                     result.getPicklistItems().add(newItem);
                 }
 			}
 		} catch (Exception e) {
 			logger.error("Cannot create copy of default picklist." ,e);
     	}
 		return result;
 		
 	}
 	
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
 	public Picklist maintainPicklist(Picklist picklist) {
     	validate(picklist);
     	picklist = picklistDao.maintainPicklist(picklist);
     	cacheGroupDao.updateCacheGroupTimestamp(CacheGroupType.PICKLIST);
     	return picklist;
     }
     
     private void validate(Picklist picklist) {
     	if (picklist.getSite() == null || !picklist.getSite().getName().equals(getSiteName())) {
 			throw new RuntimeException("Cannot update non-site-specific entry for Picklist "+picklist.getId());
 		}
     	Iterator<PicklistItem> it = picklist.getPicklistItems().iterator();
         while (it.hasNext()) {
         	PicklistItem item = it.next();
         	if (item == null || item.getItemName() == null) it.remove();
         }
     }
     
     // Since this is now auto-generated based on the display value, ensure it's unique
     private void checkUnique(Picklist picklist, PicklistItem picklistItem) {
     	for (PicklistItem item : picklist.getPicklistItems()) {
     		if (item.getItemName().equals(picklistItem.getItemName())) {
     			picklistItem.setItemName(picklistItem.getItemName()+"0");
     		}
     	}
     }
 	
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public PicklistItem maintainPicklistItem(PicklistItem picklistItem) {
     	
 		if (StringUtils.trimToNull(picklistItem.getDefaultDisplayValue()) == null) {
 			throw new RuntimeException("Blank values not allowed");
 		}
     
     	// Sanity checks
     	if (picklistItem == null || picklistItem.getPicklistId() == null) {
     		throw new RuntimeException("PicklistItem is blank.");
     	}
     	
     	Picklist picklist = populateCustomFieldsOnDependentItems(picklistDao.readPicklistById(picklistItem.getPicklistId()));
     	validate(picklist);
 
 		if (StringUtils.trimToNull(picklistItem.getItemName()) == null) {
     		picklistItem.setItemName(picklistItem.getDefaultDisplayValue());
         	checkUnique(picklist, picklistItem);
     	}
 
 		logger.info("Updating "+picklist.getSite().getName()+" site-specific copy of picklist item "+picklistItem.getItemName());
 
 		boolean found = false;
 		Long id = picklistItem.getId();
     	if (id != null) {
             for (PicklistItem apicklistItem : picklist.getPicklistItems()) {
                 if (picklistItem.getItemName().equals(apicklistItem.getItemName())) {
                 	found = true;
                     try {
                     	Long origid = apicklistItem.getId();
             			BeanUtils.copyProperties(apicklistItem, picklistItem);
             			apicklistItem.setId(origid);
             		} catch (Exception e) {
             		}
                     break;
                 }
             }
         }
     	if (!found) {
             picklist.getPicklistItems().add(picklistItem);
         }
 
     	// Set order and reset item ids.
     	for (int i = 0; i < picklist.getPicklistItems().size(); i++) {
     		PicklistItem apicklistItem = picklist.getPicklistItems().get(i);
     		apicklistItem.setItemOrder(new Integer(i+1));
     	    apicklistItem.setPicklistId(picklist.getId());
     	}
         
     	picklist = picklistDao.maintainPicklist(picklist);
     	cacheGroupDao.updateCacheGroupTimestamp(CacheGroupType.PICKLIST);
 
 
  		// Audit
         for (PicklistItem apicklistItem: picklist.getPicklistItems()) {
         	if (apicklistItem.getItemName().equals(picklistItem.getItemName())) {
         		auditService.auditObject(apicklistItem); 
         		return apicklistItem;
         	}
         }
 
         return picklistItem;
     	
     }
 
     // Normally picklist item custom fields are not needed for screen display and validation, however they are needed in picklist maintenance.
     private Picklist populateCustomFieldsOnDependentItems(Picklist picklist) {
         if (picklist == null) return null;
         List<PicklistItem> items = picklist.getPicklistItems();
         for (int i = 0; i < items.size(); i++) {
             PicklistItem item = items.get(i);
             item = picklistDao.readPicklistItemById(item.getId());
             items.set(i, item);
         }
         return picklist;
     }
 
 
 }
