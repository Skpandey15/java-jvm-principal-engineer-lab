@echo off
rem Starts payment-stub and ShopFlow (order-service) in their own windows, from a COPY of the jars in build\run.
rem Running from a copy means rebuilding (gradlew build) can never replace the jar under a running JVM.
rem   scripts\start-shopflow.cmd                       healthy
rem   scripts\start-shopflow.cmd --lab.incident=inc-002 with an incident switched on
rem Close the two windows (or Ctrl+C in them) to stop. Requires: docker compose platform up, JAVA_HOME = JDK 25.
setlocal
set ROOT=%~dp0..
call "%ROOT%\gradlew.bat" -q -p "%ROOT%" :reference-service:order-service:bootJar :reference-service:payment-stub:bootJar || exit /b 1
if not exist "%ROOT%\build\run" mkdir "%ROOT%\build\run"
copy /y "%ROOT%\reference-service\order-service\build\libs\order-service-0.1.0.jar" "%ROOT%\build\run\order-service.jar" >nul
copy /y "%ROOT%\reference-service\payment-stub\build\libs\payment-stub-0.1.0.jar" "%ROOT%\build\run\payment-stub.jar" >nul

start "payment-stub :18081" "%JAVA_HOME%\bin\java" -Duser.timezone=UTC -jar "%ROOT%\build\run\payment-stub.jar"
start "ShopFlow :18080" "%JAVA_HOME%\bin\java" -Xmx512m -Duser.timezone=UTC -XX:+ExitOnOutOfMemoryError ^
  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath="%ROOT%\build\run" ^
  "-Xlog:gc*:file=%ROOT%\build\run\gc.log:time,uptime,level,tags" ^
  -jar "%ROOT%\build\run\order-service.jar" %*

echo Starting... ShopFlow on http://localhost:18080 (ready when /actuator/health/readiness is UP)
