 /*******************************************************************************
  * Copyright Technophobia Ltd 2012
  * 
  * This file is part of the Substeps Eclipse Plugin.
  * 
  * The Substeps Eclipse Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the Eclipse Public License v1.0.
  * 
  * The Substeps Eclipse Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Eclipse Public License for more details.
  * 
  * You should have received a copy of the Eclipse Public License
  * along with the Substeps Eclipse Plugin.  If not, see <http://www.eclipse.org/legal/epl-v10.html>.
  ******************************************************************************/
 package com.technophobia.substeps.editor.outline.feature;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jface.text.Position;
 
 import com.technophobia.substeps.editor.outline.model.AbstractStepContainerElement;
 import com.technophobia.substeps.editor.outline.model.BackgroundElement;
 import com.technophobia.substeps.editor.outline.model.ExampleElement;
 import com.technophobia.substeps.editor.outline.model.ExampleRowElement;
 import com.technophobia.substeps.editor.outline.model.FeatureElement;
 import com.technophobia.substeps.editor.outline.model.ScenarioElement;
 import com.technophobia.substeps.editor.outline.model.ScenarioOutlineElement;
 import com.technophobia.substeps.editor.outline.model.StepElement;
 import com.technophobia.substeps.model.Background;
 import com.technophobia.substeps.model.ExampleParameter;
 import com.technophobia.substeps.model.FeatureFile;
 import com.technophobia.substeps.model.Scenario;
 import com.technophobia.substeps.model.Step;
 import com.technophobia.substeps.supplier.Transformer;
 
 public class FeatureFileToElementTransformer implements Transformer<FeatureFile, FeatureElement> {
 
     // private final static String TAG_POSITIONS = "__tag_positions";
     private final Transformer<Integer, Position> lineNumberToPositionTransformer;
 
 
     public FeatureFileToElementTransformer(final Transformer<Integer, Position> lineNumberToPositionTransformer) {
         this.lineNumberToPositionTransformer = lineNumberToPositionTransformer;
     }
 
 
     @Override
     public FeatureElement from(final FeatureFile file) {
 
        final FeatureElement element = new FeatureElement(file.getName(), asOffsetPosition(1));
 
         addBackground(element, file);
 
         for (final Scenario scenario : file.getScenarios()) {
             addScenarioTo(element, scenario);
         }
 
         return element;
     }
 
 
     private void addScenarioTo(final FeatureElement element, final Scenario scenario) {
         if (scenario.isOutline()) {
             final ScenarioOutlineElement scenarioElement = new ScenarioOutlineElement(scenario.getDescription(),
                     asOffsetPosition(scenario.getScenarioLineNumber()));
             final ExampleElement example = new ExampleElement("Examples",
                     asOffsetPosition(scenario.getExampleKeysLineNumber()));
             scenarioElement.setExample(example);
             for (final ExampleParameter exampleParameter : scenario.getExampleParameters()) {
                 example.addExampleRow(new ExampleRowElement(exampleRowFrom(exampleParameter.getParameters()),
                         asOffsetPosition(exampleParameter.getLineNumber())));
             }
             element.addScenarioOutline(scenarioElement);
             addStepsTo(scenarioElement, scenario.getSteps());
 
         } else {
             final ScenarioElement scenarioElement = new ScenarioElement(scenario.getDescription(),
                     asOffsetPosition(scenario.getScenarioLineNumber()));
             element.addScenario(scenarioElement);
             addStepsTo(scenarioElement, scenario.getSteps());
         }
 
     }
 
 
     private void addBackground(final FeatureElement element, final FeatureFile file) {
         final Background background = backgroundFor(file);
         if (background != null) {
             final int backgroundLineNumber = background.getLineNumber();
             final BackgroundElement backgroundElement = new BackgroundElement(background.getDescription(),
                     asOffsetPosition(backgroundLineNumber));
             addStepsTo(backgroundElement, background.getSteps());
             element.setBackground(backgroundElement);
         }
     }
 
 
     private Background backgroundFor(final FeatureFile file) {
         final List<Scenario> scenarios = file.getScenarios();
         if (!scenarios.isEmpty()) {
             final Scenario scenario = scenarios.get(0);
             return scenario.getBackground();
         }
         return null;
     }
 
 
     private void addStepsTo(final AbstractStepContainerElement stepContainerElement, final List<Step> steps) {
         if (steps != null) {
             for (final Step step : steps) {
                 stepContainerElement.addStep(new StepElement(step.getLine(), asOffsetPosition(step
                         .getSourceLineNumber())));
             }
         }
     }
 
 
     private String exampleRowFrom(final Map<String, String> examples) {
         final StringBuilder sb = new StringBuilder();
         for (final String example : examples.values()) {
             if (sb.length() == 0) {
                 sb.append("|");
             }
             sb.append(example);
             sb.append("|");
         }
         return sb.toString();
     }
 
 
     private Position asOffsetPosition(final int lineNumber) {
        // line number comes from Syntax, which is 1-based. However, the
        // lineNumberToPositionTransformer will expect it to be 0-based
        return lineNumberToPositionTransformer.from(Integer.valueOf(lineNumber - 1));
     }
 }
