#Pokemon GO (c) direct Man-in-the-Middle
##Pre-build version
You can download prebuilt version from [XPosed repository](http://repo.xposed.info/module/com.elynx.pogoxmitm)

Make sure to turn on Beta modules visibility to see it.
##Brief
Uses XPosed framework to intercept web communications of app and modify out- and inbound packages.

Internally uses POGOProtos compiled to Java. See root build.gradle for instructions and build tasks.

##What it can do now
Intercept packages going from client to server, parse them with protobuf; all without breaking actual communications.

Intercept packages going from server to client, and parse them too, with remembered request types.

Simple POC hack to put IVs into pokemon nickname.
##What is planned
Grandeur plan - use JRuby to allow different data modifications without recompilation.
##Resources and projects used
* [POGOProtos](https://github.com/AeonLucid/POGOProtos) by [Mike](https://github.com/AeonLucid)
* [Launcher icon generator](https://romannurik.github.io/AndroidAssetStudio/index.html) by [Roman Nurik](https://github.com/romannurik)
* [Pokeball icon set](http://tamarinfrog.deviantart.com/art/All-Poke-Balls-Free-Icons-368996730) by [TamarinFrog](http://tamarinfrog.deviantart.com/)
