 package br.ufsm.dsweb.model;
 
 import java.io.Serializable;
 
 import br.ufsm.dsweb.dao.UserDAO;
 import br.ufsm.dsweb.db.DBCore;
 
 public class Follows extends Model implements Serializable {
 
 	private User mFollower;
 	private User mFollowed;
 
 	public User getFollower() {
 		return mFollower;
 	}
 
 	public void setFollower(User following) {
 		this.mFollower = following;
 	}
 
 	public User getFollowed() {
 		return mFollowed;
 	}
 
 	public void setFollowed(User followed) {
 		this.mFollowed = followed;
 	}
 
 	@Override
 	public String toCSV() {
 		return mFollower.getID()+DBCore.SEPARATOR+mFollowed.getID();
 	}
 
 	@Override
 	public void fromCSV(String csv) {
 		String[] vals = csv.split(DBCore.SEPARATOR);
		setFollower(new UserDAO().getByID(Integer.parseInt(vals[0])));
		setFollowed(new UserDAO().getByID(Integer.parseInt(vals[1])));
 	}
 }
