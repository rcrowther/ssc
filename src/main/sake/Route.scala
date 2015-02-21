package sake

class Route(val v: String) extends AnyRef
{

}//Route

object Route {
protected val emptyThing : Route = new Route("empty route")
def empty: Route = emptyThing

implicit def toBase (route: Route) : String = route.v
implicit def toRoute(str: String): Route = new Route(str)
}
