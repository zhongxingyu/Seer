 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
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
 
 package org.romaframework.module.users.view.domain.baseaccount;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.romaframework.aspect.core.annotation.AnnotationConstants;
 import org.romaframework.aspect.core.annotation.CoreField;
import org.romaframework.aspect.flow.FlowAspect;
 import org.romaframework.aspect.persistence.QueryByFilter;
 import org.romaframework.core.Roma;
 import org.romaframework.frontend.domain.crud.CRUDMain;
 import org.romaframework.module.users.domain.BaseAccount;
 import org.romaframework.module.users.repository.BaseAccountRepository;
 import org.romaframework.module.users.view.domain.basegroup.BaseGroupAssegnee;
 import org.romaframework.module.users.view.domain.baseprofile.BaseProfileAssegnee;
 
 public class BaseAccountMain extends CRUDMain<BaseAccount> {
 
 	@CoreField(embedded = AnnotationConstants.TRUE)
 	protected BaseAccountFilter					filter;
 
 	protected List<BaseAccountListable>	result;
 
 	public BaseAccountMain() {
 		super(BaseAccountListable.class, BaseAccountInstance.class, BaseAccountInstance.class, BaseAccountInstance.class);
 
 		if (Roma.existComponent(BaseAccountRepository.class))
 			repository = Roma.component(BaseAccountRepository.class);
 
 		filter = new BaseAccountFilter();
 		result = new ArrayList<BaseAccountListable>();
 	}
 
 	@Override
 	public BaseAccountFilter getFilter() {
 		return filter;
 	}
 
 	@Override
 	public List<BaseAccountListable> getResult() {
 		return result;
 	}
 
 	@Override
 	public void showAll() {
 		QueryByFilter query = new QueryByFilter(BaseAccount.class);
 		searchByFilter(query);
 	}
 
 	public void assignToProfile() {
 		Object[] selection = getSelection();
 		if (selection == null || selection.length == 0)
 			return;
 
 		Roma.flow().popup(new BaseProfileAssegnee(this));
 	}
 
 	public void assignToGroup() {
 		Object[] selection = getSelection();
 		if (selection == null || selection.length == 0)
 			return;
 
 		Roma.flow().forward(new BaseGroupAssegnee(this));
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void setResult(Object iValue) {
 		result = (List<BaseAccountListable>) iValue;
 	}
 }
