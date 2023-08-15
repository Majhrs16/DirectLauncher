jar cf DirectLauncher.jar -C bin . || Goto Error
Exit /B 0

:Error
	Exit /B %ERRORLEVEL%