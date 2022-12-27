# Script thats reflects the curren quarkus api to the angular frontend
cd "$PSScriptRoot"
$url = "http://localhost:8080/q/openapi"
$output = "openapi\service.yml"
$start_time = Get-Date

Invoke-WebRequest -Uri $url -OutFile $output
Write-Output "Time taken: $((Get-Date).Subtract($start_time).Seconds) second(s)"
node_modules\.bin\ng-openapi-gen --input openapi/service.yml --output src/app/api  --ignoreUnusedModels=false


# Replace Root ur
$file='src\app\api\api-configuration.ts'
$regex = "rootUrl: string = '';"
(Get-Content $file) -replace $regex, "rootUrl: string = environment.baseUrl;" | Set-Content $file

# Add Import as first line
$content = Get-Content -Path $file
$Output = @()
$Output += "import {environment} from '../../environments/environment';"
$Output += $content
$Output | Out-file -encoding UTF8 $file
