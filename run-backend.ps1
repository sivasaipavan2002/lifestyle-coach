# Compiles and runs the WebServer (backend API + serves frontend/dist if built).
# Usage: ./run-backend.ps1 [-Port 8080]

param(
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

$repo = "$env:USERPROFILE\.m2\repository"
$classpath = (Get-ChildItem -Path $repo -Recurse -Filter *.jar | ForEach-Object { $_.FullName }) -join ";"

$out = Join-Path $env:TEMP "lifestylecoach-build"
New-Item -ItemType Directory -Force -Path $out | Out-Null

$sources = Get-ChildItem -Path "src/main/java" -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$sourcesFile = Join-Path $out "sources.txt"
[System.IO.File]::WriteAllLines($sourcesFile, $sources, [System.Text.UTF8Encoding]::new($false))

Write-Host "Compiling..."
javac -encoding UTF-8 -cp $classpath -d $out "@$sourcesFile"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed."
    exit $LASTEXITCODE
}

Write-Host "Starting WebServer on port $Port ..."
$env:WEB_PORT = $Port
java -cp "$out;$classpath" com.lifestylecoach.web.WebServer
