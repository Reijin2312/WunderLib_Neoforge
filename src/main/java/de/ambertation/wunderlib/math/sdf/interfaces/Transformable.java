package de.ambertation.wunderlib.math.sdf.interfaces;

import de.ambertation.wunderlib.math.Bounds;
import de.ambertation.wunderlib.math.Float3;
import de.ambertation.wunderlib.math.Matrix4;
import de.ambertation.wunderlib.math.Transform;

public interface Transformable extends RootedSDF {
    Float3[] getCornersInWorldSpace(boolean blockAligned, Transform localTransform);
    Float3[] getCornersAndCenterInWorldSpace(boolean blockAligned, Transform localTransform);
    Float3 getCornerInWorldSpace(Bounds.Interpolate corner, boolean blockAligned, Transform transform);
    Transform getLocalTransform();
    void setLocalTransform(Transform t);
    Matrix4 getParentTransformMatrix();

    default Float3[] getCornersInWorldSpace(boolean blockAligned) {
        return getCornersInWorldSpace(blockAligned, getLocalTransform());
    }

    default Float3[] getCornersAndCenterInWorldSpace(boolean blockAligned) {
        return getCornersAndCenterInWorldSpace(blockAligned, getLocalTransform());
    }

    default Float3 getCornerInWorldSpace(Bounds.Interpolate corner, boolean blockAligned) {
        return getCornerInWorldSpace(corner, blockAligned, getLocalTransform());
    }

    default Matrix4 getWorldTransformMatrix() {
        return getParentTransformMatrix().mul(getLocalTransform().asMatrix());
    }

    boolean isOperation();
}
