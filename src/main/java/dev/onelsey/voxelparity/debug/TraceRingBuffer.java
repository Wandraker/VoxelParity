package dev.onelsey.voxelparity.debug;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TraceRingBuffer {
    private final ArrayDeque<TraceEntry> entries = new ArrayDeque<>();
    private int capacity;

    public TraceRingBuffer(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public synchronized void add(TraceEntry entry) {
        entries.addLast(Objects.requireNonNull(entry, "entry"));
        trim();
    }

    public synchronized List<TraceEntry> snapshot() {
        return List.copyOf(new ArrayList<>(entries));
    }

    public synchronized void resize(int newCapacity) {
        capacity = Math.max(1, newCapacity);
        trim();
    }

    public synchronized int size() {
        return entries.size();
    }

    private void trim() {
        while (entries.size() > capacity) {
            entries.removeFirst();
        }
    }

    public record TraceEntry(long epochMillis, String category, String message) {
        public TraceEntry {
            Objects.requireNonNull(category, "category");
            Objects.requireNonNull(message, "message");
        }
    }
}
