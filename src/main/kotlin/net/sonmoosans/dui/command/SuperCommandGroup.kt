package net.sonmoosans.dui.command

import net.sonmoosans.dui.command.builder.SuperCommandBuilder
import bjda.plugins.supercommand.entries.*
import net.sonmoosans.dui.command.entries.SlashLocalization
import net.sonmoosans.dui.command.entries.SuperNode
import net.sonmoosans.dui.command.entries.localizes
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.internal.interactions.CommandDataImpl

abstract class SuperCommandGroup(
    override val name: String,
    val description: String,
    override val guildOnly: Boolean? = null,
    override val permissions: DefaultMemberPermissions? = null,
): SlashLocalization, SuperNode, PermissionEntry {

    open val groups: List<SuperCommandGroup>? = null
    open val commands: List<SuperCommand>? = null

    override fun build(listeners: Listeners): CommandData {
        val data = CommandDataImpl(name, description)
            .setLocalize()
            .setPermissions()

        if (groups != null) {
            data.addSubcommandGroups(
                groups!!.map { g ->
                    g.buildSub(name, listeners)
                }
            )
        }

        if (commands != null) {
            data.addSubcommands(
                commands!!.map {cmd ->
                    cmd.buildSub(name, listeners = listeners)
                }
            )
        }

        return data
    }

    private fun buildSub(group: String, listeners: Listeners): SubcommandGroupData {
        val data = SubcommandGroupData(name, description).setLocalize()
        val commands = commands?: error("Sub command group cannot be empty or null")

        data.addSubcommands(commands.map {cmd ->
            cmd.buildSub(group, name, listeners)
        })

        return data
    }

    companion object {
        fun create(
            name: String,
            description: String,
            guildOnly: Boolean? = null,
            permissions: DefaultMemberPermissions? = null,
            init: SuperCommandGroupBuilder.() -> Unit
        ): SuperCommandGroup {
            val builder = SuperCommandGroupBuilder(
                SuperCommandGroupImpl(
                    name, description, guildOnly, permissions
                )
            )

            builder.apply(init)

            return builder.base
        }

        fun create(name: String, description: String, vararg commands: SuperCommand): SuperCommandGroup {

            return object : SuperCommandGroup(name, description) {
                override val commands = commands.toList()
            }
        }

        fun create(name: String, description: String, vararg groups: SuperCommandGroup): SuperCommandGroup {

            return object : SuperCommandGroup(name, description) {
                override val groups = groups.toList()
            }
        }
    }
}

class SuperCommandGroupImpl(
    name: String,
    description: String,
    guildOnly: Boolean? = null,
    permissions: DefaultMemberPermissions? = null,
) : SuperCommandGroup(name, description, guildOnly, permissions) {

    override var localNames = localizes()
    override var localDescriptions = localizes()

    override val groups = ArrayList<SuperCommandGroup>()
    override val commands = ArrayList<SuperCommand>()
}

class SuperCommandGroupBuilder(val base: SuperCommandGroupImpl) {

    fun group(
        name: String,
        description: String,
        guildOnly: Boolean? = null,
        permissions: DefaultMemberPermissions? = null,
        init: SuperCommandGroupBuilder.() -> Unit
    ) {
        val group = SuperCommandGroup.create(name, description, guildOnly, permissions, init)

        this.group(group)
    }

    fun command(
        name: String,
        description: String,
        guildOnly: Boolean? = null,
        permissions: DefaultMemberPermissions? = null,
        init: SuperCommandBuilder.() -> Unit
    ) {
        val command = net.sonmoosans.dui.command.builder.command(
            name, description, guildOnly, permissions, init
        )

        this.command(command)
    }

    fun group(vararg group: SuperCommandGroup) {
        base.groups += group
    }

    fun command(vararg commands: SuperCommand) {
        base.commands += commands
    }

    fun name(locale: DiscordLocale, name: String) {
        base.localNames[locale] = name
    }

    fun description(locale: DiscordLocale, description: String) {
        base.localDescriptions[locale] = description
    }
}