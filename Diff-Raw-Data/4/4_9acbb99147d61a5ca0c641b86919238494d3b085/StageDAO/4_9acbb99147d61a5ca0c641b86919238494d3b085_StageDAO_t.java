 package fr.emn.ose.stage;
 
 import com.github.jmkgreen.morphia.Morphia;
 import com.github.jmkgreen.morphia.dao.BasicDAO;
 import com.github.jmkgreen.morphia.query.*;
 import com.mongodb.Mongo;
 import fr.emn.ose.queries.*;
 import fr.emn.ose.queries.QueryException;
 import org.bson.types.ObjectId;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Raoul
  * Date: 23/04/13
  * Time: 15:05
  * To change this template use File | Settings | File Templates.
  */
 public class StageDAO extends BasicDAO<Stage, ObjectId> {
 
     public StageDAO(Morphia morphia, Mongo mongo) {
         super(mongo, morphia, ConnectionDataStore.dbName);
     }
 
     public List<Stage> find(Stage stage, SearchParameters parameters) throws QueryException {
         List<Stage> stages;
         Query<Stage> query = getDatastore().createQuery(Stage.class);
 
         if (parameters.getOr().size() != 0) {
             List<Criteria> criterias = new ArrayList<Criteria>();
            for(String champ : parameters.getOr()){
               criterias.add(queryChamp(champ, stage, query));
            }
            Criteria[] criteriaArray = Arrays.copyOf(criterias.toArray(),criterias.size(), Criteria[].class);
            query.or(criteriaArray);
         }
 
         if(parameters.getAnd().size() !=0){
             List<Criteria> criterias = new ArrayList<Criteria>();
             for(String champ : parameters.getAnd()){
                 criterias.add(queryChamp(champ, stage, query));
             }
 
             query.and((Criteria[]) criterias.toArray());
 
         }
 
         return query.asList();
 
     }
 
     private Criteria queryChamp(String champ, Stage stage, Query<Stage> query) throws QueryException {
 
         if (champ.equals(Models.PAYS.toString())) {
             return (new PaysQuery(champ, query, stage)).getCriteria();
 
         } else {
             if (champ.equals(Models.ADRESSE.toString())) {
                 return (new AdresseQuery(champ, query, stage)).getCriteria();
             } else {
                 if (champ.equals(Models.DOMAINE.toString())) {
                     return (new DomaineQuery(champ, query, stage)).getCriteria();
                 } else {
                     if (champ.equals(Models.INTITULE.toString())) {
                         return (new IntituleQuery(champ, query, stage)).getCriteria();
                     } else {
                         if (champ.equals(Models.DESCRIPTION.toString())) {
                             return (new DescriptionQuery(champ, query, stage)).getCriteria();
                         } else {
                             if (champ.equals(Models.SALAIRE.toString())) {
                                 return (new SalaireQuery(champ, query, stage)).getCriteria();
                             } else {
                                 if (champ.equals(Models.OPTION.toString())) {
                                     return (new OptionQuery(champ, query, stage)).getCriteria();
                                 } else {
                                     if (champ.equals(Models.AVANTAGES.toString())) {
                                         return (new AvantageQuery(champ, query, stage)).getCriteria();
                                     } else {
                                         if (champ.equals(Models.LANGUE.toString())) {
                                             return new LangueQuery(champ, query, stage).getCriteria();
                                         } else {
                                             throw new QueryException(champ);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
 
     }
 
 
 }
