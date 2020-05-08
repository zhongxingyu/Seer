 package org.iplantc.core.client.pipelines.gxt3.views;
 
 import org.iplant.pipeline.client.builder.PipelineCreator;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.widget.core.client.button.ToggleButton;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
 import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.SimpleContainer;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 
 /**
  * The main PipelineView implementation.
  * 
  * @author psarando
  * 
  */
 public class PipelineViewImpl implements PipelineView {
 
     private static PipelineViewUiBinder uiBinder = GWT.create(PipelineViewUiBinder.class);
     private final Widget widget;
     private Presenter presenter;
 
     @UiTemplate("PipelineView.ui.xml")
     interface PipelineViewUiBinder extends UiBinder<Widget, PipelineViewImpl> {
     }
 
     public PipelineViewImpl() {
         widget = uiBinder.createAndBindUi(this);
     }
 
     @UiField
     BorderLayoutContainer borders;
 
     @UiField
     BorderLayoutData northData;
 
     @UiField
     CardLayoutContainer centerPanel;
 
     @UiField
     SimpleContainer builderPanel;
 
    @UiField
     PipelineCreator builder;
 
     @UiField
     BorderLayoutContainer stepEditorPanel;
 
     @UiField
     CardLayoutContainer stepPanel;
 
     @UiField
     SimpleContainer infoPanel;
 
     @UiField
     SimpleContainer appOrderPanel;
 
     @UiField
     SimpleContainer mappingPanel;
 
     @UiField
     ToggleButton infoBtn;
 
     @UiField
     ToggleButton appOrderBtn;
 
     @UiField
     ToggleButton mappingBtn;
 
     @UiHandler("infoBtn")
     public void onInfoClick(SelectEvent e) {
         presenter.onInfoClick();
     }
 
     @UiHandler("appOrderBtn")
     public void onAppOrderClick(SelectEvent e) {
         presenter.onAppOrderClick();
     }
 
     @UiHandler("mappingBtn")
     public void onMappingClick(SelectEvent e) {
         presenter.onMappingClick();
     }
 
     @Override
     public Widget asWidget() {
         return widget;
     }
 
     @Override
     public void setPresenter(Presenter presenter) {
         this.presenter = presenter;
     }
 
     @Override
     public void setNorthWidget(IsWidget widget) {
         borders.setNorthWidget(widget, northData);
     }
 
     @Override
     public IsWidget getActiveView() {
         return centerPanel.getActiveWidget();
     }
 
     @Override
     public void setActiveView(IsWidget view) {
         centerPanel.setActiveWidget(view);
     }
 
     @Override
     public SimpleContainer getBuilderPanel() {
         return builderPanel;
     }
 
     @Override
     public BorderLayoutContainer getStepEditorPanel() {
         return stepEditorPanel;
     }
 
     @Override
     public CardLayoutContainer getStepPanel() {
         return stepPanel;
     }
 
     @Override
     public SimpleContainer getInfoPanel() {
         return infoPanel;
     }
 
     @Override
     public SimpleContainer getAppOrderPanel() {
         return appOrderPanel;
     }
 
     @Override
     public SimpleContainer getMappingPanel() {
         return mappingPanel;
     }
 
     @Override
     public void setInfoButtonPressed(boolean pressed) {
         infoBtn.setValue(pressed);
     }
 
     @Override
     public void setAppOrderButtonPressed(boolean pressed) {
         appOrderBtn.setValue(pressed);
     }
 
     @Override
     public void setMappingButtonPressed(boolean pressed) {
         mappingBtn.setValue(pressed);
     }
 }
