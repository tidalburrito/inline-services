@echo off
setlocal

for %%F in ("target\*.inline.*") do (
	echo starting %%F
	start java -jar %%F
)

endlocal

