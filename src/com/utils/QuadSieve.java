package com.utils;

import com.binalg.BitMatrix;

import java.util.BitSet;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.16.
 * Time: 18:25:39
 * To change this template use File | Settings | File Templates.
 */

public class QuadSieve {
    // TODO: these should be also private with getters
    public long n;
    public Vector<Integer> factorBase;

    private int root;

    private Vector<Integer> sievingInterval;
    private int from, to, length;

    public QuadSieve(long n) {
        this.n = n;
        this.root = (int) Math.sqrt((double) n);
        this.factorBase = new Vector<Integer>();
    }

    public BitMatrix sift(int bound, int length) throws InterruptedException {
        return sift(bound, length, Runtime.getRuntime().availableProcessors());
    }

    public BitMatrix sift(int bound, int length, int threads) throws InterruptedException {
        // TODO: implement possibility of continued sieving

        this.from = root - length;
        this.to = root + length;
        this.length = 2*length+1;
        this.sievingInterval = new Vector<Integer>(this.length);

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        Vector<Future<BitSet>> futureExponentMatrix = new Vector<Future<BitSet>>();
        BitMatrix exponentMatrix = new BitMatrix(this.length);
        BitSet sign = new BitSet(this.length);
        long l;

        //EratoSieve.clear();
        EratoSieve.sift(bound);

        for(int i=from;i<=to;i++){
            l = i * i -n;
            if (l > Integer.MIN_VALUE || l < Integer.MAX_VALUE)
                this.sievingInterval.add((int) l);
            else
                throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
            if (l<0) sign.set(i-from);
        }

        exponentMatrix.add(sign);

        factorBase.add(-1);
        for (Integer p : EratoSieve.getPrimes()) {
            if (IntMath.legendre(n, p) == 1) {
                factorBase.add(p);
                futureExponentMatrix.add( pool.submit(new Sifter(p)) );
            }
        }

        for( Future<BitSet> column : futureExponentMatrix ){
            try {
                exponentMatrix.add(column.get());
            } catch (ExecutionException e) {
                // TODO: raise correct exception
                e.printStackTrace();
            }
        }

        pool.shutdown();

        return exponentMatrix;
    }

    public Solution getFactors(BitSet solution){
        int i;
        long x = 1, y;
        double yp = 1;
        long gcd1, gcd2;

        Solution factors = new Solution();

        for(i=0;i<this.length;i++){
            if (solution.get(i)){
                x *= (from + i);
                x %= n;
                // FIXME: OVERFLOW !
                yp *= Math.sqrt((double) Math.abs(sievingInterval.get(i)));
            }
        }
        y = (long) yp % n;

        if (x > y){
            factors.x = x;
            factors.y = y;
        } else {
            factors.x = y;
            factors.y = x;
        }

        factors.f1 = factors.x+factors.y;
        factors.f2 = factors.x-factors.y;
        return factors;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getLength() {
        return length;
    }

    public int getRoot() {
        return root;
    }

    public Vector<Integer> getInterval(){
        return sievingInterval;
    }

    public class Solution{
        public long x,y,f1,f2;
    }

    private class Sifter implements Callable<BitSet> {
        private int step;

        public Sifter(int step){
            this.step = step;
        }

        @Override
        public BitSet call() {
            int i,p,e;

            int s1 = IntMath.tonelliShanks(n,step);
            int s2 = step - s1;

            s1 -= from % step;
            s2 -= from % step;
            s1 += step;
            s2 += step;
            s1 %= step;
            s2 %= step;

            BitSet sieve = new BitSet(length);

            for (i=0;i<length;i+=step){
                p = step * step;
                e = 1;
                if (i+s1 < length){
                    //System.out.println(from+i+" "+sievingInterval.get(i+s1));
                    while ( sievingInterval.get(i+s1) % p == 0 ) {
                        e++;
                        p*=step;
                    }

                    if ( (e & 1) == 1 )
                        sieve.set(i+s1);
                }
                p = step * step;
                e = 1;
                if (i+s2 < length){
                    while ( sievingInterval.get(i+s2) % p == 0 ) {
                        e++;
                        p*=step;
                    }

                    if ( (e & 1) == 1 )
                        sieve.set(i+s2);
                }

            }

            return sieve;
        }
    }

}
