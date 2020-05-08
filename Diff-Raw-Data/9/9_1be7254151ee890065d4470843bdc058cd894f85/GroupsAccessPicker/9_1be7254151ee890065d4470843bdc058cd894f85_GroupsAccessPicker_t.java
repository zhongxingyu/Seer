 package org.mindinformatics.gwt.domeo.component.groups.ui;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.client.ui.sets.ILensRefresh;
 import org.mindinformatics.gwt.domeo.model.MAnnotationSet;
 import org.mindinformatics.gwt.domeo.model.accesscontrol.AnnotationAccessManager;
 import org.mindinformatics.gwt.domeo.plugins.resource.nif.search.antibodies.IAntibodiesWidget;
 import org.mindinformatics.gwt.framework.model.users.IUserGroup;
 import org.mindinformatics.gwt.framework.src.IContainerPanel;
 import org.mindinformatics.gwt.framework.src.IContentPanel;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class GroupsAccessPicker  extends Composite implements IContentPanel {
 
 	private static final String TITLE = "Groups access picker";
 	
 	// UI BInder
 	interface Binder extends UiBinder<VerticalPanel, GroupsAccessPicker> {}
 	private static final Binder binder = GWT.create(Binder.class);	
 	
 	// By contract 
 	private IDomeo _domeo;
 	private IContainerPanel _container;
 	private MAnnotationSet _set;
 	private Set<GroupCheckBox> _options = new HashSet<GroupCheckBox>();
 	
 	@UiField Button annotateButton;
 	@UiField VerticalPanel groupsList;
 
 	public GroupsAccessPicker(IDomeo domeo, MAnnotationSet set, final ILensRefresh lens) {
 		_domeo = domeo;
 		_set = set;
 	
 		initWidget(binder.createAndBindUi(this)); // Necessary for initializing Composite
 		
 		groupsList.add(new HTML("Access for set " + _set.getLabel()));
 		
 		Set<IUserGroup> groups = _domeo.getUserManager().getUsersGroups();
 		Set<IUserGroup> groupsForSet = _domeo.getAnnotationAccessManager().getAnnotationSetGroups(_set);
 		for(IUserGroup group: groups) {
 			GroupCheckBox gcb = new GroupCheckBox(this, group, 0);
 			if(groupsForSet!=null && groupsForSet.contains(group)) {
 				gcb.setValue(true);
 			}
 			_options.add(gcb);
 			HorizontalPanel hp = new HorizontalPanel();
 			hp.add(gcb);
 			hp.add(new HTML(group.getName() + " - " + group.getDescription()));
 			groupsList.add(hp);
 			//groupsList.add(new HTML(group.getUuid() + " - " + group.getUri() + " - " + group.getName() + " - " + group.getDescription()));
 		}
 		
 		annotateButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				// TODO
 				Set<IUserGroup> groups = new HashSet<IUserGroup>();
 				for(GroupCheckBox cb: _options) {
 					if(cb.getValue()) {
 						groups.add(cb.getGroup());
 					}
 				}
 				_domeo.getAnnotationAccessManager().setAnnotationSetAccess(_set, AnnotationAccessManager.GROUPS);
 				_domeo.getAnnotationAccessManager().setAnnotationSetGroups(_set, groups);
				_domeo.getComponentsManager().updateObjectLenses(_set);
 				lens.refresh();
 				_container.hide();
 			}
 		});
 	}
 
 	@Override
 	public void setContainer(IContainerPanel glassPanel) {
 		_container = glassPanel;
 	}
 
 	@Override
 	public IContainerPanel getContainer() {
 		return _container;
 	}
 	
 	public String getTitle() {
 		return TITLE;
 	}
 }
 
 class GroupCheckBox extends CheckBox {
 	private int _row;
 	private IUserGroup _group;
 	private GroupCheckBox _this;
 	private GroupsAccessPicker _picker;
 	private IAntibodiesWidget _main;
 	
 	public GroupCheckBox(GroupsAccessPicker picker, IUserGroup group, int row) {
 		super();
 		_group = group;
 		_row = row;
 		_picker = picker;
 		
 		_this = this;
 		
 		
 		this.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if(_this.getValue()) {
 					//_picker.styleRow(_row, true);
 					//_picker.update(_main.getFilterValue());
 				} else {
 					//_picker.styleRow(_row, false);
 				}
 				//_termList.updateList();
 			}
 		});
 	}
 	
 	public void setValue(Boolean value) {
 		super.setValue(value);
 		/*
 		if(value) {
 			_picker.styleRow(_row, true);
 		} else {
 			_picker.styleRow(_row, false);
 		}
 		*/
 	}
 	
 	public IUserGroup getGroup() {
 		return _group;
 	}
 }
