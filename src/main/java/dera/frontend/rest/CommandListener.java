package dera.frontend.rest;

import dera.core.Action;
import dera.core.Barrier;
import dera.core.Condition;
import dera.core.EventType;
import dera.frontend.command.CommandResponse;
import dera.frontend.command.DeraCommandRequest;
import dera.runtime.Application;
import dera.util.JacksonUtil;
import dera.runtime.StandaloneExecutionDomain;
import dera.util.HttpUtil;
import org.apache.http.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@Path("/cmd")
public class CommandListener {

    private static final Logger LOG = LoggerFactory.getLogger(CommandListener.class);
    private final StandaloneExecutionDomain domain = StandaloneExecutionDomain.getInstance();

    @GET
    public Response get(@Context HttpServletRequest request) {
        return Response
                .ok()
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response postAsJson(@Context HttpServletRequest request) {
        final CommandResponse status = new CommandResponse();
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
        }
        if (inputStream == null) {
            status.setCode(CommandResponse.FAILED);
            status.setMessage("Unable to retrieve the content of the request");
        } else {
            try {
                final DeraCommandRequest command = (DeraCommandRequest) JacksonUtil.getObjectFromJsonMapper().convert(DeraCommandRequest.class, request.getInputStream(), HttpUtil.getEncoding(request));
                if (command != null) {
                    handleCommand(command, status);
                } else {
                    status.setCode(CommandResponse.FAILED);
                    status.setMessage("Unable to convert the request to a valid command");
                }
            } catch (Exception e) {
                status.setCode(CommandResponse.FAILED);
                status.setMessage("Error: " + e.getMessage());
                LOG.debug("Error: ", e);
            }
        }
        String content = JacksonUtil.convert(status);
        return Response
                .ok(content)
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    private void handleCommand(final DeraCommandRequest cmd, final CommandResponse status) {
        try {
            LOG.info("Received command: " + JacksonUtil.getObjectToJsonMapper().convert(cmd, Consts.UTF_8.name()));
        } catch (Exception e) {
            LOG.error("Unable to deserialize the incoming JSON object", e);
        }
        if (cmd == null) {
            return;
        }
        switch (cmd.getCmd()) {
            case add:
                final String type = cmd.getElementType();
                if (EventType.class.getSimpleName().equals(type)) {
                    try {
                        domain.addEvent(cmd.getElementId());
                        status.setCode(CommandResponse.SUCCEEDED);
                        status.setMessage(cmd.getElementType() + " '" + cmd.getElementId() + "' added.");
                        LOG.info("Add new {} '{}'", new Object[]{EventType.class.getSimpleName(), cmd.getElementId()});
                    } catch (Exception e) {
                        status.setCode(CommandResponse.FAILED);
                        status.setMessage(e.getMessage());
                    }
                } else if (Action.class.getSimpleName().equals(type)) {
                    LOG.info("Add new {} '{}'", new Object[]{Action.class.getSimpleName(), cmd.getElementId()});

                } else if (Barrier.class.getSimpleName().equals(type)) {
                    LOG.info("Add new {} '{}'", new Object[]{Barrier.class.getSimpleName(), cmd.getElementId()});

                } else if (Condition.class.getSimpleName().equals(type)) {
                    LOG.info("Add new {} '{}'", new Object[]{Condition.class.getSimpleName(), cmd.getElementId()});

                } else if (Application.class.getSimpleName().equals(type)) {
                    LOG.info("Add new {} '{}'", new Object[]{Application.class.getSimpleName(), cmd.getElementId()});
                }
                break;
            case remove:
                break;
            case enable:
                break;
            case disable:
                break;
            default:
                LOG.info("The command of type '{}' is not handled at the moment", new Object[]{cmd.getCmd()});
                break;
        }
    }

}
