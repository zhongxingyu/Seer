 package net.frontlinesms.plugins.learn.ui.topic;
 
 import static org.mockito.Mockito.*;
 import static net.frontlinesms.plugins.learn.LearnTestUtils.*;
 
 import net.frontlinesms.data.events.EntityDeletedNotification;
 import net.frontlinesms.data.events.EntitySavedNotification;
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.plugins.learn.data.domain.*;
 import net.frontlinesms.plugins.learn.data.repository.*;
 import net.frontlinesms.plugins.learn.ui.topic.TopicTabHandler;
 import net.frontlinesms.test.spring.MockBean;
 import net.frontlinesms.test.ui.ThinletComponent;
 import net.frontlinesms.test.ui.ThinletEventHandlerTest;
 
 public class TopicTabHandlerTest extends ThinletEventHandlerTest<TopicTabHandler> {
 	@SuppressWarnings("unused")
 	@MockBean private EventBus eventBus;
 	@MockBean private TopicDao topicDao;
 	@MockBean private TopicItemDao topicItemDao;
 
 //> SETUP METHODS
 	@Override
 	protected TopicTabHandler initHandler() {
 		return new TopicTabHandler(ui, ctx);
 	}
 	
 	@Override
 	protected Object getRootComponent() {
 		return h.getTab();
 	}
 	
 //> TEST METHODS
 	public void testTopicsListVisible() {
 		$("trTopics").exists();
 	}
 	
 	public void testLazyGenerationOfTopicItemLists() throws Exception {
 		// given
 		mockTopics(topicDao, "Food", "Drink");
 		initUiForTests();
 		verify(topicItemDao, never()).getAllByTopic(any(Topic.class));
 		
 		// when
 		ThinletComponent drinkTopicNode = $("trTopics").getRootNode().withText("Drink");
 		assertFalse(drinkTopicNode.isExpanded());
 		drinkTopicNode.expand();
 		
 		// then
 		verify(topicItemDao).getAllByTopic(topicWithName("Drink"));
 	}
 	
	public void testTopicListRefreshDisablesEditAndDeleteButtons() {
		// given
		mockTopics(topicDao, "Food", "Drink");
		initUiForTests();
		$("trTopics").getSubNode().withText("Drink").select();
		assertTrue($("btEditTreeItem").isEnabled());
		assertTrue($("btDeleteTreeItem").isEnabled());
		
		// when
		h.notify(mockEntityUpdatedNotification(Topic.class));
		waitForUiEvents();
		
		// then
		assertFalse($("btEditTreeItem").isEnabled());
		assertFalse($("btDeleteTreeItem").isEnabled());
	}
	
 	@SuppressWarnings("unchecked")
 	public void testTopicListUpdatesOnNewTopicAndDeletion() {
 		// given
 		Topic mockTopic1 = mock(Topic.class);
 		Topic mockTopic2 = mock(Topic.class);
 		when(topicDao.list()).thenReturn(asList(mockTopic1),
 				asList(mockTopic1, mockTopic2),
 				asList(mockTopic2),
 				emptyList(Topic.class));
 		
 		// then on load there are no topics in the list
 		assertEquals(0, $("trTopics").getRootNode().count());
 		
 		// when
 		h.notify(new EntitySavedNotification<Topic>(mockTopic1));
 		// then
 		waitForUiEvents();
 		assertEquals(1, $("trTopics").getRootNode().count());
 		
 		// when
 		h.notify(new EntitySavedNotification<Topic>(mockTopic2));
 		// then
 		waitForUiEvents();
 		assertEquals(2, $("trTopics").getRootNode().count());
 		
 		// when
 		h.notify(new EntityDeletedNotification<Topic>(mockTopic1));
 		// then
 		waitForUiEvents();
 		assertEquals(1, $("trTopics").getRootNode().count());
 		
 		// when
 		h.notify(new EntityDeletedNotification<Topic>(mockTopic2));
 		// then
 		waitForUiEvents();
 		assertEquals(0, $("trTopics").getRootNode().count());
 	}
 
 	public void testNewTopicButton() {		
 		// when
 		$("btNewTopic").click();
 		
 		// then
 		assertEquals("plugins.learn.topic.new", $("dgEditTopic").getText());
 	}
 	
 	public void testNewReinforcementButton() {
 		// given
 		mockTopics(topicDao, "Random Topic 1", "Random Topic 2");
 		initUiForTests();
 		
 		// when
 		$("btNewReinforcement").click();
 		// then
 		assertEquals("plugins.learn.reinforcement.new", $("dgEditReinforcement").getText());
 	}
 	
 	public void testNewReinforcementButtonDisabledWhenNoTopics() {
 		assertFalse($("btNewReinforcement").isEnabled());
 	}
 	
 	public void testEditReinforcementTrigger() {
 		// given
 		mockReinforcement(topicDao, "People & Places", topicItemDao, "Albert Einstein worked in a patent office for a bit.");
 		initUiForTests();
 		
 		// when
 		ThinletComponent topicComponent = $("trTopics").getRootNode().withText("People & Places");
 		topicComponent.expand();
 		topicComponent.getSubNode().withText("Albert Einstein worked in a patent office for a bit.").select();
 		topicComponent.getSubNode().withText("Albert Einstein worked in a patent office for a bit.").doubleClick();
 		
 		// then
 		assertTrue($("dgEditReinforcement").isVisible());
 		assertEquals("People & Places", $("dgEditReinforcement").find("cbTopic").getText());
 		assertEquals("Albert Einstein worked in a patent office for a bit.",
 				$("taText").getText());
 	}
 	
 	public void testEditReinforcementWhenEditButtonClicked() {
 		// given
 		mockReinforcement(topicDao, "People & Places", topicItemDao, "Albert Einstein worked in a patent office for a bit.");
 		initUiForTests();
 		
 		// when
 		ThinletComponent topicComponent = $("trTopics").getRootNode().withText("People & Places");
 		topicComponent.expand();
 		topicComponent.getSubNode().withText("Albert Einstein worked in a patent office for a bit.").select();
 		$("btEditTreeItem").click();
 		
 		// then
 		assertTrue($("dgEditReinforcement").isVisible());
 		assertEquals("People & Places", $("dgEditReinforcement").find("cbTopic").getText());
 		assertEquals("Albert Einstein worked in a patent office for a bit.",
 				$("taText").getText());
 	}
 	
 	public void testNewQuestionButton() {
 		// given
 		mockTopics(topicDao, "Random Topic 1", "Random Topic 2");
 		initUiForTests();
 
 		// when
 		$("btNewQuestion").click();
 		// then
 		assertEquals("plugins.learn.question.new", $("dgEditQuestion").getText());
 	}
 	
 	public void testNewQuestionButtonDisabledWhenNoTopics() {
 		assertFalse($("btNewQuestion").isEnabled());
 	}
 	
 	public void testEditTopicButtonIsVisible() {
 		$("btEditTreeItem").exists();
 	}
 	
 	public void testEditTopicButtonIsDisabledByDefault() {
 		assertFalse($("btEditTreeItem").isEnabled());
 	}
 	
 	public void testEditTopicButtonIsEnabledWhenATopicIsSelected() {
 		// given
 		mockTopics(topicDao, "Random Topic 1", "Random Topic 2");
 		initUiForTests();
 		
 		// when
 		$("trTopics").setSelected("Random Topic 2");
 		
 		// then
 		assertTrue($("btEditTreeItem").isEnabled());
 	}
 	
 	public void testEditTopicButtonLaunchesEditTopicWindow() {
 		// given
 		mockTopics(topicDao, "Random Topic 1", "Random Topic 2");
 		initUiForTests();
 		
 		// when
 		$("trTopics").setSelected("Random Topic 2");
 		$("btEditTreeItem").click();
 		
 		// then
 		$("btEditTreeItem").exists();
 	}
 	
 	public void testEditQuestionTrigger() {
 		// given
 		Question q = mockQuestion(topicDao, "People & Places",
 				topicItemDao, "Which instrument is the best?", "Trumpet", "Violin", "Banjo");
 		initUiForTests();
 		
 		// when
 		ThinletComponent topicComponent = $("trTopics").getRootNode().withText("People & Places");
 		topicComponent.expand();
 		ThinletComponent questionComponent = topicComponent.getSubNode().withAttachment(q);
 		questionComponent.select();
 		questionComponent.doubleClick();
 		
 		// then
 		assertTrue($("dgEditQuestion").isVisible());
 		assertEquals("People & Places", $("dgEditQuestion").find("cbTopic").getText());
 		assertEquals("Which instrument is the best?", $("tfQuestion").getText());
 	}
 	
 	public void testEditQuestionWhenEditButtonClicked() {
 		// given
 		Question q = mockQuestion(topicDao, "People & Places",
 				topicItemDao, "Which instrument is the best?", "Trumpet", "Violin", "Banjo");
 		initUiForTests();
 		
 		// when
 		ThinletComponent topicComponent = $("trTopics").getRootNode().withText("People & Places");
 		topicComponent.expand();
 		topicComponent.getSubNode().withAttachment(q).select();
 		$("btEditTreeItem").click();
 		
 		// then
 		assertTrue($("dgEditQuestion").isVisible());
 		assertEquals("People & Places", $("dgEditQuestion").find("cbTopic").getText());
 		assertEquals("Which instrument is the best?", $("tfQuestion").getText());
 	}
 
 	public void testDeleteButtonDisabledWhenNothingSelected() {
 		assertFalse($("btDeleteTreeItem").isEnabled());
 	}
 	
 	public void testDeleteButtonEnabledWhenTopicSelected() {
 		// given
 		mockTopics(topicDao, "test-delete-topic");
 		initUiForTests();
 		
 		// when
 		$("trTopics").setSelected("test-delete-topic");
 		
 		// then
 		assertTrue($("btDeleteTreeItem").isEnabled());
 	}
 	
 	public void testClickingDeleteButtonDisplaysConfirmationScreen() {
 		// given
 		mockTopics(topicDao, "test-delete-topic");
 		initUiForTests();
 		$("trTopics").setSelected("test-delete-topic");
 		
 		// when
 		$("btDeleteTreeItem").click();
 		
 		// then
 		assertTrue($("confirmDialog").isVisible());
 	}
 	
 	public void testConfirmingDeleteCausesCorrectTopicToBeDeleted() {
 		// given
 		mockTopics(topicDao, "test-delete-topic");
 		initUiForTests();
 		$("trTopics").setSelected("test-delete-topic");
 		$("btDeleteTreeItem").click();
 		
 		// when
 		$("confirmDialog").find("btContinue").click();
 		
 		// then
 		verify(topicDao).delete(topicWithName("test-delete-topic"));
 		assertFalse($("confirmDialog").isVisible());
 	}
 	
 	public void testConfirmingDeleteCausesCorrectReinforcementToBeDeleted() throws Exception {
 		// given
 		Reinforcement r = mockReinforcement(topicDao, "reinforcement-owner",
 				topicItemDao, "Don't forget!");
 		initUiForTests();
 		$("trTopics").getRootNode().withText("reinforcement-owner").expand();
 		$("trTopics").getSubNode().withAttachment(r).select();
 		$("btDeleteTreeItem").click();
 		
 		// when
 		$("confirmDialog").find("btContinue").click();
 		
 		// then
 		verify(topicItemDao).delete(r);
 		assertFalse($("confirmDialog").isVisible());
 	}
 	
 	public void testConfirmingDeleteCausesCorrectQuestionToBeDeleted() throws Exception {
 		// given
 		Question q = mockQuestion(topicDao, "question-owner",
 				topicItemDao, "Should you forget?", "Yes!", "No!");
 		initUiForTests();
 		$("trTopics").getRootNode().withText("question-owner").expand();
 		$("trTopics").getSubNode().withAttachment(q).select();
 		$("btDeleteTreeItem").click();
 		
 		// when
 		$("confirmDialog").find("btContinue").click();
 		
 		// then
 		verify(topicItemDao).delete(q);
 		assertFalse($("confirmDialog").isVisible());
 	}
 }
