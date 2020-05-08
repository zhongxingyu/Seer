 package oshop.web.api.rest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.MatrixVariable;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import oshop.dao.GenericDao;
 import oshop.dao.GenericSearchDao;
 import oshop.model.Item;
 import oshop.model.ItemCategory;
 import oshop.web.api.dto.GenericListDto;
 import oshop.web.api.rest.adapter.EmptyResultCheckRestCallbackAdapter;
 import oshop.web.api.rest.adapter.ReturningRestCallbackAdapter;
 import oshop.web.api.rest.adapter.ValidationRestCallbackAdapter;
 import oshop.web.api.rest.adapter.VoidRestCallbackAdapter;
 
 import javax.annotation.Resource;
 import javax.validation.Valid;
 import java.util.List;
 import java.util.Map;
 
 @Controller
 @RequestMapping("/api/itemCategories")
 @Transactional(readOnly = true)
 public class ItemCategoryController {
 
     private static final Log log = LogFactory.getLog(ItemCategoryController.class);
 
     @Resource
     private GenericDao<ItemCategory, Integer> itemCategoryDao;
 
     @Resource
     private GenericSearchDao searchDao;
 
     @Resource
     private GenericDao<Item, Integer> itemDao;
 
     @RequestMapping(
             value = "/",
             method = RequestMethod.POST,
             consumes = {MediaType.APPLICATION_JSON_VALUE},
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     @Transactional(readOnly = false)
     public ResponseEntity<?> add(@RequestBody @Valid final ItemCategory itemCategory, final BindingResult result) {
         return new ValidationRestCallbackAdapter(result,
                 new ReturningRestCallbackAdapter<ItemCategory>() {
                     @Override
                     protected ItemCategory getResult() throws Exception {
                         Integer id = itemCategoryDao.add(itemCategory);
                         return itemCategoryDao.get(id);
                     }
                 }).invoke();
     }
 
     @RequestMapping(
             value = "/{id}",
             method = RequestMethod.DELETE
             /*consumes = {MediaType.APPLICATION_JSON_VALUE}*/)
     @Transactional(readOnly = false)
     public ResponseEntity<?> delete(@PathVariable final Integer id) {
         return new VoidRestCallbackAdapter() {
             @Override
             protected void perform() throws Exception {
                 itemCategoryDao.remove(id);            }
         }.invoke();
     }
 
     @RequestMapping(
             value = "/{id}",
             method = RequestMethod.GET,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     public ResponseEntity<?> get(@PathVariable final Integer id) {
         return new ReturningRestCallbackAdapter<ItemCategory>() {
             @Override
             protected ItemCategory getResult() throws Exception {
                 return itemCategoryDao.get(id);
             }
         }.invoke();
     }
 
     @RequestMapping(
             value = "/{id}",
             method = RequestMethod.PUT,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     public ResponseEntity<?> update(
             @PathVariable final Integer id,
             @RequestBody @Valid final ItemCategory itemCategory, final BindingResult result) {
 
         return new ValidationRestCallbackAdapter(result,
                 new ReturningRestCallbackAdapter<ItemCategory>() {
                     @Override
                     protected ItemCategory getResult() throws Exception {
                         itemCategory.setId(id);
                         itemCategoryDao.update(itemCategory);
                         return itemCategoryDao.get(id);
                     }
                 }).invoke();
     }
 
     @RequestMapping(
             value = "/",
             method = RequestMethod.GET,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     public ResponseEntity<?> list(
             @RequestParam(value = "limit", required = false) final Integer limit,
             @RequestParam(value = "offset", required = false) final Integer offset) {
 
         return new EmptyResultCheckRestCallbackAdapter<GenericListDto<List<ItemCategory>>>() {
             @Override
             protected GenericListDto<List<ItemCategory>> getResult() throws Exception {
                 Number size = searchDao.get(itemCategoryDao.createCriteria().setProjection(Projections.rowCount()));
                 List<ItemCategory> list = itemCategoryDao.list(itemCategoryDao.createCriteria(), offset, limit);
 
                 return new GenericListDto<List<ItemCategory>>(list, size);
             }
         }.invoke();
     }
 
     @RequestMapping(
             value = "/{filter}/{sort}",
             method = RequestMethod.GET,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     public ResponseEntity<?> listWithFiltersAndSorters(
             @MatrixVariable(pathVar="filter", required = false) final Map<String, List<String>> filters,
             @MatrixVariable(pathVar="sort", required = false) final Map<String, List<String>> sorters,
             @RequestParam(value = "limit", required = false) final Integer limit,
             @RequestParam(value = "offset", required = false) final Integer offset) {
 
         return new EmptyResultCheckRestCallbackAdapter<GenericListDto<List<ItemCategory>>>() {
             @Override
             protected GenericListDto<List<ItemCategory>> getResult() throws Exception {
                 Criteria criteria = itemCategoryDao.createCriteria();
 
                 ControllerUtils.applyFilters(filters, criteria);
                 ControllerUtils.applySorters(sorters, criteria);
 
                 Integer count = itemCategoryDao.list(criteria).size();
                 List<ItemCategory> list = itemCategoryDao.list(criteria, offset, limit);
 
                 return new GenericListDto<List<ItemCategory>>(list, count);
             }
         }.invoke();
     }
 
     @RequestMapping(
             value = "/{id}/items",
             method = RequestMethod.GET,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     //TODO: use ResponseEntity<?>
     public List<Item> items(
             @PathVariable Integer id,
             @RequestParam(value = "limit", required = false) Integer limit,
             @RequestParam(value = "offset", required = false) Integer offset) {
 
         Criteria criteria = itemDao.createCriteria();
         criteria.createAlias("category", "c").add(Restrictions.eq("c.id", id));
         return itemDao.list(criteria, offset, limit);
     }
 
     @RequestMapping(
             value = "/{id}/items/{filter}/{sort}",
             method = RequestMethod.GET,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     //TODO: use ResponseEntity<?>
     public List<Item> itemsWithFiltersAndSorters(
             @PathVariable Integer id,
             @MatrixVariable(pathVar="filter", required = false) Map<String, List<String>> filters,
             @MatrixVariable(pathVar="sort", required = false) Map<String, List<String>> sorters,
             @RequestParam(value = "limit", required = false) Integer limit,
             @RequestParam(value = "offset", required = false) Integer offset) {
 
         Criteria criteria = itemDao.createCriteria();
         criteria.createAlias("category", "c").add(Restrictions.eq("c.id", id));
 
         ControllerUtils.applyFilters(filters, criteria);
         ControllerUtils.applySorters(sorters, criteria);
 
         return itemDao.list(criteria, offset, limit);
     }
 }
