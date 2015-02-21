package sake

/** Runtime exception for sake.
*
* Used for errors sake can not recover from.
* (generally, throws out of potential multitasking)
*/
class SakeRuntimeException(message : String)
extends RuntimeException(message)
{
}
