 package net.ivoa.gui.dynamicLabel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import net.ivoa.parameter.model.AbstractCriterion;
 import net.ivoa.parameter.model.AlwaysConditionalStatement;
 import net.ivoa.parameter.model.AtomicParameterExpression;
 import net.ivoa.parameter.model.BelongToSet;
 import net.ivoa.parameter.model.DefaultValue;
 import net.ivoa.parameter.model.Expression;
 import net.ivoa.parameter.model.IfThenConditionalStatement;
 import net.ivoa.parameter.model.IsNull;
 import net.ivoa.parameter.model.ParameterType;
 import net.ivoa.parameter.model.SingleParameter;
 import net.ivoa.pdl.interpreter.conditionalStatement.StatementHelperContainer;
 import net.ivoa.pdl.interpreter.expression.ExpressionParserFactory;
 import net.ivoa.pdl.interpreter.groupInterpreter.GroupHandlerHelper;
 import net.ivoa.pdl.interpreter.utilities.Utilities;
 import CommonsObjects.GeneralParameter;
 
 public class PDLParamPanelFactory {
 	private static final PDLParamPanelFactory instance = new PDLParamPanelFactory();
 
 	private PDLParamPanelFactory() {
 		super();
 	}
 
 	public static PDLParamPanelFactory getInstance() {
 		return instance;
 	}
 
 	public PDLBaseParamPanel paramBuilder(GroupHandlerHelper ghh,
 			SingleParameter parameter) {
		List<StatementHelperContainer> helperList = ghh
				.getStatementHelperList();
 
		if (null == helperList) {
			return buildBasicPanel(parameter);
		} else {
			for (StatementHelperContainer localHelper : helperList) {
				PDLBaseParamPanel panel = test(localHelper, parameter);
				if (null != panel) {
					return panel;
				}
 			}
			return buildBasicPanel(parameter);
 		}

 	}

	private PDLBaseParamPanel buildBasicPanel(SingleParameter parameter) {
 		if (parameter.getParameterType() == ParameterType.BOOLEAN) {
 			return new PDLBooleanParamPanel(parameter);
 		} else {
 			return new PDLTextParamPanel(parameter);
 		}
 	}

 	private PDLBaseParamPanel test(StatementHelperContainer statementHelp,
 			SingleParameter parameter) {
 
 		AbstractCriterion criterion;
 		// if the statement is switched and if the parameter is involved into
 		// the statement
 		if (statementHelp.isStatementSwitched()
 				&& null != (criterion = this.getCriterionWhereParamIsInvolved(
 						parameter,
 						this.getCriterionFromStatement(statementHelp)))) {
 			// If the condition is a Default Value
 			if (criterion.getConditionType().getClass() == DefaultValue.class) {
 				DefaultValue df = (DefaultValue) criterion.getConditionType();
 
 				try {
 					// put into the mapper the default value
 					List<GeneralParameter> existingValues = Utilities
 							.getInstance().getMapper()
 							.getuserProvidedValuesForParameter(parameter);
 
 					if (null == existingValues || existingValues.size() < 1) {
 						Utilities
 								.getInstance()
 								.getMapper()
 								.getMap()
 								.put(parameter.getName(),
 										ExpressionParserFactory.getInstance()
 												.buildParser(df.getValue())
 												.parse());
 					}
 
 				} catch (Exception e) {
 					e.printStackTrace();
 					// If the default value could not be evaluated, we do
 					// nothing and just return the basic text panel
 				}
 				return buildBasicPanel(parameter);
 			}
 
 			// if the condition is a Belong To Set
 			if (criterion.getConditionType().getClass() == BelongToSet.class) {
 				BelongToSet bts = (BelongToSet) criterion.getConditionType();
 
 				List<List<GeneralParameter>> setOfValues = new ArrayList<List<GeneralParameter>>();
 
 				try {
 					for (Expression exp : bts.getValue()) {
 						setOfValues.add(ExpressionParserFactory.getInstance()
 								.buildParser(exp).parse());
 					}
 					return new PDLChoseBoxParamPanel(parameter, setOfValues);
 
 				} catch (Exception e) {
 					e.printStackTrace();
 					// If the default value could not be evaluated, we do
 					// nothing and just return the basic text panel
 				}
 			}
 
 			if (criterion.getConditionType().getClass() == IsNull.class) {
 				PDLTextParamPanel panel = new PDLTextParamPanel(parameter);
 				panel.getComponent().setEnabled(false);
 				panel.setVisible(false);
 
 				return panel;
 			}
 
 		}
		return null;
 	}
 
 	private AbstractCriterion getCriterionFromStatement(
 			StatementHelperContainer statementHelp) {
 		if (statementHelp.getStatement().getClass() == AlwaysConditionalStatement.class) {
 			AlwaysConditionalStatement t = (AlwaysConditionalStatement) statementHelp
 					.getStatement();
 			return t.getAlways().getCriterion();
 		}
 		if (statementHelp.getStatement().getClass() == IfThenConditionalStatement.class) {
 			IfThenConditionalStatement t = (IfThenConditionalStatement) statementHelp
 					.getStatement();
 			return t.getThen().getCriterion();
 		}
 		return null;
 	}
 
 	private AbstractCriterion getCriterionWhereParamIsInvolved(
 			SingleParameter parameter, AbstractCriterion criterion) {
 		if (null == criterion)
 			return null;
 
 		AbstractCriterion toReturn = null;
 
 		if (criterion.getExpression() instanceof AtomicParameterExpression) {
 			AtomicParameterExpression tempExp = (AtomicParameterExpression) criterion
 					.getExpression();
 			if (Utilities.getInstance()
 					.getParameterFromReference(tempExp.getParameterRef())
 					.getName().equalsIgnoreCase(parameter.getName())) {
 				return criterion;
 			} else if (null != criterion.getLogicalConnector()) {
 				return getCriterionWhereParamIsInvolved(parameter, criterion
 						.getLogicalConnector().getCriterion());
 			}
 
 		} else {
 			if (null != criterion.getLogicalConnector()) {
 				return getCriterionWhereParamIsInvolved(parameter, criterion
 						.getLogicalConnector().getCriterion());
 			}
 		}
 		return toReturn;
 	}
 
 }
