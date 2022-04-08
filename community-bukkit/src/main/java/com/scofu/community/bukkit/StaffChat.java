package com.scofu.community.bukkit;

import static com.scofu.text.Tags.tag;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.TextColor.fromHexString;

import com.scofu.chat.AbstractChat;
import com.scofu.chat.ChatRenderer;
import com.scofu.chat.Participant;
import com.scofu.chat.PrefixBasedChatResolver;
import com.scofu.community.bukkit.api.StaffChatMessage;
import com.scofu.network.message.MessageFlow;
import com.scofu.network.message.MessageQueue;
import com.scofu.network.message.QueueBuilder;
import javax.inject.Inject;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Staff chat.
 */
public class StaffChat extends AbstractChat {

  private final QueueBuilder<StaffChatMessage, Void> queue;
  private final Server server;

  @Inject
  StaffChat(MessageQueue messageQueue, MessageFlow messageFlow, Server server) {
    super(translatable("Staff"));
    this.queue = messageQueue.declareFor(StaffChatMessage.class)
        .withTopic("scofu.platform.staffchat");
    this.server = server;
    messageFlow.subscribeTo(StaffChatMessage.class)
        .withTopic("scofu.platform.staffchat")
        .via(this::onStaffChatMessage);
    map(PrefixBasedChatResolver.PREFIX_IDENTIFIER).to("@");
    map(ChatRenderer.CHAT_RENDERER_IDENTIFIER).to(new Renderer());
  }

  @Override
  public void sendMessage(final @Nullable Identified source, final @NotNull Component message,
      final @NotNull MessageType type) {
    if (source == null) {
      super.sendMessage(Identity.nil(), message, type);
      return;
    }
    queue.push(new StaffChatMessage(message));
  }

  @Override
  public void sendMessage(final @Nullable Identity source, final @NotNull Component message,
      final @NotNull MessageType type) {
    if (source == null) {
      super.sendMessage(Identity.nil(), message, type);
      return;
    }
    queue.push(new StaffChatMessage(message));
  }

  private void onStaffChatMessage(StaffChatMessage message) {
    sendMessage((Identified) null, message.component(), MessageType.CHAT);
  }

  final class Renderer implements ChatRenderer {

    public static final Component PREFIX = tag("staff", NamedTextColor.WHITE,
        fromHexString("#832bff"), false, 2);

    @Override
    public Component render(Participant participant, Component component) {
      final var player = server.getPlayer(participant.identity().uuid());
      if (player == null) {
        return component;
      }
      return translatable("%s %s", PREFIX,
          player.displayName().append(text(": ").append(component)));
    }
  }

}
