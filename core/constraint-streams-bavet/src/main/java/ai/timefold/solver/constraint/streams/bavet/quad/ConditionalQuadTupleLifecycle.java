package ai.timefold.solver.constraint.streams.bavet.quad;

import ai.timefold.solver.constraint.streams.bavet.common.AbstractConditionalTupleLifecycle;
import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;
import ai.timefold.solver.core.api.function.QuadPredicate;

final class ConditionalQuadTupleLifecycle<A, B, C, D> extends AbstractConditionalTupleLifecycle<QuadTuple<A, B, C, D>> {
    private final QuadPredicate<A, B, C, D> predicate;

    public ConditionalQuadTupleLifecycle(QuadPredicate<A, B, C, D> predicate,
            TupleLifecycle<QuadTuple<A, B, C, D>> tupleLifecycle) {
        super(tupleLifecycle);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(QuadTuple<A, B, C, D> tuple) {
        return predicate.test(tuple.getFactA(), tuple.getFactB(), tuple.getFactC(), tuple.getFactD());
    }
}
