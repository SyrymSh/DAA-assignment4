import graph.scc.SCCFinder;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPath;
import graph.model.Graph;
import model.GraphData;
import metrics.SimpleMetrics;
import utils.JsonGraphLoader;
import utils.GraphGenerator;

import java.io.File;
import java.util.*;

public class Main {
    private static final Map<String, AnalysisResult> results = new LinkedHashMap<>();

    public static void main(String[] args) {
        System.out.println("Starting Smart City Scheduling Analysis...");

        if (args.length > 0) {
            if (args[0].equals("--generate")) {
                generateDatasets();
                return;
            } else if (args[0].equals("--full-analysis")) {
                runComprehensiveAnalysis();
                return;
            } else if (args[0].endsWith(".json")) {
                runSingleAnalysis(args[0]);
                return;
            }
        }

        // Default: run analysis on all datasets and generate comparison
        runAllDatasetsAndCompare();
    }

    public static void runAllDatasetsAndCompare() {
        System.out.println("=== ANALYZING ALL DATASETS AND GENERATING COMPARISON ===\n");

        // Ensure datasets exist
        generateDatasetsIfNeeded();

        String[] datasets = {
                "data/small/cyclic_1.json",
                "data/small/dag_1.json",
                "data/small/mixed_1.json",
                "data/medium/dense_1.json",
                "data/medium/multi_scc_1.json",
                "data/medium/sparse_1.json",  // Fixed typo: was sparse-1.json
                "data/large/complex_1.json",
                "data/large/performance_1.json",
                "data/large/random_1.json"
        };

        for (String dataset : datasets) {
            File file = new File(dataset);
            if (file.exists()) {
                try {
                    System.out.println("\n" + "=".repeat(70));
                    System.out.println("ANALYZING: " + dataset);
                    System.out.println("=".repeat(70));

                    GraphData graphData = JsonGraphLoader.loadGraphData(dataset);
                    Graph graph = JsonGraphLoader.convertToGraph(graphData);

                    AnalysisResult result = runAlgorithmPipeline(graphData, graph, dataset);
                    results.put(dataset, result);

                } catch (Exception e) {
                    System.err.println("Failed to analyze " + dataset + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Dataset not found: " + dataset);
            }
        }

        // Generate comparative report after analyzing all datasets
        generateComparativeReport();
    }

    public static void runSingleAnalysis(String filePath) {
        try {
            System.out.println("=== Smart City Scheduling Analysis ===\n");

            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File not found: " + filePath);
                return;
            }

            GraphData graphData = JsonGraphLoader.loadGraphData(filePath);
            Graph graph = JsonGraphLoader.convertToGraph(graphData);

            printGraphSummary(graphData, filePath);
            AnalysisResult result = runAlgorithmPipeline(graphData, graph, filePath);
            results.put(filePath, result);

        } catch (Exception e) {
            System.err.println("Error analyzing " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void runComprehensiveAnalysis() {
        System.out.println("=== COMPREHENSIVE DATASET ANALYSIS ===\n");

        generateDatasetsIfNeeded();

        String[] datasets = {
                "data/tasks.json",
                "data/small/dag_1.json",
                "data/small/cyclic_1.json",
                "data/small/mixed_1.json",
                "data/medium/multi_scc_1.json",
                "data/medium/dense_1.json",
                "data/medium/sparse_1.json",
                "data/large/performance_1.json",
                "data/large/complex_1.json",
                "data/large/random_1.json"
        };

        for (String dataset : datasets) {
            File file = new File(dataset);
            if (file.exists()) {
                try {
                    System.out.println("\n" + "=".repeat(70));
                    System.out.println("ANALYZING: " + dataset);
                    System.out.println("=".repeat(70));

                    GraphData graphData = JsonGraphLoader.loadGraphData(dataset);
                    Graph graph = JsonGraphLoader.convertToGraph(graphData);

                    AnalysisResult result = runAlgorithmPipeline(graphData, graph, dataset);
                    results.put(dataset, result);

                } catch (Exception e) {
                    System.err.println("Failed to analyze " + dataset + ": " + e.getMessage());
                }
            } else {
                System.out.println("Dataset not found: " + dataset);
            }
        }

        generateComparativeReport();
    }

    private static AnalysisResult runAlgorithmPipeline(GraphData graphData, Graph graph, String datasetName) {
        AnalysisResult result = new AnalysisResult();
        result.datasetName = datasetName;
        result.vertexCount = graphData.getN();
        result.edgeCount = graphData.getEdges().size();

        // 1. Strongly Connected Components
        System.out.println("1. STRONGLY CONNECTED COMPONENTS");
        SimpleMetrics sccMetrics = new SimpleMetrics();
        SCCFinder sccFinder = new SCCFinder(sccMetrics);

        List<List<Integer>> sccs = sccFinder.findSCCsTarjan(graph);
        result.sccCount = sccs.size();
        result.sccTime = sccMetrics.getElapsedTime();
        result.sccMetrics = sccMetrics;

        System.out.println("Found " + sccs.size() + " SCCs");
        printSCCDistribution(sccs);
        sccMetrics.printMetrics();

        // 2. Condensation Graph
        Graph condensation = sccFinder.buildCondensationGraph(graph, sccs);
        result.condensationSize = condensation.getVertexCount();
        System.out.println("Condensed to " + condensation.getVertexCount() + " components\n");

        // 3. Topological Sort
        System.out.println("2. TOPOLOGICAL SORT");
        SimpleMetrics topoMetrics = new SimpleMetrics();
        TopologicalSort topoSort = new TopologicalSort(topoMetrics);

        List<Integer> topoOrder;
        try {
            topoOrder = topoSort.topologicalOrderKahn(condensation);
            System.out.println("Topological order: " + topoOrder);
            result.topoValid = true;
        } catch (IllegalArgumentException e) {
            System.out.println("Cycle detected: " + e.getMessage());
            topoOrder = topoSort.topologicalOrderDFS(condensation);
            System.out.println("DFS order (may contain cycles): " + topoOrder);
            result.topoValid = false;
        }
        result.topoTime = topoMetrics.getElapsedTime();
        result.topoMetrics = topoMetrics;
        topoMetrics.printMetrics();

        // 4. Shortest and Longest Paths
        if (result.topoValid && topoOrder.size() == condensation.getVertexCount()) {
            System.out.println("3. SHORTEST AND LONGEST PATHS");
            SimpleMetrics spMetrics = new SimpleMetrics();
            DAGShortestPath shortestPath = new DAGShortestPath(spMetrics);

            int[] vertexToSCC = mapVerticesToSCC(sccs, graph.getVertexCount());
            int sourceComponent = vertexToSCC[graphData.getSource()];
            result.sourceComponent = sourceComponent;

            System.out.println("Source vertex " + graphData.getSource() + " is in component " + sourceComponent);

            // Shortest paths
            int[] shortestDist = shortestPath.shortestPaths(condensation, topoOrder, sourceComponent);
            result.reachableComponents = countReachable(shortestDist);

            // Longest paths and critical path
            DAGShortestPath.CriticalPathResult criticalPath =
                    shortestPath.findCriticalPath(condensation, topoOrder, sourceComponent);
            result.criticalPathLength = criticalPath.getLength();
            result.spTime = spMetrics.getElapsedTime();
            result.spMetrics = spMetrics;

            System.out.println("Critical path length: " + criticalPath.getLength());
            System.out.println("Reachable components: " + result.reachableComponents + "/" + condensation.getVertexCount());
            spMetrics.printMetrics();

        } else {
            System.out.println("3. PATH FINDING - Skipped due to invalid topological order");
            result.spTime = 0;
            result.reachableComponents = 0;
            result.criticalPathLength = 0;
        }

        System.out.println("\n" + "=".repeat(70));
        return result;
    }

    private static void printGraphSummary(GraphData graphData, String filePath) {
        System.out.println("Dataset: " + filePath);
        System.out.println("Graph Summary:");
        System.out.println("- Vertices: " + graphData.getN());
        System.out.println("- Edges: " + graphData.getEdges().size());
        System.out.println("- Source: " + graphData.getSource());
        System.out.println("- Weight Model: " + graphData.getWeightModel());

        double density = (double) graphData.getEdges().size() / (graphData.getN() * (graphData.getN() - 1));
        System.out.printf("- Density: %.3f (%s)\n", density, getDensityType(density));

        if (graphData.getN() <= 15) {
            System.out.println("\nGraph Structure:");
            for (GraphData.Edge edge : graphData.getEdges()) {
                System.out.println("  " + edge.getU() + " → " + edge.getV() + " (w=" + edge.getW() + ")");
            }
        }
        System.out.println();
    }

    private static void printSCCDistribution(List<List<Integer>> sccs) {
        Map<Integer, Integer> sizeDistribution = new HashMap<>();
        for (List<Integer> scc : sccs) {
            int size = scc.size();
            sizeDistribution.put(size, sizeDistribution.getOrDefault(size, 0) + 1);
        }

        System.out.print("SCC Size Distribution: ");
        List<Integer> sizes = new ArrayList<>(sizeDistribution.keySet());
        Collections.sort(sizes);
        for (int size : sizes) {
            System.out.print(size + "(" + sizeDistribution.get(size) + ") ");
        }
        System.out.println();
    }

    private static void generateComparativeReport() {
        System.out.println("\n\n" + "=".repeat(100));
        System.out.println("COMPARATIVE ANALYSIS REPORT");
        System.out.println("=".repeat(100));

        if (results.isEmpty()) {
            System.out.println("No results to compare. Run analysis first.");
            return;
        }

        // Performance Summary Table
        System.out.println("\nPERFORMANCE SUMMARY (Time in nanoseconds)");
        System.out.println("+---------------------------------+----------+----------+----------+----------+------------+");
        System.out.println("| Dataset                         | Vertices | Edges    | SCC Time | Topo Time| SP Time    |");
        System.out.println("+---------------------------------+----------+----------+----------+----------+------------+");

        for (Map.Entry<String, AnalysisResult> entry : results.entrySet()) {
            String dataset = getShortName(entry.getKey());
            AnalysisResult result = entry.getValue();

            System.out.printf("| %-31s | %8d | %8d | %8d | %8d | %10d |\n",
                    dataset, result.vertexCount, result.edgeCount,
                    result.sccTime, result.topoTime, result.spTime);
        }
        System.out.println("+---------------------------------+----------+----------+----------+----------+------------+");

        // SCC Analysis Table
        System.out.println("\nSCC ANALYSIS");
        System.out.println("+---------------------------------+----------+----------+------------+----------------+");
        System.out.println("| Dataset                         | Vertices | SCC Count| Compression| DFS Visits     |");
        System.out.println("+---------------------------------+----------+----------+------------+----------------+");

        for (Map.Entry<String, AnalysisResult> entry : results.entrySet()) {
            String dataset = getShortName(entry.getKey());
            AnalysisResult result = entry.getValue();
            double compression = (double) result.condensationSize / result.vertexCount;
            long dfsVisits = result.sccMetrics != null ? result.sccMetrics.getOperationCount("DFS_visits") : 0;

            System.out.printf("| %-31s | %8d | %8d | %10.2f | %14d |\n",
                    dataset, result.vertexCount, result.sccCount,
                    compression, dfsVisits);
        }
        System.out.println("+---------------------------------+----------+----------+------------+----------------+");

        // Path Finding Analysis
        System.out.println("\nPATH FINDING ANALYSIS");
        System.out.println("+---------------------------------+----------+------------+------------+----------------+");
        System.out.println("| Dataset                         | Reachable| Crit. Path | Topo Valid | Edge Checks    |");
        System.out.println("|                                 | Comps    | Length     |            |                |");
        System.out.println("+---------------------------------+----------+------------+------------+----------------+");

        for (Map.Entry<String, AnalysisResult> entry : results.entrySet()) {
            String dataset = getShortName(entry.getKey());
            AnalysisResult result = entry.getValue();
            String topoValid = result.topoValid ? "Yes" : "No";
            long edgeChecks = result.spMetrics != null ?
                    result.spMetrics.getOperationCount("edge_checks") : 0;

            System.out.printf("| %-31s | %8d | %10d | %10s | %14d |\n",
                    dataset, result.reachableComponents, result.criticalPathLength,
                    topoValid, edgeChecks);
        }
        System.out.println("+---------------------------------+----------+------------+------------+----------------+");

        // Generate insights
        generateInsights();
    }

    private static void generateInsights() {
        System.out.println("\nKEY INSIGHTS");
        System.out.println("=".repeat(50));

        if (results.isEmpty()) {
            System.out.println("No results to analyze.");
            return;
        }

        // Find extremes
        AnalysisResult fastestSCC = null, slowestSCC = null;
        AnalysisResult bestCompression = null, worstCompression = null;

        for (AnalysisResult result : results.values()) {
            if (fastestSCC == null || result.sccTime < fastestSCC.sccTime) fastestSCC = result;
            if (slowestSCC == null || result.sccTime > slowestSCC.sccTime) slowestSCC = result;

            double compression = (double) result.condensationSize / result.vertexCount;
            double bestComp = bestCompression != null ?
                    (double) bestCompression.condensationSize / bestCompression.vertexCount : 1.0;

            if (bestCompression == null || compression < bestComp) bestCompression = result;
            if (worstCompression == null || compression > bestComp) worstCompression = result;
        }

        System.out.println("Performance Analysis:");
        System.out.println("• Fastest SCC: " + getShortName(fastestSCC.datasetName) +
                " (" + fastestSCC.sccTime + " ns)");
        System.out.println("• Slowest SCC: " + getShortName(slowestSCC.datasetName) +
                " (" + slowestSCC.sccTime + " ns)");
        System.out.println("• Best Compression: " + getShortName(bestCompression.datasetName) +
                " (" + String.format("%.2f", (double)bestCompression.condensationSize/bestCompression.vertexCount) + ")");
        System.out.println("• Worst Compression: " + getShortName(worstCompression.datasetName) +
                " (" + String.format("%.2f", (double)worstCompression.condensationSize/worstCompression.vertexCount) + ")");

        System.out.println("\nAlgorithm Efficiency:");
        long totalSCCtime = results.values().stream().mapToLong(r -> r.sccTime).sum();
        long totalTopotime = results.values().stream().mapToLong(r -> r.topoTime).sum();
        long totalSPtime = results.values().stream().mapToLong(r -> r.spTime).sum();
        long totalTime = totalSCCtime + totalTopotime + totalSPtime;

        if (totalTime > 0) {
            System.out.printf("• Total SCC Time: %,d ns (%.1f%%)\n", totalSCCtime, (double)totalSCCtime/totalTime*100);
            System.out.printf("• Total Topo Time: %,d ns (%.1f%%)\n", totalTopotime, (double)totalTopotime/totalTime*100);
            System.out.printf("• Total SP Time: %,d ns (%.1f%%)\n", totalSPtime, (double)totalSPtime/totalTime*100);
        }
    }

    private static void generateDatasetsIfNeeded() {
        File dataDir = new File("data");
        if (!dataDir.exists() || Objects.requireNonNull(dataDir.list()).length == 0) {
            System.out.println("Generating datasets...");
            generateDatasets();
        }
    }

    private static void generateDatasets() {
        try {
            GraphGenerator generator = new GraphGenerator();
            generator.generateAllDatasets();
            System.out.println("Datasets generated successfully in data/ directory");
        } catch (Exception e) {
            System.err.println("Error generating datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getShortName(String fullPath) {
        return fullPath.substring(fullPath.lastIndexOf('/') + 1);
    }

    private static String getDensityType(double density) {
        if (density < 0.1) return "Very Sparse";
        if (density < 0.3) return "Sparse";
        if (density < 0.7) return "Medium";
        return "Dense";
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

    private static int countReachable(int[] distances) {
        int count = 0;
        for (int dist : distances) {
            if (dist != Integer.MAX_VALUE && dist != Integer.MIN_VALUE) {
                count++;
            }
        }
        return count;
    }

    static class AnalysisResult {
        String datasetName;
        int vertexCount;
        int edgeCount;
        int sccCount;
        int condensationSize;
        boolean topoValid;
        int sourceComponent;
        int reachableComponents;
        int criticalPathLength;
        long sccTime;
        long topoTime;
        long spTime;
        SimpleMetrics sccMetrics;
        SimpleMetrics topoMetrics;
        SimpleMetrics spMetrics;
        int[] shortestDistances;
        List<Integer> criticalPath;
    }
}