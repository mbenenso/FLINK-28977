/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.planner.plan.nodes.exec.batch;

import org.apache.flink.api.dag.Transformation;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.planner.delegation.PlannerBase;
import org.apache.flink.table.planner.plan.nodes.exec.ExecNodeConfig;
import org.apache.flink.table.planner.plan.nodes.exec.ExecNodeContext;
import org.apache.flink.table.planner.plan.nodes.exec.InputProperty;
import org.apache.flink.table.planner.plan.nodes.exec.common.CommonExecLookupJoin;
import org.apache.flink.table.planner.plan.nodes.exec.spec.TemporalTableSourceSpec;
import org.apache.flink.table.planner.plan.utils.LookupJoinUtil;
import org.apache.flink.table.runtime.operators.join.FlinkJoinType;
import org.apache.flink.table.types.logical.RowType;

import org.apache.calcite.rex.RexNode;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** {@link BatchExecNode} for temporal table join that implemented by lookup. */
public class BatchExecLookupJoin extends CommonExecLookupJoin implements BatchExecNode<RowData> {
    public BatchExecLookupJoin(
            ReadableConfig tableConfig,
            FlinkJoinType joinType,
            @Nullable RexNode joinCondition,
            TemporalTableSourceSpec temporalTableSourceSpec,
            Map<Integer, LookupJoinUtil.LookupKey> lookupKeys,
            @Nullable List<RexNode> projectionOnTemporalTable,
            @Nullable RexNode filterOnTemporalTable,
            InputProperty inputProperty,
            RowType outputType,
            String description) {
        super(
                ExecNodeContext.newNodeId(),
                ExecNodeContext.newContext(BatchExecLookupJoin.class),
                ExecNodeContext.newPersistedConfig(BatchExecLookupJoin.class, tableConfig),
                joinType,
                joinCondition,
                temporalTableSourceSpec,
                lookupKeys,
                projectionOnTemporalTable,
                filterOnTemporalTable,
                ChangelogMode.insertOnly(),
                Collections.singletonList(inputProperty),
                outputType,
                // batch lookup join does not support hint currently
                null,
                description);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Transformation<RowData> translateToPlanInternal(
            PlannerBase planner, ExecNodeConfig config) {
        // There's no optimization when lookupKeyContainsPrimaryKey is true for batch, so set it to
        // false for now. We can add it to CommonExecLookupJoin when needed.
        return createJoinTransformation(planner, config, false, false);
    }
}
