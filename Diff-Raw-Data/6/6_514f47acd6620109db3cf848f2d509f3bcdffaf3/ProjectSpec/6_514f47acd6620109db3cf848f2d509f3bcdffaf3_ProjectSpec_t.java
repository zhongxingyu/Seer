 package gov.usgs.cida.ncetl.spec;
 
 import com.google.common.collect.Maps;
 import gov.usgs.webservices.jdbc.spec.Spec;
 import gov.usgs.webservices.jdbc.spec.mapping.ColumnMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.SearchMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.WhereClauseType;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Map;
 import thredds.catalog.ThreddsMetadata.Vocab;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class ProjectSpec extends AbstractNcetlSpec {
 
     private static final long serialVersionUID = 1L;
     public static final String TABLE_NAME = "project";
     public static final String NAME = "name";
     public static final String CONTROLLED_VOCAB_ID = "controlled_vocabulary_id";
 
     @Override
     public String setupTableName() {
         return TABLE_NAME;
     }
     
     @Override
     public ColumnMapping[] setupColumnMap() {
         return new ColumnMapping[] {
                     new ColumnMapping(ID, ID),
                     new ColumnMapping(NAME, NAME),
                     new ColumnMapping(CONTROLLED_VOCAB_ID, CONTROLLED_VOCAB_ID),
                     new ColumnMapping(INSERTED, null),
                     new ColumnMapping(UPDATED, null)
                 };
     }
 
     @Override
     public SearchMapping[] setupSearchMap() {
         return new SearchMapping[] {
                     new SearchMapping(ID, ID, null, WhereClauseType.equals, null,
                                       null, null),
                     new SearchMapping("s_" + NAME, NAME, NAME,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + CONTROLLED_VOCAB_ID, CONTROLLED_VOCAB_ID, CONTROLLED_VOCAB_ID,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + INSERTED, INSERTED, INSERTED,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + UPDATED, UPDATED, UPDATED,
                                       WhereClauseType.equals, null, null, null)
                 };
     }
     
     public static Vocab unmarshal(int id, Connection con) throws SQLException {
         Spec spec = new ProjectSpec();
         Map<String, String[]> params = Maps.newHashMap();
        params.put(ID, new String[] { "" + id });
         Spec.loadParameters(spec, params);
         ResultSet rs = Spec.getResultSet(spec, con);
         
         Vocab vocab = null;
         if (rs.next()) {
             String name = rs.getString(NAME);
             int vocab_id = rs.getInt(CONTROLLED_VOCAB_ID);
             vocab = ControlledVocabularySpec.lookupAndAddText(vocab_id, name, con);
         }
         return vocab;
     }
 }
