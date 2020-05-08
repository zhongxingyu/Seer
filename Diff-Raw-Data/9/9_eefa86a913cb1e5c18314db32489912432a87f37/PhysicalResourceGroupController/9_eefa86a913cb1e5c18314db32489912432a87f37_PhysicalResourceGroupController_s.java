 package nl.surfnet.bod.web;
 
 import java.util.Collection;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.service.PhysicalResourceGroupServiceImpl;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @RequestMapping("/noc/physicalresourcegroups")
 @Controller
 public class PhysicalResourceGroupController {
 
     @Autowired
     private PhysicalResourceGroupServiceImpl physicalResourceGroupService;
 
     @RequestMapping(method = RequestMethod.POST)
     public String create(@Valid final PhysicalResourceGroup physicalResourceGroup, final BindingResult bindingResult,
             final Model uiModel, final HttpServletRequest httpServletRequest) {
 
         if (bindingResult.hasErrors()) {
             uiModel.addAttribute("physicalResourceGroup", physicalResourceGroup);
             return "physicalresourcegroups/create";
         }
 
         uiModel.asMap().clear();
         physicalResourceGroupService.save(physicalResourceGroup);
 
         // Do not return to the create instance, but to the list view
         return "redirect:physicalresourcegroups/";
     }
 
     @RequestMapping(params = "form", method = RequestMethod.GET)
     public String createForm(final Model uiModel) {
         uiModel.addAttribute("physicalResourceGroup", new PhysicalResourceGroup());
         return "physicalresourcegroups/create";
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public String show(@PathVariable("id") final Long id, final Model uiModel) {
         uiModel.addAttribute("physicalresourcegroup", physicalResourceGroupService.find(id));
         uiModel.addAttribute("itemId", id);
         return "physicalresourcegroups/show";
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String list(@RequestParam(value = "page", required = false) final Integer page,
             @RequestParam(value = "size", required = false) final Integer size, final Model uiModel) {
 
         if (page != null || size != null) {
             int sizeNo = size == null ? 10 : size.intValue();
             final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
             uiModel.addAttribute("physicalresourcegroups",
                     physicalResourceGroupService.findEntries(firstResult, sizeNo));
             float nrOfPages = (float) physicalResourceGroupService.count() / sizeNo;
             uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                     : nrOfPages));
         } else {
             uiModel.addAttribute("physicalresourcegroups", physicalResourceGroupService.findAll());
         }
         return "physicalresourcegroups/list";
     }
 
     @RequestMapping(method = RequestMethod.PUT)
     public String update(@Valid final PhysicalResourceGroup physicalResourceGroup, final BindingResult bindingResult,
             final Model uiModel, final HttpServletRequest httpServletRequest) {
 
         if (bindingResult.hasErrors()) {
             uiModel.addAttribute("physicalResourceGroup", physicalResourceGroup);
             return "physicalresourcegroups/update";
         }
         uiModel.asMap().clear();
         physicalResourceGroupService.update(physicalResourceGroup);
         return "redirect:physicalresourcegroups/"
                 + HttpRequestUtils.encodeUrlPathSegment(physicalResourceGroup.getId().toString(), httpServletRequest);
     }
 
     @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
     public String updateForm(@PathVariable("id") final Long id, final Model uiModel) {
         uiModel.addAttribute("physicalResourceGroup", physicalResourceGroupService.find(id));
         return "physicalresourcegroups/update";
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
     public String delete(@PathVariable("id") final Long id,
             @RequestParam(value = "page", required = false) final Integer page,
             @RequestParam(value = "size", required = false) final Integer size, final Model uiModel) {
         PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(id);
         physicalResourceGroupService.delete(physicalResourceGroup);
         uiModel.asMap().clear();
         uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
         uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:physicalresourcegroups";
     }
 
     @ModelAttribute("physicalresourcegroups")
     public Collection<PhysicalResourceGroup> populatePhysicalResourceGroups() {
         return physicalResourceGroupService.findAll();
     }
 }
