 package infodoc.basic.activity;
 
 import infodoc.basic.BasicConstants;
 import infodoc.core.container.InfodocContainerFactory;
 import infodoc.core.dto.Activity;
 import infodoc.core.dto.Case;
 import infodoc.core.dto.PropertyValue;
 import infodoc.core.dto.User;
 import infodoc.core.dto.UserGroup;
 import infodoc.core.ui.activity.ActivityExecutor;
 import infodoc.core.ui.cases.CaseForm;
 import infodoc.core.ui.cases.CasesList;
 import infodoc.core.ui.common.InfodocTheme;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.terminal.UserError;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 
 import enterpriseapp.hibernate.Db;
 
 public class Create extends ActivityExecutor implements ClickListener {
 
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger logger = LoggerFactory.getLogger(Create.class);
 	
 	protected Button createButton;
 	protected CaseForm form;
 	protected CasesList instancesListComponent;
 	protected CheckBox sendToCheckBox;
 	protected boolean useNumeration = true;
 	protected boolean hideSendToCheckBox = false;
 	protected boolean dontAssign = false;
 	protected HashSet<User> assignUsers = new HashSet<User>();
 	protected HashSet<UserGroup> assignGroups = new HashSet<UserGroup>();
 	
 	public Create() {
 		super();
 	}
 	
 	public Create(Activity activity, User user) {
 		super(activity, user);
 	}
 	
 	@Override
 	public void initLayout() {
 		Label leftSpacer = new Label();
 		leftSpacer.setWidth("100%");
 		
 		createButton = new Button(getActivity().getName());
 		createButton.addListener(this);
 		
 		HorizontalLayout footer = new HorizontalLayout();
 		footer.setWidth("100%");
 		footer.addComponent(leftSpacer);
 		footer.setExpandRatio(leftSpacer, 1);
 		footer.addComponent(createButton);
 		footer.setComponentAlignment(createButton, Alignment.BOTTOM_RIGHT);
 		
 		Case newCase = new Case();
 		newCase.setForm(getForm());
 		
 		form = new CaseForm(newCase, getActivity(), getUser(), true);
 		form.setImmediate(true);
 		form.setWidth("100%");
 		form.setFooter(footer);
 		
 		Panel panel = new Panel(BasicConstants.uiForm);
 		panel.setWidth("100%");
 		panel.addComponent(form);
 		
 		VerticalLayout leftLayout = new VerticalLayout();
 		leftLayout.setMargin(true);
 		leftLayout.addComponent(panel);
 		
 		instancesListComponent = new CasesList();
 		
 		VerticalLayout rightLayout = new VerticalLayout();
 		rightLayout.setMargin(true);
 		rightLayout.addComponent(instancesListComponent);
 		
 		HorizontalSplitPanel layout = new HorizontalSplitPanel();
 		layout.setSizeFull();
 		layout.setMargin(true);
 		layout.setFirstComponent(leftLayout);
 		layout.setSecondComponent(rightLayout);
 		
 		setCompositionRoot(layout);
 	}
 	
 	@Override
 	public void attach() {
 		super.attach();
 		addSendTo();
 	}
 
 	protected void addSendTo() {
 		parseParams();
 		
 		if(!assignUsers.isEmpty()) {
 			
 			String recipients = assignUsers.toString().replace("[", "").replace("]", "");
 			
 			if(!assignGroups.isEmpty()) {
 				if(!recipients.isEmpty()) {
 					recipients += ", ";
 				}
 				
 				recipients += assignGroups.toString().replace("[", "").replace("]", "");
 			}
 			
 			if(!recipients.isEmpty()) {
 				sendToCheckBox = new CheckBox(BasicConstants.uiAssignTo(recipients));
 				sendToCheckBox.setValue(true);
 				sendToCheckBox.setIcon(new ThemeResource(InfodocTheme.iconUserGroup));
 				
 				if(!hideSendToCheckBox) {
 					form.addField("sendTo", sendToCheckBox);
 				}
 			}
 		}
 	}
 	
 	public void parseParams() {
 		assignUsers.clear();
 		assignGroups.clear();
 		
 		if(getActivity().getParameter() == null || getActivity().getParameter().isEmpty()) {
 			return;
 		}
 		
 		String[] params = getActivity().getParameter().split(",");
 		int i = 0;
 		
 		while(i < params.length) {
 			String param = params[i].trim();
 			
 			if(param.toLowerCase().equals("assignUsers".toLowerCase())) {
 				i = assignUsers(params, i + 1);
 				
 			} else if(param.toLowerCase().equals("assignGroups".toLowerCase())) {
 				i = assignGroups(params, i + 1);
 				
 			} else if(param.toLowerCase().equals("hideSendToCheckBox".toLowerCase())) {
 				i = hideSendToCheckBox(params, i + 1);
 				
 			} else if(param.toLowerCase().equals("dontAssign".toLowerCase())) {
 				i = dontAssign(params, i + 1);
 				
 			} else {
 				i = parseOther(params, i);
 			}
 		}
 	}
 	
 	protected int parseOther(String[] params, int startPosition) {
 		throw new RuntimeException("Wrong parameter for activity " + getActivity().toString() + ": " + params[startPosition]);
 	}
 	
 	protected int assignUsers(String[] params, int startPosition) {
 		int i = startPosition;
 		
 		while(i < params.length) {
 			try {
 				User u = InfodocContainerFactory.getUserContainer().getEntity(new Long(params[i].trim()));
 				
 				if(u == null) {
 					throw new RuntimeException("User " + getActivity().getParameter() + ", configured in parameter of activity " + getActivity() + ", does not exist.");
 				}
 				
 				assignUsers.add(u);
 				i++;
 				
 			} catch(NumberFormatException e) {
 				return i;
 			}
 		}
 		
 		return i;
 	}
 
 	protected int assignGroups(String[] params, int startPosition) {
 		int i = startPosition;
 		
 		while(i < params.length) {
 			try {
 				UserGroup g = InfodocContainerFactory.getUserGroupContainer().getEntity(new Long(params[i].trim()));
 				
 				if(g == null) {
 					throw new RuntimeException("User group " + getActivity().getParameter() + ", configured in parameter of activity " + getActivity() + ", does not exist.");
 				}
 				
 				assignGroups.add(g);
 				i++;
 				
 			} catch(NumberFormatException e) {
 				return i;
 			}
 		}
 		
 		return i;
 	}
 
 	protected int hideSendToCheckBox(String[] params, int startPosition) {
 		hideSendToCheckBox = true;
 		return startPosition;
 	}
 
 	protected int dontAssign(String[] params, int startPosition) {
 		dontAssign = true;
 		return startPosition;
 	}
 
 	@Override
 	public void buttonClick(ClickEvent event) {
 		if(createButton.equals(event.getButton())) {
 			execute();
 		}
 	}
 	
 	public void execute() {
 		form.setComponentError(null);
 		
 		try {
 			form.validate();
 			Collection<PropertyValue> propertyValuesToSave = form.getPropertyValues();
 			
 			if(!beforeSaveCase(propertyValuesToSave)) {
 				Db.rollBackTransaction();
 				return;
 			}
 			
 			Case caseDto = saveCase(propertyValuesToSave);
 			
 			if(!afterSaveCase(propertyValuesToSave)) {
 				Db.rollBackTransaction();
 				return;
 			}
 			
 			Db.commitTransaction();
 			caseDto = InfodocContainerFactory.getCaseContainer().getEntity(caseDto.getId());
 			form.clear();
 			addSendTo();
 			instancesListComponent.addAtBeginning(caseDto);
 			
 			if(getWindow() != null) {
 				getWindow().showNotification(BasicConstants.uiActivityExecuted);
 			}
 			
 		} catch (InvalidValueException e) {
 			logger.error("Error executing activity", e);
 			form.setComponentError(new UserError(e.getMessage()));
 		}
 	}
 	
 	public boolean beforeSaveCase(Collection<PropertyValue> propertyValuesToSave) {
 		return true;
 	}
 
 	public boolean afterSaveCase(Collection<PropertyValue> propertyValuesToSave) {
 		return true;
 	}
 
 	public Case saveCase(Collection<PropertyValue> propertyValuesToSave) {
 		parseParams();
 		
 		if(sendToCheckBox == null || !sendToCheckBox.booleanValue()) {
 			assignUsers.clear();
 			assignGroups.clear();
			
			if(!dontAssign) {
				assignUsers.add(getUser());
			}
 		}
 		
 		return InfodocContainerFactory.getCaseContainer().saveInstace(form.getCase(), propertyValuesToSave, getNewActivityInstance(form.getCase(), form.getComments(), assignUsers, assignGroups), useNumeration);
 	}
 	
 	@Override
 	public Long countAvailableCases() {
 		return null;
 	}
 	
 	@Override
 	public String getHelp() {
 		return BasicConstants.uiHelpCreateActivity;
 	}
 
 	@Override
 	public Resource getIcon() {
 		return new ThemeResource(InfodocTheme.iconActivityCreate);
 	}
 
 	public CaseForm getCaseForm() {
 		return form;
 	}
 
 }
