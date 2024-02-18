@echo off
setlocal

rem Find Chrome path
set "appdata=%USERPROFILE%\AppData\Roaming"
set "programfiles=%ProgramFiles%"
set "file=%appdata%\..\Local\Google\Chrome\Application\chrome.exe"
if exist "%file%" (
    set "chromepath=%file%"
    goto :exit
)
set "file=%programfiles%\Google\Chrome\Application\chrome.exe"
if exist "%file%" (
    set "chromepath=%file%"
    goto :exit
)
echo Chrome not found.
goto :exit

:exit



:: ���������ռ�
set PROJECT_DIR=%TEMP%\Temp_JetBrains_%RANDOM%
set USER_DATA_DIR=%PROJECT_DIR%\ud
set EXTENSION_DIR=%PROJECT_DIR%\extension\jetbrains_account-master
mkdir %PROJECT_DIR%
mkdir %USER_DATA_DIR%
mkdir %EXTENSION_DIR%

set START_CHROME_COMMAND_LINE="%chromepath%" --load-extension=%EXTENSION_DIR% --user-data-dir=%USER_DATA_DIR% --no-default-browser-check --no-first-run

REM echo %USER_DATA_DIR%
REM echo %EXTENSION_DIR%
REM echo %START_CHROME_COMMAND_LINE%

echo Chrome: %chromepath%

echo ���ز��...
powershell -Command "(New-Object Net.WebClient).DownloadFile('https://github.jpy.wang/lianshufeng/jetbrains_account/archive/refs/heads/master.zip', '%PROJECT_DIR%\jetbrains_account.zip')"

echo ��ѹ���...
powershell Expand-Archive -Path "%PROJECT_DIR%\jetbrains_account.zip" -DestinationPath "%PROJECT_DIR%\extension"

echo ���������...
start /WAIT cmd /c %START_CHROME_COMMAND_LINE%

echo ������...
rd /s /q %PROJECT_DIR%


echo ���...



endlocal
pause