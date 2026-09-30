@echo off
cd /d "%~dp0"
echo.
echo  Joining the APK...
if exist royal-test.apk del royal-test.apk
copy /b apk.part00+apk.part01+apk.part02 royal-test.apk >nul
for %%A in (royal-test.apk) do echo   royal-test.apk  %%~zA bytes
echo.
echo  Applying phase30.bundle...
git checkout feature/phase5-polish
git fetch phase30.bundle feature/phase5-polish
git reset --hard FETCH_HEAD
echo.
echo  ============================================
echo   HEAD is now:
git --no-pager log --oneline -1
echo.
echo   Expected: bc79a37
echo  ============================================
echo.
pause
