package com.autocrop;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystems;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
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

  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response upload(@FormDataParam("fileselect") InputStream uploadInputStream,
                         @FormDataParam("fileselect") FormDataContentDisposition fileDetail) throws IOException {
    java.nio.file.Path outputPath = FileSystems.getDefault().getPath("/tmp", "blah.jpg");
    Files.copy(uploadInputStream, outputPath);

    return Response.ok().build();
  }
}
