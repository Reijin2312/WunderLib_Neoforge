package de.ambertation.wunderlib.math;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class Float4 {
    public static final double EPSILON = 1.e-10;
    public static final Float4 ZERO = Float4.of(0);
    public static final Float4 IDENTITY = Float4.of(1);
    public static final Float4 X_AXIS = Float4.ofDirection(1, 0, 0);
    public static final Float4 Y_AXIS = Float4.ofDirection(0, 1, 0);
    public static final Float4 Z_AXIS = Float4.ofDirection(0, 0, 1);

    public static final MapCodec<Float4> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Codec.FLOAT.fieldOf("x").forGetter(o -> (float) o.x),
                    Codec.FLOAT.fieldOf("y").forGetter(o -> (float) o.y),
                    Codec.FLOAT.fieldOf("z").forGetter(o -> (float) o.z),
                    Codec.FLOAT.fieldOf("w").forGetter(o -> (float) o.w)
            )
            .apply(instance, Float4::new)
    );
    public final double x;
    public final double y;
    public final double z;
    public final double w;

    public Float4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Float4 ofDirection(double xyz) {
        return new Float4(xyz, xyz, xyz, 0);
    }

    public static Float4 ofPoint(double xyz) {
        return new Float4(xyz, xyz, xyz, 1);
    }

    public static Float4 of(double xyzw) {
        return new Float4(xyzw, xyzw, xyzw, xyzw);
    }

    public static Float4 of(double x, double y, double z, double w) {
        return new Float4(x, y, z, w);
    }

    public static Float4 ofDirection(double x, double y, double z) {
        return new Float4(x, y, z, 0);
    }

    public static Float4 ofPoint(double x, double y, double z) {
        return new Float4(x, y, z, 1);
    }

    public static Float4 ofDirection(Float3 dir) {
        return new Float4(dir.x, dir.y, dir.z, 0);
    }

    public static Float4 ofPoint(Float3 dir) {
        return new Float4(dir.x, dir.y, dir.z, 1);
    }

    public Float4 div(Float4 p) {
        return new Float4(x / p.x, y / p.y, z / p.z, w / p.w);
    }

    public Float4 mul(Float4 p) {
        return new Float4(x * p.x, y * p.y, z * p.z, w * p.w);
    }

    public double dot(Float4 p) {
        return x * p.x + y * p.y + z * p.z + w * p.w;
    }

    public Float4 square() {
        return new Float4(x * x, y * y, z * z, w * w);
    }

    public Float4 add(Float4 p) {
        return new Float4(x + p.x, y + p.y, z + p.z, w + p.w);
    }

    public Float4 add(double d) {
        return new Float4(x + d, y + d, z + d, w + d);
    }

    public Float4 add(double dx, double dy, double dz, double dw) {
        return new Float4(x + dx, y + dy, z + dz, w + dw);
    }

    public Float4 sub(double d) {
        return new Float4(x - d, y - d, z - d, w - d);
    }

    public Float4 sub(double dx, double dy, double dz, double dw) {
        return new Float4(x - dx, y - dy, z - dz, w - dw);
    }

    public Float4 sub(Float4 p) {
        return new Float4(x - p.x, y - p.y, z - p.z, w - p.w);
    }

    public Float4 mul(double d) {
        return new Float4(x * d, y * d, z * d, w * d);
    }

    public Float4 div(double d) {
        return new Float4(x / d, y / d, z / d, w / d);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public double lengthSquare() {
        return x * x + y * y + z * z + w * w;
    }


    public Float4 normalized() {
        double d = length();
        return new Float4(x / d, y / d, z / d, w / d);
    }


    @Override
    public String toString() {
        return "(" + Float3.toString(x) + ", " + Float3.toString(y) + ", " + Float3.toString(z) + ", " + Float3.toString(
                w) + ")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Float4 pos = (Float4) o;
        return Math.abs(pos.x - x) < EPSILON
                && Math.abs(pos.y - y) < EPSILON
                && Math.abs(pos.z - z) < EPSILON
                && Math.abs(pos.w - w) < EPSILON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, w);
    }


    public void serializeToNetwork(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(w);
    }

    public static Float4 deserializeFromNetwork(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        double w = buf.readDouble();
        return Float4.of(x, y, z, w);
    }

    //-------------------------------------------- SWIVELS (@formatter:off) --------------------------------------------
    //Scalar -----------
    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public double w() { return w; }


    //2D -----------
    public Float2 xx() { return new Float2(x, x); }
    public Float2 xy() { return new Float2(x, y); }
    public Float2 xz() { return new Float2(x, z); }
    public Float2 xw() { return new Float2(x, w); }
    public Float2 yx() { return new Float2(y, x); }
    public Float2 yy() { return new Float2(y, y); }
    public Float2 yz() { return new Float2(y, z); }
    public Float2 yw() { return new Float2(y, w); }
    public Float2 zx() { return new Float2(z, x); }
    public Float2 zy() { return new Float2(z, y); }
    public Float2 zz() { return new Float2(z, z); }
    public Float2 zw() { return new Float2(z, w); }
    public Float2 wx() { return new Float2(w, x); }
    public Float2 wy() { return new Float2(w, y); }
    public Float2 wz() { return new Float2(w, z); }
    public Float2 ww() { return new Float2(w, w); }


    //3D -----------
    public Float3 xxx() { return new Float3(x, x, x); }
    public Float3 xxy() { return new Float3(x, x, y); }
    public Float3 xxz() { return new Float3(x, x, z); }
    public Float3 xxw() { return new Float3(x, x, w); }
    public Float3 xyx() { return new Float3(x, y, x); }
    public Float3 xyy() { return new Float3(x, y, y); }
    public Float3 xyz() { return new Float3(x, y, z); }
    public Float3 xyw() { return new Float3(x, y, w); }
    public Float3 xzx() { return new Float3(x, z, x); }
    public Float3 xzy() { return new Float3(x, z, y); }
    public Float3 xzz() { return new Float3(x, z, z); }
    public Float3 xzw() { return new Float3(x, z, w); }
    public Float3 xwx() { return new Float3(x, w, x); }
    public Float3 xwy() { return new Float3(x, w, y); }
    public Float3 xwz() { return new Float3(x, w, z); }
    public Float3 xww() { return new Float3(x, w, w); }
    public Float3 yxx() { return new Float3(y, x, x); }
    public Float3 yxy() { return new Float3(y, x, y); }
    public Float3 yxz() { return new Float3(y, x, z); }
    public Float3 yxw() { return new Float3(y, x, w); }
    public Float3 yyx() { return new Float3(y, y, x); }
    public Float3 yyy() { return new Float3(y, y, y); }
    public Float3 yyz() { return new Float3(y, y, z); }
    public Float3 yyw() { return new Float3(y, y, w); }
    public Float3 yzx() { return new Float3(y, z, x); }
    public Float3 yzy() { return new Float3(y, z, y); }
    public Float3 yzz() { return new Float3(y, z, z); }
    public Float3 yzw() { return new Float3(y, z, w); }
    public Float3 ywx() { return new Float3(y, w, x); }
    public Float3 ywy() { return new Float3(y, w, y); }
    public Float3 ywz() { return new Float3(y, w, z); }
    public Float3 yww() { return new Float3(y, w, w); }
    public Float3 zxx() { return new Float3(z, x, x); }
    public Float3 zxy() { return new Float3(z, x, y); }
    public Float3 zxz() { return new Float3(z, x, z); }
    public Float3 zxw() { return new Float3(z, x, w); }
    public Float3 zyx() { return new Float3(z, y, x); }
    public Float3 zyy() { return new Float3(z, y, y); }
    public Float3 zyz() { return new Float3(z, y, z); }
    public Float3 zyw() { return new Float3(z, y, w); }
    public Float3 zzx() { return new Float3(z, z, x); }
    public Float3 zzy() { return new Float3(z, z, y); }
    public Float3 zzz() { return new Float3(z, z, z); }
    public Float3 zzw() { return new Float3(z, z, w); }
    public Float3 zwx() { return new Float3(z, w, x); }
    public Float3 zwy() { return new Float3(z, w, y); }
    public Float3 zwz() { return new Float3(z, w, z); }
    public Float3 zww() { return new Float3(z, w, w); }
    public Float3 wxx() { return new Float3(w, x, x); }
    public Float3 wxy() { return new Float3(w, x, y); }
    public Float3 wxz() { return new Float3(w, x, z); }
    public Float3 wxw() { return new Float3(w, x, w); }
    public Float3 wyx() { return new Float3(w, y, x); }
    public Float3 wyy() { return new Float3(w, y, y); }
    public Float3 wyz() { return new Float3(w, y, z); }
    public Float3 wyw() { return new Float3(w, y, w); }
    public Float3 wzx() { return new Float3(w, z, x); }
    public Float3 wzy() { return new Float3(w, z, y); }
    public Float3 wzz() { return new Float3(w, z, z); }
    public Float3 wzw() { return new Float3(w, z, w); }
    public Float3 wwx() { return new Float3(w, w, x); }
    public Float3 wwy() { return new Float3(w, w, y); }
    public Float3 wwz() { return new Float3(w, w, z); }
    public Float3 www() { return new Float3(w, w, w); }


    //4D -----------
    public Float4 xxxx() { return new Float4(x, x, x, x); }
    public Float4 xxxy() { return new Float4(x, x, x, y); }
    public Float4 xxxz() { return new Float4(x, x, x, z); }
    public Float4 xxxw() { return new Float4(x, x, x, w); }
    public Float4 xxyx() { return new Float4(x, x, y, x); }
    public Float4 xxyy() { return new Float4(x, x, y, y); }
    public Float4 xxyz() { return new Float4(x, x, y, z); }
    public Float4 xxyw() { return new Float4(x, x, y, w); }
    public Float4 xxzx() { return new Float4(x, x, z, x); }
    public Float4 xxzy() { return new Float4(x, x, z, y); }
    public Float4 xxzz() { return new Float4(x, x, z, z); }
    public Float4 xxzw() { return new Float4(x, x, z, w); }
    public Float4 xxwx() { return new Float4(x, x, w, x); }
    public Float4 xxwy() { return new Float4(x, x, w, y); }
    public Float4 xxwz() { return new Float4(x, x, w, z); }
    public Float4 xxww() { return new Float4(x, x, w, w); }
    public Float4 xyxx() { return new Float4(x, y, x, x); }
    public Float4 xyxy() { return new Float4(x, y, x, y); }
    public Float4 xyxz() { return new Float4(x, y, x, z); }
    public Float4 xyxw() { return new Float4(x, y, x, w); }
    public Float4 xyyx() { return new Float4(x, y, y, x); }
    public Float4 xyyy() { return new Float4(x, y, y, y); }
    public Float4 xyyz() { return new Float4(x, y, y, z); }
    public Float4 xyyw() { return new Float4(x, y, y, w); }
    public Float4 xyzx() { return new Float4(x, y, z, x); }
    public Float4 xyzy() { return new Float4(x, y, z, y); }
    public Float4 xyzz() { return new Float4(x, y, z, z); }
    public Float4 xyzw() { return new Float4(x, y, z, w); }
    public Float4 xywx() { return new Float4(x, y, w, x); }
    public Float4 xywy() { return new Float4(x, y, w, y); }
    public Float4 xywz() { return new Float4(x, y, w, z); }
    public Float4 xyww() { return new Float4(x, y, w, w); }
    public Float4 xzxx() { return new Float4(x, z, x, x); }
    public Float4 xzxy() { return new Float4(x, z, x, y); }
    public Float4 xzxz() { return new Float4(x, z, x, z); }
    public Float4 xzxw() { return new Float4(x, z, x, w); }
    public Float4 xzyx() { return new Float4(x, z, y, x); }
    public Float4 xzyy() { return new Float4(x, z, y, y); }
    public Float4 xzyz() { return new Float4(x, z, y, z); }
    public Float4 xzyw() { return new Float4(x, z, y, w); }
    public Float4 xzzx() { return new Float4(x, z, z, x); }
    public Float4 xzzy() { return new Float4(x, z, z, y); }
    public Float4 xzzz() { return new Float4(x, z, z, z); }
    public Float4 xzzw() { return new Float4(x, z, z, w); }
    public Float4 xzwx() { return new Float4(x, z, w, x); }
    public Float4 xzwy() { return new Float4(x, z, w, y); }
    public Float4 xzwz() { return new Float4(x, z, w, z); }
    public Float4 xzww() { return new Float4(x, z, w, w); }
    public Float4 xwxx() { return new Float4(x, w, x, x); }
    public Float4 xwxy() { return new Float4(x, w, x, y); }
    public Float4 xwxz() { return new Float4(x, w, x, z); }
    public Float4 xwxw() { return new Float4(x, w, x, w); }
    public Float4 xwyx() { return new Float4(x, w, y, x); }
    public Float4 xwyy() { return new Float4(x, w, y, y); }
    public Float4 xwyz() { return new Float4(x, w, y, z); }
    public Float4 xwyw() { return new Float4(x, w, y, w); }
    public Float4 xwzx() { return new Float4(x, w, z, x); }
    public Float4 xwzy() { return new Float4(x, w, z, y); }
    public Float4 xwzz() { return new Float4(x, w, z, z); }
    public Float4 xwzw() { return new Float4(x, w, z, w); }
    public Float4 xwwx() { return new Float4(x, w, w, x); }
    public Float4 xwwy() { return new Float4(x, w, w, y); }
    public Float4 xwwz() { return new Float4(x, w, w, z); }
    public Float4 xwww() { return new Float4(x, w, w, w); }
    public Float4 yxxx() { return new Float4(y, x, x, x); }
    public Float4 yxxy() { return new Float4(y, x, x, y); }
    public Float4 yxxz() { return new Float4(y, x, x, z); }
    public Float4 yxxw() { return new Float4(y, x, x, w); }
    public Float4 yxyx() { return new Float4(y, x, y, x); }
    public Float4 yxyy() { return new Float4(y, x, y, y); }
    public Float4 yxyz() { return new Float4(y, x, y, z); }
    public Float4 yxyw() { return new Float4(y, x, y, w); }
    public Float4 yxzx() { return new Float4(y, x, z, x); }
    public Float4 yxzy() { return new Float4(y, x, z, y); }
    public Float4 yxzz() { return new Float4(y, x, z, z); }
    public Float4 yxzw() { return new Float4(y, x, z, w); }
    public Float4 yxwx() { return new Float4(y, x, w, x); }
    public Float4 yxwy() { return new Float4(y, x, w, y); }
    public Float4 yxwz() { return new Float4(y, x, w, z); }
    public Float4 yxww() { return new Float4(y, x, w, w); }
    public Float4 yyxx() { return new Float4(y, y, x, x); }
    public Float4 yyxy() { return new Float4(y, y, x, y); }
    public Float4 yyxz() { return new Float4(y, y, x, z); }
    public Float4 yyxw() { return new Float4(y, y, x, w); }
    public Float4 yyyx() { return new Float4(y, y, y, x); }
    public Float4 yyyy() { return new Float4(y, y, y, y); }
    public Float4 yyyz() { return new Float4(y, y, y, z); }
    public Float4 yyyw() { return new Float4(y, y, y, w); }
    public Float4 yyzx() { return new Float4(y, y, z, x); }
    public Float4 yyzy() { return new Float4(y, y, z, y); }
    public Float4 yyzz() { return new Float4(y, y, z, z); }
    public Float4 yyzw() { return new Float4(y, y, z, w); }
    public Float4 yywx() { return new Float4(y, y, w, x); }
    public Float4 yywy() { return new Float4(y, y, w, y); }
    public Float4 yywz() { return new Float4(y, y, w, z); }
    public Float4 yyww() { return new Float4(y, y, w, w); }
    public Float4 yzxx() { return new Float4(y, z, x, x); }
    public Float4 yzxy() { return new Float4(y, z, x, y); }
    public Float4 yzxz() { return new Float4(y, z, x, z); }
    public Float4 yzxw() { return new Float4(y, z, x, w); }
    public Float4 yzyx() { return new Float4(y, z, y, x); }
    public Float4 yzyy() { return new Float4(y, z, y, y); }
    public Float4 yzyz() { return new Float4(y, z, y, z); }
    public Float4 yzyw() { return new Float4(y, z, y, w); }
    public Float4 yzzx() { return new Float4(y, z, z, x); }
    public Float4 yzzy() { return new Float4(y, z, z, y); }
    public Float4 yzzz() { return new Float4(y, z, z, z); }
    public Float4 yzzw() { return new Float4(y, z, z, w); }
    public Float4 yzwx() { return new Float4(y, z, w, x); }
    public Float4 yzwy() { return new Float4(y, z, w, y); }
    public Float4 yzwz() { return new Float4(y, z, w, z); }
    public Float4 yzww() { return new Float4(y, z, w, w); }
    public Float4 ywxx() { return new Float4(y, w, x, x); }
    public Float4 ywxy() { return new Float4(y, w, x, y); }
    public Float4 ywxz() { return new Float4(y, w, x, z); }
    public Float4 ywxw() { return new Float4(y, w, x, w); }
    public Float4 ywyx() { return new Float4(y, w, y, x); }
    public Float4 ywyy() { return new Float4(y, w, y, y); }
    public Float4 ywyz() { return new Float4(y, w, y, z); }
    public Float4 ywyw() { return new Float4(y, w, y, w); }
    public Float4 ywzx() { return new Float4(y, w, z, x); }
    public Float4 ywzy() { return new Float4(y, w, z, y); }
    public Float4 ywzz() { return new Float4(y, w, z, z); }
    public Float4 ywzw() { return new Float4(y, w, z, w); }
    public Float4 ywwx() { return new Float4(y, w, w, x); }
    public Float4 ywwy() { return new Float4(y, w, w, y); }
    public Float4 ywwz() { return new Float4(y, w, w, z); }
    public Float4 ywww() { return new Float4(y, w, w, w); }
    public Float4 zxxx() { return new Float4(z, x, x, x); }
    public Float4 zxxy() { return new Float4(z, x, x, y); }
    public Float4 zxxz() { return new Float4(z, x, x, z); }
    public Float4 zxxw() { return new Float4(z, x, x, w); }
    public Float4 zxyx() { return new Float4(z, x, y, x); }
    public Float4 zxyy() { return new Float4(z, x, y, y); }
    public Float4 zxyz() { return new Float4(z, x, y, z); }
    public Float4 zxyw() { return new Float4(z, x, y, w); }
    public Float4 zxzx() { return new Float4(z, x, z, x); }
    public Float4 zxzy() { return new Float4(z, x, z, y); }
    public Float4 zxzz() { return new Float4(z, x, z, z); }
    public Float4 zxzw() { return new Float4(z, x, z, w); }
    public Float4 zxwx() { return new Float4(z, x, w, x); }
    public Float4 zxwy() { return new Float4(z, x, w, y); }
    public Float4 zxwz() { return new Float4(z, x, w, z); }
    public Float4 zxww() { return new Float4(z, x, w, w); }
    public Float4 zyxx() { return new Float4(z, y, x, x); }
    public Float4 zyxy() { return new Float4(z, y, x, y); }
    public Float4 zyxz() { return new Float4(z, y, x, z); }
    public Float4 zyxw() { return new Float4(z, y, x, w); }
    public Float4 zyyx() { return new Float4(z, y, y, x); }
    public Float4 zyyy() { return new Float4(z, y, y, y); }
    public Float4 zyyz() { return new Float4(z, y, y, z); }
    public Float4 zyyw() { return new Float4(z, y, y, w); }
    public Float4 zyzx() { return new Float4(z, y, z, x); }
    public Float4 zyzy() { return new Float4(z, y, z, y); }
    public Float4 zyzz() { return new Float4(z, y, z, z); }
    public Float4 zyzw() { return new Float4(z, y, z, w); }
    public Float4 zywx() { return new Float4(z, y, w, x); }
    public Float4 zywy() { return new Float4(z, y, w, y); }
    public Float4 zywz() { return new Float4(z, y, w, z); }
    public Float4 zyww() { return new Float4(z, y, w, w); }
    public Float4 zzxx() { return new Float4(z, z, x, x); }
    public Float4 zzxy() { return new Float4(z, z, x, y); }
    public Float4 zzxz() { return new Float4(z, z, x, z); }
    public Float4 zzxw() { return new Float4(z, z, x, w); }
    public Float4 zzyx() { return new Float4(z, z, y, x); }
    public Float4 zzyy() { return new Float4(z, z, y, y); }
    public Float4 zzyz() { return new Float4(z, z, y, z); }
    public Float4 zzyw() { return new Float4(z, z, y, w); }
    public Float4 zzzx() { return new Float4(z, z, z, x); }
    public Float4 zzzy() { return new Float4(z, z, z, y); }
    public Float4 zzzz() { return new Float4(z, z, z, z); }
    public Float4 zzzw() { return new Float4(z, z, z, w); }
    public Float4 zzwx() { return new Float4(z, z, w, x); }
    public Float4 zzwy() { return new Float4(z, z, w, y); }
    public Float4 zzwz() { return new Float4(z, z, w, z); }
    public Float4 zzww() { return new Float4(z, z, w, w); }
    public Float4 zwxx() { return new Float4(z, w, x, x); }
    public Float4 zwxy() { return new Float4(z, w, x, y); }
    public Float4 zwxz() { return new Float4(z, w, x, z); }
    public Float4 zwxw() { return new Float4(z, w, x, w); }
    public Float4 zwyx() { return new Float4(z, w, y, x); }
    public Float4 zwyy() { return new Float4(z, w, y, y); }
    public Float4 zwyz() { return new Float4(z, w, y, z); }
    public Float4 zwyw() { return new Float4(z, w, y, w); }
    public Float4 zwzx() { return new Float4(z, w, z, x); }
    public Float4 zwzy() { return new Float4(z, w, z, y); }
    public Float4 zwzz() { return new Float4(z, w, z, z); }
    public Float4 zwzw() { return new Float4(z, w, z, w); }
    public Float4 zwwx() { return new Float4(z, w, w, x); }
    public Float4 zwwy() { return new Float4(z, w, w, y); }
    public Float4 zwwz() { return new Float4(z, w, w, z); }
    public Float4 zwww() { return new Float4(z, w, w, w); }
    public Float4 wxxx() { return new Float4(w, x, x, x); }
    public Float4 wxxy() { return new Float4(w, x, x, y); }
    public Float4 wxxz() { return new Float4(w, x, x, z); }
    public Float4 wxxw() { return new Float4(w, x, x, w); }
    public Float4 wxyx() { return new Float4(w, x, y, x); }
    public Float4 wxyy() { return new Float4(w, x, y, y); }
    public Float4 wxyz() { return new Float4(w, x, y, z); }
    public Float4 wxyw() { return new Float4(w, x, y, w); }
    public Float4 wxzx() { return new Float4(w, x, z, x); }
    public Float4 wxzy() { return new Float4(w, x, z, y); }
    public Float4 wxzz() { return new Float4(w, x, z, z); }
    public Float4 wxzw() { return new Float4(w, x, z, w); }
    public Float4 wxwx() { return new Float4(w, x, w, x); }
    public Float4 wxwy() { return new Float4(w, x, w, y); }
    public Float4 wxwz() { return new Float4(w, x, w, z); }
    public Float4 wxww() { return new Float4(w, x, w, w); }
    public Float4 wyxx() { return new Float4(w, y, x, x); }
    public Float4 wyxy() { return new Float4(w, y, x, y); }
    public Float4 wyxz() { return new Float4(w, y, x, z); }
    public Float4 wyxw() { return new Float4(w, y, x, w); }
    public Float4 wyyx() { return new Float4(w, y, y, x); }
    public Float4 wyyy() { return new Float4(w, y, y, y); }
    public Float4 wyyz() { return new Float4(w, y, y, z); }
    public Float4 wyyw() { return new Float4(w, y, y, w); }
    public Float4 wyzx() { return new Float4(w, y, z, x); }
    public Float4 wyzy() { return new Float4(w, y, z, y); }
    public Float4 wyzz() { return new Float4(w, y, z, z); }
    public Float4 wyzw() { return new Float4(w, y, z, w); }
    public Float4 wywx() { return new Float4(w, y, w, x); }
    public Float4 wywy() { return new Float4(w, y, w, y); }
    public Float4 wywz() { return new Float4(w, y, w, z); }
    public Float4 wyww() { return new Float4(w, y, w, w); }
    public Float4 wzxx() { return new Float4(w, z, x, x); }
    public Float4 wzxy() { return new Float4(w, z, x, y); }
    public Float4 wzxz() { return new Float4(w, z, x, z); }
    public Float4 wzxw() { return new Float4(w, z, x, w); }
    public Float4 wzyx() { return new Float4(w, z, y, x); }
    public Float4 wzyy() { return new Float4(w, z, y, y); }
    public Float4 wzyz() { return new Float4(w, z, y, z); }
    public Float4 wzyw() { return new Float4(w, z, y, w); }
    public Float4 wzzx() { return new Float4(w, z, z, x); }
    public Float4 wzzy() { return new Float4(w, z, z, y); }
    public Float4 wzzz() { return new Float4(w, z, z, z); }
    public Float4 wzzw() { return new Float4(w, z, z, w); }
    public Float4 wzwx() { return new Float4(w, z, w, x); }
    public Float4 wzwy() { return new Float4(w, z, w, y); }
    public Float4 wzwz() { return new Float4(w, z, w, z); }
    public Float4 wzww() { return new Float4(w, z, w, w); }
    public Float4 wwxx() { return new Float4(w, w, x, x); }
    public Float4 wwxy() { return new Float4(w, w, x, y); }
    public Float4 wwxz() { return new Float4(w, w, x, z); }
    public Float4 wwxw() { return new Float4(w, w, x, w); }
    public Float4 wwyx() { return new Float4(w, w, y, x); }
    public Float4 wwyy() { return new Float4(w, w, y, y); }
    public Float4 wwyz() { return new Float4(w, w, y, z); }
    public Float4 wwyw() { return new Float4(w, w, y, w); }
    public Float4 wwzx() { return new Float4(w, w, z, x); }
    public Float4 wwzy() { return new Float4(w, w, z, y); }
    public Float4 wwzz() { return new Float4(w, w, z, z); }
    public Float4 wwzw() { return new Float4(w, w, z, w); }
    public Float4 wwwx() { return new Float4(w, w, w, x); }
    public Float4 wwwy() { return new Float4(w, w, w, y); }
    public Float4 wwwz() { return new Float4(w, w, w, z); }
    public Float4 wwww() { return new Float4(w, w, w, w); }
    //-------------------------------------------- SWIVELS  (@formatter:on) --------------------------------------------
}
