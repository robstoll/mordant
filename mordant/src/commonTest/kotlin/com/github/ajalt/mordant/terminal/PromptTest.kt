package com.github.ajalt.mordant.terminal

import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

@OptIn(ExperimentalTerminalApi::class)
class PromptTest {
    private val vt = VirtualTerminalInterface(width = 8)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun stringPrompt() {
        vt.inputLines = mutableListOf("answer")
        StringPrompt("pr", t).ask() shouldBe "answer"
        vt.buffer() shouldBe "pr: "
    }

    @Test
    @JsName("StringPrompt_with_default")
    fun `StringPrompt with default`() {
        vt.inputLines = mutableListOf("")
        StringPrompt("pr", t, default = "def").ask() shouldBe "def"
        val style = t.theme.style("prompt.default")
        vt.buffer() shouldBe "pr ${style("(def)")}: "
    }

    @Test
    @JsName("StringPrompt_with_choices")
    fun `StringPrompt with choices`() {
        vt.inputLines = mutableListOf("b")
        StringPrompt("pr", t, choices = listOf("a", "b")).ask() shouldBe "b"
        val s = t.theme.style("prompt.choices")
        vt.buffer() shouldBe "pr ${s("[a, b]")}: "
    }

    @Test
    @JsName("StringPrompt_invalid_choices")
    fun `StringPrompt invalid choice`() {
        vt.inputLines = mutableListOf("bad", "a")
        StringPrompt("pr", t, choices = listOf("a", "b"), ).ask() shouldBe "a"
        val s = t.theme.style("prompt.choices")
        val e = t.theme.danger
        val p = "pr ${s("[a, b]")}: "
        vt.buffer() shouldBe "$p${e("Invalid value, choose from [a, b]")}\n$p"
    }
}