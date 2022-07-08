The current state of the project:
- Gradle plugin support is delayed for the time being, the probable solution
for the linking problem is to build the library for different kotlin plugin versions.
- "core" project now contains functional tests that demonstrate the functionality of the code generation
utility
- Added settings.json file which contains the settings for the dtest, such as: imports, parent classes,
test annotation
- Added a utility component to compare trees ignoring most commonly occurred insignificant differences:
whitespaces, public modifiers, Unit return type, imports ordering, empty primary constructors
