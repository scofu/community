package com.scofu.community.bukkit.permission;

import static java.util.Optional.empty;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import com.scofu.network.document.DocumentStateListener;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

/**
 * Wildcard based permission checker.
 */
public class WildcardPermissionChecker implements PermissionChecker {

  private final LoadingCache<Key, Optional<Boolean>> cache;
  private final RankRepository rankRepository;

  @Inject
  WildcardPermissionChecker(RankRepository rankRepository) {
    this.rankRepository = rankRepository;
    this.cache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build(new CacheLoader<>() {
          // scofu.command.foo.bar

          // scofu | command.foo.bar
          // > scofu.*

          // scofu.command | foo.bar
          // > scofu.command.*

          // scofu.command.atlas | bar
          // > scofu.command.foo.*

          @Override
          public Optional<Boolean> load(Key key) throws Exception {
            return rankRepository.resolvePermissionWithInheritance(key.rank, key.permission)
                .or(() -> checkWithStar(key.rank, "", key.permission));
          }
        });
    rankRepository.addStateListener(new DocumentStateListener<>() {
      @Override
      public void onUpdate(Rank rank, boolean cached) {
        System.out.println("INVALIDATING ALL - UPDATE");
        cache.invalidateAll();
        //TODO: only invalidate related ranks
      }

      @Override
      public void onDelete(String id) {
        System.out.println("INVALIDATING ALL - DELETE");
        cache.invalidateAll();
      }
    });
  }

  @Override
  public Optional<Boolean> check(Rank rank, String permission) {
    return cache.getUnchecked(new Key(rank, permission));
  }

  private Optional<Boolean> checkWithStar(Rank rank, String head, String tail) {
    return rankRepository.resolvePermissionWithInheritance(rank, head + "*").or(() -> {
      final var split = tail.split("\\.", 2);
      if (split.length == 1) {
        return empty();
      }
      return checkWithStar(rank, head + split[0] + ".", split[1]);
    });
  }

  private record Key(Rank rank, String permission) {

    @Override
    public boolean equals(Object o) {
      return o == this || o instanceof Key key && (Objects.equals(rank.id(), key.rank.id())
          && Objects.equals(permission, key.permission));
    }

    @Override
    public int hashCode() {
      return Objects.hash(rank.id(), permission);
    }
  }
}
