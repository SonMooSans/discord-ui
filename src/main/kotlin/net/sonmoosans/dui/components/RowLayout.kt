package net.sonmoosans.dui.components

import net.sonmoosans.dui.context.Container
import net.sonmoosans.dui.context.RenderContainer
import net.sonmoosans.dui.context.RenderContext
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.sonmoosans.dui.Component
import net.sonmoosans.dui.Data
import net.sonmoosans.dui.utils.join
import net.sonmoosans.dui.utils.lambdaList
import java.util.*

private const val rowSpace = 1.0

/**
 * Detects and split overflowed components into multi Action Rows
 */
fun<D: Data<P>, P : Any> RenderContext<D, P>.rowLayout(components: RenderContainer<ActionComponent, D, P>.() -> Unit) {

    val rows = builder.components.join<LayoutComponent>(
        split(lambdaList(components))
    )

    builder.setComponents(rows)
}

/**
 * Detects and split overflowed components into multi Action Rows
 */
fun Container<in ActionRow>.rowLayout(components: Container<ActionComponent>.() -> Unit) {
    for (row in split(lambdaList(components))) {
        add(row)
    }
}

private fun split(components: List<ActionComponent>): List<ActionRow> {
    val rows = arrayListOf<ActionRow>()
    val current: Stack<ActionComponent> = Stack()
    var space = rowSpace

    components.forEach { item ->
        val size = rowSpace / item.type.maxPerRow

        if (size <= space) {
            space -= size
        } else {
            rows += ActionRow.of(current)

            current.clear()
            space = rowSpace
        }

        current.push(item)
    }

    if (current.isNotEmpty())
        rows += ActionRow.of(current)

    return rows
}