/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.Node;
import javax.media.j3d.NodeComponent;
import javax.media.j3d.Shape3D;
import javax.vecmath.Vector3f;

/**
 * Box is a geometry primitive created with a given length, width, and height.
 * It is centered at the origin. By default, it lies within the bounding
 * box, [-1,-1,-1] and [1,1,1].
 *
 * When a texture is applied to a box, it is map CCW like on a Cylinder.
 * A texture is mapped CCW from the back of the
 * body. The top and bottom faces are mapped such that the texture appears
 * front facing when the faces are rotated 90 toward the viewer.
 * <p>
 * By default all primitives with the same parameters share their
 * geometry (e.g., you can have 50 shperes in your scene, but the
 * geometry is stored only once). A change to one primitive will
 * effect all shared nodes.  Another implication of this
 * implementation is that the capabilities of the geometry are shared,
 * and once one of the shared nodes is live, the capabilities cannot
 * be set.  Use the GEOMETRY_NOT_SHARED flag if you do not wish to
 * share geometry among primitives with the same parameters.
 */

public class Box extends Primitive {

  /**
   * Used to designate the front side of the box when using
   * getShape().
   *
   * @see Box#getShape
   */
  public static final int FRONT = 0;

  /**
   * Used to designate the back side of the box when using
   * getShape().
   *
   * @see Box#getShape
   */
  public static final int BACK = 1;

  /**
   * Used to designate the right side of the box when using
   * getShape().
   *
   * @see Box#getShape
   */
  public static final int RIGHT = 2;

  /**
   * Used to designate the left side of the box when using
   * getShape().
   *
   * @see Box#getShape
   */
  public static final int LEFT = 3;

  /**
   * Used to designate the top side of the box when using
   * getShape().
   *
   * @see Box#getShape
   */
  public static final int TOP = 4;

  /**
   * Used to designate the bottom side of the box when using
   * getShape().
   *
   * @see Box#getShape
   */
  public static final int BOTTOM = 5;

  float xDim, yDim, zDim;

  int numTexUnit = 1;

  /**
   * Constructs a default box of 1.0 in all dimensions.
   * Normals are generated by default, texture coordinates are not.
   */

  public Box()
  {
    this(1.0f, 1.0f, 1.0f, GENERATE_NORMALS, null);
  }

  /**
   * Constructs a box of a given dimension and appearance.
   * Normals are generated by default, texture coordinates are not.
   *
   * @param xdim X-dimension size.
   * @param ydim Y-dimension size.
   * @param zdim Z-dimension size.
   * @param ap Appearance
   */

  public Box(float xdim, float ydim, float zdim, Appearance ap)
  {
    this(xdim, ydim, zdim, GENERATE_NORMALS, ap);
  }

  /**
   * Constructs a box of a given dimension, flags, and appearance.
   *
   * @param xdim X-dimension size.
   * @param ydim Y-dimension size.
   * @param zdim Z-dimension size.
   * @param primflags primitive flags.
   * @param ap Appearance
   */

