package dera.frontend.rest;

import dera.core.EventActor;
import dera.frontend.command.ActorCommandRequest;
import dera.frontend.command.CommandResponse;
import dera.runtime.EventActorManager;
import dera.runtime.StandaloneExecutionDomain;
import dera.util.HttpUtil;
import dera.util.JacksonUtil;
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
import java.util.Collections;
import java.util.Set;

@Path("/actors")
public class ActorListener {

    private static final Logger LOG = LoggerFactory.getLogger(ActorListener.class);
    private final StandaloneExecutionDomain domain = StandaloneExecutionDomain.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getActors() {
        StringBuffer content = new StringBuffer();
        if (domain != null) {
            final EventActorManager actorManager = domain.getEventActorManager();
            if (actorManager != null) {
                final Set<EventActor> actors = actorManager.getWorkingActors();
                if (actors != null) {
                    try {
                        content.append(JacksonUtil.getCollectionToJsonMapper().convert(actors, Consts.UTF_8.name()));
                    } catch (IOException e) {
                    }
                }
            }
        }
        return Response
                .ok(content.toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @GET
    @Path("/nodirty")
    public Response resetDirtyActors() {
        LOG.info("Reset the dirty flags of all actors");
        if (domain != null) {
            final EventActorManager actorManager = domain.getEventActorManager();
            if (actorManager != null) {
                actorManager.resetDirtyActors();
            }
        }
        return Response
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, 0)
                .build();
    }

    @GET
    @Path("/{actorId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getActorById(@PathParam("actorId") String actorId) {
        StringBuffer content = new StringBuffer();
        if (domain != null) {
            final EventActorManager actorManager = domain.getEventActorManager();
            if (actorManager != null) {
                final EventActor actor = actorManager.getActor(actorId);
                try {
                    if (actor != null) {
                        content.append(JacksonUtil.getObjectToJsonMapper().convert(actor, Consts.UTF_8.name()));
                    } else {
                        content.append(JacksonUtil.getObjectToJsonMapper().convert(new Object(), Consts.UTF_8.name()));
                    }
                } catch (IOException e) {
                }
            }
        }
        return Response
                .ok(content.toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @GET
    @Path("/{actorId}/input")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getActorInputs(@PathParam("actorId") String actorId) {
        StringBuffer content = new StringBuffer();
        if (domain != null) {
            final EventActorManager actorManager = domain.getEventActorManager();
            if (actorManager != null) {
                final EventActor actor = actorManager.getActor(actorId);
                try {
                    if (actor != null) {
                        content.append(JacksonUtil.getCollectionToJsonMapper().convert(actor.getInput(), Consts.UTF_8.name()));
                    } else {
                        content.append(JacksonUtil.getCollectionToJsonMapper().convert(Collections.EMPTY_SET, Consts.UTF_8.name()));
                    }
                } catch (IOException e) {
                }
            }
        }
        return Response
                .ok(content.toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @GET
    @Path("/{actorId}/output")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getActorOutputs(@PathParam("actorId") String actorId) {
        StringBuffer content = new StringBuffer();
        if (domain != null) {
            final EventActorManager actorManager = domain.getEventActorManager();
            if (actorManager != null) {
                final EventActor actor = actorManager.getActor(actorId);
                try {
                    if (actor != null) {
                        content.append(JacksonUtil.getCollectionToJsonMapper().convert(actor.getOutput(), Consts.UTF_8.name()));
                    } else {
                        content.append(JacksonUtil.getCollectionToJsonMapper().convert(Collections.EMPTY_SET, Consts.UTF_8.name()));
                    }
                } catch (IOException e) {
                }
            }
        }
        return Response
                .ok(content.toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response addActor(@Context HttpServletRequest request) {
        final CommandResponse response = new CommandResponse();
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
        }
        if (inputStream == null) {
            response.setCode(CommandResponse.FAILED);
            response.setMessage("Unable to retrieve the content of the request");
        } else {
            try {
                ActorCommandRequest command = (ActorCommandRequest) JacksonUtil.getObjectFromJsonMapper().convert(ActorCommandRequest.class, request.getInputStream(), HttpUtil.getEncoding(request));
                if (command != null) {
                    response.setCode(CommandResponse.SUCCEEDED);
                    response.setMessage("Successfully convert the request content to an actor command");
                }
            } catch (Exception e) {
                response.setCode(CommandResponse.FAILED);
                response.setMessage("Unable to convert the request content to an actor: " + e.getMessage());
                LOG.debug("Converting error: ", e);
            }
        }
        final String content = JacksonUtil.convert(response);
        return Response
                .ok(content)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @POST
    @Path("/cmd")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response handleActorCommand(@Context HttpServletRequest request) {
        final CommandResponse response = new CommandResponse();
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
        }
        if (inputStream == null) {
            response.setCode(CommandResponse.FAILED);
            response.setMessage("Unable to retrieve the content of the request");
        } else {
            try {
                ActorCommandRequest command = (ActorCommandRequest) JacksonUtil.getObjectFromJsonMapper().convert(ActorCommandRequest.class, inputStream, HttpUtil.getEncoding(request));
                if (command != null) {
                    command.execute();
                    response.setCode(CommandResponse.SUCCEEDED);
                    response.setMessage("Successfully convert the request content to an actor command");
                }
            } catch (Exception e) {
                response.setCode(CommandResponse.FAILED);
                response.setMessage("Unable to convert the request content to an actor: " + e.getMessage());
                LOG.debug("Converting error: ", e);
            }
        }
        final String content = JacksonUtil.convert(response);
        return Response
                .ok(content)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }
}