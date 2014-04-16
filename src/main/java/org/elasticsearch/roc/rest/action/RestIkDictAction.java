package org.elasticsearch.roc.rest.action;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.XContentThrowableRestResponse;
import org.elasticsearch.rest.action.support.RestXContentBuilder;
import org.elasticsearch.roc.action.IkDictAction;
import org.elasticsearch.roc.action.IkDictRequest;
import org.elasticsearch.roc.action.IkDictResponse;
import org.elasticsearch.roc.action.IkDictResponse.IkNodeDictResponse;
import org.elasticsearch.roc.action.impl.IkDictAddAction;
import org.elasticsearch.roc.action.impl.IkDictRemoveAction;

public class RestIkDictAction extends BaseRestHandler {

    @Inject
    public RestIkDictAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(GET, "/_ik_dict_add", this);
        controller.registerHandler(GET, "/_ik_dict_remove", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        String words = request.param("words");
        if (words == null || words.length() == 0)
            responseFailed(request, channel, "None word send.");

        IkDictRequest ikRequest = new IkDictRequest().words(splitParamWords(words));
        IkDictAction action = request.path().equals("/_ik_dict_add") ?
                IkDictAddAction.INSTANCE : IkDictRemoveAction.INSTANCE;

        client.admin().cluster().execute(action, ikRequest, new ActionListener<IkDictResponse>() {
            @Override
            public void onResponse(IkDictResponse response) {
                try {
                    XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
                    builder.startObject().startObject("result");
                    IkNodeDictResponse[] nodeResponses = response.getNodes();
                    for (IkNodeDictResponse nodeResponse : nodeResponses) {
                        builder.array(nodeResponse.getNode().name(), nodeResponse.words());
                    }
                    builder.endObject().endObject();
                    channel.sendResponse(new XContentRestResponse(request, OK, builder));
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                responseFailed(request, channel, e);
            }}
        );
    }

    private String[] splitParamWords(String param) {
        final StringTokenizer st = new StringTokenizer(param, ",");
        final List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            token = token.trim();
            if (token.length() > 0) tokens.add(token);
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    private void responseFailed(RestRequest request, RestChannel channel, String msg) {
        responseFailed(request, channel, new RuntimeException(msg));
    }

    private void responseFailed(RestRequest request, RestChannel channel, Throwable e) {
        try {
            channel.sendResponse(new XContentThrowableRestResponse(request, e));
        } catch (IOException o) {
            logger.error("Failed to send failure response", o);
        }
    }

}
