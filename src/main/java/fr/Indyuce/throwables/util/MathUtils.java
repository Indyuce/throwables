package fr.Indyuce.throwables.util;

import org.bukkit.Location;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class MathUtils {
    public static float getYawDegrees(Vector vec) {
        return getYawDegrees(vec.getX(), vec.getZ());
    }

    public static float getYawDegrees(double x, double z) {
        return (float) Math.toDegrees(getYawRadians(x, z));
    }

    public static double getYawRadians(Vector vec) {
        return getYawRadians(vec.getX(), vec.getZ());
    }

    private static final double _2PI = 2 * Math.PI, PI_OVER_2 = Math.PI / 2;

    /**
     * Taken directly from the {@link Location#setDirection(Vector)} method
     *
     * @param x x coordinate
     * @param z z coordinate
     * @return Yaw of location that would have the same direction
     */
    public static double getYawRadians(double x, double z) {
        UtilityMethods.isTrue(x != 0 || z != 0, "x and z cannot be simultaneously zero");

        double theta = Math.atan2(-x, z);
        return ((theta + _2PI) % _2PI) + PI_OVER_2;
    }


    public static float getPitchDegrees(Vector vec) {
        return getPitchDegrees(vec.getX(), vec.getY(), vec.getZ());
    }

    public static float getPitchDegrees(double x, double y, double z) {
        return (float) Math.toDegrees(getPitchRadians(x, y, z));
    }

    public static double getPitchRadians(Vector vec) {
        return getPitchRadians(vec.getX(), vec.getY(), vec.getZ());
    }

    public static double getPitchRadians(double x, double y, double z) {
        double xz = Math.sqrt(x * x + z * z);
        return Math.atan2(-y, xz);
    }

    /**
     * Source:
     * - https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
     * <p>
     * Euler's rotation theorem states that any rotation can be described
     * by only one axis of rotation (and one angle).
     * <p>
     * Unit quaternions (4D generalization of complex numbers) can be used to fully
     * caracterize one rotation. It contains both the info about the axis
     * of rotation in its imaginary part, and the angle in its real part.
     * <p>
     * Let q = q1 + i.q2 + j.q3 + k.q4
     * Unit quaternion implies q1^2 + q2^2 + q3^2 + q4^2 = 1
     * <p>
     * This method is used because Minecraft utilizes Euler angles to parametrize
     * armor stand head poses, while it's easier to describe a sword rotation using XYZ
     * i.e quaternions
     *
     * @param q0 Quaternion real part
     * @param q1 Quaternion imaginary part, i component
     * @param q2 Quaternion imaginary part, j component
     * @param q3 Quaternion imaginary part, k component
     * @return Euler angle of the given quaternion
     */
    public static EulerAngle getEulerAngle(double q0, double q1, double q2, double q3) {
        double roll = Math.atan2(2 * (q0 * q1 + q2 * q3), 1 - 2 * (q1 * q1 + q2 * q2));
        double pitch = Math.asin(2 * (q0 * q2 - q3 * q1));
        double yaw = Math.atan2(2 * (q0 * q3 + q1 * q2), 1 - 2 * (q2 * q2 + q3 * q3));
        return new EulerAngle(roll, pitch, yaw);
    }

    /**
     * Source:
     * - https://eater.net/quaternions
     * <p>
     * See {@link #getEulerAngle(double, Vector)}
     *
     * @param a    Angle of rotation
     * @param axis Axis of rotation
     * @return Corresponding Euler angles
     */
    public static EulerAngle getEulerAngle(double a, Vector axis) {

        // Make sure the axis is normalized
        double x = axis.getX();
        double y = axis.getY();
        double z = axis.getZ();

        // Imaginary part coefficient
        double sin = Math.sin(a / 2);

        return getEulerAngle(Math.cos(a / 2), sin * x, sin * y, sin * z);
    }
}
