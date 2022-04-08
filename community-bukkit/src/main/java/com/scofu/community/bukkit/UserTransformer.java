package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.scofu.command.ParameterArgumentException;
import com.scofu.command.Parameters;
import com.scofu.command.Result;
import com.scofu.command.model.Parameter;
import com.scofu.command.target.Arguments;
import com.scofu.command.target.Command;
import com.scofu.command.target.Transformer;
import com.scofu.community.User;
import com.scofu.community.UserRepository;
import java.lang.reflect.Type;
import javax.inject.Inject;

final class UserTransformer implements Transformer<User> {

  private final UserRepository userRepository;

  @Inject
  UserTransformer(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public boolean test(Type type) {
    return type instanceof Class rawType && User.class.isAssignableFrom(rawType);
  }

  @Override
  public Result<User> transform(Command command, Parameter<User> parameter, Parameters parameters,
      Arguments arguments) {
    if (!arguments.hasNext()) {
      return Result.empty();
    }
    return arguments.nextQuotable(parameter).flatMap(string -> parseUser(string, parameter));
  }

  private Result<User> parseUser(String string, Parameter<User> parameter) {
    return userRepository.byId(string)
        .map(Result::value)
        .orElseGet(() -> Result.error(
            new ParameterArgumentException(translatable("No user with id %s.", text(string)),
                parameter)));
  }
}
