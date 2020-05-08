 package org.iplantc.de.apps.integration.client.view.propertyEditors;
 
 import org.iplantc.de.apps.integration.client.events.UpdateCommandLinePreviewEvent;
 import org.iplantc.de.apps.integration.client.events.UpdateCommandLinePreviewEvent.UpdateCommandLinePreviewEventHandler;
 import org.iplantc.de.apps.integration.client.view.propertyEditors.style.AppTemplateWizardPropertyContentPanelAppearance;
 import org.iplantc.de.apps.integration.client.view.propertyEditors.util.FinishEditing;
 import org.iplantc.de.apps.integration.client.view.propertyEditors.util.PrefixedHasTextEditor;
 import org.iplantc.de.apps.widgets.client.view.AppTemplateForm.ArgumentEditor;
 import org.iplantc.de.apps.widgets.client.view.AppTemplateForm.IArgumentEditorConverter;
 import org.iplantc.de.apps.widgets.client.view.editors.ReferenceGenomeProperties;
 import org.iplantc.de.apps.widgets.client.view.editors.style.AppTemplateWizardAppearance;
 import org.iplantc.de.client.models.apps.integration.Argument;
 import org.iplantc.de.client.models.apps.integration.DataSource;
 import org.iplantc.de.client.models.apps.integration.FileInfoType;
 import org.iplantc.de.client.models.apps.integration.ReferenceGenome;
 import org.iplantc.de.client.services.AppMetadataServiceFacade;
 import org.iplantc.de.commons.client.ErrorHandler;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.EditorContext;
 import com.google.gwt.editor.client.EditorVisitor;
 import com.google.gwt.editor.client.LeafValueEditor;
 import com.google.gwt.editor.client.impl.AbstractEditorDelegate;
 import com.google.gwt.editor.client.impl.Refresher;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.autobean.shared.Splittable;
 
 import static com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction.ALL;
 
 import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
 import com.sencha.gxt.data.shared.LabelProvider;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.SortDir;
 import com.sencha.gxt.data.shared.Store;
 import com.sencha.gxt.data.shared.Store.StoreSortInfo;
 import com.sencha.gxt.data.shared.event.StoreAddEvent;
 import com.sencha.gxt.data.shared.event.StoreAddEvent.StoreAddHandler;
 import com.sencha.gxt.widget.core.client.Composite;
 import com.sencha.gxt.widget.core.client.ContentPanel;
 import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
 
 import java.util.List;
 public abstract class AbstractArgumentPropertyEditor extends Composite implements ArgumentPropertyEditor {
     
     protected final class InitializeTwoWayBinding extends EditorVisitor {
 
         private final class ArgumentPropertyBindingHandler<T> implements ValueChangeHandler<T> {
 
             private final ArgumentPropertyEditor vcPpropEditor;
 
             public ArgumentPropertyBindingHandler(ArgumentPropertyEditor propEditor) {
                 this.vcPpropEditor = propEditor;
             }
 
             @Override
             public void onValueChange(ValueChangeEvent<T> event) {
                 doBinding();
             }
 
             private void doBinding() {
                 vcPpropEditor.getEditorDriver().flush();
                 AbstractEditorDelegate<Argument, ? extends Editor<Argument>> boundEditorDelegate = vcPpropEditor.getBoundEditorDelegate();
                 if (boundEditorDelegate != null) {
                     boundEditorDelegate.accept(new Refresher());
                     vcPpropEditor.asWidget().fireEvent(new UpdateCommandLinePreviewEvent());
                 }
             }
 
         }
 
         private final ArgumentPropertyEditor propEditor;
 
         public InitializeTwoWayBinding(ArgumentPropertyEditor propEditor) {
             this.propEditor = propEditor;
         }
 
         @Override
         public <T> void endVisit(EditorContext<T> ctx) {
             LeafValueEditor<T> asLeafValueEditor = ctx.asLeafValueEditor();
             if (asLeafValueEditor == null) {
                 return;
             }
             if (asLeafValueEditor instanceof HasValueChangeHandlers<?>) {
                 @SuppressWarnings("unchecked")
                 HasValueChangeHandlers<T> hasHandlers = (HasValueChangeHandlers<T>)asLeafValueEditor;
                 hasHandlers.addValueChangeHandler(new ArgumentPropertyBindingHandler<T>(propEditor));
             }
         }
     }
 
     private final class DefaultValueUpdater implements ValueChangeHandler<Splittable> {
         private final ArgumentPropertyEditor argPropEditor;
         private final LeafValueEditor<Splittable> defaultValueEditor;
         private final Argument model1;
 
         public DefaultValueUpdater(LeafValueEditor<Splittable> defaultValueEditor, Argument model, ArgumentPropertyEditor argPropEditor) {
             this.defaultValueEditor = defaultValueEditor;
             this.model1 = model;
             this.argPropEditor = argPropEditor;
         }
 
         @Override
         public void onValueChange(ValueChangeEvent<Splittable> event) {
             defaultValueEditor.setValue(event.getValue());
             model1.setDefaultValue(event.getValue());
             argPropEditor.asWidget().fireEvent(new UpdateCommandLinePreviewEvent());
         }
     }
 
     protected final AppTemplateWizardAppearance appearance;
 
     @Ignore
     protected ArgumentEditor argumentEditor;
 
     protected Argument model;
 
     final PrefixedHasTextEditor labelEditor;
 
     private final ContentPanel contentPanel;
 
     private HandlerRegistration dataSourceStoreAddHandlerReg;
 
     private HandlerRegistration defaultValueUpdaterReg;
 
     private HandlerRegistration fileInfoTypeStoreAddHandlerReg;
 
     private boolean labelOnlyEditMode = false;
 
     private final DataSourceProperties props;
 
     private final FileInfoTypeProperties props2;
 
     private final ReferenceGenomeProperties referenceGenomeProperties;
 
    private QuickTip quickTip = null;

     public AbstractArgumentPropertyEditor(AppTemplateWizardAppearance appearance) {
         this.appearance = appearance;
         contentPanel = new ContentPanel(new AppTemplateWizardPropertyContentPanelAppearance());
         labelEditor = new PrefixedHasTextEditor(contentPanel.getHeader(), appearance);
         props = GWT.create(DataSourceProperties.class);
         props2 = GWT.create(FileInfoTypeProperties.class);
         referenceGenomeProperties = GWT.create(ReferenceGenomeProperties.class);
     }
 
     @Override
     public HandlerRegistration addUpdateCommandLinePreviewEventHandler(UpdateCommandLinePreviewEventHandler handler) {
         return addHandler(handler, UpdateCommandLinePreviewEvent.TYPE);
     }
 
     @Override
     public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Splittable> handler) {
         return addHandler(handler, ValueChangeEvent.getType());
     }
 
     @Override
     public void clean() {
         if (model != null) {
             getEditorDriver().accept(new FinishEditing());
         }
 
     }
 
     @Override
     @UiFactory
     public ContentPanel createContentPanel() {
         return contentPanel;
     }
 
     @Override
     public void edit(Argument argument) {
         clean();
         this.model = argument;
        if (quickTip == null) {
            quickTip = new QuickTip(getWidget());
        }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public AbstractEditorDelegate<Argument, ? extends Editor<Argument>> getBoundEditorDelegate() {
         return (argumentEditor != null) ? (AbstractEditorDelegate<Argument, ? extends Editor<Argument>>)argumentEditor.getEditorDelegate() : null;
     }
 
     @Override
     public boolean isLabelOnlyEditMode() {
         return labelOnlyEditMode;
     }
 
     @Override
     public void setBoundArgumentEditor(ArgumentEditor argumentEditor) {
         this.argumentEditor = argumentEditor;
         IArgumentEditorConverter valueEditor = argumentEditor.valueEditor();
         if ((valueEditor != null) && (getDefaultValueEditor() != null)) {
             if (defaultValueUpdaterReg != null) {
                 defaultValueUpdaterReg.removeHandler();
             }
             defaultValueUpdaterReg = valueEditor.addValueChangeHandler(new DefaultValueUpdater(getDefaultValueEditor(), model, this));
         }
     }
 
     @Override
     public void setLabelOnlyEditMode(boolean labelOnlyEditMode) {
         this.labelOnlyEditMode = labelOnlyEditMode;
         initLabelOnlyEditMode(labelOnlyEditMode);
     }
 
     protected ComboBox<DataSource> createDataSourceComboBox(AppMetadataServiceFacade appMetadataService) {
         final ListStore<DataSource> store = new ListStore<DataSource>(props.id());
         dataSourceStoreAddHandlerReg = store.addStoreAddHandler(new StoreAddHandler<DataSource>() {
             @Override
             public void onAdd(StoreAddEvent<DataSource> event) {
                 updateDataSourceSelection(model);
             }
         });
         appMetadataService.getDataSources(new AsyncCallback<List<DataSource>>() {
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
     
             @Override
             public void onSuccess(List<DataSource> result) {
                 if (store.getAll().isEmpty()) {
                     store.addAll(result);
                     store.addSortInfo(new StoreSortInfo<DataSource>(props.type(), SortDir.ASC));
                 }
             }
         });
         ComboBox<DataSource> comboBox = new ComboBox<DataSource>(store, new LabelProvider<DataSource>() {
     
             @Override
             public String getLabel(DataSource src) {
                 return src.getType().getLabel();
             }
         });
         comboBox.setTriggerAction(TriggerAction.ALL);
         comboBox.addValueChangeHandler(new ValueChangeHandler<DataSource>() {
             @Override
             public void onValueChange(ValueChangeEvent<DataSource> event) {
                 model.getDataObject().setDataSource(event.getValue().getType());
             }
         });
         return comboBox;
     }
 
     protected ComboBox<FileInfoType> createFileInfoTypeComboBox(AppMetadataServiceFacade appMetadataService) {
         final ListStore<FileInfoType> store = new ListStore<FileInfoType>(props2.id());
         fileInfoTypeStoreAddHandlerReg = store.addStoreAddHandler(new StoreAddHandler<FileInfoType>() {
             @Override
             public void onAdd(StoreAddEvent<FileInfoType> event) {
                 updateFileInfoTypeSelection(model);
             }
         });
         appMetadataService.getFileInfoTypes(new AsyncCallback<List<FileInfoType>>() {
     
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
     
             @Override
             public void onSuccess(List<FileInfoType> result) {
                 if (store.getAll().isEmpty()) {
                     store.addAll(result);
                     store.addSortInfo(new StoreSortInfo<FileInfoType>(props2.labelValue(), SortDir.ASC));
                 }
             }
         });
         ComboBox<FileInfoType> comboBox = new ComboBox<FileInfoType>(store, props2.label());
         comboBox.setTriggerAction(TriggerAction.ALL);
         // JDS Add valueChangeHandler manually since there are errors if you try to do it with UIBinder
         comboBox.addValueChangeHandler(new ValueChangeHandler<FileInfoType>() {
             @Override
             public void onValueChange(ValueChangeEvent<FileInfoType> event) {
                 model.getDataObject().setFileInfoType(event.getValue().getType());
             }
         });
         return comboBox;
     }
 
     protected ComboBox<ReferenceGenome> createReferenceGenomeStore(AppMetadataServiceFacade appMetadataService) {
         final ListStore<ReferenceGenome> refGenomeListStore = new ListStore<ReferenceGenome>(referenceGenomeProperties.id());
 
         appMetadataService.getReferenceGenomes(new AsyncCallback<List<ReferenceGenome>>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
 
             @Override
             public void onSuccess(List<ReferenceGenome> result) {
                 if (refGenomeListStore.getAll().isEmpty()) {
                     refGenomeListStore.addAll(result);
                     refGenomeListStore.addSortInfo(new Store.StoreSortInfo<ReferenceGenome>(referenceGenomeProperties.nameValue(), SortDir.ASC));
                 }
             }
         });
 
         ComboBox<ReferenceGenome> cb = new ComboBox<ReferenceGenome>(refGenomeListStore, referenceGenomeProperties.name());
         cb.setTriggerAction(ALL);
 
         return cb;
     }
 
     @Ignore
     protected ComboBox<DataSource> getDataSourceComboBox() {
         return null;
     }
 
     @Ignore
     protected LeafValueEditor<Splittable> getDefaultValueEditor() {
         return null;
     }
 
     @Ignore
     protected ComboBox<FileInfoType> getFileInfoTypeComboBox() {
         return null;
     }
 
     protected abstract void initLabelOnlyEditMode(boolean isLabelOnlyEditMode);
 
     @UiHandler("label")
     void onLabelChanged(ValueChangeEvent<String> event) {
         labelEditor.setValue(event.getValue());
     }
 
     private void updateDataSourceSelection(Argument model) {
         if ((model != null) && (model.getDataObject() != null) && (model.getDataObject().getDataSource() != null)) {
             List<DataSource> dataSourceList = getDataSourceComboBox().getStore().getAll();
             if ((dataSourceStoreAddHandlerReg != null) && !dataSourceList.isEmpty()) {
                 dataSourceStoreAddHandlerReg.removeHandler();
                 dataSourceStoreAddHandlerReg = null;
             }
             for (DataSource ds : dataSourceList) {
                 if (ds.getType().equals(model.getDataObject().getDataSource())) {
                     getDataSourceComboBox().setValue(ds);
                     break;
                 }
             }
         }
     
     }
 
     private void updateFileInfoTypeSelection(Argument model) {
         if ((model != null) && (model.getDataObject() != null) && (model.getDataObject().getFileInfoType() != null)) {
     
             List<FileInfoType> fileInfoTypeList = getFileInfoTypeComboBox().getStore().getAll();
             if ((fileInfoTypeStoreAddHandlerReg != null) && !fileInfoTypeList.isEmpty()) {
                 fileInfoTypeStoreAddHandlerReg.removeHandler();
                 fileInfoTypeStoreAddHandlerReg = null;
             }
             for (final FileInfoType fit : fileInfoTypeList) {
                 if (fit.getType().equals(model.getDataObject().getFileInfoType())) {
                     getFileInfoTypeComboBox().setValue(fit);
                     break;
                 }
             }
         }
     
     }
 }
