 package au.org.scoutmaster.views.calendar;
 
 import java.util.Date;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.joda.time.DateTime;
 import org.vaadin.dialogs.ConfirmDialog;
 
 import au.com.vaadinutils.crud.BaseCrudView;
 import au.com.vaadinutils.crud.CrudAction;
 import au.com.vaadinutils.crud.CrudEntity;
 import au.com.vaadinutils.crud.ValidatingFieldGroup;
 import au.com.vaadinutils.dao.EntityManagerProvider;
 import au.com.vaadinutils.domain.iColorFactory;
 import au.com.vaadinutils.fields.CKEditorEmailField;
 import au.org.scoutmaster.dao.ColorDao;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.dao.EventDao;
 import au.org.scoutmaster.domain.Event_;
 import au.org.scoutmaster.util.SMMultiColumnFormLayout;
 import au.org.scoutmaster.util.SMNotification;
 import au.org.scoutmaster.validator.DateRangeValidator;
 
 import com.vaadin.addon.jpacontainer.EntityItem;
 import com.vaadin.addon.jpacontainer.JPAContainer;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
 import com.vaadin.shared.ui.datefield.Resolution;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification.Type;
 import com.vaadin.ui.RichTextArea;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 public class EventDetails extends VerticalLayout implements com.vaadin.ui.Button.ClickListener, ValueChangeListener
 {
 	@SuppressWarnings("unused")
 	private static Logger logger = LogManager.getLogger(EventDetails.class);
 	private static final long serialVersionUID = 1L;
 
 	private ValidatingFieldGroup<au.org.scoutmaster.domain.Event> fieldGroup;
 	private DateField startDateField;
 	private DateField endDateField;
 	private JPAContainer<au.org.scoutmaster.domain.Event> container;
 	private EntityItem<au.org.scoutmaster.domain.Event> entityItem;
 	private Button save;
 	private Button cancel;
 	private au.org.scoutmaster.domain.Event event;
 	private SaveEventListener newEventListener;
 	private Button delete;
 	private boolean newEvent;
 	private boolean readonly;
 	private CKEditorEmailField detailsEditor;
 	private Label clickToViewLabel;
 	private RichTextArea message;
 
 	/**
 	 * 
 	 * @param readonly
 	 * @param newButtonListener
 	 * @param sender
 	 *            the user who is sending this email.
 	 */
 	public EventDetails(SaveEventListener newEventListener, boolean readonly)
 	{
 		this.readonly = readonly;
 		EventDao daoEvent = new DaoFactory().getEventDao();
 		this.container = daoEvent.createVaadinContainer();
 		this.fieldGroup = new ValidatingFieldGroup<au.org.scoutmaster.domain.Event>(
 				au.org.scoutmaster.domain.Event.class);
 		this.newEventListener = newEventListener;
 		buildForm();
 		this.newEvent = false;
 		fieldGroup.setEnabled(false);
 		if (!readonly)
 		{
 			delete.setEnabled(false);
 			cancel.setEnabled(false);
 			save.setEnabled(false);
 		}
 	}
 
 	void setEvent(au.org.scoutmaster.domain.Event event, boolean newEvent)
 	{
 		this.event = event;
 		this.newEvent = newEvent;
 
 		if (event != null)
 		{
 			clickToViewLabel.setVisible(false);
			// we are going to manipulate the event on a different thread.
			// EntityManagerProvider.detach(event);
 			entityItem = container.createEntityItem(event);
 
 			// startDateField.removeValueChangeListener(this);
 			fieldGroup.setItemDataSource(entityItem);
 			// startDateField.addValueChangeListener(this);
 
 			if (!readonly)
 			{
 				if (newEvent)
 					delete.setEnabled(false);
 				else
 					delete.setEnabled(true);
 				save.setEnabled(true);
 				cancel.setEnabled(true);
 				fieldGroup.setEnabled(true);
 				this.detailsEditor.setReadOnly(false);
 			}
 			else
 			{
 				fieldGroup.setEnabled(true);
 				fieldGroup.setReadOnly(true);
 			}
 
 		}
 		else
 		{
 			clickToViewLabel.setVisible(false);
 			fieldGroup.discard();
 			entityItem = container.createEntityItem(new au.org.scoutmaster.domain.Event());
 			fieldGroup.setItemDataSource(entityItem);
 			fieldGroup.setEnabled(false);
 			delete.setEnabled(false);
 			cancel.setEnabled(false);
 			save.setEnabled(false);
 		}
 	}
 
 	void buildForm()
 	{
 		VerticalLayout details = new VerticalLayout();
 		// this.setMargin(true);
 		clickToViewLabel = new Label("<b>Click an event in the Calendar to view its details.</b>");
 		clickToViewLabel.setContentMode(ContentMode.HTML);
 		clickToViewLabel.setWidth(null);
 		details.addComponent(clickToViewLabel);
 		details.setComponentAlignment(clickToViewLabel, Alignment.MIDDLE_RIGHT);
 		details.setSizeFull();
 
 		SMMultiColumnFormLayout<au.org.scoutmaster.domain.Event> overviewForm = new SMMultiColumnFormLayout<au.org.scoutmaster.domain.Event>(
 				3, this.fieldGroup);
 		overviewForm.setColumnLabelWidth(0, 100);
 		overviewForm.setColumnLabelWidth(1, 0);
 		overviewForm.setColumnLabelWidth(2, 60);
 		overviewForm.setColumnFieldWidth(0, 100);
 		overviewForm.setColumnFieldWidth(1, 100);
 		overviewForm.setColumnFieldWidth(2, 20);
 		overviewForm.setColumnExpandRatio(1, 1.0f);
 		overviewForm.setSizeFull();
 		// overviewForm.setWidth("500px");
 
 		overviewForm.colspan(3);
 		TextField subject = overviewForm.bindTextField("Subject", Event_.subject);
 		overviewForm.newLine();
 
 		if (!readonly)
 		{
 
 			iColorFactory factory = new ColorDao.ColorFactory();
 			overviewForm.bindColorPicker(factory, "Colour", Event_.color.getName());
 			overviewForm.newLine();
 
 			overviewForm.bindBooleanField("All Day Event", Event_.allDayEvent);
 			overviewForm.newLine();
 		}
 
 		overviewForm.colspan(3);
 		startDateField = overviewForm.bindDateField("Start", Event_.eventStartDateTime, "yyyy-MM-dd hh:mm a",
 				Resolution.MINUTE);
 		startDateField.setRangeStart(new Date());
 		startDateField.addValueChangeListener(this);
 		overviewForm.newLine();
 
 		overviewForm.colspan(3);
 		endDateField = overviewForm.bindDateField("End", Event_.eventEndDateTime, "yyyy-MM-dd hh:mm a",
 				Resolution.MINUTE);
 		endDateField.setRangeStart(new Date());
 
 		startDateField.addValidator(new DateRangeValidator(startDateField.getCaption(), endDateField));
 
 		overviewForm.newLine();
 		overviewForm.colspan(3);
 		overviewForm.bindTextField("Street", "location.street");
 		overviewForm.newLine();
 		overviewForm.colspan(3);
 		overviewForm.bindTextField("City", "location.city");
 
 		overviewForm.newLine();
 		overviewForm.bindTextField("State", "location.state");
 		overviewForm.newLine();
 		overviewForm.bindTextField("Postcode", "location.postcode");
 		overviewForm.newLine();
 
 		overviewForm.colspan(3);
 
 		if (!readonly)
 		{
 			detailsEditor = overviewForm.bindEditorField(Event_.details, true);
 			detailsEditor.setHeight("100%");
 		}
		//overviewForm.setExpandRatio(1.0f);
 		details.addComponent(overviewForm);
 		details.setExpandRatio(overviewForm, 1.0f);
 		details.setComponentAlignment(overviewForm, Alignment.TOP_RIGHT);
 		this.addComponent(details);
 		this.setComponentAlignment(details, Alignment.TOP_RIGHT);
 
 		
 		if (!readonly)
 		{
 			HorizontalLayout buttonLayout = new HorizontalLayout();
 			buttonLayout.setSpacing(true);
 			save = new Button("Save");
 			save.addClickListener(this);
 			cancel = new Button("Cancel");
 			cancel.addClickListener(this);
 
 			delete = new Button("Delete");
 			delete.addClickListener(this);
 
 			buttonLayout.addComponent(cancel);
 			buttonLayout.addComponent(save);
 			buttonLayout.setComponentAlignment(cancel, Alignment.MIDDLE_LEFT);
 			buttonLayout.setComponentAlignment(save, Alignment.MIDDLE_RIGHT);
 
 			HorizontalLayout buttonGroupLayout = new HorizontalLayout();
 			buttonGroupLayout.setMargin(true);
 			buttonGroupLayout.addComponent(delete);
 			buttonGroupLayout.addComponent(buttonLayout);
 			buttonGroupLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
 			buttonGroupLayout.setComponentAlignment(delete, Alignment.MIDDLE_LEFT);
 			buttonGroupLayout.setWidth("100%");
 			buttonGroupLayout.setExpandRatio(buttonLayout, 1.0f);
 
 			details.addComponent(buttonGroupLayout);
 		}
 		else
 		{
 			VerticalLayout messageLayout = new VerticalLayout();
 			messageLayout.setMargin(true);
 			messageLayout.setSizeFull();
 			message = new RichTextArea();
 			message.setNullRepresentation("");
 			message.setReadOnly(true);
 			//message.setContentMode(ContentMode.HTML);
 			message.setSizeFull();
 			this.fieldGroup.bind(message, Event_.details.getName());
 			
 			messageLayout.addComponent(message);
 			messageLayout.setComponentAlignment(message, Alignment.TOP_RIGHT);
 			this.addComponent(messageLayout);
 			this.setComponentAlignment(messageLayout, Alignment.TOP_RIGHT);
 			//this.setExpandRatio(messageLayout, 1.0f);
 
 		}
 
 		subject.focus();
 		this.setSizeFull();
 
 	}
 
 	@Override
 	public void buttonClick(ClickEvent event)
 	{
 		if (event.getButton() == save)
 		{
 			EventDao daoEvent = new DaoFactory().getEventDao();
 			try
 			{
 				this.fieldGroup.commit();
 				EntityManagerProvider.getEntityManager().detach(this.event);
 				this.event = daoEvent.merge(this.event);
 
 				// the merge returns a new event so we have to reattach it to
 				// the field group.
 				this.setEvent(this.event, newEvent);
 				newEventListener.eventSaved(this.event, newEvent);
 				newEvent = false;
 			}
 			catch (CommitException e)
 			{
 				SMNotification.show(e, Type.ERROR_MESSAGE);
 			}
 		}
 
 		else if (event.getButton() == cancel)
 		{
 			if (this.newEvent)
 			{
 				newEvent = false;
 				setEvent(null, false);
 			}
 			else
 				fieldGroup.discard();
 		}
 		else if (event.getButton() == delete)
 		{
 			if (EventDetails.this.event != null)
 			{
 
 				ConfirmDialog.show(UI.getCurrent(), "Confirm Delete", "Are you sure you want to delete "
 						+ EventDetails.this.event.getSubject() + "?", "Delete", "Cancel", new ConfirmDialog.Listener()
 				{
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void onClose(ConfirmDialog dialog)
 					{
 						if (dialog.isConfirmed())
 						{
 
 							EventDetails.this.delete();
 						}
 					}
 
 				});
 			}
 		}
 
 	}
 
 	protected void delete()
 	{
 		EventDao daoEvent = new DaoFactory().getEventDao();
 		// we can't delete an unmanaged entity so merge and then delete
 		// it.
 		this.event = daoEvent.merge(EventDetails.this.event);
 		daoEvent.remove(EventDetails.this.event);
 		EventDetails.this.newEventListener.eventDeleted(EventDetails.this.event);
 		setEvent(null, false);
 	}
 
 	public interface SaveEventListener
 	{
 		/**
 		 * Called when the user clicks save to notify the listener that a new
 		 * event was added or an existing event updated..
 		 * 
 		 * @param event
 		 */
 		void eventSaved(au.org.scoutmaster.domain.Event event, boolean newEvent);
 
 		/**
 		 * Called when the user clicks delete to notify the listener that the
 		 * event was delete.
 		 * 
 		 * @param event
 		 */
 
 		void eventDeleted(au.org.scoutmaster.domain.Event event);
 	}
 
 	@Override
 	public void valueChange(ValueChangeEvent event)
 	{
 		if (!this.readonly)
 		{
 			// The start date has just changed so make certain the end date is
 			// in
 			// the future
 			// by default we set it to two hours into the future.
 
 			DateField startDateField = (DateField) event.getProperty();
 
 			DateTime startDate = new DateTime(startDateField.getValue());
 
 			if (this.endDateField.getValue() != null && this.endDateField.getValue().before(startDateField.getValue()))
 				this.endDateField.setValue(startDate.plusHours(2).toDate());
 		}
 	}
 
 	public class CrudActionDelete<E extends CrudEntity> implements CrudAction<E>
 	{
 		private static final long serialVersionUID = 1L;
 		private boolean isDefault = true;
 
 		@Override
 		public void exec(final BaseCrudView<E> crud, EntityItem<E> entity)
 		{
 			ConfirmDialog.show(UI.getCurrent(), "Confirm Delete", "Are you sure you want to delete "
 					+ entity.getEntity().getName() + "?", "Delete", "Cancel", new ConfirmDialog.Listener()
 			{
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void onClose(ConfirmDialog dialog)
 				{
 					if (dialog.isConfirmed())
 					{
 
 						crud.delete();
 					}
 				}
 
 			});
 
 		}
 
 		public String toString()
 		{
 			return "Delete";
 		}
 
 		public boolean isDefault()
 		{
 			return isDefault;
 		}
 
 		public void setIsDefault(boolean isDefault)
 		{
 			this.isDefault = isDefault;
 		}
 	}
 
 }
