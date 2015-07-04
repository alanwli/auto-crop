package com.autocrop;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.assets.AssetsBundle;

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
  }

  @Override
  public void run(AutoCropConfiguration configuration, Environment environment) {
    final AutoCropResource resource = new AutoCropResource();
    environment.jersey().register(resource);
  }
}
