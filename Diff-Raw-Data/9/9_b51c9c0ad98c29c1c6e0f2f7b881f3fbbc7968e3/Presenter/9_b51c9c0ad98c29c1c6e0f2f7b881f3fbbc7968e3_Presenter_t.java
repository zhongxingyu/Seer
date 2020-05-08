 package amu.licence.edt.presenter;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import amu.licence.edt.controller.Controller;
 import amu.licence.edt.model.ModelObserver;
 import amu.licence.edt.model.Schedule;
 import amu.licence.edt.model.beans.Admin;
 import amu.licence.edt.model.beans.CRoom;
 import amu.licence.edt.model.beans.CRoomType;
 import amu.licence.edt.model.beans.Group;
 import amu.licence.edt.model.beans.Level;
 import amu.licence.edt.model.beans.Session;
 import amu.licence.edt.model.beans.SessionType;
 import amu.licence.edt.model.beans.TU;
 import amu.licence.edt.model.beans.Teacher;
 import amu.licence.edt.view.View;
 import amu.licence.edt.view.renderers.CRoomRenderer;
 import amu.licence.edt.view.renderers.CRoomTypeRenderer;
 import amu.licence.edt.view.renderers.ClassBasedDDR;
 import amu.licence.edt.view.renderers.DMTNUserObjectBasedRenderer;
 import amu.licence.edt.view.renderers.GroupRenderer;
 import amu.licence.edt.view.renderers.LevelRenderer;
 import amu.licence.edt.view.renderers.TeacherRenderer;
 
 public class Presenter implements ModelObserver {
 
     private ClassBasedDDR classBasedDDR;
 
     private View view;
     private Controller controller;
 
     public Presenter(Controller controller) {
         this.classBasedDDR = new ClassBasedDDR();
         classBasedDDR.addRenderer(Teacher.class, new TeacherRenderer());
         classBasedDDR.addRenderer(Level.class, new LevelRenderer());
         classBasedDDR.addRenderer(CRoom.class, new CRoomRenderer());
         classBasedDDR.addRenderer(CRoomType.class, new CRoomTypeRenderer());
         classBasedDDR.addRenderer(Group.class, new GroupRenderer());
         this.controller = controller;
         this.view = new View(this);
     }
 
     public void showView() {
         view.getMainFrame().getFrame().setVisible(true);
     }
 
     @Override
     public void userChanged(Admin user) {
         if (user == null) {
             view.getMainFrame().getAdminStatusPanel().displayPublicUserStatus();
         }
         else {
             view.getMainFrame().getAdminStatusPanel().displayAdminUser(user);
         }
         view.getMainFrame().getFrame().pack();
     }
 
     @Override
     public void scheduleChanged(Schedule schedule) {
         view.getMainFrame().getScheduleStatusPanel().setDate(schedule.getFirstWeekDay());
         view.getMainFrame().getScheduleTableComponent().displaySessions(schedule.getSessions());
     }
 
     public void connectButtonPressed() {
         view.getLoginForm().getComponent().setVisible(true);
     }
 
     public void disconnectButtonPressed() {
         controller.disconnectionRequested();
     }
 
     public void validateLoginButtonPressed(String login, String password) {
         if (controller.validateLoginRequested(login, password)) {
             view.getLoginForm().getComponent().setVisible(false);
             view.getLoginForm().refresh();
         }
         else {
             view.getLoginForm().showErrorLbl(true);
             view.getLoginForm().clearTextFields();
         }
     }
 
     public void manageLevelButtonPressed(Level l) {
         view.getLevelManagmentForm(l).getComponent().setVisible(true);
     }
 
    public void removeSessionButtonPressed() {
         Session s = view.getMainFrame().getScheduleTableComponent().getSelectedSession();
         if (s == null) {
             return;
         }
         controller.removeSessionRequested(s);
         // if no exception, removal is done
         JOptionPane.showMessageDialog(null, "OK.");
     }
 
     public TreeNode scheduleTreeCreating() {
         return controller.scheduleRootNodeRequested();
     }
 
     public Date scheduleStatusCreating() {
         return controller.scheduleDateRequested();
     }
 
     public void treePathChanged(TreePath path) {
         DMTNUserObjectBasedRenderer dmtnRenderer = new DMTNUserObjectBasedRenderer(classBasedDDR);
         String separator = " - ";
         StringBuilder sb = new StringBuilder();
         for (Object o : path.getPath()) {
             sb.append(dmtnRenderer.getStrRender(o));
             sb.append(separator);
         }
         if (sb.length() != 0)
             sb.delete(sb.length() - separator.length(), sb.length());
         view.getMainFrame().getScheduleStatusPanel().setLabelText(sb.toString());
 
         controller.newScheduleSessionsRequested(((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject());
     }
 
     public ClassBasedDDR getClassBasedDDR() {
         return classBasedDDR;
     }
 
     public void manageTeachersButtonPressed() {
         view.getTeacherManagmentForm().getComponent().setVisible(true);
     }
 
     public List<Teacher> teacherManagmentFormCreating() {
         return controller.teachersListRequested();
     }
 
     public void addUnavailabilityButtonPressed(Teacher teacher, Date date, Integer duration) {
         controller.addUnavailabilityRequested(teacher, date, duration);
         // id no exception, change have been made
         JOptionPane.showMessageDialog(null, "OK.");
     }
 
     public void changeNbAdminHoursButtonPressed(Teacher t, int nHours) {
         controller.changeNbHoursAdminRequested(t, nHours);
         // if no exception, change have been made
         JOptionPane.showMessageDialog(null, "OK.");
     }
 
     public void nextWeekButtonPressed() {
         controller.nextWeekRequested();
     }
 
     public void prevWeekButtonPressed() {
         controller.prevWeekRequested();
     }
 
     public List<Session> tableCreating() {
         return controller.scheduleSessionsRequested();
     }
 
     public Iterable<Object[]> levelManagmentFormCreating(Level level) {
         return controller.unplannedSessionsRequested(level);
     }
 
     public void btnSearchCRoomTeacherPressed(TU tu, SessionType st, Group group, Date date, Integer duration) {
         if (controller.availableGroupRequest(group, date, duration)) {
             List<CRoom> crooms = controller.availableCRoomsRequested(group, st, date, duration);
             List<Teacher> teachers = controller.availableTeachersRequested(tu, date, duration);
             view.getCRoomTeacherChooser().fill(crooms, teachers, tu, st, group, date, duration).setVisible(true);
         } else {
             JOptionPane.showMessageDialog(null, "Le groupe n'est pas disponible pour cette période",
                                           "Erreur", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     public void validateAddSessionButtonPressed(Date startDate, Integer duration, TU tu,
                                                 Teacher teacher, CRoom croom, SessionType st,
                                                 Group group) {
         controller.addSessionRequest(startDate, duration, tu, teacher, croom, st, group);
         view.getCRoomTeacherChooser().getComponent().setVisible(false);
     }
 
 }
