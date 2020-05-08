 package fedora.common.policy;
 
 import java.util.Vector;
 
 import fedora.common.Constants;
 
 public class Release2_1Namespace extends XacmlNamespace {
 
     private Release2_1Namespace(XacmlNamespace parent, String localName) {
     	super(parent, localName);
     }
 
 	public static Release2_1Namespace onlyInstance = new Release2_1Namespace(FedoraAsProjectNamespace.getInstance(), "2.1");
 	static {
 		onlyInstance.addNamespace(SubjectNamespace.getInstance()); 
 		onlyInstance.addNamespace(ActionNamespace.getInstance()); 
 		onlyInstance.addNamespace(ResourceNamespace.getInstance()); 
 		onlyInstance.addNamespace(EnvironmentNamespace.getInstance()); 
 	}
 	
 	public static final Release2_1Namespace getInstance() {
 		return onlyInstance;
 	}
 	
 	public static final void main (String[] args) {
 		Release2_1Namespace instance = Release2_1Namespace.getInstance();
 		Vector list = new Vector();
 		instance.flatRep(list);
 		for (int i=0; i<list.size(); i++) {
			if (! ((String)list.get(i)).startsWith(Constants.ACTION.CONTEXT_ID.uri)) {
				System.out.println(list.get(i));
 			}
 		} 
 	}
 
 }
