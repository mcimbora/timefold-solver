package ai.timefold.solver.constraint.streams.bavet.quad;

import java.util.function.Function;

import ai.timefold.solver.constraint.streams.bavet.common.AbstractFlattenLastNode;
import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;

final class FlattenLastQuadNode<A, B, C, D, NewD>
        extends AbstractFlattenLastNode<QuadTuple<A, B, C, D>, QuadTuple<A, B, C, NewD>, D, NewD> {

    private final int outputStoreSize;

    FlattenLastQuadNode(int flattenLastStoreIndex, Function<D, Iterable<NewD>> mappingFunction,
            TupleLifecycle<QuadTuple<A, B, C, NewD>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(flattenLastStoreIndex, mappingFunction, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected QuadTuple<A, B, C, NewD> createTuple(QuadTuple<A, B, C, D> originalTuple, NewD newD) {
        return new QuadTupleImpl<>(originalTuple.getFactA(), originalTuple.getFactB(), originalTuple.getFactC(), newD,
                outputStoreSize);
    }

    @Override
    protected D getEffectiveFactIn(QuadTuple<A, B, C, D> tuple) {
        return tuple.getFactD();
    }

    @Override
    protected NewD getEffectiveFactOut(QuadTuple<A, B, C, NewD> outTuple) {
        return outTuple.getFactD();
    }
}
