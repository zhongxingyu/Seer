 package net.paguo.trafshow.backend.snmp.summary.commands.impl;
 
 import net.paguo.trafshow.backend.snmp.summary.commands.DatabaseCommand;
 import net.paguo.trafshow.backend.snmp.summary.commands.impl.util.Parameter;
 import net.paguo.trafshow.backend.snmp.summary.commands.impl.util.PreparedStatementHandler;
 import net.paguo.trafshow.backend.snmp.summary.commands.impl.util.ResultsetCommand;
 import net.paguo.trafshow.backend.snmp.summary.model.DateRoller;
 import net.paguo.trafshow.backend.snmp.summary.model.DateRollerJodaImpl;
 import net.paguo.trafshow.backend.snmp.summary.model.TrafficCollector;
 import net.paguo.trafshow.backend.snmp.summary.model.TrafficRecord;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Date;
 
 public class GetTrafficDataCommand implements DatabaseCommand<TrafficCollector> {
     private static final String SQL = "select dt, cisco, interface, inoctets, outoctets, uptime from tr_with_uptime t " +
             "where dt between ? and ? and (cisco, interface) in (select cisco, interface from cl) " +
             "order by cisco, interface, dt";
     private static final Log log = LogFactory.getLog(GetTrafficDataCommand.class);
     private Date start;
     private Date end;
 
     public GetTrafficDataCommand(Date from, Date to) {
         this.start = from;
         this.end = to;
     }
 
     public TrafficCollector getData() {
         log.debug("getData(): <<<<");
         log.debug("Parameters are: " + start.toString() + " " + end.toString());
         final TrafficCollector collector = new TrafficCollector();
         try {
             PreparedStatementHandler handler = new PreparedStatementHandler();
             DateRoller roller = new DateRollerJodaImpl(start, end);
             while(roller.hasNextDate()) {
                int i = 0;

                 Parameter start = new Parameter.TimestampParameter(1,
                         new Timestamp(roller.getCurrentDate().getTime()));
                 Parameter end = new Parameter.TimestampParameter(2,
                        new Timestamp(roller.getNextDate().getTime()));
                 handler.handle(SQL, new ResultsetCommand<Object>() {
                     public Object process(ResultSet rs) throws SQLException {
                        while(rs.next()){
                            collector.addTrafficRecord(createRecord(rs));
                        }
                        return null;
                     }
                 }, start, end);
                i++;
                log.debug(i + "records processed so far");
             }
             handler.closeConnection();
         } catch (SQLException e) {
             log.error(e);
         }
         log.debug("Result size: " + collector.getTraffic().values().size());
         log.debug("getData(): >>>>");
         return collector;
     }
 
     private static TrafficRecord createRecord(ResultSet rs) throws SQLException {
         TrafficRecord record = new TrafficRecord();
         record.setDatetime(new Date(rs.getTimestamp(1).getTime()));
         record.setRouter(rs.getString(2));
         record.setIface(rs.getString(3));
         record.setInput(rs.getLong(4));
         record.setOutput(rs.getLong(5));
         record.setUptime(rs.getLong(6));
         return record;
     }
 }
