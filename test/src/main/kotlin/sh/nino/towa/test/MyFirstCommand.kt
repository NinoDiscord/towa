package sh.nino.towa.test

import dev.kord.common.Locale
import sh.nino.towa.slash.commands.annotations.ApplicationCommand
import sh.nino.towa.slash.commands.application.AbstractApplicationCommand
import sh.nino.towa.slash.commands.application.ApplicationCommandContext
import sh.nino.towa.slash.commands.application.int

@ApplicationCommand("heck", "boop the fluff")
object MyFirstCommand: AbstractApplicationCommand() {
    init {
        localiseName(Locale.ENGLISH_GREAT_BRITAIN, "heckle")
        localiseDescription(Locale.ENGLISH_GREAT_BRITAIN, "boop thy fluffs!")
    }

//    private val amount by int("amount", "How many times we should boop the fluff") {
//        localiseDescription(Locale.ENGLISH_GREAT_BRITAIN, "how many thy we should boop ze floof?")
//    }

    override suspend fun execute(context: ApplicationCommandContext) {
//        val amountToBoop = context.option(amount)
    }
}
