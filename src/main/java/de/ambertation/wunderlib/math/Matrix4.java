package de.ambertation.wunderlib.math;

public class Matrix4 {
    public static final Matrix4 IDENTITY = new Matrix4(new double[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    });

    public static final int M00 = 0;
    public static final int M01 = 1;
    public static final int M02 = 2;
    public static final int M03 = 3;
    public static final int M10 = 4;
    public static final int M11 = 5;
    public static final int M12 = 6;
    public static final int M13 = 7;
    public static final int M20 = 8;
    public static final int M21 = 9;
    public static final int M22 = 10;
    public static final int M23 = 11;
    public static final int M30 = 12;
    public static final int M31 = 13;
    public static final int M32 = 14;
    public static final int M33 = 15;
    public static final int R0C0 = M00;
    public static final int R0C1 = M01;
    public static final int R0C2 = M02;
    public static final int R0C3 = M03;
    public static final int R1C0 = M10;
    public static final int R1C1 = M11;
    public static final int R1C2 = M12;
    public static final int R1C3 = M13;
    public static final int R2C0 = M20;
    public static final int R2C1 = M21;
    public static final int R2C2 = M22;
    public static final int R2C3 = M23;
    public static final int R3C0 = M30;
    public static final int R3C1 = M31;
    public static final int R3C2 = M32;
    public static final int R3C3 = M33;


    private final double[] m;

    protected Matrix4(double[] inN) {
        m = inN;
    }

    public static Matrix4 copyOf(double[] newM) {
        double[] copyM = new double[16];
        System.arraycopy(newM, 0, copyM, 0, copyM.length);
        return new Matrix4(copyM);
    }

    public static Matrix4 of(double[] newM) {
        return new Matrix4(newM);
    }

    //from https://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/index.htm
    public static Matrix4 ofRotation(Quaternion unitLength) {
        double[] newM = new double[16];
        double qw2 = unitLength.w * unitLength.w;
        double qx2 = unitLength.v.x * unitLength.v.x;
        double qy2 = unitLength.v.y * unitLength.v.y;
        double qz2 = unitLength.v.z * unitLength.v.z;

        // sqrt(qx2+qy2+qz2+qw2) = 1 <=>
        // qx2+qy2+qz2+qw2 = 1; =>
        // (1 - 2*qy2 - 2*qz2) =
        // = qx2 + qy2 + qz2 + qw2 - qy2 - qy2 -qz2 -qz2 =
        // = qx2 - qy2 - qz2 + qw2
        newM[M00] = (qx2 - qy2 - qz2 + qw2);  //1 - 2*qy2 - 2*qz2
        newM[M11] = (-qx2 + qy2 - qz2 + qw2); //1 - 2*qx2 - 2*qz2
        newM[M22] = (-qx2 - qy2 + qz2 + qw2); //1 - 2*qx2 - 2*qy2
        newM[M33] = 1;

        double tmp1 = unitLength.v.x * unitLength.v.y;
        double tmp2 = unitLength.v.z * unitLength.w;
        newM[M10] = 2.0 * (tmp1 + tmp2);
        newM[M01] = 2.0 * (tmp1 - tmp2);

        tmp1 = unitLength.v.x * unitLength.v.z;
        tmp2 = unitLength.v.y * unitLength.w;
        newM[M20] = 2.0 * (tmp1 - tmp2);
        newM[M02] = 2.0 * (tmp1 + tmp2);

        tmp1 = unitLength.v.y * unitLength.v.z;
        tmp2 = unitLength.v.x * unitLength.w;
        newM[M21] = 2.0 * (tmp1 + tmp2);
        newM[M12] = 2.0 * (tmp1 - tmp2);

        return new Matrix4(newM);
    }

