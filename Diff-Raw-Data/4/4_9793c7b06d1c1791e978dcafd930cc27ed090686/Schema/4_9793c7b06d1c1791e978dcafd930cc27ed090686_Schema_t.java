 package net.retakethe.policyauction.data.impl.schema;
 
 import java.util.Date;
 import java.util.UUID;
 
 import net.retakethe.policyauction.data.api.types.DateAndHour;
 import net.retakethe.policyauction.data.api.types.PolicyID;
 import net.retakethe.policyauction.data.impl.schema.column.ColumnRange;
 import net.retakethe.policyauction.data.impl.schema.column.NamedColumn;
 import net.retakethe.policyauction.data.impl.schema.column.typed.StringNamedColumn;
 import net.retakethe.policyauction.data.impl.schema.column.typed.StringStringColumn;
 import net.retakethe.policyauction.data.impl.schema.family.ColumnFamily;
 import net.retakethe.policyauction.data.impl.schema.family.RangeSupercolumnFamily;
 import net.retakethe.policyauction.data.impl.schema.family.SingleRowRangeColumnFamily;
 import net.retakethe.policyauction.data.impl.schema.subcolumn.SuperRangeNamedSubcolumn;
 import net.retakethe.policyauction.data.impl.schema.supercolumn.SupercolumnRange;
 
 /**
  * Cassandra schema elements as in cassandra-schema.txt.
  *
  * @author Nick Clarke
  */
 public final class Schema {
 
     public static final PoliciesCF POLICIES = new PoliciesCF();
 
     public static final LogHoursRow LOG_HOURS = new LogHoursRow();
 
     public static final LogSCF LOG = new LogSCF();
 
 
     public static final class PoliciesCF extends ColumnFamily<PolicyID, String> {
         public final NamedColumn<PolicyID, String, String> SHORT_NAME;
         public final NamedColumn<PolicyID, String, String> DESCRIPTION;
         public final NamedColumn<PolicyID, String, Date> LAST_EDITED;
 
         private PoliciesCF() {
             super(SchemaKeyspace.MAIN, "policies", Type.POLICY_ID, Type.UTF8);
             SHORT_NAME = new StringStringColumn<PolicyID>("short_name", this);
             DESCRIPTION = new StringStringColumn<PolicyID>("description", this);
             LAST_EDITED = new StringNamedColumn<PolicyID, Date>("last_edited", this, Type.DATE);
         }
     }
 
     public static final class LogHoursRow extends SingleRowRangeColumnFamily<String, DateAndHour, Object> {
         private LogHoursRow() {
             super(SchemaKeyspace.MAIN, "misc_string", "log hours", Type.UTF8, Type.DATE_AND_HOUR);
             setColumnRange(new ColumnRange<String, DateAndHour, Object>(this, Type.DUMMY));
         }
     }
 
    public static final class LogSCF extends RangeSupercolumnFamily<DateAndHour, UUID, String, LogSCF.LogMessageRange> {
 
         public final class LogMessageRange extends SupercolumnRange<DateAndHour, UUID, String> {
             public final SuperRangeNamedSubcolumn<DateAndHour, UUID, String, String> LOCAL_TIME;
             public final SuperRangeNamedSubcolumn<DateAndHour, UUID, String, String> SERVER_IP;
             public final SuperRangeNamedSubcolumn<DateAndHour, UUID, String, String> LEVEL;
             public final SuperRangeNamedSubcolumn<DateAndHour, UUID, String, String> MESSAGE;
 
             private LogMessageRange() {
                 super(LogSCF.this);
                 LOCAL_TIME = new SuperRangeNamedSubcolumn<DateAndHour, UUID, String, String>("local_time",
                         this, Type.UTF8);
                 SERVER_IP = new SuperRangeNamedSubcolumn<DateAndHour, UUID, String, String>("server_ip",
                         this, Type.UTF8);
                 LEVEL = new SuperRangeNamedSubcolumn<DateAndHour, UUID, String, String>("level",
                         this, Type.UTF8);
                 MESSAGE = new SuperRangeNamedSubcolumn<DateAndHour, UUID, String, String>("message",
                         this, Type.UTF8);
             }
         }
 
         private LogSCF() {
             super(SchemaKeyspace.LOGS, "log", Type.DATE_AND_HOUR, Type.TIME_UUID, Type.UTF8);
             setSupercolumnRange(new LogMessageRange());
         }
     }
 }
