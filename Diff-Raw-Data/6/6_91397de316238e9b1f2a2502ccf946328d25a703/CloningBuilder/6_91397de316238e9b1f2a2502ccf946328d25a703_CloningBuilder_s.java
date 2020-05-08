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
 package org.atemsource.atem.utility.clone;
 
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.atemsource.atem.api.attribute.Attribute;
 import org.atemsource.atem.api.type.EntityType;
 import org.atemsource.atem.api.BeanLocator;
 import org.atemsource.atem.api.infrastructure.util.ReflectionUtils;
 import org.atemsource.atem.utility.common.EntityTypeOperationBuilder;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 
 @Component
 @Scope("prototype")
 public class CloningBuilder extends
 	EntityTypeOperationBuilder<CloningAttributeBuilder, CloningBuilder, Cloning, AttributeCloning>
 {
 
 	private List<AttributeCloningBuilderFactory<?, ?, Attribute<?, ?>>> attributeFactories;
 
 	@Inject
 	protected BeanLocator beanLocator;
 
 	@Inject
 	protected CloningBuilderFactory factory;
 
 	@Override
 	protected Cloning createInternally()
 	{
 		Cloning cloning = new Cloning();
 		return cloning;
 	}
 
 	@Override
 	protected CloningAttributeBuilder createAttributeOperationBuilder(Attribute attribute)
 	{
 		for (AttributeCloningBuilderFactory<?, ?, Attribute<?, ?>> factory : attributeFactories)
 		{
 			Class<?>[] actualTypeParameters =
 				ReflectionUtils.getActualTypeParameters(factory.getClass(), AttributeCloningBuilderFactory.class);
 			if (actualTypeParameters[2].isAssignableFrom(attribute.getClass()) && factory.canCreate(attribute, null))
 			{
 				return factory.create(attribute, null);
 			}
 		}
 		throw new IllegalStateException("cannot create attributeBuilder for attribute " + attribute);
 	}
 
 	@Override
 	protected CloningBuilder createViewBuilder(EntityType<?> targetType)
 	{
 		return factory.create(targetType);
 	}
 
 	@Override
 	protected void setEntityType(EntityType<?> entityType)
 	{
 		super.setEntityType(entityType);
 	}
 
 }
