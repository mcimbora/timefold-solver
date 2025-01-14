package ai.timefold.solver.core.impl.partitionedsearch.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ai.timefold.solver.core.impl.partitionedsearch.scope.PartitionChangeMove;
import ai.timefold.solver.core.impl.testdata.domain.TestdataSolution;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PartitionQueueTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartitionQueueTest.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdownNow();
        if (!executorService.awaitTermination(1, TimeUnit.MILLISECONDS)) {
            LOGGER.warn("Thread pool didn't terminate within the timeout.");
        }
    }

    @Test
    void addMove() throws ExecutionException, InterruptedException {
        PartitionQueue<TestdataSolution> partitionQueue = new PartitionQueue<>(3);
        Iterator<PartitionChangeMove<TestdataSolution>> it = partitionQueue.iterator();

        PartitionChangeMove<TestdataSolution> moveA1 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(0, moveA1)).get();
        assertThat(it.next()).isSameAs(moveA1);

        PartitionChangeMove<TestdataSolution> moveB1 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(1, moveB1)).get(); // Skipped
        PartitionChangeMove<TestdataSolution> moveB2 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(1, moveB2)).get(); // Skipped
        PartitionChangeMove<TestdataSolution> moveB3 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(1, moveB3)).get();
        assertThat(it.next()).isSameAs(moveB3);

        PartitionChangeMove<TestdataSolution> moveA2 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(0, moveA2)).get(); // Skipped
        PartitionChangeMove<TestdataSolution> moveA3 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(0, moveA3)).get();
        PartitionChangeMove<TestdataSolution> moveB4 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(1, moveB4)).get();
        assertThat(it.next()).isSameAs(moveA3);
        assertThat(it.next()).isSameAs(moveB4);

        PartitionChangeMove<TestdataSolution> moveB5 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(1, moveB5)).get(); // Skipped
        PartitionChangeMove<TestdataSolution> moveA4 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(0, moveA4)).get(); // Skipped
        PartitionChangeMove<TestdataSolution> moveA5 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(0, moveA5)).get(); // Skipped
        PartitionChangeMove<TestdataSolution> moveB6 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(1, moveB6)).get();
        PartitionChangeMove<TestdataSolution> moveC1 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(2, moveC1)).get();
        PartitionChangeMove<TestdataSolution> moveA6 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(0, moveA6)).get();
        assertThat(it.next()).isSameAs(moveB6);
        assertThat(it.next()).isSameAs(moveA6);
        assertThat(it.next()).isSameAs(moveC1);

        executorService.submit(() -> partitionQueue.addFinish(0, 123)).get();
        PartitionChangeMove<TestdataSolution> moveC2 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(2, moveC2)).get();
        executorService.submit(() -> partitionQueue.addFinish(1, 123)).get();
        assertThat(it.next()).isSameAs(moveC2);

        executorService.submit(() -> partitionQueue.addFinish(2, 123)).get();
        assertThat(it).isExhausted();
    }

    @Test
    void addFinishWithNonEmptyQueue() throws ExecutionException, InterruptedException {
        PartitionQueue<TestdataSolution> partitionQueue = new PartitionQueue<>(3);
        Iterator<PartitionChangeMove<TestdataSolution>> it = partitionQueue.iterator();

        PartitionChangeMove<TestdataSolution> moveA1 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(0, moveA1)).get();
        executorService.submit(() -> partitionQueue.addFinish(0, 123)).get();
        PartitionChangeMove<TestdataSolution> moveC1 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(2, moveC1)).get();
        PartitionChangeMove<TestdataSolution> moveC2 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(2, moveC2)).get();
        executorService.submit(() -> partitionQueue.addFinish(2, 123)).get();
        executorService.submit(() -> partitionQueue.addFinish(1, 123)).get();
        assertThat(it).hasNext();
        assertThat(it.next()).isSameAs(moveA1);
        assertThat(it).hasNext();
        assertThat(it.next()).isSameAs(moveC2);
        assertThat(it).isExhausted();
    }

    @Test
    void addExceptionWithNonEmptyQueue() throws ExecutionException, InterruptedException {
        PartitionQueue<TestdataSolution> partitionQueue = new PartitionQueue<>(3);
        Iterator<PartitionChangeMove<TestdataSolution>> it = partitionQueue.iterator();

        PartitionChangeMove<TestdataSolution> moveA1 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(0, moveA1)).get();
        executorService.submit(() -> partitionQueue.addFinish(0, 123)).get();
        PartitionChangeMove<TestdataSolution> moveC1 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(2, moveC1)).get();
        PartitionChangeMove<TestdataSolution> moveC2 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(2, moveC2)).get();
        IllegalArgumentException exception = new IllegalArgumentException();
        executorService.submit(() -> partitionQueue.addExceptionThrown(1, exception)).get();
        PartitionChangeMove<TestdataSolution> moveB1 = buildMove();
        executorService.submit(() -> partitionQueue.addMove(1, moveB1)).get();
        executorService.submit(() -> partitionQueue.addFinish(1, 123)).get();
        assertThat(it).hasNext();
        assertThat(it.next()).isSameAs(moveA1);
        assertThat(it).hasNext();
        assertThat(it.next()).isSameAs(moveC2);
        assertThatIllegalStateException().isThrownBy(it::hasNext).withCause(exception);
    }

    PartitionChangeMove<TestdataSolution> buildMove() {
        return new PartitionChangeMove<>(null, -1);
    }

}
