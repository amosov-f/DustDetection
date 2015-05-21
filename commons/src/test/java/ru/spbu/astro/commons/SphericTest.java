package ru.spbu.astro.commons;

import org.junit.Test;
import ru.spbu.astro.util.MathTools;

import static java.lang.Math.toRadians;
import static org.junit.Assert.assertEquals;

/**
 * User: amosov-f
 * Date: 29.04.15
 * Time: 0:48
 */
@SuppressWarnings("MagicNumber")
public final class SphericTest {
    private static final double EPS = 1e-6;

    @Test
    public void toEquatorialTest() {
        final EquatorialSpheric center =  new Spheric(0, 0).toEquatorial();
        assertEquals(266.404929, Math.toDegrees(center.getRa()), EPS);
        assertEquals(-28.936217, Math.toDegrees(center.getDec()), EPS);
        final EquatorialSpheric cloud = new Spheric(toRadians(2.5), toRadians(21.74)).toEquatorial();
        assertEquals(16.577661, MathTools.toHours(cloud.getRa()), EPS);
        assertEquals(-14.467725, Math.toDegrees(cloud.getDec()), EPS);
//        System.out.println(new Spheric(toRadians(5), toRadians(19.47)).toEquatorial());
//        System.out.println(new Spheric(toRadians(5), toRadians(24.04)).toEquatorial());
//        System.out.println();
//        System.out.println(new Spheric(toRadians(22.5), toRadians(4.25)).toEquatorial());
//        System.out.println(new Spheric(toRadians(20), toRadians(6.38)).toEquatorial());
//        System.out.println(new Spheric(toRadians(17.5), toRadians(8.52)).toEquatorial());
//        System.out.println();
//        System.out.println(new Spheric(toRadians(175), toRadians(-10.67)).toEquatorial());
//        System.out.println(new Spheric(toRadians(172.5), toRadians(-12.84)).toEquatorial());
//        System.out.println(new Spheric(toRadians(177.5), toRadians(-17.24)).toEquatorial());
//        System.out.println(new Spheric(toRadians(170), toRadians(-15.03)).toEquatorial());
//        System.out.println(new Spheric(toRadians(185), toRadians(-24.04)).toEquatorial());
//        System.out.println(new Spheric(toRadians(182.5), toRadians(-26.39)).toEquatorial());
//        System.out.println();
//        System.out.println(new Spheric(toRadians(228.21), toRadians(52.97)).toEquatorial());
//        System.out.println(new Spheric(toRadians(221.79), toRadians(52.97)).toEquatorial());
//        System.out.println(new Spheric(toRadians(225), toRadians(55.70)).toEquatorial());
    }
}