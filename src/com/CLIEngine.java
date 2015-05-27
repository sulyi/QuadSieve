package com;

import com.utils.BitMatrix;
import com.utils.QuadSieve;
import com.utils.QuadSieve.Solution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.26.
 * Time: 23:58:32
 * To change this template use File | Settings | File Templates.
 */

public class CLIEngine {

    public static void run(){
        QSInput in = readInput();
        solve(in.n, in.bound, in.length);
    }

    public static QSInput readInput(){
        QSInput input = new QSInput();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.print("\nAdjon meg egy faktoriyálni kívánt számot: ");
            input.n = Long.parseLong(reader.readLine());
            System.out.print("\nAdja meg a faktorbázis felső korlátját: ");
            input.bound = Integer.parseInt(reader.readLine());
            System.out.print("\nAdja meg a szitálási intervallum sugarát: ");
            input.length = Integer.parseInt(reader.readLine());
             System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    public static void solve(long n, int bound, int length){
        int i,j,y;
        int from, ilength;
        QuadSieve QS = new QuadSieve(n);

        BitMatrix factored;

        BitMatrix nullSpace;
        BitSet solution;
        Solution factors;


        try {
            factored = QS.sift(bound, length);
            from = QS.getFrom();
            ilength = QS.getLength();

            System.out.println("Megoldandó kongruencia rendszer:\n");

            System.out.print("\t");
            for(Integer p : QS.factorBase){
                System.out.print(p+"\t");
            }
            System.out.println();
            System.out.print("\t _");
            for(Integer p : QS.factorBase){
                System.out.print("\t_");
            }
            System.out.println("\t_\t_");

            for(i=0;i<ilength;i++){
                System.out.print(from+i+"\t|");
                for( BitSet row : factored)
                    System.out.print( ((row.get(i)) ? 1 : 0)+"\t" );
                System.out.println("=\t0 (mod 2)");
            }
            System.out.println();

            nullSpace = factored.nullSpace();

            System.out.println("A Gauss elimináció után:\n");

            for(i=0;i<ilength;i++)
                System.out.print(from+i+"\t");
            System.out.println();
            for(i=0;i<ilength;i++)
                System.out.print("_\t");
            System.out.println();

            for( BitSet row : factored.reduced() ){
                for(i=0,y=factored.getNumCols();i<y;i++)
                    System.out.print( ((row.get(i)) ? 1 : 0)+"\t" );
                System.out.println();
            }
            System.out.println();

            System.out.println("A nulltér:\n");

            for(i=0;i<ilength;i++){
                System.out.print(from+i+" |");
                for(j=0,y=nullSpace.getNumRows();j<y;j++)
                    System.out.print( ((nullSpace.get(j).get(i)) ? 1 : 0)+" " );
                System.out.println();
            }

            System.out.println();
            for(BitSet vec : freeVar(nullSpace.getNumRows())){
                solution = nullSpace.rDot(vec);

                for(i=0,y=nullSpace.getNumRows();i<y;i++)
                    System.out.print(vec.get(i) ? 1 : 0);
                System.out.println();

                for(i=0,y=nullSpace.getNumCols();i<y;i++)
                    System.out.print(solution.get(i) ? 1 : 0);
                System.out.println();

                for(i=0,y=nullSpace.getNumCols();i<y;i++)
                    System.out.print(solution.get(i) ? (from + i) + " " : "");
                System.out.println();

                factors = QS.getFactors(solution);

                System.out.println(factors.x+" "+factors.y);
                System.out.println(factors.f1+" "+factors.f2);
                System.out.println();

            }
        } catch (InterruptedException ignored) {}
        //TODO: calculate final result
    }

    public static Vector<BitSet> freeVar(int bits) {
        int size = 1 << bits;
        Vector<BitSet> results = new Vector<BitSet>(size);
        for (int val = 1; val < size; val++) {
            BitSet bs = new BitSet(bits);
            results.add(bs);
            int v = val;
            int b = 0;
            while (v != 0) {
                if ( (v & 1) == 1) {
                   bs.set(b);
                }
                b++;
                v >>>= 1;
            }
        }
        return results;
    }

    private static class QSInput {
        public long n;
        public int bound, length;
    }
}
