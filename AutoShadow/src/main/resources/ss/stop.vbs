DIM objShell
set objShell = createobject("wscript.shell")
Set wmi=GetObject("winmgmts:\\.") 
Set pro_s=wmi.instancesof("win32_process") 
For Each p In pro_s 
	If p.name="Shadowsocks.exe"  then 
	   objShell.run("%comspec% /c taskkill/im Shadowsocks.exe /f"),0,TRUE
	End If 
Next