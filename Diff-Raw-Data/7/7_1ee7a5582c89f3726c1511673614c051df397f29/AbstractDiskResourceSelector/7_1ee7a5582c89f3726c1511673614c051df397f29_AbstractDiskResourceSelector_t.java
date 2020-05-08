 package org.iplantc.core.uidiskresource.client.views.widgets;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.resources.client.messages.IplantErrorStrings;
 import org.iplantc.core.uicommons.client.errorHandling.models.ServiceErrorCode;
 import org.iplantc.core.uicommons.client.errorHandling.models.SimpleServiceError;
 import org.iplantc.core.uicommons.client.gin.ServicesInjector;
 import org.iplantc.core.uicommons.client.models.HasPaths;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceStatMap;
 import org.iplantc.core.uicommons.client.services.DiskResourceServiceFacade;
 import org.iplantc.core.uicommons.client.widgets.IPlantSideErrorHandler;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.core.client.dom.XDOM;
 import com.sencha.gxt.core.client.dom.XElement;
 import com.sencha.gxt.dnd.core.client.DND.Operation;
 import com.sencha.gxt.dnd.core.client.DndDragEnterEvent;
 import com.sencha.gxt.dnd.core.client.DndDragEnterEvent.DndDragEnterHandler;
 import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
 import com.sencha.gxt.dnd.core.client.DndDragMoveEvent.DndDragMoveHandler;
 import com.sencha.gxt.dnd.core.client.DndDropEvent.DndDropHandler;
 import com.sencha.gxt.dnd.core.client.DropTarget;
 import com.sencha.gxt.dnd.core.client.StatusProxy;
 import com.sencha.gxt.widget.core.client.Component;
 import com.sencha.gxt.widget.core.client.ComponentHelper;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.event.InvalidEvent.HasInvalidHandlers;
 import com.sencha.gxt.widget.core.client.event.InvalidEvent.InvalidHandler;
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
         IsField<R>, ValueAwareEditor<R>, HasValueChangeHandlers<R>, HasEditorErrors<R>,
         DndDragEnterHandler, DndDragMoveHandler, DndDropHandler, HasInvalidHandlers, DiskResourceSelector, DiskResourceSelector.HasDisableBrowseButtons {
 
     public interface FileUploadTemplate extends XTemplates {
         @XTemplate("<div class='{style.wrap}'></div>")
         SafeHtml render(FileFolderSelectorStyle style);
     }
 
     interface FileFolderSelectorStyle extends CssResource {
         String buttonWrap();
 
         String errorText();
 
         String inputWrap();
         
         String wrap();
     }
 
     interface Resources extends ClientBundle {
         @Source("AbstractDiskResourceSelector.css")
         FileFolderSelectorStyle style();
     }
 
     /**
      * KLUDGE: CORE-4671,
      * 
      * @author jstroot
      * 
      */
     private final class DrSideErrorHandler extends IPlantSideErrorHandler {
         private final Widget button1;
         private final Widget container;
         private final Component input1;
 
         private DrSideErrorHandler(Component target, Widget container, Widget button) {
             super(target);
             this.input1 = target;
             this.container = container;
             this.button1 = button;
         }
 
         @Override
         public void clearInvalid() {
             super.clearInvalid();
             int offset = button1.getOffsetWidth() + buttonOffset;
             input1.setWidth(container.getOffsetWidth() - offset);
         }
 
         @Override
         public void markInvalid(List<EditorError> errors) {
             super.markInvalid(errors);
             Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 
                 @Override
                 public void execute() {
                     if (isShowing()) {
                         int offset = button1.getOffsetWidth() + buttonOffset + 16;
                         input1.setWidth(container.getOffsetWidth() - offset);
                     }
                 }
             });
         }
     }
 
     private boolean browseButtonEnabled = true;
     private final TextButton button;
 
     private final int buttonOffset = 3;
     private final DiskResourceServiceFacade drServiceFacade;
     private IPlantSideErrorHandler errorHandler;
     private final List<EditorError> errors = Lists.newArrayList();
     private DefaultEditorError existsEditorError = null;
     private final Element infoText;
 
     private String infoTextString;
     private final TextField input = new TextField();
     private R model;
 
     private DefaultEditorError permissionEditorError = null;
     private final Resources res = GWT.create(Resources.class);
     private final FileUploadTemplate template = GWT.create(FileUploadTemplate.class);
     // by default do not validate permissions
     private boolean validatePermissions = false;
 
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
 
         initDragAndDrop();
         
         errorHandler = new DrSideErrorHandler(input, this, button);
         errorHandler.setAdjustTargetWidth(false);
         input.setErrorSupport(errorHandler);
     }
 
     
     @Override
     public HandlerRegistration addInvalidHandler(InvalidHandler handler) {
         return input.addInvalidHandler(handler);
     }
     
     
     public void addValidator(Validator<String> validator) {
         if (validator != null) {
             input.addValidator(validator);
         }
     }
 
     @Override
     public HandlerRegistration addValueChangeHandler(ValueChangeHandler<R> handler) {
         return addHandler(handler, ValueChangeEvent.getType());
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
     public void disableBrowseButtons() {
         browseButtonEnabled = false;
     }
     
 
     @Override
     public void flush() {
         validate(false);
     }
 
     @Override
     public List<EditorError> getErrors() {
         return errors;
     }
 
     public String getInfoText() {
         return infoTextString;
     }
 
     public List<Validator<String>> getValidators() {
         return input.getValidators();
     }
 
     @Override
     public R getValue() {
         return model;
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
 
     public boolean isValidatePermissions() {
         return validatePermissions;
     }
 
     @Override
     public void onDragEnter(DndDragEnterEvent event) {
         Set<DiskResource> dropData = getDropData(event.getDragSource().getData());
 
         if (!validateDropStatus(dropData, event.getStatusProxy())) {
             event.setCancelled(true);
         }
     }
 
     @Override
     public void onDragMove(DndDragMoveEvent event) {
         Set<DiskResource> dropData = getDropData(event.getDragSource().getData());
 
         if (!validateDropStatus(dropData, event.getStatusProxy())) {
             event.setCancelled(true);
         }
     }
 
     @Override
     public void onPropertyChange(String... paths) {/* Do Nothing */}
 
     @Override
     public void reset() {
         input.reset();
     }
 
     /**
      * set id to browse button
      * 
      * @param id id to be set for browse button
      */
     public void setBrowseButtonId(String id) {
         button.setId(id);
     }
 
     @Override
     public void setDelegate(EditorDelegate<R> delegate) {/* Do Nothing */}
 
     public void setEmptyText(String emptyText) {
         input.setEmptyText(emptyText);
     }
 
     public void setInfoErrorText(String errorMessage) {
         setInfoTextClassName(res.style().errorText());
         setInfoText(errorMessage);
     }
 
     /**
      * 
      * @param text the text to be shown in the info text element. Passing in null will hide the element.
      */
     public void setInfoText(String text) {
         this.infoTextString = text;
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
 
     public void setInfoTextClassName(String className) {
         infoText.setClassName(className);
     }
 
     @Override
     public void setRequired(boolean required) {
         input.setAllowBlank(!required);
     }
 
     /**
      * @param validatePermissions the validatePermissions to set
      */
     public void setValidatePermissions(boolean validatePermissions) {
         this.validatePermissions = validatePermissions;
     }
 
     @Override
     public void setValue(R value) {
         if ((value == model)) {
             // JDS If model is not changing
             return;
         } else if ((model != null) && (value != null) && model.getPath().equals(value.getPath())) {
             return;
         }
         model = value;
         input.setValue(value == null ? null : value.getPath());
         validate(false);
 
         doGetStat(value);
     }
 
     /**
      * Convenience method which creates a HasId object from a given string id.
      * 
      * @param path
      */
     public abstract void setValueFromStringId(String path);
 
     @Override
     public void showErrors(List<EditorError> errors) {/* Do Nothing */}
 
     @Override
     public boolean validate(boolean preventMark) {
         errors.clear();
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
        return !(errors.size() > 0);
     }
 
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
 
     @SuppressWarnings("unchecked")
     protected Set<DiskResource> getDropData(Object data) {
         if (!(data instanceof Collection<?>)) {
             return null;
         }
         Collection<?> dataColl = (Collection<?>)data;
         if (dataColl.isEmpty() || !(dataColl.iterator().next() instanceof DiskResource)) {
             return null;
         }
 
         Set<DiskResource> dropData = null;
         dropData = Sets.newHashSet((Collection<DiskResource>)dataColl);
 
         return dropData;
     }
 
     @Override
     protected XElement getFocusEl() {
         return input.getElement();
     }
 
     protected boolean isBrowseButtonEnabled() {
         return browseButtonEnabled;
     }
 
     protected abstract void onBrowseSelected();
 
     @Override
     protected void onResize(int width, int height) {
         int offset = button.getOffsetWidth() + buttonOffset;
         if (errorHandler.isShowing()) {
             offset += 16;
         }
         super.onResize(width, height);
         input.setWidth(width - offset);
     }
 
     protected void setSelectedResource(R selectedResource) {
         setValue(selectedResource);
     }
 
     abstract protected boolean validateDropStatus(Set<DiskResource> dropData, StatusProxy status);
 
     private void doGetStat(final R value) {
         final String diskResourceId = value.getPath();
         HasPaths diskResourcePaths = drServiceFacade.getDiskResourceFactory().pathsList().as();
         diskResourcePaths.setPaths(Lists.newArrayList(diskResourceId));
 
         permissionEditorError = null;
         existsEditorError = null;
         drServiceFacade.getStat(diskResourcePaths, new AsyncCallback<DiskResourceStatMap>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 final IplantErrorStrings errorStrings = I18N.ERROR;
                 SimpleServiceError serviceError = AutoBeanCodex.decode(drServiceFacade.getDiskResourceFactory(), SimpleServiceError.class, caught.getMessage()).as();
                 if (serviceError.getErrorCode().equals(ServiceErrorCode.ERR_DOES_NOT_EXIST.toString())) {
                     existsEditorError = new DefaultEditorError(input, errorStrings.diskResourceDoesNotExist(diskResourceId), diskResourceId);
                     errors.add(existsEditorError);
                     setInfoErrorText(errorStrings.diskResourceDoesNotExist(diskResourceId));
                     ValueChangeEvent.fire(AbstractDiskResourceSelector.this, value);
                 }
             }
 
             @Override
             public void onSuccess(DiskResourceStatMap result) {
                 if (!validatePermissions) {
                     setInfoErrorText("");
                     return;
                 }
                 ValueChangeEvent.fire(AbstractDiskResourceSelector.this, value);
                 DiskResource diskResource = result.get(diskResourceId);
                 if (diskResource == null) {
                     permissionEditorError = new DefaultEditorError(input, I18N.DISPLAY.permissionSelectErrorMessage(), diskResourceId);
                     errors.add(permissionEditorError);
                     input.showErrors(Lists.<EditorError> newArrayList(permissionEditorError));
                     setInfoErrorText(I18N.DISPLAY.permissionSelectErrorMessage());
                 } else if (!(diskResource.getPermissions().isWritable() || diskResource.getPermissions().isOwner())) {
                     permissionEditorError = new DefaultEditorError(input, I18N.DISPLAY.permissionSelectErrorMessage(), diskResourceId);
                     errors.add(permissionEditorError);
                     input.showErrors(Lists.<EditorError> newArrayList(permissionEditorError));
                     setInfoErrorText(I18N.DISPLAY.permissionSelectErrorMessage());
                } else {
                    setInfoErrorText(null);
                 }
             }
         });
 
     }
 
     private void initDragAndDrop() {
         DropTarget dataDrop = new DropTarget(this);
         dataDrop.setOperation(Operation.COPY);
         dataDrop.addDragEnterHandler(this);
         dataDrop.addDragMoveHandler(this);
         dataDrop.addDropHandler(this);
     }
 }
