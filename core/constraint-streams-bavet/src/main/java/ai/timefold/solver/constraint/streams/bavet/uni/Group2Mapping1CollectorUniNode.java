package ai.timefold.solver.constraint.streams.bavet.uni;

import static ai.timefold.solver.constraint.streams.bavet.uni.Group2Mapping0CollectorUniNode.createGroupKey;

import java.util.function.Function;

import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;
import ai.timefold.solver.constraint.streams.bavet.tri.TriTuple;
import ai.timefold.solver.constraint.streams.bavet.tri.TriTupleImpl;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintCollector;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.util.Pair;

final class Group2Mapping1CollectorUniNode<OldA, A, B, C, ResultContainer_>
        extends AbstractGroupUniNode<OldA, TriTuple<A, B, C>, TriTupleImpl<A, B, C>, Pair<A, B>, ResultContainer_, C> {

    private final int outputStoreSize;

    public Group2Mapping1CollectorUniNode(Function<OldA, A> groupKeyMappingA, Function<OldA, B> groupKeyMappingB,
            int groupStoreIndex, int undoStoreIndex, UniConstraintCollector<OldA, ResultContainer_, C> collector,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, tuple), collector,
                nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(Pair<A, B> groupKey) {
        return new TriTupleImpl<>(groupKey.getKey(), groupKey.getValue(), null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(TriTupleImpl<A, B, C> outTuple, C c) {
        outTuple.factC = c;
    }

}
