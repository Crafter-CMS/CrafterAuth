Write-Host ""
Write-Host "CrafterAuth Build & Deploy Script" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

$VelocityPlugins = "C:\Users\Bora\Desktop\Velocity\plugins"

Write-Host "[1/3] Eski build dosyalari temizleniyor..." -ForegroundColor Blue
& ./gradlew clean --no-daemon

if ($LASTEXITCODE -ne 0) {
    Write-Host "Clean islemi basarisiz!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[2/3] Proje build ediliyor (shadowJar)..." -ForegroundColor Blue
& ./gradlew shadowJar --no-daemon

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build islemi basarisiz!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[3/3] Jar dosyasi Velocity plugins klasorune kopyalaniyor..." -ForegroundColor Blue

$JarFile = Get-ChildItem -Path "build\libs\LimboAuth-*.jar" -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $JarFile) {
    Write-Host "Jar dosyasi bulunamadi!" -ForegroundColor Red
    exit 1
}

$Version = $JarFile.Name -replace 'LimboAuth-(.*)\.jar', '$1'

if (-not (Test-Path $VelocityPlugins)) {
    Write-Host "Velocity plugins klasoru olusturuluyor..." -ForegroundColor Yellow
    New-Item -Path $VelocityPlugins -ItemType Directory -Force | Out-Null
}

Get-ChildItem -Path $VelocityPlugins -Filter "CrafterAuth-*.jar" | Remove-Item -Force -ErrorAction SilentlyContinue

Copy-Item -Path $JarFile.FullName -Destination "$VelocityPlugins\CrafterAuth-$Version.jar" -Force

if ($?) {
    Write-Host ""
    Write-Host "Build basariyla tamamlandi!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Build: build\libs\LimboAuth-$Version.jar" -ForegroundColor Green
    Write-Host "Hedef: $VelocityPlugins\CrafterAuth-$Version.jar" -ForegroundColor Green
    Write-Host "Versiyon: $Version" -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host "Kopyalama islemi basarisiz!" -ForegroundColor Red
    exit 1
}

Write-Host "Tum islemler tamamlandi!" -ForegroundColor Green
Write-Host ""
