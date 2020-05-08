 /**
  * 
  */
 package com.tmm.maker.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
 import javax.persistence.PrePersist;
 import javax.persistence.Table;
 
 import com.tmm.maker.domain.enums.ProjectCategory;
 import com.tmm.maker.domain.enums.TimePeriods;
 import com.tmm.maker.security.Account;
 import com.tmm.maker.service.ApplicationService;
 
 /**
  * a project
  * @author robhinds
  */
 @Entity
 @Table(name = "PROJECT")
 public class Project extends PersistableObject {
 	
 	private static final long serialVersionUID = 1L;
 
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = { CascadeType.ALL })
 	List<ProjectStep> steps = new ArrayList<ProjectStep>();
 	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = { CascadeType.ALL })
 	List<Ingredient> ingredients = new ArrayList<Ingredient>();
 
 	@Column
 	private String title;
 	
 	@Column
 	private String description;
 	
 	@Column 
 	private Integer stepNumber;
 	
 	@Enumerated(EnumType.STRING)
 	private ProjectCategory category;
 	
 	@Enumerated(EnumType.STRING)
 	private TimePeriods timeToComplete;
 	
 	@ManyToOne
 	@JoinColumn
 	private Profile user;
 
 
 	public List<Ingredient> getIngredients() {
 		return ingredients;
 	}
 
 	public void setIngredients(List<Ingredient> ingredients) {
 		this.ingredients = ingredients;
 	}
 
 	public List<ProjectStep> getSteps() {
 		return steps;
 	}
 
 	public void setSteps(List<ProjectStep> steps) {
 		this.steps = steps;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Integer getStepNumber() {
 		return stepNumber;
 	}
 
 	public void setStepNumber(Integer stepNumber) {
 		this.stepNumber = stepNumber;
 	}
 
 	public ProjectCategory getCategory() {
 		return category;
 	}
 
 	public void setCategory(ProjectCategory category) {
 		this.category = category;
 	}
 
 	public TimePeriods getTimeToComplete() {
 		return timeToComplete;
 	}
 
 	public void setTimeToComplete(TimePeriods timeToComplete) {
 		this.timeToComplete = timeToComplete;
 	}
 	
 	public void addProjectStep(ProjectStep step){
 		getSteps().add(step);
 	}
 	
 	public void removeProjectStep(ProjectStep step){
 		getSteps().remove(step);
 	}
 	
 	public void addIngredient(Ingredient i){
 		getIngredients().add(i);
 	}
 	
 	public void removeIngredient(Ingredient i){
 		getIngredients().remove(i);
 	}
 
 	public Profile getUser() {
 		return user;
 	}
 
 	public void setUser(Profile user) {
 		this.user = user;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Project other = (Project) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 	
 	
 	@PrePersist
 	public void distributeNotifications() {
 		Account acc = ApplicationService.getInstance().getLoggedInUserAccount();
 		Profile user = acc.getUserProfile();
 		List<Profile> notified = new ArrayList<Profile>();
 		createNotification(user, notified);
 		createNotificationFollowers(user, notified);
 	}
 
 	private void createNotificationFollowers(Profile user, List<Profile> notified) {
 		for (Profile p : user.getFollowers()) {
 			createNotification(p, notified);
 		}
 	}
 
 	private void createNotification(Profile p, List<Profile> notified) {
 		if (!notified.contains(p)) {
 			ProjectNotification n = new ProjectNotification();
 			n.setProject(this);
 			notified.add(p);
 		}
 	}
 
 }
