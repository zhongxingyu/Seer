 package org.iucn.sis.client.panels.taxomatic;
 
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticUtils.TaxonomyException;
 import org.iucn.sis.shared.api.models.Infratype;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.solertium.lwxml.shared.GWTConflictException;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class TaxonBasicEditor extends TaxomaticWindow {
 	private final VerticalPanel editor;
 	
 	private ButtonBar bar;
 	private Button close;
 	private Button save;
 	private Button saveAndClose;
 	private final Taxon  node;
 	private ListBox hybrid;
 	private TextBox name;
 	// private TextBox status;
 	private ListBox status;
 	private TextBox taxonomicAuthority;
 	private ListBox level;
 	private CheckBox invasive, feral;
 
 	public TaxonBasicEditor() {
 		super();
 		setHeading("Basic Taxon Information Editor");
 		setIconStyle("icon-note-edit");
 		setSize(500, 300);
 		
 		node = TaxonomyCache.impl.getCurrentTaxon();
 		editor = new VerticalPanel();
 		if (node != null)
 			draw();
 		else {
 			add(new HTML("Please select a taxon first."));
 		}
 
 	}
 
 	private void buildButtons() {
 		bar = getButtonBar();
 		save = new Button("Save");
 		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				save(false);
 			}
 
 		});
 		saveAndClose = new Button("Save and Close");
 		saveAndClose.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				save(true);
 			}
 
 		});
 		close = new Button("Cancel");
 		close.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				hide();
 			}
 
 		});
 		bar.add(save);
 		bar.add(close);
 		bar.add(saveAndClose);
 	}
 
 	private void draw() {
 		addStyleName("gwt-background");
 		add(editor);
 		editor.setStyleName("gwt-background");
 
 		buildButtons();
 		drawInfo();
 
 		editor.add(bar);
 		editor.setCellVerticalAlignment(bar, HasVerticalAlignment.ALIGN_BOTTOM);
 	}
 
 	private void drawInfo() {
 		String width = "150px";
 		String htmlWidth = "80px";
 
 		HTML html = new HTML("Name: ");
 		html.setWidth(htmlWidth);
 		name = new TextBox();
 		name.setWidth(width);
 		name.setText(node.getName());
 		HorizontalPanel panel = new HorizontalPanel();
 		panel.setSpacing(4);
 		panel.add(html);
 		panel.add(name);
 		editor.add(panel);
 		int height = panel.getOffsetHeight() + 15;
 		editor.setCellHeight(panel, height + "px");
 
 		level = new ListBox();
 
 		if (node.getLevel() == TaxonLevel.SUBPOPULATION) {
 			level.addItem(node.getDisplayableLevel(), node.getLevel() + "");
 			level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_SUBSPECIES),
 					TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_SUBSPECIES);
 			if (!node.getFootprintAsString().contains("ANIMALIA"))
 				level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_VARIETY),
 						TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_VARIETY);
 			level.setEnabled(true);
 		} else if (node.getLevel() == TaxonLevel.INFRARANK) {
 			if (node.getInfratype().getName().equals(Infratype.SUBSPECIES_NAME)) {
 				level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_SUBSPECIES),
 						TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_SUBSPECIES);
 				if (!node.getFootprintAsString().contains("ANIMALIA"))
 					level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_VARIETY),
 							TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_VARIETY);
 			} else {
 				if (!node.getFootprintAsString().contains("ANIMALIA"))
 					level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_VARIETY),
 							TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_VARIETY);
 				level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_SUBSPECIES),
 						TaxonLevel.INFRARANK + "" + Infratype.INFRARANK_TYPE_SUBSPECIES);
 			}
 
 			save.setEnabled(false);
 			saveAndClose.setEnabled(false);
 
 			TaxonomyCache.impl.fetchPathWithID(node.getId() + "", new GenericCallback<NativeDocument>() {
 				public void onFailure(Throwable caught) {
 					level.setEnabled(false);
 
 					save.setEnabled(true);
 					saveAndClose.setEnabled(true);
 				}
 
 				public void onSuccess(NativeDocument ndoc) {
 					if (ndoc.getDocumentElement().getElementsByTagName("option").getLength() == 0) {
 						// It has no children - it can be changed to a subpop.
 						level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.SUBPOPULATION), TaxonLevel.SUBPOPULATION
 								+ "");
 						level.setEnabled(true);
 					} else
 						level.setEnabled(false);
 
 					save.setEnabled(true);
 					saveAndClose.setEnabled(true);
 				}
 			});
 		} else {
 			level.addItem(node.getDisplayableLevel(), node.getLevel() + "");
 			level.setEnabled(false);
 		}
 
 		html = new HTML("Level: ");
 		html.setWidth(htmlWidth);
 		panel = new HorizontalPanel();
 		panel.setSpacing(4);
 		panel.add(html);
 		panel.add(level);
 		editor.add(panel);
 		height = panel.getOffsetHeight() + 15;
 		editor.setCellHeight(panel, height + "px");
 
 		html = new HTML("Status: ");
 		html.setWidth(htmlWidth);
 		// status = new TextBox();
 		// status.setWidth(width);
 		// status.setText(node.getStatus());
 
 		status = new ListBox();
 		status.addItem("<Unset>", "");
 		status.addItem("New", "N");
 		status.addItem("Accepted", "A");
 		status.addItem("Discarded", "D");
 		status.addItem("Synonym", "S");
 		if (node.getLevel() >= TaxonLevel.SPECIES)
 			status.addItem("Undescribed", "U");
 
 		if (node.getStatusCode().equalsIgnoreCase(""))
 			status.setSelectedIndex(0);
 		else if (node.getStatusCode().equalsIgnoreCase("N"))
 			status.setSelectedIndex(1);
 		else if (node.getStatusCode().equalsIgnoreCase("A"))
 			status.setSelectedIndex(2);
 		else if (node.getStatusCode().equalsIgnoreCase("D"))
 			status.setSelectedIndex(3);
 		else if (node.getStatusCode().equalsIgnoreCase("S"))
 			status.setSelectedIndex(4);
 		else if (node.getStatusCode().equalsIgnoreCase("U"))
 			status.setSelectedIndex(5);
 		else {
 			status.addItem(node.getStatusCode());
 			status.setSelectedIndex(status.getItemCount() - 1);
 		}
 
 		panel = new HorizontalPanel();
 		panel.setSpacing(4);
 		panel.add(html);
 		panel.add(status);
 		editor.add(panel);
 		height = panel.getOffsetHeight() + 15;
 		editor.setCellHeight(panel, height + "px");
 
 		
 		editor.add(panel);
 		height = panel.getOffsetHeight() + 15;
 		editor.setCellHeight(panel, height + "px");
 
 		if (node.getLevel() >= TaxonLevel.SPECIES) {
 			html = new HTML("Hybrid: ");
 			html.setWidth(htmlWidth);
 			hybrid = new ListBox(false);
 			hybrid.setWidth(width);
 			hybrid.addItem("true", "1");
 			hybrid.addItem("false", "0");
 			hybrid.setSelectedIndex(node.getHybrid() ? 0 : 1);
 			panel = new HorizontalPanel();
 			panel.setSpacing(4);
 			panel.add(html);
 			panel.add(hybrid);
 			editor.add(panel);
 			height = panel.getOffsetHeight() + 15;
 			editor.setCellHeight(panel, height + "px");
 		}
 
 		html = new HTML("Taxonomic Authority: ");
 		html.setWidth(htmlWidth);
 		taxonomicAuthority = new TextBox();
 		taxonomicAuthority.setWidth(width);
 		taxonomicAuthority.setText(node.getTaxonomicAuthority());
 		panel = new HorizontalPanel();
 		panel.setSpacing(4);
 		panel.add(html);
 		panel.add(taxonomicAuthority);
 		editor.add(panel);
 		height = panel.getOffsetHeight() + 15;
 		editor.setCellHeight(panel, height + "px");
 		
 		//if (node.getLevel() == TaxonLevel.SPECIES) {
 			html = new HTML("Invasive?" );
 			html.setWidth(htmlWidth);
 			invasive = new CheckBox();
 			invasive.setValue(node.getInvasive());
 			panel = new HorizontalPanel();
 			panel.setSpacing(4);
 			panel.add(html);
 			panel.add(invasive);
 			editor.add(panel);
 			height = panel.getOffsetHeight() + 15;
 			editor.setCellHeight(panel, height + "px");
 			
 			html = new HTML("Feral? ");
 			html.setWidth(htmlWidth);
 			feral = new CheckBox();
 			feral.setValue(node.getFeral());
 			panel = new HorizontalPanel();
 			panel.setSpacing(4);
 			panel.add(html);
 			panel.add(feral);
 			editor.add(panel);
 			height = panel.getOffsetHeight() + 15;
 			editor.setCellHeight(panel, height + "px");
 		//}
 	}
 
 	private void refresh() {
 		//manager.taxonomicSummaryPanel.update(node.getId());
 		//ClientUIContainer.bodyContainer.refreshBody();
 		TaxonomyCache.impl.fetchTaxon(node.getId(), new GenericCallback<Taxon>() {
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Could not reload taxon, please try again later.");
 			}
 			public void onSuccess(Taxon result) {
				ClientUIContainer.headerContainer.centerPanel.refreshTaxonView();
 				ClientUIContainer.bodyContainer.refreshBody();
 			}
 		});
 	}
 
 	private void save(final boolean closeAfterSave) {
 		saveInfo();
 		TaxomaticUtils.impl.saveTaxon(node, new GenericCallback<Object>() {
 			public void onFailure(Throwable caught) {
 				if (caught instanceof TaxonomyException)
 					WindowUtils.errorAlert(caught.getMessage());
 				else if (caught instanceof GWTConflictException) {
 					WindowUtils.infoAlert("Error", node.getFullName() + " has not been saved. A taxon"
 							+ " in the kingdom " + node.getKingdomName() + " already exists.");
 					TaxonomyCache.impl.evict(node.getId()+"");
 				} else {
 					WindowUtils.errorAlert(node.getFullName() + " has not been saved.  "
 							+ "Please try again later.");
 				}
 				bar.setEnabled(true);
 			}
 
 			public void onSuccess(Object arg0) {
 				WindowUtils.infoAlert("Success", node.getFullName() + " has been saved.");
 				if (closeAfterSave) {
 					hide();
 					refresh();
 				}
 				else {
 					bar.setEnabled(true);
 					refresh();
 				}
 			}
 
 		});
 	}
 
 	private void saveInfo() {
 		bar.setEnabled(false);
 
 		if (node.getLevel() >= TaxonLevel.SPECIES)
 			node.setHybrid(hybrid.getValue(hybrid.getSelectedIndex()).equalsIgnoreCase("1") ? true : false);
 
 		if (level.isEnabled()) {
 			int newLevel = Integer.parseInt(level.getValue(level.getSelectedIndex()));
 			int infraType = 1;
 			if (newLevel ==  TaxonLevel.INFRARANK || newLevel == TaxonLevel.INFRARANK_SUBPOPULATION) {
 				infraType = newLevel % (TaxonLevel.INFRARANK * 10);
 				newLevel = Integer.parseInt(level.getValue(level.getSelectedIndex()).substring(0, 1));
 				node.setTaxonLevel(TaxonLevel.getTaxonLevel(newLevel));
 				Infratype type = Infratype.getInfratype(infraType, node);
 				node.setInfratype(type);
 			} else {
 				node.setInfratype(null);
 			}
 			
 		}
 
 		node.setName(name.getText());
 		node.correctFullName();
 		node.setStatus(status.getValue(status.getSelectedIndex()));
 		node.setTaxonomicAuthority(taxonomicAuthority.getText());
 		if (node.getLevel() == TaxonLevel.SPECIES) {
 			node.setInvasive(invasive.getValue());
 			node.setFeral(feral.getValue());
 		}
 		close.setText("Close");
 	}
 }
