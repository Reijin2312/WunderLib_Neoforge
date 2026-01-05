package de.ambertation.wunderlib.math.sdf;

import de.ambertation.wunderlib.math.Float3;
import de.ambertation.wunderlib.math.Transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

public class SDFIntersection extends SDFBinaryOperation {
    public static final MapCodec<SDFIntersection> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Transform.CODEC.fieldOf("transform").orElse(Transform.IDENTITY).forGetter(o -> o.transform),
                    SDF.CODEC.fieldOf("sdf_a").forGetter(b -> b.getFirst()),
                    SDF.CODEC.fieldOf("sdf_b").forGetter(b -> b.getSecond())
            )
            .apply(instance, SDFIntersection::new)
    );

    public static final KeyDispatchDataCodec<SDFIntersection> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public SDFIntersection(Transform t, SDF a, SDF b) {
        super(t, a, b);
    }

    public SDFIntersection(SDF a, SDF b) {
        this(Transform.IDENTITY, a, b);
    }

    @Override
    public double dist(Float3 pos) {
        return Math.max(getFirst().dist(pos), getSecond().dist(pos));
    }


    @Override
    public void dist(EvaluationData d, Float3 pos) {
        getFirst().dist(d, pos);
        final double d0 = d.dist;
        final SDF s0 = d.source();

        getSecond().dist(d, pos);
        if (d0 > d.dist) {
            d.dist = d0;
            d.source = s0;
        }
    }

    @Override
    public String toString() {
        return "(" + getFirst() + " & " + getSecond() + ")" + " [" + graphIndex + "]";
    }
}

