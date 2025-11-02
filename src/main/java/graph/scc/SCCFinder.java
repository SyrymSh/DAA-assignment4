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

    public List<List<Integer>> findSCCsKosaraju(Graph graph) {
        metrics.startTimer();

        // Step 1: First DFS for finishing times
        Stack<Integer> stack = new Stack<>();
        boolean[] visited = new boolean[graph.getVertexCount()];

        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (!visited[i]) {
                dfsFirstPass(graph, i, visited, stack);
            }
        }

        // Step 2: Second DFS on transpose graph
        Arrays.fill(visited, false);
        List<List<Integer>> sccs = new ArrayList<>();

        while (!stack.isEmpty()) {
            int v = stack.pop();
            if (!visited[v]) {
                List<Integer> scc = new ArrayList<>();
                dfsSecondPass(graph, v, visited, scc);
                sccs.add(scc);
            }
        }

        metrics.stopTimer();
        return sccs;
    }

    private void dfsFirstPass(Graph graph, int v, boolean[] visited, Stack<Integer> stack) {
        metrics.incrementOperation("DFS_visits");
        visited[v] = true;

        for (int neighbor : graph.getNeighbors(v)) {
            metrics.incrementOperation("DFS_edges");
            if (!visited[neighbor]) {
                dfsFirstPass(graph, neighbor, visited, stack);
            }
        }

        stack.push(v);
    }

    private void dfsSecondPass(Graph graph, int v, boolean[] visited, List<Integer> scc) {
        metrics.incrementOperation("DFS_visits");
        visited[v] = true;
        scc.add(v);

        for (int neighbor : graph.getReverseNeighbors(v)) {
            metrics.incrementOperation("DFS_edges");
            if (!visited[neighbor]) {
                dfsSecondPass(graph, neighbor, visited, scc);
            }
        }
    }

    public Graph buildCondensationGraph(Graph originalGraph, List<List<Integer>> sccs) {
        int n = sccs.size();
        Graph condensation = new Graph(n);

        // Map each original vertex to its SCC index
        int[] vertexToSCC = new int[originalGraph.getVertexCount()];
        for (int i = 0; i < sccs.size(); i++) {
            for (int vertex : sccs.get(i)) {
                vertexToSCC[vertex] = i;
            }
        }

        // Add edges between different SCCs
        Set<String> addedEdges = new HashSet<>();
        for (int u = 0; u < originalGraph.getVertexCount(); u++) {
            for (int v : originalGraph.getNeighbors(u)) {
                int sccU = vertexToSCC[u];
                int sccV = vertexToSCC[v];
                if (sccU != sccV && !addedEdges.contains(sccU + "-" + sccV)) {
                    condensation.addEdge(sccU, sccV, 1);
                    addedEdges.add(sccU + "-" + sccV);
                }
            }
        }

        return condensation;
    }
}