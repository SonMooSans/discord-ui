package components

import Player
import UnoGame
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.sonmoosans.dui.component
import net.sonmoosans.dui.components.*
import net.sonmoosans.dui.context.EventContext
import net.sonmoosans.dui.hooks.useState
import net.sonmoosans.dui.utils.Embed
import net.sonmoosans.dui.utils.value
import utils.BlackCard
import utils.Card
import utils.CardColor
import utils.reply
import java.awt.Color

data class ActionPanelProps(val game: UnoGame, val player: Player)

val ActionPanel = component<ActionPanelProps> {
    val selecting = useState<BlackCard?> { null }

    if (selecting.value != null) {
        embed(title = "Change color")

        row {
            menu(placeholder = "Select a Color") {
                for (color in CardColor.values()) {
                    option(color.name, color.name)
                }
                submit {
                    val (game, player) = props

                    checkPlayer(game.currentPlayer) { return@submit }

                    val card = selecting.value!!
                    card.color = CardColor.valueOf(event.value())

                    put(game, player, card) {
                        selecting.value = null

                        event.edit()
                    }
                }
            }
        }

        return@component
    }

    val (_, player) = props

    if (player.cards.isEmpty()) {
        embed(title = "You already won the Game", color = Color.GREEN)

        return@component
    }

    embed(title = "Select a Card")
    rowLayout {
        val last = props.game.last

        menu(placeholder = "Select Your card") {
            for ((i, card) in player.cards.withIndex()) {
                val available = last == null || card.canPutAbove(last.card)

                option(card.name, i.toString(),
                    emoji = Emoji.fromUnicode(if (available) "✅" else "❌")
                )
            }

            submit {
                val (game) = props
                val current = game.currentPlayer

                checkPlayer(current) { return@submit }
                val selected = event.value().toInt()

                when (val card = current.cards[selected]) {

                    is BlackCard -> {
                        selecting.value = card

                        event.edit()
                    }

                    else -> put(game, current, card) {
                        event.edit()
                    }
                }
            }
        }

        button("Update") {
            event.edit()
        }
    }
}

inline fun<T> EventContext<out T, *>.put(game: UnoGame, player: Player, card: Card, then: () -> Unit) where T: IReplyCallback, T: IMessageEditCallback {
    if (game.put(player, card)) {

        if (game.winners.contains(player)) {
            event.editMessageEmbeds(
                Embed(title = "You already won the Game", color = Color.GREEN)
            ).apply {
                isReplace = true
                queue()
            }
        } else {
            then()
        }
    } else {
        Embed(
            title = "You cannot use this Card",
            description = "Try another cards or pick one",
            color = Color.RED
        ).reply(event) {
            setEphemeral(true)
            queue()
        }
    }
}