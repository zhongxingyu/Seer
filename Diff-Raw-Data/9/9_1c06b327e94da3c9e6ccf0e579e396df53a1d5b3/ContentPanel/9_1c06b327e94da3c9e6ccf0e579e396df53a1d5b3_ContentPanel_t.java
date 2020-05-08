 package org.gwtapp.ccalc.client.ui;
 
 import java.util.Date;
 
 import org.gwtapp.ccalc.client.CCalc;
 import org.gwtapp.ccalc.client.ui.book.BookPanel;
 import org.gwtapp.ccalc.rpc.data.book.Book;
 import org.gwtapp.core.client.SimpleAsyncCallback;
 import org.gwtapp.template.client.Param;
 import org.gwtapp.template.client.UiHandler;
 import org.gwtapp.template.client.handler.WidgetHandler;
 import org.gwtapp.template.client.ui.TemplatePanel;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class ContentPanel extends TemplatePanel<Book> {
 
 	private final WidgetHandler submit = new WidgetHandler();
 	private final WidgetHandler backup = new WidgetHandler();
 	private final WidgetHandler backupLabel = new WidgetHandler();
	private final WidgetHandler backupInProgress = new WidgetHandler();
 	private final UiHandler<BookPanel> bookPanel = //
 	new UiHandler<BookPanel>(new BookPanel());
 
 	private final AsyncCallback<Void> backupCallback = new SimpleAsyncCallback<Void>() {
 		@Override
 		public void onSuccess(Void result) {
 			onBackupSuccess();
 		}
 	};
 
 	public ContentPanel() {
 		super(CCalc.templateService.load("ContentPanel.jsp"));
 		add("backupButton", backup);
 		add("backupLabel", backupLabel);
 		add("submitButton", submit);
 		add("bookPanel", bookPanel);
		add("backupInProgress", backupInProgress);
 		setVisible(false);
 	}
 
 	@Override
 	public void onAddWidgets() {
 		onBackupLabel();
 		submit.getWidget().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				Book value = bookPanel.getWidget().getValue();
 				value.setCurrentVersion(Book.CURRENT_VERSION.def());
 				CCalc.ccalc.backup(value, create(backupCallback));
 				CCalc.downloader.download(value,
 						create(new SimpleAsyncCallback<Void>()));
 			}
 		});
 		backup.getWidget().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				onBackupInProgress();
 				CCalc.ccalc.backup(bookPanel.getWidget().getValue(),
 						create(create(backupCallback)));
 			}
 		});
 	}
 
 	@Override
 	public Book getValue() {
 		return bookPanel.getWidget().getValue();
 	}
 
 	@Override
 	public void setValue(Book value, boolean fireEvents) {
 		setVisible(true);
 		onBackupLabel();
 		bookPanel.getWidget().setValue(value, fireEvents);
 	}
 
 	protected void onBackupSuccess() {
		backupInProgress.getWidget().setVisible(false);
 		String format = backupLabel.getMessage("format");
 		String date = DateTimeFormat.getFormat(format).format(new Date());
 		Param param = new Param("date", date);
 		String msg = backupLabel.getParamMessage("successLabel", param);
 		backupLabel.getWidget().setHTML(msg);
 	}
 
 	protected void onBackupLabel() {
		backupInProgress.getWidget().setVisible(false);
 		backupLabel.getWidget().setHTML(backupLabel.getMessage("label"));
 	}
 
 	protected void onBackupInProgress() {
		backupInProgress.getWidget().setVisible(true);
 		backupLabel.getWidget().setHTML(backupLabel.getMessage("inprogress"));
 	}
 }
