/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.core.graph.command.impl;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;
import org.kie.workbench.common.stunner.core.rule.RuleEvaluationContext;
import org.kie.workbench.common.stunner.core.rule.RuleSet;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;
import org.kie.workbench.common.stunner.core.rule.RuleViolations;
import org.kie.workbench.common.stunner.core.rule.context.CardinalityContext;
import org.kie.workbench.common.stunner.core.rule.context.ConnectorCardinalityContext;
import org.kie.workbench.common.stunner.core.rule.context.GraphConnectionContext;
import org.kie.workbench.common.stunner.core.rule.violations.ContainmentRuleViolation;
import org.kie.workbench.common.stunner.core.rule.violations.DefaultRuleViolations;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeleteConnectorCommandTest extends AbstractGraphCommandTest {

    private static final String SOURCE_UUID = "sourceUUID";
    private static final String TARGET_UUID = "targetUUID";
    private static final String UUID = "edgeUUID";

    private Node source;
    private Node target;
    private Edge edge;
    @Mock
    private ViewConnector connContent;
    private DeleteConnectorCommand tested;

    @Before
    public void setup() throws Exception {
        super.init(500,
                   500);
        source = mockNode(SOURCE_UUID);
        target = mockNode(TARGET_UUID);
        edge = mockEdge(UUID);
        when(graphIndex.getNode(eq(SOURCE_UUID))).thenReturn(source);
        when(graphIndex.getNode(eq(TARGET_UUID))).thenReturn(target);
        when(graphIndex.getEdge(eq(UUID))).thenReturn(edge);
        when(edge.getContent()).thenReturn(connContent);
        when(edge.getSourceNode()).thenReturn(source);
        when(edge.getTargetNode()).thenReturn(target);
        when(connContent.getSourceMagnetIndex()).thenReturn(0);
        when(connContent.getTargetMagnetIndex()).thenReturn(1);
        this.tested = new DeleteConnectorCommand(UUID);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAllow() {
        CommandResult<RuleViolation> result = tested.allow(graphCommandExecutionContext);
        assertEquals(CommandResult.Type.INFO,
                     result.getType());
        final ArgumentCaptor<RuleEvaluationContext> contextCaptor = ArgumentCaptor.forClass(RuleEvaluationContext.class);
        verify(ruleManager,
               times(4)).evaluate(eq(ruleSet),
                                  contextCaptor.capture());
        final List<RuleEvaluationContext> contexts = contextCaptor.getAllValues();
        assertEquals(4,
                     contexts.size());
        verifyConnection((GraphConnectionContext) contexts.get(0),
                         edge,
                         null,
                         target);
        verifyConnectorCardinality((ConnectorCardinalityContext) contexts.get(1),
                                   graph,
                                   source,
                                   edge,
                                   ConnectorCardinalityContext.Direction.OUTGOING,
                                   CardinalityContext.Operation.DELETE);
        verifyConnection((GraphConnectionContext) contexts.get(2),
                         edge,
                         source,
                         null);
        verifyConnectorCardinality((ConnectorCardinalityContext) contexts.get(3),
                                   graph,
                                   target,
                                   edge,
                                   ConnectorCardinalityContext.Direction.INCOMING,
                                   CardinalityContext.Operation.DELETE);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAllowNoRules() {
        when(graphCommandExecutionContext.getRuleManager()).thenReturn(null);
        CommandResult<RuleViolation> result = tested.allow(graphCommandExecutionContext);
        assertEquals(CommandResult.Type.INFO,
                     result.getType());
        verify(ruleManager,
               times(0)).evaluate(any(RuleSet.class),
                                  any(RuleEvaluationContext.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNotAllowed() {
        final RuleViolations FAILED_VIOLATIONS = new DefaultRuleViolations()
                .addViolation(new ContainmentRuleViolation(graph.getUUID(),
                                                           UUID));
        when(ruleManager.evaluate(any(RuleSet.class),
                                  any(RuleEvaluationContext.class))).thenReturn(FAILED_VIOLATIONS);
        CommandResult<RuleViolation> result = tested.allow(graphCommandExecutionContext);
        assertEquals(CommandResult.Type.ERROR,
                     result.getType());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecute() {
        final List sourceOutEdges = mock(List.class);
        final List targetInEdges = mock(List.class);
        when(source.getOutEdges()).thenReturn(sourceOutEdges);
        when(target.getInEdges()).thenReturn(targetInEdges);
        CommandResult<RuleViolation> result = tested.execute(graphCommandExecutionContext);
        assertEquals(CommandResult.Type.INFO,
                     result.getType());
        verify(sourceOutEdges,
               times(1)).remove(eq(edge));
        verify(targetInEdges,
               times(1)).remove(eq(edge));
        verify(graphIndex,
               times(1)).removeEdge(eq(edge));
        verify(graphIndex,
               times(0)).addEdge(any(Edge.class));
        verify(graphIndex,
               times(0)).addNode(any(Node.class));
        verify(graphIndex,
               times(0)).removeNode(any(Node.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteCheckFailed() {
        final RuleViolations FAILED_VIOLATIONS = new DefaultRuleViolations()
                .addViolation(new ContainmentRuleViolation(graph.getUUID(),
                                                           UUID));
        when(ruleManager.evaluate(any(RuleSet.class),
                                  any(RuleEvaluationContext.class))).thenReturn(FAILED_VIOLATIONS);
        final List sourceOutEdges = mock(List.class);
        final List targetInEdges = mock(List.class);
        when(source.getOutEdges()).thenReturn(sourceOutEdges);
        when(target.getInEdges()).thenReturn(targetInEdges);
        CommandResult<RuleViolation> result = tested.execute(graphCommandExecutionContext);
        assertEquals(CommandResult.Type.ERROR,
                     result.getType());
        verify(sourceOutEdges,
               times(0)).remove(any(Edge.class));
        verify(targetInEdges,
               times(0)).remove(any(Edge.class));
        verify(graphIndex,
               times(0)).removeNode(any(Node.class));
        verify(graphIndex,
               times(0)).removeEdge(any(Edge.class));
        verify(graphIndex,
               times(0)).addEdge(any(Edge.class));
        verify(graphIndex,
               times(0)).addNode(any(Node.class));
    }
}
