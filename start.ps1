param([switch]$NoBrowser)

$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

function Get-DockerEngineType {
    $ErrorActionPreference = 'Continue'
    $engine = & $dockerExe info --format '{{.OSType}}' 2>$null
    if ($LASTEXITCODE -eq 0) { return $engine }
}

try {
    $defaultInstall = Join-Path $env:ProgramFiles 'Docker\Docker'
    $dockerCommand = Get-Command docker.exe -CommandType Application -ErrorAction SilentlyContinue |
        Select-Object -First 1
    $dockerExe = if ($dockerCommand) {
        $dockerCommand.Source
    } else {
        Join-Path $defaultInstall 'resources\bin\docker.exe'
    }
    if (-not (Test-Path -LiteralPath $dockerExe -PathType Leaf)) {
        throw '找不到 Docker。请先安装 Docker Desktop 并完成首次设置。'
    }

    $engine = Get-DockerEngineType
    if (-not $engine) {
        $candidates = @(Join-Path $defaultInstall 'Docker Desktop.exe')
        $dir = Split-Path -Parent $dockerExe
        for ($i = 0; $i -lt 4 -and $dir; $i++) {
            $candidates += Join-Path $dir 'Docker Desktop.exe'
            $dir = Split-Path -Parent $dir
        }
        $desktop = $candidates | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } |
            Select-Object -First 1
        if (-not $desktop) {
            throw '未找到 Docker Desktop。请手动打开 Docker Desktop 后重试。'
        }

        Write-Host '正在启动 Docker Desktop，请稍候...'
        Start-Process -FilePath $desktop -WindowStyle Hidden
        $deadline = (Get-Date).AddMinutes(4)
        do {
            Start-Sleep -Seconds 2
            $engine = Get-DockerEngineType
        } until ($engine -or (Get-Date) -ge $deadline)

        if (-not $engine) {
            throw 'Docker Desktop 未就绪。请打开 Docker Desktop 查看提示后重试。'
        }
    }
    if ($engine -ne 'linux') {
        throw '请把 Docker Desktop 切换到 Linux 容器模式后重试。'
    }

    & $dockerExe compose version
    if ($LASTEXITCODE -ne 0) {
        throw '未找到 Docker Compose。请更新 Docker Desktop。'
    }

    Write-Host '正在启动 MySQL、后端和前端。首次下载镜像和依赖可能需要几分钟...'
    & $dockerExe compose up -d --build --wait --wait-timeout 240
    if ($LASTEXITCODE -ne 0) {
        throw '服务启动失败。请查看上方错误，或在项目目录运行 docker compose logs。'
    }

    $binding = & $dockerExe compose port frontend 80
    if ($LASTEXITCODE -ne 0 -or $binding -notmatch '^127\.0\.0\.1:[0-9]+$') {
        throw '无法读取前端访问地址。请在项目目录运行 docker compose ps 查看端口。'
    }
    $url = "http://$binding/"
    Write-Host "系统已就绪 $url"
    if (-not $NoBrowser) { Start-Process -FilePath $url }
    exit 0
} catch {
    Write-Host "启动失败 $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
