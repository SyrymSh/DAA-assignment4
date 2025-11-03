package graph.dagsp;

import graph.model.Graph;
import metrics.Metrics;
import java.util.*;

public class DAGShortestPath {
    private Metrics metrics;

    public DAGShortestPath(Metrics metrics) {
        this.metrics = metrics;
    }

    public int[] shortestPaths(Graph graph, List<Integer> topoOrder, int source) {
        metrics.startTimer();
        int n = graph.getVertexCount();
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        for (int u : topoOrder) {
            metrics.incrementOperation("relaxations");
            if (dist[u] != Integer.MAX_VALUE) {
                for (int v : graph.getNeighbors(u)) {
                    int weight = graph.getWeight(u, v);
                    metrics.incrementOperation("edge_checks");
                    if (dist[u] + weight < dist[v]) {
                        dist[v] = dist[u] + weight;
                        metrics.incrementOperation("distance_updates");
                    }
                }
            }
        }
        metrics.stopTimer();
        return dist;
    }

    public List<Integer> reconstructPath(Graph graph, int[] dist, int source, int target) {
        if (dist[target] == Integer.MAX_VALUE) {
            return Collections.emptyList(); // No path exists
        }

        List<Integer> path = new ArrayList<>();
        path.add(target);
        int current = target;

        while (current != source) {
            boolean found = false;
            for (int i = 0; i < graph.getVertexCount(); i++) {
                if (graph.hasEdge(i, current)) {
                    int weight = graph.getWeight(i, current);
                    if (dist[i] != Integer.MAX_VALUE && dist[i] + weight == dist[current]) {
                        path.add(0, i);
                        current = i;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) break;
        }

        return path;
    }

    // Add these methods to the existing DAGShortestPath class

    public int[] longestPaths(Graph graph, List<Integer> topoOrder, int source) {
        metrics.startTimer();
        int n = graph.getVertexCount();
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MIN_VALUE);
        dist[source] = 0;

        for (int u : topoOrder) {
            metrics.incrementOperation("relaxations");
            if (dist[u] != Integer.MIN_VALUE) {
                for (int v : graph.getNeighbors(u)) {
                    int weight = graph.getWeight(u, v);
                    metrics.incrementOperation("edge_checks");
                    if (dist[u] + weight > dist[v]) {
                        dist[v] = dist[u] + weight;
                        metrics.incrementOperation("distance_updates");
                    }
                }
            }
        }
        metrics.stopTimer();
        return dist;
    }

    public CriticalPathResult findCriticalPath(Graph graph, List<Integer> topoOrder, int source) {
        int[] longestDist = longestPaths(graph, topoOrder, source);

        // Find the vertex with maximum distance
        int maxDist = Integer.MIN_VALUE;
        int endVertex = source;
        for (int i = 0; i < longestDist.length; i++) {
            if (longestDist[i] != Integer.MIN_VALUE && longestDist[i] > maxDist) {
                maxDist = longestDist[i];
                endVertex = i;
            }
        }

        // Reconstruct the critical path
        List<Integer> criticalPath = reconstructLongestPath(graph, longestDist, source, endVertex);

        return new CriticalPathResult(criticalPath, maxDist, endVertex);
    }

    private List<Integer> reconstructLongestPath(Graph graph, int[] dist, int source, int target) {
        if (dist[target] == Integer.MIN_VALUE) {
            return Collections.emptyList();
        }

        List<Integer> path = new ArrayList<>();
        path.add(target);
        int current = target;

        while (current != source) {
            boolean found = false;
            for (int i = 0; i < graph.getVertexCount(); i++) {
                if (graph.hasEdge(i, current)) {
                    int weight = graph.getWeight(i, current);
                    if (dist[i] != Integer.MIN_VALUE && dist[i] + weight == dist[current]) {
                        path.add(0, i);
                        current = i;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) break;
        }

        return path;
    }

    public static class CriticalPathResult {
        private final List<Integer> path;
        private final int length;
        private final int endVertex;

        public CriticalPathResult(List<Integer> path, int length, int endVertex) {
            this.path = path;
            this.length = length;
            this.endVertex = endVertex;
        }

        public List<Integer> getPath() { return path; }
        public int getLength() { return length; }
        public int getEndVertex() { return endVertex; }

        @Override
        public String toString() {
            return "CriticalPath{path=" + path + ", length=" + length + "}";
        }
    }
}