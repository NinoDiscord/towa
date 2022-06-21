# 👾 Towa (トワ)
> *Powerful and advanced command handling library made for Discord*

## What is this?
This is a (totally original) command handling library for [Kord](https://github.com/kordlib/kord). The name "Towa" was an inspiration
of **Towa Tokoyami** from Hololive Gen 4.

Towa has support for Discord's new slash commands API and for text-based commands if you want something done. Nino uses both slash
and text-based commands (also known as "legacy-prefixed commands").

**Towa** uses a concept to build off features that should be put into mutiple modules. This can be used from the `AbstractExtension`
where you can create your own features that can be exposed from the Towa API.

## Who uses this?
As of right now, [Nino](https://nino.sh) uses this! If you use it, submit a PR and you shall be showcased!~

## Example
```kotlin
suspend fun main(args: Array<String>) {
    val towa = Towa {
        // Enables the Slash Commands API
        useSlashCommands {
            
        }
        
        kord("token") {
            // Create your Kord instance here, or use `TowaBuilder.useKord` to
            // use an existing instance.
        }
    }
    
    // Registers all the commands and inhibitors.
    // It also registers all the select menu, text prompts, buttons,
    // and autocomplete handlers.
    towa.launch()
}
```

## Subprojects
**Towa** is split into multiple subprojects to abstract pieces from.

- [slash-commands](./slash-commands) **-** Slash commands implementation for Towa.
- [core](./core) **-** The core implementation for Towa.

## Installation
> Documentation: https://towa.nino.sh
>
> Version: 1.0.0

## Gradle
### Kotlin DSL
```kotlin
repositories {
    maven {
        url = uri("https://maven.floofy.dev/repo/releases")
    }
}

dependencies {
    implementation("sh.nino.towa:<MODULE>:<VERSION>")
}
```

### Groovy DSL
```groovy
repositories {
    maven {
        url "https://maven.floofy.dev/repo/releases"
    }
}

dependencies {
    implementation "sh.nino.towa:<MODULE>:<VERSION>"
}
```

## Maven
```xml
<repositories>
    <repository>
        <id>noel-maven</id>
        <url>https://maven.floofy.dev/repo/releases</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>sh.nino.towa</groupId>
        <artifactId>{{module}}</artifactId>
        <version>{{VERSION}}</version>
        <type>pom</type>
    </dependency>
</dependencies>
```

## Contributing
Thanks for considering contributing to **Towa**! Before you boop your heart out on your keyboard ✧ ─=≡Σ((( つ•̀ω•́)つ, we recommend you to do the following:

- Read the [Code of Conduct](./.github/CODE_OF_CONDUCT.md)
- Read the [Contributing Guide](./.github/CONTRIBUTING.md)

If you read both if you're a new time contributor, now you can do the following:

- [Fork me! ＊*♡( ⁎ᵕᴗᵕ⁎ ）](https://github.com/NinoDiscord/towa/fork)
- Clone your fork on your machine: `git clone https://github.com/your-username/towa`
- Create a new branch: `git checkout -b some-branch-name`
- BOOP THAT KEYBOARD!!!! ♡┉ˏ͛ (❛ 〰 ❛)ˊˎ┉♡
- Commit your changes onto your branch: `git commit -am "add features （｡>‿‿<｡ ）"`
- Push it to the fork you created: `git push -u origin some-branch-name`
- Submit a Pull Request and then cry! ｡･ﾟﾟ･(థ Д థ。)･ﾟﾟ･｡

## License
**Towa トワ** is licensed under the **MIT License** by the Nino Team with love :purple_heart:~
