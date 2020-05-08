 /*******************************************************************************
  * Stefan Meyer, 2012 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  ******************************************************************************/
 package org.atemsource.atem.utility.transform.impl.builder;
 
 import org.atemsource.atem.api.attribute.Attribute;
 import org.atemsource.atem.api.attribute.relation.SingleAttribute;
 import org.atemsource.atem.api.type.EntityType;
 import org.atemsource.atem.api.type.EntityTypeBuilder;
 import org.atemsource.atem.api.type.Type;
 import org.atemsource.atem.utility.path.AttributePath;
 import org.atemsource.atem.utility.transform.api.AttributeTransformation;
 import org.atemsource.atem.utility.transform.api.AttributeTransformationBuilder;
 import org.atemsource.atem.utility.transform.api.Converter;
 import org.atemsource.atem.utility.transform.impl.converter.Constraining;
 import org.atemsource.atem.utility.transform.impl.transformation.SingleAttributeTransformation;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 @Component
 @Scope("prototype")
public class SingleAttributeTransformationBuilder<A, B>
		extends
		OneToOneAttributeTransformationBuilder<A, B, SingleAttributeTransformationBuilder<A, B>> {
 	public void build(EntityTypeBuilder entityTypeBuilder) {
 		AttributePath sourcePath = attributePathBuilderFactory
 				.createAttributePath(getSourceAttribute(), sourceType);
 		Type<?> attributeTargetType;
		Converter<?, ?> converter = getConverter(sourcePath.getTargetType()
				.getType());
 		if (converter != null) {
 			attributeTargetType = converter.getTypeB();
 		} else {
 			attributeTargetType = sourcePath.getTargetType().getType();
 		}
		SingleAttribute<?> attribute = entityTypeBuilder.addSingleAttribute(
				getTargetAttribute(), attributeTargetType);
		if (converter != null && converter instanceof Constraining) {
			Constraining constraining = ((Constraining) converter);
			for (String name : constraining.getConstraintNamesAB()) {
				Attribute metaAttribute = entityTypeRepository.getEntityType(
						Attribute.class).getMetaAttribute(name);
				if (metaAttribute != null) {
					metaAttribute.setValue(attribute,
							constraining.getConstraintAB(name));
				}
 			}
 		}
 	}
 
 	public AttributeTransformation<A, B> create(EntityType<B> targetType) {
 		AttributePath sourcePath = attributePathBuilderFactory
 				.createAttributePath(getSourceAttribute(), sourceType);
 		AttributePath targetPath = attributePathBuilderFactory
 				.createAttributePath(getTargetAttribute(), targetType);
 		SingleAttributeTransformation<A, B> primitiveAttributeTransformation = beanLocator
 				.getInstance(SingleAttributeTransformation.class);
 		primitiveAttributeTransformation.setAttributeA(sourcePath);
 		primitiveAttributeTransformation.setAttributeB(targetPath);
		primitiveAttributeTransformation
				.setTransformation(getTransformation(sourcePath.getTargetType()
						.getType()));
 		primitiveAttributeTransformation.setTypeA(sourceType);
 		primitiveAttributeTransformation.setTypeB(targetType);
 		primitiveAttributeTransformation.setMeta(meta);
 		addDerivation(primitiveAttributeTransformation,
 				targetPath.getAttribute(), sourcePath.getAttribute());
 		return primitiveAttributeTransformation;
 	}
 
 }
