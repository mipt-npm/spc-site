package ru.mipt.spc

import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.css.CssBuilder
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.style
import space.kscience.snark.SnarkEnvironment
import space.kscience.snark.ktor.site
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.*

fun CommonAttributeGroupFacade.css(block: CssBuilder.() -> Unit) {
    style = CssBuilder().block().toString()
}

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

const val DEPLOY_DATE_FILE = "deployDate"
const val BUILD_DATE_FILE = "/buildDate"

@Suppress("unused")
fun Application.spcModule() {
//    install(HttpsRedirect)

    val dataPath = Path.of("data")

    // Clear data directory if it is outdated
    val deployDate = dataPath.resolve(DEPLOY_DATE_FILE).takeIf { it.exists() }
        ?.readText()?.let { LocalDateTime.parse(it) }
    val buildDate = javaClass.getResource(BUILD_DATE_FILE)?.readText()?.let { LocalDateTime.parse(it) }

    val inProduction: Boolean = environment.config.propertyOrNull("ktor.environment.production") != null

    if (inProduction) {
        log.info("Production mode activated")
        log.info("Build date: $buildDate")
        log.info("Deploy date: $deployDate")
    }

    if (deployDate != null && buildDate != null && buildDate.isAfter(deployDate)) {
        log.info("Outdated data. Resetting data directory.")

        Files.walk(dataPath)
            .sorted(Comparator.reverseOrder())
            .forEach { it.deleteIfExists() }

        //Writing deploy date file
        dataPath.createDirectories()
        dataPath.resolve(DEPLOY_DATE_FILE).writeText(LocalDateTime.now().toString())

    } else if (inProduction && deployDate == null && buildDate != null) {
        val date = LocalDateTime.now().toString()
        log.info("Deploy date: $date")
        //Writing deploy date in production mode if it does not exist
        dataPath.createDirectories()
        dataPath.resolve(DEPLOY_DATE_FILE).writeText(date)
    }

    SnarkEnvironment.default.site {

        resolveData(
            this@spcModule.javaClass.getResource("/common")!!.toURI(),
            dataPath / "common"
        )

        val homeDataPath = resolveData(
            this@spcModule.javaClass.getResource("/home")!!.toURI(),
            dataPath / "home"
        )

        spcHome(dataPath = homeDataPath)

        val mastersDataPath = resolveData(
            this@spcModule.javaClass.getResource("/magprog")!!.toURI(),
            dataPath / "magprog"
        )

        spcMasters(dataPath = mastersDataPath)
    }
}


fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

