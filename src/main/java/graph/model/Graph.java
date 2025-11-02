package graph.model;

import java.util.*;

public class Graph {
    private int vertexCount;
    private List<List<Integer>> adjacencyList;
    private List<List<Integer>> reverseAdjacencyList;
    private Map<String, Integer> weights;

    public Graph(int vertexCount) {
        this.vertexCount = vertexCount;
        this.adjacencyList = new ArrayList<>();
        this.reverseAdjacencyList = new ArrayList<>();
        this.weights = new HashMap<>();

        for (int i = 0; i < vertexCount; i++) {
            adjacencyList.add(new ArrayList<>());
            reverseAdjacencyList.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int weight) {
        adjacencyList.get(from).add(to);
        reverseAdjacencyList.get(to).add(from); // Build reverse graph for Kosaraju
        weights.put(from + "-" + to, weight);
    }

    public List<Integer> getNeighbors(int vertex) {
        return adjacencyList.get(vertex);
    }

    public List<Integer> getReverseNeighbors(int vertex) {
        return reverseAdjacencyList.get(vertex);
    }

    public int getWeight(int from, int to) {
        return weights.getOrDefault(from + "-" + to, 1);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public boolean hasEdge(int from, int to) {
        return weights.containsKey(from + "-" + to);
    }

    // Helper method to get all edges for debugging
    public void printGraph() {
        System.out.println("Graph with " + vertexCount + " vertices:");
        for (int i = 0; i < vertexCount; i++) {
            System.out.print("Vertex " + i + " -> ");
            for (int neighbor : adjacencyList.get(i)) {
                System.out.print(neighbor + "(w:" + getWeight(i, neighbor) + ") ");
            }
            System.out.println();
        }
    }
}