  public Box(float xdim, float ydim, float zdim, int primflags,
	     Appearance ap, int numTexUnit) {
    int i;
    double sign;

    xDim = xdim;
    yDim = ydim;
    zDim = zdim;
    flags = primflags;
    this.numTexUnit = numTexUnit;
    boolean texCoordYUp = (flags & GENERATE_TEXTURE_COORDS_Y_UP) != 0;

    //Depending on whether normal inward bit is set.
    if ((flags & GENERATE_NORMALS_INWARD) != 0)
        sign = -1.0;
    else sign = 1.0;


//     TransformGroup objTrans = new TransformGroup();
//     objTrans.setCapability(ALLOW_CHILDREN_READ);
//     this.addChild(objTrans);

    Shape3D shape[] = new Shape3D[6];

    GeomBuffer cache = null;

    for (i = FRONT; i <= BOTTOM; i++){

	cache = getCachedGeometry(Primitive.BOX, xdim, ydim, zdim, i, i,
				  primflags);
	if (cache != null) {
// 	    System.out.println("using cached geometry i = " + i);
	    shape[i] = new Shape3D(cache.getComputedGeometry());
	    numVerts += cache.getNumVerts();
	    numTris += cache.getNumTris();
	}
	else {

	    GeomBuffer gbuf = new GeomBuffer(4, numTexUnit);

	    gbuf.begin(GeomBuffer.QUAD_STRIP);
	    for (int j = 0; j < 2; j++){
		gbuf.normal3d( (double) normals[i].x*sign,
			       (double) normals[i].y*sign,
			       (double) normals[i].z*sign);
                if (texCoordYUp) {
                   gbuf.texCoord2d(tcoords[i*8 + j*2], 1.0 - tcoords[i*8 + j*2 + 1]);
                }
                else {
                    gbuf.texCoord2d(tcoords[i*8 + j*2], tcoords[i*8 + j*2 + 1]);
                }

		gbuf.vertex3d( (double) verts[i*12 + j*3]*xdim,
			       (double) verts[i*12+ j*3 + 1]*ydim,
			       (double) verts[i*12+ j*3 + 2]*zdim );
	    }
	    for (int j = 3; j > 1; j--){
		gbuf.normal3d( (double) normals[i].x*sign,
			       (double) normals[i].y*sign,
			       (double) normals[i].z*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(tcoords[i*8 + j*2], 1.0 - tcoords[i*8 + j*2 + 1]);
                }
                else {
                    gbuf.texCoord2d(tcoords[i*8 + j*2], tcoords[i*8 + j*2 + 1]);
                }
		gbuf.vertex3d( (double) verts[i*12 + j*3]*xdim,
			       (double) verts[i*12+ j*3 + 1]*ydim,
			       (double) verts[i*12+ j*3 + 2]*zdim );
	    }
	    gbuf.end();
	    shape[i] = new Shape3D(gbuf.getGeom(flags));
	    numVerts = gbuf.getNumVerts();
	    numTris = gbuf.getNumTris();

	    if  ((primflags & Primitive.GEOMETRY_NOT_SHARED) == 0) {
		cacheGeometry(Primitive.BOX, xdim, ydim, zdim, i, i,
			      primflags, gbuf);
	    }
	}

      if ((flags & ENABLE_APPEARANCE_MODIFY) != 0) {
	  (shape[i]).setCapability(Shape3D.ALLOW_APPEARANCE_READ);
	  (shape[i]).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      }

      if ((flags & ENABLE_GEOMETRY_PICKING) != 0) {
          (shape[i]).setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      }

//       objTrans.addChild(shape[i]);
      this.addChild(shape[i]);
    }

    if (ap == null){
      setAppearance();
    }
    else setAppearance(ap);
  }

  public Box(float xdim, float ydim, float zdim, int primflags,
	     Appearance ap) {
    this(xdim, ydim, zdim, primflags, ap, 1);
  }

  /**
   *  Gets one of the faces (Shape3D) from the box that contains the
   *  geometry and appearance. This allows users to modify the
   *  appearance or geometry of individual parts.
   * @param partId The part to return.
   * @return The Shape3D object associated with the partID.  If an
   * invalid partId is passed in, null is returned.
   */

  @Override
  public Shape3D getShape(int partId) {
    if ((partId >= FRONT) && (partId <= BOTTOM))
// 	return (Shape3D)(((Group)getChild(0)).getChild(partId));
	return (Shape3D)getChild(partId);
    return null;
  }

  /**
   *  Sets appearance of the box. This will set each face of the
   *  box to the same appearance. To set each face's appearance
   *  separately, use getShape(partId) to get the
   *  individual shape and call shape.setAppearance(ap).
   */

  @Override
  public void setAppearance(Appearance ap){
//     ((Shape3D)((Group)getChild(0)).getChild(TOP)).setAppearance(ap);
//     ((Shape3D)((Group)getChild(0)).getChild(LEFT)).setAppearance(ap);
//     ((Shape3D)((Group)getChild(0)).getChild(RIGHT)).setAppearance(ap);
//     ((Shape3D)((Group)getChild(0)).getChild(FRONT)).setAppearance(ap);
//     ((Shape3D)((Group)getChild(0)).getChild(BACK)).setAppearance(ap);
//     ((Shape3D)((Group)getChild(0)).getChild(BOTTOM)).setAppearance(ap);
      ((Shape3D)getChild(TOP)).setAppearance(ap);
      ((Shape3D)getChild(LEFT)).setAppearance(ap);
      ((Shape3D)getChild(RIGHT)).setAppearance(ap);
      ((Shape3D)getChild(FRONT)).setAppearance(ap);
      ((Shape3D)getChild(BACK)).setAppearance(ap);
      ((Shape3D)getChild(BOTTOM)).setAppearance(ap);
  }

    /**
     * Gets the appearance of the specified part of the box.
     *
     * @param partId identifier for a given subpart of the box
     *
     * @return The appearance object associated with the partID.  If an
     * invalid partId is passed in, null is returned.
     *
     * @since Java 3D 1.2.1
     */
    @Override
    public Appearance getAppearance(int partId) {
	if (partId > BOTTOM || partId < FRONT) return null;
	return getShape(partId).getAppearance();
    }


