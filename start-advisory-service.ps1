# Start Advisory Service on Port 8082
$serviceDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $serviceDir
$Host.UI.RawUI.WindowTitle = "Advisory Service (Port 8082)"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
& "C:\Program Files\Java\jdk-21\bin\java.exe" -jar "$serviceDir\advisory-service\target\advisory-service-0.0.1-SNAPSHOT.jar"
