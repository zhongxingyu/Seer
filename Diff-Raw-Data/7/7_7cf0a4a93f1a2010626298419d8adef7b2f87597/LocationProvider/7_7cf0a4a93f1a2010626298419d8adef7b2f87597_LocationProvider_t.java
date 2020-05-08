 package location;
 
import edu.tum.lua.ast.SyntaxNode;
 
 public interface LocationProvider {

	public Location getLocation(SyntaxNode symbol);
 
 }
