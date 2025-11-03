package metrics;

import java.util.HashMap;
import java.util.Map;

public class SimpleMetrics implements Metrics {
    private Map<String, Long> operationCounts = new HashMap<>();
    private long startTime;
    private long endTime;

    @Override
    public void incrementOperation(String operation) {
        operationCounts.put(operation, operationCounts.getOrDefault(operation, 0L) + 1);
    }

    @Override
    public void startTimer() {
        startTime = System.nanoTime();
    }

    @Override
    public void stopTimer() {
        endTime = System.nanoTime();
    }

    @Override
    public long getOperationCount(String operation) {
        return operationCounts.getOrDefault(operation, 0L);
    }

    @Override
    public long getElapsedTime() {
        return endTime - startTime;
    }

    @Override
    public void reset() {
        operationCounts.clear();
        startTime = 0;
        endTime = 0;
    }

    public void printMetrics() {
        System.out.println("Time: " + getElapsedTime() + " ns");
        for (String op : operationCounts.keySet()) {
            System.out.println(op + ": " + getOperationCount(op));
        }
    }
}