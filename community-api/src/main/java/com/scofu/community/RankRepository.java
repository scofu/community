package com.scofu.community;

import static com.scofu.network.document.Filter.matchesRegex;
import static com.scofu.network.document.Filter.where;
import static com.scofu.network.document.Filter.withOptions;

import com.google.common.cache.CacheBuilder;
import com.scofu.common.json.Json;
import com.scofu.network.document.AbstractDocumentRepository;
import com.scofu.network.document.Query;
import com.scofu.network.document.RepositoryConfiguration;
import com.scofu.network.message.MessageFlow;
import com.scofu.network.message.MessageQueue;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
   * Finds and returns an optional rank with the given name.
   *
   * @param name the name
   */
  public CompletableFuture<Optional<Rank>> findByName(String name) {
    return find(Query.builder()
        .filter(where("name", matchesRegex(name).and(withOptions("i"))))
        .limitTo(1)
        .build()).thenApply(ranks -> ranks.values().stream().findFirst());
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
          .orElse(Set.of())
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
