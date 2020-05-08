 package persistency;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.Developer;
 import model.Project;
 
 public class ProjectRepository extends Repository<Project> {
 
 	public ProjectRepository(Database db) {
 		super(db);
 
 		this.table = "project";
 		this.columns = new String[] { "name", "hour_budget", "deadline",
 				"manager_id" };
 	}
 
 	public Project create(String name, int hourBudget, long deadline, Developer manager) {
 		int id = this.create(new String[] { name, String.valueOf(hourBudget),
 				String.valueOf(deadline), String.valueOf(manager.getId()) });
 		Project project = new Project(this.db, id, name, hourBudget, deadline,
 				manager);
 		return project;
 	}
 	
 	public Project create(String name, int hourBudget, long deadline) {
 		int id = this.create(new String[] { name, String.valueOf(hourBudget),
 				String.valueOf(deadline), "-1" });
 		Project project = new Project(this.db, id, name, hourBudget, deadline);
 		return project;
 	}
 
 	@Override
 	protected List<Project> parse(ResultSet rs) {
 		List<Project> projects = new ArrayList<Project>();
 		try {
 			while (rs.next()) {
 				projects.add(new Project(this.db, rs.getInt("id"), rs
 						.getString(this.columns[0]),
						rs.getInt(this.columns[1]), rs.getInt(this.columns[2]),
						this.db.developer().read(rs.getInt(this.columns[3]))));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return projects;
 	}
 
 }
