package de.ambertation.wunderlib.math;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public final class Quaternion {
    public static final Quaternion IDENTITY = new Quaternion(1, Float3.ZERO);
    public static final Quaternion ZERO = new Quaternion(0, Float3.ZERO);

    public static final MapCodec<Quaternion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Codec.FLOAT.fieldOf("w").forGetter(o -> (float) o.w),
                    Codec.FLOAT.fieldOf("i").forGetter(o -> (float) o.v.x),
                    Codec.FLOAT.fieldOf("j").forGetter(o -> (float) o.v.y),
                    Codec.FLOAT.fieldOf("k").forGetter(o -> (float) o.v.z)
            )
            .apply(instance, Quaternion::of)
    );


    public final double w;
    public final Float3 v;

    public Quaternion(double real, Float3 imaginary) {
        this.w = real;
        this.v = imaginary;
    }

    public static Quaternion of(double w, double i, double j, double k) {
        return new Quaternion(w, Float3.of(i, j, k));
    }

    public static Quaternion of(double real, Float3 imaginary) {
        return new Quaternion(real, imaginary);
    }

    public static Quaternion of(Float3 imaginary) {
        return new Quaternion(0, imaginary);
    }

    public static Quaternion between(Float3 start, Float3 target) {
        double angle = start.angleTo(target);
        Float3 axis = start.cross(target).normalized();
        return ofAxisAngle(axis, angle);
    }

    public static Quaternion ofAxisAngle(Float3 normalizedAxis, double angle) {
        angle /= 2;
        return new Quaternion(Math.cos(angle), normalizedAxis.mul(Math.sin(angle)));
    }

    //https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
    public Float3 toEuler() {
        // roll (x-axis rotation)
        double sinr_cosp = 2 * (this.w * this.v.x + this.v.y * this.v.z);
        double cosr_cosp = 1 - 2 * (this.v.x * this.v.x + this.v.y * this.v.y);
        double x = Math.atan2(sinr_cosp, cosr_cosp);

        // pitch (y-axis rotation)
        double y;
        double sinp = 2 * (this.w * this.v.y - this.v.z * this.v.x);
        if (Math.abs(sinp) >= 1)
            y = Math.PI / 2 * Math.signum(sinp); // use 90 degrees if out of range
        else
            y = Math.asin(sinp);

        // yaw (z-axis rotation)
        double siny_cosp = 2 * (this.w * this.v.z + this.v.x * this.v.y);
        double cosy_cosp = 1 - 2 * (this.v.y * this.v.y + this.v.z * this.v.z);
        double z = Math.atan2(siny_cosp, cosy_cosp);

        return Float3.of(x, y, z);
    }

    public Quaternion conjugate() {
        return new Quaternion(w, v.mul(-1));
    }

    public Quaternion mul(final Quaternion q) {
        final double w = this.w * q.w - v.x * q.v.x - v.y * q.v.y - v.z * q.v.z;
        final double x = this.w * q.v.x + v.x * q.w + v.y * q.v.z - v.z * q.v.y;
        final double y = this.w * q.v.y - v.x * q.v.z + v.y * q.w + v.z * q.v.x;
        final double z = this.w * q.v.z + v.x * q.v.y - v.y * q.v.x + v.z * q.w;

        return new Quaternion(w, Float3.of(x, y, z));
    }

    public Quaternion mul(Float3 v) {
        return mul(Quaternion.of(v));
    }

    public Quaternion mul(double d) {
        return new Quaternion(w * d, v.mul(d));
    }

    public Quaternion div(double d) {
        return new Quaternion(w / d, v.div(d));
    }

    public double dot(final Quaternion q) {
        return w * q.w + v.dot(q.v);
    }

    public Quaternion add(final Quaternion q) {
        return new Quaternion(w + q.w, v.add(q.v));
    }

    public Quaternion sub(final Quaternion q) {
        return new Quaternion(w - q.w, v.sub(q.v));
    }

    public double lengthSquare() {
        return w * w + v.lengthSquare();
    }

    public double length() {
        return Math.sqrt(lengthSquare());
    }

    public Quaternion normalized() {
        final double len = lengthSquare();
        if (len < Float3.EPSILON) return ZERO;

        return this.div(Math.sqrt(len));
    }

    public Quaternion inverted() {
        final double lenSquare = lengthSquare();
        if (lenSquare < Float3.EPSILON) {
            return Quaternion.ZERO;
        }

        return new Quaternion(w / lenSquare, v.div(-lenSquare));
    }

    public Float3 rotate(Float3 p) {
        return this.mul(p).mul(this.inverted()).v;
    }

    public Float3 unRotate(Float3 p) {
        return this.inverted().mul(p).mul(this).v;
    }

    public boolean isUnit() {
        return Math.abs(length() - 1) > Float3.EPSILON;
    }

    public boolean isImaginary() {
        return Math.abs(w) < Float3.EPSILON;
    }

    public boolean isReal() {
        return v.lengthSquare() < Float3.EPSILON;
    }

    public Matrix4 asMatrix() {
        return Matrix4.ofRotation(this.normalized());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quaternion that = (Quaternion) o;
        return Math.abs(that.w - w) < Float3.EPSILON && v.equals(that.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(w, v);
    }


    @Override
    public String toString() {
        return "(" + Float3.toString(w) + " + "
                + Float3.toString(v.x) + "i + " + Float3.toString(v.y) + "j + " + Float3.toString(v.z) + "k)";
    }

    public void serializeToNetwork(FriendlyByteBuf buf) {
        buf.writeDouble(w);
        v.serializeToNetwork(buf);
    }

    public static Quaternion deserializeFromNetwork(FriendlyByteBuf buf) {
        double w = buf.readDouble();
        Float3 v = Float3.deserializeFromNetwork(buf);
        return Quaternion.of(w, v);
    }
}
