 package cz.muni.fi.pa165.pujcovnaStroju.web.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
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
 
 import cz.muni.fi.pa165.pujcovnaStroju.web.converter.StringToMachineTypeEnumDTOConverter;
 import cz.muni.fi.pa165.pujcovnaStroju.web.converter.StringToSystemUserTypeEnumDTOConverter;
 import cz.muni.fi.pa165.pujcovnastroju.dto.LoanDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.MachineDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.RevisionDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.SystemUserDTO;
 import cz.muni.fi.pa165.pujcovnastroju.entity.Loan;
 import cz.muni.fi.pa165.pujcovnastroju.entity.MachineTypeEnum;
 import cz.muni.fi.pa165.pujcovnastroju.entity.UserTypeEnum;
 import cz.muni.fi.pa165.pujcovnastroju.security.UserDetailsImpl;
 import cz.muni.fi.pa165.pujcovnastroju.service.LoanService;
 import cz.muni.fi.pa165.pujcovnastroju.service.MachineService;
 import cz.muni.fi.pa165.pujcovnastroju.service.RevisionService;
 import cz.muni.fi.pa165.pujcovnastroju.service.SystemUserService;
 
 import org.springframework.security.authentication.AnonymousAuthenticationToken;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 /**
  * Machine controller implementation
  * 
  * @author Michal Merta
  * 
  */
 @Controller
 @RequestMapping("/machine")
 public class MachineController {
 
 	MachineService machineService;
 	LoanService loanService;
 	RevisionService revisionService;
 	SystemUserService userService;
 
 	@Autowired
 	public MachineController(MachineService machineService,
 			RevisionService revisionService, LoanService loanService,
 			SystemUserService userService) {
 		this.machineService = machineService;
 		this.loanService = loanService;
 		this.revisionService = revisionService;
 		this.userService = userService;
 	}
 
 	@RequestMapping("")
 	public String redirectToList(ModelMap model) {
 		return "redirect:/machine/list";
 	}
 
 	@RequestMapping("/")
 	public String redirectToListBackslash(ModelMap model) {
 		return "redirect:/machine/list";
 	}
 
 	@RequestMapping(value = "/list")
 	public ModelAndView listMachines(
 			ModelMap model,
 			@RequestParam(value = "storeStatus", required = false, defaultValue = "") String storeStatus,
 			@RequestParam(value = "deleteStatus", required = false, defaultValue = "") String deleteStatus,
 			@RequestParam(value = "updateStatus", required = false, defaultValue = "") String updateStatus,
 			@RequestParam(value = "errorMessage", required = false, defaultValue = "") String errorMessage) {
 
 		List<MachineDTO> list = machineService.getAllMachines();
 		model.addAttribute("machines", list);
 		model.addAttribute("existingMachines", list);
 
 		model.addAttribute("list", "list of machines");
 		model.addAttribute("types", MachineTypeEnum.class.getEnumConstants());
 		model.addAttribute("pageTitle", "lang.listMachinesTitle");
		model.addAttribute("userType", DefaultController.getLoggedUserType());
 		DefaultController.addHeaderFooterInfo(model);
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
 
 		return new ModelAndView("listMachines", "command", new MachineDTO());
 	}
 
 	@RequestMapping(value = "/listByParams", method = RequestMethod.GET)
 	public ModelAndView listMachinesByParams(ModelMap model,
 			@RequestParam(value = "from", required = false) Date from,
 			@RequestParam(value = "till", required = false) Date till) {
 
 		List<MachineDTO> machinesNew = machineService.getMachineDTOsByParams(
 				null, null, null, null, null, from, till);
 		model.addAttribute("machinesNew", machinesNew);
 
 		return new ModelAndView("listMachinesByParams");
 	}
 
 	@RequestMapping(value = "/add", method = RequestMethod.POST)
 	public String addMachine(@ModelAttribute("machine") MachineDTO machine,
 			BindingResult result, ModelMap model) {
 		boolean stored = false;
 		String errorMsg = null;
 
 		try {
 			stored = machineService.create(machine) != null;
 		} catch (DataAccessException e) {
 			stored = false;
 			errorMsg = e.getMessage();
 		}
 
 		model.addAttribute("storeStatus", stored);
 		if (errorMsg != null) {
 			model.addAttribute("errorMessage", errorMsg);
 		}
 		return "redirect:list";
 	}
 
 	@RequestMapping("/detail/{id}")
 	public String viewMachine(@PathVariable String id, ModelMap model) {
 		DefaultController.addHeaderFooterInfo(model);
 		model.addAttribute("pageTitle", "lang.detailMachineTitle");
 		MachineDTO machine = null;
 		boolean found = false;
 		try {
 			Long machineID = Long.valueOf(id);
 			machine = machineService.read(machineID);
 			found = machine != null;
 		} catch (DataAccessException | NumberFormatException e) {
 			// TODO log
 		}
 
 		if (found) {
 			model.addAttribute("machine", machine);
 			List<LoanDTO> loans = new ArrayList<>();
 			for (LoanDTO loan : machine.getLoans()) {
 				loans.add(loanService.read(loan.getId()));
 			}
 
 			model.addAttribute("loans", loans.size() == 0 ? null : loans);
 			List<RevisionDTO> revisions = new ArrayList<>();
 			if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
 				UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
 						.getContext().getAuthentication().getPrincipal();
 				SystemUserDTO user = userService
 						.getSystemUserByUsername(userDetails.getUsername());
 				if (user.getType().getId() == UserTypeEnum.REVISIONER.ordinal()) {
 					for (RevisionDTO revision : machine.getRevisions()) {
 						revisions.add(revisionService.readBizRevision(revision
 								.getRevID()));
 					}
 				}
 			}
 			model.addAttribute("revisions", revisions);
 		} else {
 			model.addAttribute("id", id);
 		}
 
 		return "machineDetail";
 	}
 
 	@RequestMapping(value = "/delete/{id}")
 	public String deleteMachine(@PathVariable String id, ModelMap model) {
 		boolean deleted = false;
 		String errorMsg = null;
 		MachineDTO machine = new MachineDTO();
 		try {
 			Long machineID = Long.valueOf(id);
 			machine = machineService.read(machineID);
 			machineService.delete(machine);
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
 		return "redirect:/machine/list";
 	}
 
 	@RequestMapping(value = "/update/{id}")
 	public ModelAndView updateUser(@PathVariable String id, ModelMap model) {
 		DefaultController.addHeaderFooterInfo(model);
 		model.addAttribute("pageTitle", "lang.updateUserTitle");
 		MachineDTO machine = null;
 		boolean found = false;
 		try {
 			Long machineID = Long.valueOf(id);
 			machine = machineService.read(machineID);
 			found = true;
 		} catch (DataAccessException | NumberFormatException e) {
 			// TODO log
 		}
 
 		List<MachineTypeEnum> types = new ArrayList<>();
 		for (MachineTypeEnum e : MachineTypeEnum.class.getEnumConstants()) {
 			if (!e.toString().equals(machine.getType().getTypeLabel())) {
 				types.add(e);
 			}
 		}
 
 		model.addAttribute("types", types);
 		model.addAttribute("machine", machine);
 		if (!found) {
 			model.addAttribute("id", id);
 		}
 		return new ModelAndView("updateMachine", "command", new MachineDTO());
 	}
 
 	@RequestMapping(value = "/update/update", method = RequestMethod.POST)
 	public String editUser(@ModelAttribute("machine") MachineDTO machine,
 			BindingResult result, ModelMap model) {
 		boolean updated = false;
 		String errorMsg = null;
 		try {
 			updated = machineService.update(machine) != null;
 		} catch (DataAccessException e) {
 			updated = false;
 			errorMsg = e.getMessage();
 		}
 		model.addAttribute("updateStatus", updated);
 		if (errorMsg != null) {
 			model.addAttribute("errorMessage", errorMsg);
 		}
 		return "redirect:/machine/list";
 	}
 
 	@RequestMapping(value = "/filter", method = RequestMethod.GET, params = "submit")
 	public ModelAndView filterMachines(ModelMap model,
 			@RequestParam(required = false) String label,
 			@RequestParam(required = false) String description,
 			@RequestParam(required = false) String type) {
 		DefaultController.addHeaderFooterInfo(model);
 		StringToMachineTypeEnumDTOConverter converter = new StringToMachineTypeEnumDTOConverter();
 		model.addAttribute("selectedType", type);
 		model.addAttribute("selectedLabel", label);
 		model.addAttribute("selectedDescription", description);
 		if (type.equals("--no type--")) {
 			type = null;
 		}
 		if (label.equals("")) {
 			label = null;
 		}
 		if (description.equals("")) {
 			description = null;
 		}
 		List<String> types = new ArrayList<>();
 		for (MachineTypeEnum enums : MachineTypeEnum.class.getEnumConstants()) {
 			types.add(enums.name());
 		}
 		model.addAttribute("machines", machineService.getMachineDTOsByParams(
 				label, description, converter.convert(type), null, null, null,
 				null));
 		model.addAttribute("existingMachines", machineService.getAllMachines());
 		model.addAttribute("types", types);
 		model.addAttribute("list", "list of machines");
 		return new ModelAndView("listMachines", "command", new MachineDTO());
 	}
 
 	@RequestMapping(value = "/filter", method = RequestMethod.GET, params = "void")
 	public String voidFilter(ModelMap model) {
 		return "redirect:/machine/list";
 	}
 }
