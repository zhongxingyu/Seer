 package gui;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import inter.Updater;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableCellRenderer;
 
 import data.TopTaskModel;
 import data.task.Task;
 import data.tasks.TaskMap;
 
 /**
  * ͨ
  * 
  * @author lqy
  * 
  * @param <E>
  *            ˱е
  */
 public class TopTaskTable<E extends Task> extends JTable implements
 		ActionListener {
 	private static final long serialVersionUID = 1L;
 
 	// ¼ǹʱ򴫽
 	/**
 	 * EClass,ûE,ֻò
 	 */
 	protected Class<E> taskClass;
 	/**
 	 * ༭Ի,޸ʱ򵯳
 	 */
 	protected TaskDialog dialog;
 	/**
 	 * 
 	 */
 	protected int contentCol;
 	/**
 	 * ͻʾ?<br>
 	 * ֻнܽݱӴ
 	 */
 	protected boolean canHighlight;
 	/**
 	 * ṩˢ½ӿ<br>
 	 * \ɾӦðµ²
 	 */
 	protected Updater updater;
 	/**
 	 * ˱һ<br>
 	 * ˱ݸĶͬϼˢ,һ,еı񶼻Զˢ
 	 */
 	protected TopTaskTable<? extends Task> father;
 	/**
 	 * ˱ģ<br>
 	 * ¼Ҫݻÿ
 	 */
 	protected TopTaskModel<E> model;
 
 	// ¼Լ
 	/**
 	 * ʾ?
 	 */
 	protected JCheckBoxMenuItem check;
 	/**
 	 * Ҽ˵,ѡкҼͬʱɾ
 	 */
 	protected JPopupMenu menu;
 	/**
 	 * ͨ
 	 */
 	protected final Font normalFont;
 	/**
 	 * 
 	 */
 	protected final Font finishedFont;
 	/**
 	 * ִе
 	 */
 	protected final Font runningFont;
 	/**
 	 * еȾ
 	 */
 	protected MyRender render;
 
 	/**
 	 * 
 	 * 
 	 * @param taskClass
 	 *            ע
 	 * @param dialog
 	 *            ༭Ի
 	 * @param contentCol
 	 *            ʾ
 	 * @param canHighlight
 	 *            ͻʾ
 	 * @param updater
 	 *            ṩˢ½ӿ
 	 * @param father
 	 *            ˱һ
 	 * @param model
 	 *            ˱ģ
 	 */
 	public TopTaskTable(Class<E> taskClass, TaskDialog dialog, int contentCol,
 			boolean canHighlight, Updater updater,
 			TopTaskTable<? extends Task> father, TopTaskModel<E> model) {
 		super(model);
 		this.taskClass = taskClass;
 		this.dialog = dialog;
 		this.contentCol = contentCol;
 		this.canHighlight = canHighlight;
 		this.updater = updater;
 		this.father = father;
 		this.model = model;
 
 		// Ҽ˵
 		buildMenu();
 		// release󵯳˵
 		this.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					menu.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					menu.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 
 		});
 
 		// deleteɾһ
 		// һֻѡһ
 		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		addKeyListener(new KeyAdapter() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
 					remove();
 					TopTaskTable.this.model.showTasks(!check.isSelected());
 					TopTaskTable.this.updater.update();
 				}
 			}
 
 		});
 
 		// 3
 		final int size = 12;
 		final String font = "΢ź";
 		this.normalFont = new Font(font, 0, size);
 		this.finishedFont = new Font(font, 0, size);
 		this.runningFont = new Font(font, Font.BOLD, size);
 
 		// Ⱦ
 		render = new MyRender();
 		this.getColumnModel().getColumn(contentCol).setCellRenderer(render);
 	}
 
 	/**
 	 * Ҽ˵
 	 */
 	protected void buildMenu() {
 		menu = new JPopupMenu();
 
 		// ˿ѡ˵
 		check = new JCheckBoxMenuItem("");
 		check.addActionListener(this);
 		menu.add(check);
 		menu.addSeparator();// һָ
 
 		// ˵
 		String[] cmds = { "", "޸", "ɾ" };
 		int i;
 		JMenuItem t;
 		for (i = 0; i < cmds.length; ++i) {
 			t = new JMenuItem(cmds[i]);
 			t.addActionListener(this);
 			menu.add(t);
 		}
 	}
 
 	/**
 	 * ڴе񼯺ϿѾ,¶ȡ񼯺ˢʾ<br>
 	 * ˱ĸҲиı,ݹˢ
 	 */
 	public void updateFromMem() {
 		model.showTasks(!check.isSelected());
 		if (father != null)
 			father.updateFromMem();
 	}
 
 	/**
 	 * ļе񼯺ϿѾ,ļȡ񼯺ˢʾ<br>
 	 * ˱ĸҲиı,ݹˢ
 	 */
 	public void updateFromFile() {
 		TaskMap<E, ? extends Task> tasks = model.getTasks();
 		tasks.readTasks();
 		model.showTasks(!check.isSelected());
 		if (father != null)
 			father.updateFromFile();
 	}
 
 	/**
 	 * ±<br>
 	 * <p>
 	 * ϲ,ť¼ֻһ,<br>
 	 * ͻõ
 	 */
 	public void updateJustMe() {
 		model.showTasks(!check.isSelected());
 	}
 
 	/**
 	 * ľ崦
 	 */
 	protected void add() {
 		TaskMap<E, ? extends Task> tasks = model.getTasks();
 		if (!dialog.showAddDialog(taskClass, tasks.getFathers()))// Իȡ,ݲ
 			return;
 		@SuppressWarnings("unchecked")
 		E newtask = (E) dialog.task;
 		tasks.add(newtask);
 	}
 
 	/**
 	 * ɾľ崦
 	 * 
 	 * @return ע񼯺ǽ񼯺,<br>
 	 *         ô޸ĿܻĶǰִе, ص޸ĺǰִе,<br>
 	 *         ֻʾıŹֵ
 	 */
 	protected String remove() {
 		String cur = model.getCur();
 		do {
 			if (this.getSelectedRow() < 0)// ûбѡ򷵻
 				break;
 
 			TaskMap<E, ? extends Task> tasks = model.getTasks();
 
 			// ,TOTALʾ󲢷
 			String info = (String) this.getValueAt(this.getSelectedRow(),
 					contentCol);
 			if (info.equals(TopTaskModel.TOTAL)) {
 				JOptionPane.showMessageDialog(this, "\"ܼ\"вɾ", "ɾܾ",
 						JOptionPane.ERROR_MESSAGE);
 				break;
 			}
 
 			if (!tasks.isTaskEditable(info)) {// ɾ򷵻
 				JOptionPane.showMessageDialog(this, "ɵɾ",
 						"ɾܾ", JOptionPane.ERROR_MESSAGE);
 				break;
 			}
 
 			tasks.remove(info);
 			if (cur != null && cur.equals(info))
 				cur = null;
 		} while (false);
 		return cur;
 	}
 
 	/**
 	 * ޸ľ崦<br>
 	 * 
 	 * @return ע񼯺ǽ񼯺,<br>
 	 *         ô޸ĿܻĶǰִе, ص޸ĺǰִе,<br>
 	 *         ֻʾıŹֵ
 	 */
 	@SuppressWarnings("unchecked")
 	protected String modify() {
 		String cur = model.getCur();
 		do {
 			if (this.getSelectedRow() < 0)// ûбѡ򷵻
 				break;
 
 			TaskMap<E, ? extends Task> tasks = model.getTasks();
 
 			// ,TOTALʾ󲢷
 			String info = (String) this.getValueAt(this.getSelectedRow(),
 					contentCol);
 			if (info.equals(TopTaskModel.TOTAL)) {
 				JOptionPane.showMessageDialog(this, "\"ܼ\"в޸", "޸ıܾ",
 						JOptionPane.ERROR_MESSAGE);
 				break;
 			}
 
 			E origin = tasks.get(info);
 			// ޸򷵻
 			if (!tasks.isTaskEditable(origin.info)) {
 				JOptionPane.showMessageDialog(this, "ɵ޸",
 						"޸ıܾ", JOptionPane.ERROR_MESSAGE);
 				break;
 			}
 
 			if (!dialog.showEditDialog(origin, tasks.getFathers()))// Իȡ,ݲ
 				break;
 			tasks.modify(dialog.modifyInfo, (E) dialog.task);
 
 			// ޸ĵǵǰ(ֻн񼯺ϲеǰһ),򷵻µĵǰ
 			if (cur != null && cur.equals(dialog.modifyInfo))
 				cur = dialog.task.info;
 		} while (false);
 		return cur;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String cmd = e.getActionCommand();
 		if (cmd.equals(""))
 			add();
 		else if (cmd.equals("޸"))
 			modify();
 		else if (cmd.equals("ɾ"))
 			remove();
 
		if (cmd.equals(""))
			this.updateJustMe();// "",ֻ´˱ʾ
		else
			this.updateFromMem();// ´˱ϸʾ
 
 		updater.update();
 	}
 
 	/**
 	 * 1ֵĵԪȾ
 	 * 
 	 * @author lqy
 	 * 
 	 */
 	protected class MyRender extends DefaultTableCellRenderer {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 			Component ans;
 			// ȡҪʾ
 			ans = super.getTableCellRendererComponent(table, value, isSelected,
 					hasFocus, row, column);
 
 			TaskMap<E, ? extends Task> tasks = model.getTasks();
 
 			String cur = model.getCur();
 			// "ܼ",Ⱦ
 			if (((String) value).equals(TopTaskModel.TOTAL))
 				;
 			// ǵǰ,Ӵ
 			else if (canHighlight && cur != null && cur.equals(value)) {
 				ans.setFont(TopTaskTable.this.runningFont);
 				ans.setForeground(Color.red);
 			}
 			// ,styleΪ0,ɫΪ
 			else if (tasks.get(value).finished) {
 				ans.setFont(TopTaskTable.this.finishedFont);
 				ans.setForeground(Color.gray);
 			}
 			// ͨδɵ,styleΪ0,ɫΪ
 			else {
 				ans.setFont(TopTaskTable.this.normalFont);
 				ans.setForeground(Color.black);
 			}
 			return ans;
 		}
 
 	}
 
 }
