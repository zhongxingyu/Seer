 package au.org.scoutmaster.views;
 
 import java.util.List;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import au.com.vaadinutils.crud.BaseCrudView;
 import au.com.vaadinutils.crud.CrudAction;
 import au.com.vaadinutils.crud.FormHelper;
 import au.com.vaadinutils.crud.HeadingPropertySet;
 import au.com.vaadinutils.crud.HeadingPropertySet.Builder;
 import au.com.vaadinutils.crud.ValidatingFieldGroup;
 import au.com.vaadinutils.menu.Menu;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.domain.Contact;
 import au.org.scoutmaster.domain.Contact_;
 import au.org.scoutmaster.domain.Raffle;
 import au.org.scoutmaster.domain.Raffle_;
 import au.org.scoutmaster.util.SMMultiColumnFormLayout;
 import au.org.scoutmaster.views.actions.RaffleActionAllocateBooks;
 import au.org.scoutmaster.views.actions.RaffleActionImportBooks;
 
 import com.vaadin.addon.jpacontainer.JPAContainer;
 import com.vaadin.data.Container.Filter;
 import com.vaadin.data.util.filter.Or;
 import com.vaadin.data.util.filter.SimpleStringFilter;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.shared.ui.combobox.FilteringMode;
 import com.vaadin.shared.ui.datefield.Resolution;
 import com.vaadin.ui.AbstractLayout;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.VerticalLayout;
 
@Menu(display = "Raffle", path = "Raffle")
 public class RaffleView extends BaseCrudView<Raffle> implements View
 {
 	private static final long serialVersionUID = 1L;
 
 	@SuppressWarnings("unused")
 	private static Logger logger = LogManager.getLogger(RaffleView.class);
 
 	public static final String NAME = "Raffle";
 	
 	TabSheet tabs = new TabSheet();
 
 
 	@Override
 	protected AbstractLayout buildEditor(ValidatingFieldGroup<Raffle> fieldGroup2)
 	{
 		buildOverviewTab();
 		
 		buildRaffleBookTab();
 		buildRaffleAllocationTab();
 
 		VerticalLayout layout = new VerticalLayout();
 		layout.setSizeFull();
 		layout.addComponent(tabs);
 		return layout;
 	}
 
 	private void buildRaffleBookTab()
 	{
 		// Now add the child raffle book crud
 		RaffleBookChildView raffleBookView = new RaffleBookChildView(this);
 		raffleBookView.setSizeFull();
 		super.addChildCrudListener(raffleBookView);
 
 		tabs.addTab(raffleBookView, "Books");
 		
 	}
 
 	private void buildRaffleAllocationTab()
 	{
 		// Now add the child raffle book crud
 		RaffleAllocationChildView raffleAllocationView = new RaffleAllocationChildView(this);
 		raffleAllocationView.setSizeFull();
 		super.addChildCrudListener(raffleAllocationView);
 
 		tabs.addTab(raffleAllocationView, "Allocations");
 		
 	}
 
 	private void buildOverviewTab()
 	{
 		SMMultiColumnFormLayout<Raffle> overviewForm = new SMMultiColumnFormLayout<Raffle>(1, this.fieldGroup);
 		overviewForm.setColumnFieldWidth(0, 300);
 		overviewForm.setSizeFull();
 
 		overviewForm.bindTextField("Name", Raffle_.name);
 		 overviewForm.bindTextAreaField("Notes", Raffle_.notes, 6);
 		overviewForm.bindDateField("Start Date", Raffle_.startDate, "yyyy/MM/dd", Resolution.DAY);
 		overviewForm.bindDateField("Collect By Date", Raffle_.collectionsDate, "yyyy/MM/dd", Resolution.DAY)
 				.setDescription("The date the raffle ticksets need to be collected by.");
 		overviewForm.bindDateField("Return Date", Raffle_.returnDate, "yyyy/MM/dd", Resolution.DAY).setDescription(
 				"The date the raffle ticksets need to be returned to Branch.");
 		
 		
 
 		FormHelper<Raffle> formHelper = overviewForm.getFormHelper();
 		ComboBox groupRaffleManager = formHelper.new EntityFieldBuilder<Contact>()
 				.setLabel("Group Raffle Manager").setField(Raffle_.groupRaffleManager).setListFieldName(Contact_.fullname).build();
 		groupRaffleManager.setFilteringMode(FilteringMode.CONTAINS);
 		groupRaffleManager.setTextInputAllowed(true);
 		groupRaffleManager.setDescription("The Group member responsible for organising the Raffle.");
 		
 		ComboBox branchRaffleConact = formHelper.new EntityFieldBuilder<Contact>()
 				.setLabel("Branch Raffle Contact").setField(Raffle_.branchRaffleContact).setListFieldName(Contact_.fullname).build();
 		branchRaffleConact.setFilteringMode(FilteringMode.CONTAINS);
 		branchRaffleConact.setTextInputAllowed(true);
 		branchRaffleConact.setDescription("The Branch person who is a main contact for Raffle issues.");
 		
 		overviewForm.bindTextField("Book No. Prefix", Raffle_.raffleNoPrefix).setDescription(
 				"If raffle books have a non-numeric prefix for the ticket no's enter it here.");
 		overviewForm.bindTextField("Tickets per Book", Raffle_.ticketsPerBook);
 		overviewForm.bindTextField("Total Tickets Sold", Raffle_.totalTicketsSold).setReadOnly(true);
 		overviewForm.bindTextField("Tickets Outstanding", Raffle_.ticketsOutstanding).setReadOnly(true);
 		overviewForm.bindTextField("Sales Price per Ticket", Raffle_.salePricePerTicket).setDescription(
 				"The amount each ticket is to be sold for.");
 		overviewForm.bindTextField("Revenue Target", Raffle_.revenueTarget).setDescription(
 				"The amount the Group is aiming to raise via the Raffle.");
 
 		overviewForm.bindTextField("Revenue Raised", Raffle_.revenueRaised).setReadOnly(true);
 		
 		tabs.addTab(overviewForm, "Raffle");
 	}
 
 	@Override
 	public void enter(ViewChangeEvent event)
 	{
 		JPAContainer<Raffle> container = new DaoFactory().getRaffleDao().createVaadinContainer();
 		
 		tabs.setSizeFull();
 
 		Builder<Raffle> builder = new HeadingPropertySet.Builder<Raffle>();
 		builder.addColumn("Name", Raffle_.name).addColumn("Start Date", Raffle_.startDate)
 				.addColumn("Collection Date", Raffle_.collectionsDate);
 
 		super.init(Raffle.class, container, builder.build());
 	}
 
 	@Override
 	protected Filter getContainerFilter(String filterString, boolean advancedSearchActive)
 	{
		return new Or(new SimpleStringFilter(Raffle_.name.getName(), filterString, true, false),
				new SimpleStringFilter(Raffle_.startDate.getName(), filterString, true, false));
		
 	}
 
 
 	@Override
 	protected List<CrudAction<Raffle>> getCrudActions()
 	{
 		List<CrudAction<Raffle>> actions = super.getCrudActions();
 
 		actions.add(new RaffleActionImportBooks());
 		actions.add(new RaffleActionAllocateBooks());
 		return actions;
 	}
 
 	@Override
 	protected String getTitleText()
 	{
 		return "Raffle";
 	};
 
 }
