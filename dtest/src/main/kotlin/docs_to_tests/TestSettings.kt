package docs_to_tests

import javax.script.ScriptContext
import javax.script.ScriptEngineManager

interface TestSettings {
    fun executeTestSnippet(testSnippet: String)
}

object DefaultTestSettings : TestSettings {
    private val defaultKtsEngine =
        ScriptEngineManager().getEngineByExtension("kts")!!

    override fun executeTestSnippet(testSnippet: String) {
        defaultKtsEngine.getBindings(ScriptContext.ENGINE_SCOPE).clear()
        defaultKtsEngine.eval("import org.junit.jupiter.api.Assertions.assertEquals")
        defaultKtsEngine.eval(testSnippet)
    }
}