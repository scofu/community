package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Inject;
import com.scofu.chat.AbstractChat;
import com.scofu.chat.Participant;
import com.scofu.chat.PrefixBasedChatResolver;
import com.scofu.text.Color;
import com.scofu.text.RendererRegistry;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/** The default chat. */
public class CommunityChat extends AbstractChat {

  private final Server server;
  private final RendererRegistry rendererRegistry;

  @Inject
  CommunityChat(Server server, RendererRegistry rendererRegistry) {
    super(translatable("chat.default.name"));
    this.server = server;
    this.rendererRegistry = rendererRegistry;
    map(PrefixBasedChatResolver.PREFIX_IDENTIFIER).to("!");
  }

  @Override
  public void sendParticipantMessage(Participant participant, String message) {
    final var player = server.getPlayer(participant.identity().uuid());
    if (player == null) {
      sendRawMessage(translatable("unknown: %s", text(message)));
      return;
    }
    final var renderedMessage =
        rendererRegistry
            .render(Player.class, player)
            .orElseGet(Component::empty)
            .append(text(": ").append(text(message)).color(Color.WHITE));
    sendMessage(participant.identity(), renderedMessage, MessageType.CHAT);
  }
}
