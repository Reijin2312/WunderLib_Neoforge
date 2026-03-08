package de.ambertation.wunderlib.math.sdf.shapes;

import de.ambertation.wunderlib.math.Float3;
import de.ambertation.wunderlib.math.Transform;
import de.ambertation.wunderlib.math.sdf.SDF;
import de.ambertation.wunderlib.math.sdf.interfaces.Rotatable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

// https://iquilezles.org/articles/distfunctions/
public class Box extends BaseShape implements Rotatable {
    public static final Transform DEFAULT_TRANSFORM = Transform.of(Float3.of(0, 0, 0), Float3.of(8, 5, 5));
    public static final MapCodec<Box> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Transform.CODEC.fieldOf("transform").orElse(Transform.IDENTITY).forGetter(o -> o.transform),
                    Codec.INT.fieldOf("material").orElse(0).forGetter(BaseShape::getMaterialIndex)
            )
            .apply(instance, Box::new)
    );

    public static final KeyDispatchDataCodec<Box> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public Box(Transform t, int matIndex) {
        super(t, matIndex);
    }

    public Box(Float3 center, Float3 size) {
        this(Transform.of(center, size), 0);
    }


    @Override
    public double dist(Float3 pos) {
        pos = getParentTransformMatrix().inverted().transform(pos);
        Float3 q = pos.sub(getCenter()).unRotate(transform.rotation).abs().sub(getSize().sub(1).div(2));
        return q.max(0.0).length() + Math.min(Math.max(q.x, Math.max(q.y, q.z)), 0.0);
    }

    public Float3 getSize() {
        return transform.size;
    }

    @Override
    public Transform defaultTransform() {
        return DEFAULT_TRANSFORM;
    }
}
