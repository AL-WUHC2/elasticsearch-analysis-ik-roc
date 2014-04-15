package org.elasticsearch.roc.action;

import org.elasticsearch.action.support.nodes.NodesOperationRequestBuilder;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.internal.InternalClusterAdminClient;

public abstract class IkDictRequestBuilder extends NodesOperationRequestBuilder<IkDictRequest, IkDictResponse, IkDictRequestBuilder> {

    public IkDictRequestBuilder(ClusterAdminClient clusterClient) {
        super((InternalClusterAdminClient) clusterClient, new IkDictRequest());
    }

    public IkDictRequestBuilder(ClusterAdminClient clusterClient, String... words) {
        super((InternalClusterAdminClient) clusterClient, new IkDictRequest(words));
    }

    public IkDictRequestBuilder setWords(String... words) {
        request.words(words);
        return this;
    }

}
