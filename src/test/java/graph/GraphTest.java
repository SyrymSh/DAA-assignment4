package graph;
import graph.model.Graph;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GraphTest {

    @Test
    public void testGraphCreation() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 3);

        assertEquals(3, graph.getVertexCount());
        assertTrue(graph.getNeighbors(0).contains(1));
        assertTrue(graph.getNeighbors(1).contains(2));
        assertEquals(5, graph.getWeight(0, 1));
        assertEquals(3, graph.getWeight(1, 2));
    }

    @Test
    public void testReverseNeighbors() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        assertTrue(graph.getReverseNeighbors(1).contains(0));
        assertTrue(graph.getReverseNeighbors(2).contains(1));
    }
}