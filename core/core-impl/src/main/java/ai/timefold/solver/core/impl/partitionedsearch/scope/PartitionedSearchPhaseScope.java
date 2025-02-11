package ai.timefold.solver.core.impl.partitionedsearch.scope;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.impl.phase.scope.AbstractPhaseScope;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class PartitionedSearchPhaseScope<Solution_> extends AbstractPhaseScope<Solution_> {

    private Integer partCount;

    private PartitionedSearchStepScope<Solution_> lastCompletedStepScope;

    public PartitionedSearchPhaseScope(SolverScope<Solution_> solverScope) {
        super(solverScope);
        lastCompletedStepScope = new PartitionedSearchStepScope<>(this, -1);
    }

    public Integer getPartCount() {
        return partCount;
    }

    public void setPartCount(Integer partCount) {
        this.partCount = partCount;
    }

    @Override
    public PartitionedSearchStepScope<Solution_> getLastCompletedStepScope() {
        return lastCompletedStepScope;
    }

    public void setLastCompletedStepScope(PartitionedSearchStepScope<Solution_> lastCompletedStepScope) {
        this.lastCompletedStepScope = lastCompletedStepScope;
    }

    // ************************************************************************
    // Calculated methods
    // ************************************************************************

}
