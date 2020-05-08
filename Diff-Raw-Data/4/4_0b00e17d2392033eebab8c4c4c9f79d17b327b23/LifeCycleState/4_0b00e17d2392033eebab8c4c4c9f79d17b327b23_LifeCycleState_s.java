 package server.lifecycle;
 
 import org.dom4j.Element;
 import org.hibernate.annotations.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.User;
 import server.audit.AuditService;
 import server.audit.LogEvent;
 import server.dao.DAOFactory;
 import server.data.ObjectSystemData;
 import server.exceptions.CinnamonException;
 import server.global.Constants;
 import server.i18n.LocalMessage;
 import server.interfaces.Repository;
 import utils.ParamParser;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 /**
  *
  */
 @Entity
 @Table(name = "lifecycle_states",
         uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})}
 )
 public class LifeCycleState implements Serializable {
 
     private static final long serialVersionUID = 1L;
     private transient Logger log = LoggerFactory.getLogger(this.getClass());
 
     static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
 
     @Id
     @GeneratedValue
     @Column(name = "id")
     private long id;
 
     @Column(name = "name",
             length = Constants.NAME_LENGTH,
             nullable = false)
     private String name;
 
     @Column(name = "state_class",
             length = Constants.NAME_LENGTH,
             nullable = false)
     private Class<? extends IState> stateClass;
 
     @ManyToOne
     @JoinColumn(name = "lifecycle_id",
             nullable = true
     )
     private LifeCycle lifeCycle;
 
     @ManyToOne
     @JoinColumn(name = "lifecycle_state_id",
             nullable = true
     )
     private LifeCycleState lifeCycleStateForCopy;
 
     @Column(name = "config",
             length = Constants.METADATA_SIZE,
             nullable = false)
     @Type(type = "text")
     String config = "<config />";
 
     @Version
     @Column(name = "obj_version")
     @SuppressWarnings("unused")
     private Long obj_version = 0L;
 
     public LifeCycleState() {
 
     }
 
     public LifeCycleState(String name, Class<? extends IState> stateClass, String config, LifeCycle lifeCycle) {
         this.name = name;
         this.stateClass = stateClass;
         setConfig(config);
         this.lifeCycle = lifeCycle;
     }
 
 
     public LifeCycleState(String name, Class<? extends IState> stateClass, String config, LifeCycle lifeCycle, LifeCycleState lifeCycleStateForCopy) {
         this.name = name;
         this.stateClass = stateClass;
         setConfig(config);
         this.lifeCycle = lifeCycle;
         this.lifeCycleStateForCopy = lifeCycleStateForCopy;
     }
 
     /**
      * Convert the LifeCycleState to a dom4j-XML structure and append it to the given root element.
      * Example (with a root element called "root"):<br>
      * <pre>
      * {@code
      *  <root>
      *    <lifecycleState>
      *      <id>543</id>
      *      <name>TestState</name>
      *      <sysName>example.test.state</sysName>
      *      <stateClass>server.lifecycle.state.NopState</stateClass>
      *      <parameter>&lt;config /&gt;</parameter> (encoded XML string)
      *      <lifeCycle>44</lifeCycle> (may be empty)
      *      <lifeCycleStateForCopy>7</lifeCycleStateForCopy> (may be empty)
      *    </lifecycleState>
      *  </root>
      * }
      * </pre><br>
      * Note: the {@code <name>}-element contains the localized version of the LifeCycleState's name.
      *
      * @param root the root element for the lifeCycleState
      */
     public void toXmlElement(Element root) {
         Element lcs = root.addElement("lifecycleState");
         lcs.addElement("id").addText(String.valueOf(id));
         lcs.addElement("name").addText(LocalMessage.loc(name));
         lcs.addElement("sysName").addText(name);
         lcs.addElement("stateClass").addText(stateClass.getName());
         lcs.addElement("parameter").addText(config);
         if (lifeCycle != null) {
             lcs.addElement("lifeCycle").addText(String.valueOf(lifeCycle.getId()));
         }
         else {
             lcs.addElement("lifeCycle");
         }
         if (lifeCycleStateForCopy == null) {
             lcs.addElement("lifeCycleStateForCopy");
         }
         else {
             lcs.addElement("lifeCycleStateForCopy", String.valueOf(lifeCycleStateForCopy.getId()));
         }
     }
 
     /**
      * Check if the given OSD may enter into this lifecycle state.
      *
      * @param osd the osd to check
      * @return true if the osd may enter into this state, false otherwise.
      */
     public Boolean openForEntry(ObjectSystemData osd) {
         try {
             IState newState = stateClass.newInstance();
             return newState.checkEnteringObject(osd, config);
         }
         catch (Exception ex) {
             throw new CinnamonException(ex);
         }
     }
 
     /**
      * Change the current lifecycle state of the given OSD to nextState, if
      * possible and allowed.
      *
      * @param osd       the osd to change.
      * @param nextState the new lifecycle state
      */
     public void enterState(ObjectSystemData osd, LifeCycleState nextState, Repository repository, User user) {
         IState newState;
         try {
             newState = nextState.getStateClass().newInstance();
         }
         catch (InstantiationException e) {
             throw new CinnamonException("error.instantiating.class", e, nextState.getClass().getName());
         }
         catch (IllegalAccessException e) {
             throw new CinnamonException("error.accessing.class", e, nextState.getClass().getName());
         }
 
         log.debug("entering state of lifecycle-Class " + nextState.getName());
 
         LifeCycleState oldState = osd.getState();
        if (newState.checkEnteringObject(osd, config)) {
            newState.enter(osd, config);
             osd.setState(nextState);
             AuditService auditService = new AuditService(repository.getAuditConnection());           
             auditService.insertLogEvent(auditService.createLogEvent(osd, user, oldState, nextState));
         }
         else {
             throw new CinnamonException("error.enter.lifecycle");
         }
 
     }
 
     /**
      * The parameter osd leaves this lifecycle state, either for a new or a
      * null state.
      *
      * @param osd       the osd to change.
      * @param nextState the next lifecycle state. May be null if the OSD is being
      *                  detached from a lifecycle.
      */
     public void exitState(ObjectSystemData osd, LifeCycleState nextState, Repository repository, User user) {
         if (nextState == null) {
             AuditService auditService = new AuditService(repository.getAuditConnection());
             auditService.insertLogEvent(auditService.createLogEvent(osd, user, osd.getState(), null));
             return;
         }
 
         IState state;
         try {
             state = stateClass.newInstance();
         }
         catch (InstantiationException e) {
             throw new CinnamonException("error.instantiating.class", e, stateClass.getName());
         }
         catch (IllegalAccessException e) {
             throw new CinnamonException("error.accessing.class", e, stateClass.getName());
         }
 
         Class<? extends IState> nextStateClass = nextState.getStateClass();
         try {
             IState nextStateInstance = nextStateClass.newInstance();
             state.exit(osd, nextStateInstance, config);
         }
         catch (InstantiationException e) {
             throw new CinnamonException("error.instantiating.class", e, nextStateClass.getName());
         }
         catch (IllegalAccessException e) {
             throw new CinnamonException("error.accessing.class", e, nextStateClass.getName());
         }
     }
 
     /**
      * If an object is copied, the copy may enter a different Lifecycle than the
      * original object. The LifeCycleCopyPolicy is defined in the associated LifeCycleState.
      *
      * @return the Lifecycle for the copy (may be null)
      */
     public LifeCycleState getLifeCycleStateForCopy() {
         return lifeCycleStateForCopy;
     }
 
     public void setLifeCycleStateForCopy(LifeCycleState lifeCycleStateForCopy) {
         this.lifeCycleStateForCopy = lifeCycleStateForCopy;
     }
 
     public long getId() {
         return id;
     }
 
     private void setId(long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Class<? extends IState> getStateClass() {
         return stateClass;
     }
 
     public void setStateClass(Class<? extends IState> stateClass) {
         this.stateClass = stateClass;
     }
 
     public LifeCycle getLifeCycle() {
         return lifeCycle;
     }
 
     public void setLifeCycle(LifeCycle lifeCycle) {
         this.lifeCycle = lifeCycle;
     }
 
     public String getConfig() {
         return config;
     }
 
     /**
      * Set parameter for stateClass to the given value. If this method's parameter is null, set the field
      * to "{@code <config />}".
      *
      * @param config the new value for the parameter which will be given to stateClass when needed.
      */
     public void setConfig(String config) {
         if (config == null || config.trim().length() == 0) {
             this.config = "<config />";
         }
         else {
             ParamParser.parseXmlToDocument(config, "error.param.config");
             this.config = config;
         }
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof LifeCycleState)) return false;
 
         LifeCycleState that = (LifeCycleState) o;
 
         if (lifeCycle != null ? !lifeCycle.equals(that.lifeCycle) : that.lifeCycle != null) return false;
 
         /*
          * Check for lifeCycleStateForCopy is a little bit more elaborate because
          * we cannot use lcsfc.equals(that.lcsfc) - this can lead to infinite recursion.
          */
         if (lifeCycleStateForCopy == null && that.lifeCycleStateForCopy == null) {
             // null.equals.null == true
         }
         else if (lifeCycleStateForCopy == null) {
             // that.lifeCycleStateForCopy is != null
             return false;
         }
         else if (that.lifeCycleStateForCopy == null) {
             // this.lifeCycleStateForCopy is != null
             return false;
         }
         else if (lifeCycleStateForCopy.getId() != that.getLifeCycleStateForCopy().getId()) {
             // none are null, so check value of long.
             return false;
         }
 
         if (name != null ? !name.equals(that.name) : that.name != null) return false;
         if (config != null ? !config.equals(that.config) : that.config != null) return false;
         if (stateClass != null ? !stateClass.equals(that.stateClass) : that.stateClass != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return name != null ? name.hashCode() : 0;
     }
 }
