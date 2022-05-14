package com.scofu.community.bukkit.design;

import com.google.inject.Scopes;
import com.scofu.common.inject.AbstractFeatureModule;

/** Design module. */
public class DesignModule extends AbstractFeatureModule {

  @Override
  protected void configure() {
    bindFeature(DesignListener.class).in(Scopes.SINGLETON);
  }
}
