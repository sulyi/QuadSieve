package utils;

import java.util.BitSet;
import java.util.Vector;
import java.lang.Math;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.16.
 * Time: 18:25:39
 * To change this template use File | Settings | File Templates.
 */

public class QuadSieve {
    public long n;
    public Vector<Integer> factorBase;

    private int root;

    private Vector<Long> sievingInterval;
    private int from, to, length;

    public QuadSieve(long n) {
        this.n = n;
        this.root = (int) Math.sqrt((double) n);
        this.factorBase = new Vector<Integer>();
    }

    public void sift(int bound, int length, int threads) throws InterruptedException {
        // TODO: implement possibility of continued sieving
        int i;

        this.from = root - length;
        this.to = root + length;
        this.length = 2*length+1;
        this.sievingInterval = new Vector<Long>(this.length);

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        Vector<Future<BitSet>> futureExponentMatrix = new Vector<Future<BitSet>>();
        BitMatrix exponentMatrix = new BitMatrix();
        BitSet sign = new BitSet(this.length);

        EratoSieve.clear();
        EratoSieve.strain(bound);

        for(i=from;i<=to;i++){
            this.sievingInterval.add(i * i - n);
            if (i<root) sign.set(i-from);
        }

        exponentMatrix.add(sign);

        factorBase.add(-1);
        for (Integer p : EratoSieve.primes) {
            if (IntMath.legendre(n, p) == 1) {
                factorBase.add(p);
                futureExponentMatrix.add( pool.submit(new Sifter(p)) );
            }
        }

        //TODO: create bitmatrix
        for( Future<BitSet> column : futureExponentMatrix ){
            try {
                exponentMatrix.add(column.get());
            } catch (ExecutionException e) {
                // TODO: raise correct exception
                e.printStackTrace();
            }
        }

        pool.shutdown();

        for(Integer p : factorBase){
            System.out.print(p+" ");
        }
        System.out.println();

        for(i=0;i<this.length;i++){
            System.out.print(from+i+" ");
            for( BitSet column : exponentMatrix ){
                System.out.print( ((column .get(i)) ? 1 : 0)+" " );
            }
            System.out.println();
        }

        exponentMatrix.transpose();
        exponentMatrix.gaussElimination();
        
        //TODO: Gauss elimination
        //TODO: calculate final result
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
