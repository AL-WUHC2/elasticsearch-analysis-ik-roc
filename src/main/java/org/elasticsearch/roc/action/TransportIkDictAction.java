package org.elasticsearch.roc.action;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.elasticsearch.action.support.nodes.NodeOperationRequest;
import org.elasticsearch.action.support.nodes.TransportNodesOperationAction;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.roc.action.IkDictResponse.IkNodeDictResponse;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.dic.Dictionary;

public abstract class TransportIkDictAction extends TransportNodesOperationAction<IkDictRequest, IkDictResponse,
        TransportIkDictAction.IkNodeDictRequest, IkDictResponse.IkNodeDictResponse> {

    protected ESLogger logger = null;

    public TransportIkDictAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
            ClusterService clusterService, TransportService transportService) {
        super(settings, clusterName, threadPool, clusterService, transportService);
        Dictionary.initial(new Configuration(new Environment(settings)));
        logger = Loggers.getLogger("ik-analyzer");
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.GENERIC;
    }

    @Override
    protected IkDictRequest newRequest() {
        return new IkDictRequest();
    }

    @Override
    protected IkNodeDictRequest newNodeRequest() {
        return new IkNodeDictRequest();
    }

    @Override
    protected IkNodeDictRequest newNodeRequest(String nodeId, IkDictRequest request) {
        return new IkNodeDictRequest(nodeId, request);
    }

    @Override
    protected IkDictResponse newResponse(IkDictRequest request, AtomicReferenceArray responses) {
        final List<IkDictResponse.IkNodeDictResponse> ikNodeDictResponses = newArrayList();
        for (int i = 0; i < responses.length(); i++) {
            Object resp = responses.get(i);
            if (resp instanceof IkDictResponse.IkNodeDictResponse) {
                ikNodeDictResponses.add((IkDictResponse.IkNodeDictResponse) resp);
            }
        }
        return new IkDictResponse(clusterName, ikNodeDictResponses.toArray(
                new IkDictResponse.IkNodeDictResponse[ikNodeDictResponses.size()]));
    }

    @Override
    protected IkNodeDictResponse newNodeResponse() {
        return new IkDictResponse.IkNodeDictResponse();
    }

    @Override
    protected boolean accumulateExceptions() {
        return false;
    }

    protected static class IkNodeDictRequest extends NodeOperationRequest {

        IkDictRequest request;

        private IkNodeDictRequest() {
        }

        private IkNodeDictRequest(String nodeId, IkDictRequest request) {
            super(request, nodeId);
            this.request = request;
        }

        public IkDictRequest request() {
            return request;
        }

    }

}
