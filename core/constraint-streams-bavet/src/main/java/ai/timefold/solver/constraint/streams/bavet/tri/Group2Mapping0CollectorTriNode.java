package ai.timefold.solver.constraint.streams.bavet.tri;

import ai.timefold.solver.constraint.streams.bavet.bi.BiTuple;
import ai.timefold.solver.constraint.streams.bavet.bi.BiTupleImpl;
import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;
import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.util.Pair;

final class Group2Mapping0CollectorTriNode<OldA, OldB, OldC, A, B>
        extends AbstractGroupTriNode<OldA, OldB, OldC, BiTuple<A, B>, BiTupleImpl<A, B>, Pair<A, B>, Void, Void> {

    private final int outputStoreSize;

    public Group2Mapping0CollectorTriNode(TriFunction<OldA, OldB, OldC, A> groupKeyMappingA,
            TriFunction<OldA, OldB, OldC, B> groupKeyMappingB, int groupStoreIndex,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, tuple), nextNodesTupleLifecycle,
                environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <A, B, OldA, OldB, OldC> Pair<A, B> createGroupKey(TriFunction<OldA, OldB, OldC, A> groupKeyMappingA,
            TriFunction<OldA, OldB, OldC, B> groupKeyMappingB, TriTuple<OldA, OldB, OldC> tuple) {
        OldA oldA = tuple.getFactA();
        OldB oldB = tuple.getFactB();
        OldC oldC = tuple.getFactC();
        A a = groupKeyMappingA.apply(oldA, oldB, oldC);
        B b = groupKeyMappingB.apply(oldA, oldB, oldC);
        return Pair.of(a, b);
    }

    @Override
    protected BiTupleImpl<A, B> createOutTuple(Pair<A, B> groupKey) {
        return new BiTupleImpl<>(groupKey.getKey(), groupKey.getValue(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(BiTupleImpl<A, B> outTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

}
