package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.chat.AbstractChat;
import com.scofu.chat.Participant;
import com.scofu.chat.PrefixBasedChatResolver;
import com.scofu.community.bukkit.api.StaffChatParticipantMessage;
import com.scofu.community.bukkit.api.StaffChatRawMessage;
import com.scofu.network.message.MessageFlow;
import com.scofu.network.message.MessageQueue;
import com.scofu.network.message.QueueBuilder;
import com.scofu.text.Color;
import com.scofu.text.RendererRegistry;
import com.scofu.text.ThemeRegistry;
import com.scofu.text.json.Tag;
import com.scofu.text.json.TagFactory;
import java.util.UUID;
import javax.inject.Inject;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

/** Staff chat. */
public class StaffChat extends AbstractChat {

  private final QueueBuilder<StaffChatParticipantMessage, Void> participantQueue;
  private final QueueBuilder<StaffChatRawMessage, Void> rawQueue;
  private final RendererRegistry rendererRegistry;
  private final Tag tag;

  @Inject
  StaffChat(
      MessageQueue messageQueue,
      MessageFlow messageFlow,
      ThemeRegistry themeRegistry,
      RendererRegistry rendererRegistry,
      TagFactory tagFactory) {
    super(translatable("Staff"), themeRegistry);
    this.participantQueue =
        messageQueue
            .declareFor(StaffChatParticipantMessage.class)
            .withTopic("scofu.platform.staffchat");
    this.rawQueue =
        messageQueue.declareFor(StaffChatRawMessage.class).withTopic("scofu.platform.staffchat");
    this.rendererRegistry = rendererRegistry;
    messageFlow
        .subscribeTo(StaffChatParticipantMessage.class)
        .withTopic("scofu.platform.staffchat")
        .via(this::onStaffChatParticipantMessage);
    messageFlow
        .subscribeTo(StaffChatRawMessage.class)
        .withTopic("scofu.platform.staffchat")
        .via(this::onStaffChatRawMessage);
    map(PrefixBasedChatResolver.PREFIX_IDENTIFIER).to("@");
    tag = tagFactory.create("staff", Color.BRIGHT_WHITE, Color.PURPLE);
  }

  @Override
  public void sendParticipantMessage(Participant participant, String message) {
    participantQueue.push(new StaffChatParticipantMessage(participant.identity().uuid(), message));
  }

  @Override
  public void sendRawMessage(Component message) {
    rawQueue.push(new StaffChatRawMessage(message));
  }

  private void onStaffChatParticipantMessage(StaffChatParticipantMessage message) {
    sendThemedMessage(
        Identity.identity(message.senderId()),
        MessageType.CHAT,
        theme -> {
          final var user =
              rendererRegistry
                  .render(theme, UUID.class, message.senderId())
                  .orElse(Component.empty());
          return translatable(
              "%s %s",
              tag.render(theme).orElse(Component.empty()),
              user.append(text(": ").append(text(message.message()).color(theme.white()))));
        });
  }

  private void onStaffChatRawMessage(StaffChatRawMessage message) {
    sendThemedMessage(
        Identity.nil(),
        MessageType.SYSTEM,
        theme ->
            translatable(
                "%s %s",
                tag.render(theme).orElse(Component.empty()),
                message.component().color(theme.white())));
  }
}
