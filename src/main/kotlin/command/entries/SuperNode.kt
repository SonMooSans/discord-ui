package command.entries

import command.Listeners
import net.dv8tion.jda.api.interactions.commands.build.CommandData

interface SuperNode {
    val name: String
    fun build(listeners: Listeners): CommandData
}