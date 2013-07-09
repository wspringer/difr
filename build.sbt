import AssemblyKeys._
import java.io.File
import sbt.IO._
import ScalateKeys._

seq(scalateSettings:_*)

scalateTemplateConfig in Compile <<= (sourceDirectory in Compile) { base =>
  Seq(
    TemplateConfig(base / "resources", Seq.empty, Seq.empty, Some(""))
  )
}

defaultExcludes in Compile in unmanagedResources := "*.scaml"

assemblySettings

libraryDependencies ++= Seq(
  "org.fusesource.scalate" % "scalate-core" % "1.5.3",
  "org.slf4j" % "slf4j-api" % "1.7.0",
  "com.dadrox" % "quiet-slf4j" % "0.1",
  "org.rogach" %% "scallop" % "0.9.3"
)

name := "difr"

version := "0.1-SNAPSHOT"

jarName in assembly := "difr.jar"

mainClass in assembly := Some("nl.flotsam.difr.Tool")

resourceGenerators in Compile <+= (resourceManaged in Compile, resourceDirectory in Compile) map { (outDir, inDir) =>
    import net.nczonline.web.cssembed.CSSEmbed
    import com.yahoo.platform.yui.compressor.YUICompressor
    val original = inDir / "style.css"
    val target = outDir / "style-compressed.css"
    withTemporaryFile("style", ".css") {
        embedded =>
            val embedArgs = Array("-o", embedded.toString, original.toString)
            CSSEmbed.main(embedArgs)
            val candidates = List(
                (inDir / "editor.js") -> (outDir / "editor-min.js"),
                (embedded) -> (outDir / "style-min.css")
            )
            for ((from, to) <- candidates) yield {
                to.getParentFile().mkdirs()
                YUICompressor.main(Array("-o", to.toString, from.toString))
            }
            // For some reason, mapping to _._2 doesn't compile.
            Seq(candidates(0)._2, candidates(1)._2)
    }
}
