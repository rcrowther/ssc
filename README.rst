===
SSC
===

The Scala documenter
====================
``ssc`` is a Unix/Scala program. It has little use for non-Scala users. It's probably a bad idea to think of converting it for other uses. There are better languages than Scala for this kind of activity.


``ssc`` is good for documenting. It can also help delve round in other people's code or fix your own code (especially if broken). 


``ssc`` runs from the commandline. On that,


List of tasks
-------------
You can tell I'm not cut out for Java - I'm going to tell you what it does and how,

===========  ====================  =============================
 Task         implemented by        ssc tampering
===========  ====================  =============================
find          invoke 'grep'         formatting
findfile      Java code             formatting
tree          invoke 'tree'         formatting
introspect    invoke 'scalap'         -
bytecode      invoke 'javap'          -
repl          invoke 'scala'        (no targeting)
run           invoke 'scala'        targets classnames
doc           invoke 'scaladoc'     targets source folders
vms           invoke 'jps'          formatting
test          invoke 'scalatest'    targets test source folders
jar           invoke 'jar'          targets source folders
===========  ====================  =============================



``ssc`` has been a marsh of coding grief (a corrupt-Java self-activating tool from online documentation with no community?). On that,


The bad and the good
--------------------

:Bad: - *Not for Windows*!
  - Not for cygwin or mingw either
  - only works from commandline
  - not properly built or buildable
  - probably not sorted, as of the time of writing

:Good: - Java half-installable
  - makes documentation from broken code
  - works on several folder structures, can be asked to work on many more
  - offers most of scaladoc's commandline options from it's own commandline (-footer, -noPrefixes etc.)
  - self-documenting. If ``ssc`` can do something, it's in ``ssc -help``
  - installation-specific configuration using a local file
  - uses the Scala and Java libraries you point it at, not the ones it wants to help you with. 
  - It can use tree. tree! :)


Sorry about the Windows situation, but I can't afford a Windows computer. If someone wants to fix this, the code needs a .bat file and output formatting.


Alternatives
=============
If you want a more professional tool (it seems most people are ok), in Scala source is an Ant task, ::

    .../scala-<version>/src/scaladoc/scala/tools/ant/Scaladoc.scala

Personally, I want my documentation right there. Putting the 'I' in ``ant``, ``ant`` ain't ma' thing.


.. figure:: https://raw.githubusercontent.com/rcrowther/ssc/master/text/Screenshot.jpg
    :width: 300 px
    :alt: ssc in a terminal
    :align: center

    ssc looks at itself

How self-regarding.


Install/half-install to the computer
====================================
Needs
-----
Java7 and Scala. Both standard, OpenJDK is fine.

Much of ``ssc`` works from shell invocation. If the host computer has ``graphviz``, ``ssc`` can leverage scaladoc to add inheritance diagrams. ``ssc`` also likes ``grep`` and ``tree``.


Building
---------
I don't like build tools, and have no base to distribute Java .jar files. So building is by hand.

You need Java and Scala.

Download ``ssc``, and navigate a terminal into the top folder. Then, ::

    ./compile

That will send the terminal wild with enthusiasm, at the end of which, you have a compile. It's in the folder/directory named `/build`.

Then make an executable package, ::

    scala -verbose -toolcp build/main/scala -Dsake.runner.home= sake.PackageSake installable

If all runs ok, a folder called `sake-<some.version.name>` should appear inside the folder. It has a `/bin` and a `/lib` in it. You can half-install it.


Reminder instructions for a half-install
----------------------------------------
*Linux/Unix only*

Find or make `.bashrc`, usually in your home folder.

Add or append the path to the `/bin` (note the lack of spaces. ``bash`` is not fond of spaces) e.g. ::

    PATH="$PATH:/home/myName/.../sake-<version>/bin"

and ensure this line is at the bottom, ::

    export path

Restart ``bash`` config in every terminal you want to use (new terminals load automatically), ::
 
    source ~/.bashrc

