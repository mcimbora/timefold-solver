package ai.timefold.solver.constraint.streams.bavet.quad;

import java.util.function.Function;

import ai.timefold.solver.constraint.streams.bavet.common.AbstractIndexedJoinNode;
import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;
import ai.timefold.solver.constraint.streams.bavet.common.index.IndexProperties;
import ai.timefold.solver.constraint.streams.bavet.common.index.Indexer;
import ai.timefold.solver.constraint.streams.bavet.tri.TriTuple;
import ai.timefold.solver.constraint.streams.bavet.uni.UniTuple;
import ai.timefold.solver.core.api.function.QuadPredicate;
import ai.timefold.solver.core.api.function.TriFunction;

final class IndexedJoinQuadNode<A, B, C, D>
        extends AbstractIndexedJoinNode<TriTuple<A, B, C>, D, QuadTuple<A, B, C, D>, QuadTupleImpl<A, B, C, D>> {

    private final TriFunction<A, B, C, IndexProperties> mappingABC;
    private final QuadPredicate<A, B, C, D> filtering;
    private final int outputStoreSize;

    public IndexedJoinQuadNode(TriFunction<A, B, C, IndexProperties> mappingABC, Function<D, IndexProperties> mappingD,
            int inputStoreIndexABC, int inputStoreIndexEntryABC, int inputStoreIndexOutTupleListABC,
            int inputStoreIndexD, int inputStoreIndexEntryD, int inputStoreIndexOutTupleListD,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle, QuadPredicate<A, B, C, D> filtering,
            int outputStoreSize,
            int outputStoreIndexOutEntryABC, int outputStoreIndexOutEntryD,
            Indexer<TriTuple<A, B, C>> indexerABC,
            Indexer<UniTuple<D>> indexerD) {
        super(mappingD,
                inputStoreIndexABC, inputStoreIndexEntryABC, inputStoreIndexOutTupleListABC,
                inputStoreIndexD, inputStoreIndexEntryD, inputStoreIndexOutTupleListD,
                nextNodesTupleLifecycle, filtering != null,
                outputStoreIndexOutEntryABC, outputStoreIndexOutEntryD,
                indexerABC, indexerD);
        this.mappingABC = mappingABC;
        this.filtering = filtering;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected IndexProperties createIndexPropertiesLeft(TriTuple<A, B, C> leftTuple) {
        return mappingABC.apply(leftTuple.getFactA(), leftTuple.getFactB(), leftTuple.getFactC());
    }

    @Override
    protected QuadTupleImpl<A, B, C, D> createOutTuple(TriTuple<A, B, C> leftTuple, UniTuple<D> rightTuple) {
        return new QuadTupleImpl<>(leftTuple.getFactA(), leftTuple.getFactB(), leftTuple.getFactC(), rightTuple.getFactA(),
                outputStoreSize);
    }

    @Override
    protected void setOutTupleLeftFacts(QuadTupleImpl<A, B, C, D> outTuple, TriTuple<A, B, C> leftTuple) {
        outTuple.factA = leftTuple.getFactA();
        outTuple.factB = leftTuple.getFactB();
        outTuple.factC = leftTuple.getFactC();
    }

    @Override
    protected void setOutTupleRightFact(QuadTupleImpl<A, B, C, D> outTuple, UniTuple<D> rightTuple) {
        outTuple.factD = rightTuple.getFactA();
    }

    @Override
    protected boolean testFiltering(TriTuple<A, B, C> leftTuple, UniTuple<D> rightTuple) {
        return filtering.test(leftTuple.getFactA(), leftTuple.getFactB(), leftTuple.getFactC(), rightTuple.getFactA());
    }

}
