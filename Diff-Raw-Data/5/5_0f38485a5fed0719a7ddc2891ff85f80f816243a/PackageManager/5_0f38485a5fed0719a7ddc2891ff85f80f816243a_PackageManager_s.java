 package management;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Observable;
 
 public class PackageManager extends Observable{
 
 	private static PackageManager me;
 	private Map<String,Package> pkgs;
 	
 	private PackageManager() {
 		super();
 		pkgs = new HashMap<String, Package>();
 		me = this;
 	}
 	
 	public static PackageManager getInstance(){
 		if(me==null){
 			me = new PackageManager();
 		}
 		return me;
 	}
 	
 	public Map<String, Package> getPkgs() {
 		return pkgs;
 	}
 
 	public Package create(Package pkg){
 		Client clnt;
 		Package retval = pkgs.get(pkg.getId());
 		if(retval==null){
			pkgs.put(pkg.getId(), pkg);
			retval = pkg;
 			clnt = ClientManager.getInstance().getClient(pkg.getClnt());
 			if(clnt==null){
 				return null;
 			}
 			clnt.addPkg(pkg);
 			setChanged();
 			notifyObservers();
 			retval=pkg;
 		}
 		return retval;
 	}
 	
 	public Package getPackage(String Id){
 		return pkgs.get(Id);
 	}
 	
 }
