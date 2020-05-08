 package eu.stratosphere.sopremo.base;
 
 import static eu.stratosphere.sopremo.type.JsonUtil.createPath;
 
import org.junit.Ignore;
 import org.junit.Test;
 
 import eu.stratosphere.sopremo.expressions.ArrayCreation;
 import eu.stratosphere.sopremo.expressions.BinaryBooleanExpression;
 import eu.stratosphere.sopremo.expressions.ComparativeExpression;
 import eu.stratosphere.sopremo.expressions.ComparativeExpression.BinaryOperator;
 import eu.stratosphere.sopremo.expressions.ElementInSetExpression;
 import eu.stratosphere.sopremo.expressions.ElementInSetExpression.Quantor;
 import eu.stratosphere.sopremo.expressions.EvaluationExpression;
 import eu.stratosphere.sopremo.expressions.InputSelection;
 import eu.stratosphere.sopremo.expressions.ObjectCreation;
 import eu.stratosphere.sopremo.testing.SopremoOperatorTestBase;
 import eu.stratosphere.sopremo.testing.SopremoTestPlan;
 import eu.stratosphere.sopremo.type.JsonUtil;
 
 public class TwoSourceJoinTest extends SopremoOperatorTestBase<TwoSourceJoin> {
 	@Test
 	public void shouldPerformAntiJoin2() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		// here we set outer join flag
 		final EvaluationExpression leftTwoSourceJoinKey = new ArrayCreation(JsonUtil.createPath("0", "v3", "worksFor"));
 		final EvaluationExpression rightTwoSourceJoinKey =
 			new ArrayCreation(JsonUtil.createPath("1", "v3", "worksFor"));
 		final BinaryBooleanExpression condition = new ElementInSetExpression(leftTwoSourceJoinKey,
 			Quantor.EXISTS_NOT_IN, rightTwoSourceJoinKey);
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition);
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(0).
 			addObject("v3", JsonUtil.createObjectNode("id", null, "worksFor", "CompanyABC")).
 			addObject("v3", JsonUtil.createObjectNode("id", null, "worksFor", "CompanyUVW")).
 			addObject("v3", JsonUtil.createObjectNode("id", null, "worksFor", "CompanyXYZ"));
 		sopremoPlan.getInput(1).
 			addObject("v3", JsonUtil.createObjectNode("id", null, "worksFor", "CompanyUVW")).
 			addObject("v3", JsonUtil.createObjectNode("id", null, "worksFor", "CompanyXYZ"));
 		sopremoPlan.getExpectedOutput(0).
 			addObject("v3", JsonUtil.createObjectNode("id", null, "worksFor", "CompanyABC"));
 
 		sopremoPlan.trace();
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformAntiTwoSourceJoin() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		final BinaryBooleanExpression condition = new ElementInSetExpression(
 			createPath("0", "DeptName"), Quantor.EXISTS_NOT_IN, createPath("1", "Name"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition);
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(0).
 			addObject("Name", "Harry", "EmpId", 3415, "DeptName", "Finance").
 			addObject("Name", "Sally", "EmpId", 2241, "DeptName", "Sales").
 			addObject("Name", "George", "EmpId", 3401, "DeptName", "Finance").
 			addObject("Name", "Harriet", "EmpId", 2202, "DeptName", "Production");
 		sopremoPlan.getInput(1).
 			addObject("Name", "Sales", "Manager", "Harriet").
 			addObject("Name", "Production", "Manager", "Charles");
 		sopremoPlan.getExpectedOutput(0).
 			addObject("Name", "Harry", "EmpId", 3415, "DeptName", "Finance").
 			addObject("Name", "George", "EmpId", 3401, "DeptName", "Finance");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformAntiTwoSourceJoinWithReversedInputs() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		final BinaryBooleanExpression condition = new ElementInSetExpression(
 			createPath("1", "DeptName"), Quantor.EXISTS_NOT_IN, createPath("0", "Name"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition);
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(1).
 			addObject("Name", "Harry", "EmpId", 3415, "DeptName", "Finance").
 			addObject("Name", "Sally", "EmpId", 2241, "DeptName", "Sales").
 			addObject("Name", "George", "EmpId", 3401, "DeptName", "Finance").
 			addObject("Name", "Harriet", "EmpId", 2202, "DeptName", "Production");
 		sopremoPlan.getInput(0).
 			addObject("Name", "Sales", "Manager", "Harriet").
 			addObject("Name", "Production", "Manager", "Charles");
 		sopremoPlan.getExpectedOutput(0).
 			addObject("Name", "Harry", "EmpId", 3415, "DeptName", "Finance").
 			addObject("Name", "George", "EmpId", 3401, "DeptName", "Finance");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformEquiTwoSourceJoin() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		final BinaryBooleanExpression condition = new ComparativeExpression(createPath("0", "id"),
 			BinaryOperator.EQUAL, createPath("1", "userid"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition);
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(1).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan.
 			getExpectedOutput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url",
 				"java.sun.com/javase/6/docs/api/").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 2, "url", "www.cnn.com");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformEquiTwoSourceJoinWithReversedInputs() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		final BinaryBooleanExpression condition = new ComparativeExpression(createPath("1", "id"),
 			BinaryOperator.EQUAL, createPath("0", "userid"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition);
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(1).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(0).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan
 			.getExpectedOutput(0)
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url", "code.google.com/p/jaql/")
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url",
 				"java.sun.com/javase/6/docs/api/").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 2, "url", "www.cnn.com");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformFullOuterTwoSourceJoin() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		// here we set outer join flag
 		final EvaluationExpression leftTwoSourceJoinKey = createPath("0", "id");
 		final EvaluationExpression rightTwoSourceJoinKey = createPath("1", "userid");
 		final BinaryBooleanExpression condition = new ComparativeExpression(leftTwoSourceJoinKey,
 			BinaryOperator.EQUAL, rightTwoSourceJoinKey);
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition).
 			withOuterJoinSources(new ArrayCreation(new InputSelection(0), new InputSelection(1)));
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(1).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 4, "url", "www.nbc.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan.getExpectedOutput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url", "code.google.com/p/jaql/")
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url",
 				"java.sun.com/javase/6/docs/api/").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 2, "url", "www.cnn.com").
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3).
 			addObject("userid", 4, "url", "www.nbc.com");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformFullOuterTwoSourceJoin2() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		// here we set outer join flag
 		final EvaluationExpression leftTwoSourceJoinKey = createPath("0", "biographyId");
 		final EvaluationExpression rightTwoSourceJoinKey = createPath("1", "biography");
 		final BinaryBooleanExpression condition = new ComparativeExpression(leftTwoSourceJoinKey,
 			BinaryOperator.EQUAL, rightTwoSourceJoinKey);
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition).
 			withOuterJoinSources(new ArrayCreation(new InputSelection(0), new InputSelection(1))).
 			withResultProjection(new ObjectCreation(
 				new ObjectCreation.CopyFields(new InputSelection(1)),
 				new ObjectCreation.FieldAssignment("worksFor", JsonUtil.createPath("0", "worksFor"))));
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(0).
 			addObject("biographyId", "A000029", "worksFor", "CompanyXYZ").
 			addObject("biographyId", "A000059", "worksFor", "CompanyUVW").
 			addObject("biographyId", "A000049", "worksFor", "CompanyABC");
 		sopremoPlan.getInput(1).
 			addObject("biography", "A000029", "id", "usCongress1", "income", 1, "name", "Andrew Adams").
 			addObject("biography", "A000039", "id", "usCongress2", "income", 1, "name", "John Adams").
 			addObject("biography", "A000059", "id", "usCongress3", "income", 1, "name", "John Doe");
 		sopremoPlan.getExpectedOutput(0).
 			addObject("biography", "A000029", "id", "usCongress1", "income", 1, "name", "Andrew Adams", "worksFor",
 				"CompanyXYZ").
 			addObject("biography", "A000039", "id", "usCongress2", "income", 1, "name", "John Adams").
 			addObject("worksFor", "CompanyABC").
 			addObject("biography", "A000059", "id", "usCongress3", "income", 1, "name", "John Doe", "worksFor", "CompanyUVW");
 
 		sopremoPlan.trace();
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformFullOuterTwoSourceJoinWithReversedInputs() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		// here we set outer join flag
 		final EvaluationExpression leftTwoSourceJoinKey = createPath("1", "id");
 		final EvaluationExpression rightTwoSourceJoinKey = createPath("0", "userid");
 		final BinaryBooleanExpression condition = new ComparativeExpression(leftTwoSourceJoinKey,
 			BinaryOperator.EQUAL, rightTwoSourceJoinKey);
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition).
 			withOuterJoinSources(new ArrayCreation(new InputSelection(0), new InputSelection(1)));
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(1).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(0).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 4, "url", "www.nbc.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan
 			.getExpectedOutput(0)
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url", "code.google.com/p/jaql/")
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url",
 				"java.sun.com/javase/6/docs/api/").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 2, "url", "www.cnn.com").
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3).
 			addObject("userid", 4, "url", "www.nbc.com");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformLeftOuterTwoSourceJoin() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		// here we set outer join flag
 		final EvaluationExpression leftTwoSourceJoinKey = createPath("0", "id");
 		final BinaryBooleanExpression condition = new ComparativeExpression(leftTwoSourceJoinKey,
 			BinaryOperator.EQUAL, createPath("1", "userid"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition).
 			withOuterJoinSources(new InputSelection(0));
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(1).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan
 			.getExpectedOutput(0)
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url", "code.google.com/p/jaql/")
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url",
 				"java.sun.com/javase/6/docs/api/").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 2, "url", "www.cnn.com").
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformLeftOuterTwoSourceJoinWithReversedInputs() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		// here we set outer join flag
 		final EvaluationExpression leftTwoSourceJoinKey = createPath("1", "id");
 		final BinaryBooleanExpression condition = new ComparativeExpression(leftTwoSourceJoinKey,
 			BinaryOperator.EQUAL, createPath("0", "userid"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition).
 			withOuterJoinSources(new InputSelection(1));
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(1).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(0).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan
 			.getExpectedOutput(0)
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url", "code.google.com/p/jaql/")
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url",
 				"java.sun.com/javase/6/docs/api/").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 2, "url", "www.cnn.com").
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformRightOuterTwoSourceJoin() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		// here we set outer join flag
 		final EvaluationExpression rightTwoSourceJoinKey = createPath("1", "userid");
 		final BinaryBooleanExpression condition = new ComparativeExpression(createPath("0", "id"),
 			BinaryOperator.EQUAL, rightTwoSourceJoinKey);
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition).
 			withOuterJoinSources(new InputSelection(1));
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(1).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 4, "url", "www.nbc.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan
 			.getExpectedOutput(0)
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url", "code.google.com/p/jaql/")
 			.
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url",
 				"java.sun.com/javase/6/docs/api/").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 2, "url", "www.cnn.com").
 			addObject("userid", 4, "url", "www.nbc.com");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformRightOuterTwoSourceJoinWithReversedInputs() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		// here we set outer join flag
 		final EvaluationExpression rightTwoSourceJoinKey = createPath("0", "userid");
 		final BinaryBooleanExpression condition = new ComparativeExpression(createPath("1", "id"),
 			BinaryOperator.EQUAL, rightTwoSourceJoinKey);
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition).
 			withOuterJoinSources(new InputSelection(0));
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(1).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(0).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 4, "url", "www.nbc.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan.getExpectedOutput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 1, "url",
 				"java.sun.com/javase/6/docs/api/").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 2, "url", "www.cnn.com").
 			addObject("userid", 4, "url", "www.nbc.com");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformSemiTwoSourceJoin() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		final BinaryBooleanExpression condition = new ElementInSetExpression(createPath("0",
 			"DeptName"), Quantor.EXISTS_IN, createPath("1", "Name"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition);
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(0).
 			addObject("Name", "Harry", "EmpId", 3415, "DeptName", "Finance").
 			addObject("Name", "Sally", "EmpId", 2241, "DeptName", "Sales").
 			addObject("Name", "George", "EmpId", 3401, "DeptName", "Finance").
 			addObject("Name", "Harriet", "EmpId", 2202, "DeptName", "Production");
 		sopremoPlan.getInput(1).
 			addObject("Name", "Sales", "Manager", "Harriet").
 			addObject("Name", "Production", "Manager", "Charles");
 		sopremoPlan.getExpectedOutput(0).
 			addObject("Name", "Sally", "EmpId", 2241, "DeptName", "Sales").
 			addObject("Name", "Harriet", "EmpId", 2202, "DeptName", "Production");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformSemiTwoSourceJoinWithReversedInputs() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		final BinaryBooleanExpression condition = new ElementInSetExpression(createPath("1",
 			"DeptName"), Quantor.EXISTS_IN, createPath("0", "Name"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition);
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(1).
 			addObject("Name", "Harry", "EmpId", 3415, "DeptName", "Finance").
 			addObject("Name", "Sally", "EmpId", 2241, "DeptName", "Sales").
 			addObject("Name", "George", "EmpId", 3401, "DeptName", "Finance").
 			addObject("Name", "Harriet", "EmpId", 2202, "DeptName", "Production");
 		sopremoPlan.getInput(0).
 			addObject("Name", "Sales", "Manager", "Harriet").
 			addObject("Name", "Production", "Manager", "Charles");
 		sopremoPlan.getExpectedOutput(0).
 			addObject("Name", "Sally", "EmpId", 2241, "DeptName", "Sales").
 			addObject("Name", "Harriet", "EmpId", 2202, "DeptName", "Production");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformThetaTwoSourceJoin() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		final BinaryBooleanExpression condition = new ComparativeExpression(createPath("0", "id"),
 			BinaryOperator.LESS, createPath("1", "userid"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition);
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(1).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 4, "url", "www.nbc.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan.getExpectedOutput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 2, "url", "www.cnn.com").
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 4, "url", "www.nbc.com").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 4, "url", "www.nbc.com").
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3, "userid", 4, "url", "www.nbc.com");
 
 		sopremoPlan.run();
 	}
 
 	@Test
 	public void shouldPerformThetaTwoSourceJoinWithReversedInputs() {
 		final SopremoTestPlan sopremoPlan = new SopremoTestPlan(2, 1);
 
 		final BinaryBooleanExpression condition = new ComparativeExpression(createPath("1", "id"),
 			BinaryOperator.LESS, createPath("0", "userid"));
 		final TwoSourceJoin join = new TwoSourceJoin().withCondition(condition);
 		join.setInputs(sopremoPlan.getInputOperators(0, 2));
 		join.setResultProjection(ObjectCreation.CONCATENATION);
 		sopremoPlan.getOutputOperator(0).setInputs(join);
 		sopremoPlan.getInput(1).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1).
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2).
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3);
 		sopremoPlan.getInput(0).
 			addObject("userid", 1, "url", "code.google.com/p/jaql/").
 			addObject("userid", 2, "url", "www.cnn.com").
 			addObject("userid", 4, "url", "www.nbc.com").
 			addObject("userid", 1, "url", "java.sun.com/javase/6/docs/api/");
 		sopremoPlan.getExpectedOutput(0).
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 2, "url", "www.cnn.com").
 			addObject("name", "Jon Doe", "password", "asdf1234", "id", 1, "userid", 4, "url", "www.nbc.com").
 			addObject("name", "Jane Doe", "password", "qwertyui", "id", 2, "userid", 4, "url", "www.nbc.com").
 			addObject("name", "Max Mustermann", "password", "q1w2e3r4", "id", 3, "userid", 4, "url", "www.nbc.com");
 
 		sopremoPlan.run();
 	}
 
 	@Override
 	protected TwoSourceJoin createDefaultInstance(final int index) {
 		final ObjectCreation transformation = new ObjectCreation();
 		transformation.addMapping("field", createPath("0", "[" + index + "]"));
 		final BinaryBooleanExpression condition = new ComparativeExpression(createPath("0", "id"),
 			BinaryOperator.EQUAL, createPath("1", "userid"));
 		return new TwoSourceJoin().
 			withCondition(condition).
 			withResultProjection(transformation);
 	}
 
 }
