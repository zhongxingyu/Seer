 package com.argility.centralpages.ui;
 
 import org.apache.log4j.Logger;
 
 import com.argility.centralpages.CentralpagesApplication;
 import com.argility.centralpages.ui.table.AbstractTable;
 import com.vaadin.data.Container;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.event.ShortcutAction.KeyCode;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.NativeSelect;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.VerticalSplitPanel;
 import com.vaadin.ui.themes.Runo;
 
 @SuppressWarnings("serial")
 public class AbstractVerticalSplitPanel extends VerticalSplitPanel {
 
 	protected transient Logger log = Logger
 			.getLogger(this.getClass().getName());
 
 	private Table searchTable;
 	private TextField searchField = new TextField();
 	private TextField selectSearchField = new TextField();
 
 	public void createSingleColumnSearchableTable(Table table,
 			String searchProp, String searchPrompt) {
 		searchTable = table;
 
 		VerticalLayout vl = new VerticalLayout();
 		vl.setSizeFull();
 		vl.setMargin(false);
 		vl.setSpacing(false);
 
 		searchField = new TextField();
 
 		searchField.setInputPrompt(searchPrompt);
 		searchField.setColumns(22);
 		searchField.setData(searchProp);
 		searchField.setImmediate(true);
 
 		vl.addComponent(searchField);
 		vl.addComponent(searchTable);
 
 		vl.setExpandRatio(searchTable, 1f);
 
 		searchField.addListener(new ValueChangeListener() {
 
 			public void valueChange(ValueChangeEvent event) {
 				applySearch(searchField);
 
 			}
 		});
 
 		setFirstComponent(vl);
 		setSplitPosition(100);
 	}
 
 	protected void createSelectSearchableTable(Table table, String[] searchProps) {
 		createSelectSearchableTable(table, searchProps, null);
 	}
 		
 	protected void createSelectSearchableTable(Table table,
 			String[] searchProps, String[] searchCaptions) {
 		searchTable = table;
 
 		Component comp = createSearchComponent(searchProps, searchCaptions);
 
 		setFirstComponent(comp);
 		setSplitPosition(100);
 	}
 
 	private Component createSearchComponent(String[] searchProps,
 			String[] searchCaptions) {
 		VerticalLayout vl = new VerticalLayout();
 		vl.setSizeFull();
 		vl.setMargin(false);
 		vl.setSpacing(false);
 
 		selectSearchField.setValue("");
 		selectSearchField
 				.setInputPrompt("Select filter column and enter filter value.");
 
 		HorizontalLayout searchLayout = new HorizontalLayout();
 
 		final NativeSelect select = new NativeSelect();
 		// final Select select = new Select("select");
 		for (int i = 0; i < searchProps.length; i++) {
 			String item = searchProps[i];
 			select.addItem(item);
 			select.setNullSelectionAllowed(false);
 			if (searchCaptions != null && searchCaptions.length >= i) {
 				select.setItemCaption(searchProps[i], searchCaptions[i]);
 			}
 		}
 
 		select.setValue(searchProps[0]);
 		select.setColumns(20);
 
 		final Button doFilter = new Button("Apply Filter");
 		doFilter.addListener(new ClickListener() {
 
 			public void buttonClick(ClickEvent event) {
 				String field = (String) select.getValue();
 				selectSearchField.setData(field);
 				applySearch(selectSearchField);
 			}
 		});
 
 		select.setSizeFull();
 		select.focus();
 
 		selectSearchField.setColumns(20);
 		doFilter.setIcon(new ThemeResource(CentralpagesApplication.SEARCH_ICON));
 		doFilter.addStyleName(Runo.BUTTON_SMALL);
 		doFilter.setClickShortcut(KeyCode.ENTER);
 
 		searchLayout.setMargin(false);
 		searchLayout.setSpacing(false);
 
 		searchLayout.addComponent(select);
 		searchLayout.addComponent(selectSearchField);
 		searchLayout.addComponent(doFilter);
 
 		vl.addComponent(searchLayout);
 		vl.addComponent(searchTable);
 
 		vl.setExpandRatio(searchTable, 1f);
 
 		return vl;
 	}
 
 	protected void applySearch(TextField field) {
 		if (searchTable == null) {
 			return;
 		}
 
 		log.info("Applying search filter '" + field.getValue() + "' on table "
 				+ searchTable.getClass().getSimpleName() + " on column '"
 				+ field.getData() + "'");
 
 		Container c = searchTable.getContainerDataSource();
 		if (c instanceof BeanItemContainer<?>) {
 			((BeanItemContainer<?>) c).removeAllContainerFilters();
 
 			Class<?> type = searchTable.getContainerDataSource().getType(
 					field.getData());
 			String searchValue = getSearchValue(field, type);
 
 			((BeanItemContainer<?>) c).addContainerFilter(field.getData(),
 					searchValue, true, false);
 		}
 
 		if (searchTable instanceof AbstractTable) {
 			((AbstractTable) searchTable).addCountFooter();
 		}
 	}
 
 	private String getSearchValue(Field field, Class<?> colType) {
 		String searchValue = field.getValue() + "";
 
		log.info("Cal type is " + colType);
 		if (colType == Boolean.class) {
 			if ("yes".equalsIgnoreCase(searchValue)) {
 				searchValue = "true";
 			} else if ("no".equalsIgnoreCase(searchValue)) {
 				searchValue = "false";
 			}
		} else if (colType == java.util.Date.class) {
			searchValue = searchValue.replaceAll("/", "-");
 		}
 
 		return searchValue;
 	}
 	
 	protected String[] toStringArray(Object[] arr) {
 		String[] strArr = new String[arr.length];
 		
 		for (int i = 0; i < arr.length; i++) {
 			strArr[i] = arr[i]+"";
 		}
 		
 		return strArr;
 	}
 }
