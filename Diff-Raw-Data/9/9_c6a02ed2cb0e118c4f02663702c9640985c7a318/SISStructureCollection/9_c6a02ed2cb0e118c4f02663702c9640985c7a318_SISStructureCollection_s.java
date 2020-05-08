 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.utils.XMLUtils;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 @SuppressWarnings("unchecked")
 public class SISStructureCollection extends Structure<Field> {
 
 	public static final int TREE = 0;
 	public static final int VERTICAL_PANEL = 1;
 	public static final int HORIZONTAL_PANEL = 2;
 	public static final int FLEXTABLE = 3;
 
 	private ArrayList<DisplayStructure> structures;
 	private int displayType = 1;
 
 	public SISStructureCollection(String structure, String description, String structID, Object structures) {
 		this(structure, description, structID, structures, 1);
 	}
 
 	public SISStructureCollection(String structure, String description, String structID, Object structures, int displayType) {
 		super(structure, description, structID, structures);
 
 		// displayPanel = new VerticalPanel();
 		this.displayType = displayType;
 		// HashMap initValues = new HashMap();
 		// for (int i = 0; i < this.structures.size(); i++) {
 		// initValues.putAll(((Structure)this.structures.get(i)).getValues());
 		// }
 		// tracker.setInitValues(initValues);
 	}
 	
 	@Override
 	public void save(Field parent, Field field) {
 		if (field == null) {
 			if (getId() != null) {
 				field = new Field();
 				field.setName(getId());
 				field.setParent(parent);
 				
 				parent.addField(field);
 			}
 			else
 				field = parent;
 		}
 		
 		for (DisplayStructure cur : structures) {
 			Debug.println("Saving structure of type {0} with id {1}", cur.getStructureType(), cur.getId());
 			if (cur.isPrimitive())
 				cur.save(field, field.getPrimitiveField(cur.getId()));
 			else {
 				if (cur.hasId())
 					cur.save(field, field.getField(cur.getId()));
 				else
 					cur.save(null, field);
 			}
 		}
 	}
 
 	@Override
 	public boolean hasChanged(Field field) {
 		for (DisplayStructure cur : structures) {
 			boolean hasChanged;
 			if (cur.isPrimitive())
 				hasChanged = cur.hasChanged(field == null ? null : field.getPrimitiveField(cur.getId()));
 			else {
 				if (cur.hasId())
 					hasChanged = cur.hasChanged(field == null ? null : field.getField(cur.getId()));
 				else
 					hasChanged = cur.hasChanged(field);
 			}
 			
 			if (hasChanged)
 				return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public void clearData() {
 		for (int i = 0; i < structures.size(); i++)
 			(structures.get(i)).clearData();
 	}
 
 	@Override
 	public Widget createLabel() {
 		return createLabel(false);
 	}
 
 	private Widget createLabel(boolean viewOnly) {
 		if (displayType == 1) {		
 			// displayPanel = new VerticalPanel();
 			buildContentPanel(Orientation.VERTICAL);
 			((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 			// displayPanel.add(descriptionLabel);
 			for (int i = 0; i < structures.size(); i++) {
 				if (viewOnly)
 					displayPanel.add(( structures.get(i)).generateViewOnly());
 				else
 					displayPanel.add(( structures.get(i)).generate());
 			}
 			return displayPanel;
 		}
 		// else if (displayType == 2) {
 		else {
 			// displayPanel = new HorizontalPanel();
 			buildContentPanel(Orientation.HORIZONTAL);
 			((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 			// displayPanel.add(descriptionLabel);
 			for (int i = 0; i < structures.size(); i++) {
 				if (viewOnly)
 					displayPanel.add(( structures.get(i)).generateViewOnly());
 				else
 					displayPanel.add(( structures.get(i)).generate());
 			}
 			return displayPanel;
 		}
 		// else if (displayType == 3) {
 		// displayPanel = new FlexTable();
 		// int insert = 0;
 		// if (descriptionLabel != null)
 		// ((FlexTable)displayPanel).setWidget(0, insert++, descriptionLabel);
 		// for (int i = 0; i < structures.size(); i++) {
 		// if (viewOnly)
 		// ((FlexTable)displayPanel).setWidget(0, insert++,
 		// ((Structure)structures.get(i)).generateViewOnly());
 		// else
 		// ((FlexTable)displayPanel).setWidget(0, insert++,
 		// ((Structure)structures.get(i)).generate());
 		// }
 		// return displayPanel;
 		// }
 		// else
 		// return null;
 	}
 
 	@Override
 	public Widget createViewOnlyLabel() {
 		return createLabel(true);
 	}
 
 	@Override
 	public void createWidget() {
 		structures = (ArrayList) data;
 		descriptionLabel = new HTML(description);
 	}
 
 	/**
 	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
 	 * if it contains multiples structures, all of those, in order.
 	 */
 	@Override
 	public ArrayList<String> extractDescriptions() {
 		ArrayList<String> ret = new ArrayList<String>();
 
 		for (DisplayStructure<?, ?> structure : structures)
 			ret.addAll(structure.extractDescriptions());
 
 		return ret;
 	}
 	
 	@Override
 	public List<ClassificationInfo> getClassificationInfo() {
 		ArrayList<ClassificationInfo> list = new ArrayList<ClassificationInfo>();
 		
 		for (DisplayStructure<?, ?> structure : structures)
 			list.addAll(structure.getClassificationInfo());
 		
 		return list;
 	}
 
 	@Override
 	public String getData() {
 		return null;
 	}
 
 	/**
 	 * Pass in the raw data from an Assessment object, and this will return
 	 * it in happy, displayable String form
 	 * 
 	 * @return ArrayList of Strings, having converted the rawData to nicely
 	 *         displayable String data. Happy days!
 	 */
 	@Override
 	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
 		for (DisplayStructure<?, ?> structure : structures)
 			offset = (structure.getDisplayableData(rawData, prettyData, offset));
 
 		return offset;
 	}
 
 	public DisplayStructure<?, ?> getStructureAt(int index) {
 		return structures.get(index);
 	}
 
 	public ArrayList<DisplayStructure> getStructures() {
 		return structures;
 	}
 	
 	@Override
 	public void setData(Field field) {
 		for (DisplayStructure structure : structures) {
 			if (structure.isPrimitive())
 				structure.setData(field == null ? null : field.getPrimitiveField(structure.getId()));
 			else
 				if (structure.hasId())
 					structure.setData(field == null ? null : field.getField(structure.getId()));
 				else
 					structure.setData(field);
 			/*for(String key: ((Structure) structures.get(i)).extractModelData().getPropertyNames())
 				model.set(key, ((Structure) structures.get(i)).extractModelData().get(key));*/
 		}
 	}
 
 	public void setDisplayType(int displayType) {
 		this.displayType = displayType;
 		for (int i = 0; i < structures.size(); i++) {
 			if (getStructureAt(i).getStructureType().equalsIgnoreCase(XMLUtils.STRUCTURE_COLLECTION))
 				((SISStructureCollection) getStructureAt(i)).setDisplayType(displayType);
 		}
 	}
 
 	@Override
 	public void setEnabled(boolean isEnabled) {
 		for (DisplayStructure<?, ?> structure : structures)
 			structure.setEnabled(isEnabled);
 	}
 	
 	public boolean hideDescriptionLabel(boolean forever) {
		if (!structures.isEmpty())
			return ((Structure)getStructureAt(0)).hideDescriptionLabel(forever);
 		else
 			return false;
 	}
 	
 }
