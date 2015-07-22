package com.autocrop;

import io.dropwizard.views.View;

public final class PhotosView extends View {
  private final String _requestId;
  private final int _numPhotos;

  public PhotosView(String requestId, int numPhotos) {
    super("photos.ftl");
    _requestId = requestId;
    _numPhotos = numPhotos;
  }
  
  public String getRequestId() {
    return _requestId;
  }

  public int getNumPhotos() {
    return _numPhotos;
  }
}
