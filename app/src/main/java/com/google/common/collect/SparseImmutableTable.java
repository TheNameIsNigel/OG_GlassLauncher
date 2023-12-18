package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@GwtCompatible
@Immutable
final class SparseImmutableTable<R, C, V> extends RegularImmutableTable<R, C, V> {
    private final ImmutableMap<C, Map<R, V>> columnMap;
    private final int[] iterationOrderColumn;
    private final int[] iterationOrderRow;
    private final ImmutableMap<R, Map<C, V>> rowMap;

    SparseImmutableTable(ImmutableList<Table.Cell<R, C, V>> cellList, ImmutableSet<R> rowSpace, ImmutableSet<C> columnSpace) {
        HashMap newHashMap = Maps.newHashMap();
        LinkedHashMap newLinkedHashMap = Maps.newLinkedHashMap();
        for (R row : rowSpace) {
            newHashMap.put(row, Integer.valueOf(newLinkedHashMap.size()));
            newLinkedHashMap.put(row, new LinkedHashMap());
        }
        Map<C, Map<R, V>> columns = Maps.newLinkedHashMap();
        for (C col : columnSpace) {
            columns.put(col, new LinkedHashMap());
        }
        int[] iterationOrderRow2 = new int[cellList.size()];
        int[] iterationOrderColumn2 = new int[cellList.size()];
        for (int i = 0; i < cellList.size(); i++) {
            Table.Cell<R, C, V> cell = (Table.Cell) cellList.get(i);
            R rowKey = cell.getRowKey();
            C columnKey = cell.getColumnKey();
            V value = cell.getValue();
            iterationOrderRow2[i] = ((Integer) newHashMap.get(rowKey)).intValue();
            Map<C, V> thisRow = (Map) newLinkedHashMap.get(rowKey);
            iterationOrderColumn2[i] = thisRow.size();
            V oldValue = thisRow.put(columnKey, value);
            if (oldValue != null) {
                throw new IllegalArgumentException("Duplicate value for row=" + rowKey + ", column=" + columnKey + ": " + value + ", " + oldValue);
            }
            columns.get(columnKey).put(rowKey, value);
        }
        this.iterationOrderRow = iterationOrderRow2;
        this.iterationOrderColumn = iterationOrderColumn2;
        ImmutableMap.Builder<R, Map<C, V>> rowBuilder = ImmutableMap.builder();
        for (Map.Entry<R, Map<C, V>> row2 : newLinkedHashMap.entrySet()) {
            rowBuilder.put(row2.getKey(), ImmutableMap.copyOf(row2.getValue()));
        }
        this.rowMap = rowBuilder.build();
        ImmutableMap.Builder<C, Map<R, V>> columnBuilder = ImmutableMap.builder();
        for (Map.Entry<C, Map<R, V>> col2 : columns.entrySet()) {
            columnBuilder.put(col2.getKey(), ImmutableMap.copyOf(col2.getValue()));
        }
        this.columnMap = columnBuilder.build();
    }

    public ImmutableMap<C, Map<R, V>> columnMap() {
        return this.columnMap;
    }

    public ImmutableMap<R, Map<C, V>> rowMap() {
        return this.rowMap;
    }

    public int size() {
        return this.iterationOrderRow.length;
    }

    /* access modifiers changed from: package-private */
    public Table.Cell<R, C, V> getCell(int index) {
        Map.Entry<R, Map<C, V>> rowEntry = (Map.Entry) this.rowMap.entrySet().asList().get(this.iterationOrderRow[index]);
        Map.Entry<C, V> colEntry = (Map.Entry) ((ImmutableMap) rowEntry.getValue()).entrySet().asList().get(this.iterationOrderColumn[index]);
        return cellOf(rowEntry.getKey(), colEntry.getKey(), colEntry.getValue());
    }

    /* access modifiers changed from: package-private */
    public V getValue(int index) {
        int rowIndex = this.iterationOrderRow[index];
        return ((ImmutableMap) this.rowMap.values().asList().get(rowIndex)).values().asList().get(this.iterationOrderColumn[index]);
    }
}
