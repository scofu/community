package com.scofu.community;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.scofu.network.document.Filter.equalsTo;
import static com.scofu.network.document.Filter.where;
import static com.scofu.network.document.Query.query;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.scofu.common.json.Json;
import com.scofu.network.document.AbstractDocumentRepository;
import com.scofu.network.document.RepositoryConfiguration;
import com.scofu.network.message.MessageFlow;
import com.scofu.network.message.MessageQueue;
import com.scofu.network.message.Result;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.inject.Inject;

/** Grant repository. */
public class GrantRepository extends AbstractDocumentRepository<Grant> {

  private final Comparator<Grant> comparator;

  @Inject
  GrantRepository(
      MessageQueue messageQueue, MessageFlow messageFlow, Json json, Comparator<Grant> comparator) {
    super(
        messageQueue,
        messageFlow,
        Grant.class,
        json,
        RepositoryConfiguration.builder()
            .withCollection("scofu.grants")
            .withCacheBuilder(CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES))
            .build());
    this.comparator = comparator;
  }

  /**
   * Returns a stream of active grants with the given rank id.
   *
   * @param rankId the rank id
   */
  public Result<Stream<Grant>> byRankId(String rankId) {
    checkNotNull(rankId, "rankId");
    return find(query().filter(where("rankId", equalsTo(rankId))).build())
        .map(map -> map.values().stream())
        .apply(Stream::filter, () -> (Predicate<Grant>) Grant::isActive)
        .apply(Stream::sorted, () -> comparator);
  }

  /**
   * Returns a stream of active grants with the given user id.
   *
   * @param userId the user id
   */
  public Result<Stream<Grant>> byUserId(String userId) {
    checkNotNull(userId, "userId");
    return find(query().filter(where("userId", equalsTo(userId))).build())
        .map(map -> map.values().stream())
        .apply(Stream::filter, () -> (Predicate<Grant>) Grant::isActive)
        .apply(Stream::sorted, () -> comparator);
  }

  /**
   * Returns a stream of grants by the user id.
   *
   * @param userId the user id
   */
  public Result<Stream<Grant>> allByUserId(String userId) {
    checkNotNull(userId, "userId");
    return find(query().filter(where("userId", equalsTo(userId))).build())
        .map(map -> map.values().stream())
        .apply(Stream::sorted, () -> comparator);
  }
}
