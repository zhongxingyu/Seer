 package com.ssm.psm.web.controllers.organization;
 
 import com.ssm.psm.commons.enums.BasicStatus;
 import com.ssm.psm.commons.model.Organization;
 import com.ssm.psm.commons.model.UserInfo;
 import com.ssm.psm.commons.queryhelpers.PageableList;
 import com.ssm.psm.organization.OrganizationAccessFactory;
 import com.ssm.psm.organization.OrganizationAccessResolver;
 import com.ssm.psm.service.OrganizationSecurityService;
 import com.ssm.psm.service.OrganizationService;
 import com.ssm.psm.service.UserInfoService;
 import com.ssm.psm.service.UserOrganizationService;
 import com.ssm.psm.service.security.SortableGrantedAuthority;
 import com.ssm.psm.web.controllers.BaseController;
 import com.ssm.psm.web.forms.OrganizationFormBean;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.validation.Valid;
 
 /**
  * @author Christian
  */
 @Controller
 public class OrganizationController extends BaseController {
 
     private static final Log LOG = LogFactory.getLog(OrganizationController.class);
 
     @Autowired
     private OrganizationService organizationService;
 
     @Autowired
    private UserOrganizationService userOrganizationService;

    @Autowired
     private OrganizationSecurityService oss;
 
     @Autowired
     private UserInfoService userInfoService;
 
     @Autowired
     private MessageSource messageSource;
 
     /**
      * Retrieves the list of organizations mapped to the logged in user
      *
      * @param model  The Spring UI Model
      * @param pageNo The page number
      * @return
      */
     @RequestMapping("/organization/list")
     public String listOrganization(Model model, @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo) {
         SortableGrantedAuthority topRole = getTopRole();
         OrganizationAccessResolver resolver = OrganizationAccessFactory.getResolver(topRole.getAuthority());
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         final PageableList<Organization> organizationList = resolver.getOrganizationList(auth.getName(),
                 getPageableListParameters(pageNo));
 
         model.addAttribute("pList", organizationList);
 
         return "organization";
     }
 
     /**
      * Request handler for displaying the create organization form
      *
      * @param model The Spring UI Model
      * @return
      */
     @RequestMapping(value = "/organization/create", method = RequestMethod.GET)
     public String createOrganization(final Model model) {
         model.addAttribute("organizationForm", new OrganizationFormBean());
         return "createOrganization";
     }
 
     /**
      * The request handler for persisting an {@link Organization}.
      * <p/>
      * This request handler only accepts POST request.
      *
      * @param model            The Spring UI Model
      * @param organizationForm The form backing bean
      * @param result           The object containing the validation results
      * @param attr             The Spring {@link RedirectAttributes}
      * @return
      */
     @RequestMapping(value = "/organization/saveOrganization", method = RequestMethod.POST)
     public String saveOrganization(final Model model, @Valid @ModelAttribute("organizationForm") OrganizationFormBean
             organizationForm, final BindingResult result, final RedirectAttributes attr) {
 
         if (result.hasErrors()) {
             if (organizationForm.getOrgId() == null) {
                 return "createOrganization";
             }
 
             return "editOrganization";
         }
 
         //handler for creating new organization
         if (organizationForm.getOrgId() == null) {
             Organization organization = new Organization();
             organization.setName(organizationForm.getName());
             organization.setDescription(organizationForm.getDescription());
             organization.setParentOrgId(organizationForm.getParentOrgId());
             organization.setStatus(BasicStatus.ACTIVE);
 
             //find the info of the org admin
             UserInfo userInfo = userInfoService.getUserInfoByUsername(organizationForm.getEmailAddress());
 
             organizationService.addOrganization(getDataStore().getUserContextData(), organization, userInfo);
             attr.addFlashAttribute("success_msg", messageSource.getMessage("psm.ssm.organization.save.success.message",
                     null, null));
             return redirectTo(organizationForm.getParentOrgId());
         }
 
         //handler for updating existing organizations
         Organization o = organizationService.getOrganization(organizationForm.getOrgId());
         if (o == null) {
             return "invalidop";
         }
 
         //check if current user has rights to update the specified organization
         boolean isAllowed = oss.isEditAllowed(getDataStore().getUserContextData().getUserInfo().getUsername(),
                 o.getOrganizationId());
         if (isAllowed) {
             o.setName(organizationForm.getName());
             o.setDescription(organizationForm.getDescription());
 
             UserInfo newOrgAdmin = userInfoService.getUserInfoByUsername(organizationForm.getEmailAddress());
             organizationService.updateOrganization(getDataStore().getUserContextData(), o, newOrgAdmin);
             attr.addFlashAttribute("success_msg",
                     messageSource.getMessage("psm.ssm.organization.update.success.message", null, null));
             return redirectTo(organizationForm.getParentOrgId());
         }
 
         return "invalidop";
     }
 
     /**
      * Request handler for deleting an organization at the root level
      *
      * @param id   The organization to be deleted
      * @param attr The Spring {@link RedirectAttributes}
      * @return
      */
     @RequestMapping(value = "/organization/delete/{id}", method = {RequestMethod.GET, RequestMethod.POST})
     public String deleteOrganization(@PathVariable int id, final RedirectAttributes attr) {
         Organization organization = organizationService.getOrganization(id);
         organization.setStatus(BasicStatus.DELETED);
         organizationService.updateOrganization(getDataStore().getUserContextData(), organization);
 
         attr.addFlashAttribute("success_msg", messageSource.getMessage("psm.ssm.organization.delete.success.message",
                 null, null));
         return "redirect:/app/organization/list";
     }
 
     /**
      * @param model The Spring UI Model
      * @param id    The id of the organization to be edited
      * @return
      */
     @RequestMapping(value = "/organization/edit", method = {RequestMethod.GET, RequestMethod.POST})
     public String editOrganization(Model model, @RequestParam("orgId") int id) {
         try {
             boolean isAllowed = oss.isEditAllowed(getDataStore().getUserContextData().getUserInfo().getUsername(), id);
             if (isAllowed) {
                 Organization organization = organizationService.getOrganization(id);
 
                 OrganizationFormBean formBean = new OrganizationFormBean();
                 formBean.setOrgId(organization.getOrganizationId());
                 formBean.setName(organization.getName());
                 formBean.setDescription(organization.getDescription());
 
                 //get the organization's admin
                 UserInfo userInfo = organizationService.getAdministrator(organization.getOrganizationId());
 
                 //some organizations do not have an admin
                 if (userInfo != null) {
                     formBean.setEmailAddress(userInfo.getUsername());
                     formBean.setAdmin(userInfo.getFullname());
                 }
 
                 model.addAttribute("organizationForm", formBean);
                 return "editOrganization";
             }
         } catch (Exception e) {
             LOG.error("Error has occurred", e);
         }
        return "invalidop";
     }
 
     /**
      * If {@code parentOrgId} is null, we'll redirect to the root. Otherwise redirect to the parent
      *
      * @param parentOrgId
      * @return
      */
     private String redirectTo(Integer parentOrgId) {
         if (parentOrgId == null) return "redirect:/app/organization/list";
         return "redirect:/app/organization/" + parentOrgId;
     }
 }
