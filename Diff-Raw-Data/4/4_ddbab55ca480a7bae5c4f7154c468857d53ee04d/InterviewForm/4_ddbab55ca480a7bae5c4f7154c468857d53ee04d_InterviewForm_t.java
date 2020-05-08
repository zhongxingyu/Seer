 package br.org.indt.ndg.lwuit.ui;
 
 import br.org.indt.ndg.lwuit.control.AcceptQuestionListFormCommand;
 import br.org.indt.ndg.lwuit.control.BackInterviewFormCommand;
 import br.org.indt.ndg.lwuit.control.OpenFileBrowserCommand;
 import br.org.indt.ndg.lwuit.control.PersistenceManager;
 import br.org.indt.ndg.lwuit.control.RemovePhotoCommand;
 import br.org.indt.ndg.lwuit.control.ShowPhotoCommand;
 import br.org.indt.ndg.lwuit.control.SurveysControl;
 import br.org.indt.ndg.lwuit.control.TakePhotoCommand;
 import br.org.indt.ndg.lwuit.extended.CheckBox;
 import br.org.indt.ndg.lwuit.extended.DateField;
 import br.org.indt.ndg.lwuit.extended.DescriptiveField;
 import br.org.indt.ndg.lwuit.extended.FilterProxyListModel;
 import br.org.indt.ndg.lwuit.extended.List;
 import br.org.indt.ndg.lwuit.extended.NumericField;
 import br.org.indt.ndg.lwuit.extended.RadioButton;
 import br.org.indt.ndg.lwuit.extended.TimeField;
 import br.org.indt.ndg.lwuit.model.NDGAnswer;
 import br.org.indt.ndg.lwuit.model.Category;
 import br.org.indt.ndg.lwuit.model.CategoryAnswer;
 import br.org.indt.ndg.lwuit.model.ChoiceAnswer;
 import br.org.indt.ndg.lwuit.model.ChoiceQuestion;
 import br.org.indt.ndg.lwuit.model.DateAnswer;
 import br.org.indt.ndg.lwuit.model.DateQuestion;
 import br.org.indt.ndg.lwuit.model.DecimalQuestion;
 import br.org.indt.ndg.lwuit.model.DescriptiveQuestion;
 import br.org.indt.ndg.lwuit.model.ImageAnswer;
 import br.org.indt.ndg.lwuit.model.ImageData;
 import br.org.indt.ndg.lwuit.model.ImageQuestion;
 import br.org.indt.ndg.lwuit.model.NumericAnswer;
 import br.org.indt.ndg.lwuit.model.NumericQuestion;
 import br.org.indt.ndg.lwuit.model.NDGQuestion;
 import br.org.indt.ndg.lwuit.model.TimeAnswer;
 import br.org.indt.ndg.lwuit.model.TimeQuestion;
 import br.org.indt.ndg.lwuit.ui.camera.NDGCameraManager;
 import br.org.indt.ndg.lwuit.ui.camera.NDGCameraManagerListener;
 import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
 import br.org.indt.ndg.mobile.Resources;
 import com.sun.lwuit.Button;
 import com.sun.lwuit.ButtonGroup;
 import com.sun.lwuit.Component;
 import com.sun.lwuit.Container;
 import com.sun.lwuit.Display;
 import com.sun.lwuit.Image;
 import com.sun.lwuit.Label;
 import com.sun.lwuit.TextArea;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.events.DataChangedListener;
 import com.sun.lwuit.events.FocusListener;
 import com.sun.lwuit.geom.Dimension;
 import com.sun.lwuit.layouts.BoxLayout;
 import com.sun.lwuit.layouts.FlowLayout;
 import com.sun.lwuit.list.DefaultListModel;
 import com.sun.lwuit.list.ListCellRenderer;
 import com.sun.lwuit.list.ListModel;
 import com.sun.lwuit.plaf.Border;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 
 public class InterviewForm extends Screen implements ActionListener {
     private String title1;
     private String title2;
 
     private NDGQuestion currentQuestion;
     private NDGAnswer currentAnswer;
     private Vector vContainers;
     private boolean answerChanged = false;
 
     protected void loadData() {
         createScreen();
         vContainers = new Vector();
         title1 = SurveysControl.getInstance().getSurveyTitle();
         title2 = Resources.NEW_INTERVIEW;
     }
 
     protected void customize() {
         form.removeAllCommands();
         form.removeAll();
 
         form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
         form.getContentPane().getStyle().setBorder(Border.createEmpty(), false);
         form.setScrollAnimationSpeed(100);
         form.setFocusScrolling(true);
 
         Vector questions = SurveysControl.getInstance().getSelectedCategory().getQuestions();
 
         ContainerUI lastUI = null;
         for ( int j=0; j< questions.size(); j++ ){
             currentQuestion = (NDGQuestion)questions.elementAt(j);
 
             int categoryIndex = SurveysControl.getInstance().getSelectedCategoryIndex();
             String catIndex  = String.valueOf(categoryIndex+1);
             int subCategory = SurveysControl.getInstance().getSelectedSubCategoryIndex();
             CategoryAnswer categoryanswer = SurveysControl.getInstance().getResult().getCategoryAnswers(catIndex);
             Hashtable table = categoryanswer.getSubCategoryAnswers(subCategory);
             currentAnswer = (NDGAnswer)table.get( String.valueOf( currentQuestion.getIdNumber()));
 
             if (currentQuestion instanceof DescriptiveQuestion) {
                  DescriptiveFieldUI df = new DescriptiveFieldUI( currentQuestion, currentAnswer );
                  df.registerQuestion();
                  form.addComponent(df);
                  lastUI = df;
             }
             else if (currentQuestion instanceof NumericQuestion) {
                 NumericFieldUI  nf = new NumericFieldUI(currentQuestion, currentAnswer);
                 nf.registerQuestion();
                 form.addComponent(nf);
                 lastUI = nf;
             }
             else if (currentQuestion instanceof DateQuestion) {
                DateFieldUI df = new DateFieldUI(currentQuestion, currentAnswer);
                df.registerQuestion();
                form.addComponent(df);
                lastUI = df;
             }
             else if (currentQuestion instanceof ChoiceQuestion) {
                 if (((ChoiceQuestion) currentQuestion).isExclusive()) {
                     if (((ChoiceQuestion) currentQuestion).getChoices().size() <= 4) {
                         ExclusiveChoiceFieldUI ecf = new ExclusiveChoiceFieldUI(currentQuestion, currentAnswer);
                         ecf.registerQuestion();
                         form.addComponent(ecf);
                         lastUI = ecf;
                     } else { //if there are more than 4 choices (radiobutton)
                         ExclusiveChoiceFieldAutoCompleteUI ecfa = new ExclusiveChoiceFieldAutoCompleteUI(currentQuestion, currentAnswer);
                         ecfa.registerQuestion();
                         form.addComponent(ecfa);
                         lastUI = ecfa;
                     }
                     updateSkippedQuestion((ChoiceQuestion)currentQuestion,(ChoiceAnswer)currentAnswer);
                 } else {
                     ChoiceFieldUI cf = new ChoiceFieldUI(currentQuestion, currentAnswer);
                     cf.registerQuestion();
                     form.addComponent(cf);
                     lastUI = cf;
                 }
             }
             else if(currentQuestion instanceof ImageQuestion){
                 ImageFieldUI imgf = new ImageFieldUI(currentQuestion, currentAnswer);
                 imgf.registerQuestion();
                 form.addComponent(imgf);
                 lastUI = imgf;
             } else if (currentQuestion instanceof TimeQuestion) {
                 if (((TimeQuestion) currentQuestion).getConvention() == 24) {
                     TimeFieldUI tf = new TimeFieldUI(currentQuestion, currentAnswer);
                     tf.registerQuestion();
                     form.addComponent(tf);
                     lastUI = tf;
                 } else if (((TimeQuestion) currentQuestion).getConvention() == 12) {
                     if(currentQuestion.getFirstTime()) {
                         ((TimeQuestion) currentQuestion).setAm_pm(-1); // clear
                     }
                     TimeField12UI tf = new TimeField12UI(currentQuestion, currentAnswer);
                     tf.registerQuestion();
                     form.addComponent(tf);
                     lastUI = tf;
                 }
             }
 
             if( currentQuestion.getSkiped())
             {
                 lastUI.setEnabled(false);
             }
             vContainers.addElement(lastUI);
         }
 
         form.addCommand(BackInterviewFormCommand.getInstance().getCommand());
         form.addCommand(AcceptQuestionListFormCommand.getInstance().getCommand());
         try{
             form.removeCommandListener(this);
         } catch (NullPointerException npe ) {
             //during first initialisation remove throws exception.
             //this ensure that we have registered listener once
         }
         form.addCommandListener(this);
         
         if (PersistenceManager.getInstance().isEditing()) {
             title2 = Resources.EDITING;
         }
         setTitle(title1, title2);
     }
 
     private void setModifiedInterview(boolean _val) {
         answerChanged = _val;
     }
 
     public boolean isModifiedInterview() {
         return answerChanged;
     }
 
     private void commitAllAnswers() {
         for ( int i = 0; i < vContainers.size(); i++) {
             ((ContainerUI)vContainers.elementAt(i)).commitValue();
         }
     }
 
     public boolean validateAllAnswersAndResetModifiedFlag() {
         boolean result = validateAllAnswers();
         if (result)
             setModifiedInterview(false);
         return result;
     }
 
     private boolean validateAllAnswers() {
         commitAllAnswers();
         for ( int i = 0; i< vContainers.size(); i++)
         {
             NDGQuestion question = ((ContainerUI)vContainers.elementAt(i)).getQuestion();
             NDGAnswer answer = ((ContainerUI)vContainers.elementAt(i)).getAnswer();
             if ( !question.passConstraints( answer ) )
             {
                 ((ContainerUI)vContainers.elementAt(i)).requestFocus();
                 return false;
             }
         }
         return true;
     }
 
     private void updateSkippedQuestion( ChoiceQuestion aQuestion, ChoiceAnswer aAnswer ) {
         try
         {
             int selectedChoiceItem = Integer.parseInt( (String)aAnswer.getSelectedIndexes().elementAt(0) );
             boolean sentence1 = ((selectedChoiceItem != ((ChoiceQuestion) aQuestion).getChoiceItem()) && (((ChoiceQuestion) aQuestion).isInverse()));
             boolean sentence2 = ((selectedChoiceItem == ((ChoiceQuestion) aQuestion).getChoiceItem()) && (!((ChoiceQuestion) aQuestion).isInverse()));
 
             Vector questions = SurveysControl.getInstance().getQuestionsFlat();
             int start = questions.indexOf( aQuestion );
             int endCat = aQuestion.getCatTo();
             int endQuestion = aQuestion.getSkipTo();
 
             if( endCat > 0 && endCat <= SurveysControl.getInstance().getSurvey().getCategories().size() ) {
                 Category category = (Category)SurveysControl.getInstance().getSurvey().getCategories().elementAt(endCat-1);
                 if( endQuestion > 0 && endQuestion <= category.getQuestions().size() ) {
                     int endQuestionLinear = questions.indexOf(category.getQuestions().elementAt(endQuestion-1));
                     for( int i = start+1; i<endQuestionLinear; i++) {
                         ((NDGQuestion)questions.elementAt(i)).setSkiped(sentence1 || sentence2);
                     }
                 }
 
                 for( int i = 0; i< vContainers.size(); i++ ) {
                     ContainerUI container = (ContainerUI)vContainers.elementAt(i);
                     boolean skip = container.getQuestion().getSkiped();
                     container.setEnabled(!skip);
                 }
             }
         }
         catch(Exception ex){/*do nothing*/}
     }
 
     public void actionPerformed(ActionEvent evt) {
         Object cmd = evt.getSource();
         if (cmd == BackInterviewFormCommand.getInstance().getCommand() ) {
             BackInterviewFormCommand.getInstance().execute(this);
         } else if ( cmd == AcceptQuestionListFormCommand.getInstance().getCommand() ){
             AcceptQuestionListFormCommand.getInstance().execute(this);
         } else if( cmd == OpenFileBrowserCommand.getInstance().getCommand() ) {
             OpenFileBrowserCommand.getInstance().execute(null);
         } else if ( cmd == TakePhotoCommand.getInstance().getCommand() ) {
             TakePhotoCommand.getInstance().execute(null);
         } else if ( cmd == ShowPhotoCommand.getInstance().getCommand() ) {
             ShowPhotoCommand.getInstance().execute(null);
         } else if ( cmd == RemovePhotoCommand.getInstance().getCommand() ) {
             RemovePhotoCommand.getInstance().execute(null);
         }
     }
 
 abstract class ContainerUI extends Container implements FocusListener {
     private final int NUMBER_OF_COLUMNS = 20;
 
     protected TextArea mQuestionTextArea;
     protected NDGQuestion mQuestion;
     protected NDGAnswer mAnswer;
 
     public abstract void commitValue();
     public abstract void setEnabled(boolean enabled);
 
     public void handleMoreDetails( Object cmd )
     {
     }
 
     public ContainerUI( NDGQuestion aQuestion, NDGAnswer aAnswer)
     {
         getStyle().setBorder(Border.createBevelLowered( NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor ));
         mQuestion  = aQuestion;
         mAnswer = aAnswer;
     }
 
     protected TextArea createQuestionName( String aQuestionText ) {
         TextArea questionName = new TextArea();
         questionName.setEditable(false);
         questionName.setFocusable(false);
         questionName.setColumns(NUMBER_OF_COLUMNS);
         questionName.setRows(1);
         questionName.setGrowByContent(false);
         questionName.setText(aQuestionText);
 
         int pw = Display.getInstance().getDisplayWidth();
         int w = questionName.getStyle().getFont().stringWidth(aQuestionText);
         if (w > pw) {
             questionName.setGrowByContent(true);
             questionName.setRows(2);
         } else {
             questionName.setGrowByContent(false);
             questionName.setRows(1);
         }
 
         return questionName;
     }
 
     public NDGQuestion getQuestion() {
         return mQuestion;
     }
 
     public NDGAnswer getAnswer() {
         return mAnswer;
     }
 
     public void focusGained(Component cmpnt){
         getStyle().setBorder(Border.createBevelLowered( NDGStyleToolbox.getInstance().focusGainColor,
                                                         NDGStyleToolbox.getInstance().focusGainColor,
                                                         NDGStyleToolbox.getInstance().focusGainColor,
                                                         NDGStyleToolbox.getInstance().focusGainColor ), false);
         refreshTheme();
     }
 
     public void focusLost(Component cmpnt) {
         commitValue();	//TODO ensure it can be removed
         if (!mQuestion.passConstraints(mAnswer)) {
             cmpnt.requestFocus();
             return;
         }
         getStyle().setBorder(Border.createBevelLowered( NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor ), false);
         refreshTheme();
     }
 }
 
 
      class DescriptiveFieldUI extends ContainerUI{
         private DescriptiveField mDescriptionTextField;
 
         public DescriptiveFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion(){
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             mQuestionTextArea = createQuestionName( mQuestion.getName() );
             addComponent(mQuestionTextArea);
 
             mDescriptionTextField = new DescriptiveField(((DescriptiveQuestion) mQuestion).getLength());
             mDescriptionTextField.setText((String)mAnswer.getValue()/*(String) question.getAnswer().getValue()*/);
             mDescriptionTextField.setInputMode("Abc");
             mDescriptionTextField.setEditable(true);
             mDescriptionTextField.setFocusable(true);
             addComponent(mDescriptionTextField);
             mDescriptionTextField.addFocusListener(this);
             mDescriptionTextField.addDataChangeListener(new DataChangedListener() {
                 public void dataChanged(int type, int index) {
                     revalidate();
                 }
             });
 
             if (((DescriptiveQuestion)mQuestion).getChoices().size() >= 1) {
                 Vector vChoices = ((DescriptiveQuestion)mQuestion).getChoices();
                 int totalChoices = vChoices.size();
                 String[] choices = new String[totalChoices];
 
                 final ListModel underlyingModel = new DefaultListModel();
                 for (int i = 0; i < totalChoices; i++) {
                     choices[i] = (String) vChoices.elementAt(i);
                     underlyingModel.addItem(choices[i]);
                 }
 
                 final FilterProxyListModel proxyModel = new FilterProxyListModel(underlyingModel);
                 proxyModel.setMaxDisplay(4);
                 final List choice = new List(proxyModel);
 
                 choice.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent ae) {
                     mDescriptionTextField.setText(((List) ae.getSource()).getSelectedItem().toString());
                     }
                 });
 
                 mDescriptionTextField.addDataChangeListener(new DataChangedListener() {
 
                 public void dataChanged(int arg0, int arg1) {
                   //  skipGameKey = true;
                     proxyModel.filter(mDescriptionTextField.getText());
                     //setModifiedInterview(true);
                     }
                 });
 
                 addComponent(choice);
             } else {
                 mDescriptionTextField.addDataChangeListener(new HandleInterviewAnswersModified());
             }
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue() {
             mAnswer.setValue( mDescriptionTextField.getText() );
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled( boolean enabled ) {
             mQuestionTextArea.setEnabled(enabled);
             mDescriptionTextField.setEnabled(enabled);
         }
      }
     ///////////////////////////// Numeric Question /////////////////////////////
     class NumericFieldUI extends ContainerUI{
         private NumericField mNumberTextField;
 
         public NumericFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ){
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion( ) {
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             mQuestionTextArea = createQuestionName(mQuestion.getName());
             addComponent(mQuestionTextArea);
 
             mNumberTextField = new NumericField( ((NumericQuestion) mQuestion).getLength(),
                                                  mQuestion instanceof DecimalQuestion );
             mNumberTextField.setFocusable(true);
             String value =((NumericAnswer)mAnswer).getValueString();
             mNumberTextField.setText(value);
             mNumberTextField.addFocusListener(this);
             mNumberTextField.addDataChangeListener(new HandleInterviewAnswersModified());
 
             addComponent(mNumberTextField);
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue( ) {
             mAnswer.setValue( mNumberTextField.getText() );
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             mNumberTextField.setEnabled(enabled);
         }
     }
 
     ////////////////////////////// Date Question ///////////////////////////////
     class DateFieldUI extends ContainerUI{
         private DateField mDateTextField;
 
         public DateFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion() {
             setLayout( new BoxLayout(BoxLayout.Y_AXIS));
             mQuestionTextArea = createQuestionName( mQuestion.getName() );
             
             addComponent(mQuestionTextArea);
             mDateTextField = new DateField(DateField.DDMMYYYY);
             long datelong = ((DateAnswer)mAnswer).getDate();
             mDateTextField.setDate(new Date(datelong));
             mDateTextField.setEditable(true);
             mDateTextField.addFocusListener(this);
             mDateTextField.addDataChangeListener(new HandleInterviewAnswersModified());
 
             addComponent(mDateTextField);
 
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
         
         public void commitValue() {
             Date date = mDateTextField.getDate();
             Long datelong = new Long(date.getTime());
             ((DateAnswer)mAnswer).setDate(datelong.longValue());
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEditable(enabled);
             mDateTextField.setEnabled(enabled);
         }
     }
 
     class ExclusiveChoiceFieldUI extends ContainerUI {
         private ButtonGroup mGroupButton;
 
         public ExclusiveChoiceFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion(){
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             mQuestionTextArea = createQuestionName(mQuestion.getName());
 
             addComponent(mQuestionTextArea);
 
             mGroupButton = new ButtonGroup();
             Vector vChoices = ((ChoiceQuestion)mQuestion).getChoices();
             Vector vOthers = ((ChoiceQuestion)mQuestion).getOthers();
 
             int totalChoices = vChoices.size();
             String[] choices = new String[totalChoices];
             for (int i = 0; i < totalChoices; i++) {
                 choices[i] = (String) vChoices.elementAt(i);
                 RadioButton rb = new RadioButton(choices[i]);
                 rb.setOther(((String) vOthers.elementAt(i)).equals("1"));
                 rb.setOtherText(""); // Initializes with empty string
                 rb.addActionListener(new HandleMoreDetails()); // More Details
                 rb.addFocusListener(this); // Controls when changing to a new question
                 mGroupButton.add(rb);
                 addComponent(rb);
             }
 
             Vector vSelectedIndexes = ((ChoiceAnswer)mAnswer).getSelectedIndexes();
             for ( int i=0; i< vSelectedIndexes.size(); i++ ) {
                 int index = Integer.parseInt( (String)vSelectedIndexes.elementAt( i ) );
                 mGroupButton.setSelected(index);
 
                 RadioButton rb = (RadioButton) mGroupButton.getRadioButton(index);
                 String other  = (String)((ChoiceAnswer)mAnswer).getOtherText( (String)vSelectedIndexes.elementAt(i ) );
                 if( other != null ) {
                     rb.setOtherText( other );
                 }
             }
 
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue()
         {
             Vector selectedIndexes = new Vector();
             Hashtable selectedOtherText = new Hashtable();
 
             for (int i = 0; i < mGroupButton.getButtonCount(); i++)
             {
                 RadioButton rb = (RadioButton)mGroupButton.getRadioButton(i);
                 if (rb.isSelected())
                 {
                     selectedIndexes.addElement( Integer.toString(i) );
                     if( rb.hasOther() ) {
                         selectedOtherText.put( Integer.toString(i), rb.getOtherText() );
                     }
                     break;
                 }
             }
 
             ((ChoiceAnswer)mAnswer).setSelectedIndex( selectedIndexes );
             ((ChoiceAnswer)mAnswer).setOtherText(selectedOtherText);
 
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             for( int i = 0; i<mGroupButton.getButtonCount(); i++)
             {
                 mGroupButton.getRadioButton(i).setEnabled(enabled);
             }
         }
 
         public void handleMoreDetails( Object obj )
         {
             for( int i = 0; i<mGroupButton.getButtonCount();i++)
             {
                 RadioButton rb = (RadioButton) mGroupButton.getRadioButton(i);
                 if ((rb.hasOther()) && (rb.isSelected())) {
                     DetailsForm.show(rb.getText(), rb.getOtherText());
                     rb.setOtherText(SurveysControl.getInstance().getItemOtherText());
                 }
                 if ( (rb.hasOther()) && (!rb.isSelected()))
                 {
                     rb.setOtherText("");
                 }
             }
             commitValue();
         }
     }
 
     class ExclusiveChoiceFieldAutoCompleteUI extends ContainerUI {
         private ButtonGroup mGroupButton;
         private DescriptiveField mExclusiveChoiceTextField;
         private ListModel mDataModel;
 
         public ExclusiveChoiceFieldAutoCompleteUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void focusGained(Component cmpnt) {
             super.focusGained(cmpnt);
             mExclusiveChoiceTextField.keyPressed(Display.GAME_FIRE);
         }
 
         public void registerQuestion(){
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             mQuestionTextArea = createQuestionName(mQuestion.getName());
             addComponent(mQuestionTextArea);
 
             mGroupButton = new ButtonGroup();
 
             Vector vChoices = ((ChoiceQuestion)mQuestion).getChoices();
             Vector vOthers = ((ChoiceQuestion)mQuestion).getOthers();
             int totalChoices = vChoices.size();
             String[] choices = new String[totalChoices];
 
             mDataModel = new DefaultListModel();
             final ManagerOptionSelectableRadio managerOptionSelectableRadio = new ManagerOptionSelectableRadio();
             int maxQuestionLength = 0;
             for (int i = 0; i < totalChoices; i++) {
                     choices[i] = (String) vChoices.elementAt(i);
                     RadioButton rb = new RadioButton(choices[i]);
                     mDataModel.addItem(new OptionSelectableRadio(choices[i], managerOptionSelectableRadio));
                     rb.setOther(((String) vOthers.elementAt(i)).equals("1"));
                     rb.setOtherText(""); // Initializes with empty string
                     rb.setFocusable(false);
                     mGroupButton.add(rb);
                     maxQuestionLength = (choices[i].length() > maxQuestionLength) ? choices[i].length() : maxQuestionLength;
             }
 
             mExclusiveChoiceTextField = new DescriptiveField(maxQuestionLength);
             Vector vSelectedIndexes = ((ChoiceAnswer)mAnswer).getSelectedIndexes();
             for ( int i=0; i< vSelectedIndexes.size(); i++ ) {
                 int index = Integer.parseInt( (String)vSelectedIndexes.elementAt( i ) );
                 mGroupButton.setSelected(index);
                 ((OptionSelectableRadio) mDataModel.getItemAt(index)).setSelected(true);
 
                 RadioButton rb = (RadioButton) mGroupButton.getRadioButton(index);
                 String other  = (String)((ChoiceAnswer)mAnswer).getOtherText( (String)vSelectedIndexes.elementAt(i ) );
                 if( other != null ) {
                     rb.setOtherText( other );
                 }
             }
 
             final FilterProxyListModel proxyModel = new FilterProxyListModel(mDataModel);
             proxyModel.setMaxDisplay(-1);//Unlimited.
 
             final List choice = new List(proxyModel);
             choice.setFocusable(true);
             choice.setPaintFocusBehindList(true);
             choice.setHandlesInput(false);
             choice.setListCellRenderer(new RadioButtonRenderer(""));
             choice.addActionListener(new HandleMoreDetails());
             if( mGroupButton.getSelectedIndex() >=1 )
             {
                 choice.setSelectedIndex(mGroupButton.getSelectedIndex()-1);
             }
 
             mExclusiveChoiceTextField.addDataChangeListener(new DataChangedListener() {
                 public void dataChanged(int arg0, int arg1) {
                 proxyModel.filter(mExclusiveChoiceTextField.getText());
                 }
             });
 
             mExclusiveChoiceTextField.addFocusListener(this);
             Container cList = new Container(new BoxLayout(BoxLayout.Y_AXIS));
             cList.setFocusable(false);
             cList.addComponent(choice);
             cList.setPreferredSize(new Dimension(30, 90));
 
             addComponent(mExclusiveChoiceTextField);
             addComponent(cList);
 
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue()
         {
             Vector selectedIndexes = new Vector();
             Hashtable selectedOtherText = new Hashtable();
 
             for (int i = 0; i < mGroupButton.getButtonCount(); i++)
             {
                 RadioButton rb = (RadioButton)mGroupButton.getRadioButton(i);
                 if (rb.isSelected())
                 {
                     selectedIndexes.addElement( Integer.toString(i) );
                     if( rb.hasOther() ) {
                         selectedOtherText.put( Integer.toString(i), rb.getOtherText() );
                     }
                     break;
                 }
             }
 
             ((ChoiceAnswer)mAnswer).setSelectedIndex( selectedIndexes );
             ((ChoiceAnswer)mAnswer).setOtherText(selectedOtherText);
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             mExclusiveChoiceTextField.setEnabled(enabled);
             for( int i = 0; i<mGroupButton.getButtonCount(); i++)
             {
                 mGroupButton.getRadioButton(i).setEnabled(enabled);
             }
         }
 
         public void handleMoreDetails( Object obj )
         {
             List list = (List)obj;
             int filterOffset = list.getSelectedIndex();
             int selItem = ((FilterProxyListModel)(list.getModel())).getFilterOffset(filterOffset);
 
             mGroupButton.setSelected(selItem);
             for( int i = 0; i<mGroupButton.getButtonCount();i++)
             {
                 RadioButton rb = (RadioButton) mGroupButton.getRadioButton(i);
                 if ((rb.hasOther()) && (rb.isSelected())) {
                     DetailsForm.show(rb.getText(), rb.getOtherText());
                     rb.setOtherText(SurveysControl.getInstance().getItemOtherText());
                 }
                 if ( (rb.hasOther()) && (!rb.isSelected()))
                 {
                     rb.setOtherText("");
                 }
             }
             commitValue();
         }
     }
 
     ////////////////////////// Choice Multiple Question ////////////////////////
     class ChoiceFieldUI extends ContainerUI{
         private Vector mGroupButton;
 
         public ChoiceFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion(){
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             mQuestionTextArea = createQuestionName(mQuestion.getName());
 
             addComponent(mQuestionTextArea);
 
             mGroupButton = new Vector();
             Vector vChoices = ((ChoiceQuestion)mQuestion).getChoices();
             Vector vOthers = ((ChoiceQuestion)mQuestion).getOthers();
 
             int totalChoices = vChoices.size();
             String[] choices = new String[totalChoices];
             for (int i = 0; i < totalChoices; i++) {
                 choices[i] = (String) vChoices.elementAt(i);
                 CheckBox cb = new CheckBox(choices[i]);
                 cb.setOther(((String) vOthers.elementAt(i)).equals("1"));
                 cb.setOtherText(""); // Initializes with empty string
                 cb.addActionListener(new HandleMoreDetails()); // More Details
                 cb.addFocusListener(this); // Controls when changing to a new question
                 mGroupButton.addElement(cb);
                 addComponent(cb);
             }
 
             Vector vSelectedIndexes = ((ChoiceAnswer)mAnswer).getSelectedIndexes();
             for ( int i=0; i< vSelectedIndexes.size(); i++ ) {
                 int index = Integer.parseInt( (String)vSelectedIndexes.elementAt( i ) );
                 ((CheckBox) mGroupButton.elementAt(i)).setSelected(true);
 
                 if ( ((ChoiceAnswer)mAnswer).getOtherText( String.valueOf(i) )!= null ) {
                     ((CheckBox) mGroupButton.elementAt(i)).setOtherText((String)((ChoiceAnswer)mAnswer).getOtherText( String.valueOf( i ) ) );
                 }
             }
 
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
        }
 
         public void commitValue()
         {
             Vector selectedIndexes = new Vector();
             Hashtable othersText = new Hashtable();
 
             for (int i = 0; i < mGroupButton.size(); i++)
             {
                 CheckBox cb = (CheckBox) mGroupButton.elementAt(i);
                 if (cb.isSelected())
                 {
                     selectedIndexes.addElement( Integer.toString(i) );
                     if( cb.hasOther() ) {
                         othersText.put( Integer.toString(i), cb.getOtherText() );
                     }
                 }
             }
             ((ChoiceAnswer)mAnswer).setSelectedIndex( selectedIndexes );
             ((ChoiceAnswer)mAnswer).setOtherText( othersText );
 
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             for( int i = 0; i< mGroupButton.size(); i++)
             {
                 ((CheckBox)mGroupButton.elementAt(i)).setEnabled(enabled);
             }
         }
 
         public void handleMoreDetails( Object obj )
         {
             CheckBox cb = (CheckBox)obj;
             if ((cb.hasOther()) && (cb.isSelected()))
             {
                DetailsForm.show(cb.getText(), cb.getOtherText());
                cb.setOtherText(SurveysControl.getInstance().getItemOtherText());
             }
             if ((cb.hasOther()) && (!cb.isSelected()))
             {
                  cb.setOtherText("");
             }
         }
    }
 
     class ImageFieldUI extends ContainerUI implements ActionListener, NDGCameraManagerListener {
         private static final int FOUR_ACTIONS_CONTEXT_MENU = 4;//TakePhotoCommand,OpenFileBrowserCommand,ShowPhotoCommand,RemovePhotoCommand
         private static final int TWO_ACTIONS_CONTEXT_MENU = 2;//TakePhotoCommand,OpenFileBrowserCommand
 
         private Container mImageContainer;
 
         public ImageFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void update(){
            ImageAnswer imgAnswer = (ImageAnswer)mAnswer;
            if(mImageContainer.getComponentCount() <= imgAnswer.getImages().size()) {
                addCameraIconButton();
                setModifiedInterview(true);
            }
 
            form.showBack();
            //focus last button
            Component comp = mImageContainer.getComponentAt( mImageContainer.getComponentCount() - 1 );
            comp.requestFocus();
            rebuildOptionsMenu( comp );
         }
 
         public void registerQuestion(){
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
 
             ImageAnswer imgAnswer = (ImageAnswer)mAnswer;
 
             mImageContainer = new Container (new FlowLayout());
 
             ImageData imgData = null;
 
             Label spacer = new Label(mQuestion.getName());
             spacer.setFocusable(false);
             addComponent(spacer);
             Label maxPhotoCount = new Label( Resources.MAX_IMG_NO + String.valueOf(((ImageQuestion)mQuestion).getMaxCount()) );
             maxPhotoCount.getStyle().setFont(NDGStyleToolbox.fontSmall);
             addComponent(maxPhotoCount);
 
             if(imgAnswer.getImages().size() > 0){
                 for(int idx = 0; idx < imgAnswer.getImages().size(); idx++){
                     imgData = (ImageData)imgAnswer.getImages().elementAt(idx);
                     addButton(imgData.getThumbnail());
                 }
             }
             addCameraIconButton();
             addComponent(mImageContainer);
             addFocusListener(this);
         }
 
         public void addCameraIconButton(){
             ImageAnswer imgAnswer = (ImageAnswer)mAnswer;
             if(imgAnswer.getImages().size() < ((ImageQuestion)mQuestion).getMaxCount()){
                 Image img = Screen.getRes().getImage("camera-icon");
                 addButton(img);
             }
         }
 
         private void addButton(Image img){
             Button button = new Button();
             button.setIcon(img);
             button.addActionListener(this);
             button.setAlignment(Component.LEFT);
             button.setFocusable(true);
             button.addFocusListener(this);
             mImageContainer.addComponent(button);
         }
 
         public void focusGained(Component cmpnt) {
             super.focusGained(cmpnt);
             rebuildOptionsMenu(cmpnt);
 
             NDGCameraManager.getInstance().sendPostProcessData( this, cmpnt,
                                                                (ImageAnswer)mAnswer,
                                                                 mImageContainer);
         }
 
         private void rebuildOptionsMenu( Component cmpnt ) {
             form.removeCommand(OpenFileBrowserCommand.getInstance().getCommand());
             form.removeCommand(TakePhotoCommand.getInstance().getCommand());
             form.removeCommand(ShowPhotoCommand.getInstance().getCommand());
             form.removeCommand(RemovePhotoCommand.getInstance().getCommand());
             if (mImageContainer.getComponentIndex(cmpnt) < ((ImageAnswer)mAnswer).getImages().size()) {
                 form.addCommand(RemovePhotoCommand.getInstance().getCommand());
                 form.addCommand(ShowPhotoCommand.getInstance().getCommand());
             }
             form.addCommand(OpenFileBrowserCommand.getInstance().getCommand());
             form.addCommand(TakePhotoCommand.getInstance().getCommand());
         }
 
         public void focusLost(Component cmpnt) {
            form.removeCommand(OpenFileBrowserCommand.getInstance().getCommand());
            form.removeCommand(TakePhotoCommand.getInstance().getCommand());
            form.removeCommand(ShowPhotoCommand.getInstance().getCommand());
            form.removeCommand(RemovePhotoCommand.getInstance().getCommand());
             super.focusLost(cmpnt);
         }
 
         public void commitValue() {
             mQuestion.setVisited(true);
             mAnswer.setVisited(true);
         }
 
         public void actionPerformed(ActionEvent cmd) {
             if ( cmd.getSource() instanceof Button ) {
                 NDGCameraManager.getInstance().sendPostProcessData( this,
                                                                     (Button)cmd.getSource(),
                                                                     (ImageAnswer)mAnswer,
                                                                     mImageContainer);
 
                 int index = mImageContainer.getComponentIndex((Button)cmd.getSource());
 
                 if( index  < ((ImageAnswer)mAnswer).getImages().size() ){
                     new ImageQuestionContextMenu(0, FOUR_ACTIONS_CONTEXT_MENU).show();
                 } else {
                     new ImageQuestionContextMenu(0, TWO_ACTIONS_CONTEXT_MENU).show();
                 }
             }
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
         }
     }
 
     class TimeFieldUI extends ContainerUI {
         private TimeField mTimeTextField;
 
         public TimeFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
              super( aQuestion, aAnswer );
         }
 
         public void registerQuestion() {
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             mQuestionTextArea = createQuestionName( mQuestion.getName() );
 
             addComponent(mQuestionTextArea);
             mTimeTextField = new TimeField(TimeField.HHMM1);
 
             long datelong =  ((TimeAnswer)mAnswer).getTime();
 
             mTimeTextField.setTime(new Date(datelong));
             mTimeTextField.setEditable(true);
             mTimeTextField.addFocusListener(this);
             mTimeTextField.addDataChangeListener(new HandleInterviewAnswersModified());
 
             addComponent(mTimeTextField);
 
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue() {
             Date time = mTimeTextField.getTime();
             Long timelong = new Long(time.getTime());
             ((TimeAnswer)mAnswer).setTime( timelong.longValue() );
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             mTimeTextField.setEnabled(enabled);
         }
     }
 
     class TimeField12UI extends ContainerUI {
         private TimeField mTimeTextField;
         private ButtonGroup mAmPmGroupButton;
 
         public TimeField12UI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion(){
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             mQuestionTextArea = createQuestionName(mQuestion.getName());
 
             addComponent(mQuestionTextArea);
             mTimeTextField = new TimeField(TimeField.HHMM);
             mTimeTextField.addFocusListener(this);
 
             mAmPmGroupButton = new ButtonGroup();
             final RadioButton am = new RadioButton("am");
             final RadioButton pm = new RadioButton("pm");
             am.addFocusListener(this);
             pm.addFocusListener(this);
 
             mAmPmGroupButton.add(am);
             mAmPmGroupButton.add(pm);
             am.addActionListener(new HandleMoreDetails());
             pm.addActionListener(new HandleMoreDetails());
 
             long datelong = ((TimeAnswer)mAnswer).getTime();
             Date date = new Date(datelong);
 
             if (((TimeQuestion)mQuestion).getAm_pm() == 1) {
                 mAmPmGroupButton.setSelected(am);
             } else if (((TimeQuestion)mQuestion).getAm_pm() == 2) {
                 mAmPmGroupButton.setSelected(pm);
             } else {
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(date);
                 if( cal.get(Calendar.AM_PM) == Calendar.AM ) {
                     mAmPmGroupButton.setSelected(am);
                     ((TimeQuestion)getQuestion()).setAm_pm(TimeQuestion.AM);
                 } else {
                     mAmPmGroupButton.setSelected(pm);
                     ((TimeQuestion)getQuestion()).setAm_pm(TimeQuestion.PM);
                 }
             }
 
             mTimeTextField.setTime(date);
             mTimeTextField.setEditable(true);
             mTimeTextField.addFocusListener(this);
             mTimeTextField.addDataChangeListener(new HandleInterviewAnswersModified());
 
         addComponent(mTimeTextField);
         addComponent(am);
         addComponent(pm);
 
         Label spacer = new Label("");
         spacer.setFocusable(false);
         addComponent(spacer);
         }
 
         public void commitValue() {
             for (int i = 0; i < mAmPmGroupButton.getButtonCount(); i++)
             {
                 RadioButton rb = (RadioButton)mAmPmGroupButton.getRadioButton(i);
                 if (rb.isSelected())
                 {
                     ((TimeQuestion)mQuestion).setAm_pm(i == 0? TimeQuestion.AM : TimeQuestion.PM);
                     Date time = mTimeTextField.getTime();
                     Long timelong = new Long(time.getTime());
                     ((TimeAnswer)mAnswer).setTime( timelong.longValue() );
                     ((TimeAnswer)mAnswer).setAmPm24(i == 0? TimeQuestion.AM : TimeQuestion.PM);
                     mAnswer.setVisited(true);
                     mQuestion.setVisited(true);
                     return;
                 }
             }
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEditable(enabled);
             mTimeTextField.setEnabled(enabled);
             for( int i = 0; i < mAmPmGroupButton.getButtonCount(); i++)
             {
                 mAmPmGroupButton.getRadioButton(i).setEnabled(enabled);
             }
         }
 
         public void handleMoreDetails( Object obj )
         {
             if ( ((TimeQuestion)getQuestion()).getConvention() != 24) {
             RadioButton rb = (RadioButton)mAmPmGroupButton.getRadioButton(mAmPmGroupButton.getSelectedIndex());
                 if ( rb.getText().equals("am")) {
                        ((TimeQuestion)getQuestion()).setAm_pm(TimeQuestion.AM);
                 } else {
                      if ( rb.getText().equals("pm")) {
                          ((TimeQuestion)getQuestion()).setAm_pm(TimeQuestion.PM);
                      }
                 }
             }
         }
      }
 
     class HandleInterviewAnswersModified implements DataChangedListener {
 
         public void dataChanged(int arg0, int arg1) {
             // Modification in InterviewForm content
             setModifiedInterview(true);
         }
     }
 
   class HandleMoreDetails implements ActionListener {
 
     public void actionPerformed(ActionEvent evt) {
             setModifiedInterview(true);
             Object cmd = evt.getSource();
             if ( cmd instanceof CheckBox )
             {
                 ContainerUI parent = (ContainerUI)((CheckBox)cmd).getParent();
                 parent.handleMoreDetails( cmd );
             }
             else if (cmd instanceof RadioButton)
             {
                 ContainerUI parent = (ContainerUI)((RadioButton)cmd).getParent();
                 parent.handleMoreDetails(cmd);
                 if ( parent instanceof ExclusiveChoiceFieldUI
                   && parent.getQuestion() instanceof ChoiceQuestion )
                 {
                     updateSkippedQuestion( (ChoiceQuestion)((ExclusiveChoiceFieldUI)parent).getQuestion(),
                                            (ChoiceAnswer)((ExclusiveChoiceFieldUI)parent).getAnswer() );
                 }
             }
             else if((cmd instanceof List))
             {
                 final List list = (List) cmd;
                 if (list.getRenderer() instanceof CheckBox) {
                     ((OptionSelectable) list.getSelectedItem()).toggleSelection();
                     //Component[] group = (Component[]) vGroups.elementAt(focusIndex);
                     //((CheckBox) group[list.getSelectedIndex()]).setSelected(((OptionSelectable) list.getSelectedItem()).getSelected());
                 } else if (list.getRenderer() instanceof RadioButton) {
                     Object obj = list.getSelectedItem();
                     if( obj != null )
                     {
                         ((OptionSelectableRadio) list.getSelectedItem()).setSelected(true);
                         ExclusiveChoiceFieldAutoCompleteUI parent = (ExclusiveChoiceFieldAutoCompleteUI)list.getParent().getParent();
                         parent.handleMoreDetails(list);
                         updateSkippedQuestion( (ChoiceQuestion)parent.getQuestion(),
                                                (ChoiceAnswer)parent.getAnswer() );
                     }
                 }
             }
         }
     }
 
    class ManagerOptionSelectableRadio {
 
         private Vector options = new Vector();
 
         public void addOption(OptionSelectableRadio option) {
             if (!options.contains(option)) {
                 options.addElement(option);
             }
         }
         //todo remove from vector
 
         public void selectOption(OptionSelectableRadio option) {
             if (option.getSelected()) {
                 Enumeration options = this.options.elements();
                 while (options.hasMoreElements()) {
                     OptionSelectableRadio opt = (OptionSelectableRadio) options.nextElement();
                     if (!opt.toString().equals(option.toString()))
                     {
                         opt.setSelected(false);
                     }
                 }
             }
         }
     }
 
    class OptionSelectable {
 
         private boolean selected = false;
         private String value;
 
         public OptionSelectable(String value) {
             this.value = value;
         }
 
         //public void toggleSelection() {
         public boolean toggleSelection() {
             this.selected = !selected;
             //return selected;
             return selected;
         }
 
         public String toString() {
             return value;
         }
 
         public boolean getSelected() {
             return selected;
         }
     }
 
     class OptionSelectableRadio {
 
         private boolean selected = false;
         private String value;
         private ManagerOptionSelectableRadio group;
 
         public OptionSelectableRadio(String value, ManagerOptionSelectableRadio group) {
             this.value = value;
             this.group = group;
             this.group.addOption(this);
         }
 
         //public void toggleSelection() {
         public boolean toggleSelection() {
             this.selected = !selected;
             if (this.selected) {
 
                 group.selectOption(this);
             }
             return selected;
         }
 
         public String toString() {
             return value;
         }
 
         public boolean getSelected() {
             return selected;
         }
 
         public void setSelected(boolean selected) {
             this.selected = selected;
             if (this.selected) {
                 group.selectOption(this);
             }
         }
 
         public boolean equals(Object obj) {
             return value.equals(obj);
         }
 
         public int hashCode() {
             return value.hashCode();
         }
     }
 
     class RadioButtonRenderer extends RadioButton implements ListCellRenderer {
 
         public RadioButtonRenderer(String text) {
             super(text);
         }
 
         public Component getListCellRendererComponent(com.sun.lwuit.List list, Object o, int i, boolean isSelected) {
             if( o == null )
             {
                 return this;
             }
             setSelected(((OptionSelectableRadio) o).getSelected());
             setText(o.toString());
 
             if (isSelected) {
                 setFocus(true);
                 getStyle().setBgPainter(focusBGPainter);
 //                getStyle().setFont( NDGStyleToolbox.fontLargeBold );
             } else {
                 setFocus(false);
                 getStyle().setBgPainter(bgPainter);
 //                getStyle().setFont( NDGStyleToolbox.fontLarge );
             }
             return this;
         }
 
         public Component getListFocusComponent(com.sun.lwuit.List list) {
             return this;
         }
     }
 }
