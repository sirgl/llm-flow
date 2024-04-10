package newImpl

import ai.grazie.api.gateway.client.SuspendableAPIGatewayClient
import ai.grazie.client.common.SuspendableHTTPClient
import ai.grazie.client.ktor.GrazieKtorHTTPClient
import ai.grazie.model.auth.v5.AuthData
import ai.grazie.model.cloud.AuthType
import ai.grazie.model.llm.profile.GoogleProfileIDs
import ai.grazie.model.llm.prompt.LLMPromptID
import com.google.gson.Gson
import kotlinx.coroutines.*


suspend fun callToChat() {
    val appToken = System.getenv("Grazie-Auth-Application-JWT")

    val client = SuspendableAPIGatewayClient(
        serverUrl = "https://api.app.stgn.grazie.aws.intellij.net",
        authType = AuthType.Application,
        httpClient = SuspendableHTTPClient.WithV5(
            GrazieKtorHTTPClient.Client.Default,
            authData = AuthData(appToken, originalApplicationToken = appToken)
        ),
    )

    val chatResponseStream = client.llm().v6().chat {
        prompt = LLMPromptID("pizza_prompt")
        profile = GoogleProfileIDs.Chat.GeminiPro
        messages {
            user("""
You are an autonomous agent in IDE.
You are given a snippet:
```
21            return CachedValuesManager.getCachedValue(file) {
22                val importAliases = file.importDirectives.mapNotNull { it.aliasName }.toSet()
23                val map = ConcurrentFactoryMap.createMap<String, Boolean> { s ->
24                    s in importAliases || KotlinTypeAliasShortNameIndex.get(s, project, file.resolveScope).isNotEmpty()
25                }
26                Result.create<ConcurrentMap<String, Boolean>>(map, OuterModelsModificationTrackerManager.getTracker(project))
27            }
```
It is a snippet showing reference `OuterModelsModificationTrackerManager`.
You need to refactor this sample and extract variable.
You must provide only the replacement code for the whole segment (all lines, not omitting anything, with line numbers).
Note that you need to escape text in replacement.
Format: { "reasoning" : "<reasoning>", "replacement" : "<text>" }

Example (but when asked to rename):
```
2 class A {
3 }
```
Response:
{  "reasoning" : "Replacement requires only handling of class name.", "replacement" : "2 class B {\n 3 }" }
            """.trimMargin())
        }
    }

    chatResponseStream.collect {
        print(it.content)
    }
}

fun getPrompt(snippet: String, task: String): String {
    return """
        You are executing a task in IDE.
        You are given a snippet:
        ```
$snippet
        ```
        You need to change the code in the following way. Task:
        $task
        
        You must provide only the replacement code for the whole snippet (all lines, not omitting anything, with line numbers).
        Note that you need to escape text in replacement.
        Format: { "reasoning" : "<reasoning>", "replacement" : "<text>" }

        Example.
        Task: Rename A to B.
        ```
        2 class A {
        3 }
        ```
        Response:
        {  "reasoning" : "Replacement requires only handling of class name.", "replacement" : "2 class B {\n 3 }" }
        
        Remember - answer only in JSON, no explanations in plain text, no ``` in the code. Do NOT emit markdown
        Start with '{'.
    """.trimIndent()
}

data class Result(val reasoning: String, val replacement: String)

fun parseResult(json: String): Result? {
    return try {
        Gson().fromJson(json, Result::class.java)
    } catch (e: Exception) {
        // In case of JSON parsing failure
        null
    }
}


fun requestLLM(prompt: String) : String {
    val appToken = System.getenv("Grazie-Auth-Application-JWT")

    val client = SuspendableAPIGatewayClient(
        serverUrl = "https://api.app.stgn.grazie.aws.intellij.net",
        authType = AuthType.Application,
        httpClient = SuspendableHTTPClient.WithV5(
            GrazieKtorHTTPClient.Client.Default,
            authData = AuthData(appToken, originalApplicationToken = appToken)
        )
    )

    return runBlocking {
        val chatResponseStream = client.llm().v6().chat {
            this.prompt = LLMPromptID("hackathon2024")
            profile = GoogleProfileIDs.Chat.GeminiPro1_5
            messages {
                user(prompt)
            }
        }

        // Collecting all responses into a StringBuffer
        val buffer = StringBuffer()
        chatResponseStream.collect {
            println(it.content)
            buffer.append(it.content)
        }

        return@runBlocking buffer.toString()
    }
}

suspend fun getResponseWithRetries(prompt: String, maxRetries: Int = 3): Result? {
    // Prepare coroutine scope
    val scope = CoroutineScope(Dispatchers.IO)

    // Convert prompt to required format

    var retries = 0
    while (retries < maxRetries) {
        var result: Result? = null
        retries++

        try {
            // Timeout could be added here to also handle situations where requestLLM function takes too long
            val jsonResult = withTimeoutOrNull(10000) {
                // use launch to call the function in separate coroutine with an associated job
                scope.launch {
                    try {
                        val jsonMaybe = requestLLM(prompt)
                        val json = extractJson(jsonMaybe)
                        result = parseResult(json)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.join() // join will suspend the coroutine until the launched coroutine completes
            }

            if (jsonResult == null) {
                println("Request timed out.")
            } else if (result != null) {
                // If parse was successful, return the result
                return result
            }
        } catch (e: Exception) {
            // Retry in case of exception
            println("Exception during request: ${e.message}")
        }
    }

    // After several retries, return null
    return null
}

fun extractJson(markdown: String): String {
    val regex = "^```\\n?json\\n?(.*)\\n?```$".toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
    val matchResult = regex.find(markdown)
    return if (matchResult != null) {
        matchResult.groups[1]?.value?.trim() ?: ""
    } else {
        markdown
    }
}


fun main() {
    println(extractJson("```json {} ```"))
//    val prompt = getPrompt(
//        """
//                21            return CachedValuesManager.getCachedValue(file) {
//                22                val importAliases = file.importDirectives.mapNotNull { it.aliasName }.toSet()
//                23                val map = ConcurrentFactoryMap.createMap<String, Boolean> { s ->
//                24                    s in importAliases || KotlinTypeAliasShortNameIndex.get(s, project, file.resolveScope).isNotEmpty()
//                25                }
//                26                Result.create<ConcurrentMap<String, Boolean>>(map, OuterModelsModificationTrackerManager.getTracker(project))
//                27            }
//            """.trimIndent(), "Extract OuterModelsModificationTrackerManager expression into a variable."
//    )
//    println(prompt)
//    runBlocking {
//        println(
//            getResponseWithRetries(
//                prompt
//            )?.replacement
//        )
//    }
}