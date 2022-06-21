# Module towa-core
**towa-core** is the main core package that is used by the [slash-commands](https://towa.nino.sh/slash-commands/index.html) package. It
is also recommended to bundle **towa-core** as a dependency if you wish to use the Extensions feature.

```kotlin
suspend fun main(args: Array<String>) {
    val towa = Towa { // this: sh.nino.towa.core.TowaBuilder
        kord("token") { // this: KordBuilder
            enableShutdownHook = false
        }
    }
    
    towa.start()
    towa.kord.login()
}
```
