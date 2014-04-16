package org.elasticsearch.roc.action.impl;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.roc.action.IkDictResponse.IkNodeDictResponse;
import org.elasticsearch.roc.action.TransportIkDictAction;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.wltea.analyzer.dic.Dictionary;

public class TransportIkDictRemoveAction extends TransportIkDictAction {

    @Inject
    public TransportIkDictRemoveAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
            ClusterService clusterService, TransportService transportService) {
        super(settings, clusterName, threadPool, clusterService, transportService);
    }

    @Override
    protected String transportAction() {
        return IkDictRemoveAction.NAME;
    }

    @Override
    protected IkNodeDictResponse nodeOperation(IkNodeDictRequest request) throws ElasticsearchException {
        String[] words = request.request().words();
        logger.info("[Dict Remove Words] " + Arrays.toString(words));
        List<String> removed = Dictionary.getSingleton().disableWords(Arrays.asList(words));
        String[] array = removed.toArray(new String[removed.size()]);
        logger.info("[Dict Removed Words] " + Arrays.toString(array));
        Dictionary.getSingleton().removeRocDict(removed);
        return new IkNodeDictResponse(clusterService.localNode(), array);
    }

}
