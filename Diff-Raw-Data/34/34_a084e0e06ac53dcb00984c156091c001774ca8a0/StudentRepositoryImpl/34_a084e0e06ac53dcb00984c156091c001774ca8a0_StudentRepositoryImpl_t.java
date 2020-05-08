 package no.niths.infrastructure;
 
 import java.util.List;
 
 import no.niths.domain.Student;
 import no.niths.infrastructure.interfaces.StudentRepository;
 
 import org.hibernate.Criteria;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class StudentRepositoryImpl extends GenericRepositoryImpl<Student>
 		implements StudentRepository {
 
 	public StudentRepositoryImpl() {
 		super(Student.class);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Student> getStudentsWithNamedCourse(String name) {
 		String sql = "from " + Student.class.getSimpleName()
 				+ " s join fetch s.courses c where c.name=:name";
 		return getSession().getCurrentSession().createQuery(sql).setString("name", name)
 				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
 	}
 	
 	public Student getStudentByEmail(String email){
 		String sql = "from " + Student.class.getSimpleName()
 				+ " s where s.email=:email";
 		return (Student) getSession().getCurrentSession()
 				.createQuery(sql)
 				.setString("email", email)
 				.uniqueResult();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Student> getAllMentors() {
 		String sql = "from " + Student.class.getSimpleName()
				+ " s join fetch s.orientationGroup";
 		return getSession().getCurrentSession().createQuery(sql)
 				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Student> getMentorsByGroupe(int groupId) {
 		String sql = "from " + Student.class.getSimpleName()
				+ " s join fetch s.orientationGroup m where m.groupId=:gid";
 		return getSession().getCurrentSession().createQuery(sql).setInteger("gid", groupId)
 				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
 	}
 }
