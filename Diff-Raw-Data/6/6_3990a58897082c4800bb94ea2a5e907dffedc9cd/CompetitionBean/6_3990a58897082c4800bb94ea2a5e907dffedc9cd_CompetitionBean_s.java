 /**
  * 
  */
 package com.jwxicc.cricket.jsf;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.richfaces.component.UIDataTable;
 
 import com.jwxicc.cricket.entity.Competition;
 import com.jwxicc.cricket.entity.Review;
 import com.jwxicc.cricket.interfaces.CompetitionManager;
 
 @ManagedBean(name = "compBean")
 @ViewScoped
 public class CompetitionBean implements Serializable {
 
 	@EJB(name = "compManager")
 	CompetitionManager compManager;
 
 	private List<Competition> compList;
 	private UIDataTable compTable;
 	private int currentRowId = 0;
 	private int pageCompId = 0;
 	private Competition pageComp = null;
 	private int selectedMatchToView;
 
 	public void addRow() {
 		Competition newComp = new Competition();
 		newComp.setEditable(true);
 		this.compList.add(newComp);
 	}
 
 	public void editRow() {
 		findSelectedComp(currentRowId).setEditable(true);
 	}
 
 	public void resetAll() {
 		/*
 		 * System.out.println("start reset"); for (int i = 0; i < compList.size(); i++) {
 		 * Competition comp = compList.get(i); System.out.print("comp id: " +
 		 * comp.getCompetitionId()); if (comp.getCompetitionId() == 0) { compList.remove(i);
 		 * System.out.println(" removed"); } else { comp.setEditable(false);
 		 * System.out.println(" set uneditable"); } } System.out.println("end reset");
 		 */
 		this.compList = compManager.findAll();
 	}
 
 	public void expandComp() {
 		System.out.println("comp id set to: " + this.pageCompId);
		this.pageComp = compManager.getDetailedCompetition(pageCompId);
 	}
 
 	public void addReview() {
 		Review review = this.pageComp.getReview();
 		if (review != null) {
 			review.setCompetition(pageComp);
 			compManager.addSaveReview(pageComp);
 		}
 		System.out.println("saved review");
 	}
 
 	public void showTable() {
 		System.out.println("comp id set to: " + this.pageCompId);
 	}
 
 	public void saveAll() {
 		for (Competition comp : this.compList) {
 			try {
 				if (comp.isEditable()) {
 					System.out.println("assoc: " + comp.getAssociationName() + "; grade: "
 							+ comp.getGrade() + "; season: " + comp.getSeason());
 					if (compManager.validateRequiredFields(comp)) {
 						comp.setEditable(false);
 						if (comp.getCompetitionId() == 0) {
 							compManager.persist(comp);
 						} else {
 							comp = compManager.merge(comp);
 						}
 						FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
 								"Saved competition: " + comp.getAssociationName() + " "
 										+ comp.getGrade() + " " + comp.getSeason(), null);
 						FacesContext.getCurrentInstance().addMessage(null, message);
 					} else {
 						FacesContext
 								.getCurrentInstance()
 								.addMessage(
 										null,
 										new FacesMessage(
 												FacesMessage.SEVERITY_ERROR,
 												"Missing or invalid data in a competition - it was not saved",
 												null));
 					}
 				} else {
 					System.out.println("comp not editable");
 				}
 			} catch (Exception e) {
 				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
 						e.getMessage(), null);
 				FacesContext.getCurrentInstance().addMessage(null, message);
 			}
 		}
 	}
 
 	private Competition findSelectedComp(int id) {
 		for (Competition comp : compList) {
 			if (comp.getCompetitionId() == id) {
 				return comp;
 			}
 		}
 		return null;
 	}
 
 	public String goToMatch() {
 		System.out.println("goToMatch");
 		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
 		try {
 			context.redirect(context.getRequestContextPath() + "/MatchManagement.xhtml?matchId="
 					+ this.selectedMatchToView);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	public List<Competition> getCompList() {
 		if (this.compList == null) {
 			this.compList = compManager.findAll();
 		}
 		return compList;
 	}
 
 	public void setCompList(List<Competition> compList) {
 		this.compList = compList;
 	}
 
 	public UIDataTable getCompTable() {
 		return compTable;
 	}
 
 	public void setCompTable(UIDataTable compTable) {
 		this.compTable = compTable;
 	}
 
 	public int getCurrentRowId() {
 		return currentRowId;
 	}
 
 	public void setCurrentRowId(int currentRowId) {
 		this.currentRowId = currentRowId;
 	}
 
 	public int getPageCompId() {
 		return pageCompId;
 	}
 
 	public void setPageCompId(int pageCompId) {
 		this.pageCompId = pageCompId;
 	}
 
 	public Competition getPageComp() {
 		return pageComp;
 	}
 
 	public void setPageComp(Competition pageComp) {
 		this.pageComp = pageComp;
 	}
 
 	public int getSelectedMatchToView() {
 		return selectedMatchToView;
 	}
 
 	public void setSelectedMatchToView(int selectedMatchToView) {
 		this.selectedMatchToView = selectedMatchToView;
 	}
 
 }
