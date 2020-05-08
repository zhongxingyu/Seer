 package eu.comexis.napoleon.client.core.tenant;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.ViewImpl;
 
 import eu.comexis.napoleon.client.utils.UiHelper;
 import eu.comexis.napoleon.shared.model.Tenant;
 
 public class TenantDetailsView extends ViewImpl implements TenantDetailsPresenter.MyView {
 
   public interface Binder extends UiBinder<Widget, TenantDetailsView> {
   }
 
   private final Widget widget;
 
   private TenantDetailUiHandlers presenter;
   @UiField
   Element title;
   @UiField
   Element name;
   @UiField
   Element firstName;
   @UiField
   Element email;
   @UiField
   Element phoneNumber;
   @UiField
   Element mobileNumber;
   @UiField
   Element birthDay;
   @UiField
   Element addresse;
   @UiField
   Element maritalStatus;
   @UiField
   Element matrimonialRegime;
   @UiField
   Element bic;
   @UiField
   Element iban;
   @UiField
   Element nationalRegister;
   @UiField
   Element nationality;
   @UiField
   Element job;
   @UiField
   Element fax;
   @UiField
   Element placeOfBirth;
 
   @Inject
   public TenantDetailsView(final Binder binder) {
     widget = binder.createAndBindUi(this);
 
   }
 
   @Override
   public Widget asWidget() {
     return widget;
   }
 
   @UiHandler("btnDelete")
   public void onDeleteClicked(ClickEvent e) {
     Window.alert("Supprimer");
   }
 
   @UiHandler("btnToList")
   public void onGoToListClicked(ClickEvent e) {
     presenter.onButtonBackToListClick();
   }
 
   @UiHandler("btnUpdate")
   public void onUpdateClicked(ClickEvent e) {
     presenter.onButtonUpdateClick();
   }
 
   @Override
   public void setTenant(Tenant o) {
     title.setInnerText(UiHelper.translateEnum("Title_", o.getTitle()));
     name.setInnerText(o.getLastName());
     firstName.setInnerText(o.getFirstName());
     iban.setInnerText(o.getIban());
     bic.setInnerText(o.getBic());
     email.setInnerText(o.getEmail());
     phoneNumber.setInnerText(o.getPhoneNumber());
     mobileNumber.setInnerText(o.getMobilePhoneNumber());
     fax.setInnerText(o.getFax());
     placeOfBirth.setInnerText(o.getPlaceOfBirth());
     nationality.setInnerText(o.getNationality());
     job.setInnerText(o.getJobTitle());
     nationalRegister.setInnerText(o.getNationalRegisterNumber());
    birthDay.setInnerText(o.getDateOfBirth() != null ? o.getDateOfBirth().toGMTString() : "");
     addresse.setInnerText(o.getStreet() + " " + o.getCity() + " " + o.getCountry());
     maritalStatus.setInnerText(o.getMaritalStatus() != null ? UiHelper.translateEnum("MaritalStatus_", o.getMaritalStatus()): "");
     matrimonialRegime.setInnerText(o.getMatrimonialRegime() != null ? UiHelper.translateEnum("MatrimonialRegime_", o.getMatrimonialRegime()): "");
 
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void setTenantDetailUiHandler(TenantDetailUiHandlers handler) {
     this.presenter = handler;
 
   }
 }
