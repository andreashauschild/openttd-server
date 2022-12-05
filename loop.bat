@echo off
set /a x=0
:loop

echo ##############################  %x%  #############################
set /a x+=1
ping -n 2 127.0.0.1 > nul
goto loop
