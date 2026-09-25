@echo off
rem Runs the ShopFlow JMeter plan headless (CLI mode) and builds the HTML dashboard report.
rem   load-tests\jmeter\run-jmeter.cmd
rem   load-tests\jmeter\run-jmeter.cmd -JorderRate=20 -JdurationMin=10
rem Properties: host, port, browseRate, historyRate, orderRate, reportRate, durationMin, rampSec
setlocal
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set STAMP=%%i
set OUT=%~dp0..\..\build\jmeter\%STAMP%
mkdir "%OUT%" 2>nul
call "%USERPROFILE%\tools\jmeter\bin\jmeter.bat" -n -t "%~dp0shopflow.jmx" -l "%OUT%\results.jtl" -j "%OUT%\jmeter.log" -e -o "%OUT%\report" %*
echo.
echo HTML report: %OUT%\report\index.html
