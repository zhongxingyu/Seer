 package squared;
 
 import java.util.List;
 
 import squared.core.ClassReflection;
 import squared.utils.Tree;
 import squared.utils.TreeNode;
 import squared.utils.Utils;
 
 import com.db4o.Db4oEmbedded;
 import com.db4o.ObjectContainer;
 import com.db4o.ext.DatabaseFileLockedException;
 import com.db4o.ext.Db4oException;
 import com.db4o.reflect.ReflectClass;
 import com.db4o.reflect.ReflectField;
 
 public class DBConnection {
 	private static DBConnection instance = null; 
 	private static ObjectContainer db;
 	private static String containerPath;
 	private static Tree<ClassReflection> dbReflection;
 	
 	private DBConnection() {
 	}
 	
 	public static DBConnection getInstance() {
 		if (instance == null) {
 			instance = new DBConnection();
 		}
 		return instance;
 	}
 	
 	public void open(String path) throws DatabaseFileLockedException, Db4oException {
 		if (db != null)
 		{
 			close();
 		}
 		
 		containerPath = path;
 		System.out.println("[squared] OPEN object container: "+containerPath);
 		db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), path);
 		dbReflection = new Tree<ClassReflection>();
 		
 		try{
 			List<ReflectClass> storedClasses = Utils.getStoredClasses(db);
 			
 			// placeholder root
 			if (!storedClasses.isEmpty()) {
 				dbReflection.setRootElement(new TreeNode<ClassReflection>( new ClassReflection(storedClasses.get(0), "")) );
 			
 				for (ReflectClass cls : storedClasses)
 				{
 					traverse(new ClassReflection(cls, ""), dbReflection.getRootElement());
 				}
 			}
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	private void traverse(ClassReflection cls, TreeNode<ClassReflection> parentNode) {
		TreeNode node = new TreeNode<ClassReflection>(cls);
 		parentNode.addChild(node);
 		
 		if (cls.getType().isPrimitive() || Utils.isIgnored(cls.getType().getName()))
 		{
 		}
 		else
 		{
 			ReflectField[] fields = cls.getType().getDeclaredFields();
 			for (ReflectField field : fields) {
 				traverse(new ClassReflection(field.getFieldType(), field.getName()), node);
 			}
 			
 		}
 	}
 	
 	public void close() {
 		System.out.println("[squared] CLOSE object container: "+containerPath);
 		db.close();
 		
 		db = null;
 		dbReflection = null;
 		containerPath = null;
 	}
 	
 	public Tree<ClassReflection> getDBReflection()
 	{
 		return dbReflection;
 	}
 	
 	public boolean isOpened()
 	{
 		return db != null;
 	}
 
 }
