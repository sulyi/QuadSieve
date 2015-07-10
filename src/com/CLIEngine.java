package com;

import com.binalg.BitMatrix;
import com.binalg.SimpleBitMatrix;
import com.utils.IntMath;
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
    // TODO: undo static

    public static void run(){
        QSInput in = readInput();
        solve(in.n, in.bound, in.length);
    }

    public static QSInput readInput(){
        QSInput input = new QSInput();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println();
            System.out.print("Adjon meg egy faktorizálni kívánt számot: ");
            input.n = Long.parseLong(reader.readLine());
            System.out.println();
            System.out.print("Adja meg a faktorbázis felső korlátját: ");
            input.bound = Integer.parseInt(reader.readLine());
            System.out.println();
            System.out.print("Adja meg a szitálási intervallum sugarát: ");
            input.length = Integer.parseInt(reader.readLine());
             System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    public static void solve(long n, int bound, int length){
        int i,j,x,y;
        int iLength;
        QuadSieve QS = new QuadSieve(n);

        BitMatrix factored;

        SimpleBitMatrix nullSpace;
        BitSet solution;
        Solution factors;

        String scoreTab = "__";

        long gcd1,gcd2;

        try {
            factored = QS.sift(bound, length);
            iLength = QS.getInterval().size();

            System.out.println("Megoldandó kongruencia rendszer:");
            System.out.println();

            System.out.print("    ");
            for(i=0;i< iLength;i++)
                System.out.printf("%4d   ",QS.getInterval().get(i));
            System.out.println();
            System.out.print(scoreTab);
            for(i=0;i< iLength;i++)
                System.out.print(scoreTab + scoreTab+"___");
            System.out.println(scoreTab + scoreTab +"_______");

            for(i=0,y=QS.factorBase.size();i<y;i++){
                System.out.printf("%2d| ",QS.factorBase.get(i));
                for(j=0,solution=factored.get(i);j<iLength-1;j++)
                    System.out.print( ((solution.get(j)) ? 1 : 0)+"·x"+(j+1)+" + " );
                System.out.println( ((solution.get(j)) ? 1 : 0)+"·x"+(j+1)+" ≡ 0 (mod 2)" );
            }
            System.out.println();

            System.out.println("A mátrix:");
            System.out.println();

            for( BitSet row : factored ){
                for(i=0,y=factored.getNumCols();i<y;i++)
                    System.out.print( ((row.get(i)) ? 1 : 0)+" " );
                System.out.println();
            }
            System.out.println();

            nullSpace = factored.nullSpace();

            System.out.println("Gauss elimináció után:");
            System.out.println();

            /*
            for(i=0;i<iLength;i++)
                System.out.print(from+i+"\t");
            System.out.println();
            for(i=0;i<iLength;i++)
                System.out.print(scoreTab);
            System.out.println();
            */

            for( BitSet row : factored.getReduced() ){
                for(i=0,y=factored.getNumCols();i<y;i++)
                    System.out.print( ((row.get(i)) ? 1 : 0)+" " );
                System.out.println();
            }
            System.out.println();

            System.out.println("A nulltér:");
            System.out.println();

            for(i=0,y=nullSpace.getNumRows();i< y;i++){
                System.out.print(QS.getInterval().get(i)+" |");
                for(j=0,x=nullSpace.getNumCols();j<x;j++)
                    System.out.print( ((nullSpace.get(i).get(j)) ? 1 : 0)+" " );
                System.out.println();
            }
            System.out.println();

            //TODO: calculate final result parallel

            nullSpace.transpose();
            //for(BitSet row : nullSpace){
            for(BitSet row : freeVar(nullSpace.getNumRows())){

                /*
                System.out.print('[');
                for(i=0,y=nullSpace.getNumRows();i<y;i++)
                    System.out.print(row.get(i) ? 1 : 0);
                System.out.println(']');
                */
                row = nullSpace.lDot(row);
                /*
                System.out.print('[');
                for(i=0,y=nullSpace.getNumCols();i<y;i++)
                    System.out.print(row.get(i) ? 1 : 0);
                System.out.println(']');
                */
                factors = QS.getFactors(row);

                gcd1 = IntMath.binGcd(factors.x+factors.y,n);
                if (gcd1 == 1 | gcd1 == n)
                    continue;

                gcd2 = IntMath.binGcd(factors.x-factors.y,n);
                if (gcd2 == 1 | gcd2 == n)
                    continue;

                for(j=0,y=row.cardinality()-1,i=row.nextSetBit(0);j<y;j++,i=row.nextSetBit(i+1))
                    System.out.print(QS.getInterval().get(i) + ", ");
                System.out.println(QS.getInterval().get(i));

                System.out.println("x = "+factors.x+" y = "+factors.y);
                System.out.println("x+y = "+(factors.x+factors.y)+", x-y = "+(factors.x-factors.y));
                System.out.printf("(%d,%d) = %d, (%d,%d) = %d",factors.x+factors.y,n,gcd1,factors.x-factors.y,n,gcd2);
                System.out.println();
                System.out.println();

            }
        } catch (InterruptedException ignored) {}
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
