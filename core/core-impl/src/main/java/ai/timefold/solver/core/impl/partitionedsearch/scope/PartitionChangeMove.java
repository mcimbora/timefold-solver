package ai.timefold.solver.core.impl.partitionedsearch.scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.AbstractMove;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.util.Pair;

/**
 * Applies a new best solution from a partition child solver into the global working solution of the parent solver.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public final class PartitionChangeMove<Solution_> extends AbstractMove<Solution_> {

    public static <Solution_> PartitionChangeMove<Solution_> createMove(InnerScoreDirector<Solution_, ?> scoreDirector,
            int partIndex) {
        SolutionDescriptor<Solution_> solutionDescriptor = scoreDirector.getSolutionDescriptor();
        Solution_ workingSolution = scoreDirector.getWorkingSolution();

        int entityCount = solutionDescriptor.getEntityCount(workingSolution);
        Map<GenuineVariableDescriptor<Solution_>, List<Pair<Object, Object>>> changeMap = new LinkedHashMap<>(
                solutionDescriptor.getEntityDescriptors().size() * 3);
        for (EntityDescriptor<Solution_> entityDescriptor : solutionDescriptor.getEntityDescriptors()) {
            for (GenuineVariableDescriptor<Solution_> variableDescriptor : entityDescriptor
                    .getDeclaredGenuineVariableDescriptors()) {
                changeMap.put(variableDescriptor, new ArrayList<>(entityCount));
            }
        }
        solutionDescriptor.visitAllEntities(workingSolution, entity -> {
            EntityDescriptor<Solution_> entityDescriptor = solutionDescriptor.findEntityDescriptorOrFail(
                    entity.getClass());
            if (entityDescriptor.isMovable(scoreDirector, entity)) {
                for (GenuineVariableDescriptor<Solution_> variableDescriptor : entityDescriptor
                        .getGenuineVariableDescriptorList()) {
                    Object value = variableDescriptor.getValue(entity);
                    changeMap.get(variableDescriptor).add(Pair.of(entity, value));
                }
            }
        });
        return new PartitionChangeMove<>(changeMap, partIndex);
    }

    private final Map<GenuineVariableDescriptor<Solution_>, List<Pair<Object, Object>>> changeMap;
    private final int partIndex;

    public PartitionChangeMove(Map<GenuineVariableDescriptor<Solution_>, List<Pair<Object, Object>>> changeMap,
            int partIndex) {
        this.changeMap = changeMap;
        this.partIndex = partIndex;
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        for (Map.Entry<GenuineVariableDescriptor<Solution_>, List<Pair<Object, Object>>> entry : changeMap.entrySet()) {
            GenuineVariableDescriptor<Solution_> variableDescriptor = entry.getKey();
            for (Pair<Object, Object> pair : entry.getValue()) {
                Object entity = pair.getKey();
                Object value = pair.getValue();
                innerScoreDirector.changeVariableFacade(variableDescriptor, entity, value);
            }
        }
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        return true;
    }

    @Override
    protected PartitionChangeMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        throw new UnsupportedOperationException("Impossible state: undo move should not be called.");
    }

    @Override
    public PartitionChangeMove<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        Map<GenuineVariableDescriptor<Solution_>, List<Pair<Object, Object>>> destinationChangeMap = new LinkedHashMap<>(
                changeMap.size());
        for (Map.Entry<GenuineVariableDescriptor<Solution_>, List<Pair<Object, Object>>> entry : changeMap.entrySet()) {
            GenuineVariableDescriptor<Solution_> variableDescriptor = entry.getKey();
            List<Pair<Object, Object>> originPairList = entry.getValue();
            List<Pair<Object, Object>> destinationPairList = new ArrayList<>(originPairList.size());
            for (Pair<Object, Object> pair : originPairList) {
                Object originEntity = pair.getKey();
                Object destinationEntity = destinationScoreDirector.lookUpWorkingObject(originEntity);
                if (destinationEntity == null && originEntity != null) {
                    throw new IllegalStateException("The destinationEntity (" + destinationEntity
                            + ") cannot be null if the originEntity (" + originEntity + ") is not null.");
                }
                Object originValue = pair.getValue();
                Object destinationValue = destinationScoreDirector.lookUpWorkingObject(originValue);
                if (destinationValue == null && originValue != null) {
                    throw new IllegalStateException("The destinationEntity (" + destinationEntity
                            + ")'s destinationValue (" + destinationValue
                            + ") cannot be null if the originEntity (" + originEntity
                            + ")'s originValue (" + originValue + ") is not null.\n"
                            + "Maybe add the originValue (" + originValue + ") of class (" + originValue.getClass()
                            + ") as problem fact in the planning solution with a "
                            + ProblemFactCollectionProperty.class.getSimpleName() + " annotation.");
                }
                destinationPairList.add(Pair.of(destinationEntity, destinationValue));
            }
            destinationChangeMap.put(variableDescriptor, destinationPairList);
        }
        return new PartitionChangeMove<>(destinationChangeMap, partIndex);
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        throw new UnsupportedOperationException("Impossible situation: " + PartitionChangeMove.class.getSimpleName()
                + " is only used to communicate between a part thread and the solver thread, it's never used in Tabu Search.");
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        throw new UnsupportedOperationException("Impossible situation: " + PartitionChangeMove.class.getSimpleName()
                + " is only used to communicate between a part thread and the solver thread, it's never used in Tabu Search.");
    }

    @Override
    public String toString() {
        int changeCount = changeMap.values().stream().mapToInt(List::size).sum();
        return "part-" + partIndex + " {" + changeCount + " variables changed}";
    }

}
