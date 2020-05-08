 package eionet.eunis.dao.impl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
import org.apache.commons.lang.StringUtils;
 import org.displaytag.properties.SortOrderEnum;
 
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.utilities.EunisUtil;
 import eionet.eunis.dao.IReferencesDao;
 import eionet.eunis.dto.AttributeDto;
 import eionet.eunis.dto.DcIndexDTO;
 import eionet.eunis.dto.DesignationDcObjectDTO;
 import eionet.eunis.dto.PairDTO;
 import eionet.eunis.dto.ReferenceDTO;
 import eionet.eunis.dto.readers.DcIndexDTOReader;
 import eionet.eunis.dto.readers.ReferenceDTOReader;
 import eionet.eunis.stripes.actions.ReferencesActionBean;
 import eionet.eunis.util.CustomPaginatedList;
 
 /**
  * @author Risto Alt <a href="mailto:risto.alt@tieto.com">contact</a>
  */
 public class ReferencesDaoImpl extends MySqlBaseDao implements IReferencesDao {
 
     public ReferencesDaoImpl() {
     }
 
     /**
      * @see eionet.eunis.dao.IReferencesDao#getReferences(int page, int defaltPageSize, String sort, String dir) {@inheritDoc}
      */
     @Override
     public CustomPaginatedList<ReferenceDTO> getReferences(int page, int defaltPageSize, String sort, String dir, String like) {
 
         CustomPaginatedList<ReferenceDTO> ret = new CustomPaginatedList<ReferenceDTO>();
 
         page = Math.max(page, 1);
         int offset = (page - 1) * defaltPageSize;
 
         String order = "";
         if (sort != null) {
             if (sort.equals("idRef")) {
                 order = "ID_DC";
             } else if (sort.equals("refTitle")) {
                 order = "TITLE";
             } else if (sort.equals("author")) {
                 order = "SOURCE";
             } else if (sort.equals("refYear")) {
                 order = "CREATED";
             }
         }
 
         if (order != null && order.length() > 0) {
 
             if (dir == null || (!dir.equalsIgnoreCase("asc") && !dir.equalsIgnoreCase("desc"))) {
                 dir = "ASC";
             }
             order = order + " " + dir.toUpperCase();
         }
 
         String query = "SELECT ID_DC, TITLE, ALTERNATIVE, SOURCE, CREATED FROM DC_INDEX";
 
         String trimmedLike = "";
         boolean likeAdded = false;
         if (like != null) {
             trimmedLike = like.trim();
             if (trimmedLike.length() > 0 && !trimmedLike.equalsIgnoreCase(ReferencesActionBean.DEFAULT_FILTER_VALUE)) {
                 query = query + " WHERE (TITLE LIKE ? OR SOURCE LIKE ?) ";
                 likeAdded = true;
             }
         }
         if (order != null && order.length() > 0) {
             query = query + " ORDER BY " + order;
         }
         query = query + (defaltPageSize > 0 ? " LIMIT " + (offset > 0 ? offset + "," : "") + defaltPageSize : "");
         List<Object> values = new ArrayList<Object>();
         if (likeAdded) {
             values.add("%" + trimmedLike + "%");
             values.add("%" + trimmedLike + "%");
         }
 
         ReferenceDTOReader rsReader = new ReferenceDTOReader();
         try {
 
             executeQuery(query, values, rsReader);
             List<ReferenceDTO> docs = rsReader.getResultList();
 
             int listSize = getReferencesCnt(like);
             ret.setList(docs);
             ret.setFullListSize(listSize);
             ret.setObjectsPerPage(defaltPageSize);
             if (page == 0) {
                 page = 1;
             }
             ret.setPageNumber(page);
             ret.setSortCriterion(sort);
             if (dir != null && dir.equals("asc")) {
                 ret.setSortDirection(SortOrderEnum.ASCENDING);
             } else if (dir != null && dir.equals("desc")) {
                 ret.setSortDirection(SortOrderEnum.DESCENDING);
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return ret;
     }
 
     /**
      *
      * @param like
      * @return
      */
     private int getReferencesCnt(String like) {
         int ret = 0;
        String trimmedLike = like == null ? StringUtils.EMPTY : like.trim();
         String query = "SELECT COUNT(*) FROM DC_INDEX";
         if (trimmedLike.length() > 0 && !trimmedLike.equalsIgnoreCase(ReferencesActionBean.DEFAULT_FILTER_VALUE)) {
             query += " WHERE (TITLE LIKE ? OR SOURCE LIKE ?) ";
         }
         Connection con = null;
         PreparedStatement preparedStatement = null;
         ResultSet rs = null;
 
         try {
 
             con = getConnection();
             preparedStatement = con.prepareStatement(query);
             if (trimmedLike.length() > 0 && !trimmedLike.equalsIgnoreCase(ReferencesActionBean.DEFAULT_FILTER_VALUE)) {
                 preparedStatement.setString(1, "%" + trimmedLike + "%");
                 preparedStatement.setString(2, "%" + trimmedLike + "%");
             }
             rs = preparedStatement.executeQuery();
             while (rs.next()) {
                 ret = rs.getInt(1);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return ret;
     }
 
     /**
      * @see eionet.eunis.dao.IReferencesDao#getDcAttributes(java.lang.String) {@inheritDoc}
      */
     @Override
     public List<AttributeDto> getDcAttributes(String idDc) {
         List<AttributeDto> ret = new ArrayList<AttributeDto>();
 
         Connection con = null;
         PreparedStatement preparedStatement = null;
         ResultSet rs = null;
 
         try {
             con = getConnection();
             preparedStatement =
                     con.prepareStatement("SELECT a.NAME, b.LABEL, a.OBJECT, a.OBJECTLANG, a.TYPE from dc_attributes AS a LEFT OUTER JOIN dc_attribute_labels AS b ON a.NAME = b.NAME WHERE ID_DC = ?");
             preparedStatement.setString(1, idDc);
             rs = preparedStatement.executeQuery();
             while (rs.next()) {
                 String name = rs.getString("NAME");
                 String object = rs.getString("OBJECT");
                 String lang = rs.getString("OBJECTLANG");
                 String type = rs.getString("TYPE");
                 String label = rs.getString("LABEL");
                 if (label == null) {
                     label = name;
                 }
                 AttributeDto attr = new AttributeDto(name, type, object, lang, label);
                 ret.add(attr);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             closeAllResources(con, preparedStatement, rs);
         }
         return ret;
     }
 
     /**
      * @see eionet.eunis.dao.IReferencesDao#getDcIndex(String id) {@inheritDoc}
      */
     @Override
     public DcIndexDTO getDcIndex(String id) {
 
         DcIndexDTO object = null;
 
         String query = "SELECT * FROM DC_INDEX WHERE ID_DC = ?";
         Connection con = null;
         PreparedStatement preparedStatement = null;
         ResultSet rs = null;
 
         try {
 
             con = getConnection();
             preparedStatement = con.prepareStatement(query);
             preparedStatement.setString(1, id);
             rs = preparedStatement.executeQuery();
             while (rs.next()) {
                 object = new DcIndexDTO();
                 object.setIdDc(rs.getString("ID_DC"));
                 object.setComment(rs.getString("COMMENT"));
                 object.setRefCd(rs.getString("REFCD"));
                 object.setReference(rs.getString("REFERENCE"));
                 object.setSource(rs.getString("SOURCE"));
                 object.setEditor(rs.getString("EDITOR"));
                 object.setJournalTitle(rs.getString("JOURNAL_TITLE"));
                 object.setBookTitle(rs.getString("BOOK_TITLE"));
                 object.setJournalIssue(rs.getString("JOURNAL_ISSUE"));
                 object.setIsbn(rs.getString("ISBN"));
                 object.setUrl(rs.getString("URL"));
                 object.setCreated(rs.getString("CREATED"));
                 object.setTitle(rs.getString("TITLE"));
                 object.setAlternative(rs.getString("ALTERNATIVE"));
                 object.setPublisher(rs.getString("PUBLISHER"));
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             closeAllResources(con, preparedStatement, rs);
         }
 
         return object;
     }
 
     /**
      * @see eionet.eunis.dao.IReferencesDao#getDcObjects() {@inheritDoc}
      */
     @Override
     public List<DcIndexDTO> getDcObjects() {
 
         List<DcIndexDTO> ret = new ArrayList<DcIndexDTO>();
 
         String query = "SELECT * FROM DC_INDEX";
 
         List<Object> values = new ArrayList<Object>();
         DcIndexDTOReader rsReader = new DcIndexDTOReader();
 
         try {
 
             executeQuery(query, values, rsReader);
             ret = rsReader.getResultList();
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return ret;
     }
 
     @Override
     public Integer insertSource(String title, String source, String publisher, String editor, String url, String year)
             throws Exception {
         Integer ret = null;
 
         if (source != null) {
             int id_dc = getId("SELECT MAX(ID_DC) FROM DC_INDEX");
 
             id_dc++;
             ret = new Integer(id_dc);
 
             String insertIndex =
                     "INSERT INTO dc_index (ID_DC, REFERENCE, COMMENT, REFCD, "
                             + "TITLE, PUBLISHER, CREATED, SOURCE, EDITOR, URL) VALUES (?,-1,'RED_LIST',0,?,?,?,?,?,?)";
 
             Connection con = null;
             PreparedStatement psIndex = null;
 
             try {
                 con = getConnection();
                 psIndex = con.prepareStatement(insertIndex);
                 psIndex.setInt(1, id_dc);
                 psIndex.setString(2, title);
                 psIndex.setString(3, publisher);
                 if (year != null && year.length() == 4) {
                     psIndex.setInt(4, new Integer(year).intValue());
                 } else {
                     psIndex.setInt(4, Types.NULL);
                 }
                 psIndex.setString(5, source);
                 psIndex.setString(6, editor);
                 psIndex.setString(7, url);
                 psIndex.executeUpdate();
 
             } catch (SQLException e) {
                 e.printStackTrace();
             } finally {
                 con.close();
                 psIndex.close();
             }
         }
         return ret;
     }
 
     private int getId(String query) throws ParseException {
         String maxId = ExecuteSQL(query);
         int maxIdInt = 0;
 
         if (maxId != null && maxId.length() > 0) {
             maxIdInt = new Integer(maxId).intValue();
         }
 
         return maxIdInt;
     }
 
     @Override
     public DesignationDcObjectDTO getDesignationDcObject(String idDesig, String idGeo) throws Exception {
 
         DesignationDcObjectDTO ret = null;
 
         String query =
                 "SELECT SOURCE, EDITOR, CREATED, TITLE, PUBLISHER " + "FROM CHM62EDT_DESIGNATIONS "
                         + "INNER JOIN DC_INDEX ON (CHM62EDT_DESIGNATIONS.ID_DC = DC_INDEX.ID_DC) "
                         + "WHERE CHM62EDT_DESIGNATIONS.ID_DESIGNATION = ? AND CHM62EDT_DESIGNATIONS.ID_GEOSCOPE = ?";
 
         Connection con = null;
         PreparedStatement preparedStatement = null;
         ResultSet rs = null;
 
         try {
             con = getConnection();
             preparedStatement = con.prepareStatement(query);
             preparedStatement.setString(1, idDesig);
             preparedStatement.setString(2, idGeo);
             rs = preparedStatement.executeQuery();
             while (rs.next()) {
                 ret = new DesignationDcObjectDTO();
                 ret.setAuthor(Utilities.formatString(Utilities.FormatDatabaseFieldName(rs.getString(1)), ""));
                 ret.setEditor(Utilities.formatString(Utilities.FormatDatabaseFieldName(rs.getString(2)), ""));
                 ret.setDate(Utilities.formatString(Utilities.formatReferencesDate(rs.getDate(3)), ""));
                 ret.setTitle(Utilities.formatString(Utilities.FormatDatabaseFieldName(rs.getString(4)), ""));
                 ret.setPublisher(Utilities.formatString(Utilities.FormatDatabaseFieldName(rs.getString(5)), ""));
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             closeAllResources(con, preparedStatement, rs);
         }
 
         return ret;
     }
 
     @Override
     public List<PairDTO> getRedListSources() throws Exception {
 
         List<PairDTO> ret = new ArrayList<PairDTO>();
 
         String query =
                 "SELECT I.ID_DC, I.SOURCE, I.TITLE " + "FROM chm62edt_reports AS R, chm62edt_report_type AS T, DC_INDEX AS I "
                         + "WHERE R.ID_REPORT_TYPE = T.ID_REPORT_TYPE AND T.LOOKUP_TYPE = 'CONSERVATION_STATUS' "
                         + "AND R.ID_DC = I.ID_DC GROUP BY R.ID_DC ORDER BY I.TITLE";
 
         Connection con = null;
         PreparedStatement preparedStatement = null;
         ResultSet rs = null;
 
         try {
 
             con = getConnection();
             preparedStatement = con.prepareStatement(query);
             rs = preparedStatement.executeQuery();
             while (rs.next()) {
 
                 String idDc = rs.getString("ID_DC");
                 String title = rs.getString("TITLE");
                 String source = rs.getString("SOURCE");
 
                 String heading = EunisUtil.threeDots(title, 50) + " (" + source + ")";
                 PairDTO pair = new PairDTO();
 
                 pair.setKey(idDc);
                 pair.setValue(heading);
                 ret.add(pair);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             closeAllResources(con, preparedStatement, rs);
         }
         return ret;
     }
 
 }
