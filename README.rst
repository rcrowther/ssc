===
SSC
===

The Scala documenter
====================
``ssc`` is a Scala program. It has little use for non-Scala users. It's probably a bad idea to think of converting it for other uses. There are better languages than Scala for this kind of activity.

``ssc`` is for documenting Scala code. Which means it leverages ``scaladoc``. ``scaladoc`` is an amazing tool. It's good for programming, yet I have only seen one web post on it. I tired of trying to leverage it's abilities, hence, ``ssc``.

``ssc`` runs from the commandline. On that,


The bad and the good
--------------------

:Bad: - *Not for Windows*!
  - only works from commandline
  - not properly built or buildable
  - no JLine means no fsc
  - probably not sorted, as of the time of writing

:Good: - Java half-installable
  - makes documentation out of broken code
  - works on many folder structures, can be asked to work on any
  - offers most of scaladoc's commandline options from it's own commandline (-footer, -noPrefixes etc.)
  - installation-specific configuration using a local file
  - no JLine/FSC means more memory for everything else.

Sorry about the Windows situation, but I can't afford a Windows computer. If someone wants to fix this, the code needs a .bat file, and output formatting.

I wanted it, otherwise I wouldn't have coded it, but other people may not be interested.


Alternatives
=============
If you want a better, more professional tool (it seems most people are ok), in Scala source is an Ant task, ::

    /home/rob/CodeReference/scala-2.11.0-M4/src/scaladoc/scala/tools/ant

Personally, I want my documentation right there. Putting the 'I' in ``ant``, ``ant`` ain't ma' thing.



Install/half-install to the computer
====================================
Building
---------
I don't like build tools, and have no base to distribute Java .jar files. So building is by hand.

You need Java (OpenJDK no problem, probably works on any Java7+) and Scala.

Download ``ssc``, and navigate a terminal into the top folder. Then, ::

    mkdir build/main
    mkdir build/main/scala
    scalac -verbose @scalacArgs

That will send the terminal wild with enthusiasm, at the end of which, you have a compile. It's in the folder/directory named `sake`.

Then make executable .jar files from the code, ::

    scala -verbose -toolcp build/main/scala -Dsake.runner.home= sake.PackageSake installable

If all runs ok, a folder called `sake-<some.version.name>` should appear inside the folder. It has a `/bin` and a `/lib` in it. You can half-install it.


Reminder instructions for a half-install
----------------------------------------
*Linux/Unix only*

Move the entire `/bin` folder to wherever you keep Java code, and put them on your computer's path.

Find or make `.bashrc`, usually in your home folder.

Add or amend the path to the `/bin` (note the lack of spaces. ``bash`` is not fond of spaces) e.g. ::

    PATH="$PATH:/home/myName/.../sake-<version>/bin"

and ensure this line is at the bottom, ::

    export path

Restart ``bash`` config in every terminal you want to use (new terminals load automatically), ::
 
    source ~/.bashrc

or restart the whole computer, but in the middle of development, that's a pain.


Adjust the script
-----------------
If Scala and Java are fully installed, ``ssc`` may work now. However, most Java users have a muddle of JDK/JVMs/Scala installations. If development is on `Eclipse`, you have caos, as `Eclipse` will have it's own Versions of Scala, and Scalas and Javas are everywhere.

The launching script has been made to make this easy. Goto `bin/ssc`. At the top are a few annotated variables, notably,

JAVA_HOME=""
SCALA_HOME=""

In a common setup, Java is installed to the computer, so JAVA_HOME does not need changing. It can be pointed at any other Java `/bin` on the computer, though. For example, you may have a very up-to-date Java to replace packaging, or a Java inside an IDE like Eclipse. Point JAVA_HOME at the `/bin` folder.

The same is true of Scala, and SCALA_HOME. However, t is much more likely that Scala is not fully installed to the computer, and SCALA_HOME must be pointed at it's `/bin` folder.


Libraries
---------
One bad start is lost dependencies for the code to be documented. ``ssc`` needs to know about libraries the code uses, as it does a kind of compile to make the documentation. By default, ``ssc`` looks in `lib/` then `Lib/`, so you may be fine. If not, make a `build.ssc` file (see below) to override, e.g. with this line, ::

    libFiles = /path/to/my/library


Using SSC
=========
Quick try
---------
I hate installation. If you've go this far, try this.

Make an empty folder. Navigate in with a terminal. Run, ::

    ssc

Nothing should happen. Nothing is good (complaints are bad).

Now put a Scala file in there. Or two. Or a bit of a Scala project. Try, ::

    ssc

If ``ssc`` recognises anything in the folder, it will try to produce documentation. By default, it will try to,

- Look in several likely places, such as `src/main/scala`, and the top folder 
- Make any necessary folders
- Produce documentation whatever the broken state of the code


Where to find what can be changed
---------------------------------
Type, ::

    ssc -config

to see what can be changed. If it's in the list, it can be on the commandline. Or in a `build.ssc` file (see below).

Or look at the source code for the class ``Configuration``, which is definitive.
 

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

Every modification is a switch, even destinations. To send documentation to different folder (overriding the default and any `build.ssc` modifications), ::

    ssc -docDir docs/myDifferentlyNamedDocFolder doc

not, ::

    ssc doc docs/myDifferentlyNamedDocFolder

Worth remembering.


build.ssc
---------
This file can be invented and put into someplace you'd like to override ``ssc`` configuration. 

If ``ssc`` is run in a folder with a `build.ssc` file, it reads the file and adds any configuration it finds there to the default.

Note that commandline options override a `build.ssc` file. So, ::

   config = default + build.ssc (if it exists) + commandline options

Any configuration option added to this file overrides the default values e.g. ::

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
With the same intention of explaining what is going on, try this command, ::

    ssc -classnames <some-package-qualified-classnames> introspect

which runs 'scalap' on a class.

This command, ::

    ssc -classnames <some-package-qualified-classnames> bytecode

will deliver the guts of the JVM.


Finale
======
That's it.
