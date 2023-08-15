cd bin
jar xf ..\libs\commons-cli*.jar || Goto Error
jar xf ..\libs\org.json*.jar || Goto Error
rmdir /S /Q META-INF || Goto Error
del VERSION
cd ..
Exit /B 0

:Error
	Exit /B %ERRORLEVEL%