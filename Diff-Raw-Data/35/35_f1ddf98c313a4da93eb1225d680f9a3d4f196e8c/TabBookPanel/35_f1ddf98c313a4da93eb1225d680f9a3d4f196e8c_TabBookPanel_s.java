 package presentation.view;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.util.Observable;
 
 import javax.swing.JLabel;
 
 import presentation.model.ActionPanelModel;
 import presentation.model.TabBookPanelModel;
 import util.TextUtils;
 
 public class TabBookPanel extends TabAbstractPanel {
 
 	private static final String TITLE_FORMAT = "<html><p style='font-size:14pt; padding-left: 1.25cm; text-indent: -1cm;'>";
 	private static final String NO_BOOK_ACTIVE_TEXT = "Kein Buch ausgewählt, bitte unter Recherche ein Buch suchen und auswählen, um seine Details hier anzuzeigen.";
 	private static final long serialVersionUID = 1L;
 	private TabBookPanelModel model;
 	private JLabel detailPanel;
 
 	public TabBookPanel(ActionPanelModel action_panel_model) {
 		super(action_panel_model);
		setLayout(new BorderLayout());
		detailPanel = new JLabel(NO_BOOK_ACTIVE_TEXT);
 		model = new TabBookPanelModel();
 		model.addObserver(this);
 	}
 
 	public TabBookPanelModel getModel() {
 		return model;
 	}
 
 	public void update(Observable o, Object arg) {
 		detailPanel.setText(getBookDetailPanelText());
		add(detailPanel, BorderLayout.NORTH);
 	}
 
 	private String getBookDetailPanelText() {
 		if (model.getActiveBook() == null)
 			return NO_BOOK_ACTIVE_TEXT;
 		return TITLE_FORMAT
 				+ "<b>"
 				+ formatTitle(TextUtils.cutText(model.getActiveBook().getTitle().getName(),
 						getWidth(), TITLE_FORMAT + "<b>")) + "</b><br />Autor: "
 				+ model.getActiveBook().getTitle().getAuthor()
 				+ "<br />Verlag: "
 				+ model.getActiveBook().getTitle().getPublisher() + "</p>";
 	}
 
 	private String formatTitle(String title) {
 		return TITLE_FORMAT + "<b>" + title + "</b>";
 	}
 
 	/**
 	 * Dynamically adapt title length to size of list by telling the cell
 	 * renderer its size.
 	 */
 	public void paint(Graphics g) {
 		detailPanel.setText(getBookDetailPanelText());
 		super.paint(g);
 	}
 }
