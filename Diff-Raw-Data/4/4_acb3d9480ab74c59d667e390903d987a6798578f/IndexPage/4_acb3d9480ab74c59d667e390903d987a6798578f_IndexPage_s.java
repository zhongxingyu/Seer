 package ru.skalodrom_rf.web.pages;
 
 import org.apache.wicket.datetime.StyleDateConverter;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
 import org.apache.wicket.extensions.yui.calendar.DatePicker;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import ru.skalodrom_rf.dao.ScalodromDao;
import ru.skalodrom_rf.model.Query;
 import ru.skalodrom_rf.model.Scalodrom;
 import ru.skalodrom_rf.web.HibernateModel;
 import ru.skalodrom_rf.web.HibernateModelList;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * Homepage.
  */
 public class IndexPage extends BasePage {
     @SpringBean
     ScalodromDao scalodromDao;
 
     private final Date date = new Date();
 
     public IndexPage() {
         final Model dateModel = new Model(new Date());
         final List<Scalodrom> list = scalodromDao.findAll();
         final HibernateModel<Scalodrom,Long> skalModel = new HibernateModel<Scalodrom,Long>(list.get(0));
 
        final Form<Query> form = new Form<Query>("form"){
             @Override
             protected void onSubmit() {
                 //go to search page
             }
         };
 
 
         add(form);
         
         ChoiceRenderer<Scalodrom> choiceRenderer = new ChoiceRenderer<Scalodrom>("name", "name");
 
         final HibernateModelList<Scalodrom, Long> modelList = new HibernateModelList<Scalodrom, Long>(list);
         final DropDownChoice dropDownChoice = new DropDownChoice<Scalodrom>("scalodrom", modelList, choiceRenderer);
 
         dropDownChoice.setModel(skalModel);
         form.add(dropDownChoice);
 
         DateTextField dateTextField = new DateTextField("date",dateModel, new StyleDateConverter("S-", true));
         DatePicker datePicker = new DatePicker();
         datePicker.setShowOnFieldClick(true);
         dateTextField.add(datePicker);
         form.add(dateTextField);
 
         form.add(new Button("submit"));
     }
 
     /**
 	 * Constructor that is invoked when page is invoked without a session.
 	 *
 	 * @param parameters
 	 *            Page parameters
 	 */
 //    public IndexPage(final PageParameters parameters) {
 //
 //    }
 }
