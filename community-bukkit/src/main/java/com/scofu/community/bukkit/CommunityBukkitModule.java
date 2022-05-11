package com.scofu.community.bukkit;

import com.google.inject.Scopes;
import com.google.inject.multibindings.OptionalBinder;
import com.scofu.chat.Chat;
import com.scofu.common.inject.AbstractFeatureModule;
import com.scofu.common.inject.annotation.Module;
import com.scofu.community.bukkit.permission.PermissionModule;
import com.scofu.text.BundledTranslationProvider;
import java.util.Locale;

/**
 * Community bukkit module.
 */
@Module
public class CommunityBukkitModule extends AbstractFeatureModule {

  @Override
  protected void configure() {
    install(new PermissionModule());
    bind(PlayerRenderer.class).in(Scopes.SINGLETON);
    bind(UserRenderer.class).in(Scopes.SINGLETON);
    bind(StaffChat.class).in(Scopes.SINGLETON);
    OptionalBinder.newOptionalBinder(binder(), Chat.class)
        .setBinding()
        .to(CommunityChat.class)
        .in(Scopes.SINGLETON);
    bindFeature(RankTransformer.class).in(Scopes.SINGLETON);
    bindFeature(RankDescriber.class).in(Scopes.SINGLETON);
    bindFeature(RankCommands.class).in(Scopes.SINGLETON);
    bindFeature(UserListener.class).in(Scopes.SINGLETON);
    bindFeature(GrantCommands.class).in(Scopes.SINGLETON);
    bindFeature(StatsCommands.class).in(Scopes.SINGLETON);
    bindFeature(TimeCommand.class).in(Scopes.SINGLETON);
    bindFeature(TimeListener.class).in(Scopes.SINGLETON);
    bindFeature(ThemeCommand.class).in(Scopes.SINGLETON);
    bindFeature(ThemeListener.class).in(Scopes.SINGLETON);
    bindFeature(DesignListener.class).in(Scopes.SINGLETON);
    bindFeature(GenericStatsListener.class).in(Scopes.SINGLETON);
    bindFeatureInstance(new BundledTranslationProvider(Locale.US, "community-bukkit_en_US",
        CommunityBukkitModule.class.getClassLoader()));
  }
}
