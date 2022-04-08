package com.scofu.community;

import com.google.common.cache.CacheBuilder;
import com.scofu.common.json.Json;
import com.scofu.network.document.AbstractDocumentRepository;
import com.scofu.network.document.RepositoryConfiguration;
import com.scofu.network.message.MessageFlow;
import com.scofu.network.message.MessageQueue;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Rank repository.
 */
public class RankRepository extends AbstractDocumentRepository<Rank> {

  @Inject
  RankRepository(MessageQueue messageQueue, MessageFlow messageFlow, Json json) {
    super(messageQueue, messageFlow, Rank.class, json, RepositoryConfiguration.builder()
        .withCollection("scofu.ranks")
        .withCacheBuilder(CacheBuilder.newBuilder())
        .build());
  }

  /**
   * Resolves and returns whether the given rank has the given permission or not.
   *
   * @param rank       the rank
   * @param permission the permission
   */
  public Optional<Boolean> resolvePermissionWithInheritance(Rank rank, String permission) {
    return rank.resolvePermission(permission).or(() -> {
      if (rank.inheritance().isEmpty()) {
        return Optional.empty();
      }
      return rank.inheritance()
          .stream()
          .map(this::byId)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .sorted()
          .map(inheritedRank -> resolvePermissionWithInheritance(inheritedRank, permission))
          .filter(Optional::isPresent)
          .findFirst()
          .orElse(Optional.empty());
    });
  }
}
