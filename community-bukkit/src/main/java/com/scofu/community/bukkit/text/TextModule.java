package com.scofu.community.bukkit.text;

import com.google.inject.Scopes;
import com.scofu.common.inject.AbstractFeatureModule;

/** Text module. */
public class TextModule extends AbstractFeatureModule {

  @Override
  protected void configure() {
    bindFeature(PlayerRenderer.class).in(Scopes.SINGLETON);
    bindFeature(UserRenderer.class).in(Scopes.SINGLETON);
    bindFeature(EntityRenderer.class).in(Scopes.SINGLETON);
  }
}
