/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.client.forms.fields.assignmentsEditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.stunner.bpmn.client.forms.fields.i18n.StunnerFormsClientFieldsConstants;
import org.kie.workbench.common.stunner.bpmn.client.forms.fields.model.AssignmentData;
import org.kie.workbench.common.stunner.bpmn.client.forms.fields.model.Variable;
import org.kie.workbench.common.stunner.bpmn.client.forms.util.StringUtils;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagram;
import org.kie.workbench.common.stunner.bpmn.definition.BaseTask;
import org.kie.workbench.common.stunner.bpmn.definition.UserTask;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.DataIOModel;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;
import org.kie.workbench.common.stunner.bpmn.service.DataTypesService;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
@Templated
public class AssignmentsEditorWidget extends Composite implements HasValue<String> {

    @Inject
    @DataField
    private Button assignmentsButton;

    @Inject
    @DataField
    private TextBox assignmentsTextBox;

    @Inject
    protected ActivityDataIOEditor activityDataIOEditor;

    @Inject
    SessionManager canvasSessionManager;

    @Inject
    protected Event<NotificationEvent> notification;

    private BPMNDefinition bpmnModel;

    protected String assignmentsInfo;

    protected boolean hasInputVars = false;
    protected boolean isSingleInputVar = false;
    protected boolean hasOutputVars = false;
    protected boolean isSingleOutputVar = false;

    @EventHandler("assignmentsButton")
    public void onClickAssignmentsButton(final ClickEvent clickEvent) {
        showAssignmentsDialog();
    }

    @EventHandler("assignmentsTextBox")
    public void onClickAssignmentsTextBox(final ClickEvent clickEvent) {
        showAssignmentsDialog();
    }

    @Override
    public String getValue() {
        return assignmentsInfo;
    }

    @Override
    public void setValue(final String value) {
        setValue(value,
                 false);
    }

