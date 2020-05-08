 package com.xx.abel.service.impl;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.xx.abel.bean.Bug;
 import com.xx.abel.bean.Users;
 import com.xx.abel.dao.intf.BugDao;
 import com.xx.abel.service.intf.BugService;
 
 @Service
 public class BugServiceImpl implements BugService {
 	@Autowired
 	private BugDao bugDao;
 
 	public List<Bug> findAll() {
 		Users user = UserServiceImpl.getUser();
 		List<Bug> list = bugDao.findByProperty(Bug.class, "users.id", user
 				.getId(), 1);
 		List<Bug> l = new LinkedList<Bug>();
 		SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd");
 		for (Bug bug : list) {
 			Calendar c = Calendar.getInstance();
 			c.setTime(bug.getCreateTime());
 			c.add(c.MONTH, -1);
 			Date te = c.getTime();
 			bug.setCreateTime(te);
 			l.add(bug);
 		}
 		return l;
 	}
 
 	public void save(Bug bug) {
 		if (bug.getCreateTime() == null)
 			bug.setCreateTime(new Date());
		bugDao.saveOrUpdate(bug);
	}

	public Bug findById(int id) {
		return bugDao.findById(Bug.class, id);
	}

	public void delete(Bug bug) {
		bugDao.deleteById(Bug.class,bug.getId());
 	}
 
 }
