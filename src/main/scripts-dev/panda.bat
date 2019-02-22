@echo off
set PANDA_HOME=%~dp0\..

java -cp %PANDA_HOME%/../classes;%PANDA_HOME%/lib/* -Dfile.encoding=UTF-8 com.justai.cm.CLI %*
