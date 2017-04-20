package it.unibz.inf.ontop.executor.join;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.DataNode;

/**
 * Uses unique constraints to detect and remove redundant self inner joins
 */
@Singleton
public class UCRedundantSelfJoinExecutor extends RedundantSelfJoinExecutor {

    @Inject
    private UCRedundantSelfJoinExecutor(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }


    /**
     * TODO: explain
     *
     */
    @Override
    protected PredicateLevelProposal proposePerPredicate(ImmutableCollection<DataNode> initialNodes,
                                                         AtomPredicate predicate, DBMetadata dbMetadata)
            throws AtomUnificationException {
        ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> uniqueConstraints = dbMetadata.getUniqueConstraints();

        if (uniqueConstraints.containsKey(predicate)) {
            ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMap = groupByUniqueConstraintArguments(
                    initialNodes, uniqueConstraints.get(predicate));
            return proposeForGroupingMap(groupingMap);
        }
        else {
            return new PredicateLevelProposal(initialNodes);
        }
    }

    /**
     * dataNodes and UC positions are given for the same predicate
     * TODO: explain
     */
    private static ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupByUniqueConstraintArguments(
            ImmutableCollection<DataNode> dataNodes,
            ImmutableCollection<ImmutableList<Integer>> collectionOfUCPositions) {
        ImmutableMultimap.Builder<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMapBuilder = ImmutableMultimap.builder();

        for (ImmutableList<Integer> primaryKeyPositions : collectionOfUCPositions) {
            for (DataNode dataNode : dataNodes) {
                groupingMapBuilder.put(extractArguments(dataNode.getProjectionAtom(), primaryKeyPositions), dataNode);
            }
        }
        return groupingMapBuilder.build();
    }
}
