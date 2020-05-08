 // %4198474569:hoplugins.teamplanner.ui.tabs.fans.listener%
 package hoplugins.teamplanner.ui.tabs.fans.listener;
 
 import hoplugins.Commons;
 import hoplugins.TeamPlanner;
 
 import hoplugins.teamplanner.ui.controller.calculator.Calculator;
 import hoplugins.teamplanner.ui.model.OperationCell;
 import hoplugins.teamplanner.ui.model.inner.NumericInner;
 import hoplugins.teamplanner.ui.tabs.WeekHeader;
 import hoplugins.teamplanner.util.Util;
 
 import plugins.IFutureTrainingManager;
 
 import javax.swing.table.TableModel;
 
 
 /**
  * Missing Class Documentation
  *
  * @author Draghetto
  */
 public class FansCalculator extends Calculator {
     //~ Static fields/initializers -----------------------------------------------------------------
 
     /** Missing Parameter Documentation */
     private static final int PROMOTED = 1;
 
     /** Missing Parameter Documentation */
     private static final int RELEGATED = -1;
 
    /** Missing Parameter Documentation */
    private static final int NORMAL = 0;

    //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Missing Method Documentation
      *
      * @param row Missing Method Parameter Documentation
      * @param model Missing Method Parameter Documentation
      */
     @Override
 	public void doCalculate(int row, TableModel model) {
         int fans = Commons.getModel().getVerein().getFans();
         int season = TeamPlanner.ACTUALWEEK.getSeason();
         int weekNumber = TeamPlanner.ACTUALWEEK.getWeek();
 
         for (int i = 0; i < IFutureTrainingManager.FUTUREWEEKS; i++) {
             weekNumber++;
 
             int extrafans = 0;
 
             if (weekNumber == 17) {
                 weekNumber = 1;
 
                 int seasonEvent = TeamPlanner.getFuturePane().getYearSetting(season).getSeasonEvent();
 
                 if (seasonEvent == RELEGATED) {
                     fans = (fans * 90) / 100;
                     extrafans = (-fans * 10) / 100;
                 }
 
                 if (seasonEvent == PROMOTED) {
                     fans = (fans * 110) / 100;
                     extrafans = (fans * 10) / 100;
                 }
 
                 season++;
             }
 
             double fanMorale = TeamPlanner.getFansPane().getFansMoraleAt(WeekHeader.instance()
                                                                                    .getColumnWeek(i));
 
             int weeklyFans = getFanIncrease(fans, fanMorale) + extrafans;
             fans = fans + weeklyFans;
 
             OperationCell cell = Util.getOperationCell(model, row, i);
             NumericInner inner = (NumericInner) cell.getOperation().getInner();
             inner.setData(weeklyFans, weeklyFans * 30);
         }
     }
 
     /**
      * Missing Method Documentation
      *
      * @param fans Missing Method Parameter Documentation
      * @param fanMorale Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     private int getFanIncrease(int fans, double fanMorale) {
         // TODO Fans depends on number of fans and series
         int intValue = (int) fanMorale;
 
         switch (intValue) {
             case 1:
                 return -25;
 
             case 2:
                 return -18;
 
             case 3:
                 return -7;
 
             case 4:
                 return 4;
 
             case 5:
                 return 9;
 
             case 6:
                 return 14;
 
             case 7:
                 return 18;
         }
 
         return 0;
     }
 }
