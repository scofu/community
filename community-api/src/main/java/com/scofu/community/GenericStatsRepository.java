package com.scofu.community;

import com.google.common.cache.CacheBuilder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.scofu.common.json.Json;
import com.scofu.network.document.AbstractDocumentRepository;
import com.scofu.network.document.RepositoryConfiguration;
import com.scofu.network.message.MessageFlow;
import com.scofu.network.message.MessageQueue;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

/** Generic stats repository. */
public class GenericStatsRepository extends AbstractDocumentRepository<GenericStats> {

  @Inject
  GenericStatsRepository(MessageQueue messageQueue, MessageFlow messageFlow, Json json) {
    super(
        messageQueue,
        messageFlow,
        GenericStats.class,
        json,
        RepositoryConfiguration.builder()
            .withCollection("scofu.generic_stats")
            .withCacheBuilder(CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES))
            .build());
  }
}