    public static Matrix4 ofTranslation(Float3 t) {
        double[] newM = new double[16];

        newM[M00] = 1;
        newM[M11] = 1;
        newM[M22] = 1;
        newM[M33] = 1;

        if (t != null) {
            newM[M03] = t.x;
            newM[M13] = t.y;
            newM[M23] = t.z;
        }

        return new Matrix4(newM);
    }

    public static Matrix4 ofScale(Float3 s) {
        double[] newM = new double[16];

        newM[M00] = s.x;
        newM[M11] = s.y;
        newM[M22] = s.z;
        newM[M33] = 1;

        return new Matrix4(newM);
    }

    public static Matrix4 ofBasis(Float3 xAxis, Float3 yAxis, Float3 zAxis) {
        double[] newM = new double[16];

        newM[M00] = xAxis.x;
        newM[M10] = xAxis.y;
        newM[M20] = xAxis.z;
        newM[M30] = 0;

        newM[M01] = yAxis.x;
        newM[M11] = yAxis.y;
        newM[M21] = yAxis.z;
        newM[M31] = 0;

        newM[M02] = zAxis.x;
        newM[M12] = zAxis.y;
        newM[M22] = zAxis.z;
        newM[M32] = 0;

        newM[M33] = 1;

        return new Matrix4(newM);
    }

    public Float3 getBasisX() {
        return getUnitCubeCorner(Bounds.Interpolate.MAX_MIN_MIN, false)
                .sub(getUnitCubeCorner(Bounds.Interpolate.MIN_MIN_MIN, false)).normalized();
    }

    public Float3 getBasisY() {
        return getUnitCubeCorner(Bounds.Interpolate.MIN_MAX_MIN, false)
                .sub(getUnitCubeCorner(Bounds.Interpolate.MIN_MIN_MIN, false)).normalized();
    }

    public Float3 getBasisZ() {
        return getUnitCubeCorner(Bounds.Interpolate.MIN_MIN_MAX, false)
                .sub(getUnitCubeCorner(Bounds.Interpolate.MIN_MIN_MIN, false)).normalized();
    }

    public Matrix4 transposed() {
        double[] newM = new double[16];
        newM[M00] = m[M00];
        newM[M01] = m[M10];
        newM[M02] = m[M20];
        newM[M03] = m[M30];
        newM[M10] = m[M01];
        newM[M11] = m[M11];
        newM[M12] = m[M21];
        newM[M13] = m[M31];
        newM[M20] = m[M02];
        newM[M21] = m[M12];
        newM[M22] = m[M22];
        newM[M23] = m[M32];
        newM[M30] = m[M03];
        newM[M31] = m[M13];
        newM[M32] = m[M23];
        newM[M33] = m[M33];
        return new Matrix4(newM);
    }

