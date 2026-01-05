package de.ambertation.wunderlib.math.sdf;

import de.ambertation.wunderlib.WunderLib;
import de.ambertation.wunderlib.math.Bounds;
import de.ambertation.wunderlib.math.Float3;
import de.ambertation.wunderlib.math.Matrix4;
import de.ambertation.wunderlib.math.Transform;
import de.ambertation.wunderlib.math.sdf.interfaces.Transformable;
import de.ambertation.wunderlib.math.sdf.shapes.*;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyDispatchDataCodec;

import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public abstract class SDF {

    @FunctionalInterface
    public interface PlaceBlock {
        void place(Float3 pos, EvaluationData ed);
    }

    @FunctionalInterface
    public interface VisitBlock {
        void visit(Float3 pos, EvaluationData ed, boolean didPlace);
    }

    public static final class EvaluationData {
        double dist;
        SDF source;

        public EvaluationData() {
            this(Double.MAX_VALUE, new Empty());
        }

        public EvaluationData(double dist, SDF source) {
            this.dist = dist;
            this.source = source;
        }

        public double dist() {
            return dist;
        }

        public SDF source() {
            return source;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (EvaluationData) obj;
            return Double.doubleToLongBits(this.dist) == Double.doubleToLongBits(that.dist) &&
                    Objects.equals(this.source, that.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dist, source);
        }

        @Override
        public String toString() {
            return "EvaluationData[" +
                    "dist=" + dist + ", " +
                    "source=" + source + ']';
        }
    }

    protected SDF(int inputCount) {
        inputSlots = new SDF[inputCount];
    }

    //---------------------- TRANSFORMS ----------------------
    private Matrix4 rootTransform;

    public void setRootTransform(Matrix4 m) {
        if (parent == null) {
            rootTransform = m;
        } else {
            parent.setRootTransform(m);
        }
    }

    @NotNull
    public Matrix4 getRootTransform() {
        if (parent == null) {
            return rootTransform == null ? Matrix4.IDENTITY : rootTransform;
        }
        return parent.getRootTransform();
    }

    public Transform getLocalTransform() {
        return Transform.IDENTITY;
    }

    public Matrix4 getWorldTransformMatrix() {
        return getParentTransformMatrix().mul(getLocalTransform().asMatrix());
    }

    public Matrix4 getParentTransformMatrix() {
        if (parent == null) return rootTransform == null ? Matrix4.IDENTITY : rootTransform;
        return parent.getWorldTransformMatrix();
    }

    //---------------------- INPUT SLOTS ----------------------
    protected final SDF[] inputSlots;
    protected int graphIndex = 0;

    public int getInputSlotCount() {
        return inputSlots.length;
    }

    public boolean hasInputSlots() {
        return inputSlots.length > 0;
    }

    public SDF getSlot(int idx) {
        return inputSlots[idx];
    }

    public boolean hasInputs() {
        for (SDF inputSlot : inputSlots) {
            if (inputSlot != null && !(inputSlot instanceof Empty)) return true;
        }
        return false;
    }

    void setSlotSilent(int idx, SDF sdf) {
        if (inputSlots[idx] != sdf && inputSlots[idx] != null) {
            inputSlots[idx].setParent(null);
            inputSlots[idx].setGraphIndexRecursive(0);
        }


        inputSlots[idx] = sdf == null ? new Empty() : sdf;
        inputSlots[idx].setParent(this);
        if (idx == 0) {
            inputSlots[idx].setGraphIndexRecursive(graphIndex + 1);
        } else {
            inputSlots[idx].setGraphIndexRecursive(inputSlots[idx - 1].maxGraphIndex() + 1);
        }
    }

    public void setSlot(int idx, SDF sdf) {
        setSlotSilent(idx, sdf);
    }

    public int inputSlotIndex(SDF sdf) {
        for (int i = 0; i < inputSlots.length; i++) {
            if (inputSlots[i] == sdf) return i;
        }
        return -1;
    }

    public boolean replaceInputSlot(SDF currentSDF, SDF newSDF) {
        for (int i = 0; i < inputSlots.length; i++) {
            if (inputSlots[i] == currentSDF) {
                setSlot(i, newSDF);
                return true;
            }
        }

        return false;
    }

    private int setGraphIndexRecursive(int startIndex) {
        this.graphIndex = startIndex;
        for (int i = 0; i < inputSlots.length; i++) {
            startIndex = inputSlots[i].setGraphIndexRecursive(startIndex + 1);
        }
        return startIndex;
    }

    private int maxGraphIndex() {
        int idx = this.graphIndex;
        for (int i = 0; i < inputSlots.length; i++) {
            idx = Math.max(idx, inputSlots[i].maxGraphIndex());
        }
        return idx;
    }

    public int getGraphIndex() {
        return graphIndex;
    }

    public SDF getRoot() {
        if (parent == null) return this;
        return parent.getRoot();
    }

    public SDF getChildWithGraphIndex(int gIdx) {
        if (gIdx == graphIndex) return this;

        for (int i = 0; i < inputSlots.length; i++) {
            SDF s = inputSlots[i].getChildWithGraphIndex(gIdx);
            if (s != null) return s;
        }

        return null;
    }


    public boolean isEmpty() {
        return false;
    }

    //---------------------- BOUNDING BOX ----------------------
    public Bounds getBoundingBox() {
        Bounds b = Bounds.EMPTY;
        for (SDF sdf : inputSlots) {
            b = b.encapsulate(sdf.getBoundingBox());
        }
        return b;
    }

    public Bounds getLocalBoundingBox(Matrix4 m) {
        Bounds b = Bounds.EMPTY;

        for (SDF sdf : inputSlots) {
            Matrix4 mm = (sdf instanceof Transformable tf /*&& tf.isOperation()*/) ? tf.getLocalTransform()
                                                                                       .asMatrix()
                                                                                       .mul(m) : m;
            b = b.encapsulate(sdf.getLocalBoundingBox(mm));
        }
        return b;
    }

    public abstract Transform defaultTransform();


    //---------------------- PARENT HANDLING ----------------------
    private SDF parent;

    void setParent(SDF parent) {
        this.parent = parent;
    }

    public SDF getParent() {
        return parent;
    }

    //---------------------- EVAlUATION ----------------------
    public void dist(EvaluationData d, Float3 pos) {
        d.dist = dist(pos);
        d.source = this;
    }

    public void evaluate(PlaceBlock callback, VisitBlock visitor) {
        evaluate(getBoundingBox(), callback, visitor);
    }

    public void evaluate(Bounds bBox, PlaceBlock callback, VisitBlock visitor) {
        SDF.EvaluationData ed = new SDF.EvaluationData();
        double dist;
        if (bBox.volume() > 32 * 32 * 32) return;

        Float3 min = bBox.min.mul(2).round().div(2);
        Float3 max = bBox.max.mul(2).round().div(2);
        for (double xx = min.x - 2; xx < max.x + 2; xx++) {
            for (double xy = min.y - 2; xy < max.y + 2; xy++) {
                for (double xz = min.z - 2; xz < max.z + 2; xz++) {
                    final Float3 p = Float3.of(xx, xy, xz);
                    final Float3 pMid = p.sub(0.5).blockAligned();
                    this.dist(ed, p);
                    dist = Math.round(ed.dist() * 80) / 80.0;

                    boolean didPlace = false;
                    if (dist <= 0.5 && dist >= -0.5) {
//                        if (Math.abs(dist) < Float3.EPSILON) {
//                            callback.place(p, ed);
//                            if (visitor != null) visitor.visit(p, ed, true);
//                            continue;
//                        }

                        byte sign = 0;
                        for (Bounds.Interpolate offset : Bounds.Interpolate.CORNERS) {
                            Float3 pd = p.add(offset.t.sub(0.5));
                            double dd = this.dist(pd);
                            dd = Math.round(dd * 80) / 80.0;
                            // if (Math.abs(dd) < Float3.EPSILON) continue;
                            if (sign == 0) sign = dd < 0 ? (byte) -1 : (byte) 1;
                            else if ((dd < 0 ? (byte) -1 : (byte) 1) != sign) {
                                didPlace = true;
                                callback.place(pMid, ed);
                                if (visitor != null) visitor.visit(p, ed, true);
                                break;
                            }
//                            if (visitor != null)
//                                visitor.visit(pd, new EvaluationData(dd, ed.source), false);
                        }
                    }
                    if (!didPlace && visitor != null) visitor.visit(p, ed, false);
                }
            }
        }
    }


    //---------------------- ABSTRACT METHODS ----------------------

    public abstract double dist(Float3 pos);
    public abstract KeyDispatchDataCodec<? extends SDF> codec();


    //---------------------- SDF REGISTRY ----------------------
    public static final ResourceKey<Registry<MapCodec<? extends SDF>>> SDF_REGISTRY_KEY
            = ResourceKey.createRegistryKey(WunderLib.ID("sdf"));
    public static final Registry<MapCodec<? extends SDF>> SDF_REGISTRY
            = new RegistryBuilder<>(SDF_REGISTRY_KEY).sync(true).create();

    public static final Codec<SDF> CODEC = SDF_REGISTRY.byNameCodec()
                                                       .dispatch((sdf) -> sdf.codec().codec(), Function.identity());

    static void bootstrap(Registry<MapCodec<? extends SDF>> registry) {
        register(registry, "union", SDFUnion.CODEC);
        register(registry, "intersect", SDFIntersection.CODEC);
        register(registry, "dif", SDFDifference.CODEC);
        register(registry, "invert", SDFInvert.CODEC);

        register(registry, "empty", Empty.CODEC);
        register(registry, "sphere", Sphere.CODEC);
        register(registry, "box", Box.CODEC);
        register(registry, "cylinder", Cylinder.CODEC);
        register(registry, "ellipsoid", Ellipsoid.CODEC);
    }

    static MapCodec<? extends SDF> register(
            Registry<MapCodec<? extends SDF>> registry,
            String name,
            KeyDispatchDataCodec<? extends SDF> codec
    ) {
        return Registry.register(registry, WunderLib.ID(name), codec.codec());
    }

    @ApiStatus.Internal
    public static void ensureStaticallyLoaded() {
        bootstrap(SDF_REGISTRY);
    }
}

