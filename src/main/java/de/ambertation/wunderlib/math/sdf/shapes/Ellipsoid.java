package de.ambertation.wunderlib.math.sdf.shapes;

import de.ambertation.wunderlib.math.Float3;
import de.ambertation.wunderlib.math.Transform;
import de.ambertation.wunderlib.math.sdf.SDF;
import de.ambertation.wunderlib.math.sdf.interfaces.Rotatable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

//based on https://iquilezles.org/articles/ellipsoids/
public class Ellipsoid extends BaseShape implements Rotatable {
    public static final Transform DEFAULT_TRANSFORM = Box.DEFAULT_TRANSFORM;
    public static final MapCodec<Ellipsoid> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Transform.CODEC.fieldOf("transform").orElse(Transform.IDENTITY).forGetter(o -> o.transform),
                    Codec.INT.fieldOf("material").orElse(0).forGetter(BaseShape::getMaterialIndex)
            )
            .apply(instance, Ellipsoid::new)
    );

    public static final KeyDispatchDataCodec<Ellipsoid> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public Ellipsoid(Transform t, int matIndex) {
        super(t, matIndex);
    }

    public Ellipsoid(Transform t) {
        this(t, 0);
    }

    public Ellipsoid(Float3 center, Float3 size) {
        this(Transform.of(center, size), 0);
    }

    @Override
    public double dist(Float3 pos) {
        pos = getParentTransformMatrix().inverted().transform(pos);

        Float3 size = getSize().sub(1);
        pos = pos.sub(getCenter());
        double k1 = pos.div(size).length();
        double k2 = pos.div(size.square()).length();

        return k1 * (k1 - 1.0) / k2;
    }

    public Float3 getSize() {
        return transform.size;
    }

    @Override
    public Transform defaultTransform() {
        return DEFAULT_TRANSFORM;
    }
}
