# 👾 トワ towa
> *Powerful and advanced command handling library made for Discord*

## What is this?
This is a (totally original) command handling library for [Kord](https://github.com/kordlib/kord). The name "Towa" was an inspiration
of **Towa Tokoyami** from Hololive Gen 4.

Towa has support for Discord's new slash commands API and for text-based commands if you want something done. Nino uses both slash
and text-based commands (also known as "legacy-prefixed commands").

**Towa** uses a concept to build off features that should be put into mutiple modules. This can be used from the `AbstractFeature`
where you can create your own features that can be exposed from the Towa API.

## Example
```kotlin
suspend fun main(args: Array<String>) {
    val towa = Towa {
        // Enables the Slash Commands API
        useSlashCommands {
            locator = ReflectionBasedLocator {
                // Finds all the `AbstractApplicationCommand`s in `sh.nino.towa.commands`
                commandsPackage = "sh.nino.towa.commands"

                // Finds all `AbstractInhibitor`s in `sh.nino.towa.inhibitors`
                inhibitorsPackage = "sh.nino.towa.inhibitors"
            }

            // Registers a default `/help` and `/ping` command.
            addDefaultCommands(
                DefaultCommand.Help,
                DefaultCommand.Ping
            )
        }
    }
    
    // Registers all the commands, pipelines, and inhibitors.
    // It also registers all the select menu, text prompts, buttons,
    // and autocomplete handlers.
    towa.launch()
}
```

## Subprojects
**Towa** is split into multiple subprojects to abstract pieces from.

- [towa-slash-commands](./slash-commands) **-** Slash commands implementation for Towa.
- [towa-legacy-commands](./legacy-commands) **-** Legacy prefixed based commands impl for Towa.
- [towa-locator-koin](./locators/koin) **-** Enables the use of Koin for injecting commands, pipelines, and inhibitors.
- [towa-core](./core) **-** The core implementation for Towa.

## Pipelines
**Pipelines** are a concept to intercept the command execution process for anything really! If you wanted to do conditional logic,
your best bet is to use an [**inhibitor**](#inhibitors).

You can create a basic pipeline with the `createPipeline` function from **towa-core**:

```kotlin
val MyPipeline = createPipeline("some:pipeline:name") {
    isGlobal = true
    
    onCommandExecuted { ctx ->
        val command = ctx.command
        val message = ctx.message
        
        log.info("Executed command ${command.name} from ${message.author.tag}!")
    }
    
    onCommandErrored { ctx ->
        log.error("Command ${ctx.command.name} has failed:", ctx.cause)
    }
    
    onInteractable(InteractableType.BUTTON) { ctx ->
        log.info("User ${ctx.message.author.tag} has clicked button with custom ID '${ctx.interactable<TowaButton>().id}'")
    }
}
```

You can extend from the **CommandExecutionPipeline**, **MainPipeline**, or **InteractionPipeline** if you want it to be registered
if `isGlobal` is set to **true**, else you will need to register it to a command, so it can be interacted with.

```kotlin
class MyPipeline: CommandExecutionPipeline("some:pipeline:name") {
    override val isGlobal: Boolean = true
    
    override suspend fun onCommandExecuted(ctx: CommandExecutedContext) {
        val command = ctx.command
        val message = ctx.message

        log.info("Executed command ${command.name} from ${message.author.tag}!")
    }
    
    override suspend fun onCommandErrored(ctx: CommandErroredContext) {
        log.error("Command ${ctx.command.name} has failed:", ctx.cause)
    }
}
```

Pipelines can be executed globally or with a specific command, in which, you can register the pipeline using the `AbstractCommand.registerPipeline`
and it will be called once the command reaches a certain point in the Towa lifecycle.

## Inhibitors
**Inhibitors** is a concept to guard a command based of a condition, for example:

- If the user is executing a NSFW command and the channel is not a NSFW channel.
- If the user should be in a guild and not in a DM channel with the bot.

You can create your own inhibitors with the `createInhibitor` function if you do not use a locator or if the locator has inhibitor lookups
disabled. To load them automatically, use an `AbstractInhibitor` class.

```kotlin
val MyInhibitor = createInhibitor("some:condition") { ctx ->
    val channel = ctx.channel<TextChannel>()
    if (!channel.nsfw) {
        return err("You are not allowed to execute a NSFW command in a non NSFW channel!")
    }
    
    return ok()
}
```

```kotlin
class MyInhibitor: AbstractInhibitor("some:condition") {
    override suspend fun inhibit(ctx: CommandContext): InhibitorResult {
        val channel = ctx.channel<TextChannel>()
        if (!channel.nsfw) {
            return err("You are not allowed to execute a NSFW command in a non NSFW channel!")
        }

        return ok()
    }
}
```

## Arguments
> **Arguments** are only applicable to the **towa-legacy-commands** package only.

Coming soon.

## Installation
> Documentation: https://towa.nino.sh
>
> Version: 1.3.0

## Gradle
### Kotlin DSL
```kotlin
repositories {
    maven {
        url = uri("https://maven.floofy.dev/repo/releases")
    }
}

dependencies {
    implementation("sh.nino.towa:towa-<MODULE>:<VERSION>")
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
    implementation "sh.nino.towa:towa-<MODULE>:<VERSION>"
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
        <artifactId>towa-{{module}}</artifactId>
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
