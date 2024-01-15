/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.utils;

public final class FastMath {
   private static final double F2 = -0.5;
   private static final double F4 = -F2 / (3.0 * 4.0);
   private static final double F6 = -F4 / (5.0 * 6.0);
   private static final double F8 = -F6 / (7.0 * 8.0);
   private static final double F10 = -F8 / (9.0 * 10.0);
   private static final double F12 = -F10 / (11.0 * 12.0);
   private static final double F14 = -F12 / (13.0 * 14.0);
   private static final double F16 = -F14 / (15.0 * 16.0);
   private static final double PI2 = 2.0 * Math.PI;
   private static final double PI05 = 0.5 * Math.PI;
   private static final int ATAN2_BITS = 7;
   private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
   private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
   private static final int ATAN2_COUNT = ATAN2_MASK + 1;
   private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
   private static final float ATAN2_DIM_MINUS_1 = (ATAN2_DIM - 1);
//   private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);
   private static final float DEG = 180.0f / (float) Math.PI;
   public static final float[] ATAN2 = new float[ATAN2_COUNT];
   private static final int BIG_ENOUGH_INT = 16 * 1024;
   private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT + 0.0000;
   private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5000;
   private static final double BIG_ENOUGH_CEIL = BIG_ENOUGH_INT + 0.9999;
   private final static int ROUND_TO = 5;
   private final static double ROUNDING = Math.pow(10, ROUND_TO);

   /**
    * @author Riven
    */
   static {
      for (int i = 0; i < ATAN2_DIM; i++) {
         for (int j = 0; j < ATAN2_DIM; j++) {
            float x0 = (float) i / ATAN2_DIM;
            float y0 = (float) j / ATAN2_DIM;
            ATAN2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
         }
      }
   }

   /**
    * @author Markus Persson
    * @param a
    * @return absolute int
    */
   public static int abs(int a) {
      return (a ^ (a >> 31)) - (a >> 31);
   }

   /**
    * @author Riven
    * @param y
    * @param x
    * @return float
    */
   public static float atan2DegLookup(float y, float x) {
      return FastMath.atan2Lookup(y, x) * DEG;
   }

   /**
    * @author Riven
    * @param y
    * @param x
    * @return float
    */
   public static float atan2DegStrict(float y, float x) {
      return (float) Math.atan2(y, x) * DEG;
   }

   /**
    * @author Riven
    * @param y
    * @param x
    * @return float
    */
   public static float atan2Lookup(float y, float x) {
      float add, mul;

      if (x < 0.0f) {
         if (y < 0.0f) {
            x = -x;
            y = -y;
            mul = 1.0f;
         } else {
            x = -x;
            mul = -1.0f;
         }
         add = -3.141592653f;
      } else {
         if (y < 0.0f) {
            y = -y;
            mul = -1.0f;
         } else {
            mul = 1.0f;
         }
         add = 0.0f;
      }
      float invDiv = ATAN2_DIM_MINUS_1 / ((x < y) ? y : x);
      int xi = (int) (x * invDiv);
      int yi = (int) (y * invDiv);

      return (ATAN2[yi * ATAN2_DIM + xi] + add) * mul;
   }

   /**
    * @author Bruno Augier (DzzD) Compute and return cosinus of its parameter using Taylor series
    * @param x angle in radian to
    * @return cosinus value for the given parameter
    */
   public static double cosLookup(double x) {
      if (x < 0.0) {
         x = -x;
      }

      if (x < PI2) {
         if (x < Math.PI) {
            double x2 = x * x;
            return 1.0 + x2 * (F2 + x2 * (F4 + x2 * (F6 + x2 * (F8 + x2 * (F10 + x2 * (F12 + x2 * (F14 + x2 * (F16))))))));
         } else {
            x -= Math.PI;
            double x2 = x * x;
            return -(1.0 + x2 * (F2 + x2 * (F4 + x2 * (F6 + x2 * (F8 + x2 * (F10 + x2 * (F12 + x2 * (F14 + x2 * (F16)))))))));
         }
      }

      x %= PI2;
      x -= Math.PI;
      double x2 = x * x;

      return -(1.0 + x2 * (F2 + x2 * (F4 + x2 * (F6 + x2 * (F8 + x2 * (F10 + x2 * (F12 + x2 * (F14 + x2 * F16))))))));
   }

   /**
    * @author Riven
    * @param x
    * @return int
    */
   public static int fastCeil(float x) {
      return (int) (x + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
   }

   /**
    * @author Jeff's Game Gems
    * @param radians
    * @return cosine double
    */
   public static double fastCos(double radians) {
      return fastSin(radians + Math.PI / 2);
   }

   /**
    * @author Riven
    * @param x
    * @return int
    */
   public static int fastFloor(float x) {
      return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
   }

   /**
    * @author Riven
    * @param x
    * @return int
    */
   public static int fastRound(float x) {
      return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
   }

   /**
    * @author Jeff's Game Gems
    * @param radians
    * @return sine double
    */
   public static double fastSin(double radians) {
      radians = reduceSinAngle(radians); // limits angle to between -PI/2 and +PI/2
      if (Math.abs(radians) <= Math.PI / 4) {
         return Math.sin(radians);
      } else {
         return Math.cos(Math.PI / 2 - radians);
      }
   }

   /**
    *
    * @param x1
    * @param y1
    * @param x2
    * @param y2
    * @return distance
    */
   public static double getDistanceBetweenPoints(double x1, double y1, double x2, double y2) {
      double x = Math.pow((x1 - x2), 2);
      double y = Math.pow((y1 - y2), 2);
      double t = x + y;

      return Math.sqrt(t);
   }

   /**
    * Round the double to "roundTo" decimal places.
    *
    * @param value
    * @return double
    */
   public static double roundOff(double value) {
      long temp = (long) ((value * ROUNDING));
      return (((double) temp) / ROUNDING);
   }

   /**
    * @author David Brackeen Returns the sign of the number. Returns -1 for negative, 1 for positive, and 0 otherwise.
    *
    * @param v
    * @return int
    */
   public static int signOfNumber(long v) {
      return (v > 0) ? 1 : (v < 0) ? -1 : 0;
   }

   /**
    * @author David Brackeen Returns the sign of the number. Returns -1 for negative, 1 for positive, and 0 otherwise.
    *
    * @param v
    * @return int
    */
   public static int signOfNumber(double v) {
      return (v > 0) ? 1 : (v < 0) ? -1 : 0;
   }

   /**
    * @author Bruno Augier (DzzD) Compute and return sinus of its parameter using Taylor series
    * @param x angle in radian to
    * @return sinus value for the given parameter
    */
   public static double sinLookup(double x) {
      return cosLookup(x - PI05);
   }

   /**
    * @author Jeff's Game Gems This forces the trig functions to stay within the safe area on the x86 processor (-45
    * degrees to +45 degrees) The results may be very slightly off from what the Math and StrictMath trig functions give
    * due to rounding in the angle reduction but it will be very very close.
    */
   private static double reduceSinAngle(double radians) {
      radians %= Math.PI * 2.0; // put us in -2PI to +2PI space
      if (Math.abs(radians) > Math.PI) { // put us in -PI to +PI space
         radians = radians - (Math.PI * 2.0);
      }
      if (Math.abs(radians) > Math.PI / 2) {// put us in -PI/2 to +PI/2 space
         radians = Math.PI - radians;
      }

      return radians;
   }
}
