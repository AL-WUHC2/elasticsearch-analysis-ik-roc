package org.elasticsearch.roc.action;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.ClusterAction;
import org.elasticsearch.client.ClusterAdminClient;

public abstract class IkDictAction extends ClusterAction<IkDictRequest, IkDictResponse, IkDictRequestBuilder> {

    public abstract IkDictAction instance();

    protected IkDictAction(String name) {
        super(name);
    }

    @Override
    public IkDictRequestBuilder newRequestBuilder(ClusterAdminClient client) {
        return new IkDictRequestBuilder(client) {
            @Override
            protected void doExecute(ActionListener<IkDictResponse> listener) {
                ((ClusterAdminClient) client).execute(instance(), request, listener);
            }
        };
    }

    @Override
    public IkDictResponse newResponse() {
        return new IkDictResponse();
    }

}
