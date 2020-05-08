 package com.github.kolorobot.icm.incident;
 
import java.util.Arrays;
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.validation.Valid;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.github.kolorobot.icm.account.Account;
 import com.github.kolorobot.icm.account.AccountRepository;
 import com.github.kolorobot.icm.account.Address;
 import com.github.kolorobot.icm.account.User;
 import com.github.kolorobot.icm.incident.Incident.Status;
 import com.github.kolorobot.icm.support.web.AjaxViewException;
 import com.github.kolorobot.icm.support.web.Message;
 import com.github.kolorobot.icm.support.web.Message.Type;
 import com.google.common.collect.Lists;
 
 @Controller
 @RequestMapping("/incident")
 class IncidentController {
 	
 	@Inject
 	private AccountRepository accountRepository;
 	
 	@Inject
 	private IncidentRepository incidentRepository;
 	
 		
 	@RequestMapping("/create")
 	public IncidentForm create() {
 		return new IncidentForm();
 	}
 	
 	@RequestMapping(value = "/create", method = RequestMethod.POST)
 	public String create(User user, @Valid @ModelAttribute IncidentForm incidentForm, Errors errors, RedirectAttributes ra, Model model) {
 		if (errors.hasErrors()) {
 			model.addAttribute(Message.MESSAGE_ATTRIBUTE, new Message("incident.create.failed", Type.ERROR));
 			return null;
 		}
 		
 		Address incidentAddress = new Address();
 		// FIXME incidentAddress.setCityLine(incidentForm.getCityLine());
 		incidentAddress.setCityLine(incidentForm.getAddressLine());
 		incidentAddress.setAddressLine(incidentForm.getAddressLine());
 		incidentAddress.setOperatorId(user.getOperatorId());
 		
 		Incident incident = new Incident();
 		incident.setAddress(incidentAddress);
 		incident.setDescription(incidentForm.getDescription());
 		incident.setIncidentType(incidentForm.getType());
 		incident.setOperatorId(user.getOperatorId());
 		Account reporter = accountRepository.findOne(user.getAccountId());
 	
 		incident.setCreator(reporter);
 		Date someDate = randomDate();
 		incident.setCreated(someDate);
 		incidentRepository.save(incident);
 		
 		ra.addFlashAttribute(Message.MESSAGE_ATTRIBUTE, new Message("incident.create.success", Message.Type.SUCCESS, incident.getId()));
 		
 		return "redirect:/incident/list";
 	}
 	
 	private Date randomDate() {
 		long day = 24L * 60L * 60L * 1000L;
 		long month = 30L * day;
 		
 		long randomTime = day	+ (long) (Math.random() * ((month - day) + 1));
 		Date date = new Date(System.currentTimeMillis() - randomTime);
 		return date;
 	}
 	
 	@RequestMapping(value = "/list")
 	@ModelAttribute(value = "incidents")
 	public List<Incident> list(User user) {
 		// FIXME does not conform with the requirements
 		return incidentRepository.findAllByOperatorId(user.getOperatorId());	
 	}
 	
 	@RequestMapping(value = "/{id}", headers = "X-Requested-With=XMLHttpRequest")
 	@Transactional
 	public String detailsAjax(User user, @PathVariable("id") Long id, Model model) {
 		Incident incident = getIncident(user, id);
 		
 		if (incident == null) {			
 			throw new AjaxViewException("Exception 0x156DDE");
 		}
 		
 		model.addAttribute("incident", incident);
 		return "incident/detailsAjax";
 	}
 
 	@RequestMapping(value = "/{id}")
 	@Transactional
 	public String details(User user, @PathVariable("id") Long id, Model model) {
 		Incident incident = getIncident(user, id);
 		
 		if (incident == null) {			
 			throw new IllegalAccessError("Exception 0x156DDE");
 		}
 		
 		model.addAttribute("incident", incident);
 		return "incident/details";
 	}
 	
 	private Incident getIncident(User user, Long id) {
 		Incident incident = null;
 		if (user.isInRole(Account.ROLE_USER)) {
 			incident = incidentRepository.findOneByIdAndCreatorId(id, user.getAccountId(), user.getOperatorId());
 		} else if (user.isInRole(Account.ROLE_EMPLOYEE)) {
 			incident = incidentRepository.findOneByIdAndAssigneeIdOrCreatorId(id, user.getAccountId(), user.getOperatorId());
 		} else {
 			incident = incidentRepository.findOne(id, user.getOperatorId());
 		}
 		return incident;
 	}
 	
 	@RequestMapping(value = "search")
 	public String search(@RequestParam String q) {
 		// FIXME Not handled conversion error (functional bug)
 		Long incidentId = Long.valueOf(q);
 		return "forward:/incident/" + incidentId;
 	}
 	
 	// FIXME Should be available only for ROLE_ADMIN (security bug)
 	@RequestMapping("/{incidentId}/audit/create")	
 	public String createAudit(User user, @PathVariable Long incidentId, Model model) {
 		Incident incident = getIncident(user, incidentId);
 		
 		if (incident == null) {
 			throw new IllegalStateException("Why, Leo, why?");
 		}
 		
 		AuditForm form = new AuditForm();
 		form.setOldStatus(incident.getStatus());
 		form.setAvailableStatuses(getAvailableStatuses(user, incident));
 		form.setAvailableEmployees(getAvailableEmployees(user, incident));
 		
 		model.addAttribute(form);
 		return "incident/audit/create";
 	}
 
 	private List<Status> getAvailableStatuses(User user, Incident incident) {
 		Status status = incident.getStatus();
 		if (user.isInRole(Account.ROLE_ADMIN)) {
 			if(EnumSet.of(Status.NOT_CONFIRMED, Status.SOLVED).contains(status)) {
 				return Lists.newArrayList(Status.CLOSED, Status.NEW);
 			}
 		} else if (user.isInRole(Account.ROLE_EMPLOYEE)) {
 			switch(status) {
 				case NEW:
 					return Lists.newArrayList(Status.CONFIRMED, Status.NOT_CONFIRMED, Status.SOLVED);
 				case CONFIRMED:
 					return Lists.newArrayList(Status.NEW, Status.NOT_CONFIRMED, Status.SOLVED);
 				case NOT_CONFIRMED:
 					return Lists.newArrayList(Status.NEW, Status.CONFIRMED, Status.SOLVED);
 				case SOLVED:
 					return Lists.newArrayList(Status.NEW, Status.CONFIRMED, Status.NOT_CONFIRMED);
 			default:
 				break;
 			}
 		} else {
 			throw new IllegalAccessError();
 		}
 		return null;
 	}
 	
 	private List<Account> getAvailableEmployees(User user, Incident incident) {
 		if (user.isInRole(Account.ROLE_ADMIN) 
 				&& EnumSet.of(Status.NEW, Status.NOT_CONFIRMED, Status.SOLVED).contains(incident.getStatus())) {
 			return accountRepository.findAll(user.getOperatorId());
 		}
 		return null;
 	}
 
 	// FIXME Should be available only for ROLE_ADMIN (security bug)
 	@RequestMapping(value = "/{incidentId}/audit/create", method = RequestMethod.POST)
 	@Transactional
 	public String createAudit(User user, @PathVariable Long incidentId, @Valid @ModelAttribute AuditForm auditForm, Errors errors, RedirectAttributes ra, Model model) {
 		if (errors.hasErrors()) {
 			model.addAttribute(Message.MESSAGE_ATTRIBUTE, new Message("incident.audit.create.failed", Type.ERROR));
 			return null;
 		}
 		
 		Incident incident = incidentRepository.findOne(incidentId);
 		Account creator = accountRepository.findOne(user.getAccountId());
 		
 		Audit audit = new Audit();
 		audit.setDescription(auditForm.getDescription());		
 		
 		audit.setCreated(new Date());
 		audit.setCreator(creator);
 		audit.setPreviousStatus(incident.getStatus());
 		audit.setOperatorId(user.getOperatorId());
 		
 		Status newStatus = auditForm.getNewStatus();
 		if (newStatus == null) {
 			newStatus = incident.getStatus();
 		}
 		audit.setStatus(newStatus);
 		incident.setStatus(newStatus);
 		
 		Long assigneeId = auditForm.getAssigneeId();
 		if (assigneeId != null) {
 			Account assignee = accountRepository.findOne(assigneeId);
 			incident.setAssignee(assignee);
 		}
 		
 		audit.setIncident(incident);
 		incident.getAudits().add(audit);
 		
 		incident = incidentRepository.save(incident);
 		
 		ra.addFlashAttribute(Message.MESSAGE_ATTRIBUTE, new Message("incident.audit.create.success", Message.Type.SUCCESS));
 		
 		return "redirect:/incident/" + incidentId;
 	}
 }
