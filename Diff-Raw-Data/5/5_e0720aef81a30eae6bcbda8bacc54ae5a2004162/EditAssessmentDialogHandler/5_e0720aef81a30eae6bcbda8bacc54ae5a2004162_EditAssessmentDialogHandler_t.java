 package net.frontlinesms.plugins.learn.ui.assessment;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.GroupDao;
 import net.frontlinesms.plugins.learn.data.domain.Assessment;
 import net.frontlinesms.plugins.learn.data.domain.AssessmentMessage;
 import net.frontlinesms.plugins.learn.data.domain.TopicItem;
 import net.frontlinesms.plugins.learn.data.repository.AssessmentDao;
 import net.frontlinesms.plugins.learn.data.repository.TopicDao;
 import net.frontlinesms.plugins.learn.data.repository.TopicItemDao;
 import net.frontlinesms.plugins.learn.ui.assessment.message.EditAssessmentMessageDialogOwner;
 import net.frontlinesms.plugins.learn.ui.assessment.message.NewAssessmentMessageDialogHandler;
 import net.frontlinesms.plugins.learn.ui.topic.TopicChoosingDialogHandler;
 import net.frontlinesms.ui.FrontlineUI;
 import net.frontlinesms.ui.handler.contacts.GroupSelecterDialog;
 import net.frontlinesms.ui.handler.contacts.SingleGroupSelecterDialogOwner;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 public class EditAssessmentDialogHandler extends TopicChoosingDialogHandler<Assessment> implements SingleGroupSelecterDialogOwner, EditAssessmentMessageDialogOwner {
 	private final AssessmentDao assessmentDao;
 	private final GroupDao groupDao;
 	private final TopicItemDao topicItemDao;
 	
 	public EditAssessmentDialogHandler(FrontlineUI ui, AssessmentDao assessmentDao, GroupDao groupDao, TopicDao topicDao, TopicItemDao topicItemDao, Assessment a) {
 		super(ui, topicDao, a);
 		this.assessmentDao = assessmentDao;
 		this.groupDao = groupDao;
 		this.topicItemDao = topicItemDao;
 		
 		if(a.getGroup() != null) {
 			groupSelectionCompleted(a.getGroup());
 		}
 		
 		if(a.getTopic() != null) {
 			Object table = find("tbMessages");
 			List<TopicItem> topicItems = topicItemDao.getAllByTopic(a.getTopic());
 			for(TopicItem topicItem : topicItems) {
 				AssessmentMessage m = a.getMessage(topicItem);
 				if(m != null) ui.add(table, createTableRow(m));
 				else ui.add(table, createTableRow(topicItem));
 			}
 		}
		
		validate(null);
 	}
 
 	@Override
 	public String getLayoutFile() { return "/ui/plugins/learn/assessment/edit.xml"; }
 
 	@Override
 	public void save() {
 		editItem.setTopic(getSelectedTopic());
 
 		LinkedList<AssessmentMessage> messages = new LinkedList<AssessmentMessage>();
 		for(Object c : ui.getItems(find("tbMessages"))) {
 			Object attachment = ui.getAttachedObject(c);
 			if(attachment instanceof AssessmentMessage)
 				messages.add((AssessmentMessage) attachment);
 		}
 		editItem.setMessages(messages);
 		
 		assessmentDao.save(editItem);
 		
 		close();
 	}
 
 	@Override
 	public boolean doValidate(Object component) {
 		if(component != null && ui.getName(component).equals("cbTopic")) {
 			Object messageTable = find("tbMessages");
 			ui.removeAll(messageTable);
 			for(TopicItem t : topicItemDao.getAllByTopic(getSelectedTopic())) {
 				ui.add(messageTable, createTableRow(t));
 			}
 		}
 		
 		if(editItem.getGroup() == null) return false;
 		
 		if(!isTopicValid()) return false;
 		
 		Object messagesTable = find("tbMessages");
 		boolean messageSet = false;
 		for(Object row : ui.getItems(messagesTable)) {
 			if(ui.getAttachedObject(row) instanceof AssessmentMessage) {
 				messageSet = true;
 			}
 		}
 		if(!messageSet) return false;
 		
 		return true;
 	}
 	
 //> UI EVENT METHODS
 	public void editMessage(Object table) {
 		Object att = ui.getAttachedObject(ui.getSelectedItem(table));
 		if(att instanceof TopicItem) {
 			ui.add(new NewAssessmentMessageDialogHandler(ui, this, /*assessmentMessageDao, */(TopicItem) att).getDialog());
 		} else throw new RuntimeException();
 	}
 	
 	public void selectGroup() {
 		GroupSelecterDialog selecter = new GroupSelecterDialog(ui, this, groupDao);
 		selecter.init(new Group(null, null));
 		selecter.show();
 	}
 	
 //> UI HELPER METHODS
 	private Object createTableRow(TopicItem t) {
 		return ui.createTableRow(t, t.getMessageText());
 	}
 
 	private Object createTableRow(AssessmentMessage m) {
 		Long endDate = m.getEndDate();
 		return ui.createTableRow(m, m.getTopicItem().getMessageText(),
 				InternationalisationUtils.formatDate(m.getStartDate()),
 				InternationalisationUtils.getI18nString(m.getFrequency().getI18nKey()),
 				endDate==null?"":InternationalisationUtils.formatDate(endDate));
 	}
 	
 //> GROUP SELECTION METHODS
 	public void groupSelectionCompleted(Group g) {
 		editItem.setGroup(g);
 		ui.setText(find("tfGroup"), g.getName());
 	}
 	
 //> ASSESSMENT MESSAGE NOTIFICATION METHODS
 	public void notifyAssessmentMessageSaved(AssessmentMessage assessmentMessage) {
 		// TODO find the tableitem for the relevant topic item
 		Object table = find("tbMessages");
 		Object[] rows = ui.getItems(table);
 		for (int i = 0; i < rows.length; i++) {
 			Object row = rows[i];
 			Object attachment = ui.getAttachedObject(row);
 			TopicItem ti = null;
 			if(attachment instanceof TopicItem) {
 				ti = (TopicItem) attachment;
 			} else if (attachment instanceof AssessmentMessage) {
 				ti = ((AssessmentMessage) attachment).getTopicItem();
 			}
 			if(ti != null) {
 				ui.remove(row);
 				ui.add(table, createTableRow(assessmentMessage), i);
 			}
 		}
 		
 		validate(table);
 	}
 }
