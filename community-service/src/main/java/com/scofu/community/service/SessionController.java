package com.scofu.community.service;

import static com.scofu.network.document.Filter.equalsTo;
import static com.scofu.network.document.Filter.where;

import com.scofu.common.inject.Feature;
import com.scofu.community.Grant;
import com.scofu.community.GrantRepository;
import com.scofu.community.UserRepository;
import com.scofu.network.document.DocumentStateListener;
import com.scofu.network.document.Query;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

final class SessionController implements Feature, DocumentStateListener<Grant> {

  private final UserRepository userRepository;
  private final GrantRepository grantRepository;

  @Inject
  SessionController(UserRepository userRepository, GrantRepository grantRepository) {
    this.userRepository = userRepository;
    this.grantRepository = grantRepository;
    System.out.println("adding state listener");
  }

  @Override
  public void onUpdate(Grant grant, boolean cached) {
    userRepository
        .byIdAsync(grant.userId())
        .thenAcceptAsync(
            optionalUser ->
                optionalUser.ifPresent(
                    user ->
                        user.session()
                            .ifPresent(
                                session ->
                                    grantRepository
                                        .byUserId(user.id())
                                        .thenAcceptAsync(
                                            grants -> {
                                              final var bestGrant = grants.findFirst().orElse(null);
                                              session.setGrant(bestGrant);
                                              userRepository.update(user);
                                            }))))
        .orTimeout(10, TimeUnit.SECONDS)
        .whenCompleteAsync(
            (x, error) -> {
              if (error != null) {
                error.printStackTrace();
              }
            });
  }

  @Override
  public void onDelete(String id) {
    userRepository
        .find(Query.builder().filter(where("session.grant._id", equalsTo(id))).limitTo(1).build())
        .thenAccept(
            map ->
                map.values().stream()
                    .findFirst()
                    .ifPresent(
                        user ->
                            user.session()
                                .ifPresent(
                                    session -> {
                                      final var bestGrant =
                                          grantRepository
                                              .byUserId(user.id())
                                              .join()
                                              .findFirst()
                                              .orElse(null);
                                      session.setGrant(bestGrant);
                                      userRepository.update(user);
                                    })));
  }
}
