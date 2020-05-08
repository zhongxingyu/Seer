 package amu.licence.edt.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 
 import amu.licence.edt.model.beans.Admin;
 import amu.licence.edt.model.beans.CRoom;
 import amu.licence.edt.model.beans.CRoomType;
 import amu.licence.edt.model.beans.Group;
 import amu.licence.edt.model.beans.Level;
 import amu.licence.edt.model.beans.Promo;
 import amu.licence.edt.model.beans.SessionType;
 import amu.licence.edt.model.beans.TU;
 import amu.licence.edt.model.beans.Teacher;
 import amu.licence.edt.model.dao.DAOFactoryManager;
 
 public class Model {
 
     private Admin user;
     private Schedule schedule;
 
     private List<ModelObserver> observers;
 
     public Model() {
         super();
         this.observers = new ArrayList<ModelObserver>();
         this.schedule = new Schedule();
     }
 
     public void fireUserChanged() {
         for (ModelObserver o : observers) {
             o.userChanged(user);
         }
     }
 
     public void fireScheduleChanged() {
         for (ModelObserver o : observers) {
             o.scheduleChanged(schedule);
         }
     }
 
     public Admin getUser() {
         return user;
     }
 
     public void setUser(Admin user) {
         this.user = user;
     }
 
     public Schedule getSchedule() {
         return schedule;
     }
 
     public void setSchedule(Schedule schedule) {
         this.schedule = schedule;
     }
 
     public boolean addObserver(ModelObserver o) {
         return this.observers.add(o);
     }
 
     public boolean removeObserver(ModelObserver o) {
         return this.observers.remove(o);
     }
 
     public boolean tryToConnect(String login, String password) {
         Admin a = DAOFactoryManager.getDAOFactory().getDAOAdmin().findByLoginPassword(login, password);
         if (a == null)
             return false;
 
         user = a;
         fireUserChanged();
         return true;
     }
 
     public void disconnect() {
         user = null;
         fireUserChanged();
     }
 
     public TreeNode getScheduleRootNode() {
         DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Emplois du temps");
         rootNode.add(createStudentNode());
         rootNode.add(createTeacherNode());
         rootNode.add(createCRoomNode());
         return rootNode;
     }
 
     public MutableTreeNode createStudentNode() {
         DefaultMutableTreeNode students = new DefaultMutableTreeNode("Etudiant");
         for (Level l : DAOFactoryManager.getDAOFactory().getDAOLevel().findAll()) {
             DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(l);
             Promo p = l.getPromo();
            for (Group g : p.getGroup()) {
                 DefaultMutableTreeNode gNode = new DefaultMutableTreeNode(g);
                 pNode.add(gNode);
             }
             students.add(pNode);
         }
         return students;
     }
 
     public MutableTreeNode createTeacherNode() {
         DefaultMutableTreeNode teachers = new DefaultMutableTreeNode("Enseignant");
 
         for (Teacher t : DAOFactoryManager.getDAOFactory().getDAOTeacher().findAll()) {
             DefaultMutableTreeNode tNode = new DefaultMutableTreeNode(t);
             teachers.add(tNode);
         }
         return teachers;
     }
 
     public MutableTreeNode createCRoomNode() {
         DefaultMutableTreeNode crooms = new DefaultMutableTreeNode("Salle");
 
         for (CRoomType crType : DAOFactoryManager.getDAOFactory().getDAOCRoomType().findAll()) {
             DefaultMutableTreeNode crTypeNode = new DefaultMutableTreeNode(crType);
             for (CRoom cr : crType.getCrooms()) {
                 DefaultMutableTreeNode croomNode = new DefaultMutableTreeNode(cr);
                 crTypeNode.add(croomNode);
             }
             crooms.add(crTypeNode);
         }
         return crooms;
     }
 
     public void setSessionsOfTeacher(Teacher t) {
         schedule.setSessions(DAOFactoryManager.getDAOFactory().getDAOSession().findByTeacherPeriod(t, schedule.getFirstWeekDay(), 5));
         fireScheduleChanged();
     }
 
     public void setSessionsOfGroup(Group g) {
         schedule.setSessions(DAOFactoryManager.getDAOFactory().getDAOSession().findByGroupPeriod(g, schedule.getFirstWeekDay(), 5));
         fireScheduleChanged();
     }
 
     public void setSessionsOfCRoom(CRoom cr) {
         schedule.setSessions(DAOFactoryManager.getDAOFactory().getDAOSession().findByCRoomPeriod(cr, schedule.getFirstWeekDay(), 5));
         fireScheduleChanged();
     }
 
     public void setSessionsOfLevel(Level l) {
         schedule.setSessions(DAOFactoryManager.getDAOFactory().getDAOSession().findByLevelPeriod(l, schedule.getFirstWeekDay(), 5));
         fireScheduleChanged();
     }
 
     public List<Teacher> getTeachersList() {
         return DAOFactoryManager.getDAOFactory().getDAOTeacher().findAll();
     }
 
     public void updateTeacher(Teacher t) {
         DAOFactoryManager.getDAOFactory().getDAOTeacher().update(t);
     }
 
     public void findNextWeek() {
         schedule.setFirstWeekDay(schedule.computeNextWeek());
         schedule.setSessions(DAOFactoryManager.getDAOFactory().getDAOSession().executeFindSessionByPeriod(schedule.getFirstWeekDay(), 5));
         fireScheduleChanged();
     }
 
     public void findPrevWeek() {
         schedule.setFirstWeekDay(schedule.computePrevWeek());
         schedule.setSessions(DAOFactoryManager.getDAOFactory().getDAOSession().executeFindSessionByPeriod(schedule.getFirstWeekDay(), 5));
         fireScheduleChanged();
     }
 
     public Iterable<Object[]> getUnplannedSessions(Level l) {
         return DAOFactoryManager.getDAOFactory().getDAOSession().findUnplanned(l);
     }
 
     public List<CRoom> getAvailableCRooms(SessionType st, Date date, Integer duration) {
         return DAOFactoryManager.getDAOFactory().getDAOCRoom().findAvailables(st, date, duration);
     }
 
     public List<Teacher> getAvailableTeachers(TU tu, Date date, Integer duration) {
         return DAOFactoryManager.getDAOFactory().getDAOTeacher().findAvailables(tu, date, duration);
     }
 
 }
