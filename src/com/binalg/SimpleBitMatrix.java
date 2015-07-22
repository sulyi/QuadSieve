package com.binalg;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.07.09.
 * Time: 13:59:25
 * To change this template use File | Settings | File Templates.
 */

public class SimpleBitMatrix implements Iterable<BitSet> {
    protected Vector<BitSet> rows;
    protected int numCols;

    public SimpleBitMatrix(int numCols) {
        this.numCols = numCols;
        rows = new Vector<>();
    }

    public SimpleBitMatrix(BitSet[] rows, int numCols) {
        this.numCols = numCols;
        this.rows = new Vector<>(rows.length);
        this.rows.addAll(Arrays.asList(rows));
    }

    public void transpose() {
        int y = rows.size();
        Vector<BitSet> transposed = new Vector<>(numCols);
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

    public BitSet lDot(BitSet vec) {
        int y = rows.size();
        BitSet ret = new BitSet(y);
        for (int i = 0; i < y; i++) {
            if (vec.get(i))
                ret.xor(rows.get(i));
        }
        return ret;
    }

    public SimpleBitMatrix createCopy() {
        SimpleBitMatrix ret = new SimpleBitMatrix(numCols);
        for (BitSet row : this)
            ret.add((BitSet) row.clone());
        return ret;
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
        return rows.set(i, to);
    }

    public BitSet get(int i) {
        return rows.get(i);
    }

    @Override
    public Iterator<BitSet> iterator() {
        return rows.iterator();
    }

}
