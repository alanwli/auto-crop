package com.autocrop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/auto-crop")
public class AutoCropResource {

  @GET
  @Path("/fetch/{id}/{index}")
  @Produces({"image/jpeg"})
  public Response streamingFetch(@PathParam("id") final String id,
                                 @PathParam("index") final int index) {
    StreamingOutput streamOutput = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException {
        try {
          // TODO validate the input
          // TODO check against internal book keeping
          String jpegFilePath = String.format("/tmp/auto-crop/%s/%03d.jpg", id, index);
	        java.nio.file.Path path = FileSystems.getDefault().getPath(jpegFilePath);
	        Files.copy(path, os);
	        os.flush();
        } finally {
          os.close();
        }
      }
    };
    return Response.ok(streamOutput).build();
  }

  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response upload(@FormDataParam("fileselect") InputStream uploadInputStream,
                         @FormDataParam("fileselect") FormDataContentDisposition fileDetail) throws IOException {
    BufferedReader br = null;
    try {
      UUID uuid = UUID.randomUUID();
      String pathStr = String.format("/tmp/auto-crop/%s", uuid.toString().replaceAll("-", ""));
      Files.createDirectory(FileSystems.getDefault().getPath(pathStr));
      String uploadFileName = "upload"; // name that we're storing on disk
      java.nio.file.Path outputPath = FileSystems.getDefault().getPath(pathStr, uploadFileName);
      Files.copy(uploadInputStream, outputPath);

      ProcessBuilder pb = new ProcessBuilder("/home/alan/dev/auto-crop/script/auto-crop", "-d", "50", uploadFileName, "blah.jpg");
      pb.directory(FileSystems.getDefault().getPath(pathStr).toFile());
      Process process = pb.start();
      int exitCode = process.waitFor();
      InputStream is = (exitCode == 0) ? process.getInputStream() : process.getErrorStream();
      br = new BufferedReader(new InputStreamReader(is));
      String output = br.readLine();
      br.close();
      if (exitCode == 0) {
        String json = String.format("{\"requestId\": \"%s\", \"count\": %d}", uuid.toString(), Integer.parseInt(output));
        // TODO add to internal book keeping
        return Response.ok(json).build();
      } else {
        String json = String.format("{\"error\": \"%s\"}", output);
        return Response.status(Status.BAD_REQUEST).entity(json).build();
      }
    } catch (Exception ex) {
      String json = String.format("{\"error\": \"unexpected error, please try again in a few minutes\"");
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(json).build();
    } finally {
      if (br != null) {
        br.close();
      }

      uploadInputStream.close();
    }
  }
}
