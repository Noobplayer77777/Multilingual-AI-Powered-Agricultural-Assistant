# Start Query Service on Port 8081
$serviceDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $serviceDir
$Host.UI.RawUI.WindowTitle = "Query Service (Port 8081)"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
& "C:\Program Files\Java\jdk-21\bin\java.exe" -jar "$serviceDir\query-service\target\query-service-0.0.1-SNAPSHOT.jar"
