package com.autocrop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/auto-crop")
public class AutoCropResource {

  @GET
  @Path("/fetch")
  //@Produces({"application/octet-stream"})
  //@Produces(MediaType.TEXT_PLAIN)
  @Produces({"image/jpeg"})
  public Response streamingFetch() {
    StreamingOutput streamOutput = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException {
        // TODO
        java.nio.file.Path path = FileSystems.getDefault().getPath("/tmp/auto-crop/3b6ceb600f1540e68d2e83c2e1ede55c/0.jpg");
        Files.copy(path, os);
      }
    };
    return Response.ok(streamOutput).build();
  }

  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response upload(@FormDataParam("fileselect") InputStream uploadInputStream,
                         @FormDataParam("fileselect") FormDataContentDisposition fileDetail) throws IOException {
    UUID uuid = UUID.randomUUID();
    String pathStr = String.format("/tmp/auto-crop/%s", uuid.toString().replaceAll("-", ""));
    Files.createDirectory(FileSystems.getDefault().getPath(pathStr));
    java.nio.file.Path outputPath = FileSystems.getDefault().getPath(pathStr, fileDetail.getFileName());
    Files.copy(uploadInputStream, outputPath);

    return Response.ok(uuid.toString(), MediaType.TEXT_PLAIN).build();
  }
}
