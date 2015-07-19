package com.autocrop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

@Path("/auto-crop")
public class AutoCropResource {

  private final static Logger LOGGER = LoggerFactory.getLogger(AutoCropResource.class);

  private final Cache<String, Integer> _liveRequests;
  private final RemovalListener<String, Integer> _removalListener;

  AutoCropResource() {
    _removalListener = new RequestRemoverListener();
    _liveRequests = CacheBuilder.newBuilder()
        .maximumSize(200)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .removalListener(_removalListener)
        .build();
  }

  @GET
  @Path("/fetch/{requestId}/{index}")
  @Produces({"image/jpeg"})
  public Response streamingFetch(@PathParam("requestId") final String requestId,
                                 @PathParam("index") final int index) {
    // check if the request is valid against our book keeping
    Integer numCropped = _liveRequests.getIfPresent(requestId);
    if (numCropped == null || index >= numCropped) {
      try {
	      Thread.sleep(1000L); // add a sleep to prevent brute-forcing
      } catch(InterruptedException ex) {
        // ignore
      }
      return Response.status(Status.BAD_REQUEST).build();
    }

    StreamingOutput streamOutput = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException {
        try {
          String jpegFilePath = String.format("/tmp/auto-crop/%s/%03d.jpg", requestId, index);
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
      String requestId = uuid.toString().replaceAll("-", "");
      String pathStr = getRequestPathStr(requestId);
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
        int numCropped = Integer.parseInt(output);
        String json = String.format("{\"requestId\": \"%s\", \"count\": %d}", requestId, numCropped);
        // add to book keeping
        _liveRequests.put(requestId, numCropped);
        return Response.ok(json).build();
      } else {
        String json = String.format("{\"code\": %d, \"message\": \"%s\"}",
            Status.BAD_REQUEST.getStatusCode(), output);
        return Response.status(Status.BAD_REQUEST).entity(json).build();
      }
    } catch (Exception ex) {
      String json = String.format("{\"code\": %d, \"message\": \"unexpected error, please try again in a few minutes\"",
          Status.INTERNAL_SERVER_ERROR.getStatusCode());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(json).build();
    } finally {
      if (br != null) {
        br.close();
        uploadInputStream.close();
      }
    }
  }

  private static String getRequestPathStr(String requestId) {
    return String.format("/tmp/auto-crop/%s", requestId);
  }

  private static final class RequestRemoverListener implements RemovalListener<String, Integer> {

    private final List<String> _pendingRemovals;
    private final ScheduledExecutorService _executorService;

    private RequestRemoverListener() {
      _pendingRemovals = Collections.synchronizedList(new LinkedList<String>());
      Runnable requestRemover = new RequestRemover(_pendingRemovals);
      _executorService = Executors.newScheduledThreadPool(1);
      _executorService.scheduleWithFixedDelay(requestRemover, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void onRemoval(RemovalNotification<String, Integer> notification) {
      _pendingRemovals.add(notification.getKey());
    }
  }

  private static final class RequestRemover implements Runnable {

    private final List<String> _pendingRemovals;

    private RequestRemover(List<String> pendingRemovals) {
      _pendingRemovals = pendingRemovals;
    }

    @Override
    public void run() {
      int count = _pendingRemovals.size();

      if (count == 0) {
        return;
      }

      for (int i = 0; i < count; i++) {
        String requestId = _pendingRemovals.remove(0);
        String pathStr = getRequestPathStr(requestId);

        try {
          removeRecursive(FileSystems.getDefault().getPath(pathStr));
          LOGGER.info(String.format("cleaned up %s", pathStr));
        } catch (IOException ex) {
          // add the notification back to process the next time
          _pendingRemovals.add(requestId);
          LOGGER.warn(String.format("cannot clean up %s", pathStr), ex);
        }
      }
    }

    private static void removeRecursive(java.nio.file.Path path) throws IOException {
      Files.walkFileTree(path, new SimpleFileVisitor<java.nio.file.Path>() {
        @Override
        public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(java.nio.file.Path file, IOException exc) throws IOException {
          // try to delete the file anyway, even if its attributes
          // could not be read, since delete-only access is
          // theoretically possible
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc) throws IOException {
          if (exc == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          } else {
            // directory iteration failed; propagate exception
            throw exc;
          }
        }
      });
    }
  }
}
