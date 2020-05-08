 package org.mvnsearch.snippet.domain.manager.impl;
 
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.MatchMode;
 import org.joda.time.DateTime;
 import org.mvnsearch.snippet.domain.Category;
 import org.mvnsearch.snippet.domain.Snippet;
 import org.mvnsearch.snippet.domain.extra.Comment;
 import org.mvnsearch.snippet.domain.manager.CategoryManager;
 import org.mvnsearch.snippet.domain.manager.SnippetManager;
 import org.mvnsearch.snippet.domain.manager.SnippetService;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * snippet service implement
  *
  * @author linux_china@hotmail.com
  */
 public class SnippetServiceImpl extends HibernateDaoSupport implements SnippetService {
     private SimpleJdbcTemplate jdbcTemplate;
     private CategoryManager categoryManager;
     private SnippetManager snippetManager;
 
     /**
      * inject jdbc template
      *
      * @param jdbcTemplate jdbc template
      */
     public void setJdbcTemplate(SimpleJdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     /**
      * inject snippet manager
      *
      * @param snippetManager snippet manager
      */
     public void setSnippetManager(SnippetManager snippetManager) {
         this.snippetManager = snippetManager;
     }
 
     /**
      * inject category manager
      *
      * @param categoryManager category manager bean
      */
     public void setCategoryManager(CategoryManager categoryManager) {
         this.categoryManager = categoryManager;
     }
 
     /**
      * render template into code
      *
      * @param mnemonic    mnemonic
      * @param packageName package name
      * @param fileName    file name
      * @param author      author name
      * @return rendered code
      */
     public String renderTemplate(String mnemonic, String packageName, String fileName, String author) {
         DetachedCriteria criteria = DetachedCriteria.forClass(Snippet.class);
        criteria.add(Restrictions.like("mnemonic", mnemonic, MatchMode.END));
         List<Snippet> snippets = getHibernateTemplate().findByCriteria(criteria);
         if (!snippets.isEmpty()) {
             Snippet snippet = snippets.get(0);
             String className = fileName;
             if (StringUtils.isNotEmpty(className) && className.indexOf(".") != -1) {
                 className = className.substring(0, className.indexOf("."));
             }
             String code = snippet.getCode();
             if (StringUtils.isNotEmpty(author)) {
                 code = code.replace("${USER}", author);
             }
             if (StringUtils.isNotEmpty(packageName)) {
                 code = code.replace("${PACKAGE_NAME}", packageName);
             }
             if (StringUtils.isNotEmpty(fileName)) {
                 code = code.replace("${NAME}", className);
                 code = code.replace("${FILE_NAME}", fileName);
             }
             //date info replace
             DateTime now = new DateTime();
             code = code.replace("${YEAR}", String.valueOf(now.getYear()));
             code = code.replace("${MONTH}", String.valueOf(now.getMonthOfYear()));
             code = code.replace("${DAY}", String.valueOf(now.getDayOfMonth()));
             return code;
         }
         return "";
     }
 
     /**
      * find snippet by mnemonic
      *
      * @param mnemonic mnemonic
      * @return snippet object
      */
     public Map<String, String> findSnippetByMnemonic(String mnemonic) {
         Snippet snippet = snippetManager.findSnippetByMnemonic(mnemonic);
         return snippet != null ? convertSnippetToMap(snippet) : null;
     }
 
     /**
      * find mnemonic list according to prefix
      *
      * @param prefix prefix
      * @return mnemonic list, max size is 100
      */
     public List<String> findMnemonicList(String prefix) {
         List<String> mnemonicList = new ArrayList<String>();
         List<Map<String, Object>> maps = jdbcTemplate.queryForList("select mnemonic from snippets where mnemonic like '" + prefix + "%'");
         for (Map<String, Object> map : maps) {
             mnemonicList.add((String) map.get("mnemonic"));
         }
         return mnemonicList;
     }
 
     /**
      * find snipepts by mnemonic
      *
      * @param mnemonicPrefix mnemonic prefix
      * @return snippet map list
      */
     public List<Map<String, String>> findSnippetsByMnemonic(String mnemonicPrefix) {
         DetachedCriteria criteria = DetachedCriteria.forClass(Snippet.class);
         criteria.add(Restrictions.like("mnemonic", mnemonicPrefix + "%"));
         List<Snippet> snippets = getHibernateTemplate().findByCriteria(criteria);
         if (!snippets.isEmpty()) {
             List<Map<String, String>> snippetList = new ArrayList<Map<String, String>>();
             for (Snippet snippet : snippets) {
                 snippetList.add(convertSnippetToMap(snippet));
             }
             return snippetList;
         }
         return Collections.emptyList();
     }
 
 
     /**
      * find snippet according to word
      *
      * @param keyword key word
      * @return snippet map list
      */
     public List<Map<String, String>> findSnippetsByWord(String keyword) {
         List<Map<String, String>> infoList = new ArrayList<Map<String, String>>();
         List<Snippet> snippetList = snippetManager.findSnippetsByWord(keyword);
         for (Snippet snippet : snippetList) {
             infoList.add(convertSnippetToMap(snippet));
         }
         return infoList;
     }
 
     /**
      * find snipet map by id
      *
      * @param id snippet id
      * @return snippet map
      */
     public Map<String, String> findMapById(Integer id) {
         Snippet snippet = snippetManager.findById(id);
         return snippet != null ? convertSnippetToMap(snippet) : null;
     }
 
     /**
      * update snippet
      *
      * @param info snippet info
      */
     public Integer updateSnippet(Map<String, String> info) {
         Snippet snippet;
         if (StringUtils.isEmpty(info.get("id")) || "null".equals(info.get("id"))) {
             snippet = snippetManager.construct();
             snippet.setType("0");
             snippet.setCreatedAt(new DateTime());
         } else {
             Integer id = Integer.valueOf(info.get("id"));
             snippet = snippetManager.findById(id);
         }
         snippet.setCategoryId(Integer.valueOf(info.get("categoryId")));
         snippet.setName(info.get("name"));
         snippet.setMnemonic(info.get("mnemonic"));
         snippet.setKeywords(info.get("keywords"));
         snippet.setAuthor(info.get("author"));
         if (StringUtils.isNotEmpty(info.get("language"))) {
             snippet.setLanguage(Integer.valueOf(info.get("language")));
         }
         snippet.setCode(info.get("code"));
         snippet.setDescription(info.get("description"));
         snippet.setModifiedAt(new DateTime());
         snippet.save();
         return snippet.getId();
     }
 
     /**
      * find root category
      *
      * @return category map list
      */
     public List<Map<String, String>> findAllCategories() {
         final List<Map<String, String>> infoList = new ArrayList<Map<String, String>>();
         String SQLSelect = "select id, name from snippet_category  where id > 1 order by name asc";
         jdbcTemplate.query(SQLSelect, new ParameterizedRowMapper<Object>() {
             public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                 Map<String, String> info = new HashMap<String, String>();
                 info.put("id", resultSet.getString("id"));
                 info.put("name", resultSet.getString("name"));
                 infoList.add(info);
                 return null;
             }
         });
         return infoList;
     }
 
     /**
      * find snippet in category
      *
      * @param categoryId category id
      * @return snippet list
      */
     public List<Map<String, String>> findSnippetsInCategory(Integer categoryId) {
         Category category = categoryManager.findById(categoryId);
         return convertSnippetToMap(category.findSnippets());
     }
 
     /**
      * add snippet
      *
      * @param info snippet info
      */
     public void addSnippetComment(Integer snippetId, Map<String, String> info) {
         Comment comment = new Comment();
         comment.setSubject(info.get("subject"));
         comment.setContent(info.get("content"));
         comment.setCommentator(info.get("commentator"));
         comment.setCreatedAt(new DateTime());
         Snippet snippet = snippetManager.findById(snippetId);
         snippet.addComment(comment);
     }
 
     /**
      * list snippet comment
      *
      * @param snippetId snippet id
      * @return comment list
      */
     public List<Map<String, String>> findSnippetComments(Integer snippetId) {
         Snippet snippet = snippetManager.findById(snippetId);
         List<Comment> comments = snippet.findComments();
         List<Map<String, String>> infoList = new ArrayList<Map<String, String>>();
         for (Comment comment : comments) {
             infoList.add(convertCommentToMap(comment));
         }
         return infoList;
     }
 
     /**
      * convert comment to map
      *
      * @param comment comment
      * @return map info
      */
     public Map<String, String> convertCommentToMap(Comment comment) {
         Map<String, String> info = new HashMap<String, String>();
         info.put("id", String.valueOf(comment.getId()));
         info.put("subject", comment.getSubject());
         info.put("content", comment.getContent());
         info.put("createdAt", comment.getCreatedAt().toString());
         info.put("commentator", comment.getCommentator());
         info.put("commentatorEmail", comment.getCommentatorEmail());
         return info;
     }
 
     /**
      * find recent add snippets
      *
      * @return find recent add snippets
      */
     public List<Map<String, String>> findRecentAddedSnippets() {
         return convertSnippetToMap(snippetManager.findRecentAddedSnippets(20));
     }
 
     /**
      * convert snippet list to map list
      *
      * @param snippets snippets
      * @return map list
      */
     private List<Map<String, String>> convertSnippetToMap(List<Snippet> snippets) {
         List<Map<String, String>> infoList = new ArrayList<Map<String, String>>();
         for (Snippet snippet : snippets) {
             infoList.add(convertSnippetToMap(snippet));
         }
         return infoList;
     }
 
     /**
      * convert snippet to map
      *
      * @param snippet snippet object
      * @return map object
      */
     private Map<String, String> convertSnippetToMap(Snippet snippet) {
         Map<String, String> info = new HashMap<String, String>();
         info.put("id", String.valueOf(snippet.getId()));
         info.put("categoryId", String.valueOf(snippet.getCategoryId()));
         info.put("name", snippet.getName());
         info.put("mnemonic", snippet.getMnemonic());
         info.put("language", String.valueOf(snippet.getLanguage()));
         info.put("author", snippet.getAuthor());
         info.put("code", snippet.getCode());
         info.put("keywords", snippet.getKeywords());
         info.put("description", snippet.getDescription());
         return info;
     }
 
 
 }
