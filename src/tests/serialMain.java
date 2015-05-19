import utils.EratoSieve;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.16.
 * Time: 4:27:34
 * To change this template use File | Settings | File Templates.
 */

public class serialMain {
    public static void main(String[] args){
        EratoSieve.clear();
        long all = System.currentTimeMillis();
        try {
            EratoSieve.sift(3580000,1);
        } catch (InterruptedException e) {
            System.out.println("Test has been interrupted");
        }
        all = System.currentTimeMillis() - all;

        System.out.println("Running single threaded took: " + all + "ms");


    }
}
