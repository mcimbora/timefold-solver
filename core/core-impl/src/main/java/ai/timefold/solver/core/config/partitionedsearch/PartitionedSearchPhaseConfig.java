package ai.timefold.solver.core.config.partitionedsearch;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.exhaustivesearch.ExhaustiveSearchPhaseConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.phase.NoChangePhaseConfig;
import ai.timefold.solver.core.config.phase.PhaseConfig;
import ai.timefold.solver.core.config.phase.custom.CustomPhaseConfig;
import ai.timefold.solver.core.config.util.ConfigUtils;
import ai.timefold.solver.core.impl.io.jaxb.adapter.JaxbCustomPropertiesAdapter;
import ai.timefold.solver.core.impl.partitionedsearch.partitioner.SolutionPartitioner;

@XmlType(propOrder = {
        "solutionPartitionerClass",
        "solutionPartitionerCustomProperties",
        "runnablePartThreadLimit",
        "phaseConfigList"
})
public class PartitionedSearchPhaseConfig extends PhaseConfig<PartitionedSearchPhaseConfig> {

    public static final String XML_ELEMENT_NAME = "partitionedSearch";
    public static final String ACTIVE_THREAD_COUNT_AUTO = "AUTO";
    public static final String ACTIVE_THREAD_COUNT_UNLIMITED = "UNLIMITED";

    // Warning: all fields are null (and not defaulted) because they can be inherited
    // and also because the input config file should match the output config file

    protected Class<? extends SolutionPartitioner<?>> solutionPartitionerClass = null;
    @XmlJavaTypeAdapter(JaxbCustomPropertiesAdapter.class)
    protected Map<String, String> solutionPartitionerCustomProperties = null;

    protected String runnablePartThreadLimit = null;

    @XmlElements({
            @XmlElement(name = ConstructionHeuristicPhaseConfig.XML_ELEMENT_NAME,
                    type = ConstructionHeuristicPhaseConfig.class),
            @XmlElement(name = CustomPhaseConfig.XML_ELEMENT_NAME, type = CustomPhaseConfig.class),
            @XmlElement(name = ExhaustiveSearchPhaseConfig.XML_ELEMENT_NAME, type = ExhaustiveSearchPhaseConfig.class),
            @XmlElement(name = LocalSearchPhaseConfig.XML_ELEMENT_NAME, type = LocalSearchPhaseConfig.class),
            @XmlElement(name = NoChangePhaseConfig.XML_ELEMENT_NAME, type = NoChangePhaseConfig.class),
            @XmlElement(name = PartitionedSearchPhaseConfig.XML_ELEMENT_NAME, type = PartitionedSearchPhaseConfig.class)
    })
    protected List<PhaseConfig> phaseConfigList = null;

    // ************************************************************************
    // Constructors and simple getters/setters
    // ************************************************************************

    public Class<? extends SolutionPartitioner<?>> getSolutionPartitionerClass() {
        return solutionPartitionerClass;
    }

    public void setSolutionPartitionerClass(Class<? extends SolutionPartitioner<?>> solutionPartitionerClass) {
        this.solutionPartitionerClass = solutionPartitionerClass;
    }

    public Map<String, String> getSolutionPartitionerCustomProperties() {
        return solutionPartitionerCustomProperties;
    }

    public void setSolutionPartitionerCustomProperties(Map<String, String> solutionPartitionerCustomProperties) {
        this.solutionPartitionerCustomProperties = solutionPartitionerCustomProperties;
    }

    /**
     * Similar to a thread pool size, but instead of limiting the number of {@link Thread}s,
     * it limits the number of {@link java.lang.Thread.State#RUNNABLE runnable} {@link Thread}s to avoid consuming all
     * CPU resources (which would starve UI, Servlets and REST threads).
     * <p/>
     * The number of {@link Thread}s is always equal to the number of partitions returned by
     * {@link SolutionPartitioner#splitWorkingSolution(ScoreDirector, Integer)},
     * because otherwise some partitions would never run (especially with {@link Solver#terminateEarly() asynchronous
     * termination}).
     * If this limit (or {@link Runtime#availableProcessors()}) is lower than the number of partitions,
     * this results in a slower score calculation speed per partition {@link Solver}.
     * <p/>
     * Defaults to {@value #ACTIVE_THREAD_COUNT_AUTO} which consumes the majority
     * but not all of the CPU cores on multi-core machines, to prevent a livelock that hangs other processes
     * (such as your IDE, REST servlets threads or SSH connections) on the machine.
     * <p/>
     * Use {@value #ACTIVE_THREAD_COUNT_UNLIMITED} to give it all CPU cores.
     * This is useful if you're handling the CPU consumption on an OS level.
     *
     * @return null, a number, {@value #ACTIVE_THREAD_COUNT_AUTO} or {@value #ACTIVE_THREAD_COUNT_UNLIMITED}.
     */
    public String getRunnablePartThreadLimit() {
        return runnablePartThreadLimit;
    }

    public void setRunnablePartThreadLimit(String runnablePartThreadLimit) {
        this.runnablePartThreadLimit = runnablePartThreadLimit;
    }

    public List<PhaseConfig> getPhaseConfigList() {
        return phaseConfigList;
    }

    public void setPhaseConfigList(List<PhaseConfig> phaseConfigList) {
        this.phaseConfigList = phaseConfigList;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public PartitionedSearchPhaseConfig withSolutionPartitionerClass(
            Class<? extends SolutionPartitioner<?>> solutionPartitionerClass) {
        this.setSolutionPartitionerClass(solutionPartitionerClass);
        return this;
    }

    public PartitionedSearchPhaseConfig withSolutionPartitionerCustomProperties(
            Map<String, String> solutionPartitionerCustomProperties) {
        this.setSolutionPartitionerCustomProperties(solutionPartitionerCustomProperties);
        return this;
    }

    public PartitionedSearchPhaseConfig withRunnablePartThreadLimit(String runnablePartThreadLimit) {
        this.setRunnablePartThreadLimit(runnablePartThreadLimit);
        return this;
    }

    public PartitionedSearchPhaseConfig withPhaseConfigList(List<PhaseConfig> phaseConfigList) {
        this.setPhaseConfigList(phaseConfigList);
        return this;
    }

    public PartitionedSearchPhaseConfig withPhaseConfigs(PhaseConfig... phaseConfigs) {
        this.setPhaseConfigList(List.of(phaseConfigs));
        return this;
    }

    @Override
    public PartitionedSearchPhaseConfig inherit(PartitionedSearchPhaseConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        solutionPartitionerClass = ConfigUtils.inheritOverwritableProperty(solutionPartitionerClass,
                inheritedConfig.getSolutionPartitionerClass());
        solutionPartitionerCustomProperties = ConfigUtils.inheritMergeableMapProperty(
                solutionPartitionerCustomProperties, inheritedConfig.getSolutionPartitionerCustomProperties());
        runnablePartThreadLimit = ConfigUtils.inheritOverwritableProperty(runnablePartThreadLimit,
                inheritedConfig.getRunnablePartThreadLimit());
        phaseConfigList = ConfigUtils.inheritMergeableListConfig(
                phaseConfigList, inheritedConfig.getPhaseConfigList());
        return this;
    }

    @Override
    public PartitionedSearchPhaseConfig copyConfig() {
        return new PartitionedSearchPhaseConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        if (getTerminationConfig() != null) {
            getTerminationConfig().visitReferencedClasses(classVisitor);
        }
        classVisitor.accept(solutionPartitionerClass);
        if (phaseConfigList != null) {
            phaseConfigList.forEach(pc -> pc.visitReferencedClasses(classVisitor));
        }
    }

}
