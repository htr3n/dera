package dera.frontend.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TerminalCommandResult {

    protected static final String JSON_RPC_ID = "2.0";
    protected String error = null;
    protected List<String> result = new ArrayList<>();
    protected String id = null;

    public String getJsonrpc() {
        return JSON_RPC_ID;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
