package ai.timefold.solver.constraint.streams.bavet.common;

import java.util.List;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStream;
import ai.timefold.solver.core.config.solver.EnvironmentMode;

final class GroupNodeConstructorWithoutAccumulate<Tuple_ extends Tuple> implements GroupNodeConstructor<Tuple_> {

    private final NodeConstructorWithoutAccumulate<Tuple_> nodeConstructorFunction;

    public GroupNodeConstructorWithoutAccumulate(NodeConstructorWithoutAccumulate<Tuple_> nodeConstructorFunction) {
        this.nodeConstructorFunction = nodeConstructorFunction;
    }

    @Override
    public <Solution_, Score_ extends Score<Score_>> void build(NodeBuildHelper<Score_> buildHelper,
            BavetAbstractConstraintStream<Solution_> parentTupleSource,
            BavetAbstractConstraintStream<Solution_> groupStream, List<? extends ConstraintStream> groupStreamChildList,
            BavetAbstractConstraintStream<Solution_> bridgeStream, List<? extends ConstraintStream> bridgeStreamChildList,
            EnvironmentMode environmentMode) {
        if (!bridgeStreamChildList.isEmpty()) {
            throw new IllegalStateException("Impossible state: the stream (" + bridgeStream
                    + ") has an non-empty childStreamList (" + bridgeStreamChildList + ") but it's a groupBy bridge.");
        }
        int groupStoreIndex = buildHelper.reserveTupleStoreIndex(parentTupleSource);
        TupleLifecycle<Tuple_> tupleLifecycle = buildHelper.getAggregatedTupleLifecycle(groupStreamChildList);
        int outputStoreSize = buildHelper.extractTupleStoreSize(groupStream);
        var node = nodeConstructorFunction.apply(groupStoreIndex, tupleLifecycle, outputStoreSize, environmentMode);
        buildHelper.addNode(node, bridgeStream);
    }
}
