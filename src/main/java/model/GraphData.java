package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GraphData {
    private boolean directed;
    private int n;
    private List<Edge> edges;
    private int source;

    @JsonProperty("weight_model")  // Map JSON field to Java field
    private String weightModel;

    // Getters and setters
    public boolean isDirected() { return directed; }
    public void setDirected(boolean directed) { this.directed = directed; }

    public int getN() { return n; }
    public void setN(int n) { this.n = n; }

    public List<Edge> getEdges() { return edges; }
    public void setEdges(List<Edge> edges) { this.edges = edges; }

    public int getSource() { return source; }
    public void setSource(int source) { this.source = source; }

    public String getWeightModel() { return weightModel; }
    public void setWeightModel(String weightModel) { this.weightModel = weightModel; }

    public static class Edge {
        private int u;
        private int v;
        private int w;

        // Getters and setters
        public int getU() { return u; }
        public void setU(int u) { this.u = u; }

        public int getV() { return v; }
        public void setV(int v) { this.v = v; }

        public int getW() { return w; }
        public void setW(int w) { this.w = w; }

        @Override
        public String toString() {
            return "Edge{u=" + u + ", v=" + v + ", w=" + w + "}";
        }
    }

    @Override
    public String toString() {
        return "GraphData{n=" + n + ", edges=" + edges + ", source=" + source + ", weightModel=" + weightModel + "}";
    }
}