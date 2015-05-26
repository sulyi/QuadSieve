package com.utils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.19.
 * Time: 10:56:13
 * To change this template use File | Settings | File Templates.
 */

//TODO: cleanup out commented lines and todo's

public class BitMatrix implements Iterable<BitSet> {
    private Vector<BitSet> rows;
    private int numCols;

    private BlockingQueue<Add> addQueue;
    private Semaphore waitHere;

    //private Vector<Integer> swaps;
    //TODO: decide if gaussianElimination should return a new matrix or be a void
    private BitMatrix reducedRows;

    public BitMatrix(int numCols) {
        this.numCols = numCols;
        rows = new Vector<BitSet>();
    }

    public BitMatrix(BitSet[] rows, int numCols) {
        this.numCols = numCols;
        this.rows = new Vector<BitSet>();
        this.rows.addAll(Arrays.asList(rows));
    }

    public void transpose() {
        int y = rows.size();
        Vector<BitSet> transposed = new Vector<BitSet>(numCols);
        BitSet row;
        for (int i = 0; i < numCols; i++) {
            row = new BitSet(y);
            for (int j = 0; j < y; j++) {
                if (rows.get(j).get(i)) row.set(j);
            }
            transposed.add(row);
        }
        numCols = y;
        rows = transposed;
    }

    public BitSet gaussianElimination() throws InterruptedException {
        return gaussianElimination(Runtime.getRuntime().availableProcessors());
    }

    public BitSet gaussianElimination(int threads) throws InterruptedException {

        int y = rows.size();
        int i, j, ia;
        BitSet pivots = new BitSet(numCols);

        //swaps = new Vector<Integer>();

        reducedRows = createCopy();

        waitHere = new Semaphore(0);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        addQueue = new ArrayBlockingQueue<Add>(threads);

        for (i = 0; i < threads; i++)
            pool.submit(new RowAdder());

            int diff = 0;
            for (i = 0; i < numCols; i++) {
                j = ia = i - diff;
                while (j < y && !reducedRows.get(j).get(i))
                    j++;
                if (j == y) {
                    diff++;
                    continue;
                }

                if (j > ia) {
                    swap(ia, j);
                    //swaps.add(ia);
                    //swaps.add(j);
                }

                pivots.set(i);
                for (j = 0; j < y; j++) {
                    if (reducedRows.get(j).get(i) && j != ia)
                    //if (rows.get(j).get(i) && j != ia)
                        addQueue.put(new Add(ia, j));
                    else
                        waitHere.release();
                }
                waitHere.acquire(y);

            }

            for (i = 0; i < threads; i++)
                addQueue.put(new Add(y, y));

        pool.shutdown();

       return pivots;
        //return reducedRows;
    }

    public BitMatrix nullSpace() throws InterruptedException {
        int i, j, k, l;
        BitSet row;
        BitSet pivots = gaussianElimination();
        int y = numCols-pivots.cardinality();

        BitMatrix nullSpace = new BitMatrix(y);

        for(j=0,i=0;i<numCols;i++){
            row = new BitSet(y);
            if (pivots.get(i)){
                for(l=pivots.nextClearBit(0),k=0;k<y;k++,l=pivots.nextClearBit(l+1))
                    row.set(k,reducedRows.get(j).get(l));
                j++;
            } else {
                row.set(i-j);
            }
            nullSpace.add(row);
        }
        //TODO: redo above code in order to avoid need of transposing
        nullSpace.transpose();
        return nullSpace;
    }


    private void swap(int i, int j){
        BitSet tmp = reducedRows.get(i);
        reducedRows.set(i,reducedRows.get(j));
        reducedRows.set(j,tmp);
    }

    /*
    private void swap(int i, int j) {
        BitSet tmp = rows.get(i);
        rows.set(i, rows.get(j));
        rows.set(j, tmp);
    }
    */

    public BitSet rDot(BitSet vec){
        int y = rows.size();
        BitSet ret = new BitSet(y);
        for (int i = 0; i < y; i++) {
            if (vec.get(i))
                ret.xor(rows.get(i));
        }
        return ret;
    }

    public BitMatrix createCopy(){
        BitMatrix ret = new BitMatrix(numCols);
        for(BitSet row : this )
            ret.add((BitSet) row.clone());
        return ret;
    }

    public BitMatrix reduced(){
        //TODO: gaussianEliminate if null
        return reducedRows;
    }

    public int getNumCols() {
        return numCols;
    }

    // proxies //

    public int getNumRows() {
        return rows.size();
    }

    public boolean contains(BitSet row) {
        return rows.contains(row);
    }

    public void add(BitSet r) {
        rows.add(r);
    }

    public BitSet set(int i, BitSet to) {
        return rows.set(i,to);
    }

    public BitSet get(int i) {
        return rows.get(i);
    }

    @Override
    public Iterator<BitSet> iterator() {
        return rows.iterator();
    }

    private class Add {
        public int i, j;

        public Add(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }

    private class RowAdder implements Runnable {

        @Override
        public void run() {
            Add add;
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
