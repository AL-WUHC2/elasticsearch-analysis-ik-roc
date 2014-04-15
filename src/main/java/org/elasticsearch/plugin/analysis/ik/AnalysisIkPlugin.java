package org.elasticsearch.plugin.analysis.ik;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.IkAnalysisBinderProcessor;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.roc.action.impl.IkDictAddAction;
import org.elasticsearch.roc.action.impl.IkDictRemoveAction;
import org.elasticsearch.roc.action.impl.TransportIkDictAddAction;
import org.elasticsearch.roc.action.impl.TransportIkDictRemoveAction;
import org.elasticsearch.roc.rest.action.RestIkDictAction;

public class AnalysisIkPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "analysis-ik";
    }

    @Override
    public String description() {
        return "ik analysis";
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new IkAnalysisBinderProcessor());
    }

    public void onModule(ActionModule module) {
        module.registerAction(IkDictAddAction.INSTANCE, TransportIkDictAddAction.class);
        module.registerAction(IkDictRemoveAction.INSTANCE, TransportIkDictRemoveAction.class);
    }

    public void onModule(RestModule module) {
        module.addRestAction(RestIkDictAction.class);
    }

}
