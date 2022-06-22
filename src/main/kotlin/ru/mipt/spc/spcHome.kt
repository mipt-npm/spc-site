package ru.mipt.spc

import html5up.forty.fortyScripts
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.html.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.error
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.data.Data
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.startsWith
import space.kscience.dataforge.values.string
import space.kscience.snark.*
import java.nio.file.Path
import kotlin.reflect.typeOf


context(SiteData) internal fun HTML.spcPageContent(
    meta: Meta,
    title: String = meta["title"].string ?: SPC_TITLE,
    fragment: FlowContent.() -> Unit,
) {
    spcHead(title)
    body("is-preload") {
        wrapper {
            div("alt") {
                id = "main"
                // One
                section {
                    div("inner") {
                        header("major") {
                            h1 { +title }
                        }
                        meta["image"]?.let { imageMeta ->
                            val imagePath =
                                imageMeta.value?.string ?: imageMeta["path"].string ?: error("Image path not provided")
                            val imageClass = imageMeta["position"].string ?: "main"
                            span("image $imageClass") {
                                img {
                                    src = resolveRef(imagePath)
                                    alt = imagePath
                                }
                            }
                        }
                        fragment()
                    }
                }
            }
        }

        fortyScripts()
    }
}


internal fun SiteBuilder.spcPage(subRoute: Name, meta: Meta, fragment: FlowContent.() -> Unit) {
    page(subRoute) {
        spcPageContent(meta, fragment = fragment)
    }
}

internal fun SiteBuilder.spcPage(
    subRoute: Name,
    dataPath: Name = subRoute,
    more: FlowContent.() -> Unit = {},
) {
    val data = data.resolveHtml(dataPath)
    if (data != null) {
        spcPage(subRoute, data.meta) {
            htmlData(data)
            more()
        }
    } else {
        logger.error { "Content for page with path $dataPath not found" }
    }
}

@Suppress("UNCHECKED_CAST")
internal val FortyDataRenderer: SiteBuilder.(Data<*>) -> Unit = { data ->
    if(data.type == typeOf<HtmlFragment>()) {
        data as Data<HtmlFragment>
        page {
            spcPageContent(data.meta) {
                htmlData(data)
            }
        }
    }
}

///**
// * Route a directory
// */
//internal fun SiteBuilder.spcDirectory(
//    subRoute: String,
//    dataPath: Name = subRoute.replace("/", ".").parseAsName(),
//) {
//    data.filterByType<HtmlFragment> { name, _ -> name.startsWith(dataPath) }.forEach { html ->
//        val pageName = if (html.name.lastOrNull()?.body == SiteData.INDEX_PAGE_NAME) {
//            html.name.cutLast()
//        } else {
//            html.name
//        }
//
//        spcPage(pageName.tokens.joinToString(separator = "/"), html.meta) {
//            htmlData(html)
//        }
//    }
//}

internal fun SiteBuilder.spcPage(
    name: Name,
    more: FlowContent.() -> Unit = {},
) {
    spcPage(name, name, more)
}

