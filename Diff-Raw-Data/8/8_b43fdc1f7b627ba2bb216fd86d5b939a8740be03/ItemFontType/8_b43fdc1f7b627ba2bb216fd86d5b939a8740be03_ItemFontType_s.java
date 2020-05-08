 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.preferences;
 
 import static com.zapta.apps.maniana.util.Assertions.check;
 
 import javax.annotation.Nullable;
 
 import android.content.Context;
 import android.graphics.Typeface;
 
 import com.zapta.apps.maniana.util.EnumUtil;
 import com.zapta.apps.maniana.util.EnumUtil.KeyedEnum;
 
 /**
  * Represents possible values of Font preference.
  * 
  * @author Tal Dayan
  */
 public enum ItemFontType implements KeyedEnum {
     CURSIVE("Cursive", "cursive", 1.4f, 0.9f, null, "fonts/Vavont/Vavont-modified.ttf"),
     ELEGANT("Elegant", "elegant", 1.6f, 1.0f, null, "fonts/Pompiere/Pompiere-Regular-modified.ttf"),
     SAN_SERIF("San Serif", "sans", 1.2f, 1.1f, Typeface.SANS_SERIF, null),
     SERIF("Serif", "serif", 1.2f, 1.1f, Typeface.SERIF, null);
 
     /** User visible name. */
     public final String name;
 
     /**
      * Preference value key. Should match the values in preference xml. Persisted in user's
      * settings.
      */
     private final String mKey;
 
     /** Relative scale to normalize size among font types. */
     public final float scale;
     
     public final float lineSpacingMultipler;
 
     /** The standard typeface of null if this is an custom font. */
     @Nullable
     private final Typeface mSysTypeface;
 
     /** Asset font file path or null if this is standard font. */
     @Nullable
     final String mAssetFilePath;
 
     private ItemFontType(String name, String key, float scale, float lineSpacingMultipler, @Nullable Typeface sysTypeface,
             @Nullable String assertFilePath) {
         this.name = name;
         this.mKey = key;
         this.scale = scale;
         this.lineSpacingMultipler = lineSpacingMultipler;
         this.mSysTypeface = sysTypeface;
         this.mAssetFilePath = assertFilePath;
 
         // Exactly one of the two should be non null.
         check((mSysTypeface == null) != (mAssetFilePath == null));
     }
 
     @Override
     public final String getKey() {
         return mKey;
     }
 
     /** Return value with given key, fallback value if not found. */
     @Nullable
     public final static ItemFontType fromKey(String key, @Nullable ItemFontType fallBack) {
         return EnumUtil.fromKey(key, ItemFontType.values(), fallBack);
     }
 
     public final Typeface getTypeface(Context context) {
         if (mSysTypeface != null) {
             return mSysTypeface;
         }
         return Typeface.createFromAsset(context.getAssets(), mAssetFilePath);
     }
 }
