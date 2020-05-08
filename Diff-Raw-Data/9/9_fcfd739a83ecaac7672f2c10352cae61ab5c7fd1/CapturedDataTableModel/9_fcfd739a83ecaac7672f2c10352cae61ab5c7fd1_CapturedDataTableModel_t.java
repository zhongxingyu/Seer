 package gde.gui.tablemodels;
 
 import com.sun.jmx.snmp.Timestamp;
 import static gde.gui.tablemodels.CollectionTableModel.database;
 import gde.models.CapturedData;
 import gde.models.Field;
 import gde.models.Field.FieldType;
 import gde.models.Game;
 import gde.models.Instance;
 import java.text.DateFormat;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.SwingUtilities;
 import org.bson.types.ObjectId;
 import org.jongo.MongoCollection;
 
 public class CapturedDataTableModel extends CollectionTableModel<CapturedData> {
 
     private static final String COLLECTION = "captureddata";
 
     public CapturedDataTableModel(Game game) {
         super(game, getTypes(game), getTitles(game), COLLECTION);
     }
 
     @Override
     public void populate(String query) {
         ids.clear();
         setRowCount(0);
         Iterable<CapturedData> capturedData = collection.find(query).as(CapturedData.class);
         for (final CapturedData capturedDatum : capturedData) {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     addRow(capturedDatum);
                 }
             });
         }
     }
 
     @Override
     public void populate() {
         populate("");
     }
 
     @Override
     public CapturedData getEntryAt(int rowId) {
         return collection.findOne(ids.get(rowId)).as(CapturedData.class);
     }
 
     @Override
     public void setEntryAt(int rowId, CapturedData entry) {
         for (int i = 0; i < titles.length; i++) {
             setValueAt(entry.getData().get(titles[i].toLowerCase()), rowId, i);
         }
     }
 
     @Override
     protected void addRow(CapturedData data) {
         final Object[] objects = new Object[titles.length];
         DateFormat shortDf = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        objects[0] = shortDf.format(data.getCreateDate());
         MongoCollection instanceCollection = database.getCollection("instances");
         Instance instance = instanceCollection.findOne(new ObjectId(data.getInstanceId())).as(Instance.class);
         objects[1] = instance.getIdentifier();
         for (int i = 2; i < titles.length; i++) {
             objects[i] = data.getData().get(titles[i].toLowerCase());
         }
         addRow(objects);
         ids.add(data.getKey());
     }
 
     private static Class convertToType(FieldType type) {
         switch (type) {
             case INTEGER:
                 return Integer.class;
             case DECIMAL:
                 return Double.class;
             case BOOLEAN:
                 return Boolean.class;
             case TEXT:
                 return String.class;
         }
         return null;
     }
 
     private static String[] getTitles(Game game) {
         List<String> titles = new LinkedList<String>();
         titles.add("Date");
         titles.add("Identifier");
         MongoCollection fieldsCollection = database.getCollection("fields");
         String fieldQuery = String.format("{gameId: '%s'}", game.getKey().toString());
         Iterable<Field> fields = fieldsCollection.find(fieldQuery).as(Field.class);
         for (Field field : fields) {
             titles.add(field.getName());
         }
         return titles.toArray(new String[titles.size() - 1]);
     }
 
     private static Class[] getTypes(Game game) {
         List<Class> types = new LinkedList<Class>();
         types.add(Timestamp.class);
         types.add(String.class);
         MongoCollection fieldsCollection = database.getCollection("fields");
         String fieldQuery = String.format("{gameId: '%s'}", game.getKey().toString());
         Iterable<Field> fields = fieldsCollection.find(fieldQuery).as(Field.class);
         for (Field field : fields) {
             types.add(convertToType(field.getType()));
         }
         return types.toArray(new Class[types.size() - 1]);
     }
 }