    public Matrix4 mul(final Matrix4 B) {
        final double[] newM = new double[16];

        newM[M00] = m[M00] * B.m[M00] + m[M01] * B.m[M10] + m[M02] * B.m[M20] + m[M03] * B.m[M30];
        newM[M01] = m[M00] * B.m[M01] + m[M01] * B.m[M11] + m[M02] * B.m[M21] + m[M03] * B.m[M31];
        newM[M02] = m[M00] * B.m[M02] + m[M01] * B.m[M12] + m[M02] * B.m[M22] + m[M03] * B.m[M32];
        newM[M03] = m[M00] * B.m[M03] + m[M01] * B.m[M13] + m[M02] * B.m[M23] + m[M03] * B.m[M33];

        newM[M10] = m[M10] * B.m[M00] + m[M11] * B.m[M10] + m[M12] * B.m[M20] + m[M13] * B.m[M30];
        newM[M11] = m[M10] * B.m[M01] + m[M11] * B.m[M11] + m[M12] * B.m[M21] + m[M13] * B.m[M31];
        newM[M12] = m[M10] * B.m[M02] + m[M11] * B.m[M12] + m[M12] * B.m[M22] + m[M13] * B.m[M32];
        newM[M13] = m[M10] * B.m[M03] + m[M11] * B.m[M13] + m[M12] * B.m[M23] + m[M13] * B.m[M33];

        newM[M20] = m[M20] * B.m[M00] + m[M21] * B.m[M10] + m[M22] * B.m[M20] + m[M23] * B.m[M30];
        newM[M21] = m[M20] * B.m[M01] + m[M21] * B.m[M11] + m[M22] * B.m[M21] + m[M23] * B.m[M31];
        newM[M22] = m[M20] * B.m[M02] + m[M21] * B.m[M12] + m[M22] * B.m[M22] + m[M23] * B.m[M32];
        newM[M23] = m[M20] * B.m[M03] + m[M21] * B.m[M13] + m[M22] * B.m[M23] + m[M23] * B.m[M33];

        newM[M30] = m[M30] * B.m[M00] + m[M31] * B.m[M10] + m[M32] * B.m[M20] + m[M33] * B.m[M30];
        newM[M31] = m[M30] * B.m[M01] + m[M31] * B.m[M11] + m[M32] * B.m[M21] + m[M33] * B.m[M31];
        newM[M32] = m[M30] * B.m[M02] + m[M31] * B.m[M12] + m[M32] * B.m[M22] + m[M33] * B.m[M32];
        newM[M33] = m[M30] * B.m[M03] + m[M31] * B.m[M13] + m[M32] * B.m[M23] + m[M33] * B.m[M33];

        return new Matrix4(newM);
    }

    public Float4 transform(Float4 v) {
        return new Float4(
                m[M00] * v.x + m[M01] * v.y + m[M02] * v.z + m[M03] * v.w,
                m[M10] * v.x + m[M11] * v.y + m[M12] * v.z + m[M13] * v.w,
                m[M20] * v.x + m[M21] * v.y + m[M22] * v.z + m[M23] * v.w,
                m[M30] * v.x + m[M31] * v.y + m[M32] * v.z + m[M33] * v.w
        );
    }

    public Float3 transform(Float3 point) {
        final double w = m[M30] * point.x + m[M31] * point.y + m[M32] * point.z + m[M33];
        return new Float3(
                (m[M00] * point.x + m[M01] * point.y + m[M02] * point.z + m[M03]) / w,
                (m[M10] * point.x + m[M11] * point.y + m[M12] * point.z + m[M13]) / w,
                (m[M20] * point.x + m[M21] * point.y + m[M22] * point.z + m[M23]) / w
        );
    }

    public Float3 transformDirection(Float3 dir) {
        return new Float3(
                m[M00] * dir.x + m[M01] * dir.y + m[M02] * dir.z,
                m[M10] * dir.x + m[M11] * dir.y + m[M12] * dir.z,
                m[M20] * dir.x + m[M21] * dir.y + m[M22] * dir.z
        );
    }

    public Float3 translation() {
        return Float3.of(m[M03], m[M13], m[M23]);
    }

    public double det() {
        //http://www.euclideanspace.com/maths/algebra/matrix/functions/determinant/fourD/index.htm
        return m[M03] * m[M12] * m[M21] * m[M30] - m[M02] * m[M13] * m[M21] * m[M30] - m[M03] * m[M11] * m[M22] * m[M30] + m[M01] * m[M13] * m[M22] * m[M30] +
                m[M02] * m[M11] * m[M23] * m[M30] - m[M01] * m[M12] * m[M23] * m[M30] - m[M03] * m[M12] * m[M20] * m[M31] + m[M02] * m[M13] * m[M20] * m[M31] +
                m[M03] * m[M10] * m[M22] * m[M31] - m[M00] * m[M13] * m[M22] * m[M31] - m[M02] * m[M10] * m[M23] * m[M31] + m[M00] * m[M12] * m[M23] * m[M31] +
                m[M03] * m[M11] * m[M20] * m[M32] - m[M01] * m[M13] * m[M20] * m[M32] - m[M03] * m[M10] * m[M21] * m[M32] + m[M00] * m[M13] * m[M21] * m[M32] +
                m[M01] * m[M10] * m[M23] * m[M32] - m[M00] * m[M11] * m[M23] * m[M32] - m[M02] * m[M11] * m[M20] * m[M33] + m[M01] * m[M12] * m[M20] * m[M33] +
                m[M02] * m[M10] * m[M21] * m[M33] - m[M00] * m[M12] * m[M21] * m[M33] - m[M01] * m[M10] * m[M22] * m[M33] + m[M00] * m[M11] * m[M22] * m[M33];
    }

