package graph;

import java.util.*;

public class Graph {
    private int vertexCount;
    private List<List<Integer>> adjacencyList;
    private Map<String, Integer> weights;

    public Graph(int vertexCount) {
        this.vertexCount = vertexCount;
        this.adjacencyList = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            adjacencyList.add(new ArrayList<>());
        }
        this.weights = new HashMap<>();
    }

    public void addEdge(int from, int to, int weight) {
        adjacencyList.get(from).add(to);
        weights.put(from + "-" + to, weight);
    }

    public List<Integer> getNeighbors(int vertex) {
        return adjacencyList.get(vertex);
    }

    public int getWeight(int from, int to) {
        return weights.getOrDefault(from + "-" + to, 1);
    }

    public int getVertexCount() {
        return vertexCount;
    }
}