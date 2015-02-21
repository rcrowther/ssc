package sake.support.file

import java.nio.file.Path
import java.io.File

class StringOps(val v:String) 
extends AnyVal
{
def toPath: Path = new File(v).toPath
}



