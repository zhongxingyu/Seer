 package ai.ilikeplaces.logic.crud.unit;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.doc.WARNING;
 import ai.ilikeplaces.entities.HumansWall;
 import ai.ilikeplaces.entities.Msg;
 import ai.ilikeplaces.entities.Wall;
 import ai.ilikeplaces.exception.DBDishonourCheckedException;
 import ai.ilikeplaces.exception.DBDishonourException;
 import ai.ilikeplaces.exception.DBFetchDataException;
 import ai.ilikeplaces.jpa.CrudServiceLocal;
 import ai.ilikeplaces.jpa.QueryParameter;
 import ai.ilikeplaces.util.AbstractSLBCallbacks;
 import ai.ilikeplaces.util.jpa.RefreshException;
 import ai.ilikeplaces.util.jpa.RefreshSpec;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import java.util.List;
 
 /**
  * @author Ravindranath Akila
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @Stateless
 public class CRUDHumansWall extends AbstractSLBCallbacks implements CRUDHumansWallLocal {
 
     @EJB
     private CrudServiceLocal<HumansWall> humansWallCrudServiceLocal_;
 
     @EJB
     private CrudServiceLocal<Wall> wallCrudServiceLocal_;
 
     @EJB
     private CrudServiceLocal<Msg> crudServiceMsg_;
 
     public CRUDHumansWall() {
     }
 
     final static Logger logger = LoggerFactory.getLogger(CRUDHumansWall.class);
 
     private static final String IS_NULL_OR_EMPTY = " IS NULL OR EMPTY!";
 
 
     @Override
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
     public HumansWall doDirtyRHumansWall(final String humanId, final RefreshSpec wallRefreshSpec) throws DBFetchDataException {
         final HumansWall humansWall = doRHumansWall(humanId);
         try {
             humansWall.getWall().refresh(wallRefreshSpec);
         } catch (RefreshException e) {
             throw new DBFetchDataException(e);
         }
         return humansWall;
     }
 
     @Override
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
     public HumansWall doRHumansWall(String humanId) {
         final HumansWall humansWall = humansWallCrudServiceLocal_.find(HumansWall.class, humanId);
 
         if (humansWall.getWall().getWallMetadata() == null) {
             RecoveringFromAbsentMetadata:
             {
                 final String key = Wall.WallMetadataKey.HUMAN.toString();
                 final String value = "" + humansWall.getHumanId();
 
                 this.doUpdateMetadata(humansWall.getWall().getWallId(), key, value);
             }
         }
         return humansWall;
     }
 
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public HumansWall doRHumansWallRefreshed(final String humanId) {
         final HumansWall humansWall = doRHumansWall(humanId);
 
         if (humansWall.getWall().getWallMetadata() == null) {
             RecoveringFromAbsentMetadata:
             {
                 final String key = Wall.WallMetadataKey.HUMAN.toString();
                 final String value = "" + humansWall.getHumanId();
 
                 this.doUpdateMetadata(humansWall.getWall().getWallId(), key, value);
             }
         }
 
         humansWall.getWall().getWallMsgs().size();
         humansWall.getWall().getWallMutes().size();
         return humansWall;
     }
 
     @Override
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
     public List<Msg> doRHumansWallLastEntries(final String humanId, final Integer numberOfEntriesToFetch) {
         final HumansWall humansWall = doRHumansWall(humanId);
         final Long humansWallId = humansWall.getWall().getWallId();
 
         final List<Msg> lastWallEntryInList = crudServiceMsg_.findWithNamedQuery(Msg.FindWallEntriesByWallIdOrderByIdDesc,
                 QueryParameter.newInstance().add(Wall.wallIdCOL, humansWallId).parameters(), numberOfEntriesToFetch);
 
         return lastWallEntryInList;
     }
 
     @Override
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
     public Long doDirtyRHumansWallID(final String humanId) throws DBDishonourCheckedException {
         return humansWallCrudServiceLocal_.findBadly(HumansWall.class, humanId).getWall().getWallId();
     }
 
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     @WARNING("Reflect any changes on CRUDWall as well")
     public void doUpdateMetadata(final long wallId, final String key, final String value) {
         if (key == null || key.isEmpty()) {
             throw new NullPointerException(key + IS_NULL_OR_EMPTY);
         }
 
         if (value == null || value.isEmpty()) {
             throw new NullPointerException(value + IS_NULL_OR_EMPTY);
         }
 
         final Wall wall = wallCrudServiceLocal_.findBadly(Wall.class, wallId);
 
         final String wallMetadata = wall.getWallMetadata();
         if (wallMetadata == null || wallMetadata.isEmpty()) {
             wall.setWallMetadata(key + "=" + value);
         } else {
             final String[] pairs = wallMetadata.split(",");
             boolean present = false;
             boolean same = false;
             for (final String pairString : pairs) {
                 final String[] pair = pairString.split("=");
                 if (key.equals(pair[0])) {
                     present = true;
                     LetsWarnAnபInconsistencies:
                     {
                         if (!value.equals(pair[1])) {
                             throw new DBDishonourException("You tried to assign " + key + "=" + value + " to " + wall.toString() + " but it already contains a DIFFERENT VALUE for they key");
                         }
                     }
                 }
             }
             if (!present) {
                 wall.setWallMetadata(wall.getWallMetadata() + "," + key + "=" + value);
             }
         }
     }
 
 
    /**
     * Waring: Returns an unmanaged instance
     * @param humanId
     * @param msg
     * @return
     */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @WARNING("Returns an unmanaged instance w.r.t. Hazelcast")
     public Msg doUHumansWallMsgs(final String humanId, final Msg msg) {
         final Msg managedMsg = crudServiceMsg_.create(msg);
 
         final HumansWall humansWall = this.doRHumansWall(humanId);
        humansWall.getWall().getWallMsgs().add(msg);
 
         wallCrudServiceLocal_.update(humansWall.getWall());
 
         return managedMsg;
     }
 
 }
