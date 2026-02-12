@echo off
echo ================================================
echo Компіляція та запуск LibraryApp
echo ================================================
echo.

REM Перевірка наявності драйвера
if not exist sqlite-jdbc-3.45.0.0.jar (
    echo ПОМИЛКА: Не знайдено файл sqlite-jdbc-3.45.0.0.jar
    echo.
    echo Будь ласка, завантажте правильний драйвер:
    echo https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.0.0/sqlite-jdbc-3.45.0.0.jar
    echo.
    echo Покладіть його в цю папку і запустіть скрипт знову.
    pause
    exit /b 1
)

echo Компіляція...
javac -cp ".;sqlite-jdbc-3.45.0.0.jar" -encoding UTF-8 LibraryApp.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo ПОМИЛКА: Компіляція не вдалася!
    pause
    exit /b 1
)

echo Компіляція успішна!
echo.
echo Запуск програми...
echo.

javac -cp ".;sqlite-jdbc-3.45.0.0.jar;slf4j-api-2.0.9.jar;slf4j-simple-2.0.9.jar" -encoding UTF-8 LibraryApp.java

java -cp ".;sqlite-jdbc-3.45.0.0.jar;slf4j-api-2.0.9.jar;slf4j-simple-2.0.9.jar" LibraryApp

if %ERRORLEVEL% neq 0 (
    echo.
    echo ПОМИЛКА: Програма завершилася з помилкою!
    pause
    exit /b 1
)

echo.
echo Програма завершена.
pause