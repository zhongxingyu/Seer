 package org.mindinformatics.gwt.domeo.client.ui.sets;
 
 import org.mindinformatics.gwt.domeo.client.Domeo;
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.model.MAnnotationSet;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class AnnotationVisibilityPopup extends PopupPanel {
 
 	//public static final CurationPopupResources localResources = 
 	//	GWT.create(AnnotationAccessResources.class);
 	
 	private IDomeo _domeo;
 	private AnnotationVisibilityPopup _this;
 	private ILensRefresh _lens;
 	private MAnnotationSet _set;
 	
 	private VerticalPanel vp = new VerticalPanel();
 	
 	public AnnotationVisibilityPopup(IDomeo domeo, ILensRefresh lens, MAnnotationSet set) {
 		super(true, true);
 		
 		_domeo = domeo;
 		_this = this;
 		_lens = lens;
 		_set = set;
 
 		this.setAnimationEnabled(true);
 		this.setWidget(vp);
 		
 		
 	}
 	
 	public void show(int x, int y) {
 		buildPanel();
 		this.setPopupPosition(x, y);
 		this.show();
 	}
 	
 	
 	public void buildPanel() {
 		HorizontalPanel visiblePanel = new HorizontalPanel();
 		RadioButton visibleRadio = new RadioButton("myRadioGroup", "");
 		if(_set.getIsVisible()) visibleRadio.setValue(true);
 		visibleRadio.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				_domeo.getLogger().command(_this, "Set AnnotationSet " + _set.getLocalId() + " as visible");
 				_domeo.getContentPanel().getAnnotationFrameWrapper().manageSetVisibility(_set, true);
 				_domeo.getLogger().command(_this, "Refreshing lens");
 				_domeo.getComponentsManager().updateObjectLenses(_set);
 				_lens.refresh();
 				_this.hide();
 			}
 		});
 		visiblePanel.add(new Image(Domeo.resources.visibleLittleIcon()));
 		visiblePanel.add(visibleRadio);
 		visiblePanel.add(new Label("Visible"));
 		vp.add(visiblePanel);
 		
 		HorizontalPanel hiddenPanel = new HorizontalPanel();
 		RadioButton hiddenRadio = new RadioButton("myRadioGroup", "");
 		if(!_set.getIsVisible()) hiddenRadio.setValue(true);
 		hiddenRadio.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				_domeo.getLogger().command(_this, "Set AnnotationSet " + _set.getLocalId() + " as hidden");
 				_domeo.getContentPanel().getAnnotationFrameWrapper().manageSetVisibility(_set, false);
 				_domeo.getLogger().command(_this, "Refreshing lens");
				_domeo.refreshAnnotationComponents();
 				_lens.refresh();
 				_this.hide();
 			}
 		});
 		hiddenPanel.add(new Image(Domeo.resources.invisibleLittleIcon()));
 		hiddenPanel.add(hiddenRadio);
 		hiddenPanel.add(new Label("Hidden"));
 		vp.add(hiddenPanel);
 	}
 }
