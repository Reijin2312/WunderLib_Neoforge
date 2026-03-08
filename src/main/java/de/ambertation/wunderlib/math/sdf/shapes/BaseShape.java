package de.ambertation.wunderlib.math.sdf.shapes;

import de.ambertation.wunderlib.math.*;
import de.ambertation.wunderlib.math.sdf.SDF;
import de.ambertation.wunderlib.math.sdf.interfaces.MaterialProvider;
import de.ambertation.wunderlib.math.sdf.interfaces.Transformable;

import org.jetbrains.annotations.NotNull;

public abstract class BaseShape extends SDF implements MaterialProvider, Transformable {
    protected int materialIndex;

    @NotNull
    protected Transform transform;

    protected BaseShape(Transform t, int materialIndex) {
        super(0);
        this.transform = t;
        this.materialIndex = materialIndex;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + graphIndex + "]";
    }

    public Float3 getCenter() {
        return transform.center.blockAligned();
    }

    @Override
    public Bounds getBoundingBox() {
        return transform.getBoundingBoxWorldSpace(getParentTransformMatrix());
    }

    @Override
    public Bounds getLocalBoundingBox(Matrix4 m) {
        return transform.getBoundingBoxWorldSpace(getLocalTransform().asInvertedMatrix().mul(m));
    }

    @Override
    public int getMaterialIndex() {
        return materialIndex;
    }

    @Override
    public void setMaterialIndex(int newMaterialIndex) {
        this.materialIndex = newMaterialIndex;
    }


    //--------------------- Rotatable ---------------------
    public void rotate(double angle) {
        transform = transform.rotateBy(Quaternion.ofAxisAngle(Float3.Y_AXIS, angle));
    }

    //--------------------- Transformable ---------------------
    @Override
    public Transform getLocalTransform() {
        return transform;
    }

    @Override
    public Float3[] getCornersInWorldSpace(boolean blockAligned, Transform transform) {
        return transform.getCornersInWorldSpace(blockAligned, getParentTransformMatrix());
    }

    @Override
    public Float3[] getCornersAndCenterInWorldSpace(boolean blockAligned, Transform transform) {
        return transform.getCornersAndCenterInWorldSpace(blockAligned, getParentTransformMatrix());
    }

    public Float3 getCornerInWorldSpace(Bounds.Interpolate corner, boolean blockAligned, Transform transform) {
        return transform.getCornerInWorldSpace(corner, blockAligned, getParentTransformMatrix());
    }

    @Override
    public void setLocalTransform(Transform t) {
        transform = t;
    }

    @Override
    public boolean isOperation() {
        return false;
    }
}
