@ECHO OFF
SETLOCAL
FOR %%I IN ("%~dp0.") DO SET "BASEDIR=%%~fI"
SET "JAVA_CMD=java"
IF DEFINED JAVA_HOME IF EXIST "%JAVA_HOME%\bin\java.exe" SET "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
"%JAVA_CMD%" -Dmaven.multiModuleProjectDirectory="%BASEDIR%" -classpath "%BASEDIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
