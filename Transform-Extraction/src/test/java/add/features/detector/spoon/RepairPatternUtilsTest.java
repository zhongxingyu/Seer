package add.features.detector.spoon;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import add.features.detector.repairpatterns.RepairPatternDetector;
import add.main.Config;
import add.utils.TestUtils;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

public class RepairPatternUtilsTest {

    @Test
    public void TestIsNewVariableWithNewLocalVariable() {
        Config config = TestUtils.setupConfig("Chart 25");

        String variableName = "n"; // new local variable

        RepairPatternDetector detector = new RepairPatternDetector(config);
        Diff editScript = detector.getEditScript();

        List<CtVariable> variableList = this.findVariableInEditScript(variableName, editScript);

        Assert.assertTrue(variableList.size() > 0);

        CtVariable ctVariable = variableList.get(0);

        Assert.assertTrue(RepairPatternUtils.isNewVariable(ctVariable));
    }

    @Test
    public void TestIsNewVariableWithNewParameterInMethodDefinition() {
        Config config = TestUtils.setupConfig("Closure 64");

        String variableName = "firstOutput"; // new parameter in method definition

        RepairPatternDetector detector = new RepairPatternDetector(config);
        Diff editScript = detector.getEditScript();

        List<CtVariable> variableList = this.findVariableInEditScript(variableName, editScript);

        Assert.assertTrue(variableList.size() > 0);

        CtVariable ctVariable = variableList.get(0);

        Assert.assertTrue(RepairPatternUtils.isNewVariable(ctVariable));
    }

    @Test
    public void TestIsNewVariableWithNewFieldInClassDefinition() {
        Config config = TestUtils.setupConfig("Closure 26");

        String variableName = "modulesWithExports"; // new field in class definition

        RepairPatternDetector detector = new RepairPatternDetector(config);
        Diff editScript = detector.getEditScript();

        List<CtVariable> variableList = this.findVariableInEditScript(variableName, editScript);

        Assert.assertTrue(variableList.size() > 0);

        CtVariable ctVariable = variableList.get(0);

        Assert.assertTrue(RepairPatternUtils.isNewVariable(ctVariable));
    }

    @Test
    public void TestIsNewVariableWithNonNewLocalVariable() {
        Config config = TestUtils.setupConfig("Chart 1");

        String variableName = "dataset"; // existing local variable

        RepairPatternDetector detector = new RepairPatternDetector(config);
        Diff editScript = detector.getEditScript();

        List<CtVariable> variableList = this.findVariableInEditScript(variableName, editScript);

        Assert.assertTrue(variableList.size() > 0);

        CtVariable ctVariable = variableList.get(0);

        Assert.assertFalse(RepairPatternUtils.isNewVariable(ctVariable));
    }

    @Test
    public void TestIsNewVariableWithNonNewParameterInMethodDefinition() {
        Config config = TestUtils.setupConfig("Chart 18");

        String variableName = "columnKey"; // existing parameter in method definition

        RepairPatternDetector detector = new RepairPatternDetector(config);
        Diff editScript = detector.getEditScript();

        List<CtVariable> variableList = this.findVariableInEditScript(variableName, editScript);

        Assert.assertTrue(variableList.size() > 0);

        CtVariable ctVariable = variableList.get(0);

        Assert.assertFalse(RepairPatternUtils.isNewVariable(ctVariable));
    }

    @Test
    public void TestIsNewVariableWithNonNewFieldInClassDefinition() {
        Config config = TestUtils.setupConfig("Chart 15");

        String variableName = "dataset"; // existing field in class definition

        RepairPatternDetector detector = new RepairPatternDetector(config);
        Diff editScript = detector.getEditScript();

        List<CtVariable> variableList = this.findVariableInEditScript(variableName, editScript);

        Assert.assertTrue(variableList.size() > 0);

        CtVariable ctVariable = variableList.get(0);

        Assert.assertFalse(RepairPatternUtils.isNewVariable(ctVariable));
    }

    @Test
    public void TestIsNewVariableWithNonNewFieldInSuperClassDefinition() {
        Config config = TestUtils.setupConfig("Math 68");

        String variableName = "checker"; // existing field in super class definition

        RepairPatternDetector detector = new RepairPatternDetector(config);
        Diff editScript = detector.getEditScript();

        List<CtVariable> variableList = this.findVariableInEditScript(variableName, editScript);

        Assert.assertTrue(variableList.size() > 0);

        CtVariable ctVariable = variableList.get(0);
        if (ctVariable != null) {
            Assert.assertFalse(RepairPatternUtils.isNewVariable(ctVariable));
        }
    }



    private List<CtVariable> findVariableInEditScript(String variableName, Diff editScript) {
        System.out.println("Search for variable "+variableName+"...");
        List<CtVariable> variableList = new ArrayList<>();
        for (int i = 0; i < editScript.getRootOperations().size(); i++) {
            Operation operation = editScript.getRootOperations().get(i);
            CtElement node = null;
            if (operation instanceof InsertOperation) {
                node = operation.getSrcNode();
            } else if (operation instanceof UpdateOperation) {
                node = operation.getDstNode();
            } else if (operation instanceof DeleteOperation || operation instanceof MoveOperation) {
                continue;
            }
            node.getElements(new TypeFilter<CtVariable>(CtVariable.class) {
                @Override
                public boolean matches(CtVariable element) {
                    if (element.getSimpleName().equals(variableName)) {
                        variableList.add(element);
                        System.out.println("Variable found in line "+element.getPosition().toString());
                        return true;
                    }
                    return false;
                }
            });
            node.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class) {
                @Override
                public boolean matches(CtVariableAccess element) {
                    if (element.getVariable().getSimpleName().equals(variableName)) {
                        variableList.add(element.getVariable().getDeclaration());
                        System.out.println("Variable found in line "+element.getPosition().toString());
                        return true;
                    }
                    return false;
                }
            });
        }
        return variableList;
    }

}
