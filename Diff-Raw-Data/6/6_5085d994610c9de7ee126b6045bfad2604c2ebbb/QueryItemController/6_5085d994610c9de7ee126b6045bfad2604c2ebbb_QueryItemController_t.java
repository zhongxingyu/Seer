 package com.abudko.reseller.huuto.mvc.item;
 
 import static com.abudko.reseller.huuto.mvc.list.SearchControllerConstants.SEARCH_PARAMS_ATTRIBUTE;
 import static com.abudko.reseller.huuto.mvc.list.SearchControllerConstants.SEARCH_RESULTS_ATTRIBUTE;
 
 import javax.annotation.Resource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import com.abudko.reseller.huuto.mvc.list.SearchController;
 import com.abudko.reseller.huuto.order.ItemOrder;
 import com.abudko.reseller.huuto.query.html.item.ItemInfo;
 import com.abudko.reseller.huuto.query.html.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.item.QueryItemService;
 
 @Controller
 @SessionAttributes({ SEARCH_RESULTS_ATTRIBUTE, SEARCH_PARAMS_ATTRIBUTE })
 public class QueryItemController {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     @Resource
     private QueryItemService queryItemService;
 
     @RequestMapping(value = "/item", method = RequestMethod.GET)
    public String extractItem(@RequestParam String url, @RequestParam String brand, @RequestParam String size,
            @RequestParam String newPrice, Model model) {
         log.info(String.format("Handling item request with parameter %s", url));
 
         ItemResponse response = queryItemService.extractItem(url);
        setInfo(response, brand, size, newPrice);
 
         ItemOrder order = createItemOrderFor(response);
         model.addAttribute("itemOrder", order);
 
         SearchController.setEnumConstantsToRequest(model);
 
         return "order/order";
     }
 
     private void setInfo(ItemResponse response, String brand, String size, String newPrice) {
         ItemInfo itemInfo = new ItemInfo(brand, size, newPrice);
         response.setItemInfo(itemInfo);
     }
 
     private ItemOrder createItemOrderFor(ItemResponse response) {
         ItemOrder order = new ItemOrder();
         order.setItemResponse(response);
         return order;
     }
 }
