package com.scofu.community.bukkit.api;

import java.util.UUID;

/**
 * Staff chat participant message.
 *
 * @param senderId the sender id
 * @param message  the message
 */
public record StaffChatParticipantMessage(UUID senderId, String message) {}
