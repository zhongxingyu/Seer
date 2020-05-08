 package oreservlet.sesame;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.log4j.Logger;
 import org.openrdf.repository.RepositoryConnection;
 
 import au.edu.diasb.annotation.danno.impl.sesame.SesameAnnoteaTypeFactory;
 import au.edu.diasb.annotation.danno.impl.sesame.SesameRDFContainer;
 import au.edu.diasb.annotation.danno.model.RDFContainer;
 import au.edu.diasb.annotation.danno.model.RDFParserException;
 import au.edu.diasb.annotation.danno.model.RDFStatementFilter;
 import oreservlet.exceptions.ORETypeException;
 import oreservlet.model.CompoundObject;
 import oreservlet.model.TypeFactoryBase;
 import oreservlet.common.ORETypeFactory;
 
 public class SesameORETypeFactory extends SesameAnnoteaTypeFactory implements
 		ORETypeFactory {
 
 	public SesameORETypeFactory() {
//		super(Logger.getLogger(SesameORETypeFactory.class));
 	}
 	
 	public CompoundObject createCompoundObject(InputStream in) {
//		RepositoryConnection tconn = loadRDF(in);
//        RDFContainer tmp = new SesameRDFContainer(tconn, this);
 		return null;
 	}
 
 	public SesameRDFContainer createContainer() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public RDFContainer createContainer(InputStream in)
 			throws RDFParserException, IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public RDFContainer filterContainer(RDFContainer container,
 			RDFStatementFilter filter) throws ORETypeException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
