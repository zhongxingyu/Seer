 
 package pt.uac.cafeteria.model.persistence;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import pt.uac.cafeteria.model.domain.Student;
 
 
 public class StudentMapper extends DatabaseMapper<Student> {
 
     public static final String TABLE = "students";
     public static final String COLUMNS = " id, name, phone, email, scholarship ";
 
     @Override
     protected String findStatement() {
         return "SELECT * FROM " + TABLE + " WHERE id = ?";
     }
 
     public Student find(int id) {
         return find(new Integer(id));
     }
 
     @Override
     protected Student doLoad(Integer id, ResultSet rs) throws SQLException {
         String name = rs.getString("name");
         int phone = Integer.valueOf(rs.getString("phone"));
         String email = rs.getString("email");
         boolean scholarship = rs.getBoolean("scholarship");
         return Student.build(name, null, phone, email, scholarship, "");
     }
 
     @Override
     protected String insertStatement() {
         return "INSERT INTO " + TABLE + " VALUES (?, ?, ?, ?, ?)";
     }
 
     @Override
     protected void doInsert(Student student, PreparedStatement stmt) throws SQLException {
         stmt.setInt(1, student.getId());
         stmt.setString(2, student.getName());
         stmt.setString(3, String.valueOf(student.getPhone()));
         stmt.setString(4, student.getEmail());
         stmt.setBoolean(5, student.hasScholarship());
     }
 
     @Override
     public boolean delete(Student o) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
    @Override
    public int update(Student o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
 }
