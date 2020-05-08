 package presentation.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.io.File;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 
 import util.TextUtils;
 import domain.Book;
 import domain.Library;
 
 public class ResultCellRenderer implements ListCellRenderer {
 
 	private static final int TEXT_WIDTH_DIFFERENCE = 100;
 	private static final Color SHINY_BLUE = new Color(0xADD8E6);
 	private static final String TITLE_FORMAT = "<html><p style='font-size:14pt; padding-left: 1.25cm; text-indent: -1cm;'>";
 	private int preferredWidth;
 	private final Library library;
 
 	public ResultCellRenderer(Library library) {
 		this.library = library;
 	}
 
 	public Component getListCellRendererComponent(JList list, Object value,
 			int cellIndex, boolean isSelected, boolean cellHasFocus) {
 
 		int index = cellIndex;
 		if (index == -1) {
 			if (list.isSelectionEmpty())
 				return new JPanel();
 			index = list.getSelectedIndex();
 		}
 
		// TODO: Unterscheiden zwischen Book / Usern [Martin]
		Book selectedBook = ((Book) list.getModel().getElementAt(index));
 
 		RenderPanel c = new RenderPanel(selectedBook, isSelected);
 		c.setLayout(new BorderLayout());
 		JLabel l = new JLabel(getFormattedTitle(selectedBook));
 		String bookImage = "img" + File.separatorChar + "book.png";
 		l.setIcon(new ImageIcon(bookImage));
 		l.setFont(new Font(null, Font.PLAIN, 14));
 		c.add(l, BorderLayout.WEST);
 
 		c.setBackground(Color.WHITE);
 		if (isSelected)
 			c.setBackground(SHINY_BLUE);
 
 		return c;
 	}
 
 	private String getFormattedTitle(Book selectedBook) {
 		return TITLE_FORMAT
 				+ "<b>"
 				+ TextUtils.cutText(selectedBook.getTitle().getName(),
 						preferredWidth, TITLE_FORMAT + "<b>")
 				+ "</b><br />Autor: " + selectedBook.getTitle().getAuthor()
 				+ "<br />Verlag: " + selectedBook.getTitle().getPublisher();
 	}
 
 	public void setPreferredWidth(int width) {
 		this.preferredWidth = Math.max(100, width - TEXT_WIDTH_DIFFERENCE);
 	}
 
 	private class RenderPanel extends JPanel {
 		private static final long serialVersionUID = -8035455214107649755L;
 		private final Book active;
 		private final boolean isSelected;
 
 		public RenderPanel(Book active, boolean isSelected) {
 			this.active = active;
 			this.isSelected = isSelected;
 		}
 
 		@Override
 		public void paint(Graphics g) {
 			super.paint(g);
 			String image = "";
 			if (!library.isBookLent(active))
 				image = "img/check16x16.png";
 			if (active.getCondition().equals(Book.Condition.DAMAGED))
 				image = "img/warning16x16.png";
 			if (active.getCondition().equals(Book.Condition.WASTE))
 				image = "img/exclamation16x16.png";
 			g.drawImage(new ImageIcon(image).getImage(), 43, 38, null);
			
 			if (!isSelected)
 				return;
 			if (library.isBookLent(active)) {
 				g.drawImage(new ImageIcon("img/agenda32x32.png").getImage(),
 						getWidth() - 60, getHeight() - 37, null);
 				g.drawImage(new ImageIcon("img/return32x32.png").getImage(),
 						getWidth() - 100, getHeight() - 37, null);
 				return;
 			}
 			g.drawImage(new ImageIcon("img/add32x32.png").getImage(),
 					getWidth() - 60, getHeight() - 37, null);
 		}
 
 	}
 }
