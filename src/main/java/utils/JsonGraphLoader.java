package utils;

import model.GraphData;
import graph.model.Graph;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JsonGraphLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static GraphData loadGraphData(String filePath) throws IOException {
        return mapper.readValue(new File(filePath), GraphData.class);
    }

    public static Graph convertToGraph(GraphData graphData) {
        Graph graph = new Graph(graphData.getN());

        for (GraphData.Edge edge : graphData.getEdges()) {
            graph.addEdge(edge.getU(), edge.getV(), edge.getW());
        }

        return graph;
    }
}