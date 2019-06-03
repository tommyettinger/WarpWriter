package warpwriter.model.nonvoxel;

import com.badlogic.gdx.math.*;
import squidpony.squidmath.NumberTools;

import java.io.Serializable;
/** A variant on libGDX's quaternion class that uses {@code turns} for its angle measurements instead of radians. A turn
 * goes from 0.0 to 1.0, instead of degrees going from 0 to 360 or radians from 0 to 2PI. There are some key static
 * methods here for converting turns to integer brads and back again, which can be useful when the float imprecision in
 * radians or turns (as well as some fractions of degrees) is unacceptable and integer angles are preferred. Brads, or
 * binary radians, are integers from 0 to 255 (only the bottom 8 bits are used, so the brad value may be negative) that
 * represent angles much like degrees do, but a brad angle can be cut in half 7 or 8 times, while an angle in degrees
 * can only be cut in half 3 times before reaching a floating-point number.
 * @see <a href="http://en.wikipedia.org/wiki/Quaternion">http://en.wikipedia.org/wiki/Quaternion</a>
 * @author badlogicgames@gmail.com
 * @author vesuvio
 * @author xoppa
 * @author Tommy Ettinger
 */
public class TurnQuaternion implements Serializable {
    private static final long serialVersionUID = 1L;
    private static TurnQuaternion tmp1 = new TurnQuaternion(0, 0, 0, 0);
    private static TurnQuaternion tmp2 = new TurnQuaternion(0, 0, 0, 0);
    
    public float x;
    public float y;
    public float z;
    public float w;


    /**
     * Precisely converts the bottom 8 bits of {@code angle} to an angle in turns, from 0 to 1. This allows an input
     * range of 0 to 255 inclusive before the angle cycles back to 0, and negative numbers work just like positive ones.
     * @param angle an angle in brads, of which only the bottom 8 bits are used; may be negative or positive
     * @return the angle in turns that corresponds to the bottom 8 bits of angle.
     */
    public static float bradToTurn(final int angle) {
        return (angle & 0xFF) * 0x1p-8f;
    }

    /**
     * Rounds the given {@code angle} in turns, usually from 0 to 1 (but this tolerates inputs as low as -1 and with no
     * enforced maximum), to an angle in brads, which is always an int from 0 to 255. 
     * @param angle an angle in turns, usually from 0 to 1, with a technical minimum of -1 and no hard maximum
     * @return an angle in brads that corresponds to the given angle, rounded to the nearest valid brad angle.
     */
    public static int turnToBrad(final float angle)
    {
        return (int) (angle * 256f + 256.5f) & 0xFF;
    }
    /** Linearly interpolates between two angles in turns. Takes into account that angles wrap at 1 turn and always
     * takes the direction with the smallest delta angle.
     *
     * @param from start angle in turns
     * @param to target angle in turns
     * @param progress interpolation value in the range 0.0f inclusive to 1.0f inclusive
     * @return the interpolated angle in the range 0.0f inclusive to 1.0f exclusive
     */
    public static float lerpAngleTurn (float from, float to, float progress) {
        float delta = (to - from + 1.5f);
        delta = (from + (delta - 0.5f - (int)delta) * progress + 1f);
        return delta - (int)delta;
    }

    /** Linearly interpolates between two angles in brads. Takes into account that angles wrap at 256 brads and always
     * takes the direction with the smallest delta angle.
     *
     * @param from start angle in brads
     * @param to target angle in brads
     * @param progress interpolation value in the range 0 inclusive to 1 inclusive
     * @return the interpolated angle in the range 0 inclusive to 256 exclusive, as an int
     */
    public static int lerpAngleBrad (int from, int to, float progress) {
        int delta = ((to - from + 128) & 255) - 128;
        return (int)(from + delta * progress + 256.5f) & 255;
    }
    
    /** Constructor, sets the four components of the quaternion.
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component */
    public TurnQuaternion(float x, float y, float z, float w) {
        this.set(x, y, z, w);
    }

    public TurnQuaternion() {
        idt();
    }

    /** Constructor, sets the four components from the given quaternion.
     *
     * @param quaternion The quaternion to copy. */
    public TurnQuaternion(TurnQuaternion quaternion) {
        this.set(quaternion);
    }

