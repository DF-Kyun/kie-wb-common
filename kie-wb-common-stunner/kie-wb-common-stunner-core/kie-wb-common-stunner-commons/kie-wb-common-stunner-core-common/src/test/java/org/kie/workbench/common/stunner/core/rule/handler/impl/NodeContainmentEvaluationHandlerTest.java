/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.core.rule.handler.impl;

import java.util.HashSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;
import org.kie.workbench.common.stunner.core.rule.RuleViolations;
import org.kie.workbench.common.stunner.core.rule.context.NodeContainmentContext;
import org.kie.workbench.common.stunner.core.rule.impl.CanContain;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NodeContainmentEvaluationHandlerTest extends AbstractGraphRuleHandlerTest {

    private final static CanContain RULE = new CanContain("r1",
                                                          DEFINITION_ID,
                                                          CANDIDATE_LABELS);
    private final static CanContain RULE_INVALID = new CanContain("r2",
                                                                  DEFINITION_ID,
                                                                  new HashSet<String>(1) {{
                                                                      add("notExists");
                                                                  }});
    private final ContainmentEvaluationHandler CONTAINMENT_HANDLER = new ContainmentEvaluationHandler();

    @Mock
    NodeContainmentContext context;
    private NodeContainmentEvaluationHandler tested;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        super.setup();
        when(context.getParent()).thenReturn(Optional.of(element));
        tested = new NodeContainmentEvaluationHandler(definitionManager,
                                                      CONTAINMENT_HANDLER);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAcceptSuccess() {
        when(context.getParent()).thenReturn(Optional.of(element));
        when(context.getCandidate()).thenReturn(candidate);
        final boolean accepts = tested.accepts(RULE,
                                               context);
        assertTrue(accepts);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAcceptFailed() {
        when(context.getParent()).thenReturn(Optional.of(parent));
        when(context.getCandidate()).thenReturn(candidate);
        final boolean accepts = tested.accepts(RULE,
                                               context);
        assertFalse(accepts);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluateSuccess() {
        when(context.getCandidate()).thenReturn(candidate);
        final RuleViolations violations = tested.evaluate(RULE,
                                                          context);
        assertNotNull(violations);
        assertFalse(violations.violations(RuleViolation.Type.ERROR).iterator().hasNext());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEvaluateFailed() {
        when(context.getCandidate()).thenReturn(candidate);
        final RuleViolations violations = tested.evaluate(RULE_INVALID,
                                                          context);
        assertNotNull(violations);
        assertTrue(violations.violations(RuleViolation.Type.ERROR).iterator().hasNext());
    }
}
