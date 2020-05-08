 /*******************************************************************************
  * Stefan Meyer, 2012
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.atemsource.atem.utility.compare;
 
 
 import java.util.List;
 import java.util.Set;
 
 import javax.inject.Inject;
 
import org.atemsource.atem.api.attribute.Attribute;
import org.atemsource.atem.api.type.EntityType;
 import org.atemsource.atem.api.BeanLocator;
 import org.atemsource.atem.api.infrastructure.util.ReflectionUtils;
 import org.atemsource.atem.utility.common.EntityTypeOperationBuilder;
 
 
 public class ComparisonBuilder extends
 	EntityTypeOperationBuilder<ComparisonAttributeBuilder, ComparisonBuilder, Comparison, AttributeComparison>
 {
 
 	private List<ComparisonAttributeBuilderFactory<?, ?, Attribute<?, ?>>> attributeFactories;
 
 	@Inject
 	private ComparisonBuilderFactory comparisonBuilderFactory;
 
 	@Inject
 	private BeanLocator beanLocator;
 
 	@Override
 	protected Comparison createInternally()
 	{
 		Set<AttributeComparison> operations = createOperations();
 		Comparison comparison = beanLocator.getInstance(Comparison.class);
 		comparison.setEntityType(getEntityType());
 		comparison.setAttributeOperations(operations);
 		return comparison;
 	}
 
 	@Override
 	protected ComparisonAttributeBuilder createAttributeOperationBuilder(Attribute attribute)
 	{
 		for (ComparisonAttributeBuilderFactory<?, ?, Attribute<?, ?>> factory : attributeFactories)
 		{
 			Class<?>[] actualTypeParameters =
 				ReflectionUtils.getActualTypeParameters(factory.getClass(), ComparisonAttributeBuilderFactory.class);
 			if (actualTypeParameters.length > 1 && actualTypeParameters[2].isAssignableFrom(attribute.getClass())
 				&& factory.canCreate(attribute, null))
 			{
 				return factory.create(attribute, null);
 			}
 		}
 		throw new IllegalStateException("cannot create attributeBuilder for attribute " + attribute);
 	}
 
 	@Override
 	protected ComparisonBuilder createViewBuilder(EntityType<?> entityType)
 	{
 		return comparisonBuilderFactory.create(entityType);
 	}
 
 	public List<ComparisonAttributeBuilderFactory<?, ?, Attribute<?, ?>>> getAttributeFactories()
 	{
 		return attributeFactories;
 	}
 
 	public void setAttributeFactories(List<ComparisonAttributeBuilderFactory<?, ?, Attribute<?, ?>>> attributeFactories)
 	{
 		this.attributeFactories = attributeFactories;
 	}
 
 	@Override
 	protected void setEntityType(EntityType<?> entityType)
 	{
 		super.setEntityType(entityType);
 	}
 
 }
