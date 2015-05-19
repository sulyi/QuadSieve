import utils.EratoSieve;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.16.
 * Time: 4:30:16
 * To change this template use File | Settings | File Templates.
 */

public class parallelMain {
    public static void main(String[] args){
        EratoSieve.clear();
        long all = System.currentTimeMillis();
        try {
            EratoSieve.strain(3580000);
        } catch (InterruptedException e) {
            System.out.println("Test has been interrupted");
        }
        all = System.currentTimeMillis() - all;

        System.out.println("Running" + Runtime.getRuntime().availableProcessors() + " threaded took: " + all + "ms");


    }
}