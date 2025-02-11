package ai.timefold.solver.test.impl.score.stream;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.BiFunction;

import ai.timefold.solver.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactory;
import ai.timefold.solver.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactoryService;
import ai.timefold.solver.constraint.streams.common.InnerConstraintFactory;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorFactoryService;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorType;

/**
 * Designed for access from a single thread.
 * Callers are responsible for ensuring that instances are never run from a thread other than that which created them.
 *
 * @param <ConstraintProvider_>
 * @param <Solution_>
 * @param <Score_>
 */
final class ScoreDirectorFactoryCache<ConstraintProvider_ extends ConstraintProvider, Solution_, Score_ extends Score<Score_>> {

    /**
     * Score director factory creation is expensive; we cache it.
     * The cache needs to be recomputed every time that the parent's configuration changes.
     */
    private final Map<String, AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_>> scoreDirectorFactoryMap =
            new HashMap<>();

    private final ConfiguredConstraintVerifier<ConstraintProvider_, Solution_, Score_> parent;
    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final ServiceLoader<ScoreDirectorFactoryService<Solution_, Score_>> serviceLoader;

    public ScoreDirectorFactoryCache(ConfiguredConstraintVerifier<ConstraintProvider_, Solution_, Score_> parent,
            SolutionDescriptor<Solution_> solutionDescriptor) {
        this.parent = Objects.requireNonNull(parent);
        this.solutionDescriptor = Objects.requireNonNull(solutionDescriptor);
        this.serviceLoader = (ServiceLoader) ServiceLoader.load(ScoreDirectorFactoryService.class);
    }

    private AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_> getScoreDirectorFactoryService() {
        var constraintStreamImplType = parent.getConstraintStreamImplType();
        return serviceLoader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(s -> s.getSupportedScoreDirectorType() == ScoreDirectorType.CONSTRAINT_STREAMS)
                .map(s -> (AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_>) s)
                .filter(s -> constraintStreamImplType == null || s.supportsImplType(constraintStreamImplType))
                .max(Comparator.comparingInt(ScoreDirectorFactoryService::getPriority)) // Picks CS-D if both available.
                .orElseThrow(() -> new IllegalStateException(
                        "Constraint Streams implementation was not found on the classpath.\n"
                                + "Maybe include the ai.timefold.solver:timefold-solver-constraint-streams-bavet dependency "
                                + "in your project?\n"
                                + "Maybe ensure your uberjar bundles META-INF/services from included JAR files?"));
    }

    /**
     * Retrieve {@link AbstractConstraintStreamScoreDirectorFactory} from the cache,
     * or create and cache a new instance using the {@link AbstractConstraintStreamScoreDirectorFactoryService}.
     * Cache key is the ID of the single constraint returned by calling the constraintFunction.
     *
     * @param constraintFunction never null, determines the single constraint to be used from the constraint provider
     * @param constraintProvider never null, determines the constraint provider to be used
     * @return never null
     */
    public AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> getScoreDirectorFactory(
            BiFunction<ConstraintProvider_, ConstraintFactory, Constraint> constraintFunction,
            ConstraintProvider_ constraintProvider, EnvironmentMode environmentMode) {
        var scoreDirectorFactoryService = getScoreDirectorFactoryService();
        /*
         * Apply all validations on the constraint factory before extracting the one constraint.
         * This step is only necessary to perform validation of the constraint provider;
         * if we only wanted the one constraint, we could just call constraintFunction directly.
         */
        InnerConstraintFactory<Solution_, ?> fullConstraintFactory =
                (InnerConstraintFactory<Solution_, ?>) scoreDirectorFactoryService.buildConstraintFactory(solutionDescriptor,
                        environmentMode);
        List<Constraint> constraints = (List<Constraint>) fullConstraintFactory.buildConstraints(constraintProvider);
        Constraint expectedConstraint = constraintFunction.apply(constraintProvider, fullConstraintFactory);
        Constraint result = constraints.stream()
                .filter(c -> Objects.equals(c.getConstraintId(), expectedConstraint.getConstraintId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Impossible state: Constraint provider (" + constraintProvider
                        + ") has no constraint (" + expectedConstraint + ")."));
        var constraintId = result.getConstraintId();
        return getScoreDirectorFactory(constraintId,
                constraintFactory -> new Constraint[] {
                        result
                }, environmentMode);
    }

    /**
     * Retrieve {@link AbstractConstraintStreamScoreDirectorFactory} from the cache,
     * or create and cache a new instance using the {@link AbstractConstraintStreamScoreDirectorFactoryService}.
     *
     * @param key never null, unique identifier of the factory in the cache
     * @param constraintProvider never null, constraint provider to create the factory from; ignored on cache hit
     * @return never null
     */
    public AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> getScoreDirectorFactory(String key,
            ConstraintProvider constraintProvider, EnvironmentMode environmentMode) {
        return scoreDirectorFactoryMap.computeIfAbsent(key,
                k -> createScoreDirectorFactory(getScoreDirectorFactoryService(), constraintProvider, environmentMode));
    }

    private AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> createScoreDirectorFactory(
            AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_> scoreDirectorFactoryService,
            ConstraintProvider constraintProvider, EnvironmentMode environmentMode) {
        return scoreDirectorFactoryService.buildScoreDirectorFactory(solutionDescriptor, constraintProvider,
                environmentMode);
    }

}
