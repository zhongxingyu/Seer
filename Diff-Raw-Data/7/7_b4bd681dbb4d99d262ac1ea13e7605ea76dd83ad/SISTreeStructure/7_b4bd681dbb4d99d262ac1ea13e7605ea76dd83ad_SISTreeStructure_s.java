 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.shared.api.data.DisplayDataProcessor;
 import org.iucn.sis.shared.api.data.TreeData;
 import org.iucn.sis.shared.api.data.TreeDataRow;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
 import org.iucn.sis.shared.api.schemes.BasicClassificationSchemeViewer;
 import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
 import org.iucn.sis.shared.api.schemes.ClassificationSchemeViewer;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.extjs.client.WindowUtils.SimpleMessageBoxListener;
 import com.solertium.util.gwt.ui.StyledHTML;
 
 public class SISTreeStructure extends Structure<Field> {
 	
 	protected final Map<String, TreeDataRow> flatTree;
 	
 	private ClassificationSchemeViewer viewer;
 	private VerticalPanel readOnlyContainer;
 
 	public SISTreeStructure(String struct, String descript, String structID, Object data) {
 		super(struct, descript, structID, data);
 		this.flatTree = ((TreeData)data).flattenTree();
 		
 		buildContentPanel(Orientation.VERTICAL);
 	}
 	
 	@Override
 	public void clearData() {
 		viewer = null;
 		displayPanel.clear();
 	}
 	
 	@Override
 	protected Widget createLabel() {
 		return generateContent(false);
 	}
 	
 	@Override
 	protected Widget createViewOnlyLabel() {
 		return generateContent(true);
 	}
 	
 	protected Widget generateContent(final boolean viewOnly) {
 		displayPanel.add(readOnlyContainer);
 		
 		String buttonText = viewOnly ? "View Details" : "Make Changes";
 		
 		displayPanel.add(new Button(buttonText, new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				final Window window = new Window();
 				window.setModal(true);
 				window.setSize(800, 600);
 				window.setHeading(description);
 				window.setLayout(new FillLayout());
 				window.setLayoutOnChange(true);
 				window.add(viewer.draw(viewOnly));
 				window.setButtonAlign(HorizontalAlignment.CENTER);
 				window.addButton(new Button("Done", new SelectionListener<ButtonEvent>() {
 					public void componentSelected(ButtonEvent ce) {
 						SimpleMessageBoxListener listener = new SimpleMessageBoxListener() {
 							public void onYes() {
 								buildReadOnlyContainer(viewer.save(false));
 								window.hide();
 							}
 						};
 						
 						if (!viewer.isEditing())
 							listener.onYes();
 						else
 							WindowUtils.confirmAlert("Confirm", "It appears you are " +
 								"still editing data. If you close this window now, " +
 								"you will lose any unsaved changes. Are you sure you " +
 								"want to close this window?", listener);
 					}
 				}));
 				window.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
 					public void componentSelected(ButtonEvent ce) {
 						viewer.revert();
 						window.hide();
 					}
 				}));
 				window.addListener(Events.Show, new Listener<ComponentEvent>() {
 					@Override
 					public void handleEvent(ComponentEvent be) {
 						window.layout();
 					}
 				});
 				window.show();
 			}
 		}));
 		
 		return displayPanel;
 	}
 	
 	private void buildReadOnlyContainer(Field field) {
 		final List<ClassificationSchemeModelData> thinData = new ArrayList<ClassificationSchemeModelData>();
 		if (field != null && field.getFields() != null) {
 			for (Field subfield : field.getFields()) {
 				PrimitiveField lookup = subfield.getPrimitiveField(getId()+"Lookup"); 
 				if (lookup == null || !flatTree.containsKey(lookup.getRawValue()))
 					continue;
 				
 				TreeDataRow row = flatTree.get(lookup.getRawValue());
 				
 				ClassificationSchemeModelData model = new ClassificationSchemeModelData(null);
 				model.setSelectedRow(row);
 				
 				thinData.add(model); 
 			}
 		}
 		
 		buildReadOnlyContainer(thinData);
 	}
 	
 	private void buildReadOnlyContainer(Collection<? extends ClassificationSchemeModelData> thinData) {
 		readOnlyContainer.clear();
 		readOnlyContainer.add(new StyledHTML("Selections for " + description + ":", "bold"));
 		
 		if (thinData.isEmpty()) {
 			readOnlyContainer.add(new HTML("No selections made"));
 		}
 		else {
 			for (ClassificationSchemeModelData model : thinData) {
 				readOnlyContainer.add(new HTML(model.getSelectedRow().getFullLineage()));
 			}
 		}
 	}
 	
 	@Override
 	public void createWidget() {
 		readOnlyContainer = new VerticalPanel();
 		
 		viewer = new BasicClassificationSchemeViewer(description, (TreeData)data);
 	}
 
 	@Override
 	public ArrayList<String> extractDescriptions() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public List<ClassificationInfo> getClassificationInfo() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public String getData() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public int getDisplayableData(ArrayList<String> rawData,
 			ArrayList<String> prettyData, int offset) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	@Override
 	public boolean hasChanged(Field field) {
 		return viewer.hasChanged();
 	}
 	
 	@Override
 	public void save(Field parent, Field field) {
 		if (field == null) {
 			field = new Field(getId(), null);
 			field.setParent(parent);
 			parent.getFields().add(field);
 		}
 		
 		for (ClassificationSchemeModelData model : viewer.save(true)) {
 			Field subfield = model.getField();
 			if (subfield == null) {
 				subfield = new Field();
 				subfield.setParent(field);
 				subfield.setName(getId()+"Subfield");
 				
 				model.setField(subfield);
 			}
 			
 			model.save(field, subfield);
 			
 			PrimitiveField lookup = subfield.getPrimitiveField(getId()+"Lookup");
 			if (lookup == null)
 				lookup = new ForeignKeyPrimitiveField(getId()+"Lookup", subfield);
 			
 			lookup.setRawValue(model.getSelectedRow().getDisplayId());
 			
 			subfield.addPrimitiveField(lookup);
 			
 			/*
 			 * Since I know these two fields have the same name, I want 
 			 * to let the set's pure equals method go to work here, and 
 			 * eliminate based on ID.  This way, I can have two fields 
 			 * with the same name.
 			 */
 			field.getFields().add(subfield);
 		}
 	}
 	
 	@Override
 	public void setData(Field field) {
 		buildReadOnlyContainer(field);
 		
 		if (field != null) {
 			//TODO: This should create the appropriate models based off the subfields...
 			final List<ClassificationSchemeModelData> models = 
 				new ArrayList<ClassificationSchemeModelData>();
 			
 			for (Field subfield : field.getFields()) {
 				PrimitiveField lookup = subfield.getPrimitiveField(getId()+"Lookup"); 
 				if (lookup == null || !flatTree.containsKey(lookup.getRawValue()))
 					continue;
 				
 				TreeDataRow row = flatTree.get(lookup.getRawValue());
 				
 				final DisplayStructure structure = generateDefaultDisplayStructure(row);
 					
 				ClassificationSchemeModelData model = createModelData(structure, subfield);
 				model.setSelectedRow(row);
 				
 				models.add(model); 
 			}
 			
 			viewer.setData(models);
 		}
 		else
 			viewer.setData(new ArrayList<ClassificationSchemeModelData>());
 	}
 	
 	protected DisplayStructure generateDefaultDisplayStructure(TreeDataRow row) {
 		return DisplayDataProcessor.processDisplayStructure(((TreeData)data).getDefaultStructure());
 	}
 	
 	protected ClassificationSchemeModelData createModelData(DisplayStructure structure, Field field) {
 		return new ClassificationSchemeModelData(structure, field);
 	}
 	
 	@Override
 	public void setEnabled(boolean isEnabled) {
 		
 	}
 
 }
