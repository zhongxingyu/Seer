 package com.dhemery.victor.examples.pages;
 
 import com.dhemery.polling.PollTimer;
 import com.dhemery.victor.By;
 import com.dhemery.victor.IosApplication;
 import com.dhemery.victor.IosView;
 import org.hamcrest.Matchers;
 
 import java.util.List;
 
 import static com.dhemery.polling.Has.has;
 import static com.dhemery.victor.examples.extensions.ViewTapAction.tap;
 import static com.dhemery.victor.examples.extensions.ViewAnimatingMatcher.animating;
 import static com.dhemery.victor.examples.extensions.ViewTappableMatcher.tappable;
 import static com.dhemery.victor.examples.extensions.ViewVisibleMatcher.visible;
 import static com.dhemery.victor.examples.extensions.ViewListEmptyMatcher.empty;
 import static com.dhemery.victor.examples.extensions.ViewListSizeQuery.size;
 import static com.dhemery.victor.examples.extensions.TableCellConfirmDeletionAction.confirmDeletionOf;
 import static org.hamcrest.CoreMatchers.allOf;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.Matchers.*;
 import static org.hamcrest.core.Is.is;
 
 public class MasterPage extends Page {
     private static final By ADD_BUTTON = By.igor("UINavigationButton[accessibilityLabel=='Add']");
     private static final By DONE_BUTTON = By.igor("UINavigationButton[accessibilityLabel=='Done']");
     private static final By EDIT_BUTTON = By.igor("UINavigationButton[accessibilityLabel=='Edit']");
 
     private static final String DELETE_BUTTON_FOR_ITEM = "(%s) UITableViewCellEditControl";
     private static final By ITEM = By.igor("UITableViewCell*");
    private static final By ITEM_LABEL = By.igor(ITEM.selector + " UILabel");
    private static final String ITEM_WITH_LABEL = "(" + ITEM_LABEL.selector +"[accessibilityLabel=='%s'])";
 
     public MasterPage(IosApplication application, PollTimer timer) {
         super(application, timer);
     }
 
     private IosView addButton() {
         return view(ADD_BUTTON);
     }
 
     public void addItem() {
         addButton().sendMessage("tap");
     }
 
     public IosView deleteButtonAtRow(Integer i) {
 
         String itemBy = String.format(ITEM_WITH_LABEL, itemLabelAtRow(i));
         String buttonBy = String.format(DELETE_BUTTON_FOR_ITEM, itemBy);
         return view(By.igor(buttonBy));
     }
 
     public void deleteItemAtRow(Integer i) {
         IosView item = itemAtRow(i);
         tap(editButton());
 
         // The rows animate while they display the delete buttons.
         // We can't tap while it's animating.
         waitUntil(item, is(not(animating())));
         when(deleteButtonAtRow(i), is(tappable()), tap());
         confirmDeletionOf(item);
         waitUntil(item, is(not(visible())));
         tap(doneButton());
     }
 
     public IosView doneButton() {
         return view(DONE_BUTTON);
     }
 
     public IosView editButton() {
         return view(EDIT_BUTTON);
     }
 
     public IosView itemAtRow(Integer i) {
         return itemWithLabel(itemLabelAtRow(i));
     }
 
     private String itemLabelAtRow(Integer i) {
         waitUntil(items(), has(size(), greaterThan(i)));
         return itemLabels().get(i);
     }
 
     private List<String> itemLabels() {
         return view(ITEM_LABEL).sendMessage("accessibilityLabel");
     }
 
     private IosView itemWithLabel(String label) {
         return view(By.igor(String.format(ITEM_WITH_LABEL, label)));
     }
 
     public IosView items() {
         return view(ITEM);
     }
 
     public void visitItemAtRow(Integer i) {
         itemAtRow(i).sendMessage("tap");
     }
 
     public void deleteAllItems() {
         while(the(items(), Matchers.is(Matchers.not(empty())))) {
             deleteItemAtRow(0);
         }
 
     }
 }
