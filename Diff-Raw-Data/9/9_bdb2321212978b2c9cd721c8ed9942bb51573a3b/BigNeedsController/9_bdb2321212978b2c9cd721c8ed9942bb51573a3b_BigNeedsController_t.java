 package org.sukrupa.app.needs;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.sukrupa.bigneeds.BigNeed;
 import org.sukrupa.bigneeds.BigNeedRepository;
 
 import java.util.List;
 import java.util.Map;
 
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
 @Controller
 @RequestMapping("/bigneeds")
 public class BigNeedsController {
 
     private BigNeedRepository bigNeedRepository;
 
     @Autowired
     public BigNeedsController(BigNeedRepository bigNeedRepository) {
         this.bigNeedRepository = bigNeedRepository;
     }
 
     @RequestMapping
     public String list(Map<String, Object> model) {
         List<BigNeed> bigNeedList = bigNeedRepository.getList();
         model.put("bigNeedList", bigNeedList);
        return "bigNeeds/list";
     }
 
     @RequestMapping(value = "create", method = POST)
     public String create(@RequestParam String itemName, @RequestParam String cost, Map<String, Object> model) {
         model.put("message", "Added Successfully");
         bigNeedRepository.put(new BigNeed(itemName, Integer.parseInt(cost)));
         return list(model);
     }
 
 }
