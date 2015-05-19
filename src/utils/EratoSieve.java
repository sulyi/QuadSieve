package utils;

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
    public final static Vector<Integer> primes = new Vector<Integer>();

    private static BitSet sieve = new BitSet();
    private static int from;
    
    private static Semaphore waitHere;
    private static BlockingQueue<Strain> strains;
    private static ExecutorService pool;

    public static void strain(int bound) throws InterruptedException {
        //TODO: uncomment and delete after fixing sift
        //sift(bound, Runtime.getRuntime().availableProcessors());
        sift(bound, 1);
    }

    public static void sift(int bound, int threads) throws InterruptedException {
        //TODO: no negative bound
        //FIXME: bug somewhere in continued sieving
        int i;
        int p = 2;
        int range;
        int f,m;

        pool = Executors.newFixedThreadPool(threads);
        strains = new ArrayBlockingQueue<Strain>(threads);
        waitHere = new Semaphore(0);

        for(i=0; i<threads; i++){
            pool.submit(new Sifter());
        }

        if (sieve.isEmpty()){
            sieve.clear(0);
            sieve.clear(1);
            from = 2;
            range = bound / threads;
        } else {
            // BitSet initialized with length of registers
            range = (bound - from) / threads;
        }

        for(i=from;i<bound;i++) sieve.set(i);

        //TODO: complete overhaul of parallelization

        while (p*p <= bound){

            for(i=threads; i>0; i--){
                f = (i-1)*range;
                if ( f < p*p ) f = p*p;
                else{
                    // Chopping overhead !!!
                    m = f % p;
                    if( m != 0){
                        f += p;
                        f -= m;
                    }
                    //f += (p - f % p) % p;
                    //while(f % p != 0) f++;
                }
                strains.put(new Strain(p,f,i*range));

            }

            waitHere.acquire(threads);

            p = sieve.nextSetBit(p+1);

        }

        for (i = 0; i < threads; i++) strains.put(new Strain(0,0,0));

        pool.shutdown();

        for (i=from;i<bound;i++)
            if(sieve.get(i)) primes.add(i);

        from = bound;
    }

    public static int getBound(){
        return from;
    }

    public static boolean isPrime(int N) throws InterruptedException {
        if(N > from) strain(N);
        return sieve.get(N);
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
        public void run(){
            Strain job;
            try {
                while(true){

                        job = strains.take();
                        if (job.step == 0) break;
                        for( int i=job.from;i<job.to;i+=job.step) sieve.clear(i);
                        waitHere.release();
                }
            } catch (InterruptedException ignored) {}
        }

    }

}


