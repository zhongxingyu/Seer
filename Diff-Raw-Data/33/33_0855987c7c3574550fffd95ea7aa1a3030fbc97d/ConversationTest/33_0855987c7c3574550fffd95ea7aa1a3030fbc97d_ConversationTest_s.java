 package de.mq.merchandise.model.support;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import de.mq.merchandise.customer.Customer;
 
 public class ConversationTest {
 	
 	private static final long TIMEOUT = 19680528L;
 	private static final String CONVERSATION_ID = "Conversation-ID-0815";
 	private final FacesContextFactory facesContextFactory = Mockito.mock(FacesContextFactory.class);
 	private final FacesContext facesContext = Mockito.mock(FacesContext.class);
 	
 	private final ExternalContext externalContext = Mockito.mock(ExternalContext.class);
 	
 	private final Map<String,Object> sessionMap = new HashMap<>();
 	private final Conversation conversation = new SimpleConversationImpl(facesContextFactory);
 	
 	@Before
 	public void setup() {
 		sessionMap.clear();
 		Mockito.when(facesContextFactory.facesContext()).thenReturn(facesContext);
 		Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
 		Mockito.when(externalContext.getSessionMap()).thenReturn(sessionMap);
 		
 	}
 	
 	@Test
 	public final void begin() {
 		conversation.begin();
 		Assert.assertTrue(sessionMap.containsKey(AbstractConversation.KEY_CONVERSATION_IN_SESSION_MAP));
 		
 		final Map<String, Object> results = getResultMap();
 		Assert.assertEquals(2, results.size());
 		Assert.assertTrue(results.containsKey(AbstractConversation.KEY_CONVERSATION_ID));
 		Assert.assertTrue(results.containsKey(AbstractConversation.KEY_TRANSIENT));
 		Assert.assertFalse((boolean) results.get(AbstractConversation.KEY_TRANSIENT));
 		Assert.assertEquals(AbstractConversation.DEFAULT_ID, results.get(AbstractConversation.KEY_CONVERSATION_ID));
 	}
 
 	@SuppressWarnings("unchecked")
 	private Map<String, Object> getResultMap() {
 		return (Map<String, Object>) sessionMap.get(AbstractConversation.KEY_CONVERSATION_IN_SESSION_MAP);
 	}
 	
 	@Test
 	public final void beginWithId() {
 		conversation.begin(CONVERSATION_ID);
 		final Map<String, Object> results = getResultMap();
 		Assert.assertEquals(2, results.size());
 		Assert.assertTrue(results.containsKey(AbstractConversation.KEY_CONVERSATION_ID));
 		Assert.assertTrue(results.containsKey(AbstractConversation.KEY_TRANSIENT));
 		Assert.assertFalse((boolean) results.get(AbstractConversation.KEY_TRANSIENT));
 		Assert.assertEquals(CONVERSATION_ID, results.get(AbstractConversation.KEY_CONVERSATION_ID));
 	}
 	
 	@Test
 	public final void end() {
 		final Map<String, Object> map = new HashMap<>();
 		map.put("customer", Mockito.mock(Customer.class));
 		map.put(AbstractConversation.KEY_TRANSIENT, false);
 		map.put(AbstractConversation.KEY_CONVERSATION_ID, CONVERSATION_ID);
 		sessionMap.put(AbstractConversation.KEY_CONVERSATION_IN_SESSION_MAP, map);
 		conversation.end();
 		Assert.assertTrue(getResultMap().isEmpty());
 	}
 	
 	
 	
 	
 	@Test
 	public final void timeout() {
 		final Map<String, Object> map = new HashMap<>();
 		map.put(AbstractConversation.KEY_TIMEOUT, TIMEOUT);
 		sessionMap.put(AbstractConversation.KEY_CONVERSATION_IN_SESSION_MAP, map);
 		Assert.assertEquals(19680528L, conversation.getTimeout());
 		conversation.end();
 		Assert.assertEquals(Long.MAX_VALUE, conversation.getTimeout());
 	}
 	
 	@Test
 	public final void  setTimeOut() {
 		conversation.setTimeout(TIMEOUT);
 		Assert.assertEquals(TIMEOUT, getResultMap().get(AbstractConversation.KEY_TIMEOUT));
 	}
 	
 	@Test
 	public final void isTransient() {
 		Assert.assertTrue(conversation.isTransient());
 		
 		final Map<String, Object> map = new HashMap<>();
 		map.put(AbstractConversation.KEY_TRANSIENT, false);
 		sessionMap.put(AbstractConversation.KEY_CONVERSATION_IN_SESSION_MAP, map);
 		
 		Assert.assertFalse(conversation.isTransient());
 	}
 
 }