    /** Constructor, sets the quaternion from the given axis vector and the angle around that axis in degrees.
     *
     * @param axis The axis
     * @param angle The angle in degrees. */
    public TurnQuaternion(Vector3 axis, float angle) {
        this.set(axis, angle);
    }

    /** Sets the components of the quaternion
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component
     * @return This quaternion for chaining */
    public TurnQuaternion set (float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /** Sets the quaternion components from the given quaternion.
     * @param quaternion The quaternion.
     * @return This quaternion for chaining. */
    public TurnQuaternion set (TurnQuaternion quaternion) {
        return this.set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    /** Sets the quaternion components from the given axis and angle around that axis.
     *
     * @param axis The axis
     * @param angle The angle in turns
     * @return This quaternion for chaining. */
    public TurnQuaternion set (Vector3 axis, float angle) {
        float d = axis.len();
        if (d == 0f) return idt();
        d = 1f / d;
        float l_ang = angle < 0 ? 1f + (angle % 1f) : angle % 1f;
        float l_sin = NumberTools.sin_(l_ang * 0.5f);
        float l_cos = NumberTools.cos_(l_ang * 0.5f);
        return this.set(d * axis.x * l_sin, d * axis.y * l_sin, d * axis.z * l_sin, l_cos).nor();
    }

    /** @return a copy of this quaternion */
    public TurnQuaternion cpy () {
        return new TurnQuaternion(this);
    }

    /** @return the euclidean length of the specified quaternion */
    public final static float len (final float x, final float y, final float z, final float w) {
        return (float)Math.sqrt(x * x + y * y + z * z + w * w);
    }

    /** @return the euclidean length of this quaternion */
    public float len () {
        return (float)Math.sqrt(x * x + y * y + z * z + w * w);
    }

    @Override
    public String toString () {
        return "[" + x + "|" + y + "|" + z + "|" + w + "]";
    }

    /** Sets the quaternion to the given Euler angles in brads.
     * @param yaw the rotation around the z axis in brads
     * @param pitch the rotation around the y axis in brads
     * @param roll the rotation around the x axis in brads
     * @return this quaternion */
    public TurnQuaternion setEulerAnglesBrad (int yaw, int pitch, int roll) {
        return setEulerAngles(bradToTurn(yaw), bradToTurn(pitch), bradToTurn(roll));
    }

    /** Sets the quaternion to the given Euler angles in turns.
     * @param yaw the rotation around the z axis in turns
     * @param pitch the rotation around the y axis in turns
     * @param roll the rotation around the x axis in turns
     * @return this quaternion */
    public TurnQuaternion setEulerAngles (float yaw, float pitch, float roll) {
        final float hr = yaw * 0.5f;
        final float shr = NumberTools.sin_(hr);
        final float chr = NumberTools.cos_(hr);
        final float hp = roll * 0.5f;
        final float shp = NumberTools.sin_(hp);
        final float chp = NumberTools.cos_(hp);
        final float hy = pitch * 0.5f;
        final float shy = NumberTools.sin_(hy);
        final float chy = NumberTools.cos_(hy);
        final float chy_shp = chy * shp;
        final float shy_chp = shy * chp;
        final float chy_chp = chy * chp;
        final float shy_shp = shy * shp;

        x = (chy_shp * chr) + (shy_chp * shr); // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
        y = (shy_chp * chr) - (chy_shp * shr); // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
        z = (chy_chp * shr) - (shy_shp * chr); // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
        w = (chy_chp * chr) + (shy_shp * shr); // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
        return this;
    }

    /** Get the pole of the gimbal lock, if any.
     * @return positive (+1) for north pole, negative (-1) for south pole, zero (0) when no gimbal lock */
    public int getGimbalPole () {
        final int t = (int) ((y * x + z * w) * 2.02f);
        return (t >> 31) | ((-t) >>> 31); // Thanks, Project Nayuki! https://www.nayuki.io/page/some-bit-twiddling-functions-explained
    }

    /**
     * Get the roll Euler angle in turns, which is the rotation around the z axis. Requires that this quaternion is normalized.
     * Was getRoll() in libGDX, but WarpWriter uses the z-axis for up-down, while libGDX uses it for forward/back.
     * @return the rotation around the z axis in turns (between 0 and 1) */
    public float getYaw () {
        final int pole = getGimbalPole();
        return pole == 0 ? NumberTools.atan2_(2f * (w * z + y * x), 1f - 2f * (x * x + z * z)) : (pole << 1)
                * NumberTools.atan2_(y, w);
    }

    /** 
     * Get the pitch Euler angle in turns, which is the rotation around the x axis. Requires that this quaternion is normalized.
     * Was getPitch() in libGDX, but WarpWriter uses the x-axis for forward/back, while libGDX uses it for left/right.
     * @return the rotation around the x axis in turns (between 0.75 and 1 or between 0 and 0.25) */
    public float getRoll () {
        final int pole = getGimbalPole();
        return pole == 0 ? NumberTools.asin_(MathUtils.clamp(2f * (w * x - z * y), -1f, 1f)) : (float)pole * 0.25f;
    }

    /** 
     * Get the yaw Euler angle in turns, which is the rotation around the y axis. Requires that this quaternion is normalized.
     * Was getYaw() in libGDX, but WarpWriter uses the y-axis for left/right, while libGDX uses it for up/down.
     * @return the rotation around the y axis in turns (between 0 and 1) */
    public float getPitch () {
        return getGimbalPole() == 0 ? NumberTools.atan2_(2f * (y * w + x * z), 1f - 2f * (y * y + x * x)) : 0f;
    }

    public final static float len2 (final float x, final float y, final float z, final float w) {
        return x * x + y * y + z * z + w * w;
    }

    /** @return the length of this quaternion without square root */
    public float len2 () {
        return x * x + y * y + z * z + w * w;
    }

    /** Normalizes this quaternion to unit length
     * @return the quaternion for chaining */
    public TurnQuaternion nor () {
        float len = len2();
        if (len != 0.f && !MathUtils.isEqual(len, 1f)) {
            len = (float)Math.sqrt(len);
            w /= len;
            x /= len;
            y /= len;
            z /= len;
        }
        return this;
    }

    /** Conjugate the quaternion.
     *
     * @return This quaternion for chaining */
    public TurnQuaternion conjugate () {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /** Transforms the given vector using this quaternion. Modifies {@code v} in-place.
     *
     * @param v Vector to transform; will be modified
     * @return v, after changes
     */
    public Vector3 transform (Vector3 v) {
        tmp2.set(this);
        tmp2.conjugate();
        tmp2.mulLeft(tmp1.set(v.x, v.y, v.z, 0)).mulLeft(this);

        v.x = tmp2.x;
        v.y = tmp2.y;
        v.z = tmp2.z;
        return v;
    }

    /** Multiplies this quaternion with another one in the form of this = this * other
     *
     * @param other TurnQuaternion to multiply with
     * @return This quaternion for chaining */
    public TurnQuaternion mul (final TurnQuaternion other) {
        final float newX = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y;
        final float newY = this.w * other.y + this.y * other.w + this.z * other.x - this.x * other.z;
        final float newZ = this.w * other.z + this.z * other.w + this.x * other.y - this.y * other.x;
        final float newW = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    /** Multiplies this quaternion with another one in the form of this = this * other
     *
     * @param x the x component of the other quaternion to multiply with
     * @param y the y component of the other quaternion to multiply with
     * @param z the z component of the other quaternion to multiply with
     * @param w the w component of the other quaternion to multiply with
     * @return This quaternion for chaining */
    public TurnQuaternion mul (final float x, final float y, final float z, final float w) {
        final float newX = this.w * x + this.x * w + this.y * z - this.z * y;
        final float newY = this.w * y + this.y * w + this.z * x - this.x * z;
        final float newZ = this.w * z + this.z * w + this.x * y - this.y * x;
        final float newW = this.w * w - this.x * x - this.y * y - this.z * z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    /** Multiplies this quaternion with another one in the form of this = other * this
     *
     * @param other Quaternion to multiply with
     * @return This quaternion for chaining */
    public TurnQuaternion mulLeft (TurnQuaternion other) {
        final float newX = other.w * this.x + other.x * this.w + other.y * this.z - other.z * this.y;
        final float newY = other.w * this.y + other.y * this.w + other.z * this.x - other.x * this.z;
        final float newZ = other.w * this.z + other.z * this.w + other.x * this.y - other.y * this.x;
        final float newW = other.w * this.w - other.x * this.x - other.y * this.y - other.z * this.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    /** Multiplies this quaternion with another one in the form of this = other * this
     *
     * @param x the x component of the other quaternion to multiply with
     * @param y the y component of the other quaternion to multiply with
     * @param z the z component of the other quaternion to multiply with
     * @param w the w component of the other quaternion to multiply with
     * @return This quaternion for chaining */
    public TurnQuaternion mulLeft (final float x, final float y, final float z, final float w) {
        final float newX = w * this.x + x * this.w + y * this.z - z * this.y;
        final float newY = w * this.y + y * this.w + z * this.x - x * this.z;
        final float newZ = w * this.z + z * this.w + x * this.y - y * this.x;
        final float newW = w * this.w - x * this.x - y * this.y - z * this.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    /** Add the x,y,z,w components of the passed in quaternion to the ones of this quaternion */
    public TurnQuaternion add (TurnQuaternion quaternion) {
        this.x += quaternion.x;
        this.y += quaternion.y;
        this.z += quaternion.z;
        this.w += quaternion.w;
        return this;
    }

    /** Add the x,y,z,w components of the passed in quaternion to the ones of this quaternion */
    public TurnQuaternion add (float qx, float qy, float qz, float qw) {
        this.x += qx;
        this.y += qy;
        this.z += qz;
        this.w += qw;
        return this;
    }

    // TODO : the matrix4 set(quaternion) doesnt set the last row+col of the matrix to 0,0,0,1 so... that's why there is this method
    /** Fills a 4x4 matrix with the rotation matrix represented by this quaternion.
     *
     * @param matrix Matrix to fill */
    public void toMatrix (final float[] matrix) {
        final float xx = x * x;
        final float xy = x * y;
        final float xz = x * z;
        final float xw = x * w;
        final float yy = y * y;
        final float yz = y * z;
        final float yw = y * w;
        final float zz = z * z;
        final float zw = z * w;
        // Set matrix from quaternion
        matrix[Matrix4.M00] = 1 - 2 * (yy + zz);
        matrix[Matrix4.M01] = 2 * (xy - zw);
        matrix[Matrix4.M02] = 2 * (xz + yw);
        matrix[Matrix4.M03] = 0;
        matrix[Matrix4.M10] = 2 * (xy + zw);
        matrix[Matrix4.M11] = 1 - 2 * (xx + zz);
        matrix[Matrix4.M12] = 2 * (yz - xw);
        matrix[Matrix4.M13] = 0;
        matrix[Matrix4.M20] = 2 * (xz - yw);
        matrix[Matrix4.M21] = 2 * (yz + xw);
        matrix[Matrix4.M22] = 1 - 2 * (xx + yy);
        matrix[Matrix4.M23] = 0;
        matrix[Matrix4.M30] = 0;
        matrix[Matrix4.M31] = 0;
        matrix[Matrix4.M32] = 0;
        matrix[Matrix4.M33] = 1;
    }

    /** Sets the quaternion to an identity Quaternion
     * @return this quaternion for chaining */
    public TurnQuaternion idt () {
        return this.set(0, 0, 0, 1);
    }

    /** @return If this quaternion is an identity TurnQuaternion */
    public boolean isIdentity () {
        return MathUtils.isZero(x) && MathUtils.isZero(y) && MathUtils.isZero(z) && MathUtils.isEqual(w, 1f);
    }

    /** @return If this quaternion is an identity TurnQuaternion */
    public boolean isIdentity (final float tolerance) {
        return MathUtils.isZero(x, tolerance) && MathUtils.isZero(y, tolerance) && MathUtils.isZero(z, tolerance)
                && MathUtils.isEqual(w, 1f, tolerance);
    }

    // todo : the setFromAxis(v3,float) method should replace the set(v3,float) method

    /** Sets the quaternion components from the given axis and angle around that axis.
     *
     * @param axis The axis
     * @param turns The angle in turns
     * @return This quaternion for chaining. */
    public TurnQuaternion setFromAxis (final Vector3 axis, final float turns) {
        return setFromAxis(axis.x, axis.y, axis.z, turns);
    }
    
    /** Sets the quaternion components from the given axis and angle around that axis.
     * @param x X direction of the axis
     * @param y Y direction of the axis
     * @param z Z direction of the axis
     * @param turns The angle in turns
     * @return This quaternion for chaining. */
    public TurnQuaternion setFromAxis (final float x, final float y, final float z, final float turns) {
        float d = Vector3.len(x, y, z);
        if (d == 0f) return idt();
        d = 1f / d;
        float l_ang = turns < 0 ? (turns - (int)turns + 1f) : turns - (int)turns;
        float l_sin = NumberTools.sin_(l_ang * 0.5f);
        float l_cos = NumberTools.cos_(l_ang * 0.5f);
        return this.set(d * x * l_sin, d * y * l_sin, d * z * l_sin, l_cos).nor();
    }

    /** Sets the TurnQuaternion from the given matrix, optionally removing any scaling. */
    public TurnQuaternion setFromMatrix (boolean normalizeAxes, Matrix4 matrix) {
        return setFromAxes(normalizeAxes, matrix.val[Matrix4.M00], matrix.val[Matrix4.M01], matrix.val[Matrix4.M02],
                matrix.val[Matrix4.M10], matrix.val[Matrix4.M11], matrix.val[Matrix4.M12], matrix.val[Matrix4.M20],
                matrix.val[Matrix4.M21], matrix.val[Matrix4.M22]);
    }

    /** Sets the TurnQuaternion from the given rotation matrix, which must not contain scaling. */
    public TurnQuaternion setFromMatrix (Matrix4 matrix) {
        return setFromMatrix(false, matrix);
    }

    /** Sets the TurnQuaternion from the given matrix, optionally removing any scaling. */
    public TurnQuaternion setFromMatrix (boolean normalizeAxes, Matrix3 matrix) {
        return setFromAxes(normalizeAxes, matrix.val[Matrix3.M00], matrix.val[Matrix3.M01], matrix.val[Matrix3.M02],
                matrix.val[Matrix3.M10], matrix.val[Matrix3.M11], matrix.val[Matrix3.M12], matrix.val[Matrix3.M20],
                matrix.val[Matrix3.M21], matrix.val[Matrix3.M22]);
    }

    /** Sets the TurnQuaternion from the given rotation matrix, which must not contain scaling. */
    public TurnQuaternion setFromMatrix (Matrix3 matrix) {
        return setFromMatrix(false, matrix);
    }

    /** <p>
     * Sets the TurnQuaternion from the given x-, y- and z-axis which have to be orthonormal.
     * </p>
     *
     * <p>
     * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
     * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
     * </p>
     *
     * @param xx x-axis x-coordinate
     * @param xy x-axis y-coordinate
     * @param xz x-axis z-coordinate
     * @param yx y-axis x-coordinate
     * @param yy y-axis y-coordinate
     * @param yz y-axis z-coordinate
     * @param zx z-axis x-coordinate
     * @param zy z-axis y-coordinate
     * @param zz z-axis z-coordinate */
    public TurnQuaternion setFromAxes (float xx, float xy, float xz, float yx, float yy, float yz, float zx, float zy, float zz) {
        return setFromAxes(false, xx, xy, xz, yx, yy, yz, zx, zy, zz);
    }

    /** <p>
     * Sets the TurnQuaternion from the given x-, y- and z-axis.
     * </p>
     *
     * <p>
     * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
     * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
     * </p>
     *
     * @param normalizeAxes whether to normalize the axes (necessary when they contain scaling)
     * @param xx x-axis x-coordinate
     * @param xy x-axis y-coordinate
     * @param xz x-axis z-coordinate
     * @param yx y-axis x-coordinate
     * @param yy y-axis y-coordinate
     * @param yz y-axis z-coordinate
     * @param zx z-axis x-coordinate
     * @param zy z-axis y-coordinate
     * @param zz z-axis z-coordinate */
    public TurnQuaternion setFromAxes (boolean normalizeAxes, float xx, float xy, float xz, float yx, float yy, float yz, float zx,
                                       float zy, float zz) {
        if (normalizeAxes) {
            final float lx = 1f / Vector3.len(xx, xy, xz);
            final float ly = 1f / Vector3.len(yx, yy, yz);
            final float lz = 1f / Vector3.len(zx, zy, zz);
            xx *= lx;
            xy *= lx;
            xz *= lx;
            yx *= ly;
            yy *= ly;
            yz *= ly;
            zx *= lz;
            zy *= lz;
            zz *= lz;
        }
        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        final float t = xx + yy + zz;

        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            float s = (float)Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s; // so this division isn't bad
            x = (zy - yz) * s;
            y = (xz - zx) * s;
            z = (yx - xy) * s;
        } else if ((xx > yy) && (xx > zz)) {
            float s = (float)Math.sqrt(1.0 + xx - yy - zz); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (yx + xy) * s;
            z = (xz + zx) * s;
            w = (zy - yz) * s;
        } else if (yy > zz) {
            float s = (float)Math.sqrt(1.0 + yy - xx - zz); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (yx + xy) * s;
            z = (zy + yz) * s;
            w = (xz - zx) * s;
        } else {
            float s = (float)Math.sqrt(1.0 + zz - xx - yy); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (xz + zx) * s;
            y = (zy + yz) * s;
            w = (yx - xy) * s;
        }

        return this;
    }

    /** Set this quaternion to the rotation between two vectors.
     * @param v1 The base vector, which should be normalized.
     * @param v2 The target vector, which should be normalized.
     * @return This quaternion for chaining */
    public TurnQuaternion setFromCross (final Vector3 v1, final Vector3 v2) {
        final float dot = MathUtils.clamp(v1.dot(v2), -1f, 1f);
        final float angle = NumberTools.acos_(dot);
        return setFromAxis(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x, angle);
    }

    /** Set this quaternion to the rotation between two vectors.
     * @param x1 The base vectors x value, which should be normalized.
     * @param y1 The base vectors y value, which should be normalized.
     * @param z1 The base vectors z value, which should be normalized.
     * @param x2 The target vector x value, which should be normalized.
     * @param y2 The target vector y value, which should be normalized.
     * @param z2 The target vector z value, which should be normalized.
     * @return This quaternion for chaining */
    public TurnQuaternion setFromCross (final float x1, final float y1, final float z1, final float x2, final float y2, final float z2) {
        final float dot = MathUtils.clamp(Vector3.dot(x1, y1, z1, x2, y2, z2), -1f, 1f);
        final float angle = NumberTools.acos_(dot);
        return setFromAxis(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2, angle);
    }

    /** Spherical linear interpolation between this quaternion and the other quaternion, based on the alpha value in the range
     * [0,1]. Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/
     * @param end the end quaternion
     * @param alpha alpha in the range [0,1]
     * @return this quaternion for chaining */
    public TurnQuaternion slerp (TurnQuaternion end, float alpha) {
        final float d = this.x * end.x + this.y * end.y + this.z * end.z + this.w * end.w;
        float absDot = d < 0.f ? -d : d;

        // Set the first and second scale for the interpolation
        float scale0 = 1f - alpha;
        float scale1 = alpha;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1 - absDot) > 0.1) {// Get the angle between the 2 quaternions,
            // and then store the sin() of that angle
            final float angle = NumberTools.acos_(absDot);
            final float invSinTheta = 1f / NumberTools.sin_(angle);

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = (NumberTools.sin_((1f - alpha) * angle) * invSinTheta);
            scale1 = (NumberTools.sin_((alpha * angle)) * invSinTheta);
        }

        if (d < 0.f) scale1 = -scale1;

        // Calculate the x, y, z and w values for the quaternion by using a
        // special form of linear interpolation for quaternions.
        x = (scale0 * x) + (scale1 * end.x);
        y = (scale0 * y) + (scale1 * end.y);
        z = (scale0 * z) + (scale1 * end.z);
        w = (scale0 * w) + (scale1 * end.w);

        // Return the interpolated quaternion
        return this;
    }

    /** Spherical linearly interpolates multiple quaternions and stores the result in this TurnQuaternion. Will not destroy the data
     * previously inside the elements of q. result = (q_1^w_1)*(q_2^w_2)* ... *(q_n^w_n) where w_i=1/n.
     * @param q List of quaternions
     * @return This quaternion for chaining */
    public TurnQuaternion slerp (TurnQuaternion[] q) {

        // Calculate exponents and multiply everything from left to right
        final float w = 1.0f / q.length;
        set(q[0]).exp(w);
        for (int i = 1; i < q.length; i++)
            mul(tmp1.set(q[i]).exp(w));
        nor();
        return this;
    }

    /** Spherical linearly interpolates multiple quaternions by the given weights and stores the result in this TurnQuaternion. Will not
     * destroy the data previously inside the elements of q or w. result = (q_1^w_1)*(q_2^w_2)* ... *(q_n^w_n) where the sum of w_i
     * is 1. Lists must be equal in length.
     * @param q List of quaternions
     * @param w List of weights
     * @return This quaternion for chaining */
    public TurnQuaternion slerp (TurnQuaternion[] q, float[] w) {

        // Calculate exponents and multiply everything from left to right
        set(q[0]).exp(w[0]);
        for (int i = 1; i < q.length; i++)
            mul(tmp1.set(q[i]).exp(w[i]));
        nor();
        return this;
    }

    /** Calculates (this quaternion)^alpha where alpha is a real number and stores the result in this quaternion. See
     * http://en.wikipedia.org/wiki/BradQuaternion#Exponential.2C_logarithm.2C_and_power
     * @param alpha Exponent
     * @return This quaternion for chaining */
    public TurnQuaternion exp (float alpha) {

        // Calculate |q|^alpha
        float norm = len();
        float normExp = (float)Math.pow(norm, alpha);

        // Calculate theta
        float theta = NumberTools.acos_(w / norm);

        // Calculate coefficient of basis elements
        float coeff = 0;
        if (Math.abs(theta) < 0.001) // If theta is small enough, use the limit of sin(alpha*theta) / sin(theta) instead of actual
// value
            coeff = normExp * alpha / norm;
        else
            coeff = normExp * NumberTools.sin(alpha * theta) / (norm * NumberTools.sin(theta));

        // Write results
        w = normExp * NumberTools.cos(alpha * theta);
        x *= coeff;
        y *= coeff;
        z *= coeff;

        // Fix any possible discrepancies
        nor();

        return this;
    }

    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + NumberTools.floatToIntBits(w);
        result = prime * result + NumberTools.floatToIntBits(x);
        result = prime * result + NumberTools.floatToIntBits(y);
        result = prime * result + NumberTools.floatToIntBits(z);
        return result;
    }

    @Override
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TurnQuaternion)) {
            return false;
        }
        TurnQuaternion other = (TurnQuaternion)obj;
        return (NumberTools.floatToIntBits(w) == NumberTools.floatToIntBits(other.w))
                && (NumberTools.floatToIntBits(x) == NumberTools.floatToIntBits(other.x))
                && (NumberTools.floatToIntBits(y) == NumberTools.floatToIntBits(other.y))
                && (NumberTools.floatToIntBits(z) == NumberTools.floatToIntBits(other.z));
    }

