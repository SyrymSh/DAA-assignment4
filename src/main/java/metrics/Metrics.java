package metrics;

public interface Metrics {
    void incrementOperation(String operation);
    void startTimer();
    void stopTimer();
    long getOperationCount(String operation);
    long getElapsedTime();
    void reset();
}