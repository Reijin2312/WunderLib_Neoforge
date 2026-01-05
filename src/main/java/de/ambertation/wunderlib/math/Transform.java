package de.ambertation.wunderlib.math;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class Transform {
    public static final Transform IDENTITY = new Transform(Float3.ZERO, Float3.IDENTITY, Quaternion.IDENTITY);
    public static final MapCodec<Transform> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Float3.CODEC.fieldOf("center").orElse(Float3.ZERO).forGetter(o -> o.center),
                    Float3.CODEC.fieldOf("size").orElse(Float3.IDENTITY).forGetter(o -> o.size),
                    Quaternion.CODEC.fieldOf("rotation").orElse(Quaternion.IDENTITY).forGetter(o -> o.rotation)
            )
            .apply(instance, Transform::new)
    );

    public final Float3 center;
    public final Float3 size;
    public final Quaternion rotation;

    protected Transform(Float3 center, Float3 size, Quaternion rotation) {
        this.center = center;
        this.size = size;
        this.rotation = rotation;
    }

    public static Transform of(Float3 center, Float3 size, Quaternion rotation) {
        return new Transform(center, size, rotation);
    }

    public static Transform of(Float3 center, Float3 size) {
        return new Transform(center, size, Quaternion.IDENTITY);
    }

    public static Transform ofTranslation(Float3 center) {
        return new Transform(center, Float3.IDENTITY, Quaternion.IDENTITY);
    }

    public static Transform of(Bounds b) {
        return new Transform(b.getCenter(), b.getSize(), Quaternion.IDENTITY);
    }

    public Bounds getBoundingBoxWorldSpace() {
        return getBoundingBoxWorldSpace(Matrix4.IDENTITY);//Bounds.ofBox(center, size).rotate(rotation);
    }

    public Bounds getBoundingBoxWorldSpace(Matrix4 parentTransform) {
        Bounds b = Bounds.EMPTY;
        for (Float3 c : getCornersInWorldSpace(false, parentTransform)) {
            b = b.encapsulate(c);
        }
        return b;
    }

    public Bounds getBoundingBoxUnrotated() {
        return Bounds.ofBox(center, size);
    }

    public Float3[] getCornersInWorldSpace() {
        return getCornersInWorldSpace(false);
    }

    public Float3[] getCornersInWorldSpace(boolean blockAligned) {
        Float3[] corners = new Float3[Bounds.Interpolate.CORNERS.length];
        for (int i = 0; i < Bounds.Interpolate.CORNERS.length; i++) {
            corners[i] = transform(Bounds.Interpolate.CORNERS[i].t.sub(0.5));
            if (blockAligned) corners[i] = corners[i].blockAligned();
        }

        return corners;
    }

    public Float3[] getCornersAndCenterInWorldSpace(boolean blockAligned, Matrix4 toWorldMatrix) {
        return toWorldMatrix.mul(asMatrix()).getUnitCubeCornersAndCenter(blockAligned);
    }

    public Float3[] getCornersInWorldSpace(boolean blockAligned, Matrix4 toWorldMatrix) {
        return toWorldMatrix.mul(asMatrix()).getUnitCubeCorners(blockAligned);
    }

    public Float3 getCornerInWorldSpace(Bounds.Interpolate corner, boolean blockAligned, Matrix4 toWorldMatrix) {
        return toWorldMatrix.mul(asMatrix()).getUnitCubeCorner(corner, blockAligned);
    }


    public Transform translate(Float3 offset) {
        if (offset == null) return this;
        return new Transform(center.add(offset), size, rotation);
    }

    public Transform translate(Vec3 offset) {
        if (offset == null) return this;
        return new Transform(center.add(offset), size, rotation);
    }

    public Transform translateInverted(Float3 offset) {
        if (offset == null) return this;
        return new Transform(center.sub(offset), size, rotation);
    }

    public Transform translateInverted(Vec3 offset) {
        if (offset == null) return this;
        return new Transform(center.sub(offset), size, rotation);
    }

    public Transform scaleBy(Float3 scale) {
        if (scale == null) return this;
        return new Transform(center, size.mul(scale), rotation);
    }

    public Transform setScale(Float3 scale) {
        if (scale == null) scale = Float3.IDENTITY;
        return new Transform(center, scale, rotation);
    }


    public Transform moveTo(Float3 newCenter) {
        if (newCenter == null) newCenter = Float3.ZERO;
        return new Transform(newCenter, size, rotation);
    }

    public Transform moveBy(Float3 offset) {
        if (offset == null) return this;
        return new Transform(center.add(offset), size, rotation);
    }

    public Transform rotateBy(Quaternion rotation) {
        if (rotation == null) return this;
        return new Transform(center, size, this.rotation.mul(rotation));
    }

    public Transform rotateBy(Float3 normalizedAxis, double angle) {
        if (normalizedAxis == null || angle == 0) return this;

        return new Transform(center, size, this.rotation.mul(Quaternion.ofAxisAngle(normalizedAxis, angle)));
    }

    public Transform setRotation(Quaternion rotation) {
        if (rotation == null) rotation = Quaternion.IDENTITY;
        return new Transform(center, size, rotation);
    }

    public Float3 transform(Float3 p) {
        return p.mul(size).rotate(rotation).add(center);
    }

    public Float3 unTransform(Float3 p) {
        return p.sub(center).unRotate(rotation).div(size);
    }

    public Transform addScale(double sx, double sy, double sz) {
        return new Transform(center, size.add(sx, sy, sz), rotation);
    }

    public Matrix4 asMatrix() {
        return Matrix4.ofTranslation(center)
                      .mul(Matrix4.ofRotation(rotation))
                      .mul(Matrix4.ofScale(size));
    }

    public Matrix4 asInvertedMatrix() {
        return Matrix4.ofScale(size.reciprocal())
                      .mul(Matrix4.ofRotation(rotation.inverted()))
                      .mul(Matrix4.ofTranslation(center.mul(-1)));
    }

    @Override
    public String toString() {
        return "Transform{" +
                "c=" + center +
                ", s=" + size +
                ", r=" + rotation +
                '}';
    }

    public void serializeToNetwork(FriendlyByteBuf buf) {
        center.serializeToNetwork(buf);
        size.serializeToNetwork(buf);
        rotation.serializeToNetwork(buf);
    }

    public static Transform deserializeFromNetwork(FriendlyByteBuf buf) {
        Float3 c = Float3.deserializeFromNetwork(buf);
        Float3 s = Float3.deserializeFromNetwork(buf);
        Quaternion r = Quaternion.deserializeFromNetwork(buf);
        return new Transform(c, s, r);
    }
}