  private static final float[] verts = {
    // front face
    1.0f, -1.0f,  1.0f,
    1.0f,  1.0f,  1.0f,
-1.0f,  1.0f,  1.0f,
-1.0f, -1.0f,  1.0f,
    // back face
-1.0f, -1.0f, -1.0f,
-1.0f,  1.0f, -1.0f,
    1.0f,  1.0f, -1.0f,
    1.0f, -1.0f, -1.0f,
    // right face
    1.0f, -1.0f, -1.0f,
    1.0f,  1.0f, -1.0f,
    1.0f,  1.0f,  1.0f,
    1.0f, -1.0f,  1.0f,
    // left face
-1.0f, -1.0f,  1.0f,
-1.0f,  1.0f,  1.0f,
-1.0f,  1.0f, -1.0f,
-1.0f, -1.0f, -1.0f,
    // top face
    1.0f,  1.0f,  1.0f,
    1.0f,  1.0f, -1.0f,
-1.0f,  1.0f, -1.0f,
-1.0f,  1.0f,  1.0f,
    // bottom face
-1.0f, -1.0f,  1.0f,
-1.0f, -1.0f, -1.0f,
    1.0f, -1.0f, -1.0f,
    1.0f, -1.0f,  1.0f,
  };

  private static final double[] tcoords = {
    // front
    1.0, 0.0,
    1.0, 1.0,
    0.0, 1.0,
    0.0, 0.0,
    // back
    1.0, 0.0,
    1.0, 1.0,
    0.0, 1.0,
    0.0, 0.0,
    //right
    1.0, 0.0,
    1.0, 1.0,
    0.0, 1.0,
    0.0, 0.0,
    // left
    1.0, 0.0,
    1.0, 1.0,
    0.0, 1.0,
    0.0, 0.0,
    // top
    1.0, 0.0,
    1.0, 1.0,
    0.0, 1.0,
    0.0, 0.0,
    // bottom
    0.0, 1.0,
    0.0, 0.0,
    1.0, 0.0,
    1.0, 1.0
  };


  private static final Vector3f[] normals = {
    new Vector3f( 0.0f,  0.0f,  1.0f),	// front face
    new Vector3f( 0.0f,  0.0f, -1.0f),	// back face
    new Vector3f( 1.0f,  0.0f,  0.0f),	// right face
    new Vector3f(-1.0f,  0.0f,  0.0f),	// left face
    new Vector3f( 0.0f,  1.0f,  0.0f),	// top face
    new Vector3f( 0.0f, -1.0f,  0.0f),	// bottom face
  };


    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * <code>cloneNode</code> should be overridden by any user subclassed
     * objects.  All subclasses must have their <code>cloneNode</code>
     * method consist of the following lines:
     * <P><blockquote><pre>
     *     public Node cloneNode(boolean forceDuplicate) {
     *         UserSubClass usc = new UserSubClass();
     *         usc.duplicateNode(this, forceDuplicate);
     *         return usc;
     *     }
     * </pre></blockquote>
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    @Override
    public Node cloneNode(boolean forceDuplicate) {
        Box b = new Box(xDim, yDim, zDim, flags, getAppearance());
        b.duplicateNode(this, forceDuplicate);
        return b;
    }

    /**
     * Copies all node information from <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.
     * <P>
     * For any <i>NodeComponent</i> objects
     * contained by the object being duplicated, each <i>NodeComponent</i>
     * object's <code>duplicateOnCloneTree</code> value is used to determine
     * whether the <i>NodeComponent</i> should be duplicated in the new node
     * or if just a reference to the current node should be placed in the
     * new node.  This flag can be overridden by setting the
     * <code>forceDuplicate</code> parameter in the <code>cloneTree</code>
     * method to <code>true</code>.
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    @Override
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
        super.duplicateNode(originalNode, forceDuplicate);
    }

    /**
     * Returns the X-dimension size of the Box
     *
     * @since Java 3D 1.2.1
     */
    public float getXdimension() {
	return xDim;
    }

    /**
     * Returns the Y-dimension size of the Box
     *
     * @since Java 3D 1.2.1
     */
    public float getYdimension() {
	return yDim;
    }

    /**
     * Returns the Z-dimension size of the Box
     *
     * @since Java 3D 1.2.1
     */
    public float getZdimension() {
	return zDim;
    }
}

