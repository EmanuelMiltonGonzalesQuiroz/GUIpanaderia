; Script generado por el Asistente de Inno Setup.
; Incluye eliminación de carpeta destino antes de instalar

#define MyAppName "Panadería FX"
#define MyAppVersion "1.0"
#define MyAppExeName "PanaderiaFX.exe"

[Setup]
AppId={{E5A3893E-D3AB-4875-A694-366EFEA0B48A}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
DefaultDirName=C:\Panaderia
UninstallDisplayIcon={app}\{#MyAppExeName}
DisableProgramGroupPage=yes
OutputDir=C:\Excel
OutputBaseFilename=PanaderiaFX_Installer
SetupIconFile= C:\Excel\ProyectosPanaderia\icons\icon.ico
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Ejecutable compilado
Source: "C:\Excel\ProyectosPanaderia\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
; Todo el contenido portable
Source: "C:\Excel\ProyectosPanaderia\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssInstall then
  begin
    DelTree('C:\Panaderia', True, True, True);  // Borra carpeta destino sin preguntar
  end;
end;
