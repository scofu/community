package com.scofu.community.bukkit;

import static net.kyori.adventure.text.Component.text;

import com.scofu.community.User;
import com.scofu.community.UserRepository;
import com.scofu.design.bukkit.Design;
import com.scofu.design.bukkit.item.Button;
import com.scofu.design.bukkit.item.ButtonBuilder;
import com.scofu.design.bukkit.window.PaginatedWindow;
import com.scofu.text.Theme;
import com.scofu.text.ThemeRegistry;
import java.util.Comparator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

final class ThemeSelectWindow extends PaginatedWindow {

  private final ThemeRegistry themeRegistry;
  private final UserRepository userRepository;
  private final Design design;
  private String selectedTheme;

  public ThemeSelectWindow(Design design, ThemeRegistry themeRegistry,
      UserRepository userRepository) {
    super(null, null, design);
    this.themeRegistry = themeRegistry;
    this.design = design;
    this.userRepository = userRepository;
  }

  @Override
  public void populate() {
    selectedTheme = userRepository.byId(viewer().player().getUniqueId().toString())
        .flatMap(User::theme)
        .orElse("Vanilla");
    super.populate();
  }

  @Override
  protected Component title(String search, int page, int pages) {
    return text("Themes");
  }

  @Override
  protected List<? extends ButtonBuilder> buttons(String search, int page) {
    return themeRegistry.themes()
        .stream()
        .sorted(Comparator.comparing(Theme::name))
        .map(theme -> Button.builder()
            .withStaticItem(viewer(),
                itemBuilder -> itemBuilder.ofType(Material.FLOWER_BANNER_PATTERN)
                    .withHideFlags(127)
                    .withName(text(theme.name()))
                    .withTags(getTags(theme))
                    .adopt(it -> {
                      if (selectedTheme.equals(theme.name())) {
                        return it.withFooter(text("Selected!")).ofType(Material.NETHER_STAR);
                      }
                      return it.withFooter(text("Click to select!"));
                    }))
            .onClick(event -> {
              event.setCancelled(true);
              selectedTheme = theme.name();
              themeRegistry.set(viewer().player(), theme);
              userRepository.byId(viewer().player().getUniqueId().toString()).ifPresent(user -> {
                user.setTheme(theme.name());
                userRepository.update(user);
              });
              design.streamContainers(viewer().player())
                  .forEach(container -> container.draw(DrawReason.OTHER, true, true));
              design.bind(viewer().player(), this);
            }))
        .toList();
  }

  private List<Component> getTags(Theme theme) {
    return List.of(colorTag("Black", theme.black()), colorTag("Blue", theme.blue()),
        colorTag("Green", theme.green()), colorTag("Cyan", theme.cyan()),
        colorTag("Red", theme.red()), colorTag("Purple", theme.purple()),
        colorTag("Yellow", theme.yellow()), colorTag("White", theme.white()),
        colorTag("Bright Black", theme.brightBlack()), colorTag("Bright Blue", theme.brightBlue()),
        colorTag("Bright Green", theme.brightGreen()), colorTag("Bright Cyan", theme.brightCyan()),
        colorTag("Bright Red", theme.brightRed()), colorTag("Bright Purple", theme.brightPurple()),
        colorTag("Bright Yellow", theme.brightYellow()),
        colorTag("Bright White", theme.brightWhite()));
  }

  private Component colorTag(String name, TextColor color) {
    return text(name).append(text(": ")).append(text("■ Color").color(color));
  }

}
