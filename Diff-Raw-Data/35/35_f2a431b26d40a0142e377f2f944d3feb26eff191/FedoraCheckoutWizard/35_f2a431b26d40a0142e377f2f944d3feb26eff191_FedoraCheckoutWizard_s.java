 package org.fedoraproject.eclipse.packager.cvs;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.ssl.Certificates;
 import org.apache.commons.ssl.KeyMaterial;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.team.internal.ccvs.core.CVSException;
 import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
 import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
 import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
 import org.eclipse.team.internal.ccvs.ui.wizards.CheckoutWizard;
 import org.eclipse.ui.IImportWizard;
 
 @SuppressWarnings("restriction")
 public class FedoraCheckoutWizard extends CheckoutWizard implements
 		IImportWizard {
 
 	private List<ICVSRepositoryLocation> others;
 
 	public FedoraCheckoutWizard() {
 		super();
 		others = new ArrayList<ICVSRepositoryLocation>();
 	}
 
 	@Override
 	public void addPages() {
 		others = Arrays.asList(KnownRepositories.getInstance()
 				.getRepositories());
 		for (ICVSRepositoryLocation other : others) {
 			KnownRepositories.getInstance().disposeRepository(other);
 		}
 
 		// get Fedora CVS username from cert
		String file = System.getProperty("user.home") + Path.SEPARATOR
				+ ".fedora.cert";
 		try {
			KeyMaterial kmat = new KeyMaterial(new File(file), new File(file),
					new char[0]);

			// create location string and add to known repositories
			String location = ":ext;username=" + getCN(kmat)
					+ ";hostname=cvs.fedoraproject.org:/cvs/pkgs";
 
 			if (!KnownRepositories.getInstance().isKnownRepository(location)) {
 				ICVSRepositoryLocation repository = CVSRepositoryLocation
 						.fromString(location);
 				KnownRepositories.getInstance().addRepository(repository, true);
 			}
 
 			// invoke the CVS checkout wizard
 			super.addPages();
 
 		} catch (CVSException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (GeneralSecurityException e1) {
 			e1.printStackTrace();
			MessageDialog.openError(getShell(), "Fedora CVS problem",
					"Couldn't retrieve your SSL certificate (~/.fedora.cert)");
 		} catch (IOException e1) {
 			e1.printStackTrace();
			MessageDialog.openError(getShell(), "Fedora CVS problem",
					"Couldn't retrieve your SSL certificate (~/.fedora.cert)");
 		}
 	}
 
 	@Override
 	public void dispose() {
 		// add others back
 		for (ICVSRepositoryLocation other : others) {
 			KnownRepositories.getInstance().addRepository(other, true);
 		}
 		others = new ArrayList<ICVSRepositoryLocation>();
 		super.dispose();
 	}
 
 	private String getCN(KeyMaterial kmat) {
 		List<?> chains = kmat.getAssociatedCertificateChains();
 		Iterator<?> it = chains.iterator();
 		ArrayList<String> cns = new ArrayList<String>();
 		while (it.hasNext()) {
 			X509Certificate[] certs = (X509Certificate[]) it.next();
 			if (certs != null) {
 				for (int i = 0; i < certs.length; i++) {
 					cns.add(Certificates.getCN(certs[i]));
 				}
 			}
 		}
 		return cns.get(0);
 	}
 
 }
