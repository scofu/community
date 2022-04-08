package com.scofu.community.service;

import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.scofu.app.Service;
import com.scofu.app.bootstrap.BootstrapModule;

/**
 * Community service.
 */
public class CommunityService extends Service {

  public static void main(String[] args) {
    load(Stage.PRODUCTION, new CommunityService());
  }

  @Override
  protected void configure() {
    install(new BootstrapModule(getClass().getClassLoader()));
    bindFeature(SessionController.class).in(Scopes.SINGLETON);
  }
}
