 package com.spax.vitebsktransport.service;
 
 import android.content.Context;
 
 import com.spax.vitebsktransport.domain.Departure;
 import com.spax.vitebsktransport.domain.MoveTime;
 import com.spax.vitebsktransport.domain.Stop;
 import com.spax.vitebsktransport.domain.StopTimeHolder;
 import com.spax.vitebsktransport.domain.Time;
 import com.spax.vitebsktransport.domain.TimeTableRecord;
 import com.spax.vitebsktransport.repository.DepartureRepository;
 import com.spax.vitebsktransport.repository.MoveTimeRepository;
 import com.spax.vitebsktransport.repository.StopRepository;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class DepartureService {
     private DepartureRepository departureRepository;
     private MoveTimeRepository moveTimeRepository;
     private StopRepository stopRepository;
 
     public DepartureService(Context ctx) {
         departureRepository = new DepartureRepository(ctx);
         moveTimeRepository = new MoveTimeRepository(ctx);
         stopRepository = new StopRepository(ctx);
     }
 
     public Departure create(Departure route) {
         return departureRepository.create(route);
     }
 
     public void delete(long id) {
         departureRepository.delete(id);
     }
 
     public void delete(Departure departure) {
         departureRepository.delete(departure.getId());
     }
 
     public List<Departure> findAll() {
         return departureRepository.getAll();
     }
 
     public List<Departure> findAll(long directionId, long stopId, String day) {
         return departureRepository.getAll(directionId, day);
     }
 
     public List<Time> getTimes(long directionId, long stopId, String day) {
         List<Time> result = new ArrayList<Time>();
         List<Long> path = getPath(directionId);
         List<MoveTime> times = moveTimeRepository.getAll(directionId);
         List<Departure> deps = departureRepository.getAll(directionId, day);
         for (Departure d : deps) {
             if (isStopInPath(path, stopId, d.getFromStopId(), d.getToStopId())) {
                 String t = d.getTime();
                 Time time = parseTime(t);
                 int deltaTime = countDeltaTime(d, stopId, times);
                 time.addMins(deltaTime);
                 result.add(time);
             }
         }
         Collections.sort(result);
         return result;
     }
 
     public List<TimeTableRecord> getTimeTable(long directionId, long stopId, String day) {
         List<TimeTableRecord> result = new ArrayList<TimeTableRecord>();
         List<Time> times = getTimes(directionId, stopId, day);
         if (!times.isEmpty()) {
             int hour = times.get(0).getHours();
             TimeTableRecord rec = new TimeTableRecord();
             for (Time t : times) {
                 if (t.getHours() == hour) {
                     rec.addTime(t);
                 } else {
                     result.add(rec);
                     rec = new TimeTableRecord();
                     hour = t.getHours();
                     rec.addTime(t);
                 }
             }
             if (!rec.getTimes().isEmpty()) {
                 result.add(rec);
             }
         }
         return result;
     }
 
     public List<Long> getPath(long directionId) {
         List<Long> path = new ArrayList<Long>();
         List<MoveTime> times = moveTimeRepository.getAll(directionId);
         if (!times.isEmpty()) {
             path.add(times.get(0).getFromStopId());
             for (MoveTime mt : times) {
                 path.add(mt.getToStopId());
             }
         }
         return path;
     }
 
     private boolean isStopInPath(List<Long> path, long selectedStop, long fromStop, long toStop) {
         int beginIdx = path.indexOf(fromStop);
         int endIdx = path.lastIndexOf(toStop);
         int stopIdx = path.indexOf(selectedStop);
         return stopIdx >= beginIdx && stopIdx <= endIdx;
     }
 
     private int countDeltaTime(Departure dep, long stopId, List<MoveTime> times) {
         int result = 0;
         boolean foundFirstStop = false;
         for (MoveTime t : times) {
             if (!foundFirstStop && t.getFromStopId() == dep.getFromStopId()) {
                 foundFirstStop = true;
             }
             if (t.getFromStopId() == stopId) {
                 break;
             }
             if (foundFirstStop) {
                 result += t.getTime();
             }
         }
         return result;
     }
 
     private Time parseTime(String t) {
         String[] arr = t.split(":");
         return new Time(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
     }
 
     public List<String> findDaysForDirection(long directionId) {
         return departureRepository.getDaysForDirection(directionId);
     }
 
     public List<StopTimeHolder> getRouteDetailsForTime(Time time, long stopId, long directionId) {
         List<StopTimeHolder> result = new ArrayList<StopTimeHolder>();
 
         List<Long> path = getPath(directionId);
        int from = path.indexOf(stopId);
        result.add(new StopTimeHolder(stopRepository.getById(from), time));
         List<MoveTime> times = moveTimeRepository.getAll(directionId);
         int delta = 0;
 
        for (int i = from + 1; i < path.size(); i++) {
             Stop s = stopRepository.getById(path.get(i));
             delta += times.get(i-1).getTime();
             Time t = new Time(time).addMins(delta);
             result.add(new StopTimeHolder(s, t));
         }
         return result;
     }
 
     public void open() {
         departureRepository.open();
     }
 
     public void close() {
         departureRepository.close();
     }
 }
