import graph.scc.SCCFinder;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPath;
import graph.model.Graph;
import model.GraphData;
import metrics.SimpleMetrics;
import utils.JsonGraphLoader;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== Smart City Scheduling Analysis ===\n");

            // Load graph from JSON
            GraphData graphData = JsonGraphLoader.loadGraphData("data/tasks.json");
            Graph graph = JsonGraphLoader.convertToGraph(graphData);

            System.out.println("Graph Summary:");
            System.out.println("- Vertices: " + graphData.getN());
            System.out.println("- Edges: " + graphData.getEdges().size());
            System.out.println("- Source: " + graphData.getSource());
            System.out.println("- Weight Model: " + graphData.getWeightModel());
            System.out.println();

            // 1. Find Strongly Connected Components
            System.out.println("1. STRONGLY CONNECTED COMPONENTS");
            SimpleMetrics sccMetrics = new SimpleMetrics();
            SCCFinder sccFinder = new SCCFinder(sccMetrics);

            List<List<Integer>> sccs = sccFinder.findSCCsTarjan(graph);
            System.out.println("Found " + sccs.size() + " SCCs:");
            for (int i = 0; i < sccs.size(); i++) {
                System.out.println("  SCC " + i + ": " + sccs.get(i) + " (size: " + sccs.get(i).size() + ")");
            }
            sccMetrics.printMetrics();
            System.out.println();

            // 2. Build condensation graph
            Graph condensation = sccFinder.buildCondensationGraph(graph, sccs);
            System.out.println("Condensation Graph: " + condensation.getVertexCount() + " components");
            System.out.println();

            // 3. Topological sort of condensation
            System.out.println("2. TOPOLOGICAL SORT");
            SimpleMetrics topoMetrics = new SimpleMetrics();
            TopologicalSort topoSort = new TopologicalSort(topoMetrics);

            List<Integer> topoOrder;
            try {
                topoOrder = topoSort.topologicalOrderKahn(condensation);
                System.out.println("Topological order: " + topoOrder);
            } catch (IllegalArgumentException e) {
                System.out.println("Cannot compute topological order: " + e.getMessage());
                topoOrder = topoSort.topologicalOrderDFS(condensation);
                System.out.println("DFS order (may not be valid): " + topoOrder);
            }
            topoMetrics.printMetrics();
            System.out.println();

            // 4. Shortest and longest paths
            System.out.println("3. SHORTEST AND LONGEST PATHS");
            SimpleMetrics spMetrics = new SimpleMetrics();
            DAGShortestPath shortestPath = new DAGShortestPath(spMetrics);

            // Find which component contains the source
            int[] vertexToSCC = mapVerticesToSCC(sccs, graph.getVertexCount());
            int sourceComponent = vertexToSCC[graphData.getSource()];
            System.out.println("Source vertex " + graphData.getSource() + " is in component " + sourceComponent);

            // Shortest paths
            int[] shortestDist = shortestPath.shortestPaths(condensation, topoOrder, sourceComponent);
            System.out.println("Shortest distances from component " + sourceComponent + ":");
            for (int i = 0; i < shortestDist.length; i++) {
                System.out.println("  To " + i + ": " +
                        (shortestDist[i] == Integer.MAX_VALUE ? "∞" : shortestDist[i]));
            }

            // Longest paths and critical path
            DAGShortestPath.CriticalPathResult criticalPath =
                    shortestPath.findCriticalPath(condensation, topoOrder, sourceComponent);
            System.out.println("Critical path: " + criticalPath.getPath());
            System.out.println("Critical path length: " + criticalPath.getLength());

            spMetrics.printMetrics();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int[] mapVerticesToSCC(List<List<Integer>> sccs, int vertexCount) {
        int[] vertexToSCC = new int[vertexCount];
        for (int i = 0; i < sccs.size(); i++) {
            for (int vertex : sccs.get(i)) {
                vertexToSCC[vertex] = i;
            }
        }
        return vertexToSCC;
    }
}