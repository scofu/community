package com.scofu.community;

import static com.scofu.network.document.Filter.equalsTo;
import static com.scofu.network.document.Filter.where;

import com.google.common.cache.CacheBuilder;
import com.scofu.common.json.Json;
import com.scofu.network.document.AbstractDocumentRepository;
import com.scofu.network.document.Query;
import com.scofu.network.document.RepositoryConfiguration;
import com.scofu.network.message.MessageFlow;
import com.scofu.network.message.MessageQueue;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.inject.Inject;

/**
 * Grant repository.
 */
public class GrantRepository extends AbstractDocumentRepository<Grant> {

  private final RankRepository rankRepository;

  @Inject
  GrantRepository(MessageQueue messageQueue, MessageFlow messageFlow, Json json,
      RankRepository rankRepository) {
    super(messageQueue, messageFlow, Grant.class, json, RepositoryConfiguration.builder()
        .withCollection("scofu.grants")
        .withCacheBuilder(CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES))
        .build());
    this.rankRepository = rankRepository;
  }

  /**
   * Returns a stream of active grants with the given rank id.
   *
   * @param rankId the rank id
   */
  public CompletableFuture<Stream<Grant>> byRankId(String rankId) {
    return find(Query.builder().filter(where("rankId", equalsTo(rankId))).build()).thenApplyAsync(
        map -> map.values()
            .stream()
            .filter(Grant::isActive)
            .sorted(Comparator.comparing(
                grant -> rankRepository.byId(grant.rankId()).orElse(Rank.empty()))));
  }

  /**
   * Returns a stream of active grants with the given user id.
   *
   * @param userId the user id
   */
  public CompletableFuture<Stream<Grant>> byUserId(String userId) {
    return find(Query.builder().filter(where("userId", equalsTo(userId))).build()).thenApplyAsync(
        map -> map.values()
            .stream()
            .filter(Grant::isActive)
            .sorted(Comparator.comparing(
                grant -> rankRepository.byId(grant.rankId()).orElse(Rank.empty()))));
  }

  /**
   * Returns a stream of grants by the user id.
   *
   * @param userId the user id
   */
  public CompletableFuture<Stream<Grant>> allByUserId(String userId) {
    return find(Query.builder().filter(where("userId", equalsTo(userId))).build()).thenApplyAsync(
        map -> map.values()
            .stream()
            .sorted(Comparator.comparing(
                grant -> rankRepository.byId(grant.rankId()).orElse(Rank.empty()))));
  }
}
