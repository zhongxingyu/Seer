 package gov.usgs.cida.ncetl.spec;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import gov.usgs.webservices.jdbc.spec.Spec;
 import gov.usgs.webservices.jdbc.spec.mapping.ColumnMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.SearchMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.WhereClauseType;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import thredds.catalog.CollectionType;
import thredds.catalog.DataFormatType;
 import thredds.catalog.InvCatalog;
 import thredds.catalog.InvCatalogModifier;
 import thredds.catalog.InvDataset;
 import thredds.catalog.InvDatasetWrapper;
 import thredds.catalog.ThreddsMetadata.Contributor;
 import thredds.catalog.ThreddsMetadata.GeospatialCoverage;
 import thredds.catalog.ThreddsMetadata.Source;
 import thredds.catalog.ThreddsMetadata.Vocab;
 import ucar.nc2.constants.FeatureType;
 import ucar.nc2.units.DateRange;
 
 /**
  *
  * @author Ivan Suftin <isuftin@usgs.gov>
  */
 public class DatasetSpec extends AbstractNcetlSpec {
 
     private static final long serialVersionUID = 1L;
    // id int, catalog_id int, collection_type_id int, data_type_id int, name varchar(64), ncid varchar(128), authority varchar(64), inserted boolean, updated boolean
     private static final String TABLE_NAME = "dataset";
     public static final String CATALOG_ID = "catalog_id";
     public static final String COLLECTION_TYPE_ID = "collection_type_id";
     public static final String DATA_TYPE_ID = "data_type_id";
     public static final String NCID = "ncid";
     public static final String AUTHORITY = "authority";
     public static final String NAME = "name";
 
     @Override
     public String setupTableName() {
         return TABLE_NAME;
     }
 
     @Override
     public ColumnMapping[] setupColumnMap() {
         return new ColumnMapping[] {
                     new ColumnMapping(ID, ID),
                     new ColumnMapping(CATALOG_ID, CATALOG_ID),
                     new ColumnMapping(COLLECTION_TYPE_ID, COLLECTION_TYPE_ID),
                     new ColumnMapping(DATA_TYPE_ID, DATA_TYPE_ID),
                     new ColumnMapping(NCID, NCID),
                     new ColumnMapping(AUTHORITY, AUTHORITY),
                     new ColumnMapping(NAME, NAME),
                     new ColumnMapping(INSERTED, null),
                     new ColumnMapping(UPDATED, null)
                 };
     }
 
     @Override
     public SearchMapping[] setupSearchMap() {
         return new SearchMapping[] {
                     new SearchMapping(ID, ID, null, WhereClauseType.equals, null,
                                       null, null),
                     new SearchMapping("s_" + CATALOG_ID, CATALOG_ID, CATALOG_ID,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + COLLECTION_TYPE_ID,
                                       COLLECTION_TYPE_ID, COLLECTION_TYPE_ID,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + DATA_TYPE_ID, DATA_TYPE_ID,
                                       DATA_TYPE_ID, WhereClauseType.equals, null,
                                       null, null),
                     new SearchMapping("s_" + NCID, NCID, NCID,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + AUTHORITY, AUTHORITY, AUTHORITY,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + NAME, NAME, NAME,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + INSERTED, INSERTED, INSERTED,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + UPDATED, UPDATED, UPDATED,
                                       WhereClauseType.equals, null, null, null)
                 };
     }
 
     public static List<InvDataset> unmarshal(int id, InvCatalog cat,
                                              Connection con) throws SQLException, ParseException {
         List<InvDataset> result = Lists.newLinkedList();
        DatasetSpec spec = new DatasetSpec();
         Map<String, String[]> params = Maps.newHashMap();
         params.put("s_" + CATALOG_ID, new String[] { "" + id });
         Spec.loadParameters(spec, params);
         ResultSet rs = Spec.getResultSet(spec, con);
 
         while (rs.next()) {
             InvDatasetWrapper invDsWrapper = null;
             String ncId = rs.getString(NCID);
             InvDataset findDatasetByID = cat.findDatasetByID(ncId);
             String name = rs.getString(NAME.toUpperCase());
             if (findDatasetByID == null) {
                 invDsWrapper = new InvDatasetWrapper(name, ncId); 
                 InvCatalogModifier invCatalogModifier = new InvCatalogModifier(cat);
                 invCatalogModifier.setDatasets(Lists.asList(invDsWrapper, new InvDataset[0]));
             }
             else {
                 invDsWrapper = new InvDatasetWrapper(findDatasetByID);
             }
             int datasetId = -1;
 
             datasetId = rs.getInt(ID.toUpperCase());
 
             invDsWrapper.setAuthority(rs.getString(AUTHORITY.toUpperCase()));
 
             int ctId = rs.getInt(COLLECTION_TYPE_ID.toUpperCase());
             CollectionType ctype = CollectionTypeSpec.lookup(ctId, con);
             invDsWrapper.setCollectionType(ctype);
 
             int dtId = rs.getInt(DATA_TYPE_ID.toUpperCase());
             FeatureType dtype = DatatypeSpec.lookup(dtId, con);
             invDsWrapper.setDataType(dtype);
                         
 //            int dfId = rs.getInt()
 //            DataFormatType dft = DataFormatSpec.lookup(datasetId, con);
 //            invDsWrapper.setDataFormatType(dft);
             
             invDsWrapper.setID(rs.getString(NCID.toUpperCase()));
 
             invDsWrapper.setServiceName(ServiceSpec.lookupServiceNameByCatalogId(
                     id, con));
             
             List<Contributor> contributors = ContributorJoinSpec.unmarshal(datasetId, con);
             invDsWrapper.setContributors(contributors);
             
             GeospatialCoverage gc = GeospatialCoverageSpec.unmarshal(datasetId, con);
             invDsWrapper.setGeospatialCoverage(gc);
             
             DateRange tc = TimeCoverageSpec.unmarshal(datasetId, con);
             invDsWrapper.setTimeCoverage(tc);
             
             List<Vocab> keywords = KeywordJoinSpec.unmarshal(datasetId, con);
             invDsWrapper.setKeywords(keywords);
             
             List<Source> creators = CreatorJoinSpec.unmarshal(datasetId, con);
             invDsWrapper.setCreators(creators);
             
             List<Source> publishers = PublisherJoinSpec.unmarshal(datasetId, con);
             invDsWrapper.setPublishers(publishers);
             
             List<Vocab> projects = ProjectJoinSpec.unmarshal(datasetId, con);
             invDsWrapper.setProjects(projects);
 
             invDsWrapper.finish();
             
             result.add(invDsWrapper);
         }
 
         return result;
         // search database for datasets with id=id
         // set all the attributes and leaf nodes
         // search for children
         // recurse!
     }
 }
