package com.scofu.community.service;

import static com.scofu.network.document.Filter.equalsTo;
import static com.scofu.network.document.Filter.where;
import static com.scofu.network.document.Query.query;

import com.scofu.common.inject.Feature;
import com.scofu.community.Grant;
import com.scofu.community.GrantRepository;
import com.scofu.community.Session;
import com.scofu.community.User;
import com.scofu.community.UserRepository;
import com.scofu.network.document.DocumentStateListener;
import com.scofu.network.message.Result;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
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
        .accept(
            optionalUser ->
                optionalUser.ifPresent(
                    user ->
                        user.session()
                            .ifPresent(
                                session ->
                                    grantRepository
                                        .byUserId(user.id())
                                        .accept(
                                            grants -> {
                                              final var bestGrant = grants.findFirst().orElse(null);
                                              session.setGrant(bestGrant);
                                              userRepository.update(user);
                                            }))))
        .timeoutAfter(10, TimeUnit.SECONDS);
  }

  @Override
  public void onDelete(String id) {
    record Pair(User user, Session session) {}
    userRepository
        .find(query().filter(where("session.grant._id", equalsTo(id))).limitTo(1).build())
        .map(map -> map.values().stream().findFirst())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(user -> new Pair(user, user.session().orElse(null)))
        .filter(pair -> Objects.nonNull(pair.session))

        .flatMap(pair -> setGrantAndUpdate(pair.user, pair.session));
  }

  private Result<User> setGrantAndUpdate(User user, Session session) {
    return grantRepository
        .byUserId(user.id())
        .map(Stream::findFirst)
        .apply(Optional::orElse, (Supplier<Grant>) () -> null)
        .accept(session::setGrant)
        .map(unused -> user)
        .flatMap(userRepository::update);
  }
}
