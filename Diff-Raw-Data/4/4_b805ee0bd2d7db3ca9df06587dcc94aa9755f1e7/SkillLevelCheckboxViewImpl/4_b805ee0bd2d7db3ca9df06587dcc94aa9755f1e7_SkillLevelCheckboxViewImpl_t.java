 package logbook.client.a_nonroo.app.client.ui;
 

 
 import logbook.client.managed.proxy.SkillProxy;
 
import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SkillLevelCheckboxViewImpl   extends Composite {
 
 	private static final Binder BINDER = GWT.create(Binder.class);
 	
 	interface Binder extends UiBinder<Widget, SkillLevelCheckboxViewImpl> {
 	}
 	
 	
 	@UiField
 	CheckBox checkbox;
 	
 	private SkillProxy skillProxy;
 	
 	public SkillProxy getSkillProxy() {
 		return skillProxy;
 	}
 	
 	public boolean isLevel1=false;
 	public boolean isLevel1() {
 		return isLevel1;
 	}
 
 	public void setLevel1(boolean isLevel1) {
 		this.isLevel1 = isLevel1;
 	}
 
 	public void setSkillProxy(SkillProxy skillProxy) {
 		this.skillProxy = skillProxy;
 	}
 
 	public SkillLevelCheckboxViewImpl()
 	{
 		initWidget(BINDER.createAndBindUi(this));
 	}
 	
 	@UiHandler("checkbox")
 	public void checkBoxClicked(ClickEvent event)
 	{
 		Log.info("checkBoxClicked skill :" + skillProxy.getDescription());
 		if(checkbox.getValue())
 		{
 			
 		}
 		else
 		{
 			
 		}
 	}
 }
