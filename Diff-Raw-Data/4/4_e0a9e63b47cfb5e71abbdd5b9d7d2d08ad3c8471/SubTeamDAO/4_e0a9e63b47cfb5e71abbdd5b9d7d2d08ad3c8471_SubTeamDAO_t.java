 package svm.persistence.hibernate.dao;
 
 import svm.persistence.abstraction.dao.ISubTeamDAO;
 import svm.persistence.abstraction.exceptions.NoSessionFoundException;
 import svm.persistence.abstraction.exceptions.NotSupportedException;
 import svm.persistence.abstraction.model.ISubTeamEntity;
 import svm.persistence.hibernate.model.SubTeamEntity;
 
 /**
  * Projectteam: Team C
  * Date: 25.10.12
  */
 public class SubTeamDAO extends AbstractDAO<ISubTeamEntity> implements ISubTeamDAO {
 
     public SubTeamDAO() {
        super(SubTeamEntity.class);
     }
 
     @Override
     public ISubTeamEntity generateObject(Integer sessionId) throws InstantiationException, IllegalAccessException, NotSupportedException, NoSessionFoundException {
         return generateObject();
     }
 
     @Override
     public ISubTeamEntity generateObject() throws InstantiationException, IllegalAccessException, NoSessionFoundException, NotSupportedException {
         return new SubTeamEntity();
     }
 }
