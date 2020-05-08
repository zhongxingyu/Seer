 package hoplugins.tsforecast;
 
 /*
  * LoepiCurve.java
  *
  * Created on 22.March 2006, 11:04
  *
  *Version 0.21
  *history :
  *22.03.06  Version 0.1 Creation
  *31.03.06  Version 0.2 Improve forecast function and constructor
  *26.08.06  Version 0.21 rebuilt
  *04.09.06  Version 0.3 Change formula to a trainerLS-based formula
  */
 
 /**
  *
  * @author  michael.roux
  */
 
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import plugins.IHOMiniModel;
 import plugins.IMatchDetails;
 
 // Referenced classes of package hoplugins.tsforecast:
 //            ForecastCurve, Curve
 
 public class LoepiCurve extends ForecastCurve {
 
   private TrainerCurve m_TrainerCurve = null;
   
   public LoepiCurve(IHOMiniModel ihominimodel, TrainerCurve t, boolean future) throws SQLException {
     super(ihominimodel, future);
     m_TrainerCurve = t;
   }
 
 
   //-- protected ------------------------------------------------------------------------
 
//TS über 4,5:
 //TS(neu)=TS(alt)*(1-((TS(alt)-4.5)/(FQ/3))/100)
 //alte Formel: TS(neu)=TS(alt)*(1-((TS(alt)-4,5)*Faktor)/100).
 //TS unter 4,5:
 //TS(neu)=TS(alt)*(1+((TS(alt)-4.5)*(-1)*(FQ/2))/100)
 //alte Formel: TS(neu)=TS(alt)*(1+((TS(alt)-4,5)*(-1)/(Faktor/1,5))/100).
 
   protected double forecastUpdate(Curve.Point point1, Curve.Point point2) throws Exception {
   
     double dRet = point1.m_dSpirit;
     if( dRet >= m_dGeneralSpirit) {
       dRet *= 1.0D - (((dRet - m_dGeneralSpirit) / (m_TrainerCurve.getLeadership( point1.m_dDate) / 3D) ) / 100D);
     } else {
       dRet *= 1.0D + (((dRet - m_dGeneralSpirit) * -1D) * (m_TrainerCurve.getLeadership( point1.m_dDate) / 2D) / 100D);
     }
     return dRet;
   }
 
 }
