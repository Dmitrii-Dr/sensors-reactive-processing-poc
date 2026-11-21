<#
.SYNOPSIS
  Sends 10 UDP signals per second (alternating t1 / h1) with gradually growing values,
  so that they eventually exceed the alert limits (.env: TEMPERATURE_LIMIT=35, HUMIDITY_LIMIT=50).

.EXAMPLE
  .\scripts\send-signals.ps1
  .\scripts\send-signals.ps1 -DurationSec 30 -TempEnd 50
#>
param(
    [string]$TargetHost = "127.0.0.1",
    [int]$TempPort = 3355,
    [int]$HumidityPort = 3344,
    [string]$TempId = "t1",
    [string]$HumidityId = "h1",
    [int]$RatePerSec = 10,
    [int]$DurationSec = 10,
    [double]$TempStart = 20,
    [double]$TempEnd = 45,       # limit is 35
    [double]$HumidityStart = 30,
    [double]$HumidityEnd = 65    # limit is 50
)

$total = $RatePerSec * $DurationSec
$intervalMs = 1000.0 / $RatePerSec
$client = New-Object System.Net.Sockets.UdpClient
$sw = [System.Diagnostics.Stopwatch]::StartNew()

try {
    for ($i = 0; $i -lt $total; $i++) {
        $progress = if ($total -gt 1) { $i / ($total - 1) } else { 1 }
        $isTemp = ($i % 2 -eq 0)

        if ($isTemp) {
            $id = $TempId; $port = $TempPort
            $value = $TempStart + ($TempEnd - $TempStart) * $progress
        } else {
            $id = $HumidityId; $port = $HumidityPort
            $value = $HumidityStart + ($HumidityEnd - $HumidityStart) * $progress
        }

        $payload = "{0},{1}" -f $id, $value.ToString("0.0", [System.Globalization.CultureInfo]::InvariantCulture)
        $bytes = [System.Text.Encoding]::ASCII.GetBytes($payload)
        [void]$client.Send($bytes, $bytes.Length, $TargetHost, $port)
        Write-Host ("[{0,4}/{1}] -> :{2}  {3}" -f ($i + 1), $total, $port, $payload)

        # keep a steady rate: wait until the next tick
        $wait = ($i + 1) * $intervalMs - $sw.Elapsed.TotalMilliseconds
        if ($wait -gt 0) { Start-Sleep -Milliseconds ([int]$wait) }
    }
}
finally {
    $client.Close()
}

Write-Host "Done. Check central-monitoring-service logs for ALERT messages."