    public double det3x3() {
        //http://www.euclideanspace.com/maths/algebra/matrix/functions/determinant/threeD/index.htm
        return m[M00] * m[M11] * m[M22] + m[M01] * m[M12] * m[M20] + m[M02] * m[M10] * m[M21] - m[M00] * m[M12] * m[M21] - m[M01] * m[M10] * m[M22] - m[M02] * m[M11] * m[M20];
    }

    public boolean isOrthogonal() {
        return Math.abs(Math.abs(det()) - 1) < Float3.EPSILON;
    }

    public Matrix4 inverted() {
        final double det = det();
        //if (Math.abs(Math.abs(det()) - 1) < Float3.EPSILON) return transposed();

        //http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/fourD/index.htm
        final double iDet = 1 / det;
        double[] newM = new double[16];
        newM[M00] = (m[M12] * m[M23] * m[M31] - m[M13] * m[M22] * m[M31] + m[M13] * m[M21] * m[M32] - m[M11] * m[M23] * m[M32] - m[M12] * m[M21] * m[M33] + m[M11] * m[M22] * m[M33]) * iDet;
        newM[M01] = (m[M03] * m[M22] * m[M31] - m[M02] * m[M23] * m[M31] - m[M03] * m[M21] * m[M32] + m[M01] * m[M23] * m[M32] + m[M02] * m[M21] * m[M33] - m[M01] * m[M22] * m[M33]) * iDet;
        newM[M02] = (m[M02] * m[M13] * m[M31] - m[M03] * m[M12] * m[M31] + m[M03] * m[M11] * m[M32] - m[M01] * m[M13] * m[M32] - m[M02] * m[M11] * m[M33] + m[M01] * m[M12] * m[M33]) * iDet;
        newM[M03] = (m[M03] * m[M12] * m[M21] - m[M02] * m[M13] * m[M21] - m[M03] * m[M11] * m[M22] + m[M01] * m[M13] * m[M22] + m[M02] * m[M11] * m[M23] - m[M01] * m[M12] * m[M23]) * iDet;
        newM[M10] = (m[M13] * m[M22] * m[M30] - m[M12] * m[M23] * m[M30] - m[M13] * m[M20] * m[M32] + m[M10] * m[M23] * m[M32] + m[M12] * m[M20] * m[M33] - m[M10] * m[M22] * m[M33]) * iDet;
        newM[M11] = (m[M02] * m[M23] * m[M30] - m[M03] * m[M22] * m[M30] + m[M03] * m[M20] * m[M32] - m[M00] * m[M23] * m[M32] - m[M02] * m[M20] * m[M33] + m[M00] * m[M22] * m[M33]) * iDet;
        newM[M12] = (m[M03] * m[M12] * m[M30] - m[M02] * m[M13] * m[M30] - m[M03] * m[M10] * m[M32] + m[M00] * m[M13] * m[M32] + m[M02] * m[M10] * m[M33] - m[M00] * m[M12] * m[M33]) * iDet;
        newM[M13] = (m[M02] * m[M13] * m[M20] - m[M03] * m[M12] * m[M20] + m[M03] * m[M10] * m[M22] - m[M00] * m[M13] * m[M22] - m[M02] * m[M10] * m[M23] + m[M00] * m[M12] * m[M23]) * iDet;
        newM[M20] = (m[M11] * m[M23] * m[M30] - m[M13] * m[M21] * m[M30] + m[M13] * m[M20] * m[M31] - m[M10] * m[M23] * m[M31] - m[M11] * m[M20] * m[M33] + m[M10] * m[M21] * m[M33]) * iDet;
        newM[M21] = (m[M03] * m[M21] * m[M30] - m[M01] * m[M23] * m[M30] - m[M03] * m[M20] * m[M31] + m[M00] * m[M23] * m[M31] + m[M01] * m[M20] * m[M33] - m[M00] * m[M21] * m[M33]) * iDet;
        newM[M22] = (m[M01] * m[M13] * m[M30] - m[M03] * m[M11] * m[M30] + m[M03] * m[M10] * m[M31] - m[M00] * m[M13] * m[M31] - m[M01] * m[M10] * m[M33] + m[M00] * m[M11] * m[M33]) * iDet;
        newM[M23] = (m[M03] * m[M11] * m[M20] - m[M01] * m[M13] * m[M20] - m[M03] * m[M10] * m[M21] + m[M00] * m[M13] * m[M21] + m[M01] * m[M10] * m[M23] - m[M00] * m[M11] * m[M23]) * iDet;
        newM[M30] = (m[M12] * m[M21] * m[M30] - m[M11] * m[M22] * m[M30] - m[M12] * m[M20] * m[M31] + m[M10] * m[M22] * m[M31] + m[M11] * m[M20] * m[M32] - m[M10] * m[M21] * m[M32]) * iDet;
        newM[M31] = (m[M01] * m[M22] * m[M30] - m[M02] * m[M21] * m[M30] + m[M02] * m[M20] * m[M31] - m[M00] * m[M22] * m[M31] - m[M01] * m[M20] * m[M32] + m[M00] * m[M21] * m[M32]) * iDet;
        newM[M32] = (m[M02] * m[M11] * m[M30] - m[M01] * m[M12] * m[M30] - m[M02] * m[M10] * m[M31] + m[M00] * m[M12] * m[M31] + m[M01] * m[M10] * m[M32] - m[M00] * m[M11] * m[M32]) * iDet;
        newM[M33] = (m[M01] * m[M12] * m[M20] - m[M02] * m[M11] * m[M20] + m[M02] * m[M10] * m[M21] - m[M00] * m[M12] * m[M21] - m[M01] * m[M10] * m[M22] + m[M00] * m[M11] * m[M22]) * iDet;

        return new Matrix4(newM);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Float3.toString(m[M00]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M01]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M02]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M03]));
        sb.append("\n");

        sb.append(Float3.toString(m[M10]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M11]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M12]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M13]));
        sb.append("\n");

        sb.append(Float3.toString(m[M20]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M21]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M22]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M23]));
        sb.append("\n");

        sb.append(Float3.toString(m[M30]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M31]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M32]));
        sb.append("\t, ");
        sb.append(Float3.toString(m[M33]));
        sb.append("\n");
        return sb.toString();
    }

    public Float3[] getUnitCubeCornersAndCenter(boolean blockAligned) {
        Float3[] corners = new Float3[Bounds.Interpolate.CORNERS_AND_CENTER.length];
        for (var c : Bounds.Interpolate.CORNERS_AND_CENTER) {
            corners[c.idx] = getUnitCubeCorner(c, blockAligned);
        }

        return corners;
    }

    public Float3[] getUnitCubeCorners(boolean blockAligned) {
        Float3[] corners = new Float3[Bounds.Interpolate.CORNERS.length];
        for (var c : Bounds.Interpolate.CORNERS) {
            corners[c.idx] = getUnitCubeCorner(c, blockAligned);
        }

        return corners;
    }

    public Float3 getUnitCubeCorner(Bounds.Interpolate corner, boolean blockAligned) {
        return this.transform(Bounds.Interpolate.CORNERS_AND_CENTER[corner.idx].t.sub(0.5)).align(blockAligned);
    }
}
