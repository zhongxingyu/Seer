 package Infrastructure.Repository;
 
 import Domain.Data.IDatabaseFactory;
 import Domain.Repository.IBaseRepository;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 import org.hibernate.*;
 
 public abstract class BaseRepository<TEntity> 
         implements IBaseRepository<TEntity> {
 
     protected Session session;
     private Class<TEntity> classePersistente;
 
     public BaseRepository(IDatabaseFactory databaseFactory) {
         this.classePersistente = (Class<TEntity>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
         this.session = databaseFactory.getSession();
     }
 
     @Override
     public TEntity obterPorId(int id) {
         return (TEntity) session.get(classePersistente, id);
     }
 
     @Override
     public List<TEntity> obterTodos() {
         return session.createQuery(obterConsultaTodos()).list();
     }
 
     @Override
     public TEntity salvar(TEntity entidade) {
        Transaction trans = session.beginTransaction();
         session.saveOrUpdate(entidade);
        trans.commit(); //UNDONE: criar unit of work?
         return entidade;
     }
 
     @Override
     public void deletar(TEntity entidade) {
         session.delete(entidade);
     }
     
     private String obterNomeTabela(){
         return String.format("%ss", classePersistente.getSimpleName());
     }
     
     private String obterConsultaTodos(){
         return String.format("from %s", obterNomeTabela());
     }
 }
