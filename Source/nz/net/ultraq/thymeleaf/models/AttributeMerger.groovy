/* 
 * Copyright 2015, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.thymeleaf.models

import nz.net.ultraq.thymeleaf.LayoutDialect
import nz.net.ultraq.thymeleaf.fragments.FragmentProcessor

import org.thymeleaf.model.IModel
import org.thymeleaf.model.IModelFactory
import org.thymeleaf.standard.StandardDialect
import org.thymeleaf.standard.processor.StandardWithTagProcessor

/**
 * Merges attributes from one element into another.
 * 
 * @author Emanuel Rabina
 */
class AttributeMerger implements ModelMerger {

	private final IModelFactory modelFactory

	/**
	 * Constructor, sets up the attribute merger tools.
	 * 
	 * @param modelFactory
	 */
	AttributeMerger(IModelFactory modelFactory) {

		this.modelFactory = modelFactory
	}

	/**
	 * Merge the attributes of the source element with those of the target
	 * element.  This is basically a copy of all attributes in the source model
	 * with those in the target model, overwriting any attributes that have the
	 * same name, except for the case of {@code th:with} where variable
	 * declarations are preserved, only overwriting same-named declarations.
	 * 
	 * @param sourceModel
	 * @param targetModel
	 */
	@Override
	void merge(IModel targetModel, IModel sourceModel) {

		if (!targetModel || !sourceModel) {
			return
		}

		// Merge attributes from the source model's root event to the target model's root event
		sourceModel.get(0).allAttributes

			// Don't include layout:fragment processors
			.findAll { sourceAttribute ->
				return !sourceAttribute.equalsName(LayoutDialect.DIALECT_PREFIX, FragmentProcessor.PROCESSOR_NAME)
			}

			.each { sourceAttribute ->
				def targetEvent = targetModel.get(0)
				def mergedAttributeValue

				// Merge th:with attributes
				if (sourceAttribute.equalsName(StandardDialect.PREFIX, StandardWithTagProcessor.ATTR_NAME)) {
					mergedAttributeValue = new VariableDeclarationMerger().merge(sourceAttribute.value,
						targetEvent.getAttributeValue(StandardDialect.PREFIX, StandardWithTagProcessor.ATTR_NAME))
				}

				// Copy every other attribute straight
				else {
					mergedAttributeValue = sourceAttribute.value
				}

				targetModel.replace(0, modelFactory.replaceAttribute(targetEvent,
					sourceAttribute.attributeName, sourceAttribute.completeName,
					mergedAttributeValue))
			}
	}
}
