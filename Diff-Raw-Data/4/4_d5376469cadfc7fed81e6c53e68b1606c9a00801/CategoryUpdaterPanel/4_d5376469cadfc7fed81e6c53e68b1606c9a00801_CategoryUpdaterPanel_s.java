 package net.premereur.mvp.example.swing.view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import net.premereur.mvp.core.View;
 import net.premereur.mvp.example.domain.model.Category;
 import net.premereur.mvp.example.swing.eventbus.DemoEventBus;
 import net.premereur.mvp.example.swing.presenter.CategoryUpdatePresenter;
 
 public class CategoryUpdaterPanel extends JPanel implements View {
 
 	private static final long serialVersionUID = 1L;
 	private JLabel operationLabel;
	private JButton cancelButton = new JButton("cancel");
	private JButton operationButton = new JButton("save");
 	private JTextField nameField;
 	private Category category;
 	private DemoEventBus eventBus;	
 
 	public CategoryUpdaterPanel() {
 		init();
 	}
 
 	private void init() {
 		operationLabel = new JLabel("Category detail");
 		JPanel fieldPane = new JPanel();
 		fieldPane.add(new JLabel("name"));
 		nameField = new JTextField(20);
 		fieldPane.add(nameField);
 		JPanel buttonPane = new JPanel();
 		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
 		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		buttonPane.add(Box.createHorizontalGlue());
 		buttonPane.add(operationButton);
 		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
 		buttonPane.add(cancelButton);
 
 		setLayout(new BorderLayout());
 		add(operationLabel, BorderLayout.PAGE_START);
 		add(fieldPane, BorderLayout.CENTER);
 		add(buttonPane, BorderLayout.PAGE_END);
 		
 		setCancelButtonListener();
 	}
 
 	public DemoEventBus getEventBus() {
 		return eventBus;
 	}
 
 	public void bind(Category selectedCategory) {
 		this.category = selectedCategory;
 		nameField.setText(this.category.getName());
 		operationLabel.setText("Change category");
 		operationButton.setText("Save");
 	}
 
 	public void setOperationButtonListener(final CategoryUpdatePresenter presenter) {
 		this.operationButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				category.setName(nameField.getText());
 				presenter.updateCategory(category);
 			}
 		});
 	}
 
 	protected void setCancelButtonListener() {
 		this.cancelButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				nameField.setText(category.getName());
 			}
 		});
 	}
 }
