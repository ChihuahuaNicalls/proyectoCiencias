# Run the JavaFX app via Maven with JVM args to enable native access
# Usage: .\run-app.ps1
# This sets MAVEN_OPTS so both the Maven process and the forked JVM allow native access,
# which avoids the restricted-method warnings on recent JDKs.
$env:MAVEN_OPTS = "--enable-native-access=ALL-UNNAMED --enable-native-access=javafx.graphics"
$mvn = "mvn"
Write-Host "Running: $mvn javafx:run (MAVEN_OPTS=$env:MAVEN_OPTS)"
& $mvn "javafx:run"