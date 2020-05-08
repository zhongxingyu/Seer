 package org.zend.php.zendserver.deployment.core.internal.validation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.zend.php.zendserver.deployment.core.DeploymentCore;
 import org.zend.php.zendserver.deployment.core.descriptor.DeploymentDescriptorPackage;
 import org.zend.php.zendserver.deployment.core.descriptor.IModelContainer;
 import org.zend.php.zendserver.deployment.core.descriptor.IModelObject;
 import org.zend.php.zendserver.deployment.core.internal.descriptor.Feature;
 
 public class DescriptorSemanticValidator {
 
 	private Map<Feature, PropertyTester[]> testers;
 	private IDocument document;
 	private IFile file;
 	
 	public DescriptorSemanticValidator() {
 		testers = new HashMap<Feature, PropertyTester[]>();
 		
 		PropertyTester tester = new FieldNotEmptyTester(this);
 		add(DeploymentDescriptorPackage.PKG_NAME, tester);
 		add(DeploymentDescriptorPackage.VERSION_RELEASE, tester);
 		
 		add(DeploymentDescriptorPackage.ID, tester);
 		add(DeploymentDescriptorPackage.DISPLAY, tester);
 		add(DeploymentDescriptorPackage.TYPE, tester);
 		
 		add(DeploymentDescriptorPackage.DEPENDENCY_NAME, tester);
 		
 		add(DeploymentDescriptorPackage.VAR_NAME, tester);
 		add(DeploymentDescriptorPackage.VALUE, tester);
 		
 		tester = new FileExistsTester(this, ValidationStatus.WARNING);
 		add(DeploymentDescriptorPackage.EULA, tester);
 		add(DeploymentDescriptorPackage.ICON, tester);
 		// TODO consider mapping
 		
 		tester = new VersionTester(this);
 		add(DeploymentDescriptorPackage.VERSION_API, tester);
 		add(DeploymentDescriptorPackage.VERSION_RELEASE, tester);
 	}
 	
 	protected void add(Feature feature, PropertyTester tester) {
 		PropertyTester[] now = testers.get(feature);
 		if (now == null) {
 			testers.put(feature, new PropertyTester[] {tester});
 		} else {
 			PropertyTester[] dest = new PropertyTester[now.length + 1];
 			System.arraycopy(now, 0, dest,0, now.length);
 			dest[now.length] = tester;
 			testers.put(feature, dest);
 		}
 	}
 	
 	private void validate(int objId, int objNo, IModelObject modelObj, List<ValidationStatus> statuses) {
 		validateProperties(objId, objNo, modelObj, statuses);
 		if (modelObj instanceof IModelContainer) {
 			validate((IModelContainer) modelObj, statuses);
 		}
 	}
 	
 	private void validate(IModelContainer obj, List<ValidationStatus> statuses) {
 		for (Feature f : obj.getChildNames()) {
 			PropertyTester[] featureTests = testers.get(f);
 			if (featureTests != null) {
 				List<Object> children = obj.getChildren(f);
 				for (PropertyTester pt : featureTests) {
 					String msg = pt.test(f, children, obj);
 					if (msg != null) {
 						int offset = obj.getOffset(f);
 						
 						int line = 0;
 						try {
 							line = document.getLineOfOffset(offset) + 1;
 						} catch (BadLocationException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						statuses.add(new ValidationStatus(-1, -1, f.id, line, offset, offset, pt.severity, msg));
 					}
 				}
 			}
 			
 			if (f.type == IModelObject.class) {
 				List<Object> children = obj.getChildren(f);
 				for (int i = 0; i < children.size(); i++) {
 					validate(f.id, i, (IModelObject) children.get(i), statuses);
 				}
 			}
 		}
 	}
 	
 	private void validateProperties(int objId, int objNo, IModelObject obj, List<ValidationStatus> statuses) {
 		for (Feature f : obj.getPropertyNames()) {
 			PropertyTester[] featureTests = testers.get(f);
 			if (featureTests != null) {
 				Object value = obj.get(f);
 				for (PropertyTester pt: featureTests) {
 					String msg = pt.test(f, value, obj);
 					if (msg != null) {
 						int offset = obj.getOffset(f);
 						
 						int line = 0;
 						try {
 							line = document.getLineOfOffset(offset) + 1;
 						} catch (BadLocationException e) {
 							DeploymentCore.log(e);
 						}
 						
 						statuses.add(new ValidationStatus(objId, objNo, f.id, line, offset, offset, pt.severity, msg));
 					}
 				}
 			}
 		}
 	}
 	
 	public ValidationStatus[] validate(IModelObject descr, IDocument document) {
 		this.document = document;
 		List<ValidationStatus> statuses = new ArrayList<ValidationStatus>();
 		validate(-1, -1, descr, statuses);
 		return statuses.toArray(new ValidationStatus[statuses.size()]);
 	}
 
 	public void setFile(IFile file) {
 		this.file = file;
 	}
 	
 	public IFile getFile() {
 		return file;
 	}
 	
 }
