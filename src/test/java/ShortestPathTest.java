import graph.dagsp.DAGShortestPath;
import graph.topo.TopologicalSort;
import graph.model.Graph;
import metrics.SimpleMetrics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ShortestPathTest {

    @Test
    public void testShortestPathInDAG() {
        Graph graph = new Graph(6);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 3);
        graph.addEdge(1, 3, 6);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 4, 4);
        graph.addEdge(2, 5, 2);
        graph.addEdge(2, 3, 7);
        graph.addEdge(3, 4, -1);
        graph.addEdge(4, 5, -2);

        TopologicalSort topoSort = new TopologicalSort(new SimpleMetrics());
        List<Integer> topoOrder = topoSort.topologicalOrderKahn(graph);

        DAGShortestPath spFinder = new DAGShortestPath(new SimpleMetrics());
        int[] distances = spFinder.shortestPaths(graph, topoOrder, 0);

        assertEquals(0, distances[0]);
        assertEquals(5, distances[1]);
        assertEquals(3, distances[2]);
        assertEquals(10, distances[3]); // 0->2->3 = 3+7=10
        assertEquals(7, distances[4]);  // 0->2->3->4 = 3+7-1=9? Wait, let's check: 0->2->4 = 3+4=7
        assertEquals(5, distances[5]);  // 0->2->5 = 3+2=5
    }

    @Test
    public void testLongestPathInDAG() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 2);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 3);
        graph.addEdge(2, 3, 4);

        TopologicalSort topoSort = new TopologicalSort(new SimpleMetrics());
        List<Integer> topoOrder = topoSort.topologicalOrderKahn(graph);

        DAGShortestPath spFinder = new DAGShortestPath(new SimpleMetrics());
        int[] longestDist = spFinder.longestPaths(graph, topoOrder, 0);

        assertEquals(0, longestDist[0]);
        assertEquals(2, longestDist[1]);
        assertEquals(1, longestDist[2]);
        assertEquals(5, longestDist[3]); // 0->1->3 = 2+3=5 is longer than 0->2->3=1+4=5
    }

    @Test
    public void testCriticalPathFinding() {
        Graph graph = new Graph(5);
        graph.addEdge(0, 1, 3);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 3, 4);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 5);

        TopologicalSort topoSort = new TopologicalSort(new SimpleMetrics());
        List<Integer> topoOrder = topoSort.topologicalOrderKahn(graph);

        DAGShortestPath spFinder = new DAGShortestPath(new SimpleMetrics());
        DAGShortestPath.CriticalPathResult criticalPath = spFinder.findCriticalPath(graph, topoOrder, 0);

        assertEquals(12, criticalPath.getLength()); // 0->1->3->4 = 3+4+5=12
        assertTrue(criticalPath.getPath().contains(0));
        assertTrue(criticalPath.getPath().contains(1));
        assertTrue(criticalPath.getPath().contains(3));
        assertTrue(criticalPath.getPath().contains(4));
    }
}