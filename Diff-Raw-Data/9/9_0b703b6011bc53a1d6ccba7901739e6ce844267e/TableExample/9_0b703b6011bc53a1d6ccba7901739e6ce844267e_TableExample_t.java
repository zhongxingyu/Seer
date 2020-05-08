 /*
  *    Copyright 2010, Matthias Brandt
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package de.splitstudio.androidb;
 
 import android.content.Context;
 import de.splitstudio.androidb.annotation.Column;
 import de.splitstudio.androidb.annotation.TableMetaData;
 
 @TableMetaData(version = 4)
 public class TableExample extends Table {
 
 	public TableExample(final Context context) {
 		super(context);
 	}
 
 	@Column
 	String text;
 
	@Column(indexNames = { "idx_amount" })
 	float amount;
 }
