package com.utils;

import java.util.BitSet;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.14.
 * Time: 19:14:55
 * To change this template use File | Settings | File Templates.
 */

public class EratoSieve {              
    protected static Vector<Integer> primes = new Vector<Integer>();

    protected static BitSet sieve = new BitSet();
    protected static int from;


    public static void sift(int bound) throws InterruptedException {
        sift(bound, Runtime.getRuntime().availableProcessors());
    }

    public static void sift(int bound, int threads) throws InterruptedException {
        // TODO: no negative bound

        int i;
        int p = 2;
        int range;

        if (sieve.isEmpty()){
            sieve.clear(0);
            sieve.clear(1);
            from = 2;
            range = bound / threads;
        } else {
            range = (bound - from) / threads;
        }

        for(i=from;i<bound;i++) sieve.set(i);

        if (threads > 1){
            threads--;

            int f,rm,m;
            Semaphore waitForIt = new Semaphore(0);

            ExecutorService  pool = Executors.newFixedThreadPool(threads);
            Sifter[] sifters = new Sifter[threads];

            for(i=0; i<threads; i++){
                sifters[i] = new Sifter(waitForIt);
                pool.submit(sifters[i]);
            }

            while (p*p <= bound){
                for(i=p*p;i<range;i+=p) sieve.clear(i);
                f = i;
                m = rm = i - range;
                for(i=1; i<threads; f = i * range + m,i++){
                    sifters[i-1].strains.put(new Strain(p,f,i*range));
                    // Chopping overhead
                    m += rm;
                    m %= p;
                }
                sifters[i-1].strains.put(new Strain(p,f,bound));

                p = sieve.nextSetBit(p+1);

            }

            for (i = 0; i < threads; i++) sifters[i].strains.put(new Strain(0,0,0));

            waitForIt.acquire(threads);
            pool.shutdown();

        } else {
            while (p*p <= bound){
                for(i=p*p;i<bound;i+=p) sieve.clear(i);
                p = sieve.nextSetBit(p+1);
            }
        }

        for (i=from;i<bound;i++)
            if(sieve.get(i)) primes.add(i);

        from = bound;
    }

    public static int getBound(){
        return from;
    }

    public static boolean isPrime(int N) throws InterruptedException {
        if(N > from) sift(N);
        return sieve.get(N);
    }

    public static Vector<Integer> getPrimes(){
        return primes;
    }

    public static void clear(){
        from = 0;
        sieve = new BitSet();
        primes.clear();
    }

    private static class Strain {
        int step, from, to;

        Strain(int step, int from, int to){
            this.step = step;
            this.from = from;
            this.to = to;
        }
    }

    private static class Sifter implements Runnable{
        private BlockingQueue<Strain> strains  = new LinkedBlockingQueue<Strain>();
        private Semaphore latch;


        public Sifter(Semaphore latch){
            this.latch = latch;
        }

        public void run(){
            Strain job;
            try {
                while(true){
                        job = strains.take();
                        if (job.step == 0) break;
                        for( int i=job.from;i<job.to;i+=job.step) sieve.clear(i);
                }
            } catch (InterruptedException ignored) {}
            latch.release();
        }

    }

}


