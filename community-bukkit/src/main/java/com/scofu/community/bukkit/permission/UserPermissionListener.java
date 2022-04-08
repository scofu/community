package com.scofu.community.bukkit.permission;

import com.scofu.common.inject.Feature;
import com.scofu.community.RankRepository;
import com.scofu.community.UserRepository;
import com.scofu.community.bukkit.event.UserLoginEvent;
import java.lang.reflect.Field;
import javax.inject.Inject;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftHumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

final class UserPermissionListener implements Feature, Listener {

  private static final Field FIELD;


  static {
    try {
      FIELD = CraftHumanEntity.class.getDeclaredField("perm");
      FIELD.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  private final UserRepository userRepository;
  private final RankRepository rankRepository;
  private final WildcardPermissionChecker wildcardPermissionChecker;

  @Inject
  UserPermissionListener(UserRepository userRepository, RankRepository rankRepository,
      WildcardPermissionChecker wildcardPermissionChecker) {
    this.userRepository = userRepository;
    this.rankRepository = rankRepository;
    this.wildcardPermissionChecker = wildcardPermissionChecker;
  }

  @EventHandler
  private void onUserLoginEvent(UserLoginEvent event) {
    try {
      FIELD.set(event.player(),
          new UserPermissibleBase(event.player(), event.user().id(), userRepository, rankRepository,
              wildcardPermissionChecker));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

}
