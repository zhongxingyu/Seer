 package de.mq.merchandise.controller;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.springframework.test.util.ReflectionTestUtils;
 
import de.mq.mapping.util.proxy.Conversation;
 import de.mq.merchandise.customer.Customer;
 import de.mq.merchandise.opportunity.ClassificationService;
 import de.mq.merchandise.opportunity.support.ActivityClassification;
 import de.mq.merchandise.opportunity.support.ActivityClassificationTreeAO;
 import de.mq.merchandise.opportunity.support.CommercialRelation;
 import de.mq.merchandise.opportunity.support.CommercialSubject;
 import de.mq.merchandise.opportunity.support.Condition;
 import de.mq.merchandise.opportunity.support.ConditionAO;
 import de.mq.merchandise.opportunity.support.ConditionConstants;
 import de.mq.merchandise.opportunity.support.KeyWordModelAO;
 import de.mq.merchandise.opportunity.support.Opportunity;
 import de.mq.merchandise.opportunity.support.OpportunityAO;
 import de.mq.merchandise.opportunity.support.OpportunityImpl;
 import de.mq.merchandise.opportunity.support.OpportunityModelAO;
 import de.mq.merchandise.opportunity.support.OpportunityService;
 import de.mq.merchandise.opportunity.support.PagingAO;
 import de.mq.merchandise.opportunity.support.ProductClassification;
 import de.mq.merchandise.opportunity.support.ProductClassificationTreeAO;
 import de.mq.merchandise.util.EntityUtil;
 import de.mq.merchandise.util.Paging;
 
 public class OpportunityControllerTest {
 
 	private static final String CONDITION_VALUE = "ConditionValue";
 	private static final String KEY_WORD = "KeyWord";
 	private static final String PATTERN = "Kylie";
 	private final OpportunityService opportunityService = Mockito.mock(OpportunityService.class);
 	private final ClassificationService classificationService = Mockito.mock(ClassificationService.class);
 
 	private final OpportunityAO opportunityAO = Mockito.mock(OpportunityAO.class);
 	private final Customer customer = Mockito.mock(Customer.class);
 	private final OpportunityModelAO opportunityModelAO = Mockito.mock(OpportunityModelAO.class);
 	private final PagingAO pagingAO = Mockito.mock(PagingAO.class);
 	private final Paging paging = Mockito.mock(Paging.class);
 
 	private final Collection<Opportunity> opportunities = new ArrayList<>();
 
 	private Opportunity opportunity = Mockito.mock(Opportunity.class);
 	private final List<OpportunityAO> opportunityAOs = new ArrayList<>();
 
 	private final List<ActivityClassification> activities = new ArrayList<>();
 
 	private final ActivityClassification activityClassification = Mockito.mock(ActivityClassification.class);
 
 	private final List<ProductClassification> products = new ArrayList<>();
 
 	private final ProductClassification productClassification = Mockito.mock(ProductClassification.class);
 
 	private final KeyWordModelAO keyWordModel = Mockito.mock(KeyWordModelAO.class);
 
 	private final ConditionAO conditionAO = Mockito.mock(ConditionAO.class);
 	private final Condition condition = Mockito.mock(Condition.class);
 
 	private final Conversation conversation = Mockito.mock(Conversation.class);
 
 	@Before
 	public final void setup() {
 		opportunities.clear();
 		opportunityAOs.clear();
 		opportunities.add(opportunity);
 
 		Mockito.when(pagingAO.getPaging()).thenReturn(paging);
 
 		Mockito.when(opportunityModelAO.getPattern()).thenReturn(PATTERN);
 		Mockito.when(opportunityModelAO.getPaging()).thenReturn(pagingAO);
 
 		Mockito.when(opportunityService.opportunities(customer, PATTERN + "%", paging)).thenReturn(opportunities);
 		Mockito.when(opportunityAO.getOpportunity()).thenReturn(opportunity);
 		Mockito.when(opportunityModelAO.getOpportunities()).thenReturn(opportunityAOs);
 		opportunityAOs.add(opportunityAO);
 
 		activities.clear();
 		activities.add(activityClassification);
 
 		Mockito.when(classificationService.activityClassifications()).thenReturn(activities);
 
 		products.clear();
 		products.add(productClassification);
 		Mockito.when(classificationService.productClassCollections()).thenReturn(products);
 
 		Mockito.when(keyWordModel.getKeyWord()).thenReturn(KEY_WORD);
 
 		Mockito.when(conditionAO.getCondition()).thenReturn(condition);
 
 	}
 
 	private final OpportunityControllerImpl opportunityControllerImpl = new OpportunityControllerImpl(opportunityService, classificationService, conversation);
 
 	@Test
 	public final void opportunities() {
 
 		Mockito.when(opportunityModelAO.getSelected()).thenReturn(opportunityAO);
 		opportunityControllerImpl.opportunities(opportunityModelAO, customer);
 		Mockito.verify(opportunityModelAO).setOpportunities(opportunities);
 		Mockito.verify(opportunityModelAO, Mockito.times(0)).setSelected(null);
 
 	}
 
 	@Test
 	public final void opportunitiesSelectedNotInResult() {
 		OpportunityAO mock = Mockito.mock(OpportunityAO.class);
 		Mockito.when(opportunityModelAO.getSelected()).thenReturn(mock);
 
 		opportunityControllerImpl.opportunities(opportunityModelAO, customer);
 
 		Mockito.verify(opportunityModelAO).setOpportunities(opportunities);
 
 		Mockito.verify(opportunityModelAO, Mockito.times(1)).setSelected(null);
 
 	}
 
 	@Test
 	public final void nothingSelected() {
 		opportunityControllerImpl.opportunities(opportunityModelAO, customer);
 		Mockito.verify(opportunityModelAO).setOpportunities(opportunities);
 		Mockito.verify(opportunityModelAO, Mockito.times(0)).getOpportunities();
 		Mockito.verify(opportunityModelAO, Mockito.times(0)).setSelected(null);
 
 	}
 
 	@Test
 	public final void create() {
 		ActivityClassificationTreeAO activityClassificationTreeAO = Mockito.mock(ActivityClassificationTreeAO.class);
 		ProductClassificationTreeAO productClassificationTreeAO = Mockito.mock(ProductClassificationTreeAO.class);
 		Assert.assertEquals("opportunity", opportunityControllerImpl.create(activityClassificationTreeAO, productClassificationTreeAO));
 
 		Mockito.verify(activityClassificationTreeAO).setClassifications(activities);
 		Mockito.verify(productClassificationTreeAO).setClassifications(products);
 
 		Mockito.verify(activityClassificationTreeAO).notifyClassificationsChanged();
 		Mockito.verify(productClassificationTreeAO).notifyClassificationsChanged();
 
 	}
 
 	@Test
 	public final void onActivityNodeSelectRemove() {
 		Mockito.when(opportunity.activityClassifications()).thenReturn(activities);
 		opportunityControllerImpl.onActivityNodeSelect(activityClassification, opportunityAO);
 		Mockito.verify(opportunity).removeClassification(activityClassification);
 		Mockito.verify(opportunityAO).notifyActivityClassificationChanged();
 
 	}
 
 	@Test
 	public final void onActivityNodeSelectAdd() {
 		Mockito.when(opportunity.activityClassifications()).thenReturn(new ArrayList<ActivityClassification>());
 		opportunityControllerImpl.onActivityNodeSelect(activityClassification, opportunityAO);
 		Mockito.verify(opportunity).assignClassification(activityClassification);
 		Mockito.verify(opportunityAO).notifyActivityClassificationChanged();
 	}
 
 	@Test
 	public final void onProductNodeSelectRemove() {
 		Mockito.when(opportunity.productClassifications()).thenReturn(products);
 		opportunityControllerImpl.onProductNodeSelect(productClassification, opportunityAO);
 		Mockito.verify(opportunity).removeClassification(productClassification);
 		Mockito.verify(opportunityAO).notifyProductClassificationChanged();
 
 	}
 
 	@Test
 	public final void onProductNodeSelectAdd() {
 		Mockito.when(opportunity.productClassifications()).thenReturn(new ArrayList<ProductClassification>());
 		opportunityControllerImpl.onProductNodeSelect(productClassification, opportunityAO);
 		Mockito.verify(opportunity).assignClassification(productClassification);
 		Mockito.verify(opportunityAO).notifyProductClassificationChanged();
 	}
 
 	@Test
 	public final void addKeyWord() {
 		opportunityControllerImpl.addKeyWord(opportunity, keyWordModel);
 		Mockito.verify(opportunity).assignKeyWord(KEY_WORD);
 		Mockito.verify(keyWordModel).setKeyWord(null);
 	}
 
 	@Test
 	public final void deleteKeyWord() {
 		opportunityControllerImpl.deleteKeyWord(opportunity, keyWordModel);
 		Mockito.verify(keyWordModel).setSelectedKeyWord(null);
 	}
 
 	@Test
 	public final void addSubject() {
 		final CommercialSubject commercialSubject = Mockito.mock(CommercialSubject.class);
 		Assert.assertEquals("opportunity.xhtml", opportunityControllerImpl.addSubject(opportunityAO, commercialSubject));
 
 		Mockito.verify(opportunity).assignConditions(commercialSubject);
 		Mockito.verify(opportunityAO).notifyConditionsChanged();
 	}
 
 	@Test
 	public final void addConditionValue() {
 		Mockito.when(conditionAO.getValue()).thenReturn(CONDITION_VALUE);
 
 		opportunityControllerImpl.addConditionValue(conditionAO);
 
 		Mockito.verify(condition).assignValue(CONDITION_VALUE);
 		Mockito.verify(conditionAO).setValue(null);
 	}
 
 	@Test
 	public final void addConditionValueNull() {
 		Mockito.when(conditionAO.getValue()).thenReturn(null);
 		opportunityControllerImpl.addConditionValue(conditionAO);
 		Mockito.verify(condition, Mockito.times(0)).assignValue(CONDITION_VALUE);
 		Mockito.verify(conditionAO, Mockito.times(0)).setValue(null);
 
 	}
 
 	@Test
 	public final void addConditionBlanks() {
 		Mockito.when(conditionAO.getValue()).thenReturn(" ");
 		opportunityControllerImpl.addConditionValue(conditionAO);
 		Mockito.verify(condition, Mockito.times(0)).assignValue(CONDITION_VALUE);
 		Mockito.verify(conditionAO, Mockito.times(0)).setValue(null);
 
 	}
 
 	@Test
 	public final void deleteCondition() {
 		Mockito.when(conditionAO.getSelectedValue()).thenReturn(CONDITION_VALUE);
 		opportunityControllerImpl.deleteConditionValue(conditionAO);
 
 		Mockito.verify(condition).removeValue(CONDITION_VALUE);
 		Mockito.verify(conditionAO).setSelectedValue(null);
 	}
 
 	@Test
 	public final void deleteConditionNull() {
 		Mockito.when(conditionAO.getSelectedValue()).thenReturn(null);
 		opportunityControllerImpl.deleteConditionValue(conditionAO);
 
 		Mockito.verify(condition, Mockito.times(0)).removeValue(CONDITION_VALUE);
 		Mockito.verify(conditionAO, Mockito.times(0)).setSelectedValue(null);
 	}
 
 	@Test
 	public final void clearCondition() {
 
 		final Condition condition = EntityUtil.copy(ConditionConstants.CONDITION);
 
 		Mockito.when(conditionAO.getCondition()).thenReturn(condition);
 		Mockito.when(conditionAO.getCommercialRelation()).thenReturn(ConditionConstants.CONDITION.commercialRelation());
 
 		Assert.assertNotNull(condition.commercialRelation());
 		Assert.assertNotNull(condition.calculation());
 		Assert.assertNotNull(condition.validation());
 		Assert.assertTrue(condition.values().size() > 0);
 		Assert.assertNotNull(condition.conditionType());
 
 		opportunityControllerImpl.clearCondition(conditionAO);
 
 		Assert.assertNull(condition.commercialRelation());
 		Assert.assertNull(condition.calculation());
 		Assert.assertNull(condition.validation());
 		Assert.assertFalse(condition.values().size() > 0);
 		Assert.assertNull(condition.conditionType());
 
 		Mockito.verify(conditionAO).setCommercialRelation(ConditionConstants.CONDITION.commercialRelation());
 	}
 
 	@Test
 	public final void addCondition() {
 		final Condition condition = EntityUtil.copy(ConditionConstants.CONDITION);
 
 		opportunityControllerImpl.addCondition(opportunityAO, condition);
 		Mockito.verify(opportunity).assignConditions(condition.commercialRelation().commercialSubject(), condition);
 		Mockito.verify(opportunityAO).notifyConditionsChanged();
 
 	}
 
 	@Test
 	public final void onConditionNodeSelect() {
 		final CommercialRelation commercialRelation = Mockito.mock(CommercialRelation.class);
 		opportunityControllerImpl.onConditionNodeSelect(commercialRelation, conditionAO);
 
 		Mockito.verify(conditionAO).setCommercialRelation(commercialRelation);
 	}
 
 	@Test
 	public final void onConditionNodeSelectWrongType() {
 		opportunityControllerImpl.onConditionNodeSelect("dontLetMeGetMe", conditionAO);
 
 		Mockito.verify(conditionAO, Mockito.times(0)).setCommercialRelation(Mockito.any(CommercialRelation.class));
 	}
 
 	@Test
 	public final void deleteConditionCommercialRelation() {
 		final CommercialRelation commercialRelation = Mockito.mock(CommercialRelation.class);
 		final CommercialSubject commercialSubject = Mockito.mock(CommercialSubject.class);
 		Mockito.when(commercialRelation.commercialSubject()).thenReturn(commercialSubject);
 		opportunityControllerImpl.deleteCondition(opportunityAO, commercialRelation);
 
 		Mockito.verify(opportunity).remove(commercialSubject);
 		Mockito.verify(opportunityAO).notifyConditionsChanged();
 	}
 
 	@Test
 	public final void deleteConditionOnly() {
 		final Condition condition = EntityUtil.copy(ConditionConstants.CONDITION);
 		final CommercialSubject commercialSubject = Mockito.mock(CommercialSubject.class);
 		ReflectionTestUtils.setField(condition.commercialRelation(), "commercialSubject", commercialSubject);
 
 		opportunityControllerImpl.deleteCondition(opportunityAO, condition);
 
 		Mockito.verify(opportunity).remove(condition.commercialRelation().commercialSubject(), condition.conditionType());
 		Mockito.verify(opportunityAO).notifyConditionsChanged();
 	}
 
 	@Test
 	public final void deleteConditionNothing() {
 		opportunityControllerImpl.deleteCondition(opportunityAO, "dontLetMeGetMe");
 		Mockito.verify(opportunityAO, Mockito.times(0)).notifyConditionsChanged();
 		Mockito.verifyZeroInteractions(opportunityAO);
 	}
 
 	@Test
 	public final void save() {
 
 		final Opportunity opportunity = EntityUtil.create(OpportunityImpl.class);
 		Assert.assertEquals("opportunities.xhtml", opportunityControllerImpl.save(opportunity, customer));
 
 		Mockito.verify(opportunityService).createOrUpdate(opportunity);
 		Assert.assertEquals(customer, opportunity.customer());
 		Mockito.verify(conversation).end();
 	}
 
 	@Test
 	public final void removeNewCommercialRelationsFromConditionEspeciallyForHibernateTransient() {
 		final Condition condition = EntityUtil.copy(ConditionConstants.CONDITION);
 
 		prepareCommercialRelation(condition, null);
 
 		Assert.assertNotNull(condition.commercialRelation());
 		opportunityControllerImpl.removeNewCommercialRelationsFromConditionEspeciallyForHibernate(opportunity);
 
 		Assert.assertNull(condition.commercialRelation());
 	}
 
 	@Test
 	public final void removeNewCommercialRelationsFromConditionEspeciallyForHibernateNotTransient() {
 		final Condition condition = EntityUtil.copy(ConditionConstants.CONDITION);
 
 		prepareCommercialRelation(condition, 19680528L);
 
 		Assert.assertNotNull(condition.commercialRelation());
 		opportunityControllerImpl.removeNewCommercialRelationsFromConditionEspeciallyForHibernate(opportunity);
 
 		Assert.assertNotNull(condition.commercialRelation());
 	}
 
 	private void prepareCommercialRelation(final Condition condition, Long id) {
 		final Collection<CommercialRelation> relations = new ArrayList<>();
 		final CommercialRelation commercialRelation = condition.commercialRelation();
 		relations.add(commercialRelation);
 		EntityUtil.setId(commercialRelation, id);
 		Mockito.when(opportunity.commercialRelations()).thenReturn(relations);
 
 		commercialRelation.assign(condition);
 	}
 	
 	@Test
 	public final void change() {
 		Mockito.when(opportunityService.read(19680528L)).thenReturn(opportunity);
 		
 		Assert.assertEquals("opportunity.xhtml", opportunityControllerImpl.change(opportunityAO, 19680528L));
 		
 		Mockito.verify(opportunityAO).setOpportunity(opportunity);
 		
 		Mockito.verify(opportunityAO).notifyConditionsChanged();
 		Mockito.verify(opportunityAO).notifyActivityClassificationChanged();
 		Mockito.verify(opportunityAO).notifyProductClassificationChanged();
 	}
 	
 	@Test
 	public final void changeIdNull() {
 		Assert.assertNull(opportunityControllerImpl.change(opportunityAO, null));
 		Mockito.verifyZeroInteractions(opportunityAO);
 	}
 	
 	@Test
 	public final void delete() {
 		Mockito.when(opportunityModelAO.getSelected()).thenReturn(opportunityAO);
 		Assert.assertNotNull(opportunityModelAO.getSelected());
 		Assert.assertNotNull(opportunityModelAO.getSelected().getOpportunity());
 		opportunityControllerImpl.delete(opportunityModelAO);
 		
 		Mockito.verify(opportunityService).delete(opportunityAO.getOpportunity());
 		Mockito.verify(opportunityModelAO).setSelected(null);
 	}
 	
 	@Test
 	public final void deleteSeletedIsNull(){
 		Assert.assertNull(opportunityModelAO.getSelected());
 		opportunityControllerImpl.delete(opportunityModelAO);
 		
 		Mockito.verifyNoMoreInteractions(opportunityService);
 		Mockito.verify(opportunityModelAO, Mockito.times(0)).setSelected(Mockito.any(OpportunityAO.class));
 	}
 
 }
