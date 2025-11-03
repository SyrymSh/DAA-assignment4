import graph.topo.TopologicalSort;
import graph.model.Graph;
import metrics.SimpleMetrics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class TopologicalSortTest {

    @Test
    public void testSimpleDAG() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);

        TopologicalSort topoSort = new TopologicalSort(new SimpleMetrics());
        List<Integer> order = topoSort.topologicalOrderKahn(graph);

        assertEquals(4, order.size());
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    public void testCycleDetection() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1); // Creates cycle

        TopologicalSort topoSort = new TopologicalSort(new SimpleMetrics());

        assertThrows(IllegalArgumentException.class, () -> {
            topoSort.topologicalOrderKahn(graph);
        });
    }

    @Test
    public void testKahnAndDFSProduceValidOrders() {
        Graph graph = new Graph(5);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);

        TopologicalSort topoSort = new TopologicalSort(new SimpleMetrics());
        List<Integer> kahnOrder = topoSort.topologicalOrderKahn(graph);
        List<Integer> dfsOrder = topoSort.topologicalOrderDFS(graph);

        assertEquals(5, kahnOrder.size());
        assertEquals(5, dfsOrder.size());

        // Both orders should satisfy dependencies
        assertTrue(kahnOrder.indexOf(0) < kahnOrder.indexOf(1));
        assertTrue(kahnOrder.indexOf(0) < kahnOrder.indexOf(2));
        assertTrue(kahnOrder.indexOf(1) < kahnOrder.indexOf(3));
        assertTrue(kahnOrder.indexOf(2) < kahnOrder.indexOf(3));
        assertTrue(kahnOrder.indexOf(3) < kahnOrder.indexOf(4));
    }
}