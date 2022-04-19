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
import com.scofu.community.Rank;
import com.scofu.community.RankRepository;
import java.lang.reflect.Type;
import java.util.stream.Stream;
import javax.inject.Inject;

final class RankTransformer implements Transformer<Rank> {

  private final RankRepository rankRepository;

  @Inject
  RankTransformer(RankRepository rankRepository) {
    this.rankRepository = rankRepository;
  }

  @Override
  public boolean test(Type type) {
    return type instanceof Class rawType && Rank.class.isAssignableFrom(rawType);
  }

  @Override
  public Result<Rank> transform(Command command, Parameter<Rank> parameter, Parameters parameters,
      Arguments arguments) {
    if (!arguments.hasNext()) {
      return Result.empty();
    }
    return arguments.nextQuotable(parameter).flatMap(string -> parseRank(string, parameter));
  }

  @Override
  public Stream<String> suggest(Command command, Parameter<Rank> parameter, Parameters parameters,
      Result<String> argument) {
    return rankRepository.cache().asMap().values().stream().map(Rank::name);
  }

  private Result<Rank> parseRank(String string, Parameter<Rank> parameter) {
    return rankRepository.findByName(string)
        .join()
        .map(Result::value)
        .orElseGet(() -> Result.error(
            new ParameterArgumentException(translatable("No rank with name %s.", text(string)),
                parameter)));
  }
}
