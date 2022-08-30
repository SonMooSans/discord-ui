import net.dv8tion.jda.api.interactions.DiscordLocale
import net.sonmoosans.bjda.bjda
import net.sonmoosans.bjda.plugins.supercommand.builder.command
import net.sonmoosans.bjda.plugins.supercommand.supercommand
import net.sonmoosans.bjda.wrapper.Mode
import net.sonmoosans.dui.bjda.DUIModule
import net.sonmoosans.dui.component
import net.sonmoosans.dui.components.*
import net.sonmoosans.dui.context.RenderContext
import net.sonmoosans.dui.graphics.drawStringCenter
import net.sonmoosans.dui.graphics.paint
import net.sonmoosans.dui.graphics.toInputStream
import net.sonmoosans.dui.hooks.sync
import net.sonmoosans.dui.hooks.useModalLazy
import net.sonmoosans.dui.hooks.useState
import net.sonmoosans.dui.utils.*
import java.awt.Color
import java.awt.image.BufferedImage

data class Props(override val locale: DiscordLocale): LocaleProps

val example = component<Props> {

    tabLayout {
        tab("Todos") {
            text("Hello World")
            todo()
        }

        tab("Settings") {
            row {
                button("Close") {
                    event.delete()
                }
            }
        }
    }

    tabLayout {
        tab(locale("Todos", chinese("待辦事項"))) {
            todo()
        }

        tab("Settings") {
            row {
                button("Close") {
                    event.delete()
                }
            }
        }
    }
}

fun RenderContext<*, *>.todo() {
    val todos = useState { arrayListOf<String>() }

    val addModal by useModalLazy {
        title = "Add Todo"

        row {
            input("todo", "Todo Content")
        }

        submit {
            todos.value += event["todo"]

            event.edit()
        }
    }

    embed(title = "Todos") {
        for (todo in todos.value) {
            field(name = todo)
        }
    }

    row {
        button(label = "Add") {
            addModal.open(event)
        }
    }
}

suspend fun main() {
    bjda(Mode.Default) {
        config {
            setToken(System.getenv("TOKEN"))
        }

        supercommand(
            TestCommand(), TodoCommand()
        )

        + DUIModule()
    }
}

val ModernTodoApp = component<Unit> {
    val todos = useState { mutableListOf("hello") }

    var expectedHeight = (110 * todos.value.size) + (50 * 2)

    if (todos.size != 0) {
        expectedHeight -= 10
    }

    val image = BufferedImage(500, expectedHeight, BufferedImage.TYPE_INT_RGB)

    val addModal by useModalLazy {
        title = "Add Todo"
        row {
            input("todo", "Todo Content")
        }
        submit {
            todos += event["todo"]

            event.edit()
        }
    }

    with (image.createGraphics()) {
        val (w, h) = 450 to 100

        font = font.deriveFont(25f)
        translate((500 - w) / 2, 50)

        todos.forEach { todo ->

            paint(Color.DARK_GRAY) {
                fillRoundRect(0, 0, w, h, 20, 20)
            }

            paint(Color.WHITE) {
                drawStringCenter(todo, w, h)
            }

            translate(0, h + 10)
        }
    }

    row {
        button("Add") {
            event.replyModal(addModal).queue()
        }
    }

    files {
        file("ui.png", image.toInputStream())
    }
}

fun TestCommand() = command("test", "Testing Command") {

    execute {
        val ui = example.create(event.user.idLong, Props(locale = event.userLocale)) {
            sync(event.hook)
        }

        event.reply(ui).queue()
    }
}

fun TodoCommand() = command("todo", "Todo App") {
    execute {
        val ui = ModernTodoApp.create(event.user.idLong, Unit)

        event.reply(ui).queue()
    }
}