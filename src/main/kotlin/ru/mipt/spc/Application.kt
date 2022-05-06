package ru.mipt.spc

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.css.CssBuilder
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.style
import ru.mipt.spc.magprog.magProgPage
import space.kscience.dataforge.context.Context
import space.kscience.snark.SnarkPlugin
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.relativeTo

fun CommonAttributeGroupFacade.css(block: CssBuilder.() -> Unit) {
    style = CssBuilder().block().toString()
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

private fun Application.resolveData(uri: URI, targetPath: Path): Path {
    if (Files.isDirectory(targetPath)) {
        log.info("Using existing data directory at $targetPath.")
    } else {
        log.info("Copying data from $uri into $targetPath.")
        targetPath.createDirectories()
        //Copy everything into a temporary directory
        FileSystems.newFileSystem(uri, emptyMap<String, Any>()).use { fs ->
            val rootPath: Path = fs.provider().getPath(uri)
            Files.walk(rootPath).forEach { source: Path ->
                if (source.isRegularFile()) {
                    val relative = source.relativeTo(rootPath).toString()
                    val destination: Path = targetPath.resolve(relative)
                    destination.parent.createDirectories()
                    Files.copy(source, destination)
                }
            }
        }
    }
    return targetPath
}


fun main() {
    val context = Context("spc-site") {
        plugin(SnarkPlugin)
    }

    val dataPath = Path.of("data")

    embeddedServer(Netty, port = 7080, watchPaths = listOf("classes")) {
        install(StatusPages) {
            exception<AuthenticationException> { call, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { call, _ ->
                call.respond(HttpStatusCode.Forbidden)
            }
        }

        val magProgDataPath = resolveData(
            javaClass.getResource("/magprog")!!.toURI(),
            dataPath.resolve("magprog")
        )

        magProgPage(context, rootPath = magProgDataPath)

    }.start(wait = true)

}
