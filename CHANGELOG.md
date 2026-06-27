# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-06-27
### Added
- Complete Minecraft Hide and Seek Infection gameplay logic.
- Native Hider scaling to make Hiders tiny using `Attribute.GENERIC_SCALE` (no client resource pack required).
- Interactive, multi-arena setup configuration using custom admin commands and selection wand (Golden Axe).
- Custom Bazooka rocket launcher firing explosive snowballs with trail particle effects and damage falloff.
- Integrated Boss Bar, Scoreboard sidebar, Action Bar notifications, and Sound/Title components.
- Persistent inventory cache and automatic restoration system on match end/leave/quit.
- Automatic creation of plugin directories, config templates (`config.yml`, `messages.yml`), arenas folder, and global lobby configurations.
- GitHub Actions CI workflow to build the project and attach the release JAR on new tags.