    /** Get the dot product between the two quaternions (commutative).
     * @param x1 the x component of the first quaternion
     * @param y1 the y component of the first quaternion
     * @param z1 the z component of the first quaternion
     * @param w1 the w component of the first quaternion
     * @param x2 the x component of the second quaternion
     * @param y2 the y component of the second quaternion
     * @param z2 the z component of the second quaternion
     * @param w2 the w component of the second quaternion
     * @return the dot product between the first and second quaternion. */
    public final static float dot (final float x1, final float y1, final float z1, final float w1, final float x2, final float y2,
                                   final float z2, final float w2) {
        return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2;
    }

    /** Get the dot product between this and the other quaternion (commutative).
     * @param other the other quaternion.
     * @return the dot product of this and the other quaternion. */
    public float dot (final TurnQuaternion other) {
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
    }

    /** Get the dot product between this and the other quaternion (commutative).
     * @param x the x component of the other quaternion
     * @param y the y component of the other quaternion
     * @param z the z component of the other quaternion
     * @param w the w component of the other quaternion
     * @return the dot product of this and the other quaternion. */
    public float dot (final float x, final float y, final float z, final float w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    /** Multiplies the components of this quaternion with the given scalar.
     * @param scalar the scalar.
     * @return this quaternion for chaining. */
    public TurnQuaternion mul (float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        this.w *= scalar;
        return this;
    }
    
    /** Get the axis-angle representation of the rotation in turns. The supplied vector will receive the axis (x, y and z values)
     * of the rotation and the value returned is the angle in turns around that axis. Note that this method will alter the
     * supplied vector, the existing value of the vector is ignored. </p> This will normalize this quaternion if needed. The
     * received axis is a unit vector. However, if this is an identity quaternion (no rotation), then the length of the axis may be
     * zero.
     *
     * @param axis vector which will receive the axis
     * @return the angle in turns
     * @see <a href="http://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">wikipedia</a>
     * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle">calculation</a> */
    public float getAxisAngle (Vector3 axis) {
        if (this.w > 1) this.nor(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
        float angle = (2f * NumberTools.acos_(this.w));
        float s = (float)Math.sqrt(1 - this.w * this.w); // assuming quaternion normalised then w is less than 1, so term always positive.
        if (MathUtils.isZero(s)) { // test to avoid divide by zero, s is always positive due to sqrt
            // if s close to zero then direction of axis not important
            axis.x = this.x; // if it is important that axis is normalised then replace with x=1; y=z=0;
            axis.y = this.y;
            axis.z = this.z;
        } else {
            axis.x = (this.x / s); // normalise axis
            axis.y = (this.y / s);
            axis.z = (this.z / s);
        }

        return angle;
    }

    /** Get the angle in turns of the rotation this quaternion represents. Does not normalize the quaternion. Use
     * {@link #getAxisAngle(Vector3)} to get both the axis and the angle of this rotation. Use
     * {@link #getAngleAround(Vector3)} to get the angle around a specific axis.
     * @return the angle in turns of the rotation */
    public float getAngle () {
        return (2f * NumberTools.acos_((this.w > 1) ? (this.w / len()) : this.w));
    }

    /** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
     * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
     * axis perpendicular to the specified axis. </p> The swing and twist rotation can be used to reconstruct the original
     * quaternion: this = swing * twist
     *
     * @param axisX the X component of the normalized axis for which to get the swing and twist rotation
     * @param axisY the Y component of the normalized axis for which to get the swing and twist rotation
     * @param axisZ the Z component of the normalized axis for which to get the swing and twist rotation
     * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
     * @param twist will receive the twist rotation: the rotation around the specified axis
     * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition">calculation</a> */
    public void getSwingTwist (final float axisX, final float axisY, final float axisZ, final TurnQuaternion swing,
                               final TurnQuaternion twist) {
        final float d = Vector3.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
        twist.set(axisX * d, axisY * d, axisZ * d, this.w).nor();
        if (d < 0) twist.mul(-1f);
        swing.set(twist).conjugate().mulLeft(this);
    }

    /** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
     * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
     * axis perpendicular to the specified axis. </p> The swing and twist rotation can be used to reconstruct the original
     * quaternion: this = swing * twist
     *
     * @param axis the normalized axis for which to get the swing and twist rotation
     * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
     * @param twist will receive the twist rotation: the rotation around the specified axis
     * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition">calculation</a> */
    public void getSwingTwist (final Vector3 axis, final TurnQuaternion swing, final TurnQuaternion twist) {
        getSwingTwist(axis.x, axis.y, axis.z, swing, twist);
    }

    /** Get the angle in turns of the rotation around the specified axis. The axis must be normalized.
     * @param axisX the x component of the normalized axis for which to get the angle
     * @param axisY the y component of the normalized axis for which to get the angle
     * @param axisZ the z component of the normalized axis for which to get the angle
     * @return the angle in turns of the rotation around the specified axis */
    public float getAngleAround (final float axisX, final float axisY, final float axisZ) {
        final float d = Vector3.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
        final float l2 = TurnQuaternion.len2(axisX * d, axisY * d, axisZ * d, this.w);
        return MathUtils.isZero(l2) ? 0f : (2f * NumberTools.acos_(MathUtils.clamp(
                (float)((d < 0 ? -this.w : this.w) / Math.sqrt(l2)), -1f, 1f)));
    }

    /** Get the angle in turns of the rotation around the specified axis. The axis must be normalized.
     * @param axis the normalized axis for which to get the angle
     * @return the angle in turns of the rotation around the specified axis */
    public float getAngleAround (final Vector3 axis) {
        return getAngleAround(axis.x, axis.y, axis.z);
    }
}
