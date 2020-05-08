 package io.trygvis.esper.testing.core.badge;
 
 import com.jolbox.bonecp.*;
 import fj.*;
 import fj.data.*;
 import io.trygvis.esper.testing.*;
 import io.trygvis.esper.testing.core.*;
 import io.trygvis.esper.testing.core.db.*;
 import io.trygvis.esper.testing.util.sql.*;
 import org.codehaus.jackson.map.*;
 import org.slf4j.*;
 
 import java.io.*;
 import java.sql.*;
 import java.util.List;
 import java.util.*;
 
 import static io.trygvis.esper.testing.Config.*;
 import static io.trygvis.esper.testing.core.db.PersonBadgeDto.Type.*;
 
 public class UnbreakablePoller implements TablePoller.NewRowCallback<BuildDto> {
     Logger logger = LoggerFactory.getLogger(getClass());
 
     private static final ObjectMapper objectMapper = new ObjectMapper();
 
     public static void main(String[] args) throws Exception {
         String pollerName = "unbreakable";
         String tableName = "build";
         String columnNames = BuildDao.BUILD;
         SqlF<ResultSet, BuildDto> f = BuildDao.build;
         TablePoller.NewRowCallback<BuildDto> callback = new UnbreakablePoller();
 
         Config config = loadFromDisk();
 
         BoneCPDataSource dataSource = config.createBoneCp();
 
         new TablePoller<>(pollerName, tableName, columnNames, Option.<String>none(), f, callback).
                 testMode(true).
                 work(dataSource);
     }
 
     public void process(Connection c, BuildDto build) throws SQLException {
         Daos daos = new Daos(c);
 
         List<UUID> persons = daos.buildDao.selectPersonsFromBuildParticipant(build.uuid);
         logger.info("Processing build={}, #persons={}", build.uuid, persons.size());
 
         for (UUID person : persons) {
             logger.info("person={}", person);
 
             SqlOption<PersonBadgeProgressDto> o = daos.personDao.selectBadgeProgress(person, UNBREAKABLE);
 
             UnbreakableBadgeProgress badge;
 
             if (o.isNone()) {
                 badge = UnbreakableBadgeProgress.initial(person);
                 logger.info("New badge progress");
                 String state = serialize(badge);
                 daos.personDao.insertBadgeProgress(person, UNBREAKABLE, state);
                 continue;
             }
 
             String state = o.get().state;
             try {
                 badge = objectMapper.readValue(state, UnbreakableBadgeProgress.class);
             } catch (IOException e) {
                 logger.error("Could not de-serialize badge state: {}", state);
                 throw new RuntimeException(e);
             }
 
            logger.info("Existing badge progress: count={}", badge.count);
 
             P2<UnbreakableBadgeProgress, Option<UnbreakableBadge>> p = badge.onBuild(build);
 
             badge = p._1();
 
            logger.info("New badge progress: count={}", badge.count);
 
             if (p._2().isSome()) {
                 UnbreakableBadge b = p._2().some();
 
                 logger.info("New unbreakable badge: person={}, level={}", person, b.level);
 
                 SqlOption<PersonBadgeDto> option = daos.personDao.selectBadge(person, UNBREAKABLE, b.level);
 
                 if (option.isNone()) {
                     daos.personDao.insertBadge(person, UNBREAKABLE, b.level, 1);
                 } else {
                     daos.personDao.incrementBadgeCount(person, UNBREAKABLE, b.level);
                 }
             }
 
             state = serialize(badge);
 
             daos.personDao.updateBadgeProgress(person, UNBREAKABLE, state);
         }
     }
 
     private String serialize(UnbreakableBadgeProgress badge) {
         try {
             CharArrayWriter writer = new CharArrayWriter();
             objectMapper.writeValue(writer, badge);
             return writer.toString();
         } catch (IOException e) {
             logger.error("Could not serialize badge.", e);
             throw new RuntimeException(e);
         }
     }
 }
