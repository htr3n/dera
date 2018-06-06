package dera.frontend.rpc;

import dera.util.JacksonUtil;
import dera.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class TerminalServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.error("Get request " + request);
        PrintWriter out = response.getWriter();
        out.println("{\"jsonrpc\":\"2.0\",\"result\":[],\"error\":null,\"id\":1}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final InputStream inputStream = request.getInputStream();
        if (inputStream != null) {
            try {
                TerminalCommand command = (TerminalCommand) JacksonUtil.getObjectFromJsonMapper()
                        .convert(TerminalCommand.class, request.getInputStream(), HttpUtil.getEncoding(request));
                if (command != null) {
                    LOG.info("Received: " + JacksonUtil.getObjectToJsonMapper().convert(command, "UTF-8"));
                    //TODO
                    TerminalCommandResult result = new TerminalCommandResult();
                    result.setId(command.getId());
                    result.setError("Test error");
                    PrintWriter out = response.getWriter();
                    out.println(JacksonUtil.getObjectToJsonMapper().convert(result, "UTF-8"));
                }
            } catch (Exception e) {
                LOG.error("Error: ", e);
            }
        }
    }
}
