$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path $PSScriptRoot).Path
if (-not (Test-Path (Join-Path $projectRoot "gradlew.bat"))) {
    throw "Execute este script na raiz do projeto Android."
}

# The Gradle test worker can fail to load classes when the checkout path has
# non-ASCII characters. A SUBST drive gives it a temporary ASCII path.
$driveLetter = @("Z", "Y", "X", "W", "V", "U", "T", "S", "R", "Q", "P", "O", "N", "M", "L", "K", "J", "I", "H", "G", "F", "E", "D") |
    Where-Object { -not (Get-PSDrive -Name $_ -ErrorAction SilentlyContinue) } |
    Select-Object -First 1

if (-not $driveLetter) {
    throw "No free drive letter is available for the temporary test path."
}

$drive = "${driveLetter}:"
& subst $drive $projectRoot
if ($LASTEXITCODE -ne 0) {
    throw "Could not prepare the temporary test path."
}

$exitCode = 1
Push-Location "$drive\"
try {
    & .\gradlew.bat --no-daemon testDebugUnitTest @args
    $exitCode = $LASTEXITCODE
}
finally {
    Pop-Location
    & subst $drive /D
}

exit $exitCode
