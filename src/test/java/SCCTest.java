import graph.scc.SCCFinder;
import graph.model.Graph;
import metrics.SimpleMetrics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class SCCTest {

    @Test
    public void testSimpleCycle() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 0, 1);

        SCCFinder finder = new SCCFinder(new SimpleMetrics());
        List<List<Integer>> sccs = finder.findSCCsTarjan(graph);

        assertEquals(1, sccs.size());
        assertEquals(4, sccs.get(0).size());
        assertTrue(sccs.get(0).contains(0));
        assertTrue(sccs.get(0).contains(1));
        assertTrue(sccs.get(0).contains(2));
        assertTrue(sccs.get(0).contains(3));
    }

    @Test
    public void testDisconnectedComponents() {
        Graph graph = new Graph(6);
        // First component: 0-1-2 cycle
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        // Second component: 3-4-5 cycle
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 5, 1);
        graph.addEdge(5, 3, 1);

        SCCFinder finder = new SCCFinder(new SimpleMetrics());
        List<List<Integer>> sccs = finder.findSCCsTarjan(graph);

        assertEquals(2, sccs.size());

        // Check that each cycle is in its own SCC
        boolean foundFirstCycle = false;
        boolean foundSecondCycle = false;

        for (List<Integer> scc : sccs) {
            if (scc.contains(0) && scc.contains(1) && scc.contains(2)) {
                assertEquals(3, scc.size());
                foundFirstCycle = true;
            }
            if (scc.contains(3) && scc.contains(4) && scc.contains(5)) {
                assertEquals(3, scc.size());
                foundSecondCycle = true;
            }
        }

        assertTrue(foundFirstCycle);
        assertTrue(foundSecondCycle);
    }

    @Test
    public void testKosarajuAndTarjanProduceSameResults() {
        Graph graph = new Graph(5);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(3, 4, 1);

        SCCFinder finder = new SCCFinder(new SimpleMetrics());
        List<List<Integer>> tarjanSCCs = finder.findSCCsTarjan(graph);
        List<List<Integer>> kosarajuSCCs = finder.findSCCsKosaraju(graph);

        assertEquals(tarjanSCCs.size(), kosarajuSCCs.size());

        // Both should find the cycle [0,1,2] and singles [3], [4]
        assertEquals(3, tarjanSCCs.size());
        assertEquals(3, kosarajuSCCs.size());
    }
}