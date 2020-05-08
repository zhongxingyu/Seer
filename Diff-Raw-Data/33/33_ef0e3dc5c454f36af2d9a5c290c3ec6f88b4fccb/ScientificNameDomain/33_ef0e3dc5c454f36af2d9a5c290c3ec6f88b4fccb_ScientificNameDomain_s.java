 package ro.finsiel.eunis.jrfTables.species.names;
 
 
 import net.sf.jrf.column.columnspecs.IntegerColumnSpec;
 import net.sf.jrf.column.columnspecs.ShortColumnSpec;
 import net.sf.jrf.column.columnspecs.StringColumnSpec;
 import net.sf.jrf.domain.AbstractDomain;
 import net.sf.jrf.domain.PersistentObject;
 import net.sf.jrf.join.OuterJoinTable;
 import net.sf.jrf.join.joincolumns.StringJoinColumn;
 import ro.finsiel.eunis.exceptions.CriteriaMissingException;
 import ro.finsiel.eunis.exceptions.InitializationException;
 import ro.finsiel.eunis.search.AbstractSearchCriteria;
 import ro.finsiel.eunis.search.AbstractSortCriteria;
 import ro.finsiel.eunis.search.Paginable;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.search.species.names.NameSearchCriteria;
 import ro.finsiel.eunis.search.species.names.NameSortCriteria;
 
 import java.util.List;
 
 
 /**
  * @author finsiel
  * @version 1.4
  * @since 23.01.2003
  *
  * QUERY IMPLEMENTED:
  * SELECT * FROM CHM62EDT_SPECIES AS A
  * INNER JOIN CHM62EDT_GROUP_SPECIES AS B ON A.ID_GROUP_SPECIES = B.ID_GROUP_SPECIES
  * INNER JOIN CHM62EDT_TAXONOMY AS C ON A.ID_TAXONOMY = C.ID_TAXONOMY
  * LEFT JOIN CHM62EDT_TAXONOMY AS D ON C.ID_TAXONOMY_LINK = D.ID_TAXONOMY WHERE ...
  */
 public class ScientificNameDomain extends AbstractDomain implements Paginable {
 
     /** Criterias applied for searching. 0 length means not criteria set*/
     private AbstractSearchCriteria[] searchCriteria = new AbstractSearchCriteria[0];
 
     /** Criterias applied for sorting. 0 length means unsorted */
     private AbstractSortCriteria[] sortCriteria = new AbstractSortCriteria[0];
 
     /** Cache the results of a count to avoid overhead queries for counting */
     private Long _resultCount = new Long(-1);
 
     private boolean showEUNISInvalidatedSpecies = false;
     private boolean searchSynnonyms = false;
     private boolean searchVernacular = false;
 
     /**
      * Normal constructor
      * @param searchCriteria Search criteria used to query the database
      * @param sortCriteria Sort criteria used to sort the results from database
      */
     public ScientificNameDomain(AbstractSearchCriteria[] searchCriteria,
             AbstractSortCriteria[] sortCriteria,
             boolean searchSynonyms,
             boolean showEUNISInvalidatedSpecies,
             Boolean searchVernacular) {
         this.searchCriteria = searchCriteria;
         this.sortCriteria = sortCriteria;
         this.showEUNISInvalidatedSpecies = showEUNISInvalidatedSpecies;
         this.searchSynnonyms = searchSynonyms;
         this.searchVernacular = false;
         if (searchVernacular != null) {
             this.searchVernacular = searchVernacular.booleanValue();
         }
         // System.out.println( "searchVernacular = " + this.searchVernacular );
     }
 
     /**
      * Used by JRF Framework
      * @return The PersistentObject representing a row from results
      */
     public PersistentObject newPersistentObject() {
         return new ScientificNamePersist();
     }
 
     /**
      * Used by JRF Framework
      */
     public void setup() {
         // These setters could be used to override the default.
         // this.setDatabasePolicy(new null());
         // this.setJDBCHelper(JDBCHelperFactory.create());
 
         this.setTableName("CHM62EDT_SPECIES");
         this.setReadOnly(true);
         this.setTableAlias("A");
 
         // Table declaration
         this.addColumnSpec(
                 new IntegerColumnSpec("ID_SPECIES", "getIdSpecies",
                 "setIdSpecies", DEFAULT_TO_ZERO, NATURAL_PRIMARY_KEY));
         this.addColumnSpec(
                 new IntegerColumnSpec("ID_NATURE_OBJECT", "getIdNatureObject",
                 "setIdNatureObject", DEFAULT_TO_ZERO, REQUIRED));
         this.addColumnSpec(
                 new StringColumnSpec("SCIENTIFIC_NAME", "getScientificName",
                 "setScientificName", DEFAULT_TO_EMPTY_STRING, REQUIRED));
         this.addColumnSpec(
                 new ShortColumnSpec("VALID_NAME", "getValidName", "setValidName",
                 null, REQUIRED));
         this.addColumnSpec(
                 new IntegerColumnSpec("ID_SPECIES_LINK", "getIdSpeciesLink",
                 "setIdSpeciesLink", DEFAULT_TO_NULL));
         this.addColumnSpec(
                 new StringColumnSpec("TYPE_RELATED_SPECIES",
                 "getTypeRelatedSpecies", "setTypeRelatedSpecies",
                 DEFAULT_TO_NULL));
         this.addColumnSpec(
                 new ShortColumnSpec("TEMPORARY_SELECT", "getTemporarySelect",
                 "setTemporarySelect", null));
         this.addColumnSpec(
                 new StringColumnSpec("SPECIES_MAP", "getSpeciesMap",
                 "setSpeciesMap", DEFAULT_TO_NULL));
         this.addColumnSpec(
                 new IntegerColumnSpec("ID_GROUP_SPECIES", "getIdGroupspecies",
                 "setIdGroupspecies", DEFAULT_TO_ZERO, REQUIRED));
         this.addColumnSpec(
                 new StringColumnSpec("ID_TAXONOMY", "getIdTaxcode",
                 "setIdTaxcode", DEFAULT_TO_NULL));
 
        if(searchVernacular)
            this.addColumnSpec(new ShortColumnSpec("s_order", "getSortOrder", "setSortOrder", null));
 
         // Joined tables
         OuterJoinTable groupSpecies;
 
         groupSpecies = new OuterJoinTable("CHM62EDT_GROUP_SPECIES B",
                 "ID_GROUP_SPECIES", "ID_GROUP_SPECIES");
         groupSpecies.addJoinColumn(
                 new StringJoinColumn("COMMON_NAME", "commonName",
                 "setCommonName"));
         this.addJoinTable(groupSpecies);
 
         OuterJoinTable taxCodeFamily;
 
         taxCodeFamily = new OuterJoinTable("CHM62EDT_TAXONOMY C", "ID_TAXONOMY",
                 "ID_TAXONOMY");
         taxCodeFamily.addJoinColumn(
                 new StringJoinColumn("NAME", "taxonomyName", "setTaxonomyName"));
         taxCodeFamily.addJoinColumn(
                 new StringJoinColumn("LEVEL", "taxonomyLevel",
                 "setTaxonomyLevel"));
         taxCodeFamily.addJoinColumn(
                 new StringJoinColumn("TAXONOMY_TREE", "taxonomyTree",
                 "setTaxonomyTree"));
         taxCodeFamily.addJoinColumn(
                 new StringJoinColumn("NAME", "taxonomicNameFamily",
                 "setTaxonomicNameFamily"));
         taxCodeFamily.addJoinColumn(
                 new StringJoinColumn("NAME", "taxonomicNameOrder",
                 "setTaxonomicNameOrder"));
         this.addJoinTable(taxCodeFamily);
 
         // OuterJoinTable taxCodeOrder;
         // taxCodeOrder = new OuterJoinTable("CHM62EDT_TAXONOMY D", "ID_TAXONOMY_LINK", "ID_TAXONOMY");
         // taxCodeOrder.addJoinColumn(new StringJoinColumn("NAME", "taxonomicNameOrder", "setTaxonomicNameOrder"));
         // taxCodeFamily.addJoinTable(taxCodeOrder);
     }
 
     /** This method is used to retrieve a sub-set of the main results of a query given its start index offset and end
      * index offset.
      * @param offsetStart The start offset (i.e. 0). If offsetStart = offSetEnd then return the whole list
      * @param pageSize The end offset (i.e. 1). If offsetStart = offSetEnd then return the whole list
      * @param sortCriteria The criteria used for sorting
      * @return A list of objects which match query criteria
      */
     public List getResults(int offsetStart, int pageSize, AbstractSortCriteria[] sortCriteria) throws CriteriaMissingException {
         this.sortCriteria = sortCriteria;
 
         if (searchVernacular) {
             // adds ordering for synonym and origin
            AbstractSortCriteria[] asc = new AbstractSortCriteria[sortCriteria.length+2];
            asc[0] = new NameSortCriteria(NameSortCriteria.SORT_S_ORDER, AbstractSortCriteria.ASCENDENCY_ASC, true);
            asc[1] = new NameSortCriteria(NameSortCriteria.SORT_VALID_NAME, AbstractSortCriteria.ASCENDENCY_DESC, true);
            System.arraycopy(sortCriteria, 0, asc, 2, sortCriteria.length);
             this.sortCriteria = asc;
         }
 
         List results;
 
         if (searchVernacular) {
             String where = _prepareWhereSearch().toString();
             String query = "(" + " SELECT A.ID_SPECIES, A.ID_NATURE_OBJECT,"
                     + " A.SCIENTIFIC_NAME AS scientificName,"
                     + " A.VALID_NAME AS validName," + " A.ID_SPECIES_LINK,"
                     + " A.TYPE_RELATED_SPECIES," + " A.TEMPORARY_SELECT,"
                     + " A.SPECIES_MAP," + " A.ID_GROUP_SPECIES,"
                    + " A.ID_TAXONOMY, 1 as s_order, " +
                    " B.COMMON_NAME AS commonName,"
                     + " C.NAME AS taxonomyName," + " C.LEVEL, "
                     + " C.TAXONOMY_TREE, " + " C.NAME AS taxonomicNameFamily, "
                     + " C.NAME AS taxonomicNameOrder"
                     + " FROM CHM62EDT_SPECIES A LEFT"
                     + " OUTER JOIN CHM62EDT_GROUP_SPECIES B ON A.ID_GROUP_SPECIES=B.ID_GROUP_SPECIES"
                     + " LEFT OUTER JOIN CHM62EDT_TAXONOMY C ON A.ID_TAXONOMY=C.ID_TAXONOMY LEFT"
                     + " OUTER JOIN CHM62EDT_TAXONOMY D ON C.ID_TAXONOMY_LINK=D.ID_TAXONOMY"
                     + " WHERE " + where
                     + " ) UNION ( "
                     + " SELECT A.ID_SPECIES,"
                     + " A.ID_NATURE_OBJECT,"
                     + " A.SCIENTIFIC_NAME AS scientificName,"
                     + " A.VALID_NAME AS validName," + " A.ID_SPECIES_LINK,"
                     + " A.TYPE_RELATED_SPECIES," + " A.TEMPORARY_SELECT,"
                     + " A.SPECIES_MAP," + " A.ID_GROUP_SPECIES,"
                    + " A.ID_TAXONOMY, 2 as s_order, "
                     + " B.COMMON_NAME AS commonName,"
                     + " C.NAME AS taxonomyName," + " C.LEVEL,"
                     + " C.TAXONOMY_TREE, " + " C.NAME AS taxonomicNameFamily, "
                     + " C.NAME AS taxonomicNameOrder"
                     + " FROM CHM62EDT_REPORTS G"
                     + " INNER JOIN CHM62EDT_REPORT_TYPE F ON G.ID_REPORT_TYPE=F.ID_REPORT_TYPE"
                     + " INNER JOIN CHM62EDT_SPECIES A ON G.ID_NATURE_OBJECT=A.ID_NATURE_OBJECT"
                     + " LEFT OUTER JOIN CHM62EDT_GROUP_SPECIES B ON A.ID_GROUP_SPECIES=B.ID_GROUP_SPECIES"
                     + " LEFT OUTER JOIN CHM62EDT_TAXONOMY C ON A.ID_TAXONOMY=C.ID_TAXONOMY"
                     + " LEFT OUTER JOIN CHM62EDT_TAXONOMY D ON C.ID_TAXONOMY_LINK=D.ID_TAXONOMY"
                     + " INNER JOIN CHM62EDT_REPORT_ATTRIBUTES I ON G.ID_REPORT_ATTRIBUTES=I.ID_REPORT_ATTRIBUTES"
                     + " WHERE " + _prepareWhereSearchVernacular()
                     + " AND F.LOOKUP_TYPE = 'LANGUAGE'"
                     + " AND I.NAME ='VERNACULAR_NAME' "
                     + " ) " +  _prepareWhereSort()
                     + " LIMIT " + offsetStart + ", " + pageSize;
             results = this.findCustom(query);
         } else {
             if (searchCriteria.length < 1) {
                 throw new CriteriaMissingException(
                         "Unable to search because no search criteria was specified...");
             }
             // Prepare the WHERE clause
             StringBuffer filterSQL = _prepareWhereSearch();
 
             // Add the ORDER BY clause to do the sorting
             if (sortCriteria.length > 0) {
                 filterSQL.append(_prepareWhereSort());
             }
             // Add the LIMIT clause to return only the wanted results
             if (pageSize != 0) {
                 filterSQL.append(" LIMIT " + offsetStart + ", " + pageSize);
             }
             results = this.findWhere(filterSQL.toString());
         }
         _resultCount = new Long(-1); // After each query, reset the _resultCount, so countResults do correct numbering.
         return results;
     }
 
     /** This method is used to count the total list of results from a query. It is used to find all for use in pagination.
      * Having the total number of results and the results displayed per page, the you could find the number of pages i.e.
      * @return Number of results found
      */
     public Long countResults() throws CriteriaMissingException {
         if (-1 == _resultCount.longValue()) {
             _resultCount = _rawCount();
         }
         return _resultCount;
     }
 
     /** This method does the raw counting (meaning that will do a DB query for retrieving results count). You should check
      * in your code that this method is called (in ideal way) only once and results are cached. This is what
      * countResults() method does in this class
      * @see ro.finsiel.eunis.jrfTables.species.country.RegionDomain#countResults
      * @return count of the total list of results from a query as Long
      * @throws ro.finsiel.eunis.exceptions.CriteriaMissingException
      */
     private Long _rawCount() throws CriteriaMissingException {
         Long ret;
         StringBuffer sql = new StringBuffer();
 
         // Set the main QUERY
         if (searchVernacular) {
             sql.append(
                     " select count(idSpecies) from ("
                             + " SELECT A.ID_SPECIES As idSpecies"
                             + " FROM CHM62EDT_SPECIES A"
                             + " LEFT OUTER JOIN CHM62EDT_GROUP_SPECIES B ON"
                             + " A.ID_GROUP_SPECIES=B.ID_GROUP_SPECIES"
                             + " LEFT OUTER JOIN CHM62EDT_TAXONOMY C ON A.ID_TAXONOMY=C.ID_TAXONOMY"
                             + " LEFT OUTER JOIN CHM62EDT_TAXONOMY D ON C.ID_TAXONOMY_LINK=D.ID_TAXONOMY"
                             + " WHERE " + _prepareWhereSearch().toString()
                             + " UNION" + " SELECT A.ID_SPECIES AS idSpecies"
                             + " FROM CHM62EDT_REPORTS G"
                             + " INNER JOIN CHM62EDT_REPORT_TYPE F ON G.ID_REPORT_TYPE=F.ID_REPORT_TYPE"
                             + " INNER JOIN CHM62EDT_SPECIES A ON G.ID_NATURE_OBJECT=A.ID_NATURE_OBJECT"
                             + " LEFT OUTER JOIN CHM62EDT_GROUP_SPECIES B ON A.ID_GROUP_SPECIES=B.ID_GROUP_SPECIES"
                             + " LEFT OUTER JOIN CHM62EDT_TAXONOMY C ON A.ID_TAXONOMY=C.ID_TAXONOMY"
                             + " LEFT OUTER JOIN CHM62EDT_TAXONOMY D ON C.ID_TAXONOMY_LINK=D.ID_TAXONOMY"
                             + " INNER JOIN CHM62EDT_REPORT_ATTRIBUTES I ON G.ID_REPORT_ATTRIBUTES=I.ID_REPORT_ATTRIBUTES"
                             + " WHERE " + _prepareWhereSearchVernacular()
                             + " AND F.LOOKUP_TYPE = 'LANGUAGE'"
                             + " AND I.NAME ='VERNACULAR_NAME'" + " ) AS H");
             ret = findLong(sql.toString());
 
         } else {
             sql.append(
                     "SELECT COUNT(*) " + "FROM CHM62EDT_SPECIES AS A "
                     + "LEFT JOIN CHM62EDT_GROUP_SPECIES AS B ON A.ID_GROUP_SPECIES = B.ID_GROUP_SPECIES "
                     + "LEFT JOIN CHM62EDT_TAXONOMY AS C ON A.ID_TAXONOMY = C.ID_TAXONOMY "
                     + "LEFT JOIN CHM62EDT_TAXONOMY AS D ON C.ID_TAXONOMY_LINK = D.ID_TAXONOMY WHERE ");
             // Apply WHERE CLAUSE
             sql.append(_prepareWhereSearch().toString());
             ret = findLong(sql.toString());
         }
         if (null == ret) {
             ret = new Long(0);
         }
         return ret;
     }
 
     /** This helper method is used to construct the string after WHERE...based on search criterias set. In another words
      * constructs .....WHERE...>>B.ID_GEOSCOPE_LINK=XXX OR B.ID_GEOSCOPE_LINK=YYY OR B.ID_GEOSCOPE_LINK=ZZZ .....
      * @return SQL string
      * @throws ro.finsiel.eunis.exceptions.CriteriaMissingException If no search criteria search or wrong criteria is set.
      */
     private StringBuffer _prepareWhereSearch() throws CriteriaMissingException {
         StringBuffer filterSQL = new StringBuffer();
 
         if (searchCriteria.length <= 0) {
             throw new CriteriaMissingException(
                     "No criteria set for searching. Search interrupted.");
         }
         for (int i = 0; i < searchCriteria.length; i++) {
             if (i > 0) {
                 filterSQL.append(" AND ");
             }
             NameSearchCriteria aCriteria = (NameSearchCriteria) searchCriteria[i];
 
             // upcast
             filterSQL.append(aCriteria.toSQL());
         }
         // Here we've changed the way search is done as follows:
         // - if user checks 'Search in synonyms' then no additional condition is neccesary.
         // - if user unchecks 'Search in synonyms' then we add an additional condition:
         // "AND A.TYPE_RELATED_SPECIES='Species'" thus filtering and returning only 'species and NOT synonyms'.
         if (!searchSynnonyms) {
             filterSQL.append(" AND A.TYPE_RELATED_SPECIES='Species' ");
         }
         // The condition below is changed so that 'A.TYPE_RELATED_SPECIES' takes precedence for 'A.VALID_NAME'
         // - If an user is LOGGED we return also VALID_NAME == 0 species
         // - If an user is NOT LOGGED, we return also VALID_NAME == 0 species BUT ONLY
         // those who have "TYPE_RELATED_SPECIES='Syn'" ( synonyms).
         // filterSQL.append(Utilities.showEUNISInvalidatedSpecies("AND A.VALID_NAME", showEUNISInvalidatedSpecies));
         if (!showEUNISInvalidatedSpecies) {
             filterSQL.append(
                     " AND ( ( A.VALID_NAME > 0 ) OR ( A.VALID_NAME = 0 AND A.TYPE_RELATED_SPECIES='Synonym' ) ) ");
         }
         return filterSQL;
     }
 
     private StringBuffer _prepareWhereSearchVernacular() throws CriteriaMissingException {
         StringBuffer filterSQL = new StringBuffer();
 
         if (searchCriteria.length <= 0) {
             throw new CriteriaMissingException(
                     "No criteria set for searching. Search interrupted.");
         }
         for (int i = 0; i < searchCriteria.length; i++) {
             NameSearchCriteria aCriteria = (NameSearchCriteria) searchCriteria[i];
 
             if (aCriteria.isMainCriteria()) {
                 filterSQL.append(
                         Utilities.prepareSQLOperator("I.VALUE",
                         aCriteria.getScientificName(), aCriteria.getRelationOp()));
 
 //                removes the duplicates
                String original = aCriteria.toSQL();

                if(original.contains("LIKE")) original = original.replace("LIKE", "NOT LIKE");
                if(original.contains("=")) original = original.replace("=", "<>");
 
                filterSQL.append( " AND " + original);
             } else {
                 if (i > 0) {
                     filterSQL.append(" AND ");
                 }
                 filterSQL.append(aCriteria.toSQL());
             }
         }
         if (!showEUNISInvalidatedSpecies) {
             filterSQL.append(" AND A.VALID_NAME > 0 ");
         }
         return filterSQL;
     }
 
     /** Prepare the ORDER BY clause used to do the sorting. Basically it adds the ORDER clause with the criterias set in
      * the sortCriteria[] array.
      * @return SQL representation of the sorting.
      */
     private StringBuffer _prepareWhereSort() {
         StringBuffer filterSQL = new StringBuffer();
 
         try {
             boolean useSort = false;
 
             if (sortCriteria.length > 0) {
                 int i = 0;
 
                 do {
                     if (i > 0) {
                         filterSQL.append(", ");
                     }
                     NameSortCriteria criteria = (NameSortCriteria) sortCriteria[i]; // Notice the upcast here
 
                     if (!criteria.getCriteriaAsString().equals("none")) { // Do not add if criteria is sort to NOT SORT
                         if (!criteria.getAscendencyAsString().equals("none")) { // Don't add if ascendency is set to none, nasty hacks
                             filterSQL.append(
                                     criteria.toSQL());
                             useSort = true;
                         }
                     }
                     i++;
                 } while (i < sortCriteria.length);
             }
             if (useSort) { // If a sort criteria was indeed used, then insert ORDER BY clause at the start of the string
                 filterSQL.insert(0, " ORDER BY ");
             }
         } catch (InitializationException e) {
             e.printStackTrace(); // To change body of catch statement use Options | File Templates.
         } finally {
             return filterSQL;
         }
     }
 }
