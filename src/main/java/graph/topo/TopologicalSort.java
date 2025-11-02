package graph.topo;

import graph.model.Graph;
import metrics.Metrics;
import java.util.*;

public class TopologicalSort {
    private Metrics metrics;

    public TopologicalSort(Metrics metrics) {
        this.metrics = metrics;
    }

    public List<Integer> topologicalOrderKahn(Graph graph) {
        metrics.startTimer();
        int n = graph.getVertexCount();
        int[] inDegree = new int[n];
        List<Integer> order = new ArrayList<>();

        // Calculate in-degrees
        for (int v = 0; v < n; v++) {
            for (int neighbor : graph.getNeighbors(v)) {
                inDegree[neighbor]++;
                metrics.incrementOperation("inDegree_calc");
            }
        }

        // Initialize queue with vertices having 0 in-degree
        Queue<Integer> queue = new LinkedList<>();
        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                queue.offer(v);
                metrics.incrementOperation("queue_pushes");
            }
        }

        // Process vertices
        while (!queue.isEmpty()) {
            int v = queue.poll();
            metrics.incrementOperation("queue_pops");
            order.add(v);

            for (int neighbor : graph.getNeighbors(v)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    queue.offer(neighbor);
                    metrics.incrementOperation("queue_pushes");
                }
            }
        }

        // Check for cycles
        if (order.size() != n) {
            throw new IllegalArgumentException("Graph has cycles - no topological order exists");
        }

        metrics.stopTimer();
        return order;
    }


    public List<Integer> topologicalOrderDFS(Graph graph) {
        metrics.startTimer();
        int n = graph.getVertexCount();
        boolean[] visited = new boolean[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfsTopo(graph, i, visited, stack);
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!stack.isEmpty()) {
            order.add(stack.pop());
        }

        metrics.stopTimer();
        return order;
    }

    private void dfsTopo(Graph graph, int v, boolean[] visited, Stack<Integer> stack) {
        metrics.incrementOperation("DFS_visits");
        visited[v] = true;

        for (int neighbor : graph.getNeighbors(v)) {
            metrics.incrementOperation("DFS_edges");
            if (!visited[neighbor]) {
                dfsTopo(graph, neighbor, visited, stack);
            }
        }

        stack.push(v);
        metrics.incrementOperation("stack_pushes");
    }
}