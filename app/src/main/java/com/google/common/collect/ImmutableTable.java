package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible
public abstract class ImmutableTable<R, C, V> extends AbstractTable<R, C, V> {
    private static final ImmutableTable<Object, Object, Object> EMPTY = new SparseImmutableTable(ImmutableList.of(), ImmutableSet.of(), ImmutableSet.of());

    public abstract ImmutableMap<C, Map<R, V>> columnMap();

    public /* bridge */ /* synthetic */ boolean containsColumn(@Nullable Object obj) {
        return super.containsColumn(obj);
    }

    public /* bridge */ /* synthetic */ boolean containsRow(@Nullable Object obj) {
        return super.containsRow(obj);
    }

    /* access modifiers changed from: package-private */
    public abstract ImmutableSet<Table.Cell<R, C, V>> createCellSet();

    /* access modifiers changed from: package-private */
    public abstract ImmutableCollection<V> createValues();

    public /* bridge */ /* synthetic */ boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ Object get(@Nullable Object obj, @Nullable Object obj2) {
        return super.get(obj, obj2);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }

    public /* bridge */ /* synthetic */ boolean isEmpty() {
        return super.isEmpty();
    }

    public abstract ImmutableMap<R, Map<C, V>> rowMap();

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public static <R, C, V> ImmutableTable<R, C, V> of() {
        return EMPTY;
    }

    public static <R, C, V> ImmutableTable<R, C, V> of(R rowKey, C columnKey, V value) {
        return new SingletonImmutableTable(rowKey, columnKey, value);
    }

    public static <R, C, V> ImmutableTable<R, C, V> copyOf(Table<? extends R, ? extends C, ? extends V> table) {
        if (table instanceof ImmutableTable) {
            return (ImmutableTable) table;
        }
        switch (table.size()) {
            case 0:
                return of();
            case 1:
                Table.Cell<? extends R, ? extends C, ? extends V> onlyCell = (Table.Cell) Iterables.getOnlyElement(table.cellSet());
                return of(onlyCell.getRowKey(), onlyCell.getColumnKey(), onlyCell.getValue());
            default:
                ImmutableSet.Builder<Table.Cell<R, C, V>> cellSetBuilder = ImmutableSet.builder();
                for (Table.Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
                    cellSetBuilder.add((Object) cellOf(cell.getRowKey(), cell.getColumnKey(), cell.getValue()));
                }
                return RegularImmutableTable.forCells(cellSetBuilder.build());
        }
    }

    public static <R, C, V> Builder<R, C, V> builder() {
        return new Builder<>();
    }

    static <R, C, V> Table.Cell<R, C, V> cellOf(R rowKey, C columnKey, V value) {
        return Tables.immutableCell(Preconditions.checkNotNull(rowKey), Preconditions.checkNotNull(columnKey), Preconditions.checkNotNull(value));
    }

    public static final class Builder<R, C, V> {
        private final List<Table.Cell<R, C, V>> cells = Lists.newArrayList();
        private Comparator<? super C> columnComparator;
        private Comparator<? super R> rowComparator;

        public Builder<R, C, V> orderRowsBy(Comparator<? super R> rowComparator2) {
            this.rowComparator = (Comparator) Preconditions.checkNotNull(rowComparator2);
            return this;
        }

        public Builder<R, C, V> orderColumnsBy(Comparator<? super C> columnComparator2) {
            this.columnComparator = (Comparator) Preconditions.checkNotNull(columnComparator2);
            return this;
        }

        public Builder<R, C, V> put(R rowKey, C columnKey, V value) {
            this.cells.add(ImmutableTable.cellOf(rowKey, columnKey, value));
            return this;
        }

        public Builder<R, C, V> put(Table.Cell<? extends R, ? extends C, ? extends V> cell) {
            if (cell instanceof Tables.ImmutableCell) {
                Preconditions.checkNotNull(cell.getRowKey());
                Preconditions.checkNotNull(cell.getColumnKey());
                Preconditions.checkNotNull(cell.getValue());
                Table.Cell<? extends R, ? extends C, ? extends V> cell2 = cell;
                this.cells.add(cell);
            } else {
                put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
            }
            return this;
        }

        public Builder<R, C, V> putAll(Table<? extends R, ? extends C, ? extends V> table) {
            for (Table.Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
                put(cell);
            }
            return this;
        }

        public ImmutableTable<R, C, V> build() {
            switch (this.cells.size()) {
                case 0:
                    return ImmutableTable.of();
                case 1:
                    return new SingletonImmutableTable((Table.Cell) Iterables.getOnlyElement(this.cells));
                default:
                    return RegularImmutableTable.forCells(this.cells, this.rowComparator, this.columnComparator);
            }
        }
    }

    ImmutableTable() {
    }

    public ImmutableSet<Table.Cell<R, C, V>> cellSet() {
        return (ImmutableSet) super.cellSet();
    }

    /* access modifiers changed from: package-private */
    public final UnmodifiableIterator<Table.Cell<R, C, V>> cellIterator() {
        throw new AssertionError("should never be called");
    }

    public ImmutableCollection<V> values() {
        return (ImmutableCollection) super.values();
    }

    /* access modifiers changed from: package-private */
    public final Iterator<V> valuesIterator() {
        throw new AssertionError("should never be called");
    }

    public ImmutableMap<R, V> column(C columnKey) {
        Preconditions.checkNotNull(columnKey);
        return (ImmutableMap) MoreObjects.firstNonNull((ImmutableMap) columnMap().get(columnKey), ImmutableMap.of());
    }

    public ImmutableSet<C> columnKeySet() {
        return columnMap().keySet();
    }

    public ImmutableMap<C, V> row(R rowKey) {
        Preconditions.checkNotNull(rowKey);
        return (ImmutableMap) MoreObjects.firstNonNull((ImmutableMap) rowMap().get(rowKey), ImmutableMap.of());
    }

    public ImmutableSet<R> rowKeySet() {
        return rowMap().keySet();
    }

    public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey) {
        return get(rowKey, columnKey) != null;
    }

    public boolean containsValue(@Nullable Object value) {
        return values().contains(value);
    }

    @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final V put(R r, C c, V v) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final void putAll(Table<? extends R, ? extends C, ? extends V> table) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final V remove(Object rowKey, Object columnKey) {
        throw new UnsupportedOperationException();
    }
}
