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

public class TransportIkDictAddAction extends TransportIkDictAction {

    @Inject
    public TransportIkDictAddAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
            ClusterService clusterService, TransportService transportService) {
        super(settings, clusterName, threadPool, clusterService, transportService);
    }

    @Override
    protected String transportAction() {
        return IkDictAddAction.NAME;
    }

    @Override
    protected IkNodeDictResponse nodeOperation(IkNodeDictRequest request) throws ElasticsearchException {
        String[] words = request.request().words();
        logger.info("[Dict Add Words] " + Arrays.toString(words));
        List<String> added = Dictionary.getSingleton().addWords(Arrays.asList(words));
        String[] array = added.toArray(new String[added.size()]);
        logger.info("[Dict Added Words] " + Arrays.toString(array));
        Dictionary.getSingleton().addRocDict(added);
        return new IkNodeDictResponse(clusterService.localNode(), array);
    }

}
