#Pokemon GO (c) direct Man-in-the-Middle
![pokemon](https://img.shields.io/badge/Pokemon%20GO-0.33.0-blue.svg?style=flat-square")
![pokemon](https://img.shields.io/badge/Pokemon%20GO-0.35.0-red.svg?style=flat-square")
![license](https://img.shields.io/github/license/ELynx/pokemon-go-xposed-mitm.svg)

Module is being field-tested with 0.35.0, so far there are no problems.
##Pre-build version
You can download prebuilt version from [XPosed repository](http://repo.xposed.info/module/com.elynx.pogoxmitm).

If with version 1.2 Stable example hacks are not activated, try version 1.0 Beta. Make sure to turn on Beta modules visibility to see it.

##Brief
Uses XPosed framework to intercept web communications of app and modify out- and inbound packages.

Internally uses POGOProtos compiled to Ruby. See root build.gradle for instructions and build tasks.

##What it can do now
Intercept packages going from client to server; all without breaking actual communications.

Intercept packages going from server to client, too.

Export pokemon data including IV's and attacks to csv located in external storage at `Pokemon/PokemonData.csv`. It is done each time the application starts. You can see toast message during loading screen.

###Branch right-now, as released on XPosed repo
Simple hack to put IVs into pokemon nickname.

Simple hack to put remaining lure time into pokestop description.

UI for turning hacks on and off.

##What is planned
Grandeur plan - use JRuby to allow different data modifications without recompilation.

##Current state
Master branch is undergoing changes related to adding Ruby script execution.

Code for XPosed repo release is in branch right-now.

##Resources and projects used
* [POGOProtos](https://github.com/AeonLucid/POGOProtos) by [Mike](https://github.com/AeonLucid)
* [Launcher icon generator](https://romannurik.github.io/AndroidAssetStudio/index.html) by [Roman Nurik](https://github.com/romannurik)
* [Pokeball icon set](http://tamarinfrog.deviantart.com/art/All-Poke-Balls-Free-Icons-368996730) by [TamarinFrog](http://tamarinfrog.deviantart.com/)
