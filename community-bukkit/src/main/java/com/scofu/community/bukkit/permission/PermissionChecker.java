package com.scofu.community.bukkit.permission;

import com.scofu.community.Rank;
import java.util.Optional;

/**
 * Checks permissions.
 */
public interface PermissionChecker {

  /**
   * Checks and returns whether the given rank has the given permission or not.
   *
   * @param rank       the rank
   * @param permission the permission
   */
  Optional<Boolean> check(Rank rank, String permission);

}
