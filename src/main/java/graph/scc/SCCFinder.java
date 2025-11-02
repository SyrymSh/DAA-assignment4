package graph.scc;

import graph.model.Graph;
import metrics.Metrics;
import java.util.*;

public class SCCFinder {
    private Metrics metrics;

    public SCCFinder(Metrics metrics) {
        this.metrics = metrics;
    }

    public List<List<Integer>> findSCCsTarjan(Graph graph) {
        metrics.startTimer();
        int n = graph.getVertexCount();
        int[] indices = new int[n];
        int[] lowlinks = new int[n];
        boolean[] onStack = new boolean[n];
        Stack<Integer> stack = new Stack<>();
        List<List<Integer>> sccs = new ArrayList<>();
        int[] index = {0};

        Arrays.fill(indices, -1);

        for (int v = 0; v < n; v++) {
            if (indices[v] == -1) {
                strongConnect(graph, v, indices, lowlinks, onStack, stack, sccs, index);
            }
        }

        metrics.stopTimer();
        return sccs;
    }

    private void strongConnect(Graph graph, int v, int[] indices, int[] lowlinks,
                               boolean[] onStack, Stack<Integer> stack,
                               List<List<Integer>> sccs, int[] index) {
        metrics.incrementOperation("DFS_visits");
        indices[v] = index[0];
        lowlinks[v] = index[0];
        index[0]++;
        stack.push(v);
        onStack[v] = true;

        for (int w : graph.getNeighbors(v)) {
            metrics.incrementOperation("DFS_edges");
            if (indices[w] == -1) {
                strongConnect(graph, w, indices, lowlinks, onStack, stack, sccs, index);
                lowlinks[v] = Math.min(lowlinks[v], lowlinks[w]);
            } else if (onStack[w]) {
                lowlinks[v] = Math.min(lowlinks[v], indices[w]);
            }
        }

        if (lowlinks[v] == indices[v]) {
            List<Integer> scc = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                scc.add(w);
            } while (w != v);
            sccs.add(scc);
        }
    }
}