    @Override
    public void setValue(final String value,
                         final boolean fireEvents) {
        String oldValue = assignmentsInfo;
        assignmentsInfo = value;
        initTextBox();
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this,
                                            oldValue,
                                            assignmentsInfo);
        }
    }

    protected void setBPMNModel(final BPMNDefinition bpmnModel) {
        this.bpmnModel = bpmnModel;

        if (bpmnModel instanceof DataIOModel) {
            DataIOModel dataIOModel = (DataIOModel) bpmnModel;
            hasInputVars = dataIOModel.hasInputVars();
            isSingleInputVar = dataIOModel.isSingleInputVar();
            hasOutputVars = dataIOModel.hasOutputVars();
            isSingleOutputVar = dataIOModel.isSingleOutputVar();
        } else {
            hasInputVars = false;
            isSingleInputVar = false;
            hasOutputVars = false;
            isSingleOutputVar = false;
        }
    }

    protected void initTextBox() {
        Map<String, String> assignmentsProperties = parseAssignmentsInfo();
        String variableCountsString = getVariableCountsString(assignmentsProperties.get("datainput"),
                                                              assignmentsProperties.get("datainputset"),
                                                              assignmentsProperties.get("dataoutput"),
                                                              assignmentsProperties.get("dataoutputset"),
                                                              getProcessVariables(),
                                                              assignmentsProperties.get("assignments"),
                                                              getDisallowedPropertyNames());
        assignmentsTextBox.setText(variableCountsString);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {
        return addHandler(handler,
                          ValueChangeEvent.getType());
    }

    public void showAssignmentsDialog() {
        // Get data types to show the editor
        getDataTypes();
    }

    protected void getDataTypes() {

        final String simpleDataTypes = "Boolean:Boolean,Float:Float,Integer:Integer,Object:Object,String:String";
        MessageBuilder.createCall(
                new RemoteCallback<List<String>>() {
                    public void callback(List<String> dataTypes) {
                        String formattedDataTypes = formatDataTypes(dataTypes);
                        String allDataTypes = simpleDataTypes + "," + formattedDataTypes;
                        showDataIOEditor(allDataTypes.toString());
                    }
                },
                new BusErrorCallback() {
                    public boolean error(Message message,
                                         Throwable t) {
                        notification.fire(new NotificationEvent(StunnerFormsClientFieldsConstants.INSTANCE.Error_retrieving_datatypes(),
                                                                NotificationEvent.NotificationType.ERROR));
                        showDataIOEditor(simpleDataTypes);
                        return false;
                    }
                },
                DataTypesService.class).getDataTypeNames();
    }

    public void showDataIOEditor(final String datatypes) {
        String taskName = getTaskName();
        String processvars = getProcessVariables();

        Map<String, String> assignmentsProperties = parseAssignmentsInfo();
        String datainput = assignmentsProperties.get("datainput");
        String datainputset = assignmentsProperties.get("datainputset");
        String dataoutput = assignmentsProperties.get("dataoutput");
        String dataoutputset = assignmentsProperties.get("dataoutputset");
        String assignments = assignmentsProperties.get("assignments");

        String disallowedpropertynames = getDisallowedPropertyNames();

        String inputvars = null;
        if (datainput != null) {
            inputvars = datainput;
        }
        if (datainputset != null) {
            inputvars = datainputset;
        }
        String outputvars = null;
        if (dataoutput != null) {
            outputvars = dataoutput;
        }
        if (dataoutputset != null) {
            outputvars = dataoutputset;
        }
        AssignmentData assignmentData = new AssignmentData(inputvars,
                                                           outputvars,
                                                           processvars,
                                                           assignments,
                                                           datatypes,
                                                           disallowedpropertynames);
        assignmentData.setVariableCountsString(hasInputVars,
                                               isSingleInputVar,
                                               hasOutputVars,
                                               isSingleOutputVar);

        ActivityDataIOEditor.GetDataCallback callback = new ActivityDataIOEditor.GetDataCallback() {
            @Override
            public void getData(String assignmentDataJson) {
                AssignmentData assignmentData = Marshalling.fromJSON(assignmentDataJson,
                                                                     AssignmentData.class);
                String assignmentsInfoString = createAssignmentsInfoString(assignmentData);
                setValue(assignmentsInfoString,
                         true);
            }
        };
        activityDataIOEditor.setCallback(callback);

        activityDataIOEditor.setAssignmentData(assignmentData);
        activityDataIOEditor.setDisallowedPropertyNames(assignmentData.getDisallowedPropertyNames());
        activityDataIOEditor.setInputAssignmentRows(assignmentData.getAssignmentRows(Variable.VariableType.INPUT));
        activityDataIOEditor.setOutputAssignmentRows(assignmentData.getAssignmentRows(Variable.VariableType.OUTPUT));
        activityDataIOEditor.setDataTypes(assignmentData.getDataTypes(),
                                          assignmentData.getDataTypeDisplayNames());
        activityDataIOEditor.setProcessVariables(assignmentData.getProcessVariableNames());
        activityDataIOEditor.configureDialog(taskName,
                                             hasInputVars,
                                             isSingleInputVar,
                                             hasOutputVars,
                                             isSingleOutputVar);
        activityDataIOEditor.show();
    }

    protected String getTaskName() {
        String taskName = "Task";
        if (bpmnModel != null && bpmnModel instanceof BaseTask) {
            BaseTask task = (BaseTask) bpmnModel;
            if (task.getGeneral() != null && task.getGeneral().getName() != null &&
                    task.getGeneral().getName().getValue() != null && task.getGeneral().getName().getValue().length() > 0) {
                taskName = task.getGeneral().getName().getValue();
            }
        }
        return taskName;
    }

    protected String getProcessVariables() {
        Diagram diagram = canvasSessionManager.getCurrentSession().getCanvasHandler().getDiagram();
        Iterator<Element> it = diagram.getGraph().nodes().iterator();
        while (it.hasNext()) {
            Element element = it.next();
            if (element.getContent() instanceof View) {
                Object oDefinition = ((View) element.getContent()).getDefinition();
                if (oDefinition instanceof BPMNDiagram) {
                    BPMNDiagram bpmnDiagram = (BPMNDiagram) oDefinition;
                    ProcessVariables variables = bpmnDiagram.getProcessData().getProcessVariables();
                    if (variables != null) {
                        return variables.getValue();
                    }
                    break;
                }
            }
        }
        return null;
    }

    protected String formatDataTypes(final List<String> dataTypes) {
        StringBuilder sb = new StringBuilder();
        if (dataTypes != null && !dataTypes.isEmpty()) {
            List<String> formattedDataTypes = new ArrayList<String>(dataTypes.size());
            for (String dataType : dataTypes) {
                int i = dataType.lastIndexOf('.');
                StringBuilder formattedDataType = new StringBuilder(StringUtils.createDataTypeDisplayName(dataType));
                formattedDataType.append(":").append(dataType);
                formattedDataTypes.add(formattedDataType.toString());
            }
            Collections.sort(formattedDataTypes);
            for (String formattedDataType : formattedDataTypes) {
                sb.append(formattedDataType).append(',');
            }
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    protected Map<String, String> parseAssignmentsInfo() {
        Map<String, String> properties = new HashMap<String, String>();
        if (assignmentsInfo != null) {
            String[] parts = assignmentsInfo.split("\\|");
            if (parts.length > 0 && parts[0] != null && parts[0].length() > 0) {
                properties.put("datainput",
                               parts[0]);
            }
            if (parts.length > 1 && parts[1] != null && parts[1].length() > 0) {
                properties.put("datainputset",
                               parts[1]);
            }
            if (parts.length > 2 && parts[2] != null && parts[2].length() > 0) {
                properties.put("dataoutput",
                               parts[2]);
            }
            if (parts.length > 3 && parts[3] != null && parts[3].length() > 0) {
                properties.put("dataoutputset",
                               parts[3]);
            }
            if (parts.length > 4 && parts[4] != null && parts[4].length() > 0) {
                properties.put("assignments",
                               parts[4]);
            }
        }
        return properties;
    }

    protected String createAssignmentsInfoString(final AssignmentData assignmentData) {
        StringBuilder sb = new StringBuilder();
        String dataInput = "";
        String dataInputs = "";
        String dataOutput = "";
        String dataOutputs = "";
        if (hasInputVars) {
            if (isSingleInputVar) {
                dataInput = assignmentData.getInputVariablesString();
            } else {
                dataInputs = assignmentData.getInputVariablesString();
            }
        }
        if (hasOutputVars) {
            if (isSingleOutputVar) {
                dataOutput = assignmentData.getOutputVariablesString();
            } else {
                dataOutputs = assignmentData.getOutputVariablesString();
            }
        }

        sb.append(dataInput).append('|').append(dataInputs).append('|').append(dataOutput).append('|').
                append(dataOutputs)
                .append('|').append(assignmentData.getAssignmentsString());
        return sb.toString();
    }

    protected String getVariableCountsString(final String datainput,
                                             final String datainputset,
                                             final String dataoutput,
                                             final String dataoutputset,
                                             final String processvars,
                                             final String assignments,
                                             final String disallowedpropertynames) {
        String inputvars = null;
        if (datainput != null) {
            inputvars = datainput;
        }
        if (datainputset != null) {
            inputvars = datainputset;
        }
        String outputvars = null;
        if (dataoutput != null) {
            outputvars = dataoutput;
        }
        if (dataoutputset != null) {
            outputvars = dataoutputset;
        }
        AssignmentData assignmentData = new AssignmentData(inputvars,
                                                           outputvars,
                                                           processvars,
                                                           assignments,
                                                           disallowedpropertynames);
        return assignmentData.getVariableCountsString(hasInputVars,
                                                      isSingleInputVar,
                                                      hasOutputVars,
                                                      isSingleOutputVar);
    }

    protected String getDisallowedPropertyNames() {
        if (bpmnModel instanceof UserTask) {
            return "GroupId,Skippable,Comment,Description,Priority,Content,TaskName,Locale,CreatedBy,NotCompletedReassign,NotStartedReassign,NotCompletedNotify,NotStartedNotify";
        } else {
            return "";
        }
    }
}
