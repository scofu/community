package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Inject;
import com.scofu.chat.AbstractChat;
import com.scofu.chat.Participant;
import com.scofu.chat.PrefixBasedChatResolver;
import com.scofu.text.ThemeRegistry;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;

/**
 * The default chat.
 */
public class CommunityChat extends AbstractChat {

  private final Server server;
  private final PlayerRenderer playerRenderer;

  @Inject
  CommunityChat(Server server, ThemeRegistry themeRegistry, PlayerRenderer playerRenderer) {
    super(translatable("chat.default.name"), themeRegistry);
    this.server = server;
    this.playerRenderer = playerRenderer;
    map(PrefixBasedChatResolver.PREFIX_IDENTIFIER).to("!");
  }

  @Override
  public void sendParticipantMessage(Participant participant, String message) {
    final var player = server.getPlayer(participant.identity().uuid());
    if (player == null) {
      sendRawMessage(translatable("unknown: %s", text(message)));
      return;
    }
    sendThemedMessage(participant.identity(), MessageType.CHAT, theme -> {
      final var playerComponent = playerRenderer.render(theme, player).orElse(Component.empty());
      return playerComponent.append(text(": ").append(text(message)).color(theme.white()));
    });
  }
}
