package android.support.design.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v4.util.SimpleArrayMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

final class DirectedAcyclicGraph<T> {
    private final SimpleArrayMap<T, ArrayList<T>> mGraph = new SimpleArrayMap<>();
    private final Pools.Pool<ArrayList<T>> mListPool = new Pools.SimplePool(10);
    private final ArrayList<T> mSortResult = new ArrayList<>();
    private final HashSet<T> mSortTmpMarked = new HashSet<>();

    DirectedAcyclicGraph() {
    }

    /* access modifiers changed from: package-private */
    public void addNode(@NonNull T node) {
        if (!this.mGraph.containsKey(node)) {
            this.mGraph.put(node, null);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean contains(@NonNull T node) {
        return this.mGraph.containsKey(node);
    }

    /* access modifiers changed from: package-private */
    public void addEdge(@NonNull T node, @NonNull T incomingEdge) {
        if (!this.mGraph.containsKey(node) || (!this.mGraph.containsKey(incomingEdge))) {
            throw new IllegalArgumentException("All nodes must be present in the graph before being added as an edge");
        }
        ArrayList<T> edges = this.mGraph.get(node);
        if (edges == null) {
            edges = getEmptyList();
            this.mGraph.put(node, edges);
        }
        edges.add(incomingEdge);
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public List getIncomingEdges(@NonNull T node) {
        return this.mGraph.get(node);
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public List<T> getOutgoingEdges(@NonNull T node) {
        ArrayList<T> result = null;
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList<T> edges = this.mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(this.mGraph.keyAt(i));
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOutgoingEdges(@NonNull T node) {
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList<T> edges = this.mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList<T> edges = this.mGraph.valueAt(i);
            if (edges != null) {
                poolList(edges);
            }
        }
        this.mGraph.clear();
    }

    /* access modifiers changed from: package-private */
    @NonNull
    public ArrayList<T> getSortedList() {
        this.mSortResult.clear();
        this.mSortTmpMarked.clear();
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            dfs(this.mGraph.keyAt(i), this.mSortResult, this.mSortTmpMarked);
        }
        return this.mSortResult;
    }

    private void dfs(T node, ArrayList<T> result, HashSet<T> tmpMarked) {
        if (!result.contains(node)) {
            if (tmpMarked.contains(node)) {
                throw new RuntimeException("This graph contains cyclic dependencies");
            }
            tmpMarked.add(node);
            ArrayList<T> edges = this.mGraph.get(node);
            if (edges != null) {
                int size = edges.size();
                for (int i = 0; i < size; i++) {
                    dfs(edges.get(i), result, tmpMarked);
                }
            }
            tmpMarked.remove(node);
            result.add(node);
        }
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return this.mGraph.size();
    }

    @NonNull
    private ArrayList<T> getEmptyList() {
        ArrayList<T> list = this.mListPool.acquire();
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private void poolList(@NonNull ArrayList<T> list) {
        list.clear();
        this.mListPool.release(list);
    }
}
