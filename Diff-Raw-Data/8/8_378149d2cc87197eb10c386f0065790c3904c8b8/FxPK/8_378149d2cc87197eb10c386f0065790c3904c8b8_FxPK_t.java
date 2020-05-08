 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.content;
 
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.structure.TypeStorageMode;
 import com.flexive.shared.value.FxReference;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Collection;
 import java.util.ArrayList;
 
 /**
  * Primary key for FxContents
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxPK implements Serializable, Comparable<FxPK> {
     private static final Log LOG = LogFactory.getLog(FxPK.class);
     private static final long serialVersionUID = 8452775369399900087L;
 
     /**
      * Id to use for new instances (flags them as new)
      */
     public static final int NEW_ID = -1;
 
     /**
      * Constant to select the maximum available version
      */
     public static final int MAX = -1;
 
     /**
      * Constant to select the version whose workflow step is flagged as live
      *
      * @see com.flexive.shared.workflow.Step#isLiveStep()
      */
     public static final int LIVE = -2;
 
     protected long id;
     protected int version;
 
     /**
      * Ctor
      *
      * @param id      id
      * @param version version
      */
     public FxPK(long id, int version) {
         this.id = id;
         this.version = version;
         if (this.version < LIVE)
             throw new FxInvalidParameterException("version", LOG, "ex.content.pk.invalid.version", id, version).asRuntimeException();
     }
 
     /**
      * Ctor, version is initialized with <code>MAX</code>
      *
      * @param id id
      * @see #MAX
      */
     public FxPK(long id) {
         this.id = id;
         this.version = MAX;
     }
 
     /**
      * Constructor for new FxContents
      */
     public FxPK() {
         this.id = NEW_ID;
         this.version = 1;
     }
 
     /**
      * Is this primary key for a new FxContent?
      *
      * @return if this primary key is for a new FxContent
      */
     public boolean isNew() {
         return this.id == NEW_ID;
     }
 
     /**
      * Getter for the id
      *
      * @return id
      */
     public long getId() {
         return id;
     }
 
     /**
      * Getter for the version.
      * Can be an arbitrary number or a constant for maximum or live version
      *
      * @return version
      * @see #MAX
      * @see #LIVE
      */
     public int getVersion() {
         return version;
     }
 
     /**
      * Get the storage mode for this primary key.
      * This method is reserved for future uses when different storage modes will be implemented!
      *
      * @return storage mode
      */
     public TypeStorageMode getStorageMode() {
         return TypeStorageMode.Hierarchical;
     }
 
     /**
      * Create a new primary key
      *
      * @return a new primary key
      */
     public static FxPK createNewPK() {
         return new FxPK(-1);
     }
 
     /**
      * Does this primary key point to a distinct version or is it something like maximum or live version?
      *
      * @return distinct version
      */
     public boolean isDistinctVersion() {
         return this.version >= 0;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         String ver = "" + this.getVersion();
         if (getVersion() == LIVE)
             ver = "LIVE";
         else if (getVersion() == MAX)
             ver = "MAX";
         return (isNew() ? "NEW" : getId() + "." + ver);
     }
 
     /**
      * Construct a primary key from a String
      *
      * @param value string value
      * @return primary key
      * @throws IllegalArgumentException if the string does not represent a valid PK value
      * @see #toString()
      */
     public static FxPK fromString(String value) {
         if (StringUtils.isNumeric(value)) {
             return new FxPK(Long.parseLong(value));
         } else if (value == null || "NEW".equals(value) || value.indexOf('.') <= 0) {
             return new FxPK();
         }
         String[] pk = value.split("\\.");
         if (pk == null || pk.length != 2)
            throw new IllegalArgumentException("Invalid PK value: " + value);
         long _id, _ver;
         try {
             _id = Long.parseLong(pk[0]);
         } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
         }
         try {
             _ver = Integer.parseInt(pk[1]);
         } catch (NumberFormatException e) {
             if ("MAX".equals(pk[1]))
                 _ver = MAX;
             else if ("LIVE".equals(pk[1]))
                 _ver = LIVE;
             else
                throw new IllegalArgumentException("Illegal version: " + pk[1]);
         }
         return new FxPK(_id, (int) _ver);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         final int PRIME = 31;
         int result = 1;
         result = PRIME * result + (int) (id ^ (id >>> 32));
         result = PRIME * result + version;
         return result;
     }
 
     /**
      * {@inheritDoc}
      *
      * Note: FxPKs with generic version information like {@link FxPK#LIVE} or {@link FxPK#MAX}
      * will return "false" if they are checked for equality with FxPKs that contain a distinct
      * version information. Consider using {@link FxContent#matchesPk(FxPK)} if appropriate. 
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null || !(obj instanceof FxPK))
             return false;
         final FxPK other = (FxPK) obj;
         return id == other.id && version == other.version;
     }
 
 
     /**
      * <p>Extract a PK from the given object. Useful for scripting or JSF-related
      * calls where the actual value type may vary.</p>
      * <p>Current, following types for <code>value</code> are supported:
      * <ul>
      * <li>{@link com.flexive.shared.content.FxPK FxPK} - trivial</li>
      * <li>{@link com.flexive.shared.value.FxReference FxReference} - return the PK contained in the "best translation" for the current user</li>
      * <li>{@link com.flexive.shared.structure.FxSelectListItem FxSelectListItem} - use the item's ID as an object ID and return the PK</li>
      * <li>{@link Long} - return a new PK for the given object ID</li>
      * <li>{@link String} - return a new PK based on the given PK string representation</li>
      * </ul>
      * </p>
      *
      * @param value the value to be interpreted as an FxPK obejct
      * @return the primary key
      */
     public static FxPK fromObject(Object value) {
         if (value instanceof FxPK) {
             return (FxPK) value;
         } else if (value instanceof FxReference) {
             return ((FxReference) value).getBestTranslation();
         } else if (value instanceof FxSelectListItem) {
             return new FxPK(((FxSelectListItem) value).getId());
         } else if (value instanceof Long) {
             return new FxPK((Long) value);
         } else if (value instanceof String) {
             return fromString((String) value);
         }
         throw new FxInvalidParameterException("VALUE", LOG, "ex.content.pk.fromObject.invalid", value, value.getClass()).asRuntimeException();
     }
 
     public int compareTo(FxPK o) {
         return id > o.id ? 1 : id < o.id ? -1 : 0;
     }
 
     /**
      * Extracts the IDs of the given PK collection.
      *
      * @param pks   the PKs to be processed
      * @return      the IDs of the given PK collection.
      * @since 3.1
      */
     public static List<Long> getIds(Collection<FxPK> pks) {
         final List<Long> result = new ArrayList<Long>(pks.size());
         for (FxPK pk : pks) {
             result.add(pk.getId());
         }
         return result;
     }
 }
