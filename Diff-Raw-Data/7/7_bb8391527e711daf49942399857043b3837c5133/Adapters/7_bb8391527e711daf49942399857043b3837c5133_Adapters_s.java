 package com.example.xmladapters;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.View;
 import android.widget.BaseAdapter;
 
 /**
  * <p>This class can be used to load {@link android.widget.Adapter adapters} defined in
  * XML resources. XML-defined adapters can be used to easily create adapters in your
  * own application or to pass adapters to other processes.</p>
  * 
  * <h2>Types of adapters</h2>
  * <p>Adapters defined using XML resources can only be one of the following supported
  * types. Arbitrary adapters are not supported to guarantee the safety of the loaded
  * code when adapters are loaded across packages.</p>
  * <ul>
  *  <li><a href="#xml-cursor-adapter">Cursor adapter</a>: a cursor adapter can be used
  *  to display the content of a cursor, most often coming from a content provider</li>
  * </ul>
  * <p>The complete XML format definition of each adapter type is available below.</p>
  * 
  * <a name="xml-cursor-adapter"></a>
  * <h2>Cursor adapter</h2>
  * <p>A cursor adapter XML definition starts with the
  * <a href="#xml-cursor-adapter-tag"><code>&lt;cursor-adapter /&gt;</code></a>
  * tag and may contain one or more instances of the following tags:</p>
  * <ul>
  *  <li><a href="#xml-cursor-adapter-select-tag"><code>&lt;select /&gt;</code></a></li>
  *  <li><a href="#xml-cursor-adapter-bind-tag"><code>&lt;bind /&gt;</code></a></li>
  * </ul>
  * 
  * <a name="xml-cursor-adapter-tag"></a>
  * <h3>&lt;cursor-adapter /&gt;</h3>
  * <p>The <code>&lt;cursor-adapter /&gt;</code> element defines the beginning of the
  * document and supports the following attributes:</p>
  * <ul>
  *  <li><code>android:layout</code>: Reference to the XML layout to be inflated for
  *  each item of the adapter. This attribute is mandatory.</li>
  *  <li><code>android:selection</code>: Selection expression, used when the
  *  <code>android:uri</code> attribute is defined or when the adapter is loaded with
  *  {@link Adapters#loadCursorAdapter(android.content.Context, int, String, Object[])}.
  *  This attribute is optional.</li>
  *  <li><code>android:sortOrder</code>: Sort expression, used when the
  *  <code>android:uri</code> attribute is defined or when the adapter is loaded with
  *  {@link Adapters#loadCursorAdapter(android.content.Context, int, String, Object[])}.
  *  This attribute is optional.</li>
  *  <li><code>android:uri</code>: URI of the content provider to query to retrieve a cursor.
  *  Specifying this attribute is equivalent to calling
  *  {@link Adapters#loadCursorAdapter(android.content.Context, int, String, Object[])}.
  *  If you call this method, the value of the XML attribute is ignored. This attribute is
  *  optional.</li>
  * </ul>
  * <p>In addition, you can specify one or more instances of
  * <a href="#xml-cursor-adapter-select-tag"><code>&lt;select /&gt;</code></a> and
  * <a href="#xml-cursor-adapter-bind-tag"><code>&lt;bind /&gt;</code></a> tags as children
  * of <code>&lt;cursor-adapter /&gt;</code>.</p>
  * 
  * <a name="xml-cursor-adapter-select-tag"></a>
  * <h3>&lt;select /&gt;</h3>
  * <p>The <code>&lt;select /&gt;</code> tag is used to select columns from the cursor
  * when doing the query. This can be very useful when using transformations in the
  * <code>&lt;bind /&gt;</code> elements. It can also be very useful if you are providing
  * your own <a href="#xml-cursor-adapter-bind-data-types">binder</a> or
  * <a href="#xml-cursor-adapter-bind-data-types">transformation</a> classes.
  * <code>&lt;select /&gt;</code> elements are ignored if you supply the cursor yourself.</p>
  * <p>The <code>&lt;select /&gt;</code> supports the following attributes:</p>
  * <ul>
  *  <li><code>android:column</code>: Name of the column to select in the cursor during the
  *  query operation</li>
  * </ul>
  * <p><strong>Note:</strong> The column named <code>_id</code> is always implicitly
  * selected.</p>
  * 
  * <a name="xml-cursor-adapter-bind-tag"></a>
  * <h3>&lt;bind /&gt;</h3>
  * <p>The <code>&lt;bind /&gt;</code> tag is used to bind a column from the cursor to
  * a {@link android.view.View}. A column bound using this tag is automatically selected
  * during the query and a matching
  * <a href="#xml-cursor-adapter-select-tag"><code>&lt;select /&gt;</code> tag is therefore
  * not required.</p>
  * 
  * <p>Each binding is declared as a one to one matching but
  * custom binder classes or special
  * <a href="#xml-cursor-adapter-bind-data-transformation">data transformations</a> can
  * allow you to bind several columns to a single view. In this case you must use the
  * <a href="#xml-cursor-adapter-select-tag"><code>&lt;select /&gt;</code> tag to make
  * sure any required column is part of the query.</p>
  * 
  * <p>The <code>&lt;bind /&gt;</code> tag supports the following attributes:</p>
  * <ul>
  *  <li><code>android:from</code>: The name of the column to bind from.
  *  This attribute is mandatory. Note that <code>@</code> which are not used to reference resources
  *  should be backslash protected as in <code>\@</code>.</li>
  *  <li><code>android:to</code>: The id of the view to bind to. This attribute is mandatory.</li>
  *  <li><code>android:as</code>: The <a href="#xml-cursor-adapter-bind-data-types">data type</a>
  *  of the binding. This attribute is mandatory.</li>
  * </ul>
  * 
  * <p>In addition, a <code>&lt;bind /&gt;</code> can contain zero or more instances of
  * <a href="#xml-cursor-adapter-bind-data-transformation">data transformations</a> children
  * tags.</p>
  *
  * <a name="xml-cursor-adapter-bind-data-types"></a>
  * <h4>Binding data types</h4>
  * <p>For a binding to occur the data type of the bound column/view pair must be specified.
  * The following data types are currently supported:</p>
  * <ul>
  *  <li><code>string</code>: The content of the column is interpreted as a string and must be
  *  bound to a {@link android.widget.TextView}</li>
  *  <li><code>image</code>: The content of the column is interpreted as a blob describing an
  *  image and must be bound to an {@link android.widget.ImageView}</li>
  *  <li><code>image-uri</code>: The content of the column is interpreted as a URI to an image
  *  and must be bound to an {@link android.widget.ImageView}</li>
  *  <li><code>drawable</code>: The content of the column is interpreted as a resource id to a
  *  drawable and must be bound to an {@link android.widget.ImageView}</li>
  *  <li><code>tag</code>: The content of the column is interpreted as a string and will be set as
  *  the tag (using {@link View#setTag(Object)} of the associated View. This can be used to
  *  associate meta-data to your view, that can be used for instance by a listener.</li>
  *  <li>A fully qualified class name: The name of a class corresponding to an implementation of
  *  {@link Adapters.CursorBinder}. Cursor binders can be used to provide
  *  bindings not supported by default. Custom binders cannot be used with
  *  {@link android.content.Context#isRestricted() restricted contexts}, for instance in an
  *  application widget</li>
  * </ul>
  * 
  * <a name="xml-cursor-adapter-bind-transformation"></a>
  * <h4>Binding transformations</h4>
  * <p>When defining a data binding you can specify an optional transformation by using one
  * of the following tags as a child of a <code>&lt;bind /&gt;</code> elements:</p>
  * <ul>
  *  <li><code>&lt;map /&gt;</code>: Maps a constant string to a string or a resource. Use
  *  one instance of this tag per value you want to map</li>
  *  <li><code>&lt;transform /&gt;</code>: Transforms a column's value using an expression
  *  or an instance of {@link Adapters.CursorTransformation}</li>
  * </ul>
  * <p>While several <code>&lt;map /&gt;</code> tags can be used at the same time, you cannot
  * mix <code>&lt;map /&gt;</code> and <code>&lt;transform /&gt;</code> tags. If several
  * <code>&lt;transform /&gt;</code> tags are specified, only the last one is retained.</p>
  * 
  * <a name="xml-cursor-adapter-bind-transformation-map" />
  * <p><strong>&lt;map /&gt;</strong></p>
  * <p>A map element simply specifies a value to match from and a value to match to. When
  * a column's value equals the value to match from, it is replaced with the value to match
  * to. The following attributes are supported:</p>
  * <ul>
  *  <li><code>android:fromValue</code>: The value to match from. This attribute is mandatory</li>
  *  <li><code>android:toValue</code>: The value to match to. This value can be either a string
  *  or a resource identifier. This value is interpreted as a resource identifier when the
  *  data binding is of type <code>drawable</code>. This attribute is mandatory</li>
  * </ul>
  * 
  * <a name="xml-cursor-adapter-bind-transformation-transform"></a>
  * <p><strong>&lt;transform /&gt;</strong></p>
  * <p>A simple transform that occurs either by calling a specified class or by performing
  * simple text substitution. The following attributes are supported:</p>
  * <ul>
  *  <li><code>android:withExpression</code>: The transformation expression. The expression is
  *  a string containing column names surrounded with curly braces { and }. During the
  *  transformation each column name is replaced by its value. All columns must have been
  *  selected in the query. An example of expression is <code>"First name: {first_name},
  *  last name: {last_name}"</code>. This attribute is mandatory
  *  if <code>android:withClass</code> is not specified and ignored if <code>android:withClass</code>
  *  is specified</li>
  *  <li><code>android:withClass</code>: A fully qualified class name corresponding to an
  *  implementation of {@link Adapters.CursorTransformation}. Custom
  *  transformations cannot be used with
  *  {@link android.content.Context#isRestricted() restricted contexts}, for instance in
  *  an app widget This attribute is mandatory if <code>android:withExpression</code> is
  *  not specified</li>
  * </ul>
  * 
  * <h3>Example</h3>
  * <p>The following example defines a cursor adapter that queries all the contacts with
  * a phone number using the contacts content provider. Each contact is displayed with
  * its display name, its favorite status and its photo. To display photos, a custom data
  * binder is declared:</p>
  * 
  * <pre class="prettyprint">
  * &lt;cursor-adapter xmlns:android="http://schemas.android.com/apk/res/android"
  *     android:uri="content://com.android.contacts/contacts"
  *     android:selection="has_phone_number=1"
  *     android:layout="@layout/contact_item"&gt;
  *
  *     &lt;bind android:from="display_name" android:to="@id/name" android:as="string" /&gt;
  *     &lt;bind android:from="starred" android:to="@id/star" android:as="drawable"&gt;
  *         &lt;map android:fromValue="0" android:toValue="@android:drawable/star_big_off" /&gt;
  *         &lt;map android:fromValue="1" android:toValue="@android:drawable/star_big_on" /&gt;
  *     &lt;/bind&gt;
  *     &lt;bind android:from="_id" android:to="@id/name"
  *              android:as="com.google.android.test.adapters.ContactPhotoBinder" /&gt;
  *
  * &lt;/cursor-adapter&gt;
  * </pre>
  * 
  * <h3>Related APIs</h3>
  * <ul>
  *  <li>{@link Adapters#loadAdapter(android.content.Context, int, Object[])}</li>
  *  <li>{@link Adapters#loadCursorAdapter(android.content.Context, int, android.database.Cursor, Object[])}</li>
  *  <li>{@link Adapters#loadCursorAdapter(android.content.Context, int, String, Object[])}</li>
  *  <li>{@link Adapters.CursorBinder}</li>
  *  <li>{@link Adapters.CursorTransformation}</li>
  *  <li>{@link android.widget.CursorAdapter}</li>
  * </ul>
  * 
  * @see android.widget.Adapter
  * @see android.content.ContentProvider
  * 
  * attr ref android.R.styleable#CursorAdapter_layout
  * attr ref android.R.styleable#CursorAdapter_selection
  * attr ref android.R.styleable#CursorAdapter_sortOrder
  * attr ref android.R.styleable#CursorAdapter_uri
  * attr ref android.R.styleable#CursorAdapter_BindItem_as
  * attr ref android.R.styleable#CursorAdapter_BindItem_from
  * attr ref android.R.styleable#CursorAdapter_BindItem_to
  * attr ref android.R.styleable#CursorAdapter_MapItem_fromValue
  * attr ref android.R.styleable#CursorAdapter_MapItem_toValue
  * attr ref android.R.styleable#CursorAdapter_SelectItem_column
  * attr ref android.R.styleable#CursorAdapter_TransformItem_withClass
  * attr ref android.R.styleable#CursorAdapter_TransformItem_withExpression
  */
 public class Adapters {
     private static final String ADAPTER_CURSOR = "cursor-adapter";
     
     /**
      * <p>Interface used to bind a {@link android.database.Cursor} column to a View. This
      * interface can be used to provide bindings for data types not supported by the
      * standard implementation of {@link Adapters}.</p>
      * 
      * <p>A binder is provided with a cursor transformation which may or may not be used
      * to transform the value retrieved from the cursor. The transformation is guaranteed
      * to never be null so it's always safe to apply the transformation.</p>
      * 
      * <p>The binder is associated with a Context but can be re-used with multiple cursors.
      * As such, the implementation should make no assumption about the Cursor in use.</p>
      *
      * @see android.view.View 
      * @see android.database.Cursor
      * @see Adapters.CursorTransformation
      */
     public static abstract class CursorBinder {
         protected final Context mContext;
         protected final CursorTransformation mTransformation;
         
         public CursorBinder(Context context, CursorTransformation transformation) {
             mContext = context;
             mTransformation = transformation;
         }
         
         public abstract boolean bind(View view, Cursor cursor, int columnIndex);
     }
     
     /**
      * <p>Interface used to transform data coming out of a {@link android.database.Cursor}
      * before it is bound to a {@link android.view.View}.</p>
      * 
      * <p>Transformations are used to transform text-based data (in the form of a String),
      * or to transform data into a resource identifier. A default implementation is provided
      * to generate resource identifiers.</p>
      * 
      * @see android.database.Cursor
      * @see Adapters.CursorBinder
      */
     public static abstract class CursorTransformation {
         protected final Context mContext;
         
         public CursorTransformation(Context context) {
             mContext = context;
         }
         
         public abstract String transform(Cursor cursor, int columnIndex);
         
         public int transformToResource(Cursor cursor, int columnIndex) {
             return cursor.getInt(columnIndex);
         }
     }
     
     private static BaseAdapter loadAdapter(Context context, int id, String assertName, Object... parameters) {
         
         
         return null;
     }
     
     private static class XmlCursorAdapterParser {
         
         
         
         
     }
     
     private static interface ManagedAdapter {
         void load();
     }
     
     private static class XmlCursorAdapter extends SimpleCursorAdapter implements ManagedAdapter {
 
        
        
         @Override
         public void load() {
             
         }
         
     }
     
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
