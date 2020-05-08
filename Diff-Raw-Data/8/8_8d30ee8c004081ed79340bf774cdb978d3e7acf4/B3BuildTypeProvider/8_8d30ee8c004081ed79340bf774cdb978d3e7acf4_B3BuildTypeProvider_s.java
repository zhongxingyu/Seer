 /**
  * Copyright (c) 2010, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.typeinference;
 
 import java.lang.reflect.Type;
 
 import org.eclipse.b3.build.BeeModel;
 import org.eclipse.b3.build.BuilderNamePredicate;
 import org.eclipse.b3.build.ImplementsPredicate;
 import org.eclipse.b3.build.InputPredicate;
 import org.eclipse.b3.build.PathGroupPredicate;
 import org.eclipse.b3.build.ProvidesPredicate;
 import org.eclipse.b3.build.Repository;
 import org.eclipse.b3.build.RequiresPredicate;
 import org.eclipse.b3.build.UnitNamePredicate;
 import org.eclipse.b3.build.UnitProvider;
 import org.eclipse.b3.build.repository.IBuildUnitRepository;
 
 /**
  * Extension for build model
  * 
  */
 public class B3BuildTypeProvider extends B3ExpressionTypeProvider {
 
 	public Type type(BeeModel o) {
 		return BeeModel.class;
 	}
 
 	public Type type(BuilderNamePredicate o) {
 		return Boolean.class;
 	}
 
 	public Type type(ImplementsPredicate o) {
 		return Boolean.class;
 	}
 
 	public Type type(InputPredicate o) {
 		return Boolean.class;
 	}
 
 	public Type type(PathGroupPredicate o) {
 		return Boolean.class;
 	}
 
 	public Type type(ProvidesPredicate o) {
 		return Boolean.class;
 	}
 
 	public Type type(Repository o) {
 		return IBuildUnitRepository.class;
 	}
 
 	public Type type(RequiresPredicate o) {
 		return Boolean.class;
 	}
 
 	public Type type(UnitNamePredicate o) {
 		return Boolean.class;
 	}
 
 	public Type type(UnitProvider o) {
 		return UnitProvider.class;
 	}
 }
