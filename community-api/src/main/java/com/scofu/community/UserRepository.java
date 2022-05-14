package com.scofu.community;

import com.google.common.cache.CacheBuilder;
import com.scofu.common.json.Json;
import com.scofu.network.document.AbstractDocumentRepository;
import com.scofu.network.document.RepositoryConfiguration;
import com.scofu.network.message.MessageFlow;
import com.scofu.network.message.MessageQueue;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

/** User repository. */
public class UserRepository extends AbstractDocumentRepository<User> {

  private final RankRepository rankRepository;

  @Inject
  UserRepository(
      MessageQueue messageQueue,
      MessageFlow messageFlow,
      Json json,
      RankRepository rankRepository) {
    super(
        messageQueue,
        messageFlow,
        User.class,
        json,
        RepositoryConfiguration.builder()
            .withCollection("scofu.users")
            .withCacheBuilder(CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES))
            .build());
    this.rankRepository = rankRepository;
  }

  /**
   * Resolves and returns whether the given user has the given permission or not.
   *
   * @param user the user
   * @param permission the permission
   */
  public Optional<Boolean> resolvePermission(User user, String permission) {
    return user.session()
        .flatMap(Session::grant)
        .map(Grant::rankId)
        .flatMap(rankRepository::byId)
        .flatMap(rank -> rankRepository.resolvePermissionWithInheritance(rank, permission));
  }
}
