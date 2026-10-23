@echo off
setlocal
if not defined JAVA_HOME (
    if exist "C:\Users\viraj\.tools\jdk-21.0.5+11" (
        set "JAVA_HOME=C:\Users\viraj\.tools\jdk-21.0.5+11"
        set "PATH=C:\Users\viraj\.tools\jdk-21.0.5+11\bin;%PATH%"
    )
)
if exist "C:\Users\viraj\.tools\apache-maven-3.9.6\bin\mvn.cmd" (
    call "C:\Users\viraj\.tools\apache-maven-3.9.6\bin\mvn.cmd" %*
) else (
    mvn %*
)
endlocal
