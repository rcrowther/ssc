package ssc

import java.nio.file.Path


/** Carries data on a path location to be used for actions.
  *
  * Projects can carry references to other projects, so can be a
  * tree node.
  *
  * @param cwd the location the project is working from. This value
  *  is used extensively when ssc acts upon project information.
  * @param projectConfig the config the project will use. This will
  *   often be overlaid with values expressed on the commandline.
  * @param dependancies the projects this project is dependant upon.
  *  This data is carried in `projectConfig`, but is exposed for
  *  easy access.
  */
final class Project(
  val cwd: Path,
  val projectConfig: ConfigMap,
  val dependancies: Seq[Project]
)
{

  def addStringTree(b: StringBuilder, depth: Int)
      : StringBuilder =
  {
    b ++=  " " * depth
    b append cwd
    b ++= "\n"
    dependancies.foreach{ d =>
      d.addStringTree(b, depth + 2)
    }
    b
  }

  def addString(b: StringBuilder)
      : StringBuilder =
  {
    b append cwd
  }

  def toStringTree()
      : String =
  {
    val b = new StringBuilder()
    addStringTree(b, 0)
    b.result()
  }

  override def toString()
      : String =
  {
    addString(new StringBuilder("Project(")).result() + ')'
  }

}//Project



object Project{
  def apply(
    cwd: Path,
    projectConfig: ConfigMap,
    dependancies: Seq[Project]
  )
      : Project =
  {
    new Project(
      cwd,
      projectConfig,
      dependancies
    )
  }

}//Project
