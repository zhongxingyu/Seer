 package org.iucn.sis.client.panels.taxomatic;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticUtils.TaxonomyException;
 import org.iucn.sis.shared.api.models.Infratype;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 import org.iucn.sis.shared.api.models.TaxonStatus;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.util.Params;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.DataListItem;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.InfoConfig;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.google.gwt.user.client.ui.HTML;
 import com.solertium.lwxml.shared.GWTResponseException;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.extjs.client.FormBuilder;
 import com.solertium.util.extjs.client.WindowUtils;
 
 @SuppressWarnings("deprecation")
 public class CreateNewTaxonPanel extends TaxomaticWindow {
 	
 	private static final String DATA_KEY = "dataKeyString";
 
 	private final Taxon parentNode;
 	
 	private FormPanel form;
 	private TextField<String> name;
 	private TextField<String> taxonomicAuthority;
 	private ComboBox<ComboOption> rank;
 	private ComboBox<ComboOption> status;
 	private CheckBox hybrid;
 	
 	public CreateNewTaxonPanel(Taxon parent) {
 		super();
 		this.parentNode = parent;
 		
 		setSize(350, 250);
 		setHeading("Create New Taxon");
 		setIconStyle("icon-new-document");
 		setModal(true);
 
 		addButton(new Button("Save and Close", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				performSave();
 			}
 		}));
 
 		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				hide();
 			}
 		}));
 	}
 
 	private void addToWorkingSet(final Taxon newNode) {
 		if (newNode.getLevel() >= TaxonLevel.SPECIES) {
 			WindowUtils.confirmAlert("Add To Working Set", "Would you like to add this taxon to a working set?",
 					new WindowUtils.SimpleMessageBoxListener() {
 				public void onYes() {
 					createWorkingSetSelector(newNode);
 				}
 			});
 		}
 	}
 
 	private void createWorkingSetSelector(final Taxon newNode) {
 		final Dialog s = new Dialog();
 		s.setModal(true);
 		s.setHeading("Choose Working Set");
 
 		HTML message = new HTML("Choose the working set you would like to add " + newNode.getFullName() + " to.");
 
 		final DataList list = new DataList();
 		list.setCheckable(true);
 		for (Iterator<WorkingSet> iter = WorkingSetCache.impl.getWorkingSets().values().iterator(); iter.hasNext();) {
 			WorkingSet curWS = (WorkingSet) iter.next();
 			DataListItem curItem = new DataListItem(curWS.getWorkingSetName());
 			curItem.setData(DATA_KEY, curWS);
 
 			list.add(curItem);
 		}
 
 		Button add = new Button("Add to Selected", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				String sets = "";
 
 				List<DataListItem> checked = list.getChecked();
 				for (DataListItem item : checked) {
 					final WorkingSet cur = (WorkingSet) item.getData(DATA_KEY);
 
 					sets += cur.getWorkingSetName() + ", ";
 					cur.getSpeciesIDs().add(newNode.getId());
 //					cur.setSorted(false);
 //					cur.sortSpeciesList();
 					WorkingSetCache.impl.editWorkingSet(cur, new GenericCallback<String>() {
 						public void onFailure(Throwable caught) {
 						}
 
 						public void onSuccess(String result) {
 							Info.display("Success!", newNode.getFullName() + " was added to the set {0}.", cur
 									.getWorkingSetName());
 						}
 					});
 				}
 
 				s.hide();
 			}
 		});
 		add.setIconStyle("icon-add");
 
 		Button close = new Button("Close", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				s.hide();
 			}
 		});
 		close.setIconStyle("icon-cancel");
 
 		LayoutContainer content = s;
 		content.add(message);
 		content.add(list);
 
 		s.addButton(add);
 		s.addButton(close);
 
 		s.show();
 		s.center();
 	}
 	
 	public void show() {
 		draw();
 		super.show();
 	}
 
 	private void draw() {
 		name = FormBuilder.createTextField("name", null, "Name", true);
 		name.getMessages().setBlankText("Name must be specified.");
 		
 		taxonomicAuthority = FormBuilder.createTextField("authority", null, "Authority", false);
 		
 		ListStore<ComboOption> statusStore = new ListStore<ComboOption>();
 		for (Entry<String, String> entry : TaxonStatus.displayableStatus.entrySet())
 			statusStore.add(new ComboOption(entry.getValue(), entry.getKey()));
 		if (parentNode.getLevel() >= TaxonLevel.GENUS)
 			statusStore.add(new ComboOption("Undescribed", "U"));
 		status = new ComboBox<ComboOption>();
 		status.setTriggerAction(TriggerAction.ALL);
 		status.setFieldLabel("Status");
 		status.setAllowBlank(false);
 		status.setForceSelection(true);
 		status.setStore(statusStore);
 		status.setValue(statusStore.getAt(0));
 		
 		rank = new ComboBox<ComboOption>();
 		rank.setTriggerAction(TriggerAction.ALL);
 		rank.setFieldLabel("Rank");
 		rank.setAllowBlank(false);
 		rank.setForceSelection(true);
 		
 		hybrid = new CheckBox();
 		hybrid.setName("hybrid");
 		hybrid.setFieldLabel(" Hybrid");
 		hybrid.setValue(Boolean.FALSE);
 		
 		ListStore<ComboOption> rankStore = new ListStore<ComboOption>();
 		if (parentNode.getLevel() == TaxonLevel.SPECIES) {
 			rankStore.add(new ComboOption("Subspecies", TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_SUBSPECIES));
 			if (!parentNode.getFootprintAsString().contains("ANIMALIA"))
 				rankStore.add(new ComboOption("Variety", TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_VARIETY));
 			rankStore.add(new ComboOption("Subpopulation", "" + TaxonLevel.SUBPOPULATION));
 			rank.setEnabled(true);
 			hybrid.setVisible(true);
 		} else {
 			if (parentNode.getLevel() == TaxonLevel.INFRARANK)
 				rankStore.add(new ComboOption(Taxon.getDisplayableLevel(TaxonLevel.INFRARANK_SUBPOPULATION), ""
 						+ TaxonLevel.INFRARANK_SUBPOPULATION));
 			else
 				rankStore.add(new ComboOption(Taxon.getDisplayableLevel(parentNode.getLevel() + 1), ""
 								+ (parentNode.getLevel() + 1)));
 
 			rank.setEnabled(false);
 			hybrid.setVisible(false);
 		}
 		rank.setStore(rankStore);
 		rank.setValue(rankStore.getAt(0));
 		
 		form = new FormPanel();
 		form.setLayout(new FormLayout());
 		form.setHeaderVisible(false);
 		form.setBodyBorder(false);
 		form.setBorders(false);
 		form.add(name);
 		form.add(taxonomicAuthority);
 		form.add(rank);
 		form.add(status);
 		form.add(hybrid);
 		
 
 		add(form);
 	}
 
 	private void performSave() {
 		if (!form.isValid())
 			return;
 		
 		int level = rank.getValue().getValue();
 		Integer infraType = null;
 		if (level > TaxonLevel.INFRARANK_SUBPOPULATION) {
 			infraType = level % (TaxonLevel.INFRARANK * 10);
 			level = Integer.parseInt(rank.getValue().getValueString().substring(0, 1));
 		}
 
 		if (level >= TaxonLevel.SPECIES && (taxonomicAuthority.getValue() == null || taxonomicAuthority.getValue().equals(""))) {
 			taxonomicAuthority.markInvalid("A taxonomic authority must be specified.");
 			return;
 		}		
 
 		WindowUtils.showLoadingAlert("Saving taxon...");
 
 		final Taxon newNode = Taxon.createNode(0, name.getValue(), level, hybrid.getValue()); 
 		newNode.setParent(parentNode);
 		newNode.setStatus(status.getValue().getValueString());
 		newNode.setTaxonomicAuthority(taxonomicAuthority.getValue());
 		if (infraType != null)
 			newNode.setInfratype(Infratype.getInfratype(infraType, newNode));
 
 		TaxomaticUtils.impl.createNewTaxon(newNode, parentNode, new GenericCallback<Taxon>() {
 			public void onFailure(Throwable caught) {
 				WindowUtils.hideLoadingAlert();
 				if (caught instanceof TaxonomyException) {
 					WindowUtils.errorAlert(caught.getMessage());
 				}
 				else if (caught instanceof GWTResponseException && ((GWTResponseException)caught).getCode() == 423)
 					WindowUtils.errorAlert("Taxomatic In Use", "Sorry, but another " +
 							"taxomatic operation is currently running. Please try " +
 						"again later!");
 				else
 					WindowUtils.errorAlert("Failure!", "New node " + newNode.getFullName() + " failed to save."
 							+ " Either you have a bad connection to your server, or there "
 							+ "already exists a node with the same name.");
 			}
 			public void onSuccess(Taxon arg0) {
 				WindowUtils.hideLoadingAlert();
 				Info.display(new InfoConfig("Taxon Created", "New taxon {0} was created successfully!", new Params(
 						newNode.getFullName())));
 				TaxonomyCache.impl.evictPaths();
 				//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(arg0.getId());
 				//TaxonomyCache.impl.setCurrentTaxon(arg0);
 				StateManager.impl.setState(arg0, null);
 				BaseEvent be = new BaseEvent(newNode);
 				be.setCancelled(false);
 				fireEvent(Events.StateChange, be);
 				hide();
 				addToWorkingSet(newNode);
 			}
 		});
 	}
 	
 	private static class ComboOption extends BaseModelData {
 		
 		private static final long serialVersionUID = 1L;
 		
 		public ComboOption(String text, String value) {
 			super();
 			set("text", text);
 			set("value", value);
 		}
 		
 		public String getValueString() {
 			return get("value");
 		}
 		
 		public int getValue() {
 			return Integer.parseInt(getValueString());
 		}
 		
 	}
 	
 }
