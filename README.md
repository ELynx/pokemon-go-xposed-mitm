#Pokemon GO (c) direct Man-in-the-Middle
![pokemon](https://img.shields.io/badge/Pokemon%20GO-0.35.0-blue.svg?style=flat-square")
![license](https://img.shields.io/github/license/ELynx/pokemon-go-xposed-mitm.svg)

Consider module field-tested with 0.35.0.
##Pre-build version
You can download prebuilt version from [XPosed repository](http://repo.xposed.info/module/com.elynx.pogoxmitm).

<b>Make sure that app have Storage Permissions</b>. App uses them to store settings. If you experience crashes of UI, first check permissions. If it still crashes, please provide feedback via Issues.

All hacks implemented now don't modify data sent to Niantic servers, only examine it. They modify data going back, to add gist to it.
This hack don't do additional request, so it does not overload Niantic servser. No 'data scraping'.

I had not heard/read about bans for this or similar modules. If I ever know of such, I will inform users wherever I can and pause distribution.

##Brief
Uses XPosed framework to intercept web communications of app and process out- and inbound packages.

Internally use [POGOProtos](https://github.com/AeonLucid/POGOProtos). See root build.gradle for instructions and build tasks.

##What it can do now
Intercept packages going from client to server; all without breaking actual communications.

Intercept packages going from server to client, too.

###Branch right-now, as released on XPosed repo
Simple hack to put IVs into pokemon nickname.

Simple hack to put remaining lure time into pokestop description.

Export pokemon data including IV's and attacks to tsv file located in external storage at `Pokemon/PokemonData.tsv`.
It is done each time the application starts. You can see toast message with confirmation during loading screen.
The data can be later used for calculations. For example, like in the [Pokemon Evolution Calculator](https://docs.google.com/spreadsheets/d/1vlEsToajcid9KTkLzgqZCpji8bDVEpMM6GQ8SqNL4-k/edit?usp=sharing)

UI for turning hacks on and off.

##What is planned
Grandeur plan - use JRuby to allow different data modifications without recompilation.

##Current state
Master branch is undergoing changes related to adding Ruby script execution. Unstable development is in branch jruby.

Code for XPosed repo release is in branch right-now.

##Resources and projects used
* [POGOProtos](https://github.com/AeonLucid/POGOProtos) by [Mike](https://github.com/AeonLucid)
* [Launcher icon generator](https://romannurik.github.io/AndroidAssetStudio/index.html) by [Roman Nurik](https://github.com/romannurik)
* [Pokeball icon set](http://tamarinfrog.deviantart.com/art/All-Poke-Balls-Free-Icons-368996730) by [TamarinFrog](http://tamarinfrog.deviantart.com/)
* [Xposed module for Jodel](https://github.com/krokofant/JodelXposed) by [krokofant](https://github.com/krokofant/JodelXposed)
