package dera.frontend.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TerminalCommand {

    protected TerminalCommandMethod method;
    protected List<String> params;
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public TerminalCommandMethod getMethod() {
        return method;
    }

    public void setMethod(TerminalCommandMethod method) {
        this.method = method;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
}
