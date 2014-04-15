package org.elasticsearch.roc.action;

import static org.elasticsearch.action.ValidateActions.addValidationError;

import java.io.IOException;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.nodes.NodesOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class IkDictRequest extends NodesOperationRequest<IkDictRequest> {

    private String[] words;

    public IkDictRequest() {
    }

    public IkDictRequest(String... words) {
        this.words = words;
    }

    public String[] words() {
        return words;
    }

    public final IkDictRequest words(String... words) {
        this.words = words;
        return this;
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = super.validate();
        if (words == null) {
            validationException = addValidationError("words is missing", validationException);
        }
        return validationException;
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
