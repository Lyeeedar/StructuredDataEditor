import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

buildscript {
    extra.set("production", (findProperty("prod") ?: findProperty("production") ?: "false") == "true")
    extra.set("kotlin.version", System.getProperty("kotlinVersion"))
}

plugins {
    val kotlinVersion: String by System.getProperties()
    id("kotlinx-serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
    id("kvision") version kvisionVersion
}

version = "0.0.1-SNAPSHOT"
group = "sde"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
    maven {
        url = uri("https://dl.bintray.com/gbaldeck/kotlin")
        metadataSources {
            mavenPom()
            artifact()
        }
    }
    maven { url = uri("https://dl.bintray.com/rjaros/kotlin") }
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    mavenLocal()
}

// Versions
val kotlinVersion: String by System.getProperties()
val kvisionVersion: String by System.getProperties()
val ktorVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val h2Version: String by project
val pgsqlVersion: String by project
val kweryVersion: String by project
val logbackVersion: String by project
val commonsCodecVersion: String by project
val jdbcNamedParametersVersion: String by project

// Custom Properties
val webDir = file("src/frontendMain/web")
val electronDir = file("src/standaloneMain/electron")
val isProductionBuild = project.extra.get("production") as Boolean
val mainClassName = "io.ktor.server.netty.EngineMain"

kotlin {
    jvm("backend") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
    }
    js("frontend") {
        compilations.all {
            kotlinOptions {
                moduleKind = "commonjs"
                sourceMap = !isProductionBuild
                if (!isProductionBuild) {
                    sourceMapEmbedSources = "always"
                }
            }
        }
        browser {
            runTask {
                outputFileName = "main.bundle.js"
                devServer = KotlinWebpackConfig.DevServer(
                    open = false,
                    port = 3000,
                    proxy = mapOf(
                        "/kv/*" to "http://localhost:8080",
                        "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true)
                    ),
                    contentBase = listOf("$buildDir/processedResources/frontend/main")
                )
            }
            webpackTask {
                outputFileName = "${project.name}-frontend.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
    js("standalone") {
        compilations.all {
            kotlinOptions {
                moduleKind = "commonjs"
                sourceMap = !isProductionBuild
                if (!isProductionBuild) {
                    sourceMapEmbedSources = "always"
                }
            }
        }
        browser {
            runTask {
                outputFileName = "main.bundle.js"
                devServer = KotlinWebpackConfig.DevServer(
                        open = false,
                        port = 3000,
                        proxy = mapOf(
                                "/kv/*" to "http://localhost:8080",
                                "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true)
                        ),
                        contentBase = listOf("$buildDir/processedResources/standalone/main")
                )
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("pl.treksoft:kvision-server-ktor:$kvisionVersion")
            }
            kotlin.srcDir("build/generated-src/common")
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val backendMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(kotlin("reflect"))
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-auth:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("com.h2database:h2:$h2Version")
                implementation("org.jetbrains.exposed:exposed:$exposedVersion")
                implementation("org.postgresql:postgresql:$pgsqlVersion")
                implementation("com.zaxxer:HikariCP:$hikariVersion")
                implementation("commons-codec:commons-codec:$commonsCodecVersion")
                implementation("com.axiomalaska:jdbc-named-parameters:$jdbcNamedParametersVersion")
                implementation("com.github.andrewoma.kwery:core:$kweryVersion")
            }
        }
        val backendTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val frontendMain by getting {
            resources.srcDir(webDir)
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(npm("po2json"))
                implementation(npm("grunt"))
                implementation(npm("grunt-pot"))
                implementation(npm("raw-loader", "^4.0.1"))

                implementation("pl.treksoft:kvision:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-select:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-datetime:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-spinner:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-upload:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-dialog:$kvisionVersion")
                implementation("pl.treksoft:kvision-fontawesome:$kvisionVersion")
                implementation("pl.treksoft:kvision-i18n:$kvisionVersion")
                implementation("pl.treksoft:kvision-richtext:$kvisionVersion")
                implementation("pl.treksoft:kvision-handlebars:$kvisionVersion")
                implementation("pl.treksoft:kvision-datacontainer:$kvisionVersion")
                implementation("pl.treksoft:kvision-redux:$kvisionVersion")
                implementation("pl.treksoft:kvision-chart:$kvisionVersion")
                implementation("pl.treksoft:kvision-tabulator:$kvisionVersion")
                implementation("pl.treksoft:kvision-pace:$kvisionVersion")
                implementation("pl.treksoft:kvision-moment:$kvisionVersion")
                implementation("pl.treksoft:kvision-toast:$kvisionVersion")
            }
            kotlin.srcDir("build/generated-src/frontend")
        }
        val frontendTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("pl.treksoft:kvision-testutils:$kvisionVersion:tests")
            }
        }
        val standaloneMain by getting {
            resources.srcDirs(sourceSets["frontendMain"].resources.srcDirs)
            kotlin.srcDir("src/frontendMain/kotlin")
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(npm("po2json"))
                implementation(npm("grunt"))
                implementation(npm("grunt-pot"))

                implementation(npm("electron", "7.1.9"))
                implementation(npm("electron-builder", "21.2.0"))

                implementation("pl.treksoft:kvision:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-datetime:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-select:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-spinner:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-upload:$kvisionVersion")
                implementation("pl.treksoft:kvision-bootstrap-dialog:$kvisionVersion")
                implementation("pl.treksoft:kvision-fontawesome:$kvisionVersion")
                implementation("pl.treksoft:kvision-i18n:$kvisionVersion")
                implementation("pl.treksoft:kvision-richtext:$kvisionVersion")
                implementation("pl.treksoft:kvision-handlebars:$kvisionVersion")
                implementation("pl.treksoft:kvision-datacontainer:$kvisionVersion")
                implementation("pl.treksoft:kvision-redux:$kvisionVersion")
                implementation("pl.treksoft:kvision-chart:$kvisionVersion")
                implementation("pl.treksoft:kvision-tabulator:$kvisionVersion")
                implementation("pl.treksoft:kvision-pace:$kvisionVersion")
                implementation("pl.treksoft:kvision-moment:$kvisionVersion")
                implementation("pl.treksoft:kvision-electron:$kvisionVersion")
                implementation("pl.treksoft:kvision-toast:$kvisionVersion")
            }
        }
    }
}

