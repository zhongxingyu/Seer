 package war.webapp.action;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import war.webapp.Constants;
 import war.webapp.model.*;
 import war.webapp.service.*;
 import war.webapp.util.FacesUtils;
 import war.webapp.util.MonthHelper;
 import war.webapp.util.UserHelper;
 
 import javax.faces.event.ActionEvent;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import static org.springframework.security.context.SecurityContextHolder.getContext;
 
 public class DutyList extends BasePage implements Serializable {
     private static final transient Log logger = LogFactory.getLog(DutyList.class);
 
     private static final long serialVersionUID = 911159310602744018L;
 
     private static final String FIRST_SHIFT = "firstShift";
     private static final String SECOND_SHIFT = "secondShift";
     private static final String FIRST_SHIFT_USER = "firstShiftUser";
     private static final String SECOND_SHIFT_USER = "secondShiftUser";
     private static final String SELECT_USER_STRING = "-";
     private String[] responsibleFloors = new String[]{"2_3_4_12", "5_6_7_8", "9_10_11"};
     private DayDutyManager dayDutyManager;
     private MonthManager monthManager;
     private UserManager userManager;
     private FloorManager floorManager;
 
     private User user;
     private Integer month;
     private String floor;
 
     private List<SelectItem> floorUsersList;
 
     private List<DayDuty> dutyList;
     private static final User emptyUser = new EmptyUser();
     private List<DayDuty> emptyDayDutyList;
     private boolean monthAvailable = false;
 
     public DutyList() {
         setSortColumn("dayOfWeek");
         initializeEmptyDayDutyList();
     }
 
     private void initializeEmptyDayDutyList() {
         emptyDayDutyList = new LinkedList<DayDuty>();
         //28 - minimum days num in any month
         int size = 28;
         while (size != 0) {
             DayDuty dayDuty = new DayDuty();
             Calendar date = Calendar.getInstance();
             dayDuty.setFirstUser(emptyUser);
             dayDuty.setSecondUser(emptyUser);
             dayDuty.setDate(date);
             size--;
         }
     }
 
     public List<DayDuty> getDutyList() {
         List<DayDuty> d = dayDutyManager.loadAllDayDutyByMonthAndFloor(getMonth(), getFloor());
         for (DayDuty duty : d) {
             if (duty.isFirstEmpty()) {
                 duty.setFirstUser(getEmptyUser());
             }
             if (duty.isSecondEmpty()) {
                 duty.setSecondUser(getEmptyUser());
             }
         }
         List<DayDuty> result = getEmptyDutyList();
         for (DayDuty dayDuty : d) {
             result.set(dayDuty.getDate().get(Calendar.DAY_OF_MONTH) - 1, dayDuty);
         }
         dutyList = result;
         return dutyList;
     }
 
     private User getEmptyUser() {
         return emptyUser;
     }
 
     public List<SelectItem> getUsersForFloorhead() {
         if (isOnOwnFloor() && isUserFloorhead() && floorUsersList == null) {
             floorUsersList = new LinkedList<SelectItem>();
             floorUsersList.add(new SelectItem(SELECT_USER_STRING));
 
             List<User> floorUsers = userManager.getUsersByFloor(getFloor());
             floorUsers.remove(getUser());
             for (User user : floorUsers) {
                 floorUsersList.add(new SelectItem(user.getAddress().getHostelRoom() + " " + user.getFullName()));
             }
         }
         return floorUsersList;
     }
 
     public void deleteUserFromDuty(ActionEvent e) {
         int index = getTableRowNumber(e);
         User emptyUser = getEmptyUser();
         DayDuty dayDuty = dutyList.get(index);
         if (e.getComponent().getId().equals(FIRST_SHIFT_USER)) {
             dayDutyManager.deleteFirstDutyUser(dayDuty);
             dayDuty.setFirstUser(emptyUser);
         } else if (e.getComponent().getId().equals(SECOND_SHIFT_USER)) {
             dayDutyManager.deleteSecondDutyUser(dayDuty);
             dayDuty.setSecondUser(emptyUser);
         }
     }
 
     public void floorUserChanged(ValueChangeEvent e) {
         String newValue = (String) e.getNewValue();
         if (newValue.equals(SELECT_USER_STRING)) {
             return;
         }
 
         User userToWriteOnDuty = getUserToWriteOnDuty(newValue);
 
         Calendar date = getDate(e);
         DayDuty dayDuty = dayDutyManager.loadDayDutyByDateAndFloor(date, getFloor());
         if (dayDuty == null) {
             dayDuty = new DayDuty();
             dayDuty.setDate(date);
             dayDuty.setFloor(getFloor());
         }
 
         String shift = e.getComponent().getId();
         if (shift.equals(FIRST_SHIFT)) {
             dayDuty.setFirstUser(userToWriteOnDuty);
         } else if (shift.equals(SECOND_SHIFT)) {
             dayDuty.setSecondUser(userToWriteOnDuty);
         }
 
         dayDutyManager.saveDayDuty(dayDuty);
         dutyList = null;
     }
 
     private User getUserToWriteOnDuty(String roomPlusFullName) {
         int n = roomPlusFullName.indexOf(' ');
         String userRoom = roomPlusFullName.substring(0, n);
         String userFullName = roomPlusFullName.substring(n + 1, roomPlusFullName.length());
         return userManager.getUserByRoomAndFullName(userRoom, userFullName);
     }
 
     public boolean isUserFloorhead() {
         UserHelper userHelperBean = (UserHelper) FacesUtils.getManagedBean("userHelper");
         return userHelperBean.ifCurrentUserHasRole(Constants.STAROSTA_ROLE);
     }
 
     public int getTableRowNumber(FacesEvent e) {
         return Integer.valueOf(e.getComponent().getClientId(getFacesContext()).split(":")[2]);
     }
 
     public List<UserDuty> getUserDuties() throws Exception {
         List<UserDuty> userDuties = new ArrayList<UserDuty>();
         if (isMonthAvailable()) {
             for (DayDuty dayDuty : getDutyList()) {
                 if (dayDuty.getFirstUser() != null && dayDuty.getFirstUser().equals(getUser())) {
                     userDuties.add(new UserDuty(1, dayDuty));
                 }
                 if (dayDuty.getSecondUser() != null && dayDuty.getSecondUser().equals(getUser())) {
                     userDuties.add(new UserDuty(2, dayDuty));
                 }
             }
         }
         return userDuties;
     }
 
     public void writeFirstOnDuty(ActionEvent e) {
         if (!isOnOwnFloor() || !isMonthAvailable()) {
             return;
         }
         Calendar date = getDate(e);
         DayDuty dayDuty = getDayDutyManager().loadDayDutyByDateAndFloor(date, getFloor());
 
         if (dayDuty == null) {
             dayDuty = new DayDuty();
             dayDuty.setDate(date);
             dayDuty.setFloor(getFloor());
         }
         if (dayDuty.getFirstUser() != null) {
             return;
         }
         dayDuty.setFirstUser(getUser());
         getDayDutyManager().saveDayDuty(dayDuty);
     }
 
     public void writeSecondOnDuty(ActionEvent e) {
         if (!isOnOwnFloor() || !isMonthAvailable()) {
             return;
         }
         Calendar date = getDate(e);
         DayDuty dayDuty = getDayDutyManager().loadDayDutyByDateAndFloor(date, getFloor());
         if (dayDuty == null) {
             dayDuty = new DayDuty();
             dayDuty.setDate(date);
             dayDuty.setFloor(getFloor());
         }
         if (dayDuty.getSecondUser() != null) {
             return;
         }
         dayDuty.setSecondUser(getUser());
         getDayDutyManager().saveDayDuty(dayDuty);
     }
 
     public void deleteDuty(ActionEvent e) throws Exception {
         int index = getTableRowNumber(e);
         try {
             UserDuty userDuty = getUserDuties().get(index);
             if (userDuty.getShift() == 1) {
                 dayDutyManager.deleteFirstDutyUser(userDuty.getDayDuty());
             }
             if (userDuty.getShift() == 2) {
                 dayDutyManager.deleteSecondDutyUser(userDuty.getDayDuty());
             }
         } catch (Exception ex) {
             throw new Exception(ex);
         }
     }
 
     private Calendar getDate(FacesEvent e) {
         String id = e.getComponent().getClientId(getFacesContext());
         int day = Integer.parseInt(id.split(":")[2]) + 1;
         Calendar date = Calendar.getInstance();
         date.set(Calendar.MONTH, getMonth());
         date.set(Calendar.DAY_OF_MONTH, day);
         return date;
     }
 
     private List<DayDuty> getEmptyDutyList() {
         correctEmptyDayDutyList(MonthHelper.getDaysNumInMonth(getMonth()));
         int counter = 1;
         for (DayDuty dd : emptyDayDutyList) {
             dd.getDate().set(Calendar.MONTH, getMonth());
             dd.getDate().set(Calendar.DAY_OF_MONTH, counter);
             dd.setFirstUser(emptyUser);
             dd.setSecondUser(emptyUser);
             dd.setFloor(null);
             dd.setId(null);
             counter++;
         }
         return emptyDayDutyList;
     }
 
     private void correctEmptyDayDutyList(int countOfDays) {
         int currentEmptyListSize = emptyDayDutyList.size();
         if (currentEmptyListSize == countOfDays) {
             return;
         }
         int delta = currentEmptyListSize - countOfDays;
         if (delta > 0) {
             while (delta != 0) {
                 emptyDayDutyList.remove(0);
                 delta--;
             }
         } else {
             for (int i = 0; i < Math.abs(delta); i++) {
                 DayDuty dd = new DayDuty();
                 Calendar date = Calendar.getInstance();
                 dd.setFirstUser(emptyUser);
                 dd.setSecondUser(emptyUser);
                 dd.setDate(date);
                 emptyDayDutyList.add(dd);
             }
         }
     }
 
     public List<SelectItem> getMonthItems() {
         ArrayList<SelectItem> items = new ArrayList<SelectItem>();
         String[] months = MonthHelper.getMonths(getBundle());
         for (String month : months) {
             items.add(new SelectItem(month));
         }
         return items;
     }
 
     public void monthSelectionChanged(ValueChangeEvent e) {
         String newValue = (String) e.getNewValue();
         setMonth(MonthHelper.getMonth(newValue, getBundle()));
         dutyList = null;
     }
 
     public List<SelectItem> getFloors() {
         List<String> names = floorManager.getFloorsNames();
 
         List<SelectItem> items = new ArrayList<SelectItem>();
         for (String name : names) {
             items.add(new SelectItem(name));
         }
         return items;
     }
 
     public void floorChanged(ValueChangeEvent e) {
         setFloor((String) e.getNewValue());
         dutyList = null;
     }
 
     public void print(ActionEvent e) {
         Semaphore sem = new Semaphore(1, true);
         try {
             sem.acquire();
             //TODO forth param must be name of vospetka and starosta
             String monthForExcelReport = MonthHelper.getMonthString(getMonth(), getBundle(new Locale("ru")));
             Object[] params = new Object[]{getFloor(), monthForExcelReport, "Федоров В.В", getNameOfVospetka(getFloor()), getDutyList()};
             DutyListLoadService.getService(Constants.HTTP_DOWNLOADER).download(params);
         } catch (InterruptedException ex) {
             logger.error("Current thread was interrupted!");
         } catch (IOException ex) {
             logger.error("IO error:" + ex.getMessage());
         } catch (NullPointerException exc) {
             //no critic, it's joke! :)
             logger.error("Something wrong.." + exc.getMessage());
         } finally {
             sem.release();
         }
 
     }
 
     public void changeMonthAvailability(ActionEvent e) {
         Integer year = Calendar.getInstance().get(Calendar.YEAR);
         Integer currentMonth = Calendar.getInstance().get(Calendar.MONTH);
         //11 - DECEMBER
         if(currentMonth == 11 && getMonth() == 0){
             year++;
         }
         
         DutyMonth dutyMonth = monthManager.loadMonth(year, getMonth(), getFloor());
         if (dutyMonth == null) {
             dutyMonth = createDutyMonth();
             dutyMonth.setAvailable(true);
         } else {
             dutyMonth.setAvailable(!dutyMonth.getAvailable());
         }
         monthManager.saveMonth(dutyMonth);
         monthAvailable = !monthAvailable;
     }
 
     private DutyMonth createDutyMonth() {
         DutyMonth dutyMonth = new DutyMonth();
         // TODO user should be able to choose the year
         dutyMonth.setYear(Calendar.getInstance().get(Calendar.YEAR));
         dutyMonth.setMonth(getMonth());
         dutyMonth.setFloor(getFloor());
         return dutyMonth;
     }
 
     public boolean isMonthAvailable() {
         return monthAvailable;
     }
 
     private void refreshMonthAvailability() {
         if (monthManager == null) {
             return;
         }
         Integer year = Calendar.getInstance().get(Calendar.YEAR);
         DutyMonth dutyMonth = monthManager.loadMonth(year, getMonth(), getFloor());
         if (dutyMonth == null) {
             monthAvailable = false;
         } else {
             monthAvailable = dutyMonth.getAvailable();
         }
     }
 
     public DayDutyManager getDayDutyManager() {
         return dayDutyManager;
     }
 
     public boolean isOnOwnFloor() {
         return getUser().getAddress().getHostelFloor().equals(getFloor());
     }
 
     public String getFloor() {
         if (floor == null) {
             setFloor(getUser().getAddress().getHostelFloor());
         }
         return floor;
     }
 
     public void setFloor(String floor) {
         this.floor = floor;
         refreshMonthAvailability();
     }
 
     public void setDayDutyManager(DayDutyManager dayDutyManager) {
         this.dayDutyManager = dayDutyManager;
     }
 
     public void setMonthManager(MonthManager monthManager) {
         this.monthManager = monthManager;
         refreshMonthAvailability();
     }
 
     public String getMonthString() {
         return MonthHelper.getMonthString(getMonth(), getBundle());
     }
     
     public void setMonthString(String monthString){
         //stub
     }
     
     private String getNameOfVospetka(String floor){
         StringBuffer buf = new StringBuffer("vospetka_");
         for(String floors : responsibleFloors){
             if(isResponseForFloor(floors, floor)){
                 buf.append(floors);
                 break;
             }
         }
         return getBundle().getString(buf.toString());
     }
     
     private boolean isResponseForFloor(String floors, String floor){
         String[] existingFloors = floors.split("_");
         for(String fl : existingFloors){
             if(fl.equals(floor)){
                 return true;
             }
         }
         return false;
     }
     
 
     public void setUserManager(UserManager userManager) {
         this.userManager = userManager;
     }
 
     public User getUser() {
         if (user == null) {
             setUser((User) getContext().getAuthentication().getPrincipal());
         }
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public Integer getMonth() {
         if (month == null) {
             setMonth(Calendar.getInstance().get(Calendar.MONTH));
         }
         return month;
     }
 
     public void setMonth(Integer month) {
         this.month = month;
         refreshMonthAvailability();
     }
 
     public FloorManager getFloorManager() {
         return floorManager;
     }
 
     public void setFloorManager(FloorManager floorManager) {
         this.floorManager = floorManager;
     }
 }
