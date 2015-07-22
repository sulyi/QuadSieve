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
    private Vector<Integer> smoothNumbers;
    private Vector<Integer> smoothX;
    private Vector<Integer> smoothCheck;
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
        this.smoothCheck = new Vector<Integer>(this.length);

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        Vector<Future<BitSet>> futureExponentMatrix = new Vector<Future<BitSet>>();
        BitMatrix exponentMatrix = new BitMatrix(this.length);
        BitSet column;
        
        BitMatrix smoothMatrix;
        BitSet sign = new BitSet(this.length);
        long l;
        int i;

        //EratoSieve.clear();
        EratoSieve.sift(bound);

        for(i=from;i<=to;i++){
            l = i * i -n;
            if (l > Integer.MIN_VALUE || l < Integer.MAX_VALUE){
                this.sievingInterval.add((int) l);
                this.smoothCheck.add((int) l);
            }else
                throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
            if (l<0) sign.set(i-from);
        }

        exponentMatrix.add(sign);

        factorBase.add(-1);
        for (Integer p : EratoSieve.getPrimes()) {
            i = IntMath.legendre(n, p);
            if ( i == 1 ) {
                factorBase.add(p);
                futureExponentMatrix.add( pool.submit(new Sifter(p)) );
            }
        }
        i = 1;
        for( Future<BitSet> fcolumn : futureExponentMatrix ){
            try {
                column = fcolumn.get();
                if (column != null){
                    exponentMatrix.add(column);
                    i++;
                }else
                    factorBase.removeElementAt(i);
            } catch (ExecutionException e) {
                // TODO: raise correct exception
                e.printStackTrace();
            }
        }

        pool.shutdown();

        exponentMatrix.transpose();

        smoothMatrix = new BitMatrix(factorBase.size());
        this.smoothNumbers = new Vector<Integer>();
        this.smoothX = new Vector<Integer>();

        for(i=0;i<this.length;i++){
            if (Math.abs(this.smoothCheck.get(i)) == 1){
                smoothMatrix.add(exponentMatrix.get(i));
                this.smoothNumbers.add(sievingInterval.get(i));
                this.smoothX.add(i+from);
            }
        }

        smoothMatrix.transpose();

        return smoothMatrix;
    }

    public Solution getFactors(BitSet solution){
        int i,l;
        long x = 1, y;
        double yp = 1;

        Solution factors = new Solution();

        // TODO: use nextSetBit instead
        for(i=0,l=this.smoothX.size();i<l;i++){
            if (solution.get(i)){
                x *= (this.smoothX.get(i));
                x %= n;
                // FIXME: OVERFLOW !
                yp *= Math.sqrt((double) Math.abs(smoothNumbers.get(i)));
            }
        }
        y = (long) Math.round(yp) % n;

        if (x > y){
            factors.x = x;
            factors.y = y;
        } else {
            factors.x = y;
            factors.y = x;
        }
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
        return smoothX;
    }

    public class Solution{
        public long x,y;
    }

    private class Sifter implements Callable<BitSet> {
        private int step;

        public Sifter(int step){
            this.step = step;
        }

        @Override
        public BitSet call() {
            int i,p,exp, fx;
            int s1 = 0,s2;
            int offset = from % step;
            
            try{
                s1 = IntMath.tonelliShanks(n,step);
            } catch (ArithmeticException e){
                return null;
            }
            
            s2 = step - s1;

            s1 -= offset;
            s2 -= offset;
            s1 += step;
            s2 += step;
            s1 %= step;
            s2 %= step;

            BitSet sieve = new BitSet(length);

            for (i=0;i<length;i+=step){
                p = step * step;
                exp = 1;
                if (i+s1 < length){
                    //System.out.println(from+i+" "+sievingInterval.get(i+s1));
                    fx = sievingInterval.get(i+s1);
                    while ( fx % p == 0 ) {
                        exp++;
                        p*=step;
                    }
                    p/=step;
                    synchronized (smoothCheck){
                        fx = smoothCheck.get(i+s1);
                        smoothCheck.set(i+s1,fx/p);
                    }
                    if ( (exp & 1) == 1 )
                        sieve.set(i+s1);
                }
                p = step * step;
                exp = 1;
                if (i+s2 < length && s1 != s2){
                    fx = sievingInterval.get(i+s2);
                    while ( fx % p == 0 ) {
                        exp++;
                        p*=step;
                    }
                    p/=step;
                    synchronized (smoothCheck){
                        fx = smoothCheck.get(i+s2);
                        smoothCheck.set(i+s2,fx/p);
                    }
                    if ( (exp & 1) == 1 )
                        sieve.set(i+s2);
                }

            }

            return sieve;
        }
    }

}
