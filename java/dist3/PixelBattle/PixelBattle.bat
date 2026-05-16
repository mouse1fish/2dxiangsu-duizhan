@echo off
chcp 65001 >nul 2>&1
setlocal

set "JAVAW="

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVAW=%JAVA_HOME%\bin\javaw.exe"
)

if not defined JAVAW (
    for %%P in (javaw.exe) do set "JAVAW=%%~$PATH:P"
)

if not defined JAVAW (
    for %%D in (
        "C:\Program Files\Java\jdk-21"
        "C:\Program Files\Java\jdk-20"
        "C:\Program Files\Java\jdk-19"
        "C:\Program Files\Java\jdk-17"
        "C:\Program Files\Eclipse Adoptium\jdk-21"
        "C:\Program Files\Eclipse Adoptium\jdk-17"
        "C:\Program Files\Microsoft\jdk-21"
        "C:\Program Files\Microsoft\jdk-17"
    ) do (
        if exist %%~D\bin\javaw.exe (
            set "JAVAW=%%~D\bin\javaw.exe"
            goto :found
        )
    )
)

:found
if not defined JAVAW (
    echo ============================================
    echo   PixelBattle - Java not found!
    echo   Please install JDK 17+ and try again.
    echo   Download: https://adoptium.net/
    echo ============================================
    pause
    exit /b 1
)

start "" "%JAVAW%" -Xmx512m -jar "%~dp0PixelBattle.jar"
