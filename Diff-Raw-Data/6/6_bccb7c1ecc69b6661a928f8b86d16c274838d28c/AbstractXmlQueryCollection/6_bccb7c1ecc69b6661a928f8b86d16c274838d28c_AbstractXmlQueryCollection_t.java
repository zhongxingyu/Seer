 package cx.ath.jbzdak.sqlbuilder.xml;
 
 import cx.ath.jbzdak.sqlbuilder.*;
 import cx.ath.jbzdak.sqlbuilder.dialect.config.DialectConfig;
 import cx.ath.jbzdak.sqlbuilder.expressionConfig.ExpressionConfig;
 import cx.ath.jbzdak.sqlbuilder.expressionConfig.ExpressionConfigKey;
 import cx.ath.jbzdak.sqlbuilder.xml.query.AbstractQuery;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.*;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * Created by: Jacek Bzdak
  */
 @XmlRootElement
 @XmlType(propOrder = {"xmlDialectConfig", "xmlDefaultExpressionConfig"})
 public class AbstractXmlQueryCollection implements QueryCollection {
 
    protected String xmlDialect;
 
    @XmlTransient
    protected Dialect dialect;
 
    protected XmlDialectConfig xmlDialectConfig;
 
    @XmlTransient
    private DialectConfig dialectConfig;
    protected XmlExpressionConfig xmlDefaultExpressionConfig;
 
    protected ExpressionConfig defaultExpressionConfig;
    protected List<AbstractQuery> queryTags = new ArrayList<AbstractQuery>();
    protected Map<String, QueryTag> queries;
 
    public void parsingFinished(){
       XmlParsingContext.remove();
    }
 
    public void prepare(){
    }
 
    @XmlAttribute(required = true, name = "dialect")
    public String getXmlDialect() {
       return xmlDialect;
    }
 
    public void setXmlDialect(String xmlDialect) {
       this.xmlDialect = xmlDialect;
    }
 
    @XmlElement(required = false, name = "dialectConfig")
    public XmlDialectConfig getXmlDialectConfig() {
       return xmlDialectConfig;
    }
 
    public void setXmlDialectConfig(XmlDialectConfig xmlDialectConfig) {
       this.xmlDialectConfig = xmlDialectConfig;
    }
 
    @XmlTransient
    public Map<String, QueryTag> getQueries() {
       if (queries == null) {
          queries = new HashMap<String, QueryTag>();
          for (QueryTag queryTag : queryTags) {
             queries.put(queryTag.getName(), queryTag);
          }
       }
       return queries;
    }
 
    public void setQueries(Map<String, QueryTag> queries) {
       this.queries = queries;
    }
 
    public BasicSQLFactory getQuery(String name){
      QueryTag queryTag = getQueries().get(name);
      if (queryTag == null){
         throw new NoSuchElementException("Can't find query '" + name + "'");
      }
      return queryTag.createQuery();
    }
 
    public Dialect getDialect() {
       if (dialect == null) {
          ConfigurableDialectHolder configurableDialectHolder = new ConfigurableDialectHolder();
          dialect = configurableDialectHolder.getDialect(xmlDialect, dialectConfig);
       }
       return dialect;
    }
 
    @XmlTransient
    public ExpressionConfig getDefaultExpressionConfig() {
       if (defaultExpressionConfig == null) {
          if(xmlDefaultExpressionConfig!=null){
             defaultExpressionConfig = xmlDefaultExpressionConfig.createConfig();
          }else{
             defaultExpressionConfig = new ExpressionConfig();
          }
          defaultExpressionConfig.put(ExpressionConfigKey.DIALECT, getDialect());
       }
       return defaultExpressionConfig;
    }
 
    @XmlTransient
    public DialectConfig getDialectConfig() {
       if (dialectConfig == null) {
          if(xmlDialectConfig==null){
             dialectConfig = new DialectConfig();
          }else{
             dialectConfig = xmlDialectConfig.createDialectConfig();
          }
       }
       return dialectConfig;
    }
 
    @XmlElement(nillable = true, required = false, name = "defaultExpressionConfig")
    public XmlExpressionConfig getXmlDefaultExpressionConfig() {
       return xmlDefaultExpressionConfig;
    }
 
    public void setXmlDefaultExpressionConfig(XmlExpressionConfig xmlDefaultExpressionConfig) {
       this.xmlDefaultExpressionConfig = xmlDefaultExpressionConfig;
    }
 
    public Set<String> getQueryNames() {
       return getQueries().keySet();
    }
 
    public void setJdbcUrl(String jdbc) {
       //To change body of implemented methods use File | Settings | File Templates.
    }
 
    public void setCredentials(String username, String password) {
       //To change body of implemented methods use File | Settings | File Templates.
    }
 }
