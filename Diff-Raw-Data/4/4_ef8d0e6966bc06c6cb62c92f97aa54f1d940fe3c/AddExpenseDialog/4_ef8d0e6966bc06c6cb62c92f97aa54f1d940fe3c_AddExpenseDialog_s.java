 package com.svanberg.household.web.expense;
 
 import com.svanberg.household.domain.Category;
 import com.svanberg.household.domain.Expense;
 import com.svanberg.household.service.CategoryService;
 import com.svanberg.household.service.ExpenseService;
 import com.svanberg.household.web.components.EntityModel;
 import com.svanberg.household.web.components.InlineControlGroup;
 import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonBehavior;
 import de.agilecoders.wicket.markup.html.bootstrap.button.Buttons;
 import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
 import de.agilecoders.wicket.markup.html.bootstrap.form.FormBehavior;
 import de.agilecoders.wicket.markup.html.bootstrap.form.FormType;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.*;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Andreas Svanberg (andreass) <andreas.svanberg@mensa.se>
  */
 public class AddExpenseDialog extends Modal
 {
     private static final long serialVersionUID = -3888489317063097931L;
 
     @SpringBean private CategoryService categoryService;
     @SpringBean private ExpenseService expenseService;
 
     private final IModel<Date> date = new Model<>(new Date());
     private final IModel<Integer> cost = new Model<>();
     private final IModel<String> description = new Model<>();
     private final IModel<Category> category = new EntityModel<>(Category.class);
 
     public AddExpenseDialog(final String id)
     {
         super(id);
 
         header(new ResourceModel("title"));
 
         ExpenseForm form = new ExpenseForm(FORM);
         add(form);
 
         SubmitLabel create = new SubmitLabel(BUTTON_MARKUP_ID, new ResourceModel("create"), form);
         create.add(new ButtonBehavior(Buttons.Type.Primary));
         addButton(create);
 
         Label close = new Label(BUTTON_MARKUP_ID, new ResourceModel("cancel"));
         close.add(new ButtonBehavior(Buttons.Type.Default));
         close.add(new AttributeModifier("data-dismiss", "modal"));
         addButton(close);
     }
 
     private IModel<? extends List<? extends Category>> getCategories()
     {
         return new LoadableDetachableModel<List<? extends Category>>()
         {
             @Override
             protected List<? extends Category> load()
             {
                 return categoryService.findAll();
             }
         };
     }
 
     private class ExpenseForm extends StatelessForm<Void>
     {
         private static final long serialVersionUID = 9103097394820856360L;
 
         public ExpenseForm(final String id)
         {
             super(id);
 
             add(new FormBehavior(FormType.Horizontal));
 
             DateTextField dateInput = new DateTextField(INPUT, date) {
                 @Override
                 protected String getInputType()
                 {
                     return "date";
                 }
             };
             dateInput.setRequired(true);
             add(new InlineControlGroup(DATE, new ResourceModel("date")).add(dateInput));
 
             TextField<Integer> costInput = new TextField<Integer>(INPUT, cost, Integer.class) {
                 @Override
                 protected String getInputType()
                 {
                     return "number";
                 }
             };
             costInput.setRequired(true);
             add(new InlineControlGroup(COST, new ResourceModel("cost")).add(costInput));
 
             TextArea<String> descriptionInput = new TextArea<>(INPUT, description);
             descriptionInput.setRequired(true);
             add(new InlineControlGroup(DESCRIPTION, new ResourceModel("description")).add(descriptionInput));
 
             DropDownChoice<Category> categoryChoice = new DropDownChoice<>(INPUT, category, getCategories(),
                     new IChoiceRenderer<Category>()
             {
                 @Override
                 public Object getDisplayValue(final Category object)
                 {
                     return object.getName();
                 }
 
                 @Override
                 public String getIdValue(final Category object, final int index)
                 {
                     return String.valueOf(index);
                 }
             });
             categoryChoice.setNullValid(true);
             add(new InlineControlGroup(CATEGORY, new ResourceModel("category")).add(categoryChoice));
         }
 
         @Override
         protected void onSubmit()
         {
             Expense expense = expenseService.create(date.getObject(), description.getObject(), cost.getObject());
             expenseService.setCategory(expense, category.getObject());
             clearInput();
            date.setObject(null);
         }
 
         @Override
         protected void onError()
         {
             show(true);
             setFadeIn(false);
         }
     }
 
     private class SubmitLabel extends SubmitLink
     {
         private static final long serialVersionUID = -573817324922619136L;
 
         private SubmitLabel(final String id, final IModel<?> model, final Form<?> form)
         {
             super(id, model, form);
         }
 
         @Override
         public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
         {
             replaceComponentTagBody(markupStream, openTag, getDefaultModelObjectAsString());
         }
     }
 
     // Wicket component ids
     static final String FORM = "form";
     static final String DATE = "date";
     static final String COST = "cost";
     static final String DESCRIPTION = "description";
     static final String CATEGORY = "category";
     static final String INPUT = "input";
 }
