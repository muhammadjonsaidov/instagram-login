@echo off
echo Starting Instagram Business Discovery Application...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven 3.6 or higher
    pause
    exit /b 1
)

echo Checking environment variables...
if "%FACEBOOK_CLIENT_ID%"=="" (
    echo Warning: FACEBOOK_CLIENT_ID environment variable is not set
    echo Please set your Facebook App ID in .env file
)

if "%FACEBOOK_CLIENT_SECRET%"=="" (
    echo Warning: FACEBOOK_CLIENT_SECRET environment variable is not set
    echo Please set your Facebook App Secret in .env file
)

echo.
echo Building and starting the application...
echo This may take a few minutes on first run...
echo.

REM Build and run the application
mvn spring-boot:run

pause