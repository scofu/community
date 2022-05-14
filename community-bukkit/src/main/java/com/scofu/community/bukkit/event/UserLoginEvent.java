package com.scofu.community.bukkit.event;

import com.scofu.community.User;
import java.net.InetAddress;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Called when a user logged in. */
public class UserLoginEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();
  private final Player player;
  private final String hostname;
  private final InetAddress address;
  private final InetAddress realAddress;
  private final User user;

  /**
   * Constructs a new user login event.
   *
   * @param player the player
   * @param hostname the hostname
   * @param address the address
   * @param realAddress the real address
   * @param user the user
   */
  public UserLoginEvent(
      Player player, String hostname, InetAddress address, InetAddress realAddress, User user) {
    this.player = player;
    this.hostname = hostname;
    this.address = address;
    this.realAddress = realAddress;
    this.user = user;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  /** Returns the player. */
  public Player player() {
    return player;
  }

  /** Returns the hostname. */
  public String hostname() {
    return hostname;
  }

  /** Returns the address. */
  public InetAddress address() {
    return address;
  }

  /** Returns the real address. */
  public InetAddress realAddress() {
    return realAddress;
  }

  /** Returns the user. */
  public User user() {
    return user;
  }
}
