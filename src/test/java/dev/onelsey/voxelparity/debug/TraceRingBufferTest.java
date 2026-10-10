package dev.onelsey.voxelparity.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class TraceRingBufferTest {
    @Test
    void evictsOldestEntriesAtCapacity() {
        TraceRingBuffer buffer = new TraceRingBuffer(3);
        buffer.add(new TraceRingBuffer.TraceEntry(1L, "A", "one"));
        buffer.add(new TraceRingBuffer.TraceEntry(2L, "B", "two"));
        buffer.add(new TraceRingBuffer.TraceEntry(3L, "C", "three"));
        buffer.add(new TraceRingBuffer.TraceEntry(4L, "D", "four"));

        List<TraceRingBuffer.TraceEntry> snapshot = buffer.snapshot();
        assertEquals(3, snapshot.size());
        assertEquals("two", snapshot.get(0).message());
        assertEquals("four", snapshot.get(2).message());
    }

    @Test
    void resizeTrimsExistingData() {
        TraceRingBuffer buffer = new TraceRingBuffer(4);
        buffer.add(new TraceRingBuffer.TraceEntry(1L, "A", "one"));
        buffer.add(new TraceRingBuffer.TraceEntry(2L, "B", "two"));
        buffer.add(new TraceRingBuffer.TraceEntry(3L, "C", "three"));

        buffer.resize(2);

        assertEquals(
                List.of("two", "three"),
                buffer.snapshot().stream()
                        .map(TraceRingBuffer.TraceEntry::message)
                        .toList()
        );
    }
}
