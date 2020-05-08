 package org.iplantc.core.uidiskresource.client.views.widgets;
 
 import java.util.List;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.resources.client.messages.IplantErrorStrings;
 import org.iplantc.core.uicommons.client.errorHandling.models.ServiceErrorCode;
 import org.iplantc.core.uicommons.client.errorHandling.models.SimpleServiceError;
 import org.iplantc.core.uicommons.client.gin.ServicesInjector;
 import org.iplantc.core.uicommons.client.models.CommonModelUtils;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.HasPaths;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceStatMap;
 import org.iplantc.core.uicommons.client.services.DiskResourceServiceFacade;
 
 import com.google.common.collect.Lists;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.editor.client.EditorDelegate;
 import com.google.gwt.editor.client.EditorError;
 import com.google.gwt.editor.client.HasEditorErrors;
 import com.google.gwt.editor.client.ValueAwareEditor;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.core.client.dom.XDOM;
 import com.sencha.gxt.core.client.dom.XElement;
 import com.sencha.gxt.widget.core.client.Component;
 import com.sencha.gxt.widget.core.client.ComponentHelper;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 import com.sencha.gxt.widget.core.client.form.IsField;
 import com.sencha.gxt.widget.core.client.form.TextField;
 import com.sencha.gxt.widget.core.client.form.Validator;
 import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;
 
 /**
  * Abstract class for single select DiskResource fields.
  * 
  * TODO JDS All Diskresource selectors (incl multi) need to have a "file_info_type". This will be passed
  * to the DiskResource presenter, which will filter outputs.
  * 
  * @author jstroot
  * 
  */
 public abstract class AbstractDiskResourceSelector<R extends DiskResource> extends Component implements
  IsField<HasId>, ValueAwareEditor<HasId>, HasValueChangeHandlers<HasId>, HasEditorErrors<HasId> {
 
     interface FileFolderSelectorStyle extends CssResource {
         String buttonWrap();
 
         String wrap();
 
         String errorText();
        
        String inputWrap();
     }
 
     interface Resources extends ClientBundle {
         @Source("AbstractDiskResourceSelector.css")
         FileFolderSelectorStyle style();
     }
 
     public interface FileUploadTemplate extends XTemplates {
         @XTemplate("<div class='{style.wrap}'></div>")
         SafeHtml render(FileFolderSelectorStyle style);
     }
 
     private final TextButton button;
     private final TextField input = new TextField();
 
     private final Resources res = GWT.create(Resources.class);
     private final FileUploadTemplate template = GWT.create(FileUploadTemplate.class);
     private final int buttonOffset = 3;
     private EditorDelegate<HasId> editorDelegate;
     private List<EditorError> errors = Lists.newArrayList();
     private HasId selectedResource;
     private final Element infoText;
     private boolean browseButtonEnabled = true;
 
     // by default do not validate permissions
     private boolean validatePermissions = false;
     private final DiskResourceServiceFacade drServiceFacade;
     private HasId model;
 
     private DefaultEditorError permissionEditorError = null;
     private DefaultEditorError existsEditorError = null;
 
     protected AbstractDiskResourceSelector() {
         res.style().ensureInjected();
 
         SafeHtmlBuilder builder = new SafeHtmlBuilder();
         builder.append(template.render(res.style()));
         setElement(XDOM.create(builder.toSafeHtml()));
 
        
         input.setReadOnly(true);
        input.setStyleName(res.style().inputWrap());
         getElement().appendChild(input.getElement());
 
         sinkEvents(Event.ONCHANGE | Event.ONCLICK | Event.MOUSEEVENTS);
 
         button = new TextButton(org.iplantc.core.resources.client.messages.I18N.DISPLAY.browse());
         button.getElement().addClassName(res.style().buttonWrap());
         getElement().appendChild(button.getElement());
         button.addSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 if (!browseButtonEnabled) {
                     return;
                 }
                 onBrowseSelected();
             }
         });
         
         drServiceFacade = ServicesInjector.INSTANCE.getDiskResourceServiceFacade();
 
         infoText = DOM.createDiv();
         infoText.getStyle().setDisplay(Display.NONE);
         getElement().appendChild(infoText);
     }
 
     @Override
     public HandlerRegistration addValueChangeHandler(ValueChangeHandler<HasId> handler) {
         return addHandler(handler, ValueChangeEvent.getType());
     }
 
     protected void setSelectedResource(R selectedResource) {
         this.selectedResource = selectedResource;
         setValue(selectedResource);
     }
 
     @Override
     public void setValue(HasId value) {
         if ((value == model)) {
             // JDS If model is not changing
             return;
         } else if ((model != null) && (value != null) && model.getId().equals(value.getId())) {
             return;
         }
         model = value;
         input.setValue(value == null ? null : value.getId());
 
         doGetStat(value);
     }
 
     private void doGetStat(final HasId value) {
         final String diskResourceId = value.getId();
         HasPaths diskResourcePaths = drServiceFacade.getDiskResourceFactory().pathsList().as();
         diskResourcePaths.setPaths(Lists.newArrayList(diskResourceId));
 
         permissionEditorError = null;
         existsEditorError = null;
         drServiceFacade.getStat(diskResourcePaths, new AsyncCallback<DiskResourceStatMap>() {
 
             @Override
             public void onSuccess(DiskResourceStatMap result) {
                 if (!validatePermissions) {
                     setInfoErrorText("");
                     return;
                 }
                 DiskResource diskResource = result.get(diskResourceId);
                 if (diskResource == null) {
                     permissionEditorError = new DefaultEditorError(input, I18N.DISPLAY.permissionSelectErrorMessage(), diskResourceId);
                     setInfoErrorText(I18N.DISPLAY.permissionSelectErrorMessage());
                 } else if (!(diskResource.getPermissions().isWritable() || diskResource.getPermissions().isOwner())) {
                     permissionEditorError = new DefaultEditorError(input, I18N.DISPLAY.permissionSelectErrorMessage(), diskResourceId);
                     setInfoErrorText(I18N.DISPLAY.permissionSelectErrorMessage());
                 } else {
                     setInfoErrorText("");
                 }
                 ValueChangeEvent.fire(AbstractDiskResourceSelector.this, value);
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 final IplantErrorStrings errorStrings = I18N.ERROR;
                 SimpleServiceError serviceError = AutoBeanCodex.decode(drServiceFacade.getDiskResourceFactory(), SimpleServiceError.class, caught.getMessage()).as();
                 if (serviceError.getErrorCode().equals(ServiceErrorCode.ERR_DOES_NOT_EXIST.toString())) {
                     existsEditorError = new DefaultEditorError(input, errorStrings.diskResourceDoesNotExist(diskResourceId), diskResourceId);
                     setInfoErrorText(errorStrings.diskResourceDoesNotExist(diskResourceId));
                     ValueChangeEvent.fire(AbstractDiskResourceSelector.this, value);
                 }
             }
         });
 
     }
    

 
     public void setInfoTextClassName(String className) {
         infoText.setClassName(className);
     }
 
     /**
      * 
      * @param text the text to be shown in the info text element. Passing in null will hide the element.
      */
     public void setInfoText(String text) {
         if (text == null) {
             infoText.getStyle().setDisplay(Display.NONE);
             infoText.setInnerHTML(""); //$NON-NLS-1$
             return;
         }
         // Enable the div
         infoText.getStyle().setWidth(100, Unit.PCT);
         infoText.getStyle().setDisplay(Display.BLOCK);
         // JDS Escape the text as a precaution.
         SafeHtml safeText = SafeHtmlUtils.fromString(text);
         infoText.setInnerSafeHtml(safeText);
     }
 
     public void setInfoErrorText(String errorMessage) {
         setInfoTextClassName(res.style().errorText());
         setInfoText(errorMessage);
     }
 
     /**
      * Convenience method which creates a HasId object from a given string id.
      * 
      * @param id
      */
     public void setValueFromStringId(String id) {
         setValue(CommonModelUtils.createHasIdFromString(id));
     }
 
     @Override
     public HasId getValue() {
         return CommonModelUtils.createHasIdFromString(input.getCurrentValue());
     }
 
     protected abstract void onBrowseSelected();
 
     @Override
     protected void doAttachChildren() {
         super.doAttachChildren();
         ComponentHelper.doAttach(input);
         ComponentHelper.doAttach(button);
     }
 
     @Override
     protected void doDetachChildren() {
         super.doDetachChildren();
         ComponentHelper.doDetach(input);
         ComponentHelper.doDetach(button);
     }
 
     @Override
     protected XElement getFocusEl() {
         return input.getElement();
     }
 
     @Override
     protected void onResize(int width, int height) {
         super.onResize(width, height);
         input.setWidth(width - button.getOffsetWidth() - buttonOffset);
     }
 
     @Override
     public void clear() {
         input.clear();
     }
 
     @Override
     public void clearInvalid() {
         input.clearInvalid();
         errors.clear();
     }
 
     @Override
     public void reset() {
         input.reset();
     }
 
     public void addValidator(Validator<String> validator) {
         if (validator != null) {
             input.addValidator(validator);
         }
     }
 
     @Override
     public boolean isValid(boolean preventMark) {
         // If the input field is not valid, make a call to validate in order to
         // propagate errors from the input field to the editor delegate.
         if (!input.isValid(preventMark)) {
             validate(preventMark);
         }
         return input.isValid(preventMark);
     }
 
     @Override
     public boolean validate(boolean preventMark) {
         errors = Lists.newArrayList();
         for (Validator<String> v : input.getValidators()) {
             List<EditorError> errs = v.validate(input, input.getCurrentValue());
             if (errs != null) {
                 errors.addAll(errs);
             }
         }
         if (permissionEditorError != null) {
             errors.add(permissionEditorError);
         } else if (existsEditorError != null) {
             errors.add(existsEditorError);
         }
         if(!preventMark) {
             input.showErrors(errors);
         }
         if (errors.size() < 0) {
             input.clearInvalid();
         }
         return errors.size() > 0;
     }
 
     @Override
     public void showErrors(List<EditorError> errors) {
         input.showErrors(errors);
     }
 
     @Override
     public void flush() {
         // Validate on flush.
         validate(false);
         input.flush();
         if (editorDelegate == null) {
             return;
         }
     }
 
     @Override
     public void onPropertyChange(String... paths) {
     }
 
     @Override
     public void setDelegate(EditorDelegate<HasId> delegate) {
         editorDelegate = delegate;
     }
 
     public void disableBrowseButton() {
         browseButtonEnabled = false;
     }
 
     protected boolean isBrowseButtonEnabled() {
         return browseButtonEnabled;
     }
 
     public List<Validator<String>> getValidators() {
         return input.getValidators();
     }
 
     /**
      * @return the validatePermissions
      */
     public boolean isValidatePermissions() {
         return validatePermissions;
     }
 
     /**
      * @param validatePermissions the validatePermissions to set
      */
     public void setValidatePermissions(boolean validatePermissions) {
         this.validatePermissions = validatePermissions;
     }
 
     public void setRequired(boolean required) {
         input.setAllowBlank(!required);
     }
 
     public List<EditorError> getErrors() {
         return errors;
     }
 
 }
