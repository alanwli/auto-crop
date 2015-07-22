package com.autocrop;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class AutoCropApplication extends Application<AutoCropConfiguration> {
  public static void main(String[] args) throws Exception {
    new AutoCropApplication().run(args);
  }

  @Override
  public String getName() {
    return "auto-crop";
  }

  @Override
  public void initialize(Bootstrap<AutoCropConfiguration> bootstrap) {
    bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));
    bootstrap.addBundle(new ViewBundle<Configuration>());
  }

  @Override
  public void run(AutoCropConfiguration configuration, Environment environment) {
    final AutoCropResource resource = new AutoCropResource();
    environment.jersey().register(resource);
    environment.jersey().register(MultiPartFeature.class);
  }
}
