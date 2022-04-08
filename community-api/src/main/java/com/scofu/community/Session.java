package com.scofu.community;

import com.jsoniter.annotation.JsonCreator;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * A session.
 */
public class Session {

  private final Instant startedAt;
  private Instant stoppedAt;
  private String networkId;
  private String instanceId;
  private Grant grant;

  /**
   * Constructs a new session.
   *
   * @param startedAt the started at
   */
  @JsonCreator
  public Session(Instant startedAt) {
    this.startedAt = startedAt;
  }

  /**
   * Returns the playtime.
   */
  public Duration playtime() {
    return Duration.between(startedAt, stoppedAt().orElseGet(Instant::now));
  }

  /**
   * Returns whether this is active or not.
   */
  public boolean isActive() {
    return stoppedAt != null;
  }

  /**
   * Returns the started at.
   */
  public Instant startedAt() {
    return startedAt;
  }

  /**
   * Returns the optional stopped at.
   */
  public Optional<Instant> stoppedAt() {
    return Optional.ofNullable(stoppedAt);
  }

  /**
   * Sets the stopped at.
   *
   * @param stoppedAt the stopped at
   */
  public void setStoppedAt(Instant stoppedAt) {
    this.stoppedAt = stoppedAt;
  }

  /**
   * Returns the optional network id.
   */
  public Optional<String> networkId() {
    return Optional.ofNullable(networkId);
  }

  /**
   * Sets the network id.
   *
   * @param networkId the network id
   */
  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  /**
   * Returns the optional instance id.
   */
  public Optional<String> instanceId() {
    return Optional.ofNullable(instanceId);
  }

  /**
   * Sets the instance id.
   *
   * @param instanceId the instance id
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Returns the optional grant.
   */
  public Optional<Grant> grant() {
    return Optional.ofNullable(grant);
  }

  /**
   * Sets the grant.
   *
   * @param grant the grant
   */
  public void setGrant(Grant grant) {
    this.grant = grant;
  }
}
