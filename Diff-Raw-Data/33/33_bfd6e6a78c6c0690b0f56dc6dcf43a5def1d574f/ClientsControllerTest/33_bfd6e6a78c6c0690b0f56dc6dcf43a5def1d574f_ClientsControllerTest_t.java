 package br.com.xisp.test.controllers;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.caelum.vraptor.util.test.MockResult;
 import br.com.caelum.vraptor.util.test.MockValidator;
 import br.com.caelum.vraptor.validator.Message;
 import br.com.caelum.vraptor.validator.ValidationException;
 import br.com.xisp.controllers.ClientsController;
 import br.com.xisp.models.Client;
 import br.com.xisp.models.Project;
 import br.com.xisp.repository.ClientRepository;
 import br.com.xisp.repository.ProjectRepository;
 
 public class ClientsControllerTest {
 
 	private Mockery mockery;
 	private MockResult result;
 	private ClientRepository repo;
 	private ProjectRepository repoP;
 	
 	private ClientsController controller;
 
 	@Before
 	public void setUp() throws Exception {
 		mockery = new Mockery();
 		repo = mockery.mock(ClientRepository.class);
 		result = new MockResult();
 		controller = new ClientsController(repo, new MockValidator(), result);
 		repoP = mockery.mock(ProjectRepository.class);
 	}
 	
 	@Test
 	public void shoulNotAddInvalidClient(){
 		Client client = givenInvalidClient();
 		willNotAddClient(client);
 	    try {
 	       controller.add(client);
 	       Assert.fail();
 	    } catch (ValidationException e) {
 	       java.util.List<Message> errors = e.getErrors();
 	       Assert.assertEquals("O campo nome deve ser preenchido", errors.get(0).getMessage());
 	    }
 	}
 	
 	
 	private void willNotAddClient(final Client client) {
 		mockery.checking(new Expectations() {
 			{
 				never(repo).add(client);
 			}
 		});
 	}
 
 	private Client givenInvalidClient() {
 		Client client = new Client();
 		client.setName("");
 		client.setEndereco("");
 		return client;
 	}
 
 	@Test
 	public void shouldAddNewClient(){
 		Client client = givenAClient();
 		willAddAClient(client);
 		controller.add(client);
 	}
 
 	
 	@Test
 	public void shouldListAllClients(){
 		willListAllClients();
 		controller.index();
 	}
 	
 	@Test
 	public void shouldLoadAClientEdit(){
 		Client client = givenAClient();
 		willLoadAClientToEdit(client);
 		controller.edita(client);
 	}
 	
 	
 	@Test
 	public void shouldUpdateAClient(){
 		Client client = givenAClient();
 		willUpdateTheClient(client);
 		controller.alterar(client);
 	}
 	
 	@Test
 	public void shouldRemoveAClient() throws Exception{
 		Client client = givenAClient();
 		willAddTheClient(client);
 		controller.add(client);
 		willRemoveACliente(client);
 		controller.remove(client);
		Assert.assertNull(repo.load(client));
 	}
 	
 
 
 	private void willRemoveACliente(final Client client) throws Exception {
 		mockery.checking(new Expectations() {
 			{
 				one(repo).remove(client);
				one(repo).load(client);
 			}
 		});
 	}
 	
 
 	private void willAddTheClient(final Client client) {
 		mockery.checking(new Expectations() {
 			{
 				one(repo).add(client);
				one(repo).load(client);
 			}
 		});
 	}
 
 	private void willUpdateTheClient(final Client client) {
 		mockery.checking(new Expectations() {
 			{
 				one(repo).update(client);
 			}
 		});
 	}
 
 	private void willLoadAClientToEdit(final Client client) {
 		mockery.checking(new Expectations() {
 			{
 				one(repo).load(client);
 			}
 		});
 	}
 
 	private void willListAllClients() {
 		mockery.checking(new Expectations() {
 			{
 				one(repo).showAll();
 			}
 		});
 	}
 
 	private void willAddAClient(final Client client) {
 		mockery.checking(new Expectations() {
 			{
 				one(repo).add(client);
 			}
 		});
 	}
 	

	private Client givenAClient() {
		Client client = new Client();
		client.setName("Robert");
		client.setEndereco("New York - Main Street");
		return client;
 	}
 	
 }
