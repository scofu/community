package com.scofu.community.bukkit.permission;

import com.google.inject.Scopes;
import com.scofu.common.inject.AbstractFeatureModule;

/**
 * Permission module.
 */
public class PermissionModule extends AbstractFeatureModule {

  @Override
  protected void configure() {
    bind(WildcardPermissionChecker.class).in(Scopes.SINGLETON);
    bindFeature(UserPermissionListener.class).in(Scopes.SINGLETON);
  }
}
