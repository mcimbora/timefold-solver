package ai.timefold.solver.constraint.streams.bavet.tri;

import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;
import ai.timefold.solver.constraint.streams.bavet.uni.UniTuple;
import ai.timefold.solver.constraint.streams.bavet.uni.UniTupleImpl;
import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.config.solver.EnvironmentMode;

final class Group1Mapping0CollectorTriNode<OldA, OldB, OldC, A>
        extends AbstractGroupTriNode<OldA, OldB, OldC, UniTuple<A>, UniTupleImpl<A>, A, Void, Void> {

    private final int outputStoreSize;

    public Group1Mapping0CollectorTriNode(TriFunction<OldA, OldB, OldC, A> groupKeyMapping, int groupStoreIndex,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMapping, tuple), nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <A, OldA, OldB, OldC> A createGroupKey(TriFunction<OldA, OldB, OldC, A> groupKeyMapping,
            TriTuple<OldA, OldB, OldC> tuple) {
        return groupKeyMapping.apply(tuple.getFactA(), tuple.getFactB(), tuple.getFactC());
    }

    @Override
    protected UniTupleImpl<A> createOutTuple(A a) {
        return new UniTupleImpl<>(a, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(UniTupleImpl<A> aUniTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

}
