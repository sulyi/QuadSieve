package com.utils;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.16.
 * Time: 16:48:37
 * To change this template use File | Settings | File Templates.
 */


public class IntMath {

    public static int legendre(long n, int p) {
        if (n % p == 0) {
            if (p == 1) return 1;
            return 0;
        }
        long Euler = modpow(n, (p - 1) / 2, p);
        if (Euler == 1) {
            return 1;
        } else if (Euler == p - 1) {
            return -1;
        } else {
            throw new ArithmeticException("Error computing the IntMath legendre: (" + n + "/" + p + ")");
        }
    }

    public static int tonelliShanks(long n, int p) {
        if (p == 2) return (int) n % p;

        int Q = p - 1;
        int S = 0;
        int i, j;
        long z, b, c, R, t, tt;

        while ((Q & 1) == 0) {
            S++;
            Q >>= 1;
        }

        z = 0;
        while (legendre(z, p) != -1)
            z++;

        c = modpow(z, Q, p);
        R = modpow(n, (Q + 1) / 2, p);
        t = modpow(n, Q, p);

        while (t % p != 1) {
            tt = t * t;
            i = 1;
            while (tt % p != 1) {
                tt *= tt;
                i++;
            }
            b = c;
            for (j = S - i - 1; j > 0; j--) {
                b *= b;
                b %= p;
            }
            c = (b * b) % p;

            R *= b;
            R %= p;

            t *= c;
            t %= p;

            S = i;

        }

        return (int) R;
    }

    public static long modpow(long base, int exp, int m) {
        long result = 1;
        while (exp != 0) {
            if ((exp & 1) == 1)
                result *= base;
            result %= m;
            exp >>= 1;
            base *= base;
            base %= m;
        }
        return result;
    }

    public static long binGcd(long a, long b) {
        int shift;
        long t;

        if (a == 0)
            return b;
        if (b == 0)
            return a;

        for (shift = 0; ((a | b) & 1) == 0; ++shift) {
            a >>= 1;
            b >>= 1;
        }

        while ((a & 1) == 0)
            a >>= 1;

        do {
            while ((b & 1) == 0)
                b >>= 1;

            if (a > b) {
                t = b;
                b = a;
                a = t;
            }
            b = b - a;
        } while (b != 0);

        return a << shift;
    }

}
