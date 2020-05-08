 package monbulk.shared.Form;
 
 import java.util.Date;
 
 import arc.gui.gwt.widget.input.TextArea;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent.Type;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.i18n.client.HasDirection.Direction;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import monbulk.shared.Form.DateValidation;
 import monbulk.shared.Form.IntegerValidation;
 import monbulk.shared.Form.StringValidation;
 import monbulk.shared.Form.TextFieldValidation;
 
 	public class FormField implements iFormField
 	{
 		private String FieldName;
 		private String FieldType;
 		private Object FieldValue;
 		private String FieldWidget;
 		private Widget _FieldVaLueWidget;
 		private Widget _ValidationWidget;
 		private iFormFieldValidation fieldValidator;
 		private Boolean isSummaryField;
 		private Boolean isTitleField;
 		private Boolean isStatic;
 		private Boolean hasValue;
 		private FormWidget returnWidget;
 		public FormField(String FormName,String objectType)
 		{
 			this.FieldName = FormName;
 			this.FieldType = objectType;
 			setFieldWidget();
 			isSummaryField = false;
 			isTitleField = false;
 			isStatic = false;
 			hasValue = false;
 		}
 		
 		public FormField(String FormName,String objectType, Object Value)
 		{
 			this.FieldName = FormName;
 			this.FieldType = objectType;
 			this.FieldValue = Value;
 			this.FieldWidget="Label";
 			this.fieldValidator = null;
 			isSummaryField = false;
 			isTitleField = false;
 			isStatic = true;
 			hasValue = true;
 		}
 		@Override
 		public Boolean hasValue()
 		{
 			return this.hasValue;
 		}
 		@Override
 		public void setAsSummaryField()
 		{
 			this.isSummaryField = true;
 		}
 		@Override
 		public void setAsTitleField()
 		{
 			this.isTitleField = true;
 		}
 		@Override
 		public Boolean isStatic()
 		{
 			return this.isStatic;
 		}
 		@Override
 		public Boolean isSummary()
 		{
 			return this.isSummaryField;
 		}
 		@Override
 		public Boolean isTitle()
 		{
 			return this.isTitleField;
 		}
 		private void setFieldWidget()
 		{		
 			Label FieldNameLabel = new Label();
 			FieldNameLabel.setText(this.FieldName);
 			this._ValidationWidget = new Label();
 			this.returnWidget = new FormWidget(this.FieldName);
 			if(FieldType=="String")
 			{
 				this.FieldWidget="TextBox";
 				this.FieldValue="Enter Text here";
 				this._FieldVaLueWidget = new TextBox();
 				
 				this._ValidationWidget = new Label();
 				
 				
 				this.fieldValidator = new StringValidation(this.FieldName);
 			}
 			else if(FieldType=="Date")
 			{
 				this.FieldWidget="Calendar";
 				Date tmpDate = new Date();
 				this.FieldValue= tmpDate.toString();
 				this.fieldValidator = new DateValidation(this.FieldName);
 				this._FieldVaLueWidget = new TextBox();
 			}
 			else if(FieldName=="Description")
 			{
 				this.FieldWidget="TextArea";
 				this.FieldValue="Enter Text here";
 				this.fieldValidator = new TextFieldValidation(this.FieldName);
 				this._FieldVaLueWidget = new TextArea();
 				
 				
 			}
 			else if(FieldName=="Int")
 			{
 				this.FieldWidget="TextBox";
 				this.FieldValue="Enter Number here";
 				this.fieldValidator = new IntegerValidation(this.FieldName);
 				this._FieldVaLueWidget = new TextBox();
 			}
 			else if(FieldName=="Boolean")
 			{
 				this.FieldWidget="CheckBox";
 				this.FieldValue=false;
 				this.fieldValidator = null;
 				this._FieldVaLueWidget = new CheckBox();
 			}
 			else if(FieldName=="List")
 			{
 				this.FieldWidget="ListBox";
 				this.FieldValue=false;
 				this.fieldValidator = null;
 				this._FieldVaLueWidget = new Widget();
 			}
 			else
 			{
 				this.FieldWidget="TextBox";
 				this.FieldValue="Enter Number here";
 				this.fieldValidator = new IntegerValidation(this.FieldName);
 				this._FieldVaLueWidget = new TextBox();
 			}
			//FieldNameLabel.setHorizontalAlignment(HorizontalAlignmentConstant.endOf(Direction.LTR));
			//FieldNameLabel.setWidth("80px");
			//_FieldVaLueWidget.setPixelSize(200, 30);
			//this._ValidationWidget.setSize("30px", "30px");
 			this.returnWidget.setLabelWidget(FieldNameLabel);
 			
 			this.returnWidget.setFormWidget(_FieldVaLueWidget);
 			this.returnWidget.setValidWidget(_ValidationWidget);
 			
 		}
 		@Override
 		public String GetFieldName() {
 			// TODO Auto-generated method stub
 			return FieldName;
 		}
 
 		@Override
 		public Object GetFieldValue() {
 			// TODO Auto-generated method stub
 			return FieldValue;
 		}
 
 		@Override
 		public String getFieldTypeName() {
 			// TODO Auto-generated method stub
 			return FieldType;
 		}
 
 		@Override
 		public String GetWidgetName() {
 			// TODO Auto-generated method stub
 			return FieldWidget;
 		}
 		@Override
 		public void SetFieldValue(String FieldValue) {
 			// TODO Auto-generated method stub
 			if(this.Validate(FieldValue)=="")
 			{
 				this.FieldValue = FieldValue;
 				hasValue = true;
 			}
 		}
 		@Override
 		public iFormFieldValidation GetFieldValidation() {
 			// TODO Auto-generated method stub
 			return fieldValidator;
 		}
 
 		@Override
 		public void setFieldName(String newName) {
 			this.FieldName=newName;
 			
 		}
 
 		@Override
 		public FormWidget getWidgetReference() {
 			
 			return returnWidget;
 		}
 
 		@Override
 		public String Validate(String FieldValue) {
 			if(this.fieldValidator.isValueValid(FieldValue))
 			{
 				this._ValidationWidget.setStyleName("Valid");
 				return "";
 			}
 			else
 			{
 				this._ValidationWidget.setStyleName("InValid");
 				return fieldValidator.getInvalidReason();
 			}
 		}
 
 		@Override
 		public <H extends EventHandler> HandlerRegistration addHandler(
 				H handler, Type<H> type) {
 			return this._FieldVaLueWidget.addHandler(handler, type);
 		}
 		
 }
