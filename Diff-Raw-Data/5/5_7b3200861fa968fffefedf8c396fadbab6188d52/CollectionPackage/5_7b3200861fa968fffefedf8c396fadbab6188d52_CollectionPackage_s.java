 package pl.edu.mimuw.loxim.protocol.packages_data;
 
 import pl.edu.mimuw.loxim.protocol.packages.PackagesFactory;
 import pl.edu.mimuw.loxim.protogen.lang.java.template.exception.ProtocolException;
 import pl.edu.mimuw.loxim.protogen.lang.java.template.ptools.Package;
 import pl.edu.mimuw.loxim.protogen.lang.java.template.streams.PackageInputStreamReader;
 import pl.edu.mimuw.loxim.protogen.lang.java.template.streams.PackageOutputStreamWriter;
 
 public abstract class CollectionPackage extends Package {
 
 	/**
 	 **/
 	private Long count;
 	/**
 	 **/
 	private Long globalType;
 	/**
 	 **/
 	private Package[] dataParts;
 
 	public CollectionPackage() {
 	}
 
 	public CollectionPackage(Long a_count, Long a_globalType, Package[] a_dataParts) {
 		count = a_count;
 		globalType = a_globalType;
 		dataParts = a_dataParts;
 
 	}
 
 	@Override
 	protected void deserializeW(PackageInputStreamReader reader) throws ProtocolException {
 		super.deserializeW(reader);
 
 		try {
 			count = reader.readVaruint();
 			globalType = reader.readVaruint();
 			dataParts = new Package[count.intValue()];
 			for (int i = 0; i < count; i++) {
 				if (globalType == null) {
					dataParts[i] = PackagesFactory.getInstance().createPackage(reader.readVaruint());
 				} else {
					dataParts[i] = PackagesFactory.getInstance().createPackage(globalType);
 				}
 				dataParts[i].deserializeContent(reader);
 			}
 		} catch (Exception e) {
 			throw new ProtocolException(e);
 		}
 	}
 
 	@Override
 	protected void serializeW(PackageOutputStreamWriter writer) throws ProtocolException {
 		super.serializeW(writer);
 		try {
 			recalculateGlobalType();
 			writer.writeVaruint(count);
 			writer.writeVaruint(globalType);
 			for (int i = 0; i < count; i++) {
 				if (globalType == null)
 					writer.writeVaruint(dataParts[i].getPackageType());
 				dataParts[i].serializeContent(writer);
 			}
 		} catch (Exception e) {
 			throw new ProtocolException(e);
 		}
 	}
 
 	private void recalculateGlobalType() {
 		globalType = null;
 		for (int i = 0; i < count; i++) {
 			if (globalType == null) {
 				globalType = dataParts[i].getPackageType();
 			} else {
 				if (globalType != dataParts[i].getPackageType()) {
 					globalType = null;
 					return;
 				}
 			}
 
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!super.equals(obj))
 			return false;
 		CollectionPackage p = (CollectionPackage) obj;
 		if (!(((count == p.getCount()) || ((count != null) && (p.getCount() != null) && ((count).equals(p.getCount()))))))
 			return false;
 		if (!(((globalType == p.getGlobalType()) || ((globalType != null) && (p.getGlobalType() != null) && ((globalType).equals(p.getGlobalType()))))))
 			return false;
 
 		if (dataParts == p.getDataParts())
 			return true;
 		if ((dataParts == null) || (p.getDataParts() == null))
 			return false;
 		for (int i = 0; i < count; i++) {
 			if (dataParts[i] == p.getDataParts()[i])
 				continue;
 			if ((dataParts[i] == null) || (p.getDataParts()[i] == null))
 				return false;
 			if (!dataParts[i].equals(p.getDataParts()[i]))
 				return false;
 		}
 		return true;
 	}
 
 	// ================= GETTERS AND SETTERS ===================
 
 	public Long getCount() {
 		return count;
 	};
 
 	public void setCount(Long a_count) {
 		a_count = count;
 	};
 
 	public Long getGlobalType() {
 		return globalType;
 	};
 
 	public void setGlobalType(Long a_globalType) {
 		a_globalType = globalType;
 	};
 
 	public Package[] getDataParts() {
 		return dataParts;
 	};
 
 	public void setDataParts(Package[] a_dataParts) {
 		a_dataParts = dataParts;
 	};
 
 }
