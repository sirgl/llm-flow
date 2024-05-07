package sirgl.model.agent

interface Tool<R> {
    // TODO what exactly is returned? what exactly the arguments are?
    suspend fun execute(args: Args) : R // it is more convenient for testing
}

class ToolSpec(
    val id: String,
    val name: String,
    val description: String,
    val params: List<ParamSpec>
)


class ParamSpec(val name: String, val description: String)

class SearchEngine {
    suspend fun search(text: String): String {
        return "TODO"
    }
}

class SearchTool(private val searchEngine: SearchEngine) : Tool<String> {
    override suspend fun execute(args: Args) : String {
        args.get("")
        TODO()
//        return searchEngine.search()
    }
}

class Args {
    fun get(name: String) : String {
        TODO()
    }
}