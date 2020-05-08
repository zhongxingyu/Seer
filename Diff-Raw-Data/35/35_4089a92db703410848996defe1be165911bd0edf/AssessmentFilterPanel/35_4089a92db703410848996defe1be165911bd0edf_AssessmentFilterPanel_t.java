 package org.iucn.sis.client.panels.filters;
 
 import java.util.Iterator;
 
 import org.iucn.sis.shared.api.models.AssessmentFilter;
 import org.iucn.sis.shared.api.models.Region;
 
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
 import com.extjs.gxt.ui.client.widget.form.MultiField;
 import com.extjs.gxt.ui.client.widget.form.Radio;
 import com.extjs.gxt.ui.client.widget.form.RadioGroup;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.user.client.ui.HTML;
 
 public class AssessmentFilterPanel extends HorizontalPanel {
 	
 	protected AssessmentFilter filter;
	protected CheckBox allDraft;
 	protected CheckBox allPublished;
 	protected CheckBox recentPublisheds;
	
 	protected boolean addDescription;
 	protected boolean allowAllPublishedAssessments;
 	protected boolean allowAllLocales;
 	protected boolean allowMatchesAnyRegion;
 
 	protected AssessmentFilterRegionalPanel regionPanel;
 	
 	public AssessmentFilterPanel(AssessmentFilter filter, boolean addDescription) {
 		this(filter, addDescription, false, false, false);
 	}
 
 	/**
 	 * Called with whether to display as admin functionality or with regular functionality
 	 *  
 	 * @param filter
 	 * @param admin
 	 */
 	public AssessmentFilterPanel(AssessmentFilter filter, boolean addDescription, boolean allowAllLocales, boolean allPublishedAssessments, boolean allowMatchesAnyRegion) {
 		this.filter = filter;
 		this.addDescription = addDescription;
 		this.allowAllPublishedAssessments = allPublishedAssessments;
 		this.allowMatchesAnyRegion = allowMatchesAnyRegion;
 		this.allowAllLocales = allowAllLocales;
 		draw();
 	}
 
 	/**
 	 * Returns an error message if not valid, otherwise returns null
 	 */
 	public String checkValidity() {
 		if (!(allPublished.getValue() || recentPublisheds.getValue() || allDraft
 				.getValue())) {
 			return "Please select an assessment type.";
 		}
 		
 		if (!regionPanel.anyRegionSelected()) {
 			return "Please select a locality";
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns the field that represents the assessment type options
 	 * 
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private MultiField drawTypes() {
 
 		allPublished = new CheckBox();
 		if (allowAllPublishedAssessments)
 		{
 			recentPublisheds = new CheckBox();
 			allDraft = new CheckBox();			
 
 			allPublished.setWidth(85);	
 			allPublished.setBoxLabel("Published");
 			allPublished.setValue(filter.isAllPublished());
 			allPublished.addListener(Events.Change, new Listener<FieldEvent>() {
 
 				public void handleEvent(FieldEvent be) {
 					if ((Boolean) be.getValue()) {
 						recentPublisheds.setValue(new Boolean(false));
 					}
 
 				}
 			});
 		}
 		else {
 			recentPublisheds = new Radio();
 			allDraft = new Radio();
 			if (allowAllPublishedAssessments)
 				allPublished = new Radio();
 		}
 
 		
 		recentPublisheds.setBoxLabel("Latest Published");
 		recentPublisheds.setValue(filter.isRecentPublished());
 		recentPublisheds.addListener(Events.Change, new Listener<FieldEvent>() {
 
 			public void handleEvent(FieldEvent be) {
 				if ((Boolean) be.getValue()) {
 					// if (allTypes.getValue())
 					// allTypes.setValue(new Boolean(false));
 
 					if (allPublished.getValue())
 						allPublished.setValue(new Boolean(false));
 				}
 			}
 
 		});
 
 		allDraft.setBoxLabel("Draft");
 		allDraft.setValue(filter.isDraft());
 		
 		
 		
 		if (allowAllPublishedAssessments)
 		{
 			CheckBoxGroup typeChecks = new CheckBoxGroup();
 			typeChecks.setSpacing(4);
 			typeChecks.setAutoWidth(true);
 			typeChecks.setFieldLabel("Select assessment type");
 			typeChecks.add(allPublished);
 			typeChecks.add(recentPublisheds);
 			typeChecks.add(allDraft);
 			return typeChecks;
 		}
 		else {
 			RadioGroup typeChecks = new RadioGroup("assessmentTypes");
 			typeChecks.setSpacing(4);
 			typeChecks.setAutoWidth(true);
 			typeChecks.setFieldLabel("Select assessment type");
 			if (allowAllPublishedAssessments)
 				typeChecks.add((Radio)allPublished);
 			typeChecks.add((Radio)recentPublisheds);
 			typeChecks.add((Radio)allDraft);
 			return typeChecks;
 		}
 		
 		
 		
 	}
 
 	private LayoutContainer drawRegions() {
 		regionPanel = new AssessmentFilterRegionalPanel(allowAllLocales, filter, allowMatchesAnyRegion);
 		regionPanel.draw();
 		return regionPanel;		
 	}
 
 
 	private void draw() {
 		setBorders(false);
 		
 		if (addDescription) {
 			LayoutContainer vp = new LayoutContainer();
 			RowLayout layout = new RowLayout();
 			vp.setLayout(layout);
 			RowData data = new RowData();
 			Margins margin = new Margins(6,4,4,0);
 			data.setMargins(margin);
 			
 			RowData data1 = new RowData();
 			margin = new Margins(10, 4,4,0);
 			data1.setMargins(margin);
 			vp.add(new HTML("Choose type of assessment: "), data);
 			vp.add(new HTML("Choose assessment locale: "), data1);
 			add(vp);
 		}
 		
 		VerticalPanel vp = new VerticalPanel();
 		vp.add(drawTypes());
 		vp.add(drawRegions());
 		add(vp);
 		this.layout();
 	}
 
 	public AssessmentFilter getFilter() {
 		putIntoAssessmentFilter();
 		
 		return filter;
 	}
 
 	public boolean putIntoAssessmentFilter() {
 		if (checkValidity() == null) {
			filter.setAllPublished(allowAllPublishedAssessments && allPublished.getValue());
 			filter.setDraft(allDraft.getValue());
 			filter.setRecentPublished(recentPublisheds.getValue());
 
 			regionPanel.putIntoAssessmentFilter(filter);
 
 			return true;
 		}
 		return false;
 	}
 
 	public static String getString(AssessmentFilter filter) {
 		StringBuilder str = new StringBuilder();
 
 		if (filter.isAllPublished() && filter.isDraft()	&& filter.isAllRegions())
 			str.append("all assessments");
 		else {
 			if (filter.isAllPublished()) {
 				str.append("all published");
 				if (filter.isDraft())
 					str.append(" and draft");
 				str.append(" assessments");
 
 			} else if (filter.isRecentPublished()) {
 				str.append("most recent published");
 				if (filter.isDraft())
 					str.append(" and draft");
 				str.append(" assessments");
 			}
 
 			else if (filter.isDraft())
 				str.append("draft assessments");
 
 			str.append(getRegionString(filter));
 		}
 
 		return str.toString();
 	}
 	
 	public static String getRegionString(AssessmentFilter filter) {
 		StringBuilder regionText = new StringBuilder();
 		if (!filter.isAllRegions()) {
 			regionText.append(" with ");
 			for (Iterator<Region> iter = filter.getRegions().iterator(); iter.hasNext(); ) {
 				Region region = iter.next();
 				if (!iter.hasNext() && !"".equals(regionText.toString())) {
 					if (filter.getRegions().size() > 1)
 						regionText.append(filter.getRegionType() + " ");
 					regionText.append(region.getRegionName());
 				}
 				else {
 					regionText.append(region.getRegionName());
 					if (iter.hasNext() && filter.getRegions().size() > 2)
 						regionText.append(", ");
 					else
 						regionText.append(" ");
 				}
 			}
 			regionText.append(" locality");
 		}
 		else {
 			regionText.append(" for all regions");
 		}
 		return regionText.toString();
 	}
 
 
 	public void setFilter(AssessmentFilter filter) {
 		this.filter = filter;
 		
 		recentPublisheds.setValue(filter.isRecentPublished());
 		allPublished.setValue(filter.isAllPublished());
 		allDraft.setValue(filter.isDraft());
 		
 		
 		regionPanel.setFilter(filter);
 		this.layout();
 
 	}
 	
 	@Override
 	public void setEnabled(boolean enabled) {
 		super.setEnabled(enabled);
 		regionPanel.setEnabled(enabled);	
 	}
 
 }
