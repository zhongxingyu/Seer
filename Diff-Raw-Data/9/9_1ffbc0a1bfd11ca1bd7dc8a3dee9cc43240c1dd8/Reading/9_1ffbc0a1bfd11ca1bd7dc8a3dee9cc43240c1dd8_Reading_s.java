 package org.kalibro;
 
 import java.awt.Color;
 
 import org.kalibro.core.abstractentity.AbstractEntity;
 import org.kalibro.core.abstractentity.Ignore;
 import org.kalibro.core.abstractentity.Print;
 import org.kalibro.core.abstractentity.SortingFields;
 import org.kalibro.core.dao.DaoFactory;
 import org.kalibro.core.dao.ReadingDao;
 
 /**
  * Interpretation of a metric result.
  * 
  * @author Carlos Morais
  */
 @SortingFields("grade")
 public class Reading extends AbstractEntity<Reading> {
 
 	@Print(skip = true)
 	private Long id;
 
 	private String label;
 	private Double grade;
 	private Color color;
 
 	@Ignore
 	private ReadingGroup group;
 
 	public Reading() {
 		this("", 0.0, Color.WHITE);
 	}
 
 	public Reading(String label, Double grade, Color color) {
 		setId(null);
 		setLabel(label);
 		setGrade(grade);
 		setColor(color);
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getLabel() {
 		return label;
 	}
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public Double getGrade() {
 		return grade;
 	}
 
 	public void setGrade(Double grade) {
 		this.grade = grade;
 	}
 
 	public Color getColor() {
 		return color;
 	}
 
 	public void setColor(Color color) {
 		this.color = color;
 	}
 
 	protected void assertNoConflictWith(Reading other) {
 		if (getLabel().equals(other.getLabel()))
			throw new KalibroException("Reading with label '" + getLabel() + "' already exists in the group.");
 		if (getGrade().equals(other.getGrade()))
 			throw new KalibroException("Reading with grade " + getGrade() + " already exists in the group.");
 	}
 
 	protected void setGroup(ReadingGroup group) {
 		this.group = group;
 	}
 
 	public void save() {
 		if (group == null)
 			throw new KalibroException("Reading is not in any group.");
 		dao().save(this);
 	}
 
 	public void delete() {
 		if (id != null)
 			dao().delete(id);
 		if (group != null)
 			group.removeReading(this);
 	}
 
 	private ReadingDao dao() {
 		return DaoFactory.getReadingDao();
 	}
 }
