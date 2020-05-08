 package org.iucn.sis.client.panels.taxomatic;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.iucn.sis.client.api.caches.LanguageCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.client.panels.taxomatic.EditCommonNamePanel.IsoLanguageComparator;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.IsoLanguage;
 import org.iucn.sis.shared.api.models.Taxon;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class TaxonCommonNameEditor extends TaxomaticWindow implements DrawsLazily {
 
 	private final Taxon  node;
 	private final VerticalPanel commonNameInfo;
 	private final List<CommonName> allCommonNames;
 	private CommonName currentCommonName;
 	private ButtonBar bar;
 	private Button delete;
 	private ListBox commonNameList;
 
 	private ListBox validated;
 	private ListBox status;
 	private TextBox name;
 	private ListBox language;
 	private int numberAdded;
 
 	public TaxonCommonNameEditor() {
 		super();
 		setHeading("Common Names Validator");
 		setIconStyle("icon-note-edit");
 		
 		this.node = TaxonomyCache.impl.getCurrentTaxon();
 		commonNameInfo = new VerticalPanel();
 		allCommonNames = new ArrayList<CommonName>();
 		numberAdded = 0;
 
 		name = new TextBox();
 		name.addBlurHandler(new BlurHandler() {
 			public void onBlur(BlurEvent event) {
 				String oldName = name.getName();
 				boolean found = false;
 				for (int i = 0; i < commonNameList.getItemCount() && !found; i++) {
 					if (commonNameList.getItemText(i).equalsIgnoreCase(oldName)) {
 						commonNameList.setItemText(i, name.getText());
 						name.setName(name.getText());
 						found = true;
 					}
 				}
 			}
 
 		});
 		validated = new ListBox();
 		validated.addItem("true", "true");
 		validated.addItem("false", "false");
 
 		language = new ListBox();
 		language.addItem("", "");
 		
 		status = new ListBox();
 		for (int i = 0; i < CommonName.reasons.length; i++)
 			status.addItem(CommonName.reasons[i], i + "");
 	}
 
 	private void clearData() {
 		name.setText("");
 		language.setSelectedIndex(0);
 		status.setSelectedIndex(0);
 		validated.setSelectedIndex(0);
 
 	}
 	
 	public String createNewCommonName() {
 
 		String name = null;
 		do {
 			numberAdded++;
 			name = "new commonname " + numberAdded;
 			boolean found = false;
 			for (int i = 1; i < commonNameList.getItemCount() && !found; i++) {
 				if (commonNameList.getItemText(i).equalsIgnoreCase(name)) {
 					found = true;
 					name = null;
 				}
 			}
 
 		} while (name == null);
 
 		CommonName data = new CommonName();
 		data.setName(name);
 		refreshCommonName(data);
 		return data.getName();
 
 	}
 	
 	@Override
 	public void show() {
 		draw(new DoneDrawingCallback() {
 			public void isDrawn() {
 				open();
 			}
 		});
 	}
 	
 	private void open() {
 		super.show();
 	}
 	
 	@Override
 	public void draw(final DoneDrawingCallback callback) {
 		LanguageCache.impl.list(new ComplexListener<List<IsoLanguage>>() {
 			public void handleEvent(List<IsoLanguage> eventData) {
 				Collections.sort(eventData, new IsoLanguageComparator());
 				for (IsoLanguage current : eventData)
 					language.addItem(current.getName(), current.getCode());
 				draw();
 				callback.isDrawn();
 			}
 		});
 	}
 
 	private void draw() {
 		addStyleName("gwt-background");
 
 		if (node == null) {
 			add(new HTML("Please first select a taxon to add edit the commonName data for."));
 			return;
 		}
 
 		setLayout(new BorderLayout());
 		BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, .1f);
 		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER, .8f);
 		BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, .1f);
 
 		drawInstructions(north);
 		drawCommonNameChooser(center);
 		drawButtons(south);
 
 		getCommonName();
 		refreshListBox();
 	}
 
 	private void drawButtons(BorderLayoutData layoutData) {
 		bar = new ButtonBar();
 		bar.setAlignment(HorizontalAlignment.CENTER);
 		Button save = new Button("Save");
 		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				save();
 			}
 
 		});
 		Button saveAndClose = new Button("Save and Close");
 		saveAndClose.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				saveAndClose();
 			}
 
 		});
 		Button close = new Button("Cancel");
 		close.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				hide();
 			}
 
 		});
 		delete = new Button("Delete CommonName");
 		delete.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				TaxonomyCache.impl.deleteCommonName(node, currentCommonName, new GenericCallback<String>() {
 					
 					@Override
 					public void onSuccess(String result) {
 						allCommonNames.remove(currentCommonName);
 						
 						bar.enable();
 						WindowUtils.infoAlert("Deleted", "Common name " + currentCommonName.getName() + " has been deleted");
 						//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
 						ClientUIContainer.bodyContainer.refreshTaxonPage();
 						//TaxonomyCache.impl.setCurrentTaxon(node);
 						currentCommonName = null;
 						refreshListBox();
 						refreshCommonName(null);
 				
 					}
 				
 					@Override
 					public void onFailure(Throwable caught) {
 						WindowUtils.errorAlert("Unable to delete the common name");
 				
 					}
 				} );
 			}
 
 		});
 		bar.add(close);
 		bar.add(save);
 		bar.add(saveAndClose);
 		bar.add(delete);
 		add(bar, layoutData);
 	}
 
 	private void drawCommonNameChooser(BorderLayoutData layoutData) {
 		HorizontalPanel outterWraper = new HorizontalPanel();
 		outterWraper.setSpacing(0);
 		outterWraper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		HorizontalPanel panel = new HorizontalPanel();
 		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 
 		HTML html = new HTML("Common Names: ");
 		panel.add(html);
 		panel.setSpacing(5);
 
 		commonNameList = new ListBox(false);
 		commonNameList.addItem("", "");
 		commonNameList.addChangeHandler(new ChangeHandler() {
 			public void onChange(ChangeEvent event) {
 				String value = commonNameList.getValue(commonNameList.getSelectedIndex());
 				if (value.equalsIgnoreCase("")) {
 					refreshCommonName(null);
 				} else {
 					refreshCommonName(allCommonNames.get(Integer.parseInt(value)));
 				}
 			}
 		});
 		commonNameList.setWidth("200px");
 		panel.add(commonNameList);
 		panel.setSpacing(10);
 		html = new HTML(" or ");
 		panel.add(html);
 
 		Button createNew = new Button("Create New Common Name");
 		createNew.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				String name = createNewCommonName();
 				commonNameList.addItem(name, allCommonNames.size() - 1 + "");
 				commonNameList.setSelectedIndex(commonNameList.getItemCount() - 1);
 			}
 
 		});
 		panel.add(createNew);
 		
 		
 		
 		outterWraper.add(panel);
 		
 
 		LayoutContainer container = new LayoutContainer();
 		ScrollPanel scroller = new ScrollPanel(commonNameInfo);
 		commonNameInfo.setSize("100%", "100%");
 		container.setLayout(new BorderLayout());
 		container.add(outterWraper, new BorderLayoutData(LayoutRegion.NORTH, 35));
 		container.add(scroller, new BorderLayoutData(LayoutRegion.CENTER));
 		commonNameInfo.addStyleName("gwt-background");
 		add(container, layoutData);
 
 		layout();
 	}
 
 	private void drawInstructions(BorderLayoutData layoutData) {
 		HTML html = new HTML("<b>Instructions:</b> Select the commonName from the list which"
 				+ " you would like to edit, or chose to create a new commonName.", true);
 		add(html, layoutData);
 	}
 
 	private void getCommonName() {
 		allCommonNames.clear();
 		Set<CommonName> temp = node.getCommonNames();
 		for (CommonName cur : temp) {
 			CommonName copy = cur.deepCopy();
 			allCommonNames.add(copy);
 		}
 	}
 
 	private String getShowableData(String data) {
 		if (data == null) {
 			data = "";
 		}
 		return data;
 	}
 
 	public void refreshCommonName(CommonName newCommonName) {
 		storePreviousData();
 		clearData();
 
 		currentCommonName = newCommonName;
 
 		commonNameInfo.clear();
 		if (currentCommonName == null) {
 			commonNameInfo.setBorderWidth(0);
 			delete.setEnabled(false);
 		} else {
 			delete.setEnabled(true);
 			commonNameInfo.setBorderWidth(1);
 			commonNameInfo.setSpacing(10);
 
 			HorizontalPanel hp = new HorizontalPanel();
 			HTML html = new HTML("Name: ");
 			name.setText(getShowableData(currentCommonName.getName()));
 			name.setName(currentCommonName.getName());
 			hp.setSpacing(5);
 			hp.add(html);
 			hp.add(name);
 			commonNameInfo.add(hp);
 
 			hp = new HorizontalPanel();
 			html = new HTML("Language / ISO: ");
 			
 			
 			if (currentCommonName.getIso() == null) {
 				language.setSelectedIndex(0);
 			} else {
 				String iso = currentCommonName.getIsoCode();
 				for (int i = 0; i < language.getItemCount(); i++) {
 					if (language.getValue(i).equalsIgnoreCase(iso)) {
 						language.setSelectedIndex(i);
 						break;
 					}
 				}
 			}
 			
 			hp.setSpacing(5);
 			hp.add(html);
 			hp.add(language);
 			commonNameInfo.add(hp);
 
 			hp = new HorizontalPanel();
 			html = new HTML("Validated: ");
 			validated.setSelectedIndex(currentCommonName.getValidated() ? 0 : 1);
 			hp.setSpacing(5);
 			hp.add(html);
 			hp.add(validated);
 			commonNameInfo.add(hp);
 
 			hp = new HorizontalPanel();
 			html = new HTML("Status: ");
 			status.setSelectedIndex(currentCommonName.getChangeReason());
 			hp.setSpacing(5);
 			hp.add(html);
 			hp.add(status);
 			commonNameInfo.add(hp);
 		}
 
 	}
 
 	public void refreshListBox() {
 		commonNameList.clear();
 		commonNameList.addItem("", "");
 		for (int i = 0; i < allCommonNames.size(); i++) {
 			commonNameList.addItem(((CommonName) allCommonNames.get(i)).getName(), i + "");
 		}
 	}
 
 	private void save() {
 		if (currentCommonName == null)
 			WindowUtils.infoAlert("Please select a common name to save.");
 		else {
 			doSave(new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					allCommonNames.clear();
 					allCommonNames.addAll(node.getCommonNames());
 					bar.enable();
 					WindowUtils.infoAlert("Saved", "Common name " + currentCommonName.getName() + " was saved.");
 					//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
 					//TaxonomyCache.impl.setCurrentTaxon(node);
 					ClientUIContainer.bodyContainer.refreshTaxonPage();
 					refreshListBox();
 					refreshCommonName(null);
 				}
 				public void onFailure(Throwable caught) {
 					bar.enable();
 					WindowUtils.errorAlert("Error", "An error occurred when trying to save the common name data related to "
 							+ node.getFullName() + ".");
 				}
 			});
 		}
 	}
 
 	private void saveAndClose() {
 		if (currentCommonName == null)
 			hide();
 		else {
 			doSave(new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					bar.enable();
 					WindowUtils.infoAlert("Saved", "Common name " + currentCommonName.getName() + " was saved.");
 					//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
 					//TaxonomyCache.impl.setCurrentTaxon(node);
 					ClientUIContainer.bodyContainer.refreshTaxonPage();
 					hide();
 				}
 
 				@Override
 				public void onFailure(Throwable caught) {
 					bar.enable();
 					WindowUtils.errorAlert("Error",
 							"An error occurred when trying to save the common name data related to " + node.getFullName()
 									+ ".");
 				}
 			});
 		}
 	}
 	
 	private void doSave(final GenericCallback<String> callback) {
		if(name.getValue().equals("")){
			WindowUtils.errorAlert("You must enter a name for the common name.");
			return;
		}else if (language.getSelectedIndex() == 0) {
			WindowUtils.errorAlert("You must select a language for the common name.");
 			return;
 		}
 		bar.disable();
 		storePreviousData();
 		
 		if (currentCommonName.getId() == 0)
 			TaxonomyCache.impl.addCommonName(node, currentCommonName, callback);
 		else
 			TaxonomyCache.impl.editCommonName(node, currentCommonName, callback);
 	}
 
 	private void storePreviousData() {
 		if (currentCommonName != null) {
 			currentCommonName.setName(name.getText());
 			currentCommonName.setChangeReason(status.getSelectedIndex());
 			currentCommonName.setValidated(validated.getItemText(validated.getSelectedIndex()).equalsIgnoreCase("true"));
 			currentCommonName.setIso(new IsoLanguage(language.getItemText(language.getSelectedIndex()), language.getValue(language.getSelectedIndex())));
 		}
 	}
 	
 
 }
