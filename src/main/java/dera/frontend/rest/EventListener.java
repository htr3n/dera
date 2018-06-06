package dera.frontend.rest;

import dera.core.EventActor;
import dera.core.EventType;
import dera.error.EventTypeExistedException;
import dera.error.EventTypeNotExistedException;
import dera.frontend.command.CommandResponse;
import dera.frontend.command.EventCommandRequest;
import dera.util.JacksonUtil;
import dera.runtime.EventActorManager;
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
import java.util.Set;

@Path("/events")
public class EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventListener.class);
    private final StandaloneExecutionDomain domain = StandaloneExecutionDomain.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAllEvents() {
        StringBuffer content = new StringBuffer();
        if (domain != null) {
            final EventActorManager actorManager = domain.getEventActorManager();
            if (actorManager != null) {
                final Set<EventType> eventTypes = actorManager.getWorkingEvents();
                if (eventTypes != null) {
                    try {
                        content.append(JacksonUtil.COLLECTION_TO_JSON_MAPPER.convert(eventTypes, Consts.UTF_8.name()));
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return Response.ok(content.toString()).header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length())).build();
    }

    @GET
    @Path("/nodirty")
    public Response resetDirtyFlagsOfAllEvents() {
        LOG.info("Reset the dirty flags of all events");
        if (domain != null) {
            final EventActorManager actorManager = domain.getEventActorManager();
            if (actorManager != null) {
                actorManager.resetDirtyEvents();
            }
        }
        return Response.ok().header(HttpHeaders.CONTENT_LENGTH, 0).build();
    }

    @GET
    @Path("/{eventId}/consumers")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getConsumers(@PathParam("eventId") String eventId) {
        StringBuffer content = new StringBuffer();
        if (domain != null) {
            final EventActorManager actorManager = domain.getEventActorManager();
            if (actorManager != null) {
                final Set<EventActor> consumers = actorManager.getConsumers(eventId);
                try {
                    content.append(JacksonUtil.COLLECTION_TO_JSON_MAPPER.convert(consumers, Consts.UTF_8.name()));
                } catch (IOException ignored) {
                }
            }
        }
        return Response.ok(content.toString()).header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length())).build();
    }

    @GET
    @Path("/{eventId}/producers")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getProducers(@PathParam("eventId") String eventId) {
        StringBuffer content = new StringBuffer();
        if (domain != null) {
            final EventActorManager actorManager = domain.getEventActorManager();
            if (actorManager != null) {
                final Set<EventActor> producers = actorManager.getProducers(eventId);
                try {
                    content.append(JacksonUtil.COLLECTION_TO_JSON_MAPPER.convert(producers, Consts.UTF_8.name()));
                } catch (IOException ignored) {
                }
            }
        }
        return Response
                .ok(content.toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length())).build();
    }

    @POST
    @Path("/cmd")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response handleEventCommand(@Context HttpServletRequest request) {
        CommandResponse response = new CommandResponse();
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
        }
        if (inputStream == null) {
            response.setCode(CommandResponse.FAILED);
            response.setMessage("Unable to retrieve the content of the request");
        } else {
            EventCommandRequest eventCommand = null;
            try {
                eventCommand = (EventCommandRequest) JacksonUtil.OBJECT_FROM_JSON_MAPPER.convert(EventCommandRequest.class, inputStream, HttpUtil.getEncoding(request));
            } catch (Exception e) {
                response.setCode(CommandResponse.FAILED);
                response.setMessage("Unable to convert the request content to an event: " + e.getMessage());
                LOG.warn("Converting error: ", e);
            }
            if (eventCommand != null) {
                try {
                    eventCommand.execute();
                    response.setCode(CommandResponse.SUCCEEDED);
                    switch (eventCommand.getCmd()) {
                        case add:
                            response.setMessage("Event " + eventCommand.getElementId() + " added");
                            break;
                        case remove:
                            response.setMessage("Event " + eventCommand.getElementId() + " deleted");
                            break;
                        default:
                            break;
                    }
                } catch (EventTypeNotExistedException e) {
                    response.setCode(CommandResponse.FAILED);
                    response.setMessage("Unable to " + eventCommand.getCmd() + " event. Error: Event type '" + eventCommand.getElementId() + "' does not exist.");
                } catch (EventTypeExistedException e) {
                    response.setCode(CommandResponse.FAILED);
                    response.setMessage("Unable to " + eventCommand.getCmd() + " event. Error: Event type '" + eventCommand.getElementId() + "' exists.");
                }
            } else {
                response.setCode(CommandResponse.FAILED);
                response.setMessage("Unable to convert the request content to an event");
            }
        }
        String content = JacksonUtil.convert(response);
        return Response
                .ok(content)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response postJSON(@Context HttpServletRequest request) {
        CommandResponse response = new CommandResponse();
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
        }
        if (inputStream == null) {
            response.setCode(CommandResponse.FAILED);
            response.setMessage("Unable to retrieve the content of the request");
        } else {
            EventType eventType = null;
            try {
                eventType = (EventType) JacksonUtil.OBJECT_FROM_JSON_MAPPER.convert(EventType.class, inputStream, HttpUtil.getEncoding(request));
                domain.addEvent(eventType);
                response.setCode(CommandResponse.SUCCEEDED);
                response.setMessage("Event '" + eventType.getType() + "' added");
            } catch (IOException e) {
                response.setCode(CommandResponse.FAILED);
                response.setMessage("Unable to convert the request content to an event. Error: " + e.getMessage());
            } catch (EventTypeExistedException e) {
                response.setCode(CommandResponse.FAILED);
                response.setMessage("Unable to add event. Error: Event type '" + eventType.getType() + "' exists.");
            }
        }
        String content = JacksonUtil.convert(response);
        return Response
                .ok(content)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }
}

