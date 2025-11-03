package utils;

import graph.model.Graph;
import model.GraphData;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class GraphGenerator {
    private Random random = new Random(42); // Fixed seed for reproducibility

    public GraphData generateSmallDAG(String name) {
        GraphData data = new GraphData();
        data.setDirected(true);
        data.setN(8);
        data.setSource(0);
        data.setWeightModel("edge");

        List<GraphData.Edge> edges = new ArrayList<>();
        // Create a simple DAG structure
        edges.add(createEdge(0, 1, 3));
        edges.add(createEdge(0, 2, 2));
        edges.add(createEdge(1, 3, 4));
        edges.add(createEdge(2, 3, 1));
        edges.add(createEdge(3, 4, 5));
        edges.add(createEdge(4, 5, 2));
        edges.add(createEdge(4, 6, 3));
        edges.add(createEdge(5, 7, 1));
        edges.add(createEdge(6, 7, 2));

        data.setEdges(edges);
        return data;
    }

    public GraphData generateGraphWithCycle(String name) {
        GraphData data = new GraphData();
        data.setDirected(true);
        data.setN(8);
        data.setSource(0);
        data.setWeightModel("edge");

        List<GraphData.Edge> edges = new ArrayList<>();
        // Create a graph with a cycle 1->2->3->1
        edges.add(createEdge(0, 1, 2));
        edges.add(createEdge(1, 2, 3));
        edges.add(createEdge(2, 3, 1));
        edges.add(createEdge(3, 1, 2)); // Cycle
        edges.add(createEdge(1, 4, 4));
        edges.add(createEdge(4, 5, 2));
        edges.add(createEdge(5, 6, 3));
        edges.add(createEdge(6, 7, 1));

        data.setEdges(edges);
        return data;
    }

    public GraphData generateMultiSCCGraph(String name) {
        GraphData data = new GraphData();
        data.setDirected(true);
        data.setN(12);
        data.setSource(0);
        data.setWeightModel("edge");

        List<GraphData.Edge> edges = new ArrayList<>();
        // First SCC: 0-1-2 cycle
        edges.add(createEdge(0, 1, 1));
        edges.add(createEdge(1, 2, 2));
        edges.add(createEdge(2, 0, 3));

        // Second SCC: 3-4-5 cycle
        edges.add(createEdge(3, 4, 2));
        edges.add(createEdge(4, 5, 1));
        edges.add(createEdge(5, 3, 2));

        // Third SCC: 6-7 cycle
        edges.add(createEdge(6, 7, 1));
        edges.add(createEdge(7, 6, 1));

        // Connect SCCs
        edges.add(createEdge(0, 3, 2));
        edges.add(createEdge(3, 6, 3));
        edges.add(createEdge(5, 8, 1));
        edges.add(createEdge(8, 9, 2));
        edges.add(createEdge(9, 10, 1));
        edges.add(createEdge(10, 11, 3));

        data.setEdges(edges);
        return data;
    }

    public GraphData generateRandomGraph(String name, int vertices, double density, boolean allowCycles) {
        GraphData data = new GraphData();
        data.setDirected(true);
        data.setN(vertices);
        data.setSource(0);
        data.setWeightModel("edge");

        List<GraphData.Edge> edges = new ArrayList<>();
        int maxEdges = (int) (vertices * (vertices - 1) * density);

        for (int i = 0; i < maxEdges; i++) {
            int u = random.nextInt(vertices);
            int v = random.nextInt(vertices);

            // Avoid self-loops and ensure u != v
            if (u == v) continue;

            // If not allowing cycles, ensure u < v for DAG
            if (!allowCycles && u >= v) continue;

            int weight = random.nextInt(10) + 1; // Weight between 1-10
            edges.add(createEdge(u, v, weight));
        }

        data.setEdges(edges);
        return data;
    }

    private GraphData.Edge createEdge(int u, int v, int w) {
        GraphData.Edge edge = new GraphData.Edge();
        edge.setU(u);
        edge.setV(v);
        edge.setW(w);
        return edge;
    }

    public void generateAllDatasets() {
        // Small datasets (6-10 nodes)
        saveGraph(generateSmallDAG("small_dag_1"), "data/small/dag_1.json");
        saveGraph(generateGraphWithCycle("small_cyclic_1"), "data/small/cyclic_1.json");
        saveGraph(generateRandomGraph("small_mixed_1", 8, 0.3, true), "data/small/mixed_1.json");

        // Medium datasets (10-20 nodes)
        saveGraph(generateMultiSCCGraph("medium_multi_scc_1"), "data/medium/multi_scc_1.json");
        saveGraph(generateRandomGraph("medium_dense_1", 15, 0.4, false), "data/medium/dense_1.json");
        saveGraph(generateRandomGraph("medium_sparse_1", 15, 0.2, true), "data/medium/sparse_1.json");

        // Large datasets (20-50 nodes)
        saveGraph(generateRandomGraph("large_performance_1", 30, 0.3, false), "data/large/performance_1.json");
        saveGraph(generateRandomGraph("large_complex_1", 40, 0.25, true), "data/large/complex_1.json");
        saveGraph(generateRandomGraph("large_random_1", 35, 0.35, true), "data/large/random_1.json");

        System.out.println("Generated 9 datasets in data/ directory");
    }

    private void saveGraph(GraphData graphData, String filename) {
        try {
            JsonGraphLoader.mapper.writeValue(new java.io.File(filename), graphData);
        } catch (Exception e) {
            System.err.println("Error saving graph to " + filename + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        GraphGenerator generator = new GraphGenerator();
        generator.generateAllDatasets();
    }
}