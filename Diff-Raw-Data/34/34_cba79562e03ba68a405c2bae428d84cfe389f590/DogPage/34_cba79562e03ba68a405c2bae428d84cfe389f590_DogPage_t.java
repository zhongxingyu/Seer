 package fi.muni.pa165.web.pages;
 
 import fi.muni.pa165.dto.CustomerDto;
 import fi.muni.pa165.dto.DogDto;
 import fi.muni.pa165.service.CustomerService;
 import fi.muni.pa165.service.DogService;
 import fi.muni.pa165.web.DogBarberShopApplication;
import fi.muni.pa165.web.converter.LocalDateConverter;
import fi.muni.pa165.web.validator.LocalDateValidator;
 import java.util.List;
 import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.joda.time.LocalDate;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.joda.time.format.DateTimeFormat;
 
 
 
 /**
  * @author Jan Pacner
  */
 public class DogPage extends TemplatePage {
   /* HTML element IDs */
   private static final String FORM_LISTING    = "form_listing";
   private static final String SPAN_ROW00      = "span_row00";
   private static final String SPAN_NAME       = "span_name";
   private static final String SPAN_BREED      = "span_breed";
   private static final String SPAN_BIRTHDATE  = "span_birthdate";
   private static final String SPAN_OWNER      = "span_owner";
   private static final String BTN_DEL         = "btn_del";
   private static final String BTN_EDIT        = "btn_edit";
 
   private static final String FORM_DOG_WHOLE  = "form_dog_whole";
   private static final String INPUT_NAME      = "name";
   private static final String INPUT_BREED     = "breed";
   private static final String INPUT_BIRTHDATE = "birthDate";  // LocalDate
   private static final String SELECT_OWNER    = "owner";  // CustomerDto
   private static final String BTN_SUBMIT      = "btn_submit";
   private static final String H2_ACTION       = "h2_action";
 
   private final Label action;
   private final Form form_dog_whole;
   private final Form form_listing;
 
   private enum State {NEW_DOG, EDIT_DOG};
   private State st = State.NEW_DOG;
   private final DogService dogser;
   private final CustomerService custser;
   private RequiredTextField input_name;
   private RequiredTextField input_breed;
   //private RequiredTextField input_birthdate;
   private DateTextField input_birthdate;
   private DropDownChoice select_owner;
   private Button btn_submit;
 
   private static DogDto newDogDto() {
    return new DogDto();
   }
 
   public DogPage() {
     dogser = DogBarberShopApplication.get().getDogService();
     custser = DogBarberShopApplication.get().getCustomerService();
     
     final FeedbackPanel feedback = new FeedbackPanel("feedback");
         this.add(feedback);
 
     add(form_listing = new Form<DogDto>(FORM_LISTING, new Model<DogDto>()) {
       @Override
       protected void onInitialize() {
         super.onInitialize();
         setOutputMarkupId(true);
         add(new ListView<DogDto>(SPAN_ROW00,
                 new LoadableDetachableModel<List<DogDto>>() {
                   @Override
                   protected List<DogDto> load() {
                     return DogPage.this.dogser.getAllDogs();
                   }
                 })
         {
           @Override
           protected void populateItem(final ListItem<DogDto> li) {
             li.add(new Label(SPAN_NAME, li.getModelObject().getName()));
             li.add(new Label(SPAN_BREED, li.getModelObject().getBreed()));
             li.add(new Label(SPAN_BIRTHDATE, li.getModelObject().getBirthDate()));
             li.add(new Label(SPAN_OWNER,
                     li.getModelObject().getOwner().getSurname() + " " +
                     li.getModelObject().getOwner().getName()));
 
             li.add(new Button(BTN_DEL) {
               @Override
               public void onSubmit() {
                 super.onSubmit();
                 dogser.deleteDog(li.getModelObject());
               }
             });
 
             li.add(new Button(BTN_EDIT) {
               @Override
               public void onSubmit() {
                 super.onSubmit();
                 form_dog_whole.setModelObject(li.getModelObject());
                 st = State.EDIT_DOG;
                 action.setDefaultModelObject(DogPage.this.getLocalizer()
                         .getString("DogPage.action.edit_dog", DogPage.this));
               }
             });
           }
         });
       }
     });
 
     add(action = new Label(H2_ACTION, new Model()));
 
     form_dog_whole = new Form<DogDto>(FORM_DOG_WHOLE,
             new CompoundPropertyModel<>(newDogDto())) {
               @Override
               public void onSubmit() {
                 System.err.println("XXX HOWK 44");//FIXME
                 super.onSubmit();
               }
             };
     form_dog_whole.add(new RequiredTextField(INPUT_NAME));
     form_dog_whole.add(new RequiredTextField(INPUT_BREED));
    final TextField date = new TextField(INPUT_BIRTHDATE, LocalDate.class);
    date.add(new LocalDateValidator());
    date.add(new DatePicker() {
        @Override
            protected String getDatePattern() {
                String datePattern = super.getDatePattern();
                String actualPattern = DateTimeFormat.patternForStyle(LocalDateConverter.STYLE, this.getLocale());
                if (!actualPattern.equals(datePattern)) {
                    return actualPattern;
                }
                return datePattern;
            }
    });
    form_dog_whole.add(date);
     form_dog_whole.add(select_owner = new DropDownChoice(SELECT_OWNER,
             new Model<CustomerDto>(), custser.getAllCustomers(),
             new ChoiceRenderer<CustomerDto>("name", "id")));
     form_dog_whole.add(btn_submit = new Button(BTN_SUBMIT) {
       @Override
       public void onSubmit() {
         System.err.println("XXX HOWK 55");//FIXME
         super.onSubmit();
 
         switch (st) {
           case EDIT_DOG:
             st = State.NEW_DOG;
             action.setDefaultModelObject(DogPage.this.getLocalizer()
                     .getString("DogPage.action.new_dog", DogPage.this));
 
             System.err.println("XXX HOWK 66");//FIXME
             // update DTO
             ((DogDto)form_dog_whole.getModelObject()).setOwner(
                     (CustomerDto)select_owner.getModelObject());
             // save DTO to DB
             dogser.updateDog((DogDto)form_dog_whole.getModelObject());
             // reinitialize everything
             form_dog_whole.setModelObject(newDogDto());
             select_owner.setModelObject(null);
             break;
           case NEW_DOG:
             action.setDefaultModelObject(DogPage.this.getLocalizer()
                     .getString("DogPage.action.new_dog", DogPage.this));
 
             System.err.println("XXX HOWK 77");//FIXME
             // update DTO
             ((DogDto)form_dog_whole.getModelObject()).setOwner(
                     (CustomerDto)select_owner.getModelObject());
             // save DTO to DB
             dogser.addDog((DogDto)form_dog_whole.getModelObject());
             // reinitialize everything
             form_dog_whole.setModelObject(newDogDto());
             select_owner.setModelObject(null);
             break;
           default:
             System.err.println("XXX HOWK 88");//FIXME
         }
       }
     });
     form_dog_whole.setDefaultButton(btn_submit);
     add(form_dog_whole);
   }
 }
