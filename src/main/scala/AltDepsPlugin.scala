import sbt._
import Keys._

object AltDependency {
  implicit def p2t(p: Project): T = new T(p)
  class T(project: Project) {
    def dependsOnAlt(alt: AltDependency): Project = alt(project)
  }

  trait AltDependency extends (Project => Project) { outer => 
    def dependsOnAlt(additional: AltDependency): AltDependency = new AltDependency {
      override def apply(p: Project): Project = additional(outer(p))
    }
  }

  case class GitAltDependency(buildBase: java.io.File, localDependencyRoot: java.io.File, git: ProjectReference) extends AltDependency {
    override def apply(p: Project): Project = {
      val resolved = if (localDependencyRoot.isAbsolute) localDependencyRoot else new java.io.File(buildBase, localDependencyRoot.getPath)

      p dependsOn (if(resolved.isDirectory) RootProject(resolved) else git)
    }
  }

  case class ArtifactAltDependency(buildBase: java.io.File, localDependencyRoot: java.io.File, dep: ModuleID) extends AltDependency {
    override def apply(p: Project): Project = {
     val resolved = if (localDependencyRoot.isAbsolute) localDependencyRoot else new java.io.File(buildBase, localDependencyRoot.getPath)

     if (resolved.isDirectory) p dependsOn ( RootProject(resolved) )
     else                      p settings  ( libraryDependencies += dep )
    }
  }
}

// vim: set ts=4 sw=4 et:
