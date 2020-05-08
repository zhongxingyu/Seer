 package org.pillarone.riskanalytics.application.ui.resultnavigator.view;
 
 import com.ulcjava.base.application.*;
 import com.ulcjava.base.application.event.*;
 import com.ulcjava.base.application.tree.TreePath;
 import com.ulcjava.base.application.util.Color;
 import org.pillarone.riskanalytics.application.ui.resultnavigator.categories.CategoryMapping;
 import org.pillarone.riskanalytics.application.ui.resultnavigator.categories.ICategoryChangeListener;
 import org.pillarone.riskanalytics.application.ui.resultnavigator.categories.ICategoryResolver;
 import org.pillarone.riskanalytics.application.ui.resultnavigator.categories.resolver.CategoryResolverFactory;
 import org.pillarone.riskanalytics.application.ui.resultnavigator.categories.resolver.CategoryResolverTreeNode;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Dialog to inspect and possibly edit the CategoryMapping.
  *
  * @author martin.melchior
  */
 public class CategoryConfigurationDialog extends ULCDialog {
 
     private CategoryMapping categoryMapping;
 
     private ULCCardPane matcherTreePane;
     private ULCCardPane matcherConfigPane;
     // cache for a pane with the tree defined per category - each node in this tree consists of a resolver
     private Map<String,ULCScrollPane> resolverTreeCache;
     // cache that contains for each resolver an associated view
     private Map<ICategoryResolver,ULCBoxPane> resolverCache;
 
 
     public CategoryConfigurationDialog(ULCWindow parent) {
         super(parent);
 
         // look & feel, title, size, etc.
         boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
         if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
             setUndecorated(false);
             setWindowDecorationStyle(ULCDialog.INFORMATION_DIALOG);
         }
         setTitle("Configure Categories");
         setLocationRelativeTo(parent);
         setSize(800, 600);
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(new IWindowListener() {
             public void windowClosing(WindowEvent event) {
                 setVisible(false);
             }
         });
 
     }
 
     /**
      * Create the contents of the dialog for the given category mapping
      * @param categoryMapping
      */
     @SuppressWarnings("serial")
     public void createContent(final CategoryMapping categoryMapping) {
         this.categoryMapping = categoryMapping;
 
         // initialize the caches
         resolverTreeCache = new HashMap<String,ULCScrollPane>();
         resolverCache = new HashMap<ICategoryResolver,ULCBoxPane>();
 
         CategoryListModel categoryListModel = new CategoryListModel();
         categoryListModel.addAll(categoryMapping.getCategories());
 
         // initialize components
         ULCBoxPane contentPane = new ULCBoxPane(true);
         ULCBoxPane categoryListPane = new ULCBoxPane(true);
         categoryListPane.setBorder(BorderFactory.createTitledBorder("Categories"));
         CategoryList categoryList = new CategoryList();
         categoryList.setModel(categoryListModel);
         categoryList.setSelectedIndex(0);
         // ULCBoxPane addCategoryPane = new ULCBoxPane(false);
         // addCategoryPane.setBorder(BorderFactory.createTitledBorder("Add Category"));
         // final ULCTextField categorySpec = new ULCTextField(40);
         // categorySpec.setEditable(true);
         // ULCButton addCategoryButton = new ULCButton("Add");
         matcherTreePane = new ULCCardPane();
         matcherTreePane.setBorder(BorderFactory.createTitledBorder("How to Match Members"));
         matcherConfigPane = new ULCCardPane();
         matcherConfigPane.setBorder(BorderFactory.createTitledBorder("Matcher Specification"));
         ULCSplitPane matcherEditArea = new ULCSplitPane(ULCSplitPane.VERTICAL_SPLIT);
         matcherEditArea.setOneTouchExpandable(true);
         matcherEditArea.setDividerLocation(0.6);
         matcherEditArea.setDividerSize(10);
         ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.HORIZONTAL_SPLIT);
         splitPane.setOneTouchExpandable(true);
         splitPane.setDividerLocation(0.4);
         splitPane.setDividerSize(10);
 
 
         // layout them
         categoryListPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, categoryList);
         // addCategoryPane.add(ULCBoxPane.BOX_LEFT_BOTTOM, categorySpec);
         // addCategoryPane.add(ULCBoxPane.BOX_LEFT_BOTTOM, addCategoryButton);
         // categoryListPane.add(ULCBoxPane.BOX_LEFT_BOTTOM,addCategoryPane);
         matcherEditArea.setTopComponent(matcherTreePane);
         matcherEditArea.setBottomComponent(matcherConfigPane);
         splitPane.setLeftComponent(categoryListPane);
         splitPane.setRightComponent(matcherEditArea);
         contentPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, splitPane);
         this.add(contentPane);
         pack();
 
         // attach listeners
         categoryMapping.addCategoryChangeListener(categoryList);
         //addCategoryButton.addActionListener(new IActionListener() {
         //    public void actionPerformed(ActionEvent actionEvent) {
         //        String newCategory = categorySpec.getText();
         //        categoryMapping.addCategory(newCategory);
         //    }
         //});
         categoryList.addListSelectionListener(new IListSelectionListener() {
             public void valueChanged(ListSelectionEvent event) {
                 int index = ((ULCListSelectionModel) event.getSource()).getSelectedIndices()[0];
                 String selectedCategory = categoryMapping.getCategories().get(index);
                 showCategoryResolverTree(selectedCategory);
             }
         });
 
         // prepare the initial view - selected category in the list and associated resolver tree
        if (!categoryMapping.getCategories().isEmpty()) {
            String selectedCategory = categoryMapping.getCategories().get(0);
            showCategoryResolverTree(selectedCategory);
        }
     }
 
     /**
      * Create a tree pane for the category resolver
      * @param selectedCategory
      */
     private void showCategoryResolverTree(String selectedCategory) {
         // get the resolver associated with the selected category
         ICategoryResolver categoryResolver = categoryMapping.getCategoryResolver(selectedCategory);
 
         // check whether the tree (included in a scroll pane) is already available in the cache,
         // if no instantiate one - if yes get it from the cache
         if (!resolverTreeCache.containsKey(selectedCategory)){
             CategoryResolverTree categoryResolverTree = new CategoryResolverTree(categoryResolver);
             ULCScrollPane scrollPane = new ULCScrollPane(categoryResolverTree);
             matcherTreePane.addCard(selectedCategory, scrollPane);
             resolverTreeCache.put(selectedCategory, scrollPane);
         }
         ULCScrollPane newPane = resolverTreeCache.get(selectedCategory);
 
         // put this pane that contains the resolver tree in a ULCTree
         final ULCTree tree = (ULCTree) newPane.getViewPortView();
         matcherTreePane.setSelectedComponent(newPane);
 
         // attach a listener that allows to navigate within the tree and inspect the node in yet a separate pane
         tree.addActionListener(new IActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 TreePath selection = tree.getSelectionPath();
                 Object o = selection.getLastPathComponent();
                 if (o instanceof CategoryResolverTreeNode) {
                     showCategoryResolver(((CategoryResolverTreeNode) o).getResolver());
                 }
             }
         });
     }
 
     // Show the single category resolver
     public void showCategoryResolver(ICategoryResolver resolver) {
         // check whether the resolver view is already loaded in the cache
         // if no, instantiate and put it in the cache - if yes get the view from the cache
         if (!resolverCache.containsKey(resolver)) {
             ULCBoxPane view = CategoryResolverFactory.getMatcherView(resolver);
             if (view != null) {
                 matcherConfigPane.addCard(resolver.toString(), view);
                 resolverCache.put(resolver, view);
             }
         }
         ULCBoxPane view = resolverCache.get(resolver);
 
         // bring the selected resolver to the top in the card pane
         if (view != null) {
             matcherConfigPane.setSelectedComponent(view);
         }
     }
 
     public void addSaveActionListener(IActionListener listener) {
     }
 
     private class CategoryList extends ULCList implements ICategoryChangeListener {
 
         CategoryList() {
             super();
             setSelectionMode(ULCListSelectionModel.SINGLE_SELECTION);
             setSelectionBackground(Color.green);
         }
 
         public void categoryAdded(String category) {
         }
 
         public void categoryRemoved(String category) {
         }
     }
 
     private class CategoryListModel extends AbstractListModel implements IMutableListModel {
 
         public void add(Object o) {
             categoryMapping.addCategory((String) o);
             // fire change events ??
         }
 
         public void add(int i, Object o) {
             categoryMapping.addCategory((String) o);
         }
 
         public void addAll(Object[] objects) {
             for (Object o : objects) {
                 categoryMapping.addCategory((String) o);
             }
         }
 
         public void addAll(Collection collection) {
             for (Object o : collection) {
                 categoryMapping.addCategory((String) o);
             }
         }
 
         public void addAll(int i, Object[] objects) {
             for (Object o : objects) {
                 categoryMapping.addCategory((String) o);
             }
         }
 
         public void addAll(int i, Collection collection) {
             for (Object o : collection) {
                 categoryMapping.addCategory((String) o);
             }
         }
 
         public void clear() {
             categoryMapping.getMatcherMap().clear();
         }
 
         public Object remove(int i) {
             String category = categoryMapping.getCategories().get(i);
             boolean removed = categoryMapping.removeCategory(category);
             return removed ? category : null;
         }
 
         public boolean remove(Object o) {
             return categoryMapping.removeCategory((String)o);
         }
 
         public boolean removeAll(Object[] objects) {
             boolean test = true;
             for (Object o : objects) {
                 test = test && remove(o);
             }
             return test;
         }
 
         public boolean removeAll(Collection collection) {
             boolean test = true;
             for (Object o : collection) {
                 test = test && remove(o);
             }
             return test;
         }
 
         public Object set(int i, Object o) {
             categoryMapping.addCategory((String)o);
             return o;
         }
 
         public int getSize() {
             return categoryMapping.getNumberOfCategories();
         }
 
         public Object getElementAt(int i) {
             return categoryMapping.getCategories().get(i);
         }
     }
 }
