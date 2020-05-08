 package org.smartsnip.client;
 
 import java.util.List;
 
 import org.smartsnip.shared.ICategory;
 import org.smartsnip.shared.XCategory;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Provides static method to access and manipulate category settings..
  * 
  * The class iteself is a abstract implementation {@link PopupPanel} that is
  * implemented as inner classes to give generic access for creating and editing
  * snippets
  * 
  * @author Felix Niederwanger
  * 
  */
 public abstract class Category extends PopupPanel {
 
 	/** Item to be edited or null, if a new one */
 	private final XCategory original;
 
 	/* Components */
 	private final Panel rootPanel = new VerticalPanel();
 
 	private final Label lblTitle;
 
 	private final Grid grid;
 
 	private final Label[] lblDescs;
 	private final TextBox txtName = new TextBox();
 	private final TextBox txtDesc = new TextBox();
 	private final ListBox lstParent = new ListBox();
 
 	private final HorizontalPanel pnlToolbar = new HorizontalPanel();
 	private final Button btnApply;
 	private final Button btnClose;
 
 	private final Label lblStatus;
 
 	/* End of components */
 
 	/** No outer instances allowed */
 	private Category(String title, XCategory category) {
 		this.original = category;
 
 		lblTitle = new Label(title);
 		grid = new Grid(3, 2);
 		btnApply = new Button("Apply");
 		btnClose = new Button("Close");
 		lblStatus = new Label();
 
 		lblDescs = new Label[] { new Label("Name"), new Label("Description"), new Label("Parent") };
 
 		if (category != null)
 			txtName.setEnabled(false); // Editing a name is not yet supported by
 										// server!
 
 		for (int i = 0; i < lblDescs.length; i++)
 			grid.setWidget(i, 0, lblDescs[i]);
 		grid.setWidget(0, 1, txtName);
 		grid.setWidget(1, 1, txtDesc);
 		grid.setWidget(2, 1, lstParent);
 
 		if (category != null) {
 			txtName.setText(category.name);
 			txtDesc.setText(category.description);
 
 			// Set parent in callback
 		}
 
 		rootPanel.add(lblTitle);
 		rootPanel.add(grid);
 
 		btnApply.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				String name = txtName.getText();
 				String desc = txtDesc.getText();
 				String parent = lstParent.getItemText(lstParent.getSelectedIndex());
 
 				if (name.isEmpty() || desc.isEmpty() || parent.isEmpty()) {
 					lblStatus.setText("Recheck your arguments!");
 					return;
 				}
 
 				if (original != null)
 					if (name.equals(original.name) && desc.equals(original.description) && parent.equals(original.parent)) {
 						// No changes
 						lblStatus.setText("No changes set");
 						return;
 					}
 
 				final XCategory newCategory = new XCategory();
 				newCategory.name = name;
 				newCategory.description = desc;
 				newCategory.parent = parent;
 				newCategory.subcategories = null; // Ignored by server
 
 				try {
 					btnApply.setEnabled(false);
 					lblStatus.setText("Processing ... ");
 					onApply(newCategory);
 					close();
 				} catch (RuntimeException e) {
 					lblStatus.setText("Error: " + e.getMessage());
 				}
 			}
 		});
 		btnClose.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				close();
 			}
 		});
 
 		pnlToolbar.add(btnApply);
 		pnlToolbar.add(btnClose);
 
 		rootPanel.add(lblTitle);
 		rootPanel.add(grid);
 		rootPanel.add(pnlToolbar);
 		rootPanel.add(lblStatus);
 
 		setWidget(rootPanel);
 
 		applyStyles();
 		fetchCategories();
 	}
 
	/** Apply styles */
 	private void applyStyles() {
 		lblTitle.setStyleName("h3");
 		setStyleName("contact");
 	}
 
 	/** Closes the popup panel */
 	protected void close() {
 		this.hide();
 	}
 
 	/**
 	 * Sets the enabled status of the apply button
 	 * 
 	 * @param enabled
 	 *            true if enabled, false if disabled
 	 * */
 	protected void setApplyEnabled(boolean enabled) {
 		btnApply.setEnabled(enabled);
 	}
 
 	/**
 	 * Sets the status text
 	 * 
 	 * @param newStatus
 	 *            to be set
 	 */
 	protected void setStatus(String newStatus) {
 		if (newStatus == null)
 			return;
 		lblStatus.setText(newStatus);
 	}
 
 	/** Fetch categories */
 	private void fetchCategories() {
 		btnApply.setEnabled(false);
 		lstParent.clear();
 		lstParent.setEnabled(false);
 		lstParent.addItem("Fetching items ... ");
 		ICategory.Util.getInstance().getAllCategories(new AsyncCallback<List<XCategory>>() {
 
 			@Override
 			public void onSuccess(List<XCategory> result) {
 				if (result == null) {
 					onFailure(new IllegalArgumentException("Null returned"));
 				}
 				lstParent.clear();
 
 				for (XCategory cat : result)
 					lstParent.addItem(cat.name);
 
 				selectCategory();
 
 				lstParent.setEnabled(true);
 				btnApply.setEnabled(true);
 			}
 
 			/** Select category if */
 			private void selectCategory() {
 				if (original == null)
 					return;
 
 				int max = lstParent.getItemCount();
 				for (int i = 0; i < max; i++)
 					if (lstParent.getItemText(i).equals(original.name)) {
 						lstParent.setSelectedIndex(i);
 						return;
 					}
 				lstParent.addItem(original.name);
 				lstParent.setSelectedIndex(max);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				lstParent.clear();
 				lstParent.setEnabled(false);
 				lstParent.addItem("Error fetching categories");
 				btnApply.setEnabled(false);
 			}
 		});
 	}
 
 	/**
 	 * This method is called when the apply button is called. If the call goes
 	 * well, the popup assuems everything is OK.
 	 * 
 	 * In both cases this call does NOT close the popup
 	 * 
 	 * If a exception is thrown, the opup displays the message as error message,
 	 * and does not closes
 	 * 
 	 * @param category
 	 *            Edited category entity
 	 * @throws RuntimeException
 	 *             If thrown, the popup assumes that something happend and does
 	 *             not cloeses
 	 */
 	protected abstract void onApply(XCategory category) throws RuntimeException;
 
 	/**
 	 * Shows a popup for creating a new category
 	 */
 	public static void showCreateCategoryPopup() {
 		final Category popup = new Category("Create new category", null) {
 
 			@Override
 			protected void onApply(XCategory category) throws RuntimeException {
 				ICategory.Util.getInstance().createCategory(category, new AsyncCallback<Void>() {
 
 					@Override
 					public void onSuccess(Void result) {
 						close();
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						setApplyEnabled(true);
 						setStatus("Failure: " + caught.getMessage());
 					}
 				});
 			}
 		};
 		popup.show();
 	}
 
 	/**
 	 * Shows a popup for editing a given category. If null, nothing happens
 	 * 
 	 * @param category
 	 *            to be edited
 	 */
 	public static void showEditCategoryPopup(XCategory category) {
 		if (category == null)
 			return;
 
 		Category popup = new Category("Edit category " + category.name, null) {
 
 			@Override
 			protected void onApply(XCategory category) throws RuntimeException {
 				ICategory.Util.getInstance().edit(category.name, category, new AsyncCallback<Void>() {
 
 					@Override
 					public void onSuccess(Void result) {
 						close();
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						setApplyEnabled(true);
 						setStatus("Failure: " + caught.getMessage());
 					}
 				});
 			}
 		};
 		popup.show();
 	}
 }