fun getNodeJsBinaryExecutable(): String {
    val nodeDir = NodeJsRootPlugin.apply(project).nodeJsSetupTask.destination
    val isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
    val nodeBinDir = if (isWindows) nodeDir else nodeDir.resolve("bin")
    val command = NodeJsRootPlugin.apply(project).nodeCommand
    val finalCommand = if (isWindows && command == "node") "node.exe" else command
    return nodeBinDir.resolve(finalCommand).absolutePath
}

tasks {
    withType<KotlinJsDce> {
        doLast {
            copy {
                file("$buildDir/tmp/expandedArchives/").listFiles()?.forEach {
                    if (it.isDirectory && it.name.startsWith("kvision")) {
                        from(it) {
                            include("css/**")
                            include("img/**")
                            include("js/**")
                        }
                    }
                }
                into(file("${buildDir.path}/js/packages/${project.name}-frontend/kotlin-dce"))
            }
        }
    }
    withType<KotlinJsDce> {
        doLast {
            copy {
                file("$buildDir/tmp/expandedArchives/").listFiles()?.forEach {
                    if (it.isDirectory && it.name.startsWith("kvision")) {
                        from(it) {
                            include("css/**")
                            include("img/**")
                            include("js/**")
                        }
                    }
                }
                into(file("${buildDir.path}/js/packages/${project.name}-standalone/kotlin-dce"))
            }
        }
    }
    create("generateGruntfile") {
        outputs.file("$buildDir/js/Gruntfile.js")
        doLast {
            file("$buildDir/js/Gruntfile.js").run {
                writeText(
                    """
                    module.exports = function (grunt) {
                        grunt.initConfig({
                            pot: {
                                options: {
                                    text_domain: "messages",
                                    dest: "../../src/frontendMain/resources/i18n/",
                                    keywords: ["tr", "ntr:1,2", "gettext", "ngettext:1,2"],
                                    encoding: "UTF-8"
                                },
                                files: {
                                    src: ["../../src/frontendMain/kotlin/**/*.kt"],
                                    expand: true,
                                },
                            }
                        });
                        grunt.loadNpmTasks("grunt-pot");
                    };
                """.trimIndent()
                )
            }
        }
    }
    create("generatePotFile", Exec::class) {
        dependsOn("compileKotlinFrontend", "generateGruntfile")
        workingDir = file("$buildDir/js")
        executable = getNodeJsBinaryExecutable()
        args("$buildDir/js/node_modules/grunt/bin/grunt", "pot")
        inputs.files(kotlin.sourceSets["frontendMain"].kotlin.files)
        outputs.file("$projectDir/src/frontendMain/resources/i18n/messages.pot")
    }
}
afterEvaluate {
    tasks {
        create("cleanWebpackConfig", Delete::class) {
            delete("webpack.config.d")
            group = "build"
        }

	    // Backend
	    getByName("backendProcessResources", Copy::class) {
		    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	    }
	    getByName("backendJar").group = "package"
	    create("backendRun", JavaExec::class) {
		    dependsOn("compileKotlinBackend")
		    group = "run"
		    main = mainClassName
		    classpath =
			    configurations["backendRuntimeClasspath"] + project.tasks["compileKotlinBackend"].outputs.files +
			    project.tasks["backendProcessResources"].outputs.files
		    workingDir = buildDir
	    }
	    getByName("compileKotlinBackend") {
		    dependsOn("compileKotlinMetadata")
	    }

	    // Frontend
        create("copyFrontendWebpackConfig", Copy::class) {
            dependsOn("cleanWebpackConfig")
            group = "build"
            from("frontend.webpack.config.d")
            into("webpack.config.d")
        }
        getByName("frontendBrowserProductionWebpack", KotlinWebpack::class) {
            dependsOn("copyFrontendWebpackConfig")
        }
        getByName("frontendProcessResources", Copy::class) {
            dependsOn("compileKotlinFrontend")
            exclude("**/*.pot")
            doLast("Convert PO to JSON") {
                destinationDir.walkTopDown().filter {
                    it.isFile && it.extension == "po"
                }.forEach {
                    exec {
                        executable = getNodeJsBinaryExecutable()
                        args(
                            "$buildDir/js/node_modules/po2json/bin/po2json",
                            it.absolutePath,
                            "${it.parent}/${it.nameWithoutExtension}.json",
                            "-f",
                            "jed1.x"
                        )
                        println("Converted ${it.name} to ${it.nameWithoutExtension}.json")
                    }
                    it.delete()
                }
                copy {
                    file("$buildDir/tmp/expandedArchives/").listFiles()?.forEach {
                        if (it.isDirectory && it.name.startsWith("kvision")) {
                            val kvmodule = it.name.split("-$kvisionVersion").first()
                            from(it) {
                                include("css/**")
                                include("img/**")
                                include("js/**")
                                if (kvmodule == "kvision") {
                                    into("kvision/$kvisionVersion")
                                } else {
                                    into("kvision-$kvmodule/$kvisionVersion")
                                }
                            }
                        }
                    }
                    into(file(buildDir.path + "/js/packages_imported"))
                }
            }
        }
        create("frontendArchive", Jar::class).apply {
            dependsOn("frontendBrowserProductionWebpack")
            group = "package"
            archiveAppendix.set("frontend")
            val distribution =
                project.tasks.getByName("frontendBrowserProductionWebpack", KotlinWebpack::class).destinationDirectory!!
            from(distribution) {
                include("*.*")
            }
            from(webDir)
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            into("/assets")
            inputs.files(distribution, webDir)
            outputs.file(archiveFile)
            manifest {
                attributes(
                    mapOf(
                        "Implementation-Title" to rootProject.name,
                        "Implementation-Group" to rootProject.group,
                        "Implementation-Version" to rootProject.version,
                        "Timestamp" to System.currentTimeMillis()
                    )
                )
            }
        }
	    getByName("frontendRun").group = "run"
        getByName("compileKotlinFrontend") {
            dependsOn("compileKotlinMetadata")
            dependsOn("copyFrontendWebpackConfig")
        }

	    // Electron
        getByName("standaloneProcessResources", Copy::class) {
            dependsOn("compileKotlinStandalone")
            exclude("**/*.pot")
            doLast("Convert PO to JSON") {
                destinationDir.walkTopDown().filter {
                    it.isFile && it.extension == "po"
                }.forEach {
                    exec {
                        executable = getNodeJsBinaryExecutable()
                        args(
                                "$buildDir/js/node_modules/po2json/bin/po2json",
                                it.absolutePath,
                                "${it.parent}/${it.nameWithoutExtension}.json",
                                "-f",
                                "jed1.x"
                        )
                        println("Converted ${it.name} to ${it.nameWithoutExtension}.json")
                    }
                    it.delete()
                }
                copy {
                    file("$buildDir/tmp/expandedArchives/").listFiles()?.forEach {
                        if (it.isDirectory && it.name.startsWith("kvision")) {
                            val kvmodule = it.name.split("-$kvisionVersion").first()
                            from(it) {
                                include("css/**")
                                include("img/**")
                                include("js/**")
                                if (kvmodule == "kvision") {
                                    into("kvision/$kvisionVersion")
                                } else {
                                    into("kvision-$kvmodule/$kvisionVersion")
                                }
                            }
                        }
                    }
                    into(file(buildDir.path + "/js/packages_imported"))
                }
            }
        }
        create("copyElectronWebpackConfig", Copy::class) {
            dependsOn("cleanWebpackConfig")
            group = "build"
            from("electron.webpack.config.d")
            into("webpack.config.d")
        }
        getByName("standaloneBrowserProductionWebpack", KotlinWebpack::class) {
	        dependsOn("frontendProcessResources")
            dependsOn("copyElectronWebpackConfig")
        }
        create("buildApp", Copy::class) {
            dependsOn("standaloneBrowserProductionWebpack")
            group = "build"
            val distribution =
                    project.tasks.getByName("standaloneBrowserProductionWebpack", KotlinWebpack::class).destinationDirectory
            from(distribution, webDir, electronDir)
            inputs.files(distribution, webDir, electronDir)
            into("$buildDir/dist")
        }
        create("runApp", Exec::class) {
            dependsOn("buildApp")
            group = "run"
            workingDir = file("$buildDir/dist")
            executable = getNodeJsBinaryExecutable()
            args("$buildDir/js/node_modules/electron/cli.js", ".")
        }

	    // package
	    create("jar", Jar::class).apply {
		    dependsOn("frontendArchive", "backendJar")
		    group = "package"
		    manifest {
			    attributes(
				    mapOf(
					    "Implementation-Title" to rootProject.name,
					    "Implementation-Group" to rootProject.group,
					    "Implementation-Version" to rootProject.version,
					    "Timestamp" to System.currentTimeMillis(),
					    "Main-Class" to mainClassName
				         )
			              )
		    }
		    val dependencies = configurations["backendRuntimeClasspath"].filter { it.name.endsWith(".jar") } +
		                       project.tasks["backendJar"].outputs.files +
		                       project.tasks["frontendArchive"].outputs.files
		    dependencies.forEach {
			    if (it.isDirectory) from(it) else from(zipTree(it))
		    }
		    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
		    inputs.files(dependencies)
		    outputs.file(archiveFile)
		    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	    }
	    create("zip", Zip::class) {
		    dependsOn("standaloneBrowserProductionWebpack")
		    group = "package"
		    destinationDirectory.set(file("$buildDir/libs"))
		    val distribution =
			    project.tasks.getByName("standaloneBrowserProductionWebpack", KotlinWebpack::class).destinationDirectory!!
		    from(distribution) {
			    include("*.*")
		    }
		    from(webDir)
		    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		    inputs.files(distribution, webDir)
		    outputs.file(archiveFile)
	    }
        create("bundleApp", Exec::class) {
            dependsOn("buildApp")
            group = "package"
            doFirst {
                val targetDir = file("$buildDir/standalone")
                if (targetDir.exists()) {
                    targetDir.deleteRecursively()
                }
                targetDir.mkdirs()
            }
            workingDir = file("$buildDir/dist")
            executable = getNodeJsBinaryExecutable()
            args("$buildDir/js/node_modules/electron-builder/out/cli/cli.js", "--config")
        }
    }
}