or restart the whole computer (in the middle of development, that's a pain).


Adjust the script
-----------------
The ``ssc`` script needs pointing at a Scala distribution folder, at least. Maybe a Java one too.

The launching script has been written to make this as easy as I can make it. Goto `bin/ssc`. At the top are a few annotated variables, notably,

JAVA_HOME=""

SCALA_HOME=""

In a common setup, Java is installed to the computer, so JAVA_HOME does not need changing. If you have an up-to-date Java, or would like to use a Java inside an IDE, point JAVA_HOME at the `/bin` folder.

SCALA_HOME must be pointed at a Scala distribution folder. ``ssc`` will not work with installed Scala.


Libraries
---------
``ssc`` needs to know, or is helped, by knowing the libraries the code uses. By default, ``ssc`` looks in `lib/` then `Lib/`, so you may be fine. If not, override with a `build.ssc` file (see below) e.g. with this line, ::

    libFiles = /path/to/my/library


Using SSC
=========
Quick try
---------
I hate installation. If you've go this far, try this.

Make an empty folder. Navigate in with a terminal. Run, ::

    ssc

Nothing much should happen. Nothing is good (complaints are bad).

Now put a Scala file in there. Or two. Or a bit of a Scala project. Try, ::

    ssc

If ``ssc`` recognises anything in the folder, it will try to produce documentation. By default, it will,

- Look in several likely places, such as `src/main/scala`, and the top folder 
- Make necessary folders
- Produce documentation whatever the broken state of the code


A word about folder structures
------------------------------
``ssc`` can not handle any folder structure, but it can handle many variations. The rule is, source directory paths must not occupy another source path. So this is bad, ::

    ── src
       └── test

because /src is on the path of /test. This is bad, also, ::

    ── src
       └── main ── scala ── test
 
/src is still on the path of /test.

This is ok, ::

    ── src
       └── doc

.../doc is not a source folder (no tests, no Java).

This is ok, ::

    ── src
       ├── scala
       └── test

``ssc`` here favours intelligence over configuration or convention.


Where to find what can be changed
---------------------------------
Type, ::

    ssc -config

to see what can be changed. If it's in the list, it can be on the commandline. Or in a `build.ssc` file (see below).

Or look in the source code for the class ``CLSchema``, which is messy but definitive.
 

Commandline
-------------
``ssc`` is a commandline tool, so prints nothing but errors. A useful commandline option, ::

    ssc -verbose <task>

See all the options, ::

    ssc -help

Force strict `maven` folder usage, ::

    ssc -maven <task>

And this, ::

     ssc -meter buzz <task>

Waiting for Scala on my computers is real dull. This cheers me up.

And several more. I havn't decided which options to keep yet. Best say, "subject to alteration".


Commandline format
------------------
``ssc`` commands have this format/usage, ::

    ssc <switches> <task>

Every modification is a switch, even destinations. To send documentation to a different folder (overriding the default and `build.ssc` modifications), ::

    ssc -docDir docs/myDifferentlyNamedDocFolder doc

not, ::

    ssc doc docs/myDifferentlyNamedDocFolder

So, "Everything is a switch".


build.ssc
---------
This file can be created and placed anywhere you'd like to override ``ssc`` configuration. 

If ``ssc`` is run in a folder with a `build.ssc` file, it reads the file and adds configuration it finds there to the default.

Note that commandline options override a `build.ssc` file. So, ::

   config = default + build.ssc (if it exists) + commandline options

Any configuration option added to this file overrides default values e.g. ::

    # Build file for SSC

    # Set project data,
    # and make always verbose 
    [project]
    name = "ssc_app"
    version = "6.0"
    verbose = true

    # Insist on diagrams for documentation
    [doc]
    diagrams = true


`build.ssc` file format is a dead boring .ini file. It can stand you typing with a bandaged hand.


Other commands
----------------
With the same intention of explaining what is going on, try this, ::

    ssc -classnames <some-package-qualified-classnames> introspect

which runs 'scalap' on a class.

This command, ::

    ssc -classnames <some-package-qualified-classnames> bytecode

will deliver the guts of the JVM.


Finale
======
That's it.
