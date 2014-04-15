package org.elasticsearch.roc.action.impl;

import org.elasticsearch.roc.action.IkDictAction;

public class IkDictAddAction extends IkDictAction {

    public static final IkDictAddAction INSTANCE = new IkDictAddAction();

    public static final String NAME = "cluster/_ik_dict_add";

    protected IkDictAddAction() {
        super(NAME);
    }

    @Override
    public IkDictAction instance() {
        return INSTANCE;
    }

}
