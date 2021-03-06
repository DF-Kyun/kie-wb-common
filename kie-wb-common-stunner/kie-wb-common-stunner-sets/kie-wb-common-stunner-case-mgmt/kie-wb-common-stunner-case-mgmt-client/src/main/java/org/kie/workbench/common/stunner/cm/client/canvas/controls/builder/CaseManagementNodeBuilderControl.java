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

package org.kie.workbench.common.stunner.cm.client.canvas.controls.builder;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.cm.qualifiers.CaseManagementEditor;
import org.kie.workbench.common.stunner.core.client.api.ClientDefinitionManager;
import org.kie.workbench.common.stunner.core.client.api.ShapeManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.builder.impl.AbstractElementBuilderControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.builder.impl.Element;
import org.kie.workbench.common.stunner.core.client.canvas.controls.builder.impl.NodeBuilderControlImpl;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.shape.util.EdgeMagnetsHelper;

@Dependent
@CaseManagementEditor
public class CaseManagementNodeBuilderControl extends NodeBuilderControlImpl {

    @Inject
    public CaseManagementNodeBuilderControl(final ClientDefinitionManager clientDefinitionManager,
                                            final ShapeManager shapeManager,
                                            final @CaseManagementEditor CanvasCommandFactory<AbstractCanvasHandler> commandFactory,
                                            final @Element @CaseManagementEditor AbstractElementBuilderControl elementBuilderControl,
                                            final EdgeMagnetsHelper magnetsHelper) {
        super(clientDefinitionManager,
              shapeManager,
              commandFactory,
              elementBuilderControl,
              magnetsHelper);
    }
}
