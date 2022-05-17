package com.scofu.community;

import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.scofu.common.inject.AbstractFeatureModule;
import com.scofu.common.inject.annotation.Module;
import java.util.Comparator;

/** Community module. */
@Module
public class CommunityModule extends AbstractFeatureModule {

  @Override
  protected void configure() {
    bind(RankRepository.class).in(Scopes.SINGLETON);
    bind(UserRepository.class).in(Scopes.SINGLETON);
    bind(GrantRepository.class).in(Scopes.SINGLETON);
    bind(GenericStatsRepository.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  Comparator<Grant> grantComparator(RankRepository rankRepository) {
    return Comparator.comparing(
        grant -> rankRepository.byId(grant.rankId()).orElseGet(Rank::empty));
  }
}
