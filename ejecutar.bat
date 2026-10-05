@echo off
setlocal
cd /d "%~dp0"
set "AYC_JAVA_BIN="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "AYC_JAVA_BIN=%JAVA_HOME%\bin\"
if not defined AYC_JAVA_BIN if exist "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\javac.exe" set "AYC_JAVA_BIN=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\"
if not exist out mkdir out
"%AYC_JAVA_BIN%javac.exe" -encoding UTF-8 -d out src\*.java
if errorlevel 1 (
    echo No se pudo compilar. Verifique el JDK o la variable JAVA_HOME.
    pause
    exit /b 1
)
"%AYC_JAVA_BIN%java.exe" -cp out Main
if errorlevel 1 (
    pause
    exit /b 1
)
endlocal
