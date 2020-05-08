 /*
  * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.gramath;
 
 
 /**
  * Immutable matrix with 4x4 float elements.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 
 public final class ImmutableMatrix4f extends Matrix4f
 {
     /** Serial version UID. */
     private static final long serialVersionUID = 1L;
 
    /** The identity matrix. */
    private static final ImmutableMatrix4f IDENTITY;
 
     /** The cached determinant. */
     private float determinant;
 
     /** If determinant is cached or not. */
     private boolean determinantCached = false;
 
     /** The cached identity state. */
     private boolean identity;
 
     /** If identity state is cached or not. */
     private boolean identityCached = false;
 
     /** The cached inverse matrix. */
     private ImmutableMatrix4f inverse = null;
 
     /** The cached cofactor matrix. */
     private ImmutableMatrix4f cofactor = null;
 
     /** The cached transpose matrix. */
     private ImmutableMatrix4f transpose = null;
 
 
     /**
      * Build the constant matrices
      */
 
     static {
         IDENTITY = new ImmutableMatrix4f();
         setIdentity(IDENTITY.m);
         IDENTITY.identity = true;
         IDENTITY.identityCached = true;
     }
 
 
     /**
      * Constructs an uninitialized matrix.
      */
 
     private ImmutableMatrix4f()
     {
         super();
     }
 
 
     /**
      * Constructs a new matrix with the specified values.
      *
      * @param values
      *            The matrix values (Must have at least 16 values)
      */
 
     public ImmutableMatrix4f(final float... values)
     {
         super(values);
     }
 
 
     /**
      * Constructs a new matrix with the elements from the specified matrix.
      *
      * @param matrix
      *            The matrix to copy the elements from.
      */
 
     public ImmutableMatrix4f(final Matrix4f matrix)
     {
         super(matrix);
     }
 
 
     /**
      * Constructs a new matrix with values read from the specified array
      * beginning at the given index.
      *
      * @param values
      *            The matrix values (Must have at least index+16 values)
      * @param index
      *            The start index
      */
 
     public ImmutableMatrix4f(final float[] values, final int index)
     {
         super(values, index);
     }
 
 
 
     /**
      * Returns the immutable identity matrix.
      *
      * @return The immutable identity matrix. Never null.
      */
 
     public static ImmutableMatrix4f identity()
     {
         return IDENTITY;
     }
 
 
     /**
      * @see java.lang.Object#clone()
      */
 
     @Override
     public ImmutableMatrix4f clone()
     {
         return new ImmutableMatrix4f(this);
     }
 
 
 
     /**
      * @see de.ailis.gramath.Matrix4f#newInstance()
      */
 
     @Override
     protected ImmutableMatrix4f newInstance()
     {
         return new ImmutableMatrix4f();
     }
 
 
     /**
      * Returns the determinant of the matrix. The result is cached for
      * speed-optimization.
      *
      * @return The determinant of the matrix.
      */
 
     @Override
     public float getDeterminant()
     {
         if (this.determinantCached) return this.determinant;
         this.determinant = Matrix4f.determinant(this.m);
         this.determinantCached = true;
         return this.determinant;
     }
 
 
     /**
      * Checks if this matrix is a identity matrix. The result is cached for
      * speed-optimization.
      *
      * @return True if matrix is identity matrix, false if not
      */
 
     @Override
     public boolean isIdentity()
     {
         if (this.identityCached) return this.identity;
         this.identity = Matrix4f.isIdentity(this.m);
         this.identityCached = true;
         return this.identity;
     }
 
 
     /**
      * Returns the transpose matrix of this matrix. The source matrix itself is
      * not modified. The transpose matrix is cached for speed-optimization.
      *
      * @return The transpose matrix. Never null.
      */
 
     @Override
     public ImmutableMatrix4f getTranspose()
     {
         if (this.transpose != null) return this.transpose;
         final ImmutableMatrix4f result = new ImmutableMatrix4f();
         transpose(this.m, result.m);
         this.transpose = result;
         return result;
     }
 
 
     /**
      * Returns the cofactor matrix. The source matrix itself is not
      * modified. The cofactor matrix is cached for speed-optimization.
      *
      * @return The cofactor matrix. Never null.
      */
 
     @Override
     public ImmutableMatrix4f getCofactor()
     {
         if (this.cofactor != null) return this.cofactor;
         final ImmutableMatrix4f result = new ImmutableMatrix4f();
         cofactor(this.m, result.m);
         this.cofactor = result;
         return result;
     }
 
 
     /**
      * Returns the adjoint matrix. The source matrix itself is not
      * modified. The adjoint matrix is cached for speed-optimization.
      *
      * @return The adjoint matrix. Never null.
      */
 
     @Override
     public ImmutableMatrix4f getAdjoint()
     {
         return getCofactor().getTranspose();
     }
 
 
     /**
      * Returns the inverted form of the matrix. The source matrix itself is not
      * modified. The inverted matrix is cached for speed-optimization.
      *
      * @return The inverted matrix. Never null.
      */
 
     @Override
     public ImmutableMatrix4f getInverse()
     {
         if (this.inverse != null) return this.inverse;
         final ImmutableMatrix4f result = new ImmutableMatrix4f();
         divide(getAdjoint().m, getDeterminant(), result.m);
         this.inverse = result;
         return result;
     }
 }
