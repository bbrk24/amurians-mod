# Amurians Mod

This mod adds the following things to the game:
- Rubies, ruby blocks, and ruby armor
- The emery table, a crafting block akin to the stonecutter but with a different purpose
- Azalea wood (WIP: missing boat; missing/placeholder bark, door, & trapdoor textures)
- Amurians, as a mob (heavily WIP)
- Hishai, a plant found in the sparse jungle, which can be used for food and dyes

## Usage

Requires Java 17 or higher to run.

On Windows, use `gradlew` in cmd or `.\gradlew` in PowerShell. On other OSes, use `./gradlew`.
Gradle scripts include:
- `gradlew genSources` -- decompiles minecraft jar to readable Java.
- `gradlew runClient` -- for testing the client. Uses `run` as the minecraft folder.
- `gradlew build` -- for creating a jar. Output is in `build/libs`.
- `gradlew --stop` -- stops all running Gradle daemons, for when Java starts behaving weirdly.
