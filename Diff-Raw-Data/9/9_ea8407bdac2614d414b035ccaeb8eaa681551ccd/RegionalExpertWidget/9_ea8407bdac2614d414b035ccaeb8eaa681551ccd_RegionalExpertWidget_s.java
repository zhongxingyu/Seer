 package org.iucn.sis.client.api.panels;
 
 import java.util.ArrayList;
 
 import org.iucn.sis.shared.api.criteriacalculator.RegionalExpertQuestions;
 import org.iucn.sis.shared.api.debug.Debug;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.ListBox;
 
 public class RegionalExpertWidget extends LayoutContainer {
 
 	private final RegionalExpertQuestions questions;
 	
 	private HTML displayResult;
 	private VerticalPanel results;
 	private LayoutContainer tableEmulator;
 	
 	private ArrayList<QuestionRow> answers;
 	
 	private String result;
 	private ListBox amount;
 
 	public RegionalExpertWidget() {
 		super(new RowLayout(Orientation.VERTICAL));
 		
 		result = "";
 		
 		results = new VerticalPanel();
 		results.setSpacing(4);
 
 		tableEmulator = new LayoutContainer();
 		tableEmulator.setLayout(new RowLayout(Orientation.VERTICAL));
 
 		questions = new RegionalExpertQuestions();
 		
 		answers = new ArrayList<RegionalExpertWidget.QuestionRow>();
 		
 		displayResult = new HTML();
 		displayResult.addStyleName("redFont");
 		
 		amount = new ListBox();
 		amount.addItem("--- Select ---", "0");
 		amount.addItem("1", "1");
 		amount.addItem("2", "2");
 		
 		add(tableEmulator, new RowData(1, -1));
 		add(new Html("<hr>"), new RowData(1, -1));
 		add(results, new RowData(1, -1));
 	}
 	
 	/**
 	 * This gets called onChange of a listbox selection.
 	 * @param event event data about the question. 
 	 */
 	private void addNextQuestion(QuestionAnsweredEvent event) {
 		int index = event.getDataIndex();
 		
 		for (int i = answers.size() - 1; i > index; i--)
 			answers.remove(i);
 		
 		String nextQuestion = questions.getNextQuestion(event.getQuestion(), event.getAnswer());
 		if (!questions.isResult(nextQuestion))
 			addRow(nextQuestion, null);
 		
 		refresh();
 	}
 
 	private QuestionRow addRow(String question, Integer answer) {
 		QuestionRow row = new QuestionRow(answers.size(), question);
 		row.setValue(answer);
 		row.addListener(Events.Change, new Listener<QuestionAnsweredEvent>() {
 			public void handleEvent(QuestionAnsweredEvent be) {
 				addNextQuestion(be);
 			}
 		});
 		
 		answers.add(row);
 		
 		return row;
 	}
 
 	private void refresh() {
 		tableEmulator.removeAll();
 		result = "";
 		
 		if (answers.isEmpty())
 			addRow(questions.getFirstQuestion(), null);
 		
 		for (QuestionRow row : answers)
 			tableEmulator.add(row);
 		
 		QuestionRow row = answers.get(answers.size()-1);
 		String possibleResult = questions.getNextQuestion(row.question, row.getValueAsAnswer());
 		if (questions.isResult(possibleResult))
 			result = possibleResult;
 
 		boolean displayCategories = false;
 		if (RegionalExpertQuestions.DOWNGRADE.equalsIgnoreCase(result)) {
 			displayResult.setText("Downgrade category");
 			displayCategories = true;
 		} else if (RegionalExpertQuestions.UPGRADE.equalsIgnoreCase(result)) {
 			displayResult.setText("Upgrade category");
 			displayCategories = true;
 		} else if (RegionalExpertQuestions.NOCHANGE.equalsIgnoreCase(result)) {
 			displayResult.setText("No change in category");
 		} else {
 			displayResult.setText("(No result)");
 		}
 		
 		results.removeAll();
 		results.add(displayResult);
 
 		if (displayCategories) {
 			HorizontalPanel p = new HorizontalPanel();
 			p.setVerticalAlign(VerticalAlignment.BOTTOM);
 			p.add(new HTML("How many categories?&nbsp;"));
 			p.add(amount);
 			results.add(p);
 		} else {
 			amount.setSelectedIndex(-1);
 		}
 		
 		layout();
 	}
 
 	public void clearWidgetData() {
 		answers.clear();
 		
 		refresh();
 	}
 
 	public String getResultString() {
 		return result;
 	}
 
 	public String getWidgetData() {
 		String ret = getResultString();
 		if (ret == null)
 			ret = " ";
 		
 		ret += "," + amount.getSelectedIndex();
 
 		for (QuestionRow row : answers)
 			ret += "," + row.getValue();

		return ret.trim().equals("") ? null : ret;
 	}
 
 	public void setWidgetData(String data) {
 		if (data == null)
 			clearWidgetData();
 		else {
 			answers.clear();
 			result = "";
 			
 			String[] split = data.split(",");
 
 			String nextQuestion = questions.getFirstQuestion();
 			for (int i = 2; i < split.length; i++) {
 				QuestionRow row = addRow(nextQuestion, Integer.valueOf(split[i]));
 				if (row.getValueAsAnswer() == -1)
 					break;
 				else
 					nextQuestion = questions.getNextQuestion(row.question, row.getValueAsAnswer());
 			}
 
 			result = split[0].trim();
 			amount.setSelectedIndex(Integer.valueOf(split[1]));
 			
 			refresh();
 		}
 	}
 	
 	private static class QuestionRow extends HorizontalPanel {
 		
 		private final String question;
 		private final ListBox list;
 		private final int index;
 		
 		public QuestionRow(int index, String question) {
 			this.question = question;
 			this.index = index;
 			
 			this.list = new ListBox();
 			list.addItem("--- Select ---", "-1");
 			list.addItem("Yes", RegionalExpertQuestions.YES + "");
 			list.addItem("Don't Know", RegionalExpertQuestions.DONTKNOW + "");
 			list.addItem("No", RegionalExpertQuestions.NO + "");
 			list.addChangeHandler(new ChangeHandler() {
 				public void onChange(ChangeEvent event) {
 					fireEvent(Events.Change, new QuestionAnsweredEvent(QuestionRow.this));
 				}
 			});
 			
 			add(new Html((index+1) + ".  " + question));
 			add(list);
 		}
 		
 		public void setValue(Integer value) {
 			if (value == null || value == -1)
 				list.setSelectedIndex(0);
 			else {
 				try {
 					list.setSelectedIndex(value);
 				} catch (IndexOutOfBoundsException e){
 					Debug.println("Error, invalid index {0}, expected max of {1)", 
 						value, list.getItemCount());
 				}
 			}
 		}
 		
 		public Integer getValue() {
 			return list.getSelectedIndex();
 		}
 		
 		public Integer getValueAsAnswer() {
 			Integer answer;
 			try {
 				answer = Integer.valueOf(list.getValue(getValue()));
 			} catch (IndexOutOfBoundsException e) {
 				return -1;
 			} catch (NumberFormatException e) {
 				return -1;
 			}
 			
 			return answer;
 		}
 		
 	}
 	
 	private static class QuestionAnsweredEvent extends BaseEvent {
 		
 		private final String question;
 		private final Integer answer;
 		private final Integer dataIndex;
 		
 		public QuestionAnsweredEvent(QuestionRow row) {
 			super(Events.Change);
 			question = row.question;
 			answer = row.getValueAsAnswer();
 			dataIndex = row.index; 
 		}
 		
 		public String getQuestion() {
 			return question;
 		}
 		
 		public Integer getAnswer() {
 			return answer;
 		}
 		
 		public Integer getDataIndex() {
 			return dataIndex;
 		}
 		
 	}
 
 }
