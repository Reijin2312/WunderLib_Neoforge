[![](https://jitpack.io/v/quiqueck/WunderLib.svg)](https://jitpack.io/#quiqueck/WunderLib)

# WunderLib

WunderLib is a library mod mainly focused on UI and Math, MC 1.21.11

## Importing:

You can easily include WunderLib as an internal Dependency by adding the following to your `build.gradle`:

```
repositories {
    ...
    maven { url 'https://jitpack.io' } 
}
```

```
dependencies {
    ...
    implementation "de.ambertation:wunderlib:${project.wunderlib_version}"
}
```

You should also add a dependency to `neoforge.mods.toml`. WunderLib uses Semantic versioning, so adding the dependency as
follows should respect that and ensure that your mod is not loaded with an incompatible version of WunderLib:

```
[[dependencies.${mod_id}]]
modId="wunderlib"
mandatory=true
versionRange="[1.0,)"
ordering="NONE"
side="BOTH"
```

In this example `1.0.1` is the WunderLib Version you are building against.

## Building:

* Clone repo
* Run command line in folder: gradlew build
* Mod .jar will be in ./build/libs
