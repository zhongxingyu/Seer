 package gwtdb.client;
 
 import gwtdb.client.SamplePresenter.Display;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.dom.client.HasKeyUpHandlers;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent.Handler;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 public class SampleView extends Composite implements Display {
 	public enum View {
 		CREATE, EDIT, EMPTY
 	}
 
 	private static SampleUiBinder uiBinder = GWT.create(SampleUiBinder.class);
 
 	interface SampleUiBinder extends UiBinder<Widget, SampleView> {
 	}
 	
	private ClientEntity entity;
 
 	public SampleView() {
 		initWidget(uiBinder.createAndBindUi(this));
 
 		log.setText("Log");
 		configureTable();
 		switchView(View.EMPTY);
 	}
 
 	private void configureButtons(View view) {
 		newBtn.setVisible(view != View.EDIT && view != View.CREATE);
 		saveBtn.setVisible(view == View.EDIT || view == View.CREATE);
 
 		cancelBtn.setVisible(view == View.EDIT);
 		deleteBtn.setVisible(view == View.EDIT);
 	}
 
 	private void configureForm(final View empty) {
 		form.clear(true);
 
 		switch (empty) {
 		case EMPTY:
			entity = null; // wipe out previously stored entity
 			form.setText(0, 0, "Name");
 			form.setText(1, 0, "E-Mail");
 			break;
 		case CREATE:
			entity = null; // wipe out previously stored entity
 			name.setText("");
 			mail.setText("");
 			form.setText(0, 0, "Name");
 			form.setText(1, 0, "E-Mail");
 			form.setWidget(0, 1, name);
 			form.setWidget(1, 1, mail);
 			break;
 		case EDIT:
 			name.setText(String.valueOf(entity.get("Name")));
 			mail.setText(String.valueOf(entity.get("Mail")));
 			form.setText(0, 0, "Name");
 			form.setText(1, 0, "E-Mail");
 			form.setWidget(0, 1, name);
 			form.setWidget(1, 1, mail);
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void switchView(final View view) {
 		configureForm(view);
 		configureButtons(view);
 	}
 
 	@Override
 	public ClientEntity getEntity() {
 		// assume entity is not null
 		entity.setProperty("Name", name.getText());
 		entity.setProperty("Mail", mail.getText());
 		return entity;
 	}
 
 	private void configureTable() {
 		final TextColumn<ClientEntity> id = new TextColumn<ClientEntity>() {
 			@Override
 			public String getValue(ClientEntity object) {
 				return String.valueOf(object.getId());
 			}
 		};
 		final TextColumn<ClientEntity> name = new TextColumn<ClientEntity>() {
 			@Override
 			public String getValue(ClientEntity object) {
 				return String.valueOf(object.get("Name"));
 			}
 		};
 
 		id.setSortable(true);
 		name.setSortable(true);
 
 		table.addColumn(id, "ID");
 		table.addColumn(name, "Name");
 		table.setColumnWidth(id, 50, Unit.PX);
 		table.setColumnWidth(name, 200, Unit.PX);
 
 		final SingleSelectionModel<ClientEntity> model = new SingleSelectionModel<ClientEntity>();
 		model.addSelectionChangeHandler(new Handler() {
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				entity = model.getSelectedObject();
 				switchView(View.EDIT);
 			}
 		});
 		table.setSelectionModel(model);
 		
 		tableDataProvider.addDataDisplay(table);
 		pager.setDisplay(table);
 	}
 
 	// for form
 	TextBox name = new TextBox();
 	TextBox mail = new TextBox();
 
 	@UiField
 	SimplePager pager;
 	@UiField
 	HTML log;
 	@UiField
 	Button newBtn;
 	@UiField
 	Button cancelBtn;
 	@UiField
 	Button saveBtn;
 	@UiField
 	Button deleteBtn;
 	@UiField
 	FlexTable form;
 	@UiField
 	CellTable<ClientEntity> table;
 	private ListDataProvider<ClientEntity> tableDataProvider = new ListDataProvider<ClientEntity>();
 
 	@UiFactory SimplePager makePager() {
 		return new SimplePager(TextLocation.CENTER);
 	}
 	
 	@Override
 	public void append(String message) {
 		log.setHTML(log.getHTML() + "<br />" + message);
 	}
 
 	@Override
 	public HasClickHandlers getNewButton() {
 		return newBtn;
 	}
 
 	@Override
 	public HasClickHandlers getCancelBtn() {
 		return cancelBtn;
 	}
 
 	@Override
 	public HasClickHandlers getSaveBtn() {
 		return saveBtn;
 	}
 
 	@Override
 	public HasClickHandlers getDeleteBtn() {
 		return deleteBtn;
 	}
 
 	@Override
 	public void insert(final ClientEntity entity) {
 		tableDataProvider.getList().add(entity);
 	}
 
 	@Override
 	public void insert(ClientEntity[] clientEntities) {
 		tableDataProvider.getList().clear();
 		for (final ClientEntity e : clientEntities) {
 			tableDataProvider.getList().add(e);
 		}
 	}
 
 	@Override
 	public HasKeyUpHandlers getNameBox() {
 		return name;
 	}
 
 	@Override
 	public HasKeyUpHandlers getEmailBox() {
 		return mail;
 	}
 }
