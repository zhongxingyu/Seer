 package org.jpos.jposext.isomsgaction.testing.service.support;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.betwixt.io.BeanWriter;
 import org.jpos.iso.ISOMsg;
 import org.jpos.iso.ISOUtil;
 import org.jpos.jposext.isomsgaction.model.SimpleContextWrapper;
 import org.jpos.jposext.isomsgaction.model.validation.ValidationError;
 import org.jpos.jposext.isomsgaction.model.validation.ValidationErrorTypeEnum;
 import org.jpos.jposext.isomsgaction.service.IISOMsgAction;
 import org.jpos.jposext.isomsgaction.service.support.ISOMsgActionCheckField;
 import org.jpos.jposext.isomsgaction.testing.model.ComparisonContext;
 import org.jpos.jposext.isomsgaction.testing.model.ISOCmpDiff;
 import org.jpos.jposext.isomsgaction.testing.model.ManualCheck;
 
 public class MappingTest extends TestCase {
 
 	class MyErreurValidation extends ValidationError {
 
 		private MyErreurValidation() {
 			super();
 		}
 
 		private MyErreurValidation(ValidationError validationError) {
 			super();
 			setTypeErreur(validationError.getTypeErreur());
 			setParamName(validationError.getParamName());
 		}
 
 		private MyErreurValidation(ValidationErrorTypeEnum typeErreur,
 				String paramName) {
 			super(typeErreur, paramName);
 		}
 
 		private MyErreurValidation(ValidationErrorTypeEnum typeErreur) {
 			super(typeErreur);
 		}
 
 		public int hashCode() {
 			// On choisit les deux nombres impairs
 			int result = 7;
 			final int multiplier = 17;
 
 			// Pour chaque attribut, on calcule le hashcode
 			// que l'on ajoute au r�sultat apr�s l'avoir multipli�
 			// par le nombre "multiplieur" :
 			result = multiplier * result + this.getParamName().hashCode();
 			result = multiplier * result + this.getTypeErreur().hashCode();
 
 			// On retourne le r�sultat :
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			final MyErreurValidation other = (MyErreurValidation) obj;
 
 			if (getParamName() == null) {
 				if (other.getParamName() != null) {
 					return false;
 				}
 			} else if (!(getParamName().equals(other.getParamName()))) {
 				return false;
 			}
 
 			if (getTypeErreur() == null) {
 				if (other.getTypeErreur() != null) {
 					return false;
 				}
 			} else if (!(getTypeErreur().equals(other.getTypeErreur()))) {
 				return false;
 			}
 
 			return true;
 		}
 
 	}
 
 	private List<ISOMsg> lstSrcISOMsg = new ArrayList<ISOMsg>();
 
 	private ISOMsg expectedISOMsg;
 
 	private Map<String, Object> context = new HashMap<String, Object>();
 
 	private Map<String, ManualCheck> mapManualChecks = new TreeMap<String, ManualCheck>();
 
 	private Map<String, List<ValidationErrorTypeEnum>> mapExpectedErrsByIdPath;
 
 	private IISOMsgAction action;
 
 	private boolean showManualCheckReminder;
 
 	private Map<String, Object> expectedContext = new HashMap<String, Object>();
 
 	private List<String> expectedContextBinaryAttrs = new ArrayList<String>();
 	
 	private List<String> expectedContextNullAttrs = new ArrayList<String>();	
 
 	@Override
 	protected void runTest() throws Throwable {
 		List<ISOCmpDiff> resList = new ArrayList<ISOCmpDiff>();
 
 		try {
 			ISOMsg targetMsg = new ISOMsg();
 
 			ISOMsg[] tabISOMsg = new ISOMsg[1 + lstSrcISOMsg.size()];
 
 			tabISOMsg[0] = targetMsg;
 
 			for (int i = 1; i <= lstSrcISOMsg.size(); i++) {
 				tabISOMsg[i] = lstSrcISOMsg.get(i - 1);
 				dumpMessage(System.out, tabISOMsg[i],
 						String.format("Source ISO Message [%d]", i));
 			}
 
 			if (null != expectedISOMsg) {
 				dumpMessage(System.out, expectedISOMsg,
 						"Expected ISO Message (excluding fields needing manual check)");
 			}
 
 			action.process(tabISOMsg, context);
 
 			if (null != targetMsg) {
 				dumpMessage(System.out, targetMsg, "Result ISO message");
 			}
 
 			ComparisonContext ctx = new ComparisonContext();
 			ctx.setResList(resList);
 			ctx.setMapManualChecks(mapManualChecks);
 
 			Map<MyErreurValidation, ValidationError> mapErrsInattendues = new HashMap<MyErreurValidation, ValidationError>();
 			Map<MyErreurValidation, ValidationError> mapErrsAttenduesTrouvees = new HashMap<MyErreurValidation, ValidationError>();
 			Map<MyErreurValidation, ValidationError> mapErrsAttenduesNonTrouvees = new HashMap<MyErreurValidation, ValidationError>();
 
 			List<ValidationError> lstErrsEnSortie = null;
 			if (context
 					.containsKey(ISOMsgActionCheckField.VALIDATION_ERRORS_LIST_ATTRNAME)) {
 				lstErrsEnSortie = (List<ValidationError>) context
 						.get(ISOMsgActionCheckField.VALIDATION_ERRORS_LIST_ATTRNAME);
 				if (lstErrsEnSortie.size() > 0) {
 					dumpErrorsList(
 							System.out,
 							(List<ValidationError>) context
 									.get(ISOMsgActionCheckField.VALIDATION_ERRORS_LIST_ATTRNAME));
 				}
 				for (ValidationError validationError : lstErrsEnSortie) {
 					if (mapExpectedErrsByIdPath.containsKey(validationError
 							.getParamName())) {
 						List<ValidationErrorTypeEnum> typesErreurAttendus = mapExpectedErrsByIdPath
 								.get(validationError.getParamName());
 						if (!typesErreurAttendus.contains(validationError
 								.getTypeErreur())) {
 							mapErrsInattendues.put(new MyErreurValidation(
 									validationError), validationError);
 						} else {
 							mapErrsAttenduesTrouvees.put(
 									new MyErreurValidation(validationError),
 									validationError);
 						}
 					} else {
 						mapErrsInattendues.put(new MyErreurValidation(
 								validationError), validationError);
 					}
 				}
 
 				for (Entry<String, List<ValidationErrorTypeEnum>> entry : mapExpectedErrsByIdPath
 						.entrySet()) {
 					String idPath = entry.getKey();
 					for (ValidationErrorTypeEnum validationErrorTypeEnum : entry
 							.getValue()) {
 						MyErreurValidation errValid = new MyErreurValidation(
 								validationErrorTypeEnum, idPath);
 						if (!(mapErrsAttenduesTrouvees.containsKey(errValid))) {
 							mapErrsAttenduesNonTrouvees.put(
 									new MyErreurValidation(errValid), errValid);
 						}
 					}
 				}
 
 				for (Entry<MyErreurValidation, ValidationError> entry : mapErrsAttenduesNonTrouvees
 						.entrySet()) {
 					MyErreurValidation erreurValidation = entry.getKey();
 					resList.add(new ISOCmpDiff(
 							erreurValidation.getParamName(),
 							String.format(
 									"Field %s : a validation error [%s] was expected",
 									erreurValidation.getParamName(),
 									erreurValidation.getTypeErreur().name())));
 				}
 
 				for (Entry<MyErreurValidation, ValidationError> entry : mapErrsInattendues
 						.entrySet()) {
 					MyErreurValidation erreurValidation = entry.getKey();
 					resList.add(new ISOCmpDiff(
 							erreurValidation.getParamName(),
 							String.format(
 									"Field %s : validation error [%s] was not expected",
 									erreurValidation.getParamName(),
 									erreurValidation.getTypeErreur().name())));
 				}
 			}
 
 			boolean expectedContextPopulationFailure = false;
 			if (null != expectedContext) {
 				for (Entry<String, Object> entry : expectedContext.entrySet()) {
 					String ctxPath = entry.getKey();
 					String expectedValue = (String) entry.getValue();
 
 					Object effectiveObject = PropertyUtils.getProperty(
 							new SimpleContextWrapper(context), ctxPath);
 
 					boolean nullValueExpected = expectedContextNullAttrs.contains(ctxPath);
 					if (nullValueExpected) {
 						if (null != effectiveObject) {
 							expectedContextPopulationFailure = true;
 							resList.add(new ISOCmpDiff(
 									ctxPath,
 									String.format(
 											"Attribute %s : null expected, but set=[%s]",
 											ctxPath, effectiveObject.toString())));
 						}
 					} else {
 						String effectiveValue = null;
						if (null == effectiveObject) {
							effectiveValue = null;
						} else if (effectiveObject.getClass().isPrimitive()) {
 							effectiveValue = (String) effectiveObject;
 						} else if (effectiveObject instanceof String) {
 							effectiveValue = (String) effectiveObject;
 						} else {
							effectiveValue = effectiveObject.toString();
 						}
 						if (!(expectedValue.equals(effectiveValue))) {
 							expectedContextPopulationFailure = true;
 							boolean dumpHex = expectedContextBinaryAttrs
 									.contains(ctxPath);
 							if (null != effectiveValue) {
 								resList.add(new ISOCmpDiff(
 										ctxPath,
 										String.format(
 												"Attribute %s : expected %s=[%s], was %s=[%s]",
 												ctxPath,
 												dumpHex ? "(hex dump)" : "",
 												dumpHex ? ISOUtil
 														.hexdump(expectedValue
 																.getBytes())
 														: expectedValue,
 												dumpHex ? "(hex dump)" : "",
 												dumpHex ? ISOUtil
 														.hexdump(effectiveValue
 																.getBytes())
 														: effectiveValue)));
 							} else {
 								resList.add(new ISOCmpDiff(
 										ctxPath,
 										String.format(
 												"Attribute %s : expected %s=[%s], but not set",
 												ctxPath,
 												dumpHex ? "(hex dump)" : "",
 												dumpHex ? ISOUtil
 														.hexdump(expectedValue
 																.getBytes())
 														: expectedValue)));
 							}
 						}
 					}
 				}
 			}
 
 			if (null != expectedISOMsg) {
 				ISOComponentComparator comparator = new ISOComponentComparator(
 						ctx, "", "");
 				if (comparator.compare(expectedISOMsg, targetMsg) != 0) {
 					throw new AssertionFailedError("Test failed");
 				}
 			}
 
 			if (mapErrsAttenduesNonTrouvees.size() + mapErrsInattendues.size() > 0) {
 				throw new AssertionFailedError("Test failed");
 			}
 
 			if (expectedContextPopulationFailure) {
 				throw new AssertionFailedError("Test failed");
 			}
 
 			if (mapManualChecks.size() > 0) {
 				if (showManualCheckReminder) {
 					ByteArrayOutputStream bos = new ByteArrayOutputStream();
 					PrintStream printStream = new PrintStream(bos);
 					printStream.println("Generated ISO message :");
 					targetMsg.dump(printStream, "");
 					printStream.println();
 					printStream.println("Resulting context :");
 					BeanWriter beanWriter = new BeanWriter(printStream);
 					beanWriter.enablePrettyPrint();
 					beanWriter.write(context);
 
 					final JTextArea textArea = new JTextArea();
 					textArea.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
 					textArea.setEditable(false);
 					textArea.setText(bos.toString());
 
 					JScrollPane scrollPane = new JScrollPane(textArea);
 					scrollPane.setPreferredSize(new Dimension(450, 350));
 
 					JPanel panel = new JPanel();
 					BorderLayout layout = new BorderLayout();
 					panel.setLayout(layout);
 					panel.add(scrollPane, BorderLayout.CENTER);
 					JPanel panLstVerif = new JPanel();
 					panLstVerif.setLayout(new BoxLayout(panLstVerif,
 							BoxLayout.Y_AXIS));
 					panLstVerif.add(new JLabel("Manual checks :"));
 					for (Entry<String, ManualCheck> entry : mapManualChecks
 							.entrySet()) {
 						panLstVerif.add(new JLabel(String.format("\t* %s : %s",
 								entry.getKey(), entry.getValue().getDesc())));
 					}
 					panLstVerif
 							.add(new JLabel(
 									"Do you confirm that checked values complies to their expected check rules ?"));
 
 					panel.add(panLstVerif, BorderLayout.SOUTH);
 
 					Object[] options = { "Yes", "No" };
 					int n = JOptionPane
 							.showOptionDialog(
 									null,
 									panel,
 									String.format("Manual checks (%s)",
 											this.getName()),
 									JOptionPane.YES_NO_OPTION,
 									JOptionPane.QUESTION_MESSAGE, null,
 									options, options[1]);
 					if (n == 1) {
 						throw new AssertionFailedError("Test failed");
 					}
 				}
 			}
 
 		}
 
 		catch (AssertionFailedError t) {
 			t.setStackTrace(new StackTraceElement[] {});
 
 			StringBuffer buf = new StringBuffer();
 			buf.append(t.getMessage());
 			buf.append("\n\n");
 
 			if (resList.size() > 0) {
 				buf.append("Differences list :");
 				buf.append("\n");
 				for (ISOCmpDiff resElt : resList) {
 					buf.append(String.format("\t* %s", resElt));
 					buf.append("\n");
 				}
 				buf.append("\n");
 			}
 
 			if (mapManualChecks.size() > 0) {
 				buf.append("Some of the manual checks have failed among :");
 				buf.append("\n");
 				for (Entry<String, ManualCheck> entry : mapManualChecks
 						.entrySet()) {
 					buf.append(String.format("\t* champ %s : %s",
 							entry.getKey(), entry.getValue().getDesc()));
 					buf.append("\n");
 				}
 				buf.append("\n");
 			}
 
 			throw new AssertionFailedError(buf.toString());
 
 		}
 	}
 
 	protected void dumpErrorsList(PrintStream ps, List<ValidationError> lstErrs) {
 		if (null != lstErrs) {
 			for (ValidationError err : lstErrs) {
 				ps.println(String.format("id=%s, error=%s", err.getParamName(),
 						err.getTypeErreur()));
 			}
 		}
 
 	}
 
 	protected String genIdPathFromTab(int[] tab, String sep) {
 		StringBuffer buf = new StringBuffer();
 		String sepInter = "";
 		for (int idx : tab) {
 			buf.append(sepInter);
 			buf.append(idx);
 			sepInter = sep;
 		}
 
 		return buf.toString();
 	}
 
 	private void dumpMessage(PrintStream ps, ISOMsg msg, String libelleMsg) {
 		String lineSep = "================================================================================";
 		ps.println(lineSep);
 		ps.println(libelleMsg);
 		ps.println(lineSep);
 		msg.dump(ps, "");
 		ps.println(lineSep);
 		ps.println("");
 	}
 
 	public void addSrcISOMsg(ISOMsg isoMsg) {
 		lstSrcISOMsg.add(isoMsg);
 	}
 
 	public void addManualCheck(String idPath, String desc) {
 		mapManualChecks.put(idPath, new ManualCheck(idPath, desc));
 	}
 
 	public List<ISOMsg> getLstSrcISOMsg() {
 		return lstSrcISOMsg;
 	}
 
 	public void setLstSrcISOMsg(List<ISOMsg> lstSrcISOMsg) {
 		this.lstSrcISOMsg = lstSrcISOMsg;
 	}
 
 	public Map<String, Object> getContext() {
 		return context;
 	}
 
 	public void setContext(Map<String, Object> context) {
 		this.context = context;
 	}
 
 	public ISOMsg getExpectedISOMsg() {
 		return expectedISOMsg;
 	}
 
 	public void setExpectedISOMsg(ISOMsg expectedISOMsg) {
 		this.expectedISOMsg = expectedISOMsg;
 	}
 
 	public IISOMsgAction getAction() {
 		return action;
 	}
 
 	public void setAction(IISOMsgAction action) {
 		this.action = action;
 	}
 
 	public Map<String, List<ValidationErrorTypeEnum>> getMapExpectedErrsByIdPath() {
 		return mapExpectedErrsByIdPath;
 	}
 
 	public void setMapExpectedErrsByIdPath(
 			Map<String, List<ValidationErrorTypeEnum>> mapExpectedErrsByIdPath) {
 		this.mapExpectedErrsByIdPath = mapExpectedErrsByIdPath;
 	}
 
 	public boolean isShowManualCheckReminder() {
 		return showManualCheckReminder;
 	}
 
 	public void setShowManualCheckReminder(boolean showManualCheckReminder) {
 		this.showManualCheckReminder = showManualCheckReminder;
 	}
 
 	public Map<String, Object> getExpectedContext() {
 		return expectedContext;
 	}
 
 	public void setExpectedContext(Map<String, Object> expectedContext) {
 		this.expectedContext = expectedContext;
 	}
 
 	public List<String> getExpectedContextBinaryAttrs() {
 		return expectedContextBinaryAttrs;
 	}
 
 	public void setExpectedContextBinaryAttrs(
 			List<String> expectedContextBinaryAttrs) {
 		this.expectedContextBinaryAttrs = expectedContextBinaryAttrs;
 	}
 
 	public List<String> getExpectedContextNullAttrs() {
 		return expectedContextNullAttrs;
 	}
 
 	public void setExpectedContextNullAttrs(
 			List<String> expectedContextNullAttrs) {
 		this.expectedContextNullAttrs = expectedContextNullAttrs;
 	}	
 	
 }
