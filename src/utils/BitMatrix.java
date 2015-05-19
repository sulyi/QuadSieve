package utils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: arsene
 * Date: 2015.05.19.
 * Time: 10:56:13
 * To change this template use File | Settings | File Templates.
 */

public class BitMatrix implements Iterable<BitSet>{
    private Vector<BitSet> rows;

    public BitMatrix(){
        rows = new Vector<BitSet>();
    }

    public BitMatrix(BitSet[] rows){
        this.rows.addAll(Arrays.asList(rows));
    }

    public void add(BitSet r){
        rows.add(r);
    }

    public boolean contains(BitSet row){
        return rows.contains(row); 
    }

    public void transpose(){
        int x = rows.get(0).size();
        int y = rows.size();
        Vector<BitSet> transposed = new Vector<BitSet>(x);
        BitSet row;
        for(int i=0;i<x;i++){
            row = new BitSet(y);
            for(int j=0;j<y;j++){
                if (rows.get(j).get(i)) row.set(j);
            }
            transposed.add(row);
        }
        rows = transposed;
    }

    public BitMatrix gaussElimination(){
        
        return this;
    }

    @Override
    public Iterator<BitSet> iterator() {
        return this.rows.iterator();
    }
}
