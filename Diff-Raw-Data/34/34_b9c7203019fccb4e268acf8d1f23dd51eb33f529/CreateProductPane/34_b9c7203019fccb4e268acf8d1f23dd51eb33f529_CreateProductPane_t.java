 package devopsdistilled.operp.client.items.panes;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.util.LinkedList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 import devopsdistilled.operp.client.abstracts.SubTaskPane;
import devopsdistilled.operp.client.items.exceptions.EntityNameExistsException;
import devopsdistilled.operp.client.items.exceptions.NullFieldException;
 import devopsdistilled.operp.client.items.models.observers.CategoryModelObserver;
 import devopsdistilled.operp.client.items.panes.controllers.CreateProductPaneController;
 import devopsdistilled.operp.client.items.panes.models.observers.CreateProductPaneModelObserver;
 import devopsdistilled.operp.server.data.entity.items.Category;
import devopsdistilled.operp.server.data.entity.items.Product;
 
 public class CreateProductPane extends SubTaskPane implements
 		CreateProductPaneModelObserver, CategoryModelObserver {
 
 	@Inject
 	private CreateProductPaneController controller;
 
 	private final JPanel pane;
 	private final JTextField productNameField;
 	private final JPanel categoryPanel;
 
 	DefaultListModel<Category> listModel;
 	private final JList<Category> categoryList;
 
 	public CreateProductPane() {
 		pane = new JPanel();
 		pane.setLayout(new MigLayout("", "[][grow]", "[][grow][]"));
 
 		JLabel lblProductName = new JLabel("Product Name");
 		pane.add(lblProductName, "cell 0 0,alignx trailing");
 
 		productNameField = new JTextField();
 		pane.add(productNameField, "cell 1 0,growx");
 		productNameField.setColumns(10);
 
 		JLabel lblCategory = new JLabel("Category");
 		pane.add(lblCategory, "cell 0 1");
 
 		categoryPanel = new JPanel();
 		categoryPanel.setLayout(new MigLayout("flowy", "[92px,grow]",
 				"[23px,grow]"));
 
 		JScrollPane scrollPane = new JScrollPane(categoryPanel);
 
 		categoryList = new JList<>();
 		categoryPanel.add(categoryList, "cell 0 0,grow");
 		pane.add(scrollPane, "cell 1 1,grow");
 
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				getDialog().dispose();
 			}
 		});
 		pane.add(btnCancel, "flowx,cell 1 2");
 
 		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<Category> categories = new LinkedList<>();
				categories.addAll(categoryList.getSelectedValuesList());
				Product product = new Product();
				String productName = productNameField.getText().trim();
				product.setProductName(productName);
				product.setCategories(categories);

				try {

					controller.validate(product);
					product = controller.save(product);

				} catch (NullFieldException e1) {
					JOptionPane.showMessageDialog(getPane(),
							"Product Name can't be empty");
				} catch (EntityNameExistsException e1) {
					JOptionPane.showMessageDialog(getPane(),
							"Product Name already exists");

				}
			}
		});
 		pane.add(btnCreate, "cell 1 2");
 	}
 
 	@Override
 	public JComponent getPane() {
 		return pane;
 	}
 
 	@Override
 	public void updateCategories(List<Category> categories) {
 		listModel = null;
 		listModel = new DefaultListModel<>();
 		for (Category category : categories) {
 			listModel.addElement(category);
 		}
 		categoryList.setModel(listModel);
 	}
 }
