package com.autocrop;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auto-crop")
public class AutoCropResource {

  @GET
  @Path("/fetch")
  //@Produces({"application/octet-stream"})
  @Produces(MediaType.TEXT_PLAIN)
  public Response streamingFetch() {
    return Response.ok("foo").build();
  }

  /*
  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response upload(@FormDataParam("file") InputStream uploadInputStream,
                         @FormDataParam("file") FormDataContentDisposition fileDetail) {
    java.nio.file.Path outputPath = FileSystems.getDefault().getPath("/tmp", "blah.jpg");
    Files.copy(uploadInputStream, outputPath);

    return Response.ok().build();
  }
  */
}
