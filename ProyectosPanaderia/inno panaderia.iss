#define MyAppName "Panadería FX"
#define MyAppVersion "1.0"
#define MyAppExeName "PanaderiaFX.exe"

[Setup]
AppId={{E5A3893E-D3AB-4875-A694-366EFEA0B48A}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
DefaultDirName={userdocs}\{#MyAppName}
DefaultGroupName={#MyAppName}
UninstallDisplayIcon={app}\{#MyAppExeName}
DisableProgramGroupPage=yes
OutputDir=C:\Excel
OutputBaseFilename=PanaderiaFX_Installer
SetupIconFile=C:\Excel\ProyectosPanaderia\icons\icon.ico
SolidCompression=yes
Compression=lzma2
InternalCompressLevel=max
WizardStyle=modern

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "C:\Excel\ProyectosPanaderia\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Excel\ProyectosPanaderia\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Iniciar {#MyAppName}"; Flags: nowait postinstall skipifsilent
