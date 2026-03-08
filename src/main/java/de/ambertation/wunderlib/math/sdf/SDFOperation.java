package de.ambertation.wunderlib.math.sdf;

import de.ambertation.wunderlib.math.*;
import de.ambertation.wunderlib.math.sdf.interfaces.Rotatable;
import de.ambertation.wunderlib.math.sdf.interfaces.Transformable;

public abstract class SDFOperation extends SDF implements Transformable, Rotatable {
    protected SDFOperation(Transform t, SDF sdf) {
        this(t, sdf, 0);
    }

    protected SDFOperation(Transform t, SDF sdf, int additionalInputSlots) {
        super(1 + additionalInputSlots);
        setSlotSilent(0, sdf);
        this.transform = t;
    }

    @Override
    public String toString() {
        return "(" + getFirst() + ")" + " [" + graphIndex + "]";
    }

    public SDF getFirst() {
        return getSlot(0);
    }

    public void setFirst(SDF a) {
        setSlot(0, a);
    }

    //--------------------- Transformable ---------------------
    protected Transform transform;

    @Override
    public Transform getLocalTransform() {
        return transform;
    }

    @Override
    public Float3[] getCornersInWorldSpace(boolean blockAligned, Transform transform) {
        Float3[] corners = new Float3[Bounds.Interpolate.CORNERS.length];
        for (Bounds.Interpolate corner : Bounds.Interpolate.CORNERS) {
            corners[corner.idx] = getCornerInWorldSpace(corner, blockAligned, transform);
        }
        return corners;
    }

    @Override
    public Float3[] getCornersAndCenterInWorldSpace(boolean blockAligned, Transform transform) {
        Float3[] corners = new Float3[Bounds.Interpolate.CORNERS_AND_CENTER.length];
        for (Bounds.Interpolate corner : Bounds.Interpolate.CORNERS_AND_CENTER) {
            corners[corner.idx] = getCornerInWorldSpace(corner, blockAligned, transform);
        }
        return corners;
    }

    public Float3 getCornerInWorldSpace(Bounds.Interpolate corner, boolean blockAligned, Transform transform) {
        Bounds b = getLocalBoundingBox(Matrix4.IDENTITY);
        return getParentTransformMatrix().mul(transform.asMatrix())
                                         .transform(blockAligned
                                                 ? b.getBlockAligned(corner)
                                                 : b.get(corner));
    }

    @Override
    public void setLocalTransform(Transform t) {
        transform = t;
    }


    @Override
    public boolean isOperation() {
        return true;
    }

    @Override
    public Transform defaultTransform() {
        return Transform.IDENTITY;
    }

    //--------------------- Rotatable ---------------------
    public void rotate(double angle) {
        transform = transform.rotateBy(Quaternion.ofAxisAngle(Float3.Y_AXIS, angle));
    }
}
