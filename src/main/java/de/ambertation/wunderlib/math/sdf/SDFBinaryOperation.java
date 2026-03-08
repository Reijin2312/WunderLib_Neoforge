package de.ambertation.wunderlib.math.sdf;

import de.ambertation.wunderlib.math.Transform;

public abstract class SDFBinaryOperation extends SDFOperation {
    public SDFBinaryOperation(Transform t, SDF a, SDF b) {
        super(t, a, 1);
        setSlotSilent(1, b);
    }

    @Override
    public String toString() {
        return "(" + getFirst() + ", " + getSecond() + ")" + " [" + graphIndex + "]";
    }

    public SDF getSecond() {
        return getSlot(1);
    }

    public void setSecond(SDF b) {
        setSlot(1, b);
    }
}
