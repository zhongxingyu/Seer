 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cs.wintoosa.service;
 
 import cs.wintoosa.domain.Acceleration;
 import cs.wintoosa.domain.Compass;
 import cs.wintoosa.domain.Gps;
 import cs.wintoosa.domain.KeyPress;
 import cs.wintoosa.domain.Keyboard;
 import cs.wintoosa.domain.Log;
 import cs.wintoosa.domain.Orientation;
 import cs.wintoosa.domain.SessionLog;
 import cs.wintoosa.domain.Touch;
 import java.util.List;
 import org.apache.commons.collections.map.LinkedMap;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  *
  * @author vkukkola
  */
 @Service
 public class SessionService implements ISessionService {
 
     @Autowired
     ILogService logService;
 
     @Override
     public DataHolder formatForJsp(SessionLog session) {
         DataHolder data = new DataHolder(session);
         if (session != null) {
             
             List<Acceleration> accLogs = pullFromDb(Acceleration.class, data);
             List<Compass> comLogs = pullFromDb(Compass.class, data);
             List<KeyPress> keyPresses = pullFromDb(KeyPress.class, data);
             List<Keyboard> keyboards = pullFromDb(Keyboard.class, data);
             List<Orientation> gyroLogs = pullFromDb(Orientation.class, data);
             List<Gps> gpsLogs = pullFromDb(Gps.class, data);
             List<Touch> touchLogs = pullFromDb(Touch.class, data);
 
             int accI = 0;
             int comI = 0;
             int pressI = 0;
             int keybI = 0;
             int gyroI = 0;
             int gpsI = 0;
             int touchI = 0;
             
             for (Long time : data.getTimestamps()) {
                 if (accLogs != null && accLogs.size() > accI && (accLogs.get(accI).getTimestamp() / 10) * 10 <= time) {
                     while ((accLogs.get(accI).getTimestamp() / 10) * 10 <= time) {
                         if ((accLogs.get(accI).getTimestamp() / 10) * 10 == time) {
                             data.addToColumn("Acc X", accLogs.get(accI).getAccX().toString());
                             data.addToColumn("Acc Y", accLogs.get(accI).getAccY().toString());
                             data.addToColumn("Acc Z", accLogs.get(accI).getAccZ().toString());
                         } else {
                             data.addToColumn("Acc X", "");
                             data.addToColumn("Acc Y", "");
                             data.addToColumn("Acc Z", "");
                         }
                         accI++;
                         if (accLogs.size() >= accI)
                             break;
                     }
                 } else {
                     data.addToColumn("Acc X", "");
                     data.addToColumn("Acc Y", "");
                     data.addToColumn("Acc Z", "");
                 }
                 if (comLogs != null && comLogs.size() > comI && (comLogs.get(comI).getTimestamp() / 10) * 10 <= time) {
                     while ((comLogs.get(comI).getTimestamp() / 10) * 10 <= time) {
                         if ((comLogs.get(comI).getTimestamp() / 10) * 10 == time) {
                             data.addToColumn("Heading, true", comLogs.get(comI).getTrueHeading().toString());
                             data.addToColumn("Heading, magnetic", comLogs.get(comI).getMagneticHeading().toString());
                         } else {
                             data.addToColumn("Heading, true", "");
                             data.addToColumn("Heading, magnetic", "");
                         }
                         comI++;
                         if (comLogs.size() >= comI)
                             break;
                     }
                 } else {
                     data.addToColumn("Heading, true", "");
                     data.addToColumn("Heading, magnetic", "");
                 }
                 if (keyPresses != null && keyPresses.size() > pressI && (keyPresses.get(pressI).getTimestamp() / 10) * 10 <= time) {
                     while ((keyPresses.get(pressI).getTimestamp() / 10) * 10 <= time) {
                         if ((keyPresses.get(pressI).getTimestamp() / 10) * 10 == time) {
                             data.addToColumn("Key pressed", keyPresses.get(pressI).getKeyPressed());
                         } else {
                            data.addToColumn("Key pressed", "");
                         }
                         pressI++;
                         if (keyPresses.size() >= pressI)
                             break;
                     }
                 } else {
                    data.addToColumn("Key pressed", "");
                 }
                if (keyboards != null && keyboards.size() > keybI && (keyboards.get(keybI).getTimestamp() / 10) * 10 <= time) {
                     while ((keyboards.get(keybI).getTimestamp() / 10) * 10 <= time) {
                         if ((keyboards.get(keybI).getTimestamp() / 10) * 10 == time) {
                             data.addToColumn("Keyboard status", keyboards.get(keybI).getKeyboardFocus());
                         } else {
                             data.addToColumn("Keyboard status", "");
                         }
                         keybI++;
                         if (keyboards.size() >= keybI)
                             break;
                     }
                 } else {
                     data.addToColumn("Keyboard status", "");
                 }
                 if (gyroLogs != null && gyroLogs.size() > gyroI && (gyroLogs.get(gyroI).getTimestamp() / 10) * 10 <= time) {
                     while ((gyroLogs.get(gyroI).getTimestamp() / 10) * 10 <= time) {
                         if ((gyroLogs.get(gyroI).getTimestamp() / 10) * 10 == time) {
                             data.addToColumn("Gyro X", gyroLogs.get(gyroI).getCurrentRotationRateX().toString());
                             data.addToColumn("Gyro Y", gyroLogs.get(gyroI).getCurrentRotationRateY().toString());
                             data.addToColumn("Gyro Z", gyroLogs.get(gyroI).getCurrentRotationRateZ().toString());
                         } else {
                             data.addToColumn("Gyro X", "");
                             data.addToColumn("Gyro Y", "");
                             data.addToColumn("Gyro Z", "");
                         }
                         gyroI++;
                         if (gyroLogs.size() >= gyroI)
                             break;
                     }
                 } else {
                     data.addToColumn("Gyro X", "");
                     data.addToColumn("Gyro Y", "");
                     data.addToColumn("Gyro Z", "");
                 }
                 
                 if (gpsLogs != null && gpsLogs.size() > gpsI && (gpsLogs.get(gpsI).getTimestamp() / 10) * 10 <= time) {
                     while ((gpsLogs.get(gpsI).getTimestamp() / 10) * 10 <= time) {
                         if ((gpsLogs.get(gpsI).getTimestamp() / 10) * 10 == time) {
                             data.addToColumn("gps Latitude", gpsLogs.get(gpsI).getLat().toString());
                             data.addToColumn("gps Longitude", gpsLogs.get(gpsI).getLon().toString());
                             data.addToColumn("gps Altitude", gpsLogs.get(gpsI).getAlt().toString());
                         } else {
                             data.addToColumn("gps Latitude", "");
                             data.addToColumn("gps Longitude", "");
                             data.addToColumn("gps Altitude", "");
                         }
                         gpsI++;
                         if (gpsLogs.size() >= gpsI)
                             break;
                     }
                 } else {
                     data.addToColumn("gps Latitude", "");
                     data.addToColumn("gps Longitude", "");
                     data.addToColumn("gps Altitude", "");
                 }
                 if (touchLogs != null && touchLogs.size() > touchI && (touchLogs.get(touchI).getTimestamp() / 10) * 10 <= time) {
                     while ((touchLogs.get(touchI).getTimestamp() / 10) * 10 <= time) {
                         if ((touchLogs.get(touchI).getTimestamp() / 10) * 10 == time) {
                             data.addToColumn("touch x", ""+touchLogs.get(touchI).getXcoord());
                             data.addToColumn("touch y", ""+touchLogs.get(touchI).getYcoord());
                             data.addToColumn("touch action", touchLogs.get(touchI).getAction());
                         } else {
                             data.addToColumn("touch x", "");
                             data.addToColumn("touch y", "");
                             data.addToColumn("touch action", "");
                         }
                         touchI++;
                         if (touchLogs.size() >= touchI)
                             break;
                     }
                 } else {
                     data.addToColumn("touch x", "");
                     data.addToColumn("touch y", "");
                     data.addToColumn("touch action", "");
                 }
             }
 
         }
         return data;
     }
 
     private <T extends Log> List<T> pullFromDb(Class<T> cls, DataHolder data) {
         return logService.getAllBySessionId(cls, data.getSession());
     }
 }
