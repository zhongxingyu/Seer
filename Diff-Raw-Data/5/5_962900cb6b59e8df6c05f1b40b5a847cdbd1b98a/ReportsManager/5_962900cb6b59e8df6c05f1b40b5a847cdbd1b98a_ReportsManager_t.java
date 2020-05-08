 package to.joe.j2mc.reports;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 
 import to.joe.j2mc.core.J2MC_Manager;
 
 public class ReportsManager {
 	
 	J2MC_Reports plugin;
 	public ReportsManager(J2MC_Reports Reports){
 		this.plugin = Reports;
 	}
 	
 	private ArrayList<Report> reports;
 	private final Object sync = new Object();
 	
     /**
      * Get list of reports.
      * 
      * @return
      */
     public ArrayList<Report> getReports() {
         return new ArrayList<Report>(this.reports);
     }
     
     /**
      * Get report by id.
      * 
      * @param id
      * @return
      */
     public Report getReport(int id) {
         synchronized (this.sync) {
             for (final Report r : this.reports) {
                 if (r.getID() == id) {
                     return r;
                 }
             }
             return null;
         }
     }
     
     /**
      * Close report
      * 
      * @param id
      * @param admin
      * @param reason
      */
     public void closeReport(int id, String admin, String reason){
     	final Report r = this.getReport(id);
     	if(r != null){
     		try {
     			PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("UPDATE reports SET closed=1,admin=?,reason=? where id=?");
     			ps.setString(1, admin);
     			ps.setString(2, reason);
     			ps.setInt(3, id);
     			ps.executeUpdate();
     			this.reports.remove(r);
     		} catch (SQLException e) {
     			e.printStackTrace();
     		} catch (ClassNotFoundException e) {
     			e.printStackTrace();
     		}
     	}
     
     }
 
 	public void AddReportFromCommand(Report report){
 		try{
 			PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementWithGeneratedKeys("INSERT INTO `reports` (`user`,`message`,`x`,`y`,`z`,`pitch`,`yaw`,`server`,`world`,`time`) VALUES (?,?,?,?,?,?,?,?,?,?)");
 			ps.setString(1, report.getUser());
 			ps.setString(2, report.getMessage());
 			ps.setDouble(3, report.getLocation().getX());
 			ps.setDouble(4, report.getLocation().getY());
 			ps.setDouble(5, report.getLocation().getZ());
 			ps.setFloat(6, report.getLocation().getPitch());
 			ps.setFloat(7, report.getLocation().getYaw());
 			ps.setInt(8, J2MC_Manager.getServerID());
 			ps.setString(9, report.getLocation().getWorld().getName());
			ps.setLong(10, report.getTime());
 			ps.executeUpdate();
 			ResultSet rs = ps.getGeneratedKeys();
 		    rs.next();
 		    int auto_id = rs.getInt(1);
 		    Report ReportToAdd = new Report(auto_id, report.getLocation(), report.getUser(), report.getMessage(), report.getTime(), false);
 		    this.reports.add(ReportToAdd);
 	        final Location location = ReportToAdd.getLocation();
 	        final String pc = ChatColor.DARK_PURPLE.toString();
 	        final String gc = ChatColor.GOLD.toString();
 	        final String wc = ChatColor.WHITE.toString();
 	        final String x = gc + location.getBlockX() + pc + ",";
 	        final String y = gc + location.getBlockY() + pc + ",";
 	        final String z = gc + location.getBlockZ() + pc;
 	        final String message = pc + "[" + wc + "NEW REPORT" + pc + "][" + report.getID() + "][" + x + y + z + "]<" + gc + ReportToAdd.getUser() + pc + "> " + wc + ReportToAdd.getMessage();
 	        J2MC_Manager.getCore().adminAndLog(message);
 		}catch(SQLException e){
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 		}
 	}
 	
 	/**
 	 * Loads data from SQL table onEnable
 	 */
 	public void LoadDataIntially(){
 		plugin.Manager.reports = new ArrayList<Report>();
 		try {
 			PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT id,user,x,y,z,pitch,yaw,message,world,time,closed from reports where server=? and closed=0");
 			ps.setInt(1, J2MC_Manager.getServerID());
 			ResultSet rs = ps.executeQuery();
 			while(rs.next()){
 				Location loc = new Location(plugin.getServer().getWorld(rs.getString("world")), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"));
 				Report r = new Report(rs.getInt("id"), loc, rs.getString("user"), rs.getString("message"), rs.getLong("time"), rs.getBoolean("closed"));
 				plugin.Manager.reports.add(r);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 }
