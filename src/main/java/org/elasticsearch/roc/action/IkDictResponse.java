package org.elasticsearch.roc.action;

import java.io.IOException;

import org.elasticsearch.action.support.nodes.NodeOperationResponse;
import org.elasticsearch.action.support.nodes.NodesOperationResponse;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class IkDictResponse extends NodesOperationResponse<IkDictResponse.IkNodeDictResponse> {

    IkDictResponse() {
    }

    public IkDictResponse(ClusterName clusterName, IkNodeDictResponse[] nodes) {
        super(clusterName, nodes);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        nodes = new IkNodeDictResponse[in.readVInt()];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = IkNodeDictResponse.readIkNodeDictResponse(in);
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeVInt(nodes.length);
        for (IkNodeDictResponse node : nodes) {
            node.writeTo(out);
        }
    }

    public static class IkNodeDictResponse extends NodeOperationResponse {

        private String[] words;

        IkNodeDictResponse() {
        }

        public IkNodeDictResponse(DiscoveryNode node, String... words) {
            super(node);
            this.words = words;
        }

        public static IkNodeDictResponse readIkNodeDictResponse(StreamInput in) throws IOException {
            IkNodeDictResponse res = new IkNodeDictResponse();
            res.readFrom(in);
            return res;
        }

        public String[] words() {
            return words;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            words = in.readStringArray();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeStringArrayNullable(words);
        }

    }

}
