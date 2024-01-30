package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.internal.getEnv
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test

private const val HIDE_CURSOR = "$CSI?25l"
private const val SHOW_CURSOR = "$CSI?25h"
private const val CLEAR_SCREEN = "${CSI}0J"

class ThreadAnimatorTest {
    private val vt = TerminalRecorder(width = 56)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun `unit animator`() {
        var i = 1
        val a = t.textAnimation<Unit> { "${i++}" }.animateOnThread(fps=10000) { i >2 }
        a.runBlocking()
        vt.output() shouldBe "${HIDE_CURSOR}1\r2\r3\r$CLEAR_SCREEN${SHOW_CURSOR}\n3"
    }
    @Test
    fun `smoke test`() {
        // this test is flaky on CI since it's reliant on timing
        if (!getEnv("CI").isNullOrEmpty()) return

        val a = progressBarLayout(spacing = 0) {
            completed(fps = 30)
        }.animateOnThread(t)
        val t = a.addTask(total = 10)
        val service = Executors.newSingleThreadExecutor()
        try {
            var future = a.execute(service)

            t.update(5)
            vt.waitForOutput { it shouldBe "        5/10" }

            future.isDone shouldBe false

            t.update(10)
            future.get(100, TimeUnit.MILLISECONDS)
            future.isDone shouldBe true

            t.reset()
            future = a.execute(service)
            vt.waitForOutput { it shouldBe "        0/10" }
            a.stop()
            future.get(100, TimeUnit.MILLISECONDS)
            vt.normalizedOutput() shouldBe "        0/10\n$SHOW_CURSOR"

            t.reset()
            future = a.execute(service)
            vt.waitForOutput { it shouldBe "        0/10" }
            a.clear()
            future.get(100, TimeUnit.MILLISECONDS)
            vt.normalizedOutput() shouldBe "${CSI}0J$SHOW_CURSOR"
        } finally {
            service.shutdownNow()
        }
    }

    private fun TerminalRecorder.waitForOutput(block: (String) -> Unit) {
        for (i in 0..100) {
            try {
                block(normalizedOutput())
            } catch (e: Throwable) {
                if (i == 100) throw e
                Thread.sleep(10)
            }
        }
    }

    private fun TerminalRecorder.normalizedOutput(): String {
        return output().substringAfterLast("\r").trimEnd()
    }
}
