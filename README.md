# Amurians Mod

This mod adds the following things to the game:
- Rubies, ruby blocks, and ruby armor
- The emery table, a crafting block akin to the stonecutter but with a different purpose
- Azalea wood (WIP)
- Amurians, as a mob (WIP)

## Usage

Requires Java 17 or higher to run.

On Windows, use `gradlew` in cmd or `.\gradlew` in PowerShell. On other OSes, use `./gradlew`.
Gradle scripts include:
- `gradlew genSources` -- decompiles minecraft jar to readable Java, visible in vs code with Ctrl-P.
- `gradlew runClient` -- for testing the client. Uses `run` as the minecraft folder.
- `gradlew build` -- for creating a jar. Output is in `build/libs`.
- `gradlew --stop` -- stops all running Gradle daemons, for when Java starts behaving weirdly.