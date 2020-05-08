 package org.liveSense.misc.queryBuilder.gwt;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.liveSense.misc.queryBuilder.beans.Value;
 import org.liveSense.misc.queryBuilder.criterias.EqualCriteria;
 import org.liveSense.misc.queryBuilder.gwt.QueryBuilderTestHelper.QueryBuilderRequestContext;
 import org.liveSense.misc.queryBuilder.gwt.QueryBuilderTestHelper.QueryBuilderRequestFactory;
 import org.liveSense.misc.queryBuilder.gwt.valueproxies.beans.CompositeValueProxy;
 import org.liveSense.misc.queryBuilder.gwt.valueproxies.criterias.EqualCriteriaValueProxy;
 import org.liveSense.misc.queryBuilder.gwt.valueproxies.operands.OperandValueProxy;
 import org.liveSense.misc.queryBuilder.gwt.valueproxies.operators.AndOperatorValueProxy;
 import org.liveSense.misc.queryBuilder.gwt.valueproxies.operators.NotOperatorValueProxy;
 import org.liveSense.misc.queryBuilder.gwt.valueproxies.operators.OperatorValueProxy;
 import org.liveSense.misc.queryBuilder.operands.DefaultOperand;
 import org.liveSense.misc.queryBuilder.operators.AbstractOperator;
 import org.liveSense.misc.queryBuilder.operators.AndOperator;
 import org.liveSense.misc.queryBuilder.operators.NotOperator;
 import org.liveSense.misc.queryBuilder.operators.OrOperator;
 import org.mockito.ArgumentCaptor;
 import org.mortbay.log.Log;
 
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.AutoBeanUtils;
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 public class QueryBuilderRequestFactoryTest {
 
   private QueryBuilderService mockService;
   private QueryBuilderRequestFactory mockFactory;
 
  //private QueryBuilderService cachedService;
  //private QueryBuilderRequestFactory cachedFactory;
 
   @Before
   public void setup(){
	  Log.info("Before...");
	  System.setProperty("gwt.rf.ServiceLayerCache", "false");
 
 	mockService = RequestFactoryHelper.getMockService( QueryBuilderService.class );
     mockFactory = RequestFactoryHelper.createMock( QueryBuilderRequestFactory.class );
    //cachedService = RequestFactoryHelper.getCachedService( QueryBuilderService.class );
    //cachedFactory = RequestFactoryHelper.createCached( QueryBuilderRequestFactory.class );
     
     /*
     // Resetting GWT Cache
     Field field = ServiceLayer
         field.setAccessible(true);
 
         Field modifiersField = Field.class.getDeclaredField("modifiers");
         modifiersField.setAccessible(true);
         modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
 
         field.set(null, newValue);
      }
      */
   }
   
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Test
   public void testPolymorphism() {
     AbstractOperator abstractOperator = new AbstractOperator();
     AbstractOperator andOperator = new AndOperator();
     AbstractOperator orOperator = new OrOperator();
     AbstractOperator notOperator = new NotOperator();
 
     when( mockService.getOperators() ).thenReturn( Arrays.asList( abstractOperator, andOperator, orOperator, notOperator ) );
     Receiver<List<OperatorValueProxy>> receiver = mock( Receiver.class );
     
     mockFactory.context().getOperators().fire( receiver );
     
     ArgumentCaptor<List<OperatorValueProxy>> captor = (ArgumentCaptor)ArgumentCaptor.forClass( List.class );
     verify( receiver ).onSuccess( captor.capture() );
     List<OperatorValueProxy> operatorProxyList = captor.getValue();
     assertTrue( operatorProxyList.get( 0 ) instanceof OperatorValueProxy );
     assertTrue( operatorProxyList.get( 1 ) instanceof AndOperatorValueProxy );
     assertTrue( operatorProxyList.get( 2 ) instanceof OperatorValueProxy );
     assertTrue( operatorProxyList.get( 3 ) instanceof NotOperatorValueProxy );
   }
 
   
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Test
   public void testOperatorRequestCallback() {
 
 		// Composite test
 
 		AbstractOperator operator = new AndOperator();
 		EqualCriteria foreignKeyCriteria = new EqualCriteria();
 		DefaultOperand fieldName = new DefaultOperand();
 		Value fieldNameValue = new Value();
 		Value value = new Value();
 
 		fieldNameValue.setValueAsString("testName");
 		fieldName.setSource(fieldNameValue);
 		foreignKeyCriteria.setOperand(fieldName);
 		value.setValueAsInteger(1);
 		foreignKeyCriteria.setValue(value);
 		operator.setEqualCriteria(foreignKeyCriteria);
 
 		when( mockService.getOperator() ).thenReturn( operator);
 	    Receiver<OperatorValueProxy> receiver = mock( Receiver.class );
 	    
 	    mockFactory.context().getOperator().fire( receiver );
 	    
 	    ArgumentCaptor<OperatorValueProxy> captor = (ArgumentCaptor)ArgumentCaptor.forClass( OperatorValueProxy.class );
 	    verify( receiver ).onSuccess( captor.capture() );
 	    OperatorValueProxy operatorProxy = captor.getValue();
     
   }
 
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Test
   public void testOperatorDeserializerTest() {
 	  
	    QueryBuilderRequestContext req = mockFactory.context();// cachedFactory.context();
	    //QueryBuilderRequestContext req = cachedFactory.context();
 	    OperatorValueProxy operator = req.create(AndOperatorValueProxy.class);
 		EqualCriteriaValueProxy foreignKeyCriteria = req.create(EqualCriteriaValueProxy.class);
 		OperandValueProxy fieldName = req.create(OperandValueProxy.class);
 		CompositeValueProxy fieldNameValue = req.create(CompositeValueProxy.class);
 		CompositeValueProxy value = req.create(CompositeValueProxy.class);
 
 		fieldNameValue.setValueAsString("testName");
 		fieldName.setSource(fieldNameValue);
 		foreignKeyCriteria.setOperand(fieldName);
 		value.setValueAsInteger(1);
 		foreignKeyCriteria.setValue(value);
 		operator.setEqualCriteria(foreignKeyCriteria);
 		Assert.assertEquals("{\"equalCriteria\":{\"value\":{\"valueAsInteger\":1},\"operand\":{\"source\":{\"valueAsString\":\"testName\"}}}}", AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(operator)).getPayload());
 
 	    Receiver<Void> receiver = mock( Receiver.class );
 	    
 	    req.setOperator(operator).fire( receiver );
 	    
 	    ArgumentCaptor<Void> captor = (ArgumentCaptor)ArgumentCaptor.forClass(Void.class );
 
 	    // Verify the the Deserialized object
//	    assertNotNull(cachedService);
//	    assertNotNull(cachedService.getOperator());
//	    assertEquals(AndOperator.class.getName(), cachedService.getOperator().getClass().getName());
//	    assertEquals(EqualCriteria.class.getName(), cachedService.getOperator().getParams().get(0).getClass().getName());
//	    assertEquals("testName", ((EqualCriteria)cachedService.getOperator().getParams().get(0)).getOperand().getSource().getValueAsString());

//	    assertNotNull(mockService);
//	    assertNotNull(mockService.getOperator());
//	    assertEquals(AndOperator.class.getName(), mockService.getOperator().getClass().getName());
//	    assertEquals(EqualCriteria.class.getName(), mockService.getOperator().getParams().get(0).getClass().getName());
//	    assertEquals("testName", ((EqualCriteria)mockService.getOperator().getParams().get(0)).getOperand().getSource().getValueAsString());
 
   }
 
 }
