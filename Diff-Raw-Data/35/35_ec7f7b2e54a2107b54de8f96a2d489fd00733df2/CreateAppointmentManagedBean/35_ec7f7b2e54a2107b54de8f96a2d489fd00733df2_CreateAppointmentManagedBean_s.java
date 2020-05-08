 package com.acme.hospital.web.managedbeans;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
import org.primefaces.context.RequestContext;
import org.springframework.security.authentication.AuthenticationManager;

 import com.acme.hospital.domain.Doctor;
 import com.acme.hospital.domain.Patient;
 import com.acme.hospital.web.adapter.AppointmentFacadeSpringAdapter;
 
 @ManagedBean(name = "createAppointmentMB")
 @RequestScoped
 public class CreateAppointmentManagedBean {
 
 	@ManagedProperty(value = "#{appointmentFacadeSpringAdapter}")
 	private AppointmentFacadeSpringAdapter afsa;
 
 	@ManagedProperty(value = "#{LoginManagedBean}")
 	private LoginManagedBean loginManagedBean;
 
 	private Date date;
 
 	private Patient selectedPatient;
 
 	private List<Patient> allPatients;
 
 	public void createAppointment() {
 		boolean result = false;
		if (date == null || selectedPatient == null) {
		} else {
 			String loggedInDoctorName = loginManagedBean.getLoggedInUser();
 			Doctor d = afsa.getAppointmentFacade().getDoctorByName(
 					loggedInDoctorName);
 			result = afsa.getAppointmentFacade().createAppointment(d,
 					selectedPatient, date);
 		}
 		if (result) {
 			FacesContext.getCurrentInstance().addMessage(
 					null,
 					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
 							"The appointment creation was successful"));
 		} else {
 			FacesContext.getCurrentInstance().addMessage(
 					null,
 					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
 							"The appointment creation was unsuccessful"));
 		}
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public List<Patient> getAllPatients() {
 		this.allPatients = afsa.getAllPatients();
 		return this.allPatients;
 	}
 
 	public Patient getSelectedPatient() {
 		return selectedPatient;
 	}
 
 	public void setSelectedPatient(Patient selectedPatient) {
 		this.selectedPatient = selectedPatient;
 	}
 
 	public AppointmentFacadeSpringAdapter getAfsa() {
 		return afsa;
 	}
 
 	public void setAfsa(AppointmentFacadeSpringAdapter afsa) {
 		this.afsa = afsa;
 	}
 
 	public LoginManagedBean getLoginManagedBean() {
 		return loginManagedBean;
 	}
 
 	public void setLoginManagedBean(LoginManagedBean loginManagedBean) {
 		this.loginManagedBean = loginManagedBean;
 	}
 }
