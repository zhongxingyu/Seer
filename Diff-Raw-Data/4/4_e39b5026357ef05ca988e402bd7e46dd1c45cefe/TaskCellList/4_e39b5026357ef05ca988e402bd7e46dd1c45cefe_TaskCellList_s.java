 package de.oose.taskboard.client.widget;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.ui.Label;
 
 import de.oose.taskboard.shared.bo.TaskBO;
 import de.oose.taskboard.shared.enums.TaskVisibility;
 
 public class TaskCellList extends CellList<TaskBO> {
 
 	public TaskCellList() {
 		super(new TaskCell());
		setEmptyListWidget(new Label("nothing"));
 	}
 
 	static class TaskCell extends AbstractCell<TaskBO> {
 
 		interface Templates extends SafeHtmlTemplates {
 			@SafeHtmlTemplates.Template("<div style='{0}'><p class='tasktitle'><b>{1}</b></p><p class='taskdesc'>{2}</p></div>")
 			SafeHtml cell(String style, SafeHtml title, SafeHtml description);
 		}
 
 		private static Templates templates = GWT.create(Templates.class);
 
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context,
 				TaskBO value, SafeHtmlBuilder sb) {
 			if (value == null)
 				return;
 			SafeHtml title = SafeHtmlUtils.fromString(value.getTitle());
 			SafeHtml desc = SafeHtmlUtils.fromString(value.getDescription());
 			String style;
 			if (value.getVisibility().equals(TaskVisibility.PRIVATE)) {
 				style = "background-color: #D3D3D3;";
 			} else
 				style = "";
 			SafeHtml html = templates.cell(style, title, desc);
 			sb.append(html);
 
 		}
 
 	}
 }
