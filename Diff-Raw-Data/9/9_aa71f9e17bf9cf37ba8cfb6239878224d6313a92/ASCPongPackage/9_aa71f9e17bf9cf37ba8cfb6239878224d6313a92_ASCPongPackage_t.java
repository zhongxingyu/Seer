 package pl.edu.mimuw.loxim.protogen.lang.java.template.base_packages;
 
 import pl.edu.mimuw.loxim.protogen.lang.java.template.exception.ProtocolException;
 import pl.edu.mimuw.loxim.protogen.lang.java.template.ptools.Package;
 import pl.edu.mimuw.loxim.protogen.lang.java.template.streams.PackageInputStreamReader;
 import pl.edu.mimuw.loxim.protogen.lang.java.template.streams.PackageOutputStreamWriter;
 
 public class ASCPongPackage extends Package {
	public final static short ID=(short)129;
 
 	@Override
 	public long getPackageType() {
 		return ID;
 	}
 	
 	@Override
 	protected void deserializeW(PackageInputStreamReader reader) throws ProtocolException {
 	}
 	
 	@Override
 	protected void serializeW(PackageOutputStreamWriter writer) throws ProtocolException {
 	}
 }
