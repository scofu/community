package com.scofu.community.bukkit.permission;

import com.scofu.community.Grant;
import com.scofu.community.RankRepository;
import com.scofu.community.Session;
import com.scofu.community.User;
import com.scofu.community.UserRepository;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;

final class UserPermissibleBase extends PermissibleBase {

  private final String id;
  private final UserRepository userRepository;
  private final RankRepository rankRepository;
  private final WildcardPermissionChecker wildcardPermissionChecker;

  public UserPermissibleBase(ServerOperator opable, String id, UserRepository userRepository,
      RankRepository rankRepository, WildcardPermissionChecker wildcardPermissionChecker) {
    super(opable);
    this.id = id;
    this.userRepository = userRepository;
    this.rankRepository = rankRepository;
    this.wildcardPermissionChecker = wildcardPermissionChecker;
  }

  @Override
  public boolean hasPermission(String permission) {
    return userRepository.byId(id)
        .flatMap(User::session)
        .flatMap(Session::grant)
        .map(Grant::rankId)
        .flatMap(rankRepository::byId)
        .flatMap(rank -> wildcardPermissionChecker.check(rank, permission))
        .orElse(false);
  }

  @Override
  public boolean hasPermission(Permission perm) {
    return hasPermission(perm.getName());
  }
}
