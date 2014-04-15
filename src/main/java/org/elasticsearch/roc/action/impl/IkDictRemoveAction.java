package org.elasticsearch.roc.action.impl;

import org.elasticsearch.roc.action.IkDictAction;

public class IkDictRemoveAction extends IkDictAction {

    public static final IkDictRemoveAction INSTANCE = new IkDictRemoveAction();

    public static final String NAME = "cluster/_ik_dict_remove";

    protected IkDictRemoveAction() {
        super(NAME);
    }

    @Override
    public IkDictAction instance() {
        return INSTANCE;
    }

}
