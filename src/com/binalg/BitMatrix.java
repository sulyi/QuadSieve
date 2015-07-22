package com.binalg;

import java.util.BitSet;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.19.
 * Time: 10:56:13
 * To change this template use File | Settings | File Templates.
 */

//TODO: cleanup out commented lines and todo's

public class BitMatrix extends SimpleBitMatrix {

    private BlockingQueue<AddRows> addQueue;
    private Semaphore waitHere;

    //private Vector<Integer> swaps;
    protected SimpleBitMatrix reducedRows;
    protected BitSet pivots;

    public BitMatrix(int numCols) {
        super(numCols);
    }

    public BitMatrix(BitSet[] rows, int numCols) {
        super(rows, numCols);
    }

    public void gaussianElimination() {
        gaussianElimination(Runtime.getRuntime().availableProcessors());
    }

    public void gaussianElimination(int threads) {

        int numRows = rows.size();
        int i, j, ia;
        BitSet pivots = new BitSet(numCols);

        //swaps = new Vector<Integer>();

        reducedRows = createCopy();

        waitHere = new Semaphore(0);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        addQueue = new ArrayBlockingQueue<>(threads);

        try {
            for (i = 0; i < threads; i++)
                pool.submit(new RowAdder());

            int diff = 0;
            for (i = 0; i < numCols; i++) {
                j = ia = i - diff;
                while (j < numRows && !reducedRows.get(j).get(i))
                    j++;
                if (j == numRows) {
                    diff++;
                    continue;
                }

                if (j > ia) {
                    swap(ia, j);
                    //swaps.add(ia);
                    //swaps.add(j);
                }

                pivots.set(i);
                for (j = 0; j < numRows; j++) {
                    //if (rows.get(j).get(i) && j != ia)
                    if (reducedRows.get(j).get(i) && j != ia)
                        addQueue.put(new AddRows(ia, j));
                    else
                        waitHere.release();
                }
                waitHere.acquire(numRows);

            }

            for (i = 0; i < threads; i++)
                addQueue.put(new AddRows(numRows, numRows));

            pool.shutdown();

            this.pivots = pivots;
        } catch (InterruptedException e) {
            this.pivots = null;
            this.reducedRows = null;
        }

    }

    public SimpleBitMatrix nullSpace() {
        int i, j, k, l;
        BitSet row;
        gaussianElimination();
        if (reducedRows != null && pivots != null) {
            int numNullCols = numCols - pivots.cardinality();

            SimpleBitMatrix nullSpace = new SimpleBitMatrix(numNullCols);

            for (j = 0, i = 0; i < numCols; i++) {
                row = new BitSet(numNullCols);
                if (pivots.get(i)) {
                    for (l = pivots.nextClearBit(0), k = 0; k < numNullCols; k++, l = pivots.nextClearBit(l + 1))
                        row.set(k, reducedRows.get(j).get(l));
                    j++;
                } else {
                    row.set(i - j);
                }
                nullSpace.add(row);
            }
            // TODO: create rDot
            return nullSpace;
        } else
            return null;
    }

    protected void swap(int i, int j) {
        BitSet tmp = reducedRows.get(i);
        reducedRows.set(i, reducedRows.get(j));
        reducedRows.set(j, tmp);
    }

    /*
    private void swap(int i, int j) {
        BitSet tmp = rows.get(i);
        rows.set(i, rows.get(j));
        rows.set(j, tmp);
    }
    */

    public SimpleBitMatrix getReduced() {
        if (reducedRows == null)
            gaussianElimination();
        return reducedRows;
    }

    private class AddRows {
        public int i, j;

        public AddRows(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }

    private class RowAdder implements Runnable {

        @Override
        public void run() {
            AddRows add;
            try {
                while (true) {
                    add = addQueue.take();
                    if (add.i == rows.size() && add.j == rows.size()) break;
                    reducedRows.get(add.j).xor(reducedRows.get(add.i));
                    //rows.get(add.j).xor(rows.get(add.i));
                    waitHere.release();
                }
            } catch (InterruptedException ignored) {
            }

        }
    }

}
