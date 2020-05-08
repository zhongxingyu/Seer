 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 
 package org.amplafi.flow.web.resolvers;
 
 
 import org.amplafi.flow.impl.FlowActivityImpl;
 import org.amplafi.flow.FlowActivityImplementor;
 import org.amplafi.flow.FlowConstants;
 import org.amplafi.flow.FlowDefinitionsManager;
 import org.amplafi.flow.FlowImplementor;
 import org.amplafi.flow.impl.FlowImpl;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
 import org.amplafi.flow.web.components.FlowBorder;
 import org.amplafi.flow.web.components.FullFlowComponent;
 import org.apache.tapestry.IComponent;
 import org.apache.tapestry.IRequestCycle;
 import org.apache.tapestry.annotations.Parameter;
 import org.apache.tapestry.asset.ExternalResource;
 import org.apache.tapestry.parse.ComponentTemplate;
 import org.apache.tapestry.parse.ITemplateParser;
 import org.apache.tapestry.parse.TemplateToken;
 import org.apache.tapestry.parse.ITemplateParserDelegate;
 import org.apache.tapestry.parse.TemplateParseException;
 import org.apache.tapestry.resolver.ComponentSpecificationResolver;
 import org.apache.tapestry.spec.IComponentSpecification;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hivemind.Location;
 import org.apache.hivemind.Resource;
 import org.testng.Assert;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import org.easymock.EasyMock;
 
 import static org.amplafi.flow.web.resolvers.FlowAwareTemplateSourceDelegate.*;
 import static org.easymock.classextension.EasyMock.*;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Test {@link FlowAwareTemplateSourceDelegate}.
  */
 public class TestFlowAwareTemplateSourceDelegate extends Assert {
 
     private static final String CATEGORY_SELECTION_FPROP_CATEGORY_SELECTION = "categorySelection=\"fprop:categorySelection\" ";
     private static final String VALIDATORS = "<span jwcid=\"@flow:AttachFlowValidators\" validators=\"validators:flow\"/>";
     private static final String TEMPLATE_FORM =
         "<span jwcid=\"inF@If\" renderTag=\"false\" condition=\"ognl:insideForm\">" +
         "<span jwcid=\"@RenderBlock\" block=\"component:flowBlock\"/></span>" +
         "<span jwcid=\"orF@Else\" renderTag=\"false\">" +
         "<form jwcid=\"comp_0FlowForm@Form\" async=\"ognl:async\" clientValidationEnabled=\"true\" class=\"ognl:className\" " +
         "delegate=\"ognl:delegate\" cancel=\"listener:doCancelForm\" refresh=\"listener:doRefreshForm\" stateful=\"ognl:stateful\">" +
         "<span jwcid=\"@RenderBlock\" block=\"component:flowBlock\"/>" + VALIDATORS + "</form></span>";
     private static final String TEMPLATE_SUFFIX =
         "<span jwcid=\"@RenderBody\"/><div jwcid=\"@RenderBlock\" block=\"ognl:currentBlock\"/></div></div>\n</span>";
     private static final String TEMPLATE_PREFIX = TEMPLATE_FORM +
     "<span jwcid=\"flowBlock@Block\">" + "<div jwcid=\"" + VISIBLE_FLOW_IF
             + "@If\" condition=\"ognl:visibleFlow\" renderTag=\"false\">";
 
     @DataProvider(name="FlowAwareTemplateSourceDelegate")
     protected Object[][] getFlowAwareTemplateSourceDelegate() {
         FlowAwareTemplateSourceDelegate delegate =
             new FlowAwareTemplateSourceDelegate();
         delegate.setLog(LogFactory.getLog(this.getClass()));
         return new Object[][] { new Object[] {delegate}};
     }
     /**
      * Test a missing flow definition.
      * @param delegate
      *
      */
     @Test(dataProvider="FlowAwareTemplateSourceDelegate")
     public void testMissingFlow(FlowAwareTemplateSourceDelegate delegate) {
         String componentName = "NoSuch";
         IComponentSpecification compSpec = createSimpleCompSpec(componentName, FullFlowComponent.class);
 
         trainUsingMemoryLocation(compSpec);
 
         IComponent component = createMock(IComponent.class);
         expect(component.getSpecification()).andReturn(compSpec);
         trainGetFlowForComponent(componentName, null, delegate);
         trainParser(delegate);
         Locale locale = null;
         IRequestCycle cycle = createMock(IRequestCycle.class);
 
         replay(component, compSpec);
         ComponentTemplate bad = delegate.findTemplate(cycle, component, locale);
         assertEqualsExcludingWhitespace(new String(bad.getTemplateData()),
                 "<div>[Flow "+componentName+" not found]</div>");
     }
 
     /**
      * Test a flow definition that has no activites.
      * @param delegate
      *
      */
     @Test(dataProvider="FlowAwareTemplateSourceDelegate")
     public void testEmptyFlow(FlowAwareTemplateSourceDelegate delegate) {
         String type = "Good";
         IComponentSpecification compSpec = createSimpleCompSpec(type, FullFlowComponent.class);
         IComponent component = createMock(IComponent.class);
         expect(component.getSpecification()).andReturn(compSpec);
 
         trainUsingMemoryLocation(compSpec);
 
         FlowImplementor flow = createSimpleFlow(type);
         trainGetFlowForComponent(type, flow, delegate);
         trainParser(delegate);
         Locale locale = null;
         IRequestCycle cycle = createMock(IRequestCycle.class);
 
         replay(component, compSpec);
         ComponentTemplate bad = delegate.findTemplate(cycle, component, locale);
         assertEqualsExcludingWhitespace(new String(bad.getTemplateData()),
                 "<div>[Flow " + type + " has no activites]</div>");
     }
 
     /**
      * This flow has some activities.
      * @param delegate
      *
      */
     @Test(dataProvider="FlowAwareTemplateSourceDelegate")
     public void testSimple2Flow(FlowAwareTemplateSourceDelegate delegate) {
         String componentName = "comp_0";
         String flowActivityName = FlowActivityImpl.class.getSimpleName();
         ComponentSpecificationResolver csr = createMock(ComponentSpecificationResolver.class);
         delegate.setComponentSpecificationResolver(csr);
         IComponentSpecification compSpec = createSimpleCompSpec(componentName, FullFlowComponent.class);
 
         trainUsingMemoryLocation(compSpec);
 
         IComponent component = createMock(IComponent.class);
         expect(component.getSpecification()).andReturn(compSpec);
         expect(component.getNamespace()).andReturn(null).anyTimes();
         expect(component.getLocation()).andReturn(null);
         FlowImplementor flow = createFlow2(componentName, 1);
         trainGetFlowForComponent(componentName, flow, delegate);
         trainParser(delegate);
         IRequestCycle cycle = createMock(IRequestCycle.class);
         csr.resolve(cycle, null, componentName, null);
         expect(csr.getSpecification()).andReturn(compSpec);
         expect(compSpec.getComponentClassName()).andReturn(FakeComponent.class.getName());
 
         programFlowBorder(csr, cycle);
 
         replay(component, csr, compSpec);
         Locale locale = null;
         ComponentTemplate good = delegate.findTemplate(cycle, component, locale);
         checkTemplate(flowActivityName, good);
     }
     /**
      * @param csr
      * @param cycle
      */
     private void programFlowBorder(ComponentSpecificationResolver csr, IRequestCycle cycle) {
         IComponentSpecification flowBorderComponentSpec = createSimpleCompSpec(FLOW_BORDER_COMPONENT, FlowBorder.class);
         csr.resolve(cycle, null, FLOW_BORDER_COMPONENT, null);
         expect(csr.getSpecification()).andReturn(flowBorderComponentSpec);
         expect(flowBorderComponentSpec.getComponentClassName()).andReturn(FlowBorder.class.getName());
         replay(flowBorderComponentSpec);
     }
 
     /**
      * This flow has some activities and properties.
      *
      * TODO: This test fails due to some order dependency in a map or set.
      * the expected string lists the same output attributes but the actual some times has a different attribute order.
      * @param delegate
      *
      */
     @Test(dataProvider="FlowAwareTemplateSourceDelegate")
     public void testFlowWithProperties(FlowAwareTemplateSourceDelegate delegate) {
         String flowActivityName = FlowActivityImpl.class.getSimpleName();
         String componentName = "comp_0";
         ComponentSpecificationResolver csr = createMock(ComponentSpecificationResolver.class);
         delegate.setComponentSpecificationResolver(csr);
         IComponentSpecification compSpec = createSimpleCompSpec(componentName, FullFlowComponent.class);
 
         trainUsingMemoryLocation(compSpec);
 
         IComponent component = createMock(IComponent.class);
         expect(component.getSpecification()).andReturn(compSpec);
         expect(component.getNamespace()).andReturn(null).anyTimes();
         expect(component.getLocation()).andReturn(null);
         FlowImplementor flow = createFlowWithProperties(componentName);
         trainGetFlowForComponent(componentName, flow, delegate);
         trainParser(delegate);
         IRequestCycle cycle = createMock(IRequestCycle.class);
         csr.resolve(cycle, null, componentName, null);
         expect(csr.getSpecification()).andReturn(compSpec);
         expect(compSpec.getComponentClassName()).andReturn(FakeComponent.class.getName()).anyTimes();
 
         programFlowBorder(csr, cycle);
 
         replay(component, csr, compSpec, cycle);
         Locale locale = null;
         ComponentTemplate good = delegate.findTemplate(cycle, component, locale);
         checkTemplate(flowActivityName, good);
     }
     private void checkTemplate(String flowActivityName, ComponentTemplate good) {
         String fling = getFlingStr(flowActivityName);
         String foo = getFooString(flowActivityName);
         String[] choice= new String[] {
             fling,
             foo,
             CATEGORY_SELECTION_FPROP_CATEGORY_SELECTION
         };
         int[][] c = { new int[] { 0,1,2 }, new int[] {0,2,1 }, new int[] { 1,0,2 }, new int[] { 1, 2, 0}, new int[] {2,1,0}, new int[] {2,0,1}};
        String goodTemplateString = new String(good.getTemplateData());
         AssertionError last = null;
         for (int[] element : c) {
             try {
                 last = null;
                String templateString = TEMPLATE_PREFIX + "<div jwcid=\"fc0@Block\"><div jwcid=\"fic_" + flowActivityName + "_0@comp_0\" " + choice[element[0]] + choice[element[1]] + choice[element[2]] +"/></div>\n" + getFullFlowBorderTemplate() + TEMPLATE_SUFFIX;
                assertEqualsExcludingWhitespace(goodTemplateString, templateString);
                 break;
             } catch(AssertionError e) {
                 last = e;
             }
         }
         if ( last != null ) {
             throw last;
         }
     }
     private String getFooString(String flowActivityName) {
         return "fooMessage=\"fprop:fooMessage=fic_" + flowActivityName + "_0@message:foo-message\"literal";
     }
     private String getFlingStr(String flowActivityName) {
         return "Fling=\"fprop:literalFling=fic_"+flowActivityName+"_0@literal:fling\"";
     }
 
     private void trainUsingMemoryLocation(IComponentSpecification compSpec) {
         Location location = createMock(Location.class);
         expect(compSpec.getLocation()).andReturn(location);
         replay(location);
         compSpec.setLocation(isA(Location.class));
     }
 
     private void trainGetFlowForComponent(String componentName, FlowImplementor flow, FlowAwareTemplateSourceDelegate delegate) {
         FlowDefinitionsManager flowDefinitionsManager = createMock(FlowDefinitionsManager.class);
         expect(flowDefinitionsManager.getFlowDefinition(componentName)).andReturn(flow);
         replay(flowDefinitionsManager);
         delegate.setFlowDefinitionsManager(flowDefinitionsManager);
     }
 
     private void trainParser(FlowAwareTemplateSourceDelegate delegate) {
         // we dont want to mock the world, so we just mock this
         ITemplateParser templateParser = createMock(ITemplateParser.class);
         delegate.setParser(templateParser);
         try {
             expect(templateParser.parse(EasyMock.isA(char[].class),
                     EasyMock.isA(ITemplateParserDelegate.class),
                     EasyMock.isA(Resource.class)
             )).andReturn(new TemplateToken[0]);
         } catch (TemplateParseException e) {
             fail();
         }
         replay(templateParser);
     }
 
     private FlowImplementor createFlowWithProperties(String componentName) {
         FlowImplementor flow = createFlow2(componentName, 1);
         FlowPropertyDefinitionImpl globalDef = new FlowPropertyDefinitionImpl("globaldef1");
         globalDef.setUiComponentParameterName("componentGlobaldef1");
         flow.addPropertyDefinitions(globalDef);
         FlowPropertyDefinitionImpl globalOverlap = new FlowPropertyDefinitionImpl("overlap");
         globalOverlap.setUiComponentParameterName("globalOverlapParameter");
         flow.addPropertyDefinitions(globalOverlap);
         FlowPropertyDefinitionImpl overlap  = new FlowPropertyDefinitionImpl("overlap");
         overlap.setUiComponentParameterName("componentOverlapParameter");
         ((FlowActivityImplementor)flow.getActivity(0)).addPropertyDefinitions(overlap);
         return flow;
     }
 
     private FlowImplementor createSimpleFlow(String flowTypeName) {
         FlowImplementor simple = new FlowImpl(flowTypeName);
         return simple;
     }
 
     private FlowImplementor createFlow2(String flowTypeName, int size) {
         FlowImplementor simple = new FlowImpl(flowTypeName);
 
         for (int i = 0; i < size; i++) {
             FlowActivityImpl activity = new FlowActivityImpl();
             activity.setComponentName("comp_"+i);
             simple.addActivity(activity);
         }
         return simple;
     }
 
     private IComponentSpecification createSimpleCompSpec(String componentName, Class<?> componentClass) throws NoSuchMethodError {
         IComponentSpecification compSpec = createMock(IComponentSpecification.class);
         expect(compSpec.getPublicId()).andReturn(null);
         expect(compSpec.getComponentClassName()).andReturn(componentClass.getName());
         expect(compSpec.getDescription()).andReturn(componentName);
         Resource res = new ExternalResource("dummy", null);
         expect(compSpec.getSpecificationLocation()).andReturn(res).anyTimes();
         return compSpec;
     }
 
     private String getFullFlowBorderTemplate() {
         return "<div jwcid=\"flowBorder@flow:FlowBorder\" " +
                 FlowConstants.FSHIDE_FLOW_CONTROL +
         		"=\"ognl:" +
         		FlowConstants.FSHIDE_FLOW_CONTROL +
         		"\" updateComponents=\"ognl:updateComponents\" "
         + "endListener=\"ognl:endListener\" cancelListener=\"ognl:cancelListener\" finishListener=\"ognl:finishListener\" "
         + "async=\"ognl:async\" nextListener=\"ognl:nextListener\" previousListener=\"ognl:previousListener\""
         + " fsFlowTransitions=\"fprop:fsFlowTransitions\""
         + " attachedFlowState=\"" + FLOW_TO_USE +
         		"\" updateListener=\"fprop:updateListener\""
         +">\n";
     }
     /**
      * Asserts that two strings are equal not taking into account whitespace differences.
      * @param one
      * @param two
      */
     protected void assertEqualsExcludingWhitespace(String one, String two) {
         assertEquals(StringUtils.join(one.split("\\s")),
                 StringUtils.join(two.split("\\s")));
     }
 
     public abstract static class FakeComponent implements IComponent {
         @Parameter(required=true)
         public abstract List<Object> getCategorySelection();
         @Parameter(defaultValue="message:foo-message")
         public abstract String getFooMessage();
         @Parameter(defaultValue="literal:fling")
         public abstract String getLiteralFling();
     }
 }
