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

package org.kie.workbench.common.stunner.core.client.rule;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.core.registry.RegistryFactory;
import org.kie.workbench.common.stunner.core.rule.RuleEvaluationHandler;
import org.kie.workbench.common.stunner.core.rule.RuleManagerImpl;

@Specializes
@ApplicationScoped
public class ClientRuleManager extends RuleManagerImpl {

    private final ManagedInstance<RuleEvaluationHandler> ruleEvaluationHandlerInstances;

    protected ClientRuleManager() {
        this(null,
             null);
    }

    @Inject
    public ClientRuleManager(final RegistryFactory registryFactory,
                             final @Any ManagedInstance<RuleEvaluationHandler> ruleEvaluationHandlerInstances) {
        super(registryFactory);
        this.ruleEvaluationHandlerInstances = ruleEvaluationHandlerInstances;
    }

    @PostConstruct
    public void init() {
        ruleEvaluationHandlerInstances.forEach(registry()::register);
    }
}
