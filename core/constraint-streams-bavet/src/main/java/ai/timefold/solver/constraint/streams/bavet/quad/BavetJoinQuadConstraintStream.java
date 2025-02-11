package ai.timefold.solver.constraint.streams.bavet.quad;

import java.util.Objects;
import java.util.Set;

import ai.timefold.solver.constraint.streams.bavet.BavetConstraintFactory;
import ai.timefold.solver.constraint.streams.bavet.common.AbstractJoinNode;
import ai.timefold.solver.constraint.streams.bavet.common.BavetAbstractConstraintStream;
import ai.timefold.solver.constraint.streams.bavet.common.BavetJoinConstraintStream;
import ai.timefold.solver.constraint.streams.bavet.common.NodeBuildHelper;
import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;
import ai.timefold.solver.constraint.streams.bavet.common.index.IndexerFactory;
import ai.timefold.solver.constraint.streams.bavet.common.index.JoinerUtils;
import ai.timefold.solver.constraint.streams.bavet.tri.BavetJoinBridgeTriConstraintStream;
import ai.timefold.solver.constraint.streams.bavet.tri.TriTuple;
import ai.timefold.solver.constraint.streams.bavet.uni.BavetJoinBridgeUniConstraintStream;
import ai.timefold.solver.constraint.streams.common.quad.DefaultQuadJoiner;
import ai.timefold.solver.core.api.function.QuadPredicate;
import ai.timefold.solver.core.api.score.Score;

public final class BavetJoinQuadConstraintStream<Solution_, A, B, C, D>
        extends BavetAbstractQuadConstraintStream<Solution_, A, B, C, D>
        implements BavetJoinConstraintStream<Solution_> {

    private final BavetJoinBridgeTriConstraintStream<Solution_, A, B, C> leftParent;
    private final BavetJoinBridgeUniConstraintStream<Solution_, D> rightParent;

    private final DefaultQuadJoiner<A, B, C, D> joiner;
    private final QuadPredicate<A, B, C, D> filtering;

    public BavetJoinQuadConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetJoinBridgeTriConstraintStream<Solution_, A, B, C> leftParent,
            BavetJoinBridgeUniConstraintStream<Solution_, D> rightParent,
            DefaultQuadJoiner<A, B, C, D> joiner, QuadPredicate<A, B, C, D> filtering) {
        super(constraintFactory, leftParent.getRetrievalSemantics());
        this.leftParent = leftParent;
        this.rightParent = rightParent;
        this.joiner = joiner;
        this.filtering = filtering;
    }

    @Override
    public boolean guaranteesDistinct() {
        return leftParent.guaranteesDistinct() && rightParent.guaranteesDistinct();
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    public void collectActiveConstraintStreams(Set<BavetAbstractConstraintStream<Solution_>> constraintStreamSet) {
        leftParent.collectActiveConstraintStreams(constraintStreamSet);
        rightParent.collectActiveConstraintStreams(constraintStreamSet);
        constraintStreamSet.add(this);
    }

    @Override
    public <Score_ extends Score<Score_>> void buildNode(NodeBuildHelper<Score_> buildHelper) {
        int outputStoreSize = buildHelper.extractTupleStoreSize(this);
        TupleLifecycle<QuadTuple<A, B, C, D>> downstream = buildHelper.getAggregatedTupleLifecycle(childStreamList);
        IndexerFactory indexerFactory = new IndexerFactory(joiner);
        AbstractJoinNode<TriTuple<A, B, C>, D, QuadTuple<A, B, C, D>, QuadTupleImpl<A, B, C, D>> node =
                indexerFactory.hasJoiners()
                        ? new IndexedJoinQuadNode<>(
                                JoinerUtils.combineLeftMappings(joiner), JoinerUtils.combineRightMappings(joiner),
                                buildHelper.reserveTupleStoreIndex(leftParent.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(leftParent.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(leftParent.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(rightParent.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(rightParent.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(rightParent.getTupleSource()),
                                downstream, filtering, outputStoreSize + 2,
                                outputStoreSize, outputStoreSize + 1,
                                indexerFactory.buildIndexer(true), indexerFactory.buildIndexer(false))
                        : new UnindexedJoinQuadNode<>(
                                buildHelper.reserveTupleStoreIndex(leftParent.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(leftParent.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(rightParent.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(rightParent.getTupleSource()),
                                downstream, filtering, outputStoreSize + 2,
                                outputStoreSize, outputStoreSize + 1);
        buildHelper.addNode(node, leftParent, rightParent);
    }

    // ************************************************************************
    // Equality for node sharing
    // ************************************************************************

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BavetJoinQuadConstraintStream<?, ?, ?, ?, ?> other = (BavetJoinQuadConstraintStream<?, ?, ?, ?, ?>) o;
        /*
         * Bridge streams do not implement equality because their equals() would have to point back to this stream,
         * resulting in StackOverflowError.
         * Therefore we need to check bridge parents to see where this join node comes from.
         */
        return Objects.equals(leftParent.getParent(), other.leftParent.getParent())
                && Objects.equals(rightParent.getParent(), other.rightParent.getParent())
                && Objects.equals(joiner, other.joiner)
                && Objects.equals(filtering, other.filtering);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftParent.getParent(), rightParent.getParent(), joiner, filtering);
    }

    @Override
    public String toString() {
        return "QuadJoin() with " + childStreamList.size() + " children";
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

}
