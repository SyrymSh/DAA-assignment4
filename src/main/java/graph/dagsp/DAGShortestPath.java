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
}