context(SiteData, HTML) private fun HTML.spcHome() {
    spcHead()
    body("is-preload") {
        wrapper {
            // Banner
            section("major") {
                id = "banner"
                div("inner") {
                    header("major") {
                        h1 { +"""Scientific Programming Centre""" }
                    }
                    div("content") {
                        p {
                            +"Programming in Science"
                            br {}
                            entity(Entities.nbsp)
                            +"and Science in Programming"
                        }
                        ul("actions") {
                            li {
                                a(classes = "button next scrolly") {
                                    href = "#master"
                                    +"""Activities"""
                                }
                            }
                        }
                    }
                }
            }
            // Main
            div {
                id = "main"
                section {
                    div("inner") {
                        a(href = "https://mipt.ru/education/departments/fpmi/") {
                            span("image left") {
                                img {
                                    src = "images/FPMI.jpg"
                                    alt = "FPMI"
                                    height = "60"
                                    width = "60"
                                }
                            }
                        }
                        p {
                            +"Centre was created in 2022 based on the Phystech School of Applied Mathematics and Informatics at MIPT"
                        }
                    }
                }

                section {
                    div("inner") {
                        header("major") {
                            h2 { +"Science + education + industry" }
                        }
                        p {
                            +"""
                                Our mission is to bring together science, education and industry and
                                work on better software solutions for science and better science in 
                                software development.
                            """.trimIndent()
                        }
//                        ul("actions") {
//                            li {
//                                a(classes = "button next") {
//                                    href = "landing.html"
//                                    +"""Get Started"""
//                                }
//                            }
//                        }
                    }
                }

                section("tiles") {
                    id = "master"
                    article {
                        span("image") {
                            img {
                                src = resolveRef("images/pic01.jpg")
                                alt = ""
                            }
                        }
                        header("major") {
                            h3 {
                                a(classes = "link") {
                                    href = resolveRef("magprog")
                                    +"""Master's program"""
                                }
                            }
                            p { +"""Master's program: "Scientific programming" """ }
                        }
                    }
                    article {
                        span("image") {
                            img {
                                src = resolveRef("images/pic02.jpg")
                                alt = ""
                            }
                        }
                        header("major") {
                            h3 {
                                a(classes = "link") {
                                    href = resolveRef("research")
                                    +"""Research"""
                                }
                            }
                            p {
                                +"""Fundamental and applied research in analysis, scientific software design and data acquisition and control systems."""
                            }
                        }
                    }
                    article {
                        span("image") {
                            img {
                                src = resolveRef("images/pic03.jpg")
                                alt = ""
                            }
                        }
                        header("major") {
                            h3 {
                                a(classes = "link") {
                                    href = resolveRef("consulting")
                                    +"""Consulting"""
                                }
                            }
                            p { +"""Consultations, review and support of scientific software systems.""" }
                        }
                    }
                    article {
                        span("image") {
                            img {
                                src = resolveRef("images/pic04.jpg")
                                alt = ""
                            }
                        }
                        header("major") {
                            h3 {
                                a(classes = "link") {
                                    href = resolveRef("team")
                                    +"""Team"""
                                }
                            }
                        }
                    }
//                    article {
//                        span("image") {
//                            img {
//                                src = "images/pic05.jpg"
//                                alt = ""
//                            }
//                        }
//                        header("major") {
//                            h3 {
//                                a(classes = "link") {
//                                    href = "landing.html"
//                                    +"""Consequat"""
//                                }
//                            }
//                            p { +"""Ipsum dolor sit amet""" }
//                        }
//                    }
//                    article {
//                        span("image") {
//                            img {
//                                src = "images/pic06.jpg"
//                                alt = ""
//                            }
//                        }
//                        header("major") {
//                            h3 {
//                                a(classes = "link") {
//                                    href = "landing.html"
//                                    +"""Etiam"""
//                                }
//                            }
//                            p { +"""Feugiat amet tempus""" }
//                        }
//                    }
                }
            }
        }

        fortyScripts()
    }

}

internal fun Application.spcHome(context: Context, rootPath: Path, prefix: String = "") {

    val snark = context.fetch(SnarkPlugin)

    val homePageContext = snark.readDirectory(rootPath.resolve("content"), prefix)

    routing {
        route(prefix) {
            snarkSite(homePageContext) {
                assetDirectory("assets", rootPath.resolve("assets"))
                assetDirectory("images", rootPath.resolve("images"))

                page { spcHome() }

                pages("consulting", dataRenderer = FortyDataRenderer)
                //pages("ru.consulting".parseAsName(), dataRenderer = FortyDataRenderer)

                spcSpotlight("team") { _, m -> m["type"].string == "team" }
                spcSpotlight("research") { name, m -> name.startsWith("projects".asName()) && m["type"].string == "project" }
            }
        }
    }
}