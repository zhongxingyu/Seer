 package de.mq.merchandise.controller;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.primefaces.event.FlowEvent;
 
import de.mq.mapping.util.proxy.Conversation;
 import de.mq.merchandise.customer.Customer;
 import de.mq.merchandise.customer.CustomerService;
 import de.mq.merchandise.customer.Person;
 import de.mq.merchandise.model.Registration;
 import de.mq.merchandise.model.Registration.Kind;
 import de.mq.merchandise.util.ValidationService;
 
 public class RegistrationWizardControllerTest {
 
 	private static final long ID = 19680528L;
 	private Customer customer = Mockito.mock(Customer.class);
 	private CustomerService customerService = Mockito.mock(CustomerService.class);
     private  RegistrationWizardControllerImpl registrationWizardController;
 	private FlowEvent flowEvent;
 
 	private Registration registration = Mockito.mock(Registration.class);
 	private ValidationService validationService;
 	private Conversation conversation=Mockito.mock(Conversation.class);
 	
 	@Before
 	public final void setup() {
 		validationService=Mockito.mock(ValidationService.class);
 		customer = Mockito.mock(Customer.class);
 		Mockito.when(customer.id()).thenReturn(ID);
 		customerService = Mockito.mock(CustomerService.class);
 		
 		
 		registrationWizardController = new RegistrationWizardControllerImpl(customerService,validationService, conversation);
 		flowEvent = Mockito.mock(FlowEvent.class);
 		registration = Mockito.mock(Registration.class);
 		Mockito.when(registration.kind()).thenReturn(Registration.Kind.User);
 		Mockito.when(registration.customer()).thenReturn(customer);
 	
 	}
 	
 	@Test
 	public final void onFlowProzessNotUserRegistration() {
 		
 		//registrationWizardController = new RegistrationWizardControllerImpl(Mockito.mock(CustomerService.class), Mockito.mock(ApplicationContext.class));
 		Mockito.when(flowEvent.getNewStep()).thenReturn(RegistrationWizardControllerImpl.PERSON);
 		Mockito.when(flowEvent.getOldStep()).thenReturn(RegistrationWizardControllerImpl.GENERAL);
 		
 		Assert.assertEquals(RegistrationWizardControllerImpl.PERSON, registrationWizardController.onFlowProcess(flowEvent, registration));
 		
 		Mockito.when(flowEvent.getNewStep()).thenReturn(RegistrationWizardControllerImpl.GENERAL);
 		Mockito.when(flowEvent.getOldStep()).thenReturn(RegistrationWizardControllerImpl.PERSON);
 		Assert.assertEquals(RegistrationWizardControllerImpl.GENERAL, registrationWizardController.onFlowProcess(flowEvent, registration));
 		Mockito.verify(validationService).validate(registration.getPerson());
 	} 
 	
 	@Test
 	public final void onFlowProzessUserRegistrationNewUser() {
 		Mockito.when(customerService.customer(ID)).thenReturn(customer);
 		
 		
 		Mockito.when(flowEvent.getNewStep()).thenReturn(RegistrationWizardControllerImpl.OVERVIEW);
 		Mockito.when(flowEvent.getOldStep()).thenReturn(RegistrationWizardControllerImpl.PERSON);
 		
 		Assert.assertEquals(RegistrationWizardControllerImpl.OVERVIEW, registrationWizardController.onFlowProcess(flowEvent,registration));
 		Mockito.verify(validationService).validate(registration.getPerson());
 	}
 	
 	@Test
 	public final void onFlowProzessNotUserRegistrationLastStep() {
 		//Mockito.when(customerService.customer(ID)).thenReturn(customer);
 		Mockito.when(registration.kind()).thenReturn(Kind.LegalPerson);
 		
 		Mockito.when(flowEvent.getNewStep()).thenReturn(RegistrationWizardControllerImpl.OVERVIEW);
 		Mockito.when(flowEvent.getOldStep()).thenReturn(RegistrationWizardControllerImpl.PERSON);
 		
 		Assert.assertEquals(RegistrationWizardControllerImpl.OVERVIEW, registrationWizardController.onFlowProcess(flowEvent,registration));
 		Mockito.verifyNoMoreInteractions(customerService);
 		Mockito.verify(validationService).validate(registration.getPerson());
 	}
 	
 	
 	
 	
 	@Test
 	public final void register(){
 		
 		final Person person = Mockito.mock(Person.class);
 		registrationWizardController.register(customer, person);
 		Mockito.verify(customerService).register(customer, person);
 		Mockito.verify(conversation).end();
 	}
 	
 	
 	@Test
 	public final void startConversation() {
 		Mockito.when(conversation.isTransient()).thenReturn(true, false);
 		for(int i=0; i < 10; i++) {
 		    registrationWizardController.startConversation();
 		}
 		Mockito.verify(conversation, Mockito.times(1)).begin();
 		Mockito.verify(conversation, Mockito.times(10)).isTransient();
 	}
 
 }
