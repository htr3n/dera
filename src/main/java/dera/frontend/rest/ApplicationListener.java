package dera.frontend.rest;

import dera.runtime.Application;
import dera.runtime.ApplicationInstance;
import dera.frontend.command.CommandResponse;
import dera.util.JacksonUtil;
import dera.runtime.ApplicationManager;
import dera.runtime.StandaloneExecutionDomain;
import dera.util.HttpUtil;
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
import java.util.Iterator;
import java.util.Set;

@Path("/applications")
public class ApplicationListener {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationListener.class);
    private final StandaloneExecutionDomain domain = StandaloneExecutionDomain.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getApplications(){
        StringBuffer content = new StringBuffer();
        if (domain != null) {
            final ApplicationManager manager = domain.getApplicationManager();
            if (manager != null) {
                final Set<Application> apps = manager.getApps();
                if (apps != null) {
                    try {
                        content.append(JacksonUtil.getCollectionToJsonMapper().convert(apps, "UTF-8"));
                    } catch (IOException e) {
                    }
                }
            }
        }
        return Response
                .ok(content.toString())
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @GET
    @Path("/nodirty")
    public Response resetDirtyApplication(){
        LOG.info("Reset the dirty flags of all applications");
        if (domain != null) {
            final ApplicationManager manager = domain.getApplicationManager();
            if (manager != null) {
                //manager.resetDirtyApps();
            }
        }
        return Response
                .ok()
                .header(HttpHeaders.CONTENT_LENGTH, 0)
                .build();
    }


    @GET
    @Path("/{appId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getApplication(@PathParam("appId") String appId){
        StringBuffer content = new StringBuffer();
        if (domain != null) {
            final ApplicationManager manager = domain.getApplicationManager();
            if (manager != null) {
                final Application app = manager.getApplication(appId);
                if (app != null)
                    try {
                        content.append(JacksonUtil.getObjectToJsonMapper().convert(app, "UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        return Response
                .ok(content.toString())
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @GET
    @Path("/{appId}/instances")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getInstances(@PathParam("appId") String appId){
        final StringBuffer content = new StringBuffer();
        if (domain != null) {
            final ApplicationManager manager = domain.getApplicationManager();
            if (manager != null) {
                final Set<ApplicationInstance> instances = manager.getInstances(appId);
                if (instances != null){
                    try {
                        content.append(JacksonUtil.getCollectionToJsonMapper().convert(instances, "UTF-8"));
                    } catch (IOException e) {
                    }
                }
            }
        }
        return Response
                .ok(content.toString())
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @GET
    @Path("/{appId}/instances/{instanceId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getInstance(@PathParam("appId") String appId, @PathParam("instanceId") String instanceId){
        final StringBuffer content = new StringBuffer();
        if (domain != null) {
            final ApplicationManager manager = domain.getApplicationManager();
            if (manager != null) {
                final Set<ApplicationInstance> instances = manager.getInstances(appId);
                if (instances != null) {
                    final Iterator<ApplicationInstance> iterator = instances.iterator();
                    boolean found = false;
                    ApplicationInstance instance = null;
                    while (!found && iterator.hasNext()) {
                        instance = iterator.next();
                        found = instance.getId().equals(instanceId);
                    }
                    if (found){
                        try {
                            content.append(JacksonUtil.getObjectToJsonMapper().convert(instance, "UTF-8"));
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
        return Response
                .ok(content.toString())
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response addApplicationAsJson(@Context HttpServletRequest request){
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
                Application application = (Application) JacksonUtil.getObjectFromJsonMapper()
                        .convert(Application.class, request.getInputStream(), HttpUtil.getEncoding(request));
                if (application != null) {
                    status.setCode(CommandResponse.SUCCEEDED);
                    status.setMessage("Successfully convert the request content to an application");
                    handle(application, status);
                }
            } catch (Exception e) {
                status.setCode(CommandResponse.FAILED);
                status.setMessage("Unable to convert the request content to an application: " + e.getMessage());
                LOG.debug("Converting error: ", e);
            }
        }
        final String content = JacksonUtil.convert(status);
        return Response
                .ok(content)
                .header(HttpHeaders.CONTENT_LENGTH, Long.valueOf(content.length()))
                .build();
    }

    private void handle(final Application application, final CommandResponse status) {
        if (domain != null) {
            try {
                boolean ok = domain.getApplicationManager().addApplication(application);
                if (!ok) {
                    status.setCode(CommandResponse.FAILED);
                    status.setMessage("There exists an application with ID " + application.getId());
                }
            } catch (Exception e) {
                LOG.error("Error: ", e);
            }
        }
    }

}
