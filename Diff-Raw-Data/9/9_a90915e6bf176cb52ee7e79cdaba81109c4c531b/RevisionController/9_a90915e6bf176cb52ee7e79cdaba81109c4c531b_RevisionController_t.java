 package cz.muni.fi.pa165.pujcovnaStroju.web.controller;
 
 import cz.muni.fi.pa165.pujcovnaStroju.web.converter.StringToSystemUserTypeEnumDTOConverter;
import cz.muni.fi.pa165.pujcovnastroju.converter.UserTypeDTOConverter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import cz.muni.fi.pa165.pujcovnastroju.dto.MachineDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.RevisionDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.SystemUserDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.UserTypeEnumDTO;
import cz.muni.fi.pa165.pujcovnastroju.entity.UserTypeEnum;
 import cz.muni.fi.pa165.pujcovnastroju.service.MachineService;
 import cz.muni.fi.pa165.pujcovnastroju.service.RevisionService;
 import cz.muni.fi.pa165.pujcovnastroju.service.SystemUserService;
 
 /**
  * revision Controller implementation
  * 
  * @author Matej Fucek
  */
 @Controller
 @RequestMapping("/revision")
 public class RevisionController {
 
 	private RevisionService revisionService;
 	private MachineService machineService;
 	private SystemUserService userService;
 
 	@Autowired
 	public RevisionController(RevisionService revisionService,
 			MachineService machineService, SystemUserService userService) {
 		this.revisionService = revisionService;
 		this.machineService = machineService;
 		this.userService = userService;
 	}
 
 	@RequestMapping("")
 	public String redirectToList(ModelMap model) {
 		return "redirect:/revision/list";
 	}
 
 	@RequestMapping("/")
 	public String redirectToListBackslash(ModelMap model) {
 		return "redirect:/revision/list";
 	}
 
 	@RequestMapping(value = "/list")
 	public ModelAndView listRevisions(
 			ModelMap model,
 			@RequestParam(value = "storeStatus", required = false, defaultValue = "") String storeStatus,
 			@RequestParam(value = "deleteStatus", required = false, defaultValue = "") String deleteStatus,
 			@RequestParam(value = "updateStatus", required = false, defaultValue = "") String updateStatus,
 			@RequestParam(value = "errorMessage", required = false, defaultValue = "") String errorMessage) {
 
 		model.addAttribute("revisions",
 				revisionService.findAllrevisionsBizRevision());
 		model.addAttribute("list", "list of revisions");
 		model.addAttribute("pageTitle", "lang.listRevisionsTitle");
 		DefaultController.addHeaderFooterInfo(model);
 
 		model.addAttribute("machines", machineService.getAllMachines());
		model.addAttribute("users", userService.getSystemUsersByParams(null, null, 
                        UserTypeDTOConverter.entityToDto(UserTypeEnum.REVISIONER)));
 
 		if (storeStatus.equalsIgnoreCase("true")) {
 			model.addAttribute("storeStatus", "true");
 		}
 		if (storeStatus.equalsIgnoreCase("false")) {
 			model.addAttribute("storeStatus", "false");
 			model.addAttribute("errorMessage", errorMessage);
 		}
 		if (deleteStatus.equalsIgnoreCase("true")) {
 			model.addAttribute("deleteStatus", "true");
 		}
 
 		if (deleteStatus.equalsIgnoreCase("false")) {
 			model.addAttribute("deleteStatus", "false");
 			model.addAttribute("errorMessage", errorMessage);
 		}
 
 		if (updateStatus.equalsIgnoreCase("true")) {
 			model.addAttribute("updateStatus", "true");
 		}
 
 		if (updateStatus.equalsIgnoreCase("false")) {
 			model.addAttribute("updateStatus", "false");
 			model.addAttribute("errorMessage", errorMessage);
 		}
 		return new ModelAndView("listRevisions", "command", new RevisionDTO());
 	}
 
 	@RequestMapping(value = "/add", method = RequestMethod.POST)
 	public String addRevision(@ModelAttribute("revision") RevisionDTO revision,
 			BindingResult result, ModelMap model) {
 		boolean stored = false;
 		String errorMsg = null;
 
 		// empty objects with id only
 		MachineDTO machine = revision.getMachine();
 		SystemUserDTO user = revision.getSystemUser();
 
 		try {
 			machine = machineService.read(machine.getId());
 			if (machine == null) {
 				errorMsg = "Machine not found";
 			}
 			if (user == null) {
 				errorMsg = "User not found";
 			}
 			user = userService.read(user.getId());
 			revision.setMachine(machine);
 			revision.setSystemUser(user);
 
 			stored = revisionService.createBizRevision(revision) != null;
 		} catch (DataAccessException e) {
 			stored = false;
 			if (errorMsg != null)
 				errorMsg = e.getMessage();
 		}
 		model.addAttribute("storeStatus", stored);
 		if (errorMsg != null) {
 			model.addAttribute("errorMessage", errorMsg);
 		}
 		return "redirect:list";
 	}
 
 	@RequestMapping("/detail/{id}")
 	public String viewRevision(@PathVariable String id, ModelMap model) {
 		DefaultController.addHeaderFooterInfo(model);
 		model.addAttribute("pageTitle", "lang.detailRevisionTitle");
 		RevisionDTO revision = null;
 		boolean found = false;
 		try {
 			Long revID = Long.valueOf(id);
 			revision = revisionService.readBizRevision(revID);
 			found = true;
 		} catch (DataAccessException | NumberFormatException e) {
 			// TODO log
 		}
 		model.addAttribute("revision", revision);
 		if (!found) {
 			model.addAttribute("id", id);
 		}
 		return "revisionDetail";
 	}
 
 	@RequestMapping(value = "/delete/{id}")
 	public String deleteRevision(@PathVariable String id, ModelMap model) {
 		boolean deleted = false;
 		String errorMsg = null;
 		RevisionDTO revisionDTO = new RevisionDTO();
 		try {
 			Long revID = Long.valueOf(id);
 			revisionDTO = revisionService.readBizRevision(revID);
 			revisionService.deleteBizRevision(revisionDTO);
 			deleted = true;
 		} catch (DataAccessException | NumberFormatException
 				| NullPointerException e) {
 			// TODO log
 			deleted = false;
 			errorMsg = e.getMessage();
 		}
 		model.addAttribute("deleteStatus", deleted);
 		if (errorMsg != null) {
 			model.addAttribute("errorMessage", errorMsg);
 		}
 		return "redirect:/revision/list";
 	}
 
 	@RequestMapping(value = "/update/{id}")
 	public ModelAndView updateRevision(@PathVariable String id, ModelMap model) {
 		DefaultController.addHeaderFooterInfo(model);
 		model.addAttribute("pageTitle", "lang.updateRevisionTitle");
 		RevisionDTO revision = null;
 		boolean found = false;
 		try {
 			System.err.println("blabla1");
 			Long revID = Long.valueOf(id);
 			revision = revisionService.readBizRevision(revID);
 			found = true;
 		} catch (DataAccessException | NumberFormatException e) {
 			// TODO log
 		}
 		model.addAttribute("revision", revision);
 		if (found) {
 			model.addAttribute("machine", revision.getMachine());
 			model.addAttribute("user", revision.getSystemUser());
 			model.addAttribute("revision", revision);
 		} else {
 			model.addAttribute("id", id);
 		}
 		return new ModelAndView("updateRevision", "command", new RevisionDTO());
 	}
 
 	@RequestMapping(value = "/update/update", method = RequestMethod.POST)
 	public String editRevision(
 			@ModelAttribute("revision") RevisionDTO revision,
 			BindingResult result, ModelMap model) {
 		boolean updated = false;
 		String errorMsg = null;
 		MachineDTO machine = revision.getMachine();
 		SystemUserDTO user = revision.getSystemUser();
 		try {
 			System.err.println("blabla2");
 
 			machine = machineService.read(machine.getId());
 			if (machine == null) {
 				errorMsg = "Machine not found";
 			}
 			if (user == null) {
 				errorMsg = "User not found";
 			}
 			user = userService.read(user.getId());
 			revision.setMachine(machine);
 			revision.setSystemUser(user);
 			updated = revisionService.updateBizRevision(revision) != null;
 		} catch (DataAccessException e) {
 			updated = false;
 			errorMsg = e.getMessage();
 		}
 		model.addAttribute("updateStatus", updated);
 		if (errorMsg != null) {
 			model.addAttribute("errorMessage", errorMsg);
 		}
 		return "redirect:/revision/list";
 	}
 
